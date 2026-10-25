package net.mineskyevents.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.managers.EventManager;
import net.mineskyevents.models.EventState;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Messages {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static FileConfiguration config;

    private Messages() {}

    public static void init(FileConfiguration messagesConfig) {
        config = messagesConfig;
    }

    public static Component parse(String template, TagResolver... resolvers) {
        String prefix = config.getString("prefix", "");
        String withPrefix = template.replace("{prefix}", prefix);
        String miniMessage = withPrefix.replaceAll("\\{([a-zA-Z0-9_-]+)\\}", "<$1>");
        return MINI.deserialize(miniMessage, resolvers);
    }

    public static void send(CommandSender sender, String key, TagResolver... resolvers) {
        String template = config.getString(key, key);
        sender.sendMessage(parse(template, resolvers));
    }

    public static void broadcast(String key, TagResolver... resolvers) {
        Component message = parse(config.getString(key, key), resolvers);
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(message));
    }

    public static void broadcastEvent(String key, Map<String, String> placeholders) {
        broadcastEventAudience(key, placeholders);
    }

    /** Anúncios de vitória etc. — inclui espectadores e fase de encerramento. */
    public static void broadcastEventAudience(String key, Map<String, String> placeholders) {
        TagResolver.Builder builder = TagResolver.builder();
        placeholders.forEach((k, v) -> builder.resolver(Placeholder.unparsed(k, v)));
        Component message = parse(config.getString(key, key), builder.build());
        sendToEventAudience(message);
    }

    public static void broadcastComponentToEventAudience(Component message) {
        sendToEventAudience(message);
    }

    private static void sendToEventAudience(Component message) {
        EventManager manager = MineSkyEvents.getInstance().getEventManager();
        Set<UUID> sent = new HashSet<>();
        for (UUID uuid : manager.getSpectators()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendMessage(message);
                sent.add(uuid);
            }
        }
        for (UUID uuid : manager.getActivePlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && sent.add(uuid)) {
                player.sendMessage(message);
            }
        }
        EventState state = manager.getState();
        if (state == EventState.ENDING || state == EventState.RUNNING) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (sent.add(player.getUniqueId())) {
                    player.sendMessage(message);
                }
            }
        }
    }

    public static void title(Player player, String title, String subtitle) {
        player.showTitle(Title.title(
                MINI.deserialize(title),
                MINI.deserialize(subtitle),
                Title.Times.times(Duration.ofMillis(250), Duration.ofMillis(2000), Duration.ofMillis(500))
        ));
    }

    public static void playSound(Player player, Sound sound, float pitch) {
        player.playSound(player.getLocation(), sound, 1.0f, pitch);
    }

    public static void playToEventPlayers(Sound sound, float pitch) {
        MineSkyEvents.getInstance().getEventManager().getActivePlayers().forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) playSound(p, sound, pitch);
        });
    }

    /** Mensagem com códigos § (ex.: cores de time). */
    public static void broadcastLegacy(String legacyMessage) {
        Component message = LEGACY.deserialize(legacyMessage);
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(message));
    }

    public static void playToEventAudience(Sound sound, float pitch) {
        var manager = MineSkyEvents.getInstance().getEventManager();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (manager.isPlaying(player) || manager.isSpectating(player)) {
                playSound(player, sound, pitch);
            }
        }
    }
}
