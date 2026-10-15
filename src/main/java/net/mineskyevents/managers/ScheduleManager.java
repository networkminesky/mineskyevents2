package net.mineskyevents.managers;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.map.MapManager;
import net.mineskyevents.models.EventState;
import net.mineskyevents.models.EventType;
import net.mineskyevents.utils.Scheduler;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

public class ScheduleManager {

    private final MineSkyEvents plugin;
    private final EventManager eventManager;
    private final ZoneId timezone;
    private final boolean weekendEnabled;
    private final Set<String> triggeredSlots = new HashSet<>();
    private Scheduler.Task tickTask;

    public ScheduleManager(MineSkyEvents plugin, EventManager eventManager) {
        this.plugin = plugin;
        this.eventManager = eventManager;
        FileConfiguration config = plugin.getConfig();
        this.timezone = ZoneId.of(config.getString("timezone", "America/Sao_Paulo"));
        this.weekendEnabled = config.getBoolean("weekend-schedule-enabled", false);
    }

    public void start() {
        tickTask = Scheduler.runTaskTimer(this::tick, 100L, 1200L); // verifica a cada minuto
    }

    public void stop() {
        if (tickTask != null) tickTask.cancel();
    }

    private void tick() {
        if (eventManager.getState() != EventState.INACTIVE) return;

        LocalDateTime now = LocalDateTime.now(timezone);
        DayOfWeek day = now.getDayOfWeek();
        int hour = now.getHour();
        int minute = now.getMinute();

        if (minute > 5) return; // só dispara nos primeiros 5 minutos da hora

        boolean isWeekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
        if (isWeekend && !weekendEnabled) return;

        String slotKey = now.toLocalDate() + "-" + hour;
        if (triggeredSlots.contains(slotKey)) return;

        for (EventType type : EventType.values()) {
            if (!type.isScheduledFor(day, hour)) continue;

            String mapId = MapManager.getRandomMapId(type);
            if (mapId == null) {
                plugin.getLogger().warning("Nenhum mapa configurado para " + type.getId() + " — evento agendado ignorado.");
                continue;
            }

            triggeredSlots.add(slotKey);
            eventManager.startEvent(type, mapId, false);
            plugin.getLogger().info("Evento agendado iniciado: " + type.getDisplayName() + " no mapa " + mapId);
            break; // um evento por hora
        }
    }
}
