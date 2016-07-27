package io.sharif.pavilion.network.DataStructures;

/**
 * This class is used to store server info that is created by the same app.
 */
public class ApInfo {

    private final String name, password, BSSID, SSID;

    /**
     * @param name server name
     * @param password server password
     * @param BSSID server MAC address
     * @param SSID server wifi name
     */
    public ApInfo(String name, String password, String BSSID, String SSID) {
        this.name = name;
        this.password = password;
        this.BSSID = BSSID;
        this.SSID = SSID;
    }

    /**
     * This method returns server name that is shown to users.
     * @return server name
     */
    public String getName() {
        return name;
    }

    /**
     * This method returns the password that is needed to connect to server wifi network.
     * @return the password for server wifi network
     */
    public String getPassword() {
        return password;
    }

    /**
     * This method returns server MAC address as a string.
     * @return server MAC address
     */
    public String getBSSID() {
        return BSSID;
    }

    /**
     * This method returns server wifi network name.
     * @return server wifi name
     */
    public String getSSID() {
        return SSID;
    }

}