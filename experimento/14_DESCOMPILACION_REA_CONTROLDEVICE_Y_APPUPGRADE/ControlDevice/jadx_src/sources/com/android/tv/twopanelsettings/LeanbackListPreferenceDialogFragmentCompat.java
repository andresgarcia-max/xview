package com.android.tv.twopanelsettings;

import android.R;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.TextView;
import androidx.collection.ArraySet;
import androidx.leanback.preference.LeanbackPreferenceDialogFragmentCompat;
import androidx.leanback.widget.VerticalGridView;
import androidx.preference.DialogPreference;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/* loaded from: classes.dex */
public class LeanbackListPreferenceDialogFragmentCompat extends LeanbackPreferenceDialogFragmentCompat {
    private CharSequence mDialogMessage;
    private CharSequence mDialogTitle;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private String mInitialSelection;
    Set<String> mInitialSelections;
    private boolean mMulti;

    public interface OnItemClickListener {
        void onItemClick(ViewHolder viewHolder);
    }

    @Override // androidx.leanback.preference.LeanbackPreferenceDialogFragmentCompat, androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (bundle == null) {
            DialogPreference preference = getPreference();
            this.mDialogTitle = preference.getDialogTitle();
            this.mDialogMessage = preference.getDialogMessage();
            if (preference instanceof ListPreference) {
                this.mMulti = false;
                ListPreference listPreference = (ListPreference) preference;
                this.mEntries = listPreference.getEntries();
                this.mEntryValues = listPreference.getEntryValues();
                this.mInitialSelection = listPreference.getValue();
                return;
            }
            if (!(preference instanceof MultiSelectListPreference)) {
                throw new IllegalArgumentException("Preference must be a ListPreference or MultiSelectListPreference");
            }
            this.mMulti = true;
            MultiSelectListPreference multiSelectListPreference = (MultiSelectListPreference) preference;
            this.mEntries = multiSelectListPreference.getEntries();
            this.mEntryValues = multiSelectListPreference.getEntryValues();
            this.mInitialSelections = multiSelectListPreference.getValues();
            return;
        }
        this.mDialogTitle = bundle.getCharSequence("LeanbackListPreferenceDialogFragment.title");
        this.mDialogMessage = bundle.getCharSequence("LeanbackListPreferenceDialogFragment.message");
        this.mMulti = bundle.getBoolean("LeanbackListPreferenceDialogFragment.isMulti");
        this.mEntries = bundle.getCharSequenceArray("LeanbackListPreferenceDialogFragment.entries");
        this.mEntryValues = bundle.getCharSequenceArray("LeanbackListPreferenceDialogFragment.entryValues");
        if (this.mMulti) {
            String[] stringArray = bundle.getStringArray("LeanbackListPreferenceDialogFragment.initialSelections");
            ArraySet arraySet = new ArraySet(stringArray != null ? stringArray.length : 0);
            this.mInitialSelections = arraySet;
            if (stringArray != null) {
                Collections.addAll(arraySet, stringArray);
                return;
            }
            return;
        }
        this.mInitialSelection = bundle.getString("LeanbackListPreferenceDialogFragment.initialSelection");
    }

    @Override // androidx.fragment.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putCharSequence("LeanbackListPreferenceDialogFragment.title", this.mDialogTitle);
        bundle.putCharSequence("LeanbackListPreferenceDialogFragment.message", this.mDialogMessage);
        bundle.putBoolean("LeanbackListPreferenceDialogFragment.isMulti", this.mMulti);
        bundle.putCharSequenceArray("LeanbackListPreferenceDialogFragment.entries", this.mEntries);
        bundle.putCharSequenceArray("LeanbackListPreferenceDialogFragment.entryValues", this.mEntryValues);
        if (this.mMulti) {
            Set<String> set = this.mInitialSelections;
            bundle.putStringArray("LeanbackListPreferenceDialogFragment.initialSelections", (String[]) set.toArray(new String[set.size()]));
        } else {
            bundle.putString("LeanbackListPreferenceDialogFragment.initialSelection", this.mInitialSelection);
        }
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(R$attr.preferenceTheme, typedValue, true);
        int i = typedValue.resourceId;
        if (i == 0) {
            i = R$style.PreferenceThemeOverlayLeanback;
        }
        View inflate = layoutInflater.cloneInContext(new ContextThemeWrapper(getActivity(), i)).inflate(R$layout.leanback_list_preference_fragment, viewGroup, false);
        VerticalGridView verticalGridView = (VerticalGridView) inflate.findViewById(R.id.list);
        verticalGridView.setWindowAlignment(3);
        verticalGridView.setFocusScrollStrategy(0);
        verticalGridView.setAdapter(onCreateAdapter());
        CharSequence charSequence = this.mDialogTitle;
        if (!TextUtils.isEmpty(charSequence)) {
            ((TextView) inflate.findViewById(R$id.decor_title)).setText(charSequence);
        }
        CharSequence charSequence2 = this.mDialogMessage;
        if (!TextUtils.isEmpty(charSequence2)) {
            TextView textView = (TextView) inflate.findViewById(R.id.message);
            textView.setVisibility(0);
            textView.setText(charSequence2);
        }
        return inflate;
    }

    public RecyclerView.Adapter onCreateAdapter() {
        return this.mMulti ? new AdapterMulti(this.mEntries, this.mEntryValues, this.mInitialSelections) : new AdapterSingle(this.mEntries, this.mEntryValues, this.mInitialSelection);
    }

    public static final class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ViewGroup mContainer;
        private final OnItemClickListener mListener;
        private final TextView mTitleView;
        private final Checkable mWidgetView;

        public ViewHolder(View view, OnItemClickListener onItemClickListener) {
            super(view);
            this.mWidgetView = (Checkable) view.findViewById(R$id.button);
            ViewGroup viewGroup = (ViewGroup) view.findViewById(R$id.container);
            this.mContainer = viewGroup;
            this.mTitleView = (TextView) view.findViewById(R.id.title);
            viewGroup.setOnClickListener(this);
            this.mListener = onItemClickListener;
        }

        public Checkable getWidgetView() {
            return this.mWidgetView;
        }

        public TextView getTitleView() {
            return this.mTitleView;
        }

        public ViewGroup getContainer() {
            return this.mContainer;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            this.mListener.onItemClick(this);
        }
    }

    final class AdapterMulti extends RecyclerView.Adapter<ViewHolder> implements OnItemClickListener {
        private final CharSequence[] mEntries;
        private final CharSequence[] mEntryValues;
        private final Set<String> mSelections;

        AdapterMulti(CharSequence[] charSequenceArr, CharSequence[] charSequenceArr2, Set<String> set) {
            this.mEntries = charSequenceArr;
            this.mEntryValues = charSequenceArr2;
            this.mSelections = new HashSet(set);
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R$layout.leanback_list_preference_item_multi, viewGroup, false), this);
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            viewHolder.getWidgetView().setChecked(this.mSelections.contains(this.mEntryValues[i].toString()));
            viewHolder.getTitleView().setText(this.mEntries[i]);
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public int getItemCount() {
            return this.mEntries.length;
        }

        @Override // com.android.tv.twopanelsettings.LeanbackListPreferenceDialogFragmentCompat.OnItemClickListener
        public void onItemClick(ViewHolder viewHolder) {
            int adapterPosition = viewHolder.getAdapterPosition();
            if (adapterPosition != -1) {
                String charSequence = this.mEntryValues[adapterPosition].toString();
                if (this.mSelections.contains(charSequence)) {
                    this.mSelections.remove(charSequence);
                } else {
                    this.mSelections.add(charSequence);
                }
                MultiSelectListPreference multiSelectListPreference = (MultiSelectListPreference) LeanbackListPreferenceDialogFragmentCompat.this.getPreference();
                if (multiSelectListPreference.callChangeListener(new HashSet(this.mSelections))) {
                    multiSelectListPreference.setValues(new HashSet(this.mSelections));
                    LeanbackListPreferenceDialogFragmentCompat.this.mInitialSelections = this.mSelections;
                } else if (this.mSelections.contains(charSequence)) {
                    this.mSelections.remove(charSequence);
                } else {
                    this.mSelections.add(charSequence);
                }
                notifyDataSetChanged();
            }
        }
    }

    final class AdapterSingle extends RecyclerView.Adapter<ViewHolder> implements OnItemClickListener {
        private final CharSequence[] mEntries;
        private final CharSequence[] mEntryValues;
        private CharSequence mSelectedValue;

        AdapterSingle(CharSequence[] charSequenceArr, CharSequence[] charSequenceArr2, CharSequence charSequence) {
            this.mEntries = charSequenceArr;
            this.mEntryValues = charSequenceArr2;
            this.mSelectedValue = charSequence;
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R$layout.leanback_list_preference_item_single, viewGroup, false), this);
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            viewHolder.getWidgetView().setChecked(this.mEntryValues[i].equals(this.mSelectedValue));
            viewHolder.getTitleView().setText(this.mEntries[i]);
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public int getItemCount() {
            return this.mEntries.length;
        }

        @Override // com.android.tv.twopanelsettings.LeanbackListPreferenceDialogFragmentCompat.OnItemClickListener
        public void onItemClick(ViewHolder viewHolder) {
            int adapterPosition = viewHolder.getAdapterPosition();
            if (adapterPosition != -1) {
                CharSequence charSequence = this.mEntryValues[adapterPosition];
                ListPreference listPreference = (ListPreference) LeanbackListPreferenceDialogFragmentCompat.this.getPreference();
                if (adapterPosition >= 0) {
                    String charSequence2 = this.mEntryValues[adapterPosition].toString();
                    if (listPreference.callChangeListener(charSequence2)) {
                        listPreference.setValue(charSequence2);
                        this.mSelectedValue = charSequence;
                    }
                }
                LeanbackListPreferenceDialogFragmentCompat.this.getFragmentManager().popBackStack();
                notifyDataSetChanged();
            }
        }
    }
}
