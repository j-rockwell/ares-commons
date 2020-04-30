package com.playares.commons.services.alts.listener;

import com.playares.commons.services.alts.AltWatcherService;
import com.playares.commons.services.alts.data.AccountSession;
import com.playares.commons.services.alts.data.AccountSessionDAO;
import com.playares.commons.services.alts.event.AltDetectEvent;
import com.playares.commons.util.bukkit.Scheduler;
import com.playares.commons.util.general.IPS;
import com.playares.commons.util.general.Time;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public final class AltWatcherListener implements Listener {
    @Getter public final AltWatcherService service;

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        final AccountSession current = AccountSessionDAO.getCurrentSession(service, event.getUniqueId(), IPS.toLong(event.getAddress().getHostAddress()));

        if (current != null) {
            current.setLastSeen(Time.now());
            AccountSessionDAO.setSession(service, current);
            return;
        }

        final AccountSession session = new AccountSession(event.getUniqueId(), IPS.toLong(event.getAddress().getHostAddress()));
        AccountSessionDAO.setSession(service, session);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final UUID uniqueId = event.getPlayer().getUniqueId();
        final long address = IPS.toLong(event.getPlayer().getAddress().getAddress().getHostAddress());

        new Scheduler(service.getOwner()).async(() -> {

            final List<AccountSession> sessions = AccountSessionDAO.getSessions(service, uniqueId, address);

            new Scheduler(service.getOwner()).sync(() -> {

                if (sessions.size() <= 1) {
                    return;
                }

                final AltDetectEvent detectEvent = new AltDetectEvent(event.getPlayer(), sessions);
                Bukkit.getPluginManager().callEvent(detectEvent);

                if (detectEvent.isCancelled()) {
                    if (detectEvent.getDenyMessage() == null) {
                        event.getPlayer().kickPlayer(ChatColor.RED + "Connection denied");
                        return;
                    }

                    event.getPlayer().kickPlayer(detectEvent.getDenyMessage());
                }

            }).run();

        }).run();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final UUID uniqueId = player.getUniqueId();
        final long address = IPS.toLong(player.getAddress().getAddress().getHostAddress());

        new Scheduler(service.getOwner()).async(() -> {

            AccountSession current = AccountSessionDAO.getCurrentSession(service, uniqueId, address);

            if (current == null) {
                current = new AccountSession(uniqueId, address);
            }

            current.setLastSeen(Time.now());

            AccountSessionDAO.setSession(service, current);

        }).run();
    }
}
