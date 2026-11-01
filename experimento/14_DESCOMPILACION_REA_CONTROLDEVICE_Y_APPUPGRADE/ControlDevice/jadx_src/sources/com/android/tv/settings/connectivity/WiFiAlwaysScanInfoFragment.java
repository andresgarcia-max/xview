package com.android.tv.settings.connectivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Keep;
import com.android.tv.twopanelsettings.slices.InfoFragment;
import com.control.device.R;

@Keep
/* loaded from: classes.dex */
public class WiFiAlwaysScanInfoFragment extends InfoFragment {
    @Override // com.android.tv.twopanelsettings.slices.InfoFragment
    public void updateInfoFragment() {
    }

    @Override // com.android.tv.twopanelsettings.slices.InfoFragment, androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View onCreateView = super.onCreateView(layoutInflater, viewGroup, bundle);
        ((ImageView) onCreateView.findViewById(R.id.info_title_icon)).setImageResource(R.drawable.ic_info_outline_base);
        onCreateView.findViewById(R.id.info_title_icon).setVisibility(0);
        ((TextView) onCreateView.findViewById(R.id.info_summary)).setText(R.string.wifi_setting_always_scan_context);
        onCreateView.findViewById(R.id.info_summary).setVisibility(0);
        return onCreateView;
    }
}
