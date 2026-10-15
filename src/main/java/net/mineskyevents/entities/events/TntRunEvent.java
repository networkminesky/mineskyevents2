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

    private final Set<Location> scheduledBreaks = new HashSet<>();
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

        floorTask = Scheduler.runTaskTimer(this::breakFloors, 10L, 5L);
    }

    private void breakFloors() {
        if (MineSkyEvents.getInstance().getEventManager().getState() != EventState.RUNNING) return;

        for (UUID uuid : alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;

            Location below = p.getLocation().subtract(0, 1, 0);
            Block block = below.getBlock();
            if (block.getType() == Material.TNT || block.getType() == Material.SAND) {
                scheduledBreaks.add(below.clone());
                Scheduler.runTaskLater(() -> {
                    if (block.getType() != Material.AIR) {
                        block.setType(Material.AIR);
                    }
                }, 8L);
            }

            if (p.getLocation().getY() < getArenaMinY() - 2) {
                eliminatePlayer(p);
            }
        }
    }

    @Override
    protected void onPlayerEliminated(Player player) {}

    @Override
    protected void onCleanUp() {
        if (floorTask != null) floorTask.cancel();
    }
}
