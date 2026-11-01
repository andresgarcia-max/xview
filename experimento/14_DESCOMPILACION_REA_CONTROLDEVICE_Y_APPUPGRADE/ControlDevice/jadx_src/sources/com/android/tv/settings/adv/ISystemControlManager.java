package com.android.tv.settings.adv;

/* loaded from: classes.dex */
public interface ISystemControlManager {
    String getBootenv(String str, String str2);

    String getKeyProvisionChecksum(int i);

    String getPrefHdmiDispMode();

    String getProperty(String str);

    boolean getPropertyBoolean(String str, boolean z);

    String readSysFs(String str);

    void setBootenv(String str, String str2);

    void setMboxOutputMode(String str);

    void setProperty(String str, String str2);

    boolean writeAttestationKey(int[] iArr, int i);

    boolean writeWidevineKey(int[] iArr, int i);
}
