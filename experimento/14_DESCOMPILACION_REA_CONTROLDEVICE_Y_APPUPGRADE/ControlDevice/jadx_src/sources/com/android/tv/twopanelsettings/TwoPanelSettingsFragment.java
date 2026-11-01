package com.android.tv.twopanelsettings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.transition.Fade;
import android.util.ArrayMap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat;
import androidx.leanback.widget.OnChildViewHolderSelectedListener;
import androidx.leanback.widget.VerticalGridView;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.RecyclerView;
import com.android.tv.twopanelsettings.slices.CustomContentDescriptionPreference;
import com.android.tv.twopanelsettings.slices.HasSliceUri;
import com.android.tv.twopanelsettings.slices.InfoFragment;
import com.android.tv.twopanelsettings.slices.SliceFragment;
import com.android.tv.twopanelsettings.slices.SlicePreference;
import com.android.tv.twopanelsettings.slices.SliceSeekbarPreference;
import com.android.tv.twopanelsettings.slices.SliceSwitchPreference;
import java.util.Map;
import java.util.Set;

/* loaded from: classes.dex */
public abstract class TwoPanelSettingsFragment extends Fragment implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback, PreferenceFragmentCompat.OnPreferenceStartScreenCallback, PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback {
    private static final boolean DEFAULT_CHECK_SCROLL_STATE;
    private static final long PREVIEW_PANEL_DEFAULT_DELAY_MS;
    private static final int[] frameResIds = {R$id.frame1, R$id.frame2, R$id.frame3, R$id.frame4, R$id.frame5, R$id.frame6, R$id.frame7, R$id.frame8, R$id.frame9, R$id.frame10};
    private AudioManager mAudioManager;
    private boolean mCheckVerticalGridViewScrollState;
    private Preference mFocusedPreference;
    private Handler mHandler;
    private boolean mIsNavigatingBack;
    private int mMaxScrollX;
    private int mPrefPanelIdx;
    private HorizontalScrollView mScrollView;
    private long mPreviewPanelCreationDelay = 0;
    private final RootViewOnKeyListener mRootViewOnKeyListener = new RootViewOnKeyListener();
    private boolean mIsWaitingForUpdatingPreview = false;
    private final Map<VerticalGridView, OnChildViewHolderSelectedListenerTwoPanel> mHasOnChildViewHolderSelectedListener = new ArrayMap();
    private final BroadcastReceiver mPreviewPanelDelayReceiver = new BroadcastReceiver() { // from class: com.android.tv.twopanelsettings.TwoPanelSettingsFragment.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            long longExtra = intent.getLongExtra("delay_ms", TwoPanelSettingsFragment.PREVIEW_PANEL_DEFAULT_DELAY_MS);
            boolean booleanExtra = intent.getBooleanExtra("check_scroll_state", TwoPanelSettingsFragment.DEFAULT_CHECK_SCROLL_STATE);
            Log.d("TwoPanelSettingsFragment", "New delay for creating preview panel fragment " + longExtra + " check scroll state " + booleanExtra);
            TwoPanelSettingsFragment.this.mPreviewPanelCreationDelay = longExtra;
            TwoPanelSettingsFragment.this.mCheckVerticalGridViewScrollState = booleanExtra;
        }
    };
    private final ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() { // from class: com.android.tv.twopanelsettings.TwoPanelSettingsFragment.2
        @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
        public void onGlobalLayout() {
            if (TwoPanelSettingsFragment.this.getView() == null || TwoPanelSettingsFragment.this.getView().getViewTreeObserver() == null) {
                return;
            }
            TwoPanelSettingsFragment.this.getView().getViewTreeObserver().removeOnGlobalLayoutListener(TwoPanelSettingsFragment.this.mOnGlobalLayoutListener);
            TwoPanelSettingsFragment twoPanelSettingsFragment = TwoPanelSettingsFragment.this;
            twoPanelSettingsFragment.moveToPanel(twoPanelSettingsFragment.mPrefPanelIdx, false);
        }
    };

    public interface NavigationCallback {
        boolean canNavigateBackOnDPAD();

        void onNavigateBack();

        void onNavigateToPreview();
    }

    public interface PreviewableComponentCallback {
        void onArriveAtMainPanel(boolean z);
    }

    public interface SliceFragmentCallback {
        void onPreferenceFocused(Preference preference);

        void onSeekbarPreferenceChanged(SliceSeekbarPreference sliceSeekbarPreference, int i);
    }

    public abstract void onPreferenceStartInitialScreen();

    static {
        PREVIEW_PANEL_DEFAULT_DELAY_MS = ActivityManager.isLowRamDeviceStatic() ? 100L : 0L;
        DEFAULT_CHECK_SCROLL_STATE = ActivityManager.isLowRamDeviceStatic();
    }

    private class OnChildViewHolderSelectedListenerTwoPanel extends OnChildViewHolderSelectedListener {
        private final int mPaneLIndex;

        @Override // androidx.leanback.widget.OnChildViewHolderSelectedListener
        public void onChildViewHolderSelectedAndPositioned(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int i, int i2) {
        }

        OnChildViewHolderSelectedListenerTwoPanel(int i) {
            this.mPaneLIndex = i;
        }

        @Override // androidx.leanback.widget.OnChildViewHolderSelectedListener
        public void onChildViewHolderSelected(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int i, int i2) {
            if (recyclerView == null || viewHolder == null) {
                return;
            }
            int adapterPosition = viewHolder.getAdapterPosition();
            PreferenceGroupAdapter preferenceGroupAdapter = (PreferenceGroupAdapter) recyclerView.getAdapter();
            if (preferenceGroupAdapter != null) {
                TwoPanelSettingsFragment.this.onPreferenceFocused(preferenceGroupAdapter.getItem(adapterPosition), this.mPaneLIndex);
            }
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mCheckVerticalGridViewScrollState = getContext().getResources().getBoolean(R$bool.config_check_scroll_state);
        this.mPreviewPanelCreationDelay = getContext().getResources().getInteger(R$integer.config_preview_panel_create_delay);
        updatePreviewPanelCreationDelayForLowRamDevice();
        this.mAudioManager = (AudioManager) getContext().getSystemService("audio");
    }

    private void updatePreviewPanelCreationDelayForLowRamDevice() {
        if (ActivityManager.isLowRamDeviceStatic() && this.mPreviewPanelCreationDelay == 0) {
            this.mPreviewPanelCreationDelay = PREVIEW_PANEL_DEFAULT_DELAY_MS;
        }
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R$layout.two_panel_settings_fragment, viewGroup, false);
        this.mScrollView = (HorizontalScrollView) inflate.findViewById(R$id.scrollview);
        this.mHandler = new Handler();
        if (bundle != null) {
            this.mPrefPanelIdx = bundle.getInt("com.android.tv.twopanelsettings.PREF_PANEL_IDX", this.mPrefPanelIdx);
            inflate.getViewTreeObserver().addOnGlobalLayoutListener(this.mOnGlobalLayoutListener);
        }
        this.mMaxScrollX = computeMaxRightScroll();
        return inflate;
    }

    @Override // androidx.fragment.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putInt("com.android.tv.twopanelsettings.PREF_PANEL_IDX", this.mPrefPanelIdx);
        super.onSaveInstanceState(bundle);
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        if (bundle == null) {
            onPreferenceStartInitialScreen();
        }
    }

    private boolean isPreferenceFragment(String str) {
        try {
            return LeanbackPreferenceFragmentCompat.class.isAssignableFrom(Class.forName(str));
        } catch (ClassNotFoundException e) {
            Log.e("TwoPanelSettingsFragment", "Fragment class not found " + e);
            return false;
        }
    }

    private boolean isInfoFragment(String str) {
        try {
            return InfoFragment.class.isAssignableFrom(Class.forName(str));
        } catch (ClassNotFoundException e) {
            Log.e("TwoPanelSettingsFragment", "Fragment class not found " + e);
            return false;
        }
    }

    @Override // androidx.preference.PreferenceFragmentCompat.OnPreferenceStartFragmentCallback
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat preferenceFragmentCompat, Preference preference) {
        if (preference == null || preference.getFragment() == null) {
            return false;
        }
        Fragment findFragmentById = getChildFragmentManager().findFragmentById(frameResIds[this.mPrefPanelIdx + 1]);
        if (findFragmentById != null && !(findFragmentById instanceof DummyFragment)) {
            if (!(findFragmentById instanceof InfoFragment) && !this.mIsWaitingForUpdatingPreview) {
                navigateToPreviewFragment();
            }
        } else {
            if (preference instanceof SlicePreference) {
                return false;
            }
            try {
                Fragment instantiate = Fragment.instantiate(getActivity(), preference.getFragment(), preference.getExtras());
                if (instantiate instanceof GuidedStepSupportFragment) {
                    startImmersiveFragment(instantiate);
                }
            } catch (Exception e) {
                Log.e("TwoPanelSettingsFragment", "error trying to instantiate fragment " + e);
                return true;
            }
        }
        return true;
    }

    public void navigateBack() {
        back(false);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void navigateToPreviewFragment() {
        FragmentManager childFragmentManager = getChildFragmentManager();
        int[] iArr = frameResIds;
        Fragment findFragmentById = childFragmentManager.findFragmentById(iArr[this.mPrefPanelIdx + 1]);
        if (findFragmentById instanceof NavigationCallback) {
            ((NavigationCallback) findFragmentById).onNavigateToPreview();
        }
        if (findFragmentById == 0 || (findFragmentById instanceof DummyFragment)) {
            return;
        }
        if (this.mPrefPanelIdx + 1 >= iArr.length) {
            Log.w("TwoPanelSettingsFragment", "Maximum level of depth reached.");
            return;
        }
        Fragment initialPreviewFragment = getInitialPreviewFragment(findFragmentById);
        if (initialPreviewFragment == null) {
            initialPreviewFragment = new DummyFragment();
        }
        initialPreviewFragment.setExitTransition(null);
        if (findFragmentById.getView() != null) {
            findFragmentById.getView().setImportantForAccessibility(1);
        }
        this.mPrefPanelIdx++;
        addOrRemovePreferenceFocusedListener(getChildFragmentManager().findFragmentById(iArr[this.mPrefPanelIdx]), true);
        FragmentTransaction beginTransaction = getChildFragmentManager().beginTransaction();
        beginTransaction.replace(iArr[this.mPrefPanelIdx + 1], initialPreviewFragment, "com.android.tv.settings.TwoPanelSettingsFragment.PREVIEW_FRAGMENT");
        beginTransaction.commit();
        moveToPanel(this.mPrefPanelIdx, true);
        removeFragmentAndAddToBackStack(this.mPrefPanelIdx - 1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isA11yOn() {
        return getActivity() != null && Settings.Secure.getInt(getActivity().getContentResolver(), "accessibility_enabled", 0) == 1;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: updateAccessibilityTitle, reason: merged with bridge method [inline-methods] */
    public void lambda$startImmersiveFragment$0(Fragment fragment) {
        CharSequence charSequence;
        if (fragment instanceof SliceFragment) {
            charSequence = ((SliceFragment) fragment).getScreenTitle();
        } else if (fragment instanceof LeanbackPreferenceFragmentCompat) {
            charSequence = ((LeanbackPreferenceFragmentCompat) fragment).getPreferenceScreen().getTitle();
        } else {
            if ((fragment instanceof GuidedStepSupportFragment) && fragment.getView() != null) {
                View findViewById = fragment.getView().findViewById(R$id.guidance_title);
                if (findViewById instanceof TextView) {
                    charSequence = ((TextView) findViewById).getText();
                }
            }
            charSequence = "";
        }
        if (TextUtils.isEmpty(charSequence)) {
            return;
        }
        getActivity().getWindow().setTitle(charSequence);
        if (getView() != null) {
            View view = getView();
            int i = R$id.two_panel_fragment_container;
            if (view.findViewById(i) != null) {
                getView().findViewById(i).setAccessibilityPaneTitle(charSequence);
            }
        }
    }

    private void addOrRemovePreferenceFocusedListener(Fragment fragment, boolean z) {
        VerticalGridView verticalGridView;
        if ((fragment instanceof LeanbackPreferenceFragmentCompat) && (verticalGridView = (VerticalGridView) ((LeanbackPreferenceFragmentCompat) fragment).getListView()) != null) {
            if (z) {
                if (this.mHasOnChildViewHolderSelectedListener.containsKey(verticalGridView)) {
                    return;
                }
                OnChildViewHolderSelectedListenerTwoPanel onChildViewHolderSelectedListenerTwoPanel = new OnChildViewHolderSelectedListenerTwoPanel(this.mPrefPanelIdx);
                verticalGridView.addOnChildViewHolderSelectedListener(onChildViewHolderSelectedListenerTwoPanel);
                this.mHasOnChildViewHolderSelectedListener.put(verticalGridView, onChildViewHolderSelectedListenerTwoPanel);
                return;
            }
            if (this.mHasOnChildViewHolderSelectedListener.containsKey(verticalGridView)) {
                verticalGridView.removeOnChildViewHolderSelectedListener(this.mHasOnChildViewHolderSelectedListener.get(verticalGridView));
                this.mHasOnChildViewHolderSelectedListener.remove(verticalGridView);
            }
        }
    }

    @Override // androidx.preference.PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback
    public boolean onPreferenceDisplayDialog(PreferenceFragmentCompat preferenceFragmentCompat, Preference preference) {
        if (preference == null) {
            return false;
        }
        if (preferenceFragmentCompat == null) {
            throw new IllegalArgumentException("Cannot display dialog for preference " + preference + ", Caller must not be null!");
        }
        LifecycleOwner findFragmentById = getChildFragmentManager().findFragmentById(frameResIds[this.mPrefPanelIdx + 1]);
        if (findFragmentById == null || (findFragmentById instanceof DummyFragment)) {
            return false;
        }
        if (findFragmentById instanceof NavigationCallback) {
            ((NavigationCallback) findFragmentById).onNavigateToPreview();
        }
        int i = this.mPrefPanelIdx + 1;
        this.mPrefPanelIdx = i;
        moveToPanel(i, true);
        removeFragmentAndAddToBackStack(this.mPrefPanelIdx - 1);
        return true;
    }

    private boolean equalArguments(Bundle bundle, Bundle bundle2) {
        if (bundle == null && bundle2 == null) {
            return true;
        }
        if (bundle == null || bundle2 == null) {
            return false;
        }
        Set<String> keySet = bundle.keySet();
        Set<String> keySet2 = bundle2.keySet();
        if (bundle.size() != bundle2.size() || !keySet.containsAll(keySet2)) {
            return false;
        }
        for (String str : keySet) {
            if (bundle.get(str) != null || bundle2.get(str) != null) {
                if (bundle.get(str) == null || bundle2.get(str) == null || !bundle.get(str).equals(bundle2.get(str))) {
                    return false;
                }
            }
        }
        return true;
    }

    protected void onPreferenceFocused(Preference preference, int i) {
        onPreferenceFocusedImpl(preference, false, i);
    }

    private void onPreferenceFocusedImpl(Preference preference, boolean z, int i) {
        if (preference == null) {
            return;
        }
        LifecycleOwner findFragmentById = getChildFragmentManager().findFragmentById(frameResIds[this.mPrefPanelIdx]);
        if (findFragmentById instanceof SliceFragmentCallback) {
            ((SliceFragmentCallback) findFragmentById).onPreferenceFocused(preference);
        }
        this.mFocusedPreference = preference;
        if (this.mCheckVerticalGridViewScrollState || this.mPreviewPanelCreationDelay > 0) {
            this.mIsWaitingForUpdatingPreview = true;
            this.mHandler.postDelayed(new PostShowPreviewRunnable((VerticalGridView) ((LeanbackPreferenceFragmentCompat) findFragmentById).getListView(), preference, z, i), this.mPreviewPanelCreationDelay);
            return;
        }
        handleFragmentTransactionWhenFocused(preference, z, i);
    }

    private final class PostShowPreviewRunnable implements Runnable {
        private final boolean mForceFresh;
        private final VerticalGridView mListView;
        private final int mPanelIndex;
        private final Preference mPref;

        PostShowPreviewRunnable(VerticalGridView verticalGridView, Preference preference, boolean z, int i) {
            this.mListView = verticalGridView;
            this.mPref = preference;
            this.mForceFresh = z;
            this.mPanelIndex = i;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.mPref == TwoPanelSettingsFragment.this.mFocusedPreference) {
                if (this.mListView.getScrollState() != 0) {
                    TwoPanelSettingsFragment.this.mHandler.postDelayed(this, 100L);
                } else {
                    TwoPanelSettingsFragment.this.handleFragmentTransactionWhenFocused(this.mPref, this.mForceFresh, this.mPanelIndex);
                    TwoPanelSettingsFragment.this.mIsWaitingForUpdatingPreview = false;
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFragmentTransactionWhenFocused(Preference preference, boolean z, int i) {
        Fragment fragment;
        if (isAdded() && i == this.mPrefPanelIdx) {
            try {
                fragment = onCreatePreviewFragment(getChildFragmentManager().findFragmentById(frameResIds[this.mPrefPanelIdx]), preference);
            } catch (Exception e) {
                Log.w("TwoPanelSettingsFragment", "Cannot instantiate the fragment from preference: " + preference, e);
                fragment = null;
            }
            if (fragment == null) {
                fragment = new DummyFragment();
            }
            FragmentManager childFragmentManager = getChildFragmentManager();
            int[] iArr = frameResIds;
            Fragment findFragmentById = childFragmentManager.findFragmentById(iArr[this.mPrefPanelIdx + 1]);
            if (findFragmentById != null && findFragmentById.getClass().equals(fragment.getClass()) && equalArguments(findFragmentById.getArguments(), fragment.getArguments())) {
                if (isRTL() && this.mScrollView.getScrollX() == 0 && this.mPrefPanelIdx == 0 && getView() != null && getView().getViewTreeObserver() != null) {
                    getView().getViewTreeObserver().addOnGlobalLayoutListener(this.mOnGlobalLayoutListener);
                }
                if (!z) {
                    return;
                }
            }
            if (findFragmentById != null) {
                findFragmentById.setExitTransition(null);
            }
            fragment.setEnterTransition(new Fade());
            fragment.setExitTransition(null);
            FragmentTransaction beginTransaction = getChildFragmentManager().beginTransaction();
            beginTransaction.setCustomAnimations(R$animator.fade_in_preview_panel, R$animator.fade_out_preview_panel);
            beginTransaction.replace(iArr[this.mPrefPanelIdx + 1], fragment);
            beginTransaction.commitNow();
            if (getView() == null || getView().getViewTreeObserver() == null) {
                return;
            }
            getView().getViewTreeObserver().addOnGlobalLayoutListener(this.mOnGlobalLayoutListener);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean onSeekbarPreferenceChanged(SliceSeekbarPreference sliceSeekbarPreference, int i) {
        LifecycleOwner findFragmentById = getChildFragmentManager().findFragmentById(frameResIds[this.mPrefPanelIdx]);
        if (!(findFragmentById instanceof SliceFragmentCallback)) {
            return true;
        }
        ((SliceFragmentCallback) findFragmentById).onSeekbarPreferenceChanged(sliceSeekbarPreference, i);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isRTL() {
        return getResources().getConfiguration().getLayoutDirection() == 1;
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.tv.settings.PREVIEW_DELAY");
        getContext().registerReceiver(this.mPreviewPanelDelayReceiver, intentFilter);
        TwoPanelSettingsRootView twoPanelSettingsRootView = (TwoPanelSettingsRootView) getView();
        if (twoPanelSettingsRootView != null) {
            twoPanelSettingsRootView.setOnBackKeyListener(this.mRootViewOnKeyListener);
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(this.mPreviewPanelDelayReceiver);
        TwoPanelSettingsRootView twoPanelSettingsRootView = (TwoPanelSettingsRootView) getView();
        if (twoPanelSettingsRootView != null) {
            twoPanelSettingsRootView.setOnBackKeyListener(null);
        }
    }

    public void startImmersiveFragment(final Fragment fragment) {
        addOrRemovePreferenceFocusedListener(fragment, true);
        FragmentTransaction beginTransaction = getChildFragmentManager().beginTransaction();
        Fragment findFragmentById = getChildFragmentManager().findFragmentById(frameResIds[this.mPrefPanelIdx]);
        fragment.setTargetFragment(findFragmentById, 0);
        beginTransaction.add(R$id.two_panel_fragment_container, fragment).remove(findFragmentById).addToBackStack(null).commit();
        this.mHandler.post(new Runnable() { // from class: com.android.tv.twopanelsettings.TwoPanelSettingsFragment$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                TwoPanelSettingsFragment.this.lambda$startImmersiveFragment$0(fragment);
            }
        });
    }

    public static class DummyFragment extends Fragment {
        @Override // androidx.fragment.app.Fragment
        public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
            return layoutInflater.inflate(R$layout.dummy_fragment, viewGroup, false);
        }
    }

    private class RootViewOnKeyListener implements View.OnKeyListener {
        private RootViewOnKeyListener() {
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // android.view.View.OnKeyListener
        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            Fragment findFragmentById = TwoPanelSettingsFragment.this.getChildFragmentManager().findFragmentById(TwoPanelSettingsFragment.frameResIds[TwoPanelSettingsFragment.this.mPrefPanelIdx]);
            if (keyEvent.getAction() == 0 && (i == 22 || i == 21)) {
                Preference chosenPreference = TwoPanelSettingsFragment.getChosenPreference(findFragmentById);
                if (chosenPreference instanceof SliceSeekbarPreference) {
                    SliceSeekbarPreference sliceSeekbarPreference = (SliceSeekbarPreference) chosenPreference;
                    if (i == 22) {
                        TwoPanelSettingsFragment.this.onSeekbarPreferenceChanged(sliceSeekbarPreference, 1);
                    } else {
                        TwoPanelSettingsFragment.this.onSeekbarPreferenceChanged(sliceSeekbarPreference, -1);
                    }
                    return true;
                }
            }
            if (keyEvent.getAction() == 0 && i == 4) {
                return TwoPanelSettingsFragment.this.back(true);
            }
            if (keyEvent.getAction() == 0 && ((!TwoPanelSettingsFragment.this.isRTL() && i == 21) || (TwoPanelSettingsFragment.this.isRTL() && i == 22))) {
                if (!(findFragmentById instanceof NavigationCallback) || ((NavigationCallback) findFragmentById).canNavigateBackOnDPAD()) {
                    return TwoPanelSettingsFragment.this.back(false);
                }
                return false;
            }
            if (keyEvent.getAction() != 0 || ((TwoPanelSettingsFragment.this.isRTL() || i != 22) && !(TwoPanelSettingsFragment.this.isRTL() && i == 21))) {
                return false;
            }
            if (TwoPanelSettingsFragment.this.shouldPerformClick()) {
                view.dispatchKeyEvent(new KeyEvent(0, 23));
                view.dispatchKeyEvent(new KeyEvent(1, 23));
            } else if (!(TwoPanelSettingsFragment.this.getChildFragmentManager().findFragmentById(TwoPanelSettingsFragment.frameResIds[TwoPanelSettingsFragment.this.mPrefPanelIdx + 1]) instanceof InfoFragment) && !TwoPanelSettingsFragment.this.mIsWaitingForUpdatingPreview) {
                TwoPanelSettingsFragment.this.mAudioManager.playSoundEffect(4);
                TwoPanelSettingsFragment.this.navigateToPreviewFragment();
            }
            return !(findFragmentById instanceof NavigationCallback);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean shouldPerformClick() {
        Preference chosenPreference = getChosenPreference(getChildFragmentManager().findFragmentById(frameResIds[this.mPrefPanelIdx]));
        if (chosenPreference == null) {
            return false;
        }
        if (chosenPreference.getIntent() != null && !TextUtils.isEmpty(chosenPreference.getFragment())) {
            return true;
        }
        if (!(chosenPreference instanceof SlicePreference)) {
            return false;
        }
        SlicePreference slicePreference = (SlicePreference) chosenPreference;
        return (slicePreference.getSliceAction() == null || slicePreference.getUri() == null) ? false : true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean back(final boolean z) {
        View view;
        if (this.mIsNavigatingBack) {
            this.mHandler.postDelayed(new Runnable() { // from class: com.android.tv.twopanelsettings.TwoPanelSettingsFragment.3
                @Override // java.lang.Runnable
                public void run() {
                    TwoPanelSettingsFragment.this.back(z);
                }
            }, 400L);
            return true;
        }
        if (getChildFragmentManager().findFragmentById(R$id.two_panel_fragment_container) != null) {
            getChildFragmentManager().popBackStack();
            moveToPanel(this.mPrefPanelIdx, false);
            return true;
        }
        FragmentManager childFragmentManager = getChildFragmentManager();
        int[] iArr = frameResIds;
        Fragment findFragmentById = childFragmentManager.findFragmentById(iArr[this.mPrefPanelIdx + 1]);
        if (isA11yOn() && (findFragmentById instanceof InfoFragment) && findFragmentById.getView() != null && findFragmentById.getView().hasFocus() && (view = getChildFragmentManager().findFragmentById(iArr[this.mPrefPanelIdx]).getView()) != null) {
            view.requestFocus();
            return true;
        }
        if (this.mPrefPanelIdx < 1) {
            if (z) {
                getActivity().finish();
            }
            return true;
        }
        this.mIsNavigatingBack = true;
        getChildFragmentManager().popBackStack();
        this.mPrefPanelIdx--;
        this.mHandler.postDelayed(new Runnable() { // from class: com.android.tv.twopanelsettings.TwoPanelSettingsFragment$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                TwoPanelSettingsFragment.this.lambda$back$1(z);
            }
        }, 200L);
        this.mHandler.postDelayed(new Runnable() { // from class: com.android.tv.twopanelsettings.TwoPanelSettingsFragment$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                TwoPanelSettingsFragment.this.lambda$back$2();
            }
        }, 400L);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$back$1(boolean z) {
        if (z) {
            this.mAudioManager.playSoundEffect(10);
        } else {
            this.mAudioManager.playSoundEffect(3);
        }
        moveToPanel(this.mPrefPanelIdx, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$back$2() {
        removeFragment(this.mPrefPanelIdx + 2);
        this.mIsNavigatingBack = false;
        LifecycleOwner findFragmentById = getChildFragmentManager().findFragmentById(frameResIds[this.mPrefPanelIdx + 1]);
        if (findFragmentById instanceof NavigationCallback) {
            ((NavigationCallback) findFragmentById).onNavigateBack();
        }
    }

    private void removeFragment(int i) {
        Fragment findFragmentById = getChildFragmentManager().findFragmentById(frameResIds[i]);
        if (findFragmentById != null) {
            getChildFragmentManager().beginTransaction().remove(findFragmentById).commit();
        }
    }

    private void removeFragmentAndAddToBackStack(int i) {
        Fragment findFragmentById;
        if (i >= 0 && (findFragmentById = getChildFragmentManager().findFragmentById(frameResIds[i])) != null) {
            findFragmentById.setExitTransition(new Fade());
            getChildFragmentManager().beginTransaction().remove(findFragmentById).addToBackStack("remove " + findFragmentById.getClass().getName()).commit();
        }
    }

    private int computeMaxRightScroll() {
        int dimensionPixelSize = getResources().getDimensionPixelSize(R$dimen.tp_settings_panes_width);
        int dimensionPixelSize2 = getResources().getDimensionPixelSize(R$dimen.tp_settings_preference_pane_width);
        int length = ((frameResIds.length * dimensionPixelSize2) - dimensionPixelSize) + (getResources().getDimensionPixelSize(R$dimen.preference_pane_extra_padding_start) * 2);
        if (length < 0) {
            return 0;
        }
        return length;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void moveToPanel(final int i, final boolean z) {
        this.mHandler.post(new Runnable() { // from class: com.android.tv.twopanelsettings.TwoPanelSettingsFragment$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                TwoPanelSettingsFragment.this.lambda$moveToPanel$3(i, z);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v13 */
    /* JADX WARN: Type inference failed for: r3v14, types: [boolean] */
    /* JADX WARN: Type inference failed for: r3v17 */
    public /* synthetic */ void lambda$moveToPanel$3(int i, boolean z) {
        int scrollX;
        int i2;
        View view;
        ?? r3;
        float f;
        if (isAdded()) {
            FragmentManager childFragmentManager = getChildFragmentManager();
            int[] iArr = frameResIds;
            final Fragment findFragmentById = childFragmentManager.findFragmentById(iArr[i]);
            int i3 = i + 1;
            Fragment findFragmentById2 = getChildFragmentManager().findFragmentById(iArr[i3]);
            int dimensionPixelSize = getResources().getDimensionPixelSize(R$dimen.tp_settings_preference_pane_width);
            View findViewById = getView().findViewById(iArr[i]);
            View findViewById2 = getView().findViewById(iArr[i3]);
            if (findViewById == null || findViewById2 == null) {
                return;
            }
            int i4 = R$id.decor_title_container;
            View findViewById3 = findViewById.findViewById(i4);
            View findViewById4 = findViewById2.findViewById(i4);
            boolean z2 = !isRTL() ? this.mScrollView.getScrollX() > dimensionPixelSize * i : this.mScrollView.getScrollX() < this.mMaxScrollX - (dimensionPixelSize * i);
            boolean z3 = (findFragmentById2 == null || (findFragmentById2 instanceof DummyFragment) || (findFragmentById2 instanceof InfoFragment)) ? false : true;
            int color = getResources().getColor(R$color.tp_preview_panel_background_color);
            int color2 = getResources().getColor(R$color.tp_preference_panel_background_color);
            if (z) {
                int i5 = isRTL() ? this.mMaxScrollX - (dimensionPixelSize * i) : dimensionPixelSize * i;
                int scrollX2 = i5 - this.mScrollView.getScrollX();
                HorizontalScrollView horizontalScrollView = this.mScrollView;
                ObjectAnimator ofInt = ObjectAnimator.ofInt(horizontalScrollView, "scrollX", horizontalScrollView.getScrollX(), i5);
                ofInt.setAutoCancel(true);
                ofInt.setDuration(1000L);
                ofInt.addListener(new AnimatorListenerAdapter() { // from class: com.android.tv.twopanelsettings.TwoPanelSettingsFragment.4
                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        Fragment fragment;
                        super.onAnimationEnd(animator);
                        if (!TwoPanelSettingsFragment.this.isA11yOn() || (fragment = findFragmentById) == null || fragment.getView() == null) {
                            return;
                        }
                        findFragmentById.getView().requestFocus();
                    }
                });
                Context context = getContext();
                int i6 = R$anim.easing_browse;
                ofInt.setInterpolator(AnimationUtils.loadInterpolator(context, i6));
                ofInt.start();
                if (z2) {
                    findViewById2.setAlpha(z3 ? 0.6f : 1.0f);
                    findViewById2.setBackgroundColor(color);
                    if (findViewById4 != null) {
                        findViewById4.setBackgroundColor(color);
                    }
                    ObjectAnimator ofFloat = ObjectAnimator.ofFloat(findViewById, "alpha", findViewById.getAlpha(), 1.0f);
                    ObjectAnimator ofObject = ObjectAnimator.ofObject(findViewById, "backgroundColor", new ArgbEvaluator(), Integer.valueOf(color), Integer.valueOf(color2));
                    ofFloat.setAutoCancel(true);
                    ofFloat.setDuration(200L);
                    ofObject.setAutoCancel(true);
                    ofObject.setDuration(500L);
                    AnimatorSet animatorSet = new AnimatorSet();
                    if (findViewById3 != null) {
                        ObjectAnimator ofObject2 = ObjectAnimator.ofObject(findViewById3, "backgroundColor", new ArgbEvaluator(), Integer.valueOf(color), Integer.valueOf(color2));
                        ofObject2.setAutoCancel(true);
                        ofObject2.setDuration(500L);
                        animatorSet.playTogether(ofFloat, ofObject, ofObject2);
                    } else {
                        animatorSet.playTogether(ofFloat, ofObject);
                    }
                    animatorSet.setInterpolator(AnimationUtils.loadInterpolator(getContext(), i6));
                    animatorSet.start();
                } else {
                    findViewById.setAlpha(1.0f);
                    findViewById.setBackgroundColor(color2);
                    if (findViewById3 != null) {
                        findViewById3.setBackgroundColor(color2);
                    }
                    float[] fArr = new float[2];
                    fArr[0] = findViewById2.getAlpha();
                    if (z3) {
                        r3 = 1;
                        f = 0.6f;
                    } else {
                        r3 = 1;
                        f = 1.0f;
                    }
                    fArr[r3] = f;
                    ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(findViewById2, "alpha", fArr);
                    ArgbEvaluator argbEvaluator = new ArgbEvaluator();
                    Object[] objArr = new Object[2];
                    objArr[0] = Integer.valueOf(color2);
                    objArr[r3] = Integer.valueOf(color);
                    ObjectAnimator ofObject3 = ObjectAnimator.ofObject(findViewById2, "backgroundColor", argbEvaluator, objArr);
                    ofFloat2.setAutoCancel(r3);
                    ofFloat2.setDuration(200L);
                    ofObject3.setAutoCancel(r3);
                    ofObject3.setDuration(500L);
                    AnimatorSet animatorSet2 = new AnimatorSet();
                    if (findViewById4 != null) {
                        ArgbEvaluator argbEvaluator2 = new ArgbEvaluator();
                        Object[] objArr2 = new Object[2];
                        objArr2[0] = Integer.valueOf(color2);
                        objArr2[r3] = Integer.valueOf(color);
                        ObjectAnimator ofObject4 = ObjectAnimator.ofObject(findViewById4, "backgroundColor", argbEvaluator2, objArr2);
                        ofObject4.setAutoCancel(r3);
                        ofObject4.setDuration(500L);
                        Animator[] animatorArr = new Animator[3];
                        animatorArr[0] = ofFloat2;
                        animatorArr[r3] = ofObject3;
                        animatorArr[2] = ofObject4;
                        animatorSet2.playTogether(animatorArr);
                    } else {
                        Animator[] animatorArr2 = new Animator[2];
                        animatorArr2[0] = ofFloat2;
                        animatorArr2[r3] = ofObject3;
                        animatorSet2.playTogether(animatorArr2);
                    }
                    animatorSet2.setInterpolator(AnimationUtils.loadInterpolator(getContext(), i6));
                    animatorSet2.start();
                }
                scrollX = scrollX2;
                i2 = 0;
            } else {
                int i7 = isRTL() ? this.mMaxScrollX - (dimensionPixelSize * i) : dimensionPixelSize * i;
                scrollX = i7 - this.mScrollView.getScrollX();
                i2 = 0;
                this.mScrollView.scrollTo(i7, 0);
                findViewById2.setAlpha(z3 ? 0.6f : 1.0f);
                findViewById2.setBackgroundColor(color);
                if (findViewById4 != null) {
                    findViewById4.setBackgroundColor(color);
                }
                findViewById.setAlpha(1.0f);
                findViewById.setBackgroundColor(color2);
                if (findViewById3 != null) {
                    findViewById3.setBackgroundColor(color2);
                }
            }
            if (findFragmentById == 0 || findFragmentById.getView() == null) {
                return;
            }
            if (!isA11yOn()) {
                findFragmentById.getView().requestFocus();
            }
            int length = iArr.length;
            for (int i8 = i2; i8 < length; i8++) {
                Fragment findFragmentById3 = getChildFragmentManager().findFragmentById(iArr[i8]);
                if (findFragmentById3 != null && (view = findFragmentById3.getView()) != null) {
                    view.setImportantForAccessibility((findFragmentById3 == findFragmentById || (findFragmentById3 instanceof InfoFragment)) ? 1 : 4);
                }
            }
            if (findFragmentById instanceof PreviewableComponentCallback) {
                if (scrollX > 0) {
                    ((PreviewableComponentCallback) findFragmentById).onArriveAtMainPanel(!isRTL());
                } else if (scrollX < 0) {
                    ((PreviewableComponentCallback) findFragmentById).onArriveAtMainPanel(isRTL());
                }
            }
            lambda$startImmersiveFragment$0(findFragmentById);
        }
    }

    private Fragment getInitialPreviewFragment(Fragment fragment) {
        if (!(fragment instanceof LeanbackPreferenceFragmentCompat)) {
            return null;
        }
        LeanbackPreferenceFragmentCompat leanbackPreferenceFragmentCompat = (LeanbackPreferenceFragmentCompat) fragment;
        if (leanbackPreferenceFragmentCompat.getListView() == null) {
            return null;
        }
        VerticalGridView verticalGridView = (VerticalGridView) leanbackPreferenceFragmentCompat.getListView();
        int selectedPosition = verticalGridView.getSelectedPosition();
        PreferenceGroupAdapter preferenceGroupAdapter = (PreferenceGroupAdapter) leanbackPreferenceFragmentCompat.getListView().getAdapter();
        if (preferenceGroupAdapter == null) {
            return null;
        }
        Preference item = preferenceGroupAdapter.getItem(selectedPosition);
        if (item == null || (verticalGridView.findViewHolderForPosition(selectedPosition) != null && !verticalGridView.findViewHolderForPosition(selectedPosition).itemView.hasFocusable())) {
            int i = 0;
            while (true) {
                if (i >= verticalGridView.getChildCount()) {
                    item = null;
                    break;
                }
                View childAt = verticalGridView.getChildAt(i);
                if (childAt.hasFocusable()) {
                    item = preferenceGroupAdapter.getItem(((PreferenceViewHolder) verticalGridView.getChildViewHolder(childAt)).getAdapterPosition());
                    break;
                }
                i++;
            }
        }
        if (item == null) {
            return null;
        }
        return onCreatePreviewFragment(fragment, item);
    }

    public void refocusPreference(Fragment fragment) {
        Preference chosenPreference;
        if (isFragmentInTheMainPanel(fragment) && (chosenPreference = getChosenPreference(fragment)) != null) {
            try {
                if (chosenPreference.getFragment() != null && InfoFragment.class.isAssignableFrom(Class.forName(chosenPreference.getFragment()))) {
                    updateInfoFragmentStatus(fragment);
                }
                if (chosenPreference instanceof ListPreference) {
                    refocusPreferenceForceRefresh(chosenPreference, fragment);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void refocusPreferenceForceRefresh(Preference preference, Fragment fragment) {
        if (isFragmentInTheMainPanel(fragment)) {
            onPreferenceFocusedImpl(preference, true, this.mPrefPanelIdx);
        }
    }

    public void showErrorMessage(String str, Fragment fragment) {
        FragmentManager childFragmentManager = getChildFragmentManager();
        int[] iArr = frameResIds;
        Fragment findFragmentById = childFragmentManager.findFragmentById(iArr[this.mPrefPanelIdx]);
        if (fragment == findFragmentById) {
            Preference preference = new Preference(getContext());
            updatePreferenceWithErrorMessage(preference, str, getContext());
            Fragment onCreatePreviewFragment = onCreatePreviewFragment(null, preference);
            FragmentTransaction beginTransaction = getChildFragmentManager().beginTransaction();
            beginTransaction.setCustomAnimations(R$animator.fade_in_preview_panel, R$animator.fade_out_preview_panel);
            beginTransaction.replace(iArr[this.mPrefPanelIdx], onCreatePreviewFragment);
            beginTransaction.commit();
            return;
        }
        Preference chosenPreference = getChosenPreference(findFragmentById);
        if (chosenPreference != null) {
            if (isA11yOn()) {
                appendErrorToContentDescription(findFragmentById, str);
            }
            updatePreferenceWithErrorMessage(chosenPreference, str, getContext());
            onPreferenceFocused(chosenPreference, this.mPrefPanelIdx);
        }
    }

    private static void updatePreferenceWithErrorMessage(Preference preference, String str, Context context) {
        preference.setFragment(InfoFragment.class.getCanonicalName());
        Bundle extras = preference.getExtras();
        extras.putParcelable("extra_preference_info_title_icon", Icon.createWithResource(context, R$drawable.slice_error_icon));
        extras.putCharSequence("extra_preference_info_text", context.getString(R$string.status_unavailable));
        extras.putCharSequence("extra_preference_info_summary", str);
    }

    private void appendErrorToContentDescription(Fragment fragment, String str) {
        Preference chosenPreference = getChosenPreference(fragment);
        String str2 = "";
        if (chosenPreference.getTitle() != null) {
            str2 = "" + chosenPreference.getTitle().toString();
        }
        String str3 = str2 + ", " + getString(R$string.status_unavailable) + ", " + str;
        if (chosenPreference instanceof SlicePreference) {
            ((SlicePreference) chosenPreference).setContentDescription(str3);
        } else if (chosenPreference instanceof SliceSwitchPreference) {
            ((SliceSwitchPreference) chosenPreference).setContentDescription(str3);
        } else if (chosenPreference instanceof CustomContentDescriptionPreference) {
            ((CustomContentDescriptionPreference) chosenPreference).setContentDescription(str3);
        }
        LeanbackPreferenceFragmentCompat leanbackPreferenceFragmentCompat = (LeanbackPreferenceFragmentCompat) fragment;
        if (leanbackPreferenceFragmentCompat.getListView() == null || leanbackPreferenceFragmentCompat.getListView().getAdapter() == null) {
            return;
        }
        leanbackPreferenceFragmentCompat.getListView().getAdapter().notifyDataSetChanged();
    }

    private void updateInfoFragmentStatus(Fragment fragment) {
        if (isFragmentInTheMainPanel(fragment)) {
            Fragment findFragmentById = getChildFragmentManager().findFragmentById(frameResIds[this.mPrefPanelIdx + 1]);
            if (findFragmentById instanceof InfoFragment) {
                ((InfoFragment) findFragmentById).updateInfoFragment();
            }
        }
    }

    public static Preference getChosenPreference(Fragment fragment) {
        if (!(fragment instanceof LeanbackPreferenceFragmentCompat)) {
            return null;
        }
        LeanbackPreferenceFragmentCompat leanbackPreferenceFragmentCompat = (LeanbackPreferenceFragmentCompat) fragment;
        if (leanbackPreferenceFragmentCompat.getListView() == null) {
            return null;
        }
        int selectedPosition = ((VerticalGridView) leanbackPreferenceFragmentCompat.getListView()).getSelectedPosition();
        PreferenceGroupAdapter preferenceGroupAdapter = (PreferenceGroupAdapter) leanbackPreferenceFragmentCompat.getListView().getAdapter();
        if (preferenceGroupAdapter != null) {
            return preferenceGroupAdapter.getItem(selectedPosition);
        }
        return null;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public Fragment onCreatePreviewFragment(Fragment fragment, Preference preference) {
        Fragment fragment2 = null;
        if (preference == 0) {
            return null;
        }
        if (preference.getFragment() != null) {
            if (!isInfoFragment(preference.getFragment()) && !isPreferenceFragment(preference.getFragment())) {
                return null;
            }
            if (isPreferenceFragment(preference.getFragment()) && (preference instanceof HasSliceUri)) {
                HasSliceUri hasSliceUri = (HasSliceUri) preference;
                if (hasSliceUri.getUri() == null || !isUriValid(hasSliceUri.getUri())) {
                    return null;
                }
                Bundle extras = preference.getExtras();
                extras.putString("TAG_TARGET_URI", hasSliceUri.getUri());
                extras.putCharSequence("TAG_SCREEN_TITLE", preference.getTitle());
            }
            return Fragment.instantiate(getActivity(), preference.getFragment(), preference.getExtras());
        }
        if (preference instanceof ListPreference) {
            fragment2 = TwoPanelListPreferenceDialogFragment.newInstanceSingle(preference.getKey());
        } else if (preference instanceof MultiSelectListPreference) {
            fragment2 = androidx.leanback.preference.LeanbackListPreferenceDialogFragmentCompat.newInstanceMulti(preference.getKey());
        }
        if (fragment2 != null && fragment != null) {
            fragment2.setTargetFragment(fragment, 0);
        }
        return fragment2;
    }

    private boolean isUriValid(String str) {
        ContentProviderClient acquireContentProviderClient;
        if (str == null || (acquireContentProviderClient = getContext().getContentResolver().acquireContentProviderClient(Uri.parse(str))) == null) {
            return false;
        }
        acquireContentProviderClient.close();
        return true;
    }

    public void addListenerForFragment(Fragment fragment) {
        if (isFragmentInTheMainPanel(fragment)) {
            addOrRemovePreferenceFocusedListener(fragment, true);
        }
    }

    public void removeListenerForFragment(Fragment fragment) {
        addOrRemovePreferenceFocusedListener(fragment, false);
    }

    public boolean isFragmentInTheMainPanel(Fragment fragment) {
        return fragment == getChildFragmentManager().findFragmentById(frameResIds[this.mPrefPanelIdx]);
    }
}
