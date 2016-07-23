package io.sharif.pavilion.network.Listeners;

import io.sharif.pavilion.network.Utilities.ActionResult;

public interface SendMessageListener {

    void onMessageSent(int msgID);

    void onProgress(float progress, float speed, long totalSize, long sent);

    void onFailure(ActionResult errorCode);
}