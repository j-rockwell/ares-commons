package com.playares.commons.services.account.menu;

import com.playares.commons.AresPlugin;
import com.playares.commons.item.ItemBuilder;
import com.playares.commons.menu.ClickableItem;
import com.playares.commons.menu.Menu;
import com.playares.commons.services.account.data.AresAccount;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public final class AccountMenu extends Menu {
    @Getter public final AresAccount account;

    public AccountMenu(AresPlugin plugin, Player player, AresAccount account) {
        super(plugin, player, "Your Account", 1);
        this.account = account;
    }

    private void update() {
        clear();

        final ItemStack broadcastIcon = new ItemBuilder()
                .setMaterial(Material.SIGN)
                .setName(ChatColor.GOLD + "Show Broadcasts")
                .addLore(Arrays.asList(ChatColor.GRAY + "Enabling this feature allows you to see", ChatColor.GRAY + "automatic server broadcasts & tips", ChatColor.RESET + " ", (account.getSettings().isBroadcastsEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled")))
                .build();

        final ItemStack pmIcon = new ItemBuilder()
                .setMaterial(Material.BOOK_AND_QUILL)
                .setName(ChatColor.GOLD + "Private Messages")
                .addLore(Arrays.asList(ChatColor.GRAY + "Enabling this feature allows you to receive", ChatColor.GRAY + "private messages from other players", ChatColor.RESET + " ", (account.getSettings().isPrivateMessagesEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled")))
                .build();

        addItem(new ClickableItem(broadcastIcon, 0, click -> {
            account.getSettings().setBroadcastsEnabled(!account.getSettings().isBroadcastsEnabled());
            update();
        }));

        addItem(new ClickableItem(pmIcon, 2, click -> {
            account.getSettings().setPrivateMessagesEnabled(!account.getSettings().isPrivateMessagesEnabled());
            update();
        }));
    }

    @Override
    public void open() {
        super.open();
        update();
    }
}
