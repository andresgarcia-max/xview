package com.control.device.service;

import android.app.Instrumentation;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.control.device.settings.adbsettings.AdbAuthCheckUtil;
import com.control.device.util.MarketUtil;
import com.control.device.util.ProductUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.regex.Pattern;

/* loaded from: classes.dex */
public class PowerService extends Service {
    private static final String MARKET_OPERATOR;
    private static final boolean NEED_READ_EDID;
    private static String PROP_EDID_MANUFACTURER_NAME = "sys.edid.omni.manuname";
    private static String PROP_EDID_PRODUCT_CODE = "sys.edid.omni.portnumber";
    private static String PROP_EDID_PRODUCT_NAME = "sys.edid.omni.monitorname";
    private static String WRAPPEDSTRING = "Mega@DS#authentication";
    private static PowerManager.WakeLock wakeLock;
    private Context context;
    private HdmiReceiver hdmiReceiver;
    private ContentResolver mContentResolver;
    private PowerManager mPowerManager;
    private StandbyModeObserver mStandbyModeObserver;
    private TimingStandbyObserver mTimingStandbyObserver;
    private WifiManager mWifiManager;
    private final String WAKE_FULNESS_PROPERTY = "persist.sys.setting.wakefulness";
    private final String SLEEP = "sleep";
    private final String AWAKE = "awake";
    private final String ACTION_DEEP_STANDBY_NETGEM = "zte.netgem.DeepStandby";
    private String mMarketCode = SystemProperties.get("config.MarketCode");
    private String mOperatorCode = SystemProperties.get("config.OperatorCode");
    private String standbyMode = "";
    private Handler mCzechScreenModeHandler = new Handler();
    private Runnable mScreenModeRunnable = new Runnable() { // from class: com.control.device.service.PowerService.1
        @Override // java.lang.Runnable
        public void run() {
            PowerService powerService = PowerService.this;
            powerService.cfgSynScreenMode(powerService);
        }
    };
    private Handler mScreenModeHandler = new Handler();
    private Runnable mItalyDeepStandbyDelayedRunnable = new Runnable() { // from class: com.control.device.service.PowerService.2
        @Override // java.lang.Runnable
        public void run() {
            String string = Settings.Global.getString(PowerService.this.getContentResolver(), "is_screen_on");
            Log.d("[TvSettings]", "[PowerService]italy deep standby,time delay , isScrrenOn = " + string);
            if ("0".equals(string)) {
                if (MarketUtil.isFranceNetgem() || MarketUtil.isBulgaria_A1_OTT() || MarketUtil.isCroatian_A1() || MarketUtil.isElisal_OTT() || MarketUtil.isMk_A1_OTT() || MarketUtil.isFransat()) {
                    if (PowerService.this.mWifiManager != null) {
                        if (PowerService.this.mWifiManager.isWifiEnabled()) {
                            Log.d("PowerService", "[PowerService]WifiEnabled is true, set persist.sys.standby.wifi =1");
                            PowerService.this.mWifiManager.setWifiEnabled(PowerService.NEED_READ_EDID);
                            SystemProperties.set("persist.sys.standby.wifi", "1");
                        } else {
                            Log.d("PowerService", "[PowerService]WifiEnabled is false, set persist.sys.standby.wifi =2");
                            SystemProperties.set("persist.sys.standby.wifi", "2");
                        }
                    }
                    PowerService.this.releaseWakeLock();
                    Log.d("PowerService", "[PowerService]deep standby,releaseWakeLock");
                    return;
                }
                Intent intent = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
                intent.putExtra("android.intent.extra.KEY_CONFIRM", PowerService.NEED_READ_EDID);
                intent.setFlags(268435456);
                PowerService.this.startActivity(intent);
                Log.d("[TvSettings]", "[PowerService]italy deep standby,shut down");
                return;
            }
            Log.d("[TvSettings]", "[PowerService]italy deep standby,screen now is on , do nothing");
        }
    };
    private Runnable mBelguimDeepStandbyDelayedRunnable = new Runnable() { // from class: com.control.device.service.PowerService.3
        @Override // java.lang.Runnable
        public void run() {
            String string = Settings.Global.getString(PowerService.this.getContentResolver(), "is_screen_on");
            Log.d("PowerService", "Belguim deep standby,time delay , isScrrenOn = " + string);
            if ("0".equals(string)) {
                if (("8801 2".equals(PowerService.MARKET_OPERATOR) || "3901 3".equals(PowerService.MARKET_OPERATOR)) && PowerService.this.mWifiManager != null) {
                    if (PowerService.this.mWifiManager.isWifiEnabled()) {
                        Log.d("PowerService", "[PowerService]WifiEnabled is true, set persist.sys.standby.wifi =1");
                        PowerService.this.mWifiManager.setWifiEnabled(PowerService.NEED_READ_EDID);
                        SystemProperties.set("persist.sys.standby.wifi", "1");
                    } else {
                        Log.d("PowerService", "[PowerService]WifiEnabled is false, set persist.sys.standby.wifi =2");
                        SystemProperties.set("persist.sys.standby.wifi", "2");
                    }
                }
                PowerService.this.releaseWakeLock();
                Log.d("PowerService", "[PowerService]Belguim deep standby,releaseWakeLock");
                return;
            }
            Log.d("PowerService", "[PowerService]Belguim deep standby,screen now is on , do nothing");
        }
    };
    private BroadcastReceiver standbyModeReceiver = new BroadcastReceiver() { // from class: com.control.device.service.PowerService.4
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                Log.d("[TvSettings]", "[PowerService]onReceive: receive screen off");
                Settings.Global.putString(context.getContentResolver(), "is_screen_on", "0");
                int i = Settings.Global.getInt(context.getContentResolver(), "standby_time", 10800000);
                PowerService.this.standbyMode = Settings.Global.getString(context.getContentResolver(), "standbyMode");
                if (!TextUtils.isEmpty(PowerService.this.standbyMode)) {
                    if (!"1".equals(PowerService.this.standbyMode)) {
                        if ("0".equals(PowerService.this.standbyMode)) {
                            if ("8801 2".equals(PowerService.MARKET_OPERATOR) || "3901 3".equals(PowerService.MARKET_OPERATOR)) {
                                Log.d("PowerService", "[PowerService]standbyMode=0, set persist.sys.standby.wifi =0");
                                SystemProperties.set("persist.sys.standby.wifi", "0");
                                PowerService.this.mScreenModeHandler.postDelayed(PowerService.this.mBelguimDeepStandbyDelayedRunnable, i);
                            } else if (MarketUtil.isFranceNetgem() || MarketUtil.isBulgaria_A1_OTT() || MarketUtil.isCroatian_A1() || MarketUtil.isElisal_OTT() || MarketUtil.isMk_A1_OTT()) {
                                Log.d("PowerService", "[PowerService]standbyMode=0, set persist.sys.standby.wifi =0");
                                SystemProperties.set("persist.sys.standby.wifi", "0");
                                PowerService.this.mScreenModeHandler.postDelayed(PowerService.this.mItalyDeepStandbyDelayedRunnable, 10800000L);
                            }
                        }
                    } else {
                        Log.d("[TvSettings]", "[PowerService]onReceive: deep standby");
                        if (!"7001".equals(PowerService.this.mMarketCode) || !"1".equals(PowerService.this.mOperatorCode)) {
                            if (!"8801 2".equals(PowerService.MARKET_OPERATOR)) {
                                if ("3901 3".equals(PowerService.MARKET_OPERATOR)) {
                                    PowerService.this.mScreenModeHandler.postDelayed(PowerService.this.mBelguimDeepStandbyDelayedRunnable, 1000L);
                                } else if (MarketUtil.isFransat()) {
                                    PowerService.this.mScreenModeHandler.postDelayed(PowerService.this.mItalyDeepStandbyDelayedRunnable, 900000L);
                                } else if (!MarketUtil.isFranceNetgem() && !MarketUtil.isBulgaria_A1_OTT() && !MarketUtil.isCroatian_A1() && !MarketUtil.isElisal_OTT() && !MarketUtil.isMk_A1_OTT()) {
                                    PowerService.this.releaseWakeLock();
                                } else {
                                    PowerService.this.mScreenModeHandler.postDelayed(PowerService.this.mItalyDeepStandbyDelayedRunnable, 1000L);
                                }
                            } else {
                                PowerService.this.mScreenModeHandler.postDelayed(PowerService.this.mBelguimDeepStandbyDelayedRunnable, 1000L);
                            }
                        } else {
                            PowerService.this.mScreenModeHandler.postDelayed(PowerService.this.mItalyDeepStandbyDelayedRunnable, 10800000L);
                        }
                    }
                }
            }
            if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
                Log.d("[TvSettings]", "[PowerService]onReceive: receive screen on");
                Settings.Global.putString(context.getContentResolver(), "is_screen_on", "1");
                PowerService.this.acquireWakeLock();
                if (("8801 2".equals(PowerService.MARKET_OPERATOR) || "3901 3".equals(PowerService.MARKET_OPERATOR) || MarketUtil.isFranceNetgem() || MarketUtil.isBulgaria_A1_OTT() || MarketUtil.isCroatian_A1() || MarketUtil.isElisal_OTT() || MarketUtil.isMk_A1_OTT()) && PowerService.this.mWifiManager != null && "1".equals(SystemProperties.get("persist.sys.standby.wifi"))) {
                    Log.d("PowerService", "[PowerService]persist.sys.standby.wifi == 1,setWifiEnabled(true)");
                    PowerService.this.mWifiManager.setWifiEnabled(true);
                }
            }
        }
    };
    private BroadcastReceiver wakeFulnessReceiver = new BroadcastReceiver() { // from class: com.control.device.service.PowerService.5
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String str = SystemProperties.get("persist.sys.setting.wakefulness");
            Log.d("PowerService", "<wakeFulnessReceiver>onReceive: " + intent.getAction());
            if (MarketUtil.isFranceNetgem() || MarketUtil.isBulgaria_A1_OTT() || MarketUtil.isCroatian_A1() || MarketUtil.isElisal_OTT() || MarketUtil.isMk_A1_OTT()) {
                if (!PowerService.this.mPowerManager.isInteractive()) {
                    SystemProperties.set("persist.sys.setting.wakefulness", "sleep");
                    return;
                } else {
                    if ("awake".equals(str)) {
                        return;
                    }
                    SystemProperties.set("persist.sys.setting.wakefulness", "awake");
                    return;
                }
            }
            if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
                SystemProperties.set("persist.sys.setting.wakefulness", "awake");
                SystemProperties.set("config.wakefulness", "0");
            } else if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                SystemProperties.set("persist.sys.setting.wakefulness", "sleep");
            }
        }
    };
    private BroadcastReceiver deepStandbyReceiver = new BroadcastReceiver() { // from class: com.control.device.service.PowerService.6
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Log.d("PowerService", "<deepStandbyReceiver>onReceive: " + intent.getAction());
            if ("zte.netgem.DeepStandby".equals(intent.getAction())) {
                Log.d("[TvSettings]", "[PowerService]onReceive: receive deep standby");
                if (MarketUtil.isFranceNetgem() || MarketUtil.isBulgaria_A1_OTT() || MarketUtil.isCroatian_A1() || MarketUtil.isElisal_OTT() || MarketUtil.isMk_A1_OTT()) {
                    PowerService.this.releaseWakeLock();
                }
            }
        }
    };
    private BroadcastReceiver launcherLoginStatusReceiver = new BroadcastReceiver() { // from class: com.control.device.service.PowerService.8
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("action.launcher.login.authentication.complete".equals(intent.getAction())) {
                Log.d("PowerService", "[PowerService]on receive");
                Bundle extras = intent.getExtras();
                if (extras == null) {
                    Log.d("PowerService", "[PowerService]bundle is null!");
                    return;
                }
                String string = extras.getString("timeofday");
                String string2 = extras.getString("authinfo");
                if (string != null && string2 != null) {
                    PowerService.this.refreshLoginStatus(string, string2);
                } else {
                    Log.d("PowerService", "[PowerService]timestamp or authMessage is null!");
                }
            }
        }
    };
    private BroadcastReceiver AdbCheckReceiver = new BroadcastReceiver() { // from class: com.control.device.service.PowerService.9
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                Log.d("PowerService", "[PowerService]intent.action :" + intent.getAction());
                NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getApplicationContext().getSystemService("connectivity")).getActiveNetworkInfo();
                if (activeNetworkInfo == null || !activeNetworkInfo.isAvailable()) {
                    return;
                }
                Log.d("PowerService", "[PowerService]networkInfo:" + activeNetworkInfo + " networkInfo.isAvailable():" + activeNetworkInfo.isAvailable());
                PowerService.this.mHandler.sendEmptyMessageDelayed(1, 30000L);
            }
        }
    };
    private Handler mHandler = new Handler(Looper.getMainLooper()) { // from class: com.control.device.service.PowerService.10
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what == 1) {
                Log.d("PowerService", "MSG_MEGA_ADB_CHECK");
                PowerService.this.checkAdbForMega();
                PowerService.this.mHandler.removeMessages(1);
                PowerService.this.mHandler.sendEmptyMessageDelayed(1, 900000L);
                return;
            }
            Log.d("PowerService", "unknown msg, msg.what=" + message.what);
        }
    };
    private BroadcastReceiver shutdownReceiver = new BroadcastReceiver() { // from class: com.control.device.service.PowerService.12
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), "android.intent.action.SCREEN_OFF")) {
                Log.d("PowerService", " <B866V5W11> true standby.");
                PowerManager powerManager = (PowerManager) context.getSystemService("power");
                if (powerManager == null) {
                    Log.e("PowerService", " <B866V5W11> unkwon error!");
                } else {
                    powerManager.shutdown(PowerService.NEED_READ_EDID, "userrequested", PowerService.NEED_READ_EDID);
                }
            }
        }
    };

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    static {
        String str = SystemProperties.get("config.MarketCode") + " " + SystemProperties.get("config.OperatorCode");
        MARKET_OPERATOR = str;
        NEED_READ_EDID = ("1101 4".equals(str) || "4301 5".equals(str)) ? true : NEED_READ_EDID;
    }

    public static class HdmiReceiver extends BroadcastReceiver {
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("PowerService", "HdmiReceiver onReceive: action --- " + action);
            if ("android.intent.action.HDMI_PLUGGED".equals(action)) {
                String str = PowerService.MARKET_OPERATOR;
                str.hashCode();
                if (!str.equals("1101 4")) {
                    String unused = PowerService.PROP_EDID_MANUFACTURER_NAME = "sys.edid.omni.manuname";
                    String unused2 = PowerService.PROP_EDID_PRODUCT_NAME = "sys.edid.omni.monitorname";
                    String unused3 = PowerService.PROP_EDID_PRODUCT_CODE = "sys.edid.omni.portnumber";
                    writeEdidInfo();
                    return;
                }
                String unused4 = PowerService.PROP_EDID_MANUFACTURER_NAME = "sys.edid.nanosic.manuname";
                String unused5 = PowerService.PROP_EDID_PRODUCT_NAME = "sys.edid.nanosic.monitorname";
                String unused6 = PowerService.PROP_EDID_PRODUCT_CODE = "sys.edid.nanosic.portnumber";
                writeEdidInfo();
            }
        }

        private void writeEdidInfo() {
            SystemProperties.set(PowerService.PROP_EDID_MANUFACTURER_NAME, "");
            SystemProperties.set(PowerService.PROP_EDID_PRODUCT_NAME, "");
            SystemProperties.set(PowerService.PROP_EDID_PRODUCT_CODE, "");
            Iterator it = PowerService.readFile("/sys/class/amhdmitx/amhdmitx0/edid").iterator();
            while (it.hasNext()) {
                String str = (String) it.next();
                if (str.startsWith("Rx Manufacturer Name")) {
                    String trim = str.split(":")[1].trim();
                    Log.d("PowerService", "Rx Manufacturer Name: --- " + trim);
                    SystemProperties.set(PowerService.PROP_EDID_MANUFACTURER_NAME, trim);
                } else if (str.startsWith("Rx Product Name")) {
                    String trim2 = str.split(":")[1].trim();
                    Log.d("PowerService", "Rx Product Name: --- " + trim2);
                    SystemProperties.set(PowerService.PROP_EDID_PRODUCT_NAME, trim2);
                } else if (str.startsWith("Source Physical Address")) {
                    String substring = str.split(":")[1].trim().substring(0, 1);
                    Log.d("PowerService", "Source Physical Address: --- " + substring);
                    SystemProperties.set(PowerService.PROP_EDID_PRODUCT_CODE, substring);
                    return;
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ArrayList<String> readFile(String str) {
        ArrayList<String> arrayList = new ArrayList<>();
        File file = new File(str);
        if (file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                while (true) {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    arrayList.add(readLine);
                }
                fileInputStream.close();
                bufferedReader.close();
            } catch (IOException e) {
                Log.d("PowerService", "readFile: " + str + " IOException error");
                e.printStackTrace();
            }
        }
        return arrayList;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void acquireWakeLock() {
        if (wakeLock == null) {
            PowerManager.WakeLock newWakeLock = ((PowerManager) getSystemService("power")).newWakeLock(1, "ZTE_STANDBY_LOCK");
            wakeLock = newWakeLock;
            newWakeLock.acquire();
            Log.d("PowerService", "acquire wakelock");
        }
    }

    @Override // android.app.Service
    public void onCreate() {
        Log.d("PowerService", "PowerService onCreate");
        super.onCreate();
        this.context = this;
        this.mWifiManager = (WifiManager) getSystemService(WifiManager.class);
        this.standbyMode = Settings.Global.getString(this.context.getContentResolver(), "standbyMode");
        Log.d("PowerService", "[PowerService]standbymode = " + this.standbyMode);
        this.mPowerManager = (PowerManager) this.context.getSystemService("power");
        if (ProductUtils.isB866V5W11() && MarketUtil.isGeneralInternational()) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            registerReceiver(this.shutdownReceiver, intentFilter);
        }
        if (MarketUtil.isMEXICO_MEGACABLE_OTT()) {
            IntentFilter intentFilter2 = new IntentFilter();
            intentFilter2.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            registerReceiver(this.AdbCheckReceiver, intentFilter2);
            if (checkValidVersion()) {
                Log.d("PowerService", "PowerSrvice, check loginstatus");
                IntentFilter intentFilter3 = new IntentFilter();
                intentFilter3.addAction("action.launcher.login.authentication.complete");
                registerReceiver(this.launcherLoginStatusReceiver, intentFilter3);
            } else {
                Log.d("PowerService", "PowerService, no need check loginstatus");
                SystemProperties.set("sys.loginstatus", "1");
            }
        }
        if (MarketUtil.isFranceNetgem() || MarketUtil.isBulgaria_A1_OTT() || MarketUtil.isCroatian_A1() || MarketUtil.isElisal_OTT() || MarketUtil.isMk_A1_OTT()) {
            if (!TextUtils.isEmpty(this.standbyMode) && "0".equals(this.standbyMode)) {
                acquireWakeLock();
                new Handler().postDelayed(new Runnable() { // from class: com.control.device.service.PowerService.7
                    @Override // java.lang.Runnable
                    public void run() {
                        Log.d("PowerService", "<onCreate> run here");
                        boolean z = !PowerService.this.mPowerManager.isInteractive();
                        Log.d("[TvSettings]", "[PowerService]deep standby,time delay , isSleep = " + z);
                        String string = Settings.Global.getString(PowerService.this.getContentResolver(), "is_screen_on");
                        Log.d("[TvSettings]", "[PowerService]deep standby,time delay , isScrrenOn = " + string);
                        if (z && "0".equals(string)) {
                            Log.d("PowerService", "[PowerService]mScreenModeHandler.postDelayed");
                            PowerService.this.mScreenModeHandler.postDelayed(PowerService.this.mItalyDeepStandbyDelayedRunnable, 10800000L);
                        }
                    }
                }, 60000L);
            }
            this.mTimingStandbyObserver = new TimingStandbyObserver(this.mScreenModeHandler, this);
            ContentResolver contentResolver = getContentResolver();
            this.mContentResolver = contentResolver;
            contentResolver.registerContentObserver(Settings.Global.getUriFor("timing_standby"), NEED_READ_EDID, this.mTimingStandbyObserver);
            this.mStandbyModeObserver = new StandbyModeObserver(this.mScreenModeHandler, this);
            this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("standbyMode"), NEED_READ_EDID, this.mStandbyModeObserver);
            IntentFilter intentFilter4 = new IntentFilter();
            intentFilter4.addAction("android.intent.action.SCREEN_OFF");
            intentFilter4.addAction("android.intent.action.SCREEN_ON");
            registerReceiver(this.standbyModeReceiver, intentFilter4);
            registerReceiver(this.wakeFulnessReceiver, intentFilter4);
            IntentFilter intentFilter5 = new IntentFilter();
            intentFilter5.addAction("zte.netgem.DeepStandby");
            registerReceiver(this.deepStandbyReceiver, intentFilter5);
        } else {
            acquireWakeLock();
        }
        if ("7001".equals(this.mMarketCode) && "1".equals(this.mOperatorCode)) {
            Log.d("[TvSettings]", "[PowerService]<onCreate> mMarketCode = " + this.mMarketCode + ",mOperatorCode = " + this.mOperatorCode);
            this.mTimingStandbyObserver = new TimingStandbyObserver(this.mScreenModeHandler, this);
            ContentResolver contentResolver2 = getContentResolver();
            this.mContentResolver = contentResolver2;
            contentResolver2.registerContentObserver(Settings.Global.getUriFor("timing_standby"), NEED_READ_EDID, this.mTimingStandbyObserver);
            IntentFilter intentFilter6 = new IntentFilter();
            intentFilter6.addAction("android.intent.action.SCREEN_OFF");
            intentFilter6.addAction("android.intent.action.SCREEN_ON");
            registerReceiver(this.standbyModeReceiver, intentFilter6);
            registerReceiver(this.wakeFulnessReceiver, intentFilter6);
            SystemProperties.set("persist.sys.setting.wakefulness", "awake");
        }
        String str = MARKET_OPERATOR;
        if ("8801 2".equals(str) || "3901 3".equals(str) || MarketUtil.isFransat()) {
            Log.d("PowerService", "[PowerService]standbymode = " + this.standbyMode);
            if (!TextUtils.isEmpty(this.standbyMode) && "0".equals(this.standbyMode)) {
                acquireWakeLock();
            }
            this.mContentResolver = getContentResolver();
            this.mStandbyModeObserver = new StandbyModeObserver(this.mScreenModeHandler, this);
            this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("standbyMode"), NEED_READ_EDID, this.mStandbyModeObserver);
            IntentFilter intentFilter7 = new IntentFilter();
            intentFilter7.addAction("android.intent.action.SCREEN_OFF");
            intentFilter7.addAction("android.intent.action.SCREEN_ON");
            registerReceiver(this.standbyModeReceiver, intentFilter7);
        }
        if (NEED_READ_EDID) {
            Log.d("PowerService", "onCreate: needReadEdid");
            this.hdmiReceiver = new HdmiReceiver();
            IntentFilter intentFilter8 = new IntentFilter();
            intentFilter8.addAction("android.intent.action.HDMI_PLUGGED");
            registerReceiver(this.hdmiReceiver, intentFilter8);
        }
        if ("6701 1".equals(str) || "6701 2".equals(str) || "4501 2".equals(str) || "7101 1".equals(str) || "1801 2".equals(str) || "1801 3".equals(str)) {
            Log.d("PowerService", "Czech_OTT mTimingStandbyObserver run");
            this.mTimingStandbyObserver = new TimingStandbyObserver(this.mCzechScreenModeHandler, this);
            ContentResolver contentResolver3 = getContentResolver();
            this.mContentResolver = contentResolver3;
            contentResolver3.registerContentObserver(Settings.Global.getUriFor("standbyMode"), NEED_READ_EDID, this.mTimingStandbyObserver);
            Log.d("PowerService", "sync Aspectratio to screen_mode");
            this.mCzechScreenModeHandler.postDelayed(this.mScreenModeRunnable, 1000L);
            if (TextUtils.isEmpty(this.standbyMode) || !"1".equals(this.standbyMode)) {
                return;
            }
            Log.d("[TvSettings]", "[PowerService]Czech_OTT releaseWakeLock");
            releaseWakeLock();
        }
    }

    private boolean checkValidVersion() {
        String str = null;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo("com.dotscreen.megacable.portal", 0);
            str = packageInfo.versionName;
            Log.d("PowerService", "[PowerService]packageInfo.versionCode=" + packageInfo.versionCode + ", packageInfo.versionName=" + str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (str == null || -1 != compareVersion(str, "0.80.1.0")) {
            return true;
        }
        Log.d("PowerService", "[PowerService]installed app version is lower ");
        return NEED_READ_EDID;
    }

    public static int compareVersion(String str, String str2) {
        String trim = Pattern.compile("[^0-9]").matcher(str).replaceAll("").trim();
        String trim2 = Pattern.compile("[^0-9]").matcher(str2).replaceAll("").trim();
        Log.d("PowerService", "[PowerService]version1 = " + trim);
        Log.d("PowerService", "[PowerService]version2 = " + trim2);
        if (trim.equals(trim2)) {
            return 0;
        }
        trim.split("\\.");
        trim2.split("\\.");
        int min = Math.min(trim.length(), trim2.length());
        int i = 0;
        int i2 = 0;
        while (true) {
            if (i >= min) {
                break;
            }
            int i3 = i + 1;
            int parseInt = Integer.parseInt(trim.substring(i, i3)) - Integer.parseInt(trim2.substring(i, i3));
            if (parseInt != 0) {
                i2 = parseInt;
                break;
            }
            i = i3;
            i2 = parseInt;
        }
        if (i2 != 0) {
            return i2 > 0 ? 1 : -1;
        }
        for (int i4 = i; i4 < trim.length(); i4++) {
            if (Integer.parseInt(trim.substring(i, i + 1)) > 0) {
                return 1;
            }
        }
        for (int i5 = i; i5 < trim2.length(); i5++) {
            if (Integer.parseInt(trim2.substring(i, i + 1)) > 0) {
                return -1;
            }
        }
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refreshLoginStatus(String str, String str2) {
        Calendar.getInstance();
        long currentTimeMillis = System.currentTimeMillis();
        String str3 = str.substring(0, 4) + "-" + str.substring(4, 6) + "-" + str.substring(6, 8) + " " + str.substring(8, 10) + ":" + str.substring(10, 12) + ":" + str.substring(12, 14);
        Log.d("PowerService", "refreshLoginStatus,utctime = " + str3);
        try {
            long epochSecond = (currentTimeMillis / 1000) - LocalDateTime.parse(str3, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toEpochSecond(ZoneOffset.UTC);
            if (0 > epochSecond || 180 < epochSecond) {
                Log.d("PowerService", "[PowerService] authinfo illegal!");
                return;
            }
            String str4 = SystemProperties.get("ro.oem.key1");
            String str5 = str4 + ":" + str + ":" + WRAPPEDSTRING;
            String lowerCase = stringToMd5(str5).toLowerCase();
            if (str2.equals(lowerCase)) {
                SystemProperties.set("sys.loginstatus", "1");
                return;
            }
            Log.d("PowerService", "[PowerService], authinfo compare fail!");
            Log.d("PowerService", "[PowerService], oemInfo= " + str4 + ",time= " + str + ",WRAPPEDSTRING= " + WRAPPEDSTRING);
            StringBuilder sb = new StringBuilder();
            sb.append("[PowerService], tempAuthInfo= ");
            sb.append(str5);
            sb.append(", deviceAuthInfo = ");
            sb.append(lowerCase);
            Log.d("PowerService", sb.toString());
            Log.d("PowerService", "[PowerService] authinfo = " + str2);
        } catch (Exception unused) {
            Log.d("PowerService", "refreshLoginStatus,Parse time failed! return!");
        }
    }

    private String stringToMd5(String str) {
        if ("".equals(str) || str == null) {
            return "";
        }
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new BigInteger(1, messageDigest.digest(bytes)).toString(16);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void releaseWakeLock() {
        PowerManager.WakeLock wakeLock2 = wakeLock;
        if (wakeLock2 == null || !wakeLock2.isHeld()) {
            return;
        }
        wakeLock.release();
        wakeLock = null;
        Log.d("[TvSettings]", "[PowerService]release wakelock");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkAdbForMega() {
        this.context = this;
        int i = Settings.Secure.getInt(getContentResolver(), "user_setup_complete", 0);
        if (i != 1) {
            Log.d("PowerService", "checkAdbForMega, isSetup=" + i + ", no need to check");
            return;
        }
        int i2 = Settings.Global.getInt(this.context.getContentResolver(), "development_settings_enabled", 0);
        int i3 = Settings.Global.getInt(this.context.getContentResolver(), "adb_enabled", 0);
        if (i2 == 0 && i3 == 0) {
            Log.d("PowerService", "checkAdbForMega, isDevelopmentSettingsEnabled=" + i2 + ", isAdbEnabled = " + i3 + ", no need to check");
            return;
        }
        new AdbAuthCheckUtil();
        long qRFirstCheckTime = AdbAuthCheckUtil.getQRFirstCheckTime();
        long currentTimeMillis = System.currentTimeMillis();
        if (qRFirstCheckTime > 0) {
            long j = currentTimeMillis - qRFirstCheckTime;
            if (j >= 0 && j <= 604800000) {
                Log.d("PowerService", "checkAdbForMega, no need to close adb");
                return;
            }
        }
        if (0 == qRFirstCheckTime) {
            Log.d("PowerService", "checkAdbForMega, no need to close adb2");
            return;
        }
        Log.d("PowerService", "checkAdbForMega, close adb ");
        Settings.Global.putInt(this.context.getContentResolver(), "development_settings_enabled", 0);
        Settings.Global.putInt(this.context.getContentResolver(), "adb_enabled", 0);
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        return super.onStartCommand(intent, i, i2);
    }

    @Override // android.app.Service
    public void onDestroy() {
        Log.d("PowerService", "onDestroy: ");
        super.onDestroy();
        if (ProductUtils.isB866V5W11() && MarketUtil.isGeneralInternational()) {
            unregisterReceiver(this.shutdownReceiver);
        }
        String str = MARKET_OPERATOR;
        if ("6701 1".equals(str) || "6701 2".equals(str) || "4501 2".equals(str) || "7101 1".equals(str) || "1801 2".equals(str) || "1801 3".equals(str)) {
            this.mCzechScreenModeHandler.removeCallbacks(this.mScreenModeRunnable);
        }
        if ("7001".equals(this.mMarketCode) && "1".equals(this.mOperatorCode)) {
            this.mScreenModeHandler.removeCallbacks(this.mItalyDeepStandbyDelayedRunnable);
            unregisterReceiver(this.standbyModeReceiver);
            unregisterReceiver(this.wakeFulnessReceiver);
        }
        if ("8801 2".equals(str) || "3901 3".equals(str)) {
            this.mScreenModeHandler.removeCallbacks(this.mBelguimDeepStandbyDelayedRunnable);
            unregisterReceiver(this.standbyModeReceiver);
        }
        if (NEED_READ_EDID) {
            unregisterReceiver(this.hdmiReceiver);
        }
        if (MarketUtil.isFranceNetgem() || MarketUtil.isBulgaria_A1_OTT() || MarketUtil.isCroatian_A1() || MarketUtil.isElisal_OTT() || MarketUtil.isMk_A1_OTT()) {
            this.mScreenModeHandler.removeCallbacks(this.mItalyDeepStandbyDelayedRunnable);
            unregisterReceiver(this.standbyModeReceiver);
            unregisterReceiver(this.wakeFulnessReceiver);
            unregisterReceiver(this.deepStandbyReceiver);
        }
        super.onDestroy();
    }

    private class TimingStandbyObserver extends ContentObserver {
        private Context mContext;

        public TimingStandbyObserver(Handler handler, Context context) {
            super(handler);
            this.mContext = context;
            Log.d("[TvSettings]", "[PowerService]TimingStandbyObserver initial");
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            super.onChange(z, uri);
            if ("6701 1".equals(PowerService.MARKET_OPERATOR) || "6701 2".equals(PowerService.MARKET_OPERATOR) || "4501 2".equals(PowerService.MARKET_OPERATOR) || "7101 1".equals(PowerService.MARKET_OPERATOR) || "1801 2".equals(PowerService.MARKET_OPERATOR) || MarketUtil.isBOHER_OTT_12() || "1801 3".equals(PowerService.MARKET_OPERATOR)) {
                String string = Settings.Global.getString(PowerService.this.context.getContentResolver(), "standbyMode");
                if (!TextUtils.isEmpty(string) && "1".equals(string)) {
                    Log.d("[TvSettings]", "[PowerService]Czech_OTT TimingStandbyObserver releaseWakeLock");
                    PowerService.this.releaseWakeLock();
                }
            }
            if (("7001".equals(PowerService.this.mMarketCode) && "1".equals(PowerService.this.mOperatorCode)) || MarketUtil.isFranceNetgem() || MarketUtil.isBulgaria_A1_OTT() || MarketUtil.isCroatian_A1() || MarketUtil.isElisal_OTT() || MarketUtil.isMk_A1_OTT()) {
                Log.d("[TvSettings]", "[PowerService]onChange: " + uri.getPath());
                String string2 = Settings.Secure.getString(this.mContext.getContentResolver(), "screensaver_enabled");
                String string3 = Settings.Global.getString(this.mContext.getContentResolver(), "stay_on_while_plugged_in");
                String string4 = Settings.Global.getString(this.mContext.getContentResolver(), "timing_standby");
                Log.d("[TvSettings]", "[PowerService]<onChange> screenSaverMode = " + string2);
                Log.d("[TvSettings]", "[PowerService]<onChange> stayWakeMode = " + string3);
                try {
                    if (TextUtils.isEmpty(string4) || !"1".equals(string4)) {
                        return;
                    }
                    if (!TextUtils.isEmpty(string3) && !"0".equals(string3)) {
                        Log.d("[TvSettings]", "[PowerService]<onChange> device need to stay awake");
                        return;
                    }
                    if ("0".equals(string2)) {
                        PowerService.this.sendKeyEvent(26);
                    } else {
                        PowerService.this.standbyTip(this.mContext);
                    }
                    Settings.Global.putString(this.mContext.getContentResolver(), "timing_standby", "0");
                } catch (Exception e) {
                    Log.d("[TvSettings]", "[PowerService]<onChange> Exception = " + e);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:30:0x00cd A[Catch: IOException -> 0x00d1, TRY_ENTER, TryCatch #4 {IOException -> 0x00d1, blocks: (B:16:0x0097, B:30:0x00cd, B:32:0x00d5), top: B:8:0x007d }] */
    /* JADX WARN: Removed duplicated region for block: B:32:0x00d5 A[Catch: IOException -> 0x00d1, TRY_LEAVE, TryCatch #4 {IOException -> 0x00d1, blocks: (B:16:0x0097, B:30:0x00cd, B:32:0x00d5), top: B:8:0x007d }] */
    /* JADX WARN: Removed duplicated region for block: B:34:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:39:0x00e8 A[Catch: IOException -> 0x00e4, TRY_LEAVE, TryCatch #3 {IOException -> 0x00e4, blocks: (B:46:0x00e0, B:39:0x00e8), top: B:45:0x00e0 }] */
    /* JADX WARN: Removed duplicated region for block: B:45:0x00e0 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Type inference failed for: r3v0, types: [java.lang.String] */
    /* JADX WARN: Type inference failed for: r3v1 */
    /* JADX WARN: Type inference failed for: r3v10 */
    /* JADX WARN: Type inference failed for: r3v2 */
    /* JADX WARN: Type inference failed for: r3v3, types: [java.io.BufferedWriter] */
    /* JADX WARN: Type inference failed for: r3v4, types: [java.io.BufferedWriter] */
    /* JADX WARN: Type inference failed for: r3v5 */
    /* JADX WARN: Type inference failed for: r3v6 */
    /* JADX WARN: Type inference failed for: r3v7 */
    /* JADX WARN: Type inference failed for: r3v8 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void cfgSynScreenMode(android.content.Context r6) {
        /*
            Method dump skipped, instructions count: 240
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.control.device.service.PowerService.cfgSynScreenMode(android.content.Context):void");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void standbyTip(Context context) {
        Log.d("[TvSettings]", "[PowerService]<standbyTip> ");
        Intent intent = new Intent();
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(268435456);
        intent.setComponent(new ComponentName("com.control.device", "com.control.device.settings.standby.StandbyTipActivity"));
        context.startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendKeyEvent(final int i) {
        Log.d("[TvSettings]", "[PowerService]<sendKeyEvent> keycode = " + i);
        new Thread() { // from class: com.control.device.service.PowerService.11
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                try {
                    new Instrumentation().sendKeyDownUpSync(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private class StandbyModeObserver extends ContentObserver {
        private Context mContext;

        public StandbyModeObserver(Handler handler, Context context) {
            super(handler);
            this.mContext = context;
            Log.d("PowerService", "[PowerService]standbyModeObserver initial");
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            super.onChange(z, uri);
            Log.d("PowerService", "[PowerService]onChange: " + uri.getPath());
            PowerService powerService = PowerService.this;
            powerService.standbyMode = Settings.Global.getString(powerService.context.getContentResolver(), "standbyMode");
            if ("1".equals(PowerService.this.standbyMode)) {
                PowerService.this.releaseWakeLock();
            } else {
                PowerService.this.acquireWakeLock();
            }
        }
    }
}
