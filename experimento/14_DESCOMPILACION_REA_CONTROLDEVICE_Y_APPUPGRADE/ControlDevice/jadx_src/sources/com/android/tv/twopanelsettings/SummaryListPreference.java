package com.android.tv.twopanelsettings;

import android.content.Context;
import android.util.AttributeSet;
import androidx.preference.ListPreference;

/* loaded from: classes.dex */
public class SummaryListPreference extends ListPreference {
    private CharSequence[] mSummaries;

    public SummaryListPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet, 0, 0);
    }

    public CharSequence[] getSummaries() {
        return this.mSummaries;
    }
}
