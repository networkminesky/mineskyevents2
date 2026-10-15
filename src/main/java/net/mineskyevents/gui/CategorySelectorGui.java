package net.mineskyevents.gui;

import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.entities.categories.CategoryHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class CategorySelectorGui {
    public static final String TITLE = "§8Categorias de Eventos";

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, TITLE);

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        int index = 0;

        for (Category category : CategoryHandler.categories) {
            if (index >= slots.length) break;

            ItemStack item = new ItemStack(category.getDisplayItem());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§a" + category.getName());
                meta.setLore(Arrays.asList(
                        "§7ID do Evento: §f" + category.getId(),
                        "",
                        "§e› Clique para gerenciar os mapas!"
                ));
                item.setItemMeta(meta);
            }

            inv.setItem(slots[index++], item);
        }

        player.openInventory(inv);
    }
}
