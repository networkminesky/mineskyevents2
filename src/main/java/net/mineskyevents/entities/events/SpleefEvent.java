package net.mineskyevents.entities.events;

import net.mineskyevents.entities.categories.Category;
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

    public SpleefEvent(Category category, String mapId) {
        super(category, mapId, "Spleef");
    }

    @Override
    protected void onGameStart() {
        for (UUID uuid : alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                Scheduler.runTaskAtEntity(p, () -> {
                    p.getInventory().clear();
                    p.getInventory().addItem(EventItems.Shovel());
                });
            }
        }
    }

    @Override
    protected void onPlayerDeath(Player player) {
        alivePlayers.remove(player.getUniqueId());
        deadPlayers.add(player.getUniqueId());

        if (alivePlayers.size() <= 1) {
            resetEvent();
        }
    }

    @Override
    protected void onCleanUp() {
        brokenBlocks.forEach((location, material) ->
                Scheduler.runTaskAtLocation(location, () -> location.getBlock().setType(material))
        );
        brokenBlocks.clear();
    }
}