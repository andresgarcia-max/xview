package com.control.device.settings.adbsettings;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import com.android.tv.settings.util.MarketUtil;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/* loaded from: classes.dex */
public class AdbAuthCheckUtil {
    private static int mSDK = SystemProperties.getInt("ro.build.version.sdk", 0);

    public boolean isNeedAuth() {
        int i;
        String readSysfs;
        if (MarketUtil.isMEXICO_MEGACABLE_OTT()) {
            return isNeedAuthForMega();
        }
        if (!whetherCanReadQr()) {
            return false;
        }
        try {
            readSysfs = readSysfs("/cache/recovery/last_QR");
        } catch (Exception e) {
            e.printStackTrace();
            i = 0;
        }
        if (readSysfs == null) {
            return true;
        }
        i = Integer.parseInt(decrypt(readSysfs)) - 99;
        if (i > 500 || i < 1) {
            return true;
        }
        writeSysfs("/cache/recovery/last_QR", encrypt(String.valueOf((i - 1) + 99)));
        return false;
    }

    private boolean whetherCanReadQr() {
        File file = new File("/cache/recovery/test");
        writeSysfs("/cache/recovery/test", encrypt("1"));
        if (!file.canRead()) {
            return false;
        }
        file.delete();
        return true;
    }

    public boolean isNeedAuthForMega() {
        int i;
        String readSysfs;
        long qRFirstCheckTime = getQRFirstCheckTime();
        long currentTimeMillis = System.currentTimeMillis();
        try {
            readSysfs = readSysfs("/cache/recovery/last_QR");
        } catch (Exception e) {
            e.printStackTrace();
            i = 0;
        }
        if (readSysfs == null) {
            return true;
        }
        i = Integer.parseInt(decrypt(readSysfs)) - 99;
        if (i <= 15 && i != 0 && i >= 1) {
            if (0 == qRFirstCheckTime) {
                return false;
            }
            if (qRFirstCheckTime >= 0) {
                long j = currentTimeMillis - qRFirstCheckTime;
                if (j <= 604800000 && j >= 0) {
                    writeSysfs("/cache/recovery/last_QR", encrypt(String.valueOf((i - 1) + 99)));
                    return false;
                }
            }
        }
        return true;
    }

    private static String decrypt(String str) {
        char[] charArray = str.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            charArray[i] = (char) (charArray[i] ^ 't');
        }
        return new String(charArray);
    }

    private String encrypt(String str) {
        char[] charArray = str.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            charArray[i] = (char) (charArray[i] ^ 't');
        }
        return new String(charArray);
    }

    private boolean writeSysfs(String str, String str2) {
        FileWriter fileWriter;
        Throwable th;
        BufferedWriter bufferedWriter;
        if (!new File(str).exists()) {
            try {
                new File(str).createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        new File(str).canWrite();
        FileWriter fileWriter2 = null;
        try {
            fileWriter = new FileWriter(str);
            try {
                bufferedWriter = new BufferedWriter(fileWriter, 64);
                try {
                    try {
                        bufferedWriter.write(str2);
                        try {
                            fileWriter.close();
                            bufferedWriter.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                        return true;
                    } finally {
                        bufferedWriter.close();
                        fileWriter.close();
                    }
                } catch (IOException unused) {
                    fileWriter2 = fileWriter;
                    if (fileWriter2 != null) {
                        try {
                            fileWriter2.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                            return false;
                        }
                    }
                    if (bufferedWriter != null) {
                        bufferedWriter.close();
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileWriter != null) {
                        try {
                            fileWriter.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                            throw th;
                        }
                    }
                    if (bufferedWriter != null) {
                        bufferedWriter.close();
                    }
                    throw th;
                }
            } catch (IOException unused2) {
                bufferedWriter = null;
            } catch (Throwable th3) {
                bufferedWriter = null;
                th = th3;
            }
        } catch (IOException unused3) {
            bufferedWriter = null;
        } catch (Throwable th4) {
            fileWriter = null;
            th = th4;
            bufferedWriter = null;
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:53:0x007b A[Catch: IOException -> 0x0077, TRY_LEAVE, TryCatch #1 {IOException -> 0x0077, blocks: (B:60:0x0073, B:53:0x007b), top: B:59:0x0073 }] */
    /* JADX WARN: Removed duplicated region for block: B:59:0x0073 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Type inference failed for: r4v1 */
    /* JADX WARN: Type inference failed for: r4v13 */
    /* JADX WARN: Type inference failed for: r4v3, types: [java.io.BufferedReader] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private static java.lang.String readSysfs(java.lang.String r4) {
        /*
            java.io.File r0 = new java.io.File
            r0.<init>(r4)
            boolean r0 = r0.exists()
            r1 = 0
            if (r0 != 0) goto Ld
            return r1
        Ld:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.io.FileReader r2 = new java.io.FileReader     // Catch: java.lang.Throwable -> L54 java.io.FileNotFoundException -> L57
            r2.<init>(r4)     // Catch: java.lang.Throwable -> L54 java.io.FileNotFoundException -> L57
            java.io.BufferedReader r4 = new java.io.BufferedReader     // Catch: java.lang.Throwable -> L4e java.io.FileNotFoundException -> L51
            r4.<init>(r2)     // Catch: java.lang.Throwable -> L4e java.io.FileNotFoundException -> L51
        L1c:
            java.lang.String r3 = r4.readLine()     // Catch: java.io.IOException -> L3c java.lang.Throwable -> L6f
            if (r3 == 0) goto L26
            r0.append(r3)     // Catch: java.io.IOException -> L3c java.lang.Throwable -> L6f
            goto L1c
        L26:
            r2.close()     // Catch: java.io.IOException -> L3c java.lang.Throwable -> L6f
            r4.close()     // Catch: java.io.IOException -> L3c java.lang.Throwable -> L6f
            java.lang.String r0 = r0.toString()     // Catch: java.io.IOException -> L3c java.lang.Throwable -> L6f
            r2.close()     // Catch: java.io.IOException -> L37
            r4.close()     // Catch: java.io.IOException -> L37
            goto L3b
        L37:
            r4 = move-exception
            r4.printStackTrace()
        L3b:
            return r0
        L3c:
            r0 = move-exception
            r0.printStackTrace()     // Catch: java.io.FileNotFoundException -> L4c java.lang.Throwable -> L6f
            r2.close()     // Catch: java.io.IOException -> L47
            r4.close()     // Catch: java.io.IOException -> L47
            goto L4b
        L47:
            r4 = move-exception
            r4.printStackTrace()
        L4b:
            return r1
        L4c:
            r0 = move-exception
            goto L5a
        L4e:
            r0 = move-exception
            r4 = r1
            goto L70
        L51:
            r0 = move-exception
            r4 = r1
            goto L5a
        L54:
            r0 = move-exception
            r4 = r1
            goto L71
        L57:
            r0 = move-exception
            r4 = r1
            r2 = r4
        L5a:
            r0.printStackTrace()     // Catch: java.lang.Throwable -> L6f
            if (r2 == 0) goto L65
            r2.close()     // Catch: java.io.IOException -> L63
            goto L65
        L63:
            r4 = move-exception
            goto L6b
        L65:
            if (r4 == 0) goto L6e
            r4.close()     // Catch: java.io.IOException -> L63
            goto L6e
        L6b:
            r4.printStackTrace()
        L6e:
            return r1
        L6f:
            r0 = move-exception
        L70:
            r1 = r2
        L71:
            if (r1 == 0) goto L79
            r1.close()     // Catch: java.io.IOException -> L77
            goto L79
        L77:
            r4 = move-exception
            goto L7f
        L79:
            if (r4 == 0) goto L82
            r4.close()     // Catch: java.io.IOException -> L77
            goto L82
        L7f:
            r4.printStackTrace()
        L82:
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.control.device.settings.adbsettings.AdbAuthCheckUtil.readSysfs(java.lang.String):java.lang.String");
    }

    public void stopAdbService(Context context) {
        if (mSDK >= 23) {
            Settings.Global.putInt(context.getContentResolver(), "adb_enabled", 0);
        } else {
            SystemProperties.set("ctl.stop", "adbd");
        }
        String str = SystemProperties.get("persist.sys.usb.config", "none");
        if (str.contains("adb")) {
            if (str.contains("mtp")) {
                SystemProperties.set("persist.sys.usb.config", "mtp");
            } else {
                SystemProperties.set("persist.sys.usb.config", "none");
            }
        }
        try {
            Runtime.getRuntime().exec("stop adbd");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static long getQRFirstCheckTime() {
        try {
            String readSysfs = readSysfs("/cache/recovery/last_QR_Time");
            if (TextUtils.isEmpty(readSysfs)) {
                return 0L;
            }
            return Long.parseLong(decrypt(readSysfs));
        } catch (Exception unused) {
            return 0L;
        }
    }
}
