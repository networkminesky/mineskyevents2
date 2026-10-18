package net.mineskyevents.chat;

/**
 * Contrato de mensagens entre MineSkyCore (sendGlobal) e MineSkyEvents (chat normal).
 */
public final class GlobalChatProtocol {

    public static final String CHANNEL = "minesky:chat";
    public static final String SUB_GLOBAL = "global";

    public static final String FIELD_SOURCE_SERVER = "sourceServer";
    /** Component Adventure em JSON (Gson) — preferido (suporta hover/click do Core). */
    public static final String FIELD_FULL_JSON = "fullJson";
    /** Fallback: linha em MiniMessage puro (servidor de eventos). */
    public static final String FIELD_FULL_MINI = "fullMini";

    private GlobalChatProtocol() {}
}
