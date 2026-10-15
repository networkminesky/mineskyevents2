package net.mineskyevents.commands;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.commands.subcommands.SubCommand;
import net.mineskyevents.commands.subcommands.events.*;
import net.mineskyevents.utils.Messages;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class EventCommand implements CommandExecutor {

    private static final Set<SubCommand> subCommands = new HashSet<>();

    public EventCommand(MineSkyEvents plugin) {
        subCommands.clear();
        Arrays.asList(
                new CreateSubCommand(),
                new StartSubCommand(),
                new StopSubCommand(),
                new MapSubCommand(),
                new JoinSubCommand(),
                new LeaveSubCommand()
        ).forEach(subCommands::add);
    }

    private Optional<SubCommand> getSubCommand(String name) {
        return subCommands.stream().filter(cmd -> cmd.matches(name)).findFirst();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("ajuda")) {
            player.sendMessage("§b§lMineSkyEvents §8— Comandos");
            player.sendMessage("§8────────────────────────────");
            subCommands.stream()
                    .filter(sub -> player.hasPermission(sub.hasPermission()))
                    .forEach(sub -> player.sendMessage("§e" + sub.getUsage() + " §8- §7" + sub.getDescription()));
            return true;
        }

        getSubCommand(args[0]).ifPresentOrElse(sub -> {
            if (!player.hasPermission(sub.hasPermission())) {
                Messages.send(player, "errors.no-permission");
                Messages.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f);
                return;
            }
            sub.perform(player, args);
        }, () -> {
            Messages.send(player, "errors.no-permission");
            Messages.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f);
        });

        return true;
    }
}
