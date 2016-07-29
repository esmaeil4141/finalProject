package io.sharif.pavilion.network.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import io.sharif.pavilion.network.DataStructures.ApInfo;
import io.sharif.pavilion.network.DataStructures.Message;
import io.sharif.pavilion.network.Handlers.InputStreamHandler;
import io.sharif.pavilion.network.Listeners.ClientListener;
import io.sharif.pavilion.network.Listeners.ReceiveMessageListener;
import io.sharif.pavilion.network.Listeners.SendMessageListener;
import io.sharif.pavilion.network.Listeners.WifiListener;
import io.sharif.pavilion.network.Listeners.WifiScanListener;
import io.sharif.pavilion.network.Utilities.ActionResult;
import io.sharif.pavilion.network.Utilities.Utility;

/**
 * This class is used to provide services for clients.
 */
public class ClientService extends BroadcastReceiver {

    private final ReceiveMessageListener receiveMessageListener;
    private final WifiScanListener wifiScanListener;
    private final IntentFilter wifiIntentFilter;
    private final ClientListener clientListener;
    private final WifiListener wifiListener;
    private final WifiManager wifiManager;
    private final Context context;

    private DataOutputStream serverDataOutputStream;
    private DataInputStream serverDataInputStream;
    private Socket serverSocket;

    /**
     * connectedSSID and serverIP are accessed in multiple threads simultaneously thus they are
     * declared as volatile to ensure all reads see the earlier write.(Memory Visibility)
     */
    private volatile String connectedSSID, serverIP;

    private boolean receiverRegistered, scanRequested;
    private State networkCurrentState;
    private int networkID = -1; // currently connected wifi network ID

    public ClientService(Context context,
                         WifiScanListener wifiScanListener,
                         ClientListener clientListener,
                         WifiListener wifiListener,
                         ReceiveMessageListener receiveMessageListener) {
        this.context = context;
        this.receiveMessageListener = receiveMessageListener;
        this.wifiScanListener = wifiScanListener;
        this.clientListener = clientListener;
        this.wifiListener = wifiListener;
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        this.wifiIntentFilter = new IntentFilter();
        this.wifiIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        this.wifiIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        this.wifiIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    }

    /**
     * This method is used to registers receivers for client service.
     * @return {@code SUCCESS} if operation succeeds, {@code FAILURE} otherwise
     */
    public ActionResult start() {
        if (!receiverRegistered) {
            receiverRegistered = true;
            context.registerReceiver(this, wifiIntentFilter);
            return ActionResult.SUCCESS;
        }
        return ActionResult.FAILURE;
    }

    /**
     * This method is used to unregister receivers for client service.
     * @return {@code SUCCESS} if operation succeeds, {@code FAILURE} otherwise
     */
    public ActionResult stop() {
        if (receiverRegistered) {
            receiverRegistered = false;
            context.unregisterReceiver(this);
            return ActionResult.SUCCESS;
        }
        return ActionResult.FAILURE;
    }

    /**
     * This method is used to obtain gateway (server) IP address when connected to a wifi network.
     * @return gateway (server) IP address
     */
    @SuppressWarnings("deprecation")
    public String obtainServerIP() {
        String ip = null;
        if (wifiManager != null) {
            DhcpInfo dhcp = wifiManager.getDhcpInfo();
            if (dhcp != null)
                ip = Formatter.formatIpAddress(dhcp.gateway);
        }
        return ip;
    }

