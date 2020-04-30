package com.playares.commons.util.general;

import com.playares.commons.logger.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public final class Configs {
    /**
     * Return a Yaml Configuration
     * @param name File Name
     * @return YamlConfiguration
     */
    public static YamlConfiguration getConfig(Plugin plugin, String name) {
        final File file = new File(plugin.getDataFolder() + System.getProperty("file.separator") + name + ".yml");

        if (!file.exists()) {
            createConfig(plugin, name);
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Create a new Yaml Configuration
     * @param plugin Plugin
     * @param name File Name
     */
    public static void createConfig(Plugin plugin, String name) {
        final File file = new File(plugin.getDataFolder() + "/" + name + ".yml");

        if (file.exists()) {
            return;
        }

        if (plugin.getDataFolder().mkdirs()) {
            Logger.print("Created directory");
        }

        try {
            if (file.createNewFile()) {
                Logger.print("Created file '" + name + ".yml'");
            }
        } catch (IOException ex) {
            Logger.error("Failed to create '" + name + ".yml'");
            return;
        }

        plugin.saveResource(name + ".yml", true);
    }

    /**
     * Save a Yaml Configuration
     * @param name File Name
     * @param config Config File
     */
    public static void saveConfig(Plugin plugin, String name, YamlConfiguration config) {
        final File file = new File(plugin.getDataFolder() + "/" + name + ".yml");

        if (!file.exists()) {
            Logger.warn("Couldn't find file '" + name + ".yml'");
            createConfig(plugin, name);
        }

        try {
            config.save(file);
        } catch (IOException ex) {
            Logger.error("Failed to save file '" + name + ".yml'");
        }
    }
}
