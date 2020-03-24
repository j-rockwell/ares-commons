package com.llewkcor.ares.commons.remap;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.enchantments.Enchantment;

@AllArgsConstructor
public enum RemappedEnchantment {
    PROTECTION(Enchantment.PROTECTION_ENVIRONMENTAL),
    FIRE_PROTECTION(Enchantment.PROTECTION_FIRE),
    FEATHER_FALLING(Enchantment.PROTECTION_FALL),
    BLAST_PROTECTION(Enchantment.PROTECTION_EXPLOSIONS),
    PROJECTILE_PROTECTION(Enchantment.PROTECTION_PROJECTILE),
    RESPIRATION(Enchantment.OXYGEN),
    AQUA_INFINITY(Enchantment.WATER_WORKER),
    THORNS(Enchantment.THORNS),
    DEPTH_STRIDER(Enchantment.DEPTH_STRIDER),
    SHARPNESS(Enchantment.DAMAGE_ALL),
    SMITE(Enchantment.DAMAGE_UNDEAD),
    BANE_OF_ARTHROPODS(Enchantment.DAMAGE_ARTHROPODS),
    KNOCKBACK(Enchantment.KNOCKBACK),
    FIRE_ASPECT(Enchantment.FIRE_ASPECT),
    LOOTING(Enchantment.LOOT_BONUS_BLOCKS),
    FORTUNE(Enchantment.LOOT_BONUS_BLOCKS),
    EFFICIENCY(Enchantment.DIG_SPEED),
    SILK_TOUCH(Enchantment.SILK_TOUCH),
    UNBREAKING(Enchantment.DURABILITY),
    POWER(Enchantment.ARROW_DAMAGE),
    PUNCH(Enchantment.ARROW_KNOCKBACK),
    FLAME(Enchantment.ARROW_FIRE),
    INFINITY(Enchantment.ARROW_INFINITE),
    LUCK(Enchantment.LUCK),
    LURE(Enchantment.LURE);

    @Getter public final Enchantment enchantment;

    /**
     * Returns a Bukkit Enchantment instance matching the provided name input
     * @param name Name input
     * @return Bukkit Enchantment
     */
    public static Enchantment getEnchantmentByName(String name) {
        for (RemappedEnchantment value : values()) {
            final String userFriendlyName = value.name().toLowerCase().replace("_", "");

            if (userFriendlyName.equalsIgnoreCase(name)) {
                return value.getEnchantment();
            }
        }

        return null;
    }
}