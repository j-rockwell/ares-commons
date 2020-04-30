package com.playares.commons.services.alts.event;

import com.google.common.collect.Lists;
import com.playares.commons.services.alts.data.AccountSession;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import java.util.Collection;
import java.util.List;

public final class AltDetectEvent extends PlayerEvent implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final List<AccountSession> sessions;
    @Getter @Setter public String denyMessage;
    @Getter @Setter public boolean cancelled;

    public AltDetectEvent(Player player, Collection<AccountSession> sessions) {
        super(player);
        this.sessions = Lists.newArrayList(sessions);
        this.denyMessage = null;
        this.cancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}