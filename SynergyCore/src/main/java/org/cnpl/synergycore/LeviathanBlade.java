package org.cnpl.synergycore;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class LeviathanBlade implements Listener {

    private static final String LEVIATHAN_BLADE_ITEM_NAME = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Leviathan Blade of Silence " + ChatColor.GRAY + "[" + ChatColor.YELLOW + "" + ChatColor.BOLD + "✦" + ChatColor.GRAY + "]";
    private static final String LEVIATHAN_BLADE_ITEM_NAME_ALTERNATE = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Leviathan Blade of Wrath " + ChatColor.GRAY + "[" + ChatColor.YELLOW + "" + ChatColor.BOLD + "✦" + ChatColor.GRAY + "]";
    private static final long OCEANIC_SLASH_COOLDOWN = 15 * 1000;
    private static final long TSUNAMI_STRIKE_COOLDOWN = 30 * 1000;
    private static final long NAME_SWITCH_COOLDOWN = 10 * 1000;  // Cooldown untuk mengganti nama

    private final HashMap<UUID, Long> oceanicSlashCooldowns = new HashMap<>();
    private final HashMap<UUID, Long> tsunamiStrikeCooldowns = new HashMap<>();
    private final HashMap<UUID, Long> nameSwitchCooldowns = new HashMap<>();
    private final HashMap<UUID, Integer> consecutiveHits = new HashMap<>();

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (player.isSneaking() && isLeviathanBladeItem(item)) {
            if (itemHasName(item, LEVIATHAN_BLADE_ITEM_NAME_ALTERNATE)) {
                if (!isOnCooldown(player, tsunamiStrikeCooldowns, TSUNAMI_STRIKE_COOLDOWN)) {
                    summonTsunamiStrike(player);
                    setCooldown(player, tsunamiStrikeCooldowns);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (isLeviathanBladeItem(item)) {
            if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (itemHasName(item, LEVIATHAN_BLADE_ITEM_NAME)) {
                    if (!isOnCooldown(player, oceanicSlashCooldowns, OCEANIC_SLASH_COOLDOWN)) {
                        summonOceanicSlash(player);
                        setCooldown(player, oceanicSlashCooldowns);
                    }
                }
            } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (!isOnCooldown(player, nameSwitchCooldowns, NAME_SWITCH_COOLDOWN)) {
                    toggleItemDisplayName(item, player);
                    setCooldown(player, nameSwitchCooldowns);
                } else {
                    player.sendMessage(ChatColor.RED + "You cannot switch the mode yet. Please wait for the cooldown.");
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            ItemStack item = player.getInventory().getItemInMainHand();

            if (isLeviathanBladeItem(item)) {
                UUID playerId = player.getUniqueId();
                consecutiveHits.put(playerId, consecutiveHits.getOrDefault(playerId, 0) + 1);

                if (consecutiveHits.get(playerId) >= 5) {
                    event.setDamage(event.getDamage() * 1.15);  // Tambahkan 15% kerusakan
                    consecutiveHits.put(playerId, 0);  // Reset hit counter
                }   
            }
        }
    }

    private boolean isLeviathanBladeItem(ItemStack item) {
        if (item != null && item.getType() == Material.IRON_SWORD) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String displayName = ChatColor.stripColor(meta.getDisplayName());
                return displayName.equals(ChatColor.stripColor(LEVIATHAN_BLADE_ITEM_NAME)) || displayName.equals(ChatColor.stripColor(LEVIATHAN_BLADE_ITEM_NAME_ALTERNATE));
            }
        }
        return false;
    }

    private boolean itemHasName(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && ChatColor.stripColor(meta.getDisplayName()).equals(ChatColor.stripColor(name));
    }

    private void toggleItemDisplayName(ItemStack item, Player player) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String currentDisplayName = meta.getDisplayName();
            if (ChatColor.stripColor(currentDisplayName).equals(ChatColor.stripColor(LEVIATHAN_BLADE_ITEM_NAME))) {
                meta.setDisplayName(LEVIATHAN_BLADE_ITEM_NAME_ALTERNATE);
                updateItemLore(meta, "&7Mode: &6Blade &e&lAwaken");
                player.sendMessage(ChatColor.GREEN + "Leviathan Blade Switch Mode!");
            } else {
                meta.setDisplayName(LEVIATHAN_BLADE_ITEM_NAME);
                updateItemLore(meta, "&7Mode: &6Blade &e&lSleep");
                player.sendMessage(ChatColor.GREEN + "Leviathan Blade Switch Mode!");
            }
            item.setItemMeta(meta);
        }
    }

    private void updateItemLore(ItemMeta meta, String modeSwitchText) {
        List<String> lore = meta.getLore();
        if (lore != null) {
            for (int i = 0; i < lore.size(); i++) {
                if (ChatColor.stripColor(lore.get(i)).contains("Mode:")) {
                    lore.set(i, ChatColor.translateAlternateColorCodes('&', modeSwitchText));
                    break;
                }
            }
        } else {
            lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', modeSwitchText));
        }
        meta.setLore(lore);
    }

    private void summonOceanicSlash(Player player) {
        player.sendMessage(ChatColor.BLUE + "Activating Oceanic Slash!");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);

        Location playerLocation = player.getLocation();
        Vector direction = playerLocation.getDirection().normalize();
        double range = 3.0;

        for (double i = 1; i <= range; i += 0.5) {
            Location waveLocation = playerLocation.clone().add(direction.clone().multiply(i));

            for (Entity entity : player.getWorld().getNearbyEntities(waveLocation, 1, 1, 1)) {
                if (entity instanceof LivingEntity && entity != player) {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    livingEntity.damage(20.0);
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 * 20, 1));

                    // Apply particle effect directly to the hit entity
                    player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, livingEntity.getLocation(), 10, 0.2, 0.2, 0.2, 0.1);
                }
            }
        }
    }

    private void summonTsunamiStrike(Player player) {
        player.sendMessage(ChatColor.BLUE + "Activating Tsunami Strike!");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_SPLASH, 1.0f, 1.0f);

        Location playerLocation = player.getLocation();
        double radius = 10.0;

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity livingEntity = (LivingEntity) entity;
                livingEntity.damage(10.0);
                Vector knockback = livingEntity.getLocation().toVector().subtract(playerLocation.toVector()).normalize().multiply(1.5);
                livingEntity.setVelocity(knockback);
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 1));

                // Apply water burst effect from below and upwards
                Location burstLocation = livingEntity.getLocation();
                burstLocation.setY(burstLocation.getY() - 1);  // Adjust the location to burst from below
                for (int j = 0; j < 5; j++) {
                    player.getWorld().spawnParticle(Particle.WATER_SPLASH, burstLocation, 20, 0.5, 0.5 + (j * 0.2), 0.5, 0.2);
                }
            }
        }
    }

    private boolean isOnCooldown(Player player, HashMap<UUID, Long> cooldowns, long cooldownTime) {
        UUID playerId = player.getUniqueId();
        if (cooldowns.containsKey(playerId)) {
            long lastUseTime = cooldowns.get(playerId);
            return System.currentTimeMillis() - lastUseTime < cooldownTime;
        }
        return false;
    }

    private void setCooldown(Player player, HashMap<UUID, Long> cooldowns) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
}
