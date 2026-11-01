package com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net;

import android.net.wifi.WifiConfiguration;
import android.text.TextUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import com.android.tv.settings.connectivity.util.WifiSecurityUtil;
import com.control.device.settings.connectivity.State;
import com.control.device.settings.connectivity.StateMachine;
import com.control.device.settings.connectivity.UserChoiceInfo;

/* loaded from: classes.dex */
public class AmxKeyAddStartState implements State {
    private final FragmentActivity mActivity;
    private Fragment mFragment;
    private final StateMachine mStateMachine;
    private final UserChoiceInfo mUserChoiceInfo;

    public AmxKeyAddStartState(FragmentActivity fragmentActivity) {
        this.mActivity = fragmentActivity;
        this.mUserChoiceInfo = (UserChoiceInfo) ViewModelProviders.of(fragmentActivity).get(UserChoiceInfo.class);
        this.mStateMachine = (StateMachine) ViewModelProviders.of(fragmentActivity).get(StateMachine.class);
    }

    @Override // com.control.device.settings.connectivity.State
    public void processForward() {
        this.mFragment = null;
        int wifiSecurity = this.mUserChoiceInfo.getWifiSecurity();
        WifiConfiguration wifiConfiguration = this.mUserChoiceInfo.getWifiConfiguration();
        if ((wifiSecurity == 1 && TextUtils.isEmpty(wifiConfiguration.wepKeys[0])) || (!WifiSecurityUtil.isOpen(wifiSecurity) && TextUtils.isEmpty(wifiConfiguration.preSharedKey))) {
            this.mStateMachine.getListener().onComplete(7);
        } else {
            this.mStateMachine.getListener().onComplete(5);
        }
    }

    @Override // com.control.device.settings.connectivity.State
    public void processBackward() {
        this.mStateMachine.back();
    }
}
