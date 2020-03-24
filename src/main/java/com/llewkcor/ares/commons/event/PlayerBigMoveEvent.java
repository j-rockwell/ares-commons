package com.llewkcor.ares.commons.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class PlayerBigMoveEvent extends PlayerEvent implements Cancellable {
    @Getter public static HandlerList handlerList = new HandlerList();
    @Getter public final Location from;
    @Getter public final Location to;
    @Getter @Setter public boolean cancelled;

    /**
     * Player Big Move Event
     * @param who Player
     * @param from From Location
     * @param to To Location
     */
    public PlayerBigMoveEvent(Player who, Location from, Location to) {
        super(who);
        this.from = from;
        this.to = to;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