    /**
     * This method is used to connect to server socket.
     * @return {@code SUCCESS} if server IP is not null, {@code FAILURE} otherwise
     */
    public ActionResult createServerConnection() {

        if (serverIP == null) return ActionResult.FAILURE;

        final ClientService that = this;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new Socket(serverIP, ServerService.SERVER_PORT);
                    serverDataInputStream = new DataInputStream(serverSocket.getInputStream());
                    serverDataOutputStream = new DataOutputStream(serverSocket.getOutputStream());

                    // create a handler to handle server input stream
                    new InputStreamHandler(
                            context,
                            serverDataInputStream,
                            receiveMessageListener,
                            clientListener,
                            that,
                            connectedSSID
                    ).start();

                    if (clientListener != null)
                        Utility.postOnMainThread(context, new Runnable() {
                            @Override
                            public void run() {
                                clientListener.onConnected();
                            }
                        });

                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                    if (clientListener != null)
                        Utility.postOnMainThread(context, new Runnable() {
                            @Override
                            public void run() {
                                clientListener.onConnectionFailure();
                            }
                        });
                }
            }
        }).start();

        return ActionResult.SUCCESS;
    }

    /**
     * This method is used to close connection to server socket. It first closes input & output streams and
     * the socket, and finally assigns null to all of them.
     */
    public void closeServerConnection() {

        try {
            if (serverDataInputStream != null) serverDataInputStream.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        try {
            if (serverDataOutputStream != null) serverDataOutputStream.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        serverDataInputStream = null;
        serverDataOutputStream = null;
        serverSocket = null;
    }

    /**
     * This method is used to obtain ssid of currently connected wifi network.
     * @return connected wifi network ssid
     */
    private String getWifiName() {
        if (wifiManager != null && wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
                if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    String ssid = wifiInfo.getSSID();
                    if (ssid != null && ssid.startsWith("\""))
                        ssid = ssid.substring(1, ssid.length()-1);
                    return ssid;
                }
            }
        }
        return null;
    }

    /**
     * This method is used to register a request for wifi scan.
     * @return {@code SUCCESS} if operation succeeds, {@code FAILURE} otherwise
     */
    public ActionResult scan() {
        if (wifiManager == null || scanRequested) return ActionResult.FAILURE;
        scanRequested = wifiManager.startScan();
        return scanRequested ? ActionResult.SUCCESS : ActionResult.FAILURE;
    }

    /**
     * This method is used to check whether access point info is valid.
     * @param apInfo apInfo to check
     * @return {@code false} if ssid or password is null or empty, {@code true} otherwise
     */
    private boolean isApInfoValid(ApInfo apInfo) {
        return apInfo != null
                && apInfo.getSSID() != null && !apInfo.getSSID().trim().equals("")
                && apInfo.getPassword() != null && !apInfo.getPassword().trim().equals("");
    }

    /**
     * This method is used to connect to a wifi network.
     * @param apInfo server to connect to
     * @return {@code FAILURE} if wifiManger is null or access point info is not valid, {@code SUCCESS} otherwise
     */
    public ActionResult join(ApInfo apInfo) {

        if (wifiManager == null || !isApInfoValid(apInfo)) return ActionResult.FAILURE;

        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", apInfo.getSSID());
        wifiConfig.preSharedKey = String.format("\"%s\"", apInfo.getPassword());

        networkID = wifiManager.addNetwork(wifiConfig);

        return wifiManager.disconnect()
                && wifiManager.enableNetwork(networkID, true)
                && wifiManager.reconnect() ? ActionResult.SUCCESS : ActionResult.FAILURE;
    }

    /**
     * This method is used to leave currently connected wifi network.
     * @return {@code SUCCESS} if operation succeeds, {@code FAILURE} otherwise
     */
    public ActionResult leave() {
        return wifiManager != null && networkID != -1 && wifiManager.removeNetwork(networkID)
                && wifiManager.saveConfiguration() ? ActionResult.SUCCESS : ActionResult.FAILURE;
    }

    /**
     * This method is used to send message to server.
     * @param message message to send to server
     * @param sendMessageListener send message listener
     * @return {@code FAILURE} if message is null or output stream is not initialized, {@code SUCCESS} otherwise
     */
    public ActionResult sendMessage(Message message, SendMessageListener sendMessageListener) {

        if (message == null || serverDataOutputStream == null) return ActionResult.FAILURE;

        new MessageSender(
                context,
                message,
                serverDataOutputStream,
                sendMessageListener
        ).start();

        return ActionResult.SUCCESS;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (wifiManager == null) return;

        String action = intent.getAction();

        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

            if (networkInfo != null) {

                State state = networkInfo.getState();

                if (networkCurrentState == state) {
                    // ignore multiple identical events
                } else {
                    networkCurrentState = state;
                    if (state == State.CONNECTED) {
                        connectedSSID = getWifiName();
                        if (connectedSSID != null && connectedSSID.startsWith(ServerService.SSID_PREFIX)) {
                            // already connected to a server which is created with the same app
                            serverIP = obtainServerIP();
                            if (clientListener != null)
                                clientListener.onJoinedGroup();
                        }
                    } else if (state == State.DISCONNECTED) {
                        if (connectedSSID != null && connectedSSID.startsWith(ServerService.SSID_PREFIX)) {
                            // already disconnected from a server which is created with the same app
                            connectedSSID = null;
                            serverIP = null;
                            if (clientListener != null)
                                clientListener.onLeftGroup();
                        }
                    }
                }
            }

        } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {

            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
            switch (wifiState) {
                case 0: break; // WiFi disabling
                case 1: if (wifiListener != null) wifiListener.onWifiDisabled(); break;
                case 2: break; // WiFi enabling
                case 3: if (wifiListener != null) wifiListener.onWifiEnabled(); break;
                case 4: if (wifiListener != null) wifiListener.onFailure(ActionResult.UNKNOWN); break;
            }

        } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {

            // check whether user has registered scan request or scan result is from system automatic scan
           Log.d("myPavilion","inside wifi receive");
            if (wifiScanListener != null && scanRequested) {
                Log.d("myPavilion","inside scanRequested");

                scanRequested = false;
                List<ScanResult> scanResults = wifiManager.getScanResults();
                List<ApInfo> serversList = new ArrayList<>();

                String ssid, name;
                Log.d("myPavilion","scanResults size:"+scanResults.size());

                for (ScanResult result : scanResults) {
                    ssid = result.SSID;
                    // filter wifi networks which are created with the same app
                    if (ssid.startsWith(ServerService.SSID_PREFIX)) {
                        name = Utility.getServerName(ssid);
                        ApInfo apInfo = new ApInfo(
                                name,
                                Utility.generatePassword(result.SSID),
                                result.BSSID,
                                result.SSID);
                        serversList.add(apInfo);
                    }
                }
                Log.d("myPavilion","scanResult...size:"+scanResults.size());
                Log.d("myPavilion","inside servers scaniing...size:"+serversList.size());
for(ApInfo apInfo:serversList) Log.d("myPavilion",apInfo.getName());
                wifiScanListener.onWifiScanFinished(serversList);

            }
        }
    }
}