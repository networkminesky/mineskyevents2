package net.mineskyevents.listeners;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.events.BaseEvent;
import net.mineskyevents.managers.EventManager;
import net.mineskyevents.managers.SpectatorBounds;
import net.mineskyevents.models.EventState;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class SpectatorListener implements Listener {

    private EventManager manager() {
        return MineSkyEvents.getInstance().getEventManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpectatorMove(PlayerMoveEvent event) {
        if (!shouldEnforce(event.getPlayer())) return;

        BaseEvent active = manager().getActiveEvent();
        if (active == null) return;

        Location to = event.getTo();
        if (to == null) return;

        if (SpectatorBounds.isOutside(active, to)) {
            Location clamped = SpectatorBounds.clamp(active, to);
            event.setTo(clamped);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpectatorTeleport(PlayerTeleportEvent event) {
        if (!shouldEnforce(event.getPlayer())) return;

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            event.setCancelled(true);
            BaseEvent active = manager().getActiveEvent();
            if (active != null) {
                Location anchor = active.getSpectatorViewLocation();
                if (anchor != null) {
                    event.getPlayer().teleport(anchor, PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            }
            return;
        }

        BaseEvent active = manager().getActiveEvent();
        if (active == null || event.getTo() == null) return;

        if (SpectatorBounds.isOutside(active, event.getTo())) {
            event.setTo(SpectatorBounds.clamp(active, event.getTo()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if (!manager().isSpectating(player)) return;
        if (manager().getState() == EventState.INACTIVE) return;
        if (player.hasPermission("mineskyevents.admin")) return;

        if (event.getNewGameMode() != GameMode.SPECTATOR) {
            event.setCancelled(true);
            player.setGameMode(GameMode.SPECTATOR);
        }
    }

    private boolean shouldEnforce(Player player) {
        EventManager manager = manager();
        return manager.getState() == EventState.RUNNING
                && manager.isSpectating(player)
                && !player.hasPermission("mineskyevents.admin");
    }
}
