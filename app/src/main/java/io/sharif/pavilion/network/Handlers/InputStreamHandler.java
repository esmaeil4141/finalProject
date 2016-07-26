package io.sharif.pavilion.network.Handlers;

import android.net.Uri;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

    private long totalLength, readBytes, fileReadBytes;
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

    public InputStreamHandler(DataInputStream dataInputStream,
                              ReceiveMessageListener receiveMessageListener,
                              ClientListener clientListener,
                              ClientService clientService,
                              String connectedSSID) {
        this.dataInputStream = dataInputStream;
        this.receiveMessageListener = receiveMessageListener;
        this.clientListener = clientListener;
        this.clientService = clientService;
        this.connectedSSID = connectedSSID;
        this.role = HandlerRole.CLIENT;
    }

    public InputStreamHandler(DataInputStream dataInputStream,
                              ReceiveMessageListener receiveMessageListener,
                              ServerListener serverListener,
                              ClientDevice clientDevice) {
        this.dataInputStream = dataInputStream;
        this.receiveMessageListener = receiveMessageListener;
        this.serverListener = serverListener;
        this.clientDevice = clientDevice;
        this.role = HandlerRole.SERVER;
    }

    @Override
    public long getTotalBytes() {
        return totalLength;
    }

    @Override
    public long getSentBytes() {
        return readBytes;
    }

    @Override
    public void run() {

        if (dataInputStream == null) return;
        if (role == HandlerRole.SERVER && clientDevice == null) return;
        if (role == HandlerRole.CLIENT && connectedSSID == null) return;
        if (role == HandlerRole.CLIENT && clientService == null) return;

        try {

            progressMonitor = new ProgressMonitor(this, receiveMessageListener);

            int temp_int;
            boolean readFileResult;

            while (!Thread.currentThread().isInterrupted()) {

                temp_int = dataInputStream.readInt();

                readBytes = totalLength = fileReadBytes = 0;

                if (receiveMessageListener != null) {
                    Utility.postOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            receiveMessageListener.onReceiveStart();
                        }
                    });
                }

                final Message message = new Message(temp_int);

                totalLength = dataInputStream.readLong();

                progressMonitor.enableUpdate();

                message.setMessage(dataInputStream.readUTF());

                readBytes += message.getMessage().getBytes("UTF-8").length;

                if (readBytes < totalLength) { // it means that we have at least one file in message

                    if (role == HandlerRole.CLIENT)
                        if (suffix == null) suffix = Utility.getServerName(connectedSSID);
                    else if (role == HandlerRole.SERVER)
                        if (suffix == null) suffix = String.valueOf(Utility.ipToLong(clientDevice.getIpAddr()));

                    if (baseAddress == null) baseAddress = Utility.getAppFolderPath() + suffix;

                    if (folder == null) folder = new File(baseAddress);

                    if (!folder.exists()) folderIsPresent = folder.mkdir();

                    if (folderIsPresent) {

                        while (readBytes < totalLength) {

                            fileReadBytes = 0;

                            readFileResult = readFile();

                            if (readFileResult)
                                message.addUri(Uri.fromFile(file));

                        }
                    }
                }

                if (role == HandlerRole.CLIENT)
                    if (clientListener != null)
                        Utility.postOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                clientListener.onMessageReceived(message);
                            }
                        });
                else if (role == HandlerRole.SERVER)
                    if (serverListener != null)
                        Utility.postOnMainThread(new Runnable() {
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
                    Utility.postOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            serverListener.onClientDisconnected(clientDevice);
                        }
                    });

            } else if (role == HandlerRole.CLIENT) {

                clientService.closeServerConnection();

                if (clientListener != null)
                    Utility.postOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            clientListener.onDisconnected();
                        }
                    });
            }

            if (receiveMessageListener != null)
                Utility.postOnMainThread(new Runnable() {
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

                readBytes += count;

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