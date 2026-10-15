package net.mineskyevents.gui;

import net.mineskyevents.entities.categories.Category;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class MapManagerGui {

    public static final String TITLE_PREFIX = "§8Mapas: ";

    public static void open(Player player, Category category) {
        Inventory inv = Bukkit.createInventory(null, 45, TITLE_PREFIX + category.getName());

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        ItemStack createBtn = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta createMeta = createBtn.getItemMeta();
        if (createMeta != null) {
            createMeta.setDisplayName("§a+ Criar Novo Mapa");
            createMeta.setLore(Arrays.asList(
                    "§7Clique para iniciar a criação de um",
                    "§7novo mapa para esta categoria."
            ));
            createBtn.setItemMeta(createMeta);
        }
        inv.setItem(4, createBtn);

        ItemStack backBtn = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backBtn.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§c← Voltar");
            backMeta.setLore(Arrays.asList("§7Voltar para a seleção de categorias."));
            backBtn.setItemMeta(backMeta);
        }
        inv.setItem(40, backBtn);

        ConfigurationSection mapsSection = category.getConfig().getConfigurationSection("mapas");
        int slot = 9;

        if (mapsSection != null) {
            for (String mapId : mapsSection.getKeys(false)) {
                if (slot >= 36) break;

                String displayName = mapsSection.getString(mapId + ".name", mapId);

                ItemStack mapItem = new ItemStack(Material.PAPER);
                ItemMeta mapMeta = mapItem.getItemMeta();
                if (mapMeta != null) {
                    mapMeta.setDisplayName("§e" + displayName);
                    mapMeta.setLore(Arrays.asList(
                            "§7ID: §f" + mapId,
                            "",
                            "§aClique Esquerdo §7para editar.",
                            "§cClique Direito §7para deletar."
                    ));
                    mapItem.setItemMeta(mapMeta);
                }
                inv.setItem(slot++, mapItem);
            }
        }

        player.openInventory(inv);
    }
}