package device.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/* loaded from: classes.dex */
public class IntentUtils {
    public static boolean canExecuteIntent(Context context, Intent intent) {
        return context.getPackageManager().queryIntentActivities(intent, 65536).size() > 0;
    }

    public static boolean safeStartActivityIntent(Context context, Intent intent) {
        if (canExecuteIntent(context, intent)) {
            context.startActivity(intent);
            return true;
        }
        Log.e("IntentUtils", "" + intent.toString() + " CANNOT Execute");
        return false;
    }
}
