package com.control.device.settings.MexicAmco;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import com.android.tv.settings.adv.ISystemControlManager;
import com.android.tv.settings.adv.impl.SystemControlManager;
import com.control.device.sys.interfcae.OutputUiManager;
import com.zte.android.productcfg.ZteMcspCfgManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/* loaded from: classes.dex */
public class HdcpVerifyService extends Service {
    public static HdcpVerifyActivity mVerifyActivity;
    private static ZteMcspCfgManager mZteMcspCfgManager;
    private DisplayManager mDisplayManager;
    private ISystemControlManager mISystemControlManager;
    private OutputUiManager mOutputUiManager;
    private PowerManager mPowerManager;
    private String SYS_HDCPTxAuth_PATH = "/sys/module/hdmitx20/parameters/hdmi_authenticated";
    private Display.Mode DISPLAY_MODE_480 = new Display.Mode(720, 480, 60.000004f);
    private String mBuildType = SystemProperties.get("ro.build.type", "user");
    private String CpuName = SystemProperties.get("ro.product.cpu.name");
    private boolean isNetflixForeground = false;
    private int mLastHdcpCheckStatus = -1;
    protected BroadcastReceiver HdcpVerifyReceiver = new BroadcastReceiver() { // from class: com.control.device.settings.MexicAmco.HdcpVerifyService.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("[TvSettings]", "[HdcpVerifyService]<onReceive>, action = " + action);
            if (action.equals("droidlogic.intent.action.SYSTEM_CONTROL_EVENT")) {
                int intExtra = intent.getIntExtra("event", -1);
                Log.d("[TvSettings]", "[HdcpVerifyService]<onReceive> ACTION_SYSTEM_CONTROL_EVENT eventType = " + intExtra);
                if (intExtra == 7) {
                    HdcpVerifyService.this.mHandler.removeMessages(2);
                    HdcpVerifyService.this.mHandler.removeMessages(4);
                    if (HdcpVerifyService.this.mHandler.hasMessages(3)) {
                        return;
                    }
                    Log.d("[TvSettings]", "[HdcpVerifyService]<onReceive>, MSG_HDCP_CHECK_FAILED 3s delay");
                    HdcpVerifyService.this.mHandler.sendEmptyMessageDelayed(3, 3000L);
                    return;
                }
                if (intExtra == 8) {
                    HdcpVerifyService.this.mHandler.removeMessages(2);
                    HdcpVerifyService.this.mHandler.removeMessages(3);
                    HdcpVerifyService.this.mHandler.removeCallbacks(HdcpVerifyService.this.mFailedActivityTipsRunnable);
                    if (HdcpVerifyService.this.mHandler.hasMessages(4)) {
                        return;
                    }
                    Log.d("[TvSettings]", "[HdcpVerifyService]<onReceive>, MSG_HDCP_CHECK_SUCCESS 300ms delay");
                    HdcpVerifyService.this.mHandler.sendEmptyMessageDelayed(4, 300L);
                }
            }
        }
    };
    private Handler mHandler = new Handler(Looper.getMainLooper()) { // from class: com.control.device.settings.MexicAmco.HdcpVerifyService.2
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i != 1) {
                if (i == 2 || i == 3 || i == 4) {
                    HdcpVerifyService.this.checkHdcpVerifyBySys();
                    return;
                } else {
                    Log.d("[TvSettings]", "[HdcpVerifyService]unknown msg");
                    return;
                }
            }
            boolean isRunningActivity = HdcpVerifyService.this.isRunningActivity("com.netflix.ninja");
            if (HdcpVerifyService.this.isNetflixForeground != isRunningActivity) {
                Log.d("[TvSettings]", "[HdcpVerifyService]isNetflixForeground changed, " + HdcpVerifyService.this.isNetflixForeground + " to " + isRunningActivity);
                HdcpVerifyService.this.isNetflixForeground = isRunningActivity;
                if (HdcpVerifyService.this.isNetflixForeground) {
                    HdcpVerifyService.this.onNetflixForeground();
                } else {
                    HdcpVerifyService.this.onNetflixBackground();
                }
            }
            HdcpVerifyService.this.mHandler.removeMessages(1);
            HdcpVerifyService.this.mHandler.sendEmptyMessageDelayed(1, 1000L);
        }
    };
    private Runnable mFailedActivityTipsRunnable = new Runnable() { // from class: com.control.device.settings.MexicAmco.HdcpVerifyService.3
        @Override // java.lang.Runnable
        public void run() {
            if (HdcpVerifyService.this.isRunningActivity(HdcpVerifyActivity.class.getName())) {
                Log.i("[TvSettings]", "[HdcpVerifyService]mFailedTipsRunnable, HdcpVerifyActivity is already the top");
                return;
            }
            try {
                Intent intent = new Intent();
                intent.setClassName(HdcpVerifyService.this.getPackageName(), HdcpVerifyActivity.class.getName());
                intent.addFlags(268435456);
                HdcpVerifyService.this.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        return 1;
    }

    @Override // android.app.Service
    public void onCreate() {
        Log.d("[TvSettings]", "[HdcpVerifyService]onCreate");
        super.onCreate();
        if (this.CpuName.contains("905X4")) {
            Log.d("[TvSettings]", "[HdcpVerifyService]onCreate, cpu:905X4");
            this.SYS_HDCPTxAuth_PATH = "/sys/module/aml_media/parameters/hdmi_authenticated";
        }
        this.mDisplayManager = (DisplayManager) getSystemService("display");
        this.mISystemControlManager = SystemControlManager.getInstance();
        this.mOutputUiManager = new OutputUiManager(getApplicationContext());
        this.mPowerManager = (PowerManager) getSystemService("power");
        mZteMcspCfgManager = ZteMcspCfgManager.getInstance(getApplicationContext());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("droidlogic.intent.action.SYSTEM_CONTROL_EVENT");
        registerReceiver(this.HdcpVerifyReceiver, intentFilter);
        syncTvAspectratioFromSettings();
        this.mHandler.sendEmptyMessageDelayed(2, 3000L);
        this.mHandler.sendEmptyMessageDelayed(1, 1000L);
    }

    private boolean noNeedCheckHdcpVerify() {
        boolean isHDMIPlugged = this.mOutputUiManager.isHDMIPlugged();
        boolean isInteractive = this.mPowerManager.isInteractive();
        if (!"userdebug".equals(this.mBuildType) && isHDMIPlugged && isInteractive) {
            return false;
        }
        Log.i("[TvSettings]", "[HdcpVerifyService]noNeedCheckHdcpVerify, no need to check hdcp verify, type=" + this.mBuildType + ", hdmi=" + isHDMIPlugged + ", active=" + isInteractive);
        return true;
    }

    @Override // android.app.Service
    public void onDestroy() {
        Log.d("[TvSettings]", "[HdcpVerifyService]onDestroy");
        unregisterReceiver(this.HdcpVerifyReceiver);
        this.mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkHdcpVerifyBySys() {
        String readFileContent = readFileContent(this.SYS_HDCPTxAuth_PATH);
        Log.i("[TvSettings]", "[HdcpVerifyService]checkHdcpVerifyBySys, HDCPTxAuth=" + readFileContent);
        if ("0".equals(readFileContent)) {
            onHdcpVerificationFailed();
        } else if ("1".equals(readFileContent)) {
            onHdcpVerificationSuccess();
        }
    }

    private void onHdcpVerificationFailed() {
        Log.i("[TvSettings]", "[HdcpVerifyService]onHdcpVerificationFailed, mLastHdcpCheckStatus=" + this.mLastHdcpCheckStatus);
        if (noNeedCheckHdcpVerify()) {
            Log.i("[TvSettings]", "[HdcpVerifyService]onHdcpVerificationFailed, no need to check hdcp verify");
            return;
        }
        int i = this.mLastHdcpCheckStatus;
        if (i == 0) {
            Log.i("[TvSettings]", "[HdcpVerifyService]onHdcpVerificationFailed, already failed status");
            return;
        }
        if (this.isNetflixForeground) {
            Log.i("[TvSettings]", "[HdcpVerifyService]onHdcpVerificationFailed, netflix is foreground, do it after netflix background");
            this.mLastHdcpCheckStatus = 2;
            return;
        }
        this.mLastHdcpCheckStatus = 0;
        writeSysfs("8", "/sys/class/video/screen_mode");
        this.mOutputUiManager.updateUiMode();
        String currentMode = this.mOutputUiManager.getCurrentMode();
        Log.i("[TvSettings]", "[HdcpVerifyService]onHdcpVerificationFailed, curMode=" + currentMode);
        if (!"480p60hz".equals(currentMode)) {
            Log.i("[TvSettings]", "[HdcpVerifyService]onHdcpVerificationFailed, set mode 480p60hz");
            try {
                Display.Mode checkUserPreferredMode = checkUserPreferredMode(this.mDisplayManager.getDisplay(0).getSupportedModes(), this.DISPLAY_MODE_480);
                Display.Mode userPreferredDisplayMode = this.mDisplayManager.getUserPreferredDisplayMode();
                if (userPreferredDisplayMode != null) {
                    Log.i("[TvSettings]", "[HdcpVerifyService]onHdcpVerificationFailed, userPreferredDisplayMode");
                    if (checkSysCurrentMode(userPreferredDisplayMode, checkUserPreferredMode)) {
                        Log.i("[TvSettings]", "[HdcpVerifyService]onHdcpVerificationFailed, clearUserPreferredDisplayMode");
                        this.mDisplayManager.clearUserPreferredDisplayMode();
                    }
                }
                Log.i("[TvSettings]", "[HdcpVerifyService]onHdcpVerificationFailed, setUserPreferredDisplayMode, matcherMode=" + checkUserPreferredMode);
                this.mDisplayManager.setUserPreferredDisplayMode(checkUserPreferredMode);
                this.mISystemControlManager.setProperty("vendor.sys.hdcp_result", "0");
                if (i != 2) {
                    this.mHandler.removeCallbacks(this.mFailedActivityTipsRunnable);
                    this.mHandler.postDelayed(this.mFailedActivityTipsRunnable, 1500L);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.i("[TvSettings]", "[HdcpVerifyService]onHdcpVerificationFailed, already 480p60hz");
        }
        if (i == 2 || this.mHandler.hasCallbacks(this.mFailedActivityTipsRunnable)) {
            return;
        }
        this.mHandler.post(this.mFailedActivityTipsRunnable);
    }

    private void onHdcpVerificationSuccess() {
        Log.i("[TvSettings]", "[HdcpVerifyService]onHdcpVerificationSuccess");
        if (noNeedCheckHdcpVerify()) {
            Log.i("[TvSettings]", "[HdcpVerifyService]onHdcpVerificationSuccess, no need to check hdcp verify");
            return;
        }
        if (mVerifyActivity != null) {
            Log.i("[TvSettings]", "[HdcpVerifyService]onHdcpVerificationSuccess, VerifyActivity is not null");
            if (!mVerifyActivity.isFinishing()) {
                Log.i("[TvSettings]", "[HdcpVerifyService]onHdcpVerificationSuccess, finish VerifyActivity");
                mVerifyActivity.finish();
            }
            mVerifyActivity = null;
        }
        if (this.mLastHdcpCheckStatus == 1) {
            Log.i("[TvSettings]", "[HdcpVerifyService]onHdcpVerificationSuccess, already success status");
            return;
        }
        this.mLastHdcpCheckStatus = 1;
        syncTvAspectratioFromSettings();
        this.mOutputUiManager.updateUiMode();
        String currentMode = this.mOutputUiManager.getCurrentMode();
        Log.i("[TvSettings]", "[HdcpVerifyService]onHdcpVerificationSuccess, curMode=" + currentMode);
        if ("480p60hz".equals(currentMode)) {
            String ReadStrFromCfgmanager = mZteMcspCfgManager.ReadStrFromCfgmanager("Common", "HDMIMode", "");
            Log.i("[TvSettings]", "[HdcpVerifyService]onHdcpVerificationSuccess, configHdmiMode=" + ReadStrFromCfgmanager);
            if (TextUtils.isEmpty(ReadStrFromCfgmanager) || ReadStrFromCfgmanager.equalsIgnoreCase("Auto")) {
                try {
                    this.mOutputUiManager.change2BestMode();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            try {
                this.mOutputUiManager.change2NewMode(ReadStrFromCfgmanager);
                return;
            } catch (Exception e2) {
                e2.printStackTrace();
                Log.i("[TvSettings]", "[HdcpVerifyService]onHdcpVerificationSuccess, change2NewMode failed, set to default 1080p60hz");
                try {
                    this.mOutputUiManager.change2NewMode("1080p60hz");
                    return;
                } catch (Exception e3) {
                    e3.printStackTrace();
                    return;
                }
            }
        }
        Log.i("[TvSettings]", "[HdcpVerifyService]onHdcpVerificationSuccess, not 480p, no need to change display mode");
    }

    private String readFileContent(String str) {
        if (this.CpuName.contains("905X4")) {
            Log.d("[TvSettings]", "[HdcpVerifyService]cpu=905X4, use system control");
            return this.mISystemControlManager.readSysFs(str);
        }
        BufferedReader bufferedReader = null;
        StringBuffer stringBuffer = new StringBuffer();
        try {
            try {
                File file = new File(str);
                if (!file.exists()) {
                    Log.d("[TvSettings]", "[HdcpVerifyService]File not found: " + str);
                    return "";
                }
                BufferedReader bufferedReader2 = new BufferedReader(new FileReader(file));
                while (true) {
                    try {
                        String readLine = bufferedReader2.readLine();
                        if (readLine == null) {
                            break;
                        }
                        stringBuffer.append(readLine);
                    } catch (IOException e) {
                        e = e;
                        bufferedReader = bufferedReader2;
                        e.printStackTrace();
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            }
                        }
                        return stringBuffer.toString();
                    } catch (Throwable th) {
                        th = th;
                        bufferedReader = bufferedReader2;
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                        }
                        throw th;
                    }
                }
                bufferedReader2.close();
                String stringBuffer2 = stringBuffer.toString();
                try {
                    bufferedReader2.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
                return stringBuffer2;
            } catch (IOException e5) {
                e = e5;
            }
        } catch (Throwable th2) {
            th = th2;
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:22:0x003e A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private void writeSysfs(java.lang.String r2, java.lang.String r3) {
        /*
            r1 = this;
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r0 = "[HdcpVerifyService]writeSysfs value="
            r1.append(r0)
            r1.append(r2)
            java.lang.String r0 = " path="
            r1.append(r0)
            r1.append(r3)
            java.lang.String r1 = r1.toString()
            java.lang.String r0 = "[TvSettings]"
            android.util.Log.d(r0, r1)
            r1 = 0
            java.io.FileWriter r0 = new java.io.FileWriter     // Catch: java.lang.Throwable -> L2d java.lang.Exception -> L31
            r0.<init>(r3)     // Catch: java.lang.Throwable -> L2d java.lang.Exception -> L31
            r0.write(r2)     // Catch: java.lang.Exception -> L2b java.lang.Throwable -> L3b
        L27:
            r0.close()     // Catch: java.lang.Exception -> L3a
            goto L3a
        L2b:
            r1 = move-exception
            goto L34
        L2d:
            r2 = move-exception
            r0 = r1
            r1 = r2
            goto L3c
        L31:
            r2 = move-exception
            r0 = r1
            r1 = r2
        L34:
            r1.printStackTrace()     // Catch: java.lang.Throwable -> L3b
            if (r0 == 0) goto L3a
            goto L27
        L3a:
            return
        L3b:
            r1 = move-exception
        L3c:
            if (r0 == 0) goto L41
            r0.close()     // Catch: java.lang.Exception -> L41
        L41:
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.control.device.settings.MexicAmco.HdcpVerifyService.writeSysfs(java.lang.String, java.lang.String):void");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isRunningActivity(String str) {
        ComponentName componentName;
        List<ActivityManager.RunningTaskInfo> runningTasks = ((ActivityManager) getSystemService("activity")).getRunningTasks(1);
        return (runningTasks.size() == 0 || (componentName = runningTasks.get(0).topActivity) == null || (!componentName.getPackageName().equals(str) && !componentName.toString().contains(str))) ? false : true;
    }

    private Display.Mode checkUserPreferredMode(Display.Mode[] modeArr, Display.Mode mode) {
        for (Display.Mode mode2 : modeArr) {
            if (mode2.matches(mode.getPhysicalWidth(), mode.getPhysicalHeight(), mode.getRefreshRate())) {
                return mode2;
            }
        }
        throw new IllegalArgumentException("Unrecognized user preferred mode, invalid mode!!");
    }

    private boolean checkSysCurrentMode(Display.Mode mode, Display.Mode mode2) {
        Log.d("[TvSettings]", "sysMode: " + mode + " userSetMode: " + mode2);
        return mode.matches(mode2.getPhysicalWidth(), mode2.getPhysicalHeight(), mode2.getRefreshRate());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onNetflixForeground() {
        Log.i("[TvSettings]", "[HdcpVerifyService]onNetflixForeground");
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(3);
        this.mHandler.removeMessages(4);
        this.mHandler.removeCallbacks(this.mFailedActivityTipsRunnable);
        if (this.mLastHdcpCheckStatus == 0) {
            Log.i("[TvSettings]", "[HdcpVerifyService]onNetflixForeground, status failed to normal");
            this.mLastHdcpCheckStatus = 2;
            syncTvAspectratioFromSettings();
            if (mVerifyActivity != null) {
                Log.i("[TvSettings]", "[HdcpVerifyService]onNetflixForeground, VerifyActivity is not null");
                if (!mVerifyActivity.isFinishing()) {
                    Log.i("[TvSettings]", "[HdcpVerifyService]onNetflixForeground, finish VerifyActivity");
                    mVerifyActivity.finish();
                }
                mVerifyActivity = null;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onNetflixBackground() {
        Log.i("[TvSettings]", "[HdcpVerifyService]onNetflixBackground, last hdcp check status " + this.mLastHdcpCheckStatus);
        if (this.mLastHdcpCheckStatus == 2) {
            Log.i("[TvSettings]", "[HdcpVerifyService]onNetflixBackground, need to check");
            this.mHandler.sendEmptyMessage(2);
        }
    }

    private void syncTvAspectratioFromSettings() {
        String string = Settings.Global.getString(getApplicationContext().getContentResolver(), "tvAspectratio");
        Log.i("[TvSettings]", "[HdcpVerifyService]syncSysClassFromSettings, tvAspectratio=" + string);
        if (string == null) {
            string = "";
        }
        switch (string) {
            case "1:full stretch":
                writeSysfs("1", "/sys/class/video/screen_mode");
                break;
            case "8:4-3 pan scan":
                writeSysfs("8", "/sys/class/video/screen_mode");
                break;
            case "7:4-3 letter box":
                writeSysfs("7", "/sys/class/video/screen_mode");
                break;
            case "10:16-9 ignore":
                writeSysfs("10", "/sys/class/video/screen_mode");
                break;
            case "3:16-9":
                writeSysfs("3", "/sys/class/video/screen_mode");
                break;
            default:
                writeSysfs("1", "/sys/class/video/screen_mode");
                break;
        }
    }
}
