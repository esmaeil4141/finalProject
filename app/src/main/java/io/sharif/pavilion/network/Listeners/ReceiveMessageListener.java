package io.sharif.pavilion.network.Listeners;

import io.sharif.pavilion.network.Utilities.ActionResult;

/**
 * Receive Message callbacks.
 */
public interface ReceiveMessageListener {

    /**
     * This method is called when message receiving process has began.
     */
    void onReceiveStart();

    /**
     * This method is called every few seconds during message receiving process.
     * @param progress total progress
     * @param speed download speed in bytes per second
     * @param totalSize message total size in bytes
     * @param received total bytes received
     */
    void onProgress(float progress, float speed, long totalSize, long received);

    /**
     * This method is called when an error occurs during receive process.
     * @param errorCode error code
     */
    void onReceiveFailure(ActionResult errorCode);
}