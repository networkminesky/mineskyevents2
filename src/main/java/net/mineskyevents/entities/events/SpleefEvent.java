package net.mineskyevents.entities.events;

import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.models.EventType;
import net.mineskyevents.utils.EventItems;
import net.mineskyevents.utils.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpleefEvent extends BaseEvent {

    public static final Map<Location, Material> brokenBlocks = new ConcurrentHashMap<>();

    public SpleefEvent(EventType type, Category category, String mapId) {
        super(type, category, mapId);
    }

    @Override
    protected void onGameStart() {
        for (UUID uuid : alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                giveKit(p);
                p.getInventory().addItem(EventItems.Shovel());
            }
        }
    }

    @Override
    protected void onPlayerEliminated(Player player) {
        // handled by checkWinCondition
    }

    @Override
    protected void onCleanUp() {
        brokenBlocks.forEach((loc, mat) ->
                Scheduler.runTask(() -> loc.getBlock().setType(mat)));
        brokenBlocks.clear();
    }
}
