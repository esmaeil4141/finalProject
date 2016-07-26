package io.sharif.pavilion.network.DataStructures;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientDevice {

    private volatile String ipAddr, hWAddr;
    private volatile boolean isReachable;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Socket socket;

    public ClientDevice(String ipAddr, String hWAddr, boolean isReachable) {
        this.ipAddr = ipAddr;
        this.hWAddr = hWAddr;
        this.isReachable = isReachable;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public boolean isReachable() {
        return isReachable;
    }

    public void setReachable(boolean isReachable) {
        this.isReachable = isReachable;
    }

    public String getID() {
        return hWAddr;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

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