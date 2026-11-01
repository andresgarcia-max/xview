package com.android.tv.twopanelsettings;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

/* loaded from: classes.dex */
public class TwoPanelScrollView extends HorizontalScrollView {
    @Override // android.widget.HorizontalScrollView
    public boolean arrowScroll(int i) {
        return false;
    }

    public TwoPanelScrollView(Context context) {
        super(context);
    }

    public TwoPanelScrollView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }
}
