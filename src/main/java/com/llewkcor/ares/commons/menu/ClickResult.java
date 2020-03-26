package com.llewkcor.ares.commons.menu;

import org.bukkit.event.inventory.ClickType;

public interface ClickResult {
    /**
     * Fires when an item is clicked within a Menu
     * @param type ClickType
     */
    void click(ClickType type);
}
