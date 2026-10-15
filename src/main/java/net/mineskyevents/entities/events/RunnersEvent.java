package net.mineskyevents.entities.events;

import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.models.EventType;
import net.mineskyevents.utils.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public class RunnersEvent extends BaseEvent {

    private final Map<Location, Material> originalBlocks = new HashMap<>();
    private Location exitLocation;

    public RunnersEvent(EventType type, Category category, String mapId) {
        super(type, category, mapId);
    }

    @Override
    protected void onGameStart() {
        exitLocation = category.getConfig().contains("mapas." + mapId + ".exit")
                ? net.mineskyevents.map.MapManager.loadLocation(category.getConfig(), "mapas." + mapId + ".exit")
                : spawnLoc;

        generateMaze();
        teleportAll(spawnLoc);

        for (UUID uuid : alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) giveKit(p);
        }
    }

    private void generateMaze() {
        if (pos1Loc == null || pos2Loc == null) return;

        int minX = (int) Math.min(pos1Loc.getX(), pos2Loc.getX());
        int maxX = (int) Math.max(pos1Loc.getX(), pos2Loc.getX());
        int minZ = (int) Math.min(pos1Loc.getZ(), pos2Loc.getZ());
        int maxZ = (int) Math.max(pos1Loc.getZ(), pos2Loc.getZ());
        int y = (int) pos1Loc.getY();
        World world = pos1Loc.getWorld();

        int width = ((maxX - minX) / 2) | 1;
        int depth = ((maxZ - minZ) / 2) | 1;
        boolean[][] maze = generateMazeGrid(width, depth);

        for (int mx = 0; mx < width; mx++) {
            for (int mz = 0; mz < depth; mz++) {
                int bx = minX + mx * 2;
                int bz = minZ + mz * 2;
                setBlock(world, bx, y, bz, Material.AIR);

                if (!maze[mx][mz]) {
                    setBlock(world, bx + 1, y, bz, Material.STONE_BRICKS);
                    setBlock(world, bx, y, bz + 1, Material.STONE_BRICKS);
                } else {
                    setBlock(world, bx + 1, y, bz, Material.AIR);
                    setBlock(world, bx, y, bz + 1, Material.AIR);
                }
            }
        }
    }

    private boolean[][] generateMazeGrid(int w, int d) {
        boolean[][] grid = new boolean[w][d];
        for (boolean[] row : grid) Arrays.fill(row, false);
        carve(grid, 0, 0, w, d, new Random());
        return grid;
    }

    private void carve(boolean[][] grid, int x, int z, int w, int d, Random rand) {
        grid[x][z] = true;
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        List<int[]> shuffled = new ArrayList<>(Arrays.asList(dirs));
        Collections.shuffle(shuffled, rand);

        for (int[] dir : shuffled) {
            int nx = x + dir[0], nz = z + dir[1];
            if (nx >= 0 && nx < w && nz >= 0 && nz < d && !grid[nx][nz]) {
                carve(grid, nx, nz, w, d, rand);
            }
        }
    }

    private void setBlock(World world, int x, int y, int z, Material mat) {
        Block block = world.getBlockAt(x, y, z);
        originalBlocks.put(block.getLocation(), block.getType());
        block.setType(mat);
    }

    public void onExitReached(Player player) {
        if (!alivePlayers.contains(player.getUniqueId())) return;
        finishEvent(List.of(player));
    }

    public boolean isNearExit(Player player) {
        if (exitLocation == null) return false;
        return player.getLocation().distanceSquared(exitLocation) < 4;
    }

    @Override
    protected void onPlayerEliminated(Player player) {}

    @Override
    protected void onCleanUp() {
        originalBlocks.forEach((loc, mat) ->
                Scheduler.runTask(() -> loc.getBlock().setType(mat)));
        originalBlocks.clear();
    }
}
