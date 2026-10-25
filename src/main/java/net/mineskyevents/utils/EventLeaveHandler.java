package net.mineskyevents.utils;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.managers.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

public final class EventLeaveHandler {

    private EventLeaveHandler() {}

    public static void leaveEvent(Player player) {
        EventManager manager = MineSkyEvents.getInstance().getEventManager();

        if (!manager.isPlaying(player) && !manager.isSpectating(player)) {
            Messages.send(player, "errors.not-in-event");
            return;
        }

        if (manager.isPlaying(player) && manager.getActiveEvent() != null) {
            manager.getActiveEvent().onParticipantDeparted(player);
        }
        manager.removePlayer(player);
        manager.removeSpectator(player);
        manager.handleQuit(player);

        UUID uuid = player.getUniqueId();
        for (UUID otherId : manager.getActivePlayers()) {
            Player other = Bukkit.getPlayer(otherId);
            if (other != null && !other.getUniqueId().equals(uuid)) {
                Messages.send(other, "event.player-left",
                        Placeholder.parsed("player", player.getName()));
            }
        }

        Scheduler.runTask(() -> {
            player.getInventory().clear();
            player.setGameMode(GameMode.ADVENTURE);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.8f);
            MineSkyEvents.getInstance().getProxyService().sendPlayerToSurvival(player);
        });

        Messages.send(player, "event.left");
    }
}
