package net.mineskyevents.entities.events;

import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.models.EventType;
import net.mineskyevents.utils.EventItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SumoEvent extends BaseEvent {

    @Override
    public boolean eliminatesOnVoid() { return true; }

    @Override
    public boolean allowsPlayerVsPlayer() { return true; }

    @Override
    public boolean usesSurvivalMode() { return true; }

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
    protected void onPlayerEliminated(Player player, Player killer, Location deathLocation) {}

    @Override
    protected void onCleanUp() {}
}
