package com.llewkcor.ares.commons.menu;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public final class ClickableItem {
    @Getter public final ItemStack item;
    @Getter public final int position;
    @Getter public final ClickResult result;
}