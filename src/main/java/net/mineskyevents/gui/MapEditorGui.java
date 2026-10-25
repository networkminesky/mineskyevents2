package net.mineskyevents.gui;

import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.entities.categories.CategoryOption;
import net.mineskyevents.map.MapManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MapEditorGui {

    public static final String TITLE_PREFIX = "§8Editar Mapa: ";
    private static final String ACTION_PREFIX = "§8mse:";

    public static void open(Player player, Category category, String mapId) {
        String mapName = category.getConfig().getString("mapas." + mapId + ".name", mapId);
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_PREFIX + mapName);

        ItemStack glass = pane(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        YamlConfiguration config = category.getConfig();
        String pathPrefix = "mapas." + mapId;
        Set<MapEditorAction> actions = MapEditorProfile.actionsFor(MapEditorProfile.resolve(category.getId()));

        int slot = 10;
        for (MapEditorAction action : MapEditorAction.values()) {
            if (!actions.contains(action)) continue;
            if (slot > 34) break;
            inv.setItem(slot++, createActionItem(action, config, pathPrefix, mapId));
            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
        }

        for (int i = 36; i < 45; i++) {
            inv.setItem(i, pane(Material.BLACK_STAINED_GLASS_PANE));
        }

        int optionSlot = 45;
        for (CategoryOption option : category.getOptionalOptions().values()) {
            if (optionSlot > 52) break;
            boolean enabled = config.getBoolean("mapas." + mapId + ".options." + option.getId(), option.isDefaultEnabled());
            Material itemMat = enabled ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
            String statusStr = enabled ? "§aAtivado" : "§cDesativado";

            List<String> lore = new ArrayList<>();
            lore.add("§7Opção: §f" + option.getDisplayName());
            for (String desc : option.getDescription()) {
                lore.add("§8" + desc);
            }
            lore.add("");
            lore.add("§7Estado Atual: " + statusStr);
            lore.add("");
            lore.add("§e› Clique para alternar!");

            ItemStack optionItem = new ItemStack(itemMat);
            ItemMeta meta = optionItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§b" + option.getDisplayName());
                meta.setLore(lore);
                optionItem.setItemMeta(meta);
            }
            inv.setItem(optionSlot++, optionItem);
        }

        ItemStack backBtn = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backBtn.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§c← Voltar");
            backMeta.setLore(List.of("§7Voltar para o gerenciador de mapas do evento."));
            backBtn.setItemMeta(backMeta);
        }
        inv.setItem(49, backBtn);

        player.openInventory(inv);
    }

    public static MapEditorAction readAction(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return null;
        for (String line : lore) {
            if (line != null && line.startsWith(ACTION_PREFIX)) {
                try {
                    return MapEditorAction.valueOf(line.substring(ACTION_PREFIX.length()));
                } catch (IllegalArgumentException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private static ItemStack createActionItem(MapEditorAction action, YamlConfiguration config, String pathPrefix, String mapId) {
        return switch (action) {
            case POS1 -> pointItem(Material.RED_WOOL, "§cCanto 1 da arena",
                    "§7Primeiro canto (região / espectadores).", status(config, pathPrefix + ".pos1"), MapEditorAction.POS1);
            case POS2 -> pointItem(Material.BLUE_WOOL, "§bCanto 2 da arena",
                    "§7Segundo canto (região / espectadores).", status(config, pathPrefix + ".pos2"), MapEditorAction.POS2);
            case REGION_WE -> pointItem(Material.WOODEN_AXE, "§2Região (WorldEdit)",
                    "§7Salva seleção WE como pos1 e pos2.", "§7Requer WorldEdit", MapEditorAction.REGION_WE);
            case LOBBY -> pointItem(Material.COMPASS, "§aLobby de espera",
                    "§7Onde os jogadores aguardam o início.", status(config, pathPrefix + ".lobby"), MapEditorAction.LOBBY);
            case SPAWN -> pointItem(Material.BEACON, "§eSpawn padrão",
                    "§7Spawn geral / referência do mapa.", status(config, pathPrefix + ".spawn"), MapEditorAction.SPAWN);
            case FINISH -> pointItem(Material.GOLD_BLOCK, "§6Linha de chegada",
                    "§7Meta de corridas.", status(config, pathPrefix + ".finish"), MapEditorAction.FINISH);
            case EXIT -> pointItem(Material.IRON_DOOR, "§fSaída (Runners)",
                    "§7Saída do labirinto.", status(config, pathPrefix + ".exit"), MapEditorAction.EXIT);
            case BARRIER_WE -> pointItem(Material.IRON_BARS, "§7Barreira (WorldEdit)",
                    "§7Parede inicial (Esconde-Esconde).", status(config, pathPrefix + ".barreira.pos1"), MapEditorAction.BARRIER_WE);
            case CHECKPOINT -> listItem(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, "§eCheckpoints",
                    "§7Percurso com voltas.", "§7Total: §f" + MapManager.loadCheckpoints(config, mapId).size(),
                    MapEditorAction.CHECKPOINT, "§eEsquerdo: +1", "§cDireito: limpar");
            case ARCO -> listItem(Material.END_PORTAL_FRAME, "§dArcos (Elytra)",
                    "§7Portais do percurso aéreo.", "§7Total: §f" + MapManager.loadArcos(config, mapId).size(),
                    MapEditorAction.ARCO, "§eEsquerdo: +1", "§cDireito: limpar");
            case TEAM_SPAWN_0 -> teamItem(Material.RED_WOOL, "Vermelho", 0, config, pathPrefix);
            case TEAM_SPAWN_1 -> teamItem(Material.BLUE_WOOL, "Azul", 1, config, pathPrefix);
            case TEAM_SPAWN_2 -> teamItem(Material.LIME_WOOL, "Verde", 2, config, pathPrefix);
            case TEAM_SPAWN_3 -> teamItem(Material.YELLOW_WOOL, "Amarelo", 3, config, pathPrefix);
            case CANNON_0 -> cannonItem(0, "Vermelho", Material.RED_CONCRETE, config, pathPrefix);
            case CANNON_1 -> cannonItem(1, "Azul", Material.BLUE_CONCRETE, config, pathPrefix);
            case CANNON_2 -> cannonItem(2, "Verde", Material.LIME_CONCRETE, config, pathPrefix);
            case CANNON_3 -> cannonItem(3, "Amarelo", Material.YELLOW_CONCRETE, config, pathPrefix);
        };
    }

    private static ItemStack teamItem(Material mat, String teamName, int index, YamlConfiguration config, String pathPrefix) {
        return pointItem(mat, "§fSpawn · Time " + teamName,
                "§7Onde o time nasce ao começar a partida.",
                status(config, pathPrefix + ".spawn-time-" + index),
                teamSpawnAction(index));
    }

    private static ItemStack cannonItem(int index, String teamName, Material mat, YamlConfiguration config, String pathPrefix) {
        return pointItem(mat, "§fCanhão · Time " + teamName,
                "§7Posição e direção do canhão (olhe para onde ele aponta).",
                status(config, pathPrefix + ".cannon-" + index),
                cannonAction(index));
    }

    private static MapEditorAction teamSpawnAction(int index) {
        return switch (index) {
            case 0 -> MapEditorAction.TEAM_SPAWN_0;
            case 1 -> MapEditorAction.TEAM_SPAWN_1;
            case 2 -> MapEditorAction.TEAM_SPAWN_2;
            default -> MapEditorAction.TEAM_SPAWN_3;
        };
    }

    private static MapEditorAction cannonAction(int index) {
        return switch (index) {
            case 0 -> MapEditorAction.CANNON_0;
            case 1 -> MapEditorAction.CANNON_1;
            case 2 -> MapEditorAction.CANNON_2;
            default -> MapEditorAction.CANNON_3;
        };
    }

    private static String status(YamlConfiguration config, String path) {
        return config.contains(path) ? "§aDefinido" : "§cNão definido";
    }

    private static ItemStack pointItem(Material mat, String name, String desc, String extra, MapEditorAction action) {
        return buildItem(mat, name, List.of(desc, "", extra, "", "§e› Clique para definir na sua posição", ACTION_PREFIX + action.name()));
    }

    private static ItemStack listItem(Material mat, String name, String desc, String countLine, MapEditorAction action, String left, String right) {
        return buildItem(mat, name, List.of(desc, "", countLine, "", left, right, ACTION_PREFIX + action.name()));
    }

    private static ItemStack buildItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack pane(Material mat) {
        ItemStack glass = new ItemStack(mat);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }
        return glass;
    }

    public static void saveLocation(YamlConfiguration config, String path, Location loc) {
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", (double) loc.getYaw());
        config.set(path + ".pitch", (double) loc.getPitch());
    }
}
