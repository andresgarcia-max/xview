package com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import com.android.settingslib.wifi.AccessPoint;
import com.android.tv.settings.connectivity.ConnectivityListener;
import com.control.device.R;
import com.control.device.settings.connectivity.MessageFragment;
import com.control.device.settings.connectivity.State;
import com.control.device.settings.connectivity.StateMachine;
import com.control.device.settings.connectivity.UserChoiceInfo;
import java.lang.ref.WeakReference;
import java.util.List;

/* loaded from: classes.dex */
public class AmxKeyConnectState implements State {
    private final FragmentActivity mActivity;
    private Fragment mFragment;

    public AmxKeyConnectState(FragmentActivity fragmentActivity) {
        this.mActivity = fragmentActivity;
    }

    @Override // com.control.device.settings.connectivity.State
    public void processForward() {
        Log.i("AmxKeyConnectState", "processForward");
        AmxKeyConnectToWifiFragment newInstance = AmxKeyConnectToWifiFragment.newInstance(this.mActivity.getString(R.string.wifi_connecting, new Object[]{((UserChoiceInfo) ViewModelProviders.of(this.mActivity).get(UserChoiceInfo.class)).getWifiConfiguration().getPrintableSsid()}), true);
        this.mFragment = newInstance;
        State.FragmentChangeListener fragmentChangeListener = (State.FragmentChangeListener) this.mActivity;
        if (fragmentChangeListener != null) {
            fragmentChangeListener.onFragmentChange(newInstance, true);
        }
    }

    @Override // com.control.device.settings.connectivity.State
    public void processBackward() {
        Log.i("AmxKeyConnectState", "processBackward");
        ((StateMachine) ViewModelProviders.of(this.mActivity).get(StateMachine.class)).back();
    }

    public static class AmxKeyConnectToWifiFragment extends MessageFragment implements ConnectivityListener.WifiNetworkListener {
        static final int CONNECTION_TIMEOUT = 20000;
        static final int MSG_TIMEOUT = 1;
        private ConnectivityListener mConnectivityListener;
        Handler mHandler;
        StateMachine mStateMachine;
        WifiConfiguration mWifiConfiguration;
        WifiManager mWifiManager;

        public static AmxKeyConnectToWifiFragment newInstance(String str, boolean z) {
            AmxKeyConnectToWifiFragment amxKeyConnectToWifiFragment = new AmxKeyConnectToWifiFragment();
            Bundle bundle = new Bundle();
            MessageFragment.addArguments(bundle, str, z);
            amxKeyConnectToWifiFragment.setArguments(bundle);
            return amxKeyConnectToWifiFragment;
        }

        @Override // androidx.fragment.app.Fragment
        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            ConnectivityListener connectivityListener = new ConnectivityListener(getActivity(), null);
            this.mConnectivityListener = connectivityListener;
            connectivityListener.start();
            this.mWifiConfiguration = ((UserChoiceInfo) ViewModelProviders.of(getActivity()).get(UserChoiceInfo.class)).getWifiConfiguration();
            this.mStateMachine = (StateMachine) ViewModelProviders.of(getActivity()).get(StateMachine.class);
            this.mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService("wifi");
            this.mHandler = new MessageHandler(this);
            this.mConnectivityListener.setWifiListener(this);
        }

        @Override // com.control.device.settings.connectivity.MessageFragment, androidx.fragment.app.Fragment
        public void onResume() {
            super.onResume();
            postTimeout();
            proceedDependOnNetworkState();
        }

        void proceedDependOnNetworkState() {
            if (isNetworkConnected()) {
                this.mWifiManager.disconnect();
            }
            this.mWifiManager.addNetwork(this.mWifiConfiguration);
            this.mWifiManager.connect(this.mWifiConfiguration, null);
        }

        @Override // androidx.fragment.app.Fragment
        public void onDestroy() {
            if (!isNetworkConnected()) {
                this.mWifiManager.disconnect();
            }
            this.mConnectivityListener.stop();
            this.mConnectivityListener.destroy();
            this.mHandler.removeMessages(1);
            super.onDestroy();
        }

