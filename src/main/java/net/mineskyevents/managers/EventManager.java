package net.mineskyevents.managers;

import net.mineskyevents.entities.events.BaseEvent;
import net.mineskyevents.models.EventState;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EventManager {

    private EventState currentState = EventState.INACTIVE;
    private BaseEvent activeEvent = null; // Armazena o evento em execução

    private final Set<UUID> activePlayers = new HashSet<>();
    private final Set<UUID> spectators = new HashSet<>();

    public BaseEvent getActiveEvent() {
        return activeEvent;
    }

    public void setActiveEvent(BaseEvent activeEvent) {
        this.activeEvent = activeEvent;
    }

    public EventState getState() {
        return currentState;
    }

    public void setState(EventState state) {
        this.currentState = state;
    }

    public boolean isState(EventState state) {
        return this.currentState == state;
    }

    public void reset() {
        activePlayers.clear();
        spectators.clear();
        activeEvent = null;
        currentState = EventState.INACTIVE;
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