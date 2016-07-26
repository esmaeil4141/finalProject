package io.sharif.pavilion.network.Services;

import android.content.Context;
import android.net.Uri;
import android.support.v4.util.Pair;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import io.sharif.pavilion.network.DataStructures.Message;
import io.sharif.pavilion.network.Listeners.SendMessageListener;
import io.sharif.pavilion.network.Utilities.ActionResult;
import io.sharif.pavilion.network.Utilities.FileUtils;
import io.sharif.pavilion.network.Utilities.Utility;

public class MessageSender extends Thread implements ProgressMonitor.GetMonitorData {

    private final Message message;
    private final Context context;
    private final DataOutputStream dataOutputStream;
    private final SendMessageListener sendMessageListener;

    private long totalLength, bytesSent;

    private File file;
    private ProgressMonitor progressMonitor;

    public MessageSender(
            Context context,
            Message message,
            DataOutputStream dataOutputStream,
            SendMessageListener sendMessageListener) {
        this.context = context;
        this.message = message;
        this.dataOutputStream = dataOutputStream;
        this.sendMessageListener = sendMessageListener;
    }

    @Override
    public long getTotalBytes() {
        return totalLength;
    }

    @Override
    public long getSentBytes() {
        return bytesSent;
    }

    @Override
    public void run() {

        totalLength = Utility.getMessageTotalLength(message);

        if (totalLength > 0) {

            try {

                String address;

                bytesSent = 0;

                progressMonitor = new ProgressMonitor(this, sendMessageListener);
                progressMonitor.enableUpdate();

                dataOutputStream.writeInt(message.getID());
                dataOutputStream.writeLong(totalLength);

                if (message.getMessage() == null) message.setMessage("");

                String msg = message.getMessage();

                dataOutputStream.writeUTF(msg);
                bytesSent += msg.getBytes("UTF-8").length;

                if (message.getFileUris() != null) {

                    for (Uri uri : message.getFileUris()) {

                        if (uri != null) {

                            address = FileUtils.getPath(context, uri);
                            if (address != null)
                                if ((file = new File(address)).exists())
                                    sendFile();
                        }

                    }

                }

                if (sendMessageListener != null)
                    Utility.postOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            sendMessageListener.onMessageSent(message.getID());
                        }
                    });

            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
                if (sendMessageListener != null)
                    Utility.postOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            sendMessageListener.onFailure(ActionResult.FAILURE);
                        }
                    });
            }
        }

        progressMonitor.disableUpdate();
    }

    private boolean sendFile() throws IOException {

        if (file != null && dataOutputStream != null) {

            Pair<String, String> pair = FileUtils.splitFileNameExtension(file.getName());

            if (pair != null) {

                String fileName = pair.first;
                String extension = pair.second;

                if (fileName != null && extension != null) {

                    dataOutputStream.writeUTF(fileName);
                    dataOutputStream.writeUTF(extension);

                    dataOutputStream.writeLong(file.length());

                    FileInputStream fileInputStream = new FileInputStream(file);

                    byte[] buffer = new byte[Utility.getTcpSegmentSize()];

                    for (int len; (len = fileInputStream.read(buffer)) != -1;) {

                        dataOutputStream.write(buffer, 0, len);

                        bytesSent += len;
                    }

                    fileInputStream.close();

                    return true;
                }
            }

        }
        return false;
    }
}