package com.android.tv.twopanelsettings;

import android.os.Bundle;
import android.view.View;
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat;

/* loaded from: classes.dex */
public abstract class SettingsPreferenceFragmentBase extends LeanbackPreferenceFragmentCompat {
    @Override // androidx.leanback.preference.LeanbackPreferenceFragmentCompat, androidx.preference.PreferenceFragmentCompat, androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        if (getCallbackFragment() instanceof TwoPanelSettingsFragment) {
            ((TwoPanelSettingsFragment) getCallbackFragment()).addListenerForFragment(this);
        }
    }

    @Override // androidx.preference.PreferenceFragmentCompat, androidx.fragment.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        if (getCallbackFragment() instanceof TwoPanelSettingsFragment) {
            ((TwoPanelSettingsFragment) getCallbackFragment()).removeListenerForFragment(this);
        }
    }
}
