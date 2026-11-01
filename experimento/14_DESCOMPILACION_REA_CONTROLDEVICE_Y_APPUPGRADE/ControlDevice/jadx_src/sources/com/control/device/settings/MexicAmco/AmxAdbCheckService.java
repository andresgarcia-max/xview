package com.control.device.settings.MexicAmco;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import com.control.device.settings.adbsettings.AdbAuthCheckUtil_amx;

/* loaded from: classes.dex */
public class AmxAdbCheckService extends Service {
    private String CpuName = SystemProperties.get("ro.product.cpu.name");
    private String mBuildType = SystemProperties.get("ro.build.type");
    private Handler mHandler = new Handler(Looper.getMainLooper()) { // from class: com.control.device.settings.MexicAmco.AmxAdbCheckService.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what == 1) {
                AmxAdbCheckService.this.checkAdb();
                AmxAdbCheckService.this.mHandler.removeMessages(1);
                AmxAdbCheckService.this.mHandler.sendEmptyMessageDelayed(1, 900000L);
            } else {
                Log.d("[AmxAdbCheckService]", "unknown msg, msg.what=" + message.what);
            }
        }
    };

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public void onCreate() {
        Log.d("[AmxAdbCheckService]", "onCreate");
        super.onCreate();
        this.mHandler.sendEmptyMessageDelayed(1, 30000L);
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        if (isCheckSystem()) {
            return 1;
        }
        Log.d("[AmxAdbCheckService]", "onStartCommand, not user, no need to check");
        this.mHandler.removeCallbacksAndMessages(null);
        stopSelf();
        return 1;
    }

    private boolean isCheckSystem() {
        return "user".equals(this.mBuildType);
    }

    @Override // android.app.Service
    public void onDestroy() {
        Log.d("[AmxAdbCheckService]", "onDestroy");
        this.mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkAdb() {
        int i = Settings.Secure.getInt(getApplicationContext().getContentResolver(), "user_setup_complete", 0);
        if (i != 1) {
            Log.d("[AmxAdbCheckService]", "checkAdb, isSetup=" + i + ", no need to check");
            return;
        }
        if (AdbAuthCheckUtil_amx.getQRAvailableNum() > 0) {
            long qRFirstCheckTime = AdbAuthCheckUtil_amx.getQRFirstCheckTime();
            long currentTimeMillis = System.currentTimeMillis();
            Log.d("[AmxAdbCheckService]", "checkAdb, checkTime=" + qRFirstCheckTime + ", curTime=" + currentTimeMillis);
            if (qRFirstCheckTime <= 0 || currentTimeMillis - qRFirstCheckTime <= 1209600000) {
                if (qRFirstCheckTime <= 0) {
                    Log.d("[AmxAdbCheckService]", "checkAdb, checked, but time is null, set to current time");
                    AdbAuthCheckUtil_amx.setQRFirstCheckTime(currentTimeMillis);
                    return;
                }
                return;
            }
            Log.d("[AmxAdbCheckService]", "checkAdb, set num to 0, close adb");
            AdbAuthCheckUtil_amx.setQRAvailableNum(0);
            Settings.Global.putInt(getApplicationContext().getContentResolver(), "development_settings_enabled", 0);
            Settings.Global.putInt(getApplicationContext().getContentResolver(), "adb_enabled", 0);
        }
    }
}
