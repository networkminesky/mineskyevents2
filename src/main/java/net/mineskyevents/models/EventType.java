package net.mineskyevents.models;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

public enum EventType {

    SPLEEF("spleef", "Spleef", EventCategory.DAILY,
            "Quebre a neve abaixo dos adversários e seja o último sobrevivente!",
            2000, 3500, List.of(13, 14, 16, 17, 19)),

    TIJOLAO_WARS("tijolao-wars", "Tijolão Wars", EventCategory.DAILY,
            "Arremesse tijolos nos adversários e elimine todos os oponentes!",
            1500, 2500, List.of(13, 14, 16, 17, 19)),

    RUNNERS("runners", "Runners", EventCategory.DAILY,
            "Encontre a saída do labirinto antes dos outros jogadores!",
            1500, 2500, List.of(13, 14, 16, 17, 19)),

    CORRIDA("corrida", "Corrida", EventCategory.DAILY,
            "Corra até a linha de chegada! Os 3 primeiros colocados ganham prêmios.",
            2500, 4000, List.of(13, 14, 16, 17, 19)),

    CORRIDA_BARCO("corrida-barco", "Corrida de Barco", EventCategory.DAILY,
            "Navegue com seu barco no gelo e chegue primeiro ao destino!",
            1000, 2000, List.of(13, 14, 16, 17, 19)),

    TNT_RUN("tnt-run", "TNT RUN", EventCategory.DAILY,
            "Corra enquanto o chão de TNT desaparece sob seus pés!",
            1500, 2500, List.of(13, 14, 16, 17, 19)),

    TNT_TAG("tnt-tag", "TNT TAG", EventCategory.DAILY,
            "Passe a TNT para outro jogador antes que ela exploda!",
            1500, 2500, List.of(13, 14, 16, 17, 19)),

    SUMO("sumo", "Sumo", EventCategory.DAILY,
            "Empurre seus adversários da plataforma de gelo!",
            1500, 2500, List.of(13, 14, 16, 17, 19)),

    CORRIDA_ELYTRA("corrida-elytra", "Corrida de Elytra", EventCategory.DAILY,
            "Voe com sua elytra passando por todos os arcos do percurso!",
            1500, 2500, List.of(13, 14, 16, 17, 19)),

    MINI_WARS("mini-wars", "Mini Wars", EventCategory.SCHEDULED,
            "4 times se enfrentam com espadas e bestas. Defenda sua cor!",
            15000, 30000, List.of(13, 15, 18, 20)),

    ESCONDE_ESCONDE("esconde-esconde", "Esconde-Esconde", EventCategory.SCHEDULED,
            "Esconda-se dos caçadores ou encontre todos os escondidos!",
            10000, 25000, List.of(13, 15, 18, 20));

    private static final Set<DayOfWeek> WEEKDAYS = Set.of(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
    );

    private final String id;
    private final String displayName;
    private final EventCategory category;
    private final String description;
    private final int minReward;
    private final int maxReward;
    private final List<Integer> scheduleHours;

    EventType(String id, String displayName, EventCategory category, String description,
              int minReward, int maxReward, List<Integer> scheduleHours) {
        this.id = id;
        this.displayName = displayName;
        this.category = category;
        this.description = description;
        this.minReward = minReward;
        this.maxReward = maxReward;
        this.scheduleHours = scheduleHours;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public EventCategory getCategory() { return category; }
    public String getDescription() { return description; }
    public int getMinReward() { return minReward; }
    public int getMaxReward() { return maxReward; }
    public List<Integer> getScheduleHours() { return scheduleHours; }

    public static EventType fromId(String id) {
        for (EventType type : values()) {
            if (type.id.equalsIgnoreCase(id)) return type;
        }
        return null;
    }

    public boolean isScheduledFor(DayOfWeek day, int hour) {
        if (!scheduleHours.contains(hour)) return false;
        return category == EventCategory.SCHEDULED || WEEKDAYS.contains(day);
    }

    public int calculateReward(int playerCount, int placement) {
        double factor = Math.min(1.0, playerCount / 10.0);
        int base = minReward + (int) ((maxReward - minReward) * factor);
        if (placement == 1) return base;
        if (placement == 2) return (int) (base * 0.6);
        if (placement == 3) return (int) (base * 0.3);
        return 0;
    }

    public enum EventCategory {
        DAILY,
        SCHEDULED
    }
}
