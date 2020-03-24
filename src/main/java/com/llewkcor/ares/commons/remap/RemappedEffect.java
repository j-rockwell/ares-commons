package com.llewkcor.ares.commons.remap;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.potion.PotionEffectType;

@AllArgsConstructor
public enum RemappedEffect {
    SPEED(PotionEffectType.SPEED),
    STRENGTH(PotionEffectType.INCREASE_DAMAGE),
    SLOWNESS(PotionEffectType.SLOW),
    HASTE(PotionEffectType.FAST_DIGGING),
    MINING_FATIGUE(PotionEffectType.SLOW_DIGGING),
    HEALTH(PotionEffectType.HEAL),
    HARMING(PotionEffectType.HARM),
    JUMP_BOOST(PotionEffectType.JUMP),
    NAUSEA(PotionEffectType.CONFUSION),
    REGENERATION(PotionEffectType.REGENERATION),
    DAMAGE_RESISTANCE(PotionEffectType.DAMAGE_RESISTANCE),
    RESISTANCE(PotionEffectType.DAMAGE_RESISTANCE),
    FIRE_RESISTANCE(PotionEffectType.FIRE_RESISTANCE),
    WATER_BREATHING(PotionEffectType.WATER_BREATHING),
    INVISIBILITY(PotionEffectType.INVISIBILITY),
    BLINDNESS(PotionEffectType.BLINDNESS),
    NIGHT_VISION(PotionEffectType.NIGHT_VISION),
    HUNGER(PotionEffectType.HUNGER),
    WEAKNESS(PotionEffectType.WEAKNESS),
    POISON(PotionEffectType.POISON),
    WITHER(PotionEffectType.WITHER),
    HEALTH_BOOST(PotionEffectType.HEALTH_BOOST),
    ABSORPTION(PotionEffectType.ABSORPTION),
    SATURATION(PotionEffectType.SATURATION);

    @Getter public final PotionEffectType type;

    /**
     * Returns a RemappedEffect for the provided Bukkit PotionEffectType
     * @param type Bukkit PotionEffectType
     * @return RemappedEffect
     */
    public static RemappedEffect getEffectByBukkit(PotionEffectType type) {
        for (RemappedEffect value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }

        return null;
    }

    /**
     * Returns a RemappedEffect matching the provided string
     * @param name Effect name input
     * @return RemappedEffect
     */
    public static PotionEffectType getEffectTypeByName(String name) {
        for (RemappedEffect value : values()) {
            final String userFriendlyName = value.name().toLowerCase().replace("_", "");

            if (userFriendlyName.equalsIgnoreCase(name)) {
                return value.getType();
            }
        }

        return null;
    }
}
