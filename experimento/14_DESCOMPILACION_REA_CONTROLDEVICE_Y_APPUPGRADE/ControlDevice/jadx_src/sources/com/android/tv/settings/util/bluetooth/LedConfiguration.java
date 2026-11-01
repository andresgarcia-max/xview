package com.android.tv.settings.util.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;

/* loaded from: classes.dex */
public class LedConfiguration implements Parcelable {
    public static final Parcelable.Creator<LedConfiguration> CREATOR = new Parcelable.Creator<LedConfiguration>() { // from class: com.android.tv.settings.util.bluetooth.LedConfiguration.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public LedConfiguration createFromParcel(Parcel parcel) {
            int readInt = parcel.readInt();
            int readInt2 = parcel.readInt();
            boolean[] zArr = new boolean[2];
            parcel.readBooleanArray(zArr);
            LedConfiguration ledConfiguration = new LedConfiguration(readInt, readInt2, zArr[0]);
            ledConfiguration.isTransient = zArr[1];
            return ledConfiguration;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public LedConfiguration[] newArray(int i) {
            return new LedConfiguration[i];
        }
    };
    public final int color0;
    public final int color1;
    public boolean isTransient;
    public final boolean pulse;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public LedConfiguration(int i, int i2, boolean z) {
        this.color0 = i;
        this.color1 = i2;
        this.pulse = z;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof LedConfiguration)) {
            return false;
        }
        LedConfiguration ledConfiguration = (LedConfiguration) obj;
        return areColorsEqual(ledConfiguration) && this.pulse == ledConfiguration.pulse && this.isTransient == ledConfiguration.isTransient;
    }

    public boolean areColorsEqual(LedConfiguration ledConfiguration) {
        if (ledConfiguration == null) {
            return false;
        }
        int i = this.color0;
        int i2 = ledConfiguration.color0;
        return (i == i2 && this.color1 == ledConfiguration.color1) || (i == ledConfiguration.color1 && this.color1 == i2);
    }

    public String toString() {
        return "LedConfiguration(" + getNameString() + ")";
    }

    public String getNameString() {
        Object[] objArr = new Object[4];
        objArr[0] = Integer.valueOf(this.color0 & 16777215);
        objArr[1] = Integer.valueOf(this.color1 & 16777215);
        objArr[2] = this.pulse ? "p" : "";
        objArr[3] = this.isTransient ? "t" : "";
        return String.format("#%06x-#%06x%s%s", objArr);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.color0);
        parcel.writeInt(this.color1);
        parcel.writeBooleanArray(new boolean[]{this.pulse, this.isTransient});
    }
}
