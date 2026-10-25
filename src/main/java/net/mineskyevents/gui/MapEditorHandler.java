package net.mineskyevents.gui;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.map.MapManager;
import net.mineskyevents.map.WorldEditHelper;
import net.mineskyevents.utils.Messages;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public final class MapEditorHandler {

    private MapEditorHandler() {}

    public static void handle(Player player, Category category, String mapId, MapEditorAction action, ClickType click) {
        YamlConfiguration config = category.getConfig();
        String base = "mapas." + mapId;

        switch (action) {
            case POS1 -> {
                MapEditorGui.saveLocation(config, base + ".pos1", player.getLocation());
                feedback(player, "Canto 1 da arena definido.");
            }
            case POS2 -> {
                MapEditorGui.saveLocation(config, base + ".pos2", player.getLocation());
                feedback(player, "Canto 2 da arena definido.");
            }
            case REGION_WE -> {
                if (!WorldEditHelper.isAvailable() || WorldEditHelper.getSelection(player) == null) {
                    Messages.send(player, "admin.no-worldedit");
                    return;
                }
                WorldEditHelper.saveSelectionToMap(player, category.getId(), mapId);
                Messages.send(player, "admin.region-saved", Placeholder.parsed("map", mapId));
                return;
            }
            case LOBBY -> {
                MapEditorGui.saveLocation(config, base + ".lobby", player.getLocation());
                feedback(player, "Lobby de espera definido.");
            }
            case SPAWN -> {
                MapEditorGui.saveLocation(config, base + ".spawn", player.getLocation());
                feedback(player, "Spawn definido.");
            }
            case FINISH -> {
                MapEditorGui.saveLocation(config, base + ".finish", player.getLocation());
                feedback(player, "Linha de chegada definida.");
            }
            case EXIT -> {
                MapEditorGui.saveLocation(config, base + ".exit", player.getLocation());
                feedback(player, "Saída do labirinto definida.");
            }
            case BARRIER_WE -> {
                if (!WorldEditHelper.isAvailable() || WorldEditHelper.getSelection(player) == null) {
                    Messages.send(player, "admin.no-worldedit");
                    return;
                }
                WorldEditHelper.saveBarrierSelection(player, category.getId(), mapId);
                Messages.send(player, "admin.barrier-saved", Placeholder.parsed("map", mapId));
                return;
            }
            case CHECKPOINT -> {
                if (click == ClickType.RIGHT) {
                    MapManager.clearLocationList(category.getId(), mapId, "checkpoints");
                    player.sendMessage("§cCheckpoints limpos.");
                } else {
                    MapManager.appendLocation(category.getId(), mapId, "checkpoints", player.getLocation());
                    int count = MapManager.loadCheckpoints(config, mapId).size();
                    player.sendMessage("§aCheckpoint §f#" + count + " §aadicionado.");
                }
            }
            case ARCO -> {
                if (click == ClickType.RIGHT) {
                    MapManager.clearLocationList(category.getId(), mapId, "arcos");
                    player.sendMessage("§cArcos limpos.");
                } else {
                    MapManager.appendLocation(category.getId(), mapId, "arcos", player.getLocation());
                    int count = MapManager.loadArcos(config, mapId).size();
                    player.sendMessage("§aArco §f#" + count + " §aadicionado.");
                }
            }
            case TEAM_SPAWN_0 -> saveTeamSpawn(player, config, base, 0);
            case TEAM_SPAWN_1 -> saveTeamSpawn(player, config, base, 1);
            case TEAM_SPAWN_2 -> saveTeamSpawn(player, config, base, 2);
            case TEAM_SPAWN_3 -> saveTeamSpawn(player, config, base, 3);
            case CANNON_0 -> saveCannon(player, config, base, 0);
            case CANNON_1 -> saveCannon(player, config, base, 1);
            case CANNON_2 -> saveCannon(player, config, base, 2);
            case CANNON_3 -> saveCannon(player, config, base, 3);
        }
        category.reloadFile();
    }

    private static void saveTeamSpawn(Player player, YamlConfiguration config, String base, int team) {
        MapEditorGui.saveLocation(config, base + ".spawn-time-" + team, player.getLocation());
        feedback(player, "Spawn do time " + teamName(team) + " definido.");
    }

    private static void saveCannon(Player player, YamlConfiguration config, String base, int team) {
        MapEditorGui.saveLocation(config, base + ".cannon-" + team, player.getLocation());
        feedback(player, "Canhão do time " + teamName(team) + " definido (direção: seu olhar).");
    }

    private static String teamName(int team) {
        return switch (team) {
            case 0 -> "Vermelho";
            case 1 -> "Azul";
            case 2 -> "Verde";
            default -> "Amarelo";
        };
    }

    private static void feedback(Player player, String message) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 1.0f);
        player.sendMessage("§a[!] " + message);
    }
}
