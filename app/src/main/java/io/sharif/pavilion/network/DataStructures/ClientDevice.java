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

    private final Object outputStreamLock;
    private final Object inputStreamLock;
    private final Object closeSocketLock;

    public ClientDevice(String ipAddr, String hWAddr, boolean isReachable) {
        this.ipAddr = ipAddr;
        this.hWAddr = hWAddr;
        this.isReachable = isReachable;
        this.outputStreamLock = new Object();
        this.inputStreamLock = new Object();
        this.closeSocketLock = new Object();
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
        synchronized (closeSocketLock) {
            synchronized (inputStreamLock) {
                synchronized (outputStreamLock) {
                    this.socket = socket;
                }
            }
        }
    }

    public InputStream getInputStream() {
        synchronized (closeSocketLock) {
            synchronized (inputStreamLock) {
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
        }
    }

    public OutputStream getOutputStream() {
        synchronized (closeSocketLock) {
            synchronized (outputStreamLock) {
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
        }
    }

    public void closetSocket() {
        synchronized (closeSocketLock) {
            synchronized (inputStreamLock) {
                synchronized (outputStreamLock) {
                    try {
                        if (inputStream != null) inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        if (outputStream != null) outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        if (socket != null) socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    inputStream = null;
                    outputStream = null;
                    socket = null;
                }
            }
        }
    }
}