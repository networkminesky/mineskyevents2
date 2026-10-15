package net.mineskyevents.listeners;

import net.mineskyevents.MineSkyEvents;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEvents implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        MineSkyEvents.getInstance().getEventManager().handleQuit(e.getPlayer());
    }
}
