package com.android.tv.twopanelsettings;

import android.R;
import android.animation.AnimatorInflater;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.preference.DialogPreference;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.recyclerview.widget.RecyclerView;
import com.android.tv.twopanelsettings.LeanbackListPreferenceDialogFragmentCompat;

/* loaded from: classes.dex */
public class TwoPanelListPreferenceDialogFragment extends LeanbackListPreferenceDialogFragmentCompat {
    private CharSequence[] mEntriesCopy;
    private CharSequence[] mEntryValuesCopy;
    private String mInitialSelectionCopy;
    private boolean mMultiCopy;
    private CharSequence[] mSummariesCopy;

    public static TwoPanelListPreferenceDialogFragment newInstanceSingle(String str) {
        Bundle bundle = new Bundle(1);
        bundle.putString("key", str);
        TwoPanelListPreferenceDialogFragment twoPanelListPreferenceDialogFragment = new TwoPanelListPreferenceDialogFragment();
        twoPanelListPreferenceDialogFragment.setArguments(bundle);
        return twoPanelListPreferenceDialogFragment;
    }

    @Override // com.android.tv.twopanelsettings.LeanbackListPreferenceDialogFragmentCompat, androidx.leanback.preference.LeanbackPreferenceDialogFragmentCompat, androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (bundle == null) {
            DialogPreference preference = getPreference();
            if (preference instanceof ListPreference) {
                this.mMultiCopy = false;
                ListPreference listPreference = (ListPreference) preference;
                this.mEntriesCopy = listPreference.getEntries();
                this.mEntryValuesCopy = listPreference.getEntryValues();
                if (preference instanceof SummaryListPreference) {
                    this.mSummariesCopy = ((SummaryListPreference) preference).getSummaries();
                }
                this.mInitialSelectionCopy = listPreference.getValue();
                return;
            }
            if (preference instanceof MultiSelectListPreference) {
                this.mMultiCopy = true;
                return;
            }
            throw new IllegalArgumentException("Preference must be a ListPreference or MultiSelectListPreference");
        }
        this.mMultiCopy = bundle.getBoolean("LeanbackListPreferenceDialogFragment.isMulti");
        this.mEntriesCopy = bundle.getCharSequenceArray("LeanbackListPreferenceDialogFragment.entries");
        this.mEntryValuesCopy = bundle.getCharSequenceArray("LeanbackListPreferenceDialogFragment.entryValues");
        this.mSummariesCopy = bundle.getCharSequenceArray("LeanbackListPreferenceDialogFragment.summaries");
        if (this.mMultiCopy) {
            return;
        }
        this.mInitialSelectionCopy = bundle.getString("LeanbackListPreferenceDialogFragment.initialSelection");
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        if (view != null) {
            removeAnimationClipping(view);
            ViewGroup viewGroup = (ViewGroup) view.findViewById(R$id.main_frame);
            if (viewGroup != null) {
                viewGroup.setOutlineProvider(null);
            }
        }
    }

    protected void removeAnimationClipping(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            viewGroup.setClipChildren(false);
            viewGroup.setClipToPadding(false);
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                removeAnimationClipping(viewGroup.getChildAt(i));
            }
        }
    }

    @Override // com.android.tv.twopanelsettings.LeanbackListPreferenceDialogFragmentCompat
    public RecyclerView.Adapter onCreateAdapter() {
        if (!this.mMultiCopy) {
            return new TwoPanelAdapterSingle(this.mEntriesCopy, this.mEntryValuesCopy, this.mSummariesCopy, this.mInitialSelectionCopy);
        }
        return super.onCreateAdapter();
    }

    private class TwoPanelAdapterSingle extends RecyclerView.Adapter<LeanbackListPreferenceDialogFragmentCompat.ViewHolder> implements LeanbackListPreferenceDialogFragmentCompat.OnItemClickListener {
        private final CharSequence[] mEntries;
        private final CharSequence[] mEntryValues;
        private CharSequence mSelectedValue;
        private final CharSequence[] mSummaries;

        TwoPanelAdapterSingle(CharSequence[] charSequenceArr, CharSequence[] charSequenceArr2, CharSequence[] charSequenceArr3, CharSequence charSequence) {
            this.mEntries = charSequenceArr;
            this.mEntryValues = charSequenceArr2;
            this.mSummaries = charSequenceArr3;
            this.mSelectedValue = charSequence;
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public LeanbackListPreferenceDialogFragmentCompat.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new LeanbackListPreferenceDialogFragmentCompat.ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R$layout.leanback_list_preference_item_single, viewGroup, false), this);
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public void onBindViewHolder(LeanbackListPreferenceDialogFragmentCompat.ViewHolder viewHolder, int i) {
            viewHolder.getWidgetView().setChecked(this.mEntryValues[i].equals(this.mSelectedValue));
            viewHolder.getTitleView().setText(this.mEntries[i]);
            viewHolder.itemView.setStateListAnimator(AnimatorInflater.loadStateListAnimator(TwoPanelListPreferenceDialogFragment.this.getContext(), R$animator.preference));
            TextView textView = (TextView) viewHolder.getContainer().findViewById(R.id.summary);
            if (textView != null) {
                CharSequence[] charSequenceArr = this.mSummaries;
                if (charSequenceArr != null && !TextUtils.isEmpty(charSequenceArr[i])) {
                    textView.setText(this.mSummaries[i]);
                    textView.setVisibility(0);
                } else {
                    textView.setVisibility(8);
                }
            }
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public int getItemCount() {
            return this.mEntries.length;
        }

        @Override // com.android.tv.twopanelsettings.LeanbackListPreferenceDialogFragmentCompat.OnItemClickListener
        public void onItemClick(LeanbackListPreferenceDialogFragmentCompat.ViewHolder viewHolder) {
            int adapterPosition = viewHolder.getAdapterPosition();
            if (adapterPosition == -1) {
                return;
            }
            CharSequence charSequence = this.mEntryValues[adapterPosition];
            ListPreference listPreference = (ListPreference) TwoPanelListPreferenceDialogFragment.this.getPreference();
            if (adapterPosition >= 0) {
                String charSequence2 = this.mEntryValues[adapterPosition].toString();
                if (listPreference.callChangeListener(charSequence2)) {
                    listPreference.setValue(charSequence2);
                    this.mSelectedValue = charSequence;
                }
            }
            if (TwoPanelListPreferenceDialogFragment.this.getParentFragment() instanceof TwoPanelSettingsFragment) {
                ((TwoPanelSettingsFragment) TwoPanelListPreferenceDialogFragment.this.getParentFragment()).navigateBack();
            }
            notifyDataSetChanged();
        }
    }
}
