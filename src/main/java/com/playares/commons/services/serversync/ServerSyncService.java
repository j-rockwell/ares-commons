package com.playares.commons.services.serversync;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.playares.commons.AresPlugin;
import com.playares.commons.AresService;
import com.playares.commons.connect.mongodb.MongoDB;
import com.playares.commons.logger.Logger;
import com.playares.commons.services.serversync.data.SyncedServer;
import com.playares.commons.util.bukkit.Scheduler;
import com.playares.commons.util.general.Configs;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ServerSyncService implements AresService {
    @Getter public final AresPlugin owner;
    @Getter public final String name = "Server Sync Service";
    @Getter public final String databaseName;
    @Getter public final Set<SyncedServer> servers;
    @Getter @Setter public SyncedServer thisServer;
    @Getter public BukkitTask updateTask;

    public ServerSyncService(AresPlugin owner, String databaseName) {
        this.owner = owner;
        this.databaseName = databaseName;
        this.servers = Sets.newConcurrentHashSet();
    }

    @Override
    public void start() {
        final MongoDB database = (MongoDB)owner.getDatabaseInstance(MongoDB.class);

        if (database == null || !database.isConnected()) {
            Logger.error("Failed to initialize " + name + " because the MongoDB database was null or not connected");
            return;
        }

        owner.registerCommand(new SyncCommand(this));

        load(); // Load server information from file

        this.updateTask = new Scheduler(owner).async(() -> {

            push(false); // Push this servers information to the database
            pull(); // Pull other servers to update the cache

        }).repeat(5 * 20L, 5 * 20L).run();
    }

    @Override
    public void stop() {
        push(true);

        this.updateTask.cancel();
        this.updateTask = null;
    }

    public void load() {
        final YamlConfiguration configuration = Configs.getConfig(owner, "server-sync");

        final int serverId = configuration.getInt("server-settings.server-id");
        final String displayName = ChatColor.translateAlternateColorCodes('&', configuration.getString("server-settings.display-name"));
        final String bungeeName = configuration.getString("server-settings.bungee-name");
        final String description = configuration.getString("server-settings.description");
        final SyncedServer.ServerType type = SyncedServer.ServerType.valueOf(configuration.getString("server-settings.type"));
        final int maxPlayers = configuration.getInt("server-settings.max-players");
        final int premiumAllocatedSlots = configuration.getInt("server-settings.premium-allocated-slots");

        final List<String> playerList = Lists.newArrayList();

        if (thisServer != null) {
            playerList.addAll(thisServer.getPlayerList());
        }

        setThisServer(new SyncedServer(
                owner,
                serverId,
                displayName,
                bungeeName,
                description,
                type,
                (owner.getServer().hasWhitelist() ? SyncedServer.ServerStatus.WHITELISTED : SyncedServer.ServerStatus.ONLINE),
                playerList,
                maxPlayers,
                premiumAllocatedSlots));
    }

    /**
     * Returns the most balanced lobby
     * @return Lobby Server
     */
    public SyncedServer getLobby() {
        final List<SyncedServer> lobbies = servers.stream().filter(server -> server.getType().equals(SyncedServer.ServerType.LOBBY)).collect(Collectors.toList());

        if (lobbies.isEmpty()) {
            return null;
        }

        lobbies.sort(Comparator.comparingInt(lobby -> lobby.getPlayerList().size()));
        Collections.reverse(lobbies);

        return lobbies.get(0);
    }

    /**
     * Pushes this servers configuration to others for them cache
     * @param shutdown Sets the server status to offline
     */
    private void push(boolean shutdown) {
        final List<String> playerList = Lists.newArrayList();
        Bukkit.getOnlinePlayers().forEach(player -> playerList.add(player.getName()));
        this.thisServer.setPlayerList(playerList);

        if (shutdown) {
            thisServer.setStatus(SyncedServer.ServerStatus.OFFLINE);
        } else if (owner.getServer().hasWhitelist()) {
            thisServer.setStatus(SyncedServer.ServerStatus.WHITELISTED);
        } else {
            thisServer.setStatus(SyncedServer.ServerStatus.ONLINE);
        }

        final MongoDB database = (MongoDB)owner.getDatabaseInstance(MongoDB.class);

        if (database == null) {
            Logger.error("Failed to update server sync information: Database was not initialized");
            return;
        }

        final MongoCollection<Document> collection = database.getCollection(databaseName, "server_sync");

        if (collection == null) {
            Logger.error("Failed to update server sync information: Collection was not found");
            return;
        }

        final Document existing = collection.find(Filters.eq("id", thisServer.getServerId())).first();

        if (existing == null) {
            collection.insertOne(thisServer.toDocument());
        } else {
            collection.replaceOne(existing, thisServer.toDocument());
        }
    }

    /**
     * Handles pulling other servers information from the database to be temporarily cached here
     */
    private void pull() {
        final MongoDB database = (MongoDB)owner.getDatabaseInstance(MongoDB.class);

        if (database == null) {
            Logger.error("Failed to obtain MongoDB Database instance");
            return;
        }

        final MongoCollection<Document> collection = database.getCollection(databaseName, "server_sync");

        if (collection == null) {
            Logger.error("Failed to obtain Mongo Collection for Server Sync");
            return;
        }

        final MongoCursor<Document> cursor = collection.find().cursor();
        servers.clear();

        while (cursor.hasNext()) {
            final Document document = cursor.next();
            final SyncedServer server = new SyncedServer(owner).fromDocument(document);

            if (server.getServerId() == thisServer.getServerId()) {
                continue;
            }

            servers.add(server);
        }
    }

    @AllArgsConstructor
    @CommandAlias("ssync|serversync")
    public final class SyncCommand extends BaseCommand {
        @Getter public final ServerSyncService service;

        @Subcommand("reload")
        @Description("Reload server sync configuration")
        @CommandPermission("ssync.reload")
        public void onReload(CommandSender sender) {
            load();
            sender.sendMessage(ChatColor.GREEN + "Reload complete");
        }
    }
}