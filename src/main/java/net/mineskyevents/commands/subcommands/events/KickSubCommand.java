package net.mineskyevents.commands.subcommands.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.commands.subcommands.SubCommand;
import net.mineskyevents.managers.EventBanManager;
import net.mineskyevents.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class KickSubCommand extends SubCommand {

    @Override
    public String getName() { return "kick"; }

    @Override
    public String getDescription() { return "Remove um jogador do evento."; }

    @Override
    public String getUsage() { return "/event kick <jogador> [motivo]"; }

    @Override
    public List<String> getAliases() { return List.of(); }

    @Override
    public String hasPermission() { return "mineskyevents.admin"; }

    @Override
    public boolean allowsConsole() { return true; }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUso: " + getUsage());
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            Messages.send(sender, "errors.player-offline",
                    Placeholder.component("player", Component.text(args[1])));
            return;
        }

        var manager = MineSkyEvents.getInstance().getEventManager();
        if (!manager.isPlaying(target) && !manager.isSpectating(target)) {
            Messages.send(sender, "errors.target-not-in-event",
                    Placeholder.component("player", Component.text(target.getName())));
            return;
        }

        String reason = EventBanManager.joinReason(args, 2);
        var event = manager.getActiveEvent();
        if (event != null) {
            event.kickFromEvent(target, reason);
        }
        Messages.send(sender, "admin.kicked",
                Placeholder.component("player", Component.text(target.getName())),
                Placeholder.component("reason", Component.text(reason)));
    }
}
