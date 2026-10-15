package net.mineskyevents.entities.events;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.models.EventState;
import net.mineskyevents.models.EventType;
import net.mineskyevents.utils.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TntTagEvent extends BaseEvent {

    private UUID taggedPlayer;
    private int explosionTimer;
    private Scheduler.Task tagTask;

    public TntTagEvent(EventType type, Category category, String mapId) {
        super(type, category, mapId);
    }

    @Override
    protected void onGameStart() {
        List<UUID> players = new ArrayList<>(alivePlayers);
        if (players.size() < 2) return;

        Collections.shuffle(players);
        taggedPlayer = players.get(0);
        tagPlayer(Bukkit.getPlayer(taggedPlayer));

        explosionTimer = 15 + new Random().nextInt(16);
        tagTask = Scheduler.runTaskTimer(this::tickTag, 20L, 20L);
    }

    private void tickTag() {
        if (MineSkyEvents.getInstance().getEventManager().getState() != EventState.RUNNING) {
            if (tagTask != null) tagTask.cancel();
            return;
        }

        explosionTimer--;
        Player tagged = Bukkit.getPlayer(taggedPlayer);
        if (tagged != null) {
            tagged.sendActionBar(net.kyori.adventure.text.Component.text("TNT explode em " + explosionTimer + "s!"));
        }

        if (explosionTimer <= 0) {
            if (tagged != null) {
                tagged.getWorld().createExplosion(tagged.getLocation(), 0f, false, false);
                eliminatePlayer(tagged);
            }
            if (alivePlayers.size() > 1) {
                List<UUID> remaining = new ArrayList<>(alivePlayers);
                taggedPlayer = remaining.get(new Random().nextInt(remaining.size()));
                tagPlayer(Bukkit.getPlayer(taggedPlayer));
                explosionTimer = 15 + new Random().nextInt(16);
            }
        }
    }

    public void transferTag(Player from, Player to) {
        if (!from.getUniqueId().equals(taggedPlayer)) return;
        if (!alivePlayers.contains(to.getUniqueId())) return;

        taggedPlayer = to.getUniqueId();
        from.getInventory().setHelmet(null);
        tagPlayer(to);
        explosionTimer = 15 + new Random().nextInt(16);
    }

    private void tagPlayer(Player player) {
        if (player == null) return;
        player.getInventory().setHelmet(new ItemStack(Material.TNT));
    }

    public UUID getTaggedPlayer() { return taggedPlayer; }

    @Override
    protected void onPlayerEliminated(Player player) {
        if (player.getUniqueId().equals(taggedPlayer) && alivePlayers.size() > 1) {
            List<UUID> remaining = new ArrayList<>(alivePlayers);
            taggedPlayer = remaining.get(new Random().nextInt(remaining.size()));
            tagPlayer(Bukkit.getPlayer(taggedPlayer));
            explosionTimer = 15 + new Random().nextInt(16);
        }
    }

    @Override
    protected void onCleanUp() {
        if (tagTask != null) tagTask.cancel();
    }
}
