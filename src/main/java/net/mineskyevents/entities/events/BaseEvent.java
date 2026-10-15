package net.mineskyevents.entities.events;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.map.MapManager;
import net.mineskyevents.managers.EventManager;
import net.mineskyevents.models.EventState;
import net.mineskyevents.models.EventType;
import net.mineskyevents.reward.RewardService;
import net.mineskyevents.utils.Messages;
import net.mineskyevents.utils.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseEvent {

    /** Eliminação ao cair no void (Spleef, Sumo, TNT Run). */
    public boolean eliminatesOnVoid() { return false; }

    /** PvP vanilla entre jogadores (Mini Wars, etc.). */
    public boolean allowsPlayerVsPlayer() { return false; }

    /** Morte vanilla do Minecraft; se false, morte é cancelada e o jogador é eliminado/recuperado. */
    public boolean allowsVanillaDeath() { return false; }

    /** Survival para quebrar blocos permitidos (ex.: neve no spleef) ou bater no sumo. */
    public boolean usesSurvivalMode() { return false; }

    /** Blocos que um jogador em survival pode quebrar durante o evento. */
    public boolean canBreak(Material material) { return false; }

    public boolean showsRemainingPlayers() { return true; }

    public String formatPersonalScore(Player viewer) { return null; }

    public List<String> extraScoreboardLines(Player viewer) { return List.of(); }

    public Location getRecoveryLocation(Player player) {
        return spawnLoc != null ? spawnLoc : lobbyLoc;
    }

    private static final Set<Integer> COUNTDOWN_NOTIFY = Set.of(60, 30, 15, 10, 5, 4, 3, 2, 1);

    protected final EventType eventType;
    protected final Category category;
    protected final String mapId;

    protected Location lobbyLoc;
    protected Location spawnLoc;
    protected Location pos1Loc;
    protected Location pos2Loc;
    protected List<Location> checkpoints = new ArrayList<>();

    protected final Set<UUID> alivePlayers = ConcurrentHashMap.newKeySet();
    protected final Set<UUID> deadPlayers = ConcurrentHashMap.newKeySet();

    private Scheduler.Task timeoutTask;
    private Scheduler.Task countdownTask;

    public BaseEvent(EventType eventType, Category category, String mapId) {
        this.eventType = eventType;
        this.category = category;
        this.mapId = mapId;
    }

    protected abstract void onGameStart();
    protected abstract void onPlayerEliminated(Player player);
    protected abstract void onCleanUp();

    public EventType getEventType() { return eventType; }
    public String getMapId() { return mapId; }
    public String getEventName() { return eventType.getDisplayName(); }
    public Location getPos1() { return pos1Loc; }
    public Location getPos2() { return pos2Loc; }
    public Location getLobby() { return lobbyLoc; }
    public Location getSpawn() { return spawnLoc; }
    public List<Location> getCheckpoints() { return checkpoints; }
    public Set<UUID> getAlivePlayers() { return Collections.unmodifiableSet(alivePlayers); }

    public void eliminatePlayer(Player player) {
        if (!alivePlayers.contains(player.getUniqueId())) return;
        alivePlayers.remove(player.getUniqueId());
        deadPlayers.add(player.getUniqueId());
        setSpectator(player);
        onPlayerEliminated(player);
        checkWinCondition();
    }

    protected void checkWinCondition() {
        if (alivePlayers.size() <= 1 && getEventManager().getState() == EventState.RUNNING) {
            Player winner = alivePlayers.isEmpty() ? null : Bukkit.getPlayer(alivePlayers.iterator().next());
            finishEvent(winner != null ? List.of(winner) : List.of());
        }
    }

    public void finishEvent(List<Player> winners) {
        if (getEventManager().getState() == EventState.INACTIVE) return;

        int playerCount = getEventManager().getActivePlayers().size();
        RewardService rewards = MineSkyEvents.getInstance().getRewardService();

        if (!winners.isEmpty()) {
            rewards.rewardPlacements(winners, eventType, playerCount);
        }

        MineSkyEvents.getInstance().getProxyService().sendEventEnded(eventType.getId(), mapId);
        resetEvent();
    }

    public void startSignups() {
        YamlConfiguration config = category.getConfig();
        String path = "mapas." + mapId;

        lobbyLoc = MapManager.loadLocation(config, path + ".lobby");
        spawnLoc = MapManager.loadLocation(config, path + ".spawn");
        pos1Loc = MapManager.loadLocation(config, path + ".pos1");
        pos2Loc = MapManager.loadLocation(config, path + ".pos2");
        checkpoints = MapManager.loadCheckpoints(config, mapId);

        if (lobbyLoc == null || spawnLoc == null) {
            Bukkit.getLogger().warning("[" + eventName() + "] Lobby ou spawn não configurados para o mapa " + mapId);
            return;
        }

        EventManager manager = getEventManager();
        manager.reset();
        manager.setActiveEvent(this);
        manager.setState(EventState.WAITING);

        String mapName = MapManager.getMapDisplayName(category, mapId);
        Messages.broadcast("event.announced",
                Placeholder.parsed("event", eventType.getDisplayName()),
                Placeholder.parsed("map", mapName));
        Messages.broadcast("event.description",
                Placeholder.parsed("description", eventType.getDescription()));

        Messages.playToEventPlayers(Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f);

        int timeout = MineSkyEvents.getInstance().getConfig().getInt("signup-timeout-seconds", 600);
        timeoutTask = Scheduler.runTaskLater(this::cancelDueToInsufficientPlayers, timeout * 20L);

        MineSkyEvents.getInstance().getProxyService().sendEventStarted(
                eventType.getId(), mapId, 0);
        MineSkyEvents.getInstance().getScoreboardManager().attach(this);
    }

    public void forceStart() {
        if (timeoutTask != null) timeoutTask.cancel();
        EventState state = getEventManager().getState();
        if (state != EventState.WAITING && state != EventState.STARTING) return;
        if (countdownTask != null) countdownTask.cancel();
        finishCountdown();
    }

    public void joinInProgress(Player player) {
        EventManager manager = getEventManager();
        manager.addPlayer(player);
        alivePlayers.add(player.getUniqueId());
        player.getInventory().clear();
        Location dest = spawnLoc != null ? spawnLoc : lobbyLoc;
        if (dest != null) {
            player.teleport(dest, PlayerTeleportEvent.TeleportCause.PLUGIN);
            player.setFallDistance(0f);
        }
        onLateJoin(player);
        Messages.send(player, "event.joined-late",
                Placeholder.parsed("event", getEventName()));
    }

    protected void onLateJoin(Player player) {
        giveKit(player);
    }

    public void staffLeave(Player player) {
        boolean competing = alivePlayers.remove(player.getUniqueId());
        getEventManager().removePlayer(player);
        getEventManager().removeSpectator(player);
        player.getInventory().clear();
        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlight(true);
        player.teleport(getStaffExitLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        if (competing && getEventManager().getState() == EventState.RUNNING) {
            checkWinCondition();
        }
        Messages.send(player, "admin.left-arena");
    }

    public void kickFromEvent(Player target, String reason) {
        removeFromMatch(target);
        String why = reason == null || reason.isBlank() ? "Sem motivo" : reason;
        Messages.send(target, "event.kicked",
                Placeholder.component("reason", net.kyori.adventure.text.Component.text(why)));
        MineSkyEvents.getInstance().getProxyService().sendPlayerToHub(target);
    }

    public void removeFromMatch(Player target) {
        boolean competing = alivePlayers.remove(target.getUniqueId());
        getEventManager().removePlayer(target);
        getEventManager().removeSpectator(target);
        target.getInventory().clear();
        target.setGameMode(GameMode.ADVENTURE);
        if (competing && getEventManager().getState() == EventState.RUNNING) {
            checkWinCondition();
        }
    }

    public Location getStaffExitLocation() {
        if (lobbyLoc != null && !isInsideArena(lobbyLoc)) {
            return lobbyLoc.clone();
        }
        if (pos1Loc != null && pos2Loc != null && pos1Loc.getWorld() != null) {
            double minX = Math.min(pos1Loc.getX(), pos2Loc.getX());
            double y = Math.max(pos1Loc.getY(), pos2Loc.getY()) + 1;
            double z = (pos1Loc.getZ() + pos2Loc.getZ()) / 2.0;
            return new Location(pos1Loc.getWorld(), minX - 4, y, z);
        }
        return Bukkit.getWorlds().get(0).getSpawnLocation();
    }

    public void checkStart() {
        EventManager manager = getEventManager();
        int minPlayers = MineSkyEvents.getInstance().getConfig().getInt("min-players", 2);
        if (manager.getState() == EventState.WAITING && manager.getActivePlayers().size() >= minPlayers) {
            if (timeoutTask != null) timeoutTask.cancel();
            startCountdown();
        }
    }

    private void startCountdown() {
        getEventManager().setState(EventState.STARTING);
        int countdownSeconds = MineSkyEvents.getInstance().getConfig().getInt("countdown-seconds", 60);

        countdownTask = Scheduler.runTaskTimer(new Runnable() {
            private int remaining = countdownSeconds;

            @Override
            public void run() {
                if (remaining <= 0) {
                    finishCountdown();
                } else {
                    if (COUNTDOWN_NOTIFY.contains(remaining)) {
                        notifyCountdown(remaining);
                    }
                    remaining--;
                }
            }
        }, 0L, 20L);
    }

    private void notifyCountdown(int seconds) {
        for (UUID uuid : getEventManager().getActivePlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            Messages.title(p,
                    "<gradient:#00d4ff:#7b2ff7><bold>" + eventType.getDisplayName() + "</bold></gradient>",
                    "<gradient:#fbbf24:#f97316>Iniciando em <white><bold>" + seconds + "s</bold></white>...</gradient>"
            );
            Messages.playSound(p, Sound.BLOCK_NOTE_BLOCK_BANJO, 1.0f);
        }
        if (seconds == 5) teleportAll(spawnLoc);
    }

    private void finishCountdown() {
        if (countdownTask != null) countdownTask.cancel();
        if (getEventManager().getState() == EventState.RUNNING) return;

        getEventManager().setState(EventState.RUNNING);
        alivePlayers.clear();
        alivePlayers.addAll(getEventManager().getActivePlayers());

        teleportAll(spawnLoc);
        Messages.broadcast("event.started");
        Messages.playToEventPlayers(Sound.BLOCK_NOTE_BLOCK_PLING, 1.2f);

        onGameStart();
    }

    public void teleportAll(Location loc) {
        for (UUID uuid : getEventManager().getActivePlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline() && loc != null) {
                p.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                p.setFallDistance(0f);
            }
        }
    }

    protected void setSpectator(Player player) {
        player.getInventory().clear();
        player.setGameMode(GameMode.SPECTATOR);
        player.setAllowFlight(true);
        player.setFlying(true);
        getEventManager().addSpectator(player);
    }

    protected void giveKit(Player player) {
        player.getInventory().clear();
        player.setGameMode(usesSurvivalMode() ? GameMode.SURVIVAL : GameMode.ADVENTURE);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20f);
    }

    private void cancelDueToInsufficientPlayers() {
        Messages.broadcast("event.cancelled");
        MineSkyEvents.getInstance().getProxyService().sendEventCancelled(eventType.getId(), "insufficient_players");
        resetEvent();
    }

    public void resetEvent() {
        if (timeoutTask != null) timeoutTask.cancel();
        if (countdownTask != null) countdownTask.cancel();

        World spawnWorld = Bukkit.getWorlds().get(0);
        Location hub = spawnWorld.getSpawnLocation();

        for (UUID uuid : getEventManager().getActivePlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.getInventory().clear();
                p.setGameMode(GameMode.ADVENTURE);
                p.teleport(hub, PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
        }
        for (UUID uuid : getEventManager().getSpectators()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.getInventory().clear();
                p.setGameMode(GameMode.ADVENTURE);
                p.teleport(hub, PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
        }

        onCleanUp();
        MineSkyEvents.getInstance().getScoreboardManager().detach();
        getEventManager().reset();
    }

    protected boolean isInsideArena(Location loc) {
        if (pos1Loc == null || pos2Loc == null) return true;
        double minX = Math.min(pos1Loc.getX(), pos2Loc.getX());
        double maxX = Math.max(pos1Loc.getX(), pos2Loc.getX());
        double minY = Math.min(pos1Loc.getY(), pos2Loc.getY());
        double maxY = Math.max(pos1Loc.getY(), pos2Loc.getY());
        double minZ = Math.min(pos1Loc.getZ(), pos2Loc.getZ());
        double maxZ = Math.max(pos1Loc.getZ(), pos2Loc.getZ());
        return loc.getX() >= minX && loc.getX() <= maxX
                && loc.getY() >= minY && loc.getY() <= maxY
                && loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }

    public double getArenaMinY() {
        if (pos1Loc == null || pos2Loc == null) return 0;
        return Math.min(pos1Loc.getY(), pos2Loc.getY());
    }

    protected EventManager getEventManager() {
        return MineSkyEvents.getInstance().getEventManager();
    }

    private String eventName() {
        return eventType.getDisplayName();
    }
}
