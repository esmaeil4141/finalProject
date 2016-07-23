package io.sharif.pavilion.clientSide;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import io.sharif.pavilion.R;
import io.sharif.pavilion.model.ServerObj;
import io.sharif.pavilion.utility.Statics;


public class HeadlinesFragment extends Fragment {

    OnHeadlineSelectedListener callback;
    RecyclerView headRecyclerView;
    MainActivity activity;
    MyAdapterC adapter;

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

        int layout = android.R.layout.simple_list_item_activated_1;  //not not available before honeycomb

        headRecyclerView= (RecyclerView) getView().findViewById(R.id.head_recyclerview);
        activity= (MainActivity) getActivity();

        ArrayList<ServerObj> serverObjs= Statics.getFakeSavedServers(activity);

        adapter = new MyAdapterC(serverObjs,activity,activity);
        headRecyclerView.setAdapter(adapter);
        headRecyclerView.setLayoutManager(new LinearLayoutManager(activity));

//        setListAdapter(new ArrayAdapter<String>(getActivity(),layout,data));

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


}
