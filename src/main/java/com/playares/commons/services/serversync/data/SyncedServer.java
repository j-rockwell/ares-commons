package com.playares.commons.services.serversync.data;

import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.annotations.SerializedName;
import com.playares.commons.AresPlugin;
import com.playares.commons.connect.mongodb.MongoDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

@AllArgsConstructor
public final class SyncedServer implements MongoDocument<SyncedServer> {
    @Getter public final AresPlugin plugin;
    @Getter public int serverId;
    @Getter @Setter public String displayName;
    @Getter @Setter public String bungeeName;
    @Getter @Setter public String description;
    @Getter public ServerType type;
    @Getter @Setter public ServerStatus status;
    @Getter @Setter public List<String> playerList;
    @Getter @Setter public int maxPlayers;
    @Getter @Setter public int premiumAllocatedSlots;

    public SyncedServer(AresPlugin plugin) {
        this.plugin = plugin;
        this.serverId = 0;
        this.displayName = ChatColor.GOLD + "An Ares Server";
        this.bungeeName = "default";
        this.description = ChatColor.GRAY + "This is the default description for an Ares Server";
        this.type = ServerType.LOBBY;
        this.status = ServerStatus.OFFLINE;
        this.playerList = Lists.newArrayList();
        this.maxPlayers = 500;
        this.premiumAllocatedSlots = 350;
    }

    /**
     * Returns true if premium is required to access this server
     * @return True if premium required
     */
    public boolean isPremiumRequired() {
        if (premiumAllocatedSlots == 0) {
            return false;
        }

        return playerList.size() >= premiumAllocatedSlots;
    }

    /**
     * Sends the provided player to this server
     * @param player Player
     */
    @SuppressWarnings("UnstableApiUsage")
    public void send(Player player) {
        player.sendMessage(ChatColor.RESET + "Now sending you to " + displayName + ChatColor.RESET + "...");

        final ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("Connect");
        output.writeUTF(bungeeName);

        player.sendPluginMessage(plugin, "BungeeCord", output.toByteArray());
    }

    @SuppressWarnings("unchecked")
    @Override
    public SyncedServer fromDocument(Document document) {
        this.serverId = document.getInteger("id");
        this.displayName = ChatColor.translateAlternateColorCodes('&', document.getString("display_name"));
        this.bungeeName = document.getString("bungeecord_name");
        this.description = document.getString("description");
        this.type = ServerType.valueOf(document.getString("type"));
        this.status = ServerStatus.valueOf(document.getString("status"));
        this.playerList = (List<String>)document.get("player_list", List.class);
        this.maxPlayers = document.getInteger("max_players", maxPlayers);
        this.premiumAllocatedSlots = document.getInteger("premium_allocated_slots");

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", serverId)
                .append("display_name", displayName)
                .append("bungeecord_name", bungeeName)
                .append("type", type.name())
                .append("status", status.name())
                .append("player_list", playerList)
                .append("max_players", maxPlayers)
                .append("premium_allocated_slots", premiumAllocatedSlots);
    }

    @AllArgsConstructor
    public enum ServerType {
        @SerializedName("lobby") LOBBY("Lobby"),
        @SerializedName("civ") CIV("Civilizations"),
        @SerializedName("dev") DEV("Development"),
        @SerializedName("hc") HC("Hungercraft"),
        @SerializedName("mz") MZ("MineZ");

        @Getter public final String displayName;
    }

    @AllArgsConstructor
    public enum ServerStatus {
        @SerializedName("online") ONLINE(ChatColor.GREEN + "Online"),
        @SerializedName("offline") OFFLINE(ChatColor.RED + "Offline"),
        @SerializedName("whitelisted") WHITELISTED(ChatColor.GRAY + "Whitelisted");

        @Getter public final String displayName;
    }
}
