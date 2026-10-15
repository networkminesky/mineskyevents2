package net.mineskyevents.entities.events;

import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.models.EventType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class TijolaoWarsEvent extends BaseEvent {

    public TijolaoWarsEvent(EventType type, Category category, String mapId) {
        super(type, category, mapId);
    }

    @Override
    protected void onGameStart() {
        for (UUID uuid : alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                giveKit(p);
                p.getInventory().addItem(new ItemStack(Material.SNOWBALL, 64));
            }
        }
    }

    @Override
    protected void onPlayerEliminated(Player player) {}

    @Override
    protected void onCleanUp() {}
}
