package com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;
import androidx.leanback.widget.GuidedActionsStylist;
import androidx.lifecycle.ViewModelProviders;
import com.android.tv.settings.connectivity.WifiConfigHelper;
import com.control.device.R;
import com.control.device.settings.connectivity.State;
import com.control.device.settings.connectivity.StateMachine;
import com.control.device.settings.connectivity.UserChoiceInfo;
import java.util.List;

/* loaded from: classes.dex */
public class AmxKeyEnterSsidState implements State {
    private final FragmentActivity mActivity;
    private Fragment mFragment;

    public AmxKeyEnterSsidState(FragmentActivity fragmentActivity) {
        this.mActivity = fragmentActivity;
    }

    @Override // com.control.device.settings.connectivity.State
    public void processForward() {
        Log.i("AmxKeyEnterSsidState", "processForward");
        AmxKeyEnterSsidFragment amxKeyEnterSsidFragment = new AmxKeyEnterSsidFragment();
        this.mFragment = amxKeyEnterSsidFragment;
        State.FragmentChangeListener fragmentChangeListener = (State.FragmentChangeListener) this.mActivity;
        if (fragmentChangeListener != null) {
            fragmentChangeListener.onFragmentChange(amxKeyEnterSsidFragment, true);
        }
    }

    @Override // com.control.device.settings.connectivity.State
    public void processBackward() {
        Log.i("AmxKeyEnterSsidState", "processBackward");
        AmxKeyEnterSsidFragment amxKeyEnterSsidFragment = new AmxKeyEnterSsidFragment();
        this.mFragment = amxKeyEnterSsidFragment;
        State.FragmentChangeListener fragmentChangeListener = (State.FragmentChangeListener) this.mActivity;
        if (fragmentChangeListener != null) {
            fragmentChangeListener.onFragmentChange(amxKeyEnterSsidFragment, false);
        }
    }

    public static class AmxKeyEnterSsidFragment extends GuidedStepSupportFragment {
        private GuidedAction mAction;
        private StateMachine mStateMachine;
        private UserChoiceInfo mUserChoiceInfo;

        @Override // androidx.leanback.app.GuidedStepSupportFragment
        public GuidanceStylist onCreateGuidanceStylist() {
            return new GuidanceStylist() { // from class: com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net.AmxKeyEnterSsidState.AmxKeyEnterSsidFragment.1
                @Override // androidx.leanback.widget.GuidanceStylist
                public int onProvideLayoutId() {
                    return R.layout.amx_key_update_part2_network_list_guidance;
                }
            };
        }

        @Override // androidx.leanback.app.GuidedStepSupportFragment
        public GuidanceStylist.Guidance onCreateGuidance(Bundle bundle) {
            return new GuidanceStylist.Guidance(getString(R.string.title_ssid), null, null, null);
        }

        @Override // androidx.leanback.app.GuidedStepSupportFragment, androidx.fragment.app.Fragment
        public void onCreate(Bundle bundle) {
            this.mUserChoiceInfo = (UserChoiceInfo) ViewModelProviders.of(getActivity()).get(UserChoiceInfo.class);
            this.mStateMachine = (StateMachine) ViewModelProviders.of(getActivity()).get(StateMachine.class);
            super.onCreate(bundle);
        }

        @Override // androidx.leanback.app.GuidedStepSupportFragment
        public GuidedActionsStylist onCreateActionsStylist() {
            return new GuidedActionsStylist() { // from class: com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net.AmxKeyEnterSsidState.AmxKeyEnterSsidFragment.2
                @Override // androidx.leanback.widget.GuidedActionsStylist
                public int onProvideItemLayoutId() {
                    return R.layout.amx_key_update_setup_text_input_item;
                }
            };
        }

        @Override // androidx.leanback.app.GuidedStepSupportFragment
        public void onCreateActions(List<GuidedAction> list, Bundle bundle) {
            FragmentActivity activity = getActivity();
            String pageSummary = this.mUserChoiceInfo.getPageSummary(4);
            GuidedAction.Builder editable = new GuidedAction.Builder(activity).editable(true);
            if (pageSummary == null) {
                pageSummary = "";
            }
            GuidedAction build = editable.title(pageSummary).build();
            this.mAction = build;
            list.add(build);
        }

        @Override // androidx.fragment.app.Fragment
        public void onViewCreated(View view, Bundle bundle) {
            openInEditMode(this.mAction);
        }

        @Override // androidx.leanback.app.GuidedStepSupportFragment
        public long onGuidedActionEditedAndProceed(GuidedAction guidedAction) {
            if (TextUtils.isEmpty(guidedAction.getTitle())) {
                Log.i("AmxKeyEnterSsidState", "onGuidedActionEditedAndProceed, the title of action is null");
                return guidedAction.getId();
            }
            this.mUserChoiceInfo.put(4, guidedAction.getTitle().toString());
            CharSequence title = guidedAction.getTitle();
            if (!TextUtils.equals(this.mUserChoiceInfo.getWifiConfiguration().getPrintableSsid(), title)) {
                this.mUserChoiceInfo.removePageSummary(2);
            }
            WifiConfigHelper.setConfigSsid(this.mUserChoiceInfo.getWifiConfiguration(), title.toString());
            this.mStateMachine.getListener().onComplete(2);
            return guidedAction.getId();
        }
    }
}
