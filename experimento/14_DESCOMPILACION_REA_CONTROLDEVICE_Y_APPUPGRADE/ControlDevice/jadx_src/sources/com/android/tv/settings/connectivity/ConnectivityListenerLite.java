package com.android.tv.settings.connectivity;

import android.net.ConnectivityManager;
import android.os.Handler;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

/* loaded from: classes.dex */
public class ConnectivityListenerLite implements LifecycleObserver {
    private final ConnectivityManager mConnectivityManager;
    private final Handler mHandler;
    private final Lifecycle mLifecycle;
    private Listener mListener;
    private final ConnectivityManager.NetworkCallback mNetworkCallback;

    public interface Listener {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void registerNetworkCallback() {
        this.mConnectivityManager.registerDefaultNetworkCallback(this.mNetworkCallback);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void unregisterNetworkCallback() {
        this.mConnectivityManager.unregisterNetworkCallback(this.mNetworkCallback);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        this.mHandler.removeCallbacksAndMessages(null);
        this.mLifecycle.removeObserver(this);
        this.mListener = null;
    }
}
