package com.llewkcor.ares.commons.connect;

public interface Connectable {
    /**
     * Open a new connection
     */
    void openConnection();

    /**
     * Close this connection instance
     */
    void closeConnection();

    /**
     * Returns connection status
     * @return Connection status
     */
    boolean isConnected();
}