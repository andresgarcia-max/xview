package com.android.wifitrackerlib;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkScoreCache;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import androidx.core.util.Preconditions;
import com.android.internal.annotations.VisibleForTesting;
import com.android.wifitrackerlib.WifiEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

@VisibleForTesting
/* loaded from: classes.dex */
public class PasspointWifiEntry extends WifiEntry implements WifiEntry.WifiEntryCallback {
    private final Context mContext;
    private final List<ScanResult> mCurrentHomeScanResults;
    private final List<ScanResult> mCurrentRoamingScanResults;
    private final String mFqdn;
    private final String mFriendlyName;
    private boolean mIsRoaming;
    private final String mKey;
    private int mMeteredOverride;
    private OsuWifiEntry mOsuWifiEntry;
    private PasspointConfiguration mPasspointConfig;
    private boolean mShouldAutoOpenCaptivePortal;
    protected long mSubscriptionExpirationTimeInMillis;
    private List<Integer> mTargetSecurityTypes;
    private WifiConfiguration mWifiConfig;

    @Override // com.android.wifitrackerlib.WifiEntry
    protected String getScanResultDescription() {
        return "";
    }

    PasspointWifiEntry(Context context, Handler handler, PasspointConfiguration passpointConfiguration, WifiManager wifiManager, WifiNetworkScoreCache wifiNetworkScoreCache, boolean z) throws IllegalArgumentException {
        super(handler, wifiManager, wifiNetworkScoreCache, z);
        this.mCurrentHomeScanResults = new ArrayList();
        this.mCurrentRoamingScanResults = new ArrayList();
        this.mTargetSecurityTypes = List.of(11, 12);
        this.mIsRoaming = false;
        this.mShouldAutoOpenCaptivePortal = false;
        this.mMeteredOverride = 0;
        Preconditions.checkNotNull(passpointConfiguration, "Cannot construct with null PasspointConfiguration!");
        this.mContext = context;
        this.mPasspointConfig = passpointConfiguration;
        this.mKey = uniqueIdToPasspointWifiEntryKey(passpointConfiguration.getUniqueId());
        String fqdn = passpointConfiguration.getHomeSp().getFqdn();
        this.mFqdn = fqdn;
        Preconditions.checkNotNull(fqdn, "Cannot construct with null PasspointConfiguration FQDN!");
        this.mFriendlyName = passpointConfiguration.getHomeSp().getFriendlyName();
        this.mSubscriptionExpirationTimeInMillis = passpointConfiguration.getSubscriptionExpirationTimeMillis();
        this.mMeteredOverride = this.mPasspointConfig.getMeteredOverride();
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public String getKey() {
        return this.mKey;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public synchronized int getConnectedState() {
        OsuWifiEntry osuWifiEntry;
        if (isExpired() && super.getConnectedState() == 0 && (osuWifiEntry = this.mOsuWifiEntry) != null) {
            return osuWifiEntry.getConnectedState();
        }
        return super.getConnectedState();
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public String getTitle() {
        return this.mFriendlyName;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public synchronized String getSummary(boolean z) {
        StringJoiner stringJoiner;
        String disconnectedDescription;
        stringJoiner = new StringJoiner(this.mContext.getString(R$string.wifitrackerlib_summary_separator));
        if (isExpired()) {
            OsuWifiEntry osuWifiEntry = this.mOsuWifiEntry;
            if (osuWifiEntry != null) {
                stringJoiner.add(osuWifiEntry.getSummary(z));
            } else {
                stringJoiner.add(this.mContext.getString(R$string.wifitrackerlib_wifi_passpoint_expired));
            }
        } else {
            int connectedState = getConnectedState();
            if (connectedState == 0) {
                disconnectedDescription = Utils.getDisconnectedDescription(this.mContext, this.mWifiConfig, this.mForSavedNetworksPage, z);
            } else if (connectedState == 1) {
                disconnectedDescription = Utils.getConnectingDescription(this.mContext, this.mNetworkInfo);
            } else if (connectedState == 2) {
                disconnectedDescription = Utils.getConnectedDescription(this.mContext, this.mWifiConfig, this.mNetworkCapabilities, null, this.mIsDefaultNetwork, this.mIsLowQuality);
            } else {
                Log.e("PasspointWifiEntry", "getConnectedState() returned unknown state: " + connectedState);
                disconnectedDescription = null;
            }
            if (!TextUtils.isEmpty(disconnectedDescription)) {
                stringJoiner.add(disconnectedDescription);
            }
        }
        String speedDescription = Utils.getSpeedDescription(this.mContext, this);
        if (!TextUtils.isEmpty(speedDescription)) {
            stringJoiner.add(speedDescription);
        }
        String autoConnectDescription = Utils.getAutoConnectDescription(this.mContext, this);
        if (!TextUtils.isEmpty(autoConnectDescription)) {
            stringJoiner.add(autoConnectDescription);
        }
        String meteredDescription = Utils.getMeteredDescription(this.mContext, this);
        if (!TextUtils.isEmpty(meteredDescription)) {
            stringJoiner.add(meteredDescription);
        }
        if (!z) {
            String verboseLoggingDescription = Utils.getVerboseLoggingDescription(this);
            if (!TextUtils.isEmpty(verboseLoggingDescription)) {
                stringJoiner.add(verboseLoggingDescription);
            }
        }
        return stringJoiner.toString();
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public synchronized List<Integer> getSecurityTypes() {
        return new ArrayList(this.mTargetSecurityTypes);
    }

    /* JADX WARN: Code restructure failed: missing block: B:8:0x000e, code lost:
    
        if (r0.meteredHint != false) goto L11;
     */
    @Override // com.android.wifitrackerlib.WifiEntry
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public synchronized boolean isMetered() {
        /*
            r2 = this;
            monitor-enter(r2)
            int r0 = r2.getMeteredChoice()     // Catch: java.lang.Throwable -> L14
            r1 = 1
            if (r0 == r1) goto L12
            android.net.wifi.WifiConfiguration r0 = r2.mWifiConfig     // Catch: java.lang.Throwable -> L14
            if (r0 == 0) goto L11
            boolean r0 = r0.meteredHint     // Catch: java.lang.Throwable -> L14
            if (r0 == 0) goto L11
            goto L12
        L11:
            r1 = 0
        L12:
            monitor-exit(r2)
            return r1
        L14:
            r0 = move-exception
            monitor-exit(r2)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.wifitrackerlib.PasspointWifiEntry.isMetered():boolean");
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public synchronized boolean isSuggestion() {
        boolean z;
        WifiConfiguration wifiConfiguration = this.mWifiConfig;
        if (wifiConfiguration != null) {
            z = wifiConfiguration.fromWifiNetworkSuggestion;
        }
        return z;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public synchronized boolean isSubscription() {
        return this.mPasspointConfig != null;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public synchronized int getMeteredChoice() {
        int i = this.mMeteredOverride;
        if (i == 1) {
            return 1;
        }
        return i == 2 ? 2 : 0;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public synchronized boolean canSetMeteredChoice() {
        boolean z;
        if (!isSuggestion()) {
            z = this.mPasspointConfig != null;
        }
        return z;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public synchronized boolean isAutoJoinEnabled() {
        PasspointConfiguration passpointConfiguration = this.mPasspointConfig;
        if (passpointConfiguration != null) {
            return passpointConfiguration.isAutojoinEnabled();
        }
        WifiConfiguration wifiConfiguration = this.mWifiConfig;
        if (wifiConfiguration == null) {
            return false;
        }
        return wifiConfiguration.allowAutojoin;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public synchronized boolean canSetAutoJoinEnabled() {
        boolean z;
        if (this.mPasspointConfig == null) {
            z = this.mWifiConfig != null;
        }
        return z;
    }

    public synchronized boolean isExpired() {
        if (this.mSubscriptionExpirationTimeInMillis <= 0) {
            return false;
        }
        return System.currentTimeMillis() >= this.mSubscriptionExpirationTimeInMillis;
    }

    synchronized void updatePasspointConfig(PasspointConfiguration passpointConfiguration) {
        this.mPasspointConfig = passpointConfiguration;
        if (passpointConfiguration != null) {
            this.mSubscriptionExpirationTimeInMillis = passpointConfiguration.getSubscriptionExpirationTimeMillis();
            this.mMeteredOverride = passpointConfiguration.getMeteredOverride();
        }
        notifyOnUpdated();
    }

    synchronized void updateScanResultInfo(WifiConfiguration wifiConfiguration, List<ScanResult> list, List<ScanResult> list2) throws IllegalArgumentException {
        this.mIsRoaming = false;
        this.mWifiConfig = wifiConfiguration;
        this.mCurrentHomeScanResults.clear();
        this.mCurrentRoamingScanResults.clear();
        if (list != null) {
            this.mCurrentHomeScanResults.addAll(list);
        }
        if (list2 != null) {
            this.mCurrentRoamingScanResults.addAll(list2);
        }
        if (this.mWifiConfig != null) {
            ArrayList arrayList = new ArrayList();
            if (list != null && !list.isEmpty()) {
                arrayList.addAll(list);
            } else if (list2 != null && !list2.isEmpty()) {
                arrayList.addAll(list2);
                this.mIsRoaming = true;
            }
            ScanResult bestScanResultByLevel = Utils.getBestScanResultByLevel(arrayList);
            if (bestScanResultByLevel != null) {
                this.mWifiConfig.SSID = "\"" + bestScanResultByLevel.SSID + "\"";
            }
            if (getConnectedState() == 0) {
                this.mLevel = bestScanResultByLevel != null ? this.mWifiManager.calculateSignalLevel(bestScanResultByLevel.level) : -1;
                this.mSpeed = Utils.getAverageSpeedFromScanResults(this.mScoreCache, arrayList);
            }
        } else {
            this.mLevel = -1;
        }
        notifyOnUpdated();
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    protected synchronized void updateSecurityTypes() {
        int currentSecurityType;
        WifiInfo wifiInfo = this.mWifiInfo;
        if (wifiInfo == null || (currentSecurityType = wifiInfo.getCurrentSecurityType()) == -1) {
            return;
        }
        this.mTargetSecurityTypes = Collections.singletonList(Integer.valueOf(currentSecurityType));
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    protected boolean connectionInfoMatches(WifiInfo wifiInfo, NetworkInfo networkInfo) {
        if (wifiInfo.isPasspointAp()) {
            return TextUtils.equals(wifiInfo.getPasspointFqdn(), this.mFqdn);
        }
        return false;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    synchronized void updateNetworkCapabilities(NetworkCapabilities networkCapabilities) {
        super.updateNetworkCapabilities(networkCapabilities);
        if (canSignIn() && this.mShouldAutoOpenCaptivePortal) {
            this.mShouldAutoOpenCaptivePortal = false;
            signIn(null);
        }
    }

    static String uniqueIdToPasspointWifiEntryKey(String str) {
        Preconditions.checkNotNull(str, "Cannot create key with null unique id!");
        return "PasspointWifiEntry:" + str;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    synchronized String getNetworkSelectionDescription() {
        return Utils.getNetworkSelectionDescription(this.mWifiConfig);
    }

    @Override // com.android.wifitrackerlib.WifiEntry.WifiEntryCallback
    public void onUpdated() {
        notifyOnUpdated();
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public synchronized boolean canSignIn() {
        boolean z;
        NetworkCapabilities networkCapabilities = this.mNetworkCapabilities;
        if (networkCapabilities != null) {
            z = networkCapabilities.hasCapability(17);
        }
        return z;
    }

    public void signIn(WifiEntry.SignInCallback signInCallback) {
        if (canSignIn()) {
            ((ConnectivityManager) this.mContext.getSystemService("connectivity")).startCaptivePortalApp(this.mWifiManager.getCurrentNetwork());
        }
    }
}
