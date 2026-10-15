package net.mineskyevents.utils;

import net.mineskyevents.MineSkyEvents;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {
    public static String c(String s) {
        return hex(s);
    }

    private static String createHex(String hexString) {
        hexString = hexString.replace("&", "");
        return net.md_5.bungee.api.ChatColor.of(hexString).toString();
    }

    public static String hex(String message) {
        Pattern hexPattern = Pattern.compile("&#[A-Fa-f0-9]{6}");
        Matcher matcher = hexPattern.matcher(message);

        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(result, createHex(matcher.group()));
        }

        matcher.appendTail(result);
        message = result.toString();

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .filter(b -> !isPlayerVanished(b))
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    public static boolean isPlayerVanished(Player p) {
        for (MetadataValue meta : p.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }

    public static void awaitChatInput(Player player, ChatInputCallback callback) {
        player.sendTitle("§e§lDigite no chat", "§7Digite 'sair' para voltar!", 5, 60, 20);
        player.closeInventory();

        Listener listener = new ChatListener(player, callback);
        Bukkit.getServer().getPluginManager().registerEvents(listener, MineSkyEvents.getInstance());
    }

    public static void confirmAction(Player player, String action, Runnable onConfirm) {
        player.sendMessage(Utils.c("&e\uD83D\uDEA8 &6&lATENÇÃO&7: Digite &e\"Confirmar\" &7no chat para " + action + "."));
        Utils.awaitChatInput(player, new ChatInputCallback() {
            public void onInput(String input) {
                if (input.equalsIgnoreCase("confirmar") || input.equalsIgnoreCase("sim")) {
                    onConfirm.run();
                } else {
                    player.sendMessage(Utils.c("&c❌ Cancelado."));
                }
            }

            public void onCancel() {
                player.sendMessage(Utils.c("&c⏳ Tempo esgotado. Cancelado."));
            }
        });
    }

    public static class ChatListener implements Listener {
        private final Player player;
        private final ChatInputCallback callback;

        public ChatListener(Player player, ChatInputCallback callback) {
            this.player = player;
            this.callback = callback;
        }

        @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
        public void onChat(AsyncPlayerChatEvent e) {
            if (!e.getPlayer().equals(player)) return;

            e.setCancelled(true);

            Scheduler.runTaskAtEntity(e.getPlayer(), () -> {
                String msg = e.getMessage();

                if (msg.equalsIgnoreCase("cancel") || msg.equalsIgnoreCase("cancelar") ||
                        msg.equalsIgnoreCase("close") || msg.equalsIgnoreCase("sair")) {
                    callback.onCancel();
                    e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    e.getPlayer().sendMessage(Utils.c("&cCancelando e retornando ao menu..."));
                    AsyncPlayerChatEvent.getHandlerList().unregister(this);
                    return;
                }

                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
                AsyncPlayerChatEvent.getHandlerList().unregister(this);
                callback.onInput(Utils.c(msg));
            });
        }
    }
}
