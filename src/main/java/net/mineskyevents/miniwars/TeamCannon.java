package net.mineskyevents.miniwars;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.events.MiniWarsEvent;
import net.mineskyevents.managers.EventManager;
import net.mineskyevents.models.EventState;
import net.mineskyevents.utils.Messages;
import net.mineskyevents.utils.Scheduler;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.UUID;

public class TeamCannon {

    public static NamespacedKey windProjectileKey() {
        return new NamespacedKey(MineSkyEvents.getInstance(), "miniwars-wind-shot");
    }

    private static final int CUSTOM_MODEL_DATA = 10088;
    private static final double SEAT_BACK_OFFSET = 1.35;
    private static final double SEAT_FORWARD_ADJUST = 0.5;
    private static final double SEAT_DOWN_ADJUST = 0.35;
    private static final double SEAT_PITCH_FORWARD_MAX = 0.5;
    private static final double MUZZLE_OFFSET = 2.5;
    private static final double MUZZLE_SPAWN_Y_OFFSET = -0.12;
    private static final float FIRE_SPEED = 2.2f;
    private static final long FIRE_COOLDOWN_TICKS = 120L;
    private static final long CANNON_DISABLE_TICKS = 200L;
    private static final int COOLDOWN_BAR_COUNT = 6;
    private static final double BLAST_RADIUS = 5.5;
    private static final double BLAST_DAMAGE = 8.0;
    private static final double LAUNCH_HORIZONTAL = 1.55;
    private static final double LAUNCH_VERTICAL_MIN = 0.95;
    private static final double LAUNCH_VERTICAL_MAX = 1.65;
    private static final double CANNON_HIT_RADIUS = 4.25;
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static final float MIN_PITCH = -15f;
    private static final float MAX_PITCH = 15f;
    private static final float DISPLAY_SCALE = 2.4f;
    private static final float DISPLAY_Y_OFFSET = 1f;

    private static final Color[] TEAM_COLORS = {
            Color.RED, Color.BLUE, Color.LIME, Color.YELLOW
    };

    private final int team;
    private final MiniWarsEvent event;
    private float yaw;
    private float pitch;
    private long lastFireTick = -FIRE_COOLDOWN_TICKS;
    private long disabledUntilTick = 0L;
    private boolean reloadReadyFxPending;
    private int disableSmokeTick;
    private final Location base;

    private ItemDisplay display;
    private ArmorStand seat;

    public TeamCannon(int team, Location base, MiniWarsEvent event) {
        this.team = team;
        this.base = base.clone();
        this.yaw = base.getYaw();
        this.pitch = clampPitch(base.getPitch());
        this.event = event;
        spawnEntities();
    }

    private void spawnEntities() {
        World world = base.getWorld();
        if (world == null) return;

        Location displayLoc = base.clone();
        display = world.spawn(displayLoc, ItemDisplay.class, entity -> {
            entity.setItemStack(cannonModel(team));
            entity.setInvulnerable(true);
            entity.setPersistent(true);
            entity.setGravity(false);
            entity.setViewRange(256);
            configureDisplayInterpolation(entity);
            applyDisplayTransform(entity);
        });

        Location seatLoc = seatLocation();
        seat = world.spawn(seatLoc, ArmorStand.class, entity -> {
            entity.setVisible(false);
            entity.setGravity(false);
            entity.setInvulnerable(true);
            entity.setMarker(false);
            entity.setSmall(true);
            entity.setSilent(true);
            entity.setBasePlate(false);
            entity.setArms(false);
            entity.setPersistent(true);
            entity.setCustomNameVisible(false);
        });
        alignEntities();
    }

    public void tick() {
        if (display == null || seat == null || display.isDead() || seat.isDead()) return;

        if (isDisabled()) {
            disableSmokeTick++;
            if (disableSmokeTick % 4 == 0) {
                spawnDisabledSmoke();
            }
        } else {
            disableSmokeTick = 0;
        }

        Entity rider = seat.getPassengers().isEmpty() ? null : seat.getPassengers().get(0);
        if (rider instanceof Player player) {
            EventManager manager = event.getEventManager();
            if (!manager.isPlaying(player) || manager.getState() != EventState.RUNNING) {
                ejectRider(player);
                return;
            }
            if (isDisabled()) {
                ejectRider(player);
                player.sendMessage("§cO canhão foi danificado e está inoperante.");
                return;
            }
            applyCannonGlow(player);
            yaw = player.getLocation().getYaw();
            pitch = clampPitch(player.getLocation().getPitch());
            sendCooldownActionBar(player);
            tryPlayReloadReadyFx(player);
        }
        alignEntities();
    }

    private void alignEntities() {
        Location displayLoc = base.clone();
        displayLoc.setYaw(yaw);
        displayLoc.setPitch(pitch);
        display.teleport(displayLoc);
        display.setRotation(yaw, pitch);
        applyDisplayTransform(display);

        Location seatLoc = seatLocation();
        seat.teleport(seatLoc);
        seat.setRotation(yaw, pitch);
    }

    private static float clampPitch(float value) {
        return Math.max(MIN_PITCH, Math.min(MAX_PITCH, value));
    }

    private Location seatLocation() {
        Vector aim = aimDirection();
        double pitchForward = seatPitchForwardOffset();
        Location seatLoc = base.clone()
                .add(aim.clone().multiply(-SEAT_BACK_OFFSET + SEAT_FORWARD_ADJUST + pitchForward))
                .subtract(0, SEAT_DOWN_ADJUST, 0);
        seatLoc.setYaw(yaw);
        seatLoc.setPitch(pitch);
        return seatLoc;
    }

    private double seatPitchForwardOffset() {
        if (pitch <= 0f) {
            return 0d;
        }
        double factor = pitch / MAX_PITCH;
        return SEAT_PITCH_FORWARD_MAX * Math.min(1d, Math.max(0d, factor));
    }

    public boolean matchesEntity(Entity entity) {
        if (entity == null) return false;
        UUID id = entity.getUniqueId();
        return (display != null && !display.isDead() && display.getUniqueId().equals(id))
                || (seat != null && !seat.isDead() && seat.getUniqueId().equals(id));
    }

    public boolean isDisabled() {
        return Bukkit.getCurrentTick() < disabledUntilTick;
    }

    public void disableFromEnemyHit(Player attacker) {
        if (attacker == null || event.isOnTeam(attacker, team)) return;

        disabledUntilTick = Bukkit.getCurrentTick() + CANNON_DISABLE_TICKS;
        seat.getPassengers().forEach(passenger -> {
            if (passenger instanceof Player player) {
                clearCannonGlow(player);
            }
            passenger.leaveVehicle();
        });

        Location at = effectLocation();
        World world = at.getWorld();
        if (world != null) {
            world.playSound(at, Sound.BLOCK_GRINDSTONE_USE, 0.9f, 0.55f);
            world.playSound(at, Sound.ENTITY_IRON_GOLEM_HURT, 0.75f, 0.7f);
            world.playSound(at, Sound.BLOCK_FIRE_EXTINGUISH, 0.6f, 1.1f);
            world.spawnParticle(Particle.LARGE_SMOKE, at, 18, 0.45, 0.35, 0.45, 0.02);
            world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, at, 12, 0.35, 0.25, 0.35, 0.015);
        }
        spawnDisabledSmoke();
        attacker.sendMessage("§aCanhão inimigo desabilitado por §f10s§a!");
        attacker.playSound(attacker.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.85f, 1.25f);
    }

    private void spawnDisabledSmoke() {
        World world = base.getWorld();
        if (world == null) return;
        Location at = effectLocation();
        world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, at, 4, 0.25, 0.15, 0.25, 0.008);
        world.spawnParticle(Particle.SMOKE, at, 2, 0.15, 0.1, 0.15, 0.01);
    }

    private Location effectLocation() {
        return base.clone().add(0, DISPLAY_Y_OFFSET * 0.5, 0);
    }

    public Location getImpactCenter() {
        if (display != null && !display.isDead()) {
            return display.getLocation().clone().add(0, DISPLAY_Y_OFFSET * 0.85, 0);
        }
        return effectLocation();
    }

    public void tryMount(Player player) {
        if (isDisabled()) {
            player.sendMessage("§cEste canhão está desabilitado.");
            playCannonSound(player, Sound.BLOCK_FIRE_EXTINGUISH, 0.7f, 0.9f);
            return;
        }
        if (!seat.getPassengers().isEmpty()) {
            player.sendMessage("§cAlguém já está usando este canhão.");
            playCannonSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 0.7f, 0.6f);
            return;
        }
        seat.addPassenger(player);
        applyCannonGlow(player);
        playCannonSound(player, Sound.ITEM_ARMOR_EQUIP_IRON, 0.95f, 1.15f);
        playCannonSoundAtSeat(Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 0.55f, 1.05f);
        playCannonSoundAtSeat(Sound.BLOCK_CHAIN_PLACE, 0.4f, 1.25f);
        Messages.send(player, "miniwars.cannon-mounted");
    }

    public void fire(Player player) {
        if (display == null || display.isDead()) return;
        if (isDisabled()) {
            player.sendMessage("§cO canhão está desabilitado.");
            playCannonSound(player, Sound.BLOCK_FIRE_EXTINGUISH, 0.65f, 0.85f);
            return;
        }
        World world = display.getWorld();
        if (world == null) return;

        long now = Bukkit.getCurrentTick();
        if (now - lastFireTick < FIRE_COOLDOWN_TICKS) {
            sendCooldownActionBar(player);
            playCannonSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 0.55f, 0.55f);
            return;
        }
        lastFireTick = now;
        reloadReadyFxPending = true;

        Vector direction = aimDirection();
        Location muzzle = muzzleLocation(player, direction);

        Snowball projectile = world.spawn(muzzle, Snowball.class, ball -> {
            ball.setShooter(player);
            ball.setGravity(true);
            ball.setInvulnerable(false);
            ball.getPersistentDataContainer().set(windProjectileKey(), PersistentDataType.BYTE, (byte) 1);
        });
        projectile.setVelocity(direction.multiply(FIRE_SPEED));
        trackProjectileTrail(projectile);

        world.spawnParticle(Particle.CLOUD, muzzle, 6, 0.12, 0.12, 0.12, 0.04);
        world.spawnParticle(Particle.SWEEP_ATTACK, muzzle, 1, 0, 0, 0, 0);
        playCannonSoundAtSeat(Sound.BLOCK_PISTON_EXTEND, 0.65f, 0.75f);
        world.playSound(muzzle, Sound.ENTITY_GENERIC_EXPLODE, 0.85f, 1.15f);
        world.playSound(muzzle, Sound.ENTITY_WIND_CHARGE_THROW, 0.6f, 0.9f);
    }

    private static void trackProjectileTrail(Snowball projectile) {
        UUID projectileId = projectile.getUniqueId();
        Scheduler.Task[] taskRef = new Scheduler.Task[1];
        taskRef[0] = Scheduler.runTaskTimer(() -> {
            Entity entity = Bukkit.getEntity(projectileId);
            if (entity == null || !entity.isValid() || entity.isDead()) {
                taskRef[0].cancel();
                return;
            }
            Location loc = entity.getLocation();
            World w = loc.getWorld();
            if (w == null) return;
            w.spawnParticle(Particle.CLOUD, loc, 2, 0.04, 0.04, 0.04, 0.01);
            w.spawnParticle(Particle.GUST, loc, 1, 0, 0, 0, 0);
        }, 0L, 1L);
    }

    private void tryPlayReloadReadyFx(Player player) {
        if (!reloadReadyFxPending) return;
        long ticksLeft = FIRE_COOLDOWN_TICKS - (Bukkit.getCurrentTick() - lastFireTick);
        if (ticksLeft > 0) return;

        reloadReadyFxPending = false;
        Location at = seat.getLocation().add(0, 1.2, 0);
        World world = at.getWorld();
        if (world != null) {
            world.spawnParticle(Particle.HAPPY_VILLAGER, at, 8, 0.35, 0.35, 0.35, 0);
            world.playSound(at, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.55f, 1.35f);
            world.playSound(at, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.45f, 1.5f);
        }
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.35f, 1.6f);
    }

    private void sendCooldownActionBar(Player player) {
        long now = Bukkit.getCurrentTick();
        long ticksLeft = FIRE_COOLDOWN_TICKS - (now - lastFireTick);
        if (ticksLeft <= 0) {
            return;
        }
        long secondsLeft = (ticksLeft + 19) / 20;
        int redBars = (int) Math.min(COOLDOWN_BAR_COUNT, Math.max(1, secondsLeft));
        int darkBars = COOLDOWN_BAR_COUNT - redBars;

        StringBuilder bar = new StringBuilder("[§c§l");
        for (int i = 0; i < redBars; i++) {
            bar.append('|');
        }
        bar.append("§8§l");
        for (int i = 0; i < darkBars; i++) {
            bar.append('|');
        }
        bar.append("§r] (").append(secondsLeft).append("s)");
        player.sendActionBar(LEGACY.deserialize(bar.toString()));
    }

    public void playDismountSound(Player player) {
        clearCannonGlow(player);
        playCannonSound(player, Sound.ITEM_ARMOR_UNEQUIP_WOLF, 0.9f, 1.05f);
        playCannonSoundAtSeat(Sound.BLOCK_IRON_TRAPDOOR_OPEN, 0.45f, 1.1f);
    }

    private void ejectRider(Player player) {
        clearCannonGlow(player);
        seat.removePassenger(player);
    }

    private static void applyCannonGlow(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0, false, false, true));
    }

    private static void clearCannonGlow(Player player) {
        player.removePotionEffect(PotionEffectType.GLOWING);
    }

    private void playCannonSound(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    private void playCannonSoundAtSeat(Sound sound, float volume, float pitch) {
        World world = base.getWorld();
        if (world == null) return;
        Location at = seat != null && !seat.isDead() ? seat.getLocation() : base;
        world.playSound(at, sound, volume, pitch);
    }

    private Location muzzleLocation(Player player, Vector direction) {
        return player.getEyeLocation().clone()
                .add(0, MUZZLE_SPAWN_Y_OFFSET, 0)
                .add(direction.clone().multiply(MUZZLE_OFFSET));
    }

    private Vector aimDirection() {
        double yawRadians = Math.toRadians(yaw);
        double pitchRadians = Math.toRadians(-pitch);
        return new Vector(
                -Math.sin(yawRadians) * Math.cos(pitchRadians),
                Math.sin(pitchRadians),
                Math.cos(yawRadians) * Math.cos(pitchRadians)
        ).normalize();
    }

    public static void handleWindProjectileHit(org.bukkit.event.entity.ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (!(projectile instanceof Snowball)) return;
        if (!projectile.getPersistentDataContainer().has(windProjectileKey(), PersistentDataType.BYTE)) return;

        EventManager manager = MineSkyEvents.getInstance().getEventManager();
        if (!(manager.getActiveEvent() instanceof MiniWarsEvent wars)) return;
        MiniWarsCannonManager cannons = wars.getCannonManager();
        if (cannons == null) return;

        Player shooter = projectile.getShooter() instanceof Player p ? p : null;
        Location hit = resolveImpactLocation(event, projectile);

        TeamCannon cannon = null;
        Entity hitEntity = event.getHitEntity();
        if (hitEntity != null) {
            cannon = cannons.findCannonByEntity(hitEntity);
        }
        if (cannon == null) {
            cannon = cannons.findCannonNear(hit, CANNON_HIT_RADIUS);
        }
        if (cannon != null) {
            cannon.disableFromEnemyHit(shooter);
        }

        event.setCancelled(true);
        projectile.remove();

        applyWindBurst(hit, shooter, wars);
    }

    private static Location resolveImpactLocation(org.bukkit.event.entity.ProjectileHitEvent event, Projectile projectile) {
        Entity hitEntity = event.getHitEntity();
        if (hitEntity != null) {
            return hitEntity.getLocation();
        }
        if (event.getHitBlock() != null) {
            return event.getHitBlock().getLocation().toCenterLocation();
        }
        return projectile.getLocation().clone();
    }

    private static void applyWindBurst(Location hit, Player shooter, MiniWarsEvent wars) {
        World world = hit.getWorld();
        if (world == null) return;

        world.playSound(hit, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.2f, 0.9f);
        world.playSound(hit, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.3f);
        world.spawnParticle(Particle.GUST, hit, 10, 0.6, 0.4, 0.6, 0.02);
        world.spawnParticle(Particle.CLOUD, hit, 12, 0.5, 0.35, 0.5, 0.05);
        world.spawnParticle(Particle.EXPLOSION, hit, 2, 0.1, 0.1, 0.1, 0);

        EventManager manager = wars.getEventManager();
        for (Entity entity : world.getNearbyEntities(hit, BLAST_RADIUS, BLAST_RADIUS, BLAST_RADIUS)) {
            if (!(entity instanceof Player target)) continue;
            if (!manager.isPlaying(target) || manager.getState() != EventState.RUNNING) continue;
            if (shooter != null && wars.isSameTeam(shooter, target)) continue;

            double distance = target.getLocation().distance(hit);
            double falloff = 1.0 - Math.min(1.0, distance / BLAST_RADIUS);
            double damage = BLAST_DAMAGE * (0.4 + 0.6 * falloff);
            if (shooter != null) {
                wars.markWindDamage(target);
                wars.recordCombatAttribution(target, shooter, MiniWarsEvent.KillCause.CANNON);
            }
            if (damage > 0.5) {
                target.damage(damage, shooter);
            }

            target.playSound(target.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);

            Vector push = target.getLocation().toVector().subtract(hit.toVector());
            if (push.lengthSquared() < 0.01) {
                push = new Vector(0, 1, 0);
            } else {
                push.normalize().multiply(LAUNCH_HORIZONTAL * (0.55 + 0.45 * falloff));
            }
            double launchY = LAUNCH_VERTICAL_MIN + (LAUNCH_VERTICAL_MAX - LAUNCH_VERTICAL_MIN) * falloff;
            push.setY(Math.max(push.getY(), launchY));
            target.setVelocity(push);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1, false, true, true));
        }
    }

    public void remove() {
        if (seat != null && !seat.isDead()) {
            seat.getPassengers().forEach(passenger -> {
                if (passenger instanceof Player player) {
                    clearCannonGlow(player);
                }
                passenger.leaveVehicle();
            });
            seat.remove();
        }
        if (display != null && !display.isDead()) {
            display.remove();
        }
        seat = null;
        display = null;
    }

    public ArmorStand getSeat() {
        return seat;
    }

    public ItemDisplay getDisplay() {
        return display;
    }

    public int getTeam() {
        return team;
    }

    private static void configureDisplayInterpolation(ItemDisplay entity) {
        entity.setInterpolationDuration(1);
        entity.setTeleportDuration(1);
        entity.setInterpolationDelay(0);
    }

    private static void applyDisplayTransform(ItemDisplay entity) {
        configureDisplayInterpolation(entity);
        entity.setTransformation(new Transformation(
                new Vector3f(0f, DISPLAY_Y_OFFSET, 0f),
                new AxisAngle4f(),
                new Vector3f(DISPLAY_SCALE, DISPLAY_SCALE, DISPLAY_SCALE),
                new AxisAngle4f()
        ));
    }

    private static ItemStack cannonModel(int team) {
        ItemStack item = new ItemStack(Material.LEATHER_HORSE_ARMOR);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        if (meta != null) {
            meta.setColor(TEAM_COLORS[Math.max(0, Math.min(3, team))]);
            meta.setCustomModelData(CUSTOM_MODEL_DATA);
            item.setItemMeta(meta);
        }
        return item;
    }
}
