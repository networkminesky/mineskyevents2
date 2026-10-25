package net.mineskyevents.listeners;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.events.*;
import net.mineskyevents.managers.EventManager;
import net.mineskyevents.models.EventState;
import net.mineskyevents.utils.EventItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class GameplayListener implements Listener {

    private EventManager manager() {
        return MineSkyEvents.getInstance().getEventManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("mineskyevents.admin") && !manager().isPlaying(player)) {
            return;
        }

        BaseEvent active = manager().getActiveEvent();
        Material type = event.getBlock().getType();
        boolean allowed = manager().isPlaying(player)
                && manager().getState() == EventState.RUNNING
                && active != null
                && active.canBreak(type);

        if (!allowed) {
            event.setCancelled(true);
            return;
        }

        event.setDropItems(false);
        if (active instanceof SpleefEvent) {
            SpleefEvent.brokenBlocks.put(event.getBlock().getLocation(), type);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("mineskyevents.admin") && !manager().isPlaying(player)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player && manager().isPlaying(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        if (item.getItemMeta() != null && item.getItemMeta().isUnbreakable()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        ItemStack stack = event.getItem().getItemStack();
        if (stack.getItemMeta() == null) return;
        var active = manager().getActiveEvent();
        if (!(active instanceof TijolaoWarsEvent tijolao)) return;
        Byte marker = stack.getItemMeta().getPersistentDataContainer()
                .get(tijolao.getBrickProjectileKey(), PersistentDataType.BYTE);
        if (marker != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        if (!manager().isPlaying(player)) return;

        EventState state = manager().getState();
        var active = manager().getActiveEvent();
        if (active == null) return;

        if (state == EventState.RUNNING) {
            handleRunningMove(player, active, event);
        }
    }

    private void handleRunningMove(Player player, BaseEvent active, PlayerMoveEvent event) {
        if (active instanceof SpleefEvent || active instanceof SumoEvent || active instanceof TntRunEvent) {
            if (active.eliminatesOnVoid() && player.getLocation().getY() < active.getArenaMinY() - 1.5) {
                active.eliminatePlayer(player);
            }
        }

        if (active instanceof CorridaEvent corrida) {
            for (int i = 0; i < active.getCheckpoints().size(); i++) {
                Location cp = active.getCheckpoints().get(i);
                if (player.getLocation().distanceSquared(cp) < 16) {
                    corrida.onCheckpoint(player, i);
                }
            }
            if (corrida.isNearFinish(player)) {
                corrida.onFinish(player);
            }
        }

        if (active instanceof CorridaBarcoEvent barco && barco.isNearFinish(player)) {
            barco.onFinish(player);
        }

        if (active instanceof RunnersEvent runners && runners.isNearExit(player)) {
            runners.onExitReached(player);
        }

        if (active instanceof CorridaElytraEvent elytra) {
            elytra.handleMovement(player, event.getFrom(), event.getTo());
            elytra.tryFinish(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onVehicleExit(VehicleExitEvent event) {
        if (!(event.getExited() instanceof Player player)) return;
        if (manager().getState() != EventState.RUNNING || !manager().isPlaying(player)) return;
        if (manager().getActiveEvent() instanceof CorridaBarcoEvent barco && barco.isEventBoat(event.getVehicle())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        Vehicle vehicle = event.getVehicle();
        if (!(manager().getActiveEvent() instanceof CorridaBarcoEvent barco)) return;
        if (barco.isEventBoat(vehicle)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!(event.getVehicle() instanceof Boat boat)) return;
        if (manager().getState() != EventState.RUNNING) return;
        if (manager().getActiveEvent() instanceof CorridaBarcoEvent barco) {
            barco.tryBoatJump(boat);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBoatDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Boat boat)) return;
        if (manager().getActiveEvent() instanceof CorridaBarcoEvent barco && barco.isEventBoat(boat)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player defender)) return;

        EventManager manager = manager();
        boolean inEvent = manager.isPlaying(defender) || manager.isSpectating(defender);

        if (inEvent && manager.getState() != EventState.RUNNING) {
            event.setCancelled(true);
            return;
        }

        if (manager.getState() != EventState.RUNNING || !manager.isPlaying(defender)) {
            if (manager.getState() == EventState.ENDING) {
                event.setCancelled(true);
            }
            return;
        }

        var active = manager.getActiveEvent();
        if (active == null) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            event.setCancelled(true);
            if (active.eliminatesOnVoid()) {
                if (active instanceof MiniWarsEvent wars) {
                    wars.eliminateFromVoid(defender);
                } else {
                    active.eliminatePlayer(defender);
                }
            } else {
                recoverPlayer(defender, active);
            }
            return;
        }

        if (event instanceof EntityDamageByEntityEvent byEntity) {
            handleEntityDamageByEntity(byEntity, active, defender);
            return;
        }

        if (!active.allowsVanillaDeath()) {
            event.setCancelled(true);
        }
    }

    private void handleEntityDamageByEntity(EntityDamageByEntityEvent event, BaseEvent active, Player defender) {
        Player attacker = resolveAttacker(event);
        EventManager manager = manager();

        if (attacker != null && manager.isPlaying(attacker)) {
            if (!attacker.hasPermission("mineskyevents.admin") && !active.allowsPlayerVsPlayer()) {
                event.setCancelled(true);
                return;
            }
        }

        if (active instanceof MiniWarsEvent wars && attacker != null) {
            if (wars.isSameTeam(attacker, defender)) {
                event.setCancelled(true);
                return;
            }
            if (manager.isPlaying(attacker)) {
                wars.applyCannonSeatProtection(defender, event, attacker);
                wars.onProjectileShieldHit(defender, event);
                if (!wars.consumeWindDamageMark(defender)) {
                    wars.recordCombatAttribution(defender, attacker, resolveMiniWarsKillCause(event));
                }
            }
        }

        if (active instanceof TntTagEvent tag && attacker != null) {
            if (attacker.getUniqueId().equals(tag.getTaggedPlayer())) {
                tag.transferTag(attacker, defender);
            }
            event.setCancelled(true);
            return;
        }

        if (active instanceof EscondeEscondeEvent hide && attacker != null) {
            hide.catchPlayer(attacker, defender);
            event.setCancelled(true);
            return;
        }

        if (active instanceof TijolaoWarsEvent) {
            event.setCancelled(true);
            return;
        }

        if (!active.allowsPlayerVsPlayer()) {
            event.setCancelled(true);
        }
    }

    private static MiniWarsEvent.KillCause resolveMiniWarsKillCause(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof org.bukkit.entity.Arrow) {
            return MiniWarsEvent.KillCause.CROSSBOW;
        }
        if (event.getDamager() instanceof org.bukkit.entity.Trident) {
            return MiniWarsEvent.KillCause.CROSSBOW;
        }
        if (event.getDamager() instanceof org.bukkit.entity.Projectile) {
            return MiniWarsEvent.KillCause.CROSSBOW;
        }
        return MiniWarsEvent.KillCause.SWORD;
    }

    private Player resolveAttacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            return player;
        }
        if (event.getDamager() instanceof Projectile projectile
                && projectile.getShooter() instanceof Player shooter) {
            return shooter;
        }
        return null;
    }

    @EventHandler
    public void onRegain(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!manager().isPlaying(player) || manager().getState() != EventState.RUNNING) return;

        var active = manager().getActiveEvent();
        if (active instanceof TijolaoWarsEvent) {
            event.setCancelled(true);
            return;
        }
        if (active instanceof MiniWarsEvent) {
            var reason = event.getRegainReason();
            if (reason == EntityRegainHealthEvent.RegainReason.SATIATED
                    || reason == EntityRegainHealthEvent.RegainReason.REGEN) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!manager().isPlaying(player) || manager().getState() != EventState.RUNNING) return;

        var active = manager().getActiveEvent();
        if (active == null) return;

        if (!active.allowsVanillaDeath()) {
            event.setCancelled(true);
            event.getDrops().clear();
            event.setDeathMessage(null);
            Bukkit.getScheduler().runTask(MineSkyEvents.getInstance(), () -> {
                player.setHealth(player.getMaxHealth());
                Player killer = player.getKiller();
                EventManager em = manager();
                if (killer != null && !em.isPlaying(killer)) {
                    killer = null;
                }
                active.eliminatePlayer(player, killer);
            });
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (manager().getState() != EventState.RUNNING) return;
        if (!manager().isPlaying(event.getPlayer())) return;

        var active = manager().getActiveEvent();
        Player player = event.getPlayer();

        Action action = event.getAction();
        if (event.getItem() != null && EventItems.isCheckpointPlate(event.getItem())) {
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                if (active instanceof CorridaElytraEvent elytra) {
                    elytra.teleportToLastCheckpoint(player);
                }
            }
            return;
        }

        if (!(active instanceof TijolaoWarsEvent tijolao)) return;

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK
                && action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        if (event.getItem() != null && event.getItem().getType() == Material.BRICK) {
            event.setCancelled(true);
            tijolao.tryThrowBrick(player);
        }
    }

    private void recoverPlayer(Player player, BaseEvent active) {
        Location recovery = active.getRecoveryLocation(player);
        if (recovery != null) {
            player.teleport(recovery);
            player.setFallDistance(0f);
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
