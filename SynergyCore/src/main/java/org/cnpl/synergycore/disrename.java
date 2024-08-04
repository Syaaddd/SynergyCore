package org.cnpl.synergycore;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

public class disrename implements Listener {
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack result = event.getResult();
        if (result != null && result.hasItemMeta()) {
            String itemName = result.getItemMeta().getDisplayName();
            if ("Spectral Chestplate [✦]".equals(itemName)) {
                event.setResult(null);
                event.getView().getPlayer().sendMessage(ChatColor.RED + "You cannot rename items to 'Spectral Chestplate [✦]'.");
            }
            if ("Wraith Grip [✦]".equals(itemName)) {
                event.setResult(null);
                event.getView().getPlayer().sendMessage(ChatColor.RED + "You cannot rename items to 'Wraith Grip [✦]'.");
            }
            if ("Angelic Arrow [✦]".equals(itemName)) {
                event.setResult(null);
                event.getView().getPlayer().sendMessage(ChatColor.RED + "You cannot rename items to 'Angelic Arrow [✦]'.");
            }
            if ("Pyrostride Boots [✦]".equals(itemName)) {
                event.setResult(null);
                event.getView().getPlayer().sendMessage(ChatColor.RED + "You cannot rename items to 'Pyrostride Boots [✦]'.");
            }
            if ("&b&lLeviathan".equals(itemName)) {
                event.setResult(null);
                event.getView().getPlayer().sendMessage(ChatColor.RED + "You cannot rename items to 'Pyrostride Boots [✦]'.");
            }
            if ("OCEAN'S Totem [✦]".equals(itemName)) {
                event.setResult(null);
                event.getView().getPlayer().sendMessage(ChatColor.RED + "You cannot rename items to 'Pyrostride Boots [✦]'.");
            }
            if ("Nightfall Sickle [✦]".equals(itemName)) {
                event.setResult(null);
                event.getView().getPlayer().sendMessage(ChatColor.RED + "You cannot rename items to 'Pyrostride Boots [✦]'.");
            }
        }
    }
}
