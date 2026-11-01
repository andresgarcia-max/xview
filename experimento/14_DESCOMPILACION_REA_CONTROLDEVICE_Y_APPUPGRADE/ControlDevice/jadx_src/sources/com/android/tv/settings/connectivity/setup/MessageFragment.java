package com.android.tv.settings.connectivity.setup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.android.tv.settings.util.AccessibilityHelper;
import com.control.device.R;

/* loaded from: classes.dex */
public class MessageFragment extends Fragment {
    public static void addArguments(Bundle bundle, String str, boolean z) {
        bundle.putString("title", str);
        bundle.putBoolean("show_progress_indicator", z);
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.setup_message, viewGroup, false);
        View findViewById = inflate.findViewById(R.id.progress);
        TextView textView = (TextView) inflate.findViewById(R.id.status_text);
        Bundle arguments = getArguments();
        String string = arguments.getString("title");
        boolean z = arguments.getBoolean("show_progress_indicator");
        if (string != null) {
            textView.setText(string);
            textView.setVisibility(0);
            if (AccessibilityHelper.forceFocusableViews(getActivity())) {
                textView.setFocusable(true);
                textView.setFocusableInTouchMode(true);
            }
        } else {
            textView.setVisibility(8);
        }
        if (z) {
            findViewById.setVisibility(0);
        } else {
            findViewById.setVisibility(8);
        }
        return inflate;
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
        if (AccessibilityHelper.forceFocusableViews(getActivity())) {
            ((TextView) getView().findViewById(R.id.status_text)).requestFocus();
        }
    }
}
