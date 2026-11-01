package com.android.tv.settings.util.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class BluetoothScanner {
    private static Receiver sReceiver;

    public static class Listener {
        public void onDeviceAdded(Device device2) {
            throw null;
        }

        public void onDeviceChanged(Device device2) {
        }

        public void onDeviceRemoved(Device device2) {
            throw null;
        }

        public void onScanningStarted() {
        }

        public void onScanningStopped(ArrayList<Device> arrayList) {
        }
    }

    public static class Device {
        public String address;
        public BluetoothDevice btDevice;
        public String btName;
        public int consecutiveMisses;
        public LedConfiguration leds;
        public String name = "";
        public int configurationType = 0;

        public String toString() {
            return "Device(addr=" + this.address + " name=\"" + this.name + "\" leds=" + this.leds + "\" configuration_type=" + this.configurationType + ")";
        }

        public boolean setNameString(String str) {
            this.btName = str;
            if (str == null || !BluetoothNameUtils.isValidName(str)) {
                this.name = "";
                this.leds = null;
                return false;
            }
            this.leds = BluetoothNameUtils.getColorConfiguration(str);
            this.configurationType = BluetoothNameUtils.getSetupType(str);
            return true;
        }
    }

    public static void startListening(Context context, Listener listener, List<BluetoothDeviceCriteria> list) {
        if (sReceiver == null) {
            sReceiver = new Receiver(context.getApplicationContext());
        }
        sReceiver.startListening(listener, list);
        Log.d("BluetoothScanner", "startListening");
    }

    public static boolean stopListening(Listener listener) {
        Log.d("BluetoothScanner", "stopListening sReceiver=" + sReceiver);
        Receiver receiver = sReceiver;
        if (receiver != null) {
            return receiver.stopListening(listener);
        }
        return false;
    }

    public static void stopNow() {
        Receiver receiver = sReceiver;
        if (receiver != null) {
            receiver.stopNow();
        }
    }

    private static class ClientRecord {
        public final ArrayList<Device> devices = new ArrayList<>();
        public final Listener listener;
        public final List<BluetoothDeviceCriteria> matchers;

        public ClientRecord(Listener listener, List<BluetoothDeviceCriteria> list) {
            this.listener = listener;
            this.matchers = list;
        }
    }

    private static class Receiver extends BroadcastReceiver {
        private static boolean mKeepScanning;
        private final Context mContext;
        private final Handler mHandler = new Handler();
        private final ArrayList<ClientRecord> mClients = new ArrayList<>();
        private final ArrayList<Device> mPresentDevices = new ArrayList<>();
        private boolean mRegistered = false;
        private final Object mListenerLock = new Object();
        private final Runnable mStopTask = new Runnable() { // from class: com.android.tv.settings.util.bluetooth.BluetoothScanner.Receiver.2
            @Override // java.lang.Runnable
            public void run() {
                synchronized (Receiver.this.mListenerLock) {
                    if (Receiver.this.mClients.size() != 0) {
                        throw new RuntimeException("mStopTask running with mListeners.size=" + Receiver.this.mClients.size());
                    }
                }
                Receiver.this.stopNow();
            }
        };
        private final Runnable mScanTask = new Runnable() { // from class: com.android.tv.settings.util.bluetooth.BluetoothScanner.Receiver.3
            @Override // java.lang.Runnable
            public void run() {
                Receiver.this.mHandler.removeCallbacks(Receiver.this.mScanTask);
                Receiver.this.scanNow();
            }
        };
        private final BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        public Receiver(Context context) {
            this.mContext = context;
        }

        public void startListening(Listener listener, List<BluetoothDeviceCriteria> list) {
            int size;
            ClientRecord clientRecord = new ClientRecord(listener, list);
            synchronized (this.mListenerLock) {
                for (int size2 = this.mClients.size() - 1; size2 > -1; size2--) {
                    if (this.mClients.get(size2).listener == listener) {
                        throw new RuntimeException("Listener already registered: " + listener);
                    }
                }
                this.mClients.add(clientRecord);
                size = this.mClients.size();
            }
            if (size == 1) {
                this.mPresentDevices.clear();
                IntentFilter intentFilter = new IntentFilter("android.bluetooth.device.action.FOUND");
                intentFilter.addAction("android.bluetooth.adapter.action.DISCOVERY_FINISHED");
                this.mContext.registerReceiver(this, intentFilter);
                this.mRegistered = true;
            }
            mKeepScanning = true;
            int size3 = this.mPresentDevices.size();
            for (int i = 0; i < size3; i++) {
                Device device2 = this.mPresentDevices.get(i);
                Iterator<BluetoothDeviceCriteria> it = clientRecord.matchers.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    if (it.next().isMatchingDevice(device2.btDevice)) {
                        clientRecord.devices.add(device2);
                        clientRecord.listener.onDeviceAdded(device2);
                        break;
                    }
                }
            }
            this.mHandler.removeCallbacks(this.mStopTask);
            this.mHandler.removeCallbacks(this.mScanTask);
            scanNow();
        }

        public boolean stopListening(Listener listener) {
            boolean z;
            int size;
            synchronized (this.mListenerLock) {
                z = true;
                int size2 = this.mClients.size() - 1;
                while (true) {
                    if (size2 <= -1) {
                        z = false;
                        break;
                    }
                    if (this.mClients.get(size2).listener == listener) {
                        this.mClients.remove(size2);
                        break;
                    }
                    size2--;
                }
                size = this.mClients.size();
            }
            if (size == 0) {
                this.mHandler.removeCallbacks(this.mStopTask);
                this.mHandler.postDelayed(this.mStopTask, 20000L);
            }
            return z;
        }

        public void scanNow() {
            Long l = 0L;
            if (this.mBtAdapter.isDiscovering()) {
                this.mBtAdapter.cancelDiscovery();
                l = 300L;
            }
            this.mHandler.postDelayed(new Runnable() { // from class: com.android.tv.settings.util.bluetooth.BluetoothScanner.Receiver.1
                @Override // java.lang.Runnable
                public void run() {
                    Receiver.this.sendScanningStarted();
                    Receiver.this.mBtAdapter.startDiscovery();
                }
            }, l.longValue());
        }

        public void stopNow() {
            int size;
            synchronized (this.mListenerLock) {
                size = this.mClients.size();
            }
            if (size == 0) {
                Log.d("BluetoothScanner", "mStopTask.run()");
                this.mHandler.removeCallbacks(this.mScanTask);
                this.mHandler.removeCallbacks(this.mStopTask);
                BluetoothAdapter bluetoothAdapter = this.mBtAdapter;
                if (bluetoothAdapter != null) {
                    bluetoothAdapter.cancelDiscovery();
                }
                mKeepScanning = false;
                if (BluetoothAdapter.getDefaultAdapter().isEnabled() && this.mRegistered) {
                    this.mContext.unregisterReceiver(this);
                    this.mRegistered = false;
                }
            }
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int i = 0;
            if ("android.bluetooth.device.action.FOUND".equals(action)) {
                BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                String address = bluetoothDevice.getAddress();
                String name = bluetoothDevice.getName();
                if (address == null || name == null) {
                    return;
                }
                if (name.endsWith("\u0000")) {
                    name = name.substring(0, name.length() - 1);
                }
                Device device2 = null;
                int size = this.mPresentDevices.size();
                while (true) {
                    if (i >= size) {
                        break;
                    }
                    Device device3 = this.mPresentDevices.get(i);
                    if (address.equals(device3.address)) {
                        device2 = device3;
                        break;
                    }
                    i++;
                }
                if (device2 == null) {
                    Device device4 = new Device();
                    device4.btDevice = bluetoothDevice;
                    device4.address = address;
                    device4.consecutiveMisses = -1;
                    device4.setNameString(name);
                    this.mPresentDevices.add(device4);
                    sendDeviceAdded(device4);
                    return;
                }
                device2.consecutiveMisses = -1;
                String str = device2.btName;
                if (str != name) {
                    if (str == null || !str.equals(name)) {
                        device2.setNameString(name);
                        sendDeviceChanged(device2);
                        return;
                    }
                    return;
                }
                return;
            }
            if ("android.bluetooth.adapter.action.DISCOVERY_FINISHED".equals(action)) {
                for (int size2 = this.mPresentDevices.size() - 1; size2 >= 0; size2--) {
                    try {
                        Device device5 = this.mPresentDevices.get(size2);
                        int i2 = device5.consecutiveMisses;
                        if (i2 < 0) {
                            device5.consecutiveMisses = 0;
                        } else if (i2 >= 4) {
                            this.mPresentDevices.remove(size2);
                            sendDeviceRemoved(device5);
                        } else {
                            device5.consecutiveMisses = i2 + 1;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                sendScanningStopped();
                if (mKeepScanning) {
                    this.mHandler.postDelayed(this.mScanTask, 4000L);
                }
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void sendScanningStarted() {
            synchronized (this.mListenerLock) {
                int size = this.mClients.size();
                for (int i = 0; i < size; i++) {
                    this.mClients.get(i).listener.onScanningStarted();
                }
            }
        }

        private void sendScanningStopped() {
            synchronized (this.mListenerLock) {
                for (int size = this.mClients.size() - 1; size >= 0; size--) {
                    ClientRecord clientRecord = this.mClients.get(size);
                    clientRecord.listener.onScanningStopped(clientRecord.devices);
                }
            }
        }

        private void sendDeviceAdded(Device device2) {
            synchronized (this.mListenerLock) {
                for (int size = this.mClients.size() - 1; size > -1; size--) {
                    ClientRecord clientRecord = this.mClients.get(size);
                    Iterator<BluetoothDeviceCriteria> it = clientRecord.matchers.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        if (it.next().isMatchingDevice(device2.btDevice)) {
                            clientRecord.devices.add(device2);
                            clientRecord.listener.onDeviceAdded(device2);
                            break;
                        }
                    }
                }
            }
        }

        private void sendDeviceChanged(Device device2) {
            synchronized (this.mListenerLock) {
                int size = this.mClients.size();
                for (int i = 0; i < size; i++) {
                    ClientRecord clientRecord = this.mClients.get(i);
                    int size2 = clientRecord.devices.size() - 1;
                    while (true) {
                        if (size2 <= -1) {
                            break;
                        }
                        if (clientRecord.devices.get(size2).btDevice.getAddress().equals(device2.btDevice.getAddress())) {
                            clientRecord.listener.onDeviceChanged(device2);
                            break;
                        }
                        size2--;
                    }
                }
            }
        }

        private void sendDeviceRemoved(Device device2) {
            synchronized (this.mListenerLock) {
                for (int size = this.mClients.size() - 1; size > -1; size--) {
                    ClientRecord clientRecord = this.mClients.get(size);
                    int size2 = clientRecord.devices.size() - 1;
                    while (true) {
                        if (size2 <= -1) {
                            break;
                        }
                        if (clientRecord.devices.get(size2).btDevice.getAddress().equals(device2.btDevice.getAddress())) {
                            clientRecord.devices.remove(size2);
                            clientRecord.listener.onDeviceRemoved(device2);
                            break;
                        }
                        size2--;
                    }
                }
            }
        }
    }
}
