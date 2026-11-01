package com.android.tv.settings.connectivity;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import com.android.settingslib.wifi.AccessPoint;
import com.android.tv.settings.connectivity.util.WifiSecurityUtil;
import java.util.List;
import java.util.regex.Pattern;

/* loaded from: classes.dex */
public final class WifiConfigHelper {
    private static final Pattern HOSTNAME_PATTERN = Pattern.compile("^$|^[a-zA-Z0-9\\_]+(\\-[a-zA-Z0-9\\_]+)*(\\.[a-zA-Z0-9\\_]+(\\-[a-zA-Z0-9\\_]+)*)*$");
    private static final Pattern EXCLUSION_PATTERN = Pattern.compile("$|^(\\*)?\\.?[a-zA-Z0-9\\_]+(\\-[a-zA-Z0-9\\_]+)*(\\.[a-zA-Z0-9\\_]+(\\-[a-zA-Z0-9\\_]+)*)*$");

    public static void setConfigSsid(WifiConfiguration wifiConfiguration, String str) {
        wifiConfiguration.SSID = AccessPoint.convertToQuotedString(str);
    }

    public static void setConfigKeyManagementBySecurity(WifiConfiguration wifiConfiguration, int i) {
        wifiConfiguration.allowedKeyManagement.clear();
        wifiConfiguration.allowedAuthAlgorithms.clear();
        if (i == 0) {
            wifiConfiguration.allowedKeyManagement.set(0);
            return;
        }
        if (i == 1) {
            wifiConfiguration.allowedKeyManagement.set(0);
            wifiConfiguration.allowedAuthAlgorithms.set(0);
            wifiConfiguration.allowedAuthAlgorithms.set(1);
        } else if (i == 2) {
            wifiConfiguration.allowedKeyManagement.set(1);
        } else {
            if (i != 3) {
                return;
            }
            wifiConfiguration.allowedKeyManagement.set(2);
            wifiConfiguration.allowedKeyManagement.set(3);
        }
    }

    public static boolean isNetworkSaved(WifiConfiguration wifiConfiguration) {
        return wifiConfiguration != null && wifiConfiguration.networkId > -1;
    }

    public static WifiConfiguration getConfiguration(Context context, String str, int i) {
        WifiConfiguration fromConfiguredNetworks = getFromConfiguredNetworks(context, str, i);
        if (fromConfiguredNetworks != null) {
            return fromConfiguredNetworks;
        }
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        setConfigSsid(wifiConfiguration, str);
        setConfigKeyManagementBySecurity(wifiConfiguration, i);
        return wifiConfiguration;
    }

    private static WifiConfiguration getFromConfiguredNetworks(Context context, String str, int i) {
        String str2;
        List<WifiConfiguration> configuredNetworks = ((WifiManager) context.getSystemService("wifi")).getConfiguredNetworks();
        if (configuredNetworks == null) {
            return null;
        }
        for (WifiConfiguration wifiConfiguration : configuredNetworks) {
            if (wifiConfiguration != null && (str2 = wifiConfiguration.SSID) != null && TextUtils.equals(WifiInfo.sanitizeSsid(str2), str) && WifiSecurityUtil.getSecurity(wifiConfiguration) == i) {
                return wifiConfiguration;
            }
        }
        return null;
    }
}
