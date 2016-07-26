package io.sharif.pavilion.network.Utilities;

public enum ActionResult {

    SUCCESS,					// common success
    FAILURE,					// common failure
    JOIN_SUCCESSFUL,			// successful connection to WiFi network
    JOIN_FAILED,				// failed to connect to WiFi network
    CONNECTION_SUCCESSFUL,		// successful connection to server socket
    CONNECTION_FAILED,			// failed to connect to server socket
    LEAVE_SUCCESSFUL,			// successfully left WiFi network
    LEAVE_FAILED,				// failed to leave WiFi network
    UNKNOWN                     // unknown state

}