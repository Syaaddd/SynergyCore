package org.cnpl.synergycore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.*;
import java.util.stream.Collectors;

public class SpawnListener implements Listener {

    private final Map<String, Double> leaderboard = new HashMap<>();
    private List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>();

    public void onPlayerUseNameTag(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.NAME_TAG) {
            String nameTag = item.getItemMeta().getDisplayName();
            if (ChatColor.stripColor(nameTag).equalsIgnoreCase("Leviathan")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot name a mob '&b&lLeviathan'");
            }
        }
    }

    @EventHandler
    public void onLeviathanDeath(EntityDeathEvent event) {
        if (event.getEntity().getCustomName() != null && event.getEntity().getCustomName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Leviathan")) {
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Leviathan " + ChatColor.WHITE + "berhasil dijinakkan");
            Bukkit.broadcastMessage("");
            displayLeaderboard();
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 100f, 1f);
            }
            leaderboard.clear();
            giveRewards();
        }
    }

    @EventHandler
    public void onPlayerHitLeviathan(EntityDamageByEntityEvent event) {
        if (event.getEntity() == null || event.getDamager() == null) {
            return; // Check for null to avoid NullPointerException
        }

        if (event.getEntity().getCustomName() != null && event.getEntity().getCustomName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Leviathan")) {
            if (event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                increasePlayerScore(player.getName(), event.getDamage()); // Increase score by damage on hitting Leviathan
            } else if (event.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) event.getDamager();

                if (projectile.getShooter() instanceof Player) {
                    Player player = (Player) projectile.getShooter();
                    increasePlayerScore(player.getName(), event.getDamage());
                }
            }
        }
    }

    private void increasePlayerScore(String playerName, double amount) {
        leaderboard.put(playerName, leaderboard.getOrDefault(playerName, 0.0) + amount);
    }

    private void displayLeaderboard() {
        Bukkit.broadcastMessage("§e╔══════════════════════╗");
        Bukkit.broadcastMessage("§6      §l===== Leaderboard =====");

        int rank = 1;
        int maxNameLength = 15; // Adjust as needed

        // Sort the entries by damage in descending order
        sortedEntries = leaderboard.entrySet()
                .stream()
                .sorted(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        // Iterate up to the minimum of 3 and the size of sortedEntries
        int iterations = Math.min(3, sortedEntries.size());
        for (int i = 0; i < iterations; i++) {
            Map.Entry<String, Double> entry = sortedEntries.get(i);
            String playerName = entry.getKey();
            double score = entry.getValue();

            // Ensure a consistent length for the player name
            playerName = playerName.length() > maxNameLength ? playerName.substring(0, maxNameLength) : playerName;

            // Format the double value with one decimal place
            String formattedScore = String.format("%.1f", score);

            String message = String.format("      §e §e#%d   §7%-1s - §b%s Damage      ", rank, playerName, formattedScore);
            Bukkit.broadcastMessage(message);

            rank++;
        }

        Bukkit.broadcastMessage("§e╚══════════════════════╝");
    }

    private void giveRewards() {
        // Example: Give rewards based on leaderboard position
        for (Map.Entry<String, Double> entry : sortedEntries) {
            String playerName = entry.getKey();
            Player player = Bukkit.getPlayer(playerName);
            double score = entry.getValue();

            // Check the player's position and give rewards accordingly
            if (score > 0) {
                if (score == sortedEntries.get(0).getValue()) {
                    // First place reward
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + playerName + " 1500");
                    dropReward(player);
                } else if (score == sortedEntries.get(1).getValue()) {
                    // Second place reward
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + playerName + " 1000");
                    dropReward(player);
                } else if (score == sortedEntries.get(2).getValue()) {
                    // Third place reward
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + playerName + " 500");
                    dropReward(player);
                }
            }
        }
    }

    public void dropReward(Player player) {
        double baseChance = 0.15; // 15% base chance

        // Calculate the final chance
        double finalChance = baseChance;

        // Generate a random number between 0 and 1
        double randomValue = Math.random();

        // Reward scenarios based on leaderboard position
        int playerRank = getPlayerRank(player.getName());

        if (playerRank == 1) {
            if (randomValue <= finalChance) {
                String item = getRandomItemPerPlace(1);
                if (!item.equalsIgnoreCase("NULL")) {
                    if (item.contains("WRATH")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give TALISMAN " + item + " " + player.getName() + " 1");
                    } else if (item.contains("ROD")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "customfishing items rod give " + player.getName() + " " + item + " 1");
                    } else if (item.contains("BLADE")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give SWORD " + item + " " + player.getName() + " 1");
                    } else {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give ARMOR " + item + " " + player.getName() + " 1");
                    }
                    String itemBroadcast = formatItemName(item);
                    Bukkit.broadcastMessage("§c〖☤〗 §e" + player.getName() + " §fbaru saja mendapatkan §c" + itemBroadcast + "§f!");
                    playSoundToAllPlayers(Sound.ENTITY_WITHER_SHOOT, 100, 0f);
                }
            } else {
                player.sendMessage("§c〖☤〗 Sayang sekali kamu tidak mendapatkan hadiah setelah mengalahkan Leviathan. Teruslah berusaha!");
            }
        } else if (playerRank == 2) {
            if (randomValue <= finalChance) {
                String item = getRandomItemPerPlace(2);
                if (!item.equalsIgnoreCase("NULL")) {
                    if (item.contains("WRATH")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give TALISMAN " + item + " " + player.getName() + " 1");
                    } else if (item.contains("ROD")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "customfishing items rod give " + player.getName() + " " + item + " 1");
                    } else if (item.contains("BLADE")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give SWORD " + item + " " + player.getName() + " 1");
                    } else {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give ARMOR " + item + " " + player.getName() + " 1");
                    }
                    String itemBroadcast = formatItemName(item);
                    Bukkit.broadcastMessage("§c〖☤〗 §e" + player.getName() + " §fbaru saja mendapatkan §c" + itemBroadcast + "§f!");
                    playSoundToAllPlayers(Sound.ENTITY_WITHER_SHOOT, 100, 0f);
                }
            } else {
                player.sendMessage("§c〖☤〗 Sayang sekali kamu tidak mendapatkan hadiah setelah mengalahkan Leviathan. Teruslah berusaha!");
            }
        } else if (playerRank == 3) {
            if (randomValue <= finalChance) {
                String item = getRandomItemPerPlace(3);
                if (!item.equalsIgnoreCase("NULL")) {
                    if (item.contains("WRATH")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give TALISMAN " + item + " " + player.getName() + " 1");
                    } else if (item.contains("ROD")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "customfishing items rod give " + player.getName() + " " + item + " 1");
                    } else {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give ARMOR " + item + " " + player.getName() + " 1");
                    }
                    String itemBroadcast = formatItemName(item);
                    Bukkit.broadcastMessage("§c〖☤〗 §e" + player.getName() + " §fbaru saja mendapatkan §c" + itemBroadcast + "§f!");
                    playSoundToAllPlayers(Sound.ENTITY_WITHER_SHOOT, 100, 0f);
                }
            } else {
                player.sendMessage("§c〖☤〗 Sayang sekali kamu tidak mendapatkan hadiah setelah mengalahkan Leviathan. Teruslah berusaha!");
            }
        }
    }

    private String formatItemName(String itemName) {
        String[] words = itemName.toLowerCase().split("_");
        StringBuilder formattedName = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                formattedName.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }

        return formattedName.toString().trim();
    }

    private String getRandomItemPerPlace(int place) {
        // Define items and their corresponding chances
        Map<String, Double> itemChances = new HashMap<>();
        itemChances.put("OCEAN'S_WRATH", 0.02);
        if (place == 1) {
            itemChances.put("LEVIATHAN_ROD", 0.05);
            itemChances.put("LEVIATHAN_BLADE", 0.15);   // 40% chance
            itemChances.put("LEVIATHAN'S_VISAGE", 0.2);
            itemChances.put("LEVIATHAN'S_CHESTGUARD", 0.2);
            itemChances.put("LEVIATHAN'S_GREAVES", 0.2);
            itemChances.put("LEVIATHAN'S_TREADS", 0.2);
        }
        if (place == 2) {
            itemChances.put("LEVIATHAN_ROD", 0.3);
            itemChances.put("LEVIATHAN_BLADE", 0.1);   // 40% chance
            itemChances.put("LEVIATHAN'S_VISAGE", 0.2);
            itemChances.put("LEVIATHAN'S_GREAVES", 0.2);
            itemChances.put("LEVIATHAN'S_TREADS", 0.2);
        }
        if (place == 3) {
            itemChances.put("LEVIATHAN_ROD", 0.6);
            itemChances.put("LEVIATHAN'S_GREAVES", 0.2);
            itemChances.put("LEVIATHAN'S_TREADS", 0.2);
        }

        double totalChance = itemChances.values().stream().mapToDouble(Double::doubleValue).sum();
        double randomValue = Math.random() * totalChance;
        double currentChance = 0.0;

        for (Map.Entry<String, Double> entry : itemChances.entrySet()) {
            currentChance += entry.getValue();
            if (randomValue <= currentChance) {
                return entry.getKey();
            }
        }

        return "NULL";
    }

    private void playSoundToAllPlayers(Sound sound, float volume, float pitch) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    private int getPlayerRank(String playerName) {
        for (int i = 0; i < sortedEntries.size(); i++) {
            if (sortedEntries.get(i).getKey().equals(playerName)) {
                return i + 1;
            }
        }
        return -1; // Return -1 if the player is not found
    }
}
