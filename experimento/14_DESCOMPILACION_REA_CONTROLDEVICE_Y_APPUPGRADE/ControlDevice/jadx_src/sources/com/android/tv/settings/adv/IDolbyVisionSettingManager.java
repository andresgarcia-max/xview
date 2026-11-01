package com.android.tv.settings.adv;

/* loaded from: classes.dex */
public interface IDolbyVisionSettingManager {
    int getDolbyVisionType();

    boolean isDolbyVisionEnable();

    String isTvSupportDolbyVision();

    long resolveResolutionValue(String str);
}
