package com.android.tv.twopanelsettings.slices;

import android.content.Context;
import android.util.AttributeSet;

/* loaded from: classes.dex */
public class EmbeddedSlicePreference extends SlicePreference {
    private final EmbeddedSlicePreferenceHelper mHelper;

    public EmbeddedSlicePreference(Context context, String str) {
        super(context);
        this.mHelper = new EmbeddedSlicePreferenceHelper(this, getUri());
    }

    public EmbeddedSlicePreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mHelper = new EmbeddedSlicePreferenceHelper(this, getUri());
    }

    @Override // androidx.preference.Preference
    public void onAttached() {
        super.onAttached();
        this.mHelper.onAttached();
    }

    @Override // androidx.preference.Preference
    public void onDetached() {
        super.onDetached();
        this.mHelper.onDetached();
    }

    void update() {
        setTitle(this.mHelper.mNewPref.getTitle());
        setSummary(this.mHelper.mNewPref.getSummary());
        setIcon(this.mHelper.mNewPref.getIcon());
        Object obj = this.mHelper.mNewPref;
        if (!(obj instanceof HasSliceAction) || ((HasSliceAction) obj).getSliceAction() == null) {
            return;
        }
        setIntent(((HasSliceAction) this.mHelper.mNewPref).getSliceAction().getAction().getIntent());
    }
}
