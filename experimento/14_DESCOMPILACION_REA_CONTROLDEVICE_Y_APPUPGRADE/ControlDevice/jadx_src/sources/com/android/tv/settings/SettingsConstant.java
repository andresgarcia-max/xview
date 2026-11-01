package com.android.tv.settings;

import android.content.Context;
import com.android.tv.settings.adv.impl.SystemControlManager;

/* loaded from: classes.dex */
public class SettingsConstant {
    public static boolean needDroidlogicTvFeature(Context context) {
        return SystemControlManager.getInstance().getPropertyBoolean("ro.vendor.platform.has.tvuimode", false);
    }
}
