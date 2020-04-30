package com.playares.commons.services.alts;

import com.playares.commons.AresPlugin;
import com.playares.commons.AresService;
import com.playares.commons.services.alts.data.AccountSessionDAO;
import com.playares.commons.services.alts.listener.AltWatcherListener;
import com.playares.commons.util.bukkit.Scheduler;
import lombok.Getter;

public final class AltWatcherService implements AresService {
    @Getter public final AresPlugin owner;
    @Getter public final String databaseName;
    @Getter public final String name = "Alternate Account Watcher";

    // TODO: Make configurable
    private final int maxInactivity = 2592000;

    public AltWatcherService(AresPlugin owner, String databaseName) {
        this.owner = owner;
        this.databaseName = databaseName;
    }

    public void start() {
        owner.registerListener(new AltWatcherListener(this));

        new Scheduler(owner).async(() -> AccountSessionDAO.cleanupSessions(this, maxInactivity)).run();
    }

    public void stop() {}
}
