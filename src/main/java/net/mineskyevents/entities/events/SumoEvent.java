package net.mineskyevents.entities.events;

import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.models.EventType;
import net.mineskyevents.utils.EventItems;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SumoEvent extends BaseEvent {

    public SumoEvent(EventType type, Category category, String mapId) {
        super(type, category, mapId);
    }

    @Override
    protected void onGameStart() {
        for (UUID uuid : alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                giveKit(p);
                p.getInventory().addItem(EventItems.Stick());
            }
        }
    }

    @Override
    protected void onPlayerEliminated(Player player) {}

    @Override
    protected void onCleanUp() {}
}
