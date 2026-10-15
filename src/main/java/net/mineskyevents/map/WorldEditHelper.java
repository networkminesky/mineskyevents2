package net.mineskyevents.map;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import net.mineskyevents.MineSkyEvents;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class WorldEditHelper {

    private WorldEditHelper() {}

    public static boolean isAvailable() {
        return Bukkit.getPluginManager().getPlugin("WorldEdit") != null;
    }

    public static Region getSelection(Player player) {
        if (!isAvailable()) return null;

        WorldEditPlugin we = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        if (we == null) return null;

        try {
            return we.getSession(player).getSelection(BukkitAdapter.adapt(player.getWorld()));
        } catch (Exception e) {
            return null;
        }
    }

    public static Location[] getSelectionCorners(Player player) {
        Region region = getSelection(player);
        if (region == null) return null;

        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        Location pos1 = new Location(player.getWorld(), min.x(), min.y(), min.z());
        Location pos2 = new Location(player.getWorld(), max.x(), max.y(), max.z());
        return new Location[]{pos1, pos2};
    }

    public static void saveSelectionToMap(Player player, String categoryId, String mapId) {
        Location[] corners = getSelectionCorners(player);
        if (corners == null) return;

        MapManager.savePoint(categoryId, mapId, "pos1", corners[0]);
        MapManager.savePoint(categoryId, mapId, "pos2", corners[1]);
    }
}
