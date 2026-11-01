package com.control.device.settings.MexicAmco.AMXUpdateAtvKey;

import android.content.Context;
import android.media.MediaDrm;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.android.tv.settings.adv.ISystemControlManager;
import com.android.tv.settings.adv.impl.SystemControlManager;
import java.io.File;
import java.io.FileInputStream;
import java.util.UUID;

/* loaded from: classes.dex */
public class WidevineKeyUtil {
    private Context mContext;
    private String mKeyCheckSum_http;
    private static final UUID WIDEVINE_UUID = new UUID(-1301668207276963122L, -6645017420763422227L);
    private static int KEY_MAX_LENGTH = 1048576;
    private final int KEY_TYPE = 17;
    private final String INVALIDATE_SYSTEM_ID = "24428";
    private String mKeyDir = "WidevineKey";
    ISystemControlManager mSystenControlManager = SystemControlManager.getInstance();
    private String mSerialNo = Build.getSerial();
    private String mFileName_zip = "widevine_" + this.mSerialNo + ".zip";
    private String mFileName_bin = "widevine_" + this.mSerialNo + "_enc.bin";
    private String mFileName_txt = "widevine_" + this.mSerialNo + "_hash.txt";

    public WidevineKeyUtil(Context context) {
        this.mContext = context;
    }

    public boolean isWidevineKeyValidated() {
        boolean z;
        synchronized (WidevineKeyUtil.class) {
            Log.i("WidevineKeyUtil", "isWidevineKeyValidated");
            z = false;
            try {
                String drmPropertyString = getDrmPropertyString("systemId", false);
                Log.i("WidevineKeyUtil", "isWidevineKeyValidated, systemid=" + drmPropertyString);
                if ("24428".equals(drmPropertyString)) {
                    z = true;
                } else {
                    Log.i("WidevineKeyUtil", "isWidevineKeyValidated, not expected 24428");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return z;
    }

    public boolean httpGetWidevineKey() {
        boolean z;
        String str;
        String host;
        synchronized (WidevineKeyUtil.class) {
            Log.i("WidevineKeyUtil", "httpGetWidevineKey");
            z = false;
            try {
                String str2 = this.mContext.getCacheDir().getAbsolutePath() + "/" + this.mKeyDir;
                File file = new File(str2);
                if (file.exists()) {
                    Log.i("WidevineKeyUtil", "httpGetWidevineKey, delete " + str2);
                    ComUtil.deleteDirByFile(file);
                }
                if (!file.exists()) {
                    Log.i("WidevineKeyUtil", "httpGetWidevineKey, create dir " + str2);
                    file.mkdirs();
                }
                str = str2 + "/" + this.mFileName_zip;
                host = HttpUtil.getHost();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!TextUtils.isEmpty(host) && host.contains("https")) {
                if (HttpUtil.downloadKey(ComUtil.getUrlBySN(host, "widevine_", this.mSerialNo), str)) {
                    if (new File(str).exists()) {
                        Log.i("WidevineKeyUtil", "httpGetWidevineKey, file=" + str + " exist");
                        z = true;
                    } else {
                        Log.i("WidevineKeyUtil", "httpGetWidevineKey, file=" + str + " does not exist");
                    }
                } else {
                    Log.i("WidevineKeyUtil", "httpGetWidevineKey, downloadKey failed");
                }
            }
            Log.e("WidevineKeyUtil", "httpGetWidevineKey, host error, host=" + host);
        }
        return z;
    }

    public boolean hashCheckWidevineKey() {
        boolean z;
        synchronized (WidevineKeyUtil.class) {
            Log.i("WidevineKeyUtil", "hashCheckWidevineKey");
            z = false;
            try {
                String str = this.mContext.getCacheDir().getAbsolutePath() + "/" + this.mKeyDir;
                String str2 = str + "/" + this.mFileName_zip;
                String str3 = str + "/" + this.mFileName_bin;
                String str4 = str + "/" + this.mFileName_txt;
                File file = new File(str3);
                if (file.exists()) {
                    Log.i("WidevineKeyUtil", "hashCheckWidevineKey, delete file=" + str3);
                    file.delete();
                }
                File file2 = new File(str4);
                if (file2.exists()) {
                    Log.i("WidevineKeyUtil", "hashCheckWidevineKey, delete file=" + str4);
                    file2.delete();
                }
                ComUtil.readByZipInputStream(str2, str);
                Thread.sleep(1000L);
                if (file.exists() && file2.exists()) {
                    this.mKeyCheckSum_http = ComUtil.readFileStr(str4);
                    Log.i("WidevineKeyUtil", "hashCheckWidevineKey, sum is " + this.mKeyCheckSum_http + " from server");
                    if (!TextUtils.isEmpty(this.mKeyCheckSum_http)) {
                        z = true;
                    }
                } else {
                    Log.i("WidevineKeyUtil", "hashCheckWidevineKey, some file is lost");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return z;
    }

    public boolean installWidevineKey() {
        synchronized (WidevineKeyUtil.class) {
            Log.i("WidevineKeyUtil", "installWidevineKey");
            FileInputStream fileInputStream = null;
            boolean z = false;
            try {
                try {
                    String str = (this.mContext.getCacheDir().getAbsolutePath() + "/" + this.mKeyDir) + "/" + this.mFileName_bin;
                    File file = new File(str);
                    if (!file.exists() || file.isDirectory()) {
                        Log.i("WidevineKeyUtil", "installWidevineKey, file=" + str + " does not exist");
                    } else {
                        FileInputStream fileInputStream2 = new FileInputStream(file);
                        try {
                            int available = fileInputStream2.available();
                            if (available > KEY_MAX_LENGTH) {
                                Log.i("WidevineKeyUtil", "path=" + file.getAbsolutePath() + ", length=" + available + ", is too large");
                                try {
                                    fileInputStream2.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return false;
                            }
                            byte[] bArr = new byte[available];
                            fileInputStream2.read(bArr);
                            fileInputStream2.close();
                            Log.i("WidevineKeyUtil", "path=" + file.getAbsolutePath() + ", length=" + available + ", length(hex)=" + String.format("%08x", Integer.valueOf(available)));
                            int[] iArr = new int[available];
                            for (int i = 0; i < available; i++) {
                                iArr[i] = bArr[i] & 255;
                            }
                            boolean writeWidevineKey = this.mSystenControlManager.writeWidevineKey(iArr, available);
                            if (writeWidevineKey) {
                                Thread.sleep(1000L);
                                String keyProvisionChecksum = this.mSystenControlManager.getKeyProvisionChecksum(17);
                                if (TextUtils.isEmpty(keyProvisionChecksum) || TextUtils.isEmpty(this.mKeyCheckSum_http) || !this.mKeyCheckSum_http.equalsIgnoreCase(keyProvisionChecksum)) {
                                    Log.i("WidevineKeyUtil", "installWidevineKey, check sum failed, expected:" + this.mKeyCheckSum_http + ", local:" + keyProvisionChecksum);
                                } else {
                                    Log.i("WidevineKeyUtil", "installWidevineKey, check sum success");
                                    z = true;
                                }
                            } else {
                                Log.i("WidevineKeyUtil", "installWidevineKey, bStoreResult=" + writeWidevineKey);
                            }
                            fileInputStream = fileInputStream2;
                        } catch (Exception e2) {
                            e = e2;
                            fileInputStream = fileInputStream2;
                            e.printStackTrace();
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (Exception e3) {
                                    e = e3;
                                    e.printStackTrace();
                                    return z;
                                }
                            }
                            return z;
                        } catch (Throwable th) {
                            th = th;
                            fileInputStream = fileInputStream2;
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (Exception e4) {
                                    e4.printStackTrace();
                                }
                            }
                            throw th;
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Exception e5) {
                            e = e5;
                            e.printStackTrace();
                            return z;
                        }
                    }
                } catch (Exception e6) {
                    e = e6;
                }
                return z;
            } catch (Throwable th2) {
                th = th2;
            }
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:12:0x0020, code lost:
    
        if (r1 == null) goto L17;
     */
    /* JADX WARN: Not initialized variable reg: 1, insn: 0x0025: MOVE (r0 I:??[OBJECT, ARRAY]) = (r1 I:??[OBJECT, ARRAY]), block:B:17:0x0025 */
    /* JADX WARN: Removed duplicated region for block: B:19:0x0028  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private java.lang.String getDrmPropertyString(java.lang.String r4, boolean r5) {
        /*
            r3 = this;
            r0 = 0
            android.media.MediaDrm r1 = new android.media.MediaDrm     // Catch: java.lang.Throwable -> L19 java.lang.Exception -> L1b
            java.util.UUID r2 = com.control.device.settings.MexicAmco.AMXUpdateAtvKey.WidevineKeyUtil.WIDEVINE_UUID     // Catch: java.lang.Throwable -> L19 java.lang.Exception -> L1b
            r1.<init>(r2)     // Catch: java.lang.Throwable -> L19 java.lang.Exception -> L1b
            if (r5 == 0) goto Lf
            java.lang.String r5 = "L3"
            r3.setSecurityLevel(r1, r5)     // Catch: java.lang.Exception -> L17 java.lang.Throwable -> L24
        Lf:
            java.lang.String r0 = r1.getPropertyString(r4)     // Catch: java.lang.Exception -> L17 java.lang.Throwable -> L24
        L13:
            r1.release()
            goto L23
        L17:
            r3 = move-exception
            goto L1d
        L19:
            r3 = move-exception
            goto L26
        L1b:
            r3 = move-exception
            r1 = r0
        L1d:
            r3.printStackTrace()     // Catch: java.lang.Throwable -> L24
            if (r1 == 0) goto L23
            goto L13
        L23:
            return r0
        L24:
            r3 = move-exception
            r0 = r1
        L26:
            if (r0 == 0) goto L2b
            r0.release()
        L2b:
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.control.device.settings.MexicAmco.AMXUpdateAtvKey.WidevineKeyUtil.getDrmPropertyString(java.lang.String, boolean):java.lang.String");
    }

    private void setSecurityLevel(MediaDrm mediaDrm, String str) {
        String propertyString = mediaDrm.getPropertyString("securityLevel");
        if (propertyString.equals(str)) {
            return;
        }
        Log.i("WidevineKeyUtil", "Changing security level from " + propertyString + " to " + str);
        mediaDrm.setPropertyString("securityLevel", str);
    }
}
