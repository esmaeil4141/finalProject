package io.sharif.pavilion.activities;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import io.sharif.pavilion.R;
import io.sharif.pavilion.serverSide.dialoges.SimpleDialog;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.ic_menu_guid));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //end of template

        View.OnClickListener listener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message="";

                switch (v.getId()){
                    case R.id.q1:
                        message=HelpActivity.this.getResources().getString(R.string.q1message);
                        new SimpleDialog(HelpActivity.this, message);//showing dilog
                        break;
                    case R.id.q2:
                        message=HelpActivity.this.getResources().getString(R.string.q2message);
                        new SimpleDialog(HelpActivity.this, message);//showing dilog
                        break;
                    case R.id.q3:
                        message=HelpActivity.this.getResources().getString(R.string.q3message);
                        new SimpleDialog(HelpActivity.this, message);//showing dilog
                        break;


                }




            }
        };
        TextView q1= (TextView) findViewById(R.id.q1);
        TextView q2= (TextView) findViewById(R.id.q2);
        TextView q3= (TextView) findViewById(R.id.q3);

        q1.setOnClickListener(listener);
        q2.setOnClickListener(listener);
        q3.setOnClickListener(listener);

    }

}
