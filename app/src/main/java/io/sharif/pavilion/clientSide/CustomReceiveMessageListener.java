package io.sharif.pavilion.clientSide;

import android.util.Log;

import io.sharif.pavilion.network.Listeners.ReceiveMessageListener;
import io.sharif.pavilion.network.Services.ClientService;
import io.sharif.pavilion.network.Utilities.ActionResult;

/**
 * Created by EsiJOOn on 2016/07/30.
 */
public class CustomReceiveMessageListener implements ReceiveMessageListener{
    ClientService clientService;

    @Override
    public void onReceiveStart() {
        pr("onReceiveStart");
    }

    @Override
    public void onProgress(float progress, float speed, long totalSize, long received) {
        pr("onProgress_ progress:"+progress+" speed:"+speed+ "  totalSize:"+totalSize+"  received:"+received);
    }

    @Override
    public void onReceiveFailure(ActionResult errorCode) {
        pr("onReceiveFailure_errorCode:"+errorCode);
    }

    public void pr(String msg){
        Log.d("myPavilion",msg);
    }
}
