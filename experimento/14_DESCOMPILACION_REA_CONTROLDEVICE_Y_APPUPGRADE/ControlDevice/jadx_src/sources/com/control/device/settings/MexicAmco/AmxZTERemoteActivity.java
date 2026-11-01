package com.control.device.settings.MexicAmco;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.tv.settings.accessories.BluetoothDevicePairer;
import com.control.device.R;
import com.control.device.settings.MexicAmco.AMXUpdateAtvKey.ComUtil;
import java.util.Iterator;
import java.util.Set;

/* loaded from: classes.dex */
public class AmxZTERemoteActivity extends Activity implements BluetoothDevicePairer.EventListener {
    private RelativeLayout amx_rela_bt_pair;
    private RelativeLayout amx_rela_bt_pair_fail;
    private RelativeLayout amx_rela_bt_pair_warning;
    private RelativeLayout amx_rela_bt_pairing;
    private BluetoothDevicePairer mBluetoothPairer;
    private BluetoothAdapter mBtAdapter;
    private Handler mMsgHandler;
    private String mTargetDeviceAddress;
    private Button mbutton;
    private ImageView mpair_success;
    private ImageView mpairing;
    private ProgressBar mprogressBar;
    private TextView mtextView;
    private TextView mtextView_1;
    private static final int MINOR_DEVICE_CLASS_POINTING = Integer.parseInt("0000010000000", 2);
    private static final int MINOR_DEVICE_CLASS_JOYSTICK = Integer.parseInt("0000000000100", 2);
    private static final int MINOR_DEVICE_CLASS_GAMEPAD = Integer.parseInt("0000000001000", 2);
    private static final int MINOR_DEVICE_CLASS_KEYBOARD = Integer.parseInt("0000001000000", 2);
    private static final int MINOR_DEVICE_CLASS_REMOTE = Integer.parseInt("0000000001100", 2);
    private static AnimationDrawable animationDrawable = null;
    private boolean mPairingSuccess = false;
    private String mMarket = SystemProperties.get("config.MarketCode");
    private String mOperator = SystemProperties.get("config.OperatorCode");
    private String CpuName = SystemProperties.get("ro.product.cpu.name");
    private boolean isFailing = false;
    private int isSetup = 0;
    private int mFailCount = 0;
    private int mPreviousStatus = 0;
    final Handler flashOnTimeHandler = new Handler() { // from class: com.control.device.settings.MexicAmco.AmxZTERemoteActivity.3
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what == 1) {
                Log.d("[TvSettings]", "[ZTERemoteActivity]<handleMessage> 1");
                if (AmxZTERemoteActivity.this.mBluetoothPairer != null && AmxZTERemoteActivity.this.mBluetoothPairer.getTargetDevice() == null) {
                    Log.d("[TvSettings]", "[ZTERemoteActivity]<handleMessage> 1, ReBluetoothPairer");
                    AmxZTERemoteActivity.this.startBluetoothPairer();
                }
            }
            super.handleMessage(message);
        }
    };

    private boolean isMatchingMajorDeviceClass(int i) {
        return i == 1280;
    }

    static /* synthetic */ int access$608(AmxZTERemoteActivity amxZTERemoteActivity) {
        int i = amxZTERemoteActivity.mFailCount;
        amxZTERemoteActivity.mFailCount = i + 1;
        return i;
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Log.d("[TvSettings]", "[ZTERemoteActivity]<onCreate> ");
        if ("4301".equals(this.mMarket) && "6".equals(this.mOperator) && this.CpuName.contains("905X2")) {
            ComUtil.CheckKeyIsInstalled(getApplicationContext());
        }
        this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        setContentView(R.layout.amx_zte_remote_a);
        this.isSetup = Settings.Secure.getInt(getApplicationContext().getContentResolver(), "user_setup_complete", 0);
        if (Settings.Global.getInt(getApplicationContext().getContentResolver(), "claro_tv_rcu_type", -1) == -1) {
            setClaroTvRcuType(1);
        }
        this.amx_rela_bt_pair = (RelativeLayout) findViewById(R.id.amx_rela_bt_pair);
        this.amx_rela_bt_pairing = (RelativeLayout) findViewById(R.id.amx_rela_bt_pairing);
        this.amx_rela_bt_pair_fail = (RelativeLayout) findViewById(R.id.amx_rela_bt_pair_fail);
        this.amx_rela_bt_pair_warning = (RelativeLayout) findViewById(R.id.amx_rela_bt_pair_warning);
        this.mpairing = (ImageView) findViewById(R.id.amx_icon_bluetooth_pairing);
        this.mprogressBar = (ProgressBar) findViewById(R.id.amx_bt_progressbar);
        this.mpair_success = (ImageView) findViewById(R.id.amx_icon_bluetooth_success);
        this.mtextView = (TextView) findViewById(R.id.amx_tv_pairing);
        this.mtextView_1 = (TextView) findViewById(R.id.amx_j);
        this.mbutton = (Button) findViewById(R.id.amx_continuar);
        this.amx_rela_bt_pair.setVisibility(0);
        this.amx_rela_bt_pairing.setVisibility(8);
        this.amx_rela_bt_pair_fail.setVisibility(8);
        this.amx_rela_bt_pair_warning.setVisibility(8);
        this.mbutton.requestFocus();
        this.mbutton.setOnClickListener(new View.OnClickListener() { // from class: com.control.device.settings.MexicAmco.AmxZTERemoteActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (view.getId() == R.id.amx_continuar && AmxZTERemoteActivity.this.amx_rela_bt_pair_warning.getVisibility() == 0) {
                    AmxZTERemoteActivity.this.setClaroTvRcuType(1);
                    AmxZTERemoteActivity.this.mMsgHandler.removeCallbacksAndMessages(null);
                    AmxZTERemoteActivity.this.mMsgHandler.sendEmptyMessage(2);
                }
            }
        });
        Log.d("[TvSettings]", "[ZTERemoteActivity]<onCreate> mPairingSuccess = " + this.mPairingSuccess);
        this.mMsgHandler = new Handler() { // from class: com.control.device.settings.MexicAmco.AmxZTERemoteActivity.2
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                int i = message.what;
                if (i == 1) {
                    Log.d("[TvSettings]", "[ZTERemoteActivity]<handleMessage> MSG_UPDATA_VIEW");
                    AmxZTERemoteActivity.this.UpdataView();
                    return;
                }
                if (i == 2) {
                    Log.d("[TvSettings]", "[ZTERemoteActivity]<handleMessage> MSG_FINISH");
                    AmxZTERemoteActivity.this.FinishActivity();
                    return;
                }
                if (i != 3) {
                    if (i == 4) {
                        Log.d("[TvSettings]", "[ZTERemoteActivity]<handleMessage> MSG_SET_SCANNING_VIEW");
                        AmxZTERemoteActivity.this.setScanningView();
                        return;
                    } else if (i == 5) {
                        Log.d("[TvSettings]", "[ZTERemoteActivity]<handleMessage> MSG_SET_CONNECTED_VIEW");
                        AmxZTERemoteActivity.this.setConnectedView();
                        return;
                    } else {
                        Log.d("[TvSettings]", "[ZTERemoteActivity]<handleMessage> No handler case available for message:" + message.what);
                        return;
                    }
                }
                Log.d("[TvSettings]", "[ZTERemoteActivity]<handleMessage> MSG_CONNECTING_TIMEOUT");
                if (AmxZTERemoteActivity.this.mBluetoothPairer != null && AmxZTERemoteActivity.this.mBluetoothPairer.getTargetDevice() != null) {
                    BluetoothDevice targetDevice = AmxZTERemoteActivity.this.mBluetoothPairer.getTargetDevice();
                    Log.d("[TvSettings]", "[ZTERemoteActivity]<handleMessage> MSG_CONNECTING_TIMEOUT, unpair the device, name=" + targetDevice.getName() + ", address=" + targetDevice.getAddress());
                    AmxZTERemoteActivity.this.mBluetoothPairer.unpairDevice(targetDevice);
                }
                AmxZTERemoteActivity.access$608(AmxZTERemoteActivity.this);
                AmxZTERemoteActivity.this.setScanningView();
            }
        };
    }

    @Override // android.app.Activity
    protected void onNewIntent(Intent intent) {
        Log.d("[TvSettings]", "[ZTERemoteActivity]<onNewIntent> intent=" + intent + ", mPairingSuccess=" + this.mPairingSuccess);
        super.onNewIntent(intent);
        if (!this.mPairingSuccess || intent == null) {
            return;
        }
        String stringExtra = intent.getStringExtra("data");
        Log.d("[TvSettings]", "[ZTERemoteActivity]<onNewIntent> data=" + stringExtra);
        if ("unpair".equals(stringExtra)) {
            Log.d("[TvSettings]", "[ZTERemoteActivity]<onNewIntent> rescanning");
            this.mFailCount = 0;
            this.mPairingSuccess = false;
            this.mMsgHandler.removeCallbacksAndMessages(null);
            setScanningView();
        }
    }

    @Override // android.app.Activity
    public void onStart() {
        super.onStart();
        Log.d("[TvSettings]", "[ZTERemoteActivity]<onStart> ");
        setScanningView();
    }

    @Override // android.app.Activity
    protected void onResume() {
        Log.d("[TvSettings]", "[ZTERemoteActivity]<onResume> ");
        if ("4301".equals(this.mMarket) && "6".equals(this.mOperator) && this.CpuName.contains("905X2")) {
            ComUtil.CheckKeyIsInstalled(getApplicationContext());
        }
        super.onResume();
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        Log.d("[TvSettings]", "[ZTERemoteActivity]<onBackPressed> ");
        if (this.isSetup != 1) {
            Log.d("[TvSettings]", "[ZTERemoteActivity]<onBackPressed> can not back, setup=" + this.isSetup);
            return;
        }
        stopBluetoothPairer();
        this.flashOnTimeHandler.removeCallbacksAndMessages(null);
        this.mMsgHandler.removeCallbacksAndMessages(null);
        RemoveActivity();
        super.onBackPressed();
    }

    @Override // android.app.Activity
    public void onStop() {
        Log.d("[TvSettings]", "[ZTERemoteActivity]<onStop> ");
        super.onStop();
        stopBluetoothPairer();
        this.flashOnTimeHandler.removeCallbacksAndMessages(null);
        this.mMsgHandler.removeCallbacksAndMessages(null);
    }

    @Override // android.app.Activity
    public void onDestroy() {
        Log.d("[TvSettings]", "[ZTERemoteActivity]<onDestroy> ");
        super.onDestroy();
        stopBluetoothPairer();
        this.flashOnTimeHandler.removeCallbacksAndMessages(null);
        this.mMsgHandler.removeCallbacksAndMessages(null);
    }

    @Override // com.android.tv.settings.accessories.BluetoothDevicePairer.EventListener
    public void statusChanged() {
        Log.d("[TvSettings]", "[ZTERemoteActivity]<statusChanged> ");
        BluetoothDevicePairer bluetoothDevicePairer = this.mBluetoothPairer;
        if (bluetoothDevicePairer == null) {
            return;
        }
        int status = bluetoothDevicePairer.getStatus();
        int i = this.mPreviousStatus;
        this.mPreviousStatus = status;
        String name = this.mBluetoothPairer.getTargetDevice() == null ? "NONE" : this.mBluetoothPairer.getTargetDevice().getName();
        Log.d("[TvSettings]", "[ZTERemoteActivity]<statusChanged> RemoteName = " + name + ", oldStatus=" + i + ", status=" + status);
        if (status == -1) {
            Log.d("[TvSettings]", "[ZTERemoteActivity]<statusChanged> STATUS_ERROR");
            this.mPairingSuccess = false;
            this.isFailing = true;
            this.mFailCount++;
            this.mMsgHandler.removeCallbacksAndMessages(null);
            this.mMsgHandler.sendEmptyMessage(4);
            return;
        }
        if (status == 0) {
            Log.d("[TvSettings]", "[ZTERemoteActivity]<statusChanged> STATUS_NONE");
            if (i == 2) {
                Log.d("[TvSettings]", "[ZTERemoteActivity]<statusChanged> STATUS_NONE, oldStatus=STATUS_WAITING_TO_PAIR");
                this.mPairingSuccess = false;
                this.mFailCount++;
                this.mMsgHandler.removeCallbacksAndMessages(null);
                this.mMsgHandler.sendEmptyMessage(4);
                return;
            }
            if (i == 4) {
                Log.d("[TvSettings]", "[ZTERemoteActivity]<statusChanged> STATUS_NONE, oldStatus=STATUS_CONNECTING");
                this.mPairingSuccess = true;
                setClaroTvRcuType(1);
                this.mMsgHandler.removeCallbacksAndMessages(null);
                this.mMsgHandler.sendEmptyMessage(5);
                return;
            }
            return;
        }
        if (status != 1) {
            if (status == 2) {
                this.mTargetDeviceAddress = this.mBluetoothPairer.getTargetDevice() != null ? this.mBluetoothPairer.getTargetDevice().getAddress() : null;
                Log.d("[TvSettings]", "[ZTERemoteActivity]<statusChanged> STATUS_WAITING_TO_PAIR, Default, target address=" + this.mTargetDeviceAddress);
                this.amx_rela_bt_pair.setVisibility(8);
                this.amx_rela_bt_pair_fail.setVisibility(8);
                this.amx_rela_bt_pair_warning.setVisibility(8);
                this.mtextView.setText(getString(R.string.amx_linking_device_remote));
                this.mpair_success.setVisibility(8);
                this.mprogressBar.setProgress(1);
                this.mprogressBar.setVisibility(0);
                this.mpairing.setVisibility(0);
                this.amx_rela_bt_pairing.setVisibility(0);
                return;
            }
            if (status == 3) {
                Log.d("[TvSettings]", "[ZTERemoteActivity]<statusChanged> STATUS_PAIRING");
                this.amx_rela_bt_pair.setVisibility(8);
                this.amx_rela_bt_pair_fail.setVisibility(8);
                this.amx_rela_bt_pair_warning.setVisibility(8);
                this.mtextView.setText(getString(R.string.amx_linking_device_remote));
                this.mpair_success.setVisibility(8);
                this.mprogressBar.setVisibility(0);
                this.mprogressBar.setProgress(2);
                this.mprogressBar.setVisibility(0);
                this.mpairing.setVisibility(0);
                this.amx_rela_bt_pairing.setVisibility(0);
                return;
            }
            if (status != 4) {
                return;
            }
            Log.d("[TvSettings]", "[ZTERemoteActivity]<statusChanged> STATUS_CONNECTING");
            this.amx_rela_bt_pair.setVisibility(8);
            this.amx_rela_bt_pair_fail.setVisibility(8);
            this.amx_rela_bt_pair_warning.setVisibility(8);
            this.mtextView.setText(getString(R.string.amx_linking_device_remote));
            this.mpair_success.setVisibility(8);
            this.mprogressBar.setVisibility(0);
            this.mprogressBar.setProgress(2);
            this.mprogressBar.setVisibility(0);
            this.mpairing.setVisibility(0);
            this.amx_rela_bt_pairing.setVisibility(0);
            this.mMsgHandler.sendEmptyMessageDelayed(3, 25000L);
            return;
        }
        Log.d("[TvSettings]", "[ZTERemoteActivity]<statusChanged> STATUS_SCANNING");
        this.mPairingSuccess = false;
        if (this.mBtAdapter != null) {
            Log.d("[TvSettings]", "[ZTERemoteActivity]<statusChanged> STATUS_SCANNING, mBtAdapter != null");
            Set<BluetoothDevice> bondedDevices = this.mBtAdapter.getBondedDevices();
            if (bondedDevices != null) {
                Log.d("[TvSettings]", "[ZTERemoteActivity]<statusChanged> STATUS_SCANNING, bondedDevices != null");
                Iterator<BluetoothDevice> it = bondedDevices.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    BluetoothDevice next = it.next();
                    Log.d("[TvSettings]", "[ZTERemoteActivity]<statusChanged> STATUS_SCANNING, deviceName=" + next.getName() + ", RemoteName=" + name + ", bfmPairingSuccess=" + this.mPairingSuccess);
                    if (next.isConnected() && isInputDevice(next.getBluetoothClass())) {
                        Log.d("[TvSettings]", "[ZTERemoteActivity]<statusChanged> STATUS_SCANNING, device:" + next.getAddress() + " is connected");
                        this.mPairingSuccess = true;
                        Log.d("[TvSettings]", "[ZTERemoteActivity]<statusChanged> STATUS_SCANNING, afmPairingSuccess=" + this.mPairingSuccess);
                        break;
                    }
                    Log.d("[TvSettings]", "[ZTERemoteActivity]<statusChanged> STATUS_SCANNING, afmPairingSuccess=" + this.mPairingSuccess);
                }
            }
        }
        if (this.mPairingSuccess) {
            Log.d("[TvSettings]", "[ZTERemoteActivity]<statusChanged> STATUS_SCANNING, Pairing Success");
            setClaroTvRcuType(1);
            this.flashOnTimeHandler.removeCallbacksAndMessages(null);
            this.mMsgHandler.removeCallbacksAndMessages(null);
            this.mMsgHandler.sendEmptyMessage(5);
        }
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i == 185) {
            Log.d("[TvSettings]", "[ZTERemoteActivity]<run> KeyEvent.KEYCODE_PROG_YELLOW");
            setClaroTvRcuType(0);
            stopBluetoothPairer();
            this.mMsgHandler.removeCallbacksAndMessages(null);
            this.mMsgHandler.sendEmptyMessage(2);
            return true;
        }
        return super.onKeyDown(i, keyEvent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setScanningView() {
        Log.d("[TvSettings]", "[ZTERemoteActivity]<setScanningView>, mFailCount=" + this.mFailCount);
        int i = this.mFailCount;
        if (i == 0) {
            this.amx_rela_bt_pair.setVisibility(0);
            this.amx_rela_bt_pairing.setVisibility(8);
            this.amx_rela_bt_pair_fail.setVisibility(8);
            this.amx_rela_bt_pair_warning.setVisibility(8);
            startBluetoothPairer();
            return;
        }
        if (i == 1) {
            this.amx_rela_bt_pair.setVisibility(8);
            this.amx_rela_bt_pairing.setVisibility(8);
            this.amx_rela_bt_pair_fail.setVisibility(0);
            this.amx_rela_bt_pair_warning.setVisibility(8);
            this.mtextView_1.setText(R.string.amx_remote_pair_count);
            startBluetoothPairer();
            return;
        }
        if (i == 2) {
            this.amx_rela_bt_pair.setVisibility(8);
            this.amx_rela_bt_pairing.setVisibility(8);
            this.amx_rela_bt_pair_fail.setVisibility(0);
            this.amx_rela_bt_pair_warning.setVisibility(8);
            this.mtextView_1.setText(R.string.amx_remote_pair_count_one);
            startBluetoothPairer();
            return;
        }
        if (i == 3) {
            this.amx_rela_bt_pair.setVisibility(8);
            this.amx_rela_bt_pairing.setVisibility(8);
            this.amx_rela_bt_pair_fail.setVisibility(8);
            this.amx_rela_bt_pair_warning.setVisibility(0);
            return;
        }
        Log.d("[TvSettings]", "[ZTERemoteActivity]<setScanningView>, unknown count");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setConnectedView() {
        Log.d("[TvSettings]", "[ZTERemoteActivity]<setConnectedView>");
        this.amx_rela_bt_pair.setVisibility(8);
        this.amx_rela_bt_pair_fail.setVisibility(8);
        this.amx_rela_bt_pair_warning.setVisibility(8);
        this.mprogressBar.setVisibility(8);
        this.mpairing.setVisibility(8);
        this.mtextView.setText(getString(R.string.amx_binding_success));
        this.mpair_success.setVisibility(0);
        this.amx_rela_bt_pairing.setVisibility(0);
        stopBluetoothPairer();
        this.flashOnTimeHandler.removeCallbacksAndMessages(null);
        this.mMsgHandler.removeCallbacksAndMessages(null);
        this.mMsgHandler.sendEmptyMessageDelayed(2, 5000L);
        setClaroBleType();
    }

    private void RemoveActivity() {
        setResult(1, null);
        Log.d("[TvSettings]", "[ZTERemoteActivity]<RemoveActivity> RemoveActivity SUCCESS");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void UpdataView() {
        Log.d("[TvSettings]", "[ZTERemoteActivity]<UpdataView> ");
        statusChanged();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void FinishActivity() {
        Log.d("[TvSettings]", "[ZTERemoteActivity]<FinishActivity> ");
        RemoveActivity();
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startBluetoothPairer() {
        Log.d("[TvSettings]", "[ZTERemoteActivity]<startBluetoothPairer> ");
        stopBluetoothPairer();
        BluetoothDevicePairer bluetoothDevicePairer = new BluetoothDevicePairer(this, this);
        this.mBluetoothPairer = bluetoothDevicePairer;
        bluetoothDevicePairer.start();
        this.flashOnTimeHandler.removeCallbacksAndMessages(null);
        this.flashOnTimeHandler.sendEmptyMessageDelayed(1, 5000L);
    }

    private void stopBluetoothPairer() {
        Log.d("[TvSettings]", "[ZTERemoteActivity]<stopBluetoothPairer> ");
        BluetoothDevicePairer bluetoothDevicePairer = this.mBluetoothPairer;
        if (bluetoothDevicePairer != null) {
            bluetoothDevicePairer.setListener(null);
            this.mBluetoothPairer.dispose();
            this.mBluetoothPairer = null;
        }
    }

    private boolean isMatchingDeviceClass(int i) {
        return (((((MINOR_DEVICE_CLASS_POINTING | MINOR_DEVICE_CLASS_JOYSTICK) | MINOR_DEVICE_CLASS_GAMEPAD) | MINOR_DEVICE_CLASS_KEYBOARD) | MINOR_DEVICE_CLASS_REMOTE) & i) != 0;
    }

    private boolean isInputDevice(BluetoothClass bluetoothClass) {
        Log.d("[TvSettings]", "[ZTERemoteActivity]MajorDeviceClass=" + bluetoothClass.getMajorDeviceClass() + ", DeviceClass=" + bluetoothClass.getDeviceClass());
        return isMatchingMajorDeviceClass(bluetoothClass.getMajorDeviceClass()) && isMatchingDeviceClass(bluetoothClass.getDeviceClass());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setClaroTvRcuType(int i) {
        if (Settings.Global.getInt(getApplicationContext().getContentResolver(), "claro_tv_rcu_type", -1) != i) {
            Log.i("[TvSettings]", " set claro_tv_rcu_type=" + i);
            SystemProperties.set("persist.sys.quickset.yellow", String.valueOf(i));
            Settings.Global.putInt(getApplicationContext().getContentResolver(), "claro_tv_rcu_type", i);
        }
    }

    private void setClaroBleType() {
        Log.d("[TvSettings]", "[ZTERemoteActivity]<setClaroBleType>");
        InputManager inputManager = (InputManager) getSystemService("input");
        int[] inputDeviceIds = inputManager.getInputDeviceIds();
        Log.d("[TvSettings]", "[ZTERemoteActivity]<setClaroBleType>, devices size=" + inputDeviceIds.length);
        for (int i : inputDeviceIds) {
            InputDevice inputDevice = inputManager.getInputDevice(i);
            if (inputDevice != null) {
                if (inputDevice.getVendorId() == 1767 && inputDevice.getProductId() == 33144) {
                    Log.d("[TvSettings]", "[ZTERemoteActivity]<setClaroBleType>, v6");
                    SystemProperties.set("persist.sys.quickset.pid", "6");
                } else if (inputDevice.getVendorId() == 1767 && inputDevice.getProductId() == 33156) {
                    Log.d("[TvSettings]", "[ZTERemoteActivity]<setClaroBleType>, v7");
                    SystemProperties.set("persist.sys.quickset.pid", "7");
                } else if (inputDevice.getVendorId() == 13 && inputDevice.getProductId() == 14392) {
                    Log.d("[TvSettings]", "[ZTERemoteActivity]<setClaroBleType>, zte");
                    SystemProperties.set("persist.sys.quickset.pid", "11");
                }
            }
        }
    }
}
