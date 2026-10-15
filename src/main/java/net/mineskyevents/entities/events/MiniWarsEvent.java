package net.mineskyevents.entities.events;

import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.models.EventType;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.*;

public class MiniWarsEvent extends BaseEvent {

    @Override
    public boolean allowsPlayerVsPlayer() { return true; }

    private static final Color[] TEAM_COLORS = {
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW
    };
    private static final String[] TEAM_NAMES = {"Vermelho", "Azul", "Verde", "Amarelo"};

    private final Map<UUID, Integer> playerTeams = new HashMap<>();
    private final Map<Integer, Set<UUID>> teams = new HashMap<>();

    public MiniWarsEvent(EventType type, Category category, String mapId) {
        super(type, category, mapId);
        for (int i = 0; i < 4; i++) teams.put(i, new HashSet<>());
    }

    @Override
    protected void onGameStart() {
        List<UUID> players = new ArrayList<>(alivePlayers);
        Collections.shuffle(players);

        for (int i = 0; i < players.size(); i++) {
            int team = i % 4;
            UUID uuid = players.get(i);
            playerTeams.put(uuid, team);
            teams.get(team).add(uuid);

            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                giveKit(p);
                giveTeamKit(p, team);
            }
        }
    }

    private void giveTeamKit(Player player, int team) {
        player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
        player.getInventory().addItem(new ItemStack(Material.CROSSBOW));
        player.getInventory().addItem(new ItemStack(Material.ARROW, 32));

        ItemStack helmet = coloredArmor(Material.LEATHER_HELMET, TEAM_COLORS[team]);
        ItemStack chest = coloredArmor(Material.LEATHER_CHESTPLATE, TEAM_COLORS[team]);
        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chest);

        player.sendMessage("§7Você está no time §l" + TEAM_NAMES[team] + "§7!");
    }

    private ItemStack coloredArmor(Material mat, Color color) {
        ItemStack item = new ItemStack(mat);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        if (meta != null) {
            meta.setColor(color);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    protected void onPlayerEliminated(Player player) {
        Integer team = playerTeams.get(player.getUniqueId());
        if (team != null) {
            teams.get(team).remove(player.getUniqueId());
            checkTeamWin();
        }
    }

    private void checkTeamWin() {
        int aliveTeams = 0;
        int winningTeam = -1;
        for (Map.Entry<Integer, Set<UUID>> entry : teams.entrySet()) {
            entry.getValue().removeIf(uuid -> !alivePlayers.contains(uuid));
            if (!entry.getValue().isEmpty()) {
                aliveTeams++;
                winningTeam = entry.getKey();
            }
        }

        if (aliveTeams <= 1 && winningTeam >= 0) {
            List<Player> winners = new ArrayList<>();
            for (UUID uuid : teams.get(winningTeam)) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) winners.add(p);
            }
            finishEvent(winners.isEmpty() ? List.of() : winners);
        }
    }

    public boolean isSameTeam(Player a, Player b) {
        Integer teamA = playerTeams.get(a.getUniqueId());
        Integer teamB = playerTeams.get(b.getUniqueId());
        return teamA != null && teamA.equals(teamB);
    }

    @Override
    protected void onCleanUp() {
        playerTeams.clear();
        teams.values().forEach(Set::clear);
    }
}
