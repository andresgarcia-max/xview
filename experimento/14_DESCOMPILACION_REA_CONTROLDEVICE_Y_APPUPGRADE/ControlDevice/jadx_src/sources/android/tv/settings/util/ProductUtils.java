package android.tv.settings.util;

import android.os.Build;
import android.os.SystemProperties;
import android.text.TextUtils;

/* loaded from: classes.dex */
public class ProductUtils {
    public static final String PRODUCT_MODEL = Build.MODEL;
    public static final String HARDWARE_INFO = SystemProperties.get("ro.product.info.hardware");

    public static boolean isB866V5W11() {
        String str = HARDWARE_INFO;
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return str.contains("STBALC");
    }
}
