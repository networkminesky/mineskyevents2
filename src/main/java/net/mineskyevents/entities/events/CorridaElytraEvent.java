package net.mineskyevents.entities.events;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.map.MapManager;
import net.mineskyevents.models.EventState;
import net.mineskyevents.models.EventType;
import net.mineskyevents.utils.EventItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public class CorridaElytraEvent extends BaseEvent {

    private final Map<UUID, Integer> ringProgress = new HashMap<>();
    private final Map<UUID, Integer> lastCheckpoint = new HashMap<>();
    private final List<Player> finishOrder = new ArrayList<>();
    private List<Location> arcos = new ArrayList<>();
    private Location finishLine;

    public CorridaElytraEvent(EventType type, Category category, String mapId) {
        super(type, category, mapId);
    }

    @Override
    public String formatPersonalScore(Player viewer) {
        int rings = ringProgress.getOrDefault(viewer.getUniqueId(), 0);
        int totalRings = arcos.isEmpty() ? 0 : arcos.size();
        int cp = lastCheckpoint.getOrDefault(viewer.getUniqueId(), -1) + 1;
        int totalCp = checkpoints.isEmpty() ? 0 : checkpoints.size();
        return "§7Arcos: §f" + rings + "§7/§f" + totalRings + " §8| §7CP: §f" + cp + "§7/§f" + totalCp;
    }

    @Override
    protected void onSignupsStarted() {
        arcos = MapManager.loadArcos(category.getConfig(), mapId);
    }

    public List<Location> getArcos() {
        return arcos;
    }

    @Override
    protected void onGameStart() {
        finishLine = category.getConfig().contains("mapas." + mapId + ".finish")
                ? MapManager.loadLocation(category.getConfig(), "mapas." + mapId + ".finish")
                : pos2Loc;

        for (UUID uuid : alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                giveKit(p);
                p.getInventory().setChestplate(new ItemStack(Material.ELYTRA));
                p.getInventory().addItem(new ItemStack(Material.FIREWORK_ROCKET, 64));
                p.getInventory().addItem(EventItems.CheckPoint);
                ringProgress.put(uuid, 0);
                lastCheckpoint.put(uuid, -1);
            }
        }
    }

    private static final double RING_GATE_RADIUS = 5.5;
    private static final double CHECKPOINT_RADIUS_SQ = 64.0;

    public void handleMovement(Player player, Location from, Location to) {
        if (!arcos.isEmpty()) {
            int current = ringProgress.getOrDefault(player.getUniqueId(), 0);
            if (current < arcos.size()) {
                Location ring = arcos.get(current);
                if (crossedOrientedGate(from, to, ring)) {
                    onRingPassed(player, current);
                }
            }
        }

        if (!checkpoints.isEmpty()) {
            int expected = lastCheckpoint.getOrDefault(player.getUniqueId(), -1) + 1;
            if (expected < checkpoints.size()) {
                Location cp = checkpoints.get(expected);
                if (to.getWorld() == cp.getWorld() && to.distanceSquared(cp) <= CHECKPOINT_RADIUS_SQ) {
                    onCheckpointReached(player, expected);
                }
            }
        }
    }

    private boolean crossedOrientedGate(Location from, Location to, Location gate) {
        if (from.getWorld() != gate.getWorld() || to.getWorld() != gate.getWorld()) {
            return false;
        }

        Vector normal = gateFacing(gate);
        Vector center = gate.toVector().add(new Vector(0, 1.35, 0));

        double fromSide = from.toVector().subtract(center).dot(normal);
        double toSide = to.toVector().subtract(center).dot(normal);
        if (fromSide * toSide > 0) {
            return false;
        }

        Vector midpoint = from.toVector().midpoint(to.toVector());
        return withinGateOpening(midpoint, center, normal);
    }

    private boolean withinGateOpening(Vector point, Vector center, Vector normal) {
        Vector offset = point.clone().subtract(center);
        double along = offset.dot(normal);
        Vector lateral = offset.subtract(normal.clone().multiply(along));
        return lateral.lengthSquared() <= RING_GATE_RADIUS * RING_GATE_RADIUS
                && Math.abs(along) <= 2.5;
    }

    private static Vector gateFacing(Location gate) {
        double yawRadians = Math.toRadians(gate.getYaw());
        double pitchRadians = Math.toRadians(-gate.getPitch());
        return new Vector(
                -Math.sin(yawRadians) * Math.cos(pitchRadians),
                Math.sin(pitchRadians),
                Math.cos(yawRadians) * Math.cos(pitchRadians)
        ).normalize();
    }

    public void onRingPassed(Player player, int ringIndex) {
        if (MineSkyEvents.getInstance().getEventManager().getState() != EventState.RUNNING) return;
        if (!alivePlayers.contains(player.getUniqueId())) return;
        if (arcos.isEmpty()) return;

        int current = ringProgress.getOrDefault(player.getUniqueId(), 0);
        if (ringIndex != current) return;

        ringProgress.put(player.getUniqueId(), current + 1);
        player.sendMessage("§aArco §f" + (current + 1) + "§a/§f" + arcos.size() + " §aconcluído!");
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.35f);
    }

    public void onCheckpointReached(Player player, int index) {
        if (MineSkyEvents.getInstance().getEventManager().getState() != EventState.RUNNING) return;
        if (!alivePlayers.contains(player.getUniqueId())) return;
        if (checkpoints.isEmpty()) return;

        int expected = lastCheckpoint.getOrDefault(player.getUniqueId(), -1) + 1;
        if (index != expected) return;

        lastCheckpoint.put(player.getUniqueId(), index);
        player.sendMessage("§eCheckpoint §f" + (index + 1) + "§e salvo!");
    }

    public void tryFinish(Player player) {
        if (finishOrder.contains(player)) return;

        int ringsDone = ringProgress.getOrDefault(player.getUniqueId(), 0);
        if (!arcos.isEmpty() && ringsDone < arcos.size()) {
            return;
        }

        if (finishLine == null || player.getLocation().distanceSquared(finishLine) >= 16) {
            return;
        }

        onFinish(player);
    }

    public void teleportToLastCheckpoint(Player player) {
        int index = lastCheckpoint.getOrDefault(player.getUniqueId(), -1);
        if (index < 0 || checkpoints.isEmpty() || index >= checkpoints.size()) {
            Location fallback = getRecoveryLocation(player);
            if (fallback != null) {
                player.teleport(fallback, PlayerTeleportEvent.TeleportCause.PLUGIN);
                player.setFallDistance(0f);
            }
            player.sendMessage("§cNenhum checkpoint registrado ainda.");
            return;
        }
        Location cp = checkpoints.get(index).clone();
        player.teleport(cp, PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.setFallDistance(0f);
        player.sendMessage("§eVocê voltou ao checkpoint §f" + (index + 1) + "§e.");
    }

    private void onFinish(Player player) {
        finishOrder.add(player);

        if (finishOrder.size() >= 3 || finishOrder.size() >= alivePlayers.size()) {
            finishEvent(finishOrder.subList(0, Math.min(3, finishOrder.size())));
        }
    }

    @Override
    protected void onPlayerEliminated(Player player, Player killer, Location deathLocation) {}

    @Override
    protected void onCleanUp() {
        ringProgress.clear();
        lastCheckpoint.clear();
        finishOrder.clear();
        arcos.clear();
    }
}
