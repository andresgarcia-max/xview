package com.android.tv.twopanelsettings.slices;

import android.app.PendingIntent;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.Keep;
import androidx.lifecycle.Observer;
import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;
import androidx.slice.Slice;
import androidx.slice.widget.ListContent;
import com.android.tv.twopanelsettings.R$attr;
import com.android.tv.twopanelsettings.R$color;
import com.android.tv.twopanelsettings.R$dimen;
import com.android.tv.twopanelsettings.R$id;
import com.android.tv.twopanelsettings.R$layout;
import com.android.tv.twopanelsettings.TwoPanelSettingsFragment;
import com.android.tv.twopanelsettings.slices.PreferenceSliceLiveData;

@Keep
/* loaded from: classes.dex */
public class SliceFragment extends SettingsPreferenceFragment implements Observer<Slice>, TwoPanelSettingsFragment.SliceFragmentCallback {
    private static final int A11Y_FOCUS_REQUEST_DELAY = 1000;
    private static final String KEY_LAST_PREFERENCE = "key_last_preference";
    private static final String KEY_PREFERENCE_FOLLOWUP_INTENT = "key_preference_followup_intent";
    private static final String KEY_PREFERENCE_FOLLOWUP_RESULT_CODE = "key_preference_followup_result_code";
    private static final String KEY_SCREEN_ICON = "key_screen_icon";
    private static final String KEY_SCREEN_SUBTITLE = "key_screen_subtitle";
    private static final String KEY_SCREEN_TITLE = "key_screen_title";
    private static final String KEY_URI_STRING = "key_uri_string";
    private static final int SLICE_REQUEST_CODE = 10000;
    private static final String TAG = "SliceFragment";
    private ContextThemeWrapper mContextThemeWrapper;
    private int mCurrentPageId;
    private Intent mFollowupPendingIntentExtras;
    private Intent mFollowupPendingIntentExtrasCopy;
    private int mFollowupPendingIntentResultCode;
    private String mLastFocusedPreferenceKey;
    private ListContent mListContent;
    private PendingIntent mPreferenceFollowupIntent;
    private Icon mScreenIcon;
    private CharSequence mScreenSubtitle;
    private CharSequence mScreenTitle;
    private Slice mSlice;
    private String mUriString = null;
    private boolean mIsMainPanelReady = true;
    private final Handler mHandler = new Handler();
    private final ActivityResultLauncher<IntentSenderRequest> mActivityResultLauncher = registerForActivityResult(new ActivityResultContract<IntentSenderRequest, ActivityResult>() { // from class: androidx.activity.result.contract.ActivityResultContracts$StartIntentSenderForResult
        @Override // androidx.activity.result.contract.ActivityResultContract
        public Intent createIntent(Context context, IntentSenderRequest intentSenderRequest) {
            return new Intent("androidx.activity.result.contract.action.INTENT_SENDER_REQUEST").putExtra("androidx.activity.result.contract.extra.INTENT_SENDER_REQUEST", intentSenderRequest);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // androidx.activity.result.contract.ActivityResultContract
        public ActivityResult parseResult(int i, Intent intent) {
            return new ActivityResult(i, intent);
        }
    }, new ActivityResultCallback<ActivityResult>() { // from class: com.android.tv.twopanelsettings.slices.SliceFragment.1
        @Override // androidx.activity.result.ActivityResultCallback
        public void onActivityResult(ActivityResult activityResult) {
            Intent data = activityResult.getData();
            SliceFragment.this.mFollowupPendingIntentExtras = data;
            SliceFragment.this.mFollowupPendingIntentExtrasCopy = data == null ? null : new Intent(data);
            SliceFragment.this.mFollowupPendingIntentResultCode = activityResult.getResultCode();
        }
    });
    private final ContentObserver mContentObserver = new ContentObserver(new Handler()) { // from class: com.android.tv.twopanelsettings.slices.SliceFragment.2
        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            SliceFragment.this.handleUri(uri);
            super.onChange(z, uri);
        }
    };

    public interface OnePanelSliceFragmentContainer {
        void navigateBack();
    }

    @Deprecated
    public int getMetricsCategory() {
        return 0;
    }

