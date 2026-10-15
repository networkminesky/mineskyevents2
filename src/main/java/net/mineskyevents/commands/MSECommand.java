package net.mineskyevents.commands;

import net.mineskyevents.gui.EventGui;
import net.mineskyevents.utils.EventItems;
import net.mineskyevents.utils.Scheduler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class MSECommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command cmd, @NotNull String lbl, @NotNull String @NotNull [] args) {
        if (s instanceof Player player) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("give")) {
                    if (player.hasPermission("mineskyevents.give")) {
                        EventItems.dropAllCustomItems(player);
                    }
                }
                String type = args[0].toLowerCase();

                Inventory targetInventory = switch (type) {
                    case "cb_shopi" -> EventGui.getInventorySHOP_INICIAL();
                    case "cb_shopb" -> EventGui.getInventorySHOP_BLOCOS();
                    case "cb_shopc" -> EventGui.getInventorySHOP_COMBATE();
                    case "cb_shopp" -> EventGui.getInventorySHOP_POÇOES();
                    case "cb_shopf" -> EventGui.getInventorySHOP_FERRAMENTAS();
                    case "cb_shopa" -> EventGui.getInventorySHOP_ATAQUE();
                    case "cb_shopo" -> EventGui.getInventorySHOP_OUTROS();
                    default -> null;
                };

                if (targetInventory != null) {
                    Scheduler.runTask(() -> player.openInventory(targetInventory));
                    return true;
                }
            }
        }
        return true;
    }
}
