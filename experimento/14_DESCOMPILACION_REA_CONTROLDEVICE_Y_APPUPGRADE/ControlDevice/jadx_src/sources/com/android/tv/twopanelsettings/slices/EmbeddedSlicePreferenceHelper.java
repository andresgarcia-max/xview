package com.android.tv.twopanelsettings.slices;

import android.content.Context;
import android.net.Uri;
import android.view.ContextThemeWrapper;
import androidx.lifecycle.Observer;
import androidx.preference.Preference;
import androidx.slice.Slice;
import androidx.slice.widget.ListContent;
import androidx.slice.widget.SliceContent;
import com.android.tv.twopanelsettings.slices.PreferenceSliceLiveData;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class EmbeddedSlicePreferenceHelper implements Observer<Slice> {
    private final Context mContext;
    SlicePreferenceListener mListener;
    Preference mNewPref;
    private final Preference mPreference;
    private Slice mSlice;
    private String mUri;

    public interface SlicePreferenceListener {
        void onChangeVisibility();
    }

    EmbeddedSlicePreferenceHelper(Preference preference, String str) {
        this.mPreference = preference;
        this.mUri = str;
        this.mContext = preference.getContext();
    }

    void onAttached() {
        getSliceLiveData().observeForever(this);
    }

    void onDetached() {
        getSliceLiveData().removeObserver(this);
    }

    private PreferenceSliceLiveData.SliceLiveDataImpl getSliceLiveData() {
        return ContextSingleton.getInstance().getSliceLiveData(this.mContext, Uri.parse(this.mUri));
    }

    @Override // androidx.lifecycle.Observer
    public void onChanged(Slice slice) {
        this.mSlice = slice;
        if (slice == null || slice.getHints() == null || slice.getHints().contains("partial")) {
            updateVisibility(false);
        } else {
            update();
        }
    }

    private void updateVisibility(boolean z) {
        this.mPreference.setVisible(z);
        SlicePreferenceListener slicePreferenceListener = this.mListener;
        if (slicePreferenceListener != null) {
            slicePreferenceListener.onChangeVisibility();
        }
    }

    private void update() {
        ArrayList<SliceContent> rowItems = new ListContent(this.mSlice).getRowItems();
        if (rowItems == null || rowItems.size() == 0) {
            updateVisibility(false);
            return;
        }
        Preference preference = SlicePreferencesUtil.getPreference(SlicePreferencesUtil.getEmbeddedItem(rowItems), (ContextThemeWrapper) this.mContext, null, false);
        this.mNewPref = preference;
        if (preference == null) {
            updateVisibility(false);
            return;
        }
        updateVisibility(true);
        Preference preference2 = this.mPreference;
        if (preference2 instanceof EmbeddedSlicePreference) {
            ((EmbeddedSlicePreference) preference2).update();
        } else if (preference2 instanceof EmbeddedSliceSwitchPreference) {
            ((EmbeddedSliceSwitchPreference) preference2).update();
        }
    }
}
