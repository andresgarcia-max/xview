package tv.upgrade.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import com.zte.android.productcfg.ZteMcspCfgManager;

/* loaded from: classes.dex */
public class ResetService extends Service {
    private static final String TAG = "ResetService";
    private ZteMcspCfgManager mZteMcspCfgManager;

    @Override // android.app.Service
    public void onCreate() {
        Log.d(TAG, "PowerService onCreate");
        super.onCreate();
        Log.d(TAG, "<ResetService> ZteMcspCfgManager.getInstance");
        try {
            this.mZteMcspCfgManager = ZteMcspCfgManager.getInstance(getApplicationContext());
        } catch (Exception e) {
            Log.d(TAG, "<ResetService> Exception = " + e);
        }
        new Handler().postDelayed(new Runnable() { // from class: tv.upgrade.service.ResetService.1
            @Override // java.lang.Runnable
            public void run() {
                if (ResetService.this.mZteMcspCfgManager != null) {
                    Log.d(ResetService.TAG, "<ResetService> mZteMcspCfgManager.isProductSeparate() = " + ResetService.this.mZteMcspCfgManager.isProductSeparate());
                    int Ret = -1;
                    if (ResetService.this.mZteMcspCfgManager.isProductSeparate()) {
                        Ret = ResetService.this.mZteMcspCfgManager.ProductCfgReset();
                    }
                    Log.d(ResetService.TAG, "<ResetService> mZteMcspCfgManager.isProductSeparate()   Ret = " + Ret);
                }
                Intent resetIntent = new Intent("android.intent.action.FACTORY_RESET");
                resetIntent.setPackage("android");
                resetIntent.setFlags(268435456);
                resetIntent.putExtra("android.intent.extra.REASON", "ResetButon");
                ResetService.this.sendBroadcast(resetIntent);
            }
        }, 1000L);
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }
}
