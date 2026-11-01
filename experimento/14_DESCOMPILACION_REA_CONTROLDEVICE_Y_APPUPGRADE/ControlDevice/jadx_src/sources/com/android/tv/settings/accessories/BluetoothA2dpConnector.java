package com.android.tv.settings.accessories;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.android.tv.settings.accessories.BluetoothDevicePairer;

/* loaded from: classes.dex */
public class BluetoothA2dpConnector implements BluetoothDevicePairer.BluetoothConnector {
    private BluetoothA2dp mA2dpProfile;
    private Context mContext;
    private BluetoothDevicePairer.OpenConnectionCallback mOpenConnectionCallback;
    private BluetoothDevice mTarget;
    private boolean mConnectionStateReceiverRegistered = false;
    private Handler mHandler = new Handler() { // from class: com.android.tv.settings.accessories.BluetoothA2dpConnector.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i != 1) {
                if (i == 2 && BluetoothA2dpConnector.this.mA2dpProfile != null) {
                    BluetoothA2dpConnector.this.mA2dpProfile.connect(BluetoothA2dpConnector.this.mTarget);
                    BluetoothA2dpConnector.this.mA2dpProfile.setPriority(BluetoothA2dpConnector.this.mTarget, 1000);
                    return;
                }
                return;
            }
            BluetoothA2dpConnector.this.failed();
        }
    };
    private BroadcastReceiver mConnectionStateReceiver = new BroadcastReceiver() { // from class: com.android.tv.settings.accessories.BluetoothA2dpConnector.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (((BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE")).equals(BluetoothA2dpConnector.this.mTarget)) {
                if ("android.bluetooth.device.action.UUID".equals(intent.getAction())) {
                    BluetoothA2dpConnector.this.mHandler.sendEmptyMessageDelayed(1, 10000L);
                    if (BluetoothA2dpConnector.this.mHandler.hasMessages(2)) {
                        return;
                    }
                    BluetoothA2dpConnector.this.mHandler.sendEmptyMessageDelayed(2, 1000L);
                    return;
                }
                int intExtra = intent.getIntExtra("android.bluetooth.profile.extra.PREVIOUS_STATE", 1);
                int intExtra2 = intent.getIntExtra("android.bluetooth.profile.extra.STATE", 1);
                if (intExtra == 1) {
                    if (intExtra2 == 2) {
                        BluetoothA2dpConnector.this.succeeded();
                    } else if (intExtra2 == 0) {
                        Log.d("BluetoothA2dpConnector", "Failed to connect");
                        BluetoothA2dpConnector.this.failed();
                    }
                    BluetoothA2dpConnector.this.unregisterConnectionStateReceiver();
                    BluetoothA2dpConnector.this.closeA2dpProfileProxy();
                }
            }
        }
    };
    private BluetoothProfile.ServiceListener mServiceConnection = new BluetoothProfile.ServiceListener() { // from class: com.android.tv.settings.accessories.BluetoothA2dpConnector.3
        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceDisconnected(int i) {
            Log.w("BluetoothA2dpConnector", "Service disconnected, perhaps unexpectedly");
            BluetoothA2dpConnector.this.unregisterConnectionStateReceiver();
            BluetoothA2dpConnector.this.closeA2dpProfileProxy();
            BluetoothA2dpConnector.this.failed();
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
            BluetoothA2dpConnector.this.mA2dpProfile = (BluetoothA2dp) bluetoothProfile;
            BluetoothA2dpConnector.this.registerConnectionStateReceiver();
            BluetoothA2dpConnector.this.mTarget.fetchUuidsWithSdp();
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    public void succeeded() {
        this.mHandler.removeCallbacksAndMessages(null);
        this.mOpenConnectionCallback.succeeded();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void failed() {
        this.mHandler.removeCallbacksAndMessages(null);
        this.mOpenConnectionCallback.failed();
    }

    public BluetoothA2dpConnector(Context context, BluetoothDevice bluetoothDevice, BluetoothDevicePairer.OpenConnectionCallback openConnectionCallback) {
        this.mContext = context;
        this.mTarget = bluetoothDevice;
        this.mOpenConnectionCallback = openConnectionCallback;
    }

    @Override // com.android.tv.settings.accessories.BluetoothDevicePairer.BluetoothConnector
    public void openConnection(BluetoothAdapter bluetoothAdapter) {
        if (bluetoothAdapter.getProfileProxy(this.mContext, this.mServiceConnection, 2)) {
            return;
        }
        failed();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void closeA2dpProfileProxy() {
        this.mHandler.removeCallbacksAndMessages(null);
        if (this.mA2dpProfile != null) {
            try {
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(2, this.mA2dpProfile);
                this.mA2dpProfile = null;
            } catch (Throwable th) {
                Log.w("BluetoothA2dpConnector", "Error cleaning up A2DP proxy", th);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void registerConnectionStateReceiver() {
        IntentFilter intentFilter = new IntentFilter("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.device.action.UUID");
        this.mContext.registerReceiver(this.mConnectionStateReceiver, intentFilter);
        this.mConnectionStateReceiverRegistered = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void unregisterConnectionStateReceiver() {
        if (this.mConnectionStateReceiverRegistered) {
            this.mContext.unregisterReceiver(this.mConnectionStateReceiver);
            this.mConnectionStateReceiverRegistered = false;
        }
    }
}
