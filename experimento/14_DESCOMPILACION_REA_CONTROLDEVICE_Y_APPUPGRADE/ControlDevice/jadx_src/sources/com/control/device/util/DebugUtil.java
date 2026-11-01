package com.control.device.util;

import android.os.SystemProperties;
import android.util.Log;

/* loaded from: classes.dex */
public class DebugUtil {
    public static final Boolean DEBUG;
    private final String TAG;

    static {
        DEBUG = Boolean.valueOf(SystemProperties.getBoolean("persist.sys.settings.debug", false) || "userdebug".equals(SystemProperties.get("ro.build.type")));
    }

    public DebugUtil(String str) {
        this.TAG = str;
    }

    public void d(String str) {
        if (DEBUG.booleanValue()) {
            Log.d(this.TAG, str);
        }
    }

    public static void dTAGForClassName(String str) {
        if (DEBUG.booleanValue()) {
            Log.d(getCallerClassName(), str);
        }
    }

    public void v(String str) {
        Log.v(this.TAG, str);
    }

    public static String getCallerClassName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        return (stackTrace.length < 3 || stackTrace[3] == null) ? "UNKNOWN-CLASS" : stackTrace[3].getClassName();
    }
}
