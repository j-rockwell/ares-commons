package com.playares.commons.services.serversync.event;

import com.playares.commons.services.serversync.data.SyncedServer;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public final class ServerSyncEvent extends Event {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final List<SyncedServer> servers;

    public ServerSyncEvent(List<SyncedServer> servers) {
        this.servers = servers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
