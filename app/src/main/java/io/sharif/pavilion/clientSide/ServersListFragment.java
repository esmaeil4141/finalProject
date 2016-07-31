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

import java.util.ArrayList;

import io.sharif.pavilion.R;
import io.sharif.pavilion.model.ServerObj;
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
        this.activity= (ClientActivity) activity;
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
        this.activity= (ClientActivity) getActivity();
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


        CustomWifiScanListener wifiScanListener=new CustomWifiScanListener(activity,adapter,pd);
        CustomClientListener clientListener=new CustomClientListener(activity);
        CustomWifiListener wifiListener=new CustomWifiListener();
        CustomReceiveMessageListener receiveMessageListener=new CustomReceiveMessageListener();
        activity.clientService=new CustomClientService(activity,activity,adapter,wifiScanListener,clientListener,wifiListener,receiveMessageListener);
        activity.clientService.start();
//        activity.clientService.scan();// TODO: 2016/07/30

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

