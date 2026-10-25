package net.mineskyevents.commands.subcommands.events;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.commands.subcommands.SubCommand;
import net.mineskyevents.utils.Messages;
import org.bukkit.command.CommandSender;

import java.util.List;

public class StopSubCommand extends SubCommand {

    @Override
    public String getName() { return "stop"; }

    @Override
    public String getDescription() { return "Encerra o evento ativo."; }

    @Override
    public String getUsage() { return "/event stop"; }

    @Override
    public List<String> getAliases() { return List.of("parar", "encerrar"); }

    @Override
    public String hasPermission() { return "mineskyevents.admin"; }

    @Override
    public boolean allowsConsole() { return true; }

    @Override
    public void perform(CommandSender sender, String[] args) {
        MineSkyEvents.getInstance().getEventManager().forceStop();
        Messages.send(sender, "admin.event-stopped");
    }
}
