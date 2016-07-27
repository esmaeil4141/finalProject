package io.sharif.pavilion.network.Utilities;

import android.content.Context;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

import io.sharif.pavilion.network.DataStructures.Message;
import io.sharif.pavilion.network.Services.ServerService;

public class Utility {

    private static final String APP_FOLDER_NAME = "HotSpot";
    private static final int TCP_SEGMENT_SIZE = 8192;
    private static final String TAG = "pavilion";

    /**
     * This method returns TCP segment size in bytes.
     * @return TCP segment size
     */
    public static int getTcpSegmentSize() {
        return TCP_SEGMENT_SIZE;
    }

    /**
     * This method logs input message for debugging.
     * @param message message to be logged
     */
    public static void debugPrint(String message) {
        Log.d(TAG, message);
    }

    /**
     * This method is used to enable wifi.
     * @param context application context
     * @return {@code SUCCESS} if operation succeeds, {@code FAILURE} otherwise
     */
    public static ActionResult enableWifi(Context context) {
        if (context != null) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                wifiManager.setWifiEnabled(true);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.FAILURE;
    }

    /**
     * This method is used to disable wifi.
     * @param context application context
     * @return {@code SUCCESS} if operation succeeds, {@code FAILURE} otherwise
     */
    public static ActionResult disableWifi(Context context) {
        if (context != null) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                wifiManager.setWifiEnabled(false);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.FAILURE;
    }

    /**
     * This method is used to check whether wifi is enabled.
     * @param context application context
     * @return {@code false} if context is null or wifi is disabled, {@code true} otherwise
     */
    public static boolean isWifiEnabled(Context context) {
        if (context != null) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            return wifiManager != null && wifiManager.isWifiEnabled();
        }
        return false;
    }

    /**
     * This method is used to generate message MD5 hash string.
     * @param message message to be generate hash from
     * @return message MD5 hash
     */
    public static String md5(String message) {

        StringBuilder hexString = null;

        try {

            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(message.getBytes());

            byte messageDigest[] = digest.digest();

            hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2) h = "0" + h;
                hexString.append(h);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return hexString != null ? hexString.toString() : null;
    }

    /**
     * This method is used to generate wifi network password from it's name.
     * @param ssid wifi network name
     * @return password string
     */
    public static String generatePassword(String ssid) {
        String hash = Utility.md5(ssid);
        return hash.length() < ServerService.PASSWORD_LENGTH
                ? hash : hash.substring(0, ServerService.PASSWORD_LENGTH);
    }

    /**
     * This method is used to extract server name from it's ssid.
     * @param ssid server ssid
     * @return server name
     */
    public static String getServerName(String ssid) {
        if (ssid != null) {
            return ssid.substring(
                    ServerService.SSID_PREFIX.length(), ssid.length());
        }
        return null;
    }

    /**
     * This method is used to calculate message total length by summing text message length and all files size.
     * @param context application context
     * @param message message object
     * @return total message length in bytes
     */
    public static long getMessageTotalLength(Context context, Message message) {

        long totalLength = 0;

        if (message != null) {

            try {
                if (message.getMessage() != null)
                    totalLength += message.getMessage().getBytes("UTF-8").length;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if (message.getFileUris() != null) {
                File file;
                String address;
                for (Uri uri : message.getFileUris()) {
                    if (uri != null) {
                        address = FileUtils.getPath(context, uri);
                        if (address != null) {
                            file = new File(address);
                            if (file.exists()) {
                                totalLength += file.length();
                            }
                        }
                    }
                }

            }

        }

        return totalLength;
    }

    /**
     * This method is used to get base address of app files.
     * @return app base folder path
     */
    public static String getAppFolderPath() {
        return Environment.getExternalStorageDirectory() + File.separator + APP_FOLDER_NAME + File.separator;
    }

    /**
     * This method is used to post runnable to given thread. Typically it's used for posting on main thread.
     * @param context context to post runnable to
     * @param runnable runnable to be posted
     */
    public static void postOnMainThread(Context context, Runnable runnable) {
        Handler handler = new Handler(context.getMainLooper());
        handler.post(runnable);
    }

    /**
     * This method is used to convert IP address string to long value.
     * @param ipAddress IP address string such as "192.168.1.1"
     * @return long value of IP address
     */
    public static long ipToLong(String ipAddress) {

        String[] ipAddressInArray = ipAddress.split("\\.");

        long result = 0;
        for (int i = 0; i < ipAddressInArray.length; i++) {

            int power = 3 - i;
            int ip = Integer.parseInt(ipAddressInArray[i]);
            result += ip * Math.pow(256, power);

        }

        return result;
    }

    /**
     * This method is used to calculate progress from total and sent/received bytes.
     * @param total total bytes
     * @param received total sent/received bytes
     * @return progress percentage
     */
    public static float calculateProgress(long total, long received) {
        if (total == 0) return 0;
        return ((float) received/total)*100;
    }

    /**
     * This method is used to calculate upload/download speed from number of bytes sent/received in an interval.
     * @param milliTime the interval bytes sent/received
     * @param bytes number of sent/received bytes
     * @return upload/download speed in bytes per second
     */
    public static float calculateSpeed(long milliTime, long bytes) {
        if (milliTime == 0) return 0;
        return ((float)bytes/milliTime)*1000;
    }
}