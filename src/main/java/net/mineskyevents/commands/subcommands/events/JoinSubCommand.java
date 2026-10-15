package net.mineskyevents.commands.subcommands.events;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.commands.subcommands.SubCommand;
import net.mineskyevents.entities.events.BaseEvent;
import net.mineskyevents.managers.EventManager;
import net.mineskyevents.models.EventState;
import net.mineskyevents.utils.EventItems;
import net.mineskyevents.utils.Scheduler;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class JoinSubCommand extends SubCommand {
    @Override
    public String getName() { return "entrar"; }
    @Override
    public String getDescription() { return "Participa do evento que está ativo no momento."; }
    @Override
    public String getUsage() { return "/event entrar"; }
    @Override
    public List<String> getAliases() { return Collections.singletonList("join"); }
    @Override
    public String hasPermission() { return "mineskyevents.admin"; }

    @Override
    public void perform(Player player, String[] args) {
        EventManager manager = MineSkyEvents.getInstance().getEventManager();

        if (manager.getState() == EventState.INACTIVE) {
            player.sendMessage("§c[!] Não há nenhum evento ativo no momento.");
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            return;
        }

        if (manager.isPlaying(player)) {
            player.sendMessage("§c[!] Você já está participando deste evento.");
            return;
        }

        if (manager.getState() == EventState.RUNNING) {
            player.sendMessage("§c[!] O evento já começou! Você não pode mais entrar.");
            return;
        }

        BaseEvent activeEvent = manager.getActiveEvent();
        if (activeEvent == null) {
            player.sendMessage("§c[!] Ocorreu um erro interno: o evento ativo não foi carregado.");
            return;
        }

        Location lobbyLoc = activeEvent.getLobby();
        if (lobbyLoc == null) {
            player.sendMessage("§c[!] Erro crítico: O lobby deste mapa não está configurado!");
            return;
        }

        manager.addPlayer(player);

        Scheduler.runTaskAtEntity(player, () -> {
            player.getInventory().clear();

            player.teleportAsync(lobbyLoc, PlayerTeleportEvent.TeleportCause.COMMAND).thenAccept(success -> {
                if (success) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    player.getInventory().addItem(EventItems.createBed());
                    player.getInventory().addItem(EventItems.HeadEvents(player));
                    player.sendMessage("§a[!] Você entrou no evento " + activeEvent.getEventName() + "!");
                } else {
                    player.sendMessage("§c[!] Não foi possível teleportar você para a área de espera.");
                    manager.removePlayer(player);
                }
            });
        });

        int totalPlayers = manager.getActivePlayers().size();
        for (java.util.UUID uuid : manager.getActivePlayers()) {
            Player p = org.bukkit.Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline() && !p.equals(player)) {
                p.sendMessage("§7" + player.getName() + " §eentrou no evento! (§b" + totalPlayers + "§e)");
            }
        }

        activeEvent.checkStart();
    }
}