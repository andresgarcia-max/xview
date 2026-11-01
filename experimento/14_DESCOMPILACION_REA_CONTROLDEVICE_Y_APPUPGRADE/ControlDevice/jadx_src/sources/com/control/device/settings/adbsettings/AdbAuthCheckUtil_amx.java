package com.control.device.settings.adbsettings;

import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/* loaded from: classes.dex */
public class AdbAuthCheckUtil_amx {
    private static int confusion = 99;

    public static void setQRAvailableNum(int i) {
        writeSysfs("/cache/recovery/last_QR", encrypt(String.valueOf(i + confusion)));
    }

    public static int getQRAvailableNum() {
        try {
            String readSysfs = readSysfs("/cache/recovery/last_QR");
            if (TextUtils.isEmpty(readSysfs)) {
                return -1;
            }
            return Integer.parseInt(decrypt(readSysfs)) - confusion;
        } catch (Exception unused) {
            return -1;
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

    public static void setQRFirstCheckTime(long j) {
        writeSysfs("/cache/recovery/last_QR_Time", encrypt(String.valueOf(j)));
    }

    private static String encrypt(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        char[] charArray = str.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            charArray[i] = (char) (charArray[i] ^ 't');
        }
        return new String(charArray);
    }

    private static String decrypt(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        char[] charArray = str.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            charArray[i] = (char) (charArray[i] ^ 't');
        }
        return new String(charArray);
    }

    private static boolean writeSysfs(String str, String str2) {
        FileWriter fileWriter;
        BufferedWriter bufferedWriter;
        BufferedWriter bufferedWriter2 = null;
        try {
            fileWriter = new FileWriter(str);
            try {
                try {
                    bufferedWriter = new BufferedWriter(fileWriter, 64);
                } catch (Exception e) {
                    e = e;
                }
            } catch (Throwable th) {
                th = th;
            }
        } catch (Exception e2) {
            e = e2;
            fileWriter = null;
        } catch (Throwable th2) {
            th = th2;
            fileWriter = null;
        }
        try {
            bufferedWriter.write(str2);
            bufferedWriter.flush();
            try {
                bufferedWriter.close();
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            try {
                fileWriter.close();
                return true;
            } catch (Exception e4) {
                e4.printStackTrace();
                return true;
            }
        } catch (Exception e5) {
            e = e5;
            bufferedWriter2 = bufferedWriter;
            e.printStackTrace();
            if (bufferedWriter2 != null) {
                try {
                    bufferedWriter2.close();
                } catch (Exception e6) {
                    e6.printStackTrace();
                }
            }
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (Exception e7) {
                    e7.printStackTrace();
                }
            }
            return false;
        } catch (Throwable th3) {
            th = th3;
            bufferedWriter2 = bufferedWriter;
            if (bufferedWriter2 != null) {
                try {
                    bufferedWriter2.close();
                } catch (Exception e8) {
                    e8.printStackTrace();
                }
            }
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                    throw th;
                } catch (Exception e9) {
                    e9.printStackTrace();
                    throw th;
                }
            }
            throw th;
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r5v0, types: [java.lang.String] */
    /* JADX WARN: Type inference failed for: r5v11 */
    /* JADX WARN: Type inference failed for: r5v3 */
    /* JADX WARN: Type inference failed for: r5v5, types: [java.io.BufferedReader] */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:61:0x0037 -> B:20:0x005f). Please report as a decompilation issue!!! */
    private static String readSysfs(String str) {
        FileReader fileReader;
        Throwable th;
        BufferedReader bufferedReader;
        String str2 = null;
        try {
            try {
                try {
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (Exception e) {
                e = e;
                bufferedReader = null;
                fileReader = null;
            } catch (Throwable th3) {
                fileReader = null;
                th = th3;
                str = 0;
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        if (!new File((String) str).exists()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        fileReader = new FileReader((String) str);
        try {
            bufferedReader = new BufferedReader(fileReader);
            while (true) {
                try {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    sb.append(readLine);
                } catch (Exception e3) {
                    e = e3;
                    e.printStackTrace();
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Exception e4) {
                            e4.printStackTrace();
                        }
                    }
                    if (fileReader != null) {
                        fileReader.close();
                    }
                    return str2;
                }
            }
            str2 = sb.toString();
            try {
                bufferedReader.close();
            } catch (Exception e5) {
                e5.printStackTrace();
            }
            fileReader.close();
        } catch (Exception e6) {
            e = e6;
            bufferedReader = null;
        } catch (Throwable th4) {
            th = th4;
            str = 0;
            if (str != 0) {
                try {
                    str.close();
                } catch (Exception e7) {
                    e7.printStackTrace();
                }
            }
            if (fileReader != null) {
                try {
                    fileReader.close();
                    throw th;
                } catch (Exception e8) {
                    e8.printStackTrace();
                    throw th;
                }
            }
            throw th;
        }
        return str2;
    }
}
