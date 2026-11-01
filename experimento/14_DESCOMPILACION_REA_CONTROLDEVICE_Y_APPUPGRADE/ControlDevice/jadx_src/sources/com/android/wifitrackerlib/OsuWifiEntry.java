package com.android.wifitrackerlib;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.hotspot2.OsuProvider;
import android.text.TextUtils;

/* loaded from: classes.dex */
class OsuWifiEntry extends WifiEntry {
    private final Context mContext;
    private boolean mIsAlreadyProvisioned;
    private final String mKey;
    private final OsuProvider mOsuProvider;
    private String mOsuStatusString;
    private String mSsid;

    @Override // com.android.wifitrackerlib.WifiEntry
    protected String getScanResultDescription() {
        return "";
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public String getKey() {
        return this.mKey;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public synchronized String getTitle() {
        String friendlyName = this.mOsuProvider.getFriendlyName();
        if (!TextUtils.isEmpty(friendlyName)) {
            return friendlyName;
        }
        if (!TextUtils.isEmpty(this.mSsid)) {
            return this.mSsid;
        }
        Uri serverUri = this.mOsuProvider.getServerUri();
        if (serverUri == null) {
            return "";
        }
        return serverUri.toString();
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public synchronized String getSummary(boolean z) {
        String string;
        String str = this.mOsuStatusString;
        if (str != null) {
            return str;
        }
        if (!isAlreadyProvisioned()) {
            return this.mContext.getString(R$string.wifitrackerlib_tap_to_sign_up);
        }
        if (z) {
            string = this.mContext.getString(R$string.wifitrackerlib_wifi_passpoint_expired);
        } else {
            string = this.mContext.getString(R$string.wifitrackerlib_tap_to_renew_subscription_and_connect);
        }
        return string;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    protected boolean connectionInfoMatches(WifiInfo wifiInfo, NetworkInfo networkInfo) {
        return wifiInfo.isOsuAp() && TextUtils.equals(wifiInfo.getPasspointProviderFriendlyName(), this.mOsuProvider.getFriendlyName());
    }

    synchronized boolean isAlreadyProvisioned() {
        return this.mIsAlreadyProvisioned;
    }
}
