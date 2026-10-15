package net.mineskyevents.entities.events;

import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.entities.categories.CategoryHandler;
import net.mineskyevents.models.EventType;

public final class EventFactory {

    private EventFactory() {}

    public static BaseEvent create(EventType type, String mapId) {
        Category category = CategoryHandler.getCategoryById(type.getId());
        if (category == null) return null;

        return switch (type) {
            case SPLEEF -> new SpleefEvent(type, category, mapId);
            case TIJOLAO_WARS -> new TijolaoWarsEvent(type, category, mapId);
            case RUNNERS -> new RunnersEvent(type, category, mapId);
            case CORRIDA -> new CorridaEvent(type, category, mapId);
            case CORRIDA_BARCO -> new CorridaBarcoEvent(type, category, mapId);
            case TNT_RUN -> new TntRunEvent(type, category, mapId);
            case TNT_TAG -> new TntTagEvent(type, category, mapId);
            case SUMO -> new SumoEvent(type, category, mapId);
            case CORRIDA_ELYTRA -> new CorridaElytraEvent(type, category, mapId);
            case MINI_WARS -> new MiniWarsEvent(type, category, mapId);
            case ESCONDE_ESCONDE -> new EscondeEscondeEvent(type, category, mapId);
        };
    }
}
