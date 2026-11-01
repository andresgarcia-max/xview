package com.android.tv.settings.accessories;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.IBluetoothA2dp;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.view.InputDevice;
import com.android.tv.settings.util.bluetooth.BluetoothDeviceCriteria;
import com.android.tv.settings.util.bluetooth.BluetoothScanner;
import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes.dex */
public class BluetoothDevicePairer {
    private static final String[] INVALID_INPUT_KEYBOARD_DEVICE_NAMES = {"gpio-keypad", "cec_keyboard", "Virtual", "athome_remote"};
    private BroadcastReceiver mBluetoothStateReceiver;
    private final Context mContext;
    private final Handler mHandler;
    private InputDeviceCriteria mInputDeviceCriteria;
    private EventListener mListener;
    private BluetoothDevice mTarget;
    private String MarketCode_OperatorCode = SystemProperties.get("config.MarketCode") + " " + SystemProperties.get("config.OperatorCode");
    private final BluetoothScanner.Listener mBtListener = new BluetoothScanner.Listener() { // from class: com.android.tv.settings.accessories.BluetoothDevicePairer.1
        @Override // com.android.tv.settings.util.bluetooth.BluetoothScanner.Listener
        public void onDeviceAdded(BluetoothScanner.Device device2) {
            Log.d("BluetoothDevicePairer", "Adding device: " + device2.btDevice.getAddress());
            BluetoothDevicePairer.this.onDeviceFound(device2.btDevice);
        }

        @Override // com.android.tv.settings.util.bluetooth.BluetoothScanner.Listener
        public void onDeviceRemoved(BluetoothScanner.Device device2) {
            Log.d("BluetoothDevicePairer", "Device lost: " + device2.btDevice.getAddress());
            BluetoothDevicePairer.this.onDeviceLost(device2.btDevice);
        }
    };
    private final BroadcastReceiver mLinkStatusReceiver = new BroadcastReceiver() { // from class: com.android.tv.settings.accessories.BluetoothDevicePairer.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            Log.d("BluetoothDevicePairer", "There was a link status change for: " + bluetoothDevice.getAddress());
            if (bluetoothDevice.equals(BluetoothDevicePairer.this.mTarget)) {
                int intExtra = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", 10);
                int intExtra2 = intent.getIntExtra("android.bluetooth.device.extra.PREVIOUS_BOND_STATE", 10);
                Log.d("BluetoothDevicePairer", "Bond states: old = " + intExtra2 + ", new = " + intExtra);
                if (intExtra == 10 && intExtra2 == 11) {
                    BluetoothDevicePairer.this.unregisterLinkStatusReceiver();
                    BluetoothDevicePairer.this.onBondFailed();
                } else if (intExtra == 12) {
                    BluetoothDevicePairer.this.unregisterLinkStatusReceiver();
                    BluetoothDevicePairer.this.onBonded();
                }
            }
        }
    };
    private final OpenConnectionCallback mOpenConnectionCallback = new OpenConnectionCallback() { // from class: com.android.tv.settings.accessories.BluetoothDevicePairer.3
        @Override // com.android.tv.settings.accessories.BluetoothDevicePairer.OpenConnectionCallback
        public void succeeded() {
            BluetoothDevicePairer.this.setStatus(0);
        }

        @Override // com.android.tv.settings.accessories.BluetoothDevicePairer.OpenConnectionCallback
        public void failed() {
            BluetoothDevicePairer.this.setStatus(-1);
        }
    };
    private int mStatus = 0;
    private boolean mAutoMode = true;
    private final ArrayList<BluetoothDevice> mVisibleDevices = new ArrayList<>();
    private long mNextStageTimestamp = -1;
    private boolean mLinkReceiverRegistered = false;
    private final ArrayList<BluetoothDeviceCriteria> mBluetoothDeviceCriteria = new ArrayList<>();
    private int mDefaultScanMode = 0;

    public interface BluetoothConnector {
        void openConnection(BluetoothAdapter bluetoothAdapter);
    }

    public interface EventListener {
        void statusChanged();
    }

    public interface OpenConnectionCallback {
        void failed();

        void succeeded();
    }

    public static boolean hasValidInputDevice(Context context, int[] iArr) {
        InputManager inputManager = (InputManager) context.getSystemService("input");
        int length = iArr.length - 1;
        while (true) {
            boolean z = false;
            if (length <= -1) {
                return false;
            }
            InputDevice inputDevice = inputManager.getInputDevice(iArr[length]);
            int sources = inputDevice.getSources();
            boolean z2 = (sources & 513) == 513;
            if ((sources & 1025) == 1025) {
                z2 = true;
            }
            if ((sources & 8194) == 8194) {
                z2 = true;
            }
            if ((sources & 16777232) == 16777232) {
                z2 = true;
            }
            if ((sources & 257) == 257) {
                String name = inputDevice.getName();
                int i = 0;
                while (true) {
                    String[] strArr = INVALID_INPUT_KEYBOARD_DEVICE_NAMES;
                    if (i >= strArr.length) {
                        z = true;
                        break;
                    }
                    if (name.equals(strArr[i])) {
                        break;
                    }
                    i++;
                }
                if (z) {
                    z2 = true;
                }
            }
            if (!inputDevice.isVirtual() && z2) {
                return true;
            }
            length--;
        }
    }

    public BluetoothDevicePairer(Context context, EventListener eventListener) {
        this.mContext = context.getApplicationContext();
        this.mListener = eventListener;
        addBluetoothDeviceCriteria();
        this.mHandler = new Handler() { // from class: com.android.tv.settings.accessories.BluetoothDevicePairer.4
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                int i = message.what;
                if (i == 1) {
                    BluetoothDevicePairer.this.startBonding();
                    return;
                }
                if (i == 2) {
                    BluetoothDevicePairer.this.start();
                    return;
                }
                Log.d("BluetoothDevicePairer", "No handler case available for message: " + message.what);
            }
        };
    }

    private void addBluetoothDeviceCriteria() {
        InputDeviceCriteria inputDeviceCriteria = new InputDeviceCriteria();
        this.mInputDeviceCriteria = inputDeviceCriteria;
        this.mBluetoothDeviceCriteria.add(inputDeviceCriteria);
        ComponentName resolveSystemService = new Intent(IBluetoothA2dp.class.getName()).resolveSystemService(this.mContext.getPackageManager(), 0);
        if (resolveSystemService == null || this.mContext.getPackageManager().getComponentEnabledSetting(resolveSystemService) == 2) {
            return;
        }
        Log.d("BluetoothDevicePairer", "Adding A2dp device criteria for pairing");
        this.mBluetoothDeviceCriteria.add(new A2dpDeviceCriteria());
    }

    public void start() {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!defaultAdapter.isEnabled()) {
            Log.d("BluetoothDevicePairer", "Bluetooth not enabled, delaying startup.");
            if (this.mBluetoothStateReceiver == null) {
                BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { // from class: com.android.tv.settings.accessories.BluetoothDevicePairer.5
                    @Override // android.content.BroadcastReceiver
                    public void onReceive(Context context, Intent intent) {
                        if (intent.getIntExtra("android.bluetooth.adapter.extra.STATE", 10) == 12) {
                            Log.d("BluetoothDevicePairer", "Bluetooth now enabled, starting.");
                            BluetoothDevicePairer.this.start();
                        } else {
                            Log.d("BluetoothDevicePairer", "Bluetooth not yet started, got broadcast: " + intent);
                        }
                    }
                };
                this.mBluetoothStateReceiver = broadcastReceiver;
                this.mContext.registerReceiver(broadcastReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
            }
            defaultAdapter.enable();
            return;
        }
        BroadcastReceiver broadcastReceiver2 = this.mBluetoothStateReceiver;
        if (broadcastReceiver2 != null) {
            this.mContext.unregisterReceiver(broadcastReceiver2);
            this.mBluetoothStateReceiver = null;
        }
        int scanMode = defaultAdapter.getScanMode();
        if (scanMode != 23) {
            Log.d("BluetoothDevicePairer", "Turning on discoverability, default scan mode: " + scanMode);
            this.mDefaultScanMode = scanMode;
            defaultAdapter.setScanMode(23, 0L);
        }
        setStatus(1);
        BluetoothScanner.startListening(this.mContext, this.mBtListener, this.mBluetoothDeviceCriteria);
    }

    public void cancelPairing() {
        this.mAutoMode = false;
        doCancel();
    }

    public void dispose() {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        int i = this.mDefaultScanMode;
        if (i != 0 && i != defaultAdapter.getScanMode()) {
            Log.d("BluetoothDevicePairer", "Resetting discoverability to: " + this.mDefaultScanMode);
            defaultAdapter.setScanMode(this.mDefaultScanMode);
        }
        this.mHandler.removeCallbacksAndMessages(null);
        if (this.mLinkReceiverRegistered) {
            unregisterLinkStatusReceiver();
        }
        BroadcastReceiver broadcastReceiver = this.mBluetoothStateReceiver;
        if (broadcastReceiver != null) {
            this.mContext.unregisterReceiver(broadcastReceiver);
        }
        stopScanning();
    }

    public int getStatus() {
        return this.mStatus;
    }

    public BluetoothDevice getTargetDevice() {
        return this.mTarget;
    }

    public void setListener(EventListener eventListener) {
        this.mListener = eventListener;
    }

    private void startPairing(BluetoothDevice bluetoothDevice, boolean z) {
        this.mAutoMode = !z;
        this.mTarget = bluetoothDevice;
        if (isInProgress()) {
            throw new RuntimeException("Pairing already in progress, you must cancel the previous request first");
        }
        this.mHandler.removeCallbacksAndMessages(null);
        long elapsedRealtime = SystemClock.elapsedRealtime();
        boolean z2 = this.mAutoMode;
        this.mNextStageTimestamp = elapsedRealtime + (z2 ? 3000 : 5000);
        this.mHandler.sendEmptyMessageDelayed(1, z2 ? 3000L : 5000L);
        setStatus(2);
    }

    public boolean isInProgress() {
        int i = this.mStatus;
        return (i == 0 || i == -1 || i == 1 || i == 2) ? false : true;
    }

    private void updateListener() {
        EventListener eventListener = this.mListener;
        if (eventListener != null) {
            eventListener.statusChanged();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onDeviceFound(BluetoothDevice bluetoothDevice) {
        if (this.mAutoMode && this.mTarget != null) {
            Log.i("BluetoothDevicePairer", "onDeviceFound, auto mode ,there is already one pairing, ignore this, name=" + bluetoothDevice.getName() + ", address=" + bluetoothDevice.getAddress());
            return;
        }
        if (this.mVisibleDevices.contains(bluetoothDevice)) {
            return;
        }
        this.mVisibleDevices.add(bluetoothDevice);
        Log.d("BluetoothDevicePairer", "Added device to visible list. Name = " + bluetoothDevice.getName() + " , class = " + bluetoothDevice.getBluetoothClass().getDeviceClass());
        updatePairingState();
        updateListener();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onDeviceLost(BluetoothDevice bluetoothDevice) {
        if (this.mVisibleDevices.remove(bluetoothDevice)) {
            updatePairingState();
            updateListener();
        }
    }

    private void updatePairingState() {
        if (this.mAutoMode) {
            BluetoothDevice autoPairDevice = getAutoPairDevice();
            if (autoPairDevice != null) {
                this.mTarget = autoPairDevice;
                startPairing(autoPairDevice, false);
            } else {
                doCancel();
            }
        }
    }

    private BluetoothDevice getAutoPairDevice() {
        ArrayList arrayList = new ArrayList();
        Iterator<BluetoothDevice> it = this.mVisibleDevices.iterator();
        while (it.hasNext()) {
            BluetoothDevice next = it.next();
            if (this.mInputDeviceCriteria.isInputDevice(next.getBluetoothClass())) {
                arrayList.add(next);
            }
        }
        if (arrayList.size() == 1) {
            return (BluetoothDevice) arrayList.get(0);
        }
        return null;
    }

    private void doCancel() {
        if (isInProgress()) {
            Log.d("BluetoothDevicePairer", "Pairing process has already begun, it can not be canceled.");
            return;
        }
        boolean stopListening = BluetoothScanner.stopListening(this.mBtListener);
        BluetoothScanner.stopNow();
        this.mHandler.removeCallbacksAndMessages(null);
        unpairDevice(this.mTarget);
        this.mTarget = null;
        setStatus(0);
        if (stopListening) {
            start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setStatus(int i) {
        this.mStatus = i;
        updateListener();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startBonding() {
        stopScanning();
        setStatus(3);
        if (this.mTarget.getBondState() != 12) {
            registerLinkStatusReceiver();
            this.mTarget.createBond();
        } else {
            onBonded();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onBonded() {
        BluetoothDevice targetDevice = getTargetDevice();
        if (targetDevice.getBluetoothClass().getDeviceClass() != 1408 || targetDevice.getType() != 1) {
            openConnection();
        } else if (targetDevice.isConnected()) {
            setStatus(5);
        } else {
            Log.w("BluetoothDevicePairer", "There was an error connect by BR/EDR Mouse.");
            setStatus(-1);
        }
    }

    private void openConnection() {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothConnector bluetoothConnector = getBluetoothConnector();
        if (bluetoothConnector != null) {
            setStatus(4);
            bluetoothConnector.openConnection(defaultAdapter);
            return;
        }
        Log.w("BluetoothDevicePairer", "There was an error getting the BluetoothConnector.");
        setStatus(-1);
        if (this.mLinkReceiverRegistered) {
            unregisterLinkStatusReceiver();
        }
        unpairDevice(this.mTarget);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onBondFailed() {
        Log.w("BluetoothDevicePairer", "There was an error bonding with the device.");
        setStatus(-1);
        unpairDevice(this.mTarget);
        this.mNextStageTimestamp = SystemClock.elapsedRealtime() + 5000;
        this.mHandler.sendEmptyMessageDelayed(2, 5000L);
    }

    private void registerLinkStatusReceiver() {
        this.mLinkReceiverRegistered = true;
        this.mContext.registerReceiver(this.mLinkStatusReceiver, new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void unregisterLinkStatusReceiver() {
        this.mLinkReceiverRegistered = false;
        this.mContext.unregisterReceiver(this.mLinkStatusReceiver);
    }

    private void stopScanning() {
        BluetoothScanner.stopListening(this.mBtListener);
        BluetoothScanner.stopNow();
    }

    public boolean unpairDevice(BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice == null) {
            return false;
        }
        int bondState = bluetoothDevice.getBondState();
        if (bondState == 11) {
            bluetoothDevice.cancelBondProcess();
        }
        if (bondState == 10) {
            return false;
        }
        if (bluetoothDevice.removeBond()) {
            Log.d("BluetoothDevicePairer", "Bluetooth device successfully unpaired: " + bluetoothDevice.getName());
            return true;
        }
        Log.e("BluetoothDevicePairer", "Failed to unpair Bluetooth Device: " + bluetoothDevice.getName());
        return false;
    }

    private BluetoothConnector getBluetoothConnector() {
        int majorDeviceClass = this.mTarget.getBluetoothClass().getMajorDeviceClass();
        if (majorDeviceClass == 1024) {
            return new BluetoothA2dpConnector(this.mContext, this.mTarget, this.mOpenConnectionCallback);
        }
        if (majorDeviceClass == 1280) {
            return new BluetoothInputDeviceConnector(this.mContext, this.mTarget, this.mHandler, this.mOpenConnectionCallback);
        }
        Log.d("BluetoothDevicePairer", "Unhandle device class: " + majorDeviceClass);
        return null;
    }
}
