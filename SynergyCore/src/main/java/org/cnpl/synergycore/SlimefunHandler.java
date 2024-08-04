package org.cnpl.synergycore;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class SlimefunHandler implements Listener {

    @EventHandler
    public void onInventoryClick3(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (event.getClick() == ClickType.NUMBER_KEY) {
            int hotbarButton = event.getHotbarButton();

            // Check if the player is using a keyboard shortcut (e.g., 1-9 keys)
            if (hotbarButton >= 0 && hotbarButton <= 8) {
                ItemStack clickedItem = player.getInventory().getItem(hotbarButton);

                // Check if the clicked item is not null
                if (clickedItem != null) {
                    String inventoryTitle = ChatColor.stripColor(event.getView().getTitle());

                    // Check if the player is opening a GUI with the specified names
                    if (inventoryTitle.contains("Auto Enchanter") || inventoryTitle.contains("Auto Disenchanter")) {

                        // Check if the clicked item has lore containing the specified strings
                        if (hasCommonLore(clickedItem) || hasRareLore(clickedItem) || hasEpicLore(clickedItem) ||
                                hasLegendaryLore(clickedItem) || hasBossDropLore(clickedItem) || hasLimitedLore(clickedItem) ||
                                hasDungeonLore(clickedItem) || hasSpecialLore(clickedItem) || hasfishingLore(clickedItem) ||
                                hasfishing2Lore(clickedItem) || hascharmLore(clickedItem) || hasmiscLore(clickedItem)) {
                            event.setCancelled(true);
                            player.sendMessage(ChatColor.RED + "You cannot move this item into the GUI!");
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedInventory == null || clickedItem == null || !clickedInventory.equals(player.getInventory())) {
            return;
        }

        String inventoryTitle = ChatColor.stripColor(event.getView().getTitle());

        // Check if the player is opening a GUI with the specified names
        if (inventoryTitle.contains("Auto Enchanter") || inventoryTitle.contains("Auto Disenchanter")) {

            // Check if the clicked item has lore containing the specified strings
            if (hasCommonLore(clickedItem) || hasRareLore(clickedItem) || hasEpicLore(clickedItem) ||
                    hasLegendaryLore(clickedItem) || hasBossDropLore(clickedItem) || hasLimitedLore(clickedItem) ||
                    hasDungeonLore(clickedItem) || hasSpecialLore(clickedItem) || hasfishingLore(clickedItem) ||
                    hasfishing2Lore(clickedItem) || hascharmLore(clickedItem) || hasmiscLore(clickedItem)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot interact with this item!");
            }
        }
    }

    @EventHandler
    public void onInventoryClick2(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedInventory == null || clickedItem == null || clickedInventory.equals(player.getInventory())) {
            return;
        }

        // Check if the player is moving items using the keyboard into a GUI with the specified names
        if (event.getAction().name().contains("PLACE_ALL") && event.getSlotType() == InventoryType.SlotType.CONTAINER) {
            if (event.getView().getTitle().contains("Auto Enchanter") || event.getView().getTitle().contains("Auto Disenchanter")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot move items into this GUI!");
            }
        }
    }

    // Helper methods to check lore
    private boolean hasCommonLore(ItemStack item) {
        return hasLoreContaining(item, "COMMON");
    }

    private boolean hasRareLore(ItemStack item) {
        return hasLoreContaining(item, "RARE");
    }

    private boolean hasEpicLore(ItemStack item) {
        return hasLoreContaining(item, "EPIC");
    }

    private boolean hasLegendaryLore(ItemStack item) {
        return hasLoreContaining(item, "LEGENDARY");
    }

    private boolean hasBossDropLore(ItemStack item) {
        return hasLoreContaining(item, "BOSS DROP");
    }

    private boolean hasLimitedLore(ItemStack item) {
        return hasLoreContaining(item, "LIMITED");
    }

    private boolean hasDungeonLore(ItemStack item) {
        return hasLoreContaining(item, "DUNGEON");
    }

    private boolean hasSpecialLore(ItemStack item) {
        return hasLoreContaining(item, "SPECIAL");
    }

    private boolean hasfishingLore(ItemStack item) {
        return hasLoreContaining(item, "FISHING ARMOR");
    }

    private boolean hasfishing2Lore(ItemStack item) {
        return hasLoreContaining(item, "Fishing Material");
    }

    private boolean hascharmLore(ItemStack item) {
        return hasLoreContaining(item, "Charm");
    }

    private boolean hasmiscLore(ItemStack item) {
        return hasLoreContaining(item, "Misc. Item");
    }

    private boolean hasLoreContaining(ItemStack item, String lore) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return false;
        }

        ItemMeta itemMeta = item.getItemMeta();
        List<String> itemLore = itemMeta.getLore();

        if (itemLore == null) {
            return false;
        }

        for (String line : itemLore) {
            if (ChatColor.stripColor(line).contains(lore)) {
                return true;
            }
        }

        return false;
    }
}