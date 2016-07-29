package io.sharif.pavilion.clientSide;

import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.sharif.pavilion.model.ServerObj;
import io.sharif.pavilion.model.contents.ContentsObj;
import io.sharif.pavilion.network.DataStructures.ApInfo;
import io.sharif.pavilion.network.Listeners.WifiScanListener;



class CustomWifiScanListener implements WifiScanListener {
    Activity activity;
    ServersListAdapter adapter;
    ProgressDialog pd;

    public CustomWifiScanListener(Activity activity, ServersListAdapter adapter, ProgressDialog pd) {
        this.activity = activity;
        this.adapter = adapter;
        this.pd = pd;
    }

    @Override
    public void onWifiScanFinished(List<ApInfo> scanResults) {

        Toast.makeText(activity,"scan finished",Toast.LENGTH_LONG).show();
//        scanResults.add(new ApInfo("ghalamchi1","","1111","2222"));
//        scanResults.add(new ApInfo("gaj1","","3333","4444"));

        List<ServerObj> s=new ArrayList<>();

        for (ApInfo apInfo:scanResults){
            s.add(new ServerObj(apInfo,new ContentsObj()));
        }
//                adapter = new ServersListAdapter(s, activity, activity);
        adapter.updateList(s);
        pd.dismiss();
    }



}
