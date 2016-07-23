package io.sharif.pavilion.network.Listeners;

import io.sharif.pavilion.network.Utilities.ActionResult;

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
     * @param errorCode error code, representing reason of failure
     */
    void onFailure(ActionResult errorCode);

}