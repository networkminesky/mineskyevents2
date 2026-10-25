package net.mineskyevents.utils;

import net.mineskyevents.entities.events.BaseEvent;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.concurrent.ThreadLocalRandom;

public final class EventCelebration {

    private static final int FIREWORK_COUNT = 12;
    private static final int FINALE_FIREWORK_COUNT = 28;

    private EventCelebration() {}

    public static void spawnFireworks(BaseEvent event, Color primary, Color secondary) {
        spawnFireworks(event, primary, secondary, FIREWORK_COUNT);
    }

    public static void spawnFinaleFireworks(BaseEvent event, Color primary, Color secondary) {
        spawnFireworks(event, primary, secondary, FINALE_FIREWORK_COUNT);
    }

    public static void spawnFireworks(BaseEvent event, Color primary, Color secondary, int count) {
        World world = event.getCelebrationWorld();
        if (world == null) return;

        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < count; i++) {
            long delay = i * 5L;
            Scheduler.runTaskLater(() -> {
                Location loc = event.randomCelebrationLocation(random);
                if (loc == null) return;
                spawnFirework(world, loc, primary, secondary);
            }, delay);
        }
    }

    public static void spawnDeathFirework(Location loc, Color primary, Color secondary) {
        if (loc == null || loc.getWorld() == null) return;
        Location at = loc.clone().add(0, 0.35, 0);
        spawnFirework(loc.getWorld(), at, primary, secondary);
    }

    private static void spawnFirework(World world, Location loc, Color primary, Color secondary) {
        Firework firework = world.spawn(loc, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder()
                .withColor(primary, secondary)
                .withFade(primary)
                .with(FireworkEffect.Type.BALL_LARGE)
                .flicker(true)
                .trail(true)
                .build());
        meta.setPower(0);
        firework.setFireworkMeta(meta);
    }
}
