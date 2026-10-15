package net.mineskyevents.entities.events;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.managers.EventManager;
import net.mineskyevents.models.EventState;
import net.mineskyevents.utils.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseEvent {

    private static final Set<Integer> COUNTDOWN_NOTIFY_INTERVALS = Set.of(180, 120, 60, 30, 15, 10, 5, 4, 3, 2, 1);

    protected final Category category;
    protected final String mapId;
    protected final String eventName;

    protected Location lobbyLoc;
    protected Location spawnLoc;
    protected Location pos1Loc;
    protected Location pos2Loc;

    protected final Set<UUID> alivePlayers = ConcurrentHashMap.newKeySet();
    protected final Set<UUID> deadPlayers = ConcurrentHashMap.newKeySet();

    private Scheduler.Task timeoutTask;
    private Scheduler.Task countdownTask;

    public BaseEvent(Category category, String mapId, String eventName) {
        this.category = category;
        this.mapId = mapId;
        this.eventName = eventName;
    }

    protected abstract void onGameStart();
    protected abstract void onPlayerDeath(Player player);
    protected abstract void onCleanUp();

    public String getEventName() {
        return eventName;
    }

    public Location getPos1() {
        return pos1Loc;
    }

    public Location getPos2() {
        return pos2Loc;
    }

    public Location getLobby() {
        return lobbyLoc;
    }

    public Location getSpawn() {
        return spawnLoc;
    }

    public void eliminatePlayer(Player player) {
        if (alivePlayers.contains(player.getUniqueId())) {
            onPlayerDeath(player);
        }
    }

    public void startSignups() {
        YamlConfiguration config = category.getConfig();
        String path = "mapas." + mapId;

        this.lobbyLoc = loadLocation(config, path + ".lobby");
        this.spawnLoc = loadLocation(config, path + ".spawn");
        this.pos1Loc = loadLocation(config, path + ".pos1");
        this.pos2Loc = loadLocation(config, path + ".pos2");

        if (lobbyLoc == null || spawnLoc == null || pos1Loc == null || pos2Loc == null) {
            Bukkit.getLogger().warning("[" + eventName + "] Lobby, Spawn ou limites de região não configurados para o mapa " + mapId);
            return;
        }

        EventManager manager = MineSkyEvents.getInstance().getEventManager();
        manager.reset();
        manager.setActiveEvent(this);
        manager.setState(EventState.WAITING);

        Bukkit.broadcastMessage("§b§l" + eventName + " §8| §aAnunciado no mapa §l" + config.getString(path + ".name", mapId) + "§a!");
        Bukkit.broadcastMessage("§cISSO É APENAS UM TESTE PRIVADO!");

        timeoutTask = Scheduler.runTaskTimer(this::cancelDueToInsufficientPlayers, 12000L, 20L);
    }

    public void checkStart() {
        EventManager manager = MineSkyEvents.getInstance().getEventManager();
        if (manager.getState() == EventState.WAITING && manager.getActivePlayers().size() >= 4) {
            if (timeoutTask != null) {
                timeoutTask.cancel();
            }
            startCountdown();
        }
    }

    private void startCountdown() {
        countdownTask = Scheduler.runTaskTimer(new Runnable() {
            private int secondsRemaining = 180;

            @Override
            public void run() {
                if (secondsRemaining <= 0) {
                    finishCountdown();
                } else {
                    if (COUNTDOWN_NOTIFY_INTERVALS.contains(secondsRemaining)) {
                        notifyCountdown(secondsRemaining);
                    }
                    secondsRemaining--;
                }
            }
        }, 0L, 20L);
    }

    private void notifyCountdown(int seconds) {
        EventManager manager = MineSkyEvents.getInstance().getEventManager();
        for (UUID uuid : manager.getActivePlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                Scheduler.runTaskAtEntity(p, () -> {
                    p.sendTitle("§8[§b" + eventName + "§8]", "§7INICIANDO EM§8: §b" + seconds + "s", 5, 20, 5);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO, 1.0f, 1.0f);
                });
            }
        }
        if (seconds == 5) {
            teleportAll(spawnLoc);
        }
    }

    private void finishCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
        }
        EventManager manager = MineSkyEvents.getInstance().getEventManager();
        manager.setState(EventState.RUNNING);

        alivePlayers.addAll(manager.getActivePlayers());

        onGameStart();
    }

    public void teleportAll(Location loc) {
        EventManager manager = MineSkyEvents.getInstance().getEventManager();
        for (UUID uuid : manager.getActivePlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                Scheduler.runTaskAtEntity(p, () -> p.teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND));
            }
        }
    }

    private void cancelDueToInsufficientPlayers() {
        if (timeoutTask != null) {
            timeoutTask.cancel();
        }
        Bukkit.broadcastMessage("§b§l" + eventName + " §8| §aInfelizmente o evento não teve players suficientes para iniciar.");
        resetEvent();
    }

    protected void resetEvent() {
        if (timeoutTask != null) {
            timeoutTask.cancel();
        }
        if (countdownTask != null) {
            countdownTask.cancel();
        }
        teleportAll(Bukkit.getWorlds().get(0).getSpawnLocation());
        onCleanUp();
        MineSkyEvents.getInstance().getEventManager().reset();
    }

    private Location loadLocation(YamlConfiguration config, String path) {
        if (!config.contains(path)) {
            return null;
        }
        World world = Bukkit.getWorld(config.getString(path + ".world", "world"));
        if (world == null) {
            return null;
        }
        return new Location(world,
                config.getDouble(path + ".x"),
                config.getDouble(path + ".y"),
                config.getDouble(path + ".z"),
                (float) config.getDouble(path + ".yaw"),
                (float) config.getDouble(path + ".pitch")
        );
    }
}