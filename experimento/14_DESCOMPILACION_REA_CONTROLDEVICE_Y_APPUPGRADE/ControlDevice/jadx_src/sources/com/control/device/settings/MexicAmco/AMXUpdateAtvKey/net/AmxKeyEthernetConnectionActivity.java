package com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import com.android.tv.settings.core.instrumentation.InstrumentedActivity;
import com.control.device.R;
import com.control.device.settings.connectivity.State;
import com.control.device.settings.connectivity.StateMachine;

/* loaded from: classes.dex */
public class AmxKeyEthernetConnectionActivity extends InstrumentedActivity implements State.FragmentChangeListener {
    private State mConnectState;
    private StateMachine mStateMachine;
    private final StateMachine.Callback mStateMachineCallback = new StateMachine.Callback() { // from class: com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net.AmxKeyEthernetConnectionActivity.1
        @Override // com.control.device.settings.connectivity.StateMachine.Callback
        public void onFinish(int i) {
            AmxKeyEthernetConnectionActivity.this.setResult(i);
            AmxKeyEthernetConnectionActivity.this.finish();
        }
    };

    @Override // com.android.tv.settings.core.instrumentation.InstrumentedActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.amx_key_update_wifi_container);
        StateMachine stateMachine = (StateMachine) ViewModelProviders.of(this).get(StateMachine.class);
        this.mStateMachine = stateMachine;
        stateMachine.setCallback(this.mStateMachineCallback);
        AmxKeyEthernetConnectState amxKeyEthernetConnectState = new AmxKeyEthernetConnectState(this);
        this.mConnectState = amxKeyEthernetConnectState;
        this.mStateMachine.setStartState(amxKeyEthernetConnectState);
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
            beginTransaction.replace(R.id.amx_wifi_container, fragment, "AmxKeyEthernetConnectionActivity");
            beginTransaction.commit();
        }
    }

    @Override // com.control.device.settings.connectivity.State.FragmentChangeListener
    public void onFragmentChange(Fragment fragment, boolean z) {
        updateView(fragment, z);
    }
}
