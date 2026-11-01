package com.control.device.recevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/* loaded from: classes.dex */
public class WakeupReceiver extends BroadcastReceiver {
    private String TAG = WakeupReceiver.class.getName();

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(this.TAG, "WakeupReceiver onReceive: action --- " + action);
    }
}
