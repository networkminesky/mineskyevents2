package net.mineskyevents.commands.subcommands.events;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.commands.subcommands.SubCommand;
import net.mineskyevents.utils.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class LeaveSubCommand extends SubCommand {

    @Override
    public String getName() { return "sair"; }

    @Override
    public String getDescription() { return "Staff sai da arena em criativo."; }

    @Override
    public String getUsage() { return "/event sair"; }

    @Override
    public List<String> getAliases() { return List.of("leave"); }

    @Override
    public String hasPermission() { return "mineskyevents.admin"; }

    @Override
    public void perform(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        var event = MineSkyEvents.getInstance().getEventManager().getActiveEvent();
        if (event == null) {
            Messages.send(player, "errors.no-event");
            return;
        }
        event.staffLeave(player);
    }
}
