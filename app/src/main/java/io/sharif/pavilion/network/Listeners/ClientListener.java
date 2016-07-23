package io.sharif.pavilion.network.Listeners;

import io.sharif.pavilion.network.DataStructures.Message;

public interface ClientListener {

    void onJoinedGroup(); // called when client has joined server's WiFi network

    void onConnected(); // called when client is connected to server socket

    void onDisconnected(); // called when client socket is closed

    void onLeftGroup(); // called when client has left server's WiFi network

    void onMessageReceived(Message message);

}