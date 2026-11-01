package com.android.tv.twopanelsettings.slices;

import androidx.slice.core.SliceActionImpl;

/* loaded from: classes.dex */
public interface HasSliceAction {
    int getActionId();

    SliceActionImpl getFollowupSliceAction();

    SliceActionImpl getSliceAction();

    void setActionId(int i);

    void setFollowupSliceAction(SliceActionImpl sliceActionImpl);

    void setSliceAction(SliceActionImpl sliceActionImpl);
}
