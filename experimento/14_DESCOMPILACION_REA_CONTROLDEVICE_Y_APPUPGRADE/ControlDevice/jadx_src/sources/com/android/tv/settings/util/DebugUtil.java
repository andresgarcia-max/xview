package com.android.tv.settings.util;

import android.os.SystemProperties;

/* loaded from: classes.dex */
public class DebugUtil {
    public static final Boolean DEBUG;

    static {
        DEBUG = Boolean.valueOf(SystemProperties.getBoolean("persist.sys.settings.debug", false) || "userdebug".equals(SystemProperties.get("ro.build.type")));
    }
}
