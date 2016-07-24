package io.sharif.pavilion.network.Services;

import io.sharif.pavilion.network.Listeners.ReceiveMessageListener;
import io.sharif.pavilion.network.Listeners.SendMessageListener;
import io.sharif.pavilion.network.Utilities.Utility;

public class ProgressMonitor {

    public enum MonitorRole {
        SENDER,
        RECEIVER
    }

    private long total, current, diff;
    private GetMonitorData monitorData;
    private volatile boolean disabled;
    private ReceiveMessageListener receiveMessageListener;
    private SendMessageListener sendMessageListener;
    private MonitorRole role;

    public static long UPDATE_INTERVAL = 100;

    public ProgressMonitor(Thread thread, ReceiveMessageListener receiveMessageListener) {
        try {
            this.monitorData = (GetMonitorData) thread;
            this.receiveMessageListener = receiveMessageListener;
            this.role = MonitorRole.RECEIVER;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    public ProgressMonitor(Thread thread, SendMessageListener sendMessageListener) {
        try {
            this.monitorData = (GetMonitorData) thread;
            this.sendMessageListener = sendMessageListener;
            this.role = MonitorRole.SENDER;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

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

                        diff = current - before;

                        if (diff >= 0) {

                            if (role == MonitorRole.SENDER) {
                                if (sendMessageListener != null) {
                                    Utility.postOnMainThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            float progress = Utility.calculateProgress(total, current);
                                            float speed = Utility.calculateSpeed(UPDATE_INTERVAL, diff);
                                            sendMessageListener.onProgress(progress, speed, total, current);
                                        }
                                    });
                                }
                            }

                            if (role == MonitorRole.RECEIVER) {
                                if (receiveMessageListener != null) {
                                    Utility.postOnMainThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            float progress = Utility.calculateProgress(total, current);
                                            float speed = Utility.calculateSpeed(UPDATE_INTERVAL, diff);
                                            receiveMessageListener.onProgress(progress, speed, total, current);
                                        }
                                    });
                                }
                            }

                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void enableUpdate() {
        disabled = false;
        if (monitorData != null) {
            this.total = monitorData.getTotalBytes();
            this.current = monitorData.getSentBytes();
            if (total != 0 && current <= total &&
                    (role == MonitorRole.RECEIVER && receiveMessageListener != null) ||
                    (role == MonitorRole.SENDER && sendMessageListener != null)
                    )
                start();
        }
    }

    public void disableUpdate() {
        disabled = true;
    }

    public interface GetMonitorData {

        long getSentBytes();
        long getTotalBytes();

    }

}