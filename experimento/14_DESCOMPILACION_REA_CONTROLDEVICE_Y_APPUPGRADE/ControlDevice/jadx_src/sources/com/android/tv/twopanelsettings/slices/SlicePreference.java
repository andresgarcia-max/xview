package com.android.tv.twopanelsettings.slices;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import androidx.slice.core.SliceActionImpl;
import com.android.tv.twopanelsettings.R$styleable;

/* loaded from: classes.dex */
public class SlicePreference extends Preference implements HasSliceAction, HasSliceUri {
    private SliceActionImpl mAction;
    private int mActionId;
    private String mContentDescription;
    private SliceActionImpl mFollowUpAction;
    private String mUri;

    public SlicePreference(Context context) {
        super(context);
        init(null);
    }

    public SlicePreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(attributeSet);
    }

    @Override // androidx.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        if (TextUtils.isEmpty(this.mContentDescription)) {
            return;
        }
        preferenceViewHolder.itemView.setAccessibilityLiveRegion(1);
        preferenceViewHolder.itemView.setContentDescription(this.mContentDescription);
    }

    private void init(AttributeSet attributeSet) {
        if (attributeSet != null) {
            initStyleAttributes(attributeSet);
        }
    }

    private void initStyleAttributes(AttributeSet attributeSet) {
        TypedArray obtainStyledAttributes = getContext().obtainStyledAttributes(attributeSet, R$styleable.SlicePreference);
        for (int indexCount = obtainStyledAttributes.getIndexCount() - 1; indexCount >= 0; indexCount--) {
            int index = obtainStyledAttributes.getIndex(indexCount);
            if (index == R$styleable.SlicePreference_uri) {
                this.mUri = obtainStyledAttributes.getString(index);
                return;
            }
        }
    }

    @Override // com.android.tv.twopanelsettings.slices.HasSliceUri
    public void setUri(String str) {
        this.mUri = str;
    }

    @Override // com.android.tv.twopanelsettings.slices.HasSliceUri
    public String getUri() {
        return this.mUri;
    }

    @Override // com.android.tv.twopanelsettings.slices.HasSliceAction
    public int getActionId() {
        return this.mActionId;
    }

    @Override // com.android.tv.twopanelsettings.slices.HasSliceAction
    public void setActionId(int i) {
        this.mActionId = i;
    }

    @Override // com.android.tv.twopanelsettings.slices.HasSliceAction
    public void setSliceAction(SliceActionImpl sliceActionImpl) {
        this.mAction = sliceActionImpl;
    }

    @Override // com.android.tv.twopanelsettings.slices.HasSliceAction
    public SliceActionImpl getFollowupSliceAction() {
        return this.mFollowUpAction;
    }

    @Override // com.android.tv.twopanelsettings.slices.HasSliceAction
    public void setFollowupSliceAction(SliceActionImpl sliceActionImpl) {
        this.mFollowUpAction = sliceActionImpl;
    }

    @Override // com.android.tv.twopanelsettings.slices.HasSliceAction
    public SliceActionImpl getSliceAction() {
        return this.mAction;
    }

    public void setContentDescription(String str) {
        this.mContentDescription = str;
    }
}
