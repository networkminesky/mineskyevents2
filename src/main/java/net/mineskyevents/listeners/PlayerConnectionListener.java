package net.mineskyevents.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.map.MapManager;
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

        event.joinMessage(null);

        player.getInventory().clear();
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);

        var ban = MineSkyEvents.getInstance().getBanManager().check(player.getUniqueId(), player.getName());
        if (ban != null) {
            String key = ban.permanent() ? "errors.banned" : "errors.tempbanned";
            player.kick(Messages.parse(
                    MineSkyEvents.getInstance().getMessagesConfig().getString(key, key),
                    Placeholder.component("reason", Component.text(ban.reason())),
                    Placeholder.parsed("time", ban.remainingLabel())
            ));
            return;
        }

        if (!manager.hasActiveEvent() && !player.hasPermission("mineskyevents.bypass")) {
            String template = MineSkyEvents.getInstance().getMessagesConfig()
                    .getString("errors.server-closed", "<red>Servidor de eventos fechado.</red>");
            player.kick(Messages.parse(template));
            return;
        }

        if (manager.getState() == EventState.RUNNING && manager.getActiveEvent() != null
                && player.hasPermission("mineskyevents.join")) {
            manager.getActiveEvent().joinAsSpectator(player);
            return;
        }

        if (manager.getState() == EventState.WAITING && player.hasPermission("mineskyevents.join")
                && manager.getActiveEvent() != null) {
            var active = manager.getActiveEvent();
            String mapName = MapManager.getMapDisplayName(
                    net.mineskyevents.entities.categories.CategoryHandler.getCategoryById(active.getEventType().getId()),
                    active.getMapId());
            Messages.send(player, "event.announced",
                    Placeholder.parsed("event", active.getEventName()),
                    Placeholder.parsed("map", mapName != null ? mapName : active.getMapId()));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(null);
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
