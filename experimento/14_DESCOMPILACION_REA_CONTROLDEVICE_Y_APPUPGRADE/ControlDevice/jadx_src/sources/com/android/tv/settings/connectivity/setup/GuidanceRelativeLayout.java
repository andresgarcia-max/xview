package com.android.tv.settings.connectivity.setup;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.control.device.R;
import com.control.device.R$styleable;

/* loaded from: classes.dex */
public class GuidanceRelativeLayout extends RelativeLayout {
    private float mTitleKeylinePercent;

    public GuidanceRelativeLayout(Context context) {
        this(context, null);
    }

    public GuidanceRelativeLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public GuidanceRelativeLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mTitleKeylinePercent = getKeyLinePercent(context);
    }

    private static float getKeyLinePercent(Context context) {
        TypedArray obtainStyledAttributes = context.getTheme().obtainStyledAttributes(R$styleable.LeanbackGuidedStepTheme);
        float f = obtainStyledAttributes.getFloat(45, 40.0f);
        obtainStyledAttributes.recycle();
        return f;
    }

    @Override // android.widget.RelativeLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int lineBaseline;
        super.onLayout(z, i, i2, i3, i4);
        TextView textView = (TextView) getRootView().findViewById(R.id.guidance_title);
        TextView textView2 = (TextView) getRootView().findViewById(R.id.guidance_description);
        ImageView imageView = (ImageView) getRootView().findViewById(R.id.guidance_icon);
        int measuredHeight = (int) ((getMeasuredHeight() * this.mTitleKeylinePercent) / 100.0f);
        if (textView == null || textView.getParent() != this) {
            return;
        }
        int i5 = measuredHeight - ((RelativeLayout.LayoutParams) textView.getLayoutParams()).topMargin;
        if (textView.getLineCount() > 1) {
            lineBaseline = textView.getLayout().getLineBaseline(1);
        } else {
            lineBaseline = textView.getLayout().getLineBaseline(0);
        }
        int i6 = i5 - lineBaseline;
        textView.offsetTopAndBottom(i6);
        if (textView2 != null && textView2.getParent() == this) {
            textView2.offsetTopAndBottom(i6);
        }
        if (imageView == null || imageView.getParent() != this) {
            return;
        }
        imageView.offsetTopAndBottom(i6);
    }
}
