package net.mineskyevents.map;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.entities.categories.CategoryHandler;
import net.mineskyevents.models.EventType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MapManager {

    private MapManager() {}

    public static void createMap(EventType type, String mapId, String displayName) {
        Category category = CategoryHandler.getCategoryById(type.getId());
        if (category == null) return;

        YamlConfiguration config = category.getConfig();
        String path = "mapas." + mapId;
        config.set(path + ".name", displayName);
        config.set(path + ".id", mapId);
        saveCategory(category);
    }

    public static void savePoint(String categoryId, String mapId, String point, Location loc) {
        Category category = CategoryHandler.getCategoryById(categoryId);
        if (category == null || loc == null) return;

        String path = "mapas." + mapId + "." + point;
        YamlConfiguration config = category.getConfig();
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", loc.getYaw());
        config.set(path + ".pitch", loc.getPitch());
        saveCategory(category);
    }

    public static void saveCheckpoints(String categoryId, String mapId, List<Location> checkpoints) {
        Category category = CategoryHandler.getCategoryById(categoryId);
        if (category == null) return;

        YamlConfiguration config = category.getConfig();
        List<String> serialized = new ArrayList<>();
        for (Location loc : checkpoints) {
            serialized.add(serializeLocation(loc));
        }
        config.set("mapas." + mapId + ".checkpoints", serialized);
        saveCategory(category);
    }

    public static World resolveWorld(String configuredName) {
        if (configuredName != null && !configuredName.isBlank()) {
            World world = Bukkit.getWorld(configuredName);
            if (world != null) return world;
        }
        var worlds = Bukkit.getWorlds();
        if (worlds.size() == 1) {
            return worlds.get(0);
        }
        if (configuredName != null && !configuredName.isBlank()) {
            MineSkyEvents.l.warning("Mundo '" + configuredName + "' não está carregado.");
        }
        return null;
    }

    public static Location loadLocation(YamlConfiguration config, String path) {
        if (!config.contains(path)) return null;
        World world = resolveWorld(config.getString(path + ".world", "world"));
        if (world == null) return null;
        return new Location(world,
                config.getDouble(path + ".x"),
                config.getDouble(path + ".y"),
                config.getDouble(path + ".z"),
                (float) config.getDouble(path + ".yaw"),
                (float) config.getDouble(path + ".pitch"));
    }

    public static List<Location> loadCheckpoints(YamlConfiguration config, String mapId) {
        return loadLocationList(config, mapId, "checkpoints");
    }

    public static List<Location> loadArcos(YamlConfiguration config, String mapId) {
        return loadLocationList(config, mapId, "arcos");
    }

    public static void appendLocation(String categoryId, String mapId, String listKey, Location loc) {
        Category category = CategoryHandler.getCategoryById(categoryId);
        if (category == null || loc == null) return;

        YamlConfiguration config = category.getConfig();
        String path = "mapas." + mapId + "." + listKey;
        List<String> serialized = new ArrayList<>(config.getStringList(path));
        serialized.add(serializeLocation(loc));
        config.set(path, serialized);
        saveCategory(category);
    }

    public static void clearLocationList(String categoryId, String mapId, String listKey) {
        Category category = CategoryHandler.getCategoryById(categoryId);
        if (category == null) return;
        category.getConfig().set("mapas." + mapId + "." + listKey, new ArrayList<>());
        saveCategory(category);
    }

    public static List<Location> loadLocationList(YamlConfiguration config, String mapId, String listKey) {
        List<Location> result = new ArrayList<>();
        List<String> raw = config.getStringList("mapas." + mapId + "." + listKey);
        for (String entry : raw) {
            Location loc = deserializeLocation(entry);
            if (loc != null) result.add(loc);
        }
        return result;
    }

    public static Location loadTeamSpawn(YamlConfiguration config, String mapId, int teamIndex) {
        return loadLocation(config, "mapas." + mapId + ".spawn-time-" + teamIndex);
    }

    public static Location loadCannonLocation(YamlConfiguration config, String mapId, int teamIndex) {
        return loadLocation(config, "mapas." + mapId + ".cannon-" + teamIndex);
    }

    public static Category findCategoryForMap(String mapId) {
        for (Category category : CategoryHandler.categories) {
            if (category.getConfig().contains("mapas." + mapId)) {
                return category;
            }
        }
        return null;
    }

    public static String getMapDisplayName(Category category, String mapId) {
        return category.getConfig().getString("mapas." + mapId + ".name", mapId);
    }

    public static String getRandomMapId(EventType type) {
        Category category = CategoryHandler.getCategoryById(type.getId());
        if (category == null) return null;

        var keys = category.getConfig().getConfigurationSection("mapas");
        if (keys == null || keys.getKeys(false).isEmpty()) return null;

        List<String> mapIds = new ArrayList<>(keys.getKeys(false));
        return mapIds.get((int) (Math.random() * mapIds.size()));
    }

    private static String serializeLocation(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ()
                + ":" + loc.getYaw() + ":" + loc.getPitch();
    }

    private static Location deserializeLocation(String raw) {
        String[] parts = raw.split(":");
        if (parts.length < 4) return null;
        World world = resolveWorld(parts[0]);
        if (world == null) return null;
        float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0;
        float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0;
        return new Location(world,
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]),
                yaw, pitch);
    }

    private static void saveCategory(Category category) {
        try {
            category.getConfig().save(category.getFile());
        } catch (IOException e) {
            MineSkyEvents.l.warning("Erro ao salvar mapa: " + e.getMessage());
        }
    }
}
