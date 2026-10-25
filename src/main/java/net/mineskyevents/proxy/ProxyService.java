package net.mineskyevents.proxy;

import net.minesky.api.messaging.Messaging;
import net.minesky.core.messaging.MessageType;
import net.minesky.core.messaging.MessagingListener;
import net.minesky.core.messaging.NetworkMessage;
import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.models.EventType;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

/**
 * Comunicação com o MineSkyProxy via Mainframe Messaging (MongoDB change streams).
 * Fallback para plugin messaging apenas se o Mainframe não estiver com messaging ativo.
 */
public class ProxyService {

    private final MineSkyEvents plugin;
    private final Logger logger;
    private MessagingListener mainframeListener;
    private boolean mainframeActive;

    public ProxyService(MineSkyEvents plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void register() {
        if (mainframeListener != null) {
            return;
        }
        if (Messaging.hasObserver()) {
            mainframeListener = new MessagingListener(ProxyChannel.MAIN_CHANNEL) {
                @Override
                public void onMessage(String subchannel, String value) {
                    handleLegacyString(subchannel, value);
                }

                @Override
                public void onMessage(NetworkMessage message) {
                    handleNetworkMessage(message);
                }
            };
            Messaging.registerListener("MineSkyEvents", mainframeListener);
            mainframeActive = true;
            logger.info("Proxy integrado via Mainframe (canal " + ProxyChannel.MAIN_CHANNEL + ")");
            return;
        }

        mainframeActive = false;
        logger.warning("Mainframe messaging indisponível — comandos do proxy (start/stop) não serão recebidos.");
    }

    public void unregister() {
        if (mainframeListener != null) {
            Messaging.unregisterListener(mainframeListener);
            mainframeListener = null;
        }
    }

    private void handleNetworkMessage(NetworkMessage message) {
        String sub = message.subchannel();
        if (sub == null) return;

        switch (sub) {
            case ProxyChannel.SUB_START_EVENT -> handleStartEvent(message);
            case ProxyChannel.SUB_STOP_EVENT -> Bukkit.getScheduler().runTask(plugin, () -> plugin.getEventManager().forceStop());
            case ProxyChannel.SUB_FORCE_STOP -> Bukkit.getScheduler().runTask(plugin, () -> plugin.getEventManager().forceStop());
            case ProxyChannel.SUB_PING -> sendPong();
            default -> logger.fine("Subcanal Mainframe ignorado: " + sub);
        }
    }

    private void handleLegacyString(String subchannel, String value) {
        switch (subchannel) {
            case ProxyChannel.SUB_STOP_EVENT, ProxyChannel.SUB_FORCE_STOP ->
                    Bukkit.getScheduler().runTask(plugin, () -> plugin.getEventManager().forceStop());
            case ProxyChannel.SUB_PING -> sendPong();
            default -> { }
        }
    }

    private void handleStartEvent(NetworkMessage message) {
        String eventId;
        String mapId;
        boolean manual;

        if (message.type() == MessageType.DOCUMENT) {
            Document doc = message.asDocument();
            eventId = doc.getString("eventId");
            mapId = doc.getString("mapId");
            manual = doc.getBoolean("manual", false);
        } else {
            eventId = message.asString();
            mapId = "";
            manual = false;
        }

        if (eventId == null || eventId.isBlank()) {
            logger.warning("START_EVENT sem eventId válido.");
            return;
        }

        EventType type = EventType.fromId(eventId);
        if (type == null) {
            logger.warning("Tipo de evento desconhecido recebido do proxy: " + eventId);
            return;
        }

        String resolvedMap = mapId != null ? mapId : "";
        Bukkit.getScheduler().runTask(plugin, () ->
                plugin.getEventManager().startEvent(type, resolvedMap.isBlank() ? null : resolvedMap, manual));
    }

    public void sendEventStarted(String eventId, String mapId, int playerCount) {
        publish(ProxyChannel.SUB_EVENT_STARTED, MessageType.DOCUMENT, new Document()
                .append("eventId", eventId)
                .append("mapId", mapId != null ? mapId : "")
                .append("playerCount", playerCount));
    }

    public void sendEventEnded(String eventId, String mapId) {
        publish(ProxyChannel.SUB_EVENT_ENDED, MessageType.DOCUMENT, new Document()
                .append("eventId", eventId)
                .append("mapId", mapId != null ? mapId : ""));
    }

    public void sendEventCancelled(String eventId, String reason) {
        publish(ProxyChannel.SUB_EVENT_CANCELLED, MessageType.DOCUMENT, new Document()
                .append("eventId", eventId)
                .append("reason", reason != null ? reason : "unknown"));
    }

    public void sendReward(String playerName, int amount, String eventId, int placement) {
        publish(ProxyChannel.SUB_REWARD, MessageType.DOCUMENT, new Document()
                .append("playerName", playerName)
                .append("amount", amount)
                .append("eventId", eventId)
                .append("placement", placement));
        logger.info("Recompensa publicada no Mainframe: " + playerName + " +$" + amount + " (" + eventId + ")");
    }

    public void sendEventStatus(String eventId, String state, int players) {
        publish(ProxyChannel.SUB_EVENT_STATUS, MessageType.DOCUMENT, new Document()
                .append("eventId", eventId)
                .append("state", state)
                .append("players", players));
    }

    public void sendPlayerToHub(Player player) {
        connectPlayerToServer(player, plugin.getConfig().getString("proxy.hub-server", "lobby"));
    }

    public void sendPlayerToSurvival(Player player) {
        String survival = plugin.getConfig().getString("proxy.survival-server", "survival");
        connectPlayerToServer(player, survival);
    }

    private void connectPlayerToServer(Player player, String targetServer) {
        if (targetServer == null || targetServer.isBlank()) {
            logger.warning("Servidor de destino não configurado para " + player.getName());
            return;
        }
        publish(ProxyChannel.SUB_CONNECT_HUB, MessageType.DOCUMENT, new Document()
                .append("playerName", player.getName())
                .append("targetServerName", targetServer));
        logger.fine("CONNECT_HUB publicado para " + player.getName() + " -> " + targetServer);
    }

    private void sendPong() {
        String serverName = plugin.getConfig().getString("proxy.server-name", plugin.getServer().getName());
        publish(ProxyChannel.SUB_PONG, MessageType.STRING, serverName);
    }

    private void publish(String subchannel, MessageType type, Object payload) {
        if (!mainframeActive) {
            logger.warning("Mainframe inativo — mensagem não enviada: " + subchannel);
            return;
        }
        try {
            Messaging.publish(ProxyChannel.MAIN_CHANNEL, subchannel, type, payload);
        } catch (Exception e) {
            logger.severe("Falha ao publicar no Mainframe (" + subchannel + "): " + e.getMessage());
        }
    }
}
