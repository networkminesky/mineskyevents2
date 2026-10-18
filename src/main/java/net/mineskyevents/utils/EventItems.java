package net.mineskyevents.utils;

import net.md_5.bungee.api.ChatColor;
import net.mineskyevents.config.managers.DataManager;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EventItems {
    public static final ItemStack SpleefITEM = Shovel();
    public static final ItemStack BedLeave = createBed();
    public static final ItemStack SumoITEM = Stick();
    public static final ItemStack BarcoITEM = Boat();
    public static final ItemStack TNTHEAD = Tnt();
    public static final ItemStack CheckPoint = Plate();
    public static final ItemStack FlagORANGE = flagLARANJA();
    public static final ItemStack FlagLIME = flagVERDE();

    public static final ItemStack FlagITEM_WOODEN_SWORD = flagITEM_Wooden_sword();
    public static final ItemStack FlagITEM_STONE_SWORD = flagITEM_Stone_sword();
    public static final ItemStack FlagITEM_IRON_SWORD = flagITEM_Iron_sword();
    public static final ItemStack FlagITEM_DIAMOND_SWORD = flagITEM_Diamond_sword();

    public static final ItemStack FlagITEM_WOODEN_AXE = flagITEM_Wooden_axe();
    public static final ItemStack FlagITEM_IRON_AXE = flagITEM_Iron_axe();
    public static final ItemStack FlagITEM_GOLDEN_AXE = flagITEM_Golden_axe();
    public static final ItemStack FlagITEM_DIAMOND_AXE = flagITEM_Diamond_axe();

    public static final ItemStack FlagITEM_WOODEN_PICKAXE = flagITEM_Wooden_pickaxe();
    public static final ItemStack FlagITEM_IRON_PICKAXE = flagITEM_Iron_pickaxe();
    public static final ItemStack FlagITEM_GOLDEN_PICKAXE = flagITEM_Golden_pickaxe();
    public static final ItemStack FlagITEM_DIAMOND_PICKAXE = flagITEM_Diamond_pickaxe();

    public static final ItemStack FlagITEM_BOW = flagITEM_bow();
    public static final ItemStack FlagITEM_BOW_POWER = flagITEM_bow_power();
    public static final ItemStack FlagITEM_BOW_POWER_PUNCH = flagITEM_bow_power_punch();
    public static final ItemStack FlagITEM_SHEARS = flagITEM_shears();
    public static final ItemStack FlagITEM_SHIELD = flagITEM_shield();
    public static final ItemStack FlagITEM_FLINT_AND_STEEL = flagITEM_flint_and_steel();
    public static final ItemStack FlagITEM_TOWER = flagITEM_tower();

    public static final ItemStack FlagITEM_ARMOR_HELMET_LIME = flagITEM_Armor_helmet_lime();
    public static final ItemStack FlagITEM_ARMOR_CHESTPLATE_LIME = flagITEM_Armor_chestplate_lime();
    public static final ItemStack FlagITEM_ARMOR_LEGGING_LIME = flagITEM_Armor_legging_lime();
    public static final ItemStack FlagITEM_ARMOR_BOTTS_LIME = flagITEM_Armor_boots_lime();

    public static final ItemStack FlagITEM_ARMOR_HELMET_ORANGE = flagITEM_Armor_helmet_orange();
    public static final ItemStack FlagITEM_ARMOR_CHESTPLATE_ORANGE = flagITEM_Armor_chestplate_orange();
    public static final ItemStack FlagITEM_ARMOR_LEGGING_ORANGE = flagITEM_Armor_legging_orange();
    public static final ItemStack FlagITEM_ARMOR_BOTTS_ORANGE = flagITEM_Armor_boots_orange();

    public static final ItemStack FlagITEM_CHAILMALL_BOOTS = flagITEM_Chailmal_BOOTS();
    public static final ItemStack FlagITEM_CHAILMALL_LEGGING = flagITEM_Chailmal_legging();
    public static final ItemStack FlagITEM_IRON_BOOTS = flagITEM_Iron_boots();
    public static final ItemStack FlagITEM_IRON_LEGGING = flagITEM_Iron_legging();
    public static final ItemStack FlagITEM_DIAMOND_BOOTS = flagITEM_Diamond_boots();
    public static final ItemStack FlagITEM_DIAMOND_LEGGING = flagITEM_Diamond_legging();

    private static class Builder {
        private final ItemStack item;
        private final ItemMeta meta;

        Builder(Material material) {
            this.item = new ItemStack(material);
            this.meta = item.getItemMeta();
        }

        Builder name(String name) {
            if (meta != null) meta.setDisplayName(name);
            return this;
        }

        Builder unbreakable() {
            if (meta != null) meta.setUnbreakable(true);
            return this;
        }

        Builder enchant(Enchantment enchant, int level) {
            if (meta != null) meta.addEnchant(enchant, level, true);
            return this;
        }

        Builder lore(String... lines) {
            if (meta != null) meta.setLore(List.of(lines));
            return this;
        }

        Builder color(Color color) {
            if (meta instanceof LeatherArmorMeta) {
                ((LeatherArmorMeta) meta).setColor(color);
            }
            return this;
        }

        Builder pattern(DyeColor color, PatternType type) {
            if (meta instanceof BannerMeta) {
                ((BannerMeta) meta).addPattern(new Pattern(color, type));
            }
            return this;
        }

        ItemStack build() {
            if (meta != null) item.setItemMeta(meta);
            return item;
        }
    }

    private static Builder flagBuilder(Material mat, String name, String desc) {
        return new Builder(mat)
                .name(name)
                .unbreakable()
                .lore(
                        ChatColor.of("#a2c4c9") + "Item único do evento Capture a Bandeira",
                        "",
                        "§8| " + ChatColor.of("#a2c4c9") + "Descrição§8:",
                        "  " + ChatColor.of("#639da6") + desc
                );
    }

    private static Builder flagArmorLime(Material mat) {
        return new Builder(mat)
                .name(ChatColor.of("#8fce00") + "♟ Armadura de couro Verde!")
                .unbreakable()
                .color(Color.fromRGB(126, 255, 0))
                .lore(
                        ChatColor.of("#b6d7a8") + "Item único do evento Capture a Bandeira",
                        "",
                        "§8| " + ChatColor.of("#b6d7a8") + "Descrição§8:",
                        "  " + ChatColor.of("#83a673") + "  Armadura do time Verde"
                );
    }

    private static Builder flagArmorOrange(Material mat) {
        return new Builder(mat)
                .name(ChatColor.of("#ff6600") + "♟ Armadura de couro Laranja!")
                .unbreakable()
                .color(Color.fromRGB(255, 114, 0))
                .lore(
                        ChatColor.of("#f6b26b") + "Item único do evento Capture a Bandeira",
                        "",
                        "§8| " + ChatColor.of("#f6b26b") + "Descrição§8:",
                        "  " + ChatColor.of("#cd8334") + "  Armadura do time Laranja"
                );
    }

    private static Builder flagArmor(Material mat, String name, String desc) {
        return new Builder(mat)
                .name(ChatColor.of("#2493b4") + "♟ Armadura de " + name + "!")
                .unbreakable()
                .lore(
                        ChatColor.of("#a2c4c9") + "Item único do evento Capture a Bandeira",
                        "",
                        "§8| " + ChatColor.of("#a2c4c9") + "Descrição§8:",
                        "  " + ChatColor.of("#639da6") + "  Armadura de " + desc + "."
                );
    }

    // itens

    public static ItemStack Shovel() {
        return new Builder(Material.GOLDEN_SHOVEL)
                .name("§eQuebra TUDO!")
                .unbreakable()
                .enchant(Enchantment.EFFICIENCY, 2)
                .lore(
                        ChatColor.of("#d1be86") + "Item único do evento Spleef",
                        "",
                        "§8| " + ChatColor.of("#d1be86") + "Descrição§8:",
                        ChatColor.of("#dfb32e") + "  Quebre os blocos de neve do evento."
                )
                .build();
    }

    public static ItemStack Stick() {
        return new Builder(Material.STICK)
                .name(ChatColor.of("#F44336") + "Irei te pegar!")
                .unbreakable()
                .enchant(Enchantment.KNOCKBACK, 3)
                .lore(
                        ChatColor.of("#EA9999") + "Item único do evento SUMO",
                        "",
                        "§8| " + ChatColor.of("#EA9999") + "Descrição§8:",
                        ChatColor.of("#E06666") + "  Empurre os jogadores da plataforma de gelo."
                )
                .build();
    }

    public static ItemStack Boat() {
        return new Builder(Material.JUNGLE_BOAT)
                .name("§dCarro esportivo")
                .unbreakable()
                .lore(
                        ChatColor.of("#d5a6bd") + "Item único do evento Corrida de Barco",
                        "",
                        "§8| " + ChatColor.of("#d5a6bd") + "Descrição§8:",
                        ChatColor.of("#c27ba0") + "  Utilize esse barco para ganhar a corrida."
                )
                .build();
    }

    public static ItemStack Tnt() {
        return new Builder(Material.TNT)
                .name("§c§lBatata Quente!")
                .unbreakable()
                .enchant(Enchantment.MENDING, 1)
                .lore(
                        ChatColor.of("#dc6565") + "Item único do evento TNT-TAG",
                        "",
                        "§8| " + ChatColor.of("#dc6565") + "Descrição§8:",
                        ChatColor.of("#bc3b3b") + "  Caso tenha ela na sua cabeça e passar entre 15 a 30 segundos ela explode."
                )
                .build();
    }

    public static boolean isCheckpointPlate(ItemStack item) {
        if (item == null || item.getType() != Material.LIGHT_WEIGHTED_PRESSURE_PLATE) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && "§e→ CheckPoint".equals(meta.getDisplayName());
    }

    public static ItemStack Plate() {
        return new Builder(Material.LIGHT_WEIGHTED_PRESSURE_PLATE)
                .name("§e→ CheckPoint")
                .unbreakable()
                .lore(
                        ChatColor.of("#ffe599") + "Item únido do evento Corrida de Parapente",
                        "",
                        "§8| " + ChatColor.of("#ffe599") + "Descrição§8:",
                        ChatColor.of("#ffd966") + "  Use ele quando quiser voltar para o checkpoint do Parapente"
                )
                .build();
    }

    public static ItemStack createBed() {
        return new Builder(Material.RED_BED)
                .name("§c§l→ Clique para sair!")
                .unbreakable()
                .lore(
                        "",
                        "§8| " + ChatColor.of("#ed5858") + "Descrição§8:",
                        ChatColor.of("#ae4141") + "  Sair do evento."
                )
                .build();
    }

    // ferramentas

    public static ItemStack flagITEM_Wooden_sword() {
        return flagBuilder(Material.WOODEN_SWORD, ChatColor.of("#2493b4") + "\uD83D\uDDE1 Espada de Madeira!", "Mate players do outro time para chegar até a bandeira.").build();
    }

    public static ItemStack flagITEM_Stone_sword() {
        return flagBuilder(Material.STONE_SWORD, ChatColor.of("#2493b4") + "\uD83D\uDDE1 Espada de Pedra!", "Mate players do outro time para chegar até a bandeira.").build();
    }

    public static ItemStack flagITEM_Iron_sword() {
        return flagBuilder(Material.IRON_SWORD, ChatColor.of("#2493b4") + "\uD83D\uDDE1 Espada de Ferro!", "Mate players do outro time para chegar até a bandeira.").build();
    }

    public static ItemStack flagITEM_Diamond_sword() {
        return flagBuilder(Material.DIAMOND_SWORD, ChatColor.of("#2493b4") + "\uD83D\uDDE1 Espada de Diamante!", "Mate players do outro time para chegar até a bandeira.").build();
    }

    public static ItemStack flagITEM_Wooden_axe() {
        return flagBuilder(Material.WOODEN_AXE, ChatColor.of("#2493b4") + "\uD83E\uDE93 Machado de Madeira!", "Quebra blocos de madeira com ele. Também pode ser usado nas lutas contra jogadores").build();
    }

    public static ItemStack flagITEM_Iron_axe() {
        return flagBuilder(Material.IRON_AXE, ChatColor.of("#2493b4") + "\uD83E\uDE93 Machado de Ferro!", "Quebra blocos de madeira com ele. Também pode ser usado nas lutas contra jogadores").build();
    }

    public static ItemStack flagITEM_Golden_axe() {
        return flagBuilder(Material.GOLDEN_AXE, ChatColor.of("#2493b4") + "\uD83E\uDE93 Machado de Ouro!", "Quebra blocos de madeira com ele. Também pode ser usado nas lutas contra jogadores").build();
    }

    public static ItemStack flagITEM_Diamond_axe() {
        return flagBuilder(Material.DIAMOND_AXE, ChatColor.of("#2493b4") + "\uD83E\uDE93 Machado de Diamante!", "Quebra blocos de madeira com ele. Também pode ser usado nas lutas contra jogadores").build();
    }

    public static ItemStack flagITEM_Wooden_pickaxe() {
        return flagBuilder(Material.WOODEN_PICKAXE, ChatColor.of("#2493b4") + "⛏ Picareta de Madeira!", "Quebra blocos como pedra. Destrua a proteção do outro time com ela.").build();
    }

    public static ItemStack flagITEM_Iron_pickaxe() {
        return flagBuilder(Material.IRON_PICKAXE, ChatColor.of("#2493b4") + "⛏ Picareta de Ferro!", "Quebra blocos como pedra. Destrua a proteção do outro time com ela.").build();
    }

    public static ItemStack flagITEM_Golden_pickaxe() {
        return flagBuilder(Material.GOLDEN_PICKAXE, ChatColor.of("#2493b4") + "⛏ Picareta de Ouro!", "Quebra blocos como pedra. Destrua a proteção do outro time com ela.").build();
    }

    public static ItemStack flagITEM_Diamond_pickaxe() {
        return flagBuilder(Material.DIAMOND_PICKAXE, ChatColor.of("#2493b4") + "⛏ Picareta de Diamante!", "Quebra blocos como pedra. Destrua a proteção do outro time com ela.").build();
    }

    public static ItemStack flagITEM_bow() {
        return flagBuilder(Material.BOW, ChatColor.of("#2493b4") + "\uD83C\uDFF9 Arco normal!", "Use para atingir jogadores de longe.").build();
    }

    public static ItemStack flagITEM_bow_power() {
        return flagBuilder(Material.BOW, ChatColor.of("#2493b4") + "\uD83C\uDFF9 Arco com força I!", "Use para atingir jogadores de longe.").enchant(Enchantment.POWER, 1).build();
    }

    public static ItemStack flagITEM_bow_power_punch() {
        return flagBuilder(Material.BOW, ChatColor.of("#2493b4") + "\uD83C\uDFF9 Arco com impacto I e força I!", "Use para atingir jogadores de longe.").enchant(Enchantment.POWER, 1).enchant(Enchantment.PUNCH, 1).build();
    }

    public static ItemStack flagITEM_shield() {
        return flagBuilder(Material.SHIELD, ChatColor.of("#2493b4") + "\uD83D\uDEE1 Escudo!", "Defenda-se de ataques dos jogadores.").build();
    }

    public static ItemStack flagITEM_flint_and_steel() {
        return flagBuilder(Material.FLINT_AND_STEEL, ChatColor.of("#2493b4") + "⚔ Isqueiro!", "Coloque fogo nos blocos.").build();
    }

    public static ItemStack flagITEM_shears() {
        return flagBuilder(Material.SHEARS, ChatColor.of("#2493b4") + "✂ Tesoura!", "Quebre lã com facilidade").build();
    }

    public static ItemStack flagITEM_tower() {
        return flagBuilder(Material.BLUE_WOOL, ChatColor.of("#2493b4") + "🌊 Torre!", "Clique direito para criar uma torre.").build();
    }

    // armaduras de couro

    public static ItemStack flagITEM_Armor_helmet_lime() { return flagArmorLime(Material.LEATHER_HELMET).build(); }
    public static ItemStack flagITEM_Armor_chestplate_lime() { return flagArmorLime(Material.LEATHER_CHESTPLATE).build(); }
    public static ItemStack flagITEM_Armor_legging_lime() { return flagArmorLime(Material.LEATHER_LEGGINGS).build(); }
    public static ItemStack flagITEM_Armor_boots_lime() { return flagArmorLime(Material.LEATHER_BOOTS).build(); }

    public static ItemStack flagITEM_Armor_helmet_orange() { return flagArmorOrange(Material.LEATHER_HELMET).build(); }
    public static ItemStack flagITEM_Armor_chestplate_orange() { return flagArmorOrange(Material.LEATHER_CHESTPLATE).build(); }
    public static ItemStack flagITEM_Armor_legging_orange() { return flagArmorOrange(Material.LEATHER_LEGGINGS).build(); }
    public static ItemStack flagITEM_Armor_boots_orange() { return flagArmorOrange(Material.LEATHER_BOOTS).build(); }

    // armaduras

    public static ItemStack flagITEM_Chailmal_legging() { return flagArmor(Material.CHAINMAIL_LEGGINGS, "Cota de Malha", "cota de malha").build(); }
    public static ItemStack flagITEM_Chailmal_BOOTS() { return flagArmor(Material.CHAINMAIL_BOOTS, "Cota de Malha", "cota de malha").build(); }
    public static ItemStack flagITEM_Iron_legging() { return flagArmor(Material.IRON_LEGGINGS, "Ferro", "ferro").build(); }
    public static ItemStack flagITEM_Iron_boots() { return flagArmor(Material.IRON_BOOTS, "Ferro", "ferro").build(); }
    public static ItemStack flagITEM_Diamond_legging() { return flagArmor(Material.DIAMOND_LEGGINGS, "Diamante", "diamante").build(); }
    public static ItemStack flagITEM_Diamond_boots() { return flagArmor(Material.DIAMOND_BOOTS, "Diamante", "diamante").build(); }

    // bandeiras

    public static ItemStack flagLARANJA() {
        return new Builder(Material.ORANGE_BANNER)
                .name(ChatColor.of("#ff6600") + "Bandeira do §lTIME LARANJA")
                .pattern(DyeColor.BLACK, PatternType.GLOBE)
                .unbreakable()
                .lore(
                        ChatColor.of("#f6b26b") + "Bandeira do evento de Capture a Bandeira",
                        "",
                        "§8| " + ChatColor.of("#f6b26b") + "Descrição§8:",
                        ChatColor.of("#cd8334") + "  Leve até a base do seu time!"
                )
                .build();
    }

    public static ItemStack flagVERDE() {
        return new Builder(Material.LIME_BANNER)
                .name(ChatColor.of("#8fce00") + "Bandeira do §lTIME VERDE")
                .pattern(DyeColor.BLACK, PatternType.CREEPER)
                .unbreakable()
                .lore(
                        ChatColor.of("#b6d7a8") + "Bandeira do evento de Capture a Bandeira",
                        "",
                        "§8| " + ChatColor.of("#b6d7a8") + "Descrição§8:",
                        ChatColor.of("#83a673") + "  Leve até a base do seu time!"
                )
                .build();
    }

    // cabeça de stats

    public static ItemStack HeadEvents(Player p) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        if (skullMeta == null) return head;

        skullMeta.setOwningPlayer(p);
        skullMeta.setDisplayName("§cInformações de " + p.getName());
        skullMeta.setUnbreakable(true);

        File file = DataManager.getFile(p.getName().toLowerCase(), "playerdata");
        FileConfiguration config = DataManager.getConfiguration(file);

        //String event = MineSkyEvents.event;
        String event = "Spleef";
        String eventName;
        boolean hasDeaths = true;

        switch (event) {
            case "Spleef" -> eventName = "Spleef";
            case "TijolãoWars" -> eventName = "Tijolão Wars";
            case "Corrida" -> { eventName = "Corrida"; hasDeaths = false; }
            case "CorridaBoat" -> { eventName = "Corrida de barco"; hasDeaths = false; }
            case "Sumo" -> eventName = "Sumo";
            case "TNTRun" -> eventName = "TNTRUN";
            case "TNTTag" -> eventName = "TNT-TAG";
            case "Parapente" -> { eventName = "Corrida de Parapente"; hasDeaths = false; }
            default -> { return head; }
        }

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.of("#b77777") + "Suas informações do evento " + eventName);
        lore.add("");
        lore.add("§8| " + ChatColor.of("#b77777") + "Vitórias§8: " + ChatColor.of("#c24646") + config.getInt("Events." + event + ".win"));
        if (hasDeaths) {
            lore.add("§8| " + ChatColor.of("#b77777") + "Mortes§8: " + ChatColor.of("#c24646") + config.getInt("Events." + event + ".dead"));
        }

        skullMeta.setLore(lore);
        head.setItemMeta(skullMeta);
        return head;
    }

    public static void dropAllCustomItems(Player player) {
        Scheduler.runTask(() -> {
            Location loc = player.getLocation();

            List<ItemStack> itemsToDrop = List.of(
                    Shovel(), Stick(), Boat(), Tnt(), Plate(), createBed(),
                    flagITEM_Wooden_sword(), flagITEM_Stone_sword(), flagITEM_Iron_sword(), flagITEM_Diamond_sword(),
                    flagITEM_Wooden_axe(), flagITEM_Iron_axe(), flagITEM_Golden_axe(), flagITEM_Diamond_axe(),
                    flagITEM_Wooden_pickaxe(), flagITEM_Iron_pickaxe(), flagITEM_Golden_pickaxe(), flagITEM_Diamond_pickaxe(),
                    flagITEM_bow(), flagITEM_bow_power(), flagITEM_bow_power_punch(), flagITEM_shield(),
                    flagITEM_flint_and_steel(), flagITEM_shears(), flagITEM_tower(),
                    flagITEM_Armor_helmet_lime(), flagITEM_Armor_chestplate_lime(), flagITEM_Armor_legging_lime(), flagITEM_Armor_boots_lime(),
                    flagITEM_Armor_helmet_orange(), flagITEM_Armor_chestplate_orange(), flagITEM_Armor_legging_orange(), flagITEM_Armor_boots_orange(),
                    flagITEM_Chailmal_legging(), flagITEM_Chailmal_BOOTS(), flagITEM_Iron_legging(), flagITEM_Iron_boots(),
                    flagITEM_Diamond_legging(), flagITEM_Diamond_boots(), flagLARANJA(), flagVERDE()
            );

            for (ItemStack item : itemsToDrop) {
                if (item != null && item.getType() != Material.AIR) {
                    loc.getWorld().dropItemNaturally(loc, item);
                }
            }
        });
    }
}