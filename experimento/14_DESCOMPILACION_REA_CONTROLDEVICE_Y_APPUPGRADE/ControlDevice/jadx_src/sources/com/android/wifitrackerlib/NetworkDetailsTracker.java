package com.android.wifitrackerlib;

import android.net.LinkProperties;
import android.net.NetworkCapabilities;

/* loaded from: classes.dex */
public abstract class NetworkDetailsTracker extends BaseWifiTracker {
    public abstract WifiEntry getWifiEntry();

    protected void handleLinkPropertiesChanged(LinkProperties linkProperties) {
        WifiEntry wifiEntry = getWifiEntry();
        if (wifiEntry.getConnectedState() == 2) {
            wifiEntry.updateLinkProperties(linkProperties);
        }
    }

    protected void handleNetworkCapabilitiesChanged(NetworkCapabilities networkCapabilities) {
        WifiEntry wifiEntry = getWifiEntry();
        if (wifiEntry.getConnectedState() == 2) {
            wifiEntry.updateNetworkCapabilities(networkCapabilities);
            wifiEntry.setIsLowQuality(this.mIsWifiValidated && this.mIsCellDefaultRoute);
        }
    }
}
