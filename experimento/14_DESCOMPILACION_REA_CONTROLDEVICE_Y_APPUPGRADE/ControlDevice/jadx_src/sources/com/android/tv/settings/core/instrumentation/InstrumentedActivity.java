package com.android.tv.settings.core.instrumentation;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;

/* loaded from: classes.dex */
public abstract class InstrumentedActivity extends FragmentActivity {
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }
}
