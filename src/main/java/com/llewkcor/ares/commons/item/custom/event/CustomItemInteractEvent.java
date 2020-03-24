package com.llewkcor.ares.commons.item.custom.event;

import com.llewkcor.ares.commons.item.custom.CustomItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;

public final class CustomItemInteractEvent extends PlayerEvent implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final CustomItem item;
    @Getter public final Action action;
    @Getter public final Block clickedBlock;
    @Getter @Setter public boolean cancelled;

    /**
     * Custom Item Interact Event
     * @param who Player
     * @param item Custom Item instance
     * @param action Bukkit Action
     * @param clickedBlock Clicked block
     */
    public CustomItemInteractEvent(Player who, CustomItem item, Action action, Block clickedBlock) {
        super(who);
        this.item = item;
        this.action = action;
        this.clickedBlock = clickedBlock;
    }

    /**
     * Returns true if this was a left click event
     * @return Is left click
     */
    public boolean isLeftClick() {
        return action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_BLOCK);
    }

    /**
     * Returns true if this was a right click
     * @return Is right click
     */
    public boolean isRightClick() {
        return action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK);
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
