package com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net;

import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;
import androidx.leanback.widget.GuidedActionsStylist;
import androidx.leanback.widget.ItemAlignmentFacet;
import androidx.lifecycle.ViewModelProviders;
import com.android.tv.settings.connectivity.util.GuidedActionsAlignUtil$SetupViewHolder;
import com.control.device.R;
import com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net.AmxKeyEnterPasswordState;
import com.control.device.settings.connectivity.State;
import com.control.device.settings.connectivity.StateMachine;
import com.control.device.settings.connectivity.UserChoiceInfo;
import java.util.List;

/* loaded from: classes.dex */
public class AmxKeyEnterPasswordState implements State {
    private final FragmentActivity mActivity;
    private Fragment mFragment;

    public AmxKeyEnterPasswordState(FragmentActivity fragmentActivity) {
        this.mActivity = fragmentActivity;
    }

    @Override // com.control.device.settings.connectivity.State
    public void processForward() {
        Log.i("AmxKeyEnterPasswordState", "processForward");
        AmxKeyEnterPasswordFragment amxKeyEnterPasswordFragment = new AmxKeyEnterPasswordFragment();
        this.mFragment = amxKeyEnterPasswordFragment;
        ((State.FragmentChangeListener) this.mActivity).onFragmentChange(amxKeyEnterPasswordFragment, true);
    }

    @Override // com.control.device.settings.connectivity.State
    public void processBackward() {
        Log.i("AmxKeyEnterPasswordState", "processBackward");
        AmxKeyEnterPasswordFragment amxKeyEnterPasswordFragment = new AmxKeyEnterPasswordFragment();
        this.mFragment = amxKeyEnterPasswordFragment;
        ((State.FragmentChangeListener) this.mActivity).onFragmentChange(amxKeyEnterPasswordFragment, false);
    }

    public static class AmxKeyEnterPasswordFragment extends GuidedStepSupportFragment {
        private CheckBox mCheckBox;
        private StateMachine mStateMachine;
        private EditText mTextInput;
        private UserChoiceInfo mUserChoiceInfo;

        @Override // androidx.leanback.app.GuidedStepSupportFragment
        public GuidanceStylist onCreateGuidanceStylist() {
            return new GuidanceStylist() { // from class: com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net.AmxKeyEnterPasswordState.AmxKeyEnterPasswordFragment.1
                @Override // androidx.leanback.widget.GuidanceStylist
                public int onProvideLayoutId() {
                    return R.layout.amx_key_update_part2_network_list_guidance;
                }
            };
        }

        @Override // androidx.leanback.app.GuidedStepSupportFragment
        public GuidanceStylist.Guidance onCreateGuidance(Bundle bundle) {
            return new GuidanceStylist.Guidance(getString(R.string.amx_key_wifi_setup_input_password, this.mUserChoiceInfo.getWifiConfiguration().getPrintableSsid()), null, null, null);
        }

        /* renamed from: com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net.AmxKeyEnterPasswordState$AmxKeyEnterPasswordFragment$2, reason: invalid class name */
        class AnonymousClass2 extends GuidedActionsStylist {
            @Override // androidx.leanback.widget.GuidedActionsStylist
            public int onProvideItemLayoutId() {
                return R.layout.amx_key_update_setup_password_item;
            }

            AnonymousClass2() {
            }

            @Override // androidx.leanback.widget.GuidedActionsStylist
            public GuidedActionsStylist.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                return new PasswordViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(onProvideItemLayoutId(i), viewGroup, false));
            }

