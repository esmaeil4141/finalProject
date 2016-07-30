package io.sharif.pavilion.clientSide;

import android.app.ProgressDialog;
import android.util.Log;

import com.google.gson.Gson;

import io.sharif.pavilion.model.contents.ContentsObj;
import io.sharif.pavilion.network.DataStructures.Message;
import io.sharif.pavilion.network.Listeners.ClientListener;
import io.sharif.pavilion.network.Listeners.SendMessageListener;
import io.sharif.pavilion.network.Utilities.ActionResult;


public class CustomClientListener implements ClientListener {
    ClientActivity clientActivity;

    ProgressDialog pd;

    public CustomClientListener(ClientActivity clientActivity) {
        this.clientActivity = clientActivity;
        pd=new ProgressDialog(clientActivity);
        pd.setTitle("در حال اتصال...");
    }

    @Override
    public void onJoinedGroup() {
//        pd.dismiss(); TODO handle this
        pr("onJoinedGroup");
        clientActivity.clientService.createServerConnection();

    }

    @Override
    public void onConnected() {
        pr("onConnected");
        Message message=new Message(0);
        message.setMessage("give_me_all_files_and_contents");
        SendMessageListener sendMessageListener=new SendMessageListener() {
            @Override
            public void onMessageSent(int msgID) {
                Log.d("myPavilion","onMessageSent id:"+msgID);

            }

            @Override
            public void onProgress(float progress, float speed, long totalSize, long sent) {

            }

            @Override
            public void onFailure(ActionResult errorCode) {

            }
        };
        clientActivity.clientService.sendMessage(message,sendMessageListener);

    }

    @Override
    public void onConnectionFailure() {
        pr("onConnectionFailure");

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
        pr("onMessageReceived message:"+message.getMessage().substring(0,30)+"...");
        switch (message.getID()){
            case 0:

                ContentsObj contentsObj=(new Gson()).fromJson(message.getMessage(),ContentsObj.class);
                pr("RECEIVED CONTENTS_  first file:"+contentsObj.getSubjectObjObjsList().get(0).getFileObj().getFileName());
//                clientActivity.
                break;
            case 1:
                break;
        }
    }
    public void pr(String msg){
        Log.d("myPavilion",msg);
    }
}
