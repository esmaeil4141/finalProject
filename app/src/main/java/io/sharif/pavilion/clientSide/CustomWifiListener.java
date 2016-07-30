package io.sharif.pavilion.clientSide;

import android.util.Log;

import io.sharif.pavilion.network.Listeners.WifiListener;
import io.sharif.pavilion.network.Utilities.ActionResult;

/**
 * Created by EsiJOOn on 2016/07/30.
 */
public class CustomWifiListener implements WifiListener {
    @Override
    public void onWifiEnabled() {
        pr("onWifiEnabled");
    }

    @Override
    public void onWifiDisabled() {
        pr("onWifiDisabled");
    }

    @Override
    public void onFailure(ActionResult errorCode) {
        pr("onFailure_errorCode:"+errorCode.name());
    }
    public void pr(String msg){
        Log.d("myPavilion",msg);
    }

}
