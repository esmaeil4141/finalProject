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

/**
 * This class is used to extract Message from socket input stream.
 */
public class InputStreamHandler extends Thread implements ProgressMonitor.GetMonitorData {

    /**
     * Simple enum to hold handler role to be able to call correct callbacks.
     */
    public enum HandlerRole {
        SERVER,
        CLIENT
    }

    private byte[] buffer = new byte[Utility.getTcpSegmentSize()*3]; // input stream buffer
    private String baseAddress, fileName, fileExtension, suffix;

    /**
     * There are two threads reading and writing these members so they must be declared volatile
     * to ensure that all reads see the earlier write.(Memory Visibility)
     * Also operations on them must be atomic so AtomicLong is used.
     */
    private volatile AtomicLong totalLength, readBytes;

    private ProgressMonitor progressMonitor; // this class is used calculate download speed
    private boolean folderIsPresent = true; // indicates existence of app folder, where received files are stored
    private long fileReadBytes;
    private File folder, file;

    private ClientListener clientListener;
    private ClientService clientService;
    private String connectedSSID; // currently connected wifi network name

    private ServerListener serverListener;
    private ClientDevice clientDevice;

    private final ReceiveMessageListener receiveMessageListener; // received message are delivered to receive listener
    private final DataInputStream dataInputStream; // socket input stream wrapped in DataInputStream
    private final HandlerRole role;
    private final Context context;

    /**
     * Public constructor for handling server input stream in client service
     * @param context application context
     * @param dataInputStream input stream to read data from
     * @param receiveMessageListener listener being called on message receive
     * @param clientListener client service callbacks
     * @param clientService client service
     * @param connectedSSID currently connected wifi network name
     */
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

    /**
     * Public constructor for handling client input stream in server service
     * @param context application context
     * @param dataInputStream input stream to read data from
     * @param receiveMessageListener listener being called on message receive
     * @param serverListener server service callbacks
     * @param clientDevice client which it's input stream is going to be read
     */
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
        return readBytes.get();
    }

    @Override
    public void run() {

        // check if input data is not null
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


                temp_int = dataInputStream.readInt(); // first int number is message ID

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

                final Message message = new Message(temp_int); // create a message object with received ID

                totalLength.set(dataInputStream.readLong()); // next long number is message total length

                progressMonitor.enableUpdate(); // start progressMonitor to calculate download speed

                message.setMessage(dataInputStream.readUTF()); // next UTF string is text message

                // increment read bytes by the length of received text message (atomic operation)
                readBytes.addAndGet(message.getMessage().getBytes("UTF-8").length);

                // no race in if expression, as the other thread is not going to change readBytes or totalLength values
                if (readBytes.get() < totalLength.get()) {

                    // by entering here it means that we have at least one file in message

                    if (role == HandlerRole.CLIENT) {
                        // received files in client are stored in : /(appFolderPath)/serverName/
                        if (suffix == null) suffix = Utility.getServerName(connectedSSID);
                    }
                    else if (role == HandlerRole.SERVER) {
                        // received files in server are stored in : /(appFolderPath)/(int_value_of_client_IP_address)/
                        if (suffix == null) suffix = String.valueOf(Utility.ipToLong(clientDevice.getIpAddr()));
                    }

                    if (baseAddress == null) baseAddress = Utility.getAppFolderPath() + suffix;

                    if (folder == null) folder = new File(baseAddress);

                    if (!folder.exists()) folderIsPresent = folder.mkdir();

                    if (folderIsPresent) {

                        while (readBytes.get() < totalLength.get()) { // again no race condition here

                            fileReadBytes = 0;

                            readFileResult = readFile();

                            if (readFileResult)
                                message.addUri(Uri.fromFile(file)); // add file URI to message URI list

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

    /**
     * This method is used to read a file from input stream.
     * @return {@code true} if operation succeeds, {@code false} otherwise
     * @throws IOException if peer closes the connection before fully receiving the file.
     * @throws NullPointerException if socket is closed in the middle of reading.
     */
    private boolean readFile() throws IOException, NullPointerException {

        fileName = dataInputStream.readUTF(); // next UTF string is current file name string
        fileExtension = dataInputStream.readUTF(); // next UTF string is current file extension such as ".pdf"

        long temp_long = dataInputStream.readLong(); // next long is current file size

        file = new File(baseAddress + File.separator + fileName + fileExtension);

        setCreatableFile(); // check if the file already exists

        boolean createFileResult = file.createNewFile();

        if (createFileResult) {

            FileOutputStream fileOutputStream = new FileOutputStream(file, false);

            int count;
            while (fileReadBytes < temp_long) {

                if ((count = dataInputStream.read(buffer)) == -1) {
                    // peer closed the connection
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

    /**
     * This method is used to prevent file overwrites. It check for existence of {@code file} and appends
     * a string i.e. _number to end of file name if it already exists.
     */
    private void setCreatableFile() {
        for (int copyCounter = 1; file.exists() ; copyCounter++) {
            file = new File(baseAddress
                    + File.separator + fileName + "_" + copyCounter
                    + fileExtension);
        }
    }
}