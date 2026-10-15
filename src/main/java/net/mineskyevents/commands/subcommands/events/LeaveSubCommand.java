package net.mineskyevents.commands.subcommands.events;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.commands.subcommands.SubCommand;
import net.mineskyevents.managers.EventManager;
import net.mineskyevents.utils.Scheduler;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class LeaveSubCommand extends SubCommand {
    @Override
    public String getName() { return "sair"; }
    @Override
    public String getDescription() { return "Sai do evento atual."; }
    @Override
    public String getUsage() { return "/event sair"; }
    @Override
    public List<String> getAliases() { return Collections.singletonList("leave"); }
    @Override
    public String hasPermission() { return "mineskyevents.admin"; }

    @Override
    public void perform(Player player, String[] args) {
        EventManager manager = MineSkyEvents.getInstance().getEventManager();

        if (!manager.isPlaying(player) && !manager.isSpectating(player)) {
            player.sendMessage("§c[!] Você não está participando de nenhum evento.");
            return;
        }

        manager.handleQuit(player);

        Scheduler.runTaskAtEntity(player, () -> {
            player.getInventory().clear();
            player.performCommand("spawn");
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
        });

        player.sendMessage("§c[!] Você saiu do evento.");
    }
}