package com.llewkcor.ares.commons.logger;

import org.bukkit.Bukkit;

import java.util.logging.Level;

public final class Logger {
    /**
     * Print a simple message to console
     *
     * Does not write to file
     * @param message Message
     */
    public static void print(String message) {
        Bukkit.getLogger().log(Level.INFO, message);
    }

    /**
     * Print a message to console with the option to write the message to console
     * @param message Message
     * @param log If true message will be written to log file
     */
    public static void print(String message, boolean log) {
        print(message);

        if (log) {
            writeToFile(Level.INFO, message);
        }
    }

    /**
     * Print a warning message to console and write to file
     * @param message Message
     */
    public static void warn(String message) {
        Bukkit.getLogger().log(Level.WARNING, message);
        writeToFile(Level.WARNING, message);
    }

    /**
     * Print an error message to console and write to file
     * @param message Message
     */
    public static void error(String message) {
        Bukkit.getLogger().log(Level.SEVERE, message);
        writeToFile(Level.SEVERE, message);
    }

    /**
     * Writes log message to the current log file
     * @param level Log Level
     * @param message Message
     */
    private static void writeToFile(Level level, String message) {
        // TODO: Write to file
    }
}