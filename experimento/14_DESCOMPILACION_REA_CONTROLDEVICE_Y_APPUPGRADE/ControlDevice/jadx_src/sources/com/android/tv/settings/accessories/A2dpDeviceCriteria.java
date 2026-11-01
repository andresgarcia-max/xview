package com.android.tv.settings.accessories;

import android.util.Log;
import com.android.tv.settings.util.bluetooth.BluetoothDeviceCriteria;

/* loaded from: classes.dex */
public class A2dpDeviceCriteria extends BluetoothDeviceCriteria {
    @Override // com.android.tv.settings.util.bluetooth.BluetoothDeviceCriteria
    public boolean isMatchingMajorDeviceClass(int i) {
        return i == 1024;
    }

    @Override // com.android.tv.settings.util.bluetooth.BluetoothDeviceCriteria
    public boolean isMatchingDeviceClass(int i) {
        Log.d("aah.A2dpDeviceCriteria", "isMatchingDeviceClass : " + i);
        return i == 1024 || i == 1028 || i == 1048 || i == 1044 || i == 1052 || i == 1064;
    }
}
