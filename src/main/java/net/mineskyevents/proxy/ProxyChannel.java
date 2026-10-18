package net.mineskyevents.proxy;

public final class ProxyChannel {

    public static final String MAIN_CHANNEL = "minesky:network";

    // Velocity -> Paper (MineSkyEvents)
    public static final String SUB_START_EVENT = "start-event";
    public static final String SUB_STOP_EVENT = "stop-event";
    public static final String SUB_FORCE_STOP = "force-stop";
    public static final String SUB_PING = "ping";

    // Paper -> Velocity
    public static final String SUB_EVENT_STARTED = "event-started";
    public static final String SUB_EVENT_ENDED = "event-ended";
    public static final String SUB_EVENT_CANCELLED = "event-cancelled";
    public static final String SUB_REWARD = "event-reward";
    public static final String SUB_EVENT_STATUS = "event-status";
    public static final String SUB_PONG = "pong";
    public static final String SUB_CONNECT_HUB = "connect-hub";

    private ProxyChannel() {}
}
