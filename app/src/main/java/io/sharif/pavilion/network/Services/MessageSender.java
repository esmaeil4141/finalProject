package io.sharif.pavilion.network.Services;

import android.content.Context;
import android.net.Uri;
import android.support.v4.util.Pair;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

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

    private AtomicLong totalLength, bytesSent;

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
        this.totalLength = new AtomicLong();
        this.bytesSent = new AtomicLong();
    }

    @Override
    public long getTotalBytes() {
        return totalLength.get();
    }

    @Override
    public long getSentBytes() {
        return bytesSent.get();
    }

    @Override
    public void run() {

        totalLength.set(Utility.getMessageTotalLength(message));

        if (totalLength.get() > 0) { // no race condition here so no need to make the expression atomic

            try {

                String address;

                bytesSent.set(0);

                progressMonitor = new ProgressMonitor(this, sendMessageListener);
                progressMonitor.enableUpdate();

                dataOutputStream.writeInt(message.getID());
                dataOutputStream.writeLong(totalLength.get());

                if (message.getMessage() == null) message.setMessage("");

                String msg = message.getMessage();

                dataOutputStream.writeUTF(msg);
                bytesSent.addAndGet(msg.getBytes("UTF-8").length);

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
            } finally {
                progressMonitor.disableUpdate();
            }
        }
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

                        bytesSent.addAndGet(len);
                    }

                    fileInputStream.close();

                    return true;
                }
            }

        }
        return false;
    }
}