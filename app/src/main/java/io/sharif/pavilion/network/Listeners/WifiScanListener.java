package io.sharif.pavilion.network.Listeners;

import java.util.List;

import io.sharif.pavilion.network.DataStructures.ApInfo;
import io.sharif.pavilion.network.Utilities.ActionResult;

public interface WifiScanListener {

    void onWifiScanFinished(List<ApInfo> scanResults); // called when list of servers is available

    void onFailure(ActionResult errorCode);
}