package org.cnpl.synergycore;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class OCEANSTotem implements Listener {

    private static final String LEVIATHANS_GRIP_ITEM_NAME = "OCEAN'S Totem [✦]";
    private static final String AQUA_SHIELD_ITEM_NAME = "OCEAN'S Totem [✦]";
    private static final long LEVIATHANS_GRIP_COOLDOWN = 5 * 1000;
    private static final long AQUA_SHIELD_COOLDOWN = 60 * 1000;
    private static final double MINIMUM_DAMAGE_TO_ACTIVATE = 5.0;
    private static final int AQUA_SHIELD_ABSORB_AMOUNT = 3;
    private static final int AQUA_SHIELD_DURATION = 10 * 20;

    private HashMap<UUID, Long> leviathansGripCooldowns = new HashMap<>();
    private HashMap<UUID, Long> aquaShieldCooldowns = new HashMap<>();

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        // Activate Leviathan's Grip
        if (event.getFinalDamage() >= MINIMUM_DAMAGE_TO_ACTIVATE && isLeviathansGripItem(player.getInventory().getItemInOffHand())) {
            if (!isOnCooldown(player, leviathansGripCooldowns, LEVIATHANS_GRIP_COOLDOWN)) {
                summonLeviathansGrip(player);
                setCooldown(player, leviathansGripCooldowns);
            }
        }

        // Activate Aqua Shield
        if (isAquaShieldItem(player.getInventory().getItemInOffHand())) {
            if (!isOnCooldown(player, aquaShieldCooldowns, AQUA_SHIELD_COOLDOWN)) {
                applyAquaShield(player);
                setCooldown(player, aquaShieldCooldowns);
            }
        }
    }

    private boolean isLeviathansGripItem(ItemStack item) {
        if (item != null && item.getType() == Material.PLAYER_HEAD) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && ChatColor.stripColor(meta.getDisplayName()).equals(LEVIATHANS_GRIP_ITEM_NAME)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAquaShieldItem(ItemStack item) {
        if (item != null && item.getType() == Material.PLAYER_HEAD) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && ChatColor.stripColor(meta.getDisplayName()).equals(AQUA_SHIELD_ITEM_NAME)) {
                return true;
            }
        }
        return false;
    }

    private void summonLeviathansGrip(Player player) {
        player.sendMessage(ChatColor.GREEN + "Summoning Leviathan's Grip!");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CAT_HISS, 1.0f, 1.0f);

        Location playerLocation = player.getLocation();
        double radius = 5.0;

        List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && entity != player && entity.getType() != EntityType.VILLAGER) {
                LivingEntity livingEntity = (LivingEntity) entity;
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10 * 20, 2));
                playParticleEffect(livingEntity.getLocation());
            }
        }
    }

    private void applyAquaShield(Player player) {
        player.sendMessage(ChatColor.GREEN + "Creating Aqua Shield!");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DOLPHIN_AMBIENT_WATER, 1.0f, 1.0f);

        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, AQUA_SHIELD_DURATION, AQUA_SHIELD_ABSORB_AMOUNT - 1));
        playParticleEffect(player.getLocation());
    }

    private void playParticleEffect(Location location) {
        Location particleLocation = location.clone().subtract(0, 0.5, 0);
        double radius = 0.6;
        double height = 0.7;
        int particleCount = 15;

        for (double y = 0; y < height; y += 0.05) {
            for (int i = 0; i < particleCount; i++) {
                double angle = 2 * Math.PI * i / particleCount;
                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);
                particleLocation.add(x, y, z);
                location.getWorld().spawnParticle(Particle.WATER_BUBBLE, particleLocation, 1, 0, 0, 0, 0);
                particleLocation.subtract(x, y, z);
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
