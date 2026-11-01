package com.control.device.sys.interfcae;

import android.content.ContentResolver;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.SystemProperties;
import android.util.Log;
import android.view.Display;
import com.android.tv.settings.adv.IDolbyVisionSettingManager;
import com.android.tv.settings.adv.IOutputModeManager;
import com.android.tv.settings.adv.ISystemControlManager;
import com.android.tv.settings.adv.impl.DolbyVisionSettingManager;
import com.android.tv.settings.adv.impl.OutputModeManager;
import com.android.tv.settings.adv.impl.SystemControlManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/* loaded from: classes.dex */
public class DisplayCapabilityManager {
    private static final String TAG = "DisplayCapabilityManager";
    private static volatile DisplayCapabilityManager mDisplayCapabilityManager;
    private final ContentResolver mContentResolver;
    private DisplayManager mDisplayManager;
    private volatile List<String> mDolbyVisionModeList;
    private volatile List<String> mHdmiColorAttributeList;
    private volatile List<String> mHdmiDeepColorAttributeList;
    private volatile List<String> mHdmiModeList;
    private final IDolbyVisionSettingManager mIDolbyVisionSettingManager;
    private final IOutputModeManager mIOutputModeManager;
    private static final String[] CZECH_MODE_RESOLUTION_FIRST = {"720p50hz", "1080p50hz", "2160p50hz"};
    private static final String[] DOLBY_VISION_TYPE = {"DV_RGB_444_8BIT", "LL_YCbCr_422_12BIT", "LL_RGB_444_12BIT", "LL_RGB_444_10BIT"};
    private static final List<String> HDMI_MODE_LIST = ImmutableList.of("2160p60hz", "2160p50hz", "2160p30hz", "2160p25hz", "2160p24hz", "smpte24hz", "1080p60hz", "1080p50hz", "1080p24hz", "720p60hz", "720p50hz", "576p50hz", "480p60hz", "576cvbs", "480cvbs");
    private static final List<String> HDMI_MODE_TITLE_LIST = ImmutableList.of("4k 60Hz", "4k 50Hz", "4k 30Hz", "4k 25Hz", "4k 24Hz", "4k smpte", "1080p 60Hz", "1080p 50Hz", "1080p 24Hz", "720p 60Hz", "720p 50Hz", "576p 50Hz", "480p 60Hz", "576 CVBS", "480 CVBS");
    private static final List<String> HDMI_COLOR_LIST = ImmutableList.of("420,8bit", "420,10bit", "420,12bit", "422,12bit", "444,8bit", "444,10bit", "444,12bit", "rgb,8bit", "rgb,10bit", "rgb,12bit");
    private static final Set<String> HDMI_DEEP_COLOR_SET = ImmutableSet.of("420,10bit", "420,12bit", "422,12bit", "444,10bit", "444,12bit", "rgb,10bit", "rgb,12bit");
    private static final List<String> HDMI_COLOR_TITLE_LIST = ImmutableList.of("YCbCr 4:2:0 8-bit", "YCbCr 4:2:0 10-bit", "YCbCr 4:2:0 12-bit", "YCbCr 4:2:2 12-bit", "YCbCr 4:4:4 8-bit", "YCbCr 4:4:4 10-bit", "YCbCr 4:4:4 12-bit", "RGB 8-bit", "RGB 10-bit", "RGB 12-bit");
    private static final ImmutableMap<String, Display.Mode> USER_PREFERRED_MODE_BY_MODE = new ImmutableMap.Builder().put("2160p60hz", new Display.Mode(3840, 2160, 60.000004f)).put("2160p59.94hz", new Display.Mode(3840, 2160, 59.94006f)).put("2160p50hz", new Display.Mode(3840, 2160, 50.0f)).put("2160p30hz", new Display.Mode(3840, 2160, 30.000002f)).put("2160p29.97hz", new Display.Mode(3840, 2160, 29.97003f)).put("2160p25hz", new Display.Mode(3840, 2160, 25.0f)).put("2160p24hz", new Display.Mode(3840, 2160, 24.000002f)).put("2160p23.976hz", new Display.Mode(3840, 2160, 23.976025f)).put("smpte24hz", new Display.Mode(4096, 2160, 24.000002f)).put("smpte23.976hz", new Display.Mode(4096, 2160, 23.976025f)).put("1080p60hz", new Display.Mode(1920, 1080, 60.000004f)).put("1080p59.94hz", new Display.Mode(1920, 1080, 59.94006f)).put("1080p50hz", new Display.Mode(1920, 1080, 50.0f)).put("1080p24hz", new Display.Mode(1920, 1080, 24.000002f)).put("1080p23.976hz", new Display.Mode(1920, 1080, 23.976025f)).put("720p60hz", new Display.Mode(1280, 720, 60.000004f)).put("720p59.94hz", new Display.Mode(1280, 720, 59.94006f)).put("720p50hz", new Display.Mode(1280, 720, 50.0f)).put("576p50hz", new Display.Mode(720, 576, 50.0f)).put("480p60hz", new Display.Mode(720, 480, 60.000004f)).put("480p59.94hz", new Display.Mode(720, 480, 59.94006f)).put("576cvbs", new Display.Mode(720, 576, 50.0f)).put("480cvbs", new Display.Mode(720, 480, 60.000004f)).build();
    private static final Map<String, String> MODE_TITLE_BY_MODE = new HashMap();
    private static final Map<String, String> COLOR_TITLE_BY_ATTR = new HashMap();
    private String mMarketCode = SystemProperties.get("config.MarketCode");
    private String mOperatorCode = SystemProperties.get("config.OperatorCode");
    private final ReadWriteLock mRWLock = new ReentrantReadWriteLock();
    private boolean mIsHdr10Supported = false;
    private final ISystemControlManager mISystemControlManager = SystemControlManager.getInstance();

    static {
        int i = 0;
        int i2 = 0;
        while (true) {
            List<String> list = HDMI_MODE_LIST;
            if (i2 >= list.size()) {
                break;
            }
            MODE_TITLE_BY_MODE.put(list.get(i2), HDMI_MODE_TITLE_LIST.get(i2));
            i2++;
        }
        while (true) {
            List<String> list2 = HDMI_COLOR_LIST;
            if (i >= list2.size()) {
                return;
            }
            COLOR_TITLE_BY_ATTR.put(list2.get(i), HDMI_COLOR_TITLE_LIST.get(i));
            i++;
        }
    }

    public static DisplayCapabilityManager getDisplayCapabilityManager(Context context) {
        if (mDisplayCapabilityManager == null) {
            synchronized (DisplayCapabilityManager.class) {
                if (mDisplayCapabilityManager == null) {
                    mDisplayCapabilityManager = new DisplayCapabilityManager(context);
                }
            }
        }
        return mDisplayCapabilityManager;
    }

    private DisplayCapabilityManager(Context context) {
        this.mDisplayManager = (DisplayManager) context.getSystemService("display");
        this.mIOutputModeManager = new OutputModeManager(context);
        this.mIDolbyVisionSettingManager = new DolbyVisionSettingManager(context);
        this.mContentResolver = context.getContentResolver();
        refresh();
    }

    public boolean refresh() {
        return updateSupportForHdr10() | updateHdmiModes() | false | updateDolbyVisionModes() | updateHdmiColorAttributes();
    }

    private boolean updateHdmiModes() {
        ArrayList arrayList = this.mHdmiModeList != null ? new ArrayList(this.mHdmiModeList) : null;
        String hdmiSupportList = this.mIOutputModeManager.getHdmiSupportList();
        Log.d(TAG, "getHdmiSupportList:" + hdmiSupportList);
        if (hdmiSupportList != null && hdmiSupportList.length() != 0 && !hdmiSupportList.contains("null")) {
            ArrayList arrayList2 = new ArrayList();
            int i = 0;
            while (true) {
                List<String> list = HDMI_MODE_LIST;
                if (i >= list.size()) {
                    break;
                }
                if (hdmiSupportList.contains(list.get(i))) {
                    arrayList2.add(list.get(i));
                }
                i++;
            }
            this.mHdmiModeList = arrayList2;
        } else {
            this.mHdmiModeList = HDMI_MODE_LIST;
        }
        Lock writeLock = this.mRWLock.writeLock();
        writeLock.lock();
        filterNoSupportMode(this.mHdmiModeList);
        writeLock.unlock();
        return arrayList == null || !arrayList.equals(this.mHdmiModeList);
    }

    private boolean isContainsInFW(String str) {
        if (isCvbsMode()) {
            return true;
        }
        Display.Mode[] supportedModes = this.mDisplayManager.getDisplay(0).getSupportedModes();
        ImmutableMap<String, Display.Mode> immutableMap = USER_PREFERRED_MODE_BY_MODE;
        Log.d(TAG, "framework support mode: " + Arrays.toString(supportedModes));
        int length = supportedModes.length;
        for (int i = 0; i < length; i++) {
            if (supportedModes[i].matches(immutableMap.get(str).getPhysicalWidth(), immutableMap.get(str).getPhysicalHeight(), immutableMap.get(str).getRefreshRate())) {
                return true;
            }
        }
        return false;
    }

    private void filterNoSupportMode(List<String> list) {
        Iterator<String> it = list.iterator();
        ArrayList arrayList = new ArrayList();
        while (it.hasNext()) {
            String filterHdmiModes = filterHdmiModes(it.next());
            if (isContainsInFW(filterHdmiModes)) {
                arrayList.add(filterHdmiModes);
            }
        }
        String str = TAG;
        Log.d(str, "systemControlModeList: " + arrayList);
        Log.d(str, "sysHdmiModeIterator: " + it);
    }

    private String filterHdmiModes(String str) {
        String str2 = TAG;
        Log.d(str2, "filterHdimMode: " + str);
        IOutputModeManager iOutputModeManager = this.mIOutputModeManager;
        if ((iOutputModeManager != null && iOutputModeManager.getFrameRateOffset() != null && !this.mIOutputModeManager.getFrameRateOffset().contains("1")) || str == null) {
            return str;
        }
        if (str.contains("60Hz")) {
            str = str.replace("60Hz", "59.94Hz");
        } else if (str.contains("30Hz")) {
            str = str.replace("30Hz", "29.97Hz");
        } else if (str.contains("24Hz")) {
            str = str.replace("24Hz", "23.976Hz");
        } else if (str.contains("60hz")) {
            str = str.replace("60hz", "59.94hz");
        } else if (str.contains("30hz")) {
            str = str.replace("30hz", "29.97hz");
        } else if (str.contains("24hz")) {
            str = str.replace("24hz", "23.976hz");
        }
        Log.d(str2, "filterHdmiModeStr: " + str);
        return str;
    }

    private boolean updateDolbyVisionModes() {
        List<String> list = this.mDolbyVisionModeList;
        String highestDolbyVisionMode = getHighestDolbyVisionMode();
        if (highestDolbyVisionMode.isEmpty()) {
            this.mDolbyVisionModeList = new ArrayList();
            return !this.mDolbyVisionModeList.equals(list);
        }
        String hdmiSupportList = this.mIOutputModeManager.getHdmiSupportList();
        if (hdmiSupportList != null && hdmiSupportList.length() != 0 && !hdmiSupportList.contains("null")) {
            this.mDolbyVisionModeList = new ArrayList();
            long resolveResolutionValue = this.mIDolbyVisionSettingManager.resolveResolutionValue(highestDolbyVisionMode);
            for (String str : this.mHdmiModeList) {
                if (!str.contains("smpte") && !str.contains("i") && this.mIDolbyVisionSettingManager.resolveResolutionValue(str) <= resolveResolutionValue && (doesModeSupportColor(str, "444,8bit") || doesModeSupportColor(str, "422,12bit") || doesModeSupportColor(str, "422,10bit"))) {
                    this.mDolbyVisionModeList.add(str);
                }
            }
        } else {
            this.mDolbyVisionModeList = HDMI_MODE_LIST;
        }
        return !this.mDolbyVisionModeList.equals(list);
    }

    private String getHighestDolbyVisionMode() {
        String isTvSupportDolbyVision = this.mIDolbyVisionSettingManager.isTvSupportDolbyVision();
        if (isTvSupportDolbyVision != null && !isTvSupportDolbyVision.isEmpty()) {
            for (String str : HDMI_MODE_LIST) {
                if (isTvSupportDolbyVision.contains(str)) {
                    return str;
                }
            }
        }
        return "";
    }

    private boolean updateHdmiColorAttributes() {
        List<String> list = this.mHdmiColorAttributeList;
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        String hdmiColorSupportList = this.mIOutputModeManager.getHdmiColorSupportList();
        if (hdmiColorSupportList != null && hdmiColorSupportList.length() != 0 && !hdmiColorSupportList.contains("null")) {
            for (String str : HDMI_COLOR_LIST) {
                if (hdmiColorSupportList.contains(str)) {
                    arrayList.add(str);
                    arrayList2.add(str);
                }
            }
        }
        this.mHdmiColorAttributeList = arrayList;
        this.mHdmiDeepColorAttributeList = arrayList2;
        return !this.mHdmiColorAttributeList.equals(list);
    }

    private boolean updateSupportForHdr10() {
        String readSysFs = this.mISystemControlManager.readSysFs("/sys/class/amhdmitx/amhdmitx0/hdr_cap2");
        Log.d(TAG, "updateSupportForHdr10 hdrCap2:" + readSysFs);
        boolean z = readSysFs.contains("HDR10Plus Supported: 1") || readSysFs.contains("Traditional SDR: 1") || readSysFs.contains("SMPTE ST 2084: 1") || readSysFs.contains("Hybrid Log-Gamma: 1");
        boolean z2 = this.mIsHdr10Supported != z;
        this.mIsHdr10Supported = z;
        return z2;
    }

    public List<String> getHdmiModeLists() {
        return this.mHdmiModeList;
    }

    public List<String> getHdmiTitleLists() {
        return new ArrayList(Arrays.asList(getHdmiModes()));
    }

    public String[] getHdmiModes() {
        Lock readLock = this.mRWLock.readLock();
        readLock.lock();
        String[] strArr = (String[]) this.mHdmiModeList.toArray(new String[0]);
        readLock.unlock();
        return strArr;
    }

    public boolean isCvbsMode() {
        String currentOutputMode = this.mIOutputModeManager.getCurrentOutputMode();
        Log.d(TAG, "isCvbsMode currentMode = " + currentOutputMode);
        return (currentOutputMode != null && currentOutputMode.contains("cvbs")) || Arrays.asList("pal_n", "pal_m", "ntsc_m").contains(currentOutputMode);
    }

    public String getCzechHighestMatchResolution() {
        String hdmiSupportList = this.mIOutputModeManager.getHdmiSupportList();
        Log.d(TAG, "TV support resolution value: " + hdmiSupportList);
        for (int length = CZECH_MODE_RESOLUTION_FIRST.length + (-1); length >= 0; length--) {
            String[] strArr = CZECH_MODE_RESOLUTION_FIRST;
            if (hdmiSupportList.contains(strArr[length])) {
                Log.d(TAG, "TV Support 50Hz resolution , now TV resolution: " + strArr[length]);
                return strArr[length];
            }
        }
        Log.d(TAG, "<getHighestMatchResolution> return mSystemControl");
        return this.mISystemControlManager.getPrefHdmiDispMode();
    }

    public void change2BestMode() {
        if (!"true".equals(this.mISystemControlManager.getBootenv("ubootenv.var.is.bestmode", "false"))) {
            this.mISystemControlManager.setBootenv("ubootenv.var.is.bestmode", "true");
        }
        if (("6701".equals(this.mMarketCode) && "1".equals(this.mOperatorCode)) || (("6701".equals(this.mMarketCode) && "2".equals(this.mOperatorCode)) || ("4501".equals(this.mMarketCode) && "2".equals(this.mOperatorCode)))) {
            String str = TAG;
            Log.d(str, "<change2BestMode> getCzechPrefHdmiDispMode");
            String czechHighestMatchResolution = mDisplayCapabilityManager.getCzechHighestMatchResolution();
            Log.d(str, "<change2BestMode> getCzechHighestMatchResolution systemBestOutputMode: " + czechHighestMatchResolution);
            setUserPreferredDisplayMode(czechHighestMatchResolution);
            return;
        }
        String str2 = TAG;
        Log.d(str2, "<change2BestMode> getPrefHdmiDispMode");
        String prefHdmiDispMode = this.mISystemControlManager.getPrefHdmiDispMode();
        Log.d(str2, "<change2BestMode> getPrefHdmiDispMode systemBestOutputMode: " + prefHdmiDispMode);
        setUserPreferredDisplayMode(prefHdmiDispMode);
    }

    public void setResolutionAndRefreshRateByMode(String str) {
        if (!"false".equals(this.mISystemControlManager.getBootenv("ubootenv.var.is.bestmode", "true"))) {
            this.mISystemControlManager.setBootenv("ubootenv.var.is.bestmode", "false");
        }
        setUserPreferredDisplayMode(str);
    }

    private void setUserPreferredDisplayMode(String str) {
        String str2 = TAG;
        Log.i(str2, "userSetMode: " + str);
        if (isCvbsMode()) {
            this.mISystemControlManager.setMboxOutputMode(str);
            Log.d(str2, "setMboxOutputMode");
            return;
        }
        String filterHdmiModes = filterHdmiModes(str);
        if (checkSysCurrentMode(this.mDisplayManager.getDisplay(0).getMode(), getPreferredByMode(filterHdmiModes))) {
            this.mISystemControlManager.setMboxOutputMode(str);
            Log.d(str2, "setMboxOutputMode");
            return;
        }
        String bootenv = this.mISystemControlManager.getBootenv("ubootenv.var.is.bestmode", "true");
        Display.Mode[] supportedModes = this.mDisplayManager.getDisplay(0).getSupportedModes();
        Log.d(str2, " envIsBestMode: " + bootenv);
        Log.d(str2, "supportedModes: " + Arrays.toString(supportedModes));
        Display.Mode checkUserPreferredMode = checkUserPreferredMode(supportedModes, USER_PREFERRED_MODE_BY_MODE.get(filterHdmiModes));
        Display.Mode userPreferredDisplayMode = this.mDisplayManager.getUserPreferredDisplayMode();
        Log.d(str2, "matcherMode: " + checkUserPreferredMode);
        if (userPreferredDisplayMode != null) {
            Log.w(str2, "userPreferredDisplayMode: " + userPreferredDisplayMode);
            if (checkSysCurrentMode(userPreferredDisplayMode, checkUserPreferredMode)) {
                this.mDisplayManager.clearUserPreferredDisplayMode();
            }
        }
        this.mDisplayManager.setUserPreferredDisplayMode(checkUserPreferredMode);
    }

    private Display.Mode getPreferredByMode(String str) {
        return USER_PREFERRED_MODE_BY_MODE.get(str);
    }

    private boolean checkSysCurrentMode(Display.Mode mode, Display.Mode mode2) {
        Log.d(TAG, "sysMode: " + mode + " userSetMode: " + mode2);
        return mode.matches(mode2.getPhysicalWidth(), mode2.getPhysicalHeight(), mode2.getRefreshRate());
    }

    private Display.Mode checkUserPreferredMode(Display.Mode[] modeArr, Display.Mode mode) {
        for (Display.Mode mode2 : modeArr) {
            boolean z = Float.floatToIntBits(mode2.getRefreshRate()) == Float.floatToIntBits(mode.getRefreshRate());
            if ((isCvbsMode() && z) || (!isCvbsMode() && mode2.matches(mode.getPhysicalWidth(), mode.getPhysicalHeight(), mode.getRefreshRate()))) {
                return mode2;
            }
        }
        throw new IllegalArgumentException("Unrecognized user preferred mode, invalid mode!!");
    }

    public boolean doesModeSupportColor(String str, String str2) {
        return this.mIOutputModeManager.isModeSupportColor(str, str2);
    }
}
