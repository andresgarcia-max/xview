package com.zte.android.productcfg;

import android.content.Context;
import android.util.Log;

/* loaded from: classes.dex */
public class ZteMcspCfgManager {
    private static Context mContext;
    private static ZteMcspCfgManager mZteMcspCfgManager;
    private static ZteCfgService ztecfgservice;

    private ZteMcspCfgManager(Context context) {
        mContext = context;
        ztecfgservice = new ZteCfgService(mContext);
    }

    public static ZteMcspCfgManager getInstance(Context context) {
        ZteMcspCfgManager zteMcspCfgManager = mZteMcspCfgManager;
        if (zteMcspCfgManager != null) {
            return zteMcspCfgManager;
        }
        ZteMcspCfgManager zteMcspCfgManager2 = new ZteMcspCfgManager(context);
        mZteMcspCfgManager = zteMcspCfgManager2;
        return zteMcspCfgManager2;
    }

    public String ReadStrFromCfgmanager(String str, String str2, String str3) {
        Log.i("[SetupConfigSync]", "[ReadStrFromCfgmanager]:section = " + str + ";name = " + str2 + ";value = " + str3);
        String config = ztecfgservice.getConfig(str, str2);
        StringBuilder sb = new StringBuilder();
        sb.append("[ReadStrFromCfgmanager]:ret = ");
        sb.append(config);
        Log.i("[SetupConfigSync]", sb.toString());
        return config.isEmpty() ? str3 : config;
    }

    public int ReadIntegerFromCfgmanager(String str, String str2, int i) {
        Log.i("[SetupConfigSync]", "[ReadIntegerFromCfgmanager]:section = " + str + ";name = " + str2 + ";value = " + i);
        String config = ztecfgservice.getConfig(str, str2);
        StringBuilder sb = new StringBuilder();
        sb.append("[ReadStrFromCfgmanager]:str = ");
        sb.append(config);
        Log.i("[SetupConfigSync]", sb.toString());
        try {
            return Integer.parseInt(config);
        } catch (Exception unused) {
            Log.i("[SetupConfigSync]", "[ReadStrFromCfgmanager]:e = NumberFormatexception ");
            return i;
        }
    }

    public int SetStrToCfgmanager(String str, String str2, String str3) {
        Log.i("[SetupConfigSync]", "[SetStrToCfgmanager]:section = " + str + ";name = " + str2 + ";value = " + str3);
        int config = ztecfgservice.setConfig(str, str2, str3);
        StringBuilder sb = new StringBuilder();
        sb.append("[SetStrToCfgmanager]:ret = ");
        sb.append(config);
        Log.i("[SetupConfigSync]", sb.toString());
        return config;
    }

    public int SaveConfigtoFlash() {
        Log.d("[SetupConfigSync]", "[SaveConfigtoFlash] Call SaveConfigtoFlash");
        int cfgToFlash = ztecfgservice.cfgToFlash();
        Log.d("[SetupConfigSync]", "[SaveConfigtoFlash] Call SaveConfigtoFlash ----   iRet = " + cfgToFlash);
        if (-1 == cfgToFlash) {
            Log.d("[SetupConfigSync]", "[SaveConfigtoFlash] SaveConfigtoFlash failed!");
        }
        return cfgToFlash;
    }

    public boolean isProductSeparate() {
        try {
            return ztecfgservice.isProductSeparate();
        } catch (Exception e) {
            Log.d("[SetupConfigSync]", "[ZteMcspCfgManager][sProductSeparate] failed return, exception:" + e);
            return false;
        }
    }

    public int ProductCfgReset() {
        Log.d("[SetupConfigSync]", "ProductCfgReset() ");
        int i = -1;
        try {
            i = ztecfgservice.ProductCfgReset();
            Log.d("[SetupConfigSync]", "mProductCfgReset() ret =" + i);
            return i;
        } catch (Exception e) {
            e.printStackTrace();
            return i;
        }
    }
}
