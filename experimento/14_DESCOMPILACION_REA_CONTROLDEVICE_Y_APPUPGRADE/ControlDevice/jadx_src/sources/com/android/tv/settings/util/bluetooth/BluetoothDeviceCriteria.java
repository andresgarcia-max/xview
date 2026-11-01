package com.android.tv.settings.util.bluetooth;

import android.bluetooth.BluetoothDevice;
import java.util.regex.Pattern;

/* loaded from: classes.dex */
public class BluetoothDeviceCriteria {
    private final Pattern mAddressPattern;

    public boolean isMatchingDeviceClass(int i) {
        return true;
    }

    public boolean isMatchingMajorDeviceClass(int i) {
        return true;
    }

    public BluetoothDeviceCriteria() {
        this(".*");
    }

    public BluetoothDeviceCriteria(String str) {
        this.mAddressPattern = Pattern.compile(str, 2);
    }

    public final boolean isMatchingDevice(BluetoothDevice bluetoothDevice) {
        return bluetoothDevice != null && bluetoothDevice.getAddress() != null && isMatchingMacAddress(bluetoothDevice.getAddress()) && isMatchingMajorDeviceClass(bluetoothDevice.getBluetoothClass().getMajorDeviceClass()) && isMatchingDeviceClass(bluetoothDevice.getBluetoothClass().getDeviceClass());
    }

    public boolean isMatchingMacAddress(String str) {
        return this.mAddressPattern.matcher(str).matches();
    }
}
