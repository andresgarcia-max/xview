package com.android.wifitrackerlib;

import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import com.android.wifitrackerlib.StandardWifiEntry;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/* loaded from: classes.dex */
public class StandardNetworkDetailsTracker extends NetworkDetailsTracker {
    private final StandardWifiEntry mChosenEntry;
    private NetworkInfo mCurrentNetworkInfo;
    private final StandardWifiEntry.StandardWifiEntryKey mKey;

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
            this.mChosenEntry.updateScanResultInfo(Collections.emptyList());
        } else {
            if (z) {
                cacheNewScanResults();
            }
            throw null;
        }
    }

    private void conditionallyUpdateConfig() {
        this.mChosenEntry.updateConfig((List) this.mWifiManager.getPrivilegedConfiguredNetworks().stream().filter(new Predicate() { // from class: com.android.wifitrackerlib.StandardNetworkDetailsTracker$$ExternalSyntheticLambda1
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                boolean configMatches;
                configMatches = StandardNetworkDetailsTracker.this.configMatches((WifiConfiguration) obj);
                return configMatches;
            }
        }).collect(Collectors.toList()));
    }

    private void cacheNewScanResults() {
        throw null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$cacheNewScanResults$0(ScanResult scanResult) {
        return new StandardWifiEntry.ScanResultKey(scanResult).equals(this.mKey.getScanResultKey());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean configMatches(WifiConfiguration wifiConfiguration) {
        if (wifiConfiguration.isPasspoint()) {
            return false;
        }
        StandardWifiEntry.StandardWifiEntryKey standardWifiEntryKey = this.mKey;
        return standardWifiEntryKey.equals(new StandardWifiEntry.StandardWifiEntryKey(wifiConfiguration, standardWifiEntryKey.isTargetingNewNetworks()));
    }
}
