package com.llewkcor.ares.commons.location;

import com.llewkcor.ares.commons.connect.mongodb.MongoDocument;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class PLocatable implements Locatable, MongoDocument<PLocatable> {
    @Getter @Setter public String worldName;
    @Getter @Setter public double x;
    @Getter @Setter public double y;
    @Getter @Setter public double z;
    @Getter @Setter public float yaw;
    @Getter @Setter public float pitch;

    /**
     * Creates an empty PLocatable
     */
    public PLocatable() {
        this.worldName = null;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.yaw = 0;
        this.pitch = 0;
    }

    /**
     * Creates a PLocatable object based on a Bukkit LivingEntity instance
     * @param entity Bukkit LivingEntity
     */
    public PLocatable(LivingEntity entity) {
        this.worldName = entity.getLocation().getWorld().getName();
        this.x = entity.getLocation().getX();
        this.y = entity.getLocation().getY();
        this.z = entity.getLocation().getZ();
        this.yaw = entity.getLocation().getYaw();
        this.pitch = entity.getLocation().getPitch();
    }

    /**
     * Creates a PLocatable based on defined coordinates
     * @param worldName Bukkit World Name
     * @param x Bukkit X
     * @param y Bukkit Y
     * @param z Bukkit Z
     * @param yaw Bukkit Yaw
     * @param pitch Bukkit Pitch
     */
    public PLocatable(String worldName, double x, double y, double z, float yaw, float pitch) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Returns a rebuilt Bukkit Location
     * @return Bukkit Location
     */
    public Location getBukkit() {
        if (worldName == null || Bukkit.getWorld(worldName) == null) {
            throw new NullPointerException("World not found for Player Locatable");
        }

        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

    @Override
    public String toString() {
        return Math.round(x) + ", " + Math.round(y) + ", " + Math.round(z) + ", " + getBukkit().getWorld().getEnvironment().name();
    }

    @Override
    public PLocatable fromDocument(Document document) {
        return new PLocatable(
                document.getString("world"),
                document.getDouble("x"),
                document.getDouble("y"),
                document.getDouble("z"),
                document.getDouble("yaw").floatValue(),
                document.getDouble("pitch").floatValue());
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("world", getWorldName())
                .append("x", getX())
                .append("y", getY())
                .append("z", getZ())
                .append("yaw", getYaw())
                .append("pitch", getPitch());
    }
}