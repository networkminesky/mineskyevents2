package net.mineskyevents.entities.events;

import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.map.MapManager;
import net.mineskyevents.models.EventType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class CorridaBarcoEvent extends BaseEvent {

    private final List<Player> finishOrder = new ArrayList<>();
    private final Map<UUID, UUID> playerBoats = new HashMap<>();
    private Location finishLine;

    public CorridaBarcoEvent(EventType type, Category category, String mapId) {
        super(type, category, mapId);
    }

    @Override
    protected void onLateJoin(Player player) {
        super.onLateJoin(player);
        spawnInBoat(player);
    }

    @Override
    protected void onGameStart() {
        finishLine = category.getConfig().contains("mapas." + mapId + ".finish")
                ? MapManager.loadLocation(category.getConfig(), "mapas." + mapId + ".finish")
                : pos2Loc;

        for (UUID uuid : alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                giveKit(p);
                spawnInBoat(p);
            }
        }
    }

    public void spawnInBoat(Player player) {
        Location base = player.getLocation().clone();
        World world = base.getWorld();
        if (world == null) return;

        removePlayerBoat(player);

        Location boatLoc = base.clone().add(0, 0.1, 0);
        Boat boat = world.spawn(boatLoc, Boat.class, spawned -> {
            spawned.setMaxSpeed(0.85);
            spawned.setOccupiedDeceleration(0.02);
            spawned.setUnoccupiedDeceleration(0.02);
        });
        boat.addPassenger(player);
        playerBoats.put(player.getUniqueId(), boat.getUniqueId());
    }

    public void removePlayerBoat(Player player) {
        UUID boatId = playerBoats.remove(player.getUniqueId());
        if (boatId == null) return;
        for (World world : Bukkit.getWorlds()) {
            Entity entity = world.getEntity(boatId);
            if (entity != null) {
                entity.remove();
                return;
            }
        }
    }

    public boolean isEventBoat(Entity entity) {
        if (!(entity instanceof Boat)) return false;
        return playerBoats.containsValue(entity.getUniqueId());
    }

    public void tryBoatJump(Boat boat) {
        if (!playerBoats.containsValue(boat.getUniqueId())) return;
        if (boat.getPassengers().isEmpty()) return;

        Vector velocity = boat.getVelocity();
        if (velocity.lengthSquared() < 0.01) return;

        Location loc = boat.getLocation();
        Vector direction = velocity.clone().normalize();
        Location ahead = loc.clone().add(direction.multiply(0.9));
        Material feet = ahead.getBlock().getType();
        Material head = ahead.clone().add(0, 1, 0).getBlock().getType();

        if (feet.isSolid() && !head.isSolid()) {
            boat.setVelocity(velocity.add(new Vector(0, 0.42, 0)));
        } else if (feet.isSolid() && head.isSolid()) {
            boat.setVelocity(velocity.add(new Vector(0, 0.55, 0)));
        }
    }

    public void onFinish(Player player) {
        if (finishOrder.contains(player)) return;
        finishOrder.add(player);

        if (finishOrder.size() >= 3 || finishOrder.size() >= alivePlayers.size()) {
            finishEvent(finishOrder.subList(0, Math.min(3, finishOrder.size())));
        }
    }

    public boolean isNearFinish(Player player) {
        if (finishLine == null) return false;
        return player.getLocation().distanceSquared(finishLine) < 9;
    }

    @Override
    protected void onPlayerEliminated(Player player) {
        removePlayerBoat(player);
    }

    @Override
    protected void onCleanUp() {
        finishOrder.clear();
        for (UUID uuid : new ArrayList<>(playerBoats.keySet())) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) removePlayerBoat(p);
        }
        playerBoats.clear();
    }
}
