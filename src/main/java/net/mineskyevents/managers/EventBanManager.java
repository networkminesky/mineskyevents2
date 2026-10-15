package net.mineskyevents.managers;

import net.mineskyevents.MineSkyEvents;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventBanManager {

    private static final Pattern DURATION = Pattern.compile("(\\d+)([dhms])");

    private final File file;
    private YamlConfiguration yaml;

    public EventBanManager(MineSkyEvents plugin) {
        this.file = new File(plugin.getDataFolder(), "bans.yml");
        reload();
    }

    public void reload() {
        yaml = YamlConfiguration.loadConfiguration(file);
    }

    public ActiveBan check(UUID uuid, String name) {
        purge(uuidPath(uuid));
        ActiveBan byId = read(uuidPath(uuid));
        if (byId != null) return byId;

        if (name == null) return null;
        String namePath = namePath(name);
        purge(namePath);
        return read(namePath);
    }

    public void ban(UUID uuid, String name, String reason, String staff, long expiresAt) {
        write(uuidPath(uuid), name, reason, staff, expiresAt);
        if (name != null) {
            write(namePath(name), name, reason, staff, expiresAt);
        }
        save();
    }

    public void banByName(String name, String reason, String staff, long expiresAt) {
        write(namePath(name), name, reason, staff, expiresAt);
        save();
    }

    public static long parseDurationMillis(String raw) {
        if (raw == null || raw.isBlank()) return 0L;
        Matcher matcher = DURATION.matcher(raw.toLowerCase(Locale.ROOT).replace(" ", ""));
        long total = 0L;
        int matches = 0;
        while (matcher.find()) {
            long amount = Long.parseLong(matcher.group(1));
            total += switch (matcher.group(2)) {
                case "d" -> amount * 86_400_000L;
                case "h" -> amount * 3_600_000L;
                case "m" -> amount * 60_000L;
                case "s" -> amount * 1_000L;
                default -> 0L;
            };
            matches++;
        }
        return matches == 0 ? 0L : total;
    }

    public static String joinReason(String[] args, int from) {
        if (args.length <= from) return "Sem motivo";
        return String.join(" ", java.util.Arrays.copyOfRange(args, from, args.length));
    }

    private void write(String path, String name, String reason, String staff, long expiresAt) {
        yaml.set(path + ".name", name);
        yaml.set(path + ".reason", reason);
        yaml.set(path + ".staff", staff);
        yaml.set(path + ".expires", expiresAt);
    }

    private ActiveBan read(String path) {
        if (!yaml.contains(path + ".expires")) return null;
        long expires = yaml.getLong(path + ".expires", -1L);
        String reason = yaml.getString(path + ".reason", "Sem motivo");
        return new ActiveBan(reason, expires);
    }

    private void purge(String path) {
        if (!yaml.contains(path + ".expires")) return;
        long expires = yaml.getLong(path + ".expires", -1L);
        if (expires >= 0 && expires <= System.currentTimeMillis()) {
            yaml.set(path, null);
            save();
        }
    }

    private void save() {
        try {
            yaml.save(file);
        } catch (IOException e) {
            MineSkyEvents.l.warning("Falha ao salvar bans.yml: " + e.getMessage());
        }
    }

    private static String uuidPath(UUID uuid) {
        return "bans." + uuid;
    }

    private static String namePath(String name) {
        return "names." + name.toLowerCase(Locale.ROOT);
    }

    public record ActiveBan(String reason, long expiresAt) {
        public boolean permanent() {
            return expiresAt < 0;
        }

        public String remainingLabel() {
            if (permanent()) return "permanente";
            long ms = Math.max(0, expiresAt - System.currentTimeMillis());
            long minutes = ms / 60_000L;
            if (minutes >= 1440) return (minutes / 1440) + "d";
            if (minutes >= 60) return (minutes / 60) + "h";
            if (minutes >= 1) return minutes + "m";
            return Math.max(1, ms / 1000) + "s";
        }
    }
}
