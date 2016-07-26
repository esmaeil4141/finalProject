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
    private volatile String connectedSSID, serverIP;
    private Socket serverSocket;

    private State networkCurrentState;

    private boolean receiverRegistered, scanRequested;
    private int networkID = -1;

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

    public ActionResult start() {
        if (!receiverRegistered) {
            receiverRegistered = true;
            context.registerReceiver(this, wifiIntentFilter);
        }
        return ActionResult.SUCCESS;
    }

    public ActionResult stop() {
        if (receiverRegistered) {
            receiverRegistered = false;
            context.unregisterReceiver(this);
        }
        return ActionResult.SUCCESS;
    }

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

    public ActionResult createServerConnection() {

        if (serverIP == null) return ActionResult.FAILURE;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new Socket(serverIP, ServerService.SERVER_PORT);
                    serverDataInputStream = new DataInputStream(serverSocket.getInputStream());
                    serverDataOutputStream = new DataOutputStream(serverSocket.getOutputStream());

                    new InputStreamHandler(
                            serverDataInputStream,
                            receiveMessageListener,
                            clientListener,
                            connectedSSID
                    ).start();

                    if (clientListener != null)
                        Utility.postOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                clientListener.onConnected();
                            }
                        });

                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                    if (clientListener != null)
                        Utility.postOnMainThread(new Runnable() {
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

    public ActionResult closeServerConnection() {

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

        return ActionResult.SUCCESS;
    }

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

    // scan for available servers, when servers list is available onWifiScanFinished listener is called
    public ActionResult scan() {
        if (wifiManager != null && !scanRequested) {
            scanRequested = wifiManager.startScan();
            return scanRequested ? ActionResult.SUCCESS : ActionResult.FAILURE;
        } else
            return ActionResult.FAILURE;
    }

    private boolean isApInfoValid(ApInfo apInfo) {
        return apInfo != null
                && apInfo.getSSID() != null && !apInfo.getSSID().trim().equals("")
                && apInfo.getPassword() != null && !apInfo.getPassword().trim().equals("");
    }

    // connect to a server
    public ActionResult join(ApInfo apInfo) {
        if (wifiManager != null && isApInfoValid(apInfo)) {

            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = String.format("\"%s\"", apInfo.getSSID());
            wifiConfig.preSharedKey = String.format("\"%s\"", apInfo.getPassword());

            networkID = wifiManager.addNetwork(wifiConfig);

            return wifiManager.disconnect()
                    && wifiManager.enableNetwork(networkID, true)
                    && wifiManager.reconnect() ? ActionResult.JOIN_SUCCESSFUL : ActionResult.JOIN_FAILED;
        } else
            return ActionResult.JOIN_FAILED;
    }

    // disconnect from currently connected server
    public ActionResult leave() {
        return wifiManager != null && networkID != -1 && wifiManager.removeNetwork(networkID)
                && wifiManager.saveConfiguration() ? ActionResult.SUCCESS : ActionResult.FAILURE;
    }

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
                            serverIP = obtainServerIP();
                            if (clientListener != null)
                                clientListener.onJoinedGroup();
                        }
                    } else if (state == State.DISCONNECTED) {
                        if (connectedSSID != null && connectedSSID.startsWith(ServerService.SSID_PREFIX)) {
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

            if (wifiScanListener != null && scanRequested) {
                scanRequested = false;
                List<ScanResult> scanResults = wifiManager.getScanResults();
                List<ApInfo> serversList = new ArrayList<>();

                String ssid, name;

                for (ScanResult result : scanResults) {
                    ssid = result.SSID;
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

                wifiScanListener.onWifiScanFinished(serversList);

            }
        }
    }
}