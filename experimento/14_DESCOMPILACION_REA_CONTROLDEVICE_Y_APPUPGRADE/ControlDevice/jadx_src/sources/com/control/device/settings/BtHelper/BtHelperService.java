package com.control.device.settings.BtHelper;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import com.control.device.settings.MexicAmco.AmxZTERemoteActivity;
import com.control.device.settings.remote.ZTERemoteActivity;
import com.control.device.util.ProductUtils;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

@SuppressLint({"NewApi"})
/* loaded from: classes.dex */
public class BtHelperService extends Service {
    private static boolean DEBUG = true;
    private static final int MINOR_DEVICE_CLASS_POINTING = Integer.parseInt("0000010000000", 2);
    private static final int MINOR_DEVICE_CLASS_JOYSTICK = Integer.parseInt("0000000000100", 2);
    private static final int MINOR_DEVICE_CLASS_GAMEPAD = Integer.parseInt("0000000001000", 2);
    private static final int MINOR_DEVICE_CLASS_KEYBOARD = Integer.parseInt("0000001000000", 2);
    private static final int MINOR_DEVICE_CLASS_REMOTE = Integer.parseInt("0000000001100", 2);
    private String MarketCode_OperatorCode = SystemProperties.get("config.MarketCode") + " " + SystemProperties.get("config.OperatorCode");
    private String CpuName = SystemProperties.get("ro.product.cpu.name");
    private String defaultActivityName = ZTERemoteActivity.class.getName();
    private Context mContext = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BtStateBroadcastReceiver mBtReceiver = null;
    private final MessageHandler mMsgHandler = new MessageHandler();
    private boolean mBtBonded = false;
    private boolean mBtConnect = false;
    private boolean mRunningActivity = false;
    private boolean mBtInputDevice = false;
    private boolean mStarted = false;

