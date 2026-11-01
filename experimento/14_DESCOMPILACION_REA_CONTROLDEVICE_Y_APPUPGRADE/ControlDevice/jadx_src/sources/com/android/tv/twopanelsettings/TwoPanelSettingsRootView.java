package com.android.tv.twopanelsettings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

/* loaded from: classes.dex */
public class TwoPanelSettingsRootView extends FrameLayout {
    private View.OnKeyListener mOnBackKeyListener;

    public TwoPanelSettingsRootView(Context context) {
        super(context);
    }

    public TwoPanelSettingsRootView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public TwoPanelSettingsRootView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public void setOnBackKeyListener(View.OnKeyListener onKeyListener) {
        this.mOnBackKeyListener = onKeyListener;
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        View.OnKeyListener onKeyListener;
        return (((keyEvent.getKeyCode() == 4 || keyEvent.getKeyCode() == 19 || keyEvent.getKeyCode() == 20 || keyEvent.getKeyCode() == 21 || keyEvent.getKeyCode() == 22) && (onKeyListener = this.mOnBackKeyListener) != null) ? onKeyListener.onKey(this, keyEvent.getKeyCode(), keyEvent) : false) || super.dispatchKeyEvent(keyEvent);
    }
}
