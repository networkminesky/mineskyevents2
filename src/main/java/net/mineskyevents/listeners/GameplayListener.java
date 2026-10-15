package net.mineskyevents.listeners;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.events.*;
import net.mineskyevents.managers.EventManager;
import net.mineskyevents.models.EventState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GameplayListener implements Listener {

    private EventManager manager() {
        return MineSkyEvents.getInstance().getEventManager();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (manager().getState() != EventState.RUNNING || !manager().isPlaying(player)) return;

        var active = manager().getActiveEvent();
        if (active instanceof SpleefEvent) {
            Material type = event.getBlock().getType();
            if (type == Material.SNOW_BLOCK || type == Material.SNOW) {
                Location loc = event.getBlock().getLocation();
                SpleefEvent.brokenBlocks.put(loc, type);
                event.getBlock().setType(Material.AIR);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        if (manager().getState() != EventState.RUNNING || !manager().isPlaying(player)) return;

        var active = manager().getActiveEvent();
        if (active == null) return;

        if (active instanceof SpleefEvent || active instanceof SumoEvent || active instanceof TntRunEvent) {
            if (player.getLocation().getY() < active.getArenaMinY() - 1.5) {
                active.eliminatePlayer(player);
            }
        }

        if (active instanceof CorridaEvent corrida && corrida.isNearFinish(player)) {
            corrida.onFinish(player);
        }

        if (active instanceof CorridaBarcoEvent barco && barco.isNearFinish(player)) {
            barco.onFinish(player);
        }

        if (active instanceof RunnersEvent runners && runners.isNearExit(player)) {
            runners.onExitReached(player);
        }

        if (active instanceof CorridaElytraEvent elytra) {
            for (int i = 0; i < active.getCheckpoints().size(); i++) {
                Location cp = active.getCheckpoints().get(i);
                if (player.getLocation().distanceSquared(cp) < 16) {
                    elytra.onRingPassed(player, i);
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player defender)) return;

        if (manager().isPlaying(defender) && manager().getState() != EventState.RUNNING) {
            event.setCancelled(true);
            return;
        }

        if (manager().getState() != EventState.RUNNING) return;

        var active = manager().getActiveEvent();
        if (active == null) return;

        if (active instanceof MiniWarsEvent wars && event.getDamager() instanceof Player attacker) {
            if (wars.isSameTeam(attacker, defender)) {
                event.setCancelled(true);
            }
        }

        if (active instanceof TntTagEvent tag && event.getDamager() instanceof Player attacker) {
            tag.transferTag(defender, attacker);
            event.setCancelled(true);
        }

        if (active instanceof EscondeEscondeEvent hide && event.getDamager() instanceof Player seeker) {
            hide.catchPlayer(seeker, defender);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player shooter)) return;
        if (!(event.getHitEntity() instanceof Player target)) return;
        if (manager().getState() != EventState.RUNNING) return;

        var active = manager().getActiveEvent();
        if (active instanceof TijolaoWarsEvent && event.getEntity() instanceof Snowball) {
            if (manager().isPlaying(target) && manager().isPlaying(shooter)) {
                active.eliminatePlayer(target);
            }
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.VOID) return;
        if (!manager().isPlaying(player)) return;

        var active = manager().getActiveEvent();
        if (active != null && manager().getState() == EventState.RUNNING) {
            event.setCancelled(true);
            active.eliminatePlayer(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (manager().isPlaying(player) && manager().getState() == EventState.RUNNING) {
            var active = manager().getActiveEvent();
            if (active != null) active.eliminatePlayer(player);
        }
        manager().handleQuit(player);
    }
}
