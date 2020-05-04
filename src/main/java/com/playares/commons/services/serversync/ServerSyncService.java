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
import org.bukkit.entity.Player;
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
        owner.registerCommand(new HubCommand(this));

        load(); // Load server information from file

        this.updateTask = new Scheduler(owner).async(() -> {

            push(); // Push this servers information to the database
            pull(); // Pull other servers to update the cache

        }).repeat(5 * 20L, 5 * 20L).run();

        if (!owner.getServer().getMessenger().getOutgoingChannels().contains("BungeeCord")) {
            owner.getServer().getMessenger().registerOutgoingPluginChannel(owner, "BungeeCord");
        }
    }

    @Override
    public void stop() {
        thisServer.getPlayerList().clear();
        thisServer.setStatus(SyncedServer.ServerStatus.OFFLINE);

        push();

        if (this.updateTask != null) {
            this.updateTask.cancel();
            this.updateTask = null;
        }
    }

    public void load() {
        final YamlConfiguration configuration = Configs.getConfig(owner, "server-sync");

        final int serverId = configuration.getInt("server-settings.server-id");
        final String displayName = ChatColor.translateAlternateColorCodes('&', configuration.getString("server-settings.display-name"));
        final String bungeeName = configuration.getString("server-settings.bungee-name");
        final List<String> description = configuration.getStringList("server-settings.description");
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
     * Send a player to the lobby with a kick message
     * @param player Player
     * @param kickMessage Kick Message
     */
    public void kickToLobby(Player player, String kickMessage) {
        final SyncedServer lobby = getLobby();

        if (lobby == null) {
            player.kickPlayer(kickMessage);
            return;
        }

        player.sendMessage(kickMessage);
        lobby.send(player);
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
     */
    private void push() {
        final List<String> playerList = Lists.newArrayList();
        Bukkit.getOnlinePlayers().forEach(player -> playerList.add(player.getName()));
        this.thisServer.setPlayerList(playerList);

        if (!thisServer.getStatus().equals(SyncedServer.ServerStatus.OFFLINE)) {
            thisServer.setStatus((owner.getServer().hasWhitelist()) ? SyncedServer.ServerStatus.WHITELISTED : SyncedServer.ServerStatus.ONLINE);
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

    @AllArgsConstructor
    public final class HubCommand extends BaseCommand {
        @Getter public final ServerSyncService service;

        @CommandAlias("hub|lobby")
        @Description("Return to the Hub")
        public void onHub(Player player) {
            final SyncedServer lobby = service.getLobby();

            if (lobby == null) {
                player.kickPlayer(ChatColor.GREEN + "Returned to Hub");
                return;
            }

            lobby.send(player);
        }
    }
}