package com.android.wifitrackerlib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkScoreManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkScoreCache;
import android.os.Handler;
import android.util.Log;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import java.util.Objects;

/* loaded from: classes.dex */
public class BaseWifiTracker implements LifecycleObserver {
    private static boolean sVerboseLogging;
    private final BroadcastReceiver mBroadcastReceiver;
    protected final ConnectivityManager mConnectivityManager;
    protected final Context mContext;
    private final ConnectivityManager.NetworkCallback mDefaultNetworkCallback;
    protected boolean mIsCellDefaultRoute;
    private boolean mIsStarted;
    protected boolean mIsWifiDefaultRoute;
    protected boolean mIsWifiValidated;
    protected final Handler mMainHandler;
    private final ConnectivityManager.NetworkCallback mNetworkCallback;
    private final NetworkRequest mNetworkRequest;
    protected final NetworkScoreManager mNetworkScoreManager;
    private final Scanner mScanner;
    private final String mTag;
    protected final WifiManager mWifiManager;
    protected final WifiNetworkScoreCache mWifiNetworkScoreCache;
    protected final Handler mWorkerHandler;

    private class Scanner extends Handler {
    }

    protected void handleOnStart() {
    }

    public static boolean isVerboseLoggingEnabled() {
        return sVerboseLogging;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        intentFilter.addAction("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        intentFilter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter, null, this.mWorkerHandler);
        this.mConnectivityManager.registerNetworkCallback(this.mNetworkRequest, this.mNetworkCallback, this.mWorkerHandler);
        this.mConnectivityManager.registerDefaultNetworkCallback(this.mDefaultNetworkCallback, this.mWorkerHandler);
        ConnectivityManager connectivityManager = this.mConnectivityManager;
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        if (networkCapabilities != null) {
            this.mIsWifiDefaultRoute = networkCapabilities.hasTransport(1);
            this.mIsCellDefaultRoute = networkCapabilities.hasTransport(0);
        } else {
            this.mIsWifiDefaultRoute = false;
            this.mIsCellDefaultRoute = false;
        }
        if (isVerboseLoggingEnabled()) {
            Log.v(this.mTag, "Wifi is the default route: " + this.mIsWifiDefaultRoute);
            Log.v(this.mTag, "Cell is the default route: " + this.mIsCellDefaultRoute);
        }
        this.mNetworkScoreManager.registerNetworkScoreCache(1, this.mWifiNetworkScoreCache, 2);
        this.mWorkerHandler.post(new Runnable() { // from class: com.android.wifitrackerlib.BaseWifiTracker$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                BaseWifiTracker.this.lambda$onStart$0();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onStart$0() {
        if (this.mIsStarted) {
            return;
        }
        this.mIsStarted = true;
        handleOnStart();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        Objects.requireNonNull(this.mScanner);
        throw null;
    }
}
