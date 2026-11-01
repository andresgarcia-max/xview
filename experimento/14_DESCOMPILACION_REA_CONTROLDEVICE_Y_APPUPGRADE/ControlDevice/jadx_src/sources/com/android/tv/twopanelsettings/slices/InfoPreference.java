package com.android.tv.twopanelsettings.slices;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import com.android.tv.twopanelsettings.R$id;
import com.android.tv.twopanelsettings.R$layout;
import java.util.List;

/* loaded from: classes.dex */
public class InfoPreference extends Preference {
    private List<Pair<CharSequence, CharSequence>> mInfoList;

    public InfoPreference(Context context, List<Pair<CharSequence, CharSequence>> list) {
        super(context);
        this.mInfoList = list;
        setLayoutResource(R$layout.info_preference);
        setEnabled(false);
    }

    @Override // androidx.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        ViewGroup viewGroup = (ViewGroup) preferenceViewHolder.findViewById(R$id.item_container);
        viewGroup.removeAllViews();
        for (Pair<CharSequence, CharSequence> pair : this.mInfoList) {
            View inflate = LayoutInflater.from(getContext()).inflate(R$layout.info_preference_item, viewGroup, false);
            ((TextView) inflate.findViewById(R$id.info_item_title)).setText((CharSequence) pair.first);
            ((TextView) inflate.findViewById(R$id.info_item_summary)).setText((CharSequence) pair.second);
            viewGroup.addView(inflate);
        }
    }
}
