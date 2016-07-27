package io.sharif.pavilion.network.Listeners;

import io.sharif.pavilion.network.Utilities.ActionResult;

/**
 * WiFi state callbacks.
 */
public interface WifiListener {

    /**
     * This method is called when WiFi is enabled.
     */
    void onWifiEnabled();

    /**
     * This method is called when WiFi is disabled.
     */
    void onWifiDisabled();

    /**
     * This method is called when WiFi state is unknown.
     * @param errorCode error code
     */
    void onFailure(ActionResult errorCode);
}