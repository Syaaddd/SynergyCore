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

public class NightfallSickle implements Listener {

    private static final String REAPING_CLAW_ITEM_NAME = ChatColor.RED + "Nightfall Sickle [✦]";
    private static final long DASH_COOLDOWN = 30 * 1000;
    private static final long STRENGTH_SLOWNESS_COOLDOWN = 40 * 1000;
    private static final long SNEAK_COOLDOWN = 50 * 1000;

    private HashMap<UUID, Long> dashCooldowns = new HashMap<>();
    private HashMap<UUID, Long> strengthSlownessCooldowns = new HashMap<>();
    private HashMap<UUID, Long> sneakCooldowns = new HashMap<>();

    // @EventHandler
    // public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
    //    Player player = event.getPlayer();

    //    if (player.isSneaking() && isReapingClawItem(player.getInventory().getItemInMainHand())) {
    //        if (!isOnCooldown(player, sneakCooldowns, SNEAK_COOLDOWN)) {
    //            applySneakDamageEffect(player);
    //            setCooldown(player, sneakCooldowns);
    //        }
    //    }
    //}

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (isReapingClawItem(item)) {
                if (!isOnCooldown(player, dashCooldowns, DASH_COOLDOWN)) {
                    dashToEnemy(player);
                    setCooldown(player, dashCooldowns);
                }
            }
        } //else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            //if (isReapingClawItem(item)) {
                //if (!isOnCooldown(player, strengthSlownessCooldowns, STRENGTH_SLOWNESS_COOLDOWN)) {
                    //applyStrengthAndSlowness(player);
                    //setCooldown(player, strengthSlownessCooldowns);
                //}
            //}
        //}
    }

    private boolean isReapingClawItem(ItemStack item) {
        if (item != null && item.getType() == Material.IRON_SWORD) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && ChatColor.stripColor(meta.getDisplayName()).equals("Nightfall Sickle [✦]")) {
                return true;
            }
        }
        return false;
    }

    private void applyPassiveDamageEffect(Player player) {
        double radius = 4.0;

        List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && entity != player && entity.getType() != EntityType.VILLAGER) {
                LivingEntity livingEntity = (LivingEntity) entity;
                livingEntity.damage(4.0);
            }
        }
    }

    private void applySneakDamageEffect(Player player) {
        // player.sendMessage(ChatColor.GREEN + "Unleashing Sneak Damage!");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CAT_HISS, 1.0f, 1.0f);

        Location playerLocation = player.getLocation();
        double radius = 15.0;

        List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && entity != player && entity.getType() != EntityType.VILLAGER) {
                LivingEntity livingEntity = (LivingEntity) entity;
                livingEntity.damage(10.0);
                playParticleEffect(livingEntity.getLocation());
            }
        }
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

    private void dashToEnemy(Player player) {
        player.sendMessage(ChatColor.GREEN + "Shadow Strike");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

        Location targetLocation = null;
        double radius = 10.0;
        List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && entity != player && entity.getType() != EntityType.VILLAGER) {
                targetLocation = entity.getLocation();
                ((LivingEntity) entity).damage(10.0);
                break;
            }
        }

        if (targetLocation != null) {
            player.teleport(targetLocation);
            player.getWorld().spawnParticle(Particle.CRIT_MAGIC, targetLocation, 10);
        }
    }

    private void applyStrengthAndSlowness(Player player) {
        // player.sendMessage(ChatColor.RED + "Unleashing Strength and Slowness!");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);

        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 30 * 20, 9), true);

        double radius = 10.0;
        List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && entity != player && entity.getType() != EntityType.VILLAGER) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10 * 20, 4), true);
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
