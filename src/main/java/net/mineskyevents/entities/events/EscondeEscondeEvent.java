package net.mineskyevents.entities.events;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.models.EventState;
import net.mineskyevents.models.EventType;
import net.mineskyevents.utils.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class EscondeEscondeEvent extends BaseEvent {

    private final Set<UUID> hiders = new HashSet<>();
    private final Set<UUID> seekers = new HashSet<>();
    private final Set<UUID> caught = new HashSet<>();
    private Scheduler.Task hideTimer;

    public EscondeEscondeEvent(EventType type, Category category, String mapId) {
        super(type, category, mapId);
    }

    @Override
    protected void onGameStart() {
        List<UUID> players = new ArrayList<>(alivePlayers);
        Collections.shuffle(players);

        int seekerCount = Math.max(1, players.size() / 5);
        for (int i = 0; i < seekerCount; i++) {
            seekers.add(players.get(i));
        }
        for (int i = seekerCount; i < players.size(); i++) {
            hiders.add(players.get(i));
        }

        for (UUID uuid : hiders) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                giveKit(p);
                p.sendMessage("§aVocê é um §lESCONDIDO§a! Esconda-se dos caçadores!");
            }
        }

        for (UUID uuid : seekers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                giveKit(p);
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 1));
                p.sendMessage("§cVocê é um §lCAÇADOR§c! Encontre todos os escondidos!");
            }
        }

        hideTimer = Scheduler.runTaskLater(() -> {
            for (UUID uuid : seekers) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) p.removePotionEffect(PotionEffectType.BLINDNESS);
            }
            Bukkit.broadcastMessage("§e§lOs caçadores foram liberados!");
        }, 200L);
    }

    public void catchPlayer(Player seeker, Player hider) {
        if (!seekers.contains(seeker.getUniqueId())) return;
        if (!hiders.contains(hider.getUniqueId()) || caught.contains(hider.getUniqueId())) return;

        caught.add(hider.getUniqueId());
        hiders.remove(hider.getUniqueId());
        eliminatePlayer(hider);

        if (hiders.isEmpty()) {
            List<Player> seekerWinners = new ArrayList<>();
            for (UUID uuid : seekers) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) seekerWinners.add(p);
            }
            MineSkyEvents.getInstance().getRewardService().splitReward(seekerWinners, eventType, alivePlayers.size() + caught.size());
            resetEvent();
        }
    }

    public boolean isHider(Player player) {
        return hiders.contains(player.getUniqueId());
    }

    public boolean isSeeker(Player player) {
        return seekers.contains(player.getUniqueId());
    }

    @Override
    protected void onPlayerEliminated(Player player) {
        if (seekers.size() == 1 && hiders.contains(player.getUniqueId())) {
            // hider caught - checked in catchPlayer
        }
    }

    @Override
    protected void checkWinCondition() {
        if (hiders.isEmpty() && !seekers.isEmpty()) return;
        super.checkWinCondition();
    }

    @Override
    protected void onCleanUp() {
        if (hideTimer != null) hideTimer.cancel();
        hiders.clear();
        seekers.clear();
        caught.clear();
    }

}
