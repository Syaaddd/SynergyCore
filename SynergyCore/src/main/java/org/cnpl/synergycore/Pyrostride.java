package org.cnpl.synergycore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Pyrostride implements Listener {

    private final long cooldown = 20 * 1000;
    private final Set<UUID> cooldowns = new HashSet<>();
    private final JavaPlugin plugin;

    public Pyrostride(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (Bukkit.getPluginManager().isPluginEnabled("MMOItems")) {
            ItemStack boots = player.getInventory().getBoots();
            if (boots != null && boots.getType() != Material.AIR) {
                ItemMeta meta = boots.getItemMeta();
                if (meta != null && meta.hasDisplayName() &&
                        ChatColor.stripColor(meta.getDisplayName()).equals("Pyrostride Boots [✦]")) {
                    if (!cooldowns.contains(player.getUniqueId())) {
                        activateHellfireNova(player);
                    } else {
                        player.sendMessage(ChatColor.RED + "Skill is on cooldown.");
                    }
                }
            }
        }
    }

    private void activateHellfireNova(Player player) {
        cooldowns.add(player.getUniqueId());
        Location location = player.getLocation();

        player.sendMessage(ChatColor.GREEN + "Hellfire Nova activated!");

        location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

        spawnThickParticles(location, Color.fromRGB(255, 69, 0));

        for (Entity entity : player.getWorld().getNearbyEntities(location, 5, 5, 5)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity target = (LivingEntity) entity;
                target.damage(20, player);
                target.setFireTicks(400);
            }
        }

        new CooldownHandler(this, player).runTaskLater(plugin, cooldown / 50);
    }

    private void spawnThickParticles(Location location, Color color) {
        for (int i = 0; i < 5; i++) {
            location.getWorld().spawnParticle(
                    Particle.REDSTONE,
                    location,
                    100,
                    3.0,
                    3.0,
                    3.0,
                    new Particle.DustOptions(color, 1.0f)
            );
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (cooldowns.contains(player.getUniqueId())) {
                event.setDamage(event.getDamage() * 1.20);
            }
        }
    }

    private static class CooldownHandler extends BukkitRunnable {
        private final Pyrostride plugin;
        private final Player player;

        CooldownHandler(Pyrostride plugin, Player player) {
            this.plugin = plugin;
            this.player = player;
        }

        @Override
        public void run() {
            plugin.cooldowns.remove(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Hellfire Nova is ready to use again.");
        }
    }
}
