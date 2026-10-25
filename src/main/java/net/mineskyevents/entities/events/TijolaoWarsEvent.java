package net.mineskyevents.entities.events;

import net.kyori.adventure.text.Component;
import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.models.EventState;
import net.mineskyevents.models.EventType;
import net.mineskyevents.utils.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class TijolaoWarsEvent extends BaseEvent {

    private final NamespacedKey brickKey;
    private final Map<UUID, Long> throwCooldowns = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<FlyingBrick> bricks = new CopyOnWriteArrayList<>();
    private Scheduler.Task brickTask;
    private Scheduler.Task regenTask;

    public TijolaoWarsEvent(EventType type, Category category, String mapId) {
        super(type, category, mapId);
        this.brickKey = new NamespacedKey(MineSkyEvents.getInstance(), "tijolao_brick");
    }

    public NamespacedKey getBrickProjectileKey() {
        return brickKey;
    }

    @Override
    protected void onGameStart() {
        for (UUID uuid : alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                giveKit(p);
                p.setSaturation(0f);
                giveBrickItem(p);
            }
        }
        brickTask = Scheduler.runTaskTimer(this::tickBricks, 1L, 1L);
        startSlowRegen();
    }

    private void startSlowRegen() {
        if (regenTask != null) regenTask.cancel();
        regenTask = Scheduler.runTaskTimer(() -> {
            if (getEventManager().getState() != EventState.RUNNING) return;
            for (UUID uuid : alivePlayers) {
                Player p = Bukkit.getPlayer(uuid);
                if (p == null || !p.isOnline()) continue;
                p.setFoodLevel(20);
                p.setSaturation(0f);
                double max = p.getMaxHealth();
                if (p.getHealth() < max) {
                    p.setHealth(Math.min(max, p.getHealth() + 0.5));
                }
            }
        }, 40L, 40L);
    }

    @Override
    protected void onLateJoin(Player player) {
        super.onLateJoin(player);
        player.setSaturation(0f);
        giveBrickItem(player);
    }

    public void giveBrickItem(Player player) {
        player.getInventory().setItem(0, createBrickItem());
    }

    private ItemStack createBrickItem() {
        ItemStack brick = new ItemStack(Material.BRICK);
        ItemMeta meta = brick.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§6Tijolão"));
            meta.getPersistentDataContainer().set(brickKey, PersistentDataType.BYTE, (byte) 1);
            brick.setItemMeta(meta);
        }
        return brick;
    }

    public boolean tryThrowBrick(Player player) {
        if (!alivePlayers.contains(player.getUniqueId())) return false;

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() != Material.BRICK) {
            giveBrickItem(player);
        }

        long cooldownMs = MineSkyEvents.getInstance().getConfig().getInt("tijolao.throw-cooldown-ms", 800);
        long now = System.currentTimeMillis();
        Long last = throwCooldowns.get(player.getUniqueId());
        if (last != null && now - last < cooldownMs) {
            return false;
        }
        throwCooldowns.put(player.getUniqueId(), now);

        Location eye = player.getEyeLocation();
        Item dropped = player.getWorld().dropItem(eye, createBrickItem());
        dropped.setPickupDelay(Integer.MAX_VALUE);
        dropped.setCanMobPickup(false);
        dropped.setUnlimitedLifetime(true);
        dropped.setThrower(player.getUniqueId());
        Vector velocity = eye.getDirection().normalize().multiply(1.15);
        dropped.setVelocity(velocity);
        bricks.add(new FlyingBrick(dropped, player.getUniqueId()));

        giveBrickItem(player);
        return true;
    }

    private void tickBricks() {
        Iterator<FlyingBrick> it = bricks.iterator();
        while (it.hasNext()) {
            FlyingBrick brick = it.next();
            Item item = brick.item;
            if (item == null || !item.isValid() || item.isDead()) {
                bricks.remove(brick);
                continue;
            }

            if (!brick.hit) {
                for (Player target : item.getWorld().getNearbyPlayers(item.getLocation(), 1.4)) {
                    if (target.getUniqueId().equals(brick.shooter)) continue;
                    if (!alivePlayers.contains(target.getUniqueId())) continue;
                    if (!target.getBoundingBox().expand(0.3).overlaps(item.getBoundingBox())) continue;

                    Player shooter = Bukkit.getPlayer(brick.shooter);
                    if (shooter != null) {
                        onBrickHit(shooter, target);
                    }
                    brick.hit = true;
                    item.setVelocity(new Vector(0, -0.15, 0));
                    break;
                }
            }

            boolean resting = item.isOnGround() || isResting(item);
            if (resting || brick.hit) {
                if (brick.grounded < 0) brick.grounded = 0;
                brick.grounded++;
                if (brick.grounded >= 20) {
                    item.remove();
                    bricks.remove(brick);
                }
            }
        }
    }

    private boolean isResting(Item item) {
        if (item.getVelocity().lengthSquared() > 0.02) return false;
        return item.getLocation().clone().subtract(0, 0.25, 0).getBlock().getType().isSolid();
    }

    public void onBrickHit(Player shooter, Player target) {
        if (!alivePlayers.contains(shooter.getUniqueId()) || !alivePlayers.contains(target.getUniqueId())) {
            return;
        }
        if (shooter.getUniqueId().equals(target.getUniqueId())) return;

        double damage = MineSkyEvents.getInstance().getConfig().getDouble("tijolao.brick-damage", 4.0);
        target.setSaturation(0f);
        target.playSound(target.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 0.95f);
        if (target.getHealth() - damage <= 0) {
            eliminatePlayer(target);
        } else {
            target.damage(damage, shooter);
        }
    }

    @Override
    protected void onPlayerEliminated(Player player, Player killer, Location deathLocation) {
        throwCooldowns.remove(player.getUniqueId());
    }

    @Override
    protected void onCleanUp() {
        if (brickTask != null) brickTask.cancel();
        if (regenTask != null) regenTask.cancel();
        for (FlyingBrick brick : bricks) {
            if (brick.item != null && brick.item.isValid()) brick.item.remove();
        }
        bricks.clear();
        throwCooldowns.clear();
    }

    private static final class FlyingBrick {
        final Item item;
        final UUID shooter;
        boolean hit;
        int grounded = -1;

        FlyingBrick(Item item, UUID shooter) {
            this.item = item;
            this.shooter = shooter;
        }
    }
}
