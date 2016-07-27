package io.sharif.pavilion.network.Listeners;

import io.sharif.pavilion.network.Utilities.ActionResult;

/**
 * Send message callbacks.
 */
public interface SendMessageListener {

    /**
     * This method is called when message is sent.
     * @param msgID ID of sent message
     */
    void onMessageSent(int msgID);

    /**
     * This method is called during send message process.
     * @param progress total progress
     * @param speed upload speed in bytes per second
     * @param totalSize total message size in bytes
     * @param sent total sent bytes
     */
    void onProgress(float progress, float speed, long totalSize, long sent);

    /**
     * This method is called when an error occurs during send message process.
     * @param errorCode error code
     */
    void onFailure(ActionResult errorCode);
}