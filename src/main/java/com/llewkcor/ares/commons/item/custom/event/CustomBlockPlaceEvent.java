package com.llewkcor.ares.commons.item.custom.event;

import com.llewkcor.ares.commons.item.custom.CustomBlock;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class CustomBlockPlaceEvent extends PlayerEvent implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final CustomBlock block;
    @Getter @Setter public boolean cancelled;

    /**
     * Custom Block Place Event
     * @param who Player placing the block
     * @param block Custom Block instance
     */
    public CustomBlockPlaceEvent(Player who, CustomBlock block) {
        super(who);
        this.block = block;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
