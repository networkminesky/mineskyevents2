package net.mineskyevents.gui.miniwars;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.events.MiniWarsEvent;
import net.mineskyevents.managers.EventManager;
import net.mineskyevents.models.EventState;
import net.mineskyevents.utils.Messages;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class MiniWarsTeamSelector {

    private MiniWarsTeamSelector() {}

    public static boolean tryOpenGui(Player player) {
        EventManager manager = MineSkyEvents.getInstance().getEventManager();

        if (manager.getState() == EventState.INACTIVE || !(manager.getActiveEvent() instanceof MiniWarsEvent wars)) {
            Messages.send(player, "miniwars.team-not-miniwars");
            Messages.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f);
            return false;
        }

        if (!manager.isPlaying(player)) {
            Messages.send(player, "errors.not-in-event");
            Messages.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f);
            return false;
        }

        if (!player.hasPermission(MiniWarsEvent.PERM_CHANGE_TEAMS)) {
            Messages.send(player, "errors.no-permission");
            Messages.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f);
            return false;
        }

        if (!wars.isTeamSelectionAllowed()) {
            Messages.send(player, "miniwars.team-selection-closed");
            Messages.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f);
            return false;
        }

        Messages.playSound(player, Sound.UI_BUTTON_CLICK, 1.1f);
        MiniWarsTeamGui.open(player, wars);
        return true;
    }
}
