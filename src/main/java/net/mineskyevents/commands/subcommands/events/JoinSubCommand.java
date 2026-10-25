package net.mineskyevents.commands.subcommands.events;

import net.mineskyevents.commands.subcommands.SubCommand;
import net.mineskyevents.utils.EventJoinHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class JoinSubCommand extends SubCommand {

    @Override
    public String getName() { return "entrar"; }

    @Override
    public String getDescription() { return "Participa do evento ativo."; }

    @Override
    public String getUsage() { return "/event entrar"; }

    @Override
    public List<String> getAliases() { return List.of("join"); }

    @Override
    public String hasPermission() { return "mineskyevents.join"; }

    @Override
    public void perform(CommandSender sender, String[] args) {
        EventJoinHandler.join((Player) sender);
    }
}
