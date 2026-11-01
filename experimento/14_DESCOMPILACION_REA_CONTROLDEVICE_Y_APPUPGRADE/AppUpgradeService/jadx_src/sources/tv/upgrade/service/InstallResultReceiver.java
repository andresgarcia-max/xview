package tv.upgrade.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import java.io.File;

/* loaded from: classes.dex */
public class InstallResultReceiver extends BroadcastReceiver {
    private static final String AUTO_INSTALL_SUCCESS = "1";
    private static final String FILE = "[InstallResultReceiver]";
    private static final String SETTING_DEFAULT = "settingsAutoTestValue";
    private static final String TAG = "[AppUpgradeService]";
    private String MarketCode_OperatorCode = SystemProperties.get("config.MarketCode") + " " + SystemProperties.get("config.OperatorCode");
    final String Viettel_OTT = "1301 2";

    private void retrySetGlobalSettings(Context mContext, String SettingsName, String value) {
        for (int number = 6; !Settings.Global.putString(mContext.getContentResolver(), SettingsName, value) && number > 0; number--) {
        }
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            int status = intent.getIntExtra("android.content.pm.extra.STATUS", 1);
            String packageName = null;
            boolean autoTest = false;
            String filePathName = null;
            try {
                if (intent.getExtras() != null) {
                    packageName = intent.getExtras().getString("packageName");
                    autoTest = intent.getBooleanExtra("autoTest", false);
                    filePathName = intent.getExtras().getString("filePath");
                    Log.d(TAG, "[InstallResultReceiver] packageName: " + packageName + ", autoTest: " + autoTest + " filePath: " + filePathName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Intent finishIntent = new Intent();
            finishIntent.setAction("com.zte.APPUP_INSTALL_FINISH");
            finishIntent.putExtra("packageName", packageName);
            finishIntent.putExtra("status", status);
            context.sendBroadcast(finishIntent);
            if (filePathName != null) {
                Log.d(TAG, "[InstallResultReceiver]InstallResultReceiver, deleteSingleFile! filePathName =" + filePathName);
                deleteSingleFile(filePathName);
            }
            Log.d(TAG, "[InstallResultReceiver] packageName: " + packageName + ", status: " + status);
            if (status == 0) {
                Log.d(TAG, FILE + packageName + " install success, msg: " + intent.getStringExtra("android.content.pm.extra.STATUS_MESSAGE"));
                if ("1301 2".equals(this.MarketCode_OperatorCode)) {
                    try {
                        if ("com.zte.netmanager".equals(packageName)) {
                            Intent mIntent = new Intent();
                            mIntent.addFlags(268435456);
                            mIntent.setComponent(new ComponentName(packageName, packageName + ".NmService"));
                            context.startService(mIntent);
                            Log.d(TAG, "[InstallResultReceiver] netmanager service restart success");
                            return;
                        }
                    } catch (Exception e2) {
                        Log.d(TAG, "[InstallResultReceiver]<InstallResultReceiver> Exception = " + e2);
                    }
                }
                if (autoTest) {
                    retrySetGlobalSettings(context, SETTING_DEFAULT, AUTO_INSTALL_SUCCESS);
                    try {
                        Intent mIntent2 = new Intent();
                        mIntent2.addFlags(268435456);
                        mIntent2.setComponent(new ComponentName(packageName, packageName + ".MainActivity"));
                        Log.d(TAG, "[InstallResultReceiver] packageName: " + packageName + ", activity: " + packageName + ".MainActivity");
                        context.startActivity(mIntent2);
                        return;
                    } catch (Exception e3) {
                        Log.d(TAG, "[InstallResultReceiver]<InstallResultReceiver> Exception = " + e3);
                        return;
                    }
                }
                return;
            }
            Log.d(TAG, FILE + packageName + " install filed, msg: " + intent.getStringExtra("android.content.pm.extra.STATUS_MESSAGE"));
        }
    }

    private boolean deleteSingleFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Log.d(TAG, "[InstallResultReceiver]deleteSingleFile success");
                return true;
            }
            Log.d(TAG, "[InstallResultReceiver]deleteSingleFile false");
            return false;
        }
        Log.d(TAG, "[InstallResultReceiver]deleteSingleFile  flie error false");
        return false;
    }
}
