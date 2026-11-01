package com.android.tv.settings.adv;

/* loaded from: classes.dex */
public interface IOutputModeManager {
    void enableDobly_DRC(boolean z);

    String getCurrentOutputMode();

    String getFrameRateOffset();

    String getHdmiColorSupportList();

    String getHdmiSupportList();

    void initSoundParametersAfterBoot();

    boolean isBestOutputmode();

    boolean isHDMIPlugged();

    boolean isModeSupportColor(String str, String str2);

    void setARCLatency(int i);

    void setDigitalAudioFormatOut(int i);

    void setDigitalAudioFormatOut(int i, String str);

    void setDoblyMode(String str);
}
