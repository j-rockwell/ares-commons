package com.llewkcor.ares.commons.util.bukkit;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class Players {
    /**
     * Plays a sound for the provided player
     * @param player Player
     * @param sound Sound
     */
    public static void playSound(Player player, Sound sound) {
        player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
    }

    /**
     * Players a distanced sound for the provided player
     * @param player Player
     * @param location Sound Location
     * @param sound Sound
     */
    public static void playSound(Player player, Location location, Sound sound) {
        player.playSound(location, sound, 1.0F, 1.0F);
    }

    /**
     * Spawns an effect at the provided location
     * @param player Player
     * @param location Effect Location
     * @param effect Effect
     */
    public static void spawnEffect(Player player, Location location, Effect effect) {
        player.playEffect(location, effect, null);
    }

    /**
     * Spawns an effect at the provided location
     * @param player Player
     * @param location Effect Location
     * @param effect Effect
     * @param amoumt Particle Amount
     * @param speed Particle Speed
     */
    public static void spawnEffect(Player player, Location location, Effect effect, int amoumt, int speed) {
        player.spigot().playEffect(location, effect, 0, 0, (float)1.0, (float)0.5, (float)1.0, (float)0.01, amoumt, speed);
    }

    /**
     * Resets the walk speed for the provided player
     * @param player Player
     */
    public static void resetWalkSpeed(Player player) {
        player.setWalkSpeed(0.2F);
    }

    /**
     * Resets the fly speed for the provided player
     * @param player Player
     */
    public static void resetFlySpeed(Player player) {
        player.setFlySpeed(0.2F);
    }

    /**
     * Resets the health for the provided player
     * @param player Player
     */
    public static void resetHealth(Player player) {
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setExhaustion(0);
        player.setFallDistance(0.0F);
        player.setFireTicks(0);
        player.setNoDamageTicks(20);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
    }

    private Players() {
        throw new UnsupportedOperationException("This class can not be instantiated");
    }
}
