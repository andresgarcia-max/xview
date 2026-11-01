package com.android.tv.twopanelsettings.slices;

import android.content.Context;
import androidx.slice.core.SliceActionImpl;

/* loaded from: classes.dex */
public class SliceRadioPreference extends RadioPreference implements HasSliceAction, HasSliceUri {
    private int mActionId;
    private SliceActionImpl mFollowupSliceAction;
    private SliceActionImpl mSliceAction;
    private String mUri;

    public SliceRadioPreference(Context context, SliceActionImpl sliceActionImpl) {
        super(context);
        this.mSliceAction = sliceActionImpl;
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
        return this.mSliceAction;
    }

    @Override // com.android.tv.twopanelsettings.slices.HasSliceAction
    public void setSliceAction(SliceActionImpl sliceActionImpl) {
        this.mSliceAction = sliceActionImpl;
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
        setChecked(this.mSliceAction.isChecked());
    }

    @Override // com.android.tv.twopanelsettings.slices.HasSliceUri
    public void setUri(String str) {
        this.mUri = str;
    }

    @Override // com.android.tv.twopanelsettings.slices.HasSliceUri
    public String getUri() {
        return this.mUri;
    }
}
