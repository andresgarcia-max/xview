package com.control.device.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import com.android.tv.settings.accessories.BluetoothDevicePairer;

/* loaded from: classes.dex */
public class BluetoothAutoPairService extends Service implements BluetoothDevicePairer.EventListener {
    private static String SpringFramePairMode = "SpringFramePairMode";
    private static Intent intentSpring = null;
    private static String springFrameConnected = "android.intent.action.springFrameConnected";
    private BluetoothDevicePairer mBluetoothPairer;
    private final int bluetoothMessage = 1;
    private final long timePeriod = 5000;
    private Handler mHandler = new Handler() { // from class: com.control.device.service.BluetoothAutoPairService.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            super.handleMessage(message);
            if (message.what == 1) {
                Log.d("BluetoothAutoPairService", "<handleMessage> mBluetoothPairer != null = " + (BluetoothAutoPairService.this.mBluetoothPairer != null));
                if (BluetoothAutoPairService.this.mBluetoothPairer != null && BluetoothAutoPairService.this.mBluetoothPairer.getTargetDevice() == null) {
                    Log.d("BluetoothAutoPairService", "<handleMessage>, ReBluetoothPairer");
                    BluetoothAutoPairService.this.stopBluetoothPairer();
                    BluetoothAutoPairService.this.startBluetoothPairer();
                }
            }
            BluetoothAutoPairService.this.mHandler.sendEmptyMessageDelayed(1, 5000L);
        }
    };

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public void onCreate() {
        Log.d("BluetoothAutoPairService", "BluetoothAutoPairService onCreate");
        super.onCreate();
        this.mHandler.sendEmptyMessageDelayed(1, 1000L);
        BluetoothDevicePairer bluetoothDevicePairer = new BluetoothDevicePairer(this, this);
        this.mBluetoothPairer = bluetoothDevicePairer;
        bluetoothDevicePairer.start();
    }

    @Override // com.android.tv.settings.accessories.BluetoothDevicePairer.EventListener
    public void statusChanged() {
        BluetoothDevicePairer bluetoothDevicePairer = this.mBluetoothPairer;
        if (bluetoothDevicePairer == null) {
            return;
        }
        int status = bluetoothDevicePairer.getStatus();
        this.mBluetoothPairer.getTargetDevice();
        String name = this.mBluetoothPairer.getTargetDevice() == null ? "NONE" : this.mBluetoothPairer.getTargetDevice().getName();
        if (status == -1) {
            Log.d("BluetoothAutoPairService", "<statusChanged> STATUS_ERROR");
            SystemProperties.set("sys.uei.pairing", "0");
            Log.d("BluetoothAutoPairService", "[PairingBroadcast]statusChanged failure: 0");
            Intent intent = new Intent();
            intentSpring = intent;
            intent.putExtra("success", "0");
            intentSpring.setAction(springFrameConnected);
            sendBroadcast(intentSpring);
            Settings.Global.putString(getContentResolver(), SpringFramePairMode, "0");
            Log.d("BluetoothAutoPairService", "[PairingBroadcast]statusChanged failure: 1");
            stopSelf();
            return;
        }
        if (status == 0) {
            Log.d("BluetoothAutoPairService", "<statusChanged> STATUS_NONE");
            return;
        }
        if (status == 1) {
            Log.d("BluetoothAutoPairService", "<statusChanged> STATUS_SCANNING");
            return;
        }
        if (status == 2) {
            Log.d("BluetoothAutoPairService", "<statusChanged> STATUS_WAITING_TO_PAIR");
            return;
        }
        if (status == 3) {
            Log.d("BluetoothAutoPairService", "<statusChanged> STATUS_PAIRING");
            return;
        }
        if (status != 4) {
            return;
        }
        Log.d("BluetoothAutoPairService", "<statusChanged> STATUS_CONNECTING");
        Log.d("BluetoothAutoPairService", "<statusChanged> RemoteName = " + name);
        SystemProperties.set("sys.omni.pair", "0");
        SystemProperties.set("sys.uei.pair", "0");
        SystemProperties.set("sys.uei.pairing", "0");
        Log.d("BluetoothAutoPairService", "[PairingBroadcast]statusChanged success: 0");
        Intent intent2 = new Intent();
        intentSpring = intent2;
        intent2.putExtra("success", "1");
        intentSpring.setAction(springFrameConnected);
        sendBroadcast(intentSpring);
        Settings.Global.putString(getContentResolver(), SpringFramePairMode, "0");
        Log.d("BluetoothAutoPairService", "[PairingBroadcast]statusChanged success: 1");
        stopSelf();
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        return super.onStartCommand(intent, i, i2);
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        Log.d("BluetoothAutoPairService", "<onDestroy> ");
        stopBluetoothPairer();
        this.mHandler.removeMessages(1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopBluetoothPairer() {
        Log.d("BluetoothAutoPairService", "<stopBluetoothPairer> ");
        BluetoothDevicePairer bluetoothDevicePairer = this.mBluetoothPairer;
        if (bluetoothDevicePairer != null) {
            bluetoothDevicePairer.setListener(null);
            this.mBluetoothPairer.dispose();
            this.mBluetoothPairer = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startBluetoothPairer() {
        Log.d("BluetoothAutoPairService", "<startBluetoothPairer> ");
        stopBluetoothPairer();
        BluetoothDevicePairer bluetoothDevicePairer = new BluetoothDevicePairer(this, this);
        this.mBluetoothPairer = bluetoothDevicePairer;
        bluetoothDevicePairer.start();
        statusChanged();
    }
}
