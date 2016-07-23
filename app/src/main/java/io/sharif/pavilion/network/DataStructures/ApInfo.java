package io.sharif.pavilion.network.DataStructures;

public class ApInfo {

    private final String name, password, BSSID, SSID;

    public ApInfo(String name, String password, String BSSID, String SSID) {
        this.name = name;
        this.password = password;
        this.BSSID = BSSID;
        this.SSID = SSID;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getBSSID() {
        return BSSID;
    }

    public String getSSID() {
        return SSID;
    }

}