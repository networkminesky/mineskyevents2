package net.mineskyevents.commands.tabcompleter;

import net.mineskyevents.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventCommandTabCompleter implements TabCompleter {
    private final List<String> id = Arrays.asList("anunciar", "blacklist", "entrar", "kick", "set", "start", "finalizar");
    private final List<String> names = Arrays.asList("Spleef", "TijolãoWars", "Corrida", "TNTRun", "CorridaBoat", "Sumo", "TNTTag", "Parapente", "CaptureBandeira");
    private final List<String> namesA = Arrays.asList("Mini-Wars", "Esconde-esconde", "Ruínas", "CopaPVP");
    private final List<String> namesF = new ArrayList<>();

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String lbl, String[] args) {
        namesF.addAll(namesA);
        namesF.addAll(names);
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions = getMatches(args[0], id);
            if (args[0].isEmpty()) {
                completions.clear();
                addIfPermitted(s, completions, "mineskyevents.command.event.anunciar", "anunciar");
                addIfPermitted(s, completions, "mineskyevents.command.event.blacklist", "blacklist");
                completions.add("entrar");
                addIfPermitted(s, completions, "mineskyevents.command.event.kick", "kick");
                addIfPermitted(s, completions, "mineskyevents.command.event.set", "set");
                addIfPermitted(s, completions, "mineskyevents.command.event.start", "start");
                addIfPermitted(s, completions, "mineskyevents.command.event.finalizar", "finalizar");
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("blacklist")) {
                completions = getMatches(args[1], Arrays.asList("Adicionar", "Remover"), s, "mineskyevents.command.event.blacklist");
            } else if (args[0].equalsIgnoreCase("start")) {
                completions = getMatches(args[1], names, s, "mineskyevents.command.event.start");
            } else if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("anunciar")) {
                completions = getMatches(args[1], namesF, s, "mineskyevents.command.event." + args[0]);
            } else if (args[0].equalsIgnoreCase("finalizar")) {
                completions = getMatches(args[1], namesF, s, "mineskyevents.command.event.finalizar");
            } else if (args[0].equalsIgnoreCase("kick")) {
                //completions = getMatches(args[1], Util.PVE(), s, "mineskyevents.command.event.kick");
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("anunciar")) {
                completions.clear();
                if (!s.hasPermission("mineskyevents.command.event.anunciar")) return completions;
                switch (args[2]) {
                    case "Mini-Wars":
                        completions.addAll(Arrays.asList("1", "2", "3", "4", "5"));
                        break;
                    case "CopaPVP":
                        completions.add("1");
                        break;
                    case "Esconde-Esconde":
                        completions.addAll(Arrays.asList("1", "2"));
                        break;
                    case "Ruínas":
                        completions.add("1");
                        break;
                    default:
                        break;
                }
            } else if (args[0].equalsIgnoreCase("set")) {
                completions = Arrays.asList("spawn", "arena");
            } else if (args[0].equalsIgnoreCase("blacklist")) {
                completions = getMatches(args[2], names, s, "mineskyevents.command.event.blacklist");
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("blacklist")) {
                completions = getMatches(args[3], Utils.getOnlinePlayerNames());
            } else if (args[0].equalsIgnoreCase("set")) {
                completions = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
            }
        } else if (args.length == 5 && args[0].equalsIgnoreCase("blacklist")) {
            completions = Arrays.asList("IP");
        }

        return completions;
    }

    private List<String> getMatches(String input, List<String> possibilities) {
        List<String> matches = new ArrayList<>();
        String inputLowerCase = input.toLowerCase();
        for (String possibility : possibilities) {
            if (possibility.toLowerCase().startsWith(inputLowerCase)) {
                matches.add(possibility);
            }
        }
        return matches;
    }

    private List<String> getMatches(String input, List<String> possibilities, CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) return new ArrayList<>();
        return getMatches(input, possibilities);
    }

    private void addIfPermitted(CommandSender sender, List<String> completions, String permission, String command) {
        if (sender.hasPermission(permission)) {
            completions.add(command);
        }
    }
}
