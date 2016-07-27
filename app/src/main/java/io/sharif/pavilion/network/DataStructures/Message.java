package io.sharif.pavilion.network.DataStructures;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds message text and files, which is transferred between client and server.
 */
public class Message {

    private List<Uri> fileUris; // holds URIs of message files
    private String message; // text message
    private int ID; // message ID

    /**
     * @param ID message ID, typically a random number.
     */
    public Message(int ID) {
        this.ID = ID;
    }

    /**
     * This method return message ID.
     * @return message ID
     */
    public int getID() {
        return ID;
    }

    /**
     * This method returns text message.
     * @return text message
     */
    public String getMessage() {
        return message;
    }

    /**
     * This method sets text message.
     * @param message text message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * This method returns the list of file URIs.
     * @return file URIs list
     */
    public List<Uri> getFileUris() {
        return fileUris;
    }

    /**
     * This method first creates initializes URIs list if it's not already initialized and adds the URI
     * to the current list. Also addition is synchronized with removal.
     * @param uri URI to add
     */
    public synchronized void addUri(Uri uri) {
        if (fileUris == null) fileUris = new ArrayList<>();
        fileUris.add(uri);
    }

    /**
     * This method removes the input URI from the list. Also removal is synchronized with addition.
     * @param uri URI to remove
     * @return {@code true} if the operation succeeds, {@code false} otherwise
     */
    public synchronized boolean removeUri(Uri uri) {
        return fileUris != null && fileUris.remove(uri);
    }
}