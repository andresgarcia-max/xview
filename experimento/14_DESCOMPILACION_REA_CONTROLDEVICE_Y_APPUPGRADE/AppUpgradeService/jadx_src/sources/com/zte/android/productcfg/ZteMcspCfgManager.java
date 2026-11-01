package com.zte.android.productcfg;

import android.content.Context;
import android.util.Log;

/* loaded from: classes.dex */
public class ZteMcspCfgManager {
    private static final String FILE = "[ZteMcspCfgManager]";
    private static final String TAG = "[SetupConfigSync]";
    private static Context mContext;
    private static ZteMcspCfgManager mZteMcspCfgManager;
    private static ZteCfgService ztecfgservice = null;
    static String compName = "MCSP_ZteMcspCfgManager";

    private ZteMcspCfgManager() {
    }

    private ZteMcspCfgManager(Context context) {
        mContext = context;
        ztecfgservice = new ZteCfgService(mContext);
    }

    public static ZteMcspCfgManager getInstance(Context context) {
        if (mZteMcspCfgManager != null) {
            return mZteMcspCfgManager;
        }
        mZteMcspCfgManager = new ZteMcspCfgManager(context);
        return mZteMcspCfgManager;
    }

    public String ReadStrFromCfgmanager(String section, String name, String defaultvalue) {
        Log.i(TAG, "[ReadStrFromCfgmanager]:section = " + section + ";name = " + name + ";value = " + defaultvalue);
        String ret = ztecfgservice.getConfig(section, name);
        Log.i(TAG, "[ReadStrFromCfgmanager]:ret = " + ret);
        if (ret.isEmpty()) {
            return defaultvalue;
        }
        return ret;
    }

    public int ReadIntegerFromCfgmanager(String section, String name, int defaultvalue) {
        Log.i(TAG, "[ReadIntegerFromCfgmanager]:section = " + section + ";name = " + name + ";value = " + defaultvalue);
        String str = ztecfgservice.getConfig(section, name);
        Log.i(TAG, "[ReadStrFromCfgmanager]:str = " + str);
        try {
            int ret = Integer.parseInt(str);
            return ret;
        } catch (Exception e) {
            Log.i(TAG, "[ReadStrFromCfgmanager]:e = NumberFormatexception ");
            return defaultvalue;
        }
    }

    public int SetStrToCfgmanager(String section, String name, String value) {
        Log.i(TAG, "[SetStrToCfgmanager]:section = " + section + ";name = " + name + ";value = " + value);
        int ret = ztecfgservice.setConfig(section, name, value);
        Log.i(TAG, "[SetStrToCfgmanager]:ret = " + ret);
        return ret;
    }

    public int SetIntToCfgmanager(String section, String name, int value) {
        Log.i(TAG, "[SetIntToCfgmanager]:section = " + section + ";name = " + name + ";value = " + value);
        String str = "" + value;
        Log.i(TAG, "[SetIntToCfgmanager]:str = " + str);
        int ret = ztecfgservice.setConfig(section, name, str);
        Log.i(TAG, "[SetIntToCfgmanager]:ret = " + ret);
        return ret;
    }

    public int UpdateConfigFromFs(String filepath) {
        return -1;
    }

    public int SaveConfigtoFlash() {
        Log.d(TAG, "[SaveConfigtoFlash] Call SaveConfigtoFlash");
        int iRet = ztecfgservice.cfgToFlash();
        Log.d(TAG, "[SaveConfigtoFlash] Call SaveConfigtoFlash ----   iRet = " + iRet);
        if (-1 == iRet) {
            Log.d(TAG, "[SaveConfigtoFlash] SaveConfigtoFlash failed!");
        }
        return iRet;
    }

    public void RegisterConfigChangleListener(String section, String name, String PackageName) {
        try {
            int iRet = ztecfgservice.productCfgRegisterKey(section, name, PackageName);
            if (-1 == iRet) {
                Log.d(TAG, "[RegisterConfigChangleListener] RegisterConfigChangleListener failed!");
            }
        } catch (Exception e) {
            Log.d(TAG, "[ZteMcspCfgManager][RegisterConfigChangleListener] failed return, exception:" + e);
        }
    }

    public void UnRegisterConfigChangleListener(String section, String name, String PackageName) {
        try {
            int iRet = ztecfgservice.productCfgUnRegisterKey(section, name, PackageName);
            if (-1 == iRet) {
                Log.d(TAG, "[RegisterConfigChangleListener] RegisterConfigChangleListener failed!");
            }
        } catch (Exception e) {
            Log.d(TAG, "[ZteMcspCfgManager][UnRegisterConfigChangleListener] failed return, exception:" + e);
        }
    }

    public boolean isProductSeparate() {
        try {
            boolean ret = ztecfgservice.isProductSeparate();
            return ret;
        } catch (Exception e) {
            Log.d(TAG, "[ZteMcspCfgManager][sProductSeparate] failed return, exception:" + e);
            return false;
        }
    }

    public int ProductCfgReset() {
        Log.d(TAG, "ProductCfgReset() ");
        int ret = -1;
        try {
            ret = ztecfgservice.ProductCfgReset();
            Log.d(TAG, "mProductCfgReset() ret =" + ret);
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return ret;
        }
    }
}
