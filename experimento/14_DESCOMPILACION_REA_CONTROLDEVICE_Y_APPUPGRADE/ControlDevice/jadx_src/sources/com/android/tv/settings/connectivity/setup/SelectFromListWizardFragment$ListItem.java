package com.android.tv.settings.connectivity.setup;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.android.tv.settings.connectivity.util.WifiSecurityUtil;
import com.control.device.R;

/* loaded from: classes.dex */
public class SelectFromListWizardFragment$ListItem implements Parcelable {
    public static Parcelable.Creator<SelectFromListWizardFragment$ListItem> CREATOR = new Parcelable.Creator<SelectFromListWizardFragment$ListItem>() { // from class: com.android.tv.settings.connectivity.setup.SelectFromListWizardFragment$ListItem.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public SelectFromListWizardFragment$ListItem createFromParcel(Parcel parcel) {
            ScanResult scanResult = (ScanResult) parcel.readParcelable(ScanResult.class.getClassLoader());
            if (scanResult == null) {
                return new SelectFromListWizardFragment$ListItem(parcel.readString(), parcel.readInt());
            }
            return new SelectFromListWizardFragment$ListItem(scanResult);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public SelectFromListWizardFragment$ListItem[] newArray(int i) {
            return new SelectFromListWizardFragment$ListItem[i];
        }
    };
    private final boolean mHasIconLevel;
    private final int mIconLevel;
    private final int mIconResource;
    private final String mName;
    private final ScanResult mScanResult;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public SelectFromListWizardFragment$ListItem(String str, int i) {
        this.mName = str;
        this.mIconResource = i;
        this.mIconLevel = 0;
        this.mHasIconLevel = false;
        this.mScanResult = null;
    }

    public SelectFromListWizardFragment$ListItem(ScanResult scanResult) {
        this.mName = scanResult.SSID;
        this.mIconResource = WifiSecurityUtil.getSecurity(scanResult) == 0 ? R.drawable.setup_wifi_signal_open : R.drawable.setup_wifi_signal_lock;
        this.mIconLevel = WifiManager.calculateSignalLevel(scanResult.level, 4);
        this.mHasIconLevel = true;
        this.mScanResult = scanResult;
    }

    public String toString() {
        return this.mName;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(this.mScanResult, i);
        if (this.mScanResult == null) {
            parcel.writeString(this.mName);
            parcel.writeInt(this.mIconResource);
        }
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SelectFromListWizardFragment$ListItem)) {
            return false;
        }
        SelectFromListWizardFragment$ListItem selectFromListWizardFragment$ListItem = (SelectFromListWizardFragment$ListItem) obj;
        ScanResult scanResult = this.mScanResult;
        if (scanResult == null && selectFromListWizardFragment$ListItem.mScanResult == null) {
            return TextUtils.equals(this.mName, selectFromListWizardFragment$ListItem.mName);
        }
        return scanResult != null && selectFromListWizardFragment$ListItem.mScanResult != null && TextUtils.equals(this.mName, selectFromListWizardFragment$ListItem.mName) && WifiSecurityUtil.getSecurity(this.mScanResult) == WifiSecurityUtil.getSecurity(selectFromListWizardFragment$ListItem.mScanResult);
    }
}
