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
    public String getUsage() { return "/evento mapa <criar|ponto|regiao> ..."; }

    @Override
    public List<String> getAliases() { return List.of("map"); }

    @Override
    public String hasPermission() { return "mineskyevents.admin"; }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUso: " + getUsage());
            player.sendMessage("§7  /evento mapa criar <tipo> <id> <nome>");
            player.sendMessage("§7  /evento mapa ponto <tipo> <id> <lobby|spawn|pos1|pos2|finish|exit>");
            player.sendMessage("§7  /evento mapa regiao <tipo> <id>");
            return;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "criar", "create" -> handleCreate(player, args);
            case "ponto", "point", "set" -> handlePoint(player, args);
            case "regiao", "region", "we" -> handleRegion(player, args);
            default -> player.sendMessage("§cAção desconhecida. Use criar, ponto ou regiao.");
        }
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
        if (!WorldEditHelper.isAvailable()) {
            Messages.send(player, "admin.no-worldedit");
            return;
        }
        EventType type = EventType.fromId(args[2]);
        if (type == null) {
            player.sendMessage("§cTipo de evento inválido.");
            return;
        }
        String mapId = args[3];

        if (WorldEditHelper.getSelection(player) == null) {
            Messages.send(player, "admin.no-worldedit");
            return;
        }

        WorldEditHelper.saveSelectionToMap(player, type.getId(), mapId);
        Messages.send(player, "admin.region-saved", Placeholder.parsed("map", mapId));
    }
}
