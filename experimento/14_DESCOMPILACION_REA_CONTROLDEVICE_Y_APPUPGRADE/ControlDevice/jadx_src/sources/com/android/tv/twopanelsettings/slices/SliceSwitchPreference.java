package com.android.tv.twopanelsettings.slices;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreference;
import androidx.slice.core.SliceActionImpl;

/* loaded from: classes.dex */
public class SliceSwitchPreference extends SwitchPreference implements HasSliceAction {
    protected SliceActionImpl mAction;
    private int mActionId;
    private String mContentDescription;
    private SliceActionImpl mFollowupSliceAction;

    public SliceSwitchPreference(Context context, SliceActionImpl sliceActionImpl) {
        super(context);
        this.mAction = sliceActionImpl;
        update();
    }

    @Override // androidx.preference.SwitchPreference, androidx.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        if (TextUtils.isEmpty(this.mContentDescription)) {
            return;
        }
        preferenceViewHolder.itemView.setAccessibilityLiveRegion(1);
        preferenceViewHolder.itemView.setContentDescription(CustomContentDescriptionUtil.getFullSwitchContentDescription(getContext(), this.mContentDescription, Boolean.valueOf(isChecked())));
    }

    @Override // com.android.tv.twopanelsettings.slices.HasSliceAction
    public int getActionId() {
        return this.mActionId;
    }

    @Override // com.android.tv.twopanelsettings.slices.HasSliceAction
    public void setActionId(int i) {
        this.mActionId = i;
    }

    public SliceSwitchPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // com.android.tv.twopanelsettings.slices.HasSliceAction
    public SliceActionImpl getSliceAction() {
        return this.mAction;
    }

    @Override // com.android.tv.twopanelsettings.slices.HasSliceAction
    public void setSliceAction(SliceActionImpl sliceActionImpl) {
        this.mAction = sliceActionImpl;
    }

    @Override // com.android.tv.twopanelsettings.slices.HasSliceAction
    public SliceActionImpl getFollowupSliceAction() {
        return this.mFollowupSliceAction;
    }

    @Override // com.android.tv.twopanelsettings.slices.HasSliceAction
    public void setFollowupSliceAction(SliceActionImpl sliceActionImpl) {
        this.mFollowupSliceAction = sliceActionImpl;
    }

    private void update() {
        setChecked(this.mAction.isChecked());
    }

    public void setContentDescription(String str) {
        this.mContentDescription = str;
    }
}
