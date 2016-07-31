package io.sharif.pavilion.clientSide;


import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import io.sharif.pavilion.network.DataStructures.ApInfo;
import io.sharif.pavilion.network.Listeners.ReceiveMessageListener;
import io.sharif.pavilion.network.Listeners.WifiListener;
import io.sharif.pavilion.network.Services.ClientService;
import io.sharif.pavilion.network.Utilities.ActionResult;

public class CustomClientService extends ClientService{
    ProgressDialog wifiScanPd;
    ProgressDialog joinPd;
    ClientActivity activity;

    public CustomClientService(Context context,ClientActivity clientActivity,ServersListAdapter adapter,
                               CustomWifiScanListener wifiScanListener,CustomClientListener clientListener,
                               WifiListener wifiListener,ReceiveMessageListener receiveMessageListener) {

        super(context, wifiScanListener, clientListener, wifiListener, receiveMessageListener);

        this.activity=clientActivity;
        clientActivity.clientService=this;

       //=  new CustomWifiScanListener(activity,adapter,wifiScanPd);



        wifiScanPd=wifiScanListener.pd;
        joinPd=clientListener.pd;
    }

    public void pr(String msg){
        Log.d("myPavilion",msg);
    }



    @Override
    public ActionResult scan(){
        wifiScanPd.show();
        ActionResult result= super.scan();
        Log.d("myPavilion",result.name());
        return result;
    }

    @Override
        public ActionResult join(ApInfo apInfo){
        joinPd.show();
        Log.d("myPavilion","JOIN CUsTOM ??");
        ActionResult result=super.join(apInfo);
        Log.d("myPavilion","on join _ apInfo: "+apInfo.getName()+" result:"+result.name());
        return result;

    }



}

