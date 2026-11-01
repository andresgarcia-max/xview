package com.android.tv.settings.connectivity;

import android.content.Context;
import android.util.AttributeSet;
import androidx.preference.PreferenceCategory;

/* loaded from: classes.dex */
public class CollapsibleCategory extends PreferenceCategory {
    private boolean mCollapsed;

    public CollapsibleCategory(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mCollapsed = true;
    }

    public CollapsibleCategory(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mCollapsed = true;
    }

    public CollapsibleCategory(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mCollapsed = true;
    }

    public CollapsibleCategory(Context context) {
        super(context);
        this.mCollapsed = true;
    }

    @Override // androidx.preference.PreferenceGroup
    public int getPreferenceCount() {
        if (this.mCollapsed && shouldShowCollapsePref()) {
            return 3;
        }
        return super.getPreferenceCount();
    }

    private int getVisiblePreferenceCount() {
        int i = 0;
        for (int i2 = 0; i2 < super.getPreferenceCount(); i2++) {
            if (getPreference(i2).isVisible()) {
                i++;
            }
        }
        return i;
    }

    public boolean shouldShowCollapsePref() {
        return getVisiblePreferenceCount() >= 4;
    }
}
