package io.sharif.pavilion.network.Utilities;

import android.content.Context;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

import io.sharif.pavilion.clientSide.MainActivity;
import io.sharif.pavilion.network.DataStructures.Message;
import io.sharif.pavilion.network.Services.ServerService;

public class Utility {

    public static Context context =MainActivity.context;//TODO I should set context in first line of each Activity
    private static Looper mainLooper = context.getMainLooper();
    private static WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    private static final String appFolderName = "HotSpot";
    private static final int TCP_SEGMENT_SIZE = 8192;

    public static int getTcpSegmentSize() {
        return TCP_SEGMENT_SIZE;
    }

    public static void debugPrint(String str) {
        Log.d("myTag", str);
    }

    /**
     * Enable wifi.
     * @return {@code SUCCESS} if operation succeeds, {@code FAILURE} otherwise
     */
    public static synchronized ActionResult enableWifi() {
        if (wifiManager != null) {
            wifiManager.setWifiEnabled(true);
            return ActionResult.SUCCESS;
        } else
            return ActionResult.FAILURE;
    }

    /**
     * Disable wifi.
     * @return {@code SUCCESS} if operation succeeds, {@code FAILURE} otherwise
     */
    public static synchronized ActionResult disableWifi() {
        if (wifiManager != null) {
            wifiManager.setWifiEnabled(false);
            return ActionResult.SUCCESS;
        } else
            return ActionResult.FAILURE;
    }

    /**
     * Check whether or not wifi is enabled.
     * @return {@code true} if WiFiManager is not null and wifi is enabled, {@code false} otherwise
     */
    public static boolean isWifiEnabled() {
        return wifiManager != null && wifiManager.isWifiEnabled();
    }

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

    public static String generatePassword(String ssid) {
        String hash = Utility.md5(ssid);
        return hash.length() < ServerService.PASSWORD_LENGTH
                ? hash : hash.substring(0, ServerService.PASSWORD_LENGTH);
    }

    public static String getServerName(String ssid) {
        if (ssid != null) {
            return ssid.substring(
                    ServerService.SSID_PREFIX.length(), ssid.length());
        }
        return null;
    }

    public static long getMessageTotalLength(Message message) {

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

    public static String getAppFolderPath() {
        return Environment.getExternalStorageDirectory() + File.separator + appFolderName + File.separator;
    }

    /**
     * This method is used to run server listeners on UI thread.
     * @param runnable runnable to be run on UI thread
     */
    public static void postOnMainThread(Runnable runnable) {
        Handler handler = new Handler(mainLooper);
        handler.post(runnable);
    }

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

    public static float calculateProgress(long total, long received) {
        if (total == 0) return 0;
        return ((float) received/total)*100;
    }

    public static float calculateSpeed(long milliTime, long bytes) {
        if (milliTime == 0) return 0;
        Utility.debugPrint("Bytes : " + bytes + ", time : " + milliTime);
        return ((float)bytes/milliTime)*1000;
    }

}