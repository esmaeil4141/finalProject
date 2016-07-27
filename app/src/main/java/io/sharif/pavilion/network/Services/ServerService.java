package io.sharif.pavilion.network.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v4.util.Pair;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.sharif.pavilion.network.DataStructures.ClientDevice;
import io.sharif.pavilion.network.DataStructures.Message;
import io.sharif.pavilion.network.Handlers.InputStreamHandler;
import io.sharif.pavilion.network.Listeners.ReceiveMessageListener;
import io.sharif.pavilion.network.Listeners.SendMessageListener;
import io.sharif.pavilion.network.Listeners.ServerListener;
import io.sharif.pavilion.network.Listeners.WifiListener;
import io.sharif.pavilion.network.Utilities.ActionResult;
import io.sharif.pavilion.network.Utilities.Utility;

/**
 * This class is used to provide services for servers.
 */
public class ServerService extends BroadcastReceiver {

    public static final String WIFI_AP_STATE_CHANGED_ACTION =
            "android.net.wifi.WIFI_AP_STATE_CHANGED";

    public enum WIFI_AP_STATE {
        WIFI_AP_STATE_DISABLING,
        WIFI_AP_STATE_DISABLED,
        WIFI_AP_STATE_ENABLING,
        WIFI_AP_STATE_ENABLED,
        WIFI_AP_STATE_FAILED
    }

    private final int REACHABLE_TIMEOUT = 600;
    private final int SCAN_CLIENT_INTERVAL = 3000;
    private final int MAXIMUM_SSID_LENGTH = 31;

    public static final int SERVER_PORT = 25000;
    public static final int PASSWORD_LENGTH = 16;
    public static final String SSID_PREFIX = "!1!-";

    private boolean wifiConfigChanged, isWifiApEnabled, receiverRegistered, callServerStart;
    private String name; // server name, shown to clients

    private final ReceiveMessageListener receiveMessageListener;
    private final ServerListener serverListener;
    private final IntentFilter apIntentFilter;
    private final WifiListener wifiListener;
    private final WifiManager wifiManager;
    private final Context context;

    private ClientScanner clientScanner;
    private ServerSocket serverSocket;

    /**
     * @param context application context
     * @param serverListener server callbacks
     * @param wifiListener wifi callbacks
     * @param receiveMessageListener receive message callbacks
     */
    public ServerService(Context context,
                         ServerListener serverListener,
                         WifiListener wifiListener,
                         ReceiveMessageListener receiveMessageListener) {
        this.context = context;
        this.receiveMessageListener = receiveMessageListener;
        this.serverListener = serverListener;
        this.wifiListener = wifiListener;
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        this.apIntentFilter = new IntentFilter();
        this.apIntentFilter.addAction(WIFI_AP_STATE_CHANGED_ACTION);
        this.apIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        enableWifiAp(null, false); // disable hotspot on app start
    }

    /**
     * This method is used to register receivers, stat client scanner and enable hotspot.
     * @return {@code SUCCESS} if config is valid and operation succeeds, {@code FAILURE} otherwise
     */
    public ActionResult start() {
        if (!isConfigValid() || isWifiApEnabled) return ActionResult.FAILURE;

        if (!receiverRegistered) {
            this.context.registerReceiver(this, apIntentFilter);
            receiverRegistered = true;
        }

        clientScanner = new ClientScanner();
        clientScanner.start();

        callServerStart = true;

        WifiConfiguration wifiConfiguration = null;

        if (wifiConfigChanged) {
            wifiConfigChanged = false;
            wifiConfiguration = getNewConfig();
        }

        return enableWifiAp(wifiConfiguration, true) ? ActionResult.SUCCESS : ActionResult.FAILURE;
    }

    /**
     * This method is used to stop client scanner, unregister receivers and disable hotspot.
     * @return {@code SUCCESS} if operations succeed, {@code FAILURE} otherwise
     */
    public ActionResult stop() {
        if (receiverRegistered) {
            this.context.unregisterReceiver(this);
            receiverRegistered = false;
        }

        if (clientScanner != null) clientScanner.interrupt();

        if (serverListener != null) serverListener.onServerStopped();
        return enableWifiAp(null, false) ? ActionResult.SUCCESS : ActionResult.FAILURE;
    }

    /**
     * This method is used to create wifi configuration based on server name.
     * Server password will be a substring of ssid MD5 hash.
     * @return newly created wifi configuration
     */
    private WifiConfiguration getNewConfig() {
        String SSID = this.generateSSID();
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = String.format("%s", SSID);
        wifiConfiguration.preSharedKey = String.format("%s", Utility.generatePassword(SSID));
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
        return wifiConfiguration;
    }

    /**
     * This method is used to generate ssid from server name.
     * @return server ssid
     */
    private String generateSSID() {
        return SSID_PREFIX + this.name;
    }

    /**
     * This method is used to check whether current server config is valid.
     * @return {@code true} if name is valid, {@code false} otherwise
     */
    private boolean isConfigValid() {
        return isNameValid();
    }

    /**
     * This method is used to check whether current server name is valid.
     * @return {@code false} if name is null or empty, or
     * name length plus ssid prefix length exceeds maximum ssid length, {@code true} otherwise
     */
    private boolean isNameValid() {
        return name != null && !name.trim().equals("") && name.length() <= MAXIMUM_SSID_LENGTH - SSID_PREFIX.length();
    }

    /**
     * This method is used to start hotspot with the specified configuration,
     * update the configuration if it's already running.
     * This method also can be used to stop currently running hotspot.
     * @param wifiConfig WiFi configuration, set null to use existing configuration
     * @param enabled enable or disable hotspot
     * @return {@code true} if operation succeeds, {@code false} otherwise
     */
    private boolean enableWifiAp(WifiConfiguration wifiConfig, boolean enabled) {
        try {
            if (enabled) wifiManager.setWifiEnabled(false); // disable wifi to enable hotspot

            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            boolean result = (Boolean) method.invoke(wifiManager, wifiConfig, enabled);
            if (result) isWifiApEnabled = enabled;
            return result;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * This method is used to set server name that is shown to clients in available servers list.
     * @param name server name
     * @return {@code FAILURE} if name is null or empty, {@code SUCCESS} otherwise
     */
    public ActionResult setApName(String name) {
        if (name == null || name.trim().equals("")) return ActionResult.FAILURE;
        this.name = name;
        this.wifiConfigChanged = true;
        return ActionResult.SUCCESS;
    }

    /**
     * This method is used to send message to clients.
     * @param clientID client to send message to
     * @param message message to send
     * @param sendMessageListener send message callbacks
     * @return {@code SUCCESS} if message is not null and the client is connected, {@code FAILURE} otherwise
     */
    public ActionResult sendMessage(String clientID,
                                    Message message,
                                    final SendMessageListener sendMessageListener) {
        if (message != null) {

            ClientDevice clientDevice = clientScanner.getClient(clientID);
            if (clientDevice != null) {

                OutputStream outputStream = clientDevice.getOutputStream();
                if (outputStream != null) {

                    DataOutputStream clientDataOutputStream = new DataOutputStream(outputStream);

                    new MessageSender(
                            context,
                            message,
                            clientDataOutputStream,
                            sendMessageListener
                    ).start();

                    return ActionResult.SUCCESS;
                }
            }

        }

        return ActionResult.FAILURE;
    }

    /**
     * This method can be used to get hotspot status
     * but it lack some delay when hotspot is stopped and started immediately again.
     * Kept for future use.
     * @return returns hotspot state
     */
    private WIFI_AP_STATE getWifiApState() {
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApState");

            int tmp = ((Integer) method.invoke(wifiManager));
            tmp = tmp >= 10 ? tmp-10 : tmp; // Fix for Android 4

            return WIFI_AP_STATE.class.getEnumConstants()[tmp];

        } catch (Exception e) {
            return WIFI_AP_STATE.WIFI_AP_STATE_FAILED;
        }
    }

    /**
     * This method is used to check whether or not hotspot is enabled.
     * @return {@code true} if hotspot is enabled, {@code false} otherwise
     */
    public boolean isApEnabled() {
        return isWifiApEnabled;
        // code commented below lacks some delay when stopping and immediately starting hotspot
        // return getWifiApState() == WIFI_AP_STATE.WIFI_AP_STATE_ENABLED;
    }

    /**
     * This method is used to retrieve a list of currently connected clients.
     * @param onlyReachables {@code false} if the list should contain unreachable (probably disconnected) clients, {@code true} otherwise
     * @return list of clientDevice objects
     */
    public List<ClientDevice> getClientsList(final boolean onlyReachables) {
        return clientScanner.getClientsList(onlyReachables);
    }

    /**
     * This method is used to close all client sockets and server socket.
     * @return {@code SUCCESS} if operation succeeds, {@code FAILURE} otherwise
     */
    public ActionResult closeServerSocket() {
        if (serverSocket != null) {
            clientScanner.closeClients();
            try {
                serverSocket.close();
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return ActionResult.SUCCESS;
    }

    /**
     * This method is used to create server socket.
     */
    public void createServerSocket() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean socketCreated = false;
                try {

                    Socket clientSocket;
                    InetAddress inetAddress;

                    serverSocket = new ServerSocket(SERVER_PORT);
                    socketCreated = true;

                    if (serverListener != null)
                        Utility.postOnMainThread(context, new Runnable() {
                            @Override
                            public void run() {
                                serverListener.onSocketCreated();
                            }
                        });

                    while (!Thread.currentThread().isInterrupted()) {

                        clientSocket = serverSocket.accept();

                        inetAddress = clientSocket.getInetAddress(); // get client IP address
                        if (inetAddress != null) {

                            // set client socket in the list
                            final ClientDevice newClient = clientScanner.setClientSocket(inetAddress.getHostAddress(), clientSocket);

                            if (newClient != null) {
                                if (serverListener != null)
                                    Utility.postOnMainThread(context, new Runnable() {
                                        @Override
                                        public void run() {
                                            serverListener.onClientConnected(newClient);
                                        }
                                    });

                                // create a handler to handle client input stream
                                new InputStreamHandler(
                                        context,
                                        new DataInputStream(newClient.getInputStream()),
                                        receiveMessageListener,
                                        serverListener,
                                        newClient).start();

                            }
                        }
                    }

                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                    if (serverListener != null) {
                        if (socketCreated)
                            Utility.postOnMainThread(context, new Runnable() {
                                @Override
                                public void run() {
                                    serverListener.onSocketClosed();
                                }
                            });
                        else
                            Utility.postOnMainThread(context, new Runnable() {
                                @Override
                                public void run() {
                                    serverListener.onSocketCreateFailure();
                                }
                            });
                    }

                }
            }
        }).start();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (wifiManager != null) {
            String action = intent.getAction();

            if (WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {

                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                state = state >= 10 ? state - 10 : state;

                if (WifiManager.WIFI_STATE_ENABLED == state) {
                    if (serverListener != null) {
                        serverListener.onApEnabled();
                        if (callServerStart) {
                            callServerStart = false;
                            serverListener.onServerStarted();
                        }
                    }
                } else if (WifiManager.WIFI_STATE_DISABLED == state)
                    if (serverListener != null)
                        serverListener.onApDisabled();

            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {

                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);

                switch (wifiState) {
                    case 0: break; // WiFi disabling
                    case 1: if (wifiListener != null) wifiListener.onWifiDisabled(); break;
                    case 2: break; // WiFi enabling
                    case 3: if (wifiListener != null) wifiListener.onWifiEnabled(); break;
                    case 4: if (wifiListener != null) wifiListener.onFailure(ActionResult.UNKNOWN); break;
                }
            }
        }
    }

    /**
     * This class is used to read ARP table periodically to detect when clients join or leave the network.
     */
    public class ClientScanner extends Thread {

        /**
         * CopyOnWriteArrayList is used to prevent ConcurrentModificationException while iterating clients list.
         */
        private final CopyOnWriteArrayList<ClientDevice> clientsList;

        public ClientScanner() {
            this.clientsList  = new CopyOnWriteArrayList<>();
        }

        /**
         * This method is used to periodically check arp table to detect when client join or leave the network.
         * The interval between periods is set using SCAN_CLIENT_INTERVAL parameter.
         */
        @Override
        public void run() {

            BufferedReader br = null;
            List<ClientDevice> scanResult;
            Pair<BufferedReader, List<ClientDevice>> pair;

            while (!Thread.currentThread().isInterrupted()) {

                try {

                    pair = readARPs(false);

                    br = pair.first;
                    scanResult = pair.second;

                    handleScanResult(scanResult);

                    Thread.sleep(SCAN_CLIENT_INTERVAL);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (br != null) br.close();
                    } catch (IOException | NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        /**
         * This method is used to search clients list by client ID.
         * @param clientID client ID to search for
         * @return clientDevice object if found, {@code null} otherwise
         */
        private ClientDevice getClient(String clientID) {
            Iterator<ClientDevice> iterator = clientsList.iterator();
            ClientDevice clientDevice;
            while (iterator.hasNext()) {
                clientDevice = iterator.next();
                if (clientDevice.getID() != null && clientDevice.getID().equals(clientID))
                    return clientDevice;
            }
            return null;
        }

        /**
         * This method is used to close all clients socket.
         */
        private void closeClients() {
            Iterator<ClientDevice> iterator = clientsList.iterator();
            ClientDevice clientDevice;
            while (iterator.hasNext()) {
                clientDevice = iterator.next();
                clientDevice.closetSocket();
            }
        }

        /**
         * This method is used to set client socket.
         * @param clientIP client IP to search for
         * @param clientSocket client socket to sent
         * @return clientDevice object if found, {@code null} otherwise
         */
        private ClientDevice setClientSocket(String clientIP, Socket clientSocket) {
            Iterator<ClientDevice> iterator = clientsList.iterator();
            ClientDevice clientDevice;
            while (iterator.hasNext()) {
                clientDevice = iterator.next();
                if (clientDevice.isReachable() && clientDevice.getIpAddr() != null
                        && clientDevice.getIpAddr().equals(clientIP)) {
                    clientDevice.setSocket(clientSocket);
                    return clientDevice;
                }
            }
            return null;
        }

        /**
         * This method is used to obtain mac and ip addresses of connected clients by reading ARP table located in /proc/net/arp file.
         * @param onlyReachables {@code false} if the list should contain unreachable (probably disconnected) clients, {@code true} otherwise
         * @return a pair containing the bufferedReader and list of clients
         * @throws IOException throws when fails to read the file
         */
        private Pair<BufferedReader, List<ClientDevice>> readARPs(boolean onlyReachables) throws IOException{

            final List<ClientDevice> result = new ArrayList<>();

            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));

            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");

                if (splitted.length >= 4) {

                    String mac = splitted[3];

                    if (mac.matches("..:..:..:..:..:..") && !mac.equals("00:00:00:00:00:00")) {
                        boolean isReachable = InetAddress.getByName(splitted[0])
                                .isReachable(REACHABLE_TIMEOUT);

                        if (!onlyReachables || isReachable)
                            result.add(new ClientDevice(splitted[0], splitted[3], isReachable));
                    }
                }
            }

            return Pair.create(br, result);
        }

        /**
         * This method is used to get a list of connected clients.
         * @param onlyReachables {@code true} if the list should only contain reachable clients, {@code false} otherwise
         * @return list of clients
         */
        private List<ClientDevice> getClientsList(final boolean onlyReachables) {
            List<ClientDevice> newList = new ArrayList<>();
            Iterator<ClientDevice> iterator = clientsList.iterator();
            ClientDevice clientDevice;
            while (iterator.hasNext()) {
                clientDevice = iterator.next();
                if (!onlyReachables || clientDevice.isReachable())
                    newList.add(clientDevice);
            }
            return newList;
        }

        /**
         * This method is used to handle the scan result. It compares the newly retrieved list of clients
         * with the stored list to detect new clients or the clients who has left the network.
         * @param list recently detected list of clients
         * @return {@code FAILURE} if new list is null, {@code SUCCESS} otherwise
         */
        private ActionResult handleScanResult(List<ClientDevice> list) {

            if (list == null) return ActionResult.FAILURE;

            ClientDevice client;

            for (final ClientDevice device: list) {

                client = getClient(device.getID());

                if (client != null) {

                    if (client.isReachable() != device.isReachable()) {

                        client.setReachable(device.isReachable()); // update reachable status

                        if (device.isReachable()) { // previously connected client has joined

                            client.setIpAddr(device.getIpAddr());

                            if (serverListener != null)
                                Utility.postOnMainThread(context, new Runnable() {
                                    @Override
                                    public void run() {
                                        serverListener.onClientJoined(device);
                                    }
                                });
                        } else { // client has left
                            if (serverListener != null)
                                Utility.postOnMainThread(context, new Runnable() {
                                    @Override
                                    public void run() {
                                        serverListener.onClientLeft(device);
                                    }
                                });
                        }
                    }

                } else {

                    clientsList.add(device);

                    if (serverListener != null)
                        Utility.postOnMainThread(context, new Runnable() {
                            @Override
                            public void run() {
                                serverListener.onClientJoined(device);
                            }
                        });
                }
            }

            return ActionResult.SUCCESS;
        }
    }

}