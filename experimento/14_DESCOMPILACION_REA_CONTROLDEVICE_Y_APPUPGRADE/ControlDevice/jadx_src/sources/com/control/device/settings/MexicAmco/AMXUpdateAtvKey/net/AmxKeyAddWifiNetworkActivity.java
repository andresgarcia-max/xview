package com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import com.android.tv.settings.core.instrumentation.InstrumentedActivity;
import com.control.device.R;
import com.control.device.settings.connectivity.State;
import com.control.device.settings.connectivity.StateMachine;
import com.control.device.settings.connectivity.UserChoiceInfo;

/* loaded from: classes.dex */
public class AmxKeyAddWifiNetworkActivity extends InstrumentedActivity implements State.FragmentChangeListener {
    private State mChooseSecurityState;
    private State mConnectState;
    private State mEnterPasswordState;
    private State mEnterSsidState;
    private StateMachine mStateMachine;
    private final StateMachine.Callback mStateMachineCallback = new StateMachine.Callback() { // from class: com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net.AmxKeyAddWifiNetworkActivity.1
        @Override // com.control.device.settings.connectivity.StateMachine.Callback
        public void onFinish(int i) {
            AmxKeyAddWifiNetworkActivity.this.setResult(i);
            AmxKeyAddWifiNetworkActivity.this.finish();
        }
    };

    @Override // com.android.tv.settings.core.instrumentation.InstrumentedActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.amx_key_update_wifi_container);
        StateMachine stateMachine = (StateMachine) ViewModelProviders.of(this).get(StateMachine.class);
        this.mStateMachine = stateMachine;
        stateMachine.setCallback(this.mStateMachineCallback);
        ((UserChoiceInfo) ViewModelProviders.of(this).get(UserChoiceInfo.class)).getWifiConfiguration().hiddenSSID = true;
        this.mEnterSsidState = new AmxKeyEnterSsidState(this);
        this.mChooseSecurityState = new AmxKeyChooseSecurityState(this);
        this.mEnterPasswordState = new AmxKeyEnterPasswordState(this);
        this.mConnectState = new AmxKeyConnectState(this);
        this.mStateMachine.addState(this.mEnterSsidState, 2, this.mChooseSecurityState);
        this.mStateMachine.addState(this.mChooseSecurityState, 5, this.mConnectState);
        this.mStateMachine.addState(this.mChooseSecurityState, 7, this.mEnterPasswordState);
        this.mStateMachine.addState(this.mEnterPasswordState, 5, this.mConnectState);
        this.mStateMachine.setStartState(this.mEnterSsidState);
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
            beginTransaction.replace(R.id.amx_wifi_container, fragment, "AmxKeyAddWifiNetworkActivity");
            beginTransaction.commit();
        }
    }

    @Override // com.control.device.settings.connectivity.State.FragmentChangeListener
    public void onFragmentChange(Fragment fragment, boolean z) {
        updateView(fragment, z);
    }
}
