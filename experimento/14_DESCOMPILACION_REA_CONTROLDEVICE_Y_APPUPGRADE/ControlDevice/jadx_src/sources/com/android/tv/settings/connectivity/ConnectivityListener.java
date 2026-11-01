package com.android.tv.settings.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.EthernetManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import com.android.settingslib.wifi.AccessPoint;
import com.android.settingslib.wifi.WifiTracker;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class ConnectivityListener implements WifiTracker.WifiListener, LifecycleObserver, OnStart, OnStop {
    private SignalStrength mCellSignalStrength;
    private final ConnectivityManager mConnectivityManager;
    private final Context mContext;
    private final EthernetManager mEthernetManager;
    private final Listener mListener;
    private final BroadcastReceiver mNetworkReceiver;
    private int mNetworkType;
    private final PhoneStateListener mPhoneStateListener;
    private boolean mStarted;
    private WifiNetworkListener mWifiListener;
    private final WifiManager mWifiManager;
    private int mWifiSignalStrength;
    private String mWifiSsid;
    private WifiTracker mWifiTracker;

    public interface Listener {
        void onConnectivityChange();
    }

    public interface WifiNetworkListener {
        void onWifiListChanged();
    }

    @Deprecated
    public ConnectivityListener(Context context, Listener listener) {
        this(context, listener, null);
    }

    public ConnectivityListener(Context context, Listener listener, Lifecycle lifecycle) {
        this.mNetworkReceiver = new BroadcastReceiver() { // from class: com.android.tv.settings.connectivity.ConnectivityListener.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                ConnectivityListener.this.updateConnectivityStatus();
                if (ConnectivityListener.this.mListener != null) {
                    ConnectivityListener.this.mListener.onConnectivityChange();
                }
            }
        };
        this.mPhoneStateListener = new PhoneStateListener() { // from class: com.android.tv.settings.connectivity.ConnectivityListener.2
            @Override // android.telephony.PhoneStateListener
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                ConnectivityListener.this.mCellSignalStrength = signalStrength;
                if (ConnectivityListener.this.mListener != null) {
                    ConnectivityListener.this.mListener.onConnectivityChange();
                }
            }
        };
        this.mContext = context;
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        WifiManager wifiManager = (WifiManager) context.getSystemService(WifiManager.class);
        this.mWifiManager = wifiManager;
        this.mEthernetManager = (EthernetManager) context.getSystemService(EthernetManager.class);
        this.mListener = listener;
        if (wifiManager != null) {
            if (lifecycle != null) {
                lifecycle.addObserver(this);
                this.mWifiTracker = new WifiTracker(context, this, lifecycle, true, true);
            } else {
                this.mWifiTracker = new WifiTracker(context, this, true, true);
            }
        }
        updateConnectivityStatus();
    }

    @Deprecated
    public void start() {
        WifiTracker wifiTracker;
        if (!this.mStarted && (wifiTracker = this.mWifiTracker) != null) {
            wifiTracker.onStart();
        }
        onStart();
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnStart
    public void onStart() {
        if (this.mStarted) {
            return;
        }
        this.mStarted = true;
        updateConnectivityStatus();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mContext.registerReceiver(this.mNetworkReceiver, intentFilter);
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService(TelephonyManager.class);
        if (telephonyManager != null) {
            telephonyManager.listen(this.mPhoneStateListener, 256);
        }
    }

    @Deprecated
    public void stop() {
        WifiTracker wifiTracker;
        if (this.mStarted && (wifiTracker = this.mWifiTracker) != null) {
            wifiTracker.onStop();
        }
        onStop();
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnStop
    public void onStop() {
        if (this.mStarted) {
            this.mStarted = false;
            this.mContext.unregisterReceiver(this.mNetworkReceiver);
            this.mWifiListener = null;
            TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService(TelephonyManager.class);
            if (telephonyManager != null) {
                telephonyManager.listen(this.mPhoneStateListener, 0);
            }
        }
    }

    @Deprecated
    public void destroy() {
        WifiTracker wifiTracker = this.mWifiTracker;
        if (wifiTracker != null) {
            wifiTracker.onDestroy();
        }
    }

    public void setWifiListener(WifiNetworkListener wifiNetworkListener) {
        this.mWifiListener = wifiNetworkListener;
    }

    public List<AccessPoint> getAvailableNetworks() {
        WifiTracker wifiTracker = this.mWifiTracker;
        return wifiTracker == null ? new ArrayList() : wifiTracker.getAccessPoints();
    }

    public boolean isWifiEnabledOrEnabling() {
        WifiManager wifiManager = this.mWifiManager;
        return wifiManager != null && (wifiManager.getWifiState() == 3 || this.mWifiManager.getWifiState() == 2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateConnectivityStatus() {
        NetworkInfo activeNetworkInfo = this.mConnectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null) {
            this.mNetworkType = -1;
            return;
        }
        int type = activeNetworkInfo.getType();
        if (type == 0) {
            this.mNetworkType = 0;
            return;
        }
        if (type != 1) {
            if (type == 9) {
                this.mNetworkType = 9;
                return;
            } else {
                this.mNetworkType = -1;
                return;
            }
        }
        if (this.mWifiManager == null) {
            return;
        }
        this.mNetworkType = 1;
        String ssid = getSsid();
        if (!TextUtils.equals(this.mWifiSsid, ssid)) {
            this.mWifiSsid = ssid;
        }
        WifiInfo connectionInfo = this.mWifiManager.getConnectionInfo();
        int calculateSignalLevel = connectionInfo != null ? WifiManager.calculateSignalLevel(connectionInfo.getRssi(), 4) : 0;
        if (this.mWifiSignalStrength != calculateSignalLevel) {
            this.mWifiSignalStrength = calculateSignalLevel;
        }
    }

    @Override // com.android.settingslib.wifi.WifiTracker.WifiListener
    public void onWifiStateChanged(int i) {
        updateConnectivityStatus();
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onConnectivityChange();
        }
    }

    @Override // com.android.settingslib.wifi.WifiTracker.WifiListener
    public void onConnectedChanged() {
        updateConnectivityStatus();
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onConnectivityChange();
        }
    }

    @Override // com.android.settingslib.wifi.WifiTracker.WifiListener
    public void onAccessPointsChanged() {
        WifiNetworkListener wifiNetworkListener = this.mWifiListener;
        if (wifiNetworkListener != null) {
            wifiNetworkListener.onWifiListChanged();
        }
    }

    public String getSsid() {
        WifiInfo connectionInfo;
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null || (connectionInfo = wifiManager.getConnectionInfo()) == null) {
            return null;
        }
        String ssid = connectionInfo.getSSID();
        return ssid != null ? sanitizeSsid(ssid) : ssid;
    }

    public static String sanitizeSsid(String str) {
        return removeDoubleQuotes(str);
    }

    public static String removeDoubleQuotes(String str) {
        if (str == null) {
            return null;
        }
        int length = str.length();
        if (length <= 1 || str.charAt(0) != '\"') {
            return str;
        }
        int i = length - 1;
        return str.charAt(i) == '\"' ? str.substring(1, i) : str;
    }
}
