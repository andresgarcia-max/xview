package com.android.tv.settings.accessories;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidHost;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.util.Log;
import com.android.tv.settings.accessories.BluetoothDevicePairer;

/* loaded from: classes.dex */
public class BluetoothInputDeviceConnector implements BluetoothDevicePairer.BluetoothConnector {
    private static final String[] INVALID_INPUT_KEYBOARD_DEVICE_NAMES = {"gpio-keypad", "cec_keyboard", "Virtual", "athome_remote"};
    private Context mContext;
    private Handler mHandler;
    private BluetoothHidHost mInputProxy;
    private BluetoothDevicePairer.OpenConnectionCallback mOpenConnectionCallback;
    private BluetoothDevice mTarget;
    private BluetoothProfile.ServiceListener mServiceConnection = new BluetoothProfile.ServiceListener() { // from class: com.android.tv.settings.accessories.BluetoothInputDeviceConnector.1
        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceDisconnected(int i) {
            Log.w("BtInputDeviceConnector", "Service disconnected, perhaps unexpectedly");
            BluetoothInputDeviceConnector.this.unregisterInputMethodMonitor();
            BluetoothInputDeviceConnector.this.closeInputProfileProxy();
            BluetoothInputDeviceConnector.this.mOpenConnectionCallback.failed();
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
            BluetoothInputDeviceConnector.this.mInputProxy = (BluetoothHidHost) bluetoothProfile;
            if (BluetoothInputDeviceConnector.this.mTarget != null) {
                BluetoothInputDeviceConnector.this.registerInputMethodMonitor();
                BluetoothInputDeviceConnector.this.mInputProxy.connect(BluetoothInputDeviceConnector.this.mTarget);
                BluetoothInputDeviceConnector.this.mInputProxy.setPriority(BluetoothInputDeviceConnector.this.mTarget, 1000);
            }
        }
    };
    private boolean mInputMethodMonitorRegistered = false;
    private InputManager.InputDeviceListener mInputListener = new InputManager.InputDeviceListener() { // from class: com.android.tv.settings.accessories.BluetoothInputDeviceConnector.2
        @Override // android.hardware.input.InputManager.InputDeviceListener
        public void onInputDeviceChanged(int i) {
        }

        @Override // android.hardware.input.InputManager.InputDeviceListener
        public void onInputDeviceRemoved(int i) {
        }

        @Override // android.hardware.input.InputManager.InputDeviceListener
        public void onInputDeviceAdded(int i) {
            if (BluetoothDevicePairer.hasValidInputDevice(BluetoothInputDeviceConnector.this.mContext, new int[]{i})) {
                BluetoothInputDeviceConnector.this.onInputAdded();
            }
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    public void registerInputMethodMonitor() {
        InputManager inputManager = (InputManager) this.mContext.getSystemService("input");
        inputManager.registerInputDeviceListener(this.mInputListener, this.mHandler);
        inputManager.getInputDeviceIds();
        this.mInputMethodMonitorRegistered = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onInputAdded() {
        unregisterInputMethodMonitor();
        closeInputProfileProxy();
        this.mOpenConnectionCallback.succeeded();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void unregisterInputMethodMonitor() {
        if (this.mInputMethodMonitorRegistered) {
            ((InputManager) this.mContext.getSystemService("input")).unregisterInputDeviceListener(this.mInputListener);
            this.mInputMethodMonitorRegistered = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void closeInputProfileProxy() {
        if (this.mInputProxy != null) {
            try {
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(4, this.mInputProxy);
                this.mInputProxy = null;
            } catch (Throwable th) {
                Log.w("BtInputDeviceConnector", "Error cleaning up input profile proxy", th);
            }
        }
    }

    public BluetoothInputDeviceConnector(Context context, BluetoothDevice bluetoothDevice, Handler handler, BluetoothDevicePairer.OpenConnectionCallback openConnectionCallback) {
        this.mContext = context;
        this.mTarget = bluetoothDevice;
        this.mHandler = handler;
        this.mOpenConnectionCallback = openConnectionCallback;
    }

    @Override // com.android.tv.settings.accessories.BluetoothDevicePairer.BluetoothConnector
    public void openConnection(BluetoothAdapter bluetoothAdapter) {
        if (bluetoothAdapter.getProfileProxy(this.mContext, this.mServiceConnection, 4)) {
            return;
        }
        this.mOpenConnectionCallback.failed();
    }
}
