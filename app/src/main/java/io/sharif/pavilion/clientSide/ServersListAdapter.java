package io.sharif.pavilion.clientSide;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.sharif.pavilion.R;
import io.sharif.pavilion.model.ServerObj;
import io.sharif.pavilion.utility.Statics;



public class ServersListAdapter extends RecyclerView.Adapter<MyViewHolder> {

    List<ServerObj> serverObjs;
    Context context;
    ClientActivity activity;

    public ServersListAdapter(List<ServerObj> list, Context context, ClientActivity activity) {
        this.serverObjs=list;
        this.context=context;
        this.activity=activity;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//      Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_for_servers_list, parent, false);
        MyViewHolder holder = new MyViewHolder(v,activity);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.imageView.setImageResource(Statics.getIdOfIcon(serverObjs.get(position).getIconId()));
        holder.server_name.setText(serverObjs.get(position).getServerName());
        holder.setServerObj(serverObjs.get(position));
    }

    @Override
    public int getItemCount() {

        return serverObjs.size();
    }

    public void updateList(List<ServerObj> newServerObjs) {
        serverObjs = newServerObjs;
        notifyDataSetChanged();
    }
}
