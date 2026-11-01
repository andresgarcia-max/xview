package com.android.tv.settings.connectivity.util;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;
import android.widget.TextView;
import androidx.leanback.widget.ImeKeyMonitor;

/* loaded from: classes.dex */
public class SettingsGuidedActionEditText extends EditText implements ImeKeyMonitor {
    private ImeKeyMonitor.ImeKeyListener mKeyListener;

    public SettingsGuidedActionEditText(Context context) {
        super(context);
    }

    public SettingsGuidedActionEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public SettingsGuidedActionEditText(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override // androidx.leanback.widget.ImeKeyMonitor
    public void setImeKeyListener(ImeKeyMonitor.ImeKeyListener imeKeyListener) {
        this.mKeyListener = imeKeyListener;
    }

    @Override // android.widget.TextView, android.view.View
    public boolean onKeyPreIme(int i, KeyEvent keyEvent) {
        ImeKeyMonitor.ImeKeyListener imeKeyListener = this.mKeyListener;
        boolean onKeyPreIme = imeKeyListener != null ? imeKeyListener.onKeyPreIme(this, i, keyEvent) : false;
        return !onKeyPreIme ? super.onKeyPreIme(i, keyEvent) : onKeyPreIme;
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setClassName((isFocused() ? EditText.class : TextView.class).getName());
    }

    @Override // android.widget.TextView, android.view.View
    protected void onFocusChanged(boolean z, int i, Rect rect) {
        super.onFocusChanged(z, i, rect);
        if (z) {
            return;
        }
        setFocusable(false);
    }
}
