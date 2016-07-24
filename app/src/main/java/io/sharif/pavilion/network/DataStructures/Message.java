package io.sharif.pavilion.network.DataStructures;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class Message {

    private final Object uRIListLock;
    private List<Uri> fileUris;
    private String message;
    private int ID;

    public Message(int ID) {
        this.ID = ID;
        this.uRIListLock = new Object();
    }

    public int getID() {
        return ID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Uri> getFileUris() {
        return fileUris;
    }

    public synchronized void addUri(Uri uri) {
        synchronized (uRIListLock) {
            if (fileUris == null) fileUris = new ArrayList<>();
            fileUris.add(uri);
        }
    }

    public synchronized boolean removeUri(Uri uri) {
        synchronized (uRIListLock) {
            return fileUris != null && fileUris.remove(uri);
        }
    }
}