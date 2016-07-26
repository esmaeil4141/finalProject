package io.sharif.pavilion.network.Listeners;

import io.sharif.pavilion.network.DataStructures.ClientDevice;
import io.sharif.pavilion.network.DataStructures.Message;

public interface ServerListener {

    /**
     * This method is called when a client joins server wifi network
     * @param client ClientDevice object for the new client
     */
    void onClientJoined(ClientDevice client);

    void onClientConnected(ClientDevice client); // called when a new client socket is created

    void onClientDisconnected(ClientDevice client); // called when client socket is closed

    /**
     * This method is called when a client leaves server wifi network
     * @param client ClientDevice object
     */
    void onClientLeft(ClientDevice client);

    /**
     * This method is called when hotspot is enabled.
     */
    void onApEnabled();

    /**
     * This method is called when server socket is created.
     */
    void onServerStarted();

    void onSocketCreated();

    void onSocketClosed();
    /**
     * This method is called when server socket is closed.
     */
    void onServerStopped();

    /**
     * This method is called when hotspot is disabled.
     */
    void onApDisabled();

    void onMessageReceived(String clientID, Message message);

}