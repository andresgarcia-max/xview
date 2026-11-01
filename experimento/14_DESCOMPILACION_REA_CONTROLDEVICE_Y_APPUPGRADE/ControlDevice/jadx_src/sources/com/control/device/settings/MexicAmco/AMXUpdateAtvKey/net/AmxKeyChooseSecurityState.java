package com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net;

import android.os.Bundle;
import android.util.Log;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;
import androidx.lifecycle.ViewModelProviders;
import com.android.tv.settings.connectivity.WifiConfigHelper;
import com.control.device.R;
import com.control.device.settings.connectivity.State;
import com.control.device.settings.connectivity.StateMachine;
import com.control.device.settings.connectivity.UserChoiceInfo;
import java.util.List;

/* loaded from: classes.dex */
public class AmxKeyChooseSecurityState implements State {
    private final FragmentActivity mActivity;
    private Fragment mFragment;

    public AmxKeyChooseSecurityState(FragmentActivity fragmentActivity) {
        this.mActivity = fragmentActivity;
    }

    @Override // com.control.device.settings.connectivity.State
    public void processForward() {
        Log.i("AmxKeyChooseSecurityState", "processForward");
        AmxKeyChooseSecurityFragment amxKeyChooseSecurityFragment = new AmxKeyChooseSecurityFragment();
        this.mFragment = amxKeyChooseSecurityFragment;
        State.FragmentChangeListener fragmentChangeListener = (State.FragmentChangeListener) this.mActivity;
        if (fragmentChangeListener != null) {
            fragmentChangeListener.onFragmentChange(amxKeyChooseSecurityFragment, true);
        }
    }

    @Override // com.control.device.settings.connectivity.State
    public void processBackward() {
        Log.i("AmxKeyChooseSecurityState", "processBackward");
        AmxKeyChooseSecurityFragment amxKeyChooseSecurityFragment = new AmxKeyChooseSecurityFragment();
        this.mFragment = amxKeyChooseSecurityFragment;
        State.FragmentChangeListener fragmentChangeListener = (State.FragmentChangeListener) this.mActivity;
        if (fragmentChangeListener != null) {
            fragmentChangeListener.onFragmentChange(amxKeyChooseSecurityFragment, false);
        }
    }

    public static class AmxKeyChooseSecurityFragment extends GuidedStepSupportFragment {
        static final long WIFI_SECURITY_TYPE_NONE = 100001;
        static final long WIFI_SECURITY_TYPE_WEP = 100002;
        static final long WIFI_SECURITY_TYPE_WPA = 100003;
        private StateMachine mStateMachine;
        private UserChoiceInfo mUserChoiceInfo;

        @Override // androidx.leanback.app.GuidedStepSupportFragment
        public GuidanceStylist onCreateGuidanceStylist() {
            return new GuidanceStylist() { // from class: com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net.AmxKeyChooseSecurityState.AmxKeyChooseSecurityFragment.1
                @Override // androidx.leanback.widget.GuidanceStylist
                public int onProvideLayoutId() {
                    return R.layout.amx_key_update_part2_network_list_guidance;
                }
            };
        }

        @Override // androidx.leanback.app.GuidedStepSupportFragment
        public GuidanceStylist.Guidance onCreateGuidance(Bundle bundle) {
            return new GuidanceStylist.Guidance(getString(R.string.security_type), null, null, null);
        }

        @Override // androidx.leanback.app.GuidedStepSupportFragment, androidx.fragment.app.Fragment
        public void onCreate(Bundle bundle) {
            this.mUserChoiceInfo = (UserChoiceInfo) ViewModelProviders.of(getActivity()).get(UserChoiceInfo.class);
            this.mStateMachine = (StateMachine) ViewModelProviders.of(getActivity()).get(StateMachine.class);
            super.onCreate(bundle);
        }

        @Override // androidx.leanback.app.GuidedStepSupportFragment
        public void onCreateActions(List<GuidedAction> list, Bundle bundle) {
            FragmentActivity activity = getActivity();
            list.add(new GuidedAction.Builder(activity).title(R.string.wifi_security_type_none).id(WIFI_SECURITY_TYPE_NONE).build());
            list.add(new GuidedAction.Builder(activity).title(R.string.wifi_security_type_wep).id(WIFI_SECURITY_TYPE_WEP).build());
            list.add(new GuidedAction.Builder(activity).title(R.string.wifi_security_type_wpa).id(WIFI_SECURITY_TYPE_WPA).build());
        }

        @Override // androidx.leanback.app.GuidedStepSupportFragment
        public void onGuidedActionClicked(GuidedAction guidedAction) {
            if (guidedAction.getId() == WIFI_SECURITY_TYPE_NONE) {
                this.mUserChoiceInfo.setWifiSecurity(0);
            } else if (guidedAction.getId() == WIFI_SECURITY_TYPE_WEP) {
                this.mUserChoiceInfo.setWifiSecurity(1);
            } else if (guidedAction.getId() == WIFI_SECURITY_TYPE_WPA) {
                this.mUserChoiceInfo.setWifiSecurity(2);
            }
            WifiConfigHelper.setConfigKeyManagementBySecurity(this.mUserChoiceInfo.getWifiConfiguration(), this.mUserChoiceInfo.getWifiSecurity());
            if (guidedAction.getId() == WIFI_SECURITY_TYPE_NONE) {
                this.mStateMachine.getListener().onComplete(5);
            } else {
                this.mStateMachine.getListener().onComplete(7);
            }
        }
    }
}
