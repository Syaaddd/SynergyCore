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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ReapingClaw implements Listener {

    private static final String REAPING_CLAW_ITEM_NAME = ChatColor.RED + "Wraith Grip [✦]";
    private static final long REAPING_CLAW_COOLDOWN = 14 * 1000;
    private static final long EXPLOSION_COOLDOWN = 20 * 1000;
    private static final long BLINDNESS_REGEN_COOLDOWN = 10 * 1000;

    private HashMap<UUID, Long> reapingClawCooldowns = new HashMap<>();
    private HashMap<UUID, Long> explosionCooldowns = new HashMap<>();
    private HashMap<UUID, Long> blindnessRegenCooldowns = new HashMap<>();

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (player.isSneaking() && isReapingClawItem(player.getInventory().getItemInMainHand())) {
            if (!isOnCooldown(player, reapingClawCooldowns, REAPING_CLAW_COOLDOWN)) {
                summonReapingClaw(player);
                setCooldown(player, reapingClawCooldowns);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (isReapingClawItem(item)) {
                if (!isOnCooldown(player, explosionCooldowns, EXPLOSION_COOLDOWN)) {
                    summonExplosionEffect(player);
                    setCooldown(player, explosionCooldowns);
                }
            }
        } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (isReapingClawItem(item)) {
                if (!isOnCooldown(player, blindnessRegenCooldowns, BLINDNESS_REGEN_COOLDOWN)) {
                    applyBlindnessAndRegen(player);
                    setCooldown(player, blindnessRegenCooldowns);
                }
            }
        }
    }

    private boolean isReapingClawItem(ItemStack item) {
        if (item != null && item.getType() == Material.IRON_SWORD) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && ChatColor.stripColor(meta.getDisplayName()).equals("Wraith Grip [✦]")) {
                return true;
            }
        }
        return false;
    }

    private void summonReapingClaw(Player player) {
        player.sendMessage(ChatColor.RED + "Summoning Reaping Claw!");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CAT_HISS, 1.0f, 1.0f);

        Location playerLocation = player.getLocation();
        double radius = 15.0;

        List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && entity != player && entity.getType() != EntityType.VILLAGER) {
                LivingEntity livingEntity = (LivingEntity) entity;
                livingEntity.damage(10.0);
                pullEntityTowardsLocation(livingEntity, playerLocation);
                applySlowEffect(livingEntity);
                playParticleEffect(livingEntity.getLocation());
            }
        }
    }

    private void pullEntityTowardsLocation(LivingEntity entity, Location location) {
        Vector direction = location.toVector().subtract(entity.getLocation().toVector()).normalize();
        double pullStrength = 1.0;
        entity.setVelocity(direction.multiply(pullStrength));
    }

    private void applySlowEffect(LivingEntity entity) {
        int duration = 10 * 20;
        int amplifier = 4;
        entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, amplifier));
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
                location.getWorld().spawnParticle(Particle.CRIT_MAGIC, particleLocation, 1, 0, 0, 0, 0);
                particleLocation.subtract(x, y, z);
            }
        }
    }

    private void summonExplosionEffect(Player player) {
        player.sendMessage(ChatColor.RED + "Unleashing Explosion!");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CAT_HISS, 1.0f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);

        Location playerLocation = player.getLocation();
        double radius = 15.0;

        List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && entity.getType() != EntityType.VILLAGER) {
                ((LivingEntity) entity).damage(7.0);
                player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, entity.getLocation(), 1);
                player.getWorld().strikeLightningEffect(entity.getLocation());
            }
        }
    }

    private void applyBlindnessAndRegen(Player player) {
        player.sendMessage(ChatColor.RED + "Using Twilight Revival!");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CAT_HISS, 1.0f, 1.0f);

        int blindnessDuration = 5 * 20;
        int blindnessRadius = 5;
        for (Entity entity : player.getNearbyEntities(blindnessRadius, blindnessRadius, blindnessRadius)) {
            if (entity instanceof Player && entity != player && entity.getType() != EntityType.VILLAGER) {
                Player targetPlayer = (Player) entity;
                targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blindnessDuration, 1), true);
            }
        }

        int regenDuration = 6 * 20;
        int regenAmplifier = 0;
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regenDuration, regenAmplifier), true);
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
