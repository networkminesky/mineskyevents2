package net.mineskyevents.listeners;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.events.BaseEvent;
import net.mineskyevents.entities.events.SpleefEvent;
import net.mineskyevents.managers.EventManager;
import net.mineskyevents.models.EventState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        EventManager manager = MineSkyEvents.getInstance().getEventManager();

        if (manager.getState() != EventState.RUNNING || !manager.isPlaying(player)) return;

        BaseEvent activeEvent = manager.getActiveEvent();

        if (activeEvent instanceof SpleefEvent) {
            Material blockType = event.getBlock().getType();

            if (blockType == Material.SNOW_BLOCK || blockType == Material.SNOW) {
                Location blockLoc = event.getBlock().getLocation();

                SpleefEvent.brokenBlocks.put(blockLoc, blockType);

                event.getBlock().setType(Material.AIR);
            }

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        EventManager manager = MineSkyEvents.getInstance().getEventManager();

        if (manager.getState() != EventState.RUNNING || !manager.isPlaying(player)) return;

        BaseEvent activeEvent = manager.getActiveEvent();
        if (activeEvent == null) return;

        if (activeEvent instanceof SpleefEvent) {
            Location pos1 = activeEvent.getPos1();
            Location pos2 = activeEvent.getPos2();

            if (pos1 != null && pos2 != null) {
                double minY = Math.min(pos1.getY(), pos2.getY());
                double playerY = player.getLocation().getY();

                Material underFeet = player.getLocation().getBlock().getType();

                if (playerY < (minY - 1.5) || underFeet == Material.WATER || underFeet == Material.LAVA) {
                    activeEvent.eliminatePlayer(player);
                }
            }
        }
    }

    @EventHandler
    public void onPvpDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player defender)) return;

        EventManager manager = MineSkyEvents.getInstance().getEventManager();

        if (manager.isPlaying(defender) && manager.getState() != EventState.RUNNING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        EventManager manager = MineSkyEvents.getInstance().getEventManager();

        if (manager.isPlaying(player)) {
            BaseEvent activeEvent = manager.getActiveEvent();
            if (activeEvent != null && manager.getState() == EventState.RUNNING) {
                activeEvent.eliminatePlayer(player);
            }
            manager.handleQuit(player);
        }
    }
}