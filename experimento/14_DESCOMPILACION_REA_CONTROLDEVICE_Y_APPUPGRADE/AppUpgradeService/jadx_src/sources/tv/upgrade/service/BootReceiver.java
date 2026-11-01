package tv.upgrade.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.Log;
import com.zte.android.productcfg.ZteMcspCfgManager;

/* loaded from: classes.dex */
public class BootReceiver extends BroadcastReceiver {
    public static final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";
    private static final String FILE = "[BootReceiver]";
    private static final String TAG = "[AppUpgradeService]";
    private ZteMcspCfgManager mZteMcspCfgManager;
    private String MarketCode_OperatorCode = SystemProperties.get("config.MarketCode") + " " + SystemProperties.get("config.OperatorCode");
    private final String ThailandAIS = "4701 2";
    private final String Indonesia_Telkom = "3601 1";
    private final String Viettel = "1301 2";
    private boolean startSignal = false;

    /* JADX WARN: Type inference failed for: r2v9, types: [tv.upgrade.service.BootReceiver$1] */
    @Override // android.content.BroadcastReceiver
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "[BootReceiver]<onReceive> action = " + action);
        if (BOOT_ACTION.equals(action)) {
            switch (this.MarketCode_OperatorCode) {
                case "4701 2":
                case "3601 1":
                case "1301 2":
                    this.startSignal = true;
                    break;
            }
            try {
                this.mZteMcspCfgManager = ZteMcspCfgManager.getInstance(context.getApplicationContext());
            } catch (Exception e) {
                Log.d(TAG, "[BootReceiver]<startUpdate> Exception = " + e);
            }
            new Thread() { // from class: tv.upgrade.service.BootReceiver.1
                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException e2) {
                    }
                    String valueCustomization = BootReceiver.this.mZteMcspCfgManager.ReadStrFromCfgmanager("Android", "XtsAuto", "");
                    if (!BootReceiver.this.startSignal) {
                        String[] value = valueCustomization.split("#");
                        if (value.length >= 4 && "1".equals(value[0]) && value[1].length() > 0 && value[2].length() > 0 && value[3].length() > 0) {
                            BootReceiver.this.startSignal = true;
                        }
                    }
                    Log.d(BootReceiver.TAG, "[BootReceiver]<onReceive> startSignal: " + BootReceiver.this.startSignal);
                    if (BootReceiver.this.startSignal) {
                        try {
                            Log.d(BootReceiver.TAG, "[BootReceiver]<onReceive> start AppUpgradeService !!!");
                            Intent startAppUpgrade = new Intent(context, (Class<?>) AppUpgradeService.class);
                            context.startService(startAppUpgrade);
                        } catch (Exception e3) {
                            Log.d(BootReceiver.TAG, "[BootReceiver]<onReceive> Exception = " + e3);
                        }
                    }
                }
            }.start();
        }
    }
}
