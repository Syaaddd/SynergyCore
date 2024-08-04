package org.cnpl.synergycore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class LeviathanArmor extends JavaPlugin implements Listener {

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        if (isWearingLeviathanArmor(player)) {
            Random random = new Random();
            if (random.nextDouble() < 0.20) { // 20% chance
                Entity entity = event.getEntity();
                if (entity instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) entity;
                    target.damage(15);
                    player.sendMessage(ChatColor.AQUA + "Abyssal Strike activated!");
                }
            }
        }
    }

    private boolean isWearingLeviathanArmor(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chestplate = player.getInventory().getChestplate();
        ItemStack leggings = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();

        return isLeviathanItem(helmet, "Leviathan's Visage") &&
                isLeviathanItem(chestplate, "Leviathan's Chestguard") &&
                isLeviathanItem(leggings, "Leviathan's Greaves") &&
                isLeviathanItem(boots, "Leviathan's Treads");
    }

    private boolean isLeviathanItem(ItemStack item, String name) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', name));
    }
}
