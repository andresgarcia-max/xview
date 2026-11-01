package tv.upgrade.service;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/* loaded from: classes.dex */
public class LogcatWrite {
    private static final String FILE = "[LogcatWrite]";
    private static final String LOG_FILE_PREFIX = "logcat_";
    private static final String LOG_FILE_SUFFIX = ".log";
    private static final String LOG_SAVE_FILE_SUFFIX = "_old.log";
    private static final String SETTING_FILE_PATH = "settingsAutoTestFilePath";
    private static final String TAG = "[AppUpgradeService]";
    private static volatile LogcatWrite instance = null;
    private static String sLogDirPath;
    private Context mContext;
    private File mLogcatFile;
    private volatile Process mLogcatProcess;

    private LogcatWrite() {
    }

    public static LogcatWrite getInstance() {
        if (instance == null) {
            synchronized (LogcatWrite.class) {
                if (instance == null) {
                    instance = new LogcatWrite();
                }
            }
        }
        return instance;
    }

    public LogcatWrite init(Context context, String logDirName) {
        if (context == null) {
            throw new IllegalArgumentException("LogcatWrite: init failed, context can not be null");
        }
        if (TextUtils.isEmpty(logDirName)) {
            throw new IllegalArgumentException("LogcatWrite: init failed, logDirName can not be null");
        }
        this.mContext = context.getApplicationContext();
        if (Environment.getExternalStorageState().equals("mounted")) {
            File documentFileDir = this.mContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (documentFileDir != null) {
                sLogDirPath = documentFileDir.getAbsolutePath() + File.separator + logDirName;
                Log.e(TAG, "LogcatWrite 1: sLogDirPath: " + sLogDirPath);
            } else {
                Log.e(TAG, "LogcatWrite: init LogcatWrite failed!");
            }
        } else {
            sLogDirPath = this.mContext.getFilesDir().getAbsolutePath() + File.separator + logDirName;
            Log.e(TAG, "LogcatWrite 2: sLogDirPath: " + sLogDirPath);
        }
        return this;
    }

    public void startWatch() {
        stopWatch();
        if (TextUtils.isEmpty(sLogDirPath)) {
            Log.e(TAG, "[LogcatWrite]can not watch log, the log dir can not be created");
            return;
        }
        this.mLogcatFile = createNewLogFile();
        if (this.mLogcatFile == null) {
            Log.e(TAG, "[LogcatWrite]can not create new log file");
            return;
        }
        Log.i(TAG, "[LogcatWrite]log file save path >>> " + this.mLogcatFile.getAbsolutePath());
        try {
            Process process = Runtime.getRuntime().exec("logcat -c");
            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Settings.Global.putString(this.mContext.getContentResolver(), SETTING_FILE_PATH, this.mLogcatFile.getAbsolutePath());
        String LOGCAT_FILTER_CMD = "logcat -v time *:V -f " + this.mLogcatFile.getAbsolutePath();
        try {
            this.mLogcatProcess = Runtime.getRuntime().exec(LOGCAT_FILTER_CMD);
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    public void stopWatch() {
        if (this.mLogcatProcess != null) {
            this.mLogcatProcess.destroy();
            this.mLogcatProcess = null;
        }
        if (this.mLogcatFile != null) {
            notifySystemToScan(this.mContext, this.mLogcatFile);
        }
    }

    private File createNewLogFile() {
        File logSaveDir = new File(sLogDirPath);
        if (!logSaveDir.exists()) {
            boolean mkdirs = logSaveDir.mkdirs();
            if (!mkdirs) {
                Log.e(TAG, "[LogcatWrite]create new save dir failed");
                return null;
            }
        } else {
            try {
                for (File saveFile : logSaveDir.listFiles()) {
                    String filePath = saveFile.getAbsolutePath();
                    Log.d(TAG, "[LogcatWrite]filePath: " + filePath);
                    if (filePath != null && !TextUtils.isEmpty(filePath)) {
                        if (saveFile.delete()) {
                            Log.d(TAG, "[LogcatWrite]saveFile: " + filePath + ", delete success");
                        } else {
                            Log.e(TAG, "[LogcatWrite]saveFile: " + filePath + ", delete error");
                        }
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "[LogcatWrite]delete, Exception = " + e);
            }
        }
        String logFileName = LOG_FILE_PREFIX + getCurrentTimeStr() + LOG_FILE_SUFFIX;
        File logFile = new File(logSaveDir, logFileName);
        try {
            boolean createRet = logFile.createNewFile();
            if (!createRet) {
                Log.e(TAG, "[LogcatWrite]create new log file failed");
                return null;
            }
            return logFile;
        } catch (IOException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    private static String getCurrentTimeStr() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
        return sdf.format(new Date(System.currentTimeMillis()));
    }

    private static void notifySystemToScan(Context context, File file) {
        if (context != null && file != null) {
            Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
            Uri uri = Uri.fromFile(file);
            intent.setData(uri);
            context.sendBroadcast(intent);
        }
    }
}
