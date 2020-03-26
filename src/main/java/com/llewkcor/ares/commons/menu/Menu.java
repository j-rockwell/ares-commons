package com.llewkcor.ares.commons.menu;

import com.google.common.collect.Sets;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Set;

public final class Menu implements Listener {
    @Getter public Plugin plugin;
    @Getter public final Player player;
    @Getter public final Inventory inventory;
    @Getter public final Set<ClickableItem> items;

    /**
     * Create a new Menu instance
     * @param plugin Bukkit Plugin
     * @param player Bukkit Player
     * @param title Inventory Title
     * @param rows Inventory Rows
     */
    public Menu(Plugin plugin, Player player, String title, int rows) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, (rows * 9), title);
        this.items = Sets.newConcurrentHashSet();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Returns true if this inventory is currently open
     * @return True if open
     */
    public boolean isOpen() {
        return !getInventory().getViewers().isEmpty();
    }

    /**
     * Returns a ClickableItem instance at the provided inventory position
     * @param position Position index
     * @return ClickableItem instance
     */
    public ClickableItem getItemAtPosition(int position) {
        return items.stream().filter(item -> item.getPosition() == position).findFirst().orElse(null);
    }

    /**
     * Returns the first empty slot in this inventory
     * @return First empty slot in this inventory, -1 if no slot is found
     */
    public int getFirstEmpty() {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (getItemAtPosition(i) == null) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Adds ClickableItems to the Menu
     * @param item Single or Collection of ClickableItem instances
     */
    public void addItem(ClickableItem ...item) {
        for (ClickableItem i : item) {
            if (getItemAtPosition(i.getPosition()) != null) {
                removeItem(i.getPosition());
            }

            inventory.setItem(i.getPosition(), i.getItem());
            items.add(i);
        }
    }

    /**
     * Removes the provided ClickableItem instance from the Menu
     * @param item ClickableItem instance
     */
    public void removeItem(ClickableItem item) {
        items.remove(item);
        inventory.setItem(item.getPosition(), null);
    }

    /**
     * Removes a ClickableItem at the provided inventory index
     * @param position Inventory Index
     */
    public void removeItem(int position) {
        final ClickableItem item = getItemAtPosition(position);

        if (item == null) {
            return;
        }

        items.remove(item);
        inventory.setItem(item.getPosition(), null);
    }

    /**
     * Fills all remaining empty slots with the provided ItemStack
     * @param item Bukkit ItemStack
     */
    public void fill(ItemStack item) {
        for (int i = 0; i < inventory.getSize(); i++) {
            final ClickableItem itemAt = getItemAtPosition(i);

            if (itemAt != null) {
                continue;
            }

            addItem(new ClickableItem(item, i, click -> {}));
        }
    }

    /**
     * Removes all items from this menu
     */
    public void clear() {
        inventory.clear();
        items.clear();
    }

    /**
     * Open this menu for the player
     */
    public void open() {
        player.openInventory(inventory);
    }

    /**
     * Listens for InventoryCloseEvent and cancels this registered listener
     * @param event Bukkit InventoryCloseEvent
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }

        InventoryCloseEvent.getHandlerList().unregister(this);
        InventoryClickEvent.getHandlerList().unregister(this);
    }

    /**
     * Listens for InventoryClickEvent, cancels Bukkit logic and fires a ClickableItem runnable
     * @param event Bukkit InventoryClickEvent
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }

        final ClickType type = event.getClick();
        final ClickableItem item = getItemAtPosition(event.getRawSlot());

        event.setCancelled(true);

        if (item == null || item.getResult() == null) {
            return;
        }

        item.getResult().click(type);
    }
}