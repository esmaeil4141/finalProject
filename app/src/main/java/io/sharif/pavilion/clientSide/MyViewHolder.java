package io.sharif.pavilion.clientSide;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import io.sharif.pavilion.R;
import io.sharif.pavilion.model.ServerObj;


public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    View view;
    ClientActivity activity;
    ImageView imageView;
    TextView server_name;
    ServerObj serverObj;

    public MyViewHolder(View itemView,ClientActivity activity) {
        super(itemView);
        this.view=itemView;
        view.setOnClickListener(this);

        imageView=(ImageView)itemView.findViewById(R.id.icon_view);
        server_name= (TextView) itemView.findViewById(R.id.server_name);
        this.activity=activity;

    }
    public void setServerObj(ServerObj serverObj){
        this.serverObj=serverObj;
    }
    @Override
    public void onClick(View v) {
        ServersListFragment.OnHeadlineSelectedListener h=activity;
        h.onServerSelected(serverObj);
        Log.d("MyViewHolder.onClick","OKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKk");

    }
}
