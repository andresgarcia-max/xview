package com.control.device.settings.remote.util;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import com.control.device.R;
import com.control.device.util.MarketUtil;
import com.control.device.util.ProductUtils;

/* loaded from: classes.dex */
public class ZTERemoteAnimationControl {
    private static final String mModel = SystemProperties.get("ro.product.model", "");
    public int bfPair;
    public String containName;
    public int inPair;
    public int layout;
    public int pair_fail;
    public int pair_success;
    public int pairing;
    public int pairing_start;
    private String mMarket = SystemProperties.get("config.MarketCode");
    private String mOperator = SystemProperties.get("config.OperatorCode");
    private String MarketCode_OperatorCode = SystemProperties.get("config.MarketCode") + " " + SystemProperties.get("config.OperatorCode");
    private String mOemTag = SystemProperties.get("ro.oem.key1");
    private final String Nordnet_ott_12 = "3501 3";

    public boolean initWithContext(Context context) {
        if (!MarketUtil.isNETFLIX_W10() || !ProductUtils.isB866V5W11() || !ProductUtils.isGTV(context)) {
            return false;
        }
        this.containName = "G10C";
        this.layout = R.layout.IBC_zte_remote;
        this.bfPair = R.id.bfPair;
        this.inPair = R.id.inPair;
        this.pairing_start = R.drawable.bluetooth_pairing_start_IBC;
        this.pairing = R.drawable.bluetooth_pairing_pairing_IBC;
        this.pair_success = R.drawable.bluetooth_af_pair_success_IBC;
        this.pair_fail = R.drawable.bluetooth_af_pair_failed_IBC;
        return true;
    }

