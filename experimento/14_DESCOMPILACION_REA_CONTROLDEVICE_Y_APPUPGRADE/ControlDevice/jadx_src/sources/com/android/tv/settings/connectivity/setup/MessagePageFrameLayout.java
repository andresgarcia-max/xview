package com.android.tv.settings.connectivity.setup;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.control.device.R;
import com.control.device.R$styleable;

/* loaded from: classes.dex */
public class MessagePageFrameLayout extends FrameLayout {
    private float mTitleKeylinePercent;

    public MessagePageFrameLayout(Context context) {
        this(context, null);
    }

    public MessagePageFrameLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MessagePageFrameLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mTitleKeylinePercent = getKeyLinePercent(context);
    }

    private static float getKeyLinePercent(Context context) {
        TypedArray obtainStyledAttributes = context.getTheme().obtainStyledAttributes(R$styleable.LeanbackGuidedStepTheme);
        float f = obtainStyledAttributes.getFloat(45, 40.0f);
        obtainStyledAttributes.recycle();
        return f;
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int lineBaseline;
        super.onLayout(z, i, i2, i3, i4);
        TextView textView = (TextView) getRootView().findViewById(R.id.status_text);
        View findViewById = getRootView().findViewById(R.id.message_content);
        int measuredHeight = (int) ((getMeasuredHeight() * this.mTitleKeylinePercent) / 100.0f);
        if (textView != null) {
            int i5 = measuredHeight - ((LinearLayout.LayoutParams) textView.getLayoutParams()).topMargin;
            if (textView.getLineCount() > 1) {
                lineBaseline = textView.getLayout().getLineBaseline(1);
            } else {
                lineBaseline = textView.getLayout().getLineBaseline(0);
            }
            findViewById.offsetTopAndBottom(i5 - lineBaseline);
        }
    }
}
