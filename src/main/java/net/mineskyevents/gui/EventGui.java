package net.mineskyevents.gui;

import net.md_5.bungee.api.ChatColor;
import net.mineskyevents.utils.EventItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EventGui {
    private static Inventory inventorySHOP_INICIAL = null;
    private static Inventory inventorySHOP_BLOCOS = null;
    private static Inventory inventorySHOP_COMBATE = null;
    private static Inventory inventorySHOP_POÇOES = null;
    private static Inventory inventorySHOP_FERRAMENTAS = null;
    private static Inventory inventorySHOP_ATAQUE = null;
    private static Inventory inventorySHOP_OUTROS = null;
    private static Inventory inventoryUPGRADE = null;

    public static final Map<Player, Integer> upgradeArmor = new HashMap<>();
    public static final Map<Player, Integer> upgradeSWORD = new HashMap<>();
    public static final Map<Player, Integer> upgradePICKAXE = new HashMap<>();
    public static final Map<Player, Integer> upgradeAXE = new HashMap<>();

    public static void ShopGUI_INICIAL() {
        inventorySHOP_INICIAL = Bukkit.createInventory(null, 54, "Loja - Menu Principal");
        addNavigation(inventorySHOP_INICIAL, 37);

        addItem(inventorySHOP_INICIAL, Material.WHITE_WOOL, 16, "§f→ Lã Branca", 10, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "16", "", ChatColor.of("#a2c4c9") + "Preço§8: §f4 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_INICIAL, Material.GOLDEN_APPLE, 1, "§6→ Maçã Dourada", 19, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §63 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_INICIAL, Material.OAK_PLANKS, 16, "§e→ Madeira", 28, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "16", "", ChatColor.of("#a2c4c9") + "Preço§8: §64 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_INICIAL, Material.WOODEN_PICKAXE, 1, "§e→ Picareta de Madeira", Map.of(Enchantment.EFFICIENCY, 1), 11, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §f10 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_INICIAL, Material.TNT, 1, "§c→ TNT", 20, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §64 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_INICIAL, Material.END_STONE, 12, "§e→ Pedra do END", 29, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "12", "", ChatColor.of("#a2c4c9") + "Preço§8: §f10 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_INICIAL, Material.WOODEN_AXE, 1, "§e→ Machado de Madeira", Map.of(Enchantment.EFFICIENCY, 1), 12, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §f10 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_INICIAL, Material.FIRE_CHARGE, 1, "§c→ Bola de Fogo", 21, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §f40 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_INICIAL, Material.LADDER, 8, "§d→ Escadas", 30, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "8", "", ChatColor.of("#a2c4c9") + "Preço§8: §f4 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_INICIAL, Material.STONE_SWORD, 1, "§8→ Espada de Pedra", 13, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §f10 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_INICIAL, Material.CHAINMAIL_BOOTS, 1, "§7→ Armadura de Cota de Malha", 22, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §f24 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_INICIAL, Material.POTION, 1, PotionType.SWIFTNESS, 45, "§9→ Poção de Speed (0:45)", 31, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §a1 ESMERALDAS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_INICIAL, Material.IRON_SWORD, 1, "§7→ Espeda de Ferro", 14, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §67 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_INICIAL, Material.IRON_BOOTS, 1, "§7→ Armadura de Ferro", 23, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §612 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_INICIAL, Material.POTION, 1, PotionType.LEAPING, 45, "§a→ Poção de Pulo (0:45)", 32, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §a1 ESMERALDAS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_INICIAL, Material.BOW, 1, "§e→ Arco normal", 15, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §612 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_INICIAL, Material.DIAMOND_BOOTS, 1, "§9→ Armadura de Diamante", 24, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §a6 ESMERALDAS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_INICIAL, Material.POTION, 1, PotionType.INVISIBILITY, 30, "§9→ Poção de Invisibilidade (0:30)", 33, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §a2 ESMERALDAS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_INICIAL, Material.ARROW, 8, "§f→ Flechas", 16, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "8", "", ChatColor.of("#a2c4c9") + "Preço§8: §62 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_INICIAL, Material.STICK, 1, "§d→ Graveto de repulsão I", Map.of(Enchantment.KNOCKBACK, 1), 25, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §65 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_INICIAL, Material.ENDER_PEARL, 1, "§9→ Pérola do ender", 34, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §a4 ESMERALDAS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
    }

    public static void ShopGUI_BLOCOS() {
        inventorySHOP_BLOCOS = Bukkit.createInventory(null, 54, "Loja - Blocos");
        addNavigation(inventorySHOP_BLOCOS, 38);

        addItem(inventorySHOP_BLOCOS, Material.WHITE_WOOL, 16, "§f→ Lã Branca", 10, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "16", "", ChatColor.of("#a2c4c9") + "Preço§8: §f4 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_BLOCOS, Material.TERRACOTTA, 8, "§6→ Terracota", 11, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "8", "", ChatColor.of("#a2c4c9") + "Preço§8: §f18 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_BLOCOS, Material.END_STONE, 12, "§e→ Pedra do END", 12, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "12", "", ChatColor.of("#a2c4c9") + "Preço§8: §f24 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_BLOCOS, Material.GLASS, 8, "§7→ Vidro", 13, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "8", "", ChatColor.of("#a2c4c9") + "Preço§8: §f15 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_BLOCOS, Material.OAK_PLANKS, 16, "§e→ Madeira", 14, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "16", "", ChatColor.of("#a2c4c9") + "Preço§8: §64 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_BLOCOS, Material.LADDER, 8, "§d→ Escada", 15, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "8", "", ChatColor.of("#a2c4c9") + "Preço§8: §f4 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_BLOCOS, Material.OBSIDIAN, 4, "§5→ Obisidiana", 16, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "4", "", ChatColor.of("#a2c4c9") + "Preço§8: §a4 ESMERALDAS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
    }

    public static void ShopGUI_COMBATE() {
        inventorySHOP_COMBATE = Bukkit.createInventory(null, 54, "Loja - Combate");
        addNavigation(inventorySHOP_COMBATE, 39);

        addItem(inventorySHOP_COMBATE, Material.CHAINMAIL_BOOTS, 1, "§f→ Armadura de Cota de Malha", 10, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §f24 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_COMBATE, Material.IRON_BOOTS, 1, "§7→ Armadura de Ferro", 11, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §612 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_COMBATE, Material.DIAMOND_BOOTS, 1, "§b→ Armadura de Diamante", 12, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §a6 ESMERALDAS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_COMBATE, Material.STONE_SWORD, 1, "§8→ Espada de Pedra", 19, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §f10 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_COMBATE, Material.IRON_SWORD, 1, "§7→ Espada de Ferro", 20, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §67 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_COMBATE, Material.DIAMOND_SWORD, 1, "§b→ Espada de Diamante", 21, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §a4 ESMERALDAS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_COMBATE, Material.BOW, 1, "§f→ Arco normal", 16, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §612 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_COMBATE, Material.BOW, 1, "§d→ Arco com força I", Map.of(Enchantment.POWER, 1), 15, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §624 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_COMBATE, Material.BOW, 1, "§3→ Arco com impacto I e força I", Map.of(Enchantment.POWER, 1, Enchantment.PUNCH, 1), 24, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §a4 ESMERALDAS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_COMBATE, Material.ARROW, 8, "§f→ Flechas", 25, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "8", "", ChatColor.of("#a2c4c9") + "Preço§8: §62 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
    }

    public static void ShopGUI_POÇOES() {
        inventorySHOP_POÇOES = Bukkit.createInventory(null, 54, "Loja - Poções");
        addNavigation(inventorySHOP_POÇOES, 40);

        addItem(inventorySHOP_POÇOES, Material.POTION, 1, PotionType.SWIFTNESS, 45, "§9→ Poção de Speed (0:45)", 10, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §a1 ESMERALDAS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_POÇOES, Material.POTION, 1, PotionType.LEAPING, 45, "§a→ Poção de Pulo (0:45)", 11, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §a1 ESMERALDAS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_POÇOES, Material.POTION, 1, PotionType.INVISIBILITY, 30, "§9→ Poção de Invisibilidade (0:30)", 12, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §a2 ESMERALDAS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_POÇOES, Material.POTION, 1, PotionType.FIRE_RESISTANCE, 30, "§d→ MIX de Poções (0:30)", 14, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §a4 ESMERALDAS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_POÇOES, Material.MILK_BUCKET, 1, "§f→ Balde de Leite", 15, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §f6 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
    }

    public static void ShopGUI_FERRAMENTAS() {
        inventorySHOP_FERRAMENTAS = Bukkit.createInventory(null, 54, "Loja - Ferramentas");
        addNavigation(inventorySHOP_FERRAMENTAS, 41);

        addItem(inventorySHOP_FERRAMENTAS, Material.WOODEN_PICKAXE, 1, "§e→ Picareta de Madeira", Map.of(Enchantment.EFFICIENCY, 1), 10, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §f10 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_FERRAMENTAS, Material.IRON_PICKAXE, 1, "§7→ Picareta de Ferro", Map.of(Enchantment.EFFICIENCY, 2), 11, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §f20 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_FERRAMENTAS, Material.GOLDEN_PICKAXE, 1, "§6→ Picareta de Ouro", Map.of(Enchantment.EFFICIENCY, 3), 12, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §68 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_FERRAMENTAS, Material.DIAMOND_PICKAXE, 1, "§9→ Picareta de Diamante", Map.of(Enchantment.EFFICIENCY, 3), 13, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §616 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_FERRAMENTAS, Material.WOODEN_AXE, 1, "§e→ Machado de Madeira", Map.of(Enchantment.EFFICIENCY, 1), 19, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §f10 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_FERRAMENTAS, Material.IRON_AXE, 1, "§7→ Machado de Ferro", Map.of(Enchantment.EFFICIENCY, 2), 20, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §f20 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_FERRAMENTAS, Material.GOLDEN_AXE, 1, "§6→ Machado de Ouro", Map.of(Enchantment.EFFICIENCY, 3), 21, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §68 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_FERRAMENTAS, Material.DIAMOND_AXE, 1, "§9→ Machado de Diamante", Map.of(Enchantment.EFFICIENCY, 3, Enchantment.SHARPNESS, 1), 22, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §616 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_FERRAMENTAS, Material.SHIELD, 1, "§3→ Escudo", 15, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §618 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_FERRAMENTAS, Material.STICK, 1, "§d→ Graveto de repulsão I", Map.of(Enchantment.KNOCKBACK, 1), 16, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §65 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_FERRAMENTAS, Material.FLINT_AND_STEEL, 1, "§8→ Isqueiro", 24, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §65 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_FERRAMENTAS, Material.SHEARS, 1, "§7→ Tesoura", 25, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §f20 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
    }

    public static void ShopGUI_ATAQUE() {
        inventorySHOP_ATAQUE = Bukkit.createInventory(null, 54, "Loja - Ataque");
        addNavigation(inventorySHOP_ATAQUE, 42);

        addItem(inventorySHOP_ATAQUE, Material.FIRE_CHARGE, 1, "§c→ Bola de Fogo", 10, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §f40 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_ATAQUE, Material.TNT, 1, "§c→ TNT", 11, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §64 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_ATAQUE, Material.IRON_GOLEM_SPAWN_EGG, 1, "§f→ Golem de Ferro (5:00)", 12, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §f120 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_ATAQUE, Material.SILVERFISH_SPAWN_EGG, 1, "§7→ Traças (0:20)", 13, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §f30 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
    }

    public static void ShopGUI_OUTROS() {
        inventorySHOP_OUTROS = Bukkit.createInventory(null, 54, "Loja - Outros");
        addNavigation(inventorySHOP_OUTROS, 43);

        addItem(inventorySHOP_OUTROS, Material.WATER_BUCKET, 1, "§b→ Água", 10, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §63 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_OUTROS, Material.SPONGE, 1, "§3→ Esponja", 11, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §63 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_OUTROS, Material.ENDER_PEARL, 1, "§5→ Pérola do Ender", 12, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §a4 ESMERALDAS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_OUTROS, Material.ENDER_CHEST, 1, "§5→ Baú do Fim (0:05)", 13, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §a2 ESMERALDAS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_OUTROS, Material.BLUE_WOOL, 1, "§d→ Torre", 14, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §f24 FERROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_OUTROS, Material.GOLDEN_APPLE, 1, "§d→ Maçã dourada", 15, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §63 OUROS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
        addItem(inventorySHOP_OUTROS, Material.ENCHANTED_GOLDEN_APPLE, 1, "§d→ Maçã dourada Encantada", 16, ChatColor.of("#b4a7d6") + "Quantidade§8: " + ChatColor.of("#b4a7d6") + "1", "", ChatColor.of("#a2c4c9") + "Preço§8: §a3 ESMERALDAS", "", ChatColor.of("#ffd966") + "› Clique para comprar!");
    }

    public static void UpgradeGUI() {
        inventoryUPGRADE = Bukkit.createInventory(null, 27, "Upgrade Capture a Bandeira");
    }

    private static void addNavigation(Inventory inv, int activePaneSlot) {
        for (int slot = 37; slot <= 43; slot++) {
            Material glass = (slot == activePaneSlot) ? Material.GREEN_STAINED_GLASS_PANE : Material.BLACK_STAINED_GLASS_PANE;
            addItem(inv, glass, 1, "", slot);
        }
        addItem(inv, Material.NETHER_STAR, 1, "§e→ Menu principal", 46, activePaneSlot == 37 ? "§7Você está visualizando esse menu!" : "§7Clique para abrir o menu Principal!");
        addItem(inv, Material.BRICKS, 1, "§d→ Blocos", 47, activePaneSlot == 38 ? "§7Você está visualizando esse menu!" : "§7Clique para abrir o menu de Blocos!");
        addItem(inv, Material.GOLDEN_SWORD, 1, "§9→ Combate", 48, activePaneSlot == 39 ? "§7Você está visualizando esse menu!" : "§7Clique para abrir o menu de Combate!");
        addItem(inv, Material.BREWING_STAND, 1, "§a→ Poções", 49, activePaneSlot == 40 ? "§7Você está visualizando esse menu!" : "§7Clique para abrir o menu de Poções!");
        addItem(inv, Material.GOLDEN_PICKAXE, 1, "§c→ Ferramentas", 50, activePaneSlot == 41 ? "§7Você está visualizando esse menu!" : "§7Clique para abrir o menu de Ferramentas!");
        addItem(inv, Material.TNT, 1, "§b→ Ataque", 51, activePaneSlot == 42 ? "§7Você está visualizando esse menu!" : "§7Clique para abrir o menu de Ataque!");
        addItem(inv, Material.CHEST_MINECART, 1, "§9→ Outros", 52, activePaneSlot == 43 ? "§7Você está visualizando esse menu!" : "§7Clique para abrir o menu de Outros!");
    }

    private static void addItem(Inventory inventory, Material material, int quantidade, String name, int slot, String... lore) {
        ItemStack itemStack = new ItemStack(material, quantidade);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(name);
            itemMeta.setLore(Arrays.asList(lore));
            itemStack.setItemMeta(itemMeta);
        }
        inventory.setItem(slot, itemStack);
    }

    private static void addItem(Inventory inventory, Material material, int quantidade, PotionType potionType, int duração, String name, int slot, String... lore) {
        ItemStack itemStack = new ItemStack(material, quantidade);
        PotionMeta itemMeta = (PotionMeta) itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(name);
            itemMeta.setLore(Arrays.asList(lore));
            itemMeta.clearCustomEffects();
            itemMeta.setBasePotionData(new PotionData(potionType, false, false));
            int ticks = duração * 20;
            if (potionType == PotionType.FIRE_RESISTANCE) {
                itemMeta.addCustomEffect(new PotionEffect(PotionType.SWIFTNESS.getEffectType(), ticks, 0), true);
                itemMeta.addCustomEffect(new PotionEffect(PotionType.INVISIBILITY.getEffectType(), ticks, 0), true);
                itemMeta.addCustomEffect(new PotionEffect(PotionType.LEAPING.getEffectType(), ticks, 0), true);
            }
            itemMeta.addCustomEffect(new PotionEffect(potionType.getEffectType(), ticks, 0), true);
            itemStack.setItemMeta(itemMeta);
        }
        inventory.setItem(slot, itemStack);
    }

    private static void addItem(Inventory inventory, Material material, int quantidade, String name, Map<Enchantment, Integer> enchants, int slot, String... lore) {
        ItemStack itemStack = new ItemStack(material, quantidade);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(name);
            itemMeta.setLore(Arrays.asList(lore));
            enchants.forEach((enchant, level) -> itemMeta.addEnchant(enchant, level, true));
            itemStack.setItemMeta(itemMeta);
        }
        inventory.setItem(slot, itemStack);
    }

    private static boolean chargePlayer(Player player, Material currency, int amount, int rawSlot) {
        if (rawSlot > 35) return false;

        String currencyName = switch (currency) {
            case IRON_INGOT -> "FERROS";
            case GOLD_INGOT -> "OUROS";
            case DIAMOND -> "DIAMANTE";
            case EMERALD -> "ESMERALDA";
            default -> "MOEDAS";
        };

        if (!player.getInventory().contains(currency, amount)) {
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            player.sendMessage("§8[§c!§8] §cVocê não tem §l" + amount + " " + currencyName);
            return false;
        }

        player.getInventory().removeItem(new ItemStack(currency, amount));
        return true;
    }

    private static void sendSuccessMessage(Player player, String itemName, Material currency, int amount) {
        String currencyName = switch (currency) {
            case IRON_INGOT -> "FERROS";
            case GOLD_INGOT -> "OUROS";
            case DIAMOND -> "DIAMANTE";
            case EMERALD -> "ESMERALDA";
            default -> "MOEDAS";
        };
        String color = switch (currency) {
            case IRON_INGOT -> "§f";
            case GOLD_INGOT -> "§6";
            case DIAMOND -> "§b";
            case EMERALD -> "§2";
            default -> "§7";
        };
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 1.0f);
        player.sendMessage("§8[§a!§8] §aVocê comprou §l" + itemName + " §apor " + color + amount + " " + currencyName);
    }

    private static String getItemName(Material material) {
        return switch (material) {
            case WHITE_WOOL -> "Lã Branca";
            case TERRACOTTA -> "Terracota";
            case END_STONE -> "Pedra do END";
            case GLASS -> "Vidro";
            case OAK_PLANKS -> "Madeira";
            case LADDER -> "Escada";
            case OBSIDIAN -> "Obisidiana";
            case ARROW -> "Flechas";
            case MILK_BUCKET -> "Balde de Leite";
            case FLINT_AND_STEEL -> "Isqueiro";
            case SHEARS -> "Tesoura";
            case SHIELD -> "Escudo";
            case FIRE_CHARGE -> "Bola de Fogo";
            case TNT -> "TNT";
            case IRON_GOLEM_SPAWN_EGG -> "Spawn do Golem de Ferro (5:00)";
            case SILVERFISH_SPAWN_EGG -> "Spawn de Traças (0:20)";
            case WATER_BUCKET -> "Balde de Água";
            case SPONGE -> "Esponja";
            case ENDER_PEARL -> "Pérola do Ender";
            case ENDER_CHEST -> "Baú do Fim";
            case BLUE_WOOL -> "Torre";
            case GOLDEN_APPLE -> "Maçã dourada";
            case ENCHANTED_GOLDEN_APPLE -> "Maçã dourada Encantada";
            default -> null;
        };
    }

    public static void buyItem(InventoryClickEvent event, Material pag, int pagINT, Material item, int quantidade) {
        Player player = (Player) event.getWhoClicked();

        if (item == Material.BOW) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            ItemMeta meta = clicked.getItemMeta();

            String itemName;
            ItemStack finalItem;

            if (!meta.hasEnchants()) {
                itemName = "Arco normal";
                finalItem = EventItems.flagITEM_bow();
            } else if (meta.hasEnchant(Enchantment.POWER) && !meta.hasEnchant(Enchantment.PUNCH)) {
                itemName = "Arco com força I";
                finalItem = EventItems.flagITEM_bow_power();
            } else {
                itemName = "Arco com impacto I e força I";
                finalItem = EventItems.flagITEM_bow_power_punch();
            }

            if (chargePlayer(player, pag, pagINT, event.getRawSlot())) {
                player.getInventory().addItem(finalItem);
                sendSuccessMessage(player, itemName, pag, pagINT);
            }
            return;
        }

        if (item == Material.POTION) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !(clicked.getItemMeta() instanceof PotionMeta clickedMeta)) return;

            String itemName;
            ItemStack finalPotion = new ItemStack(Material.POTION, quantidade);
            PotionMeta finalMeta = (PotionMeta) finalPotion.getItemMeta();
            if (finalMeta == null) return;

            PotionType type = clickedMeta.getBasePotionData().getType();
            if (type == PotionType.SWIFTNESS) {
                itemName = "Poção de Speed (0:45)";
                finalMeta.setBasePotionData(new PotionData(PotionType.SWIFTNESS, false, false));
                finalMeta.addCustomEffect(new PotionEffect(PotionType.SWIFTNESS.getEffectType(), 45 * 20, 1), true);
            } else if (type == PotionType.LEAPING) {
                itemName = "Poção de Pulo (0:45)";
                finalMeta.setBasePotionData(new PotionData(PotionType.LEAPING, false, false));
                finalMeta.addCustomEffect(new PotionEffect(PotionType.LEAPING.getEffectType(), 45 * 20, 1), true);
            } else if (type == PotionType.INVISIBILITY) {
                itemName = "Poção de Invisibilidade (0:30)";
                finalMeta.setBasePotionData(new PotionData(PotionType.INVISIBILITY, false, false));
                finalMeta.addCustomEffect(new PotionEffect(PotionType.INVISIBILITY.getEffectType(), 30 * 20, 1), true);
            } else if (type == PotionType.FIRE_RESISTANCE) {
                itemName = "MIX de Poções (0:30)";
                finalMeta.setDisplayName("MIX de Poções");
                finalMeta.setBasePotionData(new PotionData(PotionType.FIRE_RESISTANCE, false, false));
                finalMeta.addCustomEffect(new PotionEffect(PotionType.SWIFTNESS.getEffectType(), 30 * 20, 0), true);
                finalMeta.addCustomEffect(new PotionEffect(PotionType.INVISIBILITY.getEffectType(), 30 * 20, 0), true);
                finalMeta.addCustomEffect(new PotionEffect(PotionType.LEAPING.getEffectType(), 30 * 20, 0), true);
                finalMeta.addCustomEffect(new PotionEffect(PotionType.FIRE_RESISTANCE.getEffectType(), 30 * 20, 0), true);
            } else {
                return;
            }
            finalPotion.setItemMeta(finalMeta);

            if (chargePlayer(player, pag, pagINT, event.getRawSlot())) {
                player.getInventory().addItem(finalPotion);
                sendSuccessMessage(player, itemName, pag, pagINT);
            }
            return;
        }

        if (item == Material.STICK) {
            if (chargePlayer(player, pag, pagINT, event.getRawSlot())) {
                ItemStack knockStick = new ItemStack(Material.STICK, quantidade);
                ItemMeta meta = knockStick.getItemMeta();
                if (meta != null) {
                    meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
                    knockStick.setItemMeta(meta);
                }
                player.getInventory().addItem(knockStick);
                sendSuccessMessage(player, "Graveto de repulsão I", pag, pagINT);
            }
            return;
        }

        if (item == Material.WHITE_WOOL) {
            if (chargePlayer(player, pag, pagINT, event.getRawSlot())) {
                Material woolType = Material.ORANGE_WOOL;
//                if ("Green".equals(CaptureBandeiraEvent.time.get(player))) {
//                    woolType = Material.LIME_WOOL;
//                }
                player.getInventory().addItem(new ItemStack(woolType, quantidade));
                sendSuccessMessage(player, "Lã Branca", pag, pagINT);
            }
            return;
        }

        String itemName = getItemName(item);
        if (itemName == null) return;

        if (chargePlayer(player, pag, pagINT, event.getRawSlot())) {
            ItemStack finalItem = new ItemStack(item, quantidade);
            ItemMeta meta = switch (itemName) {
                case "Isqueiro" -> EventItems.flagITEM_flint_and_steel().getItemMeta();
                case "Tesoura" -> EventItems.flagITEM_shears().getItemMeta();
                case "Escudo" -> EventItems.flagITEM_shield().getItemMeta();
                case "Torre" -> EventItems.flagITEM_tower().getItemMeta();
                default -> null;
            };
            if (meta != null) {
                finalItem.setItemMeta(meta);
            }
            player.getInventory().addItem(finalItem);
            sendSuccessMessage(player, itemName, pag, pagINT);
        }
    }

    private static ItemMeta getSwordMeta(Material item) {
        return switch (item) {
            case STONE_SWORD -> EventItems.flagITEM_Stone_sword().getItemMeta();
            case IRON_SWORD -> EventItems.flagITEM_Iron_sword().getItemMeta();
            case DIAMOND_SWORD -> EventItems.flagITEM_Diamond_sword().getItemMeta();
            default -> null;
        };
    }

    private static ItemMeta getAxeMeta(Material item) {
        ItemMeta meta = switch (item) {
            case WOODEN_AXE -> EventItems.flagITEM_Wooden_axe().getItemMeta();
            case IRON_AXE -> EventItems.flagITEM_Iron_axe().getItemMeta();
            case GOLDEN_AXE -> EventItems.flagITEM_Golden_axe().getItemMeta();
            case DIAMOND_AXE -> EventItems.flagITEM_Diamond_axe().getItemMeta();
            default -> null;
        };
        if (meta != null) {
            int speed = switch (item) {
                case WOODEN_AXE -> 1;
                case IRON_AXE -> 2;
                default -> 3;
            };
            meta.addEnchant(Enchantment.EFFICIENCY, speed, true);
            if (item == Material.DIAMOND_AXE) {
                meta.addEnchant(Enchantment.SHARPNESS, 1, true);
            }
        }
        return meta;
    }

    private static ItemMeta getPickaxeMeta(Material item) {
        ItemMeta meta = switch (item) {
            case WOODEN_PICKAXE -> EventItems.flagITEM_Wooden_pickaxe().getItemMeta();
            case IRON_PICKAXE -> EventItems.flagITEM_Iron_pickaxe().getItemMeta();
            case GOLDEN_PICKAXE -> EventItems.flagITEM_Golden_pickaxe().getItemMeta();
            case DIAMOND_PICKAXE -> EventItems.flagITEM_Diamond_pickaxe().getItemMeta();
            default -> null;
        };
        if (meta != null) {
            int level = (item == Material.WOODEN_PICKAXE) ? 1 : (item == Material.IRON_PICKAXE) ? 2 : 3;
            meta.addEnchant(Enchantment.EFFICIENCY, level, true);
        }
        return meta;
    }

    private static boolean replaceFirstMatch(Player player, ItemStack newItem, Material... oldTypes) {
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item != null) {
                for (Material type : oldTypes) {
                    if (item.getType() == type) {
                        player.getInventory().setItem(slot, newItem);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void buyItem(InventoryClickEvent event, Material pag, int pagINT, Material item, int quantidade, boolean equip) {
        if (!equip) return;
        Player player = (Player) event.getWhoClicked();

        String itemName = switch (item) {
            case STONE_SWORD -> "Espada de Pedra";
            case IRON_SWORD -> "Espada de Ferro";
            case DIAMOND_SWORD -> "Espada de Diamante";
            case WOODEN_PICKAXE -> "Picareta de Madeira";
            case IRON_PICKAXE -> "Picareta de Ferro";
            case GOLDEN_PICKAXE -> "Picareta de Ouro";
            case DIAMOND_PICKAXE -> "Picareta de Diamante";
            case WOODEN_AXE -> "Machado de Madeira";
            case IRON_AXE -> "Machado de Ferro";
            case GOLDEN_AXE -> "Machado de Ouro";
            case DIAMOND_AXE -> "Machado de Diamante";
            case CHAINMAIL_BOOTS -> "Armadura de Cota de Malha";
            case IRON_BOOTS -> "Armadura de Ferro";
            case DIAMOND_BOOTS -> "Armadura de Diamante";
            default -> null;
        };
        if (itemName == null) return;

        if (item == Material.STONE_SWORD || item == Material.IRON_SWORD || item == Material.DIAMOND_SWORD) {
            int currentLevel = upgradeSWORD.getOrDefault(player, 0);
            int itemLevel = (item == Material.STONE_SWORD) ? 1 : (item == Material.IRON_SWORD) ? 2 : 3;

            boolean alreadyHasBetter = currentLevel >= itemLevel;
            if (player.getInventory().contains(item) || alreadyHasBetter) {
                if (chargePlayer(player, pag, pagINT, event.getRawSlot())) {
                    ItemStack sword = new ItemStack(item, quantidade);
                    sword.setItemMeta(getSwordMeta(item));
                    player.getInventory().addItem(sword);
                    sendSuccessMessage(player, itemName, pag, pagINT);
                }
            } else {
                if (chargePlayer(player, pag, pagINT, event.getRawSlot())) {
                    upgradeSWORD.put(player, itemLevel);
                    ItemStack sword = new ItemStack(item, quantidade);
                    sword.setItemMeta(getSwordMeta(item));
                    player.getInventory().setItem(0, sword);
                    sendSuccessMessage(player, itemName, pag, pagINT);
                }
            }
            return;
        }

        if (item == Material.WOODEN_AXE || item == Material.IRON_AXE || item == Material.GOLDEN_AXE || item == Material.DIAMOND_AXE) {
            int currentLevel = upgradeAXE.getOrDefault(player, 0);
            int itemLevel = (item == Material.WOODEN_AXE) ? 1 : (item == Material.IRON_AXE) ? 2 : (item == Material.GOLDEN_AXE) ? 3 : 4;

            if (player.getInventory().contains(item) || currentLevel >= itemLevel) {
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                player.sendMessage("§8[§c!§8] §cVocê já tem um machado melhor.");
                return;
            }

            if (chargePlayer(player, pag, pagINT, event.getRawSlot())) {
                upgradeAXE.put(player, itemLevel);
                ItemStack axe = new ItemStack(item, quantidade);
                axe.setItemMeta(getAxeMeta(item));

                if (!replaceFirstMatch(player, axe, Material.DIAMOND_AXE, Material.GOLDEN_AXE, Material.IRON_AXE, Material.WOODEN_AXE)) {
                    player.getInventory().addItem(axe);
                }
                sendSuccessMessage(player, itemName, pag, pagINT);
            }
            return;
        }

        if (item == Material.WOODEN_PICKAXE || item == Material.IRON_PICKAXE || item == Material.GOLDEN_PICKAXE || item == Material.DIAMOND_PICKAXE) {
            int currentLevel = upgradePICKAXE.getOrDefault(player, 0);
            int itemLevel = (item == Material.WOODEN_PICKAXE) ? 1 : (item == Material.IRON_PICKAXE) ? 2 : (item == Material.GOLDEN_PICKAXE) ? 3 : 4;

            if (player.getInventory().contains(item) || currentLevel >= itemLevel) {
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                player.sendMessage("§8[§c!§8] §cVocê já tem uma picareta melhor.");
                return;
            }

            if (chargePlayer(player, pag, pagINT, event.getRawSlot())) {
                upgradePICKAXE.put(player, itemLevel);
                ItemStack pick = new ItemStack(item, quantidade);
                pick.setItemMeta(getPickaxeMeta(item));

                if (!replaceFirstMatch(player, pick, Material.DIAMOND_PICKAXE, Material.GOLDEN_PICKAXE, Material.IRON_PICKAXE, Material.WOODEN_PICKAXE)) {
                    player.getInventory().addItem(pick);
                }
                sendSuccessMessage(player, itemName, pag, pagINT);
            }
            return;
        }

        if (item == Material.CHAINMAIL_BOOTS || item == Material.IRON_BOOTS || item == Material.DIAMOND_BOOTS) {
            int currentLevel = upgradeArmor.getOrDefault(player, 0);
            int itemLevel = (item == Material.CHAINMAIL_BOOTS) ? 1 : (item == Material.IRON_BOOTS) ? 2 : 3;

            if (player.getInventory().contains(item) || currentLevel >= itemLevel) {
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                player.sendMessage("§8[§c!§8] §cVocê já tem uma armadura melhor.");
                return;
            }

            if (chargePlayer(player, pag, pagINT, event.getRawSlot())) {
                upgradeArmor.put(player, itemLevel);
                switch (item) {
                    case CHAINMAIL_BOOTS -> {
                        player.getInventory().setLeggings(EventItems.FlagITEM_CHAILMALL_LEGGING);
                        player.getInventory().setBoots(EventItems.FlagITEM_CHAILMALL_BOOTS);
                    }
                    case IRON_BOOTS -> {
                        player.getInventory().setLeggings(EventItems.FlagITEM_IRON_LEGGING);
                        player.getInventory().setBoots(EventItems.FlagITEM_IRON_BOOTS);
                    }
                    case DIAMOND_BOOTS -> {
                        player.getInventory().setLeggings(EventItems.FlagITEM_DIAMOND_LEGGING);
                        player.getInventory().setBoots(EventItems.FlagITEM_DIAMOND_BOOTS);
                    }
                }
                sendSuccessMessage(player, itemName, pag, pagINT);
            }
        }
    }

    public static void loadAll() {
        ShopGUI_INICIAL();
        ShopGUI_BLOCOS();
        ShopGUI_COMBATE();
        ShopGUI_POÇOES();
        ShopGUI_FERRAMENTAS();
        ShopGUI_ATAQUE();
        ShopGUI_OUTROS();
        UpgradeGUI();
    }

    public static Inventory getInventorySHOP_INICIAL() { return inventorySHOP_INICIAL; }
    public static Inventory getInventorySHOP_BLOCOS() { return inventorySHOP_BLOCOS; }
    public static Inventory getInventorySHOP_COMBATE() { return inventorySHOP_COMBATE; }
    public static Inventory getInventorySHOP_POÇOES() { return inventorySHOP_POÇOES; }
    public static Inventory getInventorySHOP_FERRAMENTAS() { return inventorySHOP_FERRAMENTAS; }
    public static Inventory getInventorySHOP_ATAQUE() { return inventorySHOP_ATAQUE; }
    public static Inventory getInventorySHOP_OUTROS() { return inventorySHOP_OUTROS; }
    public static Inventory getInventoryUPGRADE() { return inventoryUPGRADE; }
}