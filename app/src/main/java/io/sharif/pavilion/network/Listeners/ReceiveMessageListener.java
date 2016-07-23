package io.sharif.pavilion.network.Listeners;

import io.sharif.pavilion.network.Utilities.ActionResult;

public interface ReceiveMessageListener {

    void onReceiveStart();

    void onProgress(float progress, float speed, long totalSize, long received);

    void onReceiveFailure(ActionResult errorCode);
}