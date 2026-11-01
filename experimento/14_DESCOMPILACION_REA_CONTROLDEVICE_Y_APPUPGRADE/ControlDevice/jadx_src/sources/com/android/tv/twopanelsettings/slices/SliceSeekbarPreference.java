package com.android.tv.twopanelsettings.slices;

import android.content.Context;
import android.util.AttributeSet;
import androidx.preference.SeekBarPreference;
import androidx.slice.core.SliceActionImpl;

/* loaded from: classes.dex */
public class SliceSeekbarPreference extends SeekBarPreference implements HasSliceAction {
    protected SliceActionImpl mAction;
    private int mActionId;
    private SliceActionImpl mFollowupSliceAction;

    public SliceSeekbarPreference(Context context, SliceActionImpl sliceActionImpl, int i, int i2, int i3) {
        super(context);
        this.mAction = sliceActionImpl;
        update(i, i2, i3);
    }

    @Override // com.android.tv.twopanelsettings.slices.HasSliceAction
    public int getActionId() {
        return this.mActionId;
    }

    @Override // com.android.tv.twopanelsettings.slices.HasSliceAction
    public void setActionId(int i) {
        this.mActionId = i;
    }

    public SliceSeekbarPreference(Context context, AttributeSet attributeSet) {
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

    private void update(int i, int i2, int i3) {
        setMin(i);
        setMax(i2);
        setValue(i3);
    }
}
