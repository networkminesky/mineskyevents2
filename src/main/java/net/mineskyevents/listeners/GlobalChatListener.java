package net.mineskyevents.listeners;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.chat.GlobalChatBridge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Qualquer mensagem no chat (sem comando) no servidor de eventos vira chat global na rede.
 */
public class GlobalChatListener implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        GlobalChatBridge bridge = MineSkyEvents.getInstance().getGlobalChatBridge();
        if (bridge == null || !bridge.isEnabled()) return;

        event.setCancelled(true);
        bridge.sendAsGlobal(event.getPlayer(), event.getMessage());
    }
}
