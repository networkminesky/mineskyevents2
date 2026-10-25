package net.mineskyevents.miniwars;

import net.mineskyevents.entities.events.MiniWarsEvent;
import net.mineskyevents.map.MapManager;
import net.mineskyevents.models.EventState;
import net.mineskyevents.utils.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MiniWarsCannonManager {

    private final MiniWarsEvent event;
    private final List<TeamCannon> cannons = new ArrayList<>();
    private Scheduler.Task tickTask;

    public MiniWarsCannonManager(MiniWarsEvent event) {
        this.event = event;
    }

    public void spawnAll() {
        despawnAll();
        YamlConfiguration config = event.getCategory().getConfig();
        String mapId = event.getMapId();
        for (int team = 0; team < 4; team++) {
            Location loc = MapManager.loadCannonLocation(config, mapId, team);
            if (loc != null) {
                cannons.add(new TeamCannon(team, loc, event));
            }
        }
        tickTask = Scheduler.runTaskTimer(this::tick, 1L, 1L);
    }

    private void tick() {
        if (event.getEventManager().getState() != EventState.RUNNING) return;
        for (TeamCannon cannon : cannons) {
            cannon.tick();
        }
    }

    public TeamCannon findCannonBySeat(org.bukkit.entity.Entity seat) {
        for (TeamCannon cannon : cannons) {
            if (cannon.getSeat() != null && cannon.getSeat().getUniqueId().equals(seat.getUniqueId())) {
                return cannon;
            }
        }
        return null;
    }

    public TeamCannon findCannonByEntity(org.bukkit.entity.Entity entity) {
        if (entity == null) return null;
        for (TeamCannon cannon : cannons) {
            if (cannon.matchesEntity(entity)) {
                return cannon;
            }
        }
        return null;
    }

    /**
     * ItemDisplay/armor stands often não registram colisão de snowball — detecta por proximidade ao modelo.
     */
    public TeamCannon findCannonNear(org.bukkit.Location impact, double radius) {
        if (impact == null || impact.getWorld() == null) return null;
        TeamCannon closest = null;
        double bestDistSq = radius * radius;
        for (TeamCannon cannon : cannons) {
            org.bukkit.Location center = cannon.getImpactCenter();
            if (center.getWorld() != impact.getWorld()) continue;
            double distSq = center.distanceSquared(impact);
            if (distSq <= bestDistSq) {
                bestDistSq = distSq;
                closest = cannon;
            }
        }
        return closest;
    }

    public TeamCannon findCannonRiddenBy(Player player) {
        if (!player.isInsideVehicle()) return null;
        return findCannonBySeat(player.getVehicle());
    }

    /** Canhão montado e operacional (não desabilitado). */
    public TeamCannon findOperationalCannonRiddenBy(Player player) {
        TeamCannon cannon = findCannonRiddenBy(player);
        if (cannon == null || cannon.isDisabled()) return null;
        return cannon;
    }

    public void despawnAll() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
        for (TeamCannon cannon : cannons) {
            cannon.remove();
        }
        cannons.clear();
    }

    public List<TeamCannon> getCannons() {
        return List.copyOf(cannons);
    }
}
