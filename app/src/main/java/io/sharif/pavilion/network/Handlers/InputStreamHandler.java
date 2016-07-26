package io.sharif.pavilion.network.Handlers;

import android.content.Context;
import android.net.Uri;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import io.sharif.pavilion.network.DataStructures.ClientDevice;
import io.sharif.pavilion.network.DataStructures.Message;
import io.sharif.pavilion.network.Listeners.ClientListener;
import io.sharif.pavilion.network.Listeners.ReceiveMessageListener;
import io.sharif.pavilion.network.Listeners.ServerListener;
import io.sharif.pavilion.network.Services.ClientService;
import io.sharif.pavilion.network.Services.ProgressMonitor;
import io.sharif.pavilion.network.Utilities.ActionResult;
import io.sharif.pavilion.network.Utilities.Utility;

public class InputStreamHandler extends Thread implements ProgressMonitor.GetMonitorData {

    public enum HandlerRole {
        SERVER,
        CLIENT
    }

    private AtomicLong totalLength, readBytes;
    private long fileReadBytes;
    private boolean folderIsPresent = true;
    private String baseAddress, fileName, fileExtension, connectedSSID, suffix;
    private ClientDevice clientDevice;
    private ClientService clientService;
    private byte[] buffer = new byte[Utility.getTcpSegmentSize()*3];
    private File folder, file;

    private final HandlerRole role;
    private final DataInputStream dataInputStream;
    private final ReceiveMessageListener receiveMessageListener;

    private ProgressMonitor progressMonitor;
    private ClientListener clientListener;
    private ServerListener serverListener;

    private final Context context;

    public InputStreamHandler(Context context,
                              DataInputStream dataInputStream,
                              ReceiveMessageListener receiveMessageListener,
                              ClientListener clientListener,
                              ClientService clientService,
                              String connectedSSID) {
        this.context = context;
        this.dataInputStream = dataInputStream;
        this.receiveMessageListener = receiveMessageListener;
        this.clientListener = clientListener;
        this.clientService = clientService;
        this.connectedSSID = connectedSSID;
        this.role = HandlerRole.CLIENT;
    }

    public InputStreamHandler(Context context,
                              DataInputStream dataInputStream,
                              ReceiveMessageListener receiveMessageListener,
                              ServerListener serverListener,
                              ClientDevice clientDevice) {
        this.context = context;
        this.dataInputStream = dataInputStream;
        this.receiveMessageListener = receiveMessageListener;
        this.serverListener = serverListener;
        this.clientDevice = clientDevice;
        this.role = HandlerRole.SERVER;
    }

    @Override
    public long getTotalBytes() {
        return totalLength.get();
    }

    @Override
    public long getSentBytes() {
        return readBytes.get();
    }

    @Override
    public void run() {

        if (dataInputStream == null) return;
        if (role == HandlerRole.SERVER && clientDevice == null) return;
        if (role == HandlerRole.CLIENT && connectedSSID == null) return;
        if (role == HandlerRole.CLIENT && clientService == null) return;

        try {

            totalLength = new AtomicLong();
            readBytes = new AtomicLong();
            progressMonitor = new ProgressMonitor(context, this, receiveMessageListener);

            int temp_int;
            boolean readFileResult;

            while (!Thread.currentThread().isInterrupted()) {

                temp_int = dataInputStream.readInt();

                readBytes.set(0);
                totalLength.set(0);
                fileReadBytes = 0;

                if (receiveMessageListener != null) {
                    Utility.postOnMainThread(context, new Runnable() {
                        @Override
                        public void run() {
                            receiveMessageListener.onReceiveStart();
                        }
                    });
                }

                final Message message = new Message(temp_int);

                totalLength.set(dataInputStream.readLong());

                progressMonitor.enableUpdate();

                message.setMessage(dataInputStream.readUTF());

                readBytes.addAndGet(message.getMessage().getBytes("UTF-8").length);

                // no race in if expression, as the other thread is not going readBytes or totalLenght values
                if (readBytes.get() < totalLength.get()) {

                    // it means that we have at least one file in message

                    if (role == HandlerRole.CLIENT) {
                        if (suffix == null) suffix = Utility.getServerName(connectedSSID);
                    }
                    else if (role == HandlerRole.SERVER)
                        if (suffix == null) suffix = String.valueOf(Utility.ipToLong(clientDevice.getIpAddr()));

                    if (baseAddress == null) baseAddress = Utility.getAppFolderPath() + suffix;

                    if (folder == null) folder = new File(baseAddress);

                    if (!folder.exists()) folderIsPresent = folder.mkdir();

                    if (folderIsPresent) {

                        while (readBytes.get() < totalLength.get()) { // again no race condition here

                            fileReadBytes = 0;

                            readFileResult = readFile();

                            if (readFileResult)
                                message.addUri(Uri.fromFile(file));

                        }
                    }
                }

                if (role == HandlerRole.CLIENT) {
                    if (clientListener != null)
                        Utility.postOnMainThread(context, new Runnable() {
                            @Override
                            public void run() {
                                clientListener.onMessageReceived(message);
                            }
                        });
                }
                else if (role == HandlerRole.SERVER)
                    if (serverListener != null)
                        Utility.postOnMainThread(context, new Runnable() {
                            @Override
                            public void run() {
                                serverListener.onMessageReceived(clientDevice.getID(), message);
                            }
                        });
            }

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();

            // TODO: check failure reason to whether call onReceivedFailure or onClientDisconnect

            if (role == HandlerRole.SERVER) {

                clientDevice.closetSocket();

                if (serverListener != null)
                    Utility.postOnMainThread(context, new Runnable() {
                        @Override
                        public void run() {
                            serverListener.onClientDisconnected(clientDevice);
                        }
                    });

            } else if (role == HandlerRole.CLIENT) {

                clientService.closeServerConnection();

                if (clientListener != null)
                    Utility.postOnMainThread(context, new Runnable() {
                        @Override
                        public void run() {
                            clientListener.onDisconnected();
                        }
                    });
            }

            if (receiveMessageListener != null)
                Utility.postOnMainThread(context, new Runnable() {
                    @Override
                    public void run() {
                        receiveMessageListener.onReceiveFailure(ActionResult.FAILURE);
                    }
                });

        } finally {
            progressMonitor.disableUpdate();
        }

    }

    private boolean readFile() throws IOException, NullPointerException {

        fileName = dataInputStream.readUTF();
        fileExtension = dataInputStream.readUTF();

        long temp_long = dataInputStream.readLong();

        file = new File(baseAddress + File.separator + fileName + fileExtension);

        setCreatableFile();

        boolean createFileResult = file.createNewFile();

        if (createFileResult) {

            FileOutputStream fileOutputStream = new FileOutputStream(file, false);

            int count;
            while (fileReadBytes < temp_long) {

                if ((count = dataInputStream.read(buffer)) == -1) {
                    fileOutputStream.close();
                    throw new IOException();
                }

                fileReadBytes += count;

                readBytes.addAndGet(count);

                fileOutputStream.write(buffer, 0, count);

            }

            fileOutputStream.close();

            return true;

        }

        return false;
    }

    private void setCreatableFile() {
        for (int copyCounter = 1; file.exists() ; copyCounter++) {
            file = new File(baseAddress
                    + File.separator + fileName + "_" + copyCounter
                    + fileExtension);
        }
    }
}