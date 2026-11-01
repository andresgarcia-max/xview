package com.control.device.settings.MexicAmco.AMXUpdateAtvKey;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.control.device.R;
import com.control.device.settings.MexicAmco.AMXUpdateAtvKey.KeyHandleThreadManager;
import com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net.AmxKeyWifiListFragment;

/* loaded from: classes.dex */
public class UpdateAtvKeyActivity extends FragmentActivity implements KeyHandleThreadManager.EventListener {
    private FragmentManager mFm;
    private KeyHandleThreadManager mKeyHandleThreadManager;
    private LinearLayout mPart1Start;
    private Button mPart1StartBtnContinue;
    private LinearLayout mPart1Welcome;
    private Button mPart1WelcomeBtnContinue;
    private LinearLayout mPart2NetworkConnectFailed;
    private Button mPart2NetworkConnectFailedBtnContinue;
    private LinearLayout mPart2NetworkConnectSuccess;
    private LinearLayout mPart2NetworkConnecting;
    private FrameLayout mPart2NetworkList;
    private AmxKeyWifiListFragment mPart2NetworkListFragment;
    private LinearLayout mPart3InstallFail1;
    private Button mPart3InstallFail1BtnRetry;
    private LinearLayout mPart3InstallFail2;
    private LinearLayout mPart3InstallFailLast;
    private LinearLayout mPart3InstallSuccess;
    private Button mPart3InstallSuccessBtnContinue;
    private LinearLayout mPart3Installing;
    private TextView mPart3InstallingPercent;
    private ProgressBar mPart3InstallingPrg;
    private LinearLayout mPart4Reset;
    private ScheduleHandler mScheduleHandler;
    private int mCurrentStep = 0;
    private int mCurrentStepItem = 0;
    private boolean mCurrentStepItemResult = false;
    private int mCurUiId = 0;
    private int mFailCount = 0;
    private BroadcastReceiver mNetReceiver = new BroadcastReceiver() { // from class: com.control.device.settings.MexicAmco.AMXUpdateAtvKey.UpdateAtvKeyActivity.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Log.i("AMXUpdateAtvKey", "NetReceiver, action=" + intent.getAction());
            if (UpdateAtvKeyActivity.this.mKeyHandleThreadManager.isWorking()) {
                return;
            }
            Log.i("AMXUpdateAtvKey", "NetReceiver, work thread is stopped");
        }
    };

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Log.i("AMXUpdateAtvKey", "<onCreate>");
        setContentView(R.layout.amx_key_update);
        this.mFm = getSupportFragmentManager();
        initView();
        this.mKeyHandleThreadManager = KeyHandleThreadManager.getInstance(this, this);
        this.mScheduleHandler = new ScheduleHandler(getMainLooper());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(this.mNetReceiver, intentFilter);
    }

    void initView() {
        this.mPart1Welcome = (LinearLayout) findViewById(R.id.part1_welcome);
        this.mPart1Start = (LinearLayout) findViewById(R.id.part1_start);
        this.mPart2NetworkList = (FrameLayout) findViewById(R.id.part2_network_list);
        this.mPart2NetworkConnecting = (LinearLayout) findViewById(R.id.part2_network_connecting);
        this.mPart2NetworkConnectFailed = (LinearLayout) findViewById(R.id.part2_network_connect_failed);
        this.mPart2NetworkConnectSuccess = (LinearLayout) findViewById(R.id.part2_network_connect_success);
        this.mPart3Installing = (LinearLayout) findViewById(R.id.part3_installing);
        this.mPart3InstallFail1 = (LinearLayout) findViewById(R.id.part3_install_fail1);
        this.mPart3InstallFail2 = (LinearLayout) findViewById(R.id.part3_install_fail2);
        this.mPart3InstallFailLast = (LinearLayout) findViewById(R.id.part3_install_fail_last);
        this.mPart3InstallSuccess = (LinearLayout) findViewById(R.id.part3_install_success);
        this.mPart4Reset = (LinearLayout) findViewById(R.id.part4_reset);
        this.mPart1Welcome.setVisibility(8);
        this.mPart1Start.setVisibility(8);
        this.mPart2NetworkList.setVisibility(8);
        this.mPart2NetworkConnecting.setVisibility(8);
        this.mPart2NetworkConnectFailed.setVisibility(8);
        this.mPart2NetworkConnectSuccess.setVisibility(8);
        this.mPart3Installing.setVisibility(8);
        this.mPart3InstallFail1.setVisibility(8);
        this.mPart3InstallFail2.setVisibility(8);
        this.mPart3InstallFailLast.setVisibility(8);
        this.mPart3InstallSuccess.setVisibility(8);
        this.mPart4Reset.setVisibility(8);
        Button button = (Button) findViewById(R.id.part1_welcome_continue);
        this.mPart1WelcomeBtnContinue = button;
        button.setOnClickListener(new OnWelcomeContinueClickListener());
        Button button2 = (Button) findViewById(R.id.part1_start_continue);
        this.mPart1StartBtnContinue = button2;
        button2.setOnClickListener(new OnInstallContinueClickListener());
        this.mPart2NetworkListFragment = new AmxKeyWifiListFragment();
        Button button3 = (Button) findViewById(R.id.part2_network_connect_failed_continue);
        this.mPart2NetworkConnectFailedBtnContinue = button3;
        button3.setOnClickListener(new OnConnectFailContinueClickListener());
        this.mPart3InstallingPrg = (ProgressBar) findViewById(R.id.part3_installing_progressBar);
        this.mPart3InstallingPercent = (TextView) findViewById(R.id.part3_installing_percent);
        Button button4 = (Button) findViewById(R.id.part3_install_fail1_retry);
        this.mPart3InstallFail1BtnRetry = button4;
        button4.setOnClickListener(new OnInstallFailRetryClickListener());
        Button button5 = (Button) findViewById(R.id.part3_install_success_continue);
        this.mPart3InstallSuccessBtnContinue = button5;
        button5.setOnClickListener(new OnInstallSuccessContinueClickListener());
        setViewVisible(1);
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onStart() {
        Log.i("AMXUpdateAtvKey", "<onStart>");
        super.onStart();
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onResume() {
        Log.i("AMXUpdateAtvKey", "<onResume>");
        KeyHandleThreadManager keyHandleThreadManager = this.mKeyHandleThreadManager;
        if (keyHandleThreadManager != null) {
            keyHandleThreadManager.start();
        }
        super.onResume();
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onPause() {
        Log.i("AMXUpdateAtvKey", "<onPause>");
        super.onPause();
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onStop() {
        Log.i("AMXUpdateAtvKey", "<onStop>");
        super.onStop();
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        Log.i("AMXUpdateAtvKey", "<onDestroy>");
        KeyHandleThreadManager keyHandleThreadManager = this.mKeyHandleThreadManager;
        if (keyHandleThreadManager != null) {
            keyHandleThreadManager.quit();
        }
        unregisterReceiver(this.mNetReceiver);
        super.onDestroy();
    }

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        Log.i("AMXUpdateAtvKey", "<onActivityResult>, requestCode=" + i + ", resultCode=" + i2);
        if (i2 == -1) {
            if (this.mKeyHandleThreadManager.isInternetValidated()) {
                setViewVisible(6);
                if (this.mScheduleHandler.hasMessages(2)) {
                    this.mScheduleHandler.removeMessages(2);
                }
                this.mScheduleHandler.sendEmptyMessageDelayed(2, 2000L);
                return;
            }
            setViewVisible(5);
        }
    }

    @Override // androidx.activity.ComponentActivity, android.app.Activity
    public void onBackPressed() {
        Log.i("AMXUpdateAtvKey", "<onBackPressed>, nothing to do");
    }

    private class OnWelcomeContinueClickListener implements View.OnClickListener {
        private OnWelcomeContinueClickListener() {
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            Log.d("AMXUpdateAtvKey", "<onClick> welcome continue");
            UpdateAtvKeyActivity.this.setViewVisible(2);
        }
    }

    private class OnInstallContinueClickListener implements View.OnClickListener {
        private OnInstallContinueClickListener() {
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            Log.d("AMXUpdateAtvKey", "<onClick> install continue");
            if (UpdateAtvKeyActivity.this.mKeyHandleThreadManager.isInternetValidated()) {
                UpdateAtvKeyActivity.this.installing();
            } else {
                UpdateAtvKeyActivity.this.selectNetwork();
            }
        }
    }

    private class OnConnectFailContinueClickListener implements View.OnClickListener {
        private OnConnectFailContinueClickListener() {
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            Log.d("AMXUpdateAtvKey", "<onClick> connect fail continue");
            UpdateAtvKeyActivity.this.selectNetwork();
        }
    }

    private class OnInstallFailRetryClickListener implements View.OnClickListener {
        private OnInstallFailRetryClickListener() {
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            Log.d("AMXUpdateAtvKey", "<onClick> install fail retry");
            if (UpdateAtvKeyActivity.this.mKeyHandleThreadManager.isInternetValidated()) {
                UpdateAtvKeyActivity.this.installing();
            } else {
                UpdateAtvKeyActivity.this.selectNetwork();
            }
        }
    }

    private class OnInstallSuccessContinueClickListener implements View.OnClickListener {
        private OnInstallSuccessContinueClickListener() {
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            Log.d("AMXUpdateAtvKey", "<onClick> install success continue");
            UpdateAtvKeyActivity.this.setViewVisible(12);
            UpdateAtvKeyActivity.this.mKeyHandleThreadManager.start_factory_reset();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void selectNetwork() {
        this.mFailCount = 0;
        setViewVisible(3);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void installing() {
        Log.d("AMXUpdateAtvKey", "installing");
        setViewVisible(7);
        if (this.mScheduleHandler.hasMessages(2)) {
            this.mScheduleHandler.removeMessages(2);
        }
        this.mKeyHandleThreadManager.start_attestationKey_check();
    }

    private void installFailed() {
        Log.d("AMXUpdateAtvKey", "installFailed");
        int i = this.mFailCount + 1;
        this.mFailCount = i;
        if (i == 1) {
            setViewVisible(8);
            return;
        }
        if (i != 2 && i != 3) {
            if (i == 4) {
                setViewVisible(10);
            }
        } else {
            setViewVisible(9);
            if (this.mScheduleHandler.hasMessages(2)) {
                this.mScheduleHandler.removeMessages(2);
            }
            this.mScheduleHandler.sendEmptyMessageDelayed(2, 30000L);
        }
    }

    private void installSuccess() {
        Log.d("AMXUpdateAtvKey", "installSuccess");
        setViewVisible(11);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setViewVisible(int i) {
        Log.d("AMXUpdateAtvKey", "setViewVisible, id=" + i);
        switch (i) {
            case 1:
                setViewGone(this.mCurUiId);
                this.mCurUiId = i;
                this.mPart1Welcome.setVisibility(0);
                this.mPart1WelcomeBtnContinue.requestFocus();
                break;
            case 2:
                setViewGone(this.mCurUiId);
                this.mCurUiId = i;
                this.mPart1Start.setVisibility(0);
                this.mPart1StartBtnContinue.requestFocus();
                break;
            case 3:
                setViewGone(this.mCurUiId);
                this.mCurUiId = i;
                this.mPart2NetworkList.setVisibility(0);
                if (!this.mPart2NetworkListFragment.isAdded()) {
                    Log.d("AMXUpdateAtvKey", "setViewVisible, net list add fragment");
                    FragmentTransaction beginTransaction = this.mFm.beginTransaction();
                    beginTransaction.replace(R.id.part2_network_list_wifi_container, this.mPart2NetworkListFragment);
                    beginTransaction.commitNowAllowingStateLoss();
                }
                this.mPart2NetworkListFragment.setVisible(true);
                break;
            case 4:
                setViewGone(this.mCurUiId);
                this.mCurUiId = i;
                this.mPart2NetworkConnecting.setVisibility(0);
                break;
            case 5:
                setViewGone(this.mCurUiId);
                this.mCurUiId = i;
                this.mPart2NetworkConnectFailed.setVisibility(0);
                this.mPart2NetworkConnectFailedBtnContinue.requestFocus();
                break;
            case 6:
                setViewGone(this.mCurUiId);
                this.mCurUiId = i;
                this.mPart2NetworkConnectSuccess.setVisibility(0);
                break;
            case 7:
                setViewGone(this.mCurUiId);
                this.mCurUiId = i;
                this.mPart3InstallingPrg.setProgress(0);
                this.mPart3InstallingPercent.setText(String.valueOf(0) + "%");
                this.mPart3Installing.setVisibility(0);
                break;
            case 8:
                setViewGone(this.mCurUiId);
                this.mCurUiId = i;
                this.mPart3InstallFail1.setVisibility(0);
                this.mPart3InstallFail1BtnRetry.requestFocus();
                break;
            case 9:
                setViewGone(this.mCurUiId);
                this.mCurUiId = i;
                this.mPart3InstallFail2.setVisibility(0);
                break;
            case 10:
                setViewGone(this.mCurUiId);
                this.mCurUiId = i;
                this.mPart3InstallFailLast.setVisibility(0);
                break;
            case 11:
                setViewGone(this.mCurUiId);
                this.mCurUiId = i;
                this.mPart3InstallSuccess.setVisibility(0);
                this.mPart3InstallSuccessBtnContinue.requestFocus();
                break;
            case 12:
                setViewGone(this.mCurUiId);
                this.mCurUiId = i;
                this.mPart4Reset.setVisibility(0);
                break;
            default:
                Log.i("AMXUpdateAtvKey", "setViewVisible, unknown id");
                break;
        }
    }

    private void setViewGone(int i) {
        Log.d("AMXUpdateAtvKey", "setViewGone, id=" + i);
        switch (i) {
            case 1:
                this.mPart1Welcome.setVisibility(8);
                break;
            case 2:
                this.mPart1Start.setVisibility(8);
                break;
            case 3:
                this.mPart2NetworkList.setVisibility(8);
                if (this.mPart2NetworkListFragment.isAdded()) {
                    Log.d("AMXUpdateAtvKey", "setViewGone, net list remove fragment");
                    FragmentTransaction beginTransaction = this.mFm.beginTransaction();
                    beginTransaction.remove(this.mPart2NetworkListFragment);
                    beginTransaction.commitNowAllowingStateLoss();
                }
                this.mPart2NetworkListFragment.setVisible(false);
                break;
            case 4:
                this.mPart2NetworkConnecting.setVisibility(8);
                break;
            case 5:
                this.mPart2NetworkConnectFailed.setVisibility(8);
                break;
            case 6:
                this.mPart2NetworkConnectSuccess.setVisibility(8);
                break;
            case 7:
                this.mPart3Installing.setVisibility(8);
                break;
            case 8:
                this.mPart3InstallFail1.setVisibility(8);
                break;
            case 9:
                this.mPart3InstallFail2.setVisibility(8);
                break;
            case 10:
                this.mPart3InstallFailLast.setVisibility(8);
                break;
            case 11:
                this.mPart3InstallSuccess.setVisibility(8);
                break;
            case 12:
                this.mPart4Reset.setVisibility(8);
                break;
            default:
                Log.i("AMXUpdateAtvKey", "setViewGone, unknown id");
                break;
        }
    }

    @Override // com.control.device.settings.MexicAmco.AMXUpdateAtvKey.KeyHandleThreadManager.EventListener
    public void onStatusChanged(int i, int i2, boolean z) {
        if (isFinishing()) {
            Log.i("AMXUpdateAtvKey", "onStatusChanged, activity finished, step=" + i + ", item=" + i2 + ", result=" + z);
            return;
        }
        if (this.mScheduleHandler == null) {
            Log.i("AMXUpdateAtvKey", "onStatusChanged, schedule handler is null");
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putInt("key_step", i);
        bundle.putInt("key_step_item", i2);
        bundle.putBoolean("key_step_item_result", z);
        Message obtainMessage = this.mScheduleHandler.obtainMessage();
        obtainMessage.what = 1;
        obtainMessage.setData(bundle);
        this.mScheduleHandler.sendMessage(obtainMessage);
    }

    private class ScheduleHandler extends Handler {
        public ScheduleHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            Log.i("AMXUpdateAtvKey", "ScheduleHandler: msg.what=" + message.what);
            int i = message.what;
            if (i != 1) {
                if (i == 2) {
                    UpdateAtvKeyActivity.this.installing();
                    return;
                } else {
                    Log.i("AMXUpdateAtvKey", "ScheduleHandler, unknown msg");
                    return;
                }
            }
            Bundle data = message.getData();
            if (data != null) {
                UpdateAtvKeyActivity.this.statusChanged(data.getInt("key_step"), data.getInt("key_step_item"), data.getBoolean("key_step_item_result"));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void statusChanged(int i, int i2, boolean z) {
        Log.i("AMXUpdateAtvKey", "statusChanged, step=" + i + ", item=" + i2 + ", result=" + z);
        this.mCurrentStep = i;
        this.mCurrentStepItem = i2;
        this.mCurrentStepItemResult = z;
        if (i == 100) {
            statusChanged_network(i2, z);
            return;
        }
        if (i == 200) {
            statusChanged_attestationKey(i2, z);
        } else if (i == 300) {
            statusChanged_widevineKey(i2, z);
        } else if (i != 400) {
            Log.i("AMXUpdateAtvKey", "statusChanged, unknown type");
        }
    }

    private void statusChanged_network(int i, boolean z) {
        Log.i("AMXUpdateAtvKey", "statusChanged_network, item=" + i + ", result=" + z);
        if (this.mKeyHandleThreadManager == null) {
            Log.i("AMXUpdateAtvKey", "statusChanged_network, handle thread is not ready");
            return;
        }
        if (i == 101) {
            Log.i("AMXUpdateAtvKey", "statusChanged_network, network validated " + z);
            return;
        }
        if (i == 102) {
            Log.i("AMXUpdateAtvKey", "statusChanged_network, network connected " + z);
            return;
        }
        Log.i("AMXUpdateAtvKey", "statusChanged_network, unknown type");
    }

    private void statusChanged_attestationKey(int i, boolean z) {
        Log.i("AMXUpdateAtvKey", "statusChanged_attestationKey, item=" + i + ", result=" + z);
        KeyHandleThreadManager keyHandleThreadManager = this.mKeyHandleThreadManager;
        if (keyHandleThreadManager == null) {
            Log.i("AMXUpdateAtvKey", "statusChanged_attestationKey, handle thread is not ready");
        }
        switch (i) {
            case 201:
                if (z) {
                    this.mPart3InstallingPrg.setProgress(50);
                    this.mPart3InstallingPercent.setText(String.valueOf(50) + "%");
                    this.mKeyHandleThreadManager.start_widevineKey_check();
                    break;
                } else {
                    keyHandleThreadManager.start_attestationKey_get();
                    break;
                }
            case 202:
                if (z) {
                    this.mPart3InstallingPrg.setProgress(15);
                    this.mPart3InstallingPercent.setText(String.valueOf(15) + "%");
                    this.mKeyHandleThreadManager.start_attestationKey_hashCheck();
                    break;
                } else {
                    installFailed();
                    break;
                }
            case 203:
                if (z) {
                    this.mPart3InstallingPrg.setProgress(30);
                    this.mPart3InstallingPercent.setText(String.valueOf(30) + "%");
                    this.mKeyHandleThreadManager.start_attestationKey_install();
                    break;
                } else {
                    installFailed();
                    break;
                }
            case 204:
                if (z) {
                    this.mPart3InstallingPrg.setProgress(50);
                    this.mPart3InstallingPercent.setText(String.valueOf(50) + "%");
                    this.mKeyHandleThreadManager.start_widevineKey_check();
                    break;
                } else {
                    installFailed();
                    break;
                }
            default:
                Log.i("AMXUpdateAtvKey", "statusChanged_attestationKey, unknown type");
                break;
        }
    }

    private void statusChanged_widevineKey(int i, boolean z) {
        Log.i("AMXUpdateAtvKey", "statusChanged_widevineKey, item=" + i + ", result=" + z);
        KeyHandleThreadManager keyHandleThreadManager = this.mKeyHandleThreadManager;
        if (keyHandleThreadManager == null) {
            Log.i("AMXUpdateAtvKey", "statusChanged_widevineKey, handle thread is not ready");
        }
        switch (i) {
            case 301:
                if (z) {
                    this.mPart3InstallingPrg.setProgress(100);
                    this.mPart3InstallingPercent.setText(String.valueOf(100) + "%");
                    installSuccess();
                    break;
                } else {
                    keyHandleThreadManager.start_widevineKey_get();
                    break;
                }
            case 302:
                if (z) {
                    this.mPart3InstallingPrg.setProgress(65);
                    this.mPart3InstallingPercent.setText(String.valueOf(65) + "%");
                    this.mKeyHandleThreadManager.start_widevineKey_hashCheck();
                    break;
                } else {
                    installFailed();
                    break;
                }
            case 303:
                if (z) {
                    this.mPart3InstallingPrg.setProgress(80);
                    this.mPart3InstallingPercent.setText(String.valueOf(80) + "%");
                    this.mKeyHandleThreadManager.start_widevineKey_install();
                    break;
                } else {
                    installFailed();
                    break;
                }
            case 304:
                if (z) {
                    this.mPart3InstallingPrg.setProgress(100);
                    this.mPart3InstallingPercent.setText(String.valueOf(100) + "%");
                    installSuccess();
                    break;
                } else {
                    installFailed();
                    break;
                }
            default:
                Log.i("AMXUpdateAtvKey", "statusChanged_attestationKey, unknown type");
                break;
        }
    }
}
