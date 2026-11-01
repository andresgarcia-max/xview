package com.android.wifitrackerlib;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.util.ArrayMap;
import android.util.SparseArray;
import androidx.core.util.Preconditions;
import com.android.wifitrackerlib.StandardWifiEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/* loaded from: classes.dex */
public class WifiPickerTracker extends BaseWifiTracker {
    private final ArrayMap<StandardWifiEntry.StandardWifiEntryKey, List<WifiConfiguration>> mNetworkRequestConfigCache;
    private NetworkRequestEntry mNetworkRequestEntry;
    private int mNumSavedNetworks;
    private final Map<String, PasspointConfiguration> mPasspointConfigCache;
    private final SparseArray<WifiConfiguration> mPasspointWifiConfigCache;
    private final Map<String, PasspointWifiEntry> mPasspointWifiEntryCache;
    private final Map<StandardWifiEntry.StandardWifiEntryKey, List<WifiConfiguration>> mStandardWifiConfigCache;
    private final List<StandardWifiEntry> mStandardWifiEntryCache;
    private final Map<StandardWifiEntry.StandardWifiEntryKey, List<WifiConfiguration>> mSuggestedConfigCache;
    private final List<StandardWifiEntry> mSuggestedWifiEntryCache;

    @Override // com.android.wifitrackerlib.BaseWifiTracker
    protected void handleOnStart() {
        updateWifiConfigurations(this.mWifiManager.getPrivilegedConfiguredNetworks());
        updatePasspointConfigurations(this.mWifiManager.getPasspointConfigurations());
        this.mWifiManager.getScanResults();
        throw null;
    }

