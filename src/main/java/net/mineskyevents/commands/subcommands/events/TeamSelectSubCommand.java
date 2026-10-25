package net.mineskyevents.commands.subcommands.events;

import net.mineskyevents.commands.subcommands.SubCommand;
import net.mineskyevents.entities.events.MiniWarsEvent;
import net.mineskyevents.gui.miniwars.MiniWarsTeamSelector;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TeamSelectSubCommand extends SubCommand {

    @Override
    public String getName() {
        return "time";
    }

    @Override
    public String getDescription() {
        return "Escolhe seu time no Mini Wars (antes do início).";
    }

    @Override
    public String getUsage() {
        return "/event time";
    }

    @Override
    public List<String> getAliases() {
        return List.of("team", "times", "teams");
    }

    @Override
    public String hasPermission() {
        return MiniWarsEvent.PERM_CHANGE_TEAMS;
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        MiniWarsTeamSelector.tryOpenGui((Player) sender);
    }
}
