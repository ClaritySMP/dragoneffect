package org.neo.dragoneffect;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public final class Dragoneffect extends JavaPlugin implements Listener {

    private final Map<Player, Boolean> playerHasEgg = new HashMap<>();

    @Override
    public void onEnable() {
        // Load the configuration
        saveDefaultConfig();

        // Register the event listener
        Bukkit.getPluginManager().registerEvents(this, this);

        // Start a repeating task to check inventories
        Bukkit.getScheduler().runTaskTimer(this, this::checkInventories, 20L, 20L); // Check every second
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    // Check all online players' inventories for the dragon egg
    private void checkInventories() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean hasEgg = hasDragonEgg(player.getInventory());
            if (hasEgg && !playerHasEgg.getOrDefault(player, false)) {
                // Player gained the egg
                applyEffect(player);
                sendTitle(player, getConfig().getString("dragon-egg.title-message", "You possess the Dragon Egg!"),
                        getConfig().getString("dragon-egg.subtitle-message", "Feel the power surging through you!"));
            } else if (!hasEgg && playerHasEgg.getOrDefault(player, false)) {
                // Player lost the egg
                removeEffect(player);
                sendTitle(player, getConfig().getString("dragon-egg.stolen-title-message", "The Dragon Egg has been stolen!"),
                        getConfig().getString("dragon-egg.stolen-subtitle-message", "Your power fades..."));
            }
            playerHasEgg.put(player, hasEgg);
        }
    }

    // Check if the player's inventory contains the dragon egg
    private boolean hasDragonEgg(Inventory inventory) {
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType().name().equalsIgnoreCase("DRAGON_EGG")) {
                return true;
            }
        }
        return false;
    }

    // Apply an effect to the player
    private void applyEffect(Player player) {
        String effectName = getConfig().getString("dragon-egg.effect", "INCREASE_DAMAGE");
        int duration = getConfig().getInt("dragon-egg.duration", -1);
        int amplifier = getConfig().getInt("dragon-egg.amplifier", 0);

        // Set duration to maximum value if duration is -1 (infinite)
        if (duration == -1) {
            duration = Integer.MAX_VALUE;
        }

        PotionEffectType effectType = PotionEffectType.getByName(effectName.toUpperCase());
        if (effectType != null) {
            player.addPotionEffect(new PotionEffect(effectType, duration, amplifier, true, true));
        } else {
            getLogger().warning("Invalid effect type: " + effectName);
        }
    }

    // Remove all effects of the given type from the player
    private void removeEffect(Player player) {
        String effectName = getConfig().getString("dragon-egg.effect", "INCREASE_DAMAGE");
        PotionEffectType effectType = PotionEffectType.getByName(effectName.toUpperCase());
        if (effectType != null && player.hasPotionEffect(effectType)) {
            player.removePotionEffect(effectType);
        }
    }

    // Send a title to the player
    private void sendTitle(Player player, String title, String subtitle) {
        player.sendTitle(title, subtitle, 10, 70, 20);
    }

    // Handle when a player joins to update their status
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        boolean hasEgg = hasDragonEgg(player.getInventory());
        playerHasEgg.put(player, hasEgg);
        if (hasEgg) {
            applyEffect(player);
        }
    }
}
