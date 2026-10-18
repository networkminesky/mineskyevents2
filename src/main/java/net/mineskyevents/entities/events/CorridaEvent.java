package net.mineskyevents.entities.events;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.models.EventState;
import net.mineskyevents.models.EventType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class CorridaEvent extends BaseEvent {

    @Override
    public boolean showsRemainingPlayers() { return false; }

    @Override
    public String formatPersonalScore(Player viewer) {
        int cp = checkpointProgress.getOrDefault(viewer.getUniqueId(), 0);
        int total = checkpoints.isEmpty() ? 1 : checkpoints.size();
        return "§7Checkpoints: §f" + cp + "§7/§f" + total;
    }

    private final Map<UUID, Integer> checkpointProgress = new HashMap<>();
    private final List<Player> finishOrder = new ArrayList<>();
    private Location finishLine;

    public CorridaEvent(EventType type, Category category, String mapId) {
        super(type, category, mapId);
    }

    @Override
    protected void onGameStart() {
        finishLine = category.getConfig().contains("mapas." + mapId + ".finish")
                ? net.mineskyevents.map.MapManager.loadLocation(category.getConfig(), "mapas." + mapId + ".finish")
                : pos2Loc;

        for (UUID uuid : alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                giveKit(p);
                checkpointProgress.put(uuid, 0);
            }
        }
    }

    public void onCheckpoint(Player player, int index) {
        if (MineSkyEvents.getInstance().getEventManager().getState() != EventState.RUNNING) return;
        if (!alivePlayers.contains(player.getUniqueId())) return;

        int current = checkpointProgress.getOrDefault(player.getUniqueId(), 0);
        if (index != current) return;

        checkpointProgress.put(player.getUniqueId(), current + 1);
        player.sendMessage("§aCheckpoint §f" + (current + 1) + "§a/§f" + checkpoints.size() + " §aconcluído!");
    }

    public void onFinish(Player player) {
        if (finishOrder.contains(player)) return;
        if (!checkpoints.isEmpty()) {
            int done = checkpointProgress.getOrDefault(player.getUniqueId(), 0);
            if (done < checkpoints.size()) return;
        }
        finishOrder.add(player);

        if (finishOrder.size() >= 3 || finishOrder.size() >= alivePlayers.size()) {
            finishEvent(finishOrder.subList(0, Math.min(3, finishOrder.size())));
        }
    }

    public boolean isNearFinish(Player player) {
        if (finishLine == null) return false;
        return player.getLocation().distanceSquared(finishLine) < 4;
    }

    @Override
    protected void onPlayerEliminated(Player player) {}

    @Override
    protected void onCleanUp() {
        checkpointProgress.clear();
        finishOrder.clear();
    }
}
