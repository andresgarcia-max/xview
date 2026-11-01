package com.android.tv.twopanelsettings.slices;

import android.util.Log;

/* loaded from: classes.dex */
final class InstrumentationUtils {
    static void logPageFocused(int i, Boolean bool) {
        int i2;
        if (bool != null) {
            try {
                i2 = bool.booleanValue() ? 4 : 5;
            } catch (Exception e) {
                Log.e("InstrumentationUtils", "Unable to log PAGE_FOCUSED for id: " + i + " " + e);
                return;
            }
        } else {
            i2 = 1;
        }
        TvSettingsStatsLog.write(261, i2, i);
    }

    static void logEntrySelected(int i) {
        try {
            TvSettingsStatsLog.write(261, 2, i);
        } catch (Exception e) {
            Log.e("InstrumentationUtils", "Unable to log ENTRY_SELECTED for id: " + i + " " + e);
        }
    }

    static void logToggleInteracted(int i, Boolean bool) {
        int i2;
        if (bool != null) {
            try {
                i2 = bool.booleanValue() ? 6 : 7;
            } catch (Exception e) {
                Log.e("InstrumentationUtils", "Unable to log TOGGLE_INTERACTED for id: " + i + " " + e);
                return;
            }
        } else {
            i2 = 3;
        }
        TvSettingsStatsLog.write(261, i2, i);
    }
}
