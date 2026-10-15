package net.mineskyevents.gui;

import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.entities.categories.CategoryOption;
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
        Inventory inv = Bukkit.createInventory(null, 45, TITLE_PREFIX + mapName);

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

        addEditorItem(inv, Material.RED_WOOL, 10, "§cDefinir Canto 1 (Pos 1)",
                "§7Define o primeiro canto da arena na sua posição atual.", "", "§7Status: " + pos1Status, "", "§e› Clique para definir!");

        addEditorItem(inv, Material.BLUE_WOOL, 12, "§bDefinir Canto 2 (Pos 2)",
                "§7Define o segundo canto da arena na sua posição atual.", "", "§7Status: " + pos2Status, "", "§e› Clique para definir!");

        addEditorItem(inv, Material.COMPASS, 14, "§aDefinir Lobby de Espera",
                "§7Define onde os jogadores aguardarão o evento começar.", "", "§7Status: " + lobbyStatus, "", "§e› Clique para definir!");

        addEditorItem(inv, Material.BEACON, 16, "§eDefinir Spawn do Evento",
                "§7Define onde os jogadores nascerão ao iniciar a partida.", "", "§7Status: " + spawnStatus, "", "§e› Clique para definir!");

        for (int i = 18; i < 27; i++) {
            inv.setItem(i, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        }

        int slot = 28;
        for (CategoryOption option : category.getOptionalOptions().values()) {
            if (slot > 34) break; // Limite de 7 slots centrais

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
        inv.setItem(40, backBtn);

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