package io.sharif.pavilion.network.Listeners;

import java.util.List;

import io.sharif.pavilion.network.DataStructures.ApInfo;

/**
 * WiFi scan callbacks.
 */
public interface WifiScanListener {

    /**
     * This method is called when wifi scan results is available.
     * @param scanResults list of currently available servers
     */
    void onWifiScanFinished(List<ApInfo> scanResults);
}