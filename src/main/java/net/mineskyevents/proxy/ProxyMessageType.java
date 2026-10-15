package net.mineskyevents.proxy;

public enum ProxyMessageType {
    // Enviados pelo Velocity (MineSkyProxy) -> Paper (MineSkyEvents)
    START_EVENT,
    STOP_EVENT,
    FORCE_STOP,
    PING,

    // Enviados pelo Paper (MineSkyEvents) -> Velocity (MineSkyProxy)
    EVENT_STARTED,
    EVENT_ENDED,
    EVENT_CANCELLED,
    REWARD,
    EVENT_STATUS,
    PONG;

    public static ProxyMessageType fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
