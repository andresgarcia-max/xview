package com.android.tv.twopanelsettings.slices;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import com.android.tv.twopanelsettings.R$layout;
import com.android.tv.twopanelsettings.R$styleable;

/* loaded from: classes.dex */
public class RadioPreference extends CheckBoxPreference {
    private String mRadioGroup;

    public RadioPreference(Context context) {
        this(context, null);
    }

    public RadioPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.RadioPreference, 0, 0);
        this.mRadioGroup = obtainStyledAttributes.getString(R$styleable.RadioPreference_radioGroup);
        obtainStyledAttributes.recycle();
        setWidgetLayoutResource(R$layout.radio_preference_widget);
    }

    public String getRadioGroup() {
        return this.mRadioGroup;
    }

    public void setRadioGroup(String str) {
        this.mRadioGroup = str;
    }

    public void clearOtherRadioPreferences(PreferenceGroup preferenceGroup) {
        int preferenceCount = preferenceGroup.getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = preferenceGroup.getPreference(i);
            if (preference instanceof RadioPreference) {
                RadioPreference radioPreference = (RadioPreference) preference;
                if (TextUtils.equals(getRadioGroup(), radioPreference.getRadioGroup()) && !TextUtils.equals(getKey(), radioPreference.getKey())) {
                    radioPreference.setChecked(false);
                }
            }
        }
    }
}