    public void init() {
        String str;
        Log.d("[TvSettings]", "[ZTERemoteAnimationControl]<init> ");
        if ("7001".equals(this.mMarket) && "1".equals(this.mOperator)) {
            this.containName = "UPT24K";
            this.layout = R.layout.italy_tsd_remote;
            this.bfPair = R.id.bfPair;
            this.inPair = R.id.inPair;
            this.pairing_start = R.drawable.bluetooth_pairing_start_italy;
            this.pairing = R.drawable.bluetooth_pairing_pairing_italy;
            this.pair_success = R.drawable.bluetooth_af_pair_success_italy_0401;
            this.pair_fail = R.drawable.bluetooth_af_pair_failed_italy_0401;
            return;
        }
        if ("1301 2".equals(this.MarketCode_OperatorCode)) {
            this.containName = "Viettel";
            this.layout = R.layout.viettel_ott_remote;
            this.bfPair = R.id.bfPair;
            this.inPair = R.id.inPair;
            this.pairing_start = R.drawable.bluetooth_pairing_start_viettel;
            this.pairing = R.drawable.bluetooth_pairing_pairing_viettel;
            this.pair_success = R.drawable.bluetooth_af_pair_success_viettel;
            this.pair_fail = R.drawable.bluetooth_af_pair_failed_viettel;
            return;
        }
        if ("7201 2".equals(this.MarketCode_OperatorCode)) {
            this.containName = "AIS_RCU";
            this.layout = R.layout.Thailand_remote;
            this.bfPair = R.id.bfPair;
            this.inPair = R.id.inPair;
            this.pairing_start = R.drawable.bluetooth_pairing_start_Thailand;
            this.pairing = R.drawable.bluetooth_pairing_pairing_Thailand;
            this.pair_success = R.drawable.bluetooth_af_pair_success_Thailand;
            this.pair_fail = R.drawable.bluetooth_af_pair_failed_Thailand;
            return;
        }
        if ("5801 1".equals(this.MarketCode_OperatorCode)) {
            this.containName = "AIS_RCU";
            this.layout = R.layout.Thailand_remote;
            this.bfPair = R.id.bfPair;
            this.inPair = R.id.inPair;
            this.pairing_start = R.drawable.bluetooth_pairing_start_Thailand;
            this.pairing = R.drawable.bluetooth_pairing_pairing_Thailand;
            this.pair_success = R.drawable.bluetooth_af_pair_success_Thailand;
            this.pair_fail = R.drawable.bluetooth_af_pair_failed_Thailand;
            return;
        }
        if ("2501 2".equals(this.MarketCode_OperatorCode) && "B866V5-W10".equals(mModel)) {
            this.containName = "Claro";
            this.layout = R.layout.NetflixRef_OTT_remote;
            this.bfPair = R.id.bfPair;
            this.inPair = R.id.inPair;
            this.pairing_start = R.drawable.bluetooth_pairing_start_NetflixRef_OTT;
            this.pairing = R.drawable.bluetooth_pairing_pairing_NetflixRef_OTT;
            this.pair_success = R.drawable.bluetooth_af_pair_success_NetflixRef_OTT;
            this.pair_fail = R.drawable.bluetooth_af_pair_failed_NetflixRef_OTT;
            return;
        }
        if ("4301 6".equals(this.MarketCode_OperatorCode)) {
            this.containName = "XXXXXXX";
            this.layout = R.layout.AMCO_X4_zte_remote;
            this.bfPair = R.id.bfPair;
            this.inPair = R.id.inPair;
            this.pairing_start = R.drawable.bluetooth_pairing_start_AMCO_X4;
            this.pairing = R.drawable.bluetooth_pairing_pairing_AMCO_X4;
            this.pair_success = R.drawable.bluetooth_af_pair_success_AMCO_X4;
            this.pair_fail = R.drawable.bluetooth_af_pair_failed_AMCO_X4;
            return;
        }
        if ("9999 10".equals(this.MarketCode_OperatorCode) || "9999 1".equals(this.MarketCode_OperatorCode)) {
            Log.d("[TvSettings]", "[ZTERemoteAnimationControl]mOemTag " + this.mOemTag);
            Log.d("[TvSettings]", "[ZTERemoteAnimationControl]MarketCode_OperatorCode " + this.MarketCode_OperatorCode);
            if (MarketUtil.isGeneralInternational() && ProductUtils.isB866V2FAS() && ProductUtils.isRTL8852BS()) {
                this.containName = "G10C";
                this.layout = R.layout.IBC_zte_remote;
                this.bfPair = R.id.bfPair;
                this.inPair = R.id.inPair;
                this.pairing_start = R.drawable.bluetooth_pairing_start_IBC;
                this.pairing = R.drawable.bluetooth_pairing_pairing_IBC;
                this.pair_success = R.drawable.bluetooth_af_pair_success_IBC;
                this.pair_fail = R.drawable.bluetooth_af_pair_failed_IBC;
                return;
            }
            if ("".equals(this.mOemTag) || (str = this.mOemTag) == null || str.length() < 12) {
                if ("9999 1".equals(this.MarketCode_OperatorCode)) {
                    this.containName = "ZTE";
                    this.layout = R.layout.netflix_h1_zte_remote;
                    this.bfPair = R.id.bfPair;
                    this.inPair = R.id.inPair;
                    this.pairing_start = R.drawable.bluetooth_pairing_start_netflix_h1;
                    this.pairing = R.drawable.bluetooth_pairing_pairing_netflix_h1;
                    this.pair_success = R.drawable.bluetooth_af_pair_H1_success;
                    this.pair_fail = R.drawable.bluetooth_af_pair_H1_failed;
                    return;
                }
                this.containName = "ZTE";
                this.layout = R.layout.zte_remote;
                this.bfPair = R.id.bfPair;
                this.inPair = R.id.inPair;
                this.pairing_start = R.drawable.bluetooth_pairing_start;
                this.pairing = R.drawable.bluetooth_pairing_pairing;
                this.pair_success = R.drawable.bluetooth_af_pair_success;
                this.pair_fail = R.drawable.bluetooth_af_pair_failed;
                return;
            }
            String str2 = this.mOemTag;
            if (str2 != null && !str2.equals("") && this.mOemTag.equals("ATV00100023100000000000999901113")) {
                this.containName = "ZTE";
                this.layout = R.layout.Spain_Zapi_OTT_12_remote;
                this.bfPair = R.id.bfPair;
                this.inPair = R.id.inPair;
                this.pairing_start = R.drawable.bluetooth_pairing_start_Spain_Zapi_OTT_12;
                this.pairing = R.drawable.bluetooth_pairing_pairing_Spain_Zapi_OTT_12;
                this.pair_success = R.drawable.bluetooth_af_pair_success_Spain_Zapi_OTT_12;
                this.pair_fail = R.drawable.bluetooth_af_pair_failed_Spain_Zapi_OTT_12;
                return;
            }
            String str3 = this.mOemTag;
            if (str3 != null && !str3.equals("") && this.mOemTag.equals("ATV001000230000000000004301061F9")) {
                this.containName = "ZTE";
                this.layout = R.layout.manufact_12_OTT_remote;
                this.bfPair = R.id.bfPair;
                this.inPair = R.id.inPair;
                this.pairing_start = R.drawable.bluetooth_pairing_start_Manufact_12_OTT;
                this.pairing = R.drawable.bluetooth_pairing_pairing_Manufact_12_OTT;
                this.pair_success = R.drawable.bluetooth_af_pair_success_Manufact_12_OTT;
                this.pair_fail = R.drawable.bluetooth_af_pair_failed_Manufact_12_OTT;
                return;
            }
            String str4 = this.mOemTag;
            if (str4 != null && !str4.equals("") && this.mOemTag.equals("ATV001000230000000000009999011F9")) {
                this.containName = "ZTE";
                this.layout = R.layout.manufact_12_OTT_remote_19;
                this.bfPair = R.id.bfPair;
                this.inPair = R.id.inPair;
                this.pairing_start = R.drawable.bluetooth_pairing_start_Manufact_12_OTT_19;
                this.pairing = R.drawable.bluetooth_pairing_pairing_Manufact_12_OTT_19;
                this.pair_success = R.drawable.bluetooth_af_pair_success_Manufact_12_OTT_19;
                this.pair_fail = R.drawable.bluetooth_af_pair_failed_Manufact_12_OTT_19;
                return;
            }
            char[] charArray = this.mOemTag.toCharArray();
            Log.d("[TvSettings]", "[ZTERemoteAnimationControl]arr " + ((Object) charArray));
            if ('0' == charArray[11]) {
                this.containName = "Remote";
                this.layout = R.layout.netflix_zte_remote;
                this.bfPair = R.id.bfPair;
                this.inPair = R.id.inPair;
                this.pairing_start = R.drawable.bluetooth_pairing_start_netflix;
                this.pairing = R.drawable.bluetooth_pairing_pairing_netflix;
                this.pair_success = R.drawable.bluetooth_af_pair_netflix_success;
                this.pair_fail = R.drawable.bluetooth_af_pair_netflix_failed;
                return;
            }
            if ('1' == charArray[11]) {
                this.containName = "ZTE";
                this.layout = R.layout.netflix_h1_zte_remote;
                this.bfPair = R.id.bfPair;
                this.inPair = R.id.inPair;
                this.pairing_start = R.drawable.bluetooth_pairing_start_netflix_h1;
                this.pairing = R.drawable.bluetooth_pairing_pairing_netflix_h1;
                this.pair_success = R.drawable.bluetooth_af_pair_H1_success;
                this.pair_fail = R.drawable.bluetooth_af_pair_H1_failed;
                return;
            }
            if ('2' == charArray[11]) {
                this.containName = "ZTE";
                this.layout = R.layout.netflix_b043_zte_remote;
                this.bfPair = R.id.bfPair;
                this.inPair = R.id.inPair;
                this.pairing_start = R.drawable.bluetooth_pairing_start_netflix_b043;
                this.pairing = R.drawable.bluetooth_pairing_pairing_netflix_b043;
                this.pair_success = R.drawable.bluetooth_af_pair_success_netflix_b043;
                this.pair_fail = R.drawable.bluetooth_af_pair_failed_netflix_b043;
                return;
            }
            this.containName = "ZTE";
            this.layout = R.layout.netflix_h1_zte_remote;
            this.bfPair = R.id.bfPair;
            this.inPair = R.id.inPair;
            this.pairing_start = R.drawable.bluetooth_pairing_start_netflix_h1;
            this.pairing = R.drawable.bluetooth_pairing_pairing_netflix_h1;
            this.pair_success = R.drawable.bluetooth_af_pair_H1_success;
            this.pair_fail = R.drawable.bluetooth_af_pair_H1_failed;
            return;
        }
        if ("3601 1".equals(this.MarketCode_OperatorCode)) {
            this.containName = "ZTE";
            this.layout = R.layout.in_zte_remote;
            this.bfPair = R.id.in_bfPair;
            this.inPair = R.id.in_inPair;
            this.pairing_start = R.drawable.in_bluetooth_pairing_start;
            this.pairing = R.drawable.in_bluetooth_pairing_pairing;
            this.pair_success = R.drawable.in_bluetooth_af_pair_success;
            this.pair_fail = R.drawable.in_bluetooth_af_pair_failed;
            return;
        }
        if ("4701 1".equals(this.MarketCode_OperatorCode)) {
            this.containName = "True";
            this.layout = R.layout.thailand_true_remote;
            this.bfPair = R.id.bfPair;
            this.inPair = R.id.inPair;
            this.pairing_start = R.drawable.bluetooth_pairing_start_thailand_true;
            this.pairing = R.drawable.bluetooth_pairing_pairing_thailand_true;
            this.pair_success = R.drawable.bluetooth_af_pair_thailand_true_success;
            this.pair_fail = R.drawable.bluetooth_af_pair_thailand_true_failed;
            return;
        }
        if ("6701 1".equals(this.MarketCode_OperatorCode)) {
            this.containName = "KHAMSIN";
            this.layout = R.layout.o2_remote;
            this.bfPair = R.id.bfPair;
            this.inPair = R.id.inPair;
            this.pairing_start = R.drawable.bluetooth_pairing_start_o2;
            this.pairing = R.drawable.bluetooth_pairing_pairing_o2;
            this.pair_success = R.drawable.bluetooth_af_pair_success_o2;
            this.pair_fail = R.drawable.bluetooth_af_pair_failed_o2;
            return;
        }
        if ("6701 2".equals(this.MarketCode_OperatorCode)) {
            this.containName = "KHAMSIN";
            this.layout = R.layout.cz_o2_remote;
            this.bfPair = R.id.bfPair;
            this.inPair = R.id.inPair;
            this.pairing_start = R.drawable.bluetooth_pairing_start_cz_o2;
            this.pairing = R.drawable.bluetooth_pairing_pairing_cz_o2;
            this.pair_success = R.drawable.bluetooth_af_pair_success_cz_o2;
            this.pair_fail = R.drawable.bluetooth_af_pair_failed_cz_o2;
            return;
        }
        if ("4501 2".equals(this.MarketCode_OperatorCode)) {
            this.containName = "KHAMSIN";
            this.layout = R.layout.telenor_remote;
            this.bfPair = R.id.bfPair;
            this.inPair = R.id.inPair;
            this.pairing_start = R.drawable.bluetooth_pairing_start_telenor;
            this.pairing = R.drawable.bluetooth_pairing_pairing_telenor;
            this.pair_success = R.drawable.bluetooth_af_pair_success_telenor;
            this.pair_fail = R.drawable.bluetooth_af_pair_failed_telenor;
            return;
        }
        if ("1801 4".equals(this.MarketCode_OperatorCode) || "8901 1".equals(this.MarketCode_OperatorCode) || "5101 2".equals(this.MarketCode_OperatorCode)) {
            this.containName = "ZTE";
            this.layout = R.layout.Bulgaria_A1_remote_you;
            this.bfPair = R.id.bfPair;
            this.inPair = R.id.inPair;
            this.pairing_start = R.drawable.bluetooth_pairing_start_Bulgaria_A1_You;
            this.pairing = R.drawable.bluetooth_pairing_pairing_Bulgaria_A1_You;
            this.pair_success = R.drawable.bluetooth_af_pair_success_Bulgaria_A1_You;
            this.pair_fail = R.drawable.bluetooth_af_pair_failed_Bulgaria_A1_You;
            return;
        }
        if ("8701 1".equals(this.MarketCode_OperatorCode)) {
            this.containName = "elisa";
            this.layout = R.layout.Elisal_OTT_remote;
            this.bfPair = R.id.bfPair;
            this.inPair = R.id.inPair;
            this.pairing_start = R.drawable.bluetooth_pairing_start_Elisal_OTT;
            this.pairing = R.drawable.bluetooth_pairing_pairing_Elisal_OTT;
            this.pair_success = R.drawable.bluetooth_af_pair_success_Elisal_OTT;
            this.pair_fail = R.drawable.bluetooth_af_pair_failed_Elisal_OTT;
            return;
        }
        if ("7501 8".equals(this.MarketCode_OperatorCode)) {
            this.containName = "brazil_Unifique";
            this.layout = R.layout.Brazil_Unifique_remote;
            this.bfPair = R.id.bfPair;
            this.inPair = R.id.inPair;
            this.pairing_start = R.drawable.bluetooth_pairing_start_Brazil_Unifique;
            this.pairing = R.drawable.bluetooth_pairing_pairing_Brazil_Unifique;
            this.pair_success = R.drawable.bluetooth_af_pair_Brazil_Unifique_success;
            this.pair_fail = R.drawable.bluetooth_af_pair_Brazil_Unifique_failed;
            return;
        }
        if ("8801 2".equals(this.MarketCode_OperatorCode)) {
            this.containName = "ZTE";
            this.layout = R.layout.Belgium_Telenet_OTT_remote;
            this.bfPair = R.id.bfPair;
            this.inPair = R.id.inPair;
            this.pairing_start = R.drawable.bluetooth_pairing_start_Belgium_Telenet_OTT;
            this.pairing = R.drawable.bluetooth_pairing_pairing_Belgium_Telenet_OTT;
            this.pair_success = R.drawable.bluetooth_af_pair_success_Belgium_Telenet_OTT;
            this.pair_fail = R.drawable.bluetooth_af_pair_failed_Belgium_Telenet_OTT;
            return;
        }
        if ("3901 3".equals(this.MarketCode_OperatorCode)) {
            this.containName = "ZTE";
            this.layout = R.layout.Spain_Zapi_OTT_12_remote;
            this.bfPair = R.id.bfPair;
            this.inPair = R.id.inPair;
            this.pairing_start = R.drawable.bluetooth_pairing_start_Spain_Zapi_OTT_12;
            this.pairing = R.drawable.bluetooth_pairing_pairing_Spain_Zapi_OTT_12;
            this.pair_success = R.drawable.bluetooth_af_pair_success_Spain_Zapi_OTT_12;
            this.pair_fail = R.drawable.bluetooth_af_pair_failed_Spain_Zapi_OTT_12;
            return;
        }
        if ("3501 3".equals(this.MarketCode_OperatorCode)) {
            this.containName = "ZTE";
            this.layout = R.layout.Nordnet_OTT_remote;
            this.bfPair = R.id.bfPair;
            this.inPair = R.id.inPair;
            this.pairing_start = R.drawable.bluetooth_pairing_start;
            this.pairing = R.drawable.bluetooth_pairing_pairing;
            this.pair_success = R.drawable.bluetooth_af_pair_success;
            this.pair_fail = R.drawable.bluetooth_af_pair_failed;
            return;
        }
        if (MarketUtil.isBOHER_OTT_12()) {
            this.containName = "BHT";
            this.layout = R.layout.bluetooth_pair_video;
            this.bfPair = R.id.bfPair_v;
            this.inPair = R.id.inPair_v;
            this.pairing_start = R.drawable.bluetooth_pairing_start;
            this.pairing = R.drawable.bluetooth_pairing_pairing;
            this.pair_success = R.drawable.bluetooth_af_pair_success;
            this.pair_fail = R.drawable.bluetooth_af_pair_failed;
            return;
        }
        if ("5601 2".equals(this.MarketCode_OperatorCode)) {
            this.containName = "ZTE";
            this.layout = R.layout.zte_remote_Philippines;
            this.bfPair = R.id.bfPair;
            this.inPair = R.id.inPair;
            this.pairing_start = R.drawable.bluetooth_pairing_start_Philippines;
            this.pairing = R.drawable.bluetooth_pairing_pairing_Philippines;
            this.pair_success = R.drawable.bluetooth_af_pair_success_Philippines;
            this.pair_fail = R.drawable.bluetooth_af_pair_failed_Philippines;
            return;
        }
        if (MarketUtil.isFranceNetgem()) {
            this.containName = "ZTE";
            this.layout = R.layout.fr_zte_remote;
            this.bfPair = R.id.bfPair;
            this.inPair = R.id.inPair;
            this.pairing_start = R.drawable.fr_bluetooth_pairing_start;
            this.pairing = R.drawable.fr_bluetooth_pairing_pairing;
            this.pair_success = R.drawable.fr_bluetooth_af_pair_success;
            this.pair_fail = R.drawable.fr_bluetooth_af_pair_failed;
            return;
        }
        if (MarketUtil.isTurkeyTele()) {
            this.containName = "G20";
            this.layout = R.layout.italy_remote;
            this.bfPair = R.id.bfPair;
            this.inPair = R.id.inPair;
            this.pairing = R.drawable.bluetooth_pairing_pairing_italy;
            this.pair_success = R.drawable.bluetooth_af_pair_success_italy_0401;
            this.pair_fail = R.drawable.bluetooth_af_pair_failed_italy_0401;
            return;
        }
        if ("2501 6".equals(this.MarketCode_OperatorCode)) {
            this.containName = "ZTE";
            this.layout = R.layout.dialnet_zte_remote;
            this.bfPair = R.id.bfPair;
            this.inPair = R.id.inPair;
            this.pairing_start = R.drawable.dialnet_bluetooth_pairing_start;
            this.pairing = R.drawable.dialnet_bluetooth_pairing_pairing;
            this.pair_success = R.drawable.dialnet_bluetooth_af_pair_success;
            this.pair_fail = R.drawable.dialnet_bluetooth_af_pair_failed;
            return;
        }
        this.containName = "ZTE";
        this.layout = R.layout.zte_remote;
        this.bfPair = R.id.bfPair;
        this.inPair = R.id.inPair;
        this.pairing_start = R.drawable.bluetooth_pairing_start;
        this.pairing = R.drawable.bluetooth_pairing_pairing;
        this.pair_success = R.drawable.bluetooth_af_pair_success;
        this.pair_fail = R.drawable.bluetooth_af_pair_failed;
    }
}
