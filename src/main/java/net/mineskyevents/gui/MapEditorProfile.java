package net.mineskyevents.gui;

import net.mineskyevents.models.EventType;

import java.util.EnumSet;
import java.util.Set;

public final class MapEditorProfile {

    private MapEditorProfile() {}

    public static Set<MapEditorAction> actionsFor(CategoryId categoryId) {
        return switch (categoryId) {
            case CORRIDA -> EnumSet.of(
                    MapEditorAction.POS1, MapEditorAction.POS2, MapEditorAction.REGION_WE,
                    MapEditorAction.LOBBY, MapEditorAction.SPAWN,
                    MapEditorAction.FINISH, MapEditorAction.CHECKPOINT
            );
            case CORRIDA_ELYTRA -> EnumSet.of(
                    MapEditorAction.POS1, MapEditorAction.POS2, MapEditorAction.REGION_WE,
                    MapEditorAction.LOBBY, MapEditorAction.SPAWN,
                    MapEditorAction.FINISH, MapEditorAction.CHECKPOINT, MapEditorAction.ARCO
            );
            case CORRIDA_BARCO -> EnumSet.of(
                    MapEditorAction.POS1, MapEditorAction.POS2, MapEditorAction.REGION_WE,
                    MapEditorAction.LOBBY, MapEditorAction.SPAWN, MapEditorAction.FINISH
            );
            case RUNNERS -> EnumSet.of(
                    MapEditorAction.POS1, MapEditorAction.POS2, MapEditorAction.REGION_WE,
                    MapEditorAction.LOBBY, MapEditorAction.SPAWN, MapEditorAction.EXIT
            );
            case ESCONDE_ESCONDE -> EnumSet.of(
                    MapEditorAction.POS1, MapEditorAction.POS2, MapEditorAction.REGION_WE,
                    MapEditorAction.LOBBY, MapEditorAction.SPAWN, MapEditorAction.BARRIER_WE
            );
            case MINI_WARS -> EnumSet.of(
                    MapEditorAction.POS1, MapEditorAction.POS2, MapEditorAction.REGION_WE,
                    MapEditorAction.LOBBY, MapEditorAction.SPAWN,
                    MapEditorAction.TEAM_SPAWN_0, MapEditorAction.TEAM_SPAWN_1,
                    MapEditorAction.TEAM_SPAWN_2, MapEditorAction.TEAM_SPAWN_3,
                    MapEditorAction.CANNON_0, MapEditorAction.CANNON_1,
                    MapEditorAction.CANNON_2, MapEditorAction.CANNON_3
            );
            case DEFAULT_ARENA -> EnumSet.of(
                    MapEditorAction.POS1, MapEditorAction.POS2, MapEditorAction.REGION_WE,
                    MapEditorAction.LOBBY, MapEditorAction.SPAWN
            );
        };
    }

    public static CategoryId resolve(String categoryId) {
        EventType type = EventType.fromId(categoryId);
        if (type == null) return CategoryId.DEFAULT_ARENA;
        return switch (type) {
            case CORRIDA -> CategoryId.CORRIDA;
            case CORRIDA_ELYTRA -> CategoryId.CORRIDA_ELYTRA;
            case CORRIDA_BARCO -> CategoryId.CORRIDA_BARCO;
            case RUNNERS -> CategoryId.RUNNERS;
            case ESCONDE_ESCONDE -> CategoryId.ESCONDE_ESCONDE;
            case MINI_WARS -> CategoryId.MINI_WARS;
            default -> CategoryId.DEFAULT_ARENA;
        };
    }

    public enum CategoryId {
        DEFAULT_ARENA,
        CORRIDA,
        CORRIDA_ELYTRA,
        CORRIDA_BARCO,
        RUNNERS,
        ESCONDE_ESCONDE,
        MINI_WARS
    }
}
