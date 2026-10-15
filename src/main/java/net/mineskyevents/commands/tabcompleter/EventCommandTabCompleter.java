package net.mineskyevents.commands.tabcompleter;

import net.mineskyevents.models.EventType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EventCommandTabCompleter implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("mineskyevents.admin") && !sender.hasPermission("mineskyevents.join")) {
            return List.of();
        }

        if (args.length == 1) {
            List<String> subs = new ArrayList<>(List.of("entrar", "ajuda"));
            if (sender.hasPermission("mineskyevents.admin")) {
                subs.addAll(List.of("start", "stop", "criar", "mapa", "sair"));
            }
            return filter(subs, args[0]);
        }

        if (args.length >= 2 && sender.hasPermission("mineskyevents.admin")) {
            String sub = args[0].toLowerCase();
            if (sub.equals("start") || sub.equals("iniciar")) {
                if (args.length == 2) {
                    return filter(Arrays.stream(EventType.values()).map(EventType::getId).collect(Collectors.toList()), args[1]);
                }
            }
            if (sub.equals("mapa") || sub.equals("map")) {
                if (args.length == 2) return filter(List.of("criar", "ponto", "regiao"), args[1]);
                if (args.length == 3) {
                    return filter(Arrays.stream(EventType.values()).map(EventType::getId).collect(Collectors.toList()), args[2]);
                }
                if (args.length == 5 && (args[1].equals("ponto") || args[1].equals("point"))) {
                    return filter(List.of("lobby", "spawn", "pos1", "pos2", "finish", "exit"), args[4]);
                }
            }
        }

        return List.of();
    }

    private List<String> filter(List<String> options, String input) {
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
}
