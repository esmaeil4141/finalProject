package io.sharif.pavilion.clientSide;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import io.sharif.pavilion.R;
import io.sharif.pavilion.model.ServerObj;
import io.sharif.pavilion.network.DataStructures.Message;
import io.sharif.pavilion.network.Listeners.ClientListener;
import io.sharif.pavilion.network.Listeners.ReceiveMessageListener;
import io.sharif.pavilion.network.Listeners.WifiListener;
import io.sharif.pavilion.network.Utilities.ActionResult;
import io.sharif.pavilion.network.Utilities.Utility;
import io.sharif.pavilion.utility.Statics;


public class ServersListFragment extends Fragment {

    OnHeadlineSelectedListener callback;
    RecyclerView headRecyclerView;
    ClientActivity activity;
    ServersListAdapter adapter;

    public interface OnHeadlineSelectedListener{
        public void onServerSelected(ServerObj serverObj);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        //Make sure that the container Activity has implemented
        //the interface. if not, throw an exception so we can fix it
        try{
            callback = (OnHeadlineSelectedListener) activity;
        }catch(ClassCastException e ){
            throw new ClassCastException(activity.toString() + "must implement OnHeadlineSelectedListener");
        }

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.headline_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        headRecyclerView= (RecyclerView) getView().findViewById(R.id.head_recyclerview);
        headRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
                adapter = new ServersListAdapter(new ArrayList<ServerObj>(), activity, activity);
                headRecyclerView.setAdapter(adapter);

        activity= (ClientActivity) getActivity();

        ArrayList<ServerObj> serverObjs= Statics.getFakeSavedServers(activity);
//        final ProgressDialog pd = ProgressDialog.show(activity, "افزودن کارت ها به جعبه", "یه خورده دندون رو جیگر بزار...");
//        final ProgressDialog pds = ProgressDialog.show(activity, "افزودن کارت ها به جعبه", "یه خورده دندون رو جیگر بزار...");
        ProgressDialog pd=new ProgressDialog(activity);
        pd.setTitle("جست و جوی شبکه ها...");
        Utility.enableWifi(activity);

////////////Initial ClientService:

        activity.clientService=new CustomClientService(activity, new CustomWifiScanListener(activity,adapter,pd),
                new ClientListener() {
                    @Override
                    public void onJoinedGroup() {
pr("onJoinedGroup");
                    }

                    @Override
                    public void onConnected() {
pr("onConnected");
                    }

                    @Override
                    public void onConnectionFailure() {

                    }
                    @Override
                    public void onDisconnected() {
pr("onDisconnected");
                    }

                    @Override
                    public void onLeftGroup() {
pr("onLeftGroup");
                    }

                    @Override
                    public void onMessageReceived(Message message) {
pr("onMessageReceived_message:"+message.getMessage());
                    }
                },
                new WifiListener() {
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
                },
                new ReceiveMessageListener() {
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
                });
        activity.clientService.start();
        Toast.makeText(activity,"starting scan...",Toast.LENGTH_LONG).show();
        activity.clientService.scan();

//        adapter = new ServersListAdapter(serverObjs,activity,activity);
//        headRecyclerView.setAdapter(adapter);
//        //TODO layoutManager:
//        headRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
////        headRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, 1));

    }

    @Override
    public void onStart() {
        super.onStart();

        //When in a two-pane layout, set the lightview to highlight the list item
        //instead of just simply blinking

        Fragment f = getFragmentManager().findFragmentById(R.id.article_fragment);
        RecyclerView v = this.headRecyclerView;
        if(f != null && v != null){
//            v.setch(ListView.CHOICE_MODE_SINGLE);//TODO choice_mode

        }
    }


//    public void onListItemClick(RecyclerView l, View v, int position, long id) {
//
//        //Notify the parent of the selected item
//        callback.onServerSelected();
//
//        //again set the item to be highlighted in a two-pane layout
//        l.setItemChecked(position,true);
//
//    }

public void pr(String msg){
    Log.d("myPavilion",msg);
}
}

