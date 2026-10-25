package net.mineskyevents.gui;

import net.mineskyevents.entities.EventBuilder;
import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.entities.categories.CategoryHandler;
import net.mineskyevents.entities.categories.CategoryOption;
import net.mineskyevents.map.MapManager;
import net.mineskyevents.map.WorldEditHelper;
import net.mineskyevents.utils.Messages;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CategoryGuiListener implements Listener {

    private static final Map<UUID, Category> activeCreators = new HashMap<>();

    private static final Map<UUID, EditingSession> activeEditors = new HashMap<>();

    public static class EditingSession {
        private final Category category;
        private final String mapId;

        public EditingSession(Category category, String mapId) {
            this.category = category;
            this.mapId = mapId;
        }

        public Category getCategory() { return category; }
        public String getMapId() { return mapId; }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();

        // --- 1. Lógica do Menu de Seleção de Categoria ---
        if (title.equals(CategorySelectorGui.TITLE)) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR || !clicked.hasItemMeta()) return;

            String displayName = clicked.getItemMeta().getDisplayName();
            String categoryName = org.bukkit.ChatColor.stripColor(displayName);

            Category category = CategoryHandler.getCategory(categoryName);
            if (category != null) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                MapManagerGui.open(player, category);
            }
            return;
        }

        if (title.startsWith(MapManagerGui.TITLE_PREFIX)) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR || !clicked.hasItemMeta()) return;

            String categoryName = title.substring(MapManagerGui.TITLE_PREFIX.length());
            Category category = CategoryHandler.getCategory(categoryName);
            if (category == null) return;

            if (clicked.getType() == Material.ARROW) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                CategorySelectorGui.open(player);
                return;
            }

            if (clicked.getType() == Material.EMERALD_BLOCK) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                player.closeInventory();
                activeCreators.put(player.getUniqueId(), category);

                player.sendMessage("");
                player.sendMessage("§a§l[Criação de Mapa]");
                player.sendMessage("§7Digite o nome amigável do mapa no chat.");
                player.sendMessage("§7Ou digite §ccancelar §7para abortar a operação.");
                player.sendMessage("");
                return;
            }

            if (clicked.getType() == Material.PAPER) {
                String mapName = org.bukkit.ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

                String mapId = null;
                if (clicked.getItemMeta().getLore() != null) {
                    for (String line : clicked.getItemMeta().getLore()) {
                        String stripped = org.bukkit.ChatColor.stripColor(line);
                        if (stripped.startsWith("ID: ")) {
                            mapId = stripped.substring(4);
                            break;
                        }
                    }
                }

                if (mapId == null) return;

                if (event.getClick() == ClickType.RIGHT) {
                    category.getConfig().set("mapas." + mapId, null);
                    category.reloadFile();

                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    player.sendMessage("§cO mapa §l" + mapName + " §cfoi deletado com sucesso.");
                    MapManagerGui.open(player, category);
                } else {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    activeEditors.put(player.getUniqueId(), new EditingSession(category, mapId));
                    MapEditorGui.open(player, category, mapId);
                }
                return;
            }
        }

        if (title.startsWith(MapEditorGui.TITLE_PREFIX)) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR || !clicked.hasItemMeta()) return;

            EditingSession session = activeEditors.get(player.getUniqueId());
            if (session == null) return;

            Category category = session.getCategory();
            String mapId = session.getMapId();
            YamlConfiguration config = category.getConfig();

            if (clicked.getType() == Material.ARROW) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                activeEditors.remove(player.getUniqueId());
                MapManagerGui.open(player, category);
                return;
            }

            MapEditorAction editorAction = MapEditorGui.readAction(clicked);
            if (editorAction != null) {
                MapEditorHandler.handle(player, category, mapId, editorAction, event.getClick());
                MapEditorGui.open(player, category, mapId);
                return;
            }

            if (clicked.getType() == Material.LIME_CONCRETE || clicked.getType() == Material.RED_CONCRETE) {
                String optionName = org.bukkit.ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

                CategoryOption option = null;
                for (CategoryOption o : category.getOptionalOptions().values()) {
                    if (o.getDisplayName().equalsIgnoreCase(optionName)) {
                        option = o;
                        break;
                    }
                }

                if (option != null) {
                    String path = "mapas." + mapId + ".options." + option.getId();
                    boolean currentValue = config.getBoolean(path, option.isDefaultEnabled());
                    config.set(path, !currentValue);
                    category.reloadFile();

                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    player.sendMessage("§e[Opção] §f" + option.getDisplayName() + " §7foi alterado para: " + (!currentValue ? "§aAtivado" : "§cDesativado"));
                    MapEditorGui.open(player, category, mapId);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Category category = activeCreators.get(player.getUniqueId());
        if (category == null) return;

        event.setCancelled(true);
        String input = event.getMessage().trim();

        if (input.equalsIgnoreCase("cancelar")) {
            activeCreators.remove(player.getUniqueId());
            player.sendMessage("§cCriação de mapa cancelada.");
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            return;
        }

        EventBuilder builder = new EventBuilder(category);
        builder.setDisplayName(input);
        String mapId = builder.generateId();

        if (mapId.isEmpty()) {
            player.sendMessage("§cNome inválido. Digite um nome comum.");
            return;
        }

        activeCreators.remove(player.getUniqueId());

        category.getConfig().set("mapas." + mapId + ".name", input);
        category.reloadFile();

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.sendMessage("§aMapa §l" + input + " §acriado com sucesso!");

        activeEditors.put(player.getUniqueId(), new EditingSession(category, mapId));

        org.bukkit.Bukkit.getScheduler().runTask(net.mineskyevents.MineSkyEvents.getInstance(), () -> {
            MapEditorGui.open(player, category, mapId);
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        activeCreators.remove(event.getPlayer().getUniqueId());
        activeEditors.remove(event.getPlayer().getUniqueId());
    }
}