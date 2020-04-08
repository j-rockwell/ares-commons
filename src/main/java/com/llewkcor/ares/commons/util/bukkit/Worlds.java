package com.llewkcor.ares.commons.util.bukkit;

import com.google.common.base.Preconditions;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.Particle;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;

public final class Worlds {
    /**
     * Plays a sound for all players at the provided location
     * @param location Sound Location
     * @param sound Sound
     */
    public static void playSound(Location location, Sound sound) {
        location.getWorld().playSound(location, sound, 1.0F, 1.0F);
    }

    /**
     * Spawns fake lightning at the provided location
     * @param location Location
     */
    public static void spawnFakeLightning(Location location) {
        location.getWorld().strikeLightningEffect(location);
    }

    /**
     * Spawns a firework with the provided params at the provided location
     * @param plugin Plugin
     * @param location Spawn Location
     * @param power Firework power
     * @param detonate Firework detonate
     * @param effects Firework Effects
     */
    public static void spawnFirework(Plugin plugin, Location location, int power, long detonate, FireworkEffect... effects) {
        final Firework firework = (Firework)location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        final FireworkMeta meta = firework.getFireworkMeta();

        meta.setPower(power);
        meta.addEffects(effects);

        firework.setFireworkMeta(meta);

        new Scheduler(plugin).sync(firework::detonate).delay(detonate).run();
    }

    private Worlds() {
        throw new UnsupportedOperationException("This class can not be instantiated");
    }
}
