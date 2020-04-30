package com.playares.commons.services.alts.data;

import com.playares.commons.connect.mongodb.MongoDocument;
import com.playares.commons.util.general.Time;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.util.UUID;

public final class AccountSession implements MongoDocument<AccountSession> {
    @Getter public UUID bukkitId;
    @Getter public long address;
    @Getter public long firstSeen;
    @Getter @Setter public long lastSeen;

    public AccountSession() {
        this.bukkitId = null;
        this.address = 0L;
        this.firstSeen = Time.now();
        this.lastSeen = Time.now();
    }

    public AccountSession(UUID accountId, long ipAddress) {
        this.bukkitId = accountId;
        this.address = ipAddress;
        this.firstSeen = Time.now();
        this.lastSeen = Time.now();
    }

    @Override
    public AccountSession fromDocument(Document document) {
        this.bukkitId = UUID.fromString(document.getString("bukkit_id"));
        this.address = document.getLong("address");
        this.firstSeen = document.getLong("first_seen");
        this.lastSeen = document.getLong("last_seen");

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("bukkit_id", bukkitId.toString())
                .append("address", address)
                .append("first_seen", firstSeen)
                .append("last_seen", lastSeen);
    }
}