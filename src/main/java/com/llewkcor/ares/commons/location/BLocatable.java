package com.llewkcor.ares.commons.location;

import com.llewkcor.ares.commons.connect.mongodb.MongoDocument;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import java.util.Objects;

public class BLocatable implements Locatable, MongoDocument<BLocatable> {
    @Getter @Setter public String worldName;
    @Getter @Setter double x;
    @Getter @Setter double y;
    @Getter @Setter double z;

    /**
     * Create an empty BLocatable
     */
    public BLocatable() {
        this.worldName = null;
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;
    }

    /**
     * Create a BLocatable based on a Bukkit Block instance
     * @param block Bukkit Block
     */
    public BLocatable(Block block) {
        this.worldName = block.getWorld().getName();
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
    }

    /**
     * Create a BLocatable based on defined Bukkit coordinates
     * @param worldName Bukkit World
     * @param x Bukkit X
     * @param y Bukkit Y
     * @param z Bukkit Z
     */
    public BLocatable(String worldName, double x, double y, double z) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Returns a rebuilt Bukkit Block instance
     * @return Bukkit Block
     */
    public Block getBukkit() {
        if (Bukkit.getWorld(worldName) == null) {
            throw new NullPointerException("World not found for Block Locatable");
        }

        return Objects.requireNonNull(Bukkit.getWorld(worldName)).getBlockAt((int)getX(), (int)getY(), (int)getZ());
    }

    @Override
    public String toString() {
        return Math.round(x) + ", " + Math.round(y) + ", " + Math.round(z) + ", " + getBukkit().getWorld().getEnvironment().name();
    }

    @Override
    public BLocatable fromDocument(Document document) {
        return new BLocatable(
                document.getString("world"),
                document.getDouble("x"),
                document.getDouble("y"),
                document.getDouble("z"));
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("world", getWorldName())
                .append("x", getX())
                .append("y", getY())
                .append("z", getZ());
    }
}