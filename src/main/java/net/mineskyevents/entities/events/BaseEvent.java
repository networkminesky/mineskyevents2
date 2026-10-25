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
import net.mineskyevents.utils.EventCelebration;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.HeightMap;
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

    /** Onde o jogador vai ao usar /event entrar durante as inscrições. */
    public Location getJoinLocation() {
        return lobbyLoc;
    }

    protected boolean teleportsPlayersToSpawnOnCountdown() {
        return true;
    }

    protected boolean teleportsPlayersToSpawnOnStart() {
        return true;
    }

    protected void onSignupsStarted() {}

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

    private static final long END_ANNOUNCE_DELAY_TICKS = 80L;
    private static final long END_DEPARTURE_DELAY_TICKS = 200L;

    private Scheduler.Task timeoutTask;
    private Scheduler.Task countdownTask;
    private Scheduler.Task endAnnounceTask;
    private Scheduler.Task endShutdownTask;
    private int finaleParticipantCount;
    private boolean silent;

    public boolean isSilent() { return silent; }

    public void setSilent(boolean silent) { this.silent = silent; }

    public BaseEvent(EventType eventType, Category category, String mapId) {
        this.eventType = eventType;
        this.category = category;
        this.mapId = mapId;
    }

    protected abstract void onGameStart();
    protected abstract void onCleanUp();

    protected void onPlayerEliminated(Player player, Player killer, Location deathLocation) {}

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
        eliminatePlayer(player, null);
    }

    public void eliminatePlayer(Player player, Player killer) {
        if (!alivePlayers.contains(player.getUniqueId())) return;
        alivePlayers.remove(player.getUniqueId());
        deadPlayers.add(player.getUniqueId());
        Location deathLocation = player.getLocation().clone();
        setSpectator(player);
        onPlayerEliminated(player, killer, deathLocation);
        checkWinCondition();
    }

    protected void checkWinCondition() {
        if (alivePlayers.size() <= 1 && getEventManager().getState() == EventState.RUNNING) {
            Player winner = alivePlayers.isEmpty() ? null : Bukkit.getPlayer(alivePlayers.iterator().next());
            finishEvent(winner != null ? List.of(winner) : List.of());
        }
    }

    public void finishEvent(List<Player> winners) {
        EventManager manager = getEventManager();
        if (manager.getState() == EventState.INACTIVE || manager.getState() == EventState.ENDING) return;

        manager.setState(EventState.ENDING);
        cancelEndCeremonyTasks();
        finaleParticipantCount = manager.getActivePlayers().size() + manager.getSpectators().size();
        spectateEveryoneForFinale();

        spawnVictoryFireworks(winners);

        endAnnounceTask = Scheduler.runTaskLater(() -> announceVictory(winners), END_ANNOUNCE_DELAY_TICKS);
        endShutdownTask = Scheduler.runTaskLater(() -> completeEventShutdown(winners), END_DEPARTURE_DELAY_TICKS);
    }

    public void cancelEndCeremony() {
        cancelEndCeremonyTasks();
    }

    private void cancelEndCeremonyTasks() {
        if (endAnnounceTask != null) {
            endAnnounceTask.cancel();
            endAnnounceTask = null;
        }
        if (endShutdownTask != null) {
            endShutdownTask.cancel();
            endShutdownTask = null;
        }
    }

    private void spectateEveryoneForFinale() {
        Set<UUID> participants = new HashSet<>();
        participants.addAll(getEventManager().getActivePlayers());
        participants.addAll(getEventManager().getSpectators());
        participants.addAll(alivePlayers);

        for (UUID uuid : participants) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            if (getEventManager().isSpectating(player)) continue;
            setSpectator(player);
        }
        for (UUID uuid : new HashSet<>(alivePlayers)) {
            alivePlayers.remove(uuid);
        }
    }

    private void completeEventShutdown(List<Player> winners) {
        if (getEventManager().getState() != EventState.ENDING) return;

        if (!silent && !winners.isEmpty()) {
            MineSkyEvents.getInstance().getRewardService().grantPlacements(winners, eventType, finaleParticipantCount);
        }

        if (!silent) {
            MineSkyEvents.getInstance().getProxyService().sendEventEnded(eventType.getId(), mapId);
        }
        resetEvent();
    }

    /** Fogos ao encerrar (antes do anúncio no chat). */
    protected void spawnVictoryFireworks(List<Player> winners) {
        if (winners.isEmpty() || winners.get(0) == null) return;
        EventCelebration.spawnFinaleFireworks(this, Color.YELLOW, Color.ORANGE);
    }

    /** Anúncio de vitória após os fogos. */
    protected void announceVictory(List<Player> winners) {
        if (winners.isEmpty() || winners.get(0) == null) return;

        Messages.broadcastEventAudience("event.winner", Map.of(
                "event", eventType.getDisplayName(),
                "player", winners.get(0).getName()
        ));
        Messages.playToEventAudience(Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f);
    }

    public World getCelebrationWorld() {
        if (pos1Loc != null && pos1Loc.getWorld() != null) return pos1Loc.getWorld();
        if (spawnLoc != null && spawnLoc.getWorld() != null) return spawnLoc.getWorld();
        if (lobbyLoc != null && lobbyLoc.getWorld() != null) return lobbyLoc.getWorld();
        return Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
    }

    public Location randomCelebrationLocation(java.util.random.RandomGenerator random) {
        World world = getCelebrationWorld();
        if (world == null) return null;

        if (pos1Loc != null && pos2Loc != null) {
            double minX = Math.min(pos1Loc.getX(), pos2Loc.getX());
            double maxX = Math.max(pos1Loc.getX(), pos2Loc.getX());
            double minZ = Math.min(pos1Loc.getZ(), pos2Loc.getZ());
            double maxZ = Math.max(pos1Loc.getZ(), pos2Loc.getZ());
            double x = minX + random.nextDouble() * (maxX - minX);
            double z = minZ + random.nextDouble() * (maxZ - minZ);
            double y = fireworkSpawnY(world, x, z, random);
            return new Location(world, x, y, z);
        }

        Location center = spawnLoc != null ? spawnLoc : lobbyLoc;
        if (center == null) {
            Location spawn = world.getSpawnLocation();
            return new Location(world, spawn.getX(), fireworkSpawnY(world, spawn.getX(), spawn.getZ(), random), spawn.getZ());
        }
        double x = center.getX() + random.nextDouble(-12, 12);
        double z = center.getZ() + random.nextDouble(-12, 12);
        double y = fireworkSpawnY(world, x, z, random);
        return new Location(world, x, y, z);
    }

    /** Pouco acima do chão real (evita usar o topo da região WorldEdit). */
    private static double fireworkSpawnY(World world, double x, double z, java.util.random.RandomGenerator random) {
        int ground = world.getHighestBlockYAt((int) Math.floor(x), (int) Math.floor(z), HeightMap.MOTION_BLOCKING_NO_LEAVES);
        return ground + 0.55 + random.nextDouble(0, 0.85);
    }

    public boolean startSignups() {
        YamlConfiguration config = category.getConfig();
        String path = "mapas." + mapId;

        lobbyLoc = MapManager.loadLocation(config, path + ".lobby");
        spawnLoc = MapManager.loadLocation(config, path + ".spawn");
        pos1Loc = MapManager.loadLocation(config, path + ".pos1");
        pos2Loc = MapManager.loadLocation(config, path + ".pos2");
        checkpoints = MapManager.loadCheckpoints(config, mapId);

        if (lobbyLoc == null || spawnLoc == null) {
            String missing = lobbyLoc == null && spawnLoc == null ? "lobby e spawn"
                    : lobbyLoc == null ? "lobby" : "spawn";
            MineSkyEvents.l.warning("[" + eventName() + "] Ponto(s) ausente(s) (" + missing + ") no mapa "
                    + mapId + " — configure com /event mapa ou confira o nome do mundo.");
            return false;
        }

        EventManager manager = getEventManager();
        manager.reset();
        manager.setActiveEvent(this);
        manager.setState(EventState.WAITING);

        if (!silent) {
            String mapName = MapManager.getMapDisplayName(category, mapId);
            Messages.broadcast("event.announced",
                    Placeholder.parsed("event", eventType.getDisplayName()),
                    Placeholder.parsed("map", mapName));
            Messages.broadcast("event.description",
                    Placeholder.parsed("description", eventType.getDescription()));
            Messages.playToEventPlayers(Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f);
        }

        int timeout = MineSkyEvents.getInstance().getConfig().getInt("signup-timeout-seconds", 600);
        timeoutTask = Scheduler.runTaskLater(this::cancelDueToInsufficientPlayers, timeout * 20L);

        if (!silent) {
            MineSkyEvents.getInstance().getProxyService().sendEventStarted(
                    eventType.getId(), mapId, 0);
        } else {
            notifySilentSignupsOpen();
        }
        MineSkyEvents.getInstance().getScoreboardManager().attach(this);
        onSignupsStarted();
        return true;
    }

    private void notifySilentSignupsOpen() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("mineskyevents.join")) {
                notifySignupStatus(player);
            }
        }
    }

    /** Aviso privado (não broadcast) para quem entra ou já está online durante inscrições. */
    public void notifySignupStatus(Player player) {
        String mapLabel = MapManager.getMapDisplayName(category, mapId);
        String key = silent ? "event.silent-signups" : "event.announced";
        Messages.send(player, key,
                Placeholder.parsed("event", getEventName()),
                Placeholder.parsed("map", mapLabel != null ? mapLabel : mapId));
    }

    public void forceStart() {
        if (timeoutTask != null) timeoutTask.cancel();
        EventState state = getEventManager().getState();
        if (state != EventState.WAITING && state != EventState.STARTING) return;
        if (countdownTask != null) countdownTask.cancel();
        finishCountdown();
    }

    /** Entrada após o início da partida — sempre como espectador. */
    public void joinAsSpectator(Player player) {
        EventManager manager = getEventManager();
        manager.removePlayer(player);
        alivePlayers.remove(player.getUniqueId());
        player.getInventory().clear();
        setSpectator(player);
        Messages.send(player, "event.joined-spectator",
                Placeholder.parsed("event", getEventName()));
    }

    public Location getSpectatorViewLocation() {
        if (pos1Loc != null && pos2Loc != null && pos1Loc.getWorld() != null) {
            double minX = Math.min(pos1Loc.getX(), pos2Loc.getX());
            double maxX = Math.max(pos1Loc.getX(), pos2Loc.getX());
            double minZ = Math.min(pos1Loc.getZ(), pos2Loc.getZ());
            double maxZ = Math.max(pos1Loc.getZ(), pos2Loc.getZ());
            double y = Math.max(pos1Loc.getY(), pos2Loc.getY()) + 4;
            double x = (minX + maxX) / 2.0;
            double z = (minZ + maxZ) / 2.0;
            return new Location(pos1Loc.getWorld(), x, y, z, 0f, 35f);
        }
        if (spawnLoc != null) return spawnLoc.clone();
        return lobbyLoc != null ? lobbyLoc.clone() : null;
    }

    public void prepareForArena(Player player) {
        giveKit(player);
        player.setAllowFlight(false);
        player.setFlying(false);
    }

    protected void onLateJoin(Player player) {
        giveKit(player);
    }

    public void staffLeave(Player player) {
        onParticipantDeparted(player);
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
        MineSkyEvents.getInstance().getProxyService().sendPlayerToSurvival(target);
    }

    /** Jogador saiu da fila/partida (lobby, kick, disconnect). */
    public void onParticipantDeparted(Player player) {}

    public void removeFromMatch(Player target) {
        onParticipantDeparted(target);
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
        if (seconds == 5 && teleportsPlayersToSpawnOnCountdown()) {
            teleportAll(spawnLoc);
        }
    }

    private void finishCountdown() {
        if (countdownTask != null) countdownTask.cancel();
        if (getEventManager().getState() == EventState.RUNNING) return;

        getEventManager().setState(EventState.RUNNING);
        alivePlayers.clear();
        alivePlayers.addAll(getEventManager().getActivePlayers());

        if (teleportsPlayersToSpawnOnStart()) {
            teleportAll(spawnLoc);
        }

        for (UUID uuid : getEventManager().getActivePlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                getEventManager().removeSpectator(p);
                prepareForArena(p);
            }
        }

        if (!silent) {
            Messages.broadcast("event.started");
        }
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
        getEventManager().addSpectator(player);
        Location view = getSpectatorViewLocation();
        if (view != null) {
            player.teleport(view, PlayerTeleportEvent.TeleportCause.PLUGIN);
            player.setFallDistance(0f);
        }
        player.setGameMode(GameMode.SPECTATOR);
        player.setAllowFlight(true);
        player.setFlying(true);
    }

    protected void giveKit(Player player) {
        player.getInventory().clear();
        player.setGameMode(usesSurvivalMode() ? GameMode.SURVIVAL : GameMode.ADVENTURE);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20f);
    }

    private void cancelDueToInsufficientPlayers() {
        if (!silent) {
            Messages.broadcast("event.cancelled");
            MineSkyEvents.getInstance().getProxyService().sendEventCancelled(eventType.getId(), "insufficient_players");
        }
        resetEvent();
    }

    public void resetEvent() {
        if (timeoutTask != null) timeoutTask.cancel();
        if (countdownTask != null) countdownTask.cancel();
        cancelEndCeremonyTasks();

        World spawnWorld = Bukkit.getWorlds().get(0);
        Location hub = spawnWorld.getSpawnLocation();
        var proxy = MineSkyEvents.getInstance().getProxyService();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p == null || !p.isOnline()) continue;

            boolean inEvent = getEventManager().isPlaying(p) || getEventManager().isSpectating(p);
            if (!inEvent && p.hasPermission("mineskyevents.bypass")) {
                continue;
            }

            p.getInventory().clear();
            if (!p.hasPermission("mineskyevents.bypass")) {
                proxy.sendPlayerToSurvival(p);
                continue;
            }

            if (inEvent) {
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

    public Category getCategory() { return category; }

    public EventManager getEventManager() {
        return MineSkyEvents.getInstance().getEventManager();
    }

    private String eventName() {
        return eventType.getDisplayName();
    }
}
