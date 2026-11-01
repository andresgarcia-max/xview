package com.control.device.util;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Pair;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class OpsInitUtil {
    private static OpsInitUtil mInstance;
    private final AppOpsManager mAppOpsManager;
    private final PackageManager mPackageManager;
    private List<Pair<String, Integer>> opsList = new ArrayList();
    private final DebugUtil log = new DebugUtil("OpsInitUtil");

    public static OpsInitUtil getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new OpsInitUtil(context);
        }
        return mInstance;
    }

    public OpsInitUtil(Context context) {
        this.mAppOpsManager = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        this.mPackageManager = context.getPackageManager();
        initVal();
    }

    private void initVal() {
        String str = MarketUtil.MARKET_OPERATOR;
        str.hashCode();
        switch (str) {
            case "3501 3":
                this.opsList.add(new Pair<>("com.dotscreen.nordnet.tv", 24));
                break;
            case "4301 5":
                this.opsList.add(new Pair<>("com.dotscreen.megacable.updater", 66));
                this.opsList.add(new Pair<>("com.dotscreen.megacable.portal", 66));
                this.opsList.add(new Pair<>("com.dotscreen.megacable.livetv", 66));
                this.opsList.add(new Pair<>("com.dotscreen.megacable.core", 66));
                this.opsList.add(new Pair<>("com.dotscreen.megacable.core", 24));
                this.opsList.add(new Pair<>("com.dotscreen.megacable.portal", 24));
                this.opsList.add(new Pair<>("com.zte.ac", 24));
                this.opsList.add(new Pair<>("tv.broadpeak.nanocdnwrapper", 66));
                break;
            case "7001 1":
                this.opsList.add(new Pair<>("com.ita.livetv.tsd", 61));
                break;
        }
    }

    public void initOpsPerm() {
        this.log.v("start init perms for : " + MarketUtil.MARKET_OPERATOR);
        for (Pair<String, Integer> pair : this.opsList) {
            try {
                this.mAppOpsManager.setMode(((Integer) pair.second).intValue(), this.mPackageManager.getApplicationInfo((String) pair.first, 128).uid, (String) pair.first, 0);
                this.log.d(((String) pair.first) + " set done ! ");
            } catch (PackageManager.NameNotFoundException e) {
                this.log.v("A package not found");
                this.log.d(e.toString());
            }
        }
    }
}
