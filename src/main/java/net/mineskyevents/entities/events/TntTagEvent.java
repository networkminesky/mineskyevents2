package net.mineskyevents.entities.events;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.models.EventState;
import net.mineskyevents.models.EventType;
import net.mineskyevents.utils.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
            refreshTaggedGlow(tagged);
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

    public void transferTag(Player tagged, Player target) {
        if (tagged == null || target == null) return;
        if (!tagged.getUniqueId().equals(taggedPlayer)) return;
        if (!alivePlayers.contains(target.getUniqueId())) return;
        if (tagged.getUniqueId().equals(target.getUniqueId())) return;

        clearTagVisuals(tagged);
        taggedPlayer = target.getUniqueId();
        tagPlayer(target);
        explosionTimer = 15 + new Random().nextInt(16);

        spawnPassFirework(target.getLocation());
        target.playSound(target.getLocation(), Sound.ENTITY_TNT_PRIMED, 0.85f, 1.1f);
        tagged.sendMessage("§eVocê passou a TNT para §f" + target.getName() + "§e!");
        target.sendMessage("§c§lVocê está com a TNT! §7Passe batendo em outro jogador.");
    }

    private void tagPlayer(Player player) {
        if (player == null) return;
        player.getInventory().setHelmet(new ItemStack(Material.TNT));
        refreshTaggedGlow(player);
        player.playSound(player.getLocation(), Sound.ENTITY_TNT_PRIMED, 0.7f, 1.2f);
    }

    private void refreshTaggedGlow(Player player) {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.GLOWING, 80, 0, false, false, true));
    }

    private void clearTagVisuals(Player player) {
        player.getInventory().setHelmet(null);
        player.removePotionEffect(PotionEffectType.GLOWING);
    }

    private void spawnPassFirework(org.bukkit.Location location) {
        if (location.getWorld() == null) return;
        Firework firework = location.getWorld().spawn(location, Firework.class, fw -> {
            FireworkMeta meta = fw.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder()
                    .with(FireworkEffect.Type.BALL)
                    .withColor(Color.RED, Color.ORANGE)
                    .flicker(true)
                    .trail(true)
                    .build());
            meta.setPower(0);
            fw.setFireworkMeta(meta);
        });
        Scheduler.runTaskLater(firework::detonate, 1L);
    }

    public UUID getTaggedPlayer() { return taggedPlayer; }

    @Override
    public List<String> extraScoreboardLines(Player viewer) {
        if (taggedPlayer == null) return List.of();
        Player tagged = Bukkit.getPlayer(taggedPlayer);
        String name = tagged != null ? tagged.getName() : "?";
        return List.of("§cCom TNT: §f" + name);
    }

    @Override
    protected void onPlayerEliminated(Player player, Player killer, Location deathLocation) {
        clearTagVisuals(player);
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
        for (UUID uuid : alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) clearTagVisuals(p);
        }
    }
}
