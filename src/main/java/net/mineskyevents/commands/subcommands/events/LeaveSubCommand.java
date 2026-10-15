package net.mineskyevents.commands.subcommands.events;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.commands.subcommands.SubCommand;
import net.mineskyevents.managers.EventManager;
import net.mineskyevents.utils.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class LeaveSubCommand extends SubCommand {

    @Override
    public String getName() { return "sair"; }

    @Override
    public String getDescription() { return "Sai do evento atual."; }

    @Override
    public String getUsage() { return "/evento sair"; }

    @Override
    public List<String> getAliases() { return List.of("leave"); }

    @Override
    public String hasPermission() { return "mineskyevents.join"; }

    @Override
    public void perform(Player player, String[] args) {
        EventManager manager = MineSkyEvents.getInstance().getEventManager();

        if (!manager.isPlaying(player) && !manager.isSpectating(player)) {
            player.sendMessage("§cVocê não está participando de nenhum evento.");
            return;
        }

        manager.handleQuit(player);

        Scheduler.runTask(() -> {
            player.getInventory().clear();
            player.setGameMode(GameMode.ADVENTURE);
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
        });

        player.sendMessage("§cVocê saiu do evento.");
    }
}
