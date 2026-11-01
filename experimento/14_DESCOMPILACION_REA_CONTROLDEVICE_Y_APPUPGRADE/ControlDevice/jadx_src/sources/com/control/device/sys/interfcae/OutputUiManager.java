package com.control.device.sys.interfcae;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import com.android.tv.settings.adv.IDolbyVisionSettingManager;
import com.android.tv.settings.adv.IOutputModeManager;
import com.android.tv.settings.adv.impl.DolbyVisionSettingManager;
import com.android.tv.settings.adv.impl.OutputModeManager;
import com.control.device.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* loaded from: classes.dex */
public class OutputUiManager {
    private static String[] mHdmiColorTitleList;
    private static String[] mHdmiColorValueList;
    private static String[] mHdmiTitleList;
    private static String[] mHdmiValueList;
    private static String mUiMode;
    private static String tvSupportDolbyVisionMode;
    private static String tvSupportDolbyVisionType;
    private Context mContext;
    private DisplayCapabilityManager mDisplayCapabilityManager;
    private IDolbyVisionSettingManager mIDolbyVisionSettingManager;
    private IOutputModeManager mIOutputModeManager;
    private static final String[] HDMI_LIST = {"2160p60hz", "2160p50hz", "2160p30hz", "2160p25hz", "2160p24hz", "smpte24hz", "1080p60hz", "1080p50hz", "1080p24hz", "720p60hz", "720p50hz", "576p50hz", "480p60hz"};
    private static final String[] HDMI_TITLE = {"4k2k 60hz", "4k2k 50hz", "4k2k 30hz", "4k2k 25hz", "4k2k 24hz", "4k2k smpte", "1080p 60hz", "1080p 50hz", "1080p 24hz", "720p 60hz", "720p 50hz", "576p 50hz", "480p 60hz"};
    public static final String[] HDMI_COLOR_LIST = {"444,12bit", "444,10bit", "444,8bit", "422,12bit", "420,12bit", "420,10bit", "420,8bit", "rgb,12bit", "rgb,10bit", "rgb,8bit"};
    public static final String[] HDMI_COLOR_TITLE = {"YCbCr444 12bit", "YCbCr444 10bit", "YCbCr444 8bit", "YCbCr422 12bit", "YCbCr420 12bit", "YCbCr420 10bit", "YCbCr420 8bit", "RGB 12bit", "RGB 10bit", "RGB 8bit"};
    public static final String[] COLOR_SPACE_LIST = {"444", "422", "420", "rgb"};
    public static final String[] COLOR_SPACE_TITLE = {"YCbCr444", "YCbCr422", "YCbCr420", "RGB"};
    public static final String[] COLOR_DEPTH_LIST = {"16bit", "12bit", "10bit", "8bit"};
    public static final String[] CVBS_MODE_VALUE_LIST = {"480cvbs", "576cvbs"};
    private static final String[] CVBS_MODE_TITLE_LIST = {"480 CVBS", "576 CVBS"};
    public static final String[] CVBS_MODE_VALUE_LIST_MEXICO_AMCO = {"480cvbs", "576cvbs", "pal_n", "pal_m", "ntsc_m"};
    private static final String[] CVBS_MODE_TITLE_LIST_MEXICO_AMCO = {"480 CVBS", "576 CVBS", "PAL_N", "PAL_M", "NTSC_M"};
    private static final String[] DOLBY_VISION_TYPE = {"DV_RGB_444_8BIT", "LL_YCbCr_422_12BIT", "LL_RGB_444_12BIT", "LL_RGB_444_10BIT"};
    private String mMarketOperatorCode = SystemProperties.get("config.MarketCode") + " " + SystemProperties.get("config.OperatorCode");
    private ArrayList<String> mTitleList = new ArrayList<>();
    private ArrayList<String> mValueList = new ArrayList<>();
    private ArrayList<String> mSupportList = new ArrayList<>();
    private ArrayList<String> mColorTitleList = new ArrayList<>();
    private ArrayList<String> mColorValueList = new ArrayList<>();

    public OutputUiManager(Context context) {
        this.mContext = context;
        this.mIOutputModeManager = OutputModeManager.getInstance(context);
        this.mDisplayCapabilityManager = DisplayCapabilityManager.getDisplayCapabilityManager(this.mContext);
        this.mIDolbyVisionSettingManager = new DolbyVisionSettingManager(this.mContext);
        String uiMode = getUiMode();
        mUiMode = uiMode;
        initModeValues(uiMode);
        initColorValues(mUiMode);
    }

    public String getUiMode() {
        String currentOutputMode = this.mIOutputModeManager.getCurrentOutputMode();
        Log.d("OutputUiManager", "getUiMode currentMode = " + currentOutputMode);
        if (currentOutputMode.contains("cvbs") || Arrays.asList(CVBS_MODE_VALUE_LIST_MEXICO_AMCO).contains(currentOutputMode)) {
            mUiMode = "cvbs";
        } else {
            mUiMode = "hdmi";
        }
        return mUiMode;
    }

    public void updateUiMode() {
        this.mDisplayCapabilityManager.refresh();
        String uiMode = getUiMode();
        mUiMode = uiMode;
        initModeValues(uiMode);
        initColorValues(mUiMode);
    }

    public boolean isHdmiMode() {
        return (mUiMode.contains("cvbs") || Arrays.asList(CVBS_MODE_VALUE_LIST_MEXICO_AMCO).contains(mUiMode)) ? false : true;
    }

    public String getCurrentMode() {
        return this.mIOutputModeManager.getCurrentOutputMode();
    }

    private void initColorValues(String str) {
        filterColorAttribute();
        this.mColorTitleList.clear();
        this.mColorValueList.clear();
        if (str.equalsIgnoreCase("hdmi")) {
            for (int i = 0; i < mHdmiColorValueList.length; i++) {
                String[] strArr = mHdmiColorTitleList;
                if (strArr[i] != null && strArr[i].length() != 0) {
                    this.mColorTitleList.add(mHdmiColorTitleList[i]);
                    this.mColorValueList.add(mHdmiColorValueList[i]);
                }
            }
        }
    }

    public void filterColorAttribute() {
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        mHdmiColorValueList = HDMI_COLOR_LIST;
        mHdmiColorTitleList = HDMI_COLOR_TITLE;
        int i = 0;
        while (true) {
            String[] strArr = mHdmiColorValueList;
            if (i >= strArr.length) {
                break;
            }
            if (strArr[i] != null) {
                arrayList.add(strArr[i]);
                arrayList2.add(mHdmiColorTitleList[i]);
            }
            i++;
        }
        String hdmiColorSupportList = this.mIOutputModeManager.getHdmiColorSupportList();
        if (hdmiColorSupportList != null && hdmiColorSupportList.length() != 0 && !hdmiColorSupportList.contains("null")) {
            ArrayList arrayList3 = new ArrayList();
            ArrayList arrayList4 = new ArrayList();
            for (int i2 = 0; i2 < arrayList.size(); i2++) {
                if (hdmiColorSupportList.contains((CharSequence) arrayList.get(i2))) {
                    arrayList3.add((String) arrayList.get(i2));
                    arrayList4.add((String) arrayList2.get(i2));
                }
            }
            mHdmiColorValueList = (String[]) arrayList3.toArray(new String[arrayList.size()]);
            mHdmiColorTitleList = (String[]) arrayList4.toArray(new String[arrayList2.size()]);
            return;
        }
        mHdmiColorValueList = new String[]{""};
        mHdmiColorTitleList = new String[]{"No data!"};
    }

    public boolean isModeSupportColor(String str, String str2) {
        return this.mIOutputModeManager.isModeSupportColor(str, str2);
    }

    private void initModeValues(String str) {
        filterOutputMode();
        this.mTitleList.clear();
        this.mValueList.clear();
        int i = 0;
        if (str.equalsIgnoreCase("hdmi")) {
            while (i < mHdmiValueList.length) {
                String[] strArr = mHdmiTitleList;
                if (strArr[i] != null && strArr[i].length() != 0) {
                    if ("6701 1".equals(this.mMarketOperatorCode) || "6701 2".equals(this.mMarketOperatorCode) || "4501 2".equals(this.mMarketOperatorCode)) {
                        if (mHdmiTitleList[i].contains("hz")) {
                            Log.d("OutputUiManager", "<initModeValues> mHdmiTitleList: " + mHdmiTitleList[i]);
                            this.mTitleList.add(mHdmiTitleList[i].replace("hz", "Hz"));
                            this.mValueList.add(mHdmiValueList[i]);
                        }
                    } else if ("4301 4".equals(this.mMarketOperatorCode)) {
                        if (!mHdmiTitleList[i].contains("480") && !mHdmiTitleList[i].contains("576")) {
                            this.mTitleList.add(mHdmiTitleList[i]);
                            this.mValueList.add(mHdmiValueList[i]);
                        }
                    } else {
                        this.mTitleList.add(mHdmiTitleList[i]);
                        this.mValueList.add(mHdmiValueList[i]);
                    }
                }
                i++;
            }
            return;
        }
        if (!str.equalsIgnoreCase("cvbs")) {
            return;
        }
        if ("4301 6".equals(this.mMarketOperatorCode)) {
            while (true) {
                String[] strArr2 = CVBS_MODE_VALUE_LIST_MEXICO_AMCO;
                if (i >= strArr2.length) {
                    return;
                }
                this.mTitleList.add(strArr2[i]);
                this.mValueList.add(strArr2[i]);
                i++;
            }
        } else {
            int i2 = 0;
            while (true) {
                String[] strArr3 = CVBS_MODE_VALUE_LIST;
                if (i2 >= strArr3.length) {
                    break;
                }
                this.mTitleList.add(strArr3[i2]);
                i2++;
            }
            while (true) {
                String[] strArr4 = CVBS_MODE_VALUE_LIST;
                if (i >= strArr4.length) {
                    return;
                }
                this.mValueList.add(strArr4[i]);
                i++;
            }
        }
    }

    public void change2NewMode(String str) {
        this.mDisplayCapabilityManager.setResolutionAndRefreshRateByMode(str);
    }

    public void change2BestMode() {
        this.mDisplayCapabilityManager.change2BestMode();
    }

    public boolean isBestOutputmode() {
        return this.mIOutputModeManager.isBestOutputmode();
    }

    public void filterOutputMode() {
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        mHdmiValueList = HDMI_LIST;
        mHdmiTitleList = HDMI_TITLE;
        int i = 0;
        while (true) {
            String[] strArr = mHdmiValueList;
            if (i >= strArr.length) {
                break;
            }
            if (strArr[i] != null) {
                arrayList.add(strArr[i]);
                if (this.mIOutputModeManager.getFrameRateOffset().contains("1")) {
                    if (mHdmiTitleList[i].contains("60hz")) {
                        arrayList2.add(mHdmiTitleList[i].replace("60hz", "60hz"));
                    } else if (mHdmiTitleList[i].contains("30hz")) {
                        arrayList2.add(mHdmiTitleList[i].replace("30hz", "30hz"));
                    } else if (mHdmiTitleList[i].contains("24hz")) {
                        arrayList2.add(mHdmiTitleList[i].replace("24hz", "24hz"));
                    } else {
                        arrayList2.add(mHdmiTitleList[i]);
                    }
                } else {
                    arrayList2.add(mHdmiTitleList[i]);
                }
            }
            i++;
        }
        String string = this.mContext.getResources().getString(R.string.display_filter_outputmode);
        if (string != null && string.length() != 0) {
            for (String str : string.split(",")) {
                for (int i2 = 0; i2 < arrayList.size(); i2++) {
                    if (((String) arrayList.get(i2)).toString().equals(str)) {
                        arrayList.remove(i2);
                        arrayList2.remove(i2);
                    }
                }
            }
        }
        String hdmiSupportList = this.mIOutputModeManager.getHdmiSupportList();
        if (hdmiSupportList != null && hdmiSupportList.length() != 0 && !hdmiSupportList.contains("null")) {
            ArrayList arrayList3 = new ArrayList();
            ArrayList arrayList4 = new ArrayList();
            for (int i3 = 0; i3 < arrayList.size(); i3++) {
                if (hdmiSupportList.contains((CharSequence) arrayList.get(i3))) {
                    arrayList3.add((String) arrayList.get(i3));
                    arrayList4.add((String) arrayList2.get(i3));
                }
            }
            List<String> hdmiModeLists = this.mDisplayCapabilityManager.getHdmiModeLists();
            List<String> hdmiTitleLists = this.mDisplayCapabilityManager.getHdmiTitleLists();
            ArrayList arrayList5 = new ArrayList();
            ArrayList arrayList6 = new ArrayList();
            if (isDolbyVisionEnable() && isTvSupportDolbyVision()) {
                for (int i4 = 0; i4 < hdmiModeLists.size(); i4++) {
                    if (resolveResolutionValue(hdmiModeLists.get(i4)) > resolveResolutionValue(tvSupportDolbyVisionMode)) {
                        Log.e("OutputUiManager", "This TV not Support Dolby Vision in " + hdmiModeLists.get(i4));
                    } else if (isDolbyVisionEnable() && isTvSupportDolbyVision()) {
                        if (!hdmiModeLists.get(i4).contains("smpte") && !hdmiModeLists.get(i4).contains("i")) {
                            int dolbyVisionType = this.mIDolbyVisionSettingManager.getDolbyVisionType();
                            if (dolbyVisionType != 1) {
                                if (dolbyVisionType == 2) {
                                    if (isModeSupportColor(hdmiModeLists.get(i4), "422,12bit") || isModeSupportColor(hdmiModeLists.get(i4), "422,10bit")) {
                                        arrayList5.add(hdmiModeLists.get(i4));
                                        arrayList6.add(hdmiTitleLists.get(i4));
                                    }
                                } else if (dolbyVisionType == 3 && resolveResolutionValue(hdmiModeLists.get(i4)) <= resolveResolutionValue("1080p60hz") && (isModeSupportColor(hdmiModeLists.get(i4), "444,12bit") || isModeSupportColor(hdmiModeLists.get(i4), "444,10bit"))) {
                                    arrayList5.add(hdmiModeLists.get(i4));
                                    arrayList6.add(hdmiTitleLists.get(i4));
                                }
                            } else if (isModeSupportColor(hdmiModeLists.get(i4), "444,8bit")) {
                                arrayList5.add(hdmiModeLists.get(i4));
                                arrayList6.add(hdmiTitleLists.get(i4));
                            }
                        }
                    } else {
                        arrayList5.add(hdmiModeLists.get(i4));
                        arrayList6.add(hdmiTitleLists.get(i4));
                    }
                }
                mHdmiValueList = (String[]) arrayList5.toArray(new String[arrayList.size()]);
                mHdmiTitleList = (String[]) arrayList6.toArray(new String[arrayList2.size()]);
                return;
            }
            mHdmiValueList = (String[]) hdmiModeLists.toArray(new String[arrayList.size()]);
            mHdmiTitleList = (String[]) hdmiTitleLists.toArray(new String[arrayList2.size()]);
            return;
        }
        mHdmiValueList = (String[]) arrayList.toArray(new String[arrayList.size()]);
        mHdmiTitleList = (String[]) arrayList2.toArray(new String[arrayList2.size()]);
    }

    public boolean isTvSupportDolbyVision() {
        String isTvSupportDolbyVision = this.mIDolbyVisionSettingManager.isTvSupportDolbyVision();
        tvSupportDolbyVisionType = null;
        if (!isTvSupportDolbyVision.equals("")) {
            int i = 0;
            int i2 = 0;
            while (true) {
                String[] strArr = HDMI_LIST;
                if (i2 >= strArr.length) {
                    break;
                }
                if (isTvSupportDolbyVision.contains(strArr[i2])) {
                    tvSupportDolbyVisionMode = strArr[i2];
                    break;
                }
                i2++;
            }
            while (true) {
                String[] strArr2 = DOLBY_VISION_TYPE;
                if (i >= strArr2.length) {
                    break;
                }
                if (isTvSupportDolbyVision.contains(strArr2[i])) {
                    tvSupportDolbyVisionType += strArr2[i];
                }
                i++;
            }
        } else {
            tvSupportDolbyVisionMode = "";
            tvSupportDolbyVisionType = "";
        }
        return !tvSupportDolbyVisionMode.equals("");
    }

    public boolean isDolbyVisionEnable() {
        return this.mIDolbyVisionSettingManager.isDolbyVisionEnable();
    }

    public long resolveResolutionValue(String str) {
        return this.mIDolbyVisionSettingManager.resolveResolutionValue(str);
    }

    public boolean isHDMIPlugged() {
        return this.mIOutputModeManager.isHDMIPlugged();
    }
}
