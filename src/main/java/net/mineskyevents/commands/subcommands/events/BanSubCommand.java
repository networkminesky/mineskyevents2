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

public class BanSubCommand extends SubCommand {

    @Override
    public String getName() { return "ban"; }

    @Override
    public String getDescription() { return "Ban permanente de todos os eventos."; }

    @Override
    public String getUsage() { return "/event ban <jogador> [motivo]"; }

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

        String reason = EventBanManager.joinReason(args, 2);
        Player target = Bukkit.getPlayerExact(args[1]);
        var bans = MineSkyEvents.getInstance().getBanManager();
        String staff = sender.getName();

        if (target != null) {
            bans.ban(target.getUniqueId(), target.getName(), reason, staff, -1L);
            removeAndKick(target, reason, true, "permanente");
        } else {
            bans.banByName(args[1], reason, staff, -1L);
        }

        Messages.send(sender, "admin.banned",
                Placeholder.component("player", Component.text(target != null ? target.getName() : args[1])),
                Placeholder.component("reason", Component.text(reason)));
    }

    static void removeAndKick(Player target, String reason, boolean permanent, String time) {
        var manager = MineSkyEvents.getInstance().getEventManager();
        var event = manager.getActiveEvent();
        if (event != null && (manager.isPlaying(target) || manager.isSpectating(target))) {
            event.removeFromMatch(target);
        }
        String key = permanent ? "errors.banned" : "errors.tempbanned";
        target.kick(Messages.parse(
                MineSkyEvents.getInstance().getMessagesConfig().getString(key, key),
                Placeholder.component("reason", Component.text(reason)),
                Placeholder.parsed("time", time)
        ));
    }
}
