package com.android.tv.twopanelsettings.slices;

import android.animation.AnimatorInflater;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.RecyclerView;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.tv.twopanelsettings.R$animator;
import com.android.tv.twopanelsettings.R$id;
import com.android.tv.twopanelsettings.SettingsPreferenceFragmentBase;
import com.android.tv.twopanelsettings.TwoPanelSettingsFragment;

/* loaded from: classes.dex */
public abstract class SettingsPreferenceFragment extends SettingsPreferenceFragmentBase implements TwoPanelSettingsFragment.PreviewableComponentCallback {
    private final Lifecycle mLifecycle = new Lifecycle(this);

    protected int getPageId() {
        return 2;
    }

    public Lifecycle getSettingsLifecycle() {
        return this.mLifecycle;
    }

    @Override // androidx.fragment.app.Fragment
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mLifecycle.onAttach(context);
    }

    @Override // androidx.preference.PreferenceFragmentCompat, androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        this.mLifecycle.onCreate(bundle);
        this.mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
        super.onCreate(bundle);
        if (getCallbackFragment() == null || (getCallbackFragment() instanceof TwoPanelSettingsFragment)) {
            return;
        }
        InstrumentationUtils.logPageFocused(getPageId(), Boolean.TRUE);
    }

    @Override // com.android.tv.twopanelsettings.SettingsPreferenceFragmentBase, androidx.leanback.preference.LeanbackPreferenceFragmentCompat, androidx.preference.PreferenceFragmentCompat, androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        TextView textView;
        super.onViewCreated(view, bundle);
        if (view == null || (textView = (TextView) view.findViewById(R$id.decor_title)) == null || getResources().getConfiguration().getLayoutDirection() != 1) {
            return;
        }
        textView.setGravity(5);
    }

    @Override // androidx.preference.PreferenceFragmentCompat
    protected RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        return new PreferenceGroupAdapter(preferenceScreen) { // from class: com.android.tv.twopanelsettings.slices.SettingsPreferenceFragment.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // androidx.preference.PreferenceGroupAdapter, androidx.recyclerview.widget.RecyclerView.Adapter
            public PreferenceViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                PreferenceViewHolder onCreateViewHolder = super.onCreateViewHolder(viewGroup, i);
                onCreateViewHolder.itemView.setStateListAnimator(AnimatorInflater.loadStateListAnimator(SettingsPreferenceFragment.this.getContext(), R$animator.preference));
                return onCreateViewHolder;
            }
        };
    }

    @Override // androidx.preference.PreferenceFragmentCompat
    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        this.mLifecycle.setPreferenceScreen(preferenceScreen);
        super.setPreferenceScreen(preferenceScreen);
    }

    @Override // androidx.preference.PreferenceFragmentCompat, androidx.fragment.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        this.mLifecycle.onSaveInstanceState(bundle);
    }

    @Override // androidx.preference.PreferenceFragmentCompat, androidx.fragment.app.Fragment
    public void onStart() {
        this.mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START);
        super.onStart();
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
        this.mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
    }

    @Override // com.android.tv.twopanelsettings.TwoPanelSettingsFragment.PreviewableComponentCallback
    public void onArriveAtMainPanel(boolean z) {
        InstrumentationUtils.logPageFocused(getPageId(), Boolean.valueOf(z));
    }

    @Override // androidx.fragment.app.Fragment
    public void onPause() {
        this.mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE);
        super.onPause();
    }

    @Override // androidx.preference.PreferenceFragmentCompat, androidx.fragment.app.Fragment
    public void onStop() {
        this.mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
        super.onStop();
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroy() {
        this.mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
        super.onDestroy();
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        this.mLifecycle.onCreateOptionsMenu(menu, menuInflater);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override // androidx.fragment.app.Fragment
    public void onPrepareOptionsMenu(Menu menu) {
        this.mLifecycle.onPrepareOptionsMenu(menu);
        super.onPrepareOptionsMenu(menu);
    }

    @Override // androidx.fragment.app.Fragment
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        boolean onOptionsItemSelected = this.mLifecycle.onOptionsItemSelected(menuItem);
        return !onOptionsItemSelected ? super.onOptionsItemSelected(menuItem) : onOptionsItemSelected;
    }
}
