package net.mineskyevents.gui.miniwars;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.events.MiniWarsEvent;
import net.mineskyevents.managers.EventManager;
import net.mineskyevents.models.EventState;
import net.mineskyevents.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import net.mineskyevents.utils.EventItems;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class MiniWarsTeamGuiListener implements Listener {

    @EventHandler
    public void onTeamPickerInteract(PlayerInteractEvent event) {
        EventManager manager = MineSkyEvents.getInstance().getEventManager();
        EventState state = manager.getState();
        if (state != EventState.WAITING && state != EventState.STARTING) return;
        if (!manager.isPlaying(event.getPlayer())) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK
                && action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (!EventItems.isMiniWarsTeamPicker(item)) return;

        event.setCancelled(true);
        MiniWarsTeamSelector.tryOpenGui(event.getPlayer());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!MiniWarsTeamGui.isTeamGui(event.getInventory())) return;

        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        Integer team = MiniWarsTeamGui.readTeamPick(clicked);
        if (team == null) return;

        MiniWarsEvent wars = activeMiniWars();
        if (wars == null || !wars.isTeamSelectionAllowed()) {
            player.closeInventory();
            Messages.send(player, "miniwars.team-selection-closed");
            return;
        }

        if (!wars.getEventManager().isPlaying(player)) {
            player.closeInventory();
            return;
        }

        if (wars.trySelectTeam(player, team)) {
            Messages.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.1f);
            Messages.send(player, "miniwars.team-selected",
                    net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.parsed(
                            "team", MiniWarsEvent.TEAM_NAMES[team]));
            MiniWarsTeamGui.refresh(player, wars);
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.equals(player)) continue;
                if (MiniWarsTeamGui.isTeamGui(online.getOpenInventory().getTopInventory())) {
                    MiniWarsTeamGui.refresh(online, wars);
                }
            }
        } else {
            Messages.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f);
            Messages.send(player, "miniwars.team-unbalanced");
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (MiniWarsTeamGui.isTeamGui(event.getInventory())) {
            event.setCancelled(true);
        }
    }

    private MiniWarsEvent activeMiniWars() {
        EventManager manager = MineSkyEvents.getInstance().getEventManager();
        if (manager.getState() == EventState.INACTIVE) return null;
        if (!(manager.getActiveEvent() instanceof MiniWarsEvent wars)) return null;
        return wars;
    }
}
