package com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import com.android.settingslib.wifi.AccessPoint;
import com.android.tv.settings.connectivity.WifiConfigHelper;
import com.android.tv.settings.core.instrumentation.InstrumentedActivity;
import com.control.device.R;
import com.control.device.settings.connectivity.State;
import com.control.device.settings.connectivity.StateMachine;
import com.control.device.settings.connectivity.UserChoiceInfo;

/* loaded from: classes.dex */
public class AmxKeyWifiConnectionActivity extends InstrumentedActivity implements State.FragmentChangeListener {
    private State mAddStartState;
    private WifiConfiguration mConfiguration;
    private State mConnectState;
    private State mEnterPasswordState;
    private State mKnownNetworkState;
    private StateMachine mStateMachine;
    private final StateMachine.Callback mStateMachineCallback = new StateMachine.Callback() { // from class: com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net.AmxKeyWifiConnectionActivity.1
        @Override // com.control.device.settings.connectivity.StateMachine.Callback
        public void onFinish(int i) {
            AmxKeyWifiConnectionActivity.this.setResult(i);
            AmxKeyWifiConnectionActivity.this.finish();
        }
    };
    private int mWifiSecurity;

    public static Intent createIntent(Context context, AccessPoint accessPoint, int i) {
        return new Intent(context, (Class<?>) AmxKeyWifiConnectionActivity.class).putExtra("wifi_ssid", accessPoint.getSsidStr()).putExtra("wifi_security_name", i);
    }

    public static Intent createIntent(Context context, AccessPoint accessPoint) {
        return createIntent(context, accessPoint, accessPoint.getSecurity());
    }

    @Override // com.android.tv.settings.core.instrumentation.InstrumentedActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.amx_key_update_wifi_container);
        StateMachine stateMachine = (StateMachine) ViewModelProviders.of(this).get(StateMachine.class);
        this.mStateMachine = stateMachine;
        stateMachine.setCallback(this.mStateMachineCallback);
        this.mKnownNetworkState = new AmxKeyKnownNetworkState(this);
        this.mEnterPasswordState = new AmxKeyEnterPasswordState(this);
        this.mConnectState = new AmxKeyConnectState(this);
        AmxKeyAddStartState amxKeyAddStartState = new AmxKeyAddStartState(this);
        this.mAddStartState = amxKeyAddStartState;
        this.mStateMachine.addState(this.mKnownNetworkState, 0, amxKeyAddStartState);
        this.mStateMachine.addState(this.mAddStartState, 7, this.mEnterPasswordState);
        this.mStateMachine.addState(this.mAddStartState, 5, this.mConnectState);
        this.mStateMachine.addState(this.mEnterPasswordState, 5, this.mConnectState);
        this.mWifiSecurity = getIntent().getIntExtra("wifi_security_name", 0);
        this.mConfiguration = WifiConfigHelper.getConfiguration(this, getIntent().getStringExtra("wifi_ssid"), this.mWifiSecurity);
        UserChoiceInfo userChoiceInfo = (UserChoiceInfo) ViewModelProviders.of(this).get(UserChoiceInfo.class);
        userChoiceInfo.setWifiConfiguration(this.mConfiguration);
        userChoiceInfo.setWifiSecurity(this.mWifiSecurity);
        if (this.mConfiguration.getNetworkSelectionStatus().getNetworkSelectionDisableReason() == 8) {
            this.mStateMachine.setStartState(this.mEnterPasswordState);
        } else if (WifiConfigHelper.isNetworkSaved(this.mConfiguration)) {
            this.mStateMachine.setStartState(this.mKnownNetworkState);
        } else {
            this.mStateMachine.setStartState(this.mAddStartState);
        }
        this.mStateMachine.start(true);
    }

    @Override // androidx.activity.ComponentActivity, android.app.Activity
    public void onBackPressed() {
        this.mStateMachine.back();
    }

    private void updateView(Fragment fragment, boolean z) {
        if (fragment != null) {
            FragmentTransaction beginTransaction = getSupportFragmentManager().beginTransaction();
            if (z) {
                beginTransaction.setTransition(4097);
            } else {
                beginTransaction.setTransition(8194);
            }
            beginTransaction.replace(R.id.amx_wifi_container, fragment, "AmxKeyWifiConnectionActivity");
            beginTransaction.commit();
        }
    }

    @Override // com.control.device.settings.connectivity.State.FragmentChangeListener
    public void onFragmentChange(Fragment fragment, boolean z) {
        updateView(fragment, z);
    }
}
