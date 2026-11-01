package com.control.device.settings.connectivity;

import androidx.fragment.app.Fragment;

/* loaded from: classes.dex */
public interface State extends com.android.tv.settings.connectivity.util.State {

    public interface FragmentChangeListener {
        void onFragmentChange(Fragment fragment, boolean z);
    }

    public interface StateCompleteListener {
        void onComplete(int i);
    }

    void processBackward();

    void processForward();
}
