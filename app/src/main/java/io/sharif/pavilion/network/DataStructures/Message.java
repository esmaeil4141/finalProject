package io.sharif.pavilion.network.DataStructures;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class Message {

    private int ID;

    private String message;

    private List<Uri> fileUris;

    public Message(int ID) {
        this.ID = ID;
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
        if (fileUris == null) fileUris = new ArrayList<>();
        fileUris.add(uri);
    }

    public synchronized boolean removeUri(Uri uri) {
        if (fileUris == null) return false;
        return fileUris.remove(uri);
    }
}