package com.android.tv.settings.accessories;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import com.android.tv.settings.util.DebugUtil;
import com.control.device.R;
import java.util.UUID;

/* loaded from: classes.dex */
public class RemotePowerReceiver extends BroadcastReceiver {
    private static Context context;
    private static BluetoothGatt mDeviceGattBattery;
    private final Handler mHandler = new Handler();
    private static final UUID GATT_BATTERY_SERVICE_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    private static final UUID GATT_BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    private static final boolean DEBUG = DebugUtil.DEBUG.booleanValue();

    interface IPowerReceiverUtils {
        void onDeviceConnected(BluetoothDevice bluetoothDevice, Context context);

        void onDeviceDisconnected(BluetoothDevice bluetoothDevice, Context context);
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context2, Intent intent) {
        String action = intent.getAction();
        Log.d("RemotePowerReceiver", "onReceive action : " + action);
        BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
        PowerReceiverUtilsImplIzzi powerReceiverUtilsImplIzzi = new PowerReceiverUtilsImplIzzi();
        context = context2;
        BluetoothGatt bluetoothGatt = mDeviceGattBattery;
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
        if (action.equals("android.bluetooth.device.action.ACL_CONNECTED") || action.equals("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED")) {
            powerReceiverUtilsImplIzzi.onDeviceConnected(bluetoothDevice, context2);
        }
        if (action.equals("android.bluetooth.device.action.ACL_DISCONNECTED")) {
            powerReceiverUtilsImplIzzi.onDeviceDisconnected(bluetoothDevice, context2);
        }
    }

    class PowerReceiverUtilsImplIzzi implements IPowerReceiverUtils {
        @Override // com.android.tv.settings.accessories.RemotePowerReceiver.IPowerReceiverUtils
        public void onDeviceDisconnected(BluetoothDevice bluetoothDevice, Context context) {
        }

        PowerReceiverUtilsImplIzzi() {
        }

        @Override // com.android.tv.settings.accessories.RemotePowerReceiver.IPowerReceiverUtils
        public void onDeviceConnected(BluetoothDevice bluetoothDevice, Context context) {
            if (RemotePowerReceiver.this.isIzziRemote(bluetoothDevice) && bluetoothDevice != null && bluetoothDevice.isConnected()) {
                if (bluetoothDevice.getType() == 2 || bluetoothDevice.getType() == 3) {
                    BluetoothGatt unused = RemotePowerReceiver.mDeviceGattBattery = bluetoothDevice.connectGatt(context, true, new GattBatteryCallbacks());
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isIzziRemote(BluetoothDevice bluetoothDevice) {
        String name = bluetoothDevice.getName();
        Log.v("RemotePowerReceiver", "bt device's name is :" + name);
        return name != null && name.toLowerCase().contains("izzi");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isDeviceBatLow(int i) {
        Log.v("RemotePowerReceiver", "device connected : " + i);
        return (i == -100 || i == -1 || i > 30) ? false : true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendLowPowerToast(final Context context2) {
        this.mHandler.post(new Runnable() { // from class: com.android.tv.settings.accessories.RemotePowerReceiver.1
            @Override // java.lang.Runnable
            public void run() {
                Context context3 = context2;
                Toast makeText = Toast.makeText(context3, context3.getString(R.string.bluetooth_low_battery_toast), 1);
                makeText.setGravity(17, 0, 0);
                makeText.show();
            }
        });
    }

    private class GattBatteryCallbacks extends BluetoothGattCallback {
        private GattBatteryCallbacks() {
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int i, int i2) {
            if (RemotePowerReceiver.DEBUG) {
                Log.d("RemotePowerReceiver", "Connection status:" + i + " state:" + i2);
            }
            if (i == 0 && i2 == 2) {
                bluetoothGatt.discoverServices();
            }
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int i) {
            if (i != 0) {
                if (RemotePowerReceiver.DEBUG) {
                    Log.e("RemotePowerReceiver", "Service discovery failure on " + bluetoothGatt);
                    return;
                }
                return;
            }
            BluetoothGattService service = bluetoothGatt.getService(RemotePowerReceiver.GATT_BATTERY_SERVICE_UUID);
            if (service == null) {
                if (RemotePowerReceiver.DEBUG) {
                    Log.d("RemotePowerReceiver", "No battery service");
                    return;
                }
                return;
            }
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(RemotePowerReceiver.GATT_BATTERY_LEVEL_CHARACTERISTIC_UUID);
            if (characteristic == null) {
                if (RemotePowerReceiver.DEBUG) {
                    Log.d("RemotePowerReceiver", "No battery level");
                    return;
                }
                return;
            }
            bluetoothGatt.readCharacteristic(characteristic);
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            if (i != 0) {
                if (RemotePowerReceiver.DEBUG) {
                    Log.e("RemotePowerReceiver", "Read characteristic failure on " + bluetoothGatt + " " + bluetoothGattCharacteristic);
                    return;
                }
                return;
            }
            if (RemotePowerReceiver.GATT_BATTERY_LEVEL_CHARACTERISTIC_UUID.equals(bluetoothGattCharacteristic.getUuid())) {
                if (RemotePowerReceiver.this.isDeviceBatLow(bluetoothGattCharacteristic.getIntValue(17, 0).intValue())) {
                    RemotePowerReceiver.this.sendLowPowerToast(RemotePowerReceiver.context);
                    if (RemotePowerReceiver.mDeviceGattBattery != null) {
                        RemotePowerReceiver.mDeviceGattBattery.close();
                        BluetoothGatt unused = RemotePowerReceiver.mDeviceGattBattery = null;
                    }
                }
            }
        }
    }
}
