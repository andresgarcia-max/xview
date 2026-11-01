package com.zte.android.productcfg;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.zte.android.productcfg.mcspcfg;

/* loaded from: classes.dex */
public class ZteCfgService {
    private static final String TAG = "ZteCfgService";
    private ServiceConnection serviceConnection = new ServiceConnection() { // from class: com.zte.android.productcfg.ZteCfgService.1
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(ZteCfgService.TAG, "onServiceConnected ");
            mcspcfg unused = ZteCfgService.mcspcfgService = mcspcfg.Stub.asInterface(service);
            Intent intent = new Intent();
            intent.setPackage("com.zte.android.productcfg");
            intent.setAction(ZteCfgService.ZteCfgService_Connected);
            ZteCfgService.mContext.sendBroadcast(intent);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            Log.d(ZteCfgService.TAG, "onServiceDisconnected ");
            mcspcfg unused = ZteCfgService.mcspcfgService = null;
        }
    };
    private static mcspcfg mcspcfgService = null;
    private static Context mContext = null;
    private static boolean isProductSeparate = false;
    private static String ZteCfgService_Connected = "android.intent.action.ZteCfgService_Connected";

    public ZteCfgService(Context context) {
        Log.d(TAG, "ZteCfgService...");
        mContext = context;
        if (mContext != null) {
            Log.d(TAG, "ZteCfgService,mContext != null");
            Intent intent = new Intent();
            intent.setAction("com.zte.android.productcfg.service");
            intent.setPackage("com.zte.android.productcfg");
            boolean ret = mContext.bindService(intent, this.serviceConnection, 1);
            if (ret) {
                isProductSeparate = true;
                Log.d(TAG, "bindService ok,isProductSeparate =" + isProductSeparate);
            } else {
                isProductSeparate = false;
                Log.d(TAG, "bindService failed,isProductSeparate=" + isProductSeparate);
            }
        }
    }

    public int setConfig(String section, String key, String value) {
        Log.d(TAG, "setConfig section=" + section + ",key=" + key + ",value =" + value);
        if (isProductSeparate) {
            Log.d(TAG, "setConfig isProductSeparate=true");
            if (isProductCfg(section, key)) {
                Log.d(TAG, "setConfig isProductCfg is true");
                int ret = setProductConfig(section, key, value);
                return ret;
            }
            Log.d(TAG, "setConfig isProductCfg is false");
            int ret2 = setMiddleWareConfig(section, key, value);
            return ret2;
        }
        Log.d(TAG, "setConfig isProductSeparate=false,setMiddleWareConfig");
        int ret3 = setMiddleWareConfig(section, key, value);
        return ret3;
    }

    private int setProductConfig(String section, String key, String value) {
        Log.d(TAG, "setProductConfig section=" + section + ",key=" + key + ",value =" + value);
        int ret = -1;
        if (mcspcfgService == null) {
            return -1;
        }
        try {
            ret = mcspcfgService.setConfig(section, key, value);
            Log.d(TAG, "setProductConfig ret=" + ret);
            return ret;
        } catch (RemoteException e) {
            e.printStackTrace();
            return ret;
        }
    }

    private int setMiddleWareConfig(String section, String key, String value) {
        Log.d(TAG, "setMiddleWareConfig section=" + section + ",key=" + key + ",value =" + value);
        Uri uri = Uri.parse("content://com.ztestb.basecomp.provider/cfginfo/" + section + "/" + key);
        ContentValues values = new ContentValues();
        values.put("value", value);
        if (mContext == null) {
            return -1;
        }
        int ret = mContext.getContentResolver().update(uri, values, null, null);
        Log.d(TAG, "setMiddleWareConfig update ret=" + ret);
        return ret;
    }

    public String getConfig(String section, String key) {
        Log.d(TAG, "getConfig section=" + section + ",key=");
        if (isProductSeparate) {
            Log.d(TAG, "getConfig isProductSeparate=true");
            if (isProductCfg(section, key)) {
                Log.d(TAG, "getConfig isProductCfg is true");
                String ret = getProductConfig(section, key);
                return ret;
            }
            Log.d(TAG, "getConfig isProductCfg is false");
            String ret2 = getMiddleWareConfig(section, key);
            return ret2;
        }
        Log.d(TAG, "setConfig isProductSeparate=false,getMiddleWareConfig");
        String ret3 = getMiddleWareConfig(section, key);
        return ret3;
    }

    private String getProductConfig(String section, String key) {
        Log.d(TAG, "getProductConfig section=" + section + ",key=" + key);
        String ret = "";
        if (mcspcfgService == null) {
            return "";
        }
        try {
            ret = mcspcfgService.getConfig(section, key);
            Log.d(TAG, "mcspcfgService.getConfig ret =" + ret);
            return ret;
        } catch (RemoteException e) {
            e.printStackTrace();
            return ret;
        }
    }

    private String getMiddleWareConfig(String section, String key) {
        Cursor cursor;
        Log.d(TAG, "getMiddleWareConfig section=" + section + ",key=" + key);
        String retValue = "";
        Uri uri = Uri.parse("content://com.ztestb.basecomp.provider/cfginfo/" + section + "/" + key);
        if (mContext != null && (cursor = mContext.getContentResolver().query(uri, null, null, null, null)) != null) {
            if (cursor.getCount() == 0) {
                Log.d(TAG, "query failed, can not query anything!");
            }
            while (true) {
                if (!cursor.moveToNext()) {
                    break;
                }
                String tmpSection = cursor.getString(cursor.getColumnIndex("section"));
                String tmpName = cursor.getString(cursor.getColumnIndex("name"));
                String tmpValue = cursor.getString(cursor.getColumnIndex("value"));
                Log.i(TAG, "query result==> [" + tmpSection + "]" + tmpName + "=" + tmpValue);
                if (tmpSection.equals(section) && tmpName.equals(key)) {
                    retValue = tmpValue;
                    break;
                }
            }
            cursor.close();
        }
        return retValue;
    }

    public boolean isProductCfg(String section, String key) {
        Log.d(TAG, "isProductCfg section=" + section + ",key=" + key);
        boolean ret = false;
        if (mcspcfgService == null) {
            return false;
        }
        try {
            ret = mcspcfgService.isProductCfg(section, key);
            Log.d(TAG, "mcspcfgService.isProductCfg ret =" + ret);
            return ret;
        } catch (RemoteException e) {
            e.printStackTrace();
            return ret;
        }
    }

    public int cfgToFlash() {
        Log.d(TAG, "isProductCfg begin");
        int ret = -1;
        if (mcspcfgService == null) {
            return -1;
        }
        try {
            ret = mcspcfgService.cfgToFlash();
            Log.d(TAG, "mcspcfgService.cfgToFlash ret =" + ret);
            return ret;
        } catch (RemoteException e) {
            e.printStackTrace();
            return ret;
        }
    }

    public int productCfgRegisterKey(String section, String key, String PackageName) {
        Log.d(TAG, "productCfgRegisterKey section=" + section + ",key=" + key + ",PackageName=" + PackageName);
        int ret = -1;
        if (mcspcfgService != null) {
            try {
                Log.d(TAG, "mcspcfgService.productCfgRegisterKey ret_1 =-1");
                ret = mcspcfgService.productCfgRegisterKey(section, key, PackageName);
                Log.d(TAG, "mcspcfgService.productCfgRegisterKey ret =" + ret);
                return ret;
            } catch (RemoteException e) {
                Log.d(TAG, "mcspcfgService.productCfgRegisterKey failed=" + ret);
                e.printStackTrace();
                return ret;
            }
        }
        Log.d(TAG, "mcspcfgService.productCfgRegisterKey ret_1   mcspcfgService =null");
        return -1;
    }

    public int productCfgUnRegisterKey(String section, String key, String PackageName) {
        Log.d(TAG, "productCfgRegisterKey section=" + section + ",key=" + key + ",PackageName=" + PackageName);
        int ret = -1;
        if (mcspcfgService != null) {
            try {
                ret = mcspcfgService.productCfgUnRegisterKey(section, key, PackageName);
                Log.d(TAG, "mcspcfgService.productCfgUnRegisterKey ret =" + ret);
                return ret;
            } catch (RemoteException e) {
                e.printStackTrace();
                return ret;
            }
        }
        Log.d(TAG, "mcspcfgService.productCfgUnRegisterKey mcspcfgService =null");
        return -1;
    }

    public boolean isProductSeparate() {
        try {
            boolean ret = isProductSeparate;
            return ret;
        } catch (Exception e) {
            Log.d(TAG, "[isProductSeparate] failed return, exception:" + e);
            return false;
        }
    }

    public int ProductCfgReset() {
        Log.d(TAG, "ProductCfgReset() ");
        int ret = -1;
        if (mcspcfgService != null) {
            try {
                ret = mcspcfgService.ProductCfgReset();
                Log.d(TAG, "mcspcfgService.ProductCfgReset() ret =" + ret);
                return ret;
            } catch (RemoteException e) {
                e.printStackTrace();
                return ret;
            }
        }
        Log.d(TAG, "mcspcfgService.ProductCfgReset() mcspcfgService =null");
        return -1;
    }

    public String getConfig_distinguishNullCfg(String section, String key) {
        String ret = "";
        if (mcspcfgService != null) {
            try {
                ret = mcspcfgService.getConfig_distinguishNullCfg(section, key);
                Log.d(TAG, "mcspcfgService.getConfig_distinguishNullCfg ret =" + ret);
                return ret;
            } catch (RemoteException e) {
                e.printStackTrace();
                return ret;
            }
        }
        Log.d(TAG, "getConfig_distinguishNullCfg mcspcfgService =null");
        return "";
    }
}
