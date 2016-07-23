package io.sharif.pavilion.network.DataStructures;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientDevice {

    private String ipAddr;
    private String hWAddr;
    private boolean isReachable;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

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

    public String getHWAddr() {
        return hWAddr;
    }

    public boolean isReachable() {
        return isReachable;
    }
    public void setReachable(boolean isReachable) {
        this.isReachable = isReachable;
    }

    // MAC address is used as ID
    public String getID() {
        return hWAddr;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public synchronized InputStream getInputStream() {
        if (inputStream != null) return inputStream;
        if (socket != null) {
            try {
                return inputStream = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public synchronized OutputStream getOutputStream() {
        if (outputStream != null) return outputStream;
        if (socket != null) {
            try {
                return outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void closetSocket() {
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        } finally {
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

}