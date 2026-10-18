package net.mineskyevents.managers;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.events.BaseEvent;
import org.bukkit.Location;
import org.bukkit.World;

public final class SpectatorBounds {

    private SpectatorBounds() {}

    public static double margin() {
        return MineSkyEvents.getInstance().getConfig().getDouble("spectator.boundary-margin", 8.0);
    }

    public static Location clamp(BaseEvent event, Location loc) {
        if (loc == null || event == null) return loc;

        Location pos1 = event.getPos1();
        Location pos2 = event.getPos2();
        if (pos1 == null || pos2 == null || pos1.getWorld() == null) {
            return loc;
        }

        World world = pos1.getWorld();
        if (!world.equals(loc.getWorld())) {
            return event.getSpectatorViewLocation();
        }

        double m = margin();
        double minX = Math.min(pos1.getX(), pos2.getX()) - m;
        double maxX = Math.max(pos1.getX(), pos2.getX()) + m;
        double minY = Math.min(pos1.getY(), pos2.getY()) - m;
        double maxY = Math.max(pos1.getY(), pos2.getY()) + m + 40;
        double minZ = Math.min(pos1.getZ(), pos2.getZ()) - m;
        double maxZ = Math.max(pos1.getZ(), pos2.getZ()) + m;

        double x = clamp(loc.getX(), minX, maxX);
        double y = clamp(loc.getY(), minY, maxY);
        double z = clamp(loc.getZ(), minZ, maxZ);

        if (x == loc.getX() && y == loc.getY() && z == loc.getZ()) {
            return loc;
        }

        Location clamped = loc.clone();
        clamped.setX(x);
        clamped.setY(y);
        clamped.setZ(z);
        return clamped;
    }

    public static boolean isOutside(BaseEvent event, Location loc) {
        Location clamped = clamp(event, loc);
        return clamped != null && (clamped.getX() != loc.getX()
                || clamped.getY() != loc.getY()
                || clamped.getZ() != loc.getZ());
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
