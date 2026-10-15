package net.mineskyevents.commands;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.commands.subcommands.SubCommand;
import net.mineskyevents.commands.subcommands.events.*;
import net.mineskyevents.utils.Utils;
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
                new JoinSubCommand(),
                new LeaveSubCommand()
        ).forEach(this::register);
    }

    private void register(SubCommand subCommand) {
        subCommands.add(subCommand);
    }

    private Optional<SubCommand> getSubCommand(String name) {
        return subCommands.stream().filter(cmd -> cmd.matches(name)).findFirst();
    }

    public static void commandList(CommandSender s) {
        s.sendMessage(Utils.c("&6&lMineSkyEvents"));
        s.sendMessage(Utils.c("&8----------------------------------------"));
        subCommands.stream()
                .filter(sub -> s.hasPermission(sub.hasPermission()))
                .forEach(sub -> s.sendMessage(Utils.c("&e" + sub.getUsage() + " &8- &7" + sub.getDescription())));
        s.sendMessage(Utils.c("&8----------------------------------------"));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command cmd, @NotNull String lbl, @NotNull String[] args) {
        if (!(s instanceof Player player)) {
            s.sendMessage(Utils.c("&c❌ Este comando só pode ser executado por jogadores."));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("ajuda")) {
            commandList(s);
            return true;
        }

        getSubCommand(args[0]).ifPresentOrElse(sub -> {
            if (!player.hasPermission(sub.hasPermission())) {
                sendError(player, "&c❌ Você não tem permissão para usar este comando.");
                return;
            }
            sub.perform(player, args);
        }, () -> sendError(player, "&c❌ Comando inválido. Use &f/evento ajuda &cpara ver os comandos disponíveis."));

        return true;
    }

    public static void sendError(Player player, String message) {
        player.sendMessage(Utils.c(message));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
    }
}
