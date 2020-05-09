package com.playares.commons.services.alts.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.playares.commons.connect.mongodb.MongoDB;
import com.playares.commons.logger.Logger;
import com.playares.commons.services.alts.AltWatcherService;
import com.playares.commons.util.general.Time;
import org.bson.Document;

import java.util.List;
import java.util.UUID;

public final class AccountSessionDAO {
    /**
     * Returns the current Account Session stored in the database
     * @param service Alt Watcher Service
     * @param uniqueId Bukkit UUID
     * @param address IP Address
     * @return AccountSession
     */
    public static AccountSession getCurrentSession(AltWatcherService service, UUID uniqueId, long address) {
        final MongoDB database = (MongoDB)service.getOwner().getDatabaseInstance(MongoDB.class);
        final MongoCollection<Document> collection = database.getCollection(service.getDatabaseName(), "alts");

        if (collection == null) {
            return null;
        }

        final Document existing = collection.find(Filters.and(Filters.eq("bukkit_id", uniqueId.toString()), Filters.eq("address", address))).first();

        if (existing == null) {
            return null;
        }

        return new AccountSession().fromDocument(existing);
    }

    /**
     * Handles obtaining all Account Sessions matching the provided UUID/Address
     * @param service Alt Watcher Service
     * @param uniqueId Bukkit UUID
     * @param address IP Address
     * @return Immutable List of Account Sessions
     */
    public static ImmutableList<AccountSession> getSessions(AltWatcherService service, UUID uniqueId, long address) {
        final MongoDB database = (MongoDB)service.getOwner().getDatabaseInstance(MongoDB.class);
        final MongoCollection<Document> collection = database.getCollection(service.getDatabaseName(), "alts");

        if (collection == null) {
            return ImmutableList.of();
        }

        final MongoCursor<Document> match = collection.find(Filters.or(Filters.eq("bukkit_id", uniqueId.toString()), Filters.eq("address", address))).cursor();
        final List<AccountSession> result = Lists.newArrayList();

        while (match.hasNext()) {
            final Document document = match.next();
            final AccountSession session = new AccountSession().fromDocument(document);
            result.add(session);
        }

        match.close();

        return ImmutableList.copyOf(result);
    }

    /**
     * Handles saving a provided Account Session to the database
     * @param service Alt Watcher Service
     * @param session Account Session
     */
    public static void setSession(AltWatcherService service, AccountSession session) {
        final MongoDB database = (MongoDB)service.getOwner().getDatabaseInstance(MongoDB.class);
        final MongoCollection<Document> collection = database.getCollection(service.getDatabaseName(), "alts");

        if (collection == null) {
            return;
        }

        final Document existing = collection.find(Filters.and(Filters.eq("bukkit_id", session.getBukkitId().toString()), Filters.eq("address", session.getAddress()))).first();

        if (existing != null) {
            collection.replaceOne(existing, session.toDocument());
        } else {
            collection.insertOne(session.toDocument());
        }
    }

    /**
     * Handles cleaning up expired Account Sessions
     * @param service Alt Watcher Service
     * @param inactivityTime Max time (in seconds) this session has been inactive for
     */
    public static void cleanupSessions(AltWatcherService service, int inactivityTime) {
        final long before = Time.now() - (inactivityTime * 1000L);
        final MongoDB database = (MongoDB)service.getOwner().getDatabaseInstance(MongoDB.class);
        final MongoCollection<Document> collection = database.getCollection(service.getDatabaseName(), "alts");

        if (collection == null) {
            return;
        }

        final long count = collection.deleteMany(Filters.lte("last_seen", before)).getDeletedCount();
        Logger.print("Deleting " + count + " Alt Account Sessions");
    }
}