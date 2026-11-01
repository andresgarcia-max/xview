package com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;
import androidx.leanback.widget.GuidedActionsStylist;
import com.android.settingslib.wifi.AccessPoint;
import com.android.tv.settings.connectivity.ConnectivityListener;
import com.control.device.R;
import com.control.device.settings.MexicAmco.AMXUpdateAtvKey.UpdateAtvKeyActivity;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class AmxKeyWifiListFragment extends GuidedStepSupportFragment implements ConnectivityListener.Listener, ConnectivityListener.WifiNetworkListener, AccessPoint.AccessPointListener {
    private ConnectivityListener mConnectivityListener;
    private boolean mVisible = false;
    private int mWifiChangefreq = 0;

    private void updateConnectivity() {
    }

    @Override // com.android.settingslib.wifi.AccessPoint.AccessPointListener
    public void onAccessPointChanged(AccessPoint accessPoint) {
    }

    @Override // com.android.settingslib.wifi.AccessPoint.AccessPointListener
    public void onLevelChanged(AccessPoint accessPoint) {
    }

    @Override // androidx.fragment.app.Fragment
    public void onAttach(Context context) {
        Log.i("AmxKeyWifiListFragment", "onAttach");
        super.onAttach(context);
    }

    @Override // androidx.leanback.app.GuidedStepSupportFragment, androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        Log.i("AmxKeyWifiListFragment", "onCreate");
        this.mConnectivityListener = new ConnectivityListener(getContext(), this, null);
        super.onCreate(bundle);
    }

    @Override // androidx.fragment.app.Fragment
    public void onStart() {
        Log.i("AmxKeyWifiListFragment", "onStart");
        super.onStart();
        this.mConnectivityListener.start();
        this.mConnectivityListener.setWifiListener(this);
    }

    @Override // androidx.leanback.app.GuidedStepSupportFragment, androidx.fragment.app.Fragment
    public void onResume() {
        Log.i("AmxKeyWifiListFragment", "onResume");
        super.onResume();
    }

    @Override // androidx.fragment.app.Fragment
    public void onPause() {
        Log.i("AmxKeyWifiListFragment", "onPause");
        super.onPause();
    }

    @Override // androidx.fragment.app.Fragment
    public void onStop() {
        Log.i("AmxKeyWifiListFragment", "onStop");
        this.mConnectivityListener.stop();
        super.onStop();
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroy() {
        Log.i("AmxKeyWifiListFragment", "onDestroy");
        this.mConnectivityListener.stop();
        this.mConnectivityListener.destroy();
        super.onDestroy();
    }

    @Override // androidx.fragment.app.Fragment
    public void onDetach() {
        Log.i("AmxKeyWifiListFragment", "onDetach");
        super.onDetach();
    }

    public void setVisible(boolean z) {
        Log.i("AmxKeyWifiListFragment", "setVisible, visible=" + z);
        this.mVisible = z;
        if (z) {
            updateWifiList();
        }
    }

    @Override // com.android.tv.settings.connectivity.ConnectivityListener.Listener
    public void onConnectivityChange() {
        updateConnectivity();
    }

    @Override // com.android.tv.settings.connectivity.ConnectivityListener.WifiNetworkListener
    public void onWifiListChanged() {
        if (!this.mVisible) {
            Log.i("AmxKeyWifiListFragment", "onWifiListChanged, visible=" + this.mVisible);
            return;
        }
        if (this.mWifiChangefreq % 2 == 0) {
            this.mWifiChangefreq = 0;
            updateWifiList();
        }
        this.mWifiChangefreq++;
    }

    private void updateWifiList() {
        Log.i("AmxKeyWifiListFragment", "updateWifiList");
        FragmentActivity activity = getActivity();
        if (activity == null) {
            Log.i("AmxKeyWifiListFragment", "updateWifiList, context is null");
            return;
        }
        ArrayList arrayList = new ArrayList();
        if (this.mConnectivityListener.isWifiEnabledOrEnabling()) {
            for (AccessPoint accessPoint : this.mConnectivityListener.getAvailableNetworks()) {
                accessPoint.setListener(this);
                arrayList.add(new GuidedAction.Builder(activity).title(accessPoint.getTitle()).icon(R.drawable.ic_baseline_wifi_24).intent(AmxKeyWifiConnectionActivity.createIntent(getContext(), accessPoint)).build());
            }
        }
        arrayList.add(new GuidedAction.Builder(activity).title(R.string.amx_key_part2_network_list_other_network).id(100001L).icon(R.drawable.ic_add).build());
        arrayList.add(new GuidedAction.Builder(activity).title(R.string.amx_key_part2_network_list_use_ethernet).id(100002L).icon(R.drawable.ic_ethernet_white).build());
        setActions(arrayList);
    }

    @Override // androidx.leanback.app.GuidedStepSupportFragment
    public GuidanceStylist onCreateGuidanceStylist() {
        return new GuidanceStylist() { // from class: com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net.AmxKeyWifiListFragment.1
            @Override // androidx.leanback.widget.GuidanceStylist
            public int onProvideLayoutId() {
                return R.layout.amx_key_update_part2_network_list_guidance;
            }
        };
    }

    @Override // androidx.leanback.app.GuidedStepSupportFragment
    public GuidanceStylist.Guidance onCreateGuidance(Bundle bundle) {
        Log.i("AmxKeyWifiListFragment", "onCreateGuidance");
        return new GuidanceStylist.Guidance(getString(R.string.amx_key_part2_network_list_title), getString(R.string.amx_key_part2_network_list_tips), null, null);
    }

    @Override // androidx.leanback.app.GuidedStepSupportFragment
    public GuidedActionsStylist onCreateActionsStylist() {
        return new GuidedActionsStylist() { // from class: com.control.device.settings.MexicAmco.AMXUpdateAtvKey.net.AmxKeyWifiListFragment.2
            @Override // androidx.leanback.widget.GuidedActionsStylist
            public int onProvideLayoutId() {
                return R.layout.amx_key_update_part2_network_list_actions;
            }
        };
    }

    @Override // androidx.leanback.app.GuidedStepSupportFragment
    public void onCreateActions(List<GuidedAction> list, Bundle bundle) {
        FragmentActivity activity = getActivity();
        list.add(new GuidedAction.Builder(activity).title(R.string.amx_key_part2_network_list_other_network).id(100001L).icon(R.drawable.ic_add).build());
        list.add(new GuidedAction.Builder(activity).title(R.string.amx_key_part2_network_list_use_ethernet).id(100002L).icon(R.drawable.ic_ethernet_white).build());
    }

    @Override // androidx.leanback.app.GuidedStepSupportFragment
    public void onGuidedActionClicked(GuidedAction guidedAction) {
        Log.i("AmxKeyWifiListFragment", "onGuidedActionClicked, action=" + guidedAction);
        UpdateAtvKeyActivity updateAtvKeyActivity = (UpdateAtvKeyActivity) getActivity();
        if (updateAtvKeyActivity == null) {
            Log.i("AmxKeyWifiListFragment", "onGuidedActionClicked, activity is null");
            return;
        }
        Intent intent = null;
        if (guidedAction.getId() == 100001) {
            intent = new Intent();
            intent.setClassName(updateAtvKeyActivity.getPackageName(), AmxKeyAddWifiNetworkActivity.class.getName());
        } else if (guidedAction.getId() == 100002) {
            intent = new Intent();
            intent.setClassName(updateAtvKeyActivity.getPackageName(), AmxKeyEthernetConnectionActivity.class.getName());
        } else if (guidedAction.getIntent() != null) {
            intent = guidedAction.getIntent();
        }
        if (intent != null) {
            try {
                updateAtvKeyActivity.startActivityForResult(intent, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
