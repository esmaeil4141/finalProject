package io.sharif.pavilion.network.Listeners;

import io.sharif.pavilion.network.DataStructures.ClientDevice;
import io.sharif.pavilion.network.DataStructures.Message;

/**
 * Server service callbacks.
 */
public interface ServerListener {

    /**
     * This method is called when a client joins server wifi network.
     * @param client ClientDevice object for the newly connected client
     */
    void onClientJoined(ClientDevice client);

    /**
     * This method is called when client socket is created.
     * @param client client object
     */
    void onClientConnected(ClientDevice client);

    /**
     * This method is called when client socket is closed.
     * @param client client object
     */
    void onClientDisconnected(ClientDevice client);

    /**
     * This method is called when a client leaves server wifi network.
     * @param client client object
     */
    void onClientLeft(ClientDevice client);

    /**
     * This method is called when hotspot is enabled.
     */
    void onApEnabled();

    /**
     * This method is called when server service has started.
     */
    void onServerStarted();

    /**
     * This method is called when server socket is created.
     */
    void onSocketCreated();

    /**
     * This method is called when server fails to create server socket.
     */
    void onSocketCreateFailure();

    /**
     * This method is called when server socket is closed.
     */
    void onSocketClosed();

    /**
     * This method is called when server has stopped.
     */
    void onServerStopped();

    /**
     * This method is called when hotspot is disabled.
     */
    void onApDisabled();

    /**
     * This method is called when server receives a new message from a client.
     * @param clientID client id which send the message
     * @param message message object
     */
    void onMessageReceived(String clientID, Message message);
}