package io.sharif.pavilion.clientSide;

import android.app.ProgressDialog;
import android.util.Log;

import io.sharif.pavilion.model.ServerObj;
import io.sharif.pavilion.model.contents.ContentsObj;
import io.sharif.pavilion.network.DataStructures.ApInfo;
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
        pr("custom onJoinedGroup");
        new Thread()
        {
            public void run() {
                clientActivity.clientService.createServerConnection();
            }
        }.start();



    }

    @Override
    public void onConnected() {
        pr("custom onConnected");
        Message message=new Message(0);
        message.setMessage("give_me_all_files_and_contents");
        SendMessageListener sendMessageListener=new SendMessageListener() {
            @Override
            public void onMessageSent(int msgID) {
                Log.d("myPavilion","custom onMessageSent id:"+msgID);

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
        pr("custom onConnectionFailure");

    }

    @Override
    public void onDisconnected() {
        pr("custom onDisconnected");

    }

    @Override
    public void onLeftGroup() {
        pr("custom onLeftGroup");

    }

    @Override
    public void onMessageReceived(Message message) {
        pr("custom onMessageReceived id:"+message.getID()+"message:"+message.getMessage()+"...");
       pd.dismiss();
        switch (message.getID()){

            case 0:

                ContentsObj contentsObj;
//                try {
//                    contentsObj = ContentsObj.getContentsOjbFromJson(new JSONObject(message.getMessage()).getJSONObject("contentsObj").toString());
                    contentsObj = ContentsObj.getContentsOjbFromJson(message.getMessage());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//               Log.d ("myPavilion","received uriListSize: "+ message.getFileUris().size());
                pr("custom RECEIVED CONTENTS: "+contentsObj.getJson());

                ServersListFragment.OnHeadlineSelectedListener h=clientActivity;
                ApInfo apInfo=new ApInfo("name of server","password","1111","2222");
//                ApInfo apInfo=clientActivity.connectedServerApInfo;
                ServerObj serverObj=new ServerObj(apInfo,contentsObj);
                pr("serverObj 1 passed:  "+serverObj.getJson());
                h.onServerSelected(serverObj);
                break;

            case 1:

                break;
        }
    }
    public void pr(String msg){
        Log.d("myPavilion",msg);
    }
}
