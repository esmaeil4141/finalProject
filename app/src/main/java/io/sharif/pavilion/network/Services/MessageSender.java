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

/**
 * This class is used to send messages.
 */
public class MessageSender extends Thread implements ProgressMonitor.GetMonitorData {

    private final SendMessageListener sendMessageListener;
    private final DataOutputStream dataOutputStream;
    private ProgressMonitor progressMonitor; // progress monitor to calculate upload speed
    private final Message message;
    private final Context context;
    private File file;

    /**
     * There are two threads reading and writing these members so they must be declared volatile
     * to ensure that all reads see the earlier write.(Memory Visibility)
     * Also operations on them must be atomic so the AtomicLong is used.
     */
    private volatile AtomicLong totalLength, bytesSent;

    /**
     * @param context application context
     * @param message message to send
     * @param dataOutputStream output stream to write message on
     * @param sendMessageListener send message listener
     */
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

    /**
     * This method provides total message length for ProgressMonitor object.
     * @return total message length
     */
    @Override
    public long getTotalBytes() {
        return totalLength.get();
    }

    /**
     * This method provides total read bytes for ProgressMonitor object.
     * @return total read bytes by the time of calling
     */
    @Override
    public long getSentBytes() {
        return bytesSent.get();
    }

    @Override
    public void run() {

        totalLength.set(Utility.getMessageTotalLength(context, message));

        if (totalLength.get() > 0) { // no race condition here so no need to make the expression atomic

            try {

                String address;

                bytesSent.set(0);

                progressMonitor = new ProgressMonitor(context, this, sendMessageListener);
                progressMonitor.enableUpdate(); // start progress monitor to calculate upload speed

                dataOutputStream.writeInt(message.getID()); // first send message ID
                dataOutputStream.writeLong(totalLength.get()); // next is message total length

                if (message.getMessage() == null) message.setMessage("");

                String msg = message.getMessage();

                dataOutputStream.writeUTF(msg); // next is text message
                bytesSent.addAndGet(msg.getBytes("UTF-8").length); // increment sent bytes (atomic)

                // sending files if any
                if (message.getFileUris() != null) {

                    for (Uri uri : message.getFileUris()) {

                        if (uri != null) {

                            address = FileUtils.getPath(context, uri); // get path from uri
                            if (address != null)
                                if ((file = new File(address)).exists())
                                    sendFile();
                        }

                    }

                }

                if (sendMessageListener != null)
                    Utility.postOnMainThread(context, new Runnable() {
                        @Override
                        public void run() {
                            sendMessageListener.onMessageSent(message.getID());
                        }
                    });

            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
                if (sendMessageListener != null)
                    Utility.postOnMainThread(context, new Runnable() {
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

    /**
     * This method is used to write a file on an output stream.
     * @return {@code true} if operation succeeds, {@code false} otherwise
     * @throws IOException when other peer closes the connection
     */
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