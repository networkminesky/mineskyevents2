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

public class TempBanSubCommand extends SubCommand {

    @Override
    public String getName() { return "tempban"; }

    @Override
    public String getDescription() { return "Ban temporário de eventos (1d, 12h, 10m)."; }

    @Override
    public String getUsage() { return "/event tempban <jogador> <tempo> [motivo]"; }

    @Override
    public List<String> getAliases() { return List.of("tempoban"); }

    @Override
    public String hasPermission() { return "mineskyevents.admin"; }

    @Override
    public boolean allowsConsole() { return true; }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUso: " + getUsage());
            return;
        }

        long duration = EventBanManager.parseDurationMillis(args[2]);
        if (duration <= 0) {
            Messages.send(sender, "errors.invalid-duration");
            return;
        }

        String reason = EventBanManager.joinReason(args, 3);
        long expires = System.currentTimeMillis() + duration;
        Player target = Bukkit.getPlayerExact(args[1]);
        var bans = MineSkyEvents.getInstance().getBanManager();
        String staff = sender.getName();

        if (target != null) {
            bans.ban(target.getUniqueId(), target.getName(), reason, staff, expires);
            BanSubCommand.removeAndKick(target, reason, false, args[2]);
        } else {
            bans.banByName(args[1], reason, staff, expires);
        }

        Messages.send(sender, "admin.tempbanned",
                Placeholder.component("player", Component.text(target != null ? target.getName() : args[1])),
                Placeholder.parsed("time", args[2]),
                Placeholder.component("reason", Component.text(reason)));
    }
}
