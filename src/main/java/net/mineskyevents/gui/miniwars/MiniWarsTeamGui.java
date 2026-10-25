package net.mineskyevents.gui.miniwars;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.events.MiniWarsEvent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MiniWarsTeamGui {

    public static final String TITLE = "Mini Wars — Times";
    public static NamespacedKey teamPickKey() {
        return new NamespacedKey(MineSkyEvents.getInstance(), "miniwars-team-pick");
    }

    private static final int[] TEAM_COLUMNS = {1, 3, 5, 7};
    private static final Color[] TEAM_LEATHER = {
            Color.fromRGB(0xB02E26),
            Color.fromRGB(0x3C44AA),
            Color.fromRGB(0x5E7C16),
            Color.fromRGB(0xFED83D)
    };
    private static final int MAX_PLAYER_ROWS = 5;

    private MiniWarsTeamGui() {}

    public static void open(Player viewer, MiniWarsEvent event) {
        viewer.openInventory(build(viewer, event));
    }

    public static void refresh(Player viewer, MiniWarsEvent event) {
        Inventory top = viewer.getOpenInventory().getTopInventory();
        if (!(top.getHolder() instanceof Holder)) return;
        Inventory fresh = build(viewer, event);
        if (top.getSize() != fresh.getSize()) {
            viewer.openInventory(fresh);
            return;
        }
        top.clear();
        for (int slot = 0; slot < top.getSize(); slot++) {
            top.setItem(slot, fresh.getItem(slot));
        }
    }

    public static boolean isTeamGui(Inventory inventory) {
        return inventory != null && inventory.getHolder() instanceof Holder;
    }

    public static Integer readTeamPick(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        Byte raw = item.getItemMeta().getPersistentDataContainer().get(teamPickKey(), PersistentDataType.BYTE);
        return raw == null ? null : raw.intValue();
    }

    private static Inventory build(Player viewer, MiniWarsEvent event) {
        int maxListed = 0;
        for (int t = 0; t < MiniWarsEvent.TEAM_COUNT; t++) {
            maxListed = Math.max(maxListed, event.getTeamPlayers(t).size());
        }
        int playerRows = Math.min(MAX_PLAYER_ROWS, Math.max(1, maxListed));
        boolean needsOverflowRow = false;
        for (int t = 0; t < MiniWarsEvent.TEAM_COUNT; t++) {
            if (event.getTeamPlayers(t).size() > playerRows) {
                needsOverflowRow = true;
                break;
            }
        }
        int invRows = Math.min(6, 1 + playerRows + (needsOverflowRow ? 1 : 0));
        int size = invRows * 9;

        Holder holder = new Holder(viewer.getUniqueId());
        Inventory inv = Bukkit.createInventory(holder, size, TITLE);
        holder.bind(inv);

        Integer currentTeam = event.getPlayerTeamId(viewer);
        boolean bypass = viewer.hasPermission(MiniWarsEvent.PERM_BYPASS_TEAM_LIMIT);

        for (int team = 0; team < MiniWarsEvent.TEAM_COUNT; team++) {
            int col = TEAM_COLUMNS[team];
            inv.setItem(col, teamSelectorItem(event, team, currentTeam, bypass));

            List<Player> members = event.getTeamPlayers(team);
            int listed = Math.min(playerRows, members.size());
            for (int row = 0; row < listed; row++) {
                int slot = (row + 1) * 9 + col;
                inv.setItem(slot, playerHead(members.get(row), team));
            }
            if (members.size() > playerRows) {
                int overflowRow = 1 + playerRows;
                int overflowSlot = overflowRow * 9 + col;
                if (overflowSlot < size) {
                    inv.setItem(overflowSlot, overflowItem(members.size() - playerRows));
                }
            }
        }

        return inv;
    }

    private static ItemStack teamSelectorItem(MiniWarsEvent event, int team, Integer currentTeam, boolean bypass) {
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        if (meta != null) {
            meta.setColor(TEAM_LEATHER[team]);
            String code = MiniWarsEvent.teamColorCode(team);
            meta.setDisplayName(code + "§lTime " + MiniWarsEvent.TEAM_NAMES[team]);
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Jogadores: §f" + event.getTeamSize(team));
            if (currentTeam != null && currentTeam == team) {
                lore.add("§a✔ Seu time atual");
            } else {
                lore.add("§e› Clique para entrar neste time");
            }
            if (!bypass) {
                lore.add("§8Balanceamento: diferença máx. 1 entre times");
            } else {
                lore.add("§dBypass de limite ativo");
            }
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(teamPickKey(), PersistentDataType.BYTE, (byte) team);
            if (currentTeam != null && currentTeam == team) {
                meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack playerHead(Player member, int team) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(member);
            meta.setDisplayName(MiniWarsEvent.teamColorCode(team) + member.getName());
            head.setItemMeta(meta);
        }
        return head;
    }

    private static ItemStack overflowItem(int extra) {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7+" + extra + " jogador(es)");
            item.setItemMeta(meta);
        }
        return item;
    }

    public static final class Holder implements InventoryHolder {
        private final UUID viewerId;
        private Inventory inventory;

        Holder(UUID viewerId) {
            this.viewerId = viewerId;
        }

        public UUID viewerId() {
            return viewerId;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        void bind(Inventory inventory) {
            this.inventory = inventory;
        }
    }
}
