package net.mineskyevents.commands.subcommands.events;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.commands.subcommands.SubCommand;
import net.mineskyevents.entities.events.BaseEvent;
import net.mineskyevents.managers.EventManager;
import net.mineskyevents.models.EventState;
import net.mineskyevents.utils.EventItems;
import net.mineskyevents.utils.Messages;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;
import java.util.UUID;

public class JoinSubCommand extends SubCommand {

    @Override
    public String getName() { return "entrar"; }

    @Override
    public String getDescription() { return "Participa do evento ativo."; }

    @Override
    public String getUsage() { return "/evento entrar"; }

    @Override
    public List<String> getAliases() { return List.of("join"); }

    @Override
    public String hasPermission() { return "mineskyevents.join"; }

    @Override
    public void perform(Player player, String[] args) {
        EventManager manager = MineSkyEvents.getInstance().getEventManager();

        if (manager.getState() == EventState.INACTIVE) {
            Messages.send(player, "errors.no-event");
            Messages.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f);
            return;
        }

        if (manager.isPlaying(player)) {
            Messages.send(player, "errors.already-joined");
            return;
        }

        if (manager.getState() == EventState.RUNNING) {
            Messages.send(player, "errors.event-running");
            return;
        }

        BaseEvent activeEvent = manager.getActiveEvent();
        if (activeEvent == null) {
            Messages.send(player, "errors.no-event");
            return;
        }

        Location lobbyLoc = activeEvent.getLobby();
        if (lobbyLoc == null) {
            player.sendMessage("§cLobby não configurado para este mapa.");
            return;
        }

        manager.addPlayer(player);
        player.getInventory().clear();

        player.teleportAsync(lobbyLoc, PlayerTeleportEvent.TeleportCause.PLUGIN).thenAccept(success -> {
            if (success) {
                Messages.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.2f);
                player.getInventory().addItem(EventItems.createBed());
                player.sendMessage("§aVocê entrou no evento §l" + activeEvent.getEventName() + "§a!");
            } else {
                manager.removePlayer(player);
            }
        });

        int total = manager.getActivePlayers().size();
        for (UUID uuid : manager.getActivePlayers()) {
            Player p = org.bukkit.Bukkit.getPlayer(uuid);
            if (p != null && !p.equals(player)) {
                Messages.send(p, "event.player-joined",
                        Placeholder.parsed("player", player.getName()),
                        Placeholder.parsed("count", String.valueOf(total)));
            }
        }

        activeEvent.checkStart();
    }
}
