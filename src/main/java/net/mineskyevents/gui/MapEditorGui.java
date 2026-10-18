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

public class MapEditorGui {

    public static final String TITLE_PREFIX = "§8Editar Mapa: ";

    public static void open(Player player, Category category, String mapId) {
        String mapName = category.getConfig().getString("mapas." + mapId + ".name", mapId);
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_PREFIX + mapName);

        YamlConfiguration config = category.getConfig();

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        String pathPrefix = "mapas." + mapId;
        String pos1Status = config.contains(pathPrefix + ".pos1") ? "§aDefinido" : "§cNão Definido";
        String pos2Status = config.contains(pathPrefix + ".pos2") ? "§aDefinido" : "§cNão Definido";
        String lobbyStatus = config.contains(pathPrefix + ".lobby") ? "§aDefinido" : "§cNão Definido";
        String spawnStatus = config.contains(pathPrefix + ".spawn") ? "§aDefinido" : "§cNão Definido";
        String finishStatus = config.contains(pathPrefix + ".finish") ? "§aDefinido" : "§cNão Definido";
        String barrierStatus = config.contains(pathPrefix + ".barreira.pos1") ? "§aDefinido" : "§cNão Definido";
        int cpCount = MapManager.loadCheckpoints(config, mapId).size();
        int arcoCount = MapManager.loadArcos(config, mapId).size();

        addEditorItem(inv, Material.RED_WOOL, 10, "§cDefinir Canto 1 (Pos 1)",
                "§7Define o primeiro canto da arena na sua posição atual.", "", "§7Status: " + pos1Status, "", "§e› Clique para definir!");

        addEditorItem(inv, Material.BLUE_WOOL, 12, "§bDefinir Canto 2 (Pos 2)",
                "§7Define o segundo canto da arena na sua posição atual.", "", "§7Status: " + pos2Status, "", "§e› Clique para definir!");

        addEditorItem(inv, Material.WOODEN_AXE, 11, "§2Região WorldEdit (Pos 1/2)",
                "§7Salva a seleção do WorldEdit como pos1 e pos2.", "", "§7Status: " + pos1Status + " §8| " + pos2Status, "",
                "§e› Clique com seleção WE ativa!");

        addEditorItem(inv, Material.COMPASS, 14, "§aDefinir Lobby de Espera",
                "§7Define onde os jogadores aguardarão o evento começar.", "", "§7Status: " + lobbyStatus, "", "§e› Clique para definir!");

        addEditorItem(inv, Material.BEACON, 16, "§eDefinir Spawn do Evento",
                "§7Define onde os jogadores nascerão ao iniciar a partida.", "", "§7Status: " + spawnStatus, "", "§e› Clique para definir!");

        addEditorItem(inv, Material.GOLD_BLOCK, 20, "§6Linha de Chegada (finish)",
                "§7Ponto da linha de chegada na sua posição.", "", "§7Status: " + finishStatus, "", "§e› Clique para definir!");

        addEditorItem(inv, Material.IRON_BARS, 21, "§7Barreira (WorldEdit)",
                "§7Salva a seleção WE como parede inicial.", "§7(Esconde-Esconde: some ao iniciar)", "", "§7Status: " + barrierStatus, "",
                "§e› Clique com seleção WE ativa!");

        addEditorItem(inv, Material.LIGHT_WEIGHTED_PRESSURE_PLATE, 22, "§e+ Checkpoint",
                "§7Adiciona um checkpoint na sua posição.", "", "§7Total: §f" + cpCount, "", "§e› Clique esquerdo adicionar", "§c› Clique direito limpar");

        addEditorItem(inv, Material.END_PORTAL_FRAME, 23, "§d+ Arco (Elytra)",
                "§7Adiciona um arco do percurso de elytra.", "", "§7Total: §f" + arcoCount, "", "§e› Clique esquerdo adicionar", "§c› Clique direito limpar");

        String[] teamNames = {"Vermelho", "Azul", "Verde", "Amarelo"};
        Material[] teamMats = {Material.RED_WOOL, Material.BLUE_WOOL, Material.LIME_WOOL, Material.YELLOW_WOOL};
        for (int t = 0; t < 4; t++) {
            String teamStatus = config.contains(pathPrefix + ".spawn-time-" + t) ? "§aDefinido" : "§cNão Definido";
            addEditorItem(inv, teamMats[t], 29 + t, "§fSpawn Time " + teamNames[t],
                    "§7Spawn do time na sua posição atual.", "", "§7Status: " + teamStatus, "", "§e› Clique para definir!");
        }

        for (int i = 36; i < 45; i++) {
            inv.setItem(i, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        }

        int slot = 45;
        for (CategoryOption option : category.getOptionalOptions().values()) {
            if (slot > 52) break;

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
            inv.setItem(slot++, optionItem);
        }

        ItemStack backBtn = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backBtn.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§c← Voltar");
            backMeta.setLore(Arrays.asList("§7Voltar para o gerenciador de mapas do evento."));
            backBtn.setItemMeta(backMeta);
        }
        inv.setItem(49, backBtn);

        player.openInventory(inv);
    }

    private static void addEditorItem(Inventory inv, Material mat, int slot, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        inv.setItem(slot, item);
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
