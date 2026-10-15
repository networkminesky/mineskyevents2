package net.mineskyevents.commands.subcommands.events;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.commands.subcommands.SubCommand;
import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.entities.categories.CategoryHandler;
import net.mineskyevents.entities.events.SpleefEvent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class StartSubCommand extends SubCommand {
    @Override
    public String getName() { return "start"; }
    @Override
    public String getDescription() { return "Inicia um evento carregando um mapa específico."; }
    @Override
    public String getUsage() { return "/event start <mapa>"; }
    @Override
    public List<String> getAliases() { return Collections.singletonList("iniciar"); }
    @Override
    public String hasPermission() { return "mineskyevents.admin"; }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cComo usar: " + getUsage());
            return;
        }

        String mapId = args[1];
        Category foundCategory = null;

        for (Category category : CategoryHandler.categories) {
            if (category.getConfig().contains("mapas." + mapId)) {
                foundCategory = category;
                break;
            }
        }

        if (foundCategory == null) {
            player.sendMessage("§c[!] Erro: Nenhum mapa com o ID '" + mapId + "' foi encontrado.");
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            return;
        }

        String categoryId = foundCategory.getId();
        if (categoryId.equalsIgnoreCase("spleef")) {
            player.sendMessage("§aSpleef iniciado no mapa §l" + mapId + "§a!");

            SpleefEvent spleef = new SpleefEvent(foundCategory, mapId);

            MineSkyEvents.getInstance().getEventManager().setActiveEvent(spleef);
            spleef.startSignups();
        }
    }
}