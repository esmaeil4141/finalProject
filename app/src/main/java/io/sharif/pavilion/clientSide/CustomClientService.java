package io.sharif.pavilion.clientSide;


import android.app.ProgressDialog;
import android.content.Context;

import io.sharif.pavilion.network.Listeners.ClientListener;
import io.sharif.pavilion.network.Listeners.ReceiveMessageListener;
import io.sharif.pavilion.network.Listeners.WifiListener;
import io.sharif.pavilion.network.Services.ClientService;
import io.sharif.pavilion.network.Utilities.ActionResult;

public class CustomClientService extends ClientService{
   ProgressDialog wifiScanPd;
    public CustomClientService(Context context, CustomWifiScanListener wifiScanListener, ClientListener clientListener, WifiListener wifiListener, ReceiveMessageListener receiveMessageListener) {
        super(context, wifiScanListener, clientListener, wifiListener, receiveMessageListener);
        wifiScanPd=wifiScanListener.pd;
    }
    @Override
    public ActionResult scan(){
        wifiScanPd.show();
        return super.scan();
    }

}
