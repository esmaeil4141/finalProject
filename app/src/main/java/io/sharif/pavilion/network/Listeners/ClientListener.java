package io.sharif.pavilion.network.Listeners;

import io.sharif.pavilion.network.DataStructures.Message;

/**
 * Client service callbacks.
 */
public interface ClientListener {

    /**
     * This method is called when client joins server wifi network.
     */
    void onJoinedGroup();

    /**
     * This method is called when client connects to server socket.
     */
    void onConnected();

    /**
     * This method is called when client fails to connect to server socket.
     */
    void onConnectionFailure();

    /**
     * This method is called when client closes the connection to server socket.
     */
    void onDisconnected();

    /**
     * This method is called when client leaves server wifi network.
     */
    void onLeftGroup();

    /**
     * This method is called when client has received a new message from server.
     * @param message received message
     */
    void onMessageReceived(Message message);
}