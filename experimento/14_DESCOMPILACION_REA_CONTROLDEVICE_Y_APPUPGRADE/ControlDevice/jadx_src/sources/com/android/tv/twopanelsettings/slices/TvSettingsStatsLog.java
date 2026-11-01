package com.android.tv.twopanelsettings.slices;

import android.util.StatsEvent;
import android.util.StatsLog;

/* loaded from: classes.dex */
public class TvSettingsStatsLog {
    public static void write(int i, int i2, int i3) {
        StatsEvent.Builder newBuilder = StatsEvent.newBuilder();
        newBuilder.setAtomId(i);
        newBuilder.writeInt(i2);
        newBuilder.writeInt(i3);
        newBuilder.usePooledBuffer();
        StatsLog.write(newBuilder.build());
    }
}
