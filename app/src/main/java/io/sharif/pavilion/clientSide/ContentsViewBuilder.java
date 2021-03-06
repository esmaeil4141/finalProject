package io.sharif.pavilion.clientSide;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import io.sharif.pavilion.R;
import io.sharif.pavilion.model.contents.ContentsObj;
import io.sharif.pavilion.model.contents.SubjectObj;
import io.sharif.pavilion.network.Utilities.Utility;


public class ContentsViewBuilder {
    LayoutInflater inflater ;
    ClientActivity activity;
    public ContentsViewBuilder(Activity activity) {
        this.inflater = activity.getLayoutInflater();
        this.activity= (ClientActivity) activity;
    }


    public View getView(ContentsObj contentsObj){
        Log.d("myPavilion","invoke getView contentsObj:"+contentsObj);
        View v=inflater.inflate(R.layout.contents_layout,null);
        LinearLayout layout= (LinearLayout) v.findViewById(R.id.subjects_layout);

            for (SubjectObj subjectObj:contentsObj.getSubjectObjObjsList()){

                if(subjectObj.getSubjectObjsList().size()==0){//small subject:
                    View smallView=inflater.inflate(R.layout.small_subject_layout,null);
                    TextView titleTV= (TextView) smallView.findViewById(R.id.small_title);
                    TextView textTV= (TextView) smallView.findViewById(R.id.small_text);
                    titleTV.setText(subjectObj.getTitle());
                    textTV.setText(subjectObj.getText());
              ///attach show:
                    ImageButton attachButton= (ImageButton) smallView.findViewById(R.id.attach_button);
                    TextView fileNameTV= (TextView) smallView.findViewById(R.id.file_tv);
                    if(subjectObj.getFileObj()!=null) {
                        fileNameTV.setText(subjectObj.getFileObj().getFileName());
                        String path= Utility.getAppFolderPath()+subjectObj.getFileObj().getFileName();
                        attachButton.setOnClickListener(new AttachShowListener(activity,path));//TODO correct path...
                        Log.d("path",path);

                    }else{
                        attachButton.setVisibility(View.GONE);
                        fileNameTV.setVisibility(View.GONE);
                    }

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    int m= (int) activity.getResources().getDimension(R.dimen.small_margin);
                    layoutParams.setMargins(m,m,m,m);
                    layout.addView(smallView,layoutParams);
                }else{//big subject:
                    View bigView=inflater.inflate(R.layout.big_subject_layout,null);
                    TextView bigTitleTV= (TextView) bigView.findViewById(R.id.big_title);
                    bigTitleTV.setText(subjectObj.getTitle());

                    LinearLayout bigViewLayout= (LinearLayout) bigView.findViewById(R.id.big_layout_for_smalls);

                    for (SubjectObj smallSubjec:subjectObj.getSubjectObjsList()) {
                        View smallView=inflater.inflate(R.layout.inner_subject_layout,null);
                        TextView titleTV = (TextView) smallView.findViewById(R.id.small_title);
                        TextView textTV = (TextView) smallView.findViewById(R.id.small_text);
                        titleTV.setText(smallSubjec.getTitle());
                        textTV.setText(smallSubjec.getText());
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        int m= (int) activity.getResources().getDimension(R.dimen.very_small_margin);
                        layoutParams.setMargins(m,m,m,m);
                        bigViewLayout.addView(smallView,layoutParams);
                    }
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    int m= (int) activity.getResources().getDimension(R.dimen.small_margin);
                    layoutParams.setMargins(m,m,m,m);
                    layout.addView(bigView,layoutParams);
                }
            }
        return v;
    }
}

class AttachShowListener implements View.OnClickListener{
    Activity activity;
    String file;

    public AttachShowListener(Activity activity, String file) {
        this.activity = activity;
        this.file = file;
    }

    @Override
    public void onClick(View v) {
        File newFile=new File(file);
        Log.d("File",file);
        try {
            new FileOpen().openFile(v.getContext(),newFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        MimeTypeMap myMime = MimeTypeMap.getSingleton();
//        Intent newIntent = new Intent(Intent.ACTION_VIEW);
//        String mimeType = myMime.getMimeTypeFromExtension(fileExt(file).substring(1));
//        File newFile=new File(file);
//        newIntent.setDataAndType(Uri.fromFile(newFile),mimeType);
//        Log.d("myPavilion","File name:"+newFile.getName() +" path:"+newFile.getPath()+" absolutePath: "+newFile.getAbsolutePath());
//        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        try {
//            activity.startActivity(newIntent);
//        } catch (ActivityNotFoundException e) {
//            Toast.makeText(activity, "No handler for this type of file.", Toast.LENGTH_SHORT).show();
//        }
    }

    private String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }
}