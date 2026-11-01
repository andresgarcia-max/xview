package com.android.wifitrackerlib;

import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.text.TextUtils;
import java.util.function.Consumer;
import java.util.function.Predicate;

/* loaded from: classes.dex */
public class PasspointNetworkDetailsTracker extends NetworkDetailsTracker {
    private final PasspointWifiEntry mChosenEntry;
    private NetworkInfo mCurrentNetworkInfo;
    private WifiConfiguration mCurrentWifiConfig;

    @Override // com.android.wifitrackerlib.NetworkDetailsTracker
    public WifiEntry getWifiEntry() {
        return this.mChosenEntry;
    }

    @Override // com.android.wifitrackerlib.BaseWifiTracker
    protected void handleOnStart() {
        updateStartInfo();
    }

    private void updateStartInfo() {
        conditionallyUpdateScanResults(true);
        conditionallyUpdateConfig();
        WifiInfo connectionInfo = this.mWifiManager.getConnectionInfo();
        Network currentNetwork = this.mWifiManager.getCurrentNetwork();
        NetworkInfo networkInfo = this.mConnectivityManager.getNetworkInfo(currentNetwork);
        this.mCurrentNetworkInfo = networkInfo;
        this.mChosenEntry.updateConnectionInfo(connectionInfo, networkInfo);
        handleNetworkCapabilitiesChanged(this.mConnectivityManager.getNetworkCapabilities(currentNetwork));
        handleLinkPropertiesChanged(this.mConnectivityManager.getLinkProperties(currentNetwork));
        this.mChosenEntry.setIsDefaultNetwork(this.mIsWifiDefaultRoute);
        this.mChosenEntry.setIsLowQuality(this.mIsWifiValidated && this.mIsCellDefaultRoute);
    }

    private void conditionallyUpdateScanResults(boolean z) {
        if (this.mWifiManager.getWifiState() == 1) {
            this.mChosenEntry.updateScanResultInfo(this.mCurrentWifiConfig, null, null);
        } else {
            if (z) {
                cacheNewScanResults();
                throw null;
            }
            throw null;
        }
    }

    private void conditionallyUpdateConfig() {
        this.mWifiManager.getPasspointConfigurations().stream().filter(new Predicate() { // from class: com.android.wifitrackerlib.PasspointNetworkDetailsTracker$$ExternalSyntheticLambda1
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                boolean lambda$conditionallyUpdateConfig$2;
                lambda$conditionallyUpdateConfig$2 = PasspointNetworkDetailsTracker.this.lambda$conditionallyUpdateConfig$2((PasspointConfiguration) obj);
                return lambda$conditionallyUpdateConfig$2;
            }
        }).findAny().ifPresent(new Consumer() { // from class: com.android.wifitrackerlib.PasspointNetworkDetailsTracker$$ExternalSyntheticLambda0
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                PasspointNetworkDetailsTracker.this.lambda$conditionallyUpdateConfig$3((PasspointConfiguration) obj);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$conditionallyUpdateConfig$2(PasspointConfiguration passpointConfiguration) {
        return TextUtils.equals(PasspointWifiEntry.uniqueIdToPasspointWifiEntryKey(passpointConfiguration.getUniqueId()), this.mChosenEntry.getKey());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$conditionallyUpdateConfig$3(PasspointConfiguration passpointConfiguration) {
        this.mChosenEntry.updatePasspointConfig(passpointConfiguration);
    }

    private void cacheNewScanResults() {
        this.mWifiManager.getScanResults();
        throw null;
    }
}