            @Override // androidx.leanback.widget.GuidedActionsStylist
            public void onBindViewHolder(GuidedActionsStylist.ViewHolder viewHolder, final GuidedAction guidedAction) {
                super.onBindViewHolder(viewHolder, guidedAction);
                if (guidedAction.getId() == -7) {
                    PasswordViewHolder passwordViewHolder = (PasswordViewHolder) viewHolder;
                    AmxKeyEnterPasswordFragment.this.mTextInput = (EditText) passwordViewHolder.getTitleView();
                    AmxKeyEnterPasswordFragment.this.mCheckBox = passwordViewHolder.mCheckbox;
                    AmxKeyEnterPasswordFragment.this.mCheckBox.setOnClickListener(new View.OnClickListener() { // from class: com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net.AmxKeyEnterPasswordState$AmxKeyEnterPasswordFragment$2$$ExternalSyntheticLambda0
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            AmxKeyEnterPasswordState.AmxKeyEnterPasswordFragment.AnonymousClass2.this.lambda$onBindViewHolder$0(guidedAction, view);
                        }
                    });
                    AmxKeyEnterPasswordFragment.this.mCheckBox.setChecked(AmxKeyEnterPasswordFragment.this.mUserChoiceInfo.isPasswordHidden());
                    AmxKeyEnterPasswordFragment.this.updatePasswordInputObfuscation();
                    openInEditMode(guidedAction);
                }
            }

            /* JADX INFO: Access modifiers changed from: private */
            public /* synthetic */ void lambda$onBindViewHolder$0(GuidedAction guidedAction, View view) {
                AmxKeyEnterPasswordFragment.this.updatePasswordInputObfuscation();
                AmxKeyEnterPasswordFragment.this.openInEditMode(guidedAction);
            }
        }

        @Override // androidx.leanback.app.GuidedStepSupportFragment
        public GuidedActionsStylist onCreateActionsStylist() {
            return new AnonymousClass2();
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
            String pageSummary = this.mUserChoiceInfo.getPageSummary(2);
            boolean isPasswordHidden = this.mUserChoiceInfo.isPasswordHidden();
            GuidedAction.Builder builder = new GuidedAction.Builder(activity);
            if (pageSummary == null) {
                pageSummary = "";
            }
            list.add(builder.title(pageSummary).editInputType((isPasswordHidden ? 144 : 128) | 1).id(-7L).editable(true).build());
        }

        @Override // androidx.leanback.app.GuidedStepSupportFragment
        public long onGuidedActionEditedAndProceed(GuidedAction guidedAction) {
            if (guidedAction.getId() == -7) {
                String charSequence = guidedAction.getTitle().toString();
                if (charSequence.length() >= 5) {
                    this.mUserChoiceInfo.put(2, guidedAction.getTitle().toString());
                    this.mUserChoiceInfo.setPasswordHidden(this.mCheckBox.isChecked());
                    setWifiConfigurationPassword(charSequence);
                    this.mStateMachine.getListener().onComplete(5);
                }
            }
            return guidedAction.getId();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void updatePasswordInputObfuscation() {
            this.mTextInput.setInputType((this.mCheckBox.isChecked() ? 144 : 128) | 1);
        }

        private void setWifiConfigurationPassword(String str) {
            int wifiSecurity = this.mUserChoiceInfo.getWifiSecurity();
            WifiConfiguration wifiConfiguration = this.mUserChoiceInfo.getWifiConfiguration();
            if (wifiSecurity == 1) {
                int length = str.length();
                if ((length == 10 || length == 26 || length == 58) && str.matches("[0-9A-Fa-f]*")) {
                    wifiConfiguration.wepKeys[0] = str;
                    return;
                }
                if (length != 0) {
                    wifiConfiguration.wepKeys[0] = '\"' + str + '\"';
                    return;
                }
                return;
            }
            if (wifiSecurity != 2 || str.length() >= 8) {
                if (str.matches("[0-9A-Fa-f]{64}")) {
                    wifiConfiguration.preSharedKey = str;
                    return;
                }
                wifiConfiguration.preSharedKey = '\"' + str + '\"';
            }
        }

        private static class PasswordViewHolder extends GuidedActionsAlignUtil$SetupViewHolder {
            CheckBox mCheckbox;

            PasswordViewHolder(final View view) {
                new GuidedActionsStylist.ViewHolder(view) { // from class: com.android.tv.settings.connectivity.util.GuidedActionsAlignUtil$SetupViewHolder
                    @Override // androidx.leanback.widget.GuidedActionsStylist.ViewHolder, androidx.leanback.widget.FacetProvider
                    public Object getFacet(Class cls) {
                        if (!cls.equals(ItemAlignmentFacet.class)) {
                            return null;
                        }
                        ItemAlignmentFacet.ItemAlignmentDef itemAlignmentDef = new ItemAlignmentFacet.ItemAlignmentDef();
                        itemAlignmentDef.setItemAlignmentViewId(R.id.guidedactions_item_title);
                        itemAlignmentDef.setAlignedToTextViewBaseline(false);
                        itemAlignmentDef.setItemAlignmentOffset(0);
                        itemAlignmentDef.setItemAlignmentOffsetWithPadding(true);
                        itemAlignmentDef.setItemAlignmentOffsetPercent(50.0f);
                        ItemAlignmentFacet itemAlignmentFacet = new ItemAlignmentFacet();
                        itemAlignmentFacet.setAlignmentDefs(new ItemAlignmentFacet.ItemAlignmentDef[]{itemAlignmentDef});
                        return itemAlignmentFacet;
                    }
                };
                this.mCheckbox = (CheckBox) view.findViewById(R.id.password_checkbox);
            }
        }
    }
}