    public boolean isMatchingMajorDeviceClass(int i) {
        return i == 1280;
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void LOG(String str) {
        if (DEBUG) {
            Log.i("BtHelperService", str);
        }
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        LOG("onCreate");
        if ("4301 6".equals(this.MarketCode_OperatorCode)) {
            this.defaultActivityName = AmxZTERemoteActivity.class.getName();
        }
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        LOG("onStartCommand, mStarted=" + this.mStarted + ", intent=" + intent);
        if (!this.mStarted) {
            this.mStarted = true;
            if (!initBt(this)) {
                LOG("init bt fail");
            }
            LOG("init bt ok");
            this.mContext = getApplicationContext();
            this.mBtReceiver = new BtStateBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter("android.bluetooth.device.action.FOUND");
            intentFilter.addAction("android.bluetooth.adapter.action.DISCOVERY_FINISHED");
            intentFilter.addAction("android.bluetooth.device.action.BOND_STATE_CHANGED");
            intentFilter.addAction("android.bluetooth.device.action.ACL_CONNECTED");
            intentFilter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
            this.mContext.registerReceiver(this.mBtReceiver, intentFilter);
            LOG("send BT_STATUS_CHECK");
            this.mMsgHandler.sendEmptyMessageDelayed(0, 5000L);
        }
        return 1;
    }

    @Override // android.app.Service
    public void onDestroy() {
        BtStateBroadcastReceiver btStateBroadcastReceiver;
        LOG("onDestroy");
        Context context = this.mContext;
        if (context != null && (btStateBroadcastReceiver = this.mBtReceiver) != null && this.mStarted) {
            context.unregisterReceiver(btStateBroadcastReceiver);
        }
        this.mMsgHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private class MessageHandler extends Handler {
        private MessageHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            BtHelperService.this.LOG("msg.what = " + message.what);
            int i = message.what;
            if (i == 0) {
                int i2 = Settings.Secure.getInt(BtHelperService.this.getApplicationContext().getContentResolver(), "user_setup_complete", 0);
                if (i2 == 1) {
                    BtHelperService.this.isDeviceBonded();
                    BtHelperService btHelperService = BtHelperService.this;
                    btHelperService.isRunningActivity(btHelperService.defaultActivityName);
                    BtHelperService.this.LOG("mRunningActivity=" + BtHelperService.this.mRunningActivity + ", mBtConnect=" + BtHelperService.this.mBtConnect + ", mBtBonded=" + BtHelperService.this.mBtBonded);
                    if (BtHelperService.this.mRunningActivity) {
                        BtHelperService.this.LOG("defaultActivityName is running no need to check");
                        return;
                    } else {
                        if (BtHelperService.this.mBtConnect || BtHelperService.this.mBtBonded) {
                            return;
                        }
                        BtHelperService.this.mMsgHandler.sendEmptyMessage(1);
                        return;
                    }
                }
                BtHelperService.this.LOG("isSetup=" + i2 + ", no need to check");
                return;
            }
            if (i == 1) {
                BtHelperService.this.LOG("Call BT_STATUS_SHOW" + BtHelperService.this.defaultActivityName);
                if ("4301 6".equals(BtHelperService.this.MarketCode_OperatorCode)) {
                    BtHelperService.this.LOG("Call BT_STATUS_SHOW, CPU_905X2");
                    if (Settings.Global.getInt(BtHelperService.this.getApplicationContext().getContentResolver(), "claro_tv_rcu_type", -1) == 0) {
                        BtHelperService.this.LOG("BT_STATUS_SHOW, claro rcu is IR, no show");
                        return;
                    }
                    Intent intent = new Intent(BtHelperService.this.mContext, (Class<?>) AmxZTERemoteActivity.class);
                    intent.setFlags(268435456);
                    try {
                        BtHelperService.this.mContext.startActivity(intent);
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
                if (ProductUtils.isB866V5W11()) {
                    Intent intent2 = new Intent(BtHelperService.this.mContext, (Class<?>) ZTERemoteActivity.class);
                    intent2.setFlags(268435456);
                    try {
                        BtHelperService.this.mContext.startActivity(intent2);
                        return;
                    } catch (Exception e2) {
                        Log.e("BtHelperService", " e = ", e2);
                        return;
                    }
                }
                return;
            }
            if (i == 2) {
                BtHelperService.this.isDeviceConnected();
                BtHelperService btHelperService2 = BtHelperService.this;
                btHelperService2.isRunningActivity(btHelperService2.defaultActivityName);
                BtHelperService.this.LOG("mRunningActivity=" + BtHelperService.this.mRunningActivity + ", mBtConnect=" + BtHelperService.this.mBtConnect);
                if (BtHelperService.this.mRunningActivity || BtHelperService.this.mBtConnect) {
                    BtHelperService.this.LOG("defaultActivityName is running or connected, no need to check");
                    return;
                } else {
                    BtHelperService.this.mMsgHandler.sendEmptyMessage(1);
                    return;
                }
            }
            if (i == 3) {
                BtHelperService.this.isDeviceBonded();
                BtHelperService.this.LOG("mBtBonded=" + BtHelperService.this.mBtBonded);
                if (BtHelperService.this.mBtBonded) {
                    return;
                }
                BtHelperService.this.mMsgHandler.removeMessages(2);
                BtHelperService.this.mMsgHandler.removeMessages(1);
                return;
            }
            if (i == 4) {
                BtHelperService.this.isDeviceBonded();
                BtHelperService.this.isDeviceConnected();
                BtHelperService.this.LOG("mBtConnect=" + BtHelperService.this.mBtConnect + ", mBtBonded=" + BtHelperService.this.mBtBonded);
                return;
            }
            if (i == 5) {
                BtHelperService.this.LOG("Call BT_STATUS_SHOW_BY_UNPAIR" + BtHelperService.this.defaultActivityName);
                if ("4301 6".equals(BtHelperService.this.MarketCode_OperatorCode)) {
                    BtHelperService.this.isDeviceBonded();
                    BtHelperService.this.isDeviceConnected();
                    BtHelperService.this.LOG("mBtConnect=" + BtHelperService.this.mBtConnect + ", mBtBonded=" + BtHelperService.this.mBtBonded);
                    BtHelperService.this.LOG("Call BT_STATUS_SHOW_BY_UNPAIR, CPU_905X2");
                    if (Settings.Global.getInt(BtHelperService.this.getApplicationContext().getContentResolver(), "claro_tv_rcu_type", -1) == 0) {
                        BtHelperService.this.LOG("BT_STATUS_SHOW_BY_UNPAIR, claro rcu is IR, no show");
                        return;
                    }
                    if (BtHelperService.this.mBtBonded) {
                        return;
                    }
                    Intent intent3 = new Intent(BtHelperService.this.mContext, (Class<?>) AmxZTERemoteActivity.class);
                    intent3.setFlags(268435456);
                    intent3.putExtra("data", "unpair");
                    try {
                        BtHelperService.this.mContext.startActivity(intent3);
                        return;
                    } catch (Exception e3) {
                        e3.printStackTrace();
                        return;
                    }
                }
                if (ProductUtils.isB866V5W11()) {
                    BtHelperService.this.isDeviceBonded();
                    BtHelperService.this.isDeviceConnected();
                    if (BtHelperService.this.mBtInputDevice) {
                        Intent intent4 = new Intent(BtHelperService.this.mContext, (Class<?>) ZTERemoteActivity.class);
                        intent4.putExtra("add_a_new_remote", true);
                        intent4.setFlags(268435456);
                        try {
                            BtHelperService.this.mContext.startActivity(intent4);
                            return;
                        } catch (Exception e4) {
                            Log.e("BtHelperService", " e = ", e4);
                            return;
                        }
                    }
                    return;
                }
                return;
            }
            super.handleMessage(message);
        }
    }

    public boolean isMatchingDeviceClass(int i) {
        return (((((MINOR_DEVICE_CLASS_POINTING | MINOR_DEVICE_CLASS_JOYSTICK) | MINOR_DEVICE_CLASS_GAMEPAD) | MINOR_DEVICE_CLASS_KEYBOARD) | MINOR_DEVICE_CLASS_REMOTE) & i) != 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isInputDevice(BluetoothClass bluetoothClass) {
        LOG("bluetoothClass.getMajorDeviceClass() = " + bluetoothClass.getMajorDeviceClass());
        return isMatchingMajorDeviceClass(bluetoothClass.getMajorDeviceClass()) && isMatchingDeviceClass(bluetoothClass.getDeviceClass());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void isRunningActivity(String str) {
        List<ActivityManager.RunningTaskInfo> runningTasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
        this.mRunningActivity = false;
        if (runningTasks.size() != 0) {
            ComponentName componentName = runningTasks.get(0).topActivity;
            LOG("componentInfo=" + componentName);
            if (componentName == null || !componentName.toString().contains(str)) {
                return;
            }
            this.mRunningActivity = true;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void isDeviceBonded() {
        Set<BluetoothDevice> bondedDevices = this.mBluetoothAdapter.getBondedDevices();
        this.mBtBonded = false;
        if (bondedDevices == null) {
            LOG("No bonded device");
            return;
        }
        for (BluetoothDevice bluetoothDevice : bondedDevices) {
            LOG("bonded device: name=" + bluetoothDevice.getName() + " Address=" + bluetoothDevice.getAddress() + " class=" + bluetoothDevice.getBluetoothClass());
            if (isInputDevice(bluetoothDevice.getBluetoothClass())) {
                this.mBtBonded = true;
            }
            if (bluetoothDevice.getBluetoothClass().getMajorDeviceClass() == 1024) {
                sendAudioVideoConnectedBroadcast();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void isDeviceConnected() {
        this.mBtConnect = false;
        try {
            Method declaredMethod = BluetoothAdapter.class.getDeclaredMethod("getConnectionState", null);
            declaredMethod.setAccessible(true);
            int intValue = ((Integer) declaredMethod.invoke(this.mBluetoothAdapter, null)).intValue();
            LOG("BluetoothAdapter.STATE_CONNECTED = 2, state = " + intValue);
            if (intValue == 2) {
                LOG("BluetoothAdapter.STATE_CONNECTED");
                for (BluetoothDevice bluetoothDevice : this.mBluetoothAdapter.getBondedDevices()) {
                    Method declaredMethod2 = BluetoothDevice.class.getDeclaredMethod("isConnected", null);
                    declaredMethod.setAccessible(true);
                    boolean booleanValue = ((Boolean) declaredMethod2.invoke(bluetoothDevice, null)).booleanValue();
                    LOG("connect(isConnected =" + booleanValue + ") device: name=" + bluetoothDevice.getName() + " Address=" + bluetoothDevice.getAddress() + " class=" + bluetoothDevice.getBluetoothClass());
                    if (booleanValue && isInputDevice(bluetoothDevice.getBluetoothClass())) {
                        LOG("isConnected == true");
                        sendBroadcast(new Intent("action.activity.finish"));
                        this.mBtConnect = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class BtStateBroadcastReceiver extends BroadcastReceiver {
        private BtStateBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            if ("android.bluetooth.device.action.FOUND".equals(action) || "android.bluetooth.adapter.action.DISCOVERY_FINISHED".equals(action)) {
                return;
            }
            if ("android.bluetooth.device.action.BOND_STATE_CHANGED".equals(action)) {
                BtHelperService.this.LOG("ACTION_BOND_STATE_CHANGED come ......");
                int intExtra = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", 10);
                int intExtra2 = intent.getIntExtra("android.bluetooth.device.extra.PREVIOUS_BOND_STATE", 10);
                Log.i("BtHelperService", "BOND STATE CHANGED [" + bluetoothDevice.getName() + "] - was " + intExtra2 + ", now is " + intExtra);
                if (intExtra2 == 12 && intExtra == 10) {
                    BtHelperService btHelperService = BtHelperService.this;
                    btHelperService.mBtInputDevice = btHelperService.isInputDevice(bluetoothDevice.getBluetoothClass());
                    BtHelperService.this.mMsgHandler.removeMessages(5);
                    BtHelperService.this.mMsgHandler.sendEmptyMessageDelayed(5, 500L);
                    return;
                }
                return;
            }
            if ("android.bluetooth.device.action.ACL_CONNECTED".equals(action)) {
                BtHelperService.this.LOG("ACTION_ACL_CONNECTED come ......");
                BtHelperService.this.mMsgHandler.sendEmptyMessage(3);
            } else if ("android.bluetooth.device.action.ACL_DISCONNECTED".equals(action)) {
                BtHelperService.this.LOG("ACTION_ACL_DISCONNECTED come ......");
                BtHelperService.this.mMsgHandler.sendEmptyMessage(4);
            }
        }
    }

    private boolean initBt(Context context) {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mBluetoothAdapter = defaultAdapter;
        if (defaultAdapter == null) {
            LOG("No bluetooth device!");
            return false;
        }
        LOG("get Bluetooth device");
        if (this.mBluetoothAdapter.isEnabled()) {
            return true;
        }
        LOG("Bluetooth device enable");
        this.mBluetoothAdapter.enable();
        try {
            Thread.sleep(2000L);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    private void sendAudioVideoConnectedBroadcast() {
        LOG("sendAudioVideoConnectedBroadcast");
        Intent intent = new Intent("android.intent.ACTION_ADD_AUDIO_VIDEO");
        intent.putExtra("source", 0);
        intent.setPackage("com.android.tv.settings");
        sendBroadcast(intent);
    }
}
