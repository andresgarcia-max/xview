package com.control.device.settings.remote;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.VideoView;
import com.android.tv.settings.accessories.BluetoothDevicePairer;
import com.control.device.R;
import com.control.device.settings.remote.util.MojaBtAnim;
import com.control.device.settings.remote.util.VideoAnimController;
import com.control.device.settings.remote.util.ZTERemoteAnimationControl;
import com.control.device.util.DebugUtil;
import com.control.device.util.MarketUtil;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/* loaded from: classes.dex */
public class ZTERemoteVideoActivity extends Activity implements BluetoothDevicePairer.EventListener {
    private static VideoAnimController.Controller controller;
    private static boolean mPairingSuccess;
    private String containName;
    private BluetoothDevicePairer mBluetoothPairer;
    private BluetoothAdapter mBtAdapter;
    private Handler mMsgHandler;
    private static final int MINOR_DEVICE_CLASS_POINTING = Integer.parseInt("0000010000000", 2);
    private static final int MINOR_DEVICE_CLASS_JOYSTICK = Integer.parseInt("0000000000100", 2);
    private static final int MINOR_DEVICE_CLASS_GAMEPAD = Integer.parseInt("0000000001000", 2);
    private static final int MINOR_DEVICE_CLASS_KEYBOARD = Integer.parseInt("0000001000000", 2);
    private static final int MINOR_DEVICE_CLASS_REMOTE = Integer.parseInt("0000000001100", 2);
    Timer timer = new Timer();
    private int mPreviousStatus = 0;
    private boolean isFailing = false;
    final Handler flashOnTimeHandler = new Handler() { // from class: com.control.device.settings.remote.ZTERemoteVideoActivity.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what == 1) {
                Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<handleMessage> 1");
                Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<handleMessage> mBluetoothPairer != null = " + (ZTERemoteVideoActivity.this.mBluetoothPairer != null));
                if (ZTERemoteVideoActivity.this.mBluetoothPairer != null && ZTERemoteVideoActivity.this.mBluetoothPairer.getTargetDevice() == null) {
                    Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<handleMessage> 1, ReBluetoothPairer");
                    ZTERemoteVideoActivity.this.stopBluetoothPairer();
                    ZTERemoteVideoActivity.this.startBluetoothPairer();
                }
            }
            super.handleMessage(message);
        }
    };
    TimerTask task = new TimerTask() { // from class: com.control.device.settings.remote.ZTERemoteVideoActivity.2
        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<run> TimerTask");
            Message message = new Message();
            message.what = 1;
            ZTERemoteVideoActivity.this.flashOnTimeHandler.sendMessage(message);
        }
    };

    private boolean isMatchingMajorDeviceClass(int i) {
        return i == 1280;
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        ZTERemoteAnimationControl zTERemoteAnimationControl = new ZTERemoteAnimationControl();
        zTERemoteAnimationControl.init();
        this.containName = zTERemoteAnimationControl.containName;
        Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<onCreate> containName = " + this.containName);
        setContentView(zTERemoteAnimationControl.layout);
        this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        new Handler();
        controller = getCustomizationController();
        safeChangeState("bfScan");
        this.mMsgHandler = new Handler() { // from class: com.control.device.settings.remote.ZTERemoteVideoActivity.3
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                int i = message.what;
                if (i == 1) {
                    Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<handleMessage> MSG_UPDATA_VIEW");
                    ZTERemoteVideoActivity.this.UpdataView();
                } else {
                    if (i == 2) {
                        Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<handleMessage> MSG_FINISH");
                        ZTERemoteVideoActivity.this.FinishActivity();
                        return;
                    }
                    Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<handleMessage> No handler case available for message:" + message.what);
                    ZTERemoteVideoActivity.this.UpdataView();
                }
            }
        };
        this.timer.schedule(this.task, 5000L, 10000L);
    }

    @Override // android.app.Activity
    public void onStart() {
        super.onStart();
        Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<onStart> ");
        BluetoothDevicePairer bluetoothDevicePairer = new BluetoothDevicePairer(this, this);
        this.mBluetoothPairer = bluetoothDevicePairer;
        bluetoothDevicePairer.start();
        UpdataView();
    }

    @Override // android.app.Activity
    public void onStop() {
        Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<onStop> ");
        super.onStop();
        stopBluetoothPairer();
        this.flashOnTimeHandler.removeCallbacksAndMessages(null);
        this.mMsgHandler.removeCallbacksAndMessages(null);
    }

    @Override // android.app.Activity
    public void onDestroy() {
        Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<onDestroy> ");
        super.onDestroy();
        if (this.timer != null) {
            Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<onDestroy> timer.cancel");
            this.timer.cancel();
            this.timer = null;
        }
        stopBluetoothPairer();
        this.flashOnTimeHandler.removeCallbacksAndMessages(null);
        this.mMsgHandler.removeCallbacksAndMessages(null);
        if (controller != null) {
            controller = null;
        }
    }

    @Override // com.android.tv.settings.accessories.BluetoothDevicePairer.EventListener
    public void statusChanged() {
        Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<statusChanged> ");
        BluetoothDevicePairer bluetoothDevicePairer = this.mBluetoothPairer;
        if (bluetoothDevicePairer == null) {
            return;
        }
        int status = bluetoothDevicePairer.getStatus();
        int i = this.mPreviousStatus;
        this.mPreviousStatus = status;
        String name = this.mBluetoothPairer.getTargetDevice() == null ? "NONE" : this.mBluetoothPairer.getTargetDevice().getName();
        Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<statusChanged> RemoteName = " + name + ", oldStatus=" + i + ", status=" + status);
        if (status == -1) {
            Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<statusChanged> STATUS_ERROR");
            safeChangeState("error");
            mPairingSuccess = false;
            this.isFailing = true;
            String str = MarketUtil.MARKET_OPERATOR;
            if (str.hashCode() == 1534522958) {
                str.equals("4001 1");
            }
            Log.d("[TvSettings]", "<statusChanged> STATUS_ERROR, Default");
            Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<run> STATUS_ERROR, postDelayed 5s, recreate Activity");
            new Handler().postDelayed(new Runnable() { // from class: com.control.device.settings.remote.ZTERemoteVideoActivity.6
                @Override // java.lang.Runnable
                public void run() {
                    Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<run> STATUS_ERROR, recreate Activity now");
                    Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<run> isFailing = " + ZTERemoteVideoActivity.this.isFailing);
                    if (ZTERemoteVideoActivity.this.isFailing) {
                        ZTERemoteVideoActivity.this.stopBluetoothPairer();
                        ZTERemoteVideoActivity.this.recreate();
                    }
                }
            }, 5000L);
            return;
        }
        if (status == 0) {
            Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<statusChanged> STATUS_NONE");
            if (i == 2) {
                Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<statusChanged> STATUS_NONE, oldStatus=STATUS_WAITING_TO_PAIR");
                return;
            } else {
                if (i == 4) {
                    Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<statusChanged> STATUS_NONE, oldStatus=STATUS_CONNECTING");
                    return;
                }
                return;
            }
        }
        if (status == 1) {
            Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<statusChanged> STATUS_SCANNING");
            mPairingSuccess = false;
            if (this.mBtAdapter != null) {
                Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<statusChanged> STATUS_SCANNING, mBtAdapter != null");
                Set<BluetoothDevice> bondedDevices = this.mBtAdapter.getBondedDevices();
                if (bondedDevices != null) {
                    Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<statusChanged> STATUS_SCANNING, bondedDevices != null");
                    Iterator<BluetoothDevice> it = bondedDevices.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        BluetoothDevice next = it.next();
                        Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<statusChanged> STATUS_SCANNING, deviceName=" + next.getName() + ", RemoteName=" + name + ", bfmPairingSuccess=" + mPairingSuccess);
                        if (((next.getName().contains(this.containName) || next.getName().contains("ZTE") || isMojaRemote(next.getName())) && next.isConnected()) && isInputDevice(next.getBluetoothClass())) {
                            Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<statusChanged> STATUS_SCANNING, device:" + next.getAddress() + " is connected");
                            mPairingSuccess = true;
                            break;
                        }
                    }
                }
            }
            if (!mPairingSuccess) {
                Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<statusChanged> STATUS_SCANNING, Pairing unSuccess or just begin");
                safeChangeState("reScan");
                return;
            }
            Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<statusChanged> STATUS_SCANNING, Pairing Success");
            safeChangeState("done");
            if (MarketUtil.isBOHER_OTT_12()) {
                Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<statusChanged> STATUS_SCANNING, postDelayed 5s, finish Activity");
                new Handler().postDelayed(new Runnable() { // from class: com.control.device.settings.remote.ZTERemoteVideoActivity.4
                    @Override // java.lang.Runnable
                    public void run() {
                        Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<statusChanged> STATUS_SCANNING, finish Activity now");
                        ZTERemoteVideoActivity.this.FinishActivity();
                    }
                }, 7000L);
                return;
            }
            return;
        }
        if (status != 2) {
            if (status == 3) {
                Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<statusChanged> STATUS_PAIRING");
                safeChangeState("onBond");
                MarketUtil.MARKET_OPERATOR.hashCode();
                mPairingSuccess = true;
                return;
            }
            if (status != 4) {
                return;
            }
            Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<statusChanged> STATUS_CONNECTING");
            mPairingSuccess = true;
            safeChangeState("done");
            String str2 = MarketUtil.MARKET_OPERATOR;
            str2.hashCode();
            if (!str2.equals("4001 1")) {
                Log.d("[TvSettings]", "<statusChanged> STATUS_CONNECTING, Default");
            }
            this.mMsgHandler.sendEmptyMessageDelayed(2, 3000L);
            Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<statusChanged> STATUS_CONNECTING, postDelayed 5s, finish Activity");
            new Handler().postDelayed(new Runnable() { // from class: com.control.device.settings.remote.ZTERemoteVideoActivity.5
                @Override // java.lang.Runnable
                public void run() {
                    Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<statusChanged> STATUS_CONNECTING, finish Activity now");
                    ZTERemoteVideoActivity.this.FinishActivity();
                }
            }, 5000L);
            return;
        }
        Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<statusChanged> STATUS_WAITING_TO_PAIR, mPairingSuccess = " + mPairingSuccess);
        safeChangeState("onScan");
        if (mPairingSuccess) {
            Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<statusChanged> STATUS_WAITING_TO_PAIR, break");
            return;
        }
        if (!name.contains(this.containName) && !name.contains("ZTE") && !isMojaRemote(name)) {
            Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<statusChanged> STATUS_WAITING_TO_PAIR, not contains " + this.containName + ", now break!");
            BluetoothDevicePairer bluetoothDevicePairer2 = this.mBluetoothPairer;
            if (bluetoothDevicePairer2 != null) {
                bluetoothDevicePairer2.cancelPairing();
            }
            mPairingSuccess = false;
            return;
        }
        Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<statusChanged> STATUS_WAITING_TO_PAIR, go on!");
    }

    private boolean isMojaRemote(String str) {
        return MarketUtil.isBOHER_OTT_12() && str != null && str.contains("Moja");
    }

    private void safeChangeState(String str) {
        VideoAnimController.Controller controller2 = controller;
        if (controller2 != null) {
            controller2.changeState(str);
        } else {
            DebugUtil.dTAGForClassName("[warning] controller is null");
        }
    }

    private VideoAnimController.Controller getCustomizationController() {
        String str = MarketUtil.MARKET_OPERATOR;
        str.hashCode();
        if (str.equals("4001 1")) {
            return new MojaBtAnim.Controller(this, (VideoView) findViewById(R.id.video_view));
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopBluetoothPairer() {
        Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<stopBluetoothPairer> ");
        this.mPreviousStatus = 0;
        BluetoothDevicePairer bluetoothDevicePairer = this.mBluetoothPairer;
        if (bluetoothDevicePairer != null) {
            bluetoothDevicePairer.setListener(null);
            this.mBluetoothPairer.dispose();
            this.mBluetoothPairer = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startBluetoothPairer() {
        Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<startBluetoothPairer> ");
        stopBluetoothPairer();
        BluetoothDevicePairer bluetoothDevicePairer = new BluetoothDevicePairer(this, this);
        this.mBluetoothPairer = bluetoothDevicePairer;
        bluetoothDevicePairer.start();
        statusChanged();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void UpdataView() {
        Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<UpdataView> ");
        statusChanged();
    }

    private void RemoveActivity() {
        setResult(1, null);
        Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<RemoveActivity> RemoveActivity SUCCESS");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void FinishActivity() {
        Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]<FinishActivity> ");
        RemoveActivity();
        finish();
    }

    private boolean isMatchingDeviceClass(int i) {
        return (((((MINOR_DEVICE_CLASS_POINTING | MINOR_DEVICE_CLASS_JOYSTICK) | MINOR_DEVICE_CLASS_GAMEPAD) | MINOR_DEVICE_CLASS_KEYBOARD) | MINOR_DEVICE_CLASS_REMOTE) & i) != 0;
    }

    private boolean isInputDevice(BluetoothClass bluetoothClass) {
        Log.d("[TvSettings]", "[ZTERemoteActivity(Video)]MajorDeviceClass=" + bluetoothClass.getMajorDeviceClass() + ", DeviceClass=" + bluetoothClass.getDeviceClass());
        return isMatchingMajorDeviceClass(bluetoothClass.getMajorDeviceClass()) && isMatchingDeviceClass(bluetoothClass.getDeviceClass());
    }
}
