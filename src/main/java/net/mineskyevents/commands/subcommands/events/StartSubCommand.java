package net.mineskyevents.commands.subcommands.events;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.commands.subcommands.SubCommand;
import net.mineskyevents.map.MapManager;
import net.mineskyevents.models.EventType;
import net.mineskyevents.utils.Messages;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class StartSubCommand extends SubCommand {

    @Override
    public String getName() { return "start"; }

    @Override
    public String getDescription() { return "Inicia um evento manualmente."; }

    @Override
    public String getUsage() { return "/event start <tipo> [mapa]"; }

    @Override
    public List<String> getAliases() { return List.of("iniciar"); }

    @Override
    public String hasPermission() { return "mineskyevents.admin"; }

    @Override
    public boolean allowsConsole() { return true; }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUso: " + getUsage());
            return;
        }

        EventType type = EventType.fromId(args[1]);
        if (type == null) {
            type = Arrays.stream(EventType.values())
                    .filter(t -> t.getDisplayName().equalsIgnoreCase(args[1]))
                    .findFirst().orElse(null);
        }

        if (type == null) {
            Messages.send(sender, "errors.map-not-found", Placeholder.parsed("map", args[1]));
            return;
        }

        String mapId = args.length >= 3 ? args[2] : MapManager.getRandomMapId(type);
        if (mapId == null) {
            sender.sendMessage("§cNenhum mapa configurado para " + type.getDisplayName());
            return;
        }

        boolean started = MineSkyEvents.getInstance().getEventManager().startEvent(type, mapId, true);
        if (!started) {
            Messages.send(sender, "admin.event-start-failed",
                    Placeholder.parsed("event", type.getDisplayName()),
                    Placeholder.parsed("map", mapId));
            return;
        }
        Messages.send(sender, "admin.event-started",
                Placeholder.parsed("event", type.getDisplayName()),
                Placeholder.parsed("map", mapId));
        if (sender instanceof Player player) {
            Messages.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f);
        }
    }
}
