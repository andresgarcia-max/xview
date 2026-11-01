package com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import com.android.tv.settings.connectivity.setup.MessageFragment;
import com.control.device.R;
import com.control.device.settings.connectivity.State;
import com.control.device.settings.connectivity.StateMachine;
import com.control.device.settings.connectivity.UserChoiceInfo;
import java.lang.ref.WeakReference;

/* loaded from: classes.dex */
public class AmxKeyEthernetConnectState implements State {
    private final FragmentActivity mActivity;
    private Fragment mFragment;

    public AmxKeyEthernetConnectState(FragmentActivity fragmentActivity) {
        this.mActivity = fragmentActivity;
    }

    @Override // com.control.device.settings.connectivity.State
    public void processForward() {
        ((UserChoiceInfo) ViewModelProviders.of(this.mActivity).get(UserChoiceInfo.class)).getWifiConfiguration();
        AmxKeyEthernetConnectToWifiFragment newInstance = AmxKeyEthernetConnectToWifiFragment.newInstance(this.mActivity.getString(R.string.amx_key_eth_connecting), true);
        this.mFragment = newInstance;
        State.FragmentChangeListener fragmentChangeListener = (State.FragmentChangeListener) this.mActivity;
        if (fragmentChangeListener != null) {
            fragmentChangeListener.onFragmentChange(newInstance, true);
        }
    }

    @Override // com.control.device.settings.connectivity.State
    public void processBackward() {
        ((StateMachine) ViewModelProviders.of(this.mActivity).get(StateMachine.class)).back();
    }

    public static class AmxKeyEthernetConnectToWifiFragment extends MessageFragment {
        static final int CONNECTION_TIMEOUT = 20000;
        static final int MSG_TIMEOUT = 1;
        Handler mHandler;
        StateMachine mStateMachine;

        public static AmxKeyEthernetConnectToWifiFragment newInstance(String str, boolean z) {
            AmxKeyEthernetConnectToWifiFragment amxKeyEthernetConnectToWifiFragment = new AmxKeyEthernetConnectToWifiFragment();
            Bundle bundle = new Bundle();
            MessageFragment.addArguments(bundle, str, z);
            amxKeyEthernetConnectToWifiFragment.setArguments(bundle);
            return amxKeyEthernetConnectToWifiFragment;
        }

        @Override // androidx.fragment.app.Fragment
        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            this.mStateMachine = (StateMachine) ViewModelProviders.of(getActivity()).get(StateMachine.class);
            this.mHandler = new MessageHandler(this);
        }

        @Override // com.android.tv.settings.connectivity.setup.MessageFragment, androidx.fragment.app.Fragment
        public void onResume() {
            super.onResume();
            postTimeout();
        }

        @Override // androidx.fragment.app.Fragment
        public void onDestroy() {
            this.mHandler.removeMessages(2);
            this.mHandler.removeMessages(1);
            super.onDestroy();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void notifyListener(int i) {
            if (this.mStateMachine.getCurrentState() instanceof AmxKeyEthernetConnectState) {
                this.mStateMachine.getListener().onComplete(i);
            }
        }

        public boolean isNetworkConnected() {
            try {
                NetworkInfo activeNetworkInfo = ((ConnectivityManager) getActivity().getSystemService("connectivity")).getActiveNetworkInfo();
                Log.d("AmxKeyEthernetConnectState", "<isNetworkConnected> networkInfo=" + activeNetworkInfo);
                return activeNetworkInfo.isAvailable();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean isInternetValidated() {
            boolean z = false;
            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService("connectivity");
                Network activeNetwork = connectivityManager.getActiveNetwork();
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
                if (networkCapabilities != null && networkCapabilities.hasCapability(16)) {
                    Log.d("AmxKeyEthernetConnectState", "<isInternetValidated> networkCapabilities has NET_CAPABILITY_VALIDATED");
                    z = true;
                } else {
                    Log.d("AmxKeyEthernetConnectState", "<isInternetValidated> network is invalidated, network:" + activeNetwork);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return z;
        }

        private void postTimeout() {
            this.mHandler.removeMessages(2);
            this.mHandler.sendEmptyMessageDelayed(2, 3000L);
            this.mHandler.removeMessages(1);
            this.mHandler.sendEmptyMessageDelayed(1, 20000L);
        }

        private static class MessageHandler extends Handler {
            private final WeakReference<AmxKeyEthernetConnectToWifiFragment> mFragmentRef;

            MessageHandler(AmxKeyEthernetConnectToWifiFragment amxKeyEthernetConnectToWifiFragment) {
                this.mFragmentRef = new WeakReference<>(amxKeyEthernetConnectToWifiFragment);
            }

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                AmxKeyEthernetConnectToWifiFragment amxKeyEthernetConnectToWifiFragment = this.mFragmentRef.get();
                if (amxKeyEthernetConnectToWifiFragment == null) {
                    return;
                }
                int i = message.what;
                if (i == 1) {
                    if (amxKeyEthernetConnectToWifiFragment.isInternetValidated()) {
                        Log.i("AmxKeyEthernetConnectState", "Fake timeout; we're actually validated");
                        amxKeyEthernetConnectToWifiFragment.notifyListener(2);
                        return;
                    } else {
                        Log.i("AmxKeyEthernetConnectState", "Timeout is real; telling the listener");
                        amxKeyEthernetConnectToWifiFragment.notifyListener(2);
                        return;
                    }
                }
                if (i == 2) {
                    if (amxKeyEthernetConnectToWifiFragment.isNetworkConnected()) {
                        if (amxKeyEthernetConnectToWifiFragment.isInternetValidated()) {
                            Log.i("AmxKeyEthernetConnectState", "Connect check, connected, validated");
                            amxKeyEthernetConnectToWifiFragment.notifyListener(2);
                            return;
                        } else {
                            amxKeyEthernetConnectToWifiFragment.mHandler.removeMessages(2);
                            amxKeyEthernetConnectToWifiFragment.mHandler.sendEmptyMessageDelayed(2, 3000L);
                            return;
                        }
                    }
                    Log.i("AmxKeyEthernetConnectState", "Connect check, not connected");
                    amxKeyEthernetConnectToWifiFragment.notifyListener(2);
                    return;
                }
                Log.i("AmxKeyEthernetConnectState", "unknown msg");
            }
        }
    }
}
