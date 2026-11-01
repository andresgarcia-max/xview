package tv.upgrade.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import tv.upgrade.service.ReadFile;

/* loaded from: classes.dex */
public class ReadFileServer extends Service {
    private static final String FILE = "[ReadFileServer]";
    private static final String LOG_DIR = "logcat";
    private static final String LOG_FILE_PREFIX = "logcat_";
    private static final String SETTING_FILE_PATH = "settingsAutoTestFilePath";
    private static final String TAG = "ReadFileServer";
    private volatile String filePath;
    private volatile InputStream inStream = null;

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "[ReadFileServer]<onBind> ");
        return new ReadFileBinder();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean get_file() {
        this.filePath = Settings.Global.getString(getContentResolver(), SETTING_FILE_PATH);
        Log.d(TAG, "[ReadFileServer]filePath: " + this.filePath);
        return !TextUtils.isEmpty(this.filePath);
    }

    private class ReadFileBinder extends ReadFile.Stub {
        private ReadFileBinder() {
        }

        @Override // tv.upgrade.service.ReadFile
        public int readLogFile(byte[] buffer) throws RemoteException {
            if (ReadFileServer.this.inStream == null) {
                if (TextUtils.isEmpty(ReadFileServer.this.filePath)) {
                    ReadFileServer.this.get_file();
                }
                if (TextUtils.isEmpty(ReadFileServer.this.filePath)) {
                    return -2;
                }
                try {
                    ReadFileServer.this.inStream = new FileInputStream(ReadFileServer.this.filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (ReadFileServer.this.inStream != null) {
                try {
                    int c = ReadFileServer.this.inStream.read(buffer);
                    if (c == -1) {
                        ReadFileServer.this.inStream.close();
                        ReadFileServer.this.inStream = null;
                        return c;
                    }
                    return c;
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            return -2;
        }

        @Override // tv.upgrade.service.ReadFile
        public String getFileName() throws RemoteException {
            String fileName = "";
            if (!TextUtils.isEmpty(ReadFileServer.this.filePath) || ReadFileServer.this.get_file()) {
                if (!TextUtils.isEmpty(ReadFileServer.this.filePath)) {
                    fileName = ReadFileServer.this.filePath.substring(ReadFileServer.this.filePath.indexOf(ReadFileServer.LOG_FILE_PREFIX), ReadFileServer.this.filePath.length());
                }
                Log.d(ReadFileServer.TAG, "[ReadFileServer]fileName: " + fileName);
                return fileName;
            }
            return "";
        }
    }

    @Override // android.app.Service
    public void onCreate() {
        Log.d(TAG, "PowerService onCreate");
        super.onCreate();
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override // android.app.Service
    public void onDestroy() {
        try {
            if (this.inStream != null) {
                this.inStream.close();
                this.inStream = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
