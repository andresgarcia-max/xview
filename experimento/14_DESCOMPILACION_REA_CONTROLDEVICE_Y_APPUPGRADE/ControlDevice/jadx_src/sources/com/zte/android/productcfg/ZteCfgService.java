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
    private static String ZteCfgService_Connected = "android.intent.action.ZteCfgService_Connected";
    private static boolean isProductSeparate = false;
    private static Context mContext;
    private static mcspcfg mcspcfgService;
    private ServiceConnection serviceConnection = new ServiceConnection() { // from class: com.zte.android.productcfg.ZteCfgService.1
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d("ZteCfgService", "onServiceConnected ");
            mcspcfg unused = ZteCfgService.mcspcfgService = mcspcfg.Stub.asInterface(iBinder);
            Intent intent = new Intent();
            intent.setAction(ZteCfgService.ZteCfgService_Connected);
            ZteCfgService.mContext.sendBroadcast(intent);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("ZteCfgService", "onServiceDisconnected ");
            mcspcfg unused = ZteCfgService.mcspcfgService = null;
        }
    };

    public ZteCfgService(Context context) {
        Log.d("ZteCfgService", "ZteCfgService...");
        mContext = context;
        if (context != null) {
            Log.d("ZteCfgService", "ZteCfgService,mContext != null");
            Intent intent = new Intent();
            intent.setAction("com.zte.android.productcfg.service");
            intent.setPackage("com.zte.android.productcfg");
            if (mContext.bindService(intent, this.serviceConnection, 1)) {
                isProductSeparate = true;
                Log.d("ZteCfgService", "bindService ok,isProductSeparate =" + isProductSeparate);
                return;
            }
            isProductSeparate = false;
            Log.d("ZteCfgService", "bindService failed,isProductSeparate=" + isProductSeparate);
        }
    }

    public int setConfig(String str, String str2, String str3) {
        Log.d("ZteCfgService", "setConfig section=" + str + ",key=" + str2 + ",value =" + str3);
        if (isProductSeparate) {
            Log.d("ZteCfgService", "setConfig isProductSeparate=true");
            if (isProductCfg(str, str2)) {
                Log.d("ZteCfgService", "setConfig isProductCfg is true");
                return setProductConfig(str, str2, str3);
            }
            Log.d("ZteCfgService", "setConfig isProductCfg is false");
            return setMiddleWareConfig(str, str2, str3);
        }
        Log.d("ZteCfgService", "setConfig isProductSeparate=false,setMiddleWareConfig");
        return setMiddleWareConfig(str, str2, str3);
    }

    private int setProductConfig(String str, String str2, String str3) {
        Log.d("ZteCfgService", "setProductConfig section=" + str + ",key=" + str2 + ",value =" + str3);
        mcspcfg mcspcfgVar = mcspcfgService;
        int i = -1;
        if (mcspcfgVar == null) {
            return -1;
        }
        try {
            i = mcspcfgVar.setConfig(str, str2, str3);
            Log.d("ZteCfgService", "setProductConfig ret=" + i);
            return i;
        } catch (RemoteException e) {
            e.printStackTrace();
            return i;
        }
    }

    private int setMiddleWareConfig(String str, String str2, String str3) {
        Log.d("ZteCfgService", "setMiddleWareConfig section=" + str + ",key=" + str2 + ",value =" + str3);
        StringBuilder sb = new StringBuilder();
        sb.append("content://com.ztestb.basecomp.provider/cfginfo/");
        sb.append(str);
        sb.append("/");
        sb.append(str2);
        Uri parse = Uri.parse(sb.toString());
        ContentValues contentValues = new ContentValues();
        contentValues.put("value", str3);
        Context context = mContext;
        if (context == null) {
            return -1;
        }
        int update = context.getContentResolver().update(parse, contentValues, null, null);
        Log.d("ZteCfgService", "setMiddleWareConfig update ret=" + update);
        return update;
    }

    public String getConfig(String str, String str2) {
        Log.d("ZteCfgService", "getConfig section=" + str + ",key=");
        if (isProductSeparate) {
            Log.d("ZteCfgService", "getConfig isProductSeparate=true");
            if (isProductCfg(str, str2)) {
                Log.d("ZteCfgService", "getConfig isProductCfg is true");
                return getProductConfig(str, str2);
            }
            Log.d("ZteCfgService", "getConfig isProductCfg is false");
            return getMiddleWareConfig(str, str2);
        }
        Log.d("ZteCfgService", "setConfig isProductSeparate=false,getMiddleWareConfig");
        return getMiddleWareConfig(str, str2);
    }

    private String getProductConfig(String str, String str2) {
        Log.d("ZteCfgService", "getProductConfig section=" + str + ",key=" + str2);
        mcspcfg mcspcfgVar = mcspcfgService;
        String str3 = "";
        if (mcspcfgVar == null) {
            return "";
        }
        try {
            str3 = mcspcfgVar.getConfig(str, str2);
            Log.d("ZteCfgService", "mcspcfgService.getConfig ret =" + str3);
            return str3;
        } catch (RemoteException e) {
            e.printStackTrace();
            return str3;
        }
    }

    private String getMiddleWareConfig(String str, String str2) {
        Cursor query;
        Log.d("ZteCfgService", "getMiddleWareConfig section=" + str + ",key=" + str2);
        Uri parse = Uri.parse("content://com.ztestb.basecomp.provider/cfginfo/" + str + "/" + str2);
        Context context = mContext;
        String str3 = "";
        if (context != null && (query = context.getContentResolver().query(parse, null, null, null, null)) != null) {
            if (query.getCount() == 0) {
                Log.d("ZteCfgService", "query failed, can not query anything!");
            }
            while (true) {
                if (!query.moveToNext()) {
                    break;
                }
                String string = query.getString(query.getColumnIndex("section"));
                String string2 = query.getString(query.getColumnIndex("name"));
                String string3 = query.getString(query.getColumnIndex("value"));
                Log.i("ZteCfgService", "query result==> [" + string + "]" + string2 + "=" + string3);
                if (string.equals(str) && string2.equals(str2)) {
                    str3 = string3;
                    break;
                }
            }
            query.close();
        }
        return str3;
    }

    public boolean isProductCfg(String str, String str2) {
        Log.d("ZteCfgService", "isProductCfg section=" + str + ",key=" + str2);
        mcspcfg mcspcfgVar = mcspcfgService;
        boolean z = false;
        if (mcspcfgVar == null) {
            return false;
        }
        try {
            z = mcspcfgVar.isProductCfg(str, str2);
            Log.d("ZteCfgService", "mcspcfgService.isProductCfg ret =" + z);
            return z;
        } catch (RemoteException e) {
            e.printStackTrace();
            return z;
        }
    }

    public int cfgToFlash() {
        Log.d("ZteCfgService", "isProductCfg begin");
        mcspcfg mcspcfgVar = mcspcfgService;
        int i = -1;
        if (mcspcfgVar == null) {
            return -1;
        }
        try {
            i = mcspcfgVar.cfgToFlash();
            Log.d("ZteCfgService", "mcspcfgService.cfgToFlash ret =" + i);
            return i;
        } catch (RemoteException e) {
            e.printStackTrace();
            return i;
        }
    }

    public boolean isProductSeparate() {
        try {
            return isProductSeparate;
        } catch (Exception e) {
            Log.d("ZteCfgService", "[isProductSeparate] failed return, exception:" + e);
            return false;
        }
    }

    public int ProductCfgReset() {
        Log.d("ZteCfgService", "ProductCfgReset() ");
        mcspcfg mcspcfgVar = mcspcfgService;
        int i = -1;
        if (mcspcfgVar != null) {
            try {
                i = mcspcfgVar.ProductCfgReset();
                Log.d("ZteCfgService", "mcspcfgService.ProductCfgReset() ret =" + i);
                return i;
            } catch (RemoteException e) {
                e.printStackTrace();
                return i;
            }
        }
        Log.d("ZteCfgService", "mcspcfgService.ProductCfgReset() mcspcfgService =null");
        return -1;
    }
}
