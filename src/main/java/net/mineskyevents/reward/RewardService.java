package net.mineskyevents.reward;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.models.EventType;
import net.mineskyevents.proxy.ProxyService;
import net.mineskyevents.utils.Messages;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

public class RewardService {

    private final ProxyService proxy;

    public RewardService(ProxyService proxy) {
        this.proxy = proxy;
    }

    public void rewardWinner(Player winner, EventType type, int playerCount) {
        rewardPlacements(List.of(winner), type, playerCount);
    }

    public void rewardPlacements(List<Player> winners, EventType type, int playerCount) {
        grantPlacements(winners, type, playerCount);
    }

    /** Recompensas monetárias via proxy (sem anúncio global de vitória). */
    public void grantPlacements(List<Player> winners, EventType type, int playerCount) {
        for (int i = 0; i < winners.size(); i++) {
            Player player = winners.get(i);
            if (player == null) continue;

            int placement = i + 1;
            int amount = type.calculateReward(playerCount, placement);

            if (amount > 0) {
                proxy.sendReward(player.getName(), amount, type.getId(), placement);
                Messages.send(player, "event.placement",
                        Placeholder.parsed("place", String.valueOf(placement)),
                        Placeholder.parsed("player", player.getName()),
                        Placeholder.parsed("amount", String.valueOf(amount)));
                Messages.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.2f);
            }
        }
    }

    public void splitReward(List<Player> winners, EventType type, int playerCount) {
        if (winners.isEmpty()) return;

        int total = type.calculateReward(playerCount, 1);
        int perPlayer = total / winners.size();

        for (Player player : winners) {
            if (player == null) continue;
            proxy.sendReward(player.getName(), perPlayer, type.getId(), 1);
            Messages.send(player, "event.placement",
                    Placeholder.parsed("place", "1"),
                    Placeholder.parsed("player", player.getName()),
                    Placeholder.parsed("amount", String.valueOf(perPlayer)));
            Messages.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.2f);
        }
    }
}
