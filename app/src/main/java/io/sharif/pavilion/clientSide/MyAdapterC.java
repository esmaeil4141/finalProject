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

/**
 * Created by EsiJOOn on 2016/07/18.
 */
public class MyAdapterC extends RecyclerView.Adapter<MyViewHolder> {

    List<ServerObj> serverObjs;
    Context context;
    MainActivity activity;

    public MyAdapterC(List<ServerObj> list, Context context, MainActivity activity) {
        this.serverObjs=list;
        this.context=context;
        this.activity=activity;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//      Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_c, parent, false);
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
}
