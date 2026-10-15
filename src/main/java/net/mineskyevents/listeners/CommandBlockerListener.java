package net.mineskyevents.listeners;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.utils.Messages;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CommandBlockerListener implements Listener {

    private Set<String> blocked = new HashSet<>();

    public void reload() {
        List<String> fromConfig = MineSkyEvents.getInstance().getConfig().getStringList("blocked-commands");
        blocked = new HashSet<>();
        for (String cmd : fromConfig) {
            blocked.add(cmd.toLowerCase(Locale.ROOT));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("mineskyevents.admin")) {
            return;
        }

        String label = extractCommandLabel(event.getMessage());
        if (label.isEmpty()) return;

        if (!blocked.contains(label)) {
            return;
        }

        event.setCancelled(true);
        Messages.send(player, "errors.command-blocked");
        Messages.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f);
    }

    /**
     * Suporta {@code /pl}, {@code /bukkit:plugins}, {@code /?} e argumentos ({@code /pl reload}).
     */
    static String extractCommandLabel(String message) {
        if (message == null || message.isBlank()) return "";
        String raw = message.trim();
        if (raw.startsWith("/")) {
            raw = raw.substring(1);
        }
        int space = raw.indexOf(' ');
        String head = space >= 0 ? raw.substring(0, space) : raw;
        String[] namespaced = head.split(":");
        return namespaced[namespaced.length - 1].toLowerCase(Locale.ROOT);
    }
}