        @Override // com.android.tv.settings.connectivity.ConnectivityListener.WifiNetworkListener
        public void onWifiListChanged() {
            List<AccessPoint> availableNetworks = this.mConnectivityListener.getAvailableNetworks();
            if (availableNetworks != null) {
                for (AccessPoint accessPoint : availableNetworks) {
                    if (accessPoint != null && AccessPoint.convertToQuotedString(accessPoint.getSsidStr()).equals(this.mWifiConfiguration.SSID)) {
                        inferConnectionStatus(accessPoint);
                    }
                }
            }
        }

        private void inferConnectionStatus(AccessPoint accessPoint) {
            WifiConfiguration config = accessPoint.getConfig();
            if (config == null) {
                return;
            }
            if (config.getNetworkSelectionStatus().getNetworkSelectionStatus() == 0) {
                if (isNetworkConnected()) {
                    Log.i("AmxKeyConnectState", "RESULT_SUCCESS");
                    notifyListener(2);
                    return;
                }
                return;
            }
            int networkSelectionDisableReason = config.getNetworkSelectionStatus().getNetworkSelectionDisableReason();
            if (networkSelectionDisableReason != 1) {
                if (networkSelectionDisableReason != 2) {
                    if (networkSelectionDisableReason == 3) {
                        Log.i("AmxKeyConnectState", "DisableReason: DISABLED_DHCP_FAILURE");
                    } else if (networkSelectionDisableReason != 8) {
                        Log.i("AmxKeyConnectState", "DisableReason: UNKNOWN");
                    }
                }
                Log.i("AmxKeyConnectState", "DisableReason: AUTHENTICATION");
            } else {
                Log.i("AmxKeyConnectState", "DisableReason: DISABLED_ASSOCIATION_REJECTION");
            }
            notifyListener(2);
            accessPoint.clearConfig();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void notifyListener(int i) {
            if (this.mStateMachine.getCurrentState() instanceof AmxKeyConnectState) {
                this.mStateMachine.getListener().onComplete(i);
            }
        }

        private NetworkInfo getActiveWifiNetworkInfo() {
            ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(ConnectivityManager.class);
            for (Network network : connectivityManager.getAllNetworks()) {
                NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
                if (networkInfo.isConnected() && networkInfo.getType() == 1) {
                    return networkInfo;
                }
            }
            return null;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean isNetworkConnected() {
            WifiInfo connectionInfo;
            NetworkInfo activeWifiNetworkInfo = getActiveWifiNetworkInfo();
            return activeWifiNetworkInfo != null && activeWifiNetworkInfo.isConnected() && activeWifiNetworkInfo.getType() == 1 && (connectionInfo = this.mWifiManager.getConnectionInfo()) != null && connectionInfo.getSSID().equals(this.mWifiConfiguration.SSID) && isInternetValidated();
        }

        private boolean isInternetValidated() {
            boolean z = false;
            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService("connectivity");
                Network activeNetwork = connectivityManager.getActiveNetwork();
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
                if (networkCapabilities != null && networkCapabilities.hasCapability(16)) {
                    Log.d("AmxKeyConnectState", "<isInternetValidated> networkCapabilities has NET_CAPABILITY_VALIDATED");
                    z = true;
                } else {
                    Log.d("AmxKeyConnectState", "<isInternetValidated> network is invalidated, network:" + activeNetwork);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return z;
        }

        private void postTimeout() {
            this.mHandler.removeMessages(1);
            this.mHandler.sendEmptyMessageDelayed(1, 20000L);
        }

        private static class MessageHandler extends Handler {
            private final WeakReference<AmxKeyConnectToWifiFragment> mFragmentRef;

            MessageHandler(AmxKeyConnectToWifiFragment amxKeyConnectToWifiFragment) {
                this.mFragmentRef = new WeakReference<>(amxKeyConnectToWifiFragment);
            }

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                AmxKeyConnectToWifiFragment amxKeyConnectToWifiFragment = this.mFragmentRef.get();
                if (amxKeyConnectToWifiFragment == null) {
                    return;
                }
                if (amxKeyConnectToWifiFragment.isNetworkConnected()) {
                    Log.i("AmxKeyConnectState", "Fake timeout; we're actually connected");
                    amxKeyConnectToWifiFragment.notifyListener(2);
                } else {
                    Log.i("AmxKeyConnectState", "Timeout is real; telling the listener");
                    amxKeyConnectToWifiFragment.notifyListener(2);
                }
            }
        }
    }
}
