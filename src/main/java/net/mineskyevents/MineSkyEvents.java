package net.mineskyevents;

import net.mineskyevents.commands.EventCommand;
import net.mineskyevents.commands.MSECommand;
import net.mineskyevents.entities.categories.CategoryHandler;
import net.mineskyevents.gui.CategoryGuiListener;
import net.mineskyevents.gui.EventGui;
import net.mineskyevents.listeners.GameplayListener;
import net.mineskyevents.listeners.PlayerConnectionListener;
import net.mineskyevents.managers.EventManager;
import net.mineskyevents.managers.ScheduleManager;
import net.mineskyevents.proxy.ProxyService;
import net.mineskyevents.reward.RewardService;
import net.mineskyevents.utils.Messages;
import net.mineskyevents.utils.Scheduler;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public final class MineSkyEvents extends JavaPlugin {

    public static Logger l;

    private EventManager eventManager;
    private ScheduleManager scheduleManager;
    private ProxyService proxyService;
    private RewardService rewardService;
    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        l = getLogger();
        logBanner();

        saveDefaultConfig();
        saveResource("categories.yml", false);
        saveResource("messages.yml", false);

        messagesConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        Messages.init(messagesConfig);

        Scheduler.init(this);

        CategoryHandler.setupCategories();

        eventManager = new EventManager();
        proxyService = new ProxyService(this);
        proxyService.register();
        rewardService = new RewardService(proxyService);

        scheduleManager = new ScheduleManager(this, eventManager);
        scheduleManager.start();

        registerCommands();
        registerListeners();
        EventGui.loadAll();

        l.info("MineSkyEvents habilitado com sucesso! (Paper 26.2)");
    }

    @Override
    public void onDisable() {
        if (scheduleManager != null) scheduleManager.stop();
        if (eventManager != null && eventManager.hasActiveEvent()) {
            eventManager.forceStop();
        }
        if (proxyService != null) proxyService.unregister();
        l.info("MineSkyEvents desabilitado.");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(), this);
        getServer().getPluginManager().registerEvents(new GameplayListener(), this);
        getServer().getPluginManager().registerEvents(new CategoryGuiListener(), this);
    }

    private void registerCommands() {
        getCommand("evento").setExecutor(new EventCommand(this));
        getCommand("evento").setTabCompleter(new net.mineskyevents.commands.tabcompleter.EventCommandTabCompleter());
        getCommand("mse").setExecutor(new MSECommand());
    }

    private void logBanner() {
        l.info("  __  __ _             ____  _        ");
        l.info(" |  \\/  (_)_ __   ___ / ___|| | _____ ");
        l.info(" | |\\/| | | '_ \\ / _ \\\\___ \\| |/ / _ \\");
        l.info(" | |  | | | | | |  __/ ___) |   <  __/");
        l.info(" |_|  |_|_|_| |_|\\___||____/|_|\\_\\___|");
        l.info(" Servidor dedicado de eventos — Paper 26.2");
    }

    public EventManager getEventManager() { return eventManager; }
    public ProxyService getProxyService() { return proxyService; }
    public RewardService getRewardService() { return rewardService; }
    public FileConfiguration getMessagesConfig() { return messagesConfig; }

    public static MineSkyEvents getInstance() {
        return getPlugin(MineSkyEvents.class);
    }
}
