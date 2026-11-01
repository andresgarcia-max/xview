package com.control.device.util;

import android.os.SystemProperties;

/* loaded from: classes.dex */
public class MarketUtil {
    public static final String MARKET_OPERATOR = SystemProperties.get("config.MarketCode") + " " + SystemProperties.get("config.OperatorCode");

    public static boolean isNetflixRef_W11() {
        return "9999 1".equals(MARKET_OPERATOR);
    }

    public static boolean isIndonesia_Telcom() {
        return "3601 1".equals(MARKET_OPERATOR);
    }

    public static boolean isITALY_UPT24K() {
        return "7001 1".equals(MARKET_OPERATOR);
    }

    public static boolean isElisal_OTT() {
        return "8701 1".equals(MARKET_OPERATOR);
    }

    public static boolean isBulgaria_A1_OTT() {
        return "1801 4".equals(MARKET_OPERATOR);
    }

    public static boolean isMk_A1_OTT() {
        return "5101 2".equals(MARKET_OPERATOR);
    }

    public static boolean isMEXICO_IZZI() {
        return "4301 4".equals(MARKET_OPERATOR);
    }

    public static boolean isMEXICO_MEGACABLE_OTT() {
        return "4301 5".equals(MARKET_OPERATOR);
    }

    public static boolean isBOHER_OTT_12() {
        return "4001 1".equals(MARKET_OPERATOR);
    }

    public static boolean isNordnet_OTT_12() {
        return "3501 3".equals(MARKET_OPERATOR);
    }

    public static boolean isViettel_OTT_12() {
        return "1301 2".equals(MARKET_OPERATOR);
    }

    public static boolean isMEXICO_AMCO() {
        return "4301 6".equals(MARKET_OPERATOR);
    }

    public static boolean isNETFLIX_W10() {
        return "9999 10".equals(MARKET_OPERATOR);
    }

    public static boolean isGeneralInternational() {
        return "9999 1".equals(MARKET_OPERATOR);
    }

    public static boolean isNetflixRef_OTT_L() {
        return "9999 10".equals(MARKET_OPERATOR);
    }

    public static boolean isCroatian_A1() {
        return "8901 1".equals(MARKET_OPERATOR);
    }

    public static boolean isTurkeyTele() {
        return "5801 3".equals(MARKET_OPERATOR);
    }

    public static boolean isFranceNetgem() {
        return "3501 4".equals(MARKET_OPERATOR);
    }

    public static boolean isColombiaDialnet() {
        return "2501 6".equals(MARKET_OPERATOR);
    }

    public static boolean isFransat() {
        return "3501 6".equals(MARKET_OPERATOR);
    }
}