    private void updateWifiConfigurations(List<WifiConfiguration> list) {
        Preconditions.checkNotNull(list, "Config list should not be null!");
        this.mStandardWifiConfigCache.clear();
        this.mSuggestedConfigCache.clear();
        this.mNetworkRequestConfigCache.clear();
        new ArrayList();
        for (WifiConfiguration wifiConfiguration : list) {
            if (!wifiConfiguration.carrierMerged) {
                StandardWifiEntry.StandardWifiEntryKey standardWifiEntryKey = new StandardWifiEntry.StandardWifiEntryKey(wifiConfiguration, true);
                if (wifiConfiguration.isPasspoint()) {
                    this.mPasspointWifiConfigCache.put(wifiConfiguration.networkId, wifiConfiguration);
                } else if (wifiConfiguration.fromWifiNetworkSuggestion) {
                    if (!this.mSuggestedConfigCache.containsKey(standardWifiEntryKey)) {
                        this.mSuggestedConfigCache.put(standardWifiEntryKey, new ArrayList());
                    }
                    this.mSuggestedConfigCache.get(standardWifiEntryKey).add(wifiConfiguration);
                } else if (wifiConfiguration.fromWifiNetworkSpecifier) {
                    if (!this.mNetworkRequestConfigCache.containsKey(standardWifiEntryKey)) {
                        this.mNetworkRequestConfigCache.put(standardWifiEntryKey, new ArrayList());
                    }
                    this.mNetworkRequestConfigCache.get(standardWifiEntryKey).add(wifiConfiguration);
                } else {
                    if (!this.mStandardWifiConfigCache.containsKey(standardWifiEntryKey)) {
                        this.mStandardWifiConfigCache.put(standardWifiEntryKey, new ArrayList());
                    }
                    this.mStandardWifiConfigCache.get(standardWifiEntryKey).add(wifiConfiguration);
                }
            }
        }
        this.mNumSavedNetworks = (int) this.mStandardWifiConfigCache.values().stream().flatMap(new Function() { // from class: com.android.wifitrackerlib.WifiPickerTracker$$ExternalSyntheticLambda3
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return ((List) obj).stream();
            }
        }).filter(new Predicate() { // from class: com.android.wifitrackerlib.WifiPickerTracker$$ExternalSyntheticLambda6
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                boolean lambda$updateWifiConfigurations$19;
                lambda$updateWifiConfigurations$19 = WifiPickerTracker.lambda$updateWifiConfigurations$19((WifiConfiguration) obj);
                return lambda$updateWifiConfigurations$19;
            }
        }).map(new Function() { // from class: com.android.wifitrackerlib.WifiPickerTracker$$ExternalSyntheticLambda1
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                Integer lambda$updateWifiConfigurations$20;
                lambda$updateWifiConfigurations$20 = WifiPickerTracker.lambda$updateWifiConfigurations$20((WifiConfiguration) obj);
                return lambda$updateWifiConfigurations$20;
            }
        }).distinct().count();
        this.mStandardWifiEntryCache.forEach(new Consumer() { // from class: com.android.wifitrackerlib.WifiPickerTracker$$ExternalSyntheticLambda0
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                WifiPickerTracker.this.lambda$updateWifiConfigurations$21((StandardWifiEntry) obj);
            }
        });
        this.mSuggestedWifiEntryCache.removeIf(new Predicate() { // from class: com.android.wifitrackerlib.WifiPickerTracker$$ExternalSyntheticLambda4
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                boolean lambda$updateWifiConfigurations$22;
                lambda$updateWifiConfigurations$22 = WifiPickerTracker.this.lambda$updateWifiConfigurations$22((StandardWifiEntry) obj);
                return lambda$updateWifiConfigurations$22;
            }
        });
        NetworkRequestEntry networkRequestEntry = this.mNetworkRequestEntry;
        if (networkRequestEntry != null) {
            networkRequestEntry.updateConfig(this.mNetworkRequestConfigCache.get(networkRequestEntry.getStandardWifiEntryKey()));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$updateWifiConfigurations$19(WifiConfiguration wifiConfiguration) {
        return !wifiConfiguration.isEphemeral();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ Integer lambda$updateWifiConfigurations$20(WifiConfiguration wifiConfiguration) {
        return Integer.valueOf(wifiConfiguration.networkId);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateWifiConfigurations$21(StandardWifiEntry standardWifiEntry) {
        standardWifiEntry.updateConfig(this.mStandardWifiConfigCache.get(standardWifiEntry.getStandardWifiEntryKey()));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$updateWifiConfigurations$22(StandardWifiEntry standardWifiEntry) {
        standardWifiEntry.updateConfig(this.mSuggestedConfigCache.get(standardWifiEntry.getStandardWifiEntryKey()));
        return !standardWifiEntry.isSuggestion();
    }

    private void updatePasspointConfigurations(List<PasspointConfiguration> list) {
        Preconditions.checkNotNull(list, "Config list should not be null!");
        this.mPasspointConfigCache.clear();
        this.mPasspointConfigCache.putAll((Map) list.stream().collect(Collectors.toMap(new Function() { // from class: com.android.wifitrackerlib.WifiPickerTracker$$ExternalSyntheticLambda2
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                String lambda$updatePasspointConfigurations$23;
                lambda$updatePasspointConfigurations$23 = WifiPickerTracker.lambda$updatePasspointConfigurations$23((PasspointConfiguration) obj);
                return lambda$updatePasspointConfigurations$23;
            }
        }, Function.identity())));
        this.mPasspointWifiEntryCache.entrySet().removeIf(new Predicate() { // from class: com.android.wifitrackerlib.WifiPickerTracker$$ExternalSyntheticLambda5
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                boolean lambda$updatePasspointConfigurations$24;
                lambda$updatePasspointConfigurations$24 = WifiPickerTracker.this.lambda$updatePasspointConfigurations$24((Map.Entry) obj);
                return lambda$updatePasspointConfigurations$24;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ String lambda$updatePasspointConfigurations$23(PasspointConfiguration passpointConfiguration) {
        return PasspointWifiEntry.uniqueIdToPasspointWifiEntryKey(passpointConfiguration.getUniqueId());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$updatePasspointConfigurations$24(Map.Entry entry) {
        PasspointWifiEntry passpointWifiEntry = (PasspointWifiEntry) entry.getValue();
        passpointWifiEntry.updatePasspointConfig(this.mPasspointConfigCache.get(passpointWifiEntry.getKey()));
        return (passpointWifiEntry.isSubscription() || passpointWifiEntry.isSuggestion()) ? false : true;
    }
}
