package com.android.tv.settings.adv.impl;

import com.android.tv.settings.adv.ISystemControlManager;

/* loaded from: classes.dex */
public class SystemControlManager implements ISystemControlManager {
    private static com.droidlogic.app.SystemControlManager mImpl;

    public static SystemControlManager getInstance() {
        return new SystemControlManager();
    }

    public SystemControlManager() {
        mImpl = com.droidlogic.app.SystemControlManager.getInstance();
    }

    @Override // com.android.tv.settings.adv.ISystemControlManager
    public String getProperty(String str) {
        return mImpl.getProperty(str);
    }

    @Override // com.android.tv.settings.adv.ISystemControlManager
    public boolean getPropertyBoolean(String str, boolean z) {
        return mImpl.getPropertyBoolean(str, z);
    }

    @Override // com.android.tv.settings.adv.ISystemControlManager
    public void setProperty(String str, String str2) {
        mImpl.setProperty(str, str2);
    }

    @Override // com.android.tv.settings.adv.ISystemControlManager
    public String readSysFs(String str) {
        return mImpl.readSysFs(str);
    }

    @Override // com.android.tv.settings.adv.ISystemControlManager
    public boolean writeWidevineKey(int[] iArr, int i) {
        return mImpl.writeWidevineKey(iArr, i);
    }

    @Override // com.android.tv.settings.adv.ISystemControlManager
    public boolean writeAttestationKey(int[] iArr, int i) {
        return mImpl.writeAttestationKey(iArr, i);
    }

    @Override // com.android.tv.settings.adv.ISystemControlManager
    public String getKeyProvisionChecksum(int i) {
        return mImpl.getKeyProvisionChecksum(i);
    }

    @Override // com.android.tv.settings.adv.ISystemControlManager
    public String getBootenv(String str, String str2) {
        return mImpl.getBootenv(str, str2);
    }

    @Override // com.android.tv.settings.adv.ISystemControlManager
    public void setBootenv(String str, String str2) {
        mImpl.setBootenv(str, str2);
    }

    @Override // com.android.tv.settings.adv.ISystemControlManager
    public String getPrefHdmiDispMode() {
        return mImpl.getPrefHdmiDispMode();
    }

    @Override // com.android.tv.settings.adv.ISystemControlManager
    public void setMboxOutputMode(String str) {
        mImpl.setMboxOutputMode(str);
    }
}
