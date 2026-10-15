package net.mineskyevents.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.events.BaseEvent;
import net.mineskyevents.models.EventState;
import net.mineskyevents.utils.Scheduler;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventScoreboardManager {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static final String CODES = "0123456789abcdef";

    private Scheduler.Task updateTask;
    private BaseEvent attachedEvent;

    public void attach(BaseEvent event) {
        this.attachedEvent = event;
        if (updateTask != null) updateTask.cancel();
        updateTask = Scheduler.runTaskTimer(this::refreshAll, 0L, 20L);
    }

    public void detach() {
        attachedEvent = null;
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    private void refreshAll() {
        EventManager manager = MineSkyEvents.getInstance().getEventManager();
        if (!manager.hasActiveEvent() || attachedEvent == null) {
            detach();
            return;
        }

        for (UUID uuid : manager.getActivePlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) applyBoard(player, manager.getState());
        }
        for (UUID uuid : manager.getSpectators()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) applyBoard(player, manager.getState());
        }
    }

    private void applyBoard(Player player, EventState state) {
        List<Component> lines = buildLines(player, state);
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective(
                "msevent",
                Criteria.DUMMY,
                MINI.deserialize("<gradient:#00d4ff:#c084fc><bold>MineSky</bold></gradient>")
        );
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.numberFormat(NumberFormat.blank());

        int score = lines.size();
        for (int i = 0; i < lines.size(); i++) {
            String entry = entry(i);
            Team team = board.registerNewTeam("l" + i);
            team.prefix(lines.get(i));
            team.addEntry(entry);
            objective.getScore(entry).setScore(score--);
        }

        player.setScoreboard(board);
    }

    private List<Component> buildLines(Player viewer, EventState state) {
        List<Component> lines = new ArrayList<>();
        EventManager manager = MineSkyEvents.getInstance().getEventManager();

        lines.add(spacer());
        lines.add(MINI.deserialize("<gradient:#7dd3fc:#a78bfa><bold>"
                + escape(attachedEvent.getEventName()) + "</bold></gradient>"));
        lines.add(MINI.deserialize("<gradient:#334155:#64748b>━━━━━━━━━━━━</gradient>"));
        lines.add(spacer());
        lines.add(row("Status", stateLabel(state)));

        if (attachedEvent.showsRemainingPlayers()) {
            int alive = state == EventState.RUNNING
                    ? attachedEvent.getAlivePlayers().size()
                    : manager.getActivePlayers().size();
            String label = state == EventState.RUNNING ? "Vivos" : "Na fila";
            lines.add(row(label, "<gradient:#fbbf24:#fb7185><bold>" + alive + "</bold></gradient>"));
        } else if (state != EventState.RUNNING) {
            lines.add(row("Na fila", "<gradient:#fbbf24:#fb7185><bold>"
                    + manager.getActivePlayers().size() + "</bold></gradient>"));
        }

        String personal = attachedEvent.formatPersonalScore(viewer);
        if (personal != null && !personal.isBlank()) {
            lines.add(spacer());
            lines.add(asComponent(personal));
        }

        List<String> extra = attachedEvent.extraScoreboardLines(viewer);
        if (!extra.isEmpty()) {
            lines.add(spacer());
            for (String line : extra) {
                lines.add(asComponent(line));
            }
        }

        lines.add(spacer());
        lines.add(MINI.deserialize("<gradient:#334155:#64748b>━━━━━━━━━━━━</gradient>"));
        return lines;
    }

    private Component spacer() {
        return Component.text(" ");
    }

    private Component row(String label, String valueMini) {
        return MINI.deserialize("<gray>" + label + "</gray>  " + valueMini);
    }

    private String stateLabel(EventState state) {
        return switch (state) {
            case WAITING -> "<gradient:#fbbf24:#f97316>Aguardando</gradient>";
            case STARTING -> "<gradient:#38bdf8:#818cf8>Iniciando</gradient>";
            case RUNNING -> "<gradient:#4ade80:#22d3ee>Ao vivo</gradient>";
            default -> "<gray>—</gray>";
        };
    }

    private Component asComponent(String raw) {
        if (raw.indexOf('§') >= 0) {
            return LEGACY.deserialize(ChatColor.translateAlternateColorCodes('&', raw));
        }
        return MINI.deserialize(raw);
    }

    private static String escape(String text) {
        return text.replace("<", "\\<").replace(">", "\\>");
    }

    private static String entry(int index) {
        return "§" + CODES.charAt(index % 16) + "§" + CODES.charAt((index / 16) % 16) + "§r";
    }
}
