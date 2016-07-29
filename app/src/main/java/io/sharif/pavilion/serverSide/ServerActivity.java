package io.sharif.pavilion.serverSide;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

import io.sharif.pavilion.R;
import io.sharif.pavilion.model.ServerObj;
import io.sharif.pavilion.model.contents.ContentsObj;
import io.sharif.pavilion.model.contents.FileObj;
import io.sharif.pavilion.model.contents.SubjectObj;
import io.sharif.pavilion.network.Services.ServerService;
import io.sharif.pavilion.utility.Statics;

public class ServerActivity extends AppCompatActivity {

    SubjectObj waitingSubjectForFile;
    TextView waitingTVForFile;
    ContentsObj waitingContentsObj;
    public  ServerService serverService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.ic_build_server));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //end of template
        LinearLayout container = (LinearLayout) findViewById(R.id.container_of_contents_server);
        ContentsEditBuilder contentsEditBuilder=new ContentsEditBuilder(this);
        ServerObj serverObj= Statics.getFakeSavedServers(this).get(0);
        View v=contentsEditBuilder.getView(Statics.getServerContents(this));
        container.addView(v);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1){
            if (resultCode == RESULT_OK) {
                // Get the Uri of the selected file
                Uri uri = data.getData();

                String uriString = uri.toString();
                File myFile = new File(uriString);
                String path = myFile.getAbsolutePath();
                String displayName = null;

                if (uriString.startsWith("content://")) {
                    Cursor cursor = null;
                    try {
                        cursor = this.getContentResolver().query(uri, null, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        }
                    } finally {
                        cursor.close();
                    }
                } else if (uriString.startsWith("file://")) {
                    displayName = myFile.getName();
                }
                waitingSubjectForFile.setFileObj(new FileObj(path,displayName,uri));
                waitingTVForFile.setText(displayName);
                Log.d("esi", "File Uri: " + uri.toString());
                // Get the path
//                String path = FileUtils.getPath(this, uri);
//                Log.d("esi", "File Path: " + path);
                // Get the file instance
                // File file = new File(path);
                // Initiate the upload
                Statics.saveServerContents(waitingContentsObj,this);
            }
        }
    }
}
