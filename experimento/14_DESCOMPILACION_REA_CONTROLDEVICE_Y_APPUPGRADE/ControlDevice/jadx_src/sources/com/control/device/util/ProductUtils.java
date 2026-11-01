package com.control.device.util;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.zte.android.productcfg.ZteMcspCfgManager;

/* loaded from: classes.dex */
public class ProductUtils {
    public static final String PRODUCT_MODEL = Build.MODEL;
    public static final String PRODUCT_WIFI = SystemProperties.get("ro.vendor.product.wifi");
    public static final String HARDWARE_INFO = SystemProperties.get("ro.product.info.hardware");
    private static Context mContext = null;

    public static void initProductUtilEnv(Context context) {
        if (mContext == null) {
            mContext = context;
        }
    }

    public static boolean isRTL8852BS() {
        return "RTL8852BS".equalsIgnoreCase(PRODUCT_WIFI);
    }

    public static boolean isB866V2FAS() {
        Context context = mContext;
        if (context == null) {
            Log.e("ProductUtils", " <isB866V2FAS> The context used by this method has not been initialized yet!");
            return false;
        }
        String ReadStrFromCfgmanager = ZteMcspCfgManager.getInstance(context).ReadStrFromCfgmanager("Common_ext", "Displaymodel", "");
        if (TextUtils.isEmpty(ReadStrFromCfgmanager)) {
            return "B866V2FAS".equalsIgnoreCase(PRODUCT_MODEL);
        }
        return "B866V2FAS".equalsIgnoreCase(ReadStrFromCfgmanager);
    }

    public static boolean isB866V5W11() {
        String str = HARDWARE_INFO;
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return str.contains("STBALC");
    }

    public static boolean isGTV(Context context) {
        return isPackageExist(context, "com.google.android.apps.tv.launcherx");
    }

    public static boolean isPackageExist(Context context, String str) {
        try {
            context.getPackageManager().getPackageInfo(str, 1);
            return true;
        } catch (Exception unused) {
            return false;
        }
    }
}
