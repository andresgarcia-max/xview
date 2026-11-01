package tv.upgrade.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.zte.android.productcfg.ZteMcspCfgManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import org.xmlpull.v1.XmlPullParser;

/* loaded from: classes.dex */
public class AppUpgradeService extends Service {
    private static final String ACTION_DELETE = "delete";
    private static final String ACTION_FALLBACK = "fallback";
    private static final String ACTION_UPDATE = "update";
    private static final String AUTO_INSTALL_SUCCESS = "1";
    private static final boolean DEBUG = true;
    private static final String FILE = "[AppUpgradeService]";
    public static final int FLAG_MUTABLE = 33554432;
    private static final String HTTPS = "https";
    private static final String LOG_DIR = "logcat";
    private static final int MSG_CONNECT_NETWORK = 2;
    private static final int MSG_CONNECT_WIFI = 1;
    private static final int MSG_FILE_DOWNLOADED = 0;
    private static final int MSG_FILE_DOWNLOADED_SINGLE = 4;
    private static final int MSG_FILE_REDOWNLOAD = -1;
    private static final int MSG_FILE_REDOWNLOAD_SINGLE = 5;
    private static final int MSG_INSTALL_RESULT = 3;
    private static final String PACKAGENAME = "tv.upgrade.service";
    private static final String PATH_SAVE = "/data/data/tv.upgrade.service/";
    private static final String PATH_SAVE_SINGLE = "/data/data/tv.upgrade.service/cache/";
    private static final String PATH_XML_FILE_CUR = "/data/data/tv.upgrade.service/apkUpdateInfo.xml";
    private static final String PATH_XML_FILE_OLD = "/data/data/tv.upgrade.service/apkUpdateInfo_old.xml";
    private static final int PSK_MIN_LENGTH = 8;
    private static final String SETTING_DEFAULT = "settingsAutoTestValue";
    private static final String TAG = "[AppUpgradeService]";
    private static final long TIME_DELAYED = 2000;
    private static String UPDATE_URL = null;
    private static String UPDATE_URL_SINGLE = null;
    private static final String XML_FILE_NAME = "apkUpdateInfo.xml";
    private static final String XML_FILE_NAME_OLD = "apkUpdateInfo_old.xml";
    private static Context mContext = null;
    private static final String settingsDefaultValue = "0";
    private HandlerThread mHandlerThread;
    private LogcatWrite mLogcatWrite;
    private Random mRandom;
    private Handler mSubThreadHandler;
    private WifiManager mWifiManager;
    private ZteMcspCfgManager mZteMcspCfgManager;
    private PackageInstaller packageInstaller;
    private PackageInstaller.SessionParams sessionParams;
    private static String model = SystemProperties.get("ro.product.model");
    private static final char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private String valueCustomization = "";
    private String autoUpdateUrl = "";
    private volatile boolean autoTest = false;
    private WifiConfiguration mWifiConfiguration = new WifiConfiguration();
    private final long TIME_LONG_DELAYED = 90000;
    private int retryMax = MSG_FILE_REDOWNLOAD_SINGLE;
    private int retryCount = MSG_FILE_DOWNLOADED;
    private volatile int setSecureConnection = MSG_FILE_DOWNLOADED;
    private volatile int logcatCloseSignal = MSG_INSTALL_RESULT;
    private volatile int executeCloseSignal = MSG_FILE_DOWNLOADED;
    private final int SECURE_CONNECTION = MSG_CONNECT_WIFI;
    private String MarketCode_OperatorCode = SystemProperties.get("config.MarketCode") + " " + SystemProperties.get("config.OperatorCode");
    private final String ThailandAIS = "4701 2";
    private final String Indonesia_Telkom = "3601 1";
    private final String Viettel = "1301 2";
    private Runnable closeLogcatRunnable = new Runnable() { // from class: tv.upgrade.service.AppUpgradeService.1
        @Override // java.lang.Runnable
        public void run() {
            if (AppUpgradeService.this.logcatCloseSignal <= 0) {
                if (AppUpgradeService.this.mLogcatWrite != null) {
                    Log.d("[AppUpgradeService]", "close mLogcatWrite");
                    AppUpgradeService.this.mLogcatWrite.stopWatch();
                    return;
                }
                return;
            }
            AppUpgradeService.access$020(AppUpgradeService.this, AppUpgradeService.MSG_CONNECT_WIFI);
            Log.d("[AppUpgradeService]", "closeLogcatRunnable: " + AppUpgradeService.this.logcatCloseSignal);
            AppUpgradeService.this.mHandler.postDelayed(this, 120000L);
        }
    };
    protected BroadcastReceiver isConnectedReceiver = new BroadcastReceiver() { // from class: tv.upgrade.service.AppUpgradeService.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Log.d("[AppUpgradeService]", "[AppUpgradeService]<onReceive> ");
            if (AppUpgradeService.this.executeCloseSignal == 0 && AppUpgradeService.this.isNetworkConnected(context)) {
                AppUpgradeService.this.executeCloseSignal = AppUpgradeService.MSG_CONNECT_WIFI;
                AppUpgradeService.this.mHandler.sendMessageDelayed(Message.obtain(AppUpgradeService.this.mHandler, AppUpgradeService.MSG_CONNECT_NETWORK, new MessageObj("", "", "", AppUpgradeService.MSG_FILE_DOWNLOADED)), AppUpgradeService.TIME_DELAYED);
            }
        }
    };
    protected BroadcastReceiver singleInstallReceiver = new BroadcastReceiver() { // from class: tv.upgrade.service.AppUpgradeService.3
        @Override // android.content.BroadcastReceiver
        public void onReceive(final Context context, final Intent intent) {
            Log.d("[AppUpgradeService]", "[AppUpgradeService]<onReceive> singleInstallReceiver ," + intent.getAction());
            new Thread(new Runnable() { // from class: tv.upgrade.service.AppUpgradeService.3.1
                @Override // java.lang.Runnable
                public void run() {
                    String fileName = null;
                    String packageName = null;
                    String md5 = null;
                    try {
                        if (intent.getExtras() != null) {
                            fileName = intent.getExtras().getString("fileName");
                            packageName = intent.getExtras().getString("packageName");
                            md5 = intent.getExtras().getString("md5");
                            Log.d("[AppUpgradeService]", "[AppUpgradeService] fileName: " + fileName + " packageName: " + packageName + ", md5: " + md5);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    AppUpgradeService.this.startUpdateSingle(context, fileName, packageName, md5);
                }
            }).start();
        }
    };
    private final Handler mHandler = new Handler() { // from class: tv.upgrade.service.AppUpgradeService.4
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg == null) {
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> ");
                return;
            }
            MessageObj mMessageObj = (MessageObj) msg.obj;
            final String msg_fileName = mMessageObj.fileName;
            final String msg_packageName = mMessageObj.packName;
            final String msg_md5 = mMessageObj.md5;
            final int msg_retryTime = mMessageObj.retryTime;
            switch (msg.what) {
                case AppUpgradeService.MSG_FILE_REDOWNLOAD /* -1 */:
                    Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_FILE_REDOWNLOAD, msg_fileName = " + msg_fileName);
                    Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_FILE_REDOWNLOAD, msg_packageName = " + msg_packageName);
                    Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_FILE_REDOWNLOAD, msg_retryTime = " + msg_retryTime);
                    AppUpgradeService.this.mHandler.postDelayed(new Runnable() { // from class: tv.upgrade.service.AppUpgradeService.4.1
                        @Override // java.lang.Runnable
                        public void run() {
                            int retryTime = msg_retryTime + AppUpgradeService.MSG_CONNECT_WIFI;
                            AppUpgradeService.this.downLoad(msg_fileName, msg_packageName, msg_md5, retryTime);
                        }
                    }, 60000L);
                    break;
                case AppUpgradeService.MSG_FILE_DOWNLOADED /* 0 */:
                    Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_FILE_DOWNLOADED, msg_fileName = " + msg_fileName);
                    Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_FILE_DOWNLOADED, msg_packageName = " + msg_packageName);
                    if (AppUpgradeService.XML_FILE_NAME.equals(msg_fileName)) {
                        Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_FILE_DOWNLOADED, rule file is downloaded, start resolve!");
                        AppUpgradeService.this.resolveRuleXml();
                        break;
                    } else {
                        Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_FILE_DOWNLOADED, apk file is downloaded, start install!");
                        try {
                            String apkPath = AppUpgradeService.PATH_SAVE + msg_fileName;
                            Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_FILE_DOWNLOADED, apkPath = " + apkPath);
                            Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_FILE_DOWNLOADED, getFileMD5 Start");
                            String fileMd5 = AppUpgradeService.this.getFileMD5(new File(apkPath));
                            Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_FILE_DOWNLOADED, fileMd5 = " + fileMd5);
                            Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_FILE_DOWNLOADED, msg_md5 = " + msg_md5);
                            if (!fileMd5.equalsIgnoreCase(msg_md5)) {
                                Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_FILE_DOWNLOADED, md5 not match, skip!");
                                Intent md5ErrorIntent = new Intent();
                                md5ErrorIntent.setAction("com.zte.APPUP_INSTALL_MD5Error");
                                md5ErrorIntent.putExtra("packageName", msg_packageName);
                                AppUpgradeService.this.sendBroadcast(md5ErrorIntent);
                                if (AppUpgradeService.this.mLogcatWrite != null) {
                                    AppUpgradeService.this.mLogcatWrite.stopWatch();
                                }
                            } else {
                                Intent beginIntent = new Intent();
                                beginIntent.setAction("com.zte.APPUP_INSTALL_BEGIN");
                                beginIntent.putExtra("packageName", msg_packageName);
                                AppUpgradeService.this.sendBroadcast(beginIntent);
                                AppUpgradeService.this.installApkSilence(AppUpgradeService.mContext, msg_fileName, msg_packageName);
                            }
                            break;
                        } catch (Exception e) {
                            Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_FILE_DOWNLOADED, Exception = " + e);
                            return;
                        }
                    }
                case AppUpgradeService.MSG_CONNECT_WIFI /* 1 */:
                    Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_CONNECT_WIFI");
                    if (!AppUpgradeService.this.isNetworkConnected(AppUpgradeService.mContext) && AppUpgradeService.this.retryCount < AppUpgradeService.this.retryMax) {
                        String[] value = AppUpgradeService.this.valueCustomization.split("#");
                        if (value.length >= AppUpgradeService.MSG_FILE_DOWNLOADED_SINGLE && AppUpgradeService.AUTO_INSTALL_SUCCESS.equals(value[AppUpgradeService.MSG_FILE_DOWNLOADED]) && value[AppUpgradeService.MSG_CONNECT_WIFI].length() > 0 && value[AppUpgradeService.MSG_CONNECT_NETWORK].length() > 0 && !"null".equals(value[AppUpgradeService.MSG_CONNECT_WIFI]) && value[AppUpgradeService.MSG_INSTALL_RESULT].length() > 0) {
                            WifiManager mWifiManager = (WifiManager) AppUpgradeService.this.getSystemService("wifi");
                            mWifiManager.setWifiEnabled(AppUpgradeService.DEBUG);
                            AppUpgradeService.this.sentWIFI(value[AppUpgradeService.MSG_CONNECT_WIFI], value[AppUpgradeService.MSG_CONNECT_NETWORK]);
                        }
                        if (AppUpgradeService.this.mRandom == null) {
                            AppUpgradeService.this.mRandom = new Random();
                        }
                        AppUpgradeService.this.mHandler.sendMessageDelayed(Message.obtain(AppUpgradeService.this.mHandler, AppUpgradeService.MSG_CONNECT_WIFI, new MessageObj("", "", "", AppUpgradeService.MSG_FILE_DOWNLOADED)), AppUpgradeService.TIME_DELAYED * (AppUpgradeService.this.mRandom.nextInt(AppUpgradeService.MSG_FILE_REDOWNLOAD_SINGLE) + 20));
                        AppUpgradeService.access$1012(AppUpgradeService.this, AppUpgradeService.MSG_CONNECT_WIFI);
                        AppUpgradeService.this.logcatCloseSignal = AppUpgradeService.MSG_INSTALL_RESULT;
                        Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_CONNECT_WIFI not Connected");
                        break;
                    }
                    break;
                case AppUpgradeService.MSG_CONNECT_NETWORK /* 2 */:
                    if (AppUpgradeService.this.executeCloseSignal != AppUpgradeService.MSG_CONNECT_NETWORK) {
                        AppUpgradeService.this.executeCloseSignal = AppUpgradeService.MSG_CONNECT_NETWORK;
                        Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_CONNECT_NETWORK isNetworkConnected");
                        AppUpgradeService.this.retryCount = AppUpgradeService.MSG_FILE_DOWNLOADED;
                        AppUpgradeService.this.startUpdate(AppUpgradeService.mContext);
                        break;
                    }
                    break;
                case AppUpgradeService.MSG_INSTALL_RESULT /* 3 */:
                    Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> detect result to installed");
                    String installSignal = Settings.Global.getString(AppUpgradeService.this.getContentResolver(), AppUpgradeService.SETTING_DEFAULT);
                    if (!AppUpgradeService.AUTO_INSTALL_SUCCESS.equals(installSignal) && AppUpgradeService.this.retryCount < AppUpgradeService.this.retryMax) {
                        try {
                            AppUpgradeService.this.installApkSilence(AppUpgradeService.mContext, msg_fileName, msg_packageName);
                        } catch (Exception e2) {
                            Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_FILE_DOWNLOADED, Exception = " + e2);
                            if (AppUpgradeService.this.mLogcatWrite != null) {
                                AppUpgradeService.this.mLogcatWrite.stopWatch();
                            }
                        }
                        AppUpgradeService.access$1012(AppUpgradeService.this, AppUpgradeService.MSG_CONNECT_WIFI);
                        break;
                    } else {
                        Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> install successfully, retryCount: " + AppUpgradeService.this.retryCount);
                        if (AppUpgradeService.this.mLogcatWrite != null) {
                            AppUpgradeService.this.mLogcatWrite.stopWatch();
                            break;
                        }
                    }
                    break;
            }
        }
    };
    private ApkInfo currentApkInfo = null;
    private List<ApkInfo> apkInfos = null;

    static /* synthetic */ int access$020(AppUpgradeService x0, int x1) {
        int i = x0.logcatCloseSignal - x1;
        x0.logcatCloseSignal = i;
        return i;
    }

    static /* synthetic */ int access$1012(AppUpgradeService x0, int x1) {
        int i = x0.retryCount + x1;
        x0.retryCount = i;
        return i;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isNetworkConnected(Context context) {
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<isNetworkConnected> ");
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<isNetworkConnected> false");
        return false;
    }

    @Override // android.app.Service
    public void onCreate() {
        int i = MSG_FILE_DOWNLOADED;
        super.onCreate();
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<onCreate> AppUpgradeService");
        mContext = this;
        this.packageInstaller = createInstaller(this);
        this.sessionParams = new PackageInstaller.SessionParams(MSG_CONNECT_WIFI);
        this.sessionParams.installFlags = 128;
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<onCreate> get mZteMcspCfgManager");
        try {
            this.mZteMcspCfgManager = ZteMcspCfgManager.getInstance(mContext.getApplicationContext());
        } catch (Exception e) {
            Log.d("[AppUpgradeService]", "[AppUpgradeService]<startUpdate> Exception = " + e);
        }
        String getDefaultValue = Settings.Global.getString(getContentResolver(), SETTING_DEFAULT);
        if (!settingsDefaultValue.equals(getDefaultValue) && !AUTO_INSTALL_SUCCESS.equals(getDefaultValue)) {
            this.valueCustomization = this.mZteMcspCfgManager.ReadStrFromCfgmanager("Android", "XtsAuto", "");
            String[] value = this.valueCustomization.split("#");
            if (value.length >= MSG_FILE_DOWNLOADED_SINGLE && AUTO_INSTALL_SUCCESS.equals(value[MSG_FILE_DOWNLOADED]) && value[MSG_CONNECT_WIFI].length() > 0 && value[MSG_CONNECT_NETWORK].length() > 0 && value[MSG_INSTALL_RESULT].length() > 0) {
                this.autoTest = DEBUG;
                IntentFilter mcfilter = new IntentFilter();
                mcfilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
                registerReceiver(this.isConnectedReceiver, mcfilter);
                this.mLogcatWrite = LogcatWrite.getInstance();
                try {
                    this.mLogcatWrite.init(this, LOG_DIR);
                    this.mLogcatWrite.startWatch();
                    this.mHandler.postDelayed(this.closeLogcatRunnable, 120000L);
                } catch (Exception e2) {
                    Log.d("[AppUpgradeService]", "[AppUpgradeService]<startUpdate> mLogcatWrite Exception = " + e2);
                }
                this.autoUpdateUrl = value[MSG_INSTALL_RESULT];
                this.mRandom = new Random();
                if (!"null".equals(value[MSG_CONNECT_WIFI])) {
                    WifiManager mWifiManager = (WifiManager) getSystemService("wifi");
                    mWifiManager.setWifiEnabled(DEBUG);
                    Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> WIFI name: " + value[MSG_CONNECT_WIFI] + ", WIFI password: " + value[MSG_CONNECT_NETWORK]);
                    sentWIFI(value[MSG_CONNECT_WIFI], value[MSG_CONNECT_NETWORK]);
                }
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<startUpdate> TIME_DELAYED start");
                this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, MSG_CONNECT_WIFI, new MessageObj("", "", "", i)), TIME_DELAYED * (this.mRandom.nextInt(MSG_FILE_REDOWNLOAD_SINGLE) + 10));
            }
            Settings.Global.putString(getContentResolver(), SETTING_DEFAULT, settingsDefaultValue);
        }
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<onCreate> add IntentFilter com.zte.APPUP_SINGLE_INSTALL");
        IntentFilter installfilter = new IntentFilter();
        installfilter.addAction("com.zte.APPUP_SINGLE_INSTALL");
        registerReceiver(this.singleInstallReceiver, installfilter);
        initHandlerThraed();
    }

    private void initHandlerThraed() {
        this.mHandlerThread = new HandlerThread("handler_thread");
        this.mHandlerThread.start();
        Looper loop = this.mHandlerThread.getLooper();
        this.mSubThreadHandler = new Handler(loop) { // from class: tv.upgrade.service.AppUpgradeService.5
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                Log.i("[AppUpgradeService]", "mSubThreadHandler handleMessage thread:" + Thread.currentThread());
                if (msg == null) {
                    Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> ");
                }
                MessageObj mMessageObj = (MessageObj) msg.obj;
                String msg_fileName = mMessageObj.fileName;
                String msg_packageName = mMessageObj.packName;
                String msg_md5 = mMessageObj.md5;
                int i = mMessageObj.retryTime;
                switch (msg.what) {
                    case AppUpgradeService.MSG_FILE_DOWNLOADED_SINGLE /* 4 */:
                        Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_FILE_DOWNLOADED_SINGLE, msg_fileName = " + msg_fileName);
                        Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_FILE_DOWNLOADED_SINGLE, msg_packageName = " + msg_packageName);
                        Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_FILE_DOWNLOADED_SINGLE, apk file is downloaded, start install!");
                        try {
                            String apkPath = AppUpgradeService.PATH_SAVE_SINGLE + msg_fileName;
                            Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_FILE_DOWNLOADED_SINGLE, apkPath = " + apkPath);
                            String fileMd5 = AppUpgradeService.this.getFileMD5(new File(apkPath));
                            Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_FILE_DOWNLOADED_SINGLE, fileMd5 = " + fileMd5);
                            Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_FILE_DOWNLOADED_SINGLE, msg_md5 = " + msg_md5);
                            if (!fileMd5.equalsIgnoreCase(msg_md5)) {
                                Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_FILE_DOWNLOADED_SINGLE, md5 not match, skip!");
                                if (apkPath != null) {
                                    Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_FILE_DOWNLOADED_SINGLE, md5 not match, deleteSingleFile! apkPath =" + apkPath);
                                    AppUpgradeService.this.deleteSingleFile(apkPath);
                                }
                                Intent md5ErrorIntent = new Intent();
                                md5ErrorIntent.setAction("com.zte.APPUP_INSTALL_MD5Error");
                                md5ErrorIntent.putExtra("packageName", msg_packageName);
                                AppUpgradeService.this.sendBroadcast(md5ErrorIntent);
                                if (AppUpgradeService.this.mLogcatWrite != null) {
                                    AppUpgradeService.this.mLogcatWrite.stopWatch();
                                    break;
                                }
                            } else {
                                Intent beginIntent = new Intent();
                                beginIntent.setAction("com.zte.APPUP_INSTALL_BEGIN");
                                beginIntent.putExtra("packageName", msg_packageName);
                                AppUpgradeService.this.sendBroadcast(beginIntent);
                                AppUpgradeService.this.installSingleApkSilence(AppUpgradeService.mContext, msg_fileName, msg_packageName);
                                break;
                            }
                        } catch (Exception e) {
                            Log.d("[AppUpgradeService]", "[AppUpgradeService]<handleMessage> MSG_FILE_DOWNLOADED_SINGLE, Exception = " + e);
                            return;
                        }
                        break;
                }
            }
        };
    }

    /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue */
    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<onStartCommand> start update-apk service");
        if (!this.autoTest) {
            String str = this.MarketCode_OperatorCode;
            char c = 65535;
            switch (str.hashCode()) {
                case 1451406069:
                    if (str.equals("1301 2")) {
                        c = 2;
                        break;
                    }
                    break;
                case 1511434933:
                    if (str.equals("3601 1")) {
                        c = 1;
                        break;
                    }
                    break;
                case 1540987606:
                    if (str.equals("4701 2")) {
                        c = MSG_FILE_DOWNLOADED;
                        break;
                    }
                    break;
            }
            switch (c) {
                case MSG_FILE_DOWNLOADED /* 0 */:
                case MSG_CONNECT_WIFI /* 1 */:
                case MSG_CONNECT_NETWORK /* 2 */:
                    startUpdate(mContext);
                default:
                    return MSG_FILE_DOWNLOADED;
            }
        }
        return MSG_FILE_DOWNLOADED;
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<onBind> ");
        return null;
    }

    @Override // android.app.Service
    public void onDestroy() {
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<onDestroy> ");
        if (this.autoTest) {
            unregisterReceiver(this.isConnectedReceiver);
        }
        unregisterReceiver(this.isConnectedReceiver);
        this.mHandler.removeCallbacksAndMessages(null);
        this.mHandlerThread.quit();
        if (this.mLogcatWrite != null) {
            this.mLogcatWrite.stopWatch();
        }
        super.onDestroy();
    }

    private boolean isOldXmlFileExist() {
        try {
            File file = new File(PATH_XML_FILE_OLD);
            File absoluteFile = file.getAbsoluteFile();
            Log.d("[AppUpgradeService]", "[AppUpgradeService]<isOldXmlFileExist> absoluteFile = " + absoluteFile.getPath());
            if (!absoluteFile.exists()) {
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<isOldXmlFileExist> file not exist!");
                return false;
            }
            return DEBUG;
        } catch (Exception e) {
            Log.d("[AppUpgradeService]", "[AppUpgradeService]<isOldXmlFileExist> Exception = " + e);
            return false;
        }
    }

    /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue */
    private void executeAction(List<ApkInfo> mApkInfoList) {
        if (mApkInfoList != null) {
            this.logcatCloseSignal = MSG_INSTALL_RESULT;
            for (ApkInfo mApkInfo : mApkInfoList) {
                String packageName = mApkInfo.getPackageName();
                String fileName = mApkInfo.getFileName();
                String action = mApkInfo.getAction();
                String xmlMd5 = mApkInfo.getMd5();
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<executeAction> xmlMd5 = " + xmlMd5);
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<executeAction> packageName = " + packageName);
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<executeAction> fileName = " + fileName);
                int versionCode = getInstalledApkVersionCode(packageName);
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<executeAction> versionCode = " + versionCode);
                String xmlVersioncode = mApkInfo.getVersioncode();
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<executeAction> xmlVersioncode = " + xmlVersioncode);
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<executeAction> action = " + action);
                char c = 65535;
                switch (action.hashCode()) {
                    case -1335458389:
                        if (action.equals(ACTION_DELETE)) {
                            c = 2;
                            break;
                        }
                        break;
                    case -838846263:
                        if (action.equals(ACTION_UPDATE)) {
                            c = MSG_FILE_DOWNLOADED;
                            break;
                        }
                        break;
                    case 761243362:
                        if (action.equals(ACTION_FALLBACK)) {
                            c = 1;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case MSG_FILE_DOWNLOADED /* 0 */:
                        Log.d("[AppUpgradeService]", "[AppUpgradeService]<executeAction> ACTION_UPDATE");
                        if (versionCode >= Integer.parseInt(xmlVersioncode)) {
                            Log.d("[AppUpgradeService]", "[AppUpgradeService]<executeAction> ACTION_UPDATE, may has been upgraded, skip!");
                            if (this.mLogcatWrite != null) {
                                this.mLogcatWrite.stopWatch();
                                break;
                            } else {
                                break;
                            }
                        } else {
                            downLoad(fileName, packageName, xmlMd5, MSG_FILE_DOWNLOADED);
                            break;
                        }
                    case MSG_CONNECT_WIFI /* 1 */:
                        Log.d("[AppUpgradeService]", "[AppUpgradeService]<executeAction> ACTION_FALLBACK");
                        if (versionCode == Integer.parseInt(xmlVersioncode)) {
                            Log.d("[AppUpgradeService]", "[AppUpgradeService]<executeAction> ACTION_FALLBACK, " + fileName + " may has been installed, skip!");
                            if (this.mLogcatWrite != null) {
                                this.mLogcatWrite.stopWatch();
                                break;
                            } else {
                                break;
                            }
                        } else {
                            uninstallApkSilent(mContext, packageName);
                            downLoad(fileName, packageName, xmlMd5, MSG_FILE_DOWNLOADED);
                            break;
                        }
                    case MSG_CONNECT_NETWORK /* 2 */:
                        Log.d("[AppUpgradeService]", "[AppUpgradeService]<executeAction> ACTION_DELETE");
                        if (!checkApkIsInstalled(packageName)) {
                            Log.d("[AppUpgradeService]", "[AppUpgradeService]<executeAction> ACTION_DELETE, " + packageName + " may has been uninstalled, skip!");
                            if (this.mLogcatWrite != null) {
                                this.mLogcatWrite.stopWatch();
                                break;
                            } else {
                                break;
                            }
                        } else {
                            uninstallApkSilent(mContext, packageName);
                            break;
                        }
                }
            }
        }
    }

    public void startUpdateSingle(Context stContext, final String fileName, final String packageName, final String md5) {
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<startUpdateSingle> ");
        mContext = stContext;
        this.logcatCloseSignal = MSG_INSTALL_RESULT;
        if (this.mZteMcspCfgManager == null) {
            try {
                this.mZteMcspCfgManager = ZteMcspCfgManager.getInstance(mContext.getApplicationContext());
            } catch (Exception e) {
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<startUpdateSingle> Exception = " + e);
            }
        }
        this.mHandler.postDelayed(new Runnable() { // from class: tv.upgrade.service.AppUpgradeService.6
            @Override // java.lang.Runnable
            public void run() {
                String systemUpdateUrl;
                if (AppUpgradeService.this.autoTest) {
                    systemUpdateUrl = AppUpgradeService.this.autoUpdateUrl;
                } else if ("3601 1".equals(AppUpgradeService.this.MarketCode_OperatorCode)) {
                    String DefaultPlatform = AppUpgradeService.this.mZteMcspCfgManager.ReadStrFromCfgmanager("qcs", "DefaultPlatform", "");
                    Log.d("[AppUpgradeService]", "[AppUpgradeService]<startUpdateSingle>DefaultPlatform=" + DefaultPlatform);
                    systemUpdateUrl = AppUpgradeService.AUTO_INSTALL_SUCCESS.equals(DefaultPlatform) ? AppUpgradeService.this.mZteMcspCfgManager.ReadStrFromCfgmanager("Pltm_option", "OTTVersionServer", "") : AppUpgradeService.this.mZteMcspCfgManager.ReadStrFromCfgmanager("Common", "VersionServer", "");
                } else {
                    systemUpdateUrl = AppUpgradeService.this.mZteMcspCfgManager.ReadStrFromCfgmanager("Common", "VersionServer", "");
                }
                if (systemUpdateUrl == null || TextUtils.isEmpty(systemUpdateUrl) || !systemUpdateUrl.startsWith(AppUpgradeService.HTTPS)) {
                    AppUpgradeService.this.setSecureConnection = AppUpgradeService.MSG_FILE_DOWNLOADED;
                } else {
                    AppUpgradeService.this.setSecureConnection = AppUpgradeService.MSG_CONNECT_WIFI;
                }
                String unused = AppUpgradeService.UPDATE_URL_SINGLE = systemUpdateUrl + "/updateSingleAPK/";
                if (systemUpdateUrl == null || TextUtils.isEmpty(systemUpdateUrl)) {
                    Log.d("[AppUpgradeService]", "[AppUpgradeService]<startUpdate> error update path, stop update!");
                    return;
                }
                String lastString = systemUpdateUrl.substring(systemUpdateUrl.length() + AppUpgradeService.MSG_FILE_REDOWNLOAD);
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<startUpdateSingle> lastString = " + lastString);
                if ("/".equals(lastString)) {
                    Log.d("[AppUpgradeService]", "[AppUpgradeService]<startUpdate> add updateSingleAPK/");
                    String unused2 = AppUpgradeService.UPDATE_URL_SINGLE = systemUpdateUrl + "updateSingleAPK/";
                }
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<startUpdateSingle> UPDATE_URL_SINGLE = " + AppUpgradeService.UPDATE_URL_SINGLE);
                AppUpgradeService.this.downLoadSingle(fileName, packageName, md5, AppUpgradeService.MSG_FILE_DOWNLOADED);
            }
        }, 500L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void downLoadSingle(final String fileName, final String packageName, final String md5, final int time) {
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<downLoad> ");
        Intent downloadIntent = new Intent();
        downloadIntent.setAction("com.zte.APPUP_DOWNLOAD_BEGIN");
        downloadIntent.putExtra("packageName", packageName);
        sendBroadcast(downloadIntent);
        final String url_path = UPDATE_URL_SINGLE + fileName;
        final String save_path = PATH_SAVE_SINGLE + fileName;
        new Thread(new Runnable() { // from class: tv.upgrade.service.AppUpgradeService.7
            /* JADX WARN: Not initialized variable reg: 15, insn: 0x00de: MOVE (r14 I:??[OBJECT, ARRAY]) = (r15 I:??[OBJECT, ARRAY] A[D('fileOutputStream' java.io.FileOutputStream)]), block:B:39:0x00de */
            /* JADX WARN: Not initialized variable reg: 15, insn: 0x01ca: MOVE (r14 I:??[OBJECT, ARRAY]) = (r15 I:??[OBJECT, ARRAY] A[D('fileOutputStream' java.io.FileOutputStream)]), block:B:45:0x01ca */
            @Override // java.lang.Runnable
            public void run() {
                URL url;
                FileOutputStream fileOutputStream;
                FileOutputStream fileOutputStream2;
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<downLoad> time = " + time);
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<downLoad> url_path = " + url_path);
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<downLoad> save_path = " + save_path);
                InputStream is = null;
                FileOutputStream fileOutputStream3 = null;
                try {
                    try {
                        url = new URL(url_path);
                    } catch (Exception e) {
                        e = e;
                    }
                } catch (Throwable th) {
                    th = th;
                }
                try {
                    switch (AppUpgradeService.this.setSecureConnection) {
                        case AppUpgradeService.MSG_CONNECT_WIFI /* 1 */:
                            SSLContext sslContext = SSLContext.getInstance("SSL");
                            TrustManager[] tm = {new MyX509TrustManager()};
                            sslContext.init(null, tm, new SecureRandom());
                            SSLSocketFactory ssf = sslContext.getSocketFactory();
                            HttpsURLConnection conHtpps = (HttpsURLConnection) url.openConnection();
                            conHtpps.setHostnameVerifier(AppUpgradeService.this.new TrustAnyHostnameVerifier());
                            conHtpps.setReadTimeout(5000);
                            conHtpps.setConnectTimeout(5000);
                            conHtpps.setRequestProperty("Charset", "UTF-8");
                            conHtpps.setRequestMethod("GET");
                            conHtpps.setDoInput(AppUpgradeService.DEBUG);
                            conHtpps.setSSLSocketFactory(ssf);
                            conHtpps.connect();
                            int getResponseCode = conHtpps.getResponseCode();
                            Log.d("[AppUpgradeService]", "[AppUpgradeService]<downLoadFromHttps> getResponseCode = " + getResponseCode);
                            if (getResponseCode == 200) {
                                is = conHtpps.getInputStream();
                                if (is != null) {
                                    FileOutputStream fileOutputStream4 = new FileOutputStream(AppUpgradeService.this.createFile(save_path));
                                    Log.d("[AppUpgradeService]", "[AppUpgradeService]<downLoad> start save file");
                                    byte[] buf = new byte[1024];
                                    while (true) {
                                        int ch = is.read(buf);
                                        if (ch != AppUpgradeService.MSG_FILE_REDOWNLOAD) {
                                            fileOutputStream4.write(buf, AppUpgradeService.MSG_FILE_DOWNLOADED, ch);
                                        } else {
                                            fileOutputStream3 = fileOutputStream4;
                                        }
                                    }
                                }
                                if (fileOutputStream3 != null) {
                                    Log.d("[AppUpgradeService]", "[AppUpgradeService]<downLoad> close");
                                    fileOutputStream3.flush();
                                    break;
                                }
                            }
                            break;
                        default:
                            HttpURLConnection con = (HttpURLConnection) url.openConnection();
                            con.setReadTimeout(5000);
                            con.setConnectTimeout(5000);
                            con.setRequestProperty("Charset", "UTF-8");
                            con.setRequestMethod("GET");
                            con.setDoInput(AppUpgradeService.DEBUG);
                            con.connect();
                            int getResponseCode2 = con.getResponseCode();
                            Log.d("[AppUpgradeService]", "[AppUpgradeService]<downLoad> getResponseCode = " + getResponseCode2);
                            if (getResponseCode2 == 200) {
                                is = con.getInputStream();
                                if (is != null) {
                                    FileOutputStream fileOutputStream5 = new FileOutputStream(AppUpgradeService.this.createFile(save_path));
                                    byte[] buf2 = new byte[1024];
                                    while (true) {
                                        int ch2 = is.read(buf2);
                                        if (ch2 != AppUpgradeService.MSG_FILE_REDOWNLOAD) {
                                            fileOutputStream5.write(buf2, AppUpgradeService.MSG_FILE_DOWNLOADED, ch2);
                                        } else {
                                            fileOutputStream3 = fileOutputStream5;
                                        }
                                    }
                                }
                                if (fileOutputStream3 != null) {
                                    fileOutputStream3.flush();
                                    break;
                                }
                            }
                            break;
                    }
                    AppUpgradeService.this.mSubThreadHandler.sendMessageDelayed(Message.obtain(AppUpgradeService.this.mSubThreadHandler, AppUpgradeService.MSG_FILE_DOWNLOADED_SINGLE, new MessageObj(fileName, packageName, md5, time)), AppUpgradeService.TIME_DELAYED);
                    AppUpgradeService.cleanClose(is);
                    AppUpgradeService.cleanClose(fileOutputStream3);
                } catch (Exception e2) {
                    e = e2;
                    fileOutputStream3 = fileOutputStream2;
                    Log.d("[AppUpgradeService]", "[AppUpgradeService]<downLoad> Exception = " + e);
                    e.printStackTrace();
                    Intent downloadFailedIntent = new Intent();
                    downloadFailedIntent.setAction("com.zte.APPUP_DOWNLOAD_FAILED");
                    downloadFailedIntent.putExtra("packageName", packageName);
                    AppUpgradeService.this.sendBroadcast(downloadFailedIntent);
                    AppUpgradeService.cleanClose(is);
                    AppUpgradeService.cleanClose(fileOutputStream3);
                } catch (Throwable th2) {
                    th = th2;
                    fileOutputStream3 = fileOutputStream;
                    AppUpgradeService.cleanClose(is);
                    AppUpgradeService.cleanClose(fileOutputStream3);
                    throw th;
                }
            }
        }).start();
    }

    public void startUpdate(Context stContext) {
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<startUpdate> ");
        mContext = stContext;
        this.logcatCloseSignal = MSG_INSTALL_RESULT;
        if (this.mZteMcspCfgManager == null) {
            try {
                this.mZteMcspCfgManager = ZteMcspCfgManager.getInstance(mContext.getApplicationContext());
            } catch (Exception e) {
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<startUpdate> Exception = " + e);
            }
        }
        this.mHandler.postDelayed(new Runnable() { // from class: tv.upgrade.service.AppUpgradeService.8
            @Override // java.lang.Runnable
            public void run() {
                String systemUpdateUrl;
                if (AppUpgradeService.this.autoTest) {
                    systemUpdateUrl = AppUpgradeService.this.autoUpdateUrl;
                } else if ("3601 1".equals(AppUpgradeService.this.MarketCode_OperatorCode)) {
                    String DefaultPlatform = AppUpgradeService.this.mZteMcspCfgManager.ReadStrFromCfgmanager("qcs", "DefaultPlatform", "");
                    Log.d("[AppUpgradeService]", "[AppUpgradeService]<startUpdate>DefaultPlatform=" + DefaultPlatform);
                    systemUpdateUrl = AppUpgradeService.AUTO_INSTALL_SUCCESS.equals(DefaultPlatform) ? AppUpgradeService.this.mZteMcspCfgManager.ReadStrFromCfgmanager("Pltm_option", "OTTVersionServer", "") : AppUpgradeService.this.mZteMcspCfgManager.ReadStrFromCfgmanager("Common", "VersionServer", "");
                } else {
                    systemUpdateUrl = AppUpgradeService.this.mZteMcspCfgManager.ReadStrFromCfgmanager("Common", "VersionServer", "");
                }
                if (systemUpdateUrl == null || TextUtils.isEmpty(systemUpdateUrl) || !systemUpdateUrl.startsWith(AppUpgradeService.HTTPS)) {
                    AppUpgradeService.this.setSecureConnection = AppUpgradeService.MSG_FILE_DOWNLOADED;
                } else {
                    AppUpgradeService.this.setSecureConnection = AppUpgradeService.MSG_CONNECT_WIFI;
                }
                String unused = AppUpgradeService.UPDATE_URL = systemUpdateUrl + "/updateAPK/";
                if (systemUpdateUrl == null || TextUtils.isEmpty(systemUpdateUrl)) {
                    Log.d("[AppUpgradeService]", "[AppUpgradeService]<startUpdate> error update path, stop update!");
                    return;
                }
                String lastString = systemUpdateUrl.substring(systemUpdateUrl.length() + AppUpgradeService.MSG_FILE_REDOWNLOAD);
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<startUpdate> lastString = " + lastString);
                if ("/".equals(lastString)) {
                    Log.d("[AppUpgradeService]", "[AppUpgradeService]<startUpdate> add updateAPK/");
                    String unused2 = AppUpgradeService.UPDATE_URL = systemUpdateUrl + "updateAPK/";
                }
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<startUpdate> UPDATE_URL = " + AppUpgradeService.UPDATE_URL);
                AppUpgradeService.this.downLoad(AppUpgradeService.XML_FILE_NAME, null, null, AppUpgradeService.MSG_FILE_DOWNLOADED);
            }
        }, 500L);
    }

    public void resolveRuleXml() {
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<resolveRuleXml> ");
        if (!isVersionNewer()) {
            Log.d("[AppUpgradeService]", "[AppUpgradeService]<resolveRuleXml> no new version, stop Upgdate!");
            if (this.mLogcatWrite != null) {
                this.mLogcatWrite.stopWatch();
                return;
            }
            return;
        }
        XmlInfo new_XmlInfo = readXmlByPull_xmlInfo(PATH_XML_FILE_CUR);
        String new_model = "";
        if (new_XmlInfo != null) {
            new_model = new_XmlInfo.getModel();
        }
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<resolveRuleXml> new_model = " + new_model);
        if (!this.autoTest && (model == null || !model.equals(new_model))) {
            Log.d("[AppUpgradeService]", "[AppUpgradeService]<resolveRuleXml> model not match, stop Upgdate!");
            if (this.mLogcatWrite != null) {
                this.mLogcatWrite.stopWatch();
                return;
            }
            return;
        }
        renameFile(PATH_XML_FILE_CUR, PATH_XML_FILE_OLD);
        List<ApkInfo> apkInfoList = readXmlByPull_apkInfo(PATH_XML_FILE_OLD);
        executeAction(apkInfoList);
    }

    private boolean isVersionNewer() {
        long long_new;
        long long_old;
        boolean isOldXmlFileExist = isOldXmlFileExist();
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<isVersionNewer> isOldXmlFileExist = " + isOldXmlFileExist);
        if (isOldXmlFileExist) {
            XmlInfo old_XmlInfo = readXmlByPull_xmlInfo(PATH_XML_FILE_OLD);
            XmlInfo new_XmlInfo = readXmlByPull_xmlInfo(PATH_XML_FILE_CUR);
            String old_version = "";
            String new_version = "";
            if (old_XmlInfo != null) {
                old_version = old_XmlInfo.getVersion().trim();
            }
            if (new_XmlInfo != null) {
                new_version = new_XmlInfo.getVersion().trim();
            }
            Log.d("[AppUpgradeService]", "[AppUpgradeService]<isVersionNewer> old_version = " + old_version);
            Log.d("[AppUpgradeService]", "[AppUpgradeService]<isVersionNewer> new_version = " + new_version);
            if ("".equals(new_version)) {
                long_new = 0;
            } else {
                long_new = Long.parseLong(new_version);
            }
            if ("".equals(old_version)) {
                long_old = 0;
            } else {
                long_old = Long.parseLong(old_version);
            }
            if ("".equals(new_version) || long_new < long_old) {
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<isVersionNewer> no new version, stop Upgdate!");
                return false;
            }
        }
        return DEBUG;
    }

    private File renameFile(String oldPath, String newPath) {
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<renameFile> oldPath = " + oldPath);
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<renameFile> newPath = " + newPath);
        if (TextUtils.isEmpty(oldPath) || TextUtils.isEmpty(newPath)) {
            return null;
        }
        File oldFile = new File(oldPath);
        File newFile = new File(newPath);
        oldFile.renameTo(newFile);
        return new File(newPath);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void downLoad(final String fileName, final String packageName, final String md5, final int time) {
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<downLoad> ");
        Intent downloadIntent = new Intent();
        downloadIntent.setAction("com.zte.APPUP_DOWNLOAD_BEGIN");
        downloadIntent.putExtra("packageName", packageName);
        sendBroadcast(downloadIntent);
        if (time > MSG_INSTALL_RESULT) {
            Log.d("[AppUpgradeService]", "[AppUpgradeService]<downLoad> retry has 3 times, meybe network is error!");
            if (this.mLogcatWrite != null) {
                this.mLogcatWrite.stopWatch();
                return;
            }
            return;
        }
        this.logcatCloseSignal = MSG_INSTALL_RESULT;
        final String url_path = UPDATE_URL + fileName;
        final String save_path = PATH_SAVE + fileName;
        new Thread(new Runnable() { // from class: tv.upgrade.service.AppUpgradeService.9
            /* JADX WARN: Not initialized variable reg: 14, insn: 0x00da: MOVE (r13 I:??[OBJECT, ARRAY]) = (r14 I:??[OBJECT, ARRAY] A[D('fileOutputStream' java.io.FileOutputStream)]), block:B:39:0x00da */
            /* JADX WARN: Not initialized variable reg: 14, insn: 0x01df: MOVE (r13 I:??[OBJECT, ARRAY]) = (r14 I:??[OBJECT, ARRAY] A[D('fileOutputStream' java.io.FileOutputStream)]), block:B:44:0x01df */
            @Override // java.lang.Runnable
            public void run() {
                URL url;
                FileOutputStream fileOutputStream;
                FileOutputStream fileOutputStream2;
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<downLoad> time = " + time);
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<downLoad> url_path = " + url_path);
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<downLoad> save_path = " + save_path);
                InputStream is = null;
                FileOutputStream fileOutputStream3 = null;
                try {
                    try {
                        url = new URL(url_path);
                    } catch (Throwable th) {
                        th = th;
                    }
                } catch (Exception e) {
                    e = e;
                }
                try {
                    switch (AppUpgradeService.this.setSecureConnection) {
                        case AppUpgradeService.MSG_CONNECT_WIFI /* 1 */:
                            SSLContext sslContext = SSLContext.getInstance("SSL");
                            TrustManager[] tm = {new MyX509TrustManager()};
                            sslContext.init(null, tm, new SecureRandom());
                            SSLSocketFactory ssf = sslContext.getSocketFactory();
                            HttpsURLConnection conHtpps = (HttpsURLConnection) url.openConnection();
                            conHtpps.setHostnameVerifier(AppUpgradeService.this.new TrustAnyHostnameVerifier());
                            conHtpps.setReadTimeout(5000);
                            conHtpps.setConnectTimeout(5000);
                            conHtpps.setRequestProperty("Charset", "UTF-8");
                            conHtpps.setRequestMethod("GET");
                            conHtpps.setDoInput(AppUpgradeService.DEBUG);
                            conHtpps.setSSLSocketFactory(ssf);
                            conHtpps.connect();
                            int getResponseCode = conHtpps.getResponseCode();
                            Log.d("[AppUpgradeService]", "[AppUpgradeService]<downLoadFromHttps> getResponseCode = " + getResponseCode);
                            if (getResponseCode == 200) {
                                is = conHtpps.getInputStream();
                                if (is != null) {
                                    FileOutputStream fileOutputStream4 = new FileOutputStream(AppUpgradeService.this.createFile(save_path));
                                    Log.d("[AppUpgradeService]", "[AppUpgradeService]<downLoad> start save file");
                                    byte[] buf = new byte[1024];
                                    while (true) {
                                        int ch = is.read(buf);
                                        if (ch != AppUpgradeService.MSG_FILE_REDOWNLOAD) {
                                            fileOutputStream4.write(buf, AppUpgradeService.MSG_FILE_DOWNLOADED, ch);
                                        } else {
                                            fileOutputStream3 = fileOutputStream4;
                                        }
                                    }
                                }
                                if (fileOutputStream3 != null) {
                                    Log.d("[AppUpgradeService]", "[AppUpgradeService]<downLoad> close");
                                    fileOutputStream3.flush();
                                    break;
                                }
                            }
                            break;
                        default:
                            HttpURLConnection con = (HttpURLConnection) url.openConnection();
                            con.setReadTimeout(5000);
                            con.setConnectTimeout(5000);
                            con.setRequestProperty("Charset", "UTF-8");
                            con.setRequestMethod("GET");
                            con.setDoInput(AppUpgradeService.DEBUG);
                            con.connect();
                            int getResponseCode2 = con.getResponseCode();
                            Log.d("[AppUpgradeService]", "[AppUpgradeService]<downLoad> getResponseCode = " + getResponseCode2);
                            if (getResponseCode2 == 200) {
                                is = con.getInputStream();
                                if (is != null) {
                                    FileOutputStream fileOutputStream5 = new FileOutputStream(AppUpgradeService.this.createFile(save_path));
                                    byte[] buf2 = new byte[1024];
                                    while (true) {
                                        int ch2 = is.read(buf2);
                                        if (ch2 != AppUpgradeService.MSG_FILE_REDOWNLOAD) {
                                            fileOutputStream5.write(buf2, AppUpgradeService.MSG_FILE_DOWNLOADED, ch2);
                                        } else {
                                            fileOutputStream3 = fileOutputStream5;
                                        }
                                    }
                                }
                                if (fileOutputStream3 != null) {
                                    fileOutputStream3.flush();
                                    break;
                                }
                            }
                            break;
                    }
                    AppUpgradeService.this.mHandler.sendMessageDelayed(Message.obtain(AppUpgradeService.this.mHandler, AppUpgradeService.MSG_FILE_DOWNLOADED, new MessageObj(fileName, packageName, md5, time)), AppUpgradeService.TIME_DELAYED);
                    AppUpgradeService.cleanClose(is);
                    AppUpgradeService.cleanClose(fileOutputStream3);
                } catch (Exception e2) {
                    e = e2;
                    fileOutputStream3 = fileOutputStream2;
                    Log.d("[AppUpgradeService]", "[AppUpgradeService]<downLoad> Exception = " + e);
                    e.printStackTrace();
                    AppUpgradeService.this.mHandler.sendMessageDelayed(Message.obtain(AppUpgradeService.this.mHandler, AppUpgradeService.MSG_FILE_REDOWNLOAD, new MessageObj(fileName, packageName, md5, time)), AppUpgradeService.TIME_DELAYED);
                    AppUpgradeService.cleanClose(is);
                    AppUpgradeService.cleanClose(fileOutputStream3);
                } catch (Throwable th2) {
                    th = th2;
                    fileOutputStream3 = fileOutputStream;
                    AppUpgradeService.cleanClose((InputStream) null);
                    AppUpgradeService.cleanClose(fileOutputStream3);
                    throw th;
                }
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public File createFile(String filePath) {
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<createFile> ");
        File saveDir = new File(PATH_SAVE);
        if (!saveDir.exists()) {
            try {
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<createFile> saveDir not exist, create it!");
                saveDir.mkdirs();
            } catch (Exception e) {
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<createFile> Exception = " + e);
            }
        }
        File saveFile = new File(filePath);
        if (!saveFile.exists()) {
            try {
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<createFile> saveFile not exist, create it!");
                saveFile.createNewFile();
            } catch (Exception e2) {
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<createFile> Exception = " + e2);
            }
        }
        return new File(filePath);
    }

    private static class MessageObj {
        String fileName;
        String md5;
        String packName;
        int retryTime;

        private MessageObj(String mFileName, String mPackName, String mMd5, int mRetryTime) {
            this.fileName = mFileName;
            this.packName = mPackName;
            this.md5 = mMd5;
            this.retryTime = mRetryTime;
        }
    }

    public List<ApkInfo> readXmlByPull_apkInfo(String xmlFilePath) {
        InputStream mInputStream;
        List<ApkInfo> list = null;
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<readXmlByPull_apkInfo> xmlFilePath = " + xmlFilePath);
        XmlPullParser parser = Xml.newPullParser();
        InputStream mInputStream2 = null;
        this.logcatCloseSignal = MSG_INSTALL_RESULT;
        try {
            try {
                mInputStream = new FileInputStream(new File(xmlFilePath));
            } catch (Exception e) {
                e = e;
            }
        } catch (Throwable th) {
            th = th;
        }
        try {
            parser.setInput(mInputStream, "UTF-8");
            for (int eventType = parser.getEventType(); eventType != MSG_CONNECT_WIFI; eventType = parser.next()) {
                switch (eventType) {
                    case MSG_FILE_DOWNLOADED /* 0 */:
                        this.apkInfos = new ArrayList();
                        break;
                    case MSG_CONNECT_NETWORK /* 2 */:
                        String name = parser.getName();
                        if ("apk".equals(name)) {
                            this.currentApkInfo = new ApkInfo();
                            this.currentApkInfo.setNumber(new Integer(parser.getAttributeValue(null, "number")).intValue());
                            break;
                        } else {
                            switchApkInfo(parser);
                            break;
                        }
                    case MSG_INSTALL_RESULT /* 3 */:
                        if (parser.getName().equalsIgnoreCase("apk") && this.currentApkInfo != null) {
                            this.apkInfos.add(this.currentApkInfo);
                            this.currentApkInfo = null;
                            break;
                        }
                        break;
                }
            }
            mInputStream.close();
            list = this.apkInfos;
            cleanClose(mInputStream);
        } catch (Exception e2) {
            e = e2;
            mInputStream2 = mInputStream;
            Log.d("[AppUpgradeService]", "[AppUpgradeService]<readXmlByPull> Exception = " + e);
            e.printStackTrace();
            if (this.mLogcatWrite != null) {
                this.mLogcatWrite.stopWatch();
            }
            cleanClose(mInputStream2);
            return list;
        } catch (Throwable th2) {
            th = th2;
            mInputStream2 = mInputStream;
            cleanClose(mInputStream2);
            throw th;
        }
        return list;
    }

    private void switchApkInfo(XmlPullParser parser) {
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<switchApkInfo> ");
        try {
            String name = parser.getName();
            if (this.currentApkInfo != null) {
                if ("filename".equals(name)) {
                    this.currentApkInfo.setFileName(parser.nextText());
                } else if ("packagename".equals(name)) {
                    this.currentApkInfo.setPackageName(parser.nextText());
                } else if ("versioncode".equals(name)) {
                    this.currentApkInfo.setVersioncode(parser.nextText());
                } else if ("action".equals(name)) {
                    this.currentApkInfo.setAction(parser.nextText());
                } else if ("md5".equals(name)) {
                    this.currentApkInfo.setMd5(parser.nextText());
                }
            }
        } catch (Exception e) {
            Log.d("[AppUpgradeService]", "[AppUpgradeService]<switchApkInfo> Exception = " + e);
        }
    }

    public XmlInfo readXmlByPull_xmlInfo(String xmlFilePath) {
        InputStream mInputStream;
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<readXmlByPull_xmlInfo> xmlFilePath = " + xmlFilePath);
        XmlPullParser parser = Xml.newPullParser();
        this.logcatCloseSignal = MSG_INSTALL_RESULT;
        InputStream mInputStream2 = null;
        try {
            try {
                mInputStream = new FileInputStream(new File(xmlFilePath));
            } catch (Exception e) {
                e = e;
            }
        } catch (Throwable th) {
            th = th;
        }
        try {
            parser.setInput(mInputStream, "UTF-8");
            XmlInfo mXmlInfo = null;
            for (int eventType = parser.getEventType(); eventType != MSG_CONNECT_WIFI; eventType = parser.next()) {
                switch (eventType) {
                    case MSG_FILE_DOWNLOADED /* 0 */:
                        mXmlInfo = new XmlInfo();
                        break;
                    case MSG_CONNECT_NETWORK /* 2 */:
                        Log.d("[AppUpgradeService]", "[AppUpgradeService]<readXmlByPull_xmlInfo> START_TAG");
                        String name = parser.getName();
                        if (mXmlInfo == null) {
                            break;
                        } else if ("xmlversion".equals(name)) {
                            String xmlversion = parser.nextText();
                            mXmlInfo.setVersion(xmlversion);
                            break;
                        } else if ("model".equals(name)) {
                            String xmlModel = parser.nextText();
                            mXmlInfo.setModel(xmlModel);
                            break;
                        } else {
                            break;
                        }
                }
            }
            mInputStream.close();
            cleanClose(mInputStream);
            return mXmlInfo;
        } catch (Exception e2) {
            e = e2;
            mInputStream2 = mInputStream;
            Log.d("[AppUpgradeService]", "[AppUpgradeService]<readXmlByPull_xmlInfo> Exception = " + e);
            e.printStackTrace();
            if (this.mLogcatWrite != null) {
                this.mLogcatWrite.stopWatch();
            }
            cleanClose(mInputStream2);
            return null;
        } catch (Throwable th2) {
            th = th2;
            mInputStream2 = mInputStream;
            cleanClose(mInputStream2);
            throw th;
        }
    }

    private int getInstalledApkVersionCode(String mPackageName) {
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<getInstalledApkVersionCode> ");
        if (!checkApkIsInstalled(mPackageName)) {
            Log.d("[AppUpgradeService]", "[AppUpgradeService]<getInstalledApkVersionCode> apk is not exist");
            return MSG_FILE_REDOWNLOAD;
        }
        try {
            PackageManager packageManager = mContext.getPackageManager();
            PackageInfo mPackageInfo = packageManager.getPackageInfo(mPackageName, MSG_FILE_DOWNLOADED);
            int versionCode = mPackageInfo.versionCode;
            Log.d("[AppUpgradeService]", "[AppUpgradeService]<getInstalledApkVersionCode> versionCode = " + versionCode);
            return versionCode;
        } catch (Exception e) {
            Log.d("[AppUpgradeService]", "[AppUpgradeService]<getInstalledApkVersionCode> Exception = " + e);
            return MSG_FILE_REDOWNLOAD;
        }
    }

    private boolean checkApkIsInstalled(String packageName) {
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<checkApkIsInstalled> packageName = " + packageName);
        if (packageName == null || "".equals(packageName)) {
            return false;
        }
        try {
            mContext.getPackageManager().getApplicationInfo(packageName, 8192);
            Log.d("[AppUpgradeService]", "[AppUpgradeService]<checkApkIsInstalled> true");
            return DEBUG;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("[AppUpgradeService]", "[AppUpgradeService]<checkApkIsInstalled> false");
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getFileMD5(File file) {
        String str;
        FileInputStream in;
        FileInputStream in2 = null;
        try {
            try {
                in = new FileInputStream(file);
            } catch (Exception e) {
                e = e;
            }
        } catch (Throwable th) {
            th = th;
        }
        try {
            FileChannel ch = in.getChannel();
            str = culMD5(ch.map(FileChannel.MapMode.READ_ONLY, 0L, file.length()));
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e2) {
                    Log.d("[AppUpgradeService]", "Exception ==" + e2.getMessage());
                }
            }
        } catch (Exception e3) {
            e = e3;
            in2 = in;
            Log.d("[AppUpgradeService]", "Exception ==" + e.getMessage());
            str = "";
            if (in2 != null) {
                try {
                    in2.close();
                } catch (IOException e4) {
                    Log.d("[AppUpgradeService]", "Exception ==" + e4.getMessage());
                }
            }
            return str;
        } catch (Throwable th2) {
            th = th2;
            in2 = in;
            if (in2 != null) {
                try {
                    in2.close();
                } catch (IOException e5) {
                    Log.d("[AppUpgradeService]", "Exception ==" + e5.getMessage());
                }
            }
            throw th;
        }
        return str;
    }

    private String culMD5(ByteBuffer buffer) {
        if (buffer == null) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(buffer);
            byte[] tmp = md.digest();
            char[] str = new char[32];
            int k = MSG_FILE_DOWNLOADED;
            for (int i = MSG_FILE_DOWNLOADED; i < 16; i += MSG_CONNECT_WIFI) {
                byte byte0 = tmp[i];
                int k2 = k + MSG_CONNECT_WIFI;
                str[k] = hexDigits[(byte0 >>> 4) & 15];
                k = k2 + MSG_CONNECT_WIFI;
                str[k2] = hexDigits[byte0 & 15];
            }
            String s = new String(str);
            return s;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void uninstallApkSilent(Context mContext2, String packName) {
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<uninstallApkSilent> packName = " + packName);
        try {
            Intent intent = new Intent();
            intent.setFlags(268435456);
            PendingIntent sender = PendingIntent.getActivity(mContext2, MSG_FILE_DOWNLOADED, intent, FLAG_MUTABLE);
            PackageInstaller mPackageInstaller = mContext2.getPackageManager().getPackageInstaller();
            mPackageInstaller.uninstall(packName, sender.getIntentSender());
        } catch (Exception e) {
            Log.d("[AppUpgradeService]", "[AppUpgradeService]<uninstallApkSilent> Exception = " + e);
        }
    }

    private PackageInstaller createInstaller(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInstaller packageInstaller = pm.getPackageInstaller();
        packageInstaller.registerSessionCallback(new PackageInstaller.SessionCallback() { // from class: tv.upgrade.service.AppUpgradeService.10
            @Override // android.content.pm.PackageInstaller.SessionCallback
            public void onCreated(int sessionId) {
            }

            @Override // android.content.pm.PackageInstaller.SessionCallback
            public void onBadgingChanged(int sessionId) {
            }

            @Override // android.content.pm.PackageInstaller.SessionCallback
            public void onActiveChanged(int sessionId, boolean active) {
            }

            @Override // android.content.pm.PackageInstaller.SessionCallback
            public void onProgressChanged(int sessionId, float progress) {
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<createInstaller> onProgressChanged: progress " + progress + "  session id: " + sessionId);
            }

            @Override // android.content.pm.PackageInstaller.SessionCallback
            public void onFinished(int sessionId, boolean success) {
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<createInstaller> onFinished: success = " + success + ",  session id: " + sessionId);
            }
        });
        return packageInstaller;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean deleteSingleFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Log.d("[AppUpgradeService]", "[AppUpgradeService]deleteSingleFile success");
                return DEBUG;
            }
            Log.d("[AppUpgradeService]", "[AppUpgradeService]deleteSingleFile false");
            return false;
        }
        Log.d("[AppUpgradeService]", "[AppUpgradeService]deleteSingleFile  flie error false");
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean installSingleApkSilence(Context context, String fileName, String pkgName) throws IOException {
        this.logcatCloseSignal = MSG_INSTALL_RESULT;
        String filePath = PATH_SAVE_SINGLE + fileName;
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<installApkSilence> filePath = " + filePath);
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<installApkSilence> pkgName = " + pkgName);
        File file = new File(filePath);
        int sessionId = this.packageInstaller.createSession(this.sessionParams);
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<installApkSilence> create session id = " + sessionId + ", create for package = " + pkgName);
        PackageInstaller.Session mPkgSession = this.packageInstaller.openSession(sessionId);
        InputStream inStream = new FileInputStream(file);
        long sizeBytes = file.length();
        OutputStream mOStream = null;
        byte[] buffer = new byte[65536];
        try {
            mOStream = mPkgSession.openWrite(pkgName, 0L, -1L);
            while (true) {
                int c = inStream.read(buffer);
                if (c == MSG_FILE_REDOWNLOAD) {
                    break;
                }
                mOStream.write(buffer, MSG_FILE_DOWNLOADED, c);
                if (sizeBytes > 0) {
                    float fraction = c / sizeBytes;
                    mPkgSession.setStagingProgress(fraction);
                }
            }
            mOStream.flush();
            mPkgSession.fsync(mOStream);
        } catch (Exception e) {
            Log.d("[AppUpgradeService]", "[AppUpgradeService]<installApkSilence> Exception = " + e);
        } finally {
            cleanClose(inStream);
            cleanClose(mOStream);
        }
        Intent intent = new Intent(context, (Class<?>) InstallResultReceiver.class);
        intent.putExtra("packageName", pkgName);
        intent.putExtra("autoTest", this.autoTest);
        intent.putExtra("filePath", filePath);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, sessionId, intent, FLAG_MUTABLE);
        mPkgSession.commit(pendingIntent.getIntentSender());
        return DEBUG;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean installApkSilence(Context context, String fileName, String pkgName) throws IOException {
        this.logcatCloseSignal = MSG_INSTALL_RESULT;
        String filePath = PATH_SAVE + fileName;
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<installApkSilence> filePath = " + filePath);
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<installApkSilence> pkgName = " + pkgName);
        File file = new File(filePath);
        int sessionId = this.packageInstaller.createSession(this.sessionParams);
        Log.d("[AppUpgradeService]", "[AppUpgradeService]<installApkSilence> create session id = " + sessionId + ", create for package = " + pkgName);
        PackageInstaller.Session mPkgSession = this.packageInstaller.openSession(sessionId);
        InputStream inStream = new FileInputStream(file);
        long sizeBytes = file.length();
        OutputStream mOStream = null;
        byte[] buffer = new byte[65536];
        try {
            mOStream = mPkgSession.openWrite(pkgName, 0L, -1L);
            while (true) {
                int c = inStream.read(buffer);
                if (c == MSG_FILE_REDOWNLOAD) {
                    break;
                }
                mOStream.write(buffer, MSG_FILE_DOWNLOADED, c);
                if (sizeBytes > 0) {
                    float fraction = c / sizeBytes;
                    mPkgSession.setStagingProgress(fraction);
                }
            }
            mOStream.flush();
            mPkgSession.fsync(mOStream);
        } catch (Exception e) {
            Log.d("[AppUpgradeService]", "[AppUpgradeService]<installApkSilence> Exception = " + e);
        } finally {
            cleanClose(inStream);
            cleanClose(mOStream);
        }
        Intent intent = new Intent(context, (Class<?>) InstallResultReceiver.class);
        intent.putExtra("packageName", pkgName);
        intent.putExtra("autoTest", this.autoTest);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, sessionId, intent, FLAG_MUTABLE);
        mPkgSession.commit(pendingIntent.getIntentSender());
        if (this.autoTest) {
            this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, MSG_INSTALL_RESULT, new MessageObj(fileName, pkgName, "", MSG_FILE_DOWNLOADED)), 90000L);
            return DEBUG;
        }
        return DEBUG;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static InputStream cleanClose(InputStream in) {
        if (in != null) {
            try {
                in.close();
                return null;
            } catch (Exception e) {
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<cleanClose> InputStream Exception = " + e);
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    private static OutputStream cleanClose(OutputStream out) {
        if (out != null) {
            try {
                out.close();
                return null;
            } catch (Exception e) {
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<cleanClose> OutputStream Exception = " + e);
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    private static FileInputStream cleanClose(FileInputStream in) {
        if (in != null) {
            try {
                in.close();
                return null;
            } catch (Exception e) {
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<cleanClose> FileInputStream Exception = " + e);
                return null;
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static FileOutputStream cleanClose(FileOutputStream out) {
        if (out != null) {
            try {
                out.close();
                return null;
            } catch (Exception e) {
                Log.d("[AppUpgradeService]", "[AppUpgradeService]<cleanClose> FileOutputStream Exception = " + e);
                return null;
            }
        }
        return null;
    }

    public class TrustAnyHostnameVerifier implements HostnameVerifier {
        public TrustAnyHostnameVerifier() {
        }

        @Override // javax.net.ssl.HostnameVerifier
        public boolean verify(String hostname, SSLSession session) {
            Log.d("[AppUpgradeService]", " Hostname : " + hostname + " ,Update_url : " + AppUpgradeService.UPDATE_URL);
            if (AppUpgradeService.UPDATE_URL.contains(hostname)) {
                Log.d("[AppUpgradeService]", " Host verify successful.");
                return AppUpgradeService.DEBUG;
            }
            Log.d("[AppUpgradeService]", "Host verify fail .");
            return false;
        }
    }

    private boolean isAnyNetworkConnect() {
        ConnectivityManager connMan = (ConnectivityManager) getSystemService(ConnectivityManager.class);
        Network[] networks = connMan.getAllNetworks();
        int length = networks.length;
        for (int i = MSG_FILE_DOWNLOADED; i < length; i += MSG_CONNECT_WIFI) {
            Network network = networks[i];
            NetworkInfo networkInfo = connMan.getNetworkInfo(network);
            if (networkInfo.isConnected()) {
                Log.d("[AppUpgradeService]", "Network is connected");
                return DEBUG;
            }
        }
        return false;
    }

    private NetworkInfo getActiveWifiNetworkInfo() {
        ConnectivityManager connMan = (ConnectivityManager) getSystemService(ConnectivityManager.class);
        Network[] networks = connMan.getAllNetworks();
        int length = networks.length;
        for (int i = MSG_FILE_DOWNLOADED; i < length; i += MSG_CONNECT_WIFI) {
            Network network = networks[i];
            NetworkInfo networkInfo = connMan.getNetworkInfo(network);
            if (networkInfo.isConnected() && networkInfo.getType() == MSG_CONNECT_WIFI) {
                return networkInfo;
            }
        }
        return null;
    }

    private boolean isNetworkConnected() {
        NetworkInfo netInfo = getActiveWifiNetworkInfo();
        if (netInfo == null) {
            Log.d("[AppUpgradeService]", "NetworkInfo is null; network is not connected");
            return false;
        }
        Log.d("[AppUpgradeService]", "NetworkInfo: " + netInfo.toString());
        if (netInfo.isConnected() && netInfo.getType() == MSG_CONNECT_WIFI) {
            WifiInfo currentConnection = this.mWifiManager.getConnectionInfo();
            Log.d("[AppUpgradeService]", "Connected to " + (currentConnection == null ? "nothing" : currentConnection.getSSID()));
            if (currentConnection != null && currentConnection.getSSID().equals(this.mWifiConfiguration.SSID)) {
                return DEBUG;
            }
        } else {
            Log.d("[AppUpgradeService]", "Network is not connected");
        }
        return false;
    }

    private String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sentWIFI(String WIFIssid, String WIFIpassword) {
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) getSystemService("wifi");
        }
        this.mWifiConfiguration.SSID = convertToQuotedString(WIFIssid);
        this.mWifiConfiguration.allowedKeyManagement.set(MSG_CONNECT_WIFI);
        if (WIFIpassword.length() < PSK_MIN_LENGTH) {
            Log.d("[AppUpgradeService]", "password too small");
            return;
        }
        if (WIFIpassword.matches("[0-9A-Fa-f]{64}")) {
            this.mWifiConfiguration.preSharedKey = WIFIpassword;
        } else {
            this.mWifiConfiguration.preSharedKey = '\"' + WIFIpassword + '\"';
        }
        if (!isNetworkConnected()) {
            this.mWifiManager.addNetwork(this.mWifiConfiguration);
            this.mWifiManager.connect(this.mWifiConfiguration, null);
        }
    }
}
