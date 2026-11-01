package com.android.tv.twopanelsettings.slices;

import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.android.tv.twopanelsettings.R$color;
import com.android.tv.twopanelsettings.R$dimen;
import com.android.tv.twopanelsettings.R$id;
import com.android.tv.twopanelsettings.R$layout;
import com.android.tv.twopanelsettings.R$string;

/* loaded from: classes.dex */
public class InfoFragment extends Fragment {
    public static final String EXTRA_INFO_HAS_STATUS = "extra_info_has_status";

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R$layout.info_fragment, viewGroup, false);
        updateInfo(inflate);
        return inflate;
    }

    public void updateInfoFragment() {
        updateInfo(getView());
    }

    private void updateInfo(View view) {
        Icon icon = (Icon) getArguments().getParcelable("extra_preference_info_image");
        Icon icon2 = (Icon) getArguments().getParcelable("extra_preference_info_title_icon");
        String string = getArguments().getString("extra_preference_info_text");
        String string2 = getArguments().getString("extra_preference_info_summary");
        boolean z = getArguments().getBoolean(EXTRA_INFO_HAS_STATUS);
        boolean z2 = getArguments().getBoolean("extra_preference_info_status");
        Resources resources = getResources();
        ImageView imageView = (ImageView) view.findViewById(R$id.info_image);
        ImageView imageView2 = (ImageView) view.findViewById(R$id.info_title_icon);
        TextView textView = (TextView) view.findViewById(R$id.info_title);
        TextView textView2 = (TextView) view.findViewById(R$id.info_status);
        TextView textView3 = (TextView) view.findViewById(R$id.info_summary);
        if (icon != null) {
            imageView.setImageDrawable(icon.loadDrawable(getContext()));
        }
        if (icon2 != null) {
            imageView2.setImageDrawable(icon2.loadDrawable(getContext()));
            imageView2.setVisibility(0);
        } else {
            imageView2.setVisibility(8);
        }
        if (string != null) {
            textView.setText(string);
            textView.setVisibility(0);
            if (z) {
                textView2.setVisibility(0);
                if (imageView2.getVisibility() == 0) {
                    textView.setMaxWidth(resources.getDimensionPixelSize(R$dimen.settings_info_fragment_title_text_max_width_short));
                } else {
                    textView.setMaxWidth(resources.getDimensionPixelSize(R$dimen.settings_info_fragment_title_text_max_width_long));
                }
                if (z2) {
                    textView2.setTextColor(getResources().getColor(R$color.info_status_on));
                    textView2.setText(getString(R$string.info_status_on));
                } else {
                    textView2.setTextColor(getResources().getColor(R$color.info_status_off));
                    textView2.setText(getString(R$string.info_status_off));
                }
            } else {
                textView2.setVisibility(8);
                textView.setMaxWidth(Integer.MAX_VALUE);
            }
        } else {
            textView.setVisibility(8);
            textView2.setVisibility(8);
        }
        if (string2 != null) {
            textView3.setText(string2);
        }
    }
}
