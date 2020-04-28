package com.playares.commons.services.account;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.playares.commons.AresPlugin;
import com.playares.commons.AresService;
import com.playares.commons.connect.mongodb.MongoDB;
import com.playares.commons.event.ProcessedChatEvent;
import com.playares.commons.logger.Logger;
import com.playares.commons.promise.Promise;
import com.playares.commons.promise.SimplePromise;
import com.playares.commons.services.account.data.AresAccount;
import com.playares.commons.services.account.menu.AccountMenu;
import com.playares.commons.util.bukkit.Scheduler;
import com.playares.commons.util.general.IPS;
import com.playares.commons.util.general.Time;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class AccountService implements AresService, Listener {
    @Getter public final String name = "Account Service";
    @Getter public final AresPlugin owner;
    @Getter public final String databaseName;
    @Getter public final Set<AresAccount> accountRepository;

    public AccountService(AresPlugin owner, String databaseName) {
        this.owner = owner;
        this.databaseName = databaseName;
        this.accountRepository = Sets.newConcurrentHashSet();
    }

    @Override
    public void start() {
        owner.registerListener(this);
    }

    @Override
    public void stop() {
        AsyncPlayerPreLoginEvent.getHandlerList().unregister(this);
        PlayerJoinEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
        ProcessedChatEvent.getHandlerList().unregister(this);
    }

    /**
     * Handles opening the settings menu for the provided player
     * @param player Player
     * @param promise Promise
     */
    public void openSettingsMenu(Player player, SimplePromise promise) {
        final AresAccount account = getAccountByBukkitID(player.getUniqueId());

        if (account == null) {
            promise.fail("Account not found");
            return;
        }

        final AccountMenu menu = new AccountMenu(owner, player, account);
        menu.open();
        promise.success();
    }

    /**
     * Handles retrieving an account from the database
     * @param filter Filter
     * @return Ares Account
     */
    private AresAccount getAccountFromDatabase(Bson filter) {
        final MongoDB database = (MongoDB)owner.getDatabaseInstance(MongoDB.class);

        if (database == null) {
            Logger.error("Database not initialized");
            return null;
        }

        final MongoCollection<Document> collection = database.getCollection(databaseName, "accounts");
        final Document existing;

        if (collection == null) {
            return null;
        }

        existing = collection.find(filter).first();

        if (existing != null) {
            return new AresAccount().fromDocument(existing);
        } else {
            return null;
        }
    }

    /**
     * Handles saving a provided Ares Account to the database
     * @param account Ares Account
     */
    private void setAccount(AresAccount account) {
        final MongoDB database = (MongoDB)owner.getDatabaseInstance(MongoDB.class);

        if (database == null) {
            Logger.error("Database not initialized");
            return;
        }

        final MongoCollection<Document> collection = database.getCollection(databaseName, "account");
        final Document existing;

        if (collection == null) {
            return;
        }

        existing = collection.find(Filters.eq("ares_id", account.getUniqueId())).first();

        if (existing != null) {
            collection.replaceOne(existing, account.toDocument());
        } else {
            collection.insertOne(account.toDocument());
        }
    }

    /**
     * Handles getting an account by Ares UUID
     * @param aresUniqueId Ares UUID
     * @param promise Promise
     */
    public void getAccountByAresID(UUID aresUniqueId, Promise<AresAccount> promise) {
        new Scheduler(owner).async(() -> {

            final AresAccount account = getAccountFromDatabase(Filters.eq("ares_id", aresUniqueId));
            new Scheduler(owner).sync(() -> promise.ready(account)).run();

        }).run();
    }

    /**
     * Handles getting an account by Bukkit UUID
     * @param bukkitUniqueId Ares UUID
     * @param promise Promise
     */
    public void getAccountByBukkitID(UUID bukkitUniqueId, Promise<AresAccount> promise) {
        new Scheduler(owner).async(() -> {

            final AresAccount account = getAccountFromDatabase(Filters.eq("bukkit_id", bukkitUniqueId));
            new Scheduler(owner).sync(() -> promise.ready(account)).run();

        }).run();
    }

    /**
     * Handles getting an account by Bukkit Username
     * @param username Username
     * @param promise Promise
     */
    public void getAccountByUsername(String username, Promise<AresAccount> promise) {
        new Scheduler(owner).async(() -> {

            final AresAccount account = getAccountFromDatabase(Filters.eq("username", username));
            new Scheduler(owner).sync(() -> promise.ready(account)).run();

        }).run();
    }

    /**
     * Handles returning a cached Ares Account matching the provided Ares UUID
     * @param aresUniqueId Ares UUID
     * @return Ares Account
     */
    public AresAccount getAccountByAresID(UUID aresUniqueId) {
        return accountRepository.stream().filter(account -> account.getUniqueId().equals(aresUniqueId)).findFirst().orElse(null);
    }

    /**
     * Handles returning a cached Ares Account matching the provided Bukkit UUID
     * @param bukkitUniqueId Bukkit UUID
     * @return Ares Account
     */
    public AresAccount getAccountByBukkitID(UUID bukkitUniqueId) {
        return accountRepository.stream().filter(account -> account.getBukkitId().equals(bukkitUniqueId)).findFirst().orElse(null);
    }

    /**
     * Handles returning a cached Ares Account matching the provided Bukkit Username
     * @param username Bukkit Username
     * @return Ares Account
     */
    public AresAccount getAccountByUsername(String username) {
        return accountRepository.stream().filter(account -> account.getUsername().equalsIgnoreCase(username)).findFirst().orElse(null);
    }

    /**
     * Handles Ares Account loading/creation
     * @param event Bukkit Event
     */
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        final MongoDB database = (MongoDB)owner.getDatabaseInstance(MongoDB.class);

        if (database == null || !database.isConnected()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "The server is still starting up");
            return;
        }

        final UUID uniqueId = event.getUniqueId();
        final String username = event.getName();
        final String address = event.getAddress().getHostAddress();
        final long convertedAddress = IPS.toLong(address);
        boolean updated = false;
        AresAccount account = getAccountFromDatabase(Filters.eq("bukkit_id", uniqueId));

        if (account == null) {
            account = new AresAccount(uniqueId, username);
            updated = true;
        }

        if (!account.getUsername().equals(username)) {
            Logger.print("Updated profile username " + account.getUsername() + " to " + username);
            account.setUsername(username);
            updated = true;
        }

        if (convertedAddress != account.getAddress()) {
            Logger.print("Updated profile address for " + username);
            account.setAddress(convertedAddress);
            updated = true;
        }

        if (updated) {
            setAccount(account);
        }

        accountRepository.add(account);
    }

    /**
     * Handles issuing warning if player is not connected to the web
     * @param event Bukkit PlayerJoinEvent
     */
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final AresAccount account = getAccountByBukkitID(player.getUniqueId());

        if (account == null) {
            player.kickPlayer(ChatColor.RED + "Failed to obtain your account");
            return;
        }

        account.setLastLogin(Time.now());
    }

    /**
     * Handles saving Ares Account to database upon disconnecting
     * @param event Bukkit PlayerQuitEvent
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final AresAccount account = getAccountByBukkitID(player.getUniqueId());

        if (account == null) {
            Logger.error("Was unable to save the Ares Account for " + player.getName());
            return;
        }

        account.setLastLogin(Time.now());
        setAccount(account);
        accountRepository.remove(account);
    }

    /**
     * Handles filtering chat messages and applies each players settings
     * @param event Ares ProcessedChatEvent
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onProcessedChat(ProcessedChatEvent event) {
        final Player player = event.getPlayer();
        final AresAccount account = getAccountByBukkitID(player.getUniqueId());
        final List<Player> toRemove = Lists.newArrayList();

        if (account == null) {
            return;
        }

        if (player.hasPermission("arescore.admin")) {
            return;
        }

        for (Player recipient : event.getRecipients()) {
            final AresAccount viewerAccount = getAccountByBukkitID(recipient.getUniqueId());

            if (viewerAccount == null) {
                continue;
            }

            if (account.getSettings().isIgnoring(recipient.getUniqueId()) || viewerAccount.getSettings().isIgnoring(player.getUniqueId())) {
                toRemove.add(recipient);
            }
        }

        event.getRecipients().removeAll(toRemove);
    }
}