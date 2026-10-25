package net.mineskyevents;

import net.mineskyevents.chat.GlobalChatBridge;
import net.mineskyevents.commands.EventCommand;
import net.mineskyevents.commands.MSECommand;
import net.mineskyevents.entities.categories.CategoryHandler;
import net.mineskyevents.gui.CategoryGuiListener;
import net.mineskyevents.gui.EventGui;
import net.mineskyevents.commands.LeaveCommand;
import net.mineskyevents.listeners.CommandBlockerListener;
import net.mineskyevents.listeners.GameplayListener;
import net.mineskyevents.listeners.PlayerConnectionListener;
import net.mineskyevents.managers.EventBanManager;
import net.mineskyevents.managers.EventManager;
import net.mineskyevents.managers.EventScoreboardManager;
import net.mineskyevents.managers.ScheduleManager;
import net.mineskyevents.proxy.ProxyService;
import net.mineskyevents.reward.RewardService;
import net.mineskyevents.utils.Messages;
import net.mineskyevents.utils.Scheduler;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public final class MineSkyEvents extends JavaPlugin {

    public static Logger l;

    private EventManager eventManager;
    private EventScoreboardManager scoreboardManager;
    private ScheduleManager scheduleManager;
    private CommandBlockerListener commandBlockerListener;
    private ProxyService proxyService;
    private GlobalChatBridge globalChatBridge;
    private RewardService rewardService;
    private EventBanManager banManager;
    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        l = getLogger();
        logBanner();

        saveDefaultConfig();
        saveResource("categories.yml", false);
        saveResource("messages.yml", false);

        messagesConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        try (var reader = new InputStreamReader(getResource("messages.yml"), StandardCharsets.UTF_8)) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(reader);
            messagesConfig.setDefaults(defaults);
            messagesConfig.options().copyDefaults(true);
        } catch (Exception e) {
            l.warning("Não foi possível carregar os padrões de messages.yml: " + e.getMessage());
        }
        Messages.init(messagesConfig);

        Scheduler.init(this);

        CategoryHandler.setupCategories();

        eventManager = new EventManager();
        banManager = new EventBanManager(this);
        scoreboardManager = new EventScoreboardManager();
        proxyService = new ProxyService(this);
        proxyService.register();
        Scheduler.runTaskLater(proxyService::register, 100L);

        globalChatBridge = new GlobalChatBridge(this);
        globalChatBridge.register();
        Scheduler.runTaskLater(globalChatBridge::register, 100L);
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
        if (globalChatBridge != null) globalChatBridge.unregister();
        l.info("MineSkyEvents desabilitado.");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(), this);
        getServer().getPluginManager().registerEvents(new GameplayListener(), this);
        getServer().getPluginManager().registerEvents(new net.mineskyevents.listeners.MiniWarsCannonListener(), this);
        getServer().getPluginManager().registerEvents(new net.mineskyevents.listeners.SpectatorListener(), this);
        getServer().getPluginManager().registerEvents(new net.mineskyevents.listeners.GlobalChatListener(), this);
        getServer().getPluginManager().registerEvents(new CategoryGuiListener(), this);
        getServer().getPluginManager().registerEvents(new net.mineskyevents.gui.miniwars.MiniWarsTeamGuiListener(), this);
        commandBlockerListener = new CommandBlockerListener();
        commandBlockerListener.reload();
        getServer().getPluginManager().registerEvents(commandBlockerListener, this);
    }

    private void registerCommands() {
        getCommand("event").setExecutor(new EventCommand(this));
        getCommand("event").setTabCompleter(new net.mineskyevents.commands.tabcompleter.EventCommandTabCompleter());

        getCommand("sair").setExecutor(new LeaveCommand());
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
    public EventScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public ProxyService getProxyService() { return proxyService; }
    public GlobalChatBridge getGlobalChatBridge() { return globalChatBridge; }
    public RewardService getRewardService() { return rewardService; }
    public FileConfiguration getMessagesConfig() { return messagesConfig; }
    public EventBanManager getBanManager() { return banManager; }

    public static MineSkyEvents getInstance() {
        return getPlugin(MineSkyEvents.class);
    }
}
