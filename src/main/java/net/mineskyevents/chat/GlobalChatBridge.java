package net.mineskyevents.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minesky.api.messaging.Messaging;
import net.minesky.core.messaging.MessageType;
import net.minesky.core.messaging.MessagingListener;
import net.minesky.core.messaging.NetworkMessage;
import net.mineskyevents.MineSkyEvents;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

public final class GlobalChatBridge {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private final MineSkyEvents plugin;
    private final Logger logger;
    private MessagingListener listener;
    private boolean active;

    public GlobalChatBridge(MineSkyEvents plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void register() {
        if (listener != null || !isEnabled()) return;
        if (!Messaging.hasObserver()) {
            logger.warning("Bridge de chat global: Mainframe messaging indisponível.");
            return;
        }

        listener = new MessagingListener(GlobalChatProtocol.CHANNEL) {
            @Override
            public void onMessage(String subchannel, String value) { }

            @Override
            public void onMessage(NetworkMessage message) {
                if (!GlobalChatProtocol.SUB_GLOBAL.equals(message.subchannel())) return;
                if (message.type() != MessageType.DOCUMENT) return;
                Document doc = message.asDocument();
                runOnGlobalRegion(() -> deliverFromNetwork(doc));
            }
        };

        Messaging.registerListener("MineSkyEvents-GlobalChat", listener);
        active = true;
        logger.info("Bridge de chat global ativo (" + GlobalChatProtocol.CHANNEL + ").");
    }

    public void unregister() {
        if (listener != null) {
            Messaging.unregisterListener(listener);
            listener = null;
        }
        active = false;
    }

    public void sendAsGlobal(Player sender, String plainMessage) {
        if (!isEnabled() || plainMessage == null || plainMessage.isBlank()) return;

        Component line = formatEventGlobalLine(sender, plainMessage.trim());
        broadcastLocal(line);
        publishToNetwork(line);
    }

    private Component formatEventGlobalLine(Player sender, String text) {
        String template = plugin.getConfig().getString("chat.global-bridge.format",
                "<gray>[g]</gray> <aqua>[Eventos]</aqua> <gray><name></gray><dark_gray>: </dark_gray><message>");
        String mini = template
                .replace("<name>", MINI.escapeTags(sender.getName()))
                .replace("<message>", MINI.escapeTags(text));
        return MINI.deserialize(mini);
    }

    private void broadcastLocal(Component line) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(line);
        }
        logger.info("[CHAT] [GLOBAL] " + MINI.serialize(line));
    }

    private void publishToNetwork(Component line) {
        if (!active || !Messaging.hasObserver()) return;
        try {
            Document payload = GlobalChatCodec.encode(serverId(), line);
            Messaging.publish(GlobalChatProtocol.CHANNEL, GlobalChatProtocol.SUB_GLOBAL,
                    MessageType.DOCUMENT, payload);
        } catch (Exception e) {
            logger.warning("Falha ao publicar chat global: " + e.getMessage());
        }
    }

    private void deliverFromNetwork(Document doc) {
        if (doc == null) return;

        String source = doc.getString(GlobalChatProtocol.FIELD_SOURCE_SERVER);
        if (source != null && source.equalsIgnoreCase(serverId())) return;

        Component line = GlobalChatCodec.decode(doc, logger);
        if (line == null) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(line);
        }
    }

    private void runOnGlobalRegion(Runnable task) {
        Plugin p = plugin;
        Bukkit.getGlobalRegionScheduler().execute(p, task);
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("chat.global-bridge.enabled", true);
    }

    public String serverId() {
        return plugin.getConfig().getString("chat.global-bridge.server-id",
                plugin.getConfig().getString("proxy.server-name", "eventos"));
    }
}
