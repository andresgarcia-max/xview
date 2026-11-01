package com.android.tv.settings.adv.impl;

import android.content.Context;
import com.android.tv.settings.adv.IOutputModeManager;

/* loaded from: classes.dex */
public class OutputModeManager implements IOutputModeManager {
    private static com.droidlogic.app.OutputModeManager mImpl;

    public static OutputModeManager getInstance(Context context) {
        return new OutputModeManager(context);
    }

    public OutputModeManager(Context context) {
        mImpl = new com.droidlogic.app.OutputModeManager(context);
    }

    @Override // com.android.tv.settings.adv.IOutputModeManager
    public String getHdmiColorSupportList() {
        return mImpl.getHdmiColorSupportList();
    }

    @Override // com.android.tv.settings.adv.IOutputModeManager
    public boolean isModeSupportColor(String str, String str2) {
        return mImpl.isModeSupportColor(str, str2);
    }

    @Override // com.android.tv.settings.adv.IOutputModeManager
    public String getCurrentOutputMode() {
        return mImpl.getCurrentOutputMode();
    }

    @Override // com.android.tv.settings.adv.IOutputModeManager
    public String getHdmiSupportList() {
        return mImpl.getHdmiSupportList();
    }

    @Override // com.android.tv.settings.adv.IOutputModeManager
    public String getFrameRateOffset() {
        return mImpl.getFrameRateOffset();
    }

    @Override // com.android.tv.settings.adv.IOutputModeManager
    public boolean isBestOutputmode() {
        return mImpl.isBestOutputmode();
    }

    @Override // com.android.tv.settings.adv.IOutputModeManager
    public boolean isHDMIPlugged() {
        return mImpl.isHDMIPlugged();
    }

    @Override // com.android.tv.settings.adv.IOutputModeManager
    public void enableDobly_DRC(boolean z) {
        mImpl.enableDobly_DRC(z);
    }

    @Override // com.android.tv.settings.adv.IOutputModeManager
    public void setDoblyMode(String str) {
        mImpl.setDoblyMode(str);
    }

    @Override // com.android.tv.settings.adv.IOutputModeManager
    public void setDigitalAudioFormatOut(int i) {
        mImpl.setDigitalAudioFormatOut(i);
    }

    @Override // com.android.tv.settings.adv.IOutputModeManager
    public void setDigitalAudioFormatOut(int i, String str) {
        mImpl.setDigitalAudioFormatOut(i, str);
    }

    @Override // com.android.tv.settings.adv.IOutputModeManager
    public void setARCLatency(int i) {
        mImpl.setARCLatency(i);
    }

    @Override // com.android.tv.settings.adv.IOutputModeManager
    public void initSoundParametersAfterBoot() {
        mImpl.initSoundParametersAfterBoot();
    }
}
