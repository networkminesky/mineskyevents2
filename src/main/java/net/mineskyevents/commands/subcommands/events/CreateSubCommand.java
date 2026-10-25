package net.mineskyevents.commands.subcommands.events;

import net.mineskyevents.commands.subcommands.SubCommand;
import net.mineskyevents.gui.CategorySelectorGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CreateSubCommand extends SubCommand {
    @Override
    public String getName() { return "criar"; }
    @Override
    public String getDescription() { return "Abre o menu interativo para criar/editar mapas de eventos."; }
    @Override
    public String getUsage() { return "/event criar"; }
    @Override
    public List<String> getAliases() {
        return List.of("create", "edit");
    }
    @Override
    public String hasPermission() { return "mineskyevents.admin"; }

    @Override
    public void perform(CommandSender sender, String[] args) {
        CategorySelectorGui.open((Player) sender);
    }
}