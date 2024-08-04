package org.cnpl.synergycore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SynergyCore extends JavaPlugin implements Listener {

    private final long cooldown = 60 * 1000;
    private final Set<UUID> cooldowns = new HashSet<>();
    private final Set<UUID> activeSpirits = new HashSet<>();

    @Override
    public void onEnable() {
        getLogger().info("Plugin SynergyCore telah diaktifkan!");
        this.getCommand("leviathanspawn").setExecutor(new LeviathanSpawnCommand());
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new ReapingClaw(), this);
        getServer().getPluginManager().registerEvents(new disrename(), this);
        getServer().getPluginManager().registerEvents(new AngelicArrow(), this);
        getServer().getPluginManager().registerEvents(new Pyrostride(this), this);
        getServer().getPluginManager().registerEvents(new SpawnListener(), this);
        getServer().getPluginManager().registerEvents(new OCEANSTotem(), this);
        getServer().getPluginManager().registerEvents(new NightfallSickle(), this);
        getServer().getPluginManager().registerEvents(new LeviathanBlade(), this);
        getServer().getPluginManager().registerEvents(new SlimefunHandler(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin SynergyCore telah dinonaktifkan!");
    }

    // Command
    public class LeviathanSpawnCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender.hasPermission("leviathanspawn.use")) {
                Bukkit.broadcastMessage("");
                Bukkit.broadcastMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Leviathan " + ChatColor.WHITE + "berhasil dipancing");
                Bukkit.broadcastMessage("");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_ALLAY_DEATH, 100f, 1f);
                }
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return false;
            }
        }
    }
    // end command

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (Bukkit.getPluginManager().isPluginEnabled("MMOItems")) {
            ItemStack chestplate = player.getInventory().getChestplate();
            if (chestplate != null && chestplate.getType() != Material.AIR) {
                ItemMeta meta = chestplate.getItemMeta();
                if (meta != null && meta.hasDisplayName() &&
                        ChatColor.stripColor(meta.getDisplayName()).equals("Spectral Chestplate [✦]")) {
                    if (!cooldowns.contains(player.getUniqueId())) {
                        activatePhantomRequiem(player);
                    } else {
                        player.sendMessage(ChatColor.RED + "Skill is on cooldown.");
                    }
                }
            }
        }
    }

    private void activatePhantomRequiem(Player player) {
        activeSpirits.add(player.getUniqueId());
        cooldowns.add(player.getUniqueId());
        Location location = player.getLocation();

        player.sendMessage(ChatColor.GREEN + "Phantom Requiem activated!");
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 20, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 20 * 20, 0));

        location.getWorld().playSound(location, Sound.ENTITY_WITHER_DEATH, 1.0f, 1.0f);

        new PhantomRequiemTask(this, player, location).runTaskTimer(this, 0, 40); // Jalankan setiap 2 detik

        new CooldownHandler(this, player).runTaskLater(this, cooldown / 50);
    }

    private void spawnThickParticles(Location location, Color color) {
        for (int i = 0; i < 5; i++) {
            location.getWorld().spawnParticle(
                    Particle.REDSTONE,
                    location,
                    10,
                    1.0,
                    1.0,
                    1.0,
                    new Particle.DustOptions(color, 1.0f)
            );
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (activeSpirits.contains(player.getUniqueId())) {
                event.setDamage(event.getDamage() * 1.15);
            }
        }
    }

    private static class PhantomRequiemTask extends BukkitRunnable {
        private final SynergyCore plugin;
        private final Player player;
        private final Location origin;
        private int count = 0;
        private static final double SKILL_RADIUS = 7.0;

        PhantomRequiemTask(SynergyCore plugin, Player player, Location origin) {
            this.plugin = plugin;
            this.player = player;
            this.origin = origin;
        }

        @Override
        public void run() {
            if (count >= 20 || !plugin.activeSpirits.contains(player.getUniqueId())) {
                plugin.activeSpirits.remove(player.getUniqueId());
                cancel();
                return;
            }

            if (!player.getWorld().equals(origin.getWorld()) || player.getLocation().distance(origin) > SKILL_RADIUS) {
                player.sendMessage(ChatColor.RED + "You moved too far from the skill's origin point!");
                plugin.activeSpirits.remove(player.getUniqueId());
                cancel();
                return;
            }

            double damage = getWeaponDamage(player);

            for (Entity entity : origin.getWorld().getNearbyEntities(origin, 10, 10, 10)) {
                if (entity instanceof LivingEntity && entity != player) {
                    LivingEntity target = (LivingEntity) entity;
                    target.damage(damage, player);
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));

                    plugin.spawnThickParticles(target.getLocation(), Color.fromRGB(0, 0, 255));
                }
            }

            count++;
        }

        private double getWeaponDamage(Player player) {
            ItemStack weapon = player.getInventory().getItemInMainHand();
            if (weapon == null || weapon.getType() == Material.AIR) {
                return 10.0; //
            }
            switch (weapon.getType()) {
                case WOODEN_SWORD:
                case STONE_SWORD:
                case IRON_SWORD:
                case DIAMOND_SWORD:
                case NETHERITE_SWORD:
                case WOODEN_AXE:
                case STONE_AXE:
                case IRON_AXE:
                case DIAMOND_AXE:
                case NETHERITE_AXE:
                    return 20.0;
                default:
                    return 10.0;
            }
        }
    }

    private static class CooldownHandler extends BukkitRunnable {
        private final SynergyCore plugin;
        private final Player player;

        CooldownHandler(SynergyCore plugin, Player player) {
            this.plugin = plugin;
            this.player = player;
        }

        @Override
        public void run() {
            plugin.cooldowns.remove(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Phantom Requiem is ready to use again.");
        }
    }
}
