package com.android.tv.twopanelsettings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import androidx.core.content.ContextCompat;

/* loaded from: classes.dex */
public class IconUtil {
    public static Drawable getCompoundIcon(Context context, Drawable drawable) {
        Drawable drawable2 = ContextCompat.getDrawable(context, R$drawable.compound_icon_background);
        Resources resources = context.getResources();
        try {
            drawable.setTintList(ColorStateList.createFromXml(resources, resources.getXml(R$color.two_panel_preference_icon_color)));
        } catch (Exception e) {
            Log.e("IconUtil", "Cannot set tint", e);
        }
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{drawable2, drawable});
        layerDrawable.setLayerGravity(0, 17);
        layerDrawable.setLayerGravity(1, 17);
        layerDrawable.setLayerInset(1, 12, 12, 12, 12);
        return layerDrawable;
    }
}
