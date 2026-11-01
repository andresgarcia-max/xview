package com.android.tv.settings.util;

import android.os.SystemProperties;

/* loaded from: classes.dex */
public class MarketUtil {
    public static final String MARKET_OPERATOR = SystemProperties.get("config.MarketCode") + " " + SystemProperties.get("config.OperatorCode");

    public static boolean isMEXICO_MEGACABLE_OTT() {
        return "4301 5".equals(MARKET_OPERATOR);
    }

    public static boolean isNETFLIX_W10() {
        return "9999 10".equals(MARKET_OPERATOR);
    }
}
