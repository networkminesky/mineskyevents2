package net.mineskyevents.commands.subcommands.events;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.commands.subcommands.SubCommand;
import net.mineskyevents.utils.Messages;
import org.bukkit.entity.Player;

import java.util.List;

public class StopSubCommand extends SubCommand {

    @Override
    public String getName() { return "stop"; }

    @Override
    public String getDescription() { return "Encerra o evento ativo."; }

    @Override
    public String getUsage() { return "/evento stop"; }

    @Override
    public List<String> getAliases() { return List.of("parar", "encerrar"); }

    @Override
    public String hasPermission() { return "mineskyevents.admin"; }

    @Override
    public void perform(Player player, String[] args) {
        MineSkyEvents.getInstance().getEventManager().forceStop();
        Messages.send(player, "admin.event-stopped");
    }
}
