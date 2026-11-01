package com.android.tv.settings.util;

import android.content.Context;
import android.view.accessibility.AccessibilityManager;

/* loaded from: classes.dex */
public class AccessibilityHelper {
    public static boolean forceFocusableViews(Context context) {
        return ((AccessibilityManager) context.getSystemService("accessibility")).isEnabled();
    }
}
