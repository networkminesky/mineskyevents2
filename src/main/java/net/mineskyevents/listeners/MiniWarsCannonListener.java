package net.mineskyevents.listeners;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.events.MiniWarsEvent;
import net.mineskyevents.managers.EventManager;
import net.mineskyevents.miniwars.MiniWarsCannonManager;
import net.mineskyevents.miniwars.TeamCannon;
import net.mineskyevents.models.EventState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.EquipmentSlot;

public class MiniWarsCannonListener implements Listener {

    private EventManager manager() {
        return MineSkyEvents.getInstance().getEventManager();
    }

    private MiniWarsEvent miniWars() {
        if (manager().getState() != EventState.RUNNING) return null;
        if (!(manager().getActiveEvent() instanceof MiniWarsEvent wars)) return null;
        return wars;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractCannon(PlayerInteractAtEntityEvent event) {
        MiniWarsEvent wars = miniWars();
        if (wars == null) return;

        Entity clicked = event.getRightClicked();
        MiniWarsCannonManager cannons = wars.getCannonManager();
        if (cannons == null) return;

        for (TeamCannon cannon : cannons.getCannons()) {
            if (cannon.getDisplay() != null && cannon.getDisplay().getUniqueId().equals(clicked.getUniqueId())) {
                event.setCancelled(true);
                cannon.tryMount(event.getPlayer());
                return;
            }
            if (cannon.getSeat() != null && cannon.getSeat().getUniqueId().equals(clicked.getUniqueId())) {
                event.setCancelled(true);
                cannon.tryMount(event.getPlayer());
                return;
            }
        }
    }

    /** Clique esquerdo no ar (e em geral) não dispara PlayerInteractEvent em veículos — usa animação do braço. */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onArmSwing(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) return;
        tryCannonFire(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onFireInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        tryCannonFire(event.getPlayer());
        if (isRidingCannon(event.getPlayer())) {
            event.setUseItemInHand(Event.Result.DENY);
            event.setCancelled(true);
        }
    }

    private void tryCannonFire(Player player) {
        MiniWarsEvent wars = miniWars();
        if (wars == null) return;

        MiniWarsCannonManager cannons = wars.getCannonManager();
        if (cannons == null) return;

        TeamCannon cannon = cannons.findCannonRiddenBy(player);
        if (cannon == null) return;

        cannon.fire(player);
    }

    private boolean isRidingCannon(Player player) {
        MiniWarsEvent wars = miniWars();
        if (wars == null) return false;
        MiniWarsCannonManager cannons = wars.getCannonManager();
        return cannons != null && cannons.findCannonRiddenBy(player) != null;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (miniWars() == null) return;
        TeamCannon.handleWindProjectileHit(event);
    }

    @EventHandler
    public void onCannonDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ItemDisplay || entity.getType().name().contains("ARMOR_STAND")) {
            MiniWarsEvent wars = miniWars();
            if (wars == null) return;
            MiniWarsCannonManager cannons = wars.getCannonManager();
            if (cannons == null) return;
            for (TeamCannon cannon : cannons.getCannons()) {
                if (cannon.getDisplay() != null && cannon.getDisplay().getUniqueId().equals(entity.getUniqueId())) {
                    event.setCancelled(true);
                    return;
                }
                if (cannon.getSeat() != null && cannon.getSeat().getUniqueId().equals(entity.getUniqueId())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onDismount(VehicleExitEvent event) {
        if (!(event.getExited() instanceof Player player)) return;
        MiniWarsEvent wars = miniWars();
        if (wars == null) return;
        MiniWarsCannonManager cannons = wars.getCannonManager();
        if (cannons == null) return;
        Entity vehicle = event.getVehicle();
        for (TeamCannon cannon : cannons.getCannons()) {
            if (cannon.getSeat() != null && cannon.getSeat().getUniqueId().equals(vehicle.getUniqueId())) {
                cannon.playDismountSound(player);
                return;
            }
        }
    }
}
