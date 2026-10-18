package net.mineskyevents.map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;

public final class MapBarrierHelper {

    public static final Material BARRIER_MATERIAL = Material.BARRIER;

    private MapBarrierHelper() {}

    public static Location[] loadBarrierCorners(YamlConfiguration config, String mapId) {
        String base = "mapas." + mapId + ".barreira";
        Location pos1 = MapManager.loadLocation(config, base + ".pos1");
        Location pos2 = MapManager.loadLocation(config, base + ".pos2");
        if (pos1 == null || pos2 == null) return null;
        return new Location[]{pos1, pos2};
    }

    public static void saveBarrierCorners(String categoryId, String mapId, Location pos1, Location pos2) {
        MapManager.savePoint(categoryId, mapId, "barreira.pos1", pos1);
        MapManager.savePoint(categoryId, mapId, "barreira.pos2", pos2);
    }

    public static List<BlockSnapshot> placeBarrier(Location pos1, Location pos2) {
        List<BlockSnapshot> snapshots = new ArrayList<>();
        if (pos1 == null || pos2 == null || pos1.getWorld() == null) return snapshots;
        if (!pos1.getWorld().equals(pos2.getWorld())) return snapshots;

        World world = pos1.getWorld();
        int minX = (int) Math.floor(Math.min(pos1.getX(), pos2.getX()));
        int maxX = (int) Math.floor(Math.max(pos1.getX(), pos2.getX()));
        int minY = (int) Math.floor(Math.min(pos1.getY(), pos2.getY()));
        int maxY = (int) Math.floor(Math.max(pos1.getY(), pos2.getY()));
        int minZ = (int) Math.floor(Math.min(pos1.getZ(), pos2.getZ()));
        int maxZ = (int) Math.floor(Math.max(pos1.getZ(), pos2.getZ()));

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    snapshots.add(new BlockSnapshot(block.getLocation(), block.getBlockData().clone()));
                    block.setType(BARRIER_MATERIAL, false);
                }
            }
        }
        return snapshots;
    }

    public static void restoreBarrier(List<BlockSnapshot> snapshots) {
        if (snapshots == null) return;
        for (BlockSnapshot snap : snapshots) {
            Block block = snap.location.getBlock();
            block.setBlockData(snap.data, false);
        }
    }

    public record BlockSnapshot(Location location, BlockData data) {}
}
