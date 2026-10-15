package net.mineskyevents.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import net.mineskyevents.MineSkyEvents;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Map;

public final class Messages {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
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
        TagResolver.Builder builder = TagResolver.builder();
        placeholders.forEach((k, v) -> builder.resolver(Placeholder.parsed(k, v)));
        broadcast(key, builder.build());
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
}
