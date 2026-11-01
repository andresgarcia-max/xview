package com.android.tv.twopanelsettings.slices;

import android.app.slice.SliceManager;
import android.content.Context;
import android.net.Uri;
import android.util.ArrayMap;
import com.android.tv.twopanelsettings.slices.PreferenceSliceLiveData;

/* loaded from: classes.dex */
public class ContextSingleton {
    private static ContextSingleton sInstance;
    private ArrayMap<Uri, PreferenceSliceLiveData.SliceLiveDataImpl> mSliceMap = new ArrayMap<>();
    private boolean mGivenFullSliceAccess = false;

    public static ContextSingleton getInstance() {
        if (sInstance == null) {
            sInstance = new ContextSingleton();
        }
        return sInstance;
    }

    private ContextSingleton() {
    }

    public PreferenceSliceLiveData.SliceLiveDataImpl getSliceLiveData(Context context, Uri uri) {
        if (!this.mSliceMap.containsKey(uri)) {
            this.mSliceMap.put(uri, PreferenceSliceLiveData.fromUri(context, uri));
        }
        return this.mSliceMap.get(uri);
    }

    public void grantFullAccess(Context context, Uri uri) {
        if (this.mGivenFullSliceAccess) {
            return;
        }
        ((SliceManager) context.getSystemService(SliceManager.class)).grantPermissionFromUser(uri, context.getApplicationContext().getPackageName(), true);
        this.mGivenFullSliceAccess = true;
    }
}
