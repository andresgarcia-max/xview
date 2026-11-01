package com.android.tv.settings.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/* loaded from: classes.dex */
public class NetworkInfoHelper {
    private static int mAndroidVersion = 4;
    private static ConnectivityManager mCM;
    private static WifiManager mWifiManager;

    public NetworkInfoHelper(Context context) {
        getAndroidVersion();
        mCM = (ConnectivityManager) context.getSystemService("connectivity");
        mWifiManager = (WifiManager) context.getSystemService("wifi");
    }

    public int getWifiFrequencyType() {
        return getWifiFrequencyType(mWifiManager.getConnectionInfo());
    }

    public static int getWifiFrequencyType(WifiInfo wifiInfo) {
        if (wifiInfo == null || wifiInfo.getNetworkId() == -1) {
            return -1;
        }
        String ssid = wifiInfo.getSSID();
        int frequency = wifiInfo.getFrequency();
        Log.i("NetworkInfoHelper", "<getWifiFrequencyType> SSID : [" + ssid + "], Frequency : [" + frequency + "]");
        if (frequency < 5160 || frequency > 5885) {
            return (frequency < 2142 || frequency > 2484) ? -1 : 0;
        }
        return 1;
    }

    private void getAndroidVersion() {
        mAndroidVersion = Integer.valueOf(propertyget("ro.build.version.release", "4.0").substring(0, 1)).intValue();
    }

    private String propertyget(String str, String str2) {
        try {
            return (String) Class.forName("android.os.SystemProperties").getDeclaredMethod("get", String.class, String.class).invoke(null, str, str2);
        } catch (Exception unused) {
            return str2;
        }
    }
}
