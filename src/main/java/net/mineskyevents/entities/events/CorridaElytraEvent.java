package net.mineskyevents.entities.events;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.models.EventState;
import net.mineskyevents.models.EventType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CorridaElytraEvent extends BaseEvent {

    private final Map<UUID, Integer> ringProgress = new HashMap<>();
    private final List<Player> finishOrder = new ArrayList<>();

    public CorridaElytraEvent(EventType type, Category category, String mapId) {
        super(type, category, mapId);
    }

    @Override
    protected void onGameStart() {
        for (UUID uuid : alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                giveKit(p);
                p.getInventory().setChestplate(new ItemStack(Material.ELYTRA));
                p.getInventory().addItem(new ItemStack(Material.FIREWORK_ROCKET, 64));
                ringProgress.put(uuid, 0);
            }
        }
    }

    public void onRingPassed(Player player, int ringIndex) {
        if (MineSkyEvents.getInstance().getEventManager().getState() != EventState.RUNNING) return;
        if (!alivePlayers.contains(player.getUniqueId())) return;

        int current = ringProgress.getOrDefault(player.getUniqueId(), 0);
        if (ringIndex != current) return;

        ringProgress.put(player.getUniqueId(), current + 1);

        if (checkpoints.isEmpty() || current + 1 >= checkpoints.size()) {
            onFinish(player);
        }
    }

    private void onFinish(Player player) {
        if (finishOrder.contains(player)) return;
        finishOrder.add(player);

        if (finishOrder.size() >= 3 || finishOrder.size() >= alivePlayers.size()) {
            finishEvent(finishOrder.subList(0, Math.min(3, finishOrder.size())));
        }
    }

    @Override
    protected void onPlayerEliminated(Player player) {}

    @Override
    protected void onCleanUp() {
        ringProgress.clear();
        finishOrder.clear();
    }
}
