package com.android.tv.twopanelsettings.slices;

import android.content.Context;
import androidx.preference.CheckBoxPreference;
import androidx.slice.core.SliceActionImpl;

/* loaded from: classes.dex */
public class SliceCheckboxPreference extends CheckBoxPreference implements HasSliceAction {
    private SliceActionImpl mAction;
    private int mActionId;
    private SliceActionImpl mFollowupSliceAction;

    public SliceCheckboxPreference(Context context, SliceActionImpl sliceActionImpl) {
        super(context);
        this.mAction = sliceActionImpl;
        update();
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
}
