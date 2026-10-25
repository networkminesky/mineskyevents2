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

    @Override
    public boolean eliminatesOnVoid() { return true; }

    @Override
    public boolean usesSurvivalMode() { return true; }

    @Override
    public boolean canBreak(Material material) {
        return material == Material.SNOW_BLOCK || material == Material.SNOW || material == Material.POWDER_SNOW;
    }

    public static final Map<Location, Material> brokenBlocks = new ConcurrentHashMap<>();

    public SpleefEvent(EventType type, Category category, String mapId) {
        super(type, category, mapId);
    }

    @Override
    protected void giveKit(Player player) {
        super.giveKit(player);
        player.getInventory().addItem(EventItems.Shovel());
    }

    @Override
    protected void onGameStart() {
        for (UUID uuid : alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) giveKit(p);
        }
    }

    @Override
    protected void onPlayerEliminated(Player player, Player killer, Location deathLocation) {
        // handled by checkWinCondition
    }

    @Override
    protected void onCleanUp() {
        brokenBlocks.forEach((loc, mat) ->
                Scheduler.runTask(() -> loc.getBlock().setType(mat)));
        brokenBlocks.clear();
    }
}
