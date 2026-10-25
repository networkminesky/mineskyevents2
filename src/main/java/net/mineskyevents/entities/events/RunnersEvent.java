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

    private long exitArmedAt;

    @Override
    protected void onGameStart() {
        generateMaze();
        exitArmedAt = System.currentTimeMillis() + 6000L;
        teleportAll(spawnLoc);

        for (UUID uuid : alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) giveKit(p);
        }
    }

    private void generateMaze() {
        if (pos1Loc == null || pos2Loc == null || pos1Loc.getWorld() == null) return;

        int minX = (int) Math.min(pos1Loc.getX(), pos2Loc.getX());
        int maxX = (int) Math.max(pos1Loc.getX(), pos2Loc.getX());
        int minZ = (int) Math.min(pos1Loc.getZ(), pos2Loc.getZ());
        int maxZ = (int) Math.max(pos1Loc.getZ(), pos2Loc.getZ());
        int floorY = (int) Math.min(pos1Loc.getY(), pos2Loc.getY());
        World world = pos1Loc.getWorld();

        int cellW = Math.max(1, (maxX - minX) / 2);
        int cellD = Math.max(1, (maxZ - minZ) / 2);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                setColumn(world, x, floorY, z, Material.STONE_BRICKS);
            }
        }

        boolean[][] visited = new boolean[cellW][cellD];
        carve(world, visited, 0, 0, cellW, cellD, minX, minZ, floorY, new Random());

        int endX = minX + (cellW - 1) * 2;
        int endZ = minZ + (cellD - 1) * 2;
        openColumn(world, endX, floorY, endZ);
        if (spawnLoc != null) {
            openColumn(world, spawnLoc.getBlockX(), floorY, spawnLoc.getBlockZ());
        }

        Location computedExit = new Location(world, endX + 0.5, floorY + 1.0, endZ + 0.5);
        Location configured = category.getConfig().contains("mapas." + mapId + ".exit")
                ? net.mineskyevents.map.MapManager.loadLocation(category.getConfig(), "mapas." + mapId + ".exit")
                : null;

        if (configured != null && configured.getWorld() != null
                && (spawnLoc == null || configured.distanceSquared(spawnLoc) >= 16)) {
            exitLocation = configured;
            openColumn(world, configured.getBlockX(), floorY, configured.getBlockZ());
        } else {
            exitLocation = computedExit;
        }

        if (spawnLoc != null && exitLocation.distanceSquared(spawnLoc) < 16) {
            exitLocation = new Location(world, maxX + 0.5, floorY + 1.0, maxZ + 0.5);
            openColumn(world, maxX, floorY, maxZ);
        }
    }

    private void carve(World world, boolean[][] visited, int cx, int cz, int cellW, int cellD,
                       int minX, int minZ, int floorY, Random rand) {
        visited[cx][cz] = true;
        openColumn(world, minX + cx * 2, floorY, minZ + cz * 2);

        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        List<int[]> shuffled = new ArrayList<>(Arrays.asList(dirs));
        Collections.shuffle(shuffled, rand);

        for (int[] dir : shuffled) {
            int nx = cx + dir[0];
            int nz = cz + dir[1];
            if (nx < 0 || nz < 0 || nx >= cellW || nz >= cellD || visited[nx][nz]) continue;
            openColumn(world, minX + cx * 2 + dir[0], floorY, minZ + cz * 2 + dir[1]);
            carve(world, visited, nx, nz, cellW, cellD, minX, minZ, floorY, rand);
        }
    }

    private void setColumn(World world, int x, int floorY, int z, Material mat) {
        setBlock(world, x, floorY + 1, z, mat);
        setBlock(world, x, floorY + 2, z, mat);
    }

    private void openColumn(World world, int x, int floorY, int z) {
        setColumn(world, x, floorY, z, Material.AIR);
    }

    private void setBlock(World world, int x, int y, int z, Material mat) {
        Block block = world.getBlockAt(x, y, z);
        originalBlocks.putIfAbsent(block.getLocation(), block.getType());
        block.setType(mat);
    }

    public void onExitReached(Player player) {
        if (!alivePlayers.contains(player.getUniqueId())) return;
        if (System.currentTimeMillis() < exitArmedAt) return;
        finishEvent(List.of(player));
    }

    public boolean isNearExit(Player player) {
        if (exitLocation == null || exitLocation.getWorld() == null) return false;
        if (System.currentTimeMillis() < exitArmedAt) return false;
        if (spawnLoc != null && player.getLocation().distanceSquared(spawnLoc) < 9) return false;
        if (!player.getWorld().equals(exitLocation.getWorld())) return false;
        return player.getLocation().distanceSquared(exitLocation) < 4;
    }

    @Override
    protected void checkWinCondition() {
        if (alivePlayers.isEmpty() && getEventManager().getState() == net.mineskyevents.models.EventState.RUNNING) {
            finishEvent(List.of());
        }
    }

    @Override
    protected void onPlayerEliminated(Player player, Player killer, Location deathLocation) {}

    @Override
    protected void onCleanUp() {
        originalBlocks.forEach((loc, mat) ->
                Scheduler.runTask(() -> loc.getBlock().setType(mat)));
        originalBlocks.clear();
    }
}
