package com.android.tv.twopanelsettings.slices;

import android.content.Context;
import com.android.tv.twopanelsettings.R$string;

/* loaded from: classes.dex */
public final class CustomContentDescriptionUtil {
    public static String getFullSwitchContentDescription(Context context, String str, Boolean bool) {
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append(", ");
        if (bool.booleanValue()) {
            sb.append(context.getString(R$string.switch_on));
            sb.append(", ");
        } else {
            sb.append(context.getString(R$string.switch_off));
            sb.append(", ");
        }
        sb.append(context.getString(R$string.switch_talkback_identifier));
        return sb.toString();
    }
}
