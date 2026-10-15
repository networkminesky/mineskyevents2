package net.mineskyevents.listeners;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.managers.EventManager;
import net.mineskyevents.models.EventState;
import net.mineskyevents.utils.Messages;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        EventManager manager = MineSkyEvents.getInstance().getEventManager();

        player.getInventory().clear();
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);

        if (!manager.hasActiveEvent() && !player.hasPermission("mineskyevents.bypass")) {
            event.setJoinMessage(null);
            String template = MineSkyEvents.getInstance().getMessagesConfig()
                    .getString("errors.server-closed", "<red>Servidor de eventos fechado.</red>");
            player.kick(Messages.parse(template));
            return;
        }

        if (manager.getState() == EventState.WAITING && player.hasPermission("mineskyevents.join")) {
            Messages.send(player, "event.announced");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        EventManager manager = MineSkyEvents.getInstance().getEventManager();

        if (manager.isPlaying(player) && manager.getState() == EventState.RUNNING) {
            var activeEvent = manager.getActiveEvent();
            if (activeEvent != null) activeEvent.eliminatePlayer(player);
        }

        manager.handleQuit(player);

        if (!player.hasPermission("mineskyevents.bypass")) {
            player.getInventory().clear();
            player.getEnderChest().clear();
        }
    }
}
