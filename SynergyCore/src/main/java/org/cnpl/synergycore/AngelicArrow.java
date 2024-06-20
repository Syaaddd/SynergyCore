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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.bukkit.plugin.java.JavaPlugin;

public class AngelicArrow implements Listener {

    private static final String ANGELIC_ARROW_ITEM_NAME = ChatColor.GOLD + "Angelic Arrow [✦]";
    private static final long ANGELIC_ARROW_COOLDOWN = 60 * 1000;
    private static final long SNEAK_SKILL_COOLDOWN = 30 * 1000;

    private HashMap<UUID, Long> angelicArrowCooldowns = new HashMap<>();
    private HashMap<UUID, Long> sneakSkillCooldowns = new HashMap<>();
    private HashMap<UUID, BukkitRunnable> activeParticles = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (isAngelicArrowItem(item)) {
                if (!isOnCooldown(player, angelicArrowCooldowns, ANGELIC_ARROW_COOLDOWN)) {
                    applyRegenAndStrength(player);
                    setCooldown(player, angelicArrowCooldowns);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (player.isSneaking() && isAngelicArrowItem(player.getInventory().getItemInMainHand())) {
            if (!isOnCooldown(player, sneakSkillCooldowns, SNEAK_SKILL_COOLDOWN)) {
                applyGlowAndBlind(player);
                setCooldown(player, sneakSkillCooldowns);
            }
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());

        if (isAngelicArrowItem(item)) {
            startParticleEffect(player);
        } else {
            stopParticleEffect(player);
        }
    }

    private boolean isAngelicArrowItem(ItemStack item) {
        if (item != null && item.getType() == Material.BOW) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && ChatColor.stripColor(meta.getDisplayName()).equals("Angelic Arrow [✦]")) {
                return true;
            }
        }
        return false;
    }

    private void applyRegenAndStrength(Player player) {
        player.sendMessage(ChatColor.GOLD + "Using Skill Divine Empowerment!");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.TOTEM, player.getLocation(), 30);

        int regenDuration = 50 * 20;
        int regenAmplifier = 0;
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regenDuration, regenAmplifier), true);

        int strengthDuration = 50 * 20;
        int strengthAmplifier = 4;
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, strengthDuration, strengthAmplifier), true);
    }

    private void applyGlowAndBlind(Player player) {
        player.sendMessage(ChatColor.GOLD + "Using Skill Heavenly Might!");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);

        Location playerLocation = player.getLocation();
        double radius = 30.0;

        List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity livingEntity = (LivingEntity) entity;
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 10 * 20, 1), true); // Efek Glow selama 10 detik
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10 * 20, 1), true); // Efek Blind selama 10 detik
            }
        }
    }

    private void startParticleEffect(Player player) {
        stopParticleEffect(player);

        BukkitRunnable particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isAngelicArrowItem(player.getInventory().getItemInMainHand())) {
                    cancel();
                    return;
                }

                Location loc = player.getLocation();
                loc.subtract(0, 1, 0);
                for (double t = 0; t < Math.PI * 2; t += Math.PI / 16) {
                    double x = 0.5 * Math.cos(t);
                    double z = 0.5 * Math.sin(t);
                    loc.add(x, 0, z);
                    player.getWorld().spawnParticle(Particle.SPELL_MOB, loc, 0, 1, 1, 1, 1);
                    loc.subtract(x, 0, z);
                }
                loc.add(0, 1, 0);
            }
        };
        particleTask.runTaskTimer(JavaPlugin.getPlugin(SynergyCore.class), 0, 5);
        activeParticles.put(player.getUniqueId(), particleTask);
    }

    private void stopParticleEffect(Player player) {
        UUID playerId = player.getUniqueId();
        if (activeParticles.containsKey(playerId)) {
            activeParticles.get(playerId).cancel();
            activeParticles.remove(playerId);
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
