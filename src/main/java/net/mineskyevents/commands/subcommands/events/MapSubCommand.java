package net.mineskyevents.commands.subcommands.events;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.mineskyevents.commands.subcommands.SubCommand;
import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.entities.categories.CategoryHandler;
import net.mineskyevents.map.MapManager;
import net.mineskyevents.map.WorldEditHelper;
import net.mineskyevents.models.EventType;
import net.mineskyevents.utils.Messages;
import org.bukkit.entity.Player;

import java.util.List;

public class MapSubCommand extends SubCommand {

    @Override
    public String getName() { return "mapa"; }

    @Override
    public String getDescription() { return "Gerencia mapas de eventos."; }

    @Override
    public String getUsage() { return "/evento mapa <criar|ponto|regiao|checkpoint|arco|barreira> ..."; }

    @Override
    public List<String> getAliases() { return List.of("map"); }

    @Override
    public String hasPermission() { return "mineskyevents.admin"; }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 2) {
            sendHelp(player);
            return;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "criar", "create" -> handleCreate(player, args);
            case "ponto", "point", "set" -> handlePoint(player, args);
            case "regiao", "region", "we" -> handleRegion(player, args);
            case "barreira", "barrier" -> handleBarrier(player, args);
            case "checkpoint", "cp" -> handleCheckpoint(player, args);
            case "arco", "ring" -> handleArco(player, args);
            default -> player.sendMessage("§cAção desconhecida. Use criar, ponto, regiao, checkpoint, arco ou barreira.");
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage("§cUso: " + getUsage());
        player.sendMessage("§7  /evento mapa criar <tipo> <id> <nome>");
        player.sendMessage("§7  /evento mapa ponto <tipo> <id> <lobby|spawn|pos1|pos2|finish|exit|spawn-time-0..3>");
        player.sendMessage("§7  /evento mapa regiao <tipo> <id>  §8(WorldEdit → pos1/pos2)");
        player.sendMessage("§7  /evento mapa barreira <tipo> <id>  §8(WorldEdit → parede inicial)");
        player.sendMessage("§7  /evento mapa checkpoint <tipo> <id> [adicionar|limpar]");
        player.sendMessage("§7  /evento mapa arco <tipo> <id> [adicionar|limpar]");
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage("§cUso: /evento mapa criar <tipo> <id> <nome>");
            return;
        }
        EventType type = EventType.fromId(args[2]);
        if (type == null) {
            player.sendMessage("§cTipo de evento inválido: " + args[2]);
            return;
        }
        String mapId = args[3];
        String name = args[4];
        MapManager.createMap(type, mapId, name);
        Messages.send(player, "admin.map-created",
                Placeholder.parsed("map", mapId),
                Placeholder.parsed("event", type.getDisplayName()));
    }

    private void handlePoint(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage("§cUso: /evento mapa ponto <tipo> <id> <ponto>");
            return;
        }
        EventType type = EventType.fromId(args[2]);
        if (type == null) {
            player.sendMessage("§cTipo de evento inválido.");
            return;
        }
        String mapId = args[3];
        String point = args[4].toLowerCase();

        MapManager.savePoint(type.getId(), mapId, point, player.getLocation());
        Messages.send(player, "admin.map-saved",
                Placeholder.parsed("point", point),
                Placeholder.parsed("map", mapId));
    }

    private void handleRegion(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage("§cUso: /evento mapa regiao <tipo> <id>");
            return;
        }
        if (!requireWorldEditSelection(player)) return;
        EventType type = EventType.fromId(args[2]);
        if (type == null) {
            player.sendMessage("§cTipo de evento inválido.");
            return;
        }
        String mapId = args[3];

        WorldEditHelper.saveSelectionToMap(player, type.getId(), mapId);
        Messages.send(player, "admin.region-saved", Placeholder.parsed("map", mapId));
    }

    private void handleBarrier(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage("§cUso: /evento mapa barreira <tipo> <id>");
            return;
        }
        if (!requireWorldEditSelection(player)) return;
        EventType type = EventType.fromId(args[2]);
        if (type == null) {
            player.sendMessage("§cTipo de evento inválido.");
            return;
        }
        String mapId = args[3];

        WorldEditHelper.saveBarrierSelection(player, type.getId(), mapId);
        Messages.send(player, "admin.barrier-saved", Placeholder.parsed("map", mapId));
    }

    private void handleCheckpoint(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage("§cUso: /evento mapa checkpoint <tipo> <id> [adicionar|limpar]");
            return;
        }
        EventType type = EventType.fromId(args[2]);
        if (type == null) {
            player.sendMessage("§cTipo de evento inválido.");
            return;
        }
        String mapId = args[3];
        String sub = args.length >= 5 ? args[4].toLowerCase() : "adicionar";

        if (sub.equals("limpar") || sub.equals("clear")) {
            MapManager.clearLocationList(type.getId(), mapId, "checkpoints");
            player.sendMessage("§aCheckpoints do mapa §f" + mapId + " §alimpos.");
            return;
        }

        MapManager.appendLocation(type.getId(), mapId, "checkpoints", player.getLocation());
        Category category = CategoryHandler.getCategoryById(type.getId());
        int count = category != null
                ? MapManager.loadCheckpoints(category.getConfig(), mapId).size()
                : 0;
        player.sendMessage("§aCheckpoint §f#" + count + " §aadicionado ao mapa §f" + mapId + "§a.");
    }

    private void handleArco(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage("§cUso: /evento mapa arco <tipo> <id> [adicionar|limpar]");
            return;
        }
        EventType type = EventType.fromId(args[2]);
        if (type == null) {
            player.sendMessage("§cTipo de evento inválido.");
            return;
        }
        String mapId = args[3];
        String sub = args.length >= 5 ? args[4].toLowerCase() : "adicionar";

        if (sub.equals("limpar") || sub.equals("clear")) {
            MapManager.clearLocationList(type.getId(), mapId, "arcos");
            player.sendMessage("§aArcos do mapa §f" + mapId + " §alimpos.");
            return;
        }

        MapManager.appendLocation(type.getId(), mapId, "arcos", player.getLocation());
        Category category = CategoryHandler.getCategoryById(type.getId());
        int count = category != null
                ? MapManager.loadArcos(category.getConfig(), mapId).size()
                : 0;
        player.sendMessage("§aArco §f#" + count + " §aadicionado ao mapa §f" + mapId + "§a.");
    }

    private boolean requireWorldEditSelection(Player player) {
        if (!WorldEditHelper.isAvailable()) {
            Messages.send(player, "admin.no-worldedit");
            return false;
        }
        if (WorldEditHelper.getSelection(player) == null) {
            Messages.send(player, "admin.no-worldedit");
            return false;
        }
        return true;
    }
}
