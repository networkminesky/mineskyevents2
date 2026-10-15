package net.mineskyevents.managers;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.entities.events.BaseEvent;
import net.mineskyevents.entities.events.EventFactory;
import net.mineskyevents.map.MapManager;
import net.mineskyevents.models.EventState;
import net.mineskyevents.models.EventType;
import net.mineskyevents.utils.Messages;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EventManager {

    private EventState currentState = EventState.INACTIVE;
    private BaseEvent activeEvent;
    private final Set<UUID> activePlayers = new HashSet<>();
    private final Set<UUID> spectators = new HashSet<>();

    public boolean hasActiveEvent() {
        return currentState != EventState.INACTIVE && activeEvent != null;
    }

    public BaseEvent getActiveEvent() { return activeEvent; }

    public void setActiveEvent(BaseEvent event) { this.activeEvent = event; }

    public EventState getState() { return currentState; }

    public void setState(EventState state) { this.currentState = state; }

    public boolean isState(EventState state) { return currentState == state; }

    public void reset() {
        activePlayers.clear();
        spectators.clear();
        activeEvent = null;
        currentState = EventState.INACTIVE;
    }

    public void startEvent(EventType type, String mapId, boolean manual) {
        if (hasActiveEvent()) {
            MineSkyEvents.l.warning("Tentativa de iniciar evento enquanto outro está ativo.");
            return;
        }

        Category category = MapManager.findCategoryForMap(mapId);
        if (category == null && mapId != null) {
            category = net.mineskyevents.entities.categories.CategoryHandler.getCategoryById(type.getId());
        }

        if (mapId == null || mapId.isEmpty()) {
            mapId = MapManager.getRandomMapId(type);
        }

        if (mapId == null) {
            MineSkyEvents.l.warning("Nenhum mapa disponível para " + type.getId());
            return;
        }

        BaseEvent event = EventFactory.create(type, mapId);
        if (event == null) {
            MineSkyEvents.l.warning("Falha ao criar instância do evento: " + type.getId());
            return;
        }

        event.startSignups();
        MineSkyEvents.l.info("Evento iniciado" + (manual ? " (manual)" : " (agendado)") + ": " + type.getDisplayName());
    }

    public void forceStop() {
        if (!hasActiveEvent()) return;
        Messages.broadcast("event.stopped");
        activeEvent.resetEvent();
    }

    public void addPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        spectators.remove(uuid);
        activePlayers.add(uuid);
    }

    public void removePlayer(Player player) {
        activePlayers.remove(player.getUniqueId());
    }

    public boolean isPlaying(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    public void addSpectator(Player player) {
        UUID uuid = player.getUniqueId();
        activePlayers.remove(uuid);
        spectators.add(uuid);
    }

    public void removeSpectator(Player player) {
        spectators.remove(player.getUniqueId());
    }

    public boolean isSpectating(Player player) {
        return spectators.contains(player.getUniqueId());
    }

    public void handleQuit(Player player) {
        UUID uuid = player.getUniqueId();
        activePlayers.remove(uuid);
        spectators.remove(uuid);
    }

    public Set<UUID> getActivePlayers() {
        return Collections.unmodifiableSet(activePlayers);
    }

    public Set<UUID> getSpectators() {
        return Collections.unmodifiableSet(spectators);
    }
}
