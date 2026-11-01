package com.android.tv.twopanelsettings.slices;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import androidx.preference.PreferenceViewHolder;
import com.android.settingslib.RestrictedPreference;

/* loaded from: classes.dex */
public class CustomContentDescriptionPreference extends RestrictedPreference {
    private String mContentDescription;

    public CustomContentDescriptionPreference(Context context) {
        super(context);
    }

    public CustomContentDescriptionPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // com.android.settingslib.RestrictedPreference, com.android.settingslib.widget.TwoTargetPreference, androidx.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        if (TextUtils.isEmpty(this.mContentDescription)) {
            return;
        }
        preferenceViewHolder.itemView.setAccessibilityLiveRegion(1);
        preferenceViewHolder.itemView.setContentDescription(this.mContentDescription);
    }

    public void setContentDescription(String str) {
        this.mContentDescription = str;
    }
}
