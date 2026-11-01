package com.android.tv.settings.adv.impl;

import android.content.Context;
import com.android.tv.settings.adv.IHdmiCecManager;

/* loaded from: classes.dex */
public class HdmiCecManager implements IHdmiCecManager {
    private static com.droidlogic.app.HdmiCecManager mImpl;

    public HdmiCecManager(Context context) {
        mImpl = new com.droidlogic.app.HdmiCecManager(context);
    }

    @Override // com.android.tv.settings.adv.IHdmiCecManager
    public void enableOneTouchPlay(boolean z) {
        mImpl.enableOneTouchPlay(z);
    }

    @Override // com.android.tv.settings.adv.IHdmiCecManager
    public void enableAutoPowerOff(boolean z) {
        mImpl.enableAutoPowerOff(z);
    }

    @Override // com.android.tv.settings.adv.IHdmiCecManager
    public void enableAutoChangeLanguage(boolean z) {
        mImpl.enableAutoChangeLanguage(z);
    }
}
