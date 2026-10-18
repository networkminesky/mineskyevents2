package net.mineskyevents.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bson.Document;

import java.util.logging.Logger;

public final class GlobalChatCodec {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final GsonComponentSerializer GSON = GsonComponentSerializer.gson();

    private GlobalChatCodec() {}

    public static Document encode(String sourceServer, Component line) {
        return new Document()
                .append(GlobalChatProtocol.FIELD_SOURCE_SERVER, sourceServer)
                .append(GlobalChatProtocol.FIELD_FULL_JSON, GSON.serialize(line))
                .append(GlobalChatProtocol.FIELD_FULL_MINI, MINI.serialize(line));
    }

    public static Component decode(Document doc, Logger logger) {
        if (doc == null) return null;

        String json = doc.getString(GlobalChatProtocol.FIELD_FULL_JSON);
        if (json != null && !json.isBlank()) {
            try {
                return GSON.deserialize(json);
            } catch (Exception e) {
                if (logger != null) {
                    logger.fine("Chat bridge: falha ao ler fullJson: " + e.getMessage());
                }
            }
        }

        String mini = doc.getString(GlobalChatProtocol.FIELD_FULL_MINI);
        if (mini == null || mini.isBlank()) return null;

        try {
            return MINI.deserialize(mini);
        } catch (Exception e) {
            if (logger != null) {
                logger.warning("Chat bridge: MiniMessage inválido, mensagem ignorada: " + e.getMessage());
            }
            return null;
        }
    }
}
