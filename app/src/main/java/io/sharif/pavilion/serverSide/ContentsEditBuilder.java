package io.sharif.pavilion.serverSide;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import io.sharif.pavilion.R;
import io.sharif.pavilion.model.contents.ContentsObj;
import io.sharif.pavilion.model.contents.SubjectObj;
import io.sharif.pavilion.network.DataStructures.ClientDevice;
import io.sharif.pavilion.network.DataStructures.Message;
import io.sharif.pavilion.network.Listeners.ReceiveMessageListener;
import io.sharif.pavilion.network.Listeners.ServerListener;
import io.sharif.pavilion.network.Services.ServerService;
import io.sharif.pavilion.network.Utilities.ActionResult;
import io.sharif.pavilion.utility.Statics;



public class ContentsEditBuilder {
    LayoutInflater inflater ;
    ServerActivity activity;
    public ContentsEditBuilder(Activity activity) {
        this.inflater = activity.getLayoutInflater();
        this.activity= (ServerActivity) activity;
    }


    public View getView(ContentsObj contentsObj){
        View v=inflater.inflate(R.layout.contents_edit_layout,null);
        LinearLayout layout= (LinearLayout) v.findViewById(R.id.subjects_layout);
        ImageButton addSmallSubject= (ImageButton) v.findViewById(R.id.add_small_subject);
        ImageButton addBigSubject= (ImageButton) v.findViewById(R.id.add_big_subject);

        EditText serverNameEditText= (EditText) v.findViewById(R.id.server_name);

        //set serverName if had been set
        serverNameEditText.setText(Statics.getServerName(activity));

        // set textWatcher for serverName EditText to save changes
        serverNameEditText.addTextChangedListener(new ServerNameWatcher(activity) );

        // set filter for serverName
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!(Character.isDigit(source.charAt(i)) || Character.isLowerCase(source.charAt(i)) ||Character.isSpaceChar(source.charAt(i)) )) {
                        return "";
                    }
                }
                return null;
            }
        };
        serverNameEditText.setFilters(new InputFilter[] { filter });

        //set listener for switch to enable/disable server...
        Switch serverSwith= (Switch) v.findViewById(R.id.server_switch);
        serverSwith.setOnCheckedChangeListener(new SwitchChangeListener(activity));



            addSmallSubject.setOnClickListener(new AddSmallSubjectListener(activity,contentsObj,layout));
            addBigSubject.setOnClickListener(new AddBigSubjectListener(activity,contentsObj,layout));
            for (SubjectObj subjectObj:contentsObj.getSubjectObjObjsList()){

                if(subjectObj.getSubjectObjsList().size()==0){//small subject:
                    LinearLayout smallView= (LinearLayout) inflater.inflate(R.layout.edit_small_subject_layout,null);
                     EditText titleTV= (EditText) smallView.findViewById(R.id.small_title);
                    titleTV.addTextChangedListener(new EditTextChangeListener(contentsObj,true,subjectObj,activity));
                    EditText textTV= (EditText) smallView.findViewById(R.id.small_text);
                    textTV.addTextChangedListener(new EditTextChangeListener(contentsObj,false,subjectObj,activity));
                    titleTV.setText(subjectObj.getTitle());
                    textTV.setText(subjectObj.getText());
////////////delete button:
                        ImageButton deleteButton= (ImageButton) smallView.findViewById(R.id.delete_icon);
                        deleteButton.setOnClickListener(
                                new BigSmallDeleteListener(contentsObj,smallView,subjectObj)
                        );
////////////attach button:
                    ImageButton attachButton= (ImageButton) smallView.findViewById(R.id.attach_button);
                    TextView fileNameTV= (TextView) smallView.findViewById(R.id.file_tv);
                    attachButton.setOnClickListener(new AttachListener(activity,fileNameTV,subjectObj,contentsObj));

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    int m= (int) activity.getResources().getDimension(R.dimen.small_margin);
                    layoutParams.setMargins(m,m,m,m);
                    layout.addView(smallView,layoutParams);

                }else{//big subject:
                    View bigView=inflater.inflate(R.layout.edit_big_subject_layout,null);
                    LinearLayout bigViewLayout= (LinearLayout) bigView.findViewById(R.id.big_layout_for_smalls);
                    EditText bigTitleTV= (EditText) bigView.findViewById(R.id.big_title);
                    bigTitleTV.addTextChangedListener(new EditTextChangeListener(contentsObj,true,subjectObj,activity));
                    bigTitleTV.setText(subjectObj.getTitle());

                    ImageButton deleteButton= (ImageButton) bigView.findViewById(R.id.delete_icon);

                    deleteButton.setOnClickListener(
                            new BigSmallDeleteListener(contentsObj,bigView,subjectObj)
                    );
                    for (SubjectObj smallSubjec:subjectObj.getSubjectObjsList()) {
                        View innerView=inflater.inflate(R.layout.edit_inner_subject_layout,null);
                        EditText titleTV = (EditText) innerView.findViewById(R.id.small_title);
                        titleTV.addTextChangedListener(new EditTextChangeListener(contentsObj,true,smallSubjec,activity));
                        EditText textTV = (EditText) innerView.findViewById(R.id.small_text);
                        textTV.addTextChangedListener(new EditTextChangeListener(contentsObj,false,smallSubjec,activity));

                        titleTV.setText(smallSubjec.getTitle());
                        textTV.setText(smallSubjec.getText());
                        ImageButton innerDeleteButton= (ImageButton) innerView.findViewById(R.id.delete_icon);
                        innerDeleteButton.setOnClickListener(
                                new InnerDeleteListener(contentsObj,subjectObj,smallSubjec,innerView)
                        );
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        int m= (int) activity.getResources().getDimension(R.dimen.very_small_margin);
                        layoutParams.setMargins(m,m,m,m);
                        bigViewLayout.addView(innerView,layoutParams);
                    }
                    ImageButton addInnerButton= (ImageButton) bigView.findViewById(R.id.add_inner_subject);

                    addInnerButton.setOnClickListener(new AddInnerButtonListener(contentsObj,subjectObj,activity,bigViewLayout));

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

class BigSmallDeleteListener implements View.OnClickListener{
    ContentsObj contentsObj;
    SubjectObj subjectObj;
    View parentView;

    public BigSmallDeleteListener(ContentsObj contentsObj, View parentView, SubjectObj subjectObj) {
        this.contentsObj = contentsObj;
        this.parentView = parentView;
        this.subjectObj = subjectObj;
    }

    @Override
    public void onClick(View v) {
        contentsObj.deleteSubjectObjFromList(subjectObj);
        parentView.animate()
                .translationX(parentView.getWidth())
                .alpha(0.0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        parentView.setVisibility(View.GONE);
                    }
                });

        Statics.saveServerContents(contentsObj,v.getContext());
    }
}
class InnerDeleteListener implements View.OnClickListener{
    ContentsObj contentsObj;
    SubjectObj bigSubjectObj;
    SubjectObj innerSubjectObj;
    View parentView;

    public InnerDeleteListener(ContentsObj contentsObj,SubjectObj bigSubjectObj, SubjectObj innerSubjectObj, View parentView) {
        this.contentsObj=contentsObj;
        this.bigSubjectObj = bigSubjectObj;
        this.innerSubjectObj = innerSubjectObj;
        this.parentView = parentView;
    }

    @Override
    public void onClick(View v) {
        bigSubjectObj.deleteInnerSubject(innerSubjectObj);
        parentView.animate()
                .translationX(parentView.getWidth())
                .alpha(0.0f)
                .setDuration(600)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        parentView.setVisibility(View.GONE);
                    }
                });
        Statics.saveServerContents(contentsObj,v.getContext());

    }
}

class AddInnerListener implements View.OnClickListener{
    Activity activity;

    @Override
    public void onClick(View v) {

    }
}
class EditTextChangeListener implements TextWatcher{
    ContentsObj contentsObj;
    SubjectObj subjectObj;
    boolean isTitle;
    Context context;

    public EditTextChangeListener(ContentsObj contentsObj, boolean isTitle, SubjectObj subjectObj, Context context) {
        this.contentsObj = contentsObj;
        this.isTitle = isTitle;
        this.subjectObj = subjectObj;
        this.context = context;
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }
    @Override
    public void afterTextChanged(Editable s) {
        if(isTitle){
            subjectObj.setTitle(s.toString());
        }else {//isText
            subjectObj.setText(s.toString());
        }
        Statics.saveServerContents(contentsObj,context);
    }
}

class AddInnerButtonListener implements View.OnClickListener{
    View innerView;//i should make this with inflater
    ContentsObj contentsObj;
    SubjectObj smallSubjec;//i should make a new smallSubjec and pass it to this listener
    SubjectObj parentSubjectObj;
    Activity activity;
    LinearLayout bigViewLayout;

    public AddInnerButtonListener( ContentsObj contentsObj, SubjectObj parentSubjectObj, Activity activity, LinearLayout bigViewLayout) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        this.innerView = inflater.inflate(R.layout.edit_inner_subject_layout,null);
        Log.d("esi",innerView.toString());
        this.contentsObj = contentsObj;
        this.parentSubjectObj = parentSubjectObj;
        this.activity = activity;
        this.bigViewLayout = bigViewLayout;
    }

    @Override
    public void onClick(View v) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        this.innerView = inflater.inflate(R.layout.edit_inner_subject_layout,null);
        smallSubjec=new SubjectObj("","");
        parentSubjectObj.addSubject(smallSubjec);

        EditText titleTV = (EditText) innerView.findViewById(R.id.small_title);
        titleTV.addTextChangedListener(new EditTextChangeListener(contentsObj,true,smallSubjec,activity));
        EditText textTV = (EditText) innerView.findViewById(R.id.small_text);
        textTV.addTextChangedListener(new EditTextChangeListener(contentsObj,false,smallSubjec,activity));
        titleTV.setText(smallSubjec.getTitle());
        textTV.setText(smallSubjec.getText());

        ImageButton innerDeleteButton= (ImageButton) innerView.findViewById(R.id.delete_icon);
        innerDeleteButton.setOnClickListener(
                new InnerDeleteListener(contentsObj,parentSubjectObj,smallSubjec,innerView)
        );
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int m= (int) activity.getResources().getDimension(R.dimen.very_small_margin);
        layoutParams.setMargins(m,m,m,m);
        bigViewLayout.addView(innerView,layoutParams);

    }
}

class AttachListener implements View.OnClickListener{
    ServerActivity activity;
    TextView fileNameTV;
    SubjectObj subjectObj;
    ContentsObj contentsObj;

    public AttachListener(ServerActivity activity, TextView fileNameTV, SubjectObj subjectObj, ContentsObj contentsObj) {
        this.activity = activity;
        this.fileNameTV = fileNameTV;
        this.subjectObj = subjectObj;
        this.contentsObj = contentsObj;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        activity.waitingSubjectForFile=subjectObj;
        activity.waitingTVForFile=fileNameTV;
        activity.waitingContentsObj=contentsObj;
        activity.startActivityForResult(intent, 1);
    }
}

class AddSmallSubjectListener implements View.OnClickListener{
        ServerActivity activity;
        ContentsObj contentsObj;
        SubjectObj subjectObj;
        LinearLayout parentLayout;

    public AddSmallSubjectListener(ServerActivity activity, ContentsObj contentsObj, LinearLayout parentLayout) {
        this.activity = activity;
        this.contentsObj = contentsObj;
        this.parentLayout = parentLayout;
    }

    @Override
    public void onClick(View v) {
        subjectObj =new SubjectObj("","");
        contentsObj.addAbstractSubject(subjectObj);
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        LinearLayout smallView= (LinearLayout) inflater.inflate(R.layout.edit_small_subject_layout,null);
        EditText titleTV= (EditText) smallView.findViewById(R.id.small_title);
        titleTV.addTextChangedListener(new EditTextChangeListener(contentsObj,true,subjectObj,activity));
        EditText textTV= (EditText) smallView.findViewById(R.id.small_text);
        textTV.addTextChangedListener(new EditTextChangeListener(contentsObj,false,subjectObj,activity));
        titleTV.setText(subjectObj.getTitle());
        textTV.setText(subjectObj.getText());
////////////delete button:
        ImageButton deleteButton= (ImageButton) smallView.findViewById(R.id.delete_icon);
        deleteButton.setOnClickListener(
                new BigSmallDeleteListener(contentsObj,smallView,subjectObj)
        );
////////////attach button:
        ImageButton attachButton= (ImageButton) smallView.findViewById(R.id.attach_button);
        TextView fileNameTV= (TextView) smallView.findViewById(R.id.file_tv);
        attachButton.setOnClickListener(new AttachListener(activity,fileNameTV,subjectObj,contentsObj));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int m= (int) activity.getResources().getDimension(R.dimen.small_margin);
        layoutParams.setMargins(m,m,m,m);
        parentLayout.addView(smallView,layoutParams);

    }
}
class AddBigSubjectListener implements View.OnClickListener{
    ServerActivity activity;
    ContentsObj contentsObj;
    SubjectObj subjectObj;
    LinearLayout parentLayout;

    public AddBigSubjectListener(ServerActivity activity, ContentsObj contentsObj, LinearLayout parentLayout) {
        this.activity = activity;
        this.contentsObj = contentsObj;
        this.parentLayout = parentLayout;
    }

    @Override
    public void onClick(View v) {

        subjectObj = new SubjectObj("", "");
        contentsObj.addAbstractSubject(subjectObj);
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View bigView = inflater.inflate(R.layout.edit_big_subject_layout, null);
        LinearLayout bigViewLayout = (LinearLayout) bigView.findViewById(R.id.big_layout_for_smalls);
        EditText bigTitleTV = (EditText) bigView.findViewById(R.id.big_title);
        bigTitleTV.addTextChangedListener(new EditTextChangeListener(contentsObj, true, subjectObj, activity));
        bigTitleTV.setText(subjectObj.getTitle());

        ImageButton deleteButton = (ImageButton) bigView.findViewById(R.id.delete_icon);

        deleteButton.setOnClickListener(
                new BigSmallDeleteListener(contentsObj, bigView, subjectObj)
        );
        for (SubjectObj smallSubjec : subjectObj.getSubjectObjsList()) {
            View innerView = inflater.inflate(R.layout.edit_inner_subject_layout, null);
            EditText titleTV = (EditText) innerView.findViewById(R.id.small_title);
            titleTV.addTextChangedListener(new EditTextChangeListener(contentsObj, true, smallSubjec, activity));
            EditText textTV = (EditText) innerView.findViewById(R.id.small_text);
            textTV.addTextChangedListener(new EditTextChangeListener(contentsObj, false, smallSubjec, activity));

            titleTV.setText(smallSubjec.getTitle());
            textTV.setText(smallSubjec.getText());
            ImageButton innerDeleteButton = (ImageButton) innerView.findViewById(R.id.delete_icon);
            innerDeleteButton.setOnClickListener(
                    new InnerDeleteListener(contentsObj, subjectObj, smallSubjec, innerView)
            );
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            int m = (int) activity.getResources().getDimension(R.dimen.very_small_margin);
            layoutParams.setMargins(m, m, m, m);
            bigViewLayout.addView(innerView, layoutParams);
        }
        ImageButton addInnerButton= (ImageButton) bigView.findViewById(R.id.add_inner_subject);

        addInnerButton.setOnClickListener(new AddInnerButtonListener(contentsObj,subjectObj,activity,bigViewLayout));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int m= (int) activity.getResources().getDimension(R.dimen.small_margin);
        layoutParams.setMargins(m,m,m,m);
        parentLayout.addView(bigView,layoutParams);
    }
}
class ServerNameWatcher implements TextWatcher{
Activity activity;

    public ServerNameWatcher(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        Statics.saveServerName(activity,s.toString());
    }
}

class SwitchChangeListener implements CompoundButton.OnCheckedChangeListener{
Activity activity;
    ServerService serverService;

    public SwitchChangeListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked){// enable server
            serverService=new ServerService(activity, new ServerListener() {

                @Override
                public void onSocketCreateFailure() {

                }

                @Override
                public void onClientJoined(ClientDevice client) {

                }

                @Override
                public void onClientConnected(ClientDevice client) {

                }

                @Override
                public void onClientDisconnected(ClientDevice client) {

                }

                @Override
                public void onClientLeft(ClientDevice client) {

                }

                @Override
                public void onApEnabled() {

                }

                @Override
                public void onServerStarted() {

                }

                @Override
                public void onSocketCreated() {

                }

                @Override
                public void onSocketClosed() {

                }

                @Override
                public void onServerStopped() {

                }

                @Override
                public void onApDisabled() {

                }

                @Override
                public void onMessageReceived(String clientID, Message message) {

                }
            } ,  null,new ReceiveMessageListener(){

                @Override
                public void onReceiveStart() {

                }

                @Override
                public void onProgress(float progress, float speed, long totalSize, long received) {

                }

                @Override
                public void onReceiveFailure(ActionResult errorCode) {

                }
            } );
            String apName=Statics.getServerName(activity)+Statics.convertNumToCharacter(Statics.getServerIconId(activity));
            serverService.setApName(apName);
            serverService.start();
            Toast.makeText(activity,"سرور فعال شد",Toast.LENGTH_SHORT).show();
        }else{//disable server
            if(serverService!=null){
                serverService.stop();
                Toast.makeText(activity,"سرور غیرفعال شد!",Toast.LENGTH_SHORT).show();

            }
        }
    }
}