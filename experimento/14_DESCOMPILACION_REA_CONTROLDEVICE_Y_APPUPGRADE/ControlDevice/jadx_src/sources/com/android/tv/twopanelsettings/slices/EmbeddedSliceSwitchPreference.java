package com.android.tv.twopanelsettings.slices;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;
import androidx.slice.core.SliceActionImpl;
import com.android.tv.twopanelsettings.R$styleable;

/* loaded from: classes.dex */
public class EmbeddedSliceSwitchPreference extends SliceSwitchPreference {
    private final EmbeddedSlicePreferenceHelper mHelper;
    private String mUri;

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

    public EmbeddedSliceSwitchPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(attributeSet);
        this.mHelper = new EmbeddedSlicePreferenceHelper(this, this.mUri);
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

    void update() {
        setTitle(this.mHelper.mNewPref.getTitle());
        setSummary(this.mHelper.mNewPref.getSummary());
        setIcon(this.mHelper.mNewPref.getIcon());
        Preference preference = this.mHelper.mNewPref;
        if (preference instanceof TwoStatePreference) {
            setChecked(((TwoStatePreference) preference).isChecked());
        }
        Object obj = this.mHelper.mNewPref;
        if (obj instanceof HasSliceAction) {
            setSliceAction(((HasSliceAction) obj).getSliceAction());
        }
        setVisible(true);
    }

    @Override // androidx.preference.TwoStatePreference, androidx.preference.Preference
    public void onClick() {
        SliceActionImpl sliceActionImpl;
        boolean z = !isChecked();
        try {
            sliceActionImpl = this.mAction;
        } catch (PendingIntent.CanceledException e) {
            z = !z;
            Log.e("EmbeddedSliceSwitchPreference", "PendingIntent for slice cannot be sent", e);
        }
        if (sliceActionImpl == null) {
            return;
        }
        if (sliceActionImpl.isToggle()) {
            this.mAction.getActionItem().fireAction(getContext(), new Intent().putExtra("android.app.slice.extra.TOGGLE_STATE", z));
        } else {
            this.mAction.getActionItem().fireAction(null, null);
        }
        if (callChangeListener(Boolean.valueOf(z))) {
            setChecked(z);
        }
    }
}
