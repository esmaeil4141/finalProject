package io.sharif.pavilion.network.Services;

import android.content.Context;

import io.sharif.pavilion.network.Listeners.ReceiveMessageListener;
import io.sharif.pavilion.network.Listeners.SendMessageListener;
import io.sharif.pavilion.network.Utilities.Utility;

/**
 * This class is ued to monitor upload/download speed.
 */
public class ProgressMonitor {

    /**
     * Simple enum to know whether upload or download is monitored.
     */
    public enum MonitorRole {
        SENDER,
        RECEIVER
    }

    public static final int UPDATE_INTERVAL = 100; // update interval

    private ReceiveMessageListener receiveMessageListener;
    private SendMessageListener sendMessageListener;
    private GetMonitorData monitorData;
    private long total, current, diff;
    private MonitorRole role;
    private Context context;

    /**
     * disabled member is accessed by two threads so it must be declare as volatile.
     */
    private volatile boolean disabled;

    /**
     * Constructor for monitoring download process.
     * @param context application context
     * @param thread calling thread
     * @param receiveMessageListener listener to be called when message received
     */
    public ProgressMonitor(Context context, Thread thread, ReceiveMessageListener receiveMessageListener) {
        try {
            this.monitorData = (GetMonitorData) thread; // calling thread must implement GeMonitorData interface
            this.context = context;
            this.receiveMessageListener = receiveMessageListener;
            this.role = MonitorRole.RECEIVER;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor for monitoring upload process.
     * @param context application context
     * @param thread calling thread
     * @param sendMessageListener listener to be called when message received
     */
    public ProgressMonitor(Context context, Thread thread, SendMessageListener sendMessageListener) {
        try {
            this.monitorData = (GetMonitorData) thread; // calling thread must implement GeMonitorData interface
            this.context = context;
            this.sendMessageListener = sendMessageListener;
            this.role = MonitorRole.SENDER;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public void start() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                long before;

                while (!disabled) {

                    try {

                        Thread.sleep(UPDATE_INTERVAL);

                        before = current;

                        current = monitorData.getSentBytes();

                        diff = current - before; // number of bytes sent during UPDATE_INTERVAL

                        if (diff >= 0) {

                            if (role == MonitorRole.SENDER)
                                if (sendMessageListener != null)
                                    Utility.postOnMainThread(context, new Runnable() {
                                        @Override
                                        public void run() {
                                            float progress = Utility.calculateProgress(total, current);
                                            float speed = Utility.calculateSpeed(UPDATE_INTERVAL, diff);
                                            sendMessageListener.onProgress(progress, speed, total, current);
                                        }
                                    });
                            else if (role == MonitorRole.RECEIVER)
                                    if (receiveMessageListener != null)
                                        Utility.postOnMainThread(context, new Runnable() {
                                            @Override
                                            public void run() {
                                                float progress = Utility.calculateProgress(total, current);
                                                float speed = Utility.calculateSpeed(UPDATE_INTERVAL, diff);
                                                receiveMessageListener.onProgress(progress, speed, total, current);
                                            }
                                        });
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * This method is used by calling thread to start monitoring process.
     * It first gets total message size and then creates a new thread by calling start method
     * to start monitoring process.
     */
    public void enableUpdate() {
        disabled = false;
        if (monitorData == null) return;

        this.total = monitorData.getTotalBytes();
        this.current = monitorData.getSentBytes();

        if (total == 0 || current > total) return;

        if (role == MonitorRole.RECEIVER && receiveMessageListener == null) return;
        if (role == MonitorRole.SENDER && sendMessageListener == null) return;

        start();
    }

    /**
     * This method is used by calling thread to stop monitoring process.
     */
    public void disableUpdate() {
        disabled = true;
    }

    /**
     * This interface is used to retrieve data from calling thread,
     * so calling thread must implement this interface.
     */
    public interface GetMonitorData {

        /**
         * This method is used to get total message size in bytes.
         * @return total message size
         */
        long getTotalBytes();

        /**
         * This method is used to get sent bytes.
         * @return sent bytes
         */
        long getSentBytes();
    }

}