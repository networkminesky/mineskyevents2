package net.mineskyevents.commands.subcommands.events;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.commands.subcommands.SubCommand;
import net.mineskyevents.managers.EventManager;
import net.mineskyevents.models.EventState;
import net.mineskyevents.utils.Messages;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ForceStartSubCommand extends SubCommand {

    @Override
    public String getName() { return "forcestart"; }

    @Override
    public String getDescription() { return "Força o início do evento (ignora mínimo de jogadores)."; }

    @Override
    public String getUsage() { return "/event forcestart"; }

    @Override
    public List<String> getAliases() { return List.of("forcar", "forçar"); }

    @Override
    public String hasPermission() { return "mineskyevents.admin"; }

    @Override
    public boolean allowsConsole() { return true; }

    @Override
    public void perform(CommandSender sender, String[] args) {
        EventManager manager = MineSkyEvents.getInstance().getEventManager();

        if (!manager.hasActiveEvent()) {
            Messages.send(sender, "errors.no-event");
            return;
        }

        EventState state = manager.getState();
        if (state == EventState.INACTIVE) {
            Messages.send(sender, "errors.no-event");
            return;
        }

        if (state == EventState.RUNNING) {
            sender.sendMessage("§eO evento já está em andamento.");
            return;
        }

        var event = manager.getActiveEvent();
        if (event == null) {
            Messages.send(sender, "errors.no-event");
            return;
        }

        event.forceStart();
        Messages.send(sender, "admin.force-started");
        if (sender instanceof Player player) {
            Messages.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.2f);
        }
    }
}
