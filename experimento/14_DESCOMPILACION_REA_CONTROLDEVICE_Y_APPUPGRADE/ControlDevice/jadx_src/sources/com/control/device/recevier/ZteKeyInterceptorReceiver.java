package com.control.device.recevier;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DreamManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import com.android.settingslib.development.DevelopmentSettingsEnabler;
import com.android.tv.settings.adv.IHdmiCecManager;
import com.android.tv.settings.adv.ISystemControlManager;
import com.android.tv.settings.adv.impl.HdmiCecManager;
import com.android.tv.settings.adv.impl.SystemControlManager;
import com.android.tv.settings.util.UxTools;
import com.control.device.R;
import com.control.device.service.BluetoothAutoPairService;
import com.control.device.service.ConnectionService;
import com.control.device.service.PowerService;
import com.control.device.settings.BtHelper.BtHelperService;
import com.control.device.settings.MexicAmco.AmxAdbCheckService;
import com.control.device.settings.MexicAmco.AmxZTERemoteActivity;
import com.control.device.settings.MexicAmco.HdcpVerifyService;
import com.control.device.settings.adbsettings.AdbAuthCheckUtil;
import com.control.device.settings.remote.ZTERemoteActivity;
import com.control.device.settings.tvoption.SoundParameterSettingManager;
import com.control.device.sys.interfcae.OutputUiManager;
import com.control.device.util.DebugUtil;
import com.control.device.util.MarketUtil;
import com.control.device.util.OpsInitUtil;
import com.control.device.util.ProductUtils;
import com.zte.android.productcfg.ZteMcspCfgManager;
import device.util.IntentUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* loaded from: classes.dex */
public class ZteKeyInterceptorReceiver extends BroadcastReceiver {
    private static String SZ_SettingAction = "android.settings.SETTINGS";
    private static String SpringFramePairMode = "SpringFramePairMode";
    private static AlertDialog btAlertDialog = null;
    private static Toast cannotFindNetlix = null;
    public static ZteMcspCfgManager mZteMcspCfgManager = null;
    private static String springFramePairing = "android.intent.action.springFramePairing";
    private static PowerManager.WakeLock wakeLock;
    private final String HE_1;
    private final String HE_2;
    private final String HW;
    private final String Izzi_Deviceinfo;
    private String SpringFrameMode;
    private int SpringFrameModeNumber;
    private String brand;

    /* renamed from: device, reason: collision with root package name */
    private String f0device;
    private String mClaroNation;
    private ContentResolver mContentResolver;
    private IHdmiCecManager mIHdmiCecManager;
    private ISystemControlManager mISystemControlManager;
    private OutputUiManager mOutputUiManager;
    private SetStandbyBox mSetStandbyBox;
    private String market;
    private String oemKey;
    private String operator;
    private static final Map<String, String> WIFI_TYPE_CODE = new HashMap<String, String>() { // from class: com.control.device.recevier.ZteKeyInterceptorReceiver.1
        {
            put("RTL8822CS", "1");
            put("MTK7668", "2");
            put("MTK7661", "3");
            put("BCM43752", "4");
            put("MTK7663", "5");
            put("UWE5621DS", "6");
            put("BCM4375", "7");
        }
    };
    private static final Map<String, String> WIFI_TYPE_CODE_4_OEM_MODEL = new HashMap<String, String>() { // from class: com.control.device.recevier.ZteKeyInterceptorReceiver.2
        {
            put("2", "7668");
            put("3", "7661");
            put("5", "7663");
        }
    };
    private static final Map<String, String> WIFI_TYPE_CODE_4_OEM_MODEL_MEGA = new HashMap<String, String>() { // from class: com.control.device.recevier.ZteKeyInterceptorReceiver.3
        {
            put("3", "7661");
            put("5", "7663");
        }
    };
    private static final String MARKET_OPERATOR = SystemProperties.get("config.MarketCode") + " " + SystemProperties.get("config.OperatorCode");
    private static final String mModel = SystemProperties.get("ro.product.model", "");
    private static final String mBuildType = SystemProperties.get("ro.build.type");
    private static String Nordnet_FIRST_TIME = "0";
    private static PowerManager.WakeLock wakeLockStandby = null;
    private final DebugUtil log = new DebugUtil("[ZteKeyInterceptorReceiver]");
    private final String Viettel_TVAPP_PKG = "com.viettel.tv360.tv";
    private final String VIETTEL_MAIN_ACTIVITY = "com.viettel.tv360.tv.screen.home.HomeActivity";
    private String mMarketCode = SystemProperties.get("config.MarketCode");
    private String mOperatorCode = SystemProperties.get("config.OperatorCode");
    private String AUDIO_DELAY = SystemProperties.get("persist.sys.audio_delay");
    private String CpuName = SystemProperties.get("ro.product.cpu.name");
    private Handler mElisaHandler = new Handler();
    private final String WAKE_FULNESS_PROPERTY = "persist.sys.setting.wakefulness";
    private final String REBOOT_MODE_PROPERTY = "ro.boot.rebootmode";
    private final String COLD_BOOT = "cold_boot";
    private final String SLEEP = "sleep";
    private final String AWAKE = "awake";

    public ZteKeyInterceptorReceiver() {
        this.mClaroNation = TextUtils.isEmpty(SystemProperties.get("ro.vendor.nrdp.oemmodel_px")) ? "Claro Peru_Claro Peru ZTE ATV Model" : SystemProperties.get("ro.vendor.nrdp.oemmodel_px");
        this.oemKey = SystemProperties.get("ro.oem.key1");
        this.market = SystemProperties.get("config.MarketCode");
        this.operator = SystemProperties.get("config.OperatorCode");
        this.HW = SystemProperties.get("ro.boot.boardtype");
        this.HE_1 = SystemProperties.get("ro.product.wifi");
        this.HE_2 = SystemProperties.get("ro.vendor.product.wifi");
        this.Izzi_Deviceinfo = SystemProperties.get("config.zte.deviceinfo");
        this.brand = SystemProperties.get("ro.product.brand", "");
        this.f0device = SystemProperties.get("ro.product.device", "");
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(final Context context, Intent intent) {
        int i;
        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> --action-- = " + intent.getAction());
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            this.log.v("[Broadcast]receive android.intent.action.BOOT_COMPLETED");
            ProductUtils.initProductUtilEnv(context.getApplicationContext());
            mZteMcspCfgManager = ZteMcspCfgManager.getInstance(context.getApplicationContext());
            this.mOutputUiManager = new OutputUiManager(context.getApplicationContext());
            this.mIHdmiCecManager = new HdmiCecManager(context);
            HDMICECInitOnlyOnce(context);
            syncHDMIModeOrCVBSMode(context);
            if (!MarketUtil.isFranceNetgem()) {
                powerServiceInit(context);
            }
            AdbAuthCheck(context);
            appOpsInitPerm(context);
            this.log.d("boot action on : " + MarketUtil.MARKET_OPERATOR);
            String string = Settings.Global.getString(context.getContentResolver(), "standbyMode");
            String str = MARKET_OPERATOR;
            if ("6701 1".equals(str) || "6701 2".equals(str) || "4501 2".equals(str)) {
                if (TextUtils.isEmpty(string)) {
                    Log.d("[ZteKeyInterceptorReceiver]", "<onReceive> cetin : set default standby mode flag is 0");
                    Settings.Global.putString(context.getContentResolver(), "standbyMode", "0");
                }
                Intent intent2 = new Intent();
                intent2.setComponent(new ComponentName("com.control.device", "com.control.device.service.BluetoothInformationService"));
                context.startService(intent2);
                Log.d("[ZteKeyInterceptorReceiver]", "<onReceive> startService BluetoothInformationService.class");
            }
            if (MarketUtil.isBulgaria_A1_OTT() || MarketUtil.isCroatian_A1() || MarketUtil.isElisal_OTT() || MarketUtil.isMk_A1_OTT()) {
                if (TextUtils.isEmpty(string)) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> france: set default standby mode flag is 0");
                    Settings.Global.putString(context.getContentResolver(), "standbyMode", "0");
                }
                if (Settings.Global.getString(context.getApplicationContext().getContentResolver(), "defaultSet_screenOff_sleepTimeout") == null) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> france: set default time ms is null");
                    Settings.Global.putString(context.getApplicationContext().getContentResolver(), "defaultSet_screenOff_sleepTimeout", "1");
                    Settings.Secure.putInt(context.getApplicationContext().getContentResolver(), "sleep_timeout", 10800000);
                }
            }
            if (isCloseCecAutoPowerOffOrSetDefaultNormalDeepStandbyMarket()) {
                if (TextUtils.isEmpty(string)) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> france: set default standby mode flag is 0");
                    Settings.Global.putString(context.getContentResolver(), "standbyMode", "0");
                }
                if (Settings.Global.getString(context.getApplicationContext().getContentResolver(), "defaultSet_screenOff_sleepTimeout") == null) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> france: set default time ms is null");
                    Settings.Global.putString(context.getApplicationContext().getContentResolver(), "defaultSet_screenOff_sleepTimeout", "1");
                    Settings.Secure.putInt(context.getApplicationContext().getContentResolver(), "sleep_timeout", 10800000);
                }
            }
            if ("8801 2".equals(str) || "3901 3".equals(str)) {
                if (TextUtils.isEmpty(string)) {
                    Log.d("[ZteKeyInterceptorReceiver]", "<onReceive> cetin : set default standby mode flag  0 for Belgium_Telenet");
                    Settings.Global.putString(context.getContentResolver(), "standbyMode", "0");
                }
                if (TextUtils.isEmpty(Settings.Global.getString(context.getContentResolver(), "sleep_timeout"))) {
                    Settings.Secure.putInt(context.getApplicationContext().getContentResolver(), "sleep_timeout", -1);
                }
            }
            if (MarketUtil.isITALY_UPT24K()) {
                upt24kBootAction(context);
                setLongPressOnPowerBehavier(context);
                UxTools.detectOutDateSoftware(context);
            }
            if ("4301 4".equals(str)) {
                if (!"1".equals(this.Izzi_Deviceinfo)) {
                    izzisetdisable(context);
                }
                setAudioDelay(context);
                Settings.Secure.putInt(context.getContentResolver(), "restricted_settings", 0);
                UxTools.makeBluetoothDiscoverableByInvokeWithNoTimeout();
            }
            if ("4301 4".equals(str) || "4301 5".equals(str)) {
                defaultSetOfMegacable(context);
            }
            if (MarketUtil.isNordnet_OTT_12()) {
                PowerManager.WakeLock newWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "ZTE_STANDBY_LOCK");
                wakeLockStandby = newWakeLock;
                newWakeLock.acquire();
                Log.d("[ZteKeyInterceptorReceiver]", "<bootcomplete>  acquire standby lock");
                if (!"1".equals(Settings.Global.getString(context.getContentResolver(), Nordnet_FIRST_TIME))) {
                    Settings.Global.putString(context.getContentResolver(), "device_name", "WiBox.tv");
                    Settings.Global.putString(context.getContentResolver(), Nordnet_FIRST_TIME, "1");
                }
            }
            if ("4301 6".equals(str)) {
                startBtHelperService(context);
                defaultTimeFormatTo24(context);
                if (this.CpuName.contains("905X2")) {
                    try {
                        SoundParameterSettingManager soundParameterSettingManager = new SoundParameterSettingManager(context);
                        if (Settings.Global.getString(context.getApplicationContext().getContentResolver(), "first_default_audio_auto") == null) {
                            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]first set audio to auto");
                            soundParameterSettingManager.setDigitalAudioFormat("auto");
                            Settings.Global.putString(context.getApplicationContext().getContentResolver(), "first_default_audio_auto", "1");
                        }
                        soundParameterSettingManager.initParameterAfterBoot();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                defaultSet_MEXICO_AMCO(context);
                context.startService(new Intent(context, (Class<?>) HdcpVerifyService.class));
                context.startService(new Intent(context, (Class<?>) AmxAdbCheckService.class));
                if (Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0) != 1) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]Setup not complete, set persist.sys.need_privacy false for sei");
                    SystemProperties.set("persist.sys.need_privacy", "false");
                }
            }
            if ("6701 2".equals(MARKET_OPERATOR)) {
                try {
                    SoundParameterSettingManager soundParameterSettingManager2 = new SoundParameterSettingManager(context);
                    if (Settings.Global.getString(context.getApplicationContext().getContentResolver(), "first_default_audio_auto_Czech") == null) {
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]first set audio to auto");
                        soundParameterSettingManager2.setDigitalAudioFormat("auto");
                        Settings.Global.putString(context.getApplicationContext().getContentResolver(), "first_default_audio_auto_Czech", "1");
                    }
                    soundParameterSettingManager2.initParameterAfterBoot();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            if ("2501 2".equals(MARKET_OPERATOR) && "B866V5-W10".equals(mModel) && Settings.Global.getInt(context.getApplicationContext().getContentResolver(), "device_provisioned", 0) == 0) {
                Log.i("[ZteKeyInterceptorReceiver]", "set the default device name Claro Colombia STB");
                Settings.Global.putString(context.getApplicationContext().getContentResolver(), "device_name", "Claro Colombia STB");
            }
            if (MarketUtil.isElisal_OTT()) {
                this.mContentResolver = context.getContentResolver();
                this.mSetStandbyBox = new SetStandbyBox(this.mElisaHandler, context);
                this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("areena_not_installed"), false, this.mSetStandbyBox);
            }
            if (MarketUtil.isBOHER_OTT_12()) {
                SetStandbyModeDefaultValue(context);
            }
            if (MarketUtil.isFransat()) {
                SetStandbyModeDefaultValue(context);
            }
            boolean isB866V5W11 = ProductUtils.isB866V5W11();
            if (MarketUtil.isNETFLIX_W10() && isB866V5W11) {
                context.startService(new Intent(context, (Class<?>) ConnectionService.class));
            }
            if (isB866V5W11) {
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast] <B866V5W11>");
                final long j = ((BluetoothManager) context.getSystemService(BluetoothManager.class)).getAdapter() == null ? 0L : 60000L;
                startBtHelperService(context);
                final Runnable runnable = new Runnable() { // from class: com.control.device.recevier.ZteKeyInterceptorReceiver.4
                    @Override // java.lang.Runnable
                    public void run() {
                        ZteKeyInterceptorReceiver.this.startBluetoothPair(context);
                    }
                };
                if (isFirstBoot(context)) {
                    ContentObserver contentObserver = new ContentObserver(new Handler()) { // from class: com.control.device.recevier.ZteKeyInterceptorReceiver.5
                        @Override // android.database.ContentObserver
                        public void onChange(boolean z) {
                            super.onChange(z);
                            if (ZteKeyInterceptorReceiver.this.isFirstBoot(context)) {
                                return;
                            }
                            Log.i("[ZteKeyInterceptorReceiver]", "[Broadcast] <B866V5W11> may start bluetooth pairing after 60s.");
                            new Handler().postDelayed(runnable, j);
                            try {
                                context.getContentResolver().unregisterContentObserver(this);
                                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast] <B866V5W11> content observer completed the mission, unregistered!");
                            } catch (Exception e3) {
                                Log.e("[ZteKeyInterceptorReceiver]", "[Broadcast] <B866V5W11> e = " + e3);
                            }
                        }
                    };
                    Uri uriFor = Settings.Secure.getUriFor("tv_user_setup_complete");
                    Uri uriFor2 = Settings.Secure.getUriFor("user_setup_complete");
                    context.getContentResolver().registerContentObserver(uriFor, true, contentObserver);
                    context.getContentResolver().registerContentObserver(uriFor2, true, contentObserver);
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast] <B866V5W11> content observer registered!");
                } else {
                    Log.i("[ZteKeyInterceptorReceiver]", "[Broadcast] <B866V5W11> may start bluetooth pairing after 60s.");
                    new Handler().postDelayed(runnable, j);
                }
            }
            if (isCloseCecAutoPowerOffOrSetDefaultNormalDeepStandbyMarket() && Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0) == 0) {
                Log.i("[ZteKeyInterceptorReceiver]", " France Netgem cec one touch play unenabled!");
                this.mIHdmiCecManager.enableOneTouchPlay(false);
                this.mIHdmiCecManager.enableAutoPowerOff(false);
            }
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : activityManager.getRunningAppProcesses()) {
            if (runningAppProcessInfo.processName.equals("com.google.android.backdrop")) {
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> ScreenSaver exist");
                activityManager.forceStopPackage(runningAppProcessInfo.processName);
            }
        }
        if ("android.intent.action.GLOBAL_BUTTON".equals(intent.getAction())) {
            KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra("android.intent.extra.KEY_EVENT");
            int keyCode = keyEvent.getKeyCode();
            int action = keyEvent.getAction();
            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> GLOBAL_BUTTON, KeyCode is " + keyCode);
            if (keyCode == 176 && action == 1) {
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> KEYCODE_SETTINGS");
                if (MarketUtil.isITALY_UPT24K() && up24kSettingButtonAction(context)) {
                    return;
                }
                if (SystemProperties.get("sys.settingkey.forbid").equals("1")) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]1 == system.settingkey.forbid, Not Start Settings");
                    return;
                }
                if ("4301".equals(this.market) && "4".equals(this.operator)) {
                    sendExplicitBroadcasts(context, "tv.mirada.iris.screen.CL_SETTINGS_SCREEN_TAG");
                    return;
                }
                if ("4301".equals(this.market) && "6".equals(this.operator)) {
                    if (Settings.Secure.getInt(context.getApplicationContext().getContentResolver(), "user_setup_complete", 0) != 1) {
                        try {
                            Intent intent3 = new Intent();
                            intent3.setAction("android.settings.WIFI_SETTINGS");
                            intent3.addCategory("android.intent.category.DEFAULT");
                            intent3.addFlags(268435456);
                            intent3.setPackage("com.android.tv.settings");
                            context.startActivity(intent3);
                            return;
                        } catch (Exception e3) {
                            e3.printStackTrace();
                            return;
                        }
                    }
                    try {
                        Intent intent4 = new Intent();
                        intent4.addCategory("android.intent.category.DEFAULT");
                        intent4.addFlags(268435456);
                        intent4.setAction(SZ_SettingAction);
                        intent4.setComponent(new ComponentName("com.android.tv.settings", "com.android.tv.settings.MainSettings"));
                        context.startActivity(intent4);
                        return;
                    } catch (Exception e4) {
                        e4.printStackTrace();
                        return;
                    }
                }
                if (Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0) != 1) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> User Setup not complete, return");
                    return;
                }
                Intent intent5 = new Intent();
                intent5.addCategory("android.intent.category.DEFAULT");
                intent5.addFlags(268435456);
                intent5.setAction(SZ_SettingAction);
                intent5.setComponent(new ComponentName("com.android.tv.settings", "com.android.tv.settings.MainSettings"));
                context.startActivity(intent5);
                return;
            }
            if (keyCode == 265 && action == 1) {
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> KEYCODE_STEM_1");
                if (MarketUtil.isTurkeyTele() || MarketUtil.isITALY_UPT24K() || MarketUtil.isFransat()) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> open youtube ");
                    if (Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0) != 1) {
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> User Setup not complete, return");
                        return;
                    } else {
                        IntentUtils.safeStartActivityIntent(context, new Intent().addFlags(268435456).setPackage("com.google.android.youtube.tv").putExtra("yt_remote_button", true));
                        return;
                    }
                }
                if (("4301".equals(this.market) && "6".equals(this.operator)) || (MarketUtil.isNetflixRef_W11() && ProductUtils.isB866V2FAS() && ProductUtils.isRTL8852BS())) {
                    if (isFirstBoot(context)) {
                        return;
                    }
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> try open netflix ");
                    try {
                        Intent intent6 = new Intent();
                        intent6.addCategory("android.intent.category.DEFAULT");
                        intent6.addFlags(268435456);
                        intent6.setComponent(new ComponentName("com.netflix.ninja", "com.netflix.ninja.MainActivity"));
                        context.startActivity(intent6);
                        return;
                    } catch (Exception e5) {
                        e5.printStackTrace();
                        return;
                    }
                }
                String str2 = MARKET_OPERATOR;
                if ("1801 4".equals(str2) || "8901 1".equals(str2) || "5101 2".equals(str2)) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> try open youtube ");
                    try {
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> Bulgaria_A1_OTT receive KEYCODE_STEM_1,open youtube");
                        context.startActivity(new Intent().addFlags(268435456).setPackage("com.google.android.youtube.tv").putExtra("yt_remote_button", true));
                        return;
                    } catch (Exception e6) {
                        Log.d("[ZteKeyInterceptorReceiver]", "youtube is not open");
                        e6.printStackTrace();
                        return;
                    }
                }
                if (MarketUtil.isNETFLIX_W10() && ProductUtils.isGTV(context) && ProductUtils.isB866V5W11()) {
                    lunchNetflix(context);
                    return;
                }
                return;
            }
            if (keyCode == 172 && action == 1) {
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> KEYCODE_GUIDE");
                if ("4301".equals(this.market) && "4".equals(this.operator)) {
                    sendExplicitBroadcasts(context, "tv.mirada.iris.screen.TV_GUIDE_SCREEN_TAG");
                    return;
                }
                if (MarketUtil.isNordnet_OTT_12()) {
                    Intent intent7 = new Intent();
                    intent7.addCategory("android.intent.category.LAUNCHER");
                    intent7.addFlags(268435456);
                    intent7.setAction("android.intent.action.MAIN");
                    intent7.setComponent(new ComponentName("com.dotscreen.nordnet.tv", "com.dotscreen.nordnet.tv.ui.MainActivity"));
                    context.startActivity(intent7);
                    return;
                }
                if ("2501 2".equals(MARKET_OPERATOR)) {
                    if (Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0) != 1) {
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> User Setup not complete, return");
                        return;
                    } else {
                        Toast.makeText(context, context.getString(R.string.developing), 0).show();
                        return;
                    }
                }
                return;
            }
            if (keyCode == 267 && action == 1) {
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> KEYCODE_STEM_3");
                if (Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0) != 1) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> User Setup not complete, return");
                    return;
                }
                if ("7501 8".equals(MARKET_OPERATOR)) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> KEYCODE_KEYCODE_F2 open Unifique apk for Brazil_Un");
                    if (isPackageExist(context, "br.unifique.unifiquetv")) {
                        try {
                            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> open unifique play store for Brazil_Un");
                            Intent intent8 = new Intent();
                            intent8.addCategory("android.intent.category.DEFAULT");
                            intent8.addFlags(268435456);
                            intent8.setComponent(new ComponentName("br.unifique.unifiquetv", "eu.motv.tv.activities.MainActivity"));
                            context.startActivity(intent8);
                            return;
                        } catch (Exception e7) {
                            Log.i("[ZteKeyInterceptorReceiver]", "google play exception");
                            e7.printStackTrace();
                            return;
                        }
                    }
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast] Unifique is not installed, go to store.");
                    AlertDialog.Builder builder = new AlertDialog.Builder(context.getApplicationContext(), android.R.style.Theme.DeviceDefault.Dialog.Alert);
                    builder.setMessage(context.getString(R.string.unifique_not_installed));
                    builder.setPositiveButton(context.getString(R.string.settings_ok), new DialogInterface.OnClickListener() { // from class: com.control.device.recevier.ZteKeyInterceptorReceiver.6
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i2) {
                            try {
                                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> start google play store");
                                Intent intent9 = new Intent("android.intent.action.VIEW");
                                intent9.addFlags(268435456);
                                intent9.setData(Uri.parse("https://play.google.com/store/apps/details?id=br.unifique.unifiquetv"));
                                intent9.setPackage("com.android.vending");
                                context.startActivity(intent9);
                            } catch (ActivityNotFoundException e8) {
                                Log.e("[ZteKeyInterceptorReceiver]", "[Broadcast] <onReceive> can not start google play!");
                                Log.e("[ZteKeyInterceptorReceiver]", " e = " + e8);
                            }
                            dialogInterface.dismiss();
                            AlertDialog unused = ZteKeyInterceptorReceiver.btAlertDialog = null;
                        }
                    });
                    builder.setNegativeButton(context.getString(R.string.zteremote_dismiss), new DialogInterface.OnClickListener() { // from class: com.control.device.recevier.ZteKeyInterceptorReceiver.7
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i2) {
                            dialogInterface.dismiss();
                            AlertDialog unused = ZteKeyInterceptorReceiver.btAlertDialog = null;
                        }
                    });
                    builder.setCancelable(false);
                    AlertDialog create = builder.create();
                    btAlertDialog = create;
                    create.getWindow().setType(2003);
                    btAlertDialog.show();
                    return;
                }
                if (MarketUtil.isColombiaDialnet()) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> Colombia Dialnet receive KEYCODE_STEM_3!");
                    Intent intent9 = new Intent();
                    intent9.addCategory("android.intent.category.DEFAULT");
                    intent9.addFlags(268435456);
                    intent9.setComponent(new ComponentName("com.optivamedia.dialnet", "com.omtv.androidtv.MainActivity"));
                    context.startActivity(intent9);
                    return;
                }
                PowerManager.WakeLock newWakeLock2 = ((PowerManager) context.getSystemService("power")).newWakeLock(268435482, "zte_amazon");
                wakeLock = newWakeLock2;
                newWakeLock2.acquire();
                if (wakeLock != null) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> KEYCODE_STEM_3 newWakeLock");
                }
                lunchPrimeVideo(context);
                PowerManager.WakeLock wakeLock2 = wakeLock;
                if (wakeLock2 != null) {
                    wakeLock2.release();
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> KEYCODE_STEM_3 wakeLock release");
                    return;
                }
                return;
            }
            if (keyCode == 225 && action == 1) {
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> KEYCODE_PAIRING");
                if (btAlertDialog != null) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> KEYCODE_PAIRING, not null");
                    return;
                }
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> KEYCODE_PAIRING, btAlertDialog == null");
                AlertDialog.Builder builder2 = new AlertDialog.Builder(context.getApplicationContext(), android.R.style.Theme.DeviceDefault.Dialog.Alert);
                builder2.setMessage(getPairingDialogMessage(context));
                builder2.setPositiveButton(getPairingDialogPositiveButtonText(context), new DialogInterface.OnClickListener() { // from class: com.control.device.recevier.ZteKeyInterceptorReceiver.8
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        Intent intent10;
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onClick> KEYCODE_PAIRING, Pair now!");
                        if ("4301 6".equals(ZteKeyInterceptorReceiver.MARKET_OPERATOR)) {
                            intent10 = new Intent(context.getApplicationContext(), (Class<?>) AmxZTERemoteActivity.class);
                            intent10.addFlags(268435456);
                        } else if (ProductUtils.isB866V5W11()) {
                            intent10 = new Intent();
                            intent10.addCategory("android.intent.category.DEFAULT");
                            intent10.addFlags(268435456);
                            intent10.setAction("android.settings.ZTEREMOTE");
                            intent10.putExtra("add_a_new_remote", true);
                            intent10.setComponent(new ComponentName("com.control.device", "com.control.device.settings.remote.ZTERemoteActivity"));
                        } else {
                            intent10 = new Intent();
                            intent10.addCategory("android.intent.category.DEFAULT");
                            intent10.addFlags(268435456);
                            intent10.setAction("android.settings.ZTEREMOTE");
                            intent10.setComponent(new ComponentName("com.control.device", "com.control.device.settings.remote.ZTERemoteActivity"));
                        }
                        try {
                            context.startActivity(intent10);
                        } catch (Exception e8) {
                            e8.printStackTrace();
                        }
                        dialogInterface.dismiss();
                        AlertDialog unused = ZteKeyInterceptorReceiver.btAlertDialog = null;
                    }
                });
                builder2.setNegativeButton(getPairingDialogNegativeButtonText(context), new DialogInterface.OnClickListener() { // from class: com.control.device.recevier.ZteKeyInterceptorReceiver.9
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        dialogInterface.dismiss();
                        AlertDialog unused = ZteKeyInterceptorReceiver.btAlertDialog = null;
                    }
                });
                builder2.setCancelable(false);
                AlertDialog create2 = builder2.create();
                btAlertDialog = create2;
                create2.getWindow().setType(2003);
                new Handler().postDelayed(new Runnable() { // from class: com.control.device.recevier.ZteKeyInterceptorReceiver.10
                    @Override // java.lang.Runnable
                    public void run() {
                        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
                        boolean z = false;
                        if (defaultAdapter != null) {
                            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<run> mBtAdapter != null");
                            Set<BluetoothDevice> bondedDevices = defaultAdapter.getBondedDevices();
                            if (bondedDevices != null) {
                                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<run> bondedDevices != null");
                                for (BluetoothDevice bluetoothDevice : bondedDevices) {
                                    if (!bluetoothDevice.getName().contains("Megacable")) {
                                        if ("1301 2".equals(ZteKeyInterceptorReceiver.MARKET_OPERATOR) && bluetoothDevice.getName().contains("Viettel")) {
                                            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<run> Viettel exist!");
                                            z = bluetoothDevice.isConnected();
                                        }
                                    } else {
                                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<run> Megacable Remote exist!");
                                        z = bluetoothDevice.isConnected();
                                    }
                                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<run> afmPairingSuccess = " + z);
                                }
                            }
                        }
                        if (z) {
                            AlertDialog unused = ZteKeyInterceptorReceiver.btAlertDialog = null;
                        } else {
                            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<run> btAlertDialog");
                            ZteKeyInterceptorReceiver.btAlertDialog.show();
                        }
                    }
                }, 1500L);
                return;
            }
            if (keyCode == 135 && action == 1) {
                Log.d("[ZteKeyInterceptorReceiver]", "KEYCODE_F5 received");
                String str3 = SystemProperties.get("sys.omni.pair");
                if (TextUtils.isEmpty(str3)) {
                    return;
                }
                Log.d("[ZteKeyInterceptorReceiver]", "omniIndex = " + str3);
                if (str3.equals("1")) {
                    Log.d("[ZteKeyInterceptorReceiver]", "bluetooth auto pair for remote control ");
                    new Handler().postDelayed(new Runnable() { // from class: com.control.device.recevier.ZteKeyInterceptorReceiver.11
                        @Override // java.lang.Runnable
                        public void run() {
                            ZteKeyInterceptorReceiver.this.startBluetoothAutoPairService(context);
                        }
                    }, 5000L);
                    return;
                }
                return;
            }
            if (keyCode == 165) {
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> KEYCODE_INFO, Reset");
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> RP = " + keyEvent.getRepeatCount());
                String str4 = MARKET_OPERATOR;
                if ("3601 1".equals(str4)) {
                    if (keyEvent.getRepeatCount() == 80) {
                        try {
                            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> ZteMcspCfgManager.getInstance");
                            mZteMcspCfgManager = ZteMcspCfgManager.getInstance(context.getApplicationContext());
                            new Handler().postDelayed(new Runnable() { // from class: com.control.device.recevier.ZteKeyInterceptorReceiver.12
                                @Override // java.lang.Runnable
                                public void run() {
                                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> mZteMcspCfgManager.isProductSeparate() = " + ZteKeyInterceptorReceiver.mZteMcspCfgManager.isProductSeparate());
                                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> mZteMcspCfgManager.isProductSeparate() Ret = " + (ZteKeyInterceptorReceiver.mZteMcspCfgManager.isProductSeparate() ? ZteKeyInterceptorReceiver.mZteMcspCfgManager.ProductCfgReset() : -1));
                                    ((PowerManager) context.getSystemService("power")).reboot("recovery");
                                }
                            }, 1000L);
                            return;
                        } catch (Exception unused) {
                            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> set sys.tool.reset failed");
                            return;
                        }
                    }
                    return;
                }
                if (action == 1) {
                    if (("9999 1".equals(str4) || "5601 2".equals(str4) || "4401 1".equals(str4)) && Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0) == 1) {
                        Intent intent10 = new Intent();
                        intent10.addCategory("android.intent.category.DEFAULT");
                        intent10.addFlags(268435456);
                        intent10.setComponent(new ComponentName("com.android.tv.settings", "com.android.tv.settings.about.AboutActivity"));
                        context.startActivity(intent10);
                        return;
                    }
                    return;
                }
                return;
            }
            if (keyCode == 136 && action == 1) {
                Log.d("[ZteKeyInterceptorReceiver]", "bluetooth unpair for omni remote control ");
                unpairRemoteControl("Megacable");
                unpairRemoteControl("Megacable Remote");
                SystemProperties.set("sys.omni.pair", "1");
                SystemProperties.set("sys.uei.pair", "1");
                return;
            }
            if (keyCode == 131 && action == 1) {
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> KEYCODE_F1");
                if ("4301".equals(this.market) && "4".equals(this.operator)) {
                    sendExplicitBroadcasts(context, "tv.mirada.iris.screen.ON_DEMAND_SCREEN_TAG");
                    return;
                }
                return;
            }
            if (keyCode == 132 && action == 1) {
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> KEYCODE_F2");
                if (Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0) != 1) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> User Setup not complete, return");
                    return;
                }
                if ("4301".equals(this.market) && "4".equals(this.operator)) {
                    sendExplicitBroadcasts(context, "tv.mirada.iris.screen.WHATS_ON_SCREEN_TAG");
                    return;
                }
                if (MarketUtil.isITALY_UPT24K()) {
                    this.log.d("TSD receive key F2 & no action to execute");
                    return;
                }
                if (MarketUtil.isNetflixRef_OTT_L()) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> KEYCODE_F2 Try to Open Netflix >>> " + MarketUtil.MARKET_OPERATOR);
                    lunchNetflix(context);
                    return;
                }
                return;
            }
            if (keyCode == 133 && action == 1) {
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> KEYCODE_F3");
                if ("4301".equals(this.market) && "4".equals(this.operator)) {
                    sendExplicitBroadcasts(context, "tv.mirada.iris.screen.MOSAIC_SCREEN_TAG");
                    return;
                }
                return;
            }
            if (keyCode == 134 && action == 1) {
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> KEYCODE_F4");
                if ("4301".equals(this.market) && "4".equals(this.operator)) {
                    sendExplicitBroadcasts(context, "tv.mirada.iris.screen.LIVE_TV_SCREEN_TAG");
                }
                if (MarketUtil.isViettel_OTT_12()) {
                    String packageName = ((ActivityManager) context.getSystemService(ActivityManager.class)).getRunningTasks(1).get(0).topActivity.getPackageName();
                    if (packageName.equals("com.viettel.tv360.tv")) {
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]topPkgName is " + packageName + ",do nothing");
                        return;
                    }
                    ComponentName pkgTopActivityInTask = getPkgTopActivityInTask(context, "com.viettel.tv360.tv");
                    Intent intent11 = new Intent();
                    intent11.addCategory("android.intent.category.HOME");
                    intent11.addFlags(268435456);
                    if (pkgTopActivityInTask != null) {
                        intent11.setComponent(pkgTopActivityInTask);
                        intent11.addFlags(536870912);
                    } else {
                        intent11.setComponent(new ComponentName("com.viettel.tv360.tv", "com.viettel.tv360.tv.screen.home.HomeActivity"));
                    }
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> open 360tv app.Intent FLAG = " + intent11.getFlags());
                    context.startActivity(intent11);
                    return;
                }
                return;
            }
            if (keyCode == 179) {
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> KEYCODE_STB_POWER, Reset");
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> RP = " + keyEvent.getRepeatCount());
                if (action == 1) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> try to set sys.tool.reset-->2");
                    SystemProperties.set("sys.tool.reset", "2");
                }
                if (MarketUtil.isMEXICO_IZZI()) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> try to set sys.tool.reset-->2");
                    SystemProperties.set("sys.tool.reset", "2");
                    if (keyEvent.getRepeatCount() == 160) {
                        try {
                            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> ZteMcspCfgManager.getInstance");
                            new Handler().postDelayed(new Runnable() { // from class: com.control.device.recevier.ZteKeyInterceptorReceiver.13
                                @Override // java.lang.Runnable
                                public void run() {
                                }
                            }, 1000L);
                            return;
                        } catch (Exception unused2) {
                            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> set sys.tool.reset failed");
                            return;
                        }
                    }
                    return;
                }
                if (MarketUtil.isMEXICO_AMCO() || MarketUtil.isBOHER_OTT_12() || "5801 1".equals(MARKET_OPERATOR) || MarketUtil.isViettel_OTT_12() || MarketUtil.isFransat()) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> KEYCODE_STB_POWER, Reset, RP=" + keyEvent.getRepeatCount());
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> try to set sys.tool.reset-->2");
                    SystemProperties.set("sys.tool.reset", "2");
                    if (keyEvent.getRepeatCount() == 100) {
                        try {
                            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> ZteMcspCfgManager.getInstance in 5 seconds");
                            mZteMcspCfgManager = ZteMcspCfgManager.getInstance(context.getApplicationContext());
                            new Handler().postDelayed(new Runnable() { // from class: com.control.device.recevier.ZteKeyInterceptorReceiver.14
                                @Override // java.lang.Runnable
                                public void run() {
                                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> mZteMcspCfgManager.isProductSeparate() = " + ZteKeyInterceptorReceiver.mZteMcspCfgManager.isProductSeparate());
                                    if (ZteKeyInterceptorReceiver.mZteMcspCfgManager.isProductSeparate()) {
                                        Log.d("[ZteKeyInterceptorReceiver]", "mZteMcspCfgManager.ProductCfgReset()   Ret = " + ZteKeyInterceptorReceiver.mZteMcspCfgManager.ProductCfgReset());
                                        Log.d("[ZteKeyInterceptorReceiver]", "mZteMcspCfgManager.SaveConfigtoFlash()   Ret = " + ZteKeyInterceptorReceiver.mZteMcspCfgManager.SaveConfigtoFlash());
                                    }
                                    try {
                                        Thread.sleep(500L);
                                    } catch (InterruptedException e8) {
                                        e8.printStackTrace();
                                    }
                                    Log.d("[ZteKeyInterceptorReceiver]", "[KeyEvent.KEYCODE_STB_POWER] sleep 500ms");
                                    Intent intent12 = new Intent("android.intent.action.FACTORY_RESET");
                                    intent12.setPackage("android");
                                    intent12.setFlags(268435456);
                                    intent12.putExtra("android.intent.extra.REASON", "ResetButon");
                                    context.sendBroadcast(intent12);
                                }
                            }, 1000L);
                            return;
                        } catch (Exception unused3) {
                            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> set sys.tool.reset failed");
                            return;
                        }
                    }
                    return;
                }
                if (MarketUtil.isNordnet_OTT_12()) {
                    if (keyEvent.getRepeatCount() == 180) {
                        try {
                            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> ZteMcspCfgManager.getInstance in 5 seconds");
                            mZteMcspCfgManager = ZteMcspCfgManager.getInstance(context.getApplicationContext());
                            new Handler().postDelayed(new Runnable() { // from class: com.control.device.recevier.ZteKeyInterceptorReceiver.15
                                @Override // java.lang.Runnable
                                public void run() {
                                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> mZteMcspCfgManager.isProductSeparate() = " + ZteKeyInterceptorReceiver.mZteMcspCfgManager.isProductSeparate());
                                    if (ZteKeyInterceptorReceiver.mZteMcspCfgManager.isProductSeparate()) {
                                        Log.d("[ZteKeyInterceptorReceiver]", "mZteMcspCfgManager.ProductCfgReset()   Ret = " + ZteKeyInterceptorReceiver.mZteMcspCfgManager.ProductCfgReset());
                                        Log.d("[ZteKeyInterceptorReceiver]", "mZteMcspCfgManager.SaveConfigtoFlash()   Ret = " + ZteKeyInterceptorReceiver.mZteMcspCfgManager.SaveConfigtoFlash());
                                    }
                                    try {
                                        Thread.sleep(500L);
                                    } catch (InterruptedException e8) {
                                        e8.printStackTrace();
                                    }
                                    Log.d("[ZteKeyInterceptorReceiver]", "[KeyEvent.KEYCODE_STB_POWER] sleep 500ms");
                                    Intent intent12 = new Intent("android.intent.action.FACTORY_RESET");
                                    intent12.setPackage("android");
                                    intent12.setFlags(268435456);
                                    intent12.putExtra("android.intent.extra.REASON", "ResetButon");
                                    context.sendBroadcast(intent12);
                                }
                            }, 1000L);
                        } catch (Exception unused4) {
                            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> set sys.tool.reset failed");
                        }
                    }
                    if (action != 1 || keyEvent.getRepeatCount() >= 180) {
                        return;
                    }
                    ((PowerManager) context.getSystemService("power")).goToSleep(SystemClock.uptimeMillis());
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<stbpower> Standby!");
                    return;
                }
                if (keyEvent.getRepeatCount() == 100) {
                    doFactoryReset(context);
                    return;
                }
                return;
            }
            if (keyCode == 201 && action == 1) {
                Log.d("[ZteKeyInterceptorReceiver]", "KEYCODE_BUTTON_14 received");
                if (Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0) != 1) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> User Setup not complete, return");
                    return;
                }
                if ("2501 2".equals(MARKET_OPERATOR)) {
                    if (!checkInstallApp(context, "com.nagra.ion.claroco")) {
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]Columbia,install claro!");
                        Intent intent12 = new Intent("android.intent.action.VIEW");
                        intent12.addCategory("android.intent.category.DEFAULT");
                        intent12.addFlags(268435456);
                        intent12.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.nagra.ion.claroco"));
                        intent12.setPackage("com.android.vending");
                        context.startActivity(intent12);
                        return;
                    }
                    try {
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> receive KEYCODE_BUTTON_14,open ckaro tv");
                        Intent intent13 = new Intent();
                        intent13.addCategory("android.intent.category.DEFAULT");
                        intent13.addFlags(268435456);
                        intent13.setComponent(new ComponentName("com.nagra.ion.claroco", "com.nagra.uk.jado.MainActivity"));
                        context.startActivity(intent13);
                        return;
                    } catch (Exception e8) {
                        Log.i("[ZteKeyInterceptorReceiver]", "claro exception");
                        e8.printStackTrace();
                        return;
                    }
                }
                return;
            }
            if (keyCode == 199 && action == 1) {
                Log.d("[ZteKeyInterceptorReceiver]", "KEYCODE_BUTTON_12 received");
                String str5 = SystemProperties.get("sys.uei.pairing");
                Log.d("[ZteKeyInterceptorReceiver]", "ueiPairing = " + str5);
                if (str5.equals("1")) {
                    Log.d("[ZteKeyInterceptorReceiver]", "UEI bluetooth auto pairing is Pairing.");
                    String string2 = Settings.Global.getString(context.getContentResolver(), SpringFramePairMode);
                    this.SpringFrameMode = string2;
                    if (TextUtils.isEmpty(string2)) {
                        return;
                    }
                    int intValue = Integer.valueOf(this.SpringFrameMode).intValue();
                    this.SpringFrameModeNumber = intValue;
                    if (intValue > 0) {
                        Log.d("[ZteKeyInterceptorReceiver]", "<statusChanged> STATUS_PAIRING SpringFrameModeNumberP: " + this.SpringFrameModeNumber);
                        Settings.Global.putString(context.getContentResolver(), SpringFramePairMode, "5");
                        return;
                    }
                    Settings.Global.putString(context.getContentResolver(), SpringFramePairMode, "5");
                    Intent intent14 = new Intent();
                    intent14.setAction(springFramePairing);
                    context.sendBroadcast(intent14);
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]Pairing: 2");
                    return;
                }
                Log.d("[ZteKeyInterceptorReceiver]", "UEI bluetooth Start auto pair.");
                String str6 = SystemProperties.get("sys.uei.pair");
                Log.d("[ZteKeyInterceptorReceiver]", "ueiIndex = " + str6);
                SystemProperties.set("sys.uei.pairing", "1");
                if (str6.equals("1")) {
                    Log.d("[ZteKeyInterceptorReceiver]", "uei BUTTON_12 bluetooth auto pair for remote control ");
                    new Handler().postDelayed(new Runnable() { // from class: com.control.device.recevier.ZteKeyInterceptorReceiver.16
                        @Override // java.lang.Runnable
                        public void run() {
                            ZteKeyInterceptorReceiver.this.startBluetoothAutoPairService(context);
                        }
                    }, 5000L);
                } else {
                    Log.d("[ZteKeyInterceptorReceiver]", "uei BUTTON_12 unpair RemoteControl");
                    unpairRemoteControl("Megacable");
                    unpairRemoteControl("Megacable Remote");
                    Log.d("[ZteKeyInterceptorReceiver]", "uei BUTTON_12 bluetooth auto pair for remote control after unpair");
                    new Handler().postDelayed(new Runnable() { // from class: com.control.device.recevier.ZteKeyInterceptorReceiver.17
                        @Override // java.lang.Runnable
                        public void run() {
                            ZteKeyInterceptorReceiver.this.startBluetoothAutoPairService(context);
                        }
                    }, 5000L);
                }
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]onReceive: 0");
                Settings.Global.putString(context.getContentResolver(), SpringFramePairMode, "5");
                Intent intent15 = new Intent();
                intent15.setAction(springFramePairing);
                context.sendBroadcast(intent15);
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]onReceive: 0");
                return;
            }
            if (keyCode == 195 && action == 1) {
                Log.d("[ZteKeyInterceptorReceiver]", "KEYCODE_BUTTON_8 received ");
                if (Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0) != 1) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> User Setup not complete, return");
                    return;
                }
                if (MarketUtil.isBOHER_OTT_12()) {
                    toHome(context);
                    return;
                }
                if ("5801 1".equals(MARKET_OPERATOR)) {
                    try {
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> Turksat open Turksat");
                        Intent intent16 = new Intent();
                        intent16.addCategory("android.intent.category.DEFAULT");
                        intent16.addFlags(268435456);
                        intent16.setComponent(new ComponentName("io.vucos.android.tv", "io.vucos.android.tv.newer.Activity.SplashActivity"));
                        context.startActivity(intent16);
                        return;
                    } catch (Exception e9) {
                        Log.i("[ZteKeyInterceptorReceiver]", "Turksat exception");
                        e9.printStackTrace();
                        return;
                    }
                }
                return;
            }
            if (keyCode == 196 && action == 1) {
                Log.d("[ZteKeyInterceptorReceiver]", "KEYCODE_BUTTON_9 received ");
                if (Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0) != 1) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> User Setup not complete, return");
                    return;
                }
                if ("5801 1".equals(MARKET_OPERATOR)) {
                    try {
                        Intent intent17 = new Intent();
                        intent17.addCategory("android.intent.category.DEFAULT");
                        intent17.addFlags(268435456);
                        intent17.setComponent(new ComponentName("com.google.android.videos", "com.google.android.apps.play.movies.tv.usecase.home.TvHomeActivity"));
                        context.startActivity(intent17);
                        return;
                    } catch (Exception e10) {
                        Log.d("[ZteKeyInterceptorReceiver]", "Disney+ Exception");
                        e10.printStackTrace();
                        return;
                    }
                }
                return;
            }
            if (keyCode == 194 && action == 1) {
                Log.d("[ZteKeyInterceptorReceiver]", "KEYCODE_BUTTON_7 received ");
                if (Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0) != 1) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> User Setup not complete, return");
                    return;
                }
                if (MarketUtil.isNetflixRef_W11() && ProductUtils.isB866V2FAS() && ProductUtils.isRTL8852BS()) {
                    startAppByPkgName(context, "com.disney.disneyplus");
                    return;
                }
                if (MarketUtil.isNetflixRef_OTT_L() || MarketUtil.isNETFLIX_W10()) {
                    if (ProductUtils.isB866V5W11() && ProductUtils.isGTV(context)) {
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> GTV (9999 10) start >>> Disney+");
                        startAppByPkgName(context, "com.disney.disneyplus");
                        return;
                    } else {
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> (9999 10) open >>> google play store ");
                        lunchPlayStore(context);
                        return;
                    }
                }
                if (MarketUtil.isITALY_UPT24K() || MarketUtil.isTurkeyTele()) {
                    this.log.d("TSD receive key button 7 & not action execute");
                    Intent intent18 = new Intent();
                    intent18.addCategory("android.intent.category.DEFAULT");
                    intent18.addFlags(268435456);
                    intent18.setComponent(new ComponentName("com.disney.disneyplus", "com.bamtechmedia.dominguez.main.MainActivity"));
                    IntentUtils.safeStartActivityIntent(context, intent18);
                    return;
                }
                String str7 = MARKET_OPERATOR;
                if ("2501 2".equals(str7) || MarketUtil.isGeneralInternational()) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> open google play movies ");
                    Intent data = new Intent().setPackage("com.google.android.videos").setData(Uri.parse("http://play.google.com/movies").buildUpon().appendQueryParameter("pcampaignid", "ro.product.brand.property_remote_button").build());
                    data.addFlags(268435456);
                    context.startActivity(data);
                    return;
                }
                if ("8801 2".equals(str7)) {
                    try {
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> open google play store for Belgium_Telenet_OTT");
                        Intent intent19 = new Intent();
                        intent19.addCategory("android.intent.category.DEFAULT");
                        intent19.addFlags(268435456);
                        intent19.setComponent(new ComponentName("com.android.vending", "com.google.android.finsky.tvmainactivity.TvMainActivity"));
                        context.startActivity(intent19);
                        return;
                    } catch (Exception e11) {
                        Log.i("[ZteKeyInterceptorReceiver]", "google play exception");
                        e11.printStackTrace();
                        return;
                    }
                }
                if ("3901 3".equals(str7) || MarketUtil.isColombiaDialnet()) {
                    try {
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> open google play store for Spain_Zapi_OTT_12");
                        Intent intent20 = new Intent();
                        intent20.addCategory("android.intent.category.DEFAULT");
                        intent20.addFlags(268435456);
                        intent20.setComponent(new ComponentName("com.android.vending", "com.google.android.finsky.tvmainactivity.TvMainActivity"));
                        context.startActivity(intent20);
                        return;
                    } catch (Exception e12) {
                        Log.i("[ZteKeyInterceptorReceiver]", "google play exception");
                        e12.printStackTrace();
                        return;
                    }
                }
                if ("9999 1".equals(str7)) {
                    String str8 = this.oemKey;
                    if (str8 == null || str8.equals("")) {
                        return;
                    }
                    if (this.oemKey.equals("ATV00100023100000000000999901113") || this.oemKey.equals("ATV001000230000000000004301061F9") || this.oemKey.equals("ATV001000231000000000009999011F9")) {
                        try {
                            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> open google play store for GENERAL_INTERNATIONAL");
                            Intent intent21 = new Intent();
                            intent21.addCategory("android.intent.category.DEFAULT");
                            intent21.addFlags(268435456);
                            intent21.setComponent(new ComponentName("com.android.vending", "com.google.android.finsky.tvmainactivity.TvMainActivity"));
                            context.startActivity(intent21);
                            return;
                        } catch (Exception e13) {
                            Log.i("[ZteKeyInterceptorReceiver]", "google play exception");
                            e13.printStackTrace();
                            return;
                        }
                    }
                    return;
                }
                if ("7501 8".equals(str7)) {
                    try {
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> open google play store for Brazil_Un");
                        Intent intent22 = new Intent();
                        intent22.addCategory("android.intent.category.DEFAULT");
                        intent22.addFlags(268435456);
                        intent22.setComponent(new ComponentName("com.android.vending", "com.google.android.finsky.tvmainactivity.TvMainActivity"));
                        context.startActivity(intent22);
                        return;
                    } catch (Exception e14) {
                        Log.i("[ZteKeyInterceptorReceiver]", "google play exception");
                        e14.printStackTrace();
                        return;
                    }
                }
                return;
            }
            if (keyCode != 193) {
                i = 1;
            } else {
                if (action == 1) {
                    Log.d("[ZteKeyInterceptorReceiver]", "KEYCODE_BUTTON_6 received ");
                    String str9 = MARKET_OPERATOR;
                    if ("9999 10".equals(str9) || "1301 2".equals(str9) || MarketUtil.isNetflixRef_W11() || "2501 2".equals(str9) || "8801 2".equals(str9) || MarketUtil.isBOHER_OTT_12() || "7201 2".equals(str9) || "5801 1".equals(str9) || MarketUtil.isNetflixRef_OTT_L() || MarketUtil.isColombiaDialnet() || "3901 3".equals(str9)) {
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> open youtube ");
                        if (Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0) != 1) {
                            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> User Setup not complete, return");
                            return;
                        } else {
                            context.startActivity(new Intent().addFlags(268435456).setPackage("com.google.android.youtube.tv").putExtra("yt_remote_button", true));
                            return;
                        }
                    }
                    if ("9999 1".equals(str9)) {
                        String str10 = this.oemKey;
                        if (str10 == null || str10.equals("")) {
                            return;
                        }
                        if (this.oemKey.equals("ATV00100023100000000000999901113") || this.oemKey.equals("ATV001000230000000000004301061F9") || this.oemKey.equals("ATV001000231000000000009999011F9")) {
                            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> open youtube ");
                            if (Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0) != 1) {
                                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> User Setup not complete, return");
                                return;
                            } else {
                                context.startActivity(new Intent().addFlags(268435456).setPackage("com.google.android.youtube.tv").putExtra("yt_remote_button", true));
                                return;
                            }
                        }
                        return;
                    }
                    if ("1801 4".equals(str9)) {
                        try {
                            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> Bulgaria_A1_OTT receive KEYCODE_BUTTON_6,open A1");
                            Intent intent23 = new Intent();
                            intent23.addCategory("android.intent.category.DEFAULT");
                            intent23.addFlags(268435456);
                            intent23.setComponent(new ComponentName("xploretv.android.tv", "tv.threess.threeready.ui.generic.activity.MainActivity"));
                            context.startActivity(intent23);
                            return;
                        } catch (Exception e15) {
                            Log.d("[ZteKeyInterceptorReceiver]", "A1 is not open");
                            e15.printStackTrace();
                            return;
                        }
                    }
                    if ("7501 8".equals(str9)) {
                        if (!isFirstBoot(context)) {
                            try {
                                Log.d("[ZteKeyInterceptorReceiver]", "Youtube Brazil_Un is redy not openning");
                                context.startActivity(new Intent().addFlags(268435456).setPackage("com.google.android.youtube.tv").putExtra("yt_remote_button", true));
                                return;
                            } catch (Exception e16) {
                                Log.d("[ZteKeyInterceptorReceiver]", "Youtube is not open");
                                e16.printStackTrace();
                                return;
                            }
                        }
                        return;
                    }
                    if (MarketUtil.isCroatian_A1()) {
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> Croatian_A1_A1 receive KEYCODE_BUTTON_6,open A1");
                        Intent intent24 = new Intent();
                        intent24.addCategory("android.intent.category.DEFAULT");
                        intent24.addFlags(268435456);
                        intent24.setComponent(new ComponentName("hr.a1.android.tv.xploretv", "tv.threess.threeready.ui.generic.activity.MainActivity"));
                        safelyStartActivity(context, intent24);
                        return;
                    }
                    if (MarketUtil.isMk_A1_OTT()) {
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> Mk_A1_OTT receive KEYCODE_BUTTON_6,open A1");
                        Intent intent25 = new Intent();
                        intent25.addCategory("android.intent.category.DEFAULT");
                        intent25.addFlags(268435456);
                        intent25.setComponent(new ComponentName("mk.a1.xploretv.android.tv", "tv.threess.threeready.ui.generic.activity.MainActivity"));
                        safelyStartActivity(context, intent25);
                        return;
                    }
                    return;
                }
                i = 1;
            }
            if (keyCode == 266 && action == i) {
                Log.d("[ZteKeyInterceptorReceiver]", "KEYCODE_STEM_2 received ");
                if ("3601".equals(this.market) && "1".equals(this.operator)) {
                    if (context.getPackageManager().getLaunchIntentForPackage("com.netflix.ninja") == null) {
                        Log.d("[ZteKeyInterceptorReceiver]", "Cannot find intent for Netlix package: com.netflix.ninja");
                        Toast toast = cannotFindNetlix;
                        if (toast != null) {
                            toast.cancel();
                        }
                        Toast makeText = Toast.makeText(context, context.getString(R.string.show_netflix_is_under_testing), 0);
                        cannotFindNetlix = makeText;
                        makeText.show();
                        return;
                    }
                    try {
                        Intent intent26 = new Intent();
                        intent26.setAction("com.netflix.action.NETFLIX_KEY_START");
                        intent26.setPackage("com.netflix.ninja");
                        intent26.addFlags(268566528);
                        context.startActivity(intent26);
                        return;
                    } catch (Exception e17) {
                        Log.i("[ZteKeyInterceptorReceiver]", "netflix exception");
                        e17.printStackTrace();
                        return;
                    }
                }
                Intent intent27 = new Intent();
                intent27.setAction("android.intent.action.ASSIST");
                intent27.putExtra("search_type", 2);
                intent27.putExtra("android.intent.extra.ASSIST_INPUT_HINT_KEYBOARD", true);
                intent27.addCategory("android.intent.category.DEFAULT");
                intent27.addFlags(268435456);
                intent27.setComponent(new ComponentName("com.google.android.katniss", "com.google.android.apps.tvsearch.app.launch.trampoline.SearchActivityTrampoline"));
                context.startActivity(intent27);
                return;
            }
            if (keyCode == 119 && action == 1) {
                Log.d("[ZteKeyInterceptorReceiver]", "KeyEvent.KEYCODE_FUNCTION ------- Received");
                if ("4301".equals(this.market) && "6".equals(this.operator)) {
                    Log.d("[ZteKeyInterceptorReceiver]", "KeyEvent.KEYCODE_FUNCTION for AMX received ");
                    try {
                        ComponentName componentName = new ComponentName("com.control.device", "com.control.device.settings.MexicAmco.MenuKeyRspActivity");
                        Intent intent28 = new Intent();
                        intent28.addCategory("android.intent.category.DEFAULT");
                        intent28.addFlags(268435456);
                        intent28.setComponent(componentName);
                        context.startActivity(intent28);
                        return;
                    } catch (Exception e18) {
                        Log.i("[ZteKeyInterceptorReceiver]", "com.android.tv.settings.customization.MexicAmco.MenuKeyRspActivity exception");
                        e18.printStackTrace();
                        return;
                    }
                }
                return;
            }
            if (keyCode != 170 || action != 1) {
                if (keyCode == 200 && action == 1) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> KEYCODE_BUTTON_13");
                    if (MarketUtil.isBOHER_OTT_12()) {
                        startMojaMainActivity(context);
                        return;
                    }
                    return;
                }
                if (keyCode == 288 && action == 1 && ProductUtils.isGTV(context) && MarketUtil.isGeneralInternational() && ProductUtils.isB866V2FAS() && ProductUtils.isRTL8852BS()) {
                    try {
                        Intent intent29 = new Intent();
                        intent29.addFlags(268435456);
                        intent29.addCategory("android.intent.category.DEFAULT");
                        intent29.setComponent(new ComponentName("com.google.android.apps.tv.launcherx", "com.google.android.apps.tv.launcherx.profile.chooser.ProfileChooserActivity"));
                        context.startActivity(intent29);
                        return;
                    } catch (Exception e19) {
                        Log.e("[ZteKeyInterceptorReceiver]", "[Broadcast] e = ", e19);
                        return;
                    }
                }
                return;
            }
            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> KEYCODE_TV");
            if ("7001".equals(this.mMarketCode) && "1".equals(this.mOperatorCode)) {
                if (Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0) != 1) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> User Setup not complete, return");
                    return;
                }
                String packageName2 = ((ActivityManager) context.getSystemService(ActivityManager.class)).getRunningTasks(1).get(0).topActivity.getPackageName();
                if (packageName2.equals("com.ita.livetv.tsd") || packageName2.equals("com.tif.tvinput")) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]topPkgName is " + packageName2 + ",do nothing");
                    return;
                }
                ComponentName pkgTopActivityInTask2 = getPkgTopActivityInTask(context, "com.ita.livetv.tsd");
                Intent intent30 = new Intent();
                intent30.addCategory("android.intent.category.DEFAULT");
                intent30.addFlags(268435456);
                if (pkgTopActivityInTask2 != null) {
                    intent30.setComponent(pkgTopActivityInTask2);
                    intent30.addFlags(536870912);
                } else {
                    intent30.setComponent(new ComponentName("com.ita.livetv.tsd", "com.ita.livetv.LivePlayActivity"));
                }
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> open tv app.Intent FLAG = " + intent30.getFlags());
                context.startActivity(intent30);
                return;
            }
            String str11 = MARKET_OPERATOR;
            if ("4701 1".equals(str11)) {
                if (Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0) != 1) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> User Setup not complete, return");
                    return;
                }
                try {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> receive KEYCODE_TV,open live channels");
                    Intent intent31 = new Intent();
                    intent31.addCategory("android.intent.category.DEFAULT");
                    intent31.addFlags(268435456);
                    intent31.setAction("com.google.android.tvlauncher.LUANCHER_BUTTON");
                    intent31.setComponent(new ComponentName("com.google.android.tv", "com.android.tv.TvActivity"));
                    context.startActivity(intent31);
                    return;
                } catch (Exception e20) {
                    Log.d("[ZteKeyInterceptorReceiver]", "live channels is not installed");
                    e20.printStackTrace();
                    return;
                }
            }
            if ("7201 2".equals(str11)) {
                if (Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0) != 1) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> User Setup not complete, return");
                    return;
                }
                try {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> receive KEYCODE_TV,open mts");
                    Intent intent32 = new Intent();
                    intent32.addCategory("android.intent.category.DEFAULT");
                    intent32.addFlags(268435456);
                    intent32.setComponent(new ComponentName("com.mtssi.supernovabih", "com.mtssi.mtssistb.MainActivity"));
                    context.startActivity(intent32);
                } catch (Exception e21) {
                    Log.d("[ZteKeyInterceptorReceiver]", "mts is not installed");
                    e21.printStackTrace();
                }
            }
        }
    }

    private static void lunchPrimeVideo(Context context) {
        Intent intent = new Intent();
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(268435456);
        intent.setAction("com.amazon.amazonvideo.livingroom.AMAZON_BUTTON");
        intent.setComponent(new ComponentName("com.amazon.amazonvideo.livingroom", "com.amazon.ignition.IgnitionActivity"));
        IntentUtils.safeStartActivityIntent(context, intent);
    }

    private static void lunchPlayStore(Context context) {
        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> open google play store ");
        Intent intent = new Intent();
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(268435456);
        intent.setComponent(new ComponentName("com.android.vending", "com.google.android.finsky.tvmainactivity.TvMainActivity"));
        IntentUtils.safeStartActivityIntent(context, intent);
    }

    private static void lunchNetflix(Context context) {
        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]Try to Open Netflix >>> " + MarketUtil.MARKET_OPERATOR);
        PowerManager.WakeLock newWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(268435482, "zte_netflix");
        wakeLock = newWakeLock;
        newWakeLock.acquire();
        if (wakeLock != null) {
            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> KEYCODE_F2 newWakeLock");
        }
        boolean z = ((DisplayManager) context.getSystemService("display")).getDisplay(0).getState() == 1;
        try {
            DreamManager dreamManager = (DreamManager) context.getSystemService(DreamManager.class);
            Log.d("[ZteKeyInterceptorReceiver]", "[NTSDREAM] is dreaming: " + dreamManager.isDreaming());
            if (dreamManager.isDreaming()) {
                Log.d("[ZteKeyInterceptorReceiver]", "[NTSDREAM] stop dream");
                dreamManager.stopDream();
            }
            Intent intent = new Intent("com.netflix.action.NETFLIX_KEY_START");
            intent.setPackage("com.netflix.ninja");
            intent.putExtra("power_on", z);
            intent.addFlags(268566528);
            context.startActivity(intent);
        } catch (ActivityNotFoundException unused) {
            Log.d("[ZteKeyInterceptorReceiver]", "ActivityNotFound");
        }
        PowerManager.WakeLock wakeLock2 = wakeLock;
        if (wakeLock2 != null) {
            wakeLock2.release();
            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> KEYCODE_F2 wakeLock release");
        }
    }

    private class SetStandbyBox extends ContentObserver {
        private Context mContext;

        public SetStandbyBox(Handler handler, Context context) {
            super(handler);
            this.mContext = context;
            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]SetStandbyBox: 0");
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            super.onChange(z, uri);
            if (MarketUtil.isElisal_OTT()) {
                try {
                    if ("1".equals(Settings.Global.getString(this.mContext.getContentResolver(), "areena_not_installed"))) {
                        Toast.makeText(this.mContext, "The APK has not been installed", 1).show();
                        Settings.Global.putString(this.mContext.getContentResolver(), "areena_not_installed", "0");
                        Log.d("[ZteKeyInterceptorReceiver]", "The apk has not been installed, areena_not_installed set 0");
                    }
                } catch (Exception e) {
                    Log.d("[ZteKeyInterceptorReceiver]", "Areena apk not installed Exception = " + e);
                }
            }
        }
    }

    protected boolean checkInstallApp(Context context, String str) {
        Iterator<PackageInfo> it = context.getPackageManager().getInstalledPackages(0).iterator();
        while (it.hasNext()) {
            if (str.equals(it.next().packageName)) {
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]installed app");
                return true;
            }
        }
        return false;
    }

    public static void toHome(Context context) {
        if (Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0) != 1) {
            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> User Setup not complete, return");
            return;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addFlags(268435456);
        intent.addCategory("android.intent.category.HOME");
        context.startActivity(intent);
    }

    public void startMojaMainActivity(Context context) {
        if (Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0) != 1) {
            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> User Setup not complete, return");
            return;
        }
        Intent intent = new Intent();
        intent.addFlags(268435456);
        intent.setClassName("ba.bhtelecom.tv.services.release", "ba.bhtelecom.tv.services.MainActivity");
        context.startActivity(intent);
    }

    public void doFactoryReset(final Context context) {
        try {
            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> ZteMcspCfgManager.getInstance");
            mZteMcspCfgManager = ZteMcspCfgManager.getInstance(context.getApplicationContext());
            String str = SystemProperties.get("sys.too.powkey");
            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> powkey = " + str);
            if ("1".equals(str)) {
                return;
            }
            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> try to set sys.tool.reset-->2");
            SystemProperties.set("sys.tool.reset", "2");
            new Handler().postDelayed(new Runnable() { // from class: com.control.device.recevier.ZteKeyInterceptorReceiver.19
                @Override // java.lang.Runnable
                public void run() {
                    int i;
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> mZteMcspCfgManager.isProductSeparate() = " + ZteKeyInterceptorReceiver.mZteMcspCfgManager.isProductSeparate());
                    if (ZteKeyInterceptorReceiver.mZteMcspCfgManager.isProductSeparate()) {
                        ZteKeyInterceptorReceiver.mZteMcspCfgManager.ProductCfgReset();
                        i = ZteKeyInterceptorReceiver.mZteMcspCfgManager.SaveConfigtoFlash();
                    } else {
                        i = -1;
                    }
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> mZteMcspCfgManager.isProductSeparate()   Ret = " + i);
                    Intent intent = new Intent("android.intent.action.FACTORY_RESET");
                    intent.setPackage("android");
                    intent.setFlags(268435456);
                    intent.putExtra("android.intent.extra.REASON", "ResetButon");
                    context.sendBroadcast(intent);
                }
            }, 10L);
        } catch (Exception unused) {
            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> set sys.tool.reset failed");
        }
    }

    public void unpairRemoteControl(String str) {
        Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        Log.d("[ZteKeyInterceptorReceiver]", "bondedDevices.size = " + bondedDevices.size());
        if (bondedDevices.size() <= 0) {
            return;
        }
        for (BluetoothDevice bluetoothDevice : bondedDevices) {
            Log.d("[ZteKeyInterceptorReceiver]", "bluetoothDevice.getName() = " + bluetoothDevice.getName() + ",bluetoothName =" + str);
            if (bluetoothDevice.getName().equals(str)) {
                int bondState = bluetoothDevice.getBondState();
                Log.d("[ZteKeyInterceptorReceiver]", "getBondState = " + bondState);
                if (bondState == 11) {
                    bluetoothDevice.cancelBondProcess();
                }
                if (bondState != 10) {
                    Log.d("[ZteKeyInterceptorReceiver]", "unpairRemoteControl result = " + bluetoothDevice.removeBond());
                }
            }
        }
    }

    public void startBluetoothAutoPairService(Context context) {
        context.startService(new Intent(context, (Class<?>) BluetoothAutoPairService.class));
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:30:0x00bb A[Catch: IOException -> 0x00b7, TRY_LEAVE, TryCatch #2 {IOException -> 0x00b7, blocks: (B:37:0x00b3, B:30:0x00bb), top: B:36:0x00b3 }] */
    /* JADX WARN: Removed duplicated region for block: B:36:0x00b3 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:46:0x00ce A[Catch: IOException -> 0x00ca, TRY_LEAVE, TryCatch #1 {IOException -> 0x00ca, blocks: (B:53:0x00c6, B:46:0x00ce), top: B:52:0x00c6 }] */
    /* JADX WARN: Removed duplicated region for block: B:52:0x00c6 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Type inference failed for: r2v0, types: [java.lang.StringBuilder] */
    /* JADX WARN: Type inference failed for: r2v1 */
    /* JADX WARN: Type inference failed for: r2v3, types: [java.io.FileWriter] */
    /* JADX WARN: Type inference failed for: r2v4 */
    /* JADX WARN: Type inference failed for: r2v9 */
    /* JADX WARN: Type inference failed for: r3v0, types: [java.lang.String] */
    /* JADX WARN: Type inference failed for: r3v1 */
    /* JADX WARN: Type inference failed for: r3v2 */
    /* JADX WARN: Type inference failed for: r3v3, types: [java.io.BufferedWriter] */
    /* JADX WARN: Type inference failed for: r3v4 */
    /* JADX WARN: Type inference failed for: r3v5 */
    /* JADX WARN: Type inference failed for: r3v6, types: [java.io.BufferedWriter] */
    /* JADX WARN: Type inference failed for: r3v7 */
    /* JADX WARN: Type inference failed for: r3v8 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public boolean writeSysfs(java.lang.String r6, java.lang.String r7) {
        /*
            Method dump skipped, instructions count: 214
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.control.device.recevier.ZteKeyInterceptorReceiver.writeSysfs(java.lang.String, java.lang.String):boolean");
    }

    void sendExplicitBroadcasts(Context context, String str) {
        Intent intent = new Intent(str);
        List<ResolveInfo> queryBroadcastReceivers = context.getPackageManager().queryBroadcastReceivers(intent, 0);
        if (queryBroadcastReceivers == null || queryBroadcastReceivers.size() <= 0) {
            return;
        }
        for (ResolveInfo resolveInfo : queryBroadcastReceivers) {
            Intent intent2 = new Intent(intent);
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            intent2.setComponent(new ComponentName(activityInfo.applicationInfo.packageName, activityInfo.name));
            context.sendBroadcast(intent2);
        }
    }

    void startBtHelperService(Context context) {
        if (context == null) {
            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]startBtHelperService, context is null");
        } else {
            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]startBtHelperService, started");
            context.startService(new Intent(context, (Class<?>) BtHelperService.class));
        }
    }

    private void defaultTimeFormatTo24(Context context) {
        if (Settings.System.getString(context.getApplicationContext().getContentResolver(), "time_12_24") == null) {
            Log.i("[ZteKeyInterceptorReceiver]", "defaultTimeFormatTo24");
            Settings.System.putString(context.getApplicationContext().getContentResolver(), "time_12_24", "24");
        }
    }

    private void syncHDMIModeOrCVBSMode(Context context) {
        mZteMcspCfgManager = ZteMcspCfgManager.getInstance(context.getApplicationContext());
        new Handler().postDelayed(new Runnable() { // from class: com.control.device.recevier.ZteKeyInterceptorReceiver.20
            @Override // java.lang.Runnable
            public void run() {
                ZteKeyInterceptorReceiver.this.mOutputUiManager.getUiMode();
                String currentMode = ZteKeyInterceptorReceiver.this.mOutputUiManager.getCurrentMode();
                boolean isHdmiMode = ZteKeyInterceptorReceiver.this.mOutputUiManager.isHdmiMode();
                if (isHdmiMode) {
                    String ReadStrFromCfgmanager = ZteKeyInterceptorReceiver.mZteMcspCfgManager.ReadStrFromCfgmanager("Common", "HDMIMode", "");
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> isHdmiMode=" + isHdmiMode + ", configMode=" + ReadStrFromCfgmanager + ", curMode=" + currentMode);
                    if (!ReadStrFromCfgmanager.equals(currentMode) && ZteKeyInterceptorReceiver.this.mOutputUiManager.isHDMIPlugged()) {
                        if (ZteKeyInterceptorReceiver.this.mOutputUiManager.isBestOutputmode()) {
                            if ("6701 1".equals(ZteKeyInterceptorReceiver.MARKET_OPERATOR) || "6701 2".equals(ZteKeyInterceptorReceiver.MARKET_OPERATOR) || "4501 2".equals(ZteKeyInterceptorReceiver.MARKET_OPERATOR) || (MarketUtil.isViettel_OTT_12() && "B866V2F".equals(ZteKeyInterceptorReceiver.mBuildType))) {
                                Log.d("[ZteKeyInterceptorReceiver]", "<onReceive> Czech_OTT SetStrToCfgmanager HDMIMode: " + ReadStrFromCfgmanager);
                                ZteKeyInterceptorReceiver.mZteMcspCfgManager.SetStrToCfgmanager("Common", "HDMIMode", ReadStrFromCfgmanager);
                            } else {
                                ZteKeyInterceptorReceiver.mZteMcspCfgManager.SetStrToCfgmanager("Common", "HDMIMode", "Auto");
                                ZteKeyInterceptorReceiver.this.log.v("[Broadcast]<onReceive> HDMIMode = Auto");
                            }
                        } else if ("4301 4".equals(ZteKeyInterceptorReceiver.MARKET_OPERATOR)) {
                            ZteKeyInterceptorReceiver.mZteMcspCfgManager.SetStrToCfgmanager("Common", "HDMIMode", "Auto");
                            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> Mexico_IZZI need set Auto!");
                        } else {
                            ZteKeyInterceptorReceiver.mZteMcspCfgManager.SetStrToCfgmanager("Common", "HDMIMode", currentMode);
                            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> HDMIMode = " + currentMode);
                        }
                    }
                } else {
                    String ReadStrFromCfgmanager2 = ZteKeyInterceptorReceiver.mZteMcspCfgManager.ReadStrFromCfgmanager("Common", "CVBSMode", "");
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> isHdmiMode=" + isHdmiMode + ", configMode=" + ReadStrFromCfgmanager2 + ", curMode=" + currentMode);
                    if (!ReadStrFromCfgmanager2.equals(currentMode)) {
                        ZteKeyInterceptorReceiver.mZteMcspCfgManager.SetStrToCfgmanager("Common", "CVBSMode", currentMode);
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> CVBSMode = " + currentMode);
                    }
                }
                String ReadStrFromCfgmanager3 = ZteKeyInterceptorReceiver.mZteMcspCfgManager.ReadStrFromCfgmanager("Common", "HDMIMode", null);
                if (ReadStrFromCfgmanager3 != null && (ReadStrFromCfgmanager3.contains("cvbs") || ReadStrFromCfgmanager3.contains("dummy_l"))) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> error HDMIMode=" + ReadStrFromCfgmanager3 + ", set to 1080p50hz");
                    ZteKeyInterceptorReceiver.mZteMcspCfgManager.SetStrToCfgmanager("Common", "HDMIMode", "1080p50hz");
                }
                String ReadStrFromCfgmanager4 = ZteKeyInterceptorReceiver.mZteMcspCfgManager.ReadStrFromCfgmanager("Common", "CVBSMode", null);
                if (TextUtils.isEmpty(ReadStrFromCfgmanager4) || Arrays.asList(OutputUiManager.CVBS_MODE_VALUE_LIST).contains(ReadStrFromCfgmanager4) || Arrays.asList(OutputUiManager.CVBS_MODE_VALUE_LIST_MEXICO_AMCO).contains(ReadStrFromCfgmanager4)) {
                    return;
                }
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> error CVBSMode=" + ReadStrFromCfgmanager4 + ", set to 576cvbs");
                ZteKeyInterceptorReceiver.mZteMcspCfgManager.SetStrToCfgmanager("Common", "CVBSMode", "576cvbs");
            }
        }, 1500L);
    }

    private void defaultSetOfMegacable(Context context) {
        try {
            mZteMcspCfgManager = ZteMcspCfgManager.getInstance(context.getApplicationContext());
            new OutputUiManager(context.getApplicationContext());
            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> set /sys/class/video/mega_ratio");
            new Handler().postDelayed(new Runnable() { // from class: com.control.device.recevier.ZteKeyInterceptorReceiver.21
                @Override // java.lang.Runnable
                public void run() {
                    int ReadIntegerFromCfgmanager = ZteKeyInterceptorReceiver.mZteMcspCfgManager.ReadIntegerFromCfgmanager("Common", "Aspectratio", 2);
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> configAspectratio = " + ReadIntegerFromCfgmanager);
                    if (ReadIntegerFromCfgmanager == 0) {
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> writeSysfs /sys/class/video/mega_ratio = 0");
                        ZteKeyInterceptorReceiver.this.writeSysfs("/sys/class/video/mega_ratio", "0");
                    } else if (ReadIntegerFromCfgmanager == 1) {
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> writeSysfs /sys/class/video/mega_ratio = 1");
                        ZteKeyInterceptorReceiver.this.writeSysfs("/sys/class/video/mega_ratio", "1");
                    } else if (ReadIntegerFromCfgmanager == 2) {
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> writeSysfs /sys/class/video/mega_ratio = 2");
                        ZteKeyInterceptorReceiver.this.writeSysfs("/sys/class/video/mega_ratio", "2");
                    } else {
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> default writeSysfs /sys/class/video/mega_ratio = 2");
                        ZteKeyInterceptorReceiver.this.writeSysfs("/sys/class/video/mega_ratio", "2");
                    }
                    if (!"".equals(ZteKeyInterceptorReceiver.this.oemKey) && ZteKeyInterceptorReceiver.this.oemKey.length() < 32) {
                        StringBuilder sb = new StringBuilder(32);
                        sb.append(ZteKeyInterceptorReceiver.this.oemKey);
                        sb.append("000000000000");
                        sb.append(ZteKeyInterceptorReceiver.this.market);
                        if (!"".equals(ZteKeyInterceptorReceiver.this.operator) && ZteKeyInterceptorReceiver.this.operator != null) {
                            sb.append(String.format("%02d", Integer.valueOf(Integer.parseInt(ZteKeyInterceptorReceiver.this.operator))));
                        }
                        if (!"".equals(ZteKeyInterceptorReceiver.this.HW) && ZteKeyInterceptorReceiver.this.HW != null) {
                            sb.append(String.format("%02x", Integer.valueOf(Integer.parseInt(ZteKeyInterceptorReceiver.this.HW))));
                        }
                        if ("".equals(ZteKeyInterceptorReceiver.this.HE_1) || ZteKeyInterceptorReceiver.this.HE_1 == null) {
                            if (!"".equals(ZteKeyInterceptorReceiver.this.HE_2) && ZteKeyInterceptorReceiver.this.HE_2 != null) {
                                if (ZteKeyInterceptorReceiver.WIFI_TYPE_CODE.containsKey(ZteKeyInterceptorReceiver.this.HE_2)) {
                                    sb.append((String) ZteKeyInterceptorReceiver.WIFI_TYPE_CODE.get(ZteKeyInterceptorReceiver.this.HE_2));
                                }
                            } else {
                                sb.append("0");
                            }
                        } else if (ZteKeyInterceptorReceiver.WIFI_TYPE_CODE.containsKey(ZteKeyInterceptorReceiver.this.HE_1)) {
                            sb.append((String) ZteKeyInterceptorReceiver.WIFI_TYPE_CODE.get(ZteKeyInterceptorReceiver.this.HE_1));
                        }
                        Log.d("[ZteKeyInterceptorReceiver]", "<onReceive> Oemkey = " + sb.toString());
                        if (sb.toString().length() == 32) {
                            Log.d("[ZteKeyInterceptorReceiver]", "<onReceive> OemKey length is 32");
                            ZteKeyInterceptorReceiver.mZteMcspCfgManager.SetStrToCfgmanager("Common_ext", "OemKey", sb.toString());
                        }
                        Log.d("[ZteKeyInterceptorReceiver]", "<onReceive> Oemkey from ztecfgservice = " + sb.toString());
                    }
                    StringBuilder sb2 = new StringBuilder();
                    if (!"".equals(ZteKeyInterceptorReceiver.this.brand) && !"".equals(ZteKeyInterceptorReceiver.this.f0device)) {
                        if (!"4301 4".equals(ZteKeyInterceptorReceiver.MARKET_OPERATOR)) {
                            sb2.append(ZteKeyInterceptorReceiver.this.brand);
                        } else {
                            sb2.append("Izzi");
                        }
                        sb2.append("_");
                        ZteKeyInterceptorReceiver zteKeyInterceptorReceiver = ZteKeyInterceptorReceiver.this;
                        zteKeyInterceptorReceiver.f0device = zteKeyInterceptorReceiver.f0device.replace("_ZTE", "");
                        sb2.append(ZteKeyInterceptorReceiver.this.f0device);
                        if (ZteKeyInterceptorReceiver.this.oemKey.length() == 32) {
                            String substring = ZteKeyInterceptorReceiver.this.oemKey.substring(31);
                            if ("4301 5".equals(ZteKeyInterceptorReceiver.MARKET_OPERATOR)) {
                                if (ZteKeyInterceptorReceiver.WIFI_TYPE_CODE_4_OEM_MODEL_MEGA.containsKey(substring)) {
                                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> OemModel mega ");
                                    sb2.append("-");
                                    sb2.append((String) ZteKeyInterceptorReceiver.WIFI_TYPE_CODE_4_OEM_MODEL_MEGA.get(substring));
                                }
                            } else if (!"4301 4".equals(ZteKeyInterceptorReceiver.MARKET_OPERATOR) || !ZteKeyInterceptorReceiver.WIFI_TYPE_CODE_4_OEM_MODEL.containsKey(substring)) {
                                if (ZteKeyInterceptorReceiver.WIFI_TYPE_CODE_4_OEM_MODEL.containsKey(substring)) {
                                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> OemModel defalut: ");
                                    sb2.append("-");
                                    sb2.append((String) ZteKeyInterceptorReceiver.WIFI_TYPE_CODE_4_OEM_MODEL.get(substring));
                                }
                            } else if ("22".equals(ZteKeyInterceptorReceiver.this.HW)) {
                                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> OemModel izzi ott: ");
                                sb2.append("-OTT");
                            } else {
                                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> OemModel izzi  ");
                                sb2.append("-");
                                sb2.append((String) ZteKeyInterceptorReceiver.WIFI_TYPE_CODE_4_OEM_MODEL.get(substring));
                            }
                        }
                    }
                    if (!"4301 4".equals(ZteKeyInterceptorReceiver.MARKET_OPERATOR) || !"B866V2FI".equals(ZteKeyInterceptorReceiver.mModel)) {
                        if ("4301 4".equals(ZteKeyInterceptorReceiver.MARKET_OPERATOR)) {
                            ZteKeyInterceptorReceiver.this.mISystemControlManager = SystemControlManager.getInstance();
                            String str = SystemProperties.get("config.nrdp.oemmodel");
                            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> OemModel now is " + str);
                            if (ZteKeyInterceptorReceiver.this.oemKey.length() == 32) {
                                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> oemkey length is right ");
                                if (ZteKeyInterceptorReceiver.this.oemKey.contains("*")) {
                                    return;
                                }
                                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> OemModel izzi set to:ready ");
                                if ("".equals(str)) {
                                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> OemModel izzi set to: " + sb2.toString());
                                    SystemProperties.set("config.nrdp.oemmodel", sb2.toString());
                                    ZteKeyInterceptorReceiver.mZteMcspCfgManager.SetStrToCfgmanager("Common_ext", "OemModel", sb2.toString());
                                    return;
                                }
                                return;
                            }
                            return;
                        }
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> OemModel: " + sb2.toString());
                        ZteKeyInterceptorReceiver.mZteMcspCfgManager.SetStrToCfgmanager("Common_ext", "OemModel", sb2.toString());
                        return;
                    }
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> izzi-ott, B866V2FI, no need set OemModel");
                }
            }, 1000L);
        } catch (Exception e) {
            Log.i("[ZteKeyInterceptorReceiver]", "e=" + e);
        }
        writeSysfs("/sys/class/audiodsp/ac3_drc_control", "drcmode 3");
    }

    private void defaultSet_MEXICO_AMCO(Context context) {
        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]defaultSet_MEXICO_AMCO");
        String string = Settings.Global.getString(context.getApplicationContext().getContentResolver(), "defaultSet_screenOff_sleepTimeout_mexico_amco");
        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]defaultSet_screenOff_sleepTimeout_mexico_amco=" + string);
        if (string == null) {
            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]default set screenOff and sleepTimeout to never");
            Settings.System.putInt(context.getApplicationContext().getContentResolver(), "screen_off_timeout", Integer.MAX_VALUE);
            Settings.Secure.putInt(context.getApplicationContext().getContentResolver(), "sleep_timeout", -1);
            Settings.Global.putString(context.getApplicationContext().getContentResolver(), "defaultSet_screenOff_sleepTimeout_mexico_amco", "1");
        }
        if (!this.CpuName.contains("905X2")) {
            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]defaultSet_MEXICO_AMCO, cpu=" + this.CpuName);
            new Handler().postDelayed(new Runnable() { // from class: com.control.device.recevier.ZteKeyInterceptorReceiver.22
                @Override // java.lang.Runnable
                public void run() {
                    String str = SystemProperties.get("ro.vendor.nrdp.oemmodel_px");
                    String ReadStrFromCfgmanager = ZteKeyInterceptorReceiver.mZteMcspCfgManager.ReadStrFromCfgmanager("Common_ext", "OemModel", "");
                    if (TextUtils.isEmpty(str)) {
                        str = "Claro Peru_Claro B866V2-H";
                    }
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]Common_ext OemModel, old=" + ReadStrFromCfgmanager + ", new=" + str);
                    if (TextUtils.isEmpty(ReadStrFromCfgmanager) || !ReadStrFromCfgmanager.equals(str)) {
                        if (!TextUtils.isEmpty(str)) {
                            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]Set OemModel result =" + ZteKeyInterceptorReceiver.mZteMcspCfgManager.SetStrToCfgmanager("Common_ext", "OemModel", str));
                            ZteKeyInterceptorReceiver.mZteMcspCfgManager.SaveConfigtoFlash();
                            return;
                        }
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]OemModel is empty, nothing to do ,newOemModel=" + str);
                    }
                }
            }, 1500L);
            return;
        }
        new Handler().postDelayed(new Runnable() { // from class: com.control.device.recevier.ZteKeyInterceptorReceiver.23
            @Override // java.lang.Runnable
            public void run() {
                String str;
                String str2 = SystemProperties.get("ro.vendor.product.wifi");
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]ro.vendor.product.wifi=" + str2);
                if (TextUtils.isEmpty(str2)) {
                    str2 = SystemProperties.get("ro.vendor.wifi");
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]ro.vendor.wifi=" + str2);
                    if (TextUtils.isEmpty(str2)) {
                        String readFileContent = ZteKeyInterceptorReceiver.this.readFileContent("/cache/recovery/last_apk_cfg_file.ini.bak");
                        if (!readFileContent.contains("MTK7661")) {
                            if (readFileContent.contains("MTK7663")) {
                                str2 = "MTK7663";
                            } else if (readFileContent.contains("MTK7668")) {
                                str2 = "MTK7668";
                            } else {
                                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]No wifi type in file, set it to default:MTK7661");
                            }
                            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]contentStr wifi =" + str2);
                        }
                        str2 = "MTK7661";
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]contentStr wifi =" + str2);
                    }
                }
                String ReadStrFromCfgmanager = ZteKeyInterceptorReceiver.mZteMcspCfgManager.ReadStrFromCfgmanager("Common_ext", "OemModel", "");
                String amcoOemModel = ZteKeyInterceptorReceiver.this.getAmcoOemModel(str2);
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]Common_ext OemModel, old=" + ReadStrFromCfgmanager + ", new=" + amcoOemModel);
                if (TextUtils.isEmpty(ReadStrFromCfgmanager) || !ReadStrFromCfgmanager.equals(amcoOemModel)) {
                    if (!TextUtils.isEmpty(amcoOemModel)) {
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]Set OemModel result =" + ZteKeyInterceptorReceiver.mZteMcspCfgManager.SetStrToCfgmanager("Common_ext", "OemModel", amcoOemModel));
                        ZteKeyInterceptorReceiver.mZteMcspCfgManager.SaveConfigtoFlash();
                    } else {
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]OemModel is empty, nothing to do ,newOemModel=" + amcoOemModel);
                    }
                }
                String str3 = SystemProperties.get("ro.product.version.boardtype");
                String numToHex8 = ZteKeyInterceptorReceiver.this.numToHex8(str3);
                if (!TextUtils.isEmpty(str2) && ZteKeyInterceptorReceiver.WIFI_TYPE_CODE.containsKey(str2)) {
                    str = (String) ZteKeyInterceptorReceiver.WIFI_TYPE_CODE.get(str2);
                } else {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]set OemKey error, can not find wifi=" + str2 + " set hw to 3");
                    str = "3";
                }
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]boardtype=" + str3 + ", hw=" + numToHex8 + ", he=" + str);
                StringBuilder sb = new StringBuilder();
                sb.append("ATV00100022000000000000430106");
                sb.append(numToHex8);
                sb.append(str);
                String sb2 = sb.toString();
                String ReadStrFromCfgmanager2 = ZteKeyInterceptorReceiver.mZteMcspCfgManager.ReadStrFromCfgmanager("Common_ext", "OemKey", "");
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]Common_ext OemKey, oldOemKey=" + ReadStrFromCfgmanager2 + ", newOemKey=" + sb2);
                if (TextUtils.isEmpty(ReadStrFromCfgmanager2) || !ReadStrFromCfgmanager2.equals(sb2)) {
                    if (!TextUtils.isEmpty(sb2)) {
                        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]Set OemKey result =" + ZteKeyInterceptorReceiver.mZteMcspCfgManager.SetStrToCfgmanager("Common_ext", "OemKey", sb2));
                        ZteKeyInterceptorReceiver.mZteMcspCfgManager.SaveConfigtoFlash();
                        return;
                    }
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]OemKey is empty, nothing to do, newOemKey=" + sb2);
                }
            }
        }, 1500L);
    }

    public String numToHex8(String str) {
        try {
            int intValue = Integer.valueOf(str).intValue();
            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]numToHex8 " + intValue);
            return String.format("%02x", Integer.valueOf(intValue));
        } catch (Exception e) {
            e.printStackTrace();
            return String.format("%02x", 0);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getAmcoOemModel(String str) {
        String str2;
        String str3 = this.mClaroNation + "-7661";
        if (TextUtils.isEmpty(str)) {
            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]getAmcoOemModel value is empty");
            return str3;
        }
        str.hashCode();
        switch (str) {
            case "MTK7661":
                str2 = this.mClaroNation + "-7661";
                break;
            case "MTK7663":
                str2 = this.mClaroNation + "-7663";
                break;
            case "MTK7668":
                str2 = this.mClaroNation;
                break;
            default:
                str2 = this.mClaroNation + "-7661";
                break;
        }
        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]getAmcoOemModel, model=" + str2);
        return str2;
    }

    public String readFileContent(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        BufferedReader bufferedReader = null;
        try {
            try {
                File file = new File(str);
                if (!file.exists()) {
                    Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]File not found: " + str);
                    return "";
                }
                BufferedReader bufferedReader2 = new BufferedReader(new FileReader(file));
                while (true) {
                    try {
                        String readLine = bufferedReader2.readLine();
                        if (readLine == null) {
                            break;
                        }
                        stringBuffer.append(readLine);
                    } catch (Exception e) {
                        bufferedReader = bufferedReader2;
                        e = e;
                        e.printStackTrace();
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                        }
                        return stringBuffer.toString();
                    } catch (Throwable th) {
                        th = th;
                        bufferedReader = bufferedReader2;
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (Exception e3) {
                                e3.printStackTrace();
                            }
                        }
                        throw th;
                    }
                }
                bufferedReader2.close();
                String stringBuffer2 = stringBuffer.toString();
                try {
                    bufferedReader2.close();
                } catch (Exception e4) {
                    e4.printStackTrace();
                }
                return stringBuffer2;
            } catch (Throwable th2) {
                th = th2;
            }
        } catch (Exception e5) {
            e = e5;
        }
    }

    private void powerFailureManager(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService("power");
        String str = SystemProperties.get("ro.boot.rebootmode");
        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]bootMode = " + str);
        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]Italy TSD power failure manager");
        if ("cold_boot".equals(str)) {
            String str2 = SystemProperties.get("persist.sys.setting.wakefulness");
            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> lastWakeFulness = " + str2);
            if ("sleep".equals(str2)) {
                powerManager.goToSleep(SystemClock.uptimeMillis());
                SystemProperties.set("config.wakefulness", "1");
            } else {
                SystemProperties.set("persist.sys.setting.wakefulness", "awake");
            }
        }
    }

    private void setAudioDelay(Context context) {
        String valueOf = String.valueOf(this.AUDIO_DELAY);
        Log.d("[ZteKeyInterceptorReceiver]", "=======setAudioDelay() ==============delaytime:" + valueOf);
        writeSysfs("/sys/class/video/video_delay_val", valueOf);
    }

    private void izzisetdisable(Context context) {
        String format;
        String string = Settings.Secure.getString(context.getContentResolver(), "enabled_accessibility_services");
        Log.i("[ZteKeyInterceptorReceiver]", String.format("==accessibility is %s", string));
        if (string == null) {
            Log.i("[ZteKeyInterceptorReceiver]", "==accessibility is null");
        }
        Log.i("[ZteKeyInterceptorReceiver]", "=============enabled==1=======");
        if (string != null && string.contains("com.zte.deviceinfoservice/com.zte.deviceinfoservice.MyKeyService")) {
            Log.i("[ZteKeyInterceptorReceiver]", "com.zte.deviceinfoservice/com.zte.deviceinfoservice.MyKeyService%s is open before");
            return;
        }
        if (string == null || string.length() == 0) {
            format = String.format("%s", "com.zte.deviceinfoservice/com.zte.deviceinfoservice.MyKeyService");
            Log.i("[ZteKeyInterceptorReceiver]", "com.zte.deviceinfoservice/com.zte.deviceinfoservice.MyKeyService%s is 1=== " + format);
        } else {
            format = String.format("%s:%s", string, "com.zte.deviceinfoservice/com.zte.deviceinfoservice.MyKeyService");
            Log.i("[ZteKeyInterceptorReceiver]", "com.zte.deviceinfoservice/com.zte.deviceinfoservice.MyKeyService%s is 2=== " + format);
        }
        Settings.Secure.putString(context.getContentResolver(), "enabled_accessibility_services", format);
        Settings.Secure.putInt(context.getContentResolver(), "accessibility_enabled", 1);
    }

    private ComponentName getPkgTopActivityInTask(Context context, String str) {
        for (ActivityManager.RunningTaskInfo runningTaskInfo : ((ActivityManager) context.getSystemService(ActivityManager.class)).getRunningTasks(99)) {
            if (runningTaskInfo.topActivity.getPackageName().equals(str)) {
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]get target task topActivity = " + runningTaskInfo.topActivity);
                return runningTaskInfo.topActivity;
            }
        }
        return null;
    }

    private void appOpsInitPerm(Context context) {
        OpsInitUtil.getInstance(context).initOpsPerm();
    }

    private void HDMICECInitOnlyOnce(Context context) {
        String str = SystemProperties.get("config.HDMI_CEC", "0");
        if ("1".equals(str)) {
            Settings.Global.putInt(context.getApplicationContext().getContentResolver(), "hdmi_control_enabled", 1);
            if (!MarketUtil.isITALY_UPT24K() && !isCloseCecAutoPowerOffOrSetDefaultNormalDeepStandbyMarket()) {
                this.mIHdmiCecManager.enableOneTouchPlay(true);
                this.mIHdmiCecManager.enableAutoPowerOff(true);
            }
        } else if ("0".equals(str)) {
            Settings.Global.putInt(context.getApplicationContext().getContentResolver(), "hdmi_control_enabled", 0);
            if (!MarketUtil.isITALY_UPT24K()) {
                this.mIHdmiCecManager.enableOneTouchPlay(false);
                this.mIHdmiCecManager.enableAutoPowerOff(false);
            }
        }
        this.mIHdmiCecManager.enableAutoChangeLanguage(false);
    }

    private boolean isCloseCecAutoPowerOffOrSetDefaultNormalDeepStandbyMarket() {
        return MarketUtil.isFranceNetgem();
    }

    private void upt24kBootAction(Context context) {
        powerFailureManager(context);
        upt24kNormalInit(context);
        upt24kInitWhenUserSetupNotDone(context);
    }

    private void powerServiceInit(Context context) {
        context.startService(new Intent(context, (Class<?>) PowerService.class));
        Log.d("[ZteKeyInterceptorReceiver]", "<onReceive> startService PowerService.class");
    }

    private void upt24kNormalInit(Context context) {
        Log.d("[ZteKeyInterceptorReceiver]", "Settings.Secure.USER_SETUP_COMPLETE == user_setup_complete");
        if (Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0) != 1) {
            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]init 24-hour format");
            Settings.System.putString(context.getContentResolver(), "time_12_24", "24");
        }
        Settings.Global.putString(context.getContentResolver(), "is_screen_on", "1");
        if (TextUtils.isEmpty(Settings.Global.getString(context.getContentResolver(), "standbyMode"))) {
            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> italy : set default standby mode flag is 1");
            Settings.Global.putString(context.getContentResolver(), "standbyMode", "1");
        }
    }

    private void upt24kInitWhenUserSetupNotDone(Context context) {
        if ("done".equals(Settings.Global.getString(context.getContentResolver(), "upk_24_init_set"))) {
            return;
        }
        Settings.Global.putInt(context.getContentResolver(), "hdmi_control_volume_control_enabled", 0);
        Settings.Global.putInt(context.getContentResolver(), "encoded_surround_output", 1);
        Settings.Global.putString(context.getContentResolver(), "upk_24_init_set", "done");
    }

    private boolean up24kSettingButtonAction(Context context) {
        if (1 == Settings.Secure.getInt(context.getApplicationContext().getContentResolver(), "user_setup_complete", 0)) {
            Intent intent = new Intent();
            intent.addFlags(268435456);
            intent.setComponent(new ComponentName("com.droidlogic.tv.settings", "com.droidlogic.tv.settings.MainSettings"));
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        Log.v("[ZteKeyInterceptorReceiver]", "user setup not done yet ... skip starting av settings ");
        return false;
    }

    private String getPairingDialogMessage(Context context) {
        if ("4301 6".equals(MARKET_OPERATOR)) {
            return context.getString(R.string.zteremote_tip_MEXICO_AMCO);
        }
        if (MarketUtil.isFranceNetgem()) {
            return context.getString(R.string.zteremote_tip_fr_netgem);
        }
        return context.getString(R.string.zteremote_tip);
    }

    private String getPairingDialogPositiveButtonText(Context context) {
        if ("4301 6".equals(MARKET_OPERATOR)) {
            return context.getString(R.string.zteremote_pairnow_MEXICO_AMCO);
        }
        if (MarketUtil.isFranceNetgem()) {
            return context.getString(R.string.zteremote_pairnow_fr_netgem);
        }
        return context.getString(R.string.zteremote_pairnow);
    }

    private String getPairingDialogNegativeButtonText(Context context) {
        if ("4301 6".equals(MARKET_OPERATOR)) {
            return context.getString(R.string.zteremote_dismiss_MEXICO_AMCO);
        }
        if (MarketUtil.isFranceNetgem()) {
            return context.getString(R.string.zteremote_dismiss_fr_netgem);
        }
        return context.getString(R.string.zteremote_dismiss);
    }

    private void setLongPressOnPowerBehavier(Context context) {
        Settings.Global.putInt(context.getContentResolver(), "power_button_long_press", 0);
        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]<onReceive> italy : set LongPressOnPowerBehavier  is 0");
    }

    private void AdbAuthCheck(Context context) {
        if (MarketUtil.isIndonesia_Telcom() || MarketUtil.isViettel_OTT_12() || MarketUtil.isViettel_OTT_12() || MarketUtil.isNordnet_OTT_12()) {
            AdbAuthCheckUtil adbAuthCheckUtil = new AdbAuthCheckUtil();
            if ("user".equals(mBuildType) && adbAuthCheckUtil.isNeedAuth()) {
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]AdbAuthCheck, need to AdbAuth");
                adbAuthCheckUtil.stopAdbService(context);
                DevelopmentSettingsEnabler.setDevelopmentSettingsEnabled(context, false);
            }
        }
    }

    private void SetStandbyModeDefaultValue(Context context) {
        String string = Settings.Global.getString(context.getContentResolver(), "standbyMode");
        if ("0".equals(string) || "1".equals(string)) {
            Log.d("[ZteKeyInterceptorReceiver]", "<SetStandbyModeDefaultValue> have legal standby value : " + string);
            return;
        }
        Settings.Global.putString(context.getContentResolver(), "standbyMode", "0");
        Log.d("[ZteKeyInterceptorReceiver]", "<SetStandbyModeDefaultValue> no standby value or illegal value, set default value : " + Settings.Global.getString(context.getContentResolver(), "standbyMode"));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isFirstBoot(Context context) {
        int i = Settings.Secure.getInt(context.getContentResolver(), "tv_user_setup_complete", 0);
        int i2 = Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0);
        Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast]tvUserSetupComplete & userSetupComplete: " + i + " & " + i2);
        return (i == 1 && i2 == 1) ? false : true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startBluetoothPair(Context context) {
        String packageName = context.getPackageName();
        String canonicalName = ZTERemoteActivity.class.getCanonicalName();
        Log.i("[ZteKeyInterceptorReceiver]", "[Broadcast] Timer is running, current time is " + System.currentTimeMillis());
        if (isTopActivity(packageName, canonicalName) || isRemoteConnected(context)) {
            return;
        }
        Log.i("[ZteKeyInterceptorReceiver]", "[Broadcast] Start to pair bluetooth.");
        Intent intent = new Intent(context, (Class<?>) ZTERemoteActivity.class);
        intent.setFlags(268435456);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("[ZteKeyInterceptorReceiver]", " e = ", e);
        }
    }

    private boolean isTopActivity(String str, String str2) {
        try {
            ComponentName componentName = ((ActivityManager.RunningTaskInfo) ActivityManager.getService().getTasks(1).get(0)).topActivity;
            String packageName = componentName.getPackageName();
            String className = componentName.getClassName();
            if (TextUtils.equals(packageName, str)) {
                return TextUtils.equals(className, str2);
            }
            return false;
        } catch (Exception e) {
            Log.e("[ZteKeyInterceptorReceiver]", "[Broadcast] " + e);
            return false;
        }
    }

    private boolean isRemoteConnected(Context context) {
        BluetoothAdapter adapter = ((BluetoothManager) context.getSystemService(BluetoothManager.class)).getAdapter();
        if (adapter != null) {
            boolean z = false;
            for (BluetoothDevice bluetoothDevice : adapter.getBondedDevices()) {
                if (bluetoothDevice != null) {
                    try {
                        boolean booleanValue = ((Boolean) bluetoothDevice.getClass().getMethod("isConnected", new Class[0]).invoke(bluetoothDevice, new Object[0])).booleanValue();
                        int deviceClass = bluetoothDevice.getBluetoothClass().getDeviceClass();
                        int majorDeviceClass = bluetoothDevice.getBluetoothClass().getMajorDeviceClass();
                        z = (booleanValue && majorDeviceClass == 1280) || z;
                        String name = bluetoothDevice.getName();
                        int bondState = bluetoothDevice.getBondState();
                        Log.d("[ZteKeyInterceptorReceiver]", "<isRemoteBonded>" + ("Device: " + name + ", type: " + bluetoothDevice.getType() + ", deviceClass: " + deviceClass + ", majorClass: " + majorDeviceClass + ", mac: " + bluetoothDevice.getAddress() + ", bondState: " + bondState + ", connected: " + booleanValue + ". "));
                    } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                        Log.e("[ZteKeyInterceptorReceiver]", "<isRemoteBonded> e = ", e);
                    }
                }
            }
            return z;
        }
        Log.w("[ZteKeyInterceptorReceiver]", "<isRemoteBonded> Unable to get default adapter.");
        return false;
    }

    private void startAppByPkgName(Context context, String str) {
        if (!isPackageExist(context, str)) {
            Log.w("[ZteKeyInterceptorReceiver]", "[Broadcast] <startAppByPkgName> could not find " + str + "!");
            return;
        }
        if (isTopApp(str)) {
            Log.w("[ZteKeyInterceptorReceiver]", "[Broadcast] <startAppByPkgName> " + str + " is running!");
            return;
        }
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(str, 0);
            if (packageInfo == null) {
                Log.w("[ZteKeyInterceptorReceiver]", "[Broadcast] <startAppByPkgName> could not find package information for " + str + "!");
                return;
            }
            Intent intent = new Intent("android.intent.action.MAIN", (Uri) null);
            intent.addCategory("android.intent.category.LAUNCHER");
            intent.setPackage(packageInfo.packageName);
            ResolveInfo resolveInfo = context.getPackageManager().queryIntentActivities(intent, 0).get(0);
            if (resolveInfo != null) {
                ActivityInfo activityInfo = resolveInfo.activityInfo;
                String str2 = activityInfo.packageName;
                String str3 = activityInfo.name;
                Intent intent2 = new Intent("android.intent.action.MAIN");
                intent2.addCategory("android.intent.category.LAUNCHER");
                intent2.addFlags(268435456);
                intent2.setComponent(new ComponentName(str2, str3));
                Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast] <startAppByPkgName> start " + str);
                safelyStartActivity(context, intent2);
            }
        } catch (Exception e) {
            Log.e("[ZteKeyInterceptorReceiver]", "[Broadcast] <startAppByPkgName> e:", e);
        }
    }

    private boolean isPackageExist(Context context, String str) {
        try {
            context.getPackageManager().getPackageInfo(str, 1);
            return true;
        } catch (Exception e) {
            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast] e = " + e);
            return false;
        }
    }

    private boolean isTopApp(String str) {
        try {
            if (!((ActivityManager.RunningTaskInfo) ActivityManager.getService().getTasks(1).get(0)).topActivity.getPackageName().equals(str)) {
                return false;
            }
            Log.d("[ZteKeyInterceptorReceiver]", "[Broadcast] getPackageName = " + str);
            return true;
        } catch (Exception e) {
            Log.e("[ZteKeyInterceptorReceiver]", "[Broadcast] " + e);
            return false;
        }
    }

    private void safelyStartActivity(Context context, Intent intent) {
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("[ZteKeyInterceptorReceiver]", "[Broadcast] e = " + e);
        }
    }
}
