package net.mineskyevents.entities.events;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.models.EventState;
import net.mineskyevents.models.EventType;
import net.mineskyevents.utils.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public class TntRunEvent extends BaseEvent {

    @Override
    public boolean eliminatesOnVoid() { return true; }

    private static final Set<Material> SURFACE_BLOCKS = Set.of(
            Material.SAND, Material.RED_SAND, Material.GRAVEL, Material.SUSPICIOUS_SAND, Material.SUSPICIOUS_GRAVEL
    );

    private final Set<Location> pendingBreaks = new HashSet<>();
    private Scheduler.Task floorTask;

    public TntRunEvent(EventType type, Category category, String mapId) {
        super(type, category, mapId);
    }

    @Override
    protected void onGameStart() {
        for (UUID uuid : alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) giveKit(p);
        }

        floorTask = Scheduler.runTaskTimer(this::scheduleFloorBreaks, 5L, 2L);
    }

    private void scheduleFloorBreaks() {
        if (MineSkyEvents.getInstance().getEventManager().getState() != EventState.RUNNING) return;

        for (UUID uuid : alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;

            for (int depth = 1; depth <= 2; depth++) {
                Location blockLoc = p.getLocation().clone().subtract(0, depth, 0).toBlockLocation();
                scheduleBreak(blockLoc);
            }

            if (p.getLocation().getY() < getArenaMinY() - 2) {
                eliminatePlayer(p);
            }
        }
    }

    private void scheduleBreak(Location blockLoc) {
        if (!pendingBreaks.add(blockLoc)) return;

        Scheduler.runTaskLater(() -> {
            pendingBreaks.remove(blockLoc);
            Block block = blockLoc.getBlock();
            Material type = block.getType();
            if (type == Material.TNT || SURFACE_BLOCKS.contains(type)) {
                block.setType(Material.AIR, false);
            }
        }, 7L);
    }

    @Override
    protected void onPlayerEliminated(Player player, Player killer, Location deathLocation) {}

    @Override
    protected void onCleanUp() {
        if (floorTask != null) floorTask.cancel();
        pendingBreaks.clear();
    }
}
