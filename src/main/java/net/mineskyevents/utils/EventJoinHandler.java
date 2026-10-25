package net.mineskyevents.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.events.BaseEvent;
import net.mineskyevents.managers.EventManager;
import net.mineskyevents.models.EventState;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

public final class EventJoinHandler {

    private EventJoinHandler() {}

    public static void join(Player player) {
        EventManager manager = MineSkyEvents.getInstance().getEventManager();
        BaseEvent active = manager.getActiveEvent();
        boolean notifyOthers = active == null || !active.isSilent();
        join(player, notifyOthers);
    }

    public static void join(Player player, boolean notifyOthers) {
        EventManager manager = MineSkyEvents.getInstance().getEventManager();

        if (manager.getState() == EventState.INACTIVE) {
            Messages.send(player, "errors.no-event");
            Messages.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f);
            return;
        }

        var ban = MineSkyEvents.getInstance().getBanManager().check(player.getUniqueId(), player.getName());
        if (ban != null) {
            Messages.send(player, ban.permanent() ? "errors.banned" : "errors.tempbanned",
                    Placeholder.component("reason", Component.text(ban.reason())),
                    Placeholder.parsed("time", ban.remainingLabel()));
            return;
        }

        if (manager.isPlaying(player) || manager.isSpectating(player)) {
            Messages.send(player, "errors.already-joined");
            return;
        }

        BaseEvent activeEvent = manager.getActiveEvent();
        if (activeEvent == null) {
            Messages.send(player, "errors.no-event");
            return;
        }

        if (manager.getState() == EventState.RUNNING) {
            activeEvent.joinAsSpectator(player);
            return;
        }

        if (manager.getState() != EventState.WAITING && manager.getState() != EventState.STARTING) {
            Messages.send(player, "errors.no-event");
            return;
        }

        Location lobbyLoc = activeEvent.getJoinLocation();
        if (lobbyLoc == null) {
            player.sendMessage("§cLobby não configurado para este mapa.");
            return;
        }

        manager.addPlayer(player);
        player.getInventory().clear();

        player.teleportAsync(lobbyLoc, PlayerTeleportEvent.TeleportCause.PLUGIN).thenAccept(success -> {
            if (success) {
                activeEvent.prepareForArena(player);
                Messages.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.2f);
                EventLobbyItems.giveWaitingHotbar(player, activeEvent);
                player.sendMessage("§aVocê entrou no evento §l" + activeEvent.getEventName() + "§a!");
            } else {
                manager.removePlayer(player);
            }
        });

        if (notifyOthers) {
            int total = manager.getActivePlayers().size();
            for (UUID uuid : manager.getActivePlayers()) {
                Player p = org.bukkit.Bukkit.getPlayer(uuid);
                if (p != null && !p.equals(player)) {
                    Messages.send(p, "event.player-joined",
                            Placeholder.parsed("player", player.getName()),
                            Placeholder.parsed("count", String.valueOf(total)));
                }
            }
        }

        activeEvent.checkStart();
    }
}
