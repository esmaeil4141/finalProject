package io.sharif.pavilion.network.DataStructures;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Server service uses this class to represent individual client.
 */
public class ClientDevice {

    /**
     * Multiple threads read and write IP & MAC addresses simultaneously, thus it is good to
     * mark them as volatile to guarantee that reads see the earlier write. (Memory Visibility)
     */
    private volatile String ipAddr, hWAddr;

    /**
     * Multiple threads read and write isReachable attribute simultaneously,
     * thus it needs to be declared as volatile.
     */
    private volatile boolean isReachable;

    private OutputStream outputStream;
    private InputStream inputStream;
    private Socket socket;

    /**
     * @param ipAddr IP address
     * @param hWAddr MAC address
     * @param isReachable whether client is reachable
     */
    public ClientDevice(String ipAddr, String hWAddr, boolean isReachable) {
        this.ipAddr = ipAddr;
        this.hWAddr = hWAddr;
        this.isReachable = isReachable;
    }

    /**
     * This method is used to set the IP address of client.
     * @param ipAddr client IP address
     */
    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    /**
     * This method returns client IP address.
     * @return client IP address
     */
    public String getIpAddr() {
        return ipAddr;
    }

    /**
     * This method is used to decide whether client is reachable.
     * @return {@code true} if client is reachable, {@code false} otherwise
     */
    public boolean isReachable() {
        return isReachable;
    }

    /**
     * This method is used to change client accessibility state.
     * @param isReachable new state
     */
    public void setReachable(boolean isReachable) {
        this.isReachable = isReachable;
    }

    /**
     * This method returns client ID as a string. By now, client MAC address is used as ID.
     * @return client ID
     */
    public String getID() {
        return hWAddr;
    }

    /**
     * This method sets client socket.
     * @param socket client socket
     */
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     * This method lazy initializes socket input stream and returns the stream.
     * @return {@code InputStream} if socket is not null and no IOException or NullPointerException occur.
     */
    public InputStream getInputStream() {
        if (inputStream != null) return inputStream;
        if (socket != null) {
            try {
                // getInputStream returns the same object on multiple calls,
                // so lazy initialization is not a problem here
                return inputStream = socket.getInputStream();
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * This method lazy initializes socket output stream and returns the stream.
     * @return {@code OutputStream} if socket is not null and no IOException or NullPointerException occur.
     */
    public OutputStream getOutputStream() {
        if (outputStream != null) return outputStream;
        if (socket != null) {
            try {
                return outputStream = socket.getOutputStream();
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * This method is used to close client socket. It first closes input & output streams and the socket,
     * and eventually sets all of them to null.
     */
    public void closetSocket() {
        try {
            if (inputStream != null) inputStream.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        try {
            if (outputStream != null) outputStream.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        try {
            if (socket != null) socket.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        inputStream = null;
        outputStream = null;
        socket = null;
    }
}