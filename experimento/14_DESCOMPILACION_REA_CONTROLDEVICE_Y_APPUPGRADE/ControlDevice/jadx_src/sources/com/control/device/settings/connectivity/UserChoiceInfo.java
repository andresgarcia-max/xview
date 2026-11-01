package com.control.device.settings.connectivity;

import android.net.wifi.WifiConfiguration;
import android.util.ArrayMap;
import androidx.lifecycle.ViewModel;
import java.util.HashMap;
import java.util.Map;

/* loaded from: classes.dex */
public class UserChoiceInfo extends ViewModel {
    private int mWifiSecurity;
    Map<Integer, Boolean> mIsPageVisible = new ArrayMap();
    private HashMap<Integer, String> mDataSummary = new HashMap<>();
    private HashMap<Integer, Integer> mChoiceSummary = new HashMap<>();
    private WifiConfiguration mWifiConfiguration = new WifiConfiguration();
    private boolean mIsPasswordHidden = false;
    private int mEasyConnectNetworkId = -1;

    public void put(int i, String str) {
        this.mDataSummary.put(Integer.valueOf(i), str);
    }

    public String getPageSummary(int i) {
        if (this.mDataSummary.containsKey(Integer.valueOf(i))) {
            return this.mDataSummary.get(Integer.valueOf(i));
        }
        return null;
    }

    public void removePageSummary(int i) {
        this.mDataSummary.remove(Integer.valueOf(i));
    }

    public WifiConfiguration getWifiConfiguration() {
        return this.mWifiConfiguration;
    }

    public void setWifiConfiguration(WifiConfiguration wifiConfiguration) {
        this.mWifiConfiguration = wifiConfiguration;
    }

    public int getWifiSecurity() {
        return this.mWifiSecurity;
    }

    public void setWifiSecurity(int i) {
        this.mWifiSecurity = i;
    }

    public boolean isPasswordHidden() {
        return this.mIsPasswordHidden;
    }

    public void setPasswordHidden(boolean z) {
        this.mIsPasswordHidden = z;
    }
}
