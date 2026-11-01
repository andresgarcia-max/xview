package com.control.device.settings.MexicAmco.AMXUpdateAtvKey;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import java.io.File;
import java.security.MessageDigest;

/* loaded from: classes.dex */
public class ComUtil {
    private static int mWidevineValidated = -1;

    public static void CheckKeyIsInstalled(Context context) {
        if (context == null) {
            Log.d("AMXUpdateAtvKey.ComUtil", "context is null");
        }
        int i = Settings.Secure.getInt(context.getApplicationContext().getContentResolver(), "tv_user_setup_complete", 0);
        Log.d("AMXUpdateAtvKey.ComUtil", "CheckKeyIsInstalled, TvSetup=" + i);
        if (i == 0) {
            if (mWidevineValidated == -1) {
                Log.d("AMXUpdateAtvKey.ComUtil", "CheckKeyIsInstalled, status not check, check first time");
                mWidevineValidated = new WidevineKeyUtil(context.getApplicationContext()).isWidevineKeyValidated() ? 1 : 0;
            }
            Log.d("AMXUpdateAtvKey.ComUtil", "CheckKeyIsInstalled, widevine key validate " + mWidevineValidated);
            if (mWidevineValidated != 1) {
                Intent intent = new Intent();
                intent.addCategory("android.intent.category.DEFAULT");
                intent.addFlags(268435456);
                intent.setComponent(new ComponentName(context.getPackageName(), UpdateAtvKeyActivity.class.getName()));
                context.startActivity(intent);
            }
        }
    }

    public static void assertTrue(String str, boolean z) throws Exception {
        if (z) {
            return;
        }
        if (str != null) {
            throw new Exception(str);
        }
        throw new Exception();
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:39:0x0100 A[Catch: Exception -> 0x0104, TRY_ENTER, TRY_LEAVE, TryCatch #25 {Exception -> 0x0104, blocks: (B:39:0x0100, B:115:0x00a3), top: B:2:0x0001 }] */
    /* JADX WARN: Removed duplicated region for block: B:44:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:45:0x00f6 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:50:0x00ec A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:55:0x00e2 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:60:0x00d8 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:72:0x0134 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:79:? A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:80:0x012a A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:85:0x0120 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:90:0x0116 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:95:0x010c A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Type inference failed for: r3v0 */
    /* JADX WARN: Type inference failed for: r3v1 */
    /* JADX WARN: Type inference failed for: r3v10 */
    /* JADX WARN: Type inference failed for: r3v11 */
    /* JADX WARN: Type inference failed for: r3v12, types: [java.io.BufferedInputStream] */
    /* JADX WARN: Type inference failed for: r3v13 */
    /* JADX WARN: Type inference failed for: r3v2, types: [java.io.BufferedInputStream] */
    /* JADX WARN: Type inference failed for: r3v3, types: [java.io.BufferedInputStream] */
    /* JADX WARN: Type inference failed for: r3v4 */
    /* JADX WARN: Type inference failed for: r3v5 */
    /* JADX WARN: Type inference failed for: r3v6 */
    /* JADX WARN: Type inference failed for: r3v7 */
    /* JADX WARN: Type inference failed for: r3v8 */
    /* JADX WARN: Type inference failed for: r3v9 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static void readByZipInputStream(java.lang.String r9, java.lang.String r10) {
        /*
            Method dump skipped, instructions count: 317
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.control.device.settings.MexicAmco.AMXUpdateAtvKey.ComUtil.readByZipInputStream(java.lang.String, java.lang.String):void");
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:35:0x0074 A[Catch: Exception -> 0x003c, TRY_ENTER, TRY_LEAVE, TryCatch #11 {Exception -> 0x003c, blocks: (B:19:0x0038, B:35:0x0074), top: B:3:0x0003 }] */
    /* JADX WARN: Removed duplicated region for block: B:36:0x006a A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:41:0x0060 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:55:0x0090 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:62:? A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:63:0x0086 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:68:0x007c A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Type inference failed for: r6v0, types: [java.lang.String] */
    /* JADX WARN: Type inference failed for: r6v10, types: [java.io.InputStreamReader] */
    /* JADX WARN: Type inference failed for: r6v12 */
    /* JADX WARN: Type inference failed for: r6v14, types: [java.io.InputStreamReader, java.io.Reader] */
    /* JADX WARN: Type inference failed for: r6v4 */
    /* JADX WARN: Type inference failed for: r6v9 */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:78:0x003d -> B:20:0x0077). Please report as a decompilation issue!!! */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static java.lang.String readFileStr(java.lang.String r6) {
        /*
            java.lang.String r0 = ""
            r1 = 0
            java.io.FileInputStream r2 = new java.io.FileInputStream     // Catch: java.lang.Throwable -> L52 java.lang.Exception -> L56
            r2.<init>(r6)     // Catch: java.lang.Throwable -> L52 java.lang.Exception -> L56
            java.io.InputStreamReader r6 = new java.io.InputStreamReader     // Catch: java.lang.Throwable -> L4b java.lang.Exception -> L4f
            r6.<init>(r2)     // Catch: java.lang.Throwable -> L4b java.lang.Exception -> L4f
            java.io.BufferedReader r3 = new java.io.BufferedReader     // Catch: java.lang.Throwable -> L43 java.lang.Exception -> L46
            r3.<init>(r6)     // Catch: java.lang.Throwable -> L43 java.lang.Exception -> L46
        L12:
            java.lang.String r1 = r3.readLine()     // Catch: java.lang.Exception -> L41 java.lang.Throwable -> L78
            if (r1 == 0) goto L28
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> L41 java.lang.Throwable -> L78
            r4.<init>()     // Catch: java.lang.Exception -> L41 java.lang.Throwable -> L78
            r4.append(r0)     // Catch: java.lang.Exception -> L41 java.lang.Throwable -> L78
            r4.append(r1)     // Catch: java.lang.Exception -> L41 java.lang.Throwable -> L78
            java.lang.String r0 = r4.toString()     // Catch: java.lang.Exception -> L41 java.lang.Throwable -> L78
            goto L12
        L28:
            r2.close()     // Catch: java.lang.Exception -> L2c
            goto L30
        L2c:
            r1 = move-exception
            r1.printStackTrace()
        L30:
            r6.close()     // Catch: java.lang.Exception -> L34
            goto L38
        L34:
            r6 = move-exception
            r6.printStackTrace()
        L38:
            r3.close()     // Catch: java.lang.Exception -> L3c
            goto L77
        L3c:
            r6 = move-exception
            r6.printStackTrace()
            goto L77
        L41:
            r1 = move-exception
            goto L5b
        L43:
            r0 = move-exception
            r3 = r1
            goto L79
        L46:
            r3 = move-exception
            r5 = r3
            r3 = r1
            r1 = r5
            goto L5b
        L4b:
            r0 = move-exception
            r6 = r1
            r3 = r6
            goto L79
        L4f:
            r6 = move-exception
            r3 = r1
            goto L59
        L52:
            r0 = move-exception
            r6 = r1
            r3 = r6
            goto L7a
        L56:
            r6 = move-exception
            r2 = r1
            r3 = r2
        L59:
            r1 = r6
            r6 = r3
        L5b:
            r1.printStackTrace()     // Catch: java.lang.Throwable -> L78
            if (r2 == 0) goto L68
            r2.close()     // Catch: java.lang.Exception -> L64
            goto L68
        L64:
            r1 = move-exception
            r1.printStackTrace()
        L68:
            if (r6 == 0) goto L72
            r6.close()     // Catch: java.lang.Exception -> L6e
            goto L72
        L6e:
            r6 = move-exception
            r6.printStackTrace()
        L72:
            if (r3 == 0) goto L77
            r3.close()     // Catch: java.lang.Exception -> L3c
        L77:
            return r0
        L78:
            r0 = move-exception
        L79:
            r1 = r2
        L7a:
            if (r1 == 0) goto L84
            r1.close()     // Catch: java.lang.Exception -> L80
            goto L84
        L80:
            r1 = move-exception
            r1.printStackTrace()
        L84:
            if (r6 == 0) goto L8e
            r6.close()     // Catch: java.lang.Exception -> L8a
            goto L8e
        L8a:
            r6 = move-exception
            r6.printStackTrace()
        L8e:
            if (r3 == 0) goto L98
            r3.close()     // Catch: java.lang.Exception -> L94
            goto L98
        L94:
            r6 = move-exception
            r6.printStackTrace()
        L98:
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.control.device.settings.MexicAmco.AMXUpdateAtvKey.ComUtil.readFileStr(java.lang.String):java.lang.String");
    }

    public static void deleteDirByFile(File file) {
        if (file.isDirectory()) {
            for (File file2 : file.listFiles()) {
                deleteDirByFile(file2);
            }
            file.delete();
            return;
        }
        file.delete();
    }

    public static String getUrlBySN(String str, String str2, String str3) {
        if (str == null || str2 == null || str3 == null) {
            Log.e("AMXUpdateAtvKey.ComUtil", "error, host=" + str + ", prefix=" + str2 + ", sn=" + str3);
            return null;
        }
        try {
            if (!str.endsWith("/")) {
                str = str + "/";
            }
            MessageDigest messageDigest = MessageDigest.getInstance("SHA256");
            messageDigest.update(str3.getBytes("UTF-8"));
            byte[] digest = messageDigest.digest();
            StringBuffer stringBuffer = new StringBuffer();
            for (byte b : digest) {
                stringBuffer.append(String.format("%02X", Byte.valueOf(b)));
            }
            String stringBuffer2 = stringBuffer.toString();
            Log.d("AMXUpdateAtvKey.ComUtil", "sum=" + stringBuffer2);
            return str + str2 + stringBuffer2 + ".zip";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
