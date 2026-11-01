package com.android.tv.settings.adv.impl;

import android.content.Context;
import com.android.tv.settings.adv.IDolbyVisionSettingManager;

/* loaded from: classes.dex */
public class DolbyVisionSettingManager implements IDolbyVisionSettingManager {
    com.droidlogic.app.DolbyVisionSettingManager mImpl;

    public DolbyVisionSettingManager(Context context) {
        this.mImpl = new com.droidlogic.app.DolbyVisionSettingManager(context);
    }

    @Override // com.android.tv.settings.adv.IDolbyVisionSettingManager
    public String isTvSupportDolbyVision() {
        return this.mImpl.isTvSupportDolbyVision();
    }

    @Override // com.android.tv.settings.adv.IDolbyVisionSettingManager
    public boolean isDolbyVisionEnable() {
        return this.mImpl.isDolbyVisionEnable();
    }

    @Override // com.android.tv.settings.adv.IDolbyVisionSettingManager
    public int getDolbyVisionType() {
        return this.mImpl.getDolbyVisionType();
    }

    @Override // com.android.tv.settings.adv.IDolbyVisionSettingManager
    public long resolveResolutionValue(String str) {
        return this.mImpl.resolveResolutionValue(str);
    }
}
