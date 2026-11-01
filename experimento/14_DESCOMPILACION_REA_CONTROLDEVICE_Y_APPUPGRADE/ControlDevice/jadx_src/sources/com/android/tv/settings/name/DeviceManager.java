package com.android.tv.settings.name;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/* loaded from: classes.dex */
public class DeviceManager {
    public static void setDeviceName(Context context, String str) {
        Settings.Global.putString(context.getContentResolver(), "device_name", str);
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (defaultAdapter != null) {
            defaultAdapter.setName(str);
        } else {
            Log.v("DeviceManager", "Bluetooth adapter is null. Running on device without bluetooth?");
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("com.android.tv.settings.name.DeviceManager.DEVICE_NAME_UPDATE"));
    }
}
