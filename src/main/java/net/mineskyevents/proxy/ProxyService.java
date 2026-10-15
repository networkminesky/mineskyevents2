package net.mineskyevents.proxy;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.models.EventType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.logging.Logger;

/**
 * Canal de comunicação Plugin Messaging entre MineSkyEvents (Paper) e MineSkyProxy (Velocity).
 * Veja PROXY_INTEGRATION.md para detalhes do protocolo.
 */
public class ProxyService implements PluginMessageListener {

    public static final String CHANNEL = "minesky:proxy";

    private final MineSkyEvents plugin;
    private final Logger logger;

    public ProxyService(MineSkyEvents plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void register() {
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, CHANNEL, this);
        logger.info("Canal de proxy registrado: " + CHANNEL);
    }

    public void unregister() {
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, CHANNEL);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, CHANNEL, this);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!CHANNEL.equals(channel)) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String typeStr = in.readUTF();
        ProxyMessageType type = ProxyMessageType.fromString(typeStr);
        if (type == null) {
            logger.warning("Mensagem de proxy desconhecida: " + typeStr);
            return;
        }

        switch (type) {
            case START_EVENT -> handleStartEvent(in);
            case STOP_EVENT, FORCE_STOP -> plugin.getEventManager().forceStop();
            case PING -> sendPong();
            default -> logger.fine("Mensagem de proxy ignorada: " + type);
        }
    }

    private void handleStartEvent(ByteArrayDataInput in) {
        String eventId = in.readUTF();
        String mapId = in.readUTF();
        boolean manual = in.readBoolean();

        EventType type = EventType.fromId(eventId);
        if (type == null) {
            logger.warning("Tipo de evento desconhecido recebido do proxy: " + eventId);
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () ->
                plugin.getEventManager().startEvent(type, mapId, manual));
    }

    public void sendEventStarted(String eventId, String mapId, int playerCount) {
        send(ProxyMessageType.EVENT_STARTED, out -> {
            out.writeUTF(eventId);
            out.writeUTF(mapId);
            out.writeInt(playerCount);
        });
    }

    public void sendEventEnded(String eventId, String mapId) {
        send(ProxyMessageType.EVENT_ENDED, out -> {
            out.writeUTF(eventId);
            out.writeUTF(mapId);
        });
    }

    public void sendEventCancelled(String eventId, String reason) {
        send(ProxyMessageType.EVENT_CANCELLED, out -> {
            out.writeUTF(eventId);
            out.writeUTF(reason);
        });
    }

    public void sendReward(String playerName, int amount, String eventId, int placement) {
        send(ProxyMessageType.REWARD, out -> {
            out.writeUTF(playerName);
            out.writeInt(amount);
            out.writeUTF(eventId);
            out.writeInt(placement);
        });
        logger.info("Recompensa enviada ao proxy: " + playerName + " +$" + amount + " (" + eventId + ")");
    }

    public void sendEventStatus(String eventId, String state, int players) {
        send(ProxyMessageType.EVENT_STATUS, out -> {
            out.writeUTF(eventId);
            out.writeUTF(state);
            out.writeInt(players);
        });
    }

    private void sendPong() {
        send(ProxyMessageType.PONG, out -> out.writeUTF(plugin.getServer().getName()));
    }

    private void send(ProxyMessageType type, MessageWriter writer) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(type.name());
        writer.write(out);

        Player carrier = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
        if (carrier != null) {
            carrier.sendPluginMessage(plugin, CHANNEL, out.toByteArray());
        } else {
            logger.warning("Nenhum jogador online para enviar mensagem ao proxy: " + type);
        }
    }

    @FunctionalInterface
    private interface MessageWriter {
        void write(ByteArrayDataOutput out);
    }
}
