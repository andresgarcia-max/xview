package com.control.device.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/* loaded from: classes.dex */
public class BluetoothInformationService extends Service {
    public static Timer timer = new Timer();
    private Context mContext;
    private CzBluetoothStateBroadcastReceiver mCzBluetoothStateBroadcastReceiver;
    private IntentFilter mFilter;
    private String mMarketCode = SystemProperties.get("config.MarketCode");
    private String mOperatorCode = SystemProperties.get("config.OperatorCode");

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        Log.d("BluetoothInformationService", "onCreate: ");
        this.mContext = this;
        TimerTask timerTask = new TimerTask() { // from class: com.control.device.service.BluetoothInformationService.1
            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                int i = Settings.Global.getInt(BluetoothInformationService.this.mContext.getContentResolver(), "device_provisioned", -1);
                BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
                Log.d("BluetoothInformationService", "<task> mCzBtAdapter : " + defaultAdapter);
                if (defaultAdapter != null) {
                    Log.d("BluetoothInformationService", "<task> mCzBtAdapter != null");
                    Set<BluetoothDevice> bondedDevices = defaultAdapter.getBondedDevices();
                    if (bondedDevices != null) {
                        Log.d("BluetoothInformationService", "<task> CzBondedDevices != null");
                        boolean z = false;
                        for (BluetoothDevice bluetoothDevice : bondedDevices) {
                            Log.d("BluetoothInformationService", "<task> STATUS_SCANNING, deviceName=" + bluetoothDevice.getName() + ", bfmPairingSuccess=" + z);
                            if (bluetoothDevice.getName().contains("KHAMSIN")) {
                                BluetoothInformationService.timer.cancel();
                                BluetoothInformationService.timer = null;
                                Log.d("BluetoothInformationService", "<task> KHAMSIN Remote had bonded , mRemotePairingSuccess state is : true");
                                Log.d("BluetoothInformationService", "<task> KHAMSIN Remote had bonded, 48 hours timer cancel");
                                z = true;
                            }
                        }
                        if (z || i != 1) {
                            return;
                        }
                        Log.d("BluetoothInformationService", "<task> 48 hours timer had arrived ,KHAMSIN Remote do not bonded ,now open ZTERemoteActivity");
                        Intent intent = new Intent();
                        intent.addCategory("android.intent.category.DEFAULT");
                        intent.addFlags(268435456);
                        intent.setAction("android.settings.ZTEREMOTE");
                        intent.setComponent(new ComponentName("com.control.device", "com.control.device.settings.remote.ZTERemoteActivity"));
                        BluetoothInformationService.this.mContext.startActivity(intent);
                    }
                }
            }
        };
        if (("6701".equals(this.mMarketCode) && "1".equals(this.mOperatorCode)) || (("6701".equals(this.mMarketCode) && "2".equals(this.mOperatorCode)) || ("4501".equals(this.mMarketCode) && "2".equals(this.mOperatorCode)))) {
            BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
            boolean z = false;
            Log.d("BluetoothInformationService", "mCzBtAdapter : " + defaultAdapter);
            if (defaultAdapter != null) {
                Log.d("BluetoothInformationService", "mCzBtAdapter != null");
                Set<BluetoothDevice> bondedDevices = defaultAdapter.getBondedDevices();
                if (bondedDevices != null) {
                    Log.d("BluetoothInformationService", "CzBondedDevices != null");
                    for (BluetoothDevice bluetoothDevice : bondedDevices) {
                        Log.d("BluetoothInformationService", "<statusChanged> STATUS_SCANNING, deviceName=" + bluetoothDevice.getName() + ", bfmPairingSuccess=" + z);
                        if (bluetoothDevice.getName().contains("KHAMSIN")) {
                            z = true;
                            Log.d("BluetoothInformationService", "KHAMSIN Remote had bonded , mRemotePairingSuccess state is : true");
                        }
                    }
                    if (z) {
                        return;
                    }
                    Log.d("BluetoothInformationService", "KHAMSIN Remote do not bonded , so after 48 hours open ZTERemoteActivity");
                    timer.schedule(timerTask, 172800000L);
                }
            }
        }
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        Log.d("BluetoothInformationService", "onStartCommand: ");
        this.mCzBluetoothStateBroadcastReceiver = new CzBluetoothStateBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        this.mFilter = intentFilter;
        intentFilter.addAction("android.bluetooth.device.action.BOND_STATE_CHANGED");
        this.mFilter.addAction("android.bluetooth.device.action.ACL_CONNECTED");
        this.mFilter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
        registerReceiver(this.mCzBluetoothStateBroadcastReceiver, this.mFilter);
        Log.d("BluetoothInformationService", "onCreate: register CzBluetoothStateBroadcastReceiver");
        return 1;
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        Log.d("BluetoothInformationService", "onBind: ");
        return null;
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        Log.d("BluetoothInformationService", "onDestroy: ");
        unregisterReceiver(this.mCzBluetoothStateBroadcastReceiver);
        Log.d("BluetoothInformationService", "onDestroy: unregister success");
    }

    private class CzBluetoothStateBroadcastReceiver extends BroadcastReceiver {
        private CzBluetoothStateBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(final Context context, Intent intent) {
            intent.getAction();
            if ("android.bluetooth.device.action.BOND_STATE_CHANGED".equals(intent.getAction())) {
                Log.d("BluetoothInformationService", "onReceive: ACTION_BOND_STATE_CHANGED");
                String name = ((BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE")).getName();
                Log.d("BluetoothInformationService", "onReceive: deviceName = " + name);
                int intExtra = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", -1);
                int intExtra2 = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", 10);
                int intExtra3 = intent.getIntExtra("android.bluetooth.device.extra.PREVIOUS_BOND_STATE", 10);
                Log.d("BluetoothInformationService", "<CzBluetoothStateBroadcastReceiver> Bond states become: old = " + intExtra3 + ", new = " + intExtra2);
                switch (intExtra) {
                    case 10:
                        Log.d("BluetoothInformationService", "onReceive: BOND_NONE");
                        break;
                    case 11:
                        Log.d("BluetoothInformationService", "onReceive: BOND_BONDING");
                        break;
                    case 12:
                        Log.d("BluetoothInformationService", "onReceive: BOND_BONDED");
                        break;
                }
                if (("6701".equals(BluetoothInformationService.this.mMarketCode) && "1".equals(BluetoothInformationService.this.mOperatorCode)) || (("6701".equals(BluetoothInformationService.this.mMarketCode) && "2".equals(BluetoothInformationService.this.mOperatorCode)) || ("4501".equals(BluetoothInformationService.this.mMarketCode) && "2".equals(BluetoothInformationService.this.mOperatorCode)))) {
                    Log.d("BluetoothInformationService", "bonded device name : " + name);
                    if (intExtra3 == 12 && intExtra2 == 10 && name.contains("KHAMSIN")) {
                        if (BluetoothInformationService.timer != null) {
                            Log.d("BluetoothInformationService", " KHAMSIN timer != null ,so timer cancel");
                            BluetoothInformationService.timer.cancel();
                            BluetoothInformationService.timer = null;
                        }
                        new Handler().postDelayed(new Runnable() { // from class: com.control.device.service.BluetoothInformationService.CzBluetoothStateBroadcastReceiver.1
                            @Override // java.lang.Runnable
                            public void run() {
                                Log.d("BluetoothInformationService", "KHAMSIN Remote disconnection ,now start ZTERemoteActivity BluetoothPairing");
                                Intent intent2 = new Intent();
                                intent2.addCategory("android.intent.category.DEFAULT");
                                intent2.addFlags(268435456);
                                intent2.setAction("android.settings.ZTEREMOTE");
                                intent2.setComponent(new ComponentName("com.control.device", "com.control.device.settings.remote.ZTERemoteActivity"));
                                context.startActivity(intent2);
                            }
                        }, 500L);
                    }
                }
            }
        }
    }
}
