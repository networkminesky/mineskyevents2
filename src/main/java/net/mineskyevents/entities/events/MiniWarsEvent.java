package net.mineskyevents.entities.events;

import net.mineskyevents.MineSkyEvents;
import net.mineskyevents.entities.categories.Category;
import net.mineskyevents.map.MapManager;
import net.mineskyevents.miniwars.MiniWarsCannonManager;
import net.mineskyevents.miniwars.TeamCannon;
import net.mineskyevents.models.EventState;
import net.mineskyevents.models.EventType;
import net.mineskyevents.utils.EventCelebration;
import net.mineskyevents.utils.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mineskyevents.utils.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Trident;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.ShieldMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MiniWarsEvent extends BaseEvent {

    @Override
    public boolean allowsPlayerVsPlayer() { return true; }

    @Override
    public boolean eliminatesOnVoid() { return true; }

    @Override
    public boolean usesSurvivalMode() { return true; }

    private static final Color[] TEAM_COLORS = {
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW
    };
    private static final DyeColor[] TEAM_DYES = {
            DyeColor.RED, DyeColor.BLUE, DyeColor.GREEN, DyeColor.YELLOW
    };
    public static final int TEAM_COUNT = 4;
    public static final String[] TEAM_NAMES = {"Vermelho", "Azul", "Verde", "Amarelo"};
    private static final String[] TEAM_COLOR_CODES = {"§c", "§9", "§a", "§e"};
    private static final String[][] TEAM_GRADIENT_HEX = {
            {"#ff6b6b", "#c92a2a"},
            {"#5c9dff", "#1c4ed8"},
            {"#51cf66", "#2b8a3e"},
            {"#ffd43b", "#e67700"}
    };
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    public static final String PERM_CHANGE_TEAMS = "mineskyevents.changeteams";
    public static final String PERM_BYPASS_TEAM_LIMIT = "mineskyevents.bypassteamlimit";

    private static final int SHIELD_DISABLE_TICKS = 40;
    private static final double CANNON_RANGED_DAMAGE = 1.0;
    private static final long CANNON_PROTECT_HINT_COOLDOWN_MS = 8_000L;

    private final Map<UUID, Integer> playerTeams = new HashMap<>();
    private final Map<UUID, Long> cannonProtectHintSent = new ConcurrentHashMap<>();
    private final Map<UUID, CombatAttribution> combatAttribution = new ConcurrentHashMap<>();
    private final Map<UUID, KillCause> pendingKillCauses = new ConcurrentHashMap<>();
    private final Set<UUID> voidEliminations = ConcurrentHashMap.newKeySet();
    private final Set<UUID> windDamageTicks = ConcurrentHashMap.newKeySet();

    private static final long ATTRIBUTION_MAX_AGE_TICKS = 20L * 15L;

    public enum KillCause {
        SWORD,
        CROSSBOW,
        CANNON
    }

    private record CombatAttribution(Player attacker, KillCause cause, long tick) {}
    private final Map<Integer, Set<UUID>> teams = new HashMap<>();
    private final Map<UUID, ItemStack> shieldsDisabled = new HashMap<>();
    private final Map<UUID, Scheduler.Task> shieldRestoreTasks = new HashMap<>();
    private Scheduler.Task regenTask;
    private MiniWarsCannonManager cannonManager;

    public MiniWarsEvent(EventType type, Category category, String mapId) {
        super(type, category, mapId);
        for (int i = 0; i < 4; i++) teams.put(i, new HashSet<>());
    }

    @Override
    protected void onGameStart() {
        startSlowRegen();
        assignTeamsForGameStart();

        for (UUID uuid : alivePlayers) {
            Integer team = playerTeams.get(uuid);
            if (team == null) continue;
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                giveKit(p);
                giveTeamKit(p, team);
                teleportToTeamSpawn(p, team);
            }
        }

        cannonManager = new MiniWarsCannonManager(this);
        cannonManager.spawnAll();

        for (UUID uuid : alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                Messages.send(p, "miniwars.cannon-protection-tip");
            }
        }
    }

    private void assignTeamsForGameStart() {
        teams.values().forEach(Set::clear);
        playerTeams.keySet().retainAll(alivePlayers);

        List<UUID> unassigned = new ArrayList<>();
        for (UUID uuid : alivePlayers) {
            Integer team = playerTeams.get(uuid);
            if (team != null && team >= 0 && team < TEAM_COUNT) {
                teams.get(team).add(uuid);
            } else {
                unassigned.add(uuid);
            }
        }

        Collections.shuffle(unassigned);
        for (UUID uuid : unassigned) {
            int team = smallestTeamIndex();
            playerTeams.put(uuid, team);
            teams.get(team).add(uuid);
        }
    }

    private int smallestTeamIndex() {
        int best = 0;
        int bestSize = teams.get(0).size();
        for (int i = 1; i < TEAM_COUNT; i++) {
            int size = teams.get(i).size();
            if (size < bestSize) {
                bestSize = size;
                best = i;
            }
        }
        return best;
    }

    public boolean isTeamSelectionAllowed() {
        EventState state = getEventManager().getState();
        return state == EventState.WAITING || state == EventState.STARTING;
    }

    public int getTeamSize(int team) {
        if (team < 0 || team >= TEAM_COUNT) return 0;
        return teams.get(team).size();
    }

    public List<Player> getTeamPlayers(int team) {
        if (team < 0 || team >= TEAM_COUNT) return List.of();
        List<Player> list = new ArrayList<>();
        for (UUID uuid : teams.get(team)) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                list.add(p);
            }
        }
        list.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    public Integer getPlayerTeamId(Player player) {
        return playerTeams.get(player.getUniqueId());
    }

    public static String teamColorCode(int team) {
        if (team < 0 || team >= TEAM_COLOR_CODES.length) return "§7";
        return TEAM_COLOR_CODES[team];
    }

    public boolean trySelectTeam(Player player, int team) {
        if (!isTeamSelectionAllowed()) return false;
        if (team < 0 || team >= TEAM_COUNT) return false;
        if (!getEventManager().isPlaying(player)) return false;

        UUID uuid = player.getUniqueId();
        Integer current = playerTeams.get(uuid);
        if (current != null && current == team) {
            return true;
        }

        boolean bypass = player.hasPermission(PERM_BYPASS_TEAM_LIMIT);
        if (!bypass && !wouldStayBalanced(team, uuid)) {
            return false;
        }

        if (current != null) {
            teams.get(current).remove(uuid);
        }
        playerTeams.put(uuid, team);
        teams.get(team).add(uuid);
        return true;
    }

    private boolean wouldStayBalanced(int targetTeam, UUID playerId) {
        int[] counts = new int[TEAM_COUNT];
        for (int i = 0; i < TEAM_COUNT; i++) {
            counts[i] = teams.get(i).size();
        }
        Integer current = playerTeams.get(playerId);
        if (current != null) {
            counts[current]--;
        }
        counts[targetTeam]++;
        int min = counts[0];
        int max = counts[0];
        for (int i = 1; i < TEAM_COUNT; i++) {
            min = Math.min(min, counts[i]);
            max = Math.max(max, counts[i]);
        }
        return max - min <= 1;
    }

    @Override
    public void onParticipantDeparted(Player player) {
        UUID uuid = player.getUniqueId();
        Integer team = playerTeams.remove(uuid);
        if (team != null) {
            teams.get(team).remove(uuid);
        }
    }

    public MiniWarsCannonManager getCannonManager() {
        return cannonManager;
    }

    public boolean isOnTeam(Player player, int team) {
        Integer assigned = playerTeams.get(player.getUniqueId());
        return assigned != null && assigned == team;
    }

    private void startSlowRegen() {
        if (regenTask != null) regenTask.cancel();
        regenTask = Scheduler.runTaskTimer(() -> {
            if (getEventManager().getState() != EventState.RUNNING) return;
            for (UUID uuid : alivePlayers) {
                Player p = Bukkit.getPlayer(uuid);
                if (p == null || !p.isOnline()) continue;
                double max = p.getMaxHealth();
                if (p.getHealth() < max) {
                    p.setHealth(Math.min(max, p.getHealth() + 0.5));
                }
                p.setFoodLevel(20);
                p.setSaturation(0f);
            }
        }, 40L, 40L);
    }

    private void teleportToTeamSpawn(Player player, int team) {
        Location teamSpawn = MapManager.loadTeamSpawn(category.getConfig(), mapId, team);
        if (teamSpawn != null) {
            player.teleport(teamSpawn, PlayerTeleportEvent.TeleportCause.PLUGIN);
            player.setFallDistance(0f);
        }
    }

    private void giveTeamKit(Player player, int team) {
        Color armorColor = TEAM_COLORS[team];
        player.getInventory().setHelmet(coloredArmor(Material.LEATHER_HELMET, armorColor));
        player.getInventory().setChestplate(coloredArmor(Material.LEATHER_CHESTPLATE, armorColor));
        player.getInventory().setLeggings(coloredArmor(Material.LEATHER_LEGGINGS, armorColor));
        player.getInventory().setBoots(coloredArmor(Material.LEATHER_BOOTS, armorColor));

        player.getInventory().setItem(0, woodenSword());
        for (int slot = 1; slot <= 5; slot++) {
            player.getInventory().setItem(slot, quickChargeCrossbow());
        }
        player.getInventory().setItem(27, new ItemStack(Material.ARROW, 64));
        player.getInventory().setItem(28, new ItemStack(Material.ARROW, 64));
        player.getInventory().setItem(29, new ItemStack(Material.ARROW, 64));
        player.getInventory().setItemInOffHand(teamShield(TEAM_DYES[team]));

        player.setFoodLevel(20);
        player.setSaturation(0f);

        player.sendMessage("§7Você está no time §l" + TEAM_NAMES[team] + "§7!");
    }

    private ItemStack woodenSword() {
        ItemStack sword = new ItemStack(Material.WOODEN_SWORD);
        ItemMeta meta = sword.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true);
            meta.addEnchant(Enchantment.SHARPNESS, 1, true);
            meta.addEnchant(Enchantment.SWEEPING_EDGE, 1, true);
            sword.setItemMeta(meta);
        }
        return sword;
    }

    private ItemStack quickChargeCrossbow() {
        ItemStack crossbow = new ItemStack(Material.CROSSBOW);
        CrossbowMeta meta = (CrossbowMeta) crossbow.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true);
            meta.addEnchant(Enchantment.QUICK_CHARGE, 1, true);
            crossbow.setItemMeta(meta);
        }
        return crossbow;
    }

    private ItemStack teamShield(DyeColor baseColor) {
        ItemStack shield = new ItemStack(Material.SHIELD);
        ShieldMeta meta = (ShieldMeta) shield.getItemMeta();
        if (meta != null) {
            meta.setBaseColor(baseColor);
            meta.addPattern(new Pattern(DyeColor.BLACK, snoutBannerPattern()));
            shield.setItemMeta(meta);
        }
        return shield;
    }

    private static PatternType snoutBannerPattern() {
        for (String name : List.of("PIGLIN", "SNOUT")) {
            try {
                return PatternType.valueOf(name);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return PatternType.SKULL;
    }

    private ItemStack coloredArmor(Material mat, Color color) {
        ItemStack item = new ItemStack(mat);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        if (meta != null) {
            meta.setColor(color);
            meta.setUnbreakable(true);
            meta.addEnchant(Enchantment.PROTECTION, 2, true);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    protected void onPlayerEliminated(Player player, Player killer, Location deathLocation) {
        broadcastKillMessage(player, killer);
        spawnEliminationFirework(player, deathLocation);
        cancelShieldDisable(player.getUniqueId());
        Integer team = playerTeams.get(player.getUniqueId());
        if (team != null) {
            teams.get(team).remove(player.getUniqueId());
            checkTeamWin();
        }
    }

    private void broadcastKillMessage(Player victim, Player killer) {
        if (killer == null || killer.equals(victim)) return;

        KillCause cause = pendingKillCauses.remove(victim.getUniqueId());
        boolean voidDeath = voidEliminations.remove(victim.getUniqueId());
        if (cause == null) {
            cause = KillCause.SWORD;
        }

        Integer victimTeam = playerTeams.get(victim.getUniqueId());
        Integer killerTeam = playerTeams.get(killer.getUniqueId());

        Component victimName = gradientPlayerName(victim, victimTeam);
        Component killerName = gradientPlayerName(killer, killerTeam);
        Component action = killActionText(cause, voidDeath);

        Component line = MINI.deserialize("<gray><bold>MINI-WARS</bold></gray> <dark_gray>|</dark_gray> ")
                .append(victimName)
                .append(action)
                .append(killerName);

        Messages.broadcastComponentToEventAudience(line);
    }

    private static Component killActionText(KillCause cause, boolean voidDeath) {
        if (voidDeath && cause == KillCause.CANNON) {
            return Component.text(" foi lançado ao void por ", NamedTextColor.GRAY);
        }
        return switch (cause) {
            case CANNON -> Component.text(" foi eliminado pelo canhão de ", NamedTextColor.GRAY);
            case CROSSBOW -> Component.text(" foi abatido por um tiro de besta de ", NamedTextColor.GRAY);
            case SWORD -> Component.text(" foi morto por ", NamedTextColor.GRAY);
        };
    }

    private void spawnEliminationFirework(Player victim, Location deathLocation) {
        Integer team = playerTeams.get(victim.getUniqueId());
        if (team == null || deathLocation == null) return;
        Color primary = TEAM_COLORS[team];
        Color secondary = primary.mixColors(Color.fromRGB(30, 30, 30));
        EventCelebration.spawnDeathFirework(deathLocation, primary, secondary);
    }

    private static Component gradientPlayerName(Player player, Integer team) {
        if (team == null || team < 0 || team >= TEAM_GRADIENT_HEX.length) {
            return Component.text(player.getName(), NamedTextColor.GRAY);
        }
        String[] hex = TEAM_GRADIENT_HEX[team];
        String safe = player.getName().replace("<", "\\<").replace(">", "\\>");
        return MINI.deserialize("<gradient:" + hex[0] + ":" + hex[1] + "><bold>" + safe + "</bold></gradient>");
    }

    @Override
    public List<String> extraScoreboardLines(Player viewer) {
        if (getEventManager().getState() != EventState.RUNNING) {
            return List.of();
        }
        List<String> lines = new ArrayList<>();
        for (int team = 0; team < TEAM_COUNT; team++) {
            int total = 0;
            int alive = 0;
            for (Map.Entry<UUID, Integer> entry : playerTeams.entrySet()) {
                if (entry.getValue() != team) continue;
                total++;
                if (alivePlayers.contains(entry.getKey())) {
                    alive++;
                }
            }
            if (total == 0) continue;
            lines.add(teamColorCode(team) + TEAM_NAMES[team] + " §8" + alive + "/" + total);
        }
        return lines;
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

    public void recordCombatAttribution(Player victim, Player attacker, KillCause cause) {
        if (victim == null || attacker == null || isSameTeam(attacker, victim)) return;
        if (!getEventManager().isPlaying(attacker) || !getEventManager().isPlaying(victim)) return;
        combatAttribution.put(victim.getUniqueId(), new CombatAttribution(attacker, cause, Bukkit.getCurrentTick()));
        pendingKillCauses.put(victim.getUniqueId(), cause);
    }

    public void markWindDamage(Player victim) {
        if (victim != null) {
            windDamageTicks.add(victim.getUniqueId());
        }
    }

    public boolean consumeWindDamageMark(Player victim) {
        return victim != null && windDamageTicks.remove(victim.getUniqueId());
    }

    public void eliminateFromVoid(Player player) {
        CombatAttribution attr = consumeCombatAttribution(player);
        if (attr != null && attr.attacker() != null && attr.attacker().isOnline()) {
            pendingKillCauses.put(player.getUniqueId(), attr.cause());
            if (attr.cause() == KillCause.CANNON) {
                voidEliminations.add(player.getUniqueId());
            }
            eliminatePlayer(player, attr.attacker());
        } else {
            eliminatePlayer(player);
        }
    }

    private CombatAttribution consumeCombatAttribution(Player victim) {
        CombatAttribution attr = combatAttribution.remove(victim.getUniqueId());
        if (attr == null) return null;
        if (Bukkit.getCurrentTick() - attr.tick() > ATTRIBUTION_MAX_AGE_TICKS) return null;
        if (!attr.attacker().isOnline()) return null;
        return attr;
    }

    /**
     * Reduz dano de flechas/bestas contra jogador em canhão ativo; espada e rajada do canhão de vento mantêm dano normal.
     */
    public void applyCannonSeatProtection(Player defender, EntityDamageByEntityEvent event, Player attacker) {
        if (cannonManager == null) return;
        if (cannonManager.findOperationalCannonRiddenBy(defender) == null) return;
        if (event.getDamager() instanceof Player) return;
        if (!isRangedArrowLikeProjectile(event.getDamager())) return;

        event.setDamage(CANNON_RANGED_DAMAGE);
        defender.playSound(defender.getLocation(), Sound.ENTITY_IRON_GOLEM_REPAIR, 0.9f, 1.15f);

        if (attacker != null) {
            long now = System.currentTimeMillis();
            Long last = cannonProtectHintSent.get(attacker.getUniqueId());
            if (last == null || now - last >= CANNON_PROTECT_HINT_COOLDOWN_MS) {
                cannonProtectHintSent.put(attacker.getUniqueId(), now);
                Messages.send(attacker, "miniwars.cannon-protected");
            }
        }
    }

    private static boolean isRangedArrowLikeProjectile(org.bukkit.entity.Entity damager) {
        if (!(damager instanceof Projectile projectile)) return false;
        if (projectile instanceof Snowball snowball
                && snowball.getPersistentDataContainer().has(TeamCannon.windProjectileKey(), PersistentDataType.BYTE)) {
            return false;
        }
        return projectile instanceof Arrow || projectile instanceof Trident;
    }

    public void onProjectileShieldHit(Player defender, EntityDamageByEntityEvent event) {
        ItemStack offhand = defender.getInventory().getItemInOffHand();
        if (offhand.getType() != Material.SHIELD) return;
        if (!(event.getDamager() instanceof Projectile)) return;
        if (!defender.isBlocking() && event.getFinalDamage() > 0) return;

        disableShield(defender, offhand);
    }

    private void disableShield(Player defender, ItemStack shield) {
        UUID uuid = defender.getUniqueId();
        Scheduler.Task pending = shieldRestoreTasks.remove(uuid);
        if (pending != null) pending.cancel();

        defender.clearActiveItem();
        shieldsDisabled.put(uuid, shield.clone());
        defender.getInventory().setItemInOffHand(null);
        defender.setCooldown(Material.SHIELD, SHIELD_DISABLE_TICKS);
        defender.playSound(defender.getLocation(), Sound.ITEM_SHIELD_BREAK, 1f, 0.9f);

        shieldRestoreTasks.put(uuid, Scheduler.runTaskLater(() -> restoreShield(defender), SHIELD_DISABLE_TICKS));
    }

    private void restoreShield(Player defender) {
        UUID uuid = defender.getUniqueId();
        shieldRestoreTasks.remove(uuid);
        ItemStack shield = shieldsDisabled.remove(uuid);
        if (shield == null || !defender.isOnline() || !alivePlayers.contains(uuid)) return;

        ItemStack offhand = defender.getInventory().getItemInOffHand();
        if (offhand == null || offhand.getType().isAir()) {
            defender.getInventory().setItemInOffHand(shield);
        } else {
            defender.getInventory().addItem(shield);
        }
    }

    private void cancelShieldDisable(UUID uuid) {
        shieldsDisabled.remove(uuid);
        Scheduler.Task task = shieldRestoreTasks.remove(uuid);
        if (task != null) task.cancel();
    }

    @Override
    protected void spawnVictoryFireworks(List<Player> winners) {
        if (winners.isEmpty() || winners.get(0) == null) return;

        Integer teamId = playerTeams.get(winners.get(0).getUniqueId());
        int team = teamId != null ? teamId : 0;
        Color primary = TEAM_COLORS[team];
        EventCelebration.spawnFinaleFireworks(this, primary,
                primary.mixColors(TEAM_COLORS[(team + 1) % TEAM_COLORS.length]));
    }

    @Override
    protected void announceVictory(List<Player> winners) {
        if (winners.isEmpty() || winners.get(0) == null) return;

        Integer teamId = playerTeams.get(winners.get(0).getUniqueId());
        int team = teamId != null ? teamId : 0;
        String roster = winners.stream()
                .map(Player::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse(winners.get(0).getName());

        Messages.broadcastEventAudience("event.team-winner", Map.of(
                "event", getEventName(),
                "team", TEAM_NAMES[team],
                "players", roster
        ));
        Messages.playToEventAudience(Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f);
    }

    @Override
    protected void onCleanUp() {
        if (regenTask != null) {
            regenTask.cancel();
            regenTask = null;
        }
        for (UUID uuid : new ArrayList<>(shieldRestoreTasks.keySet())) {
            cancelShieldDisable(uuid);
        }
        shieldsDisabled.clear();
        if (cannonManager != null) {
            cannonManager.despawnAll();
            cannonManager = null;
        }
        cannonProtectHintSent.clear();
        combatAttribution.clear();
        pendingKillCauses.clear();
        voidEliminations.clear();
        windDamageTicks.clear();
        playerTeams.clear();
        teams.values().forEach(Set::clear);
    }
}
