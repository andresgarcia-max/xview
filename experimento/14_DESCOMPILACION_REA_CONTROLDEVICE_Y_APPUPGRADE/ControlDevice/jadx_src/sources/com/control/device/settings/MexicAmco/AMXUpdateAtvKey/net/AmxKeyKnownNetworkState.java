package com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;
import androidx.lifecycle.ViewModelProviders;
import com.control.device.R;
import com.control.device.settings.connectivity.State;
import com.control.device.settings.connectivity.StateMachine;
import com.control.device.settings.connectivity.UserChoiceInfo;
import java.util.List;

/* loaded from: classes.dex */
public class AmxKeyKnownNetworkState implements State {
    private final FragmentActivity mActivity;
    private Fragment mFragment;

    public AmxKeyKnownNetworkState(FragmentActivity fragmentActivity) {
        this.mActivity = fragmentActivity;
    }

    @Override // com.control.device.settings.connectivity.State
    public void processForward() {
        AmxKeyKnownNetworkFragment amxKeyKnownNetworkFragment = new AmxKeyKnownNetworkFragment();
        this.mFragment = amxKeyKnownNetworkFragment;
        State.FragmentChangeListener fragmentChangeListener = (State.FragmentChangeListener) this.mActivity;
        if (fragmentChangeListener != null) {
            fragmentChangeListener.onFragmentChange(amxKeyKnownNetworkFragment, true);
        }
    }

    @Override // com.control.device.settings.connectivity.State
    public void processBackward() {
        AmxKeyKnownNetworkFragment amxKeyKnownNetworkFragment = new AmxKeyKnownNetworkFragment();
        this.mFragment = amxKeyKnownNetworkFragment;
        State.FragmentChangeListener fragmentChangeListener = (State.FragmentChangeListener) this.mActivity;
        if (fragmentChangeListener != null) {
            fragmentChangeListener.onFragmentChange(amxKeyKnownNetworkFragment, false);
        }
    }

    public static class AmxKeyKnownNetworkFragment extends GuidedStepSupportFragment {
        private StateMachine mStateMachine;
        private UserChoiceInfo mUserChoiceInfo;

        @Override // androidx.leanback.app.GuidedStepSupportFragment
        public GuidanceStylist onCreateGuidanceStylist() {
            return new GuidanceStylist() { // from class: com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net.AmxKeyKnownNetworkState.AmxKeyKnownNetworkFragment.1
                @Override // androidx.leanback.widget.GuidanceStylist
                public int onProvideLayoutId() {
                    return R.layout.amx_key_update_part2_network_list_guidance;
                }
            };
        }

        @Override // androidx.leanback.app.GuidedStepSupportFragment
        public GuidanceStylist.Guidance onCreateGuidance(Bundle bundle) {
            return new GuidanceStylist.Guidance(getString(R.string.title_wifi_known_network, this.mUserChoiceInfo.getWifiConfiguration().getPrintableSsid()), null, null, null);
        }

        @Override // androidx.leanback.app.GuidedStepSupportFragment
        public void onCreateActions(List<GuidedAction> list, Bundle bundle) {
            FragmentActivity activity = getActivity();
            list.add(new GuidedAction.Builder(activity).id(100001L).title(R.string.wifi_connect).build());
            list.add(new GuidedAction.Builder(activity).id(100002L).title(R.string.wifi_forget_network).build());
        }

        @Override // androidx.leanback.app.GuidedStepSupportFragment, androidx.fragment.app.Fragment
        public void onCreate(Bundle bundle) {
            this.mUserChoiceInfo = (UserChoiceInfo) ViewModelProviders.of(getActivity()).get(UserChoiceInfo.class);
            this.mStateMachine = (StateMachine) ViewModelProviders.of(getActivity()).get(StateMachine.class);
            super.onCreate(bundle);
        }

        @Override // androidx.leanback.app.GuidedStepSupportFragment
        public void onGuidedActionClicked(GuidedAction guidedAction) {
            long id = guidedAction.getId();
            if (id == 100001) {
                this.mStateMachine.getListener().onComplete(0);
            } else if (id == 100002) {
                ((WifiManager) getActivity().getApplicationContext().getSystemService("wifi")).forget(this.mUserChoiceInfo.getWifiConfiguration().networkId, null);
                this.mStateMachine.getListener().onComplete(1);
            }
        }
    }
}