    @Override // com.android.tv.twopanelsettings.slices.SettingsPreferenceFragment, androidx.preference.PreferenceFragmentCompat, androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        this.mUriString = getArguments().getString("TAG_TARGET_URI");
        ContextSingleton.getInstance().grantFullAccess(getContext(), Uri.parse(this.mUriString));
        super.onCreate(bundle);
    }

    @Override // com.android.tv.twopanelsettings.slices.SettingsPreferenceFragment, androidx.fragment.app.Fragment
    public void onResume() {
        setTitle(this.mScreenTitle);
        setSubtitle(this.mScreenSubtitle);
        setIcon(this.mScreenIcon);
        getPreferenceScreen().removeAll();
        showProgressBar();
        getSliceLiveData().observeForever(this);
        if (TextUtils.isEmpty(this.mScreenTitle)) {
            this.mScreenTitle = getArguments().getCharSequence("TAG_SCREEN_TITLE", "");
        }
        super.onResume();
        getContext().getContentResolver().registerContentObserver(SlicePreferencesUtil.getStatusPath(this.mUriString), false, this.mContentObserver);
        fireFollowupPendingIntent();
    }

    private PreferenceSliceLiveData.SliceLiveDataImpl getSliceLiveData() {
        return ContextSingleton.getInstance().getSliceLiveData(getActivity(), Uri.parse(this.mUriString));
    }

    private void fireFollowupPendingIntent() {
        Parcelable parcelable;
        if (this.mFollowupPendingIntentExtras == null) {
            return;
        }
        try {
            parcelable = this.mFollowupPendingIntentExtrasCopy.getParcelableExtra("extra_slice_followup");
        } catch (Throwable unused) {
            parcelable = null;
        }
        if (parcelable instanceof PendingIntent) {
            try {
                ((PendingIntent) parcelable).send();
                return;
            } catch (PendingIntent.CanceledException e) {
                Log.e(TAG, "Followup PendingIntent for slice cannot be sent", e);
                return;
            }
        }
        PendingIntent pendingIntent = this.mPreferenceFollowupIntent;
        if (pendingIntent == null) {
            return;
        }
        try {
            pendingIntent.send(getContext(), this.mFollowupPendingIntentResultCode, this.mFollowupPendingIntentExtras);
        } catch (PendingIntent.CanceledException e2) {
            Log.e(TAG, "Followup PendingIntent for slice cannot be sent", e2);
        }
        this.mPreferenceFollowupIntent = null;
    }

    @Override // com.android.tv.twopanelsettings.slices.SettingsPreferenceFragment, androidx.fragment.app.Fragment
    public void onPause() {
        super.onPause();
        hideProgressBar();
        getContext().getContentResolver().unregisterContentObserver(this.mContentObserver);
        getSliceLiveData().removeObserver(this);
    }

    @Override // androidx.preference.PreferenceFragmentCompat
    public void onCreatePreferences(Bundle bundle, String str) {
        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getContext()));
        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(R$attr.preferenceTheme, typedValue, true);
        this.mContextThemeWrapper = new ContextThemeWrapper(getActivity(), typedValue.resourceId);
    }

    private boolean isUriValid(String str) {
        ContentProviderClient acquireContentProviderClient;
        if (str == null || (acquireContentProviderClient = getContext().getContentResolver().acquireContentProviderClient(Uri.parse(str))) == null) {
            return false;
        }
        acquireContentProviderClient.close();
        return true;
    }

    /* JADX WARN: Removed duplicated region for block: B:16:0x0047  */
    /* JADX WARN: Removed duplicated region for block: B:19:0x007e  */
    /* JADX WARN: Removed duplicated region for block: B:22:0x00bd  */
    /* JADX WARN: Removed duplicated region for block: B:28:0x00dd  */
    /* JADX WARN: Removed duplicated region for block: B:45:0x011e  */
    /* JADX WARN: Removed duplicated region for block: B:48:0x0135  */
    /* JADX WARN: Removed duplicated region for block: B:51:0x0126  */
    /* JADX WARN: Removed duplicated region for block: B:54:0x0084  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private void update() {
        /*
            Method dump skipped, instructions count: 325
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.tv.twopanelsettings.slices.SliceFragment.update():void");
    }

    private void resetA11yFocusIfNeeded() {
        if (isA11yOn()) {
            this.mHandler.postDelayed(new Runnable() { // from class: com.android.tv.twopanelsettings.slices.SliceFragment$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    SliceFragment.this.lambda$resetA11yFocusIfNeeded$0();
                }
            }, 1000L);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$resetA11yFocusIfNeeded$0() {
        if (!isResumed() || getListView() == null || getListView().findFocus() == null) {
            return;
        }
        getListView().findFocus().requestAccessibilityFocus();
    }

    private boolean isA11yOn() {
        return getActivity() != null && Settings.Secure.getInt(getActivity().getContentResolver(), "accessibility_enabled", 0) == 1;
    }

    private void back() {
        if (getCallbackFragment() instanceof TwoPanelSettingsFragment) {
            TwoPanelSettingsFragment twoPanelSettingsFragment = (TwoPanelSettingsFragment) getCallbackFragment();
            if (twoPanelSettingsFragment.isFragmentInTheMainPanel(this)) {
                twoPanelSettingsFragment.navigateBack();
                return;
            }
            return;
        }
        if (getCallbackFragment() instanceof OnePanelSliceFragmentContainer) {
            ((OnePanelSliceFragmentContainer) getCallbackFragment()).navigateBack();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: forward, reason: merged with bridge method [inline-methods] */
    public void lambda$forward$1() {
        String str;
        if (this.mIsMainPanelReady) {
            if (getCallbackFragment() instanceof TwoPanelSettingsFragment) {
                TwoPanelSettingsFragment twoPanelSettingsFragment = (TwoPanelSettingsFragment) getCallbackFragment();
                Preference chosenPreference = TwoPanelSettingsFragment.getChosenPreference(this);
                if (chosenPreference == null && (str = this.mLastFocusedPreferenceKey) != null) {
                    chosenPreference = findPreference(str);
                }
                if (chosenPreference != null && (chosenPreference instanceof HasSliceUri) && ((HasSliceUri) chosenPreference).getUri() != null) {
                    chosenPreference.setFragment(SliceFragment.class.getCanonicalName());
                    twoPanelSettingsFragment.refocusPreferenceForceRefresh(chosenPreference, this);
                }
                if (twoPanelSettingsFragment.isFragmentInTheMainPanel(this)) {
                    twoPanelSettingsFragment.navigateToPreviewFragment();
                    return;
                }
                return;
            }
            return;
        }
        this.mHandler.post(new Runnable() { // from class: com.android.tv.twopanelsettings.slices.SliceFragment$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                SliceFragment.this.lambda$forward$1();
            }
        });
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:66:0x010c  */
    /* JADX WARN: Removed duplicated region for block: B:69:0x0112 A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private void updatePreferenceScreen(androidx.preference.PreferenceScreen r10, java.util.List<androidx.preference.Preference> r11) {
        /*
            Method dump skipped, instructions count: 332
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.tv.twopanelsettings.slices.SliceFragment.updatePreferenceScreen(androidx.preference.PreferenceScreen, java.util.List):void");
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

    @Override // com.android.tv.twopanelsettings.TwoPanelSettingsFragment.SliceFragmentCallback
    public void onPreferenceFocused(Preference preference) {
        setLastFocused(preference);
    }

    @Override // com.android.tv.twopanelsettings.TwoPanelSettingsFragment.SliceFragmentCallback
    public void onSeekbarPreferenceChanged(SliceSeekbarPreference sliceSeekbarPreference, int i) {
        int value = sliceSeekbarPreference.getValue();
        if ((i <= 0 || value >= sliceSeekbarPreference.getMax()) && (i >= 0 || value <= sliceSeekbarPreference.getMin())) {
            return;
        }
        sliceSeekbarPreference.setValue(value + i);
        try {
            firePendingIntent(sliceSeekbarPreference, new Intent().putExtra("android.app.slice.extra.SLIDER_VALUE", sliceSeekbarPreference.getValue()).putExtra("extra_preference_key", sliceSeekbarPreference.getKey()));
        } catch (Exception e) {
            Log.e(TAG, "PendingIntent for slice cannot be sent", e);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // androidx.preference.PreferenceFragmentCompat, androidx.preference.PreferenceManager.OnPreferenceTreeClickListener
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof SliceRadioPreference) {
            SliceRadioPreference sliceRadioPreference = (SliceRadioPreference) preference;
            if (!sliceRadioPreference.isChecked()) {
                sliceRadioPreference.setChecked(true);
                if (TextUtils.isEmpty(sliceRadioPreference.getUri())) {
                    return true;
                }
            }
            InstrumentationUtils.logEntrySelected(getPreferenceActionId(preference));
            boolean firePendingIntent = firePendingIntent(sliceRadioPreference, new Intent().putExtra("extra_preference_key", preference.getKey()));
            sliceRadioPreference.clearOtherRadioPreferences(getPreferenceScreen());
            if (firePendingIntent) {
                return true;
            }
        } else {
            if ((preference instanceof TwoStatePreference) && (preference instanceof HasSliceAction)) {
                boolean isChecked = ((TwoStatePreference) preference).isChecked();
                preference.getExtras().putBoolean("extra_preference_info_status", isChecked);
                if (getParentFragment() instanceof TwoPanelSettingsFragment) {
                    ((TwoPanelSettingsFragment) getParentFragment()).refocusPreference(this);
                }
                InstrumentationUtils.logToggleInteracted(getPreferenceActionId(preference), Boolean.valueOf(isChecked));
                firePendingIntent((HasSliceAction) preference, new Intent().putExtra("android.app.slice.extra.TOGGLE_STATE", isChecked).putExtra("extra_preference_key", preference.getKey()));
                return true;
            }
            if (preference instanceof SlicePreference) {
                if (getPreferenceActionId(preference) != 4) {
                    InstrumentationUtils.logEntrySelected(getPreferenceActionId(preference));
                }
                if (firePendingIntent((HasSliceAction) preference, new Intent().putExtra("extra_preference_key", preference.getKey()))) {
                    return true;
                }
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    private boolean firePendingIntent(HasSliceAction hasSliceAction, Intent intent) {
        if (hasSliceAction.getSliceAction() == null) {
            return false;
        }
        this.mActivityResultLauncher.launch(new IntentSenderRequest.Builder(hasSliceAction.getSliceAction().getAction().getIntentSender()).setFillInIntent(intent).build());
        if (hasSliceAction.getFollowupSliceAction() == null) {
            return true;
        }
        this.mPreferenceFollowupIntent = hasSliceAction.getFollowupSliceAction().getAction();
        return true;
    }

    @Override // com.android.tv.twopanelsettings.slices.SettingsPreferenceFragment, androidx.preference.PreferenceFragmentCompat, androidx.fragment.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putParcelable(KEY_PREFERENCE_FOLLOWUP_INTENT, this.mPreferenceFollowupIntent);
        bundle.putInt(KEY_PREFERENCE_FOLLOWUP_RESULT_CODE, this.mFollowupPendingIntentResultCode);
        bundle.putCharSequence(KEY_SCREEN_TITLE, this.mScreenTitle);
        bundle.putCharSequence(KEY_SCREEN_SUBTITLE, this.mScreenSubtitle);
        bundle.putParcelable(KEY_SCREEN_ICON, this.mScreenIcon);
        bundle.putString(KEY_LAST_PREFERENCE, this.mLastFocusedPreferenceKey);
        bundle.putString(KEY_URI_STRING, this.mUriString);
    }

    @Override // androidx.fragment.app.Fragment
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        if (bundle != null) {
            this.mPreferenceFollowupIntent = (PendingIntent) bundle.getParcelable(KEY_PREFERENCE_FOLLOWUP_INTENT);
            this.mFollowupPendingIntentResultCode = bundle.getInt(KEY_PREFERENCE_FOLLOWUP_RESULT_CODE);
            this.mScreenTitle = bundle.getCharSequence(KEY_SCREEN_TITLE);
            this.mScreenSubtitle = bundle.getCharSequence(KEY_SCREEN_SUBTITLE);
            this.mScreenIcon = (Icon) bundle.getParcelable(KEY_SCREEN_ICON);
            this.mLastFocusedPreferenceKey = bundle.getString(KEY_LAST_PREFERENCE);
            this.mUriString = bundle.getString(KEY_URI_STRING);
        }
    }

    @Override // androidx.lifecycle.Observer
    public void onChanged(Slice slice) {
        this.mSlice = slice;
        if (slice == null || slice.getHints() == null) {
            return;
        }
        if (slice.getHints().contains("partial")) {
            showProgressBar();
        } else {
            hideProgressBar();
        }
        this.mIsMainPanelReady = false;
        update();
    }

    private void showProgressBar() {
        View findViewById = getView() == null ? null : getView().findViewById(R$id.progress_bar);
        if (findViewById != null) {
            findViewById.bringToFront();
            findViewById.setVisibility(0);
        }
    }

    private void hideProgressBar() {
        View findViewById = getView() == null ? null : getView().findViewById(R$id.progress_bar);
        if (findViewById != null) {
            findViewById.setVisibility(8);
        }
    }

    private void setSubtitle(CharSequence charSequence) {
        View view = getView();
        TextView textView = view == null ? null : (TextView) view.findViewById(R$id.decor_subtitle);
        if (textView != null) {
            if (getResources().getConfiguration().getLayoutDirection() == 1) {
                textView.setGravity(53);
            }
            if (TextUtils.isEmpty(charSequence)) {
                textView.setVisibility(8);
            } else {
                textView.setVisibility(0);
                textView.setText(charSequence);
            }
        }
        this.mScreenSubtitle = charSequence;
    }

    private void setIcon(Icon icon) {
        View view = getView();
        ImageView imageView = view == null ? null : (ImageView) view.findViewById(R$id.decor_icon);
        if (imageView != null && icon != null) {
            TextView textView = (TextView) view.findViewById(R$id.decor_title);
            if (textView != null) {
                textView.setMaxWidth(getResources().getDimensionPixelSize(R$dimen.decor_title_width));
            }
            imageView.setImageDrawable(icon.loadDrawable(this.mContextThemeWrapper));
            imageView.setVisibility(0);
        } else if (imageView != null) {
            imageView.setVisibility(8);
        }
        this.mScreenIcon = icon;
    }

    @Override // androidx.leanback.preference.LeanbackPreferenceFragmentCompat, androidx.preference.PreferenceFragmentCompat, androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        ViewGroup viewGroup2 = (ViewGroup) super.onCreateView(layoutInflater, viewGroup, bundle);
        LayoutInflater from = LayoutInflater.from(viewGroup2.getContext());
        View inflate = from.inflate(R$layout.slice_title_container, viewGroup, false);
        viewGroup2.removeView(viewGroup2.findViewById(R$id.decor_title_container));
        viewGroup2.addView(inflate, 0);
        if (inflate != null) {
            inflate.setOutlineProvider(null);
            inflate.setBackgroundResource(R$color.tp_preference_panel_background_color);
        }
        View inflate2 = from.inflate(R$layout.slice_progress_bar, viewGroup, false);
        if (inflate2 != null) {
            ((ViewGroup) inflate2).addView(viewGroup2);
        }
        return inflate2;
    }

    public void setLastFocused(Preference preference) {
        this.mLastFocusedPreferenceKey = preference.getKey();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleUri(Uri uri) {
        String queryParameter;
        String queryParameter2 = uri.getQueryParameter("uri");
        String queryParameter3 = uri.getQueryParameter("error");
        if (queryParameter2 != null && queryParameter3 != null) {
            if (!queryParameter2.equals(this.mUriString)) {
                showErrorMessageAsToast(queryParameter3);
            } else {
                showErrorMessage(queryParameter3);
            }
        }
        if (queryParameter2 == null || !queryParameter2.equals(this.mUriString) || (queryParameter = uri.getQueryParameter("direction")) == null) {
            return;
        }
        if (queryParameter.equals("forward")) {
            lambda$forward$1();
        } else if (queryParameter.equals("back")) {
            back();
        } else if (queryParameter.equals("exit")) {
            finish();
        }
    }

    private void showErrorMessageAsToast(String str) {
        Toast.makeText(getActivity(), str, 0).show();
    }

    private void finish() {
        getActivity().setResult(-1);
        getActivity().finish();
    }

    private void showErrorMessage(String str) {
        if (getCallbackFragment() instanceof TwoPanelSettingsFragment) {
            ((TwoPanelSettingsFragment) getCallbackFragment()).showErrorMessage(str, this);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private int getPreferenceActionId(Preference preference) {
        if (!(preference instanceof HasSliceAction)) {
            return 4;
        }
        HasSliceAction hasSliceAction = (HasSliceAction) preference;
        if (hasSliceAction.getActionId() != 0) {
            return hasSliceAction.getActionId();
        }
        return 4;
    }

    public CharSequence getScreenTitle() {
        return this.mScreenTitle;
    }

    @Override // com.android.tv.twopanelsettings.slices.SettingsPreferenceFragment
    protected int getPageId() {
        int i = this.mCurrentPageId;
        if (i != 0) {
            return i;
        }
        return 3;
    }
}
