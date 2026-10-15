package net.mineskyevents;

import com.mongodb.client.MongoCollection;
import net.mineskyevents.commands.EventCommand;
import net.mineskyevents.commands.MSECommand;
import net.mineskyevents.database.MongoDBManager;
import net.mineskyevents.entities.categories.CategoryHandler;
import net.mineskyevents.gui.CategoryGuiListener;
import net.mineskyevents.gui.EventGui;
import net.mineskyevents.listeners.EventListener;
import net.mineskyevents.listeners.PlayerEvents;
import net.mineskyevents.managers.EventManager;
import net.mineskyevents.utils.Scheduler;
import org.bson.Document;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class MineSkyEvents extends JavaPlugin {
    public static Logger l;

    public MongoDBManager mm;
    public MongoCollection<Document> coll;

    private EventManager eventManager;

    @Override
    public void onEnable() {
        l = this.getLogger();
        l.info("    __  ___    _                           __               ______                        __         ");
        l.info("   /  |/  /   (_)   ____   ___    _____   / /__   __  __   / ____/ _   __  ___    ____   / /_   _____");
        l.info("  / /|_/ /   / /   / __ \\ / _ \\  / ___/  / //_/  / / / /  / __/   | | / / / _ \\  / __ \\ / __/  / ___/");
        l.info(" / /  / /   / /   / / / //  __/ (__  )  / ,<    / /_/ /  / /___   | |/ / /  __/ / / / // /_   (__  )");
        l.info("/_/  /_/   /_/   /_/ /_/ \\___/ /____/  /_/|_|   \\__, /  /_____/   |___/  \\___/ /_/ /_/ \\__/  /____/  ");
        l.info("                                               /____/");
        l.info("");
        l.info("Carregando plugin...");
        Scheduler.init(this);
        //loadMongoDB();
        CategoryHandler.setupCategories();
        registerCommands();
        registerEvents();
        GUISLoad();
        this.eventManager = new EventManager();
        getServer().getMessenger().registerOutgoingPluginChannel(this, "minesky:proxy");
        l.info("Plugin habilitado com sucesso!");
    }

    @Override
    public void onDisable() {
        l.info("GoodBye! Shutting down");
        mm.close();
    }

    private void registerEvents() {
        l.info("Registering event listeners...");
        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
        getServer().getPluginManager().registerEvents(new CategoryGuiListener(), this);
        getServer().getPluginManager().registerEvents(new EventListener(), this);
    }

    private void registerCommands() {
        l.info("Registering plugin commands...");
        this.getCommand("event").setExecutor(new EventCommand(this));
        this.getCommand("mse").setExecutor(new MSECommand());
    }

    private void GUISLoad() {
        l.info("Carregando todos os menus");
        EventGui.loadAll();
    }

    public void loadMongoDB() {
        if (mm != null) {
            mm.close();
        }
        l.info("Connecting MongoDB...");

        mm = new MongoDBManager();
        mm.connect("mongodb+srv://zbruno:YJHIhrG740G5tChT@me21db.jecbxit.mongodb.net/", "events");
        //mm.connect(Config.MongoUri, Config.StorageDatabase);
        coll = mm.getEventos();
    }

    public EventManager getEventManager() {
        return this.eventManager;
    }

    public static MineSkyEvents getInstance() {
        return MineSkyEvents.getPlugin(MineSkyEvents.class);
    }
}
