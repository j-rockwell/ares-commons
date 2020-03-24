package com.llewkcor.ares.commons.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class PlayerDamagePlayerEvent extends Event implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final Player damager;
    @Getter public final Player damaged;
    @Getter public final DamageType type;
    @Getter @Setter public boolean cancelled;

    /**
     * Player Damage Player Event
     * @param damager Attacking player
     * @param damaged Attacked player
     * @param type Damage type
     */
    public PlayerDamagePlayerEvent(Player damager, Player damaged, DamageType type) {
        this.damager = damager;
        this.damaged = damaged;
        this.type = type;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public enum DamageType {
        PHYSICAL,

        PROJECTILE
    }
}