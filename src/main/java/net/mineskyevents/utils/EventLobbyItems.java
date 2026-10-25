package net.mineskyevents.utils;

import net.mineskyevents.entities.events.BaseEvent;
import net.mineskyevents.entities.events.MiniWarsEvent;
import org.bukkit.entity.Player;

public final class EventLobbyItems {

    public static final int SLOT_LEAVE = 8;
    public static final int SLOT_MINI_WARS_TEAM = 7;

    private EventLobbyItems() {}

    public static void giveWaitingHotbar(Player player, BaseEvent event) {
        player.getInventory().setItem(SLOT_LEAVE, EventItems.createBed());
        if (event instanceof MiniWarsEvent && player.hasPermission(MiniWarsEvent.PERM_CHANGE_TEAMS)) {
            player.getInventory().setItem(SLOT_MINI_WARS_TEAM, EventItems.createMiniWarsTeamPicker());
        }
    }
}
