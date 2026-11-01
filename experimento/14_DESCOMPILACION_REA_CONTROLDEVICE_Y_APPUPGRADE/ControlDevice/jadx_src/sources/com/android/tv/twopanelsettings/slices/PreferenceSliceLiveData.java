package com.android.tv.twopanelsettings.slices;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import androidx.slice.Slice;
import androidx.slice.SliceViewManager;
import java.util.concurrent.atomic.AtomicBoolean;

/* loaded from: classes.dex */
public final class PreferenceSliceLiveData {
    static SliceLiveDataImpl fromUri(Context context, Uri uri) {
        return new SliceLiveDataImpl(context.getApplicationContext(), uri);
    }

    static class SliceLiveDataImpl extends MutableLiveData<Slice> {
        final SliceViewManager mSliceViewManager;
        Uri mUri;
        final AtomicBoolean mUpdatePending = new AtomicBoolean(false);
        private final Runnable mUpdateSlice = new Runnable() { // from class: com.android.tv.twopanelsettings.slices.PreferenceSliceLiveData.SliceLiveDataImpl.1
            @Override // java.lang.Runnable
            public void run() {
                try {
                    SliceLiveDataImpl sliceLiveDataImpl = SliceLiveDataImpl.this;
                    Uri uri = sliceLiveDataImpl.mUri;
                    Slice bindSlice = uri != null ? sliceLiveDataImpl.mSliceViewManager.bindSlice(uri) : sliceLiveDataImpl.mSliceViewManager.bindSlice(sliceLiveDataImpl.mIntent);
                    SliceLiveDataImpl sliceLiveDataImpl2 = SliceLiveDataImpl.this;
                    if (sliceLiveDataImpl2.mUri == null && bindSlice != null) {
                        sliceLiveDataImpl2.mUri = bindSlice.getUri();
                        SliceLiveDataImpl sliceLiveDataImpl3 = SliceLiveDataImpl.this;
                        sliceLiveDataImpl3.mSliceViewManager.registerSliceCallback(sliceLiveDataImpl3.mUri, sliceLiveDataImpl3.mSliceCallback);
                    }
                    SliceLiveDataImpl.this.postValue(bindSlice);
                } catch (Exception e) {
                    Log.e("SliceLiveData", "Error binding slice", e);
                    SliceLiveDataImpl.this.postValue(null);
                }
            }
        };
        final SliceViewManager.SliceCallback mSliceCallback = new SliceViewManager.SliceCallback() { // from class: com.android.tv.twopanelsettings.slices.PreferenceSliceLiveData.SliceLiveDataImpl.2
            @Override // androidx.slice.SliceViewManager.SliceCallback
            public void onSliceUpdated(Slice slice) {
                SliceLiveDataImpl.this.postValue(slice);
            }
        };
        final Intent mIntent = null;

        SliceLiveDataImpl(Context context, Uri uri) {
            this.mSliceViewManager = SliceViewManager.getInstance(context);
            this.mUri = uri;
        }

        @Override // androidx.lifecycle.LiveData
        protected void onActive() {
            AsyncTask.execute(this.mUpdateSlice);
            Uri uri = this.mUri;
            if (uri != null) {
                this.mSliceViewManager.registerSliceCallback(uri, this.mSliceCallback);
            }
        }

        @Override // androidx.lifecycle.LiveData
        protected void onInactive() {
            Uri uri = this.mUri;
            if (uri != null) {
                this.mSliceViewManager.unregisterSliceCallback(uri, this.mSliceCallback);
            }
        }

        @Override // androidx.lifecycle.MutableLiveData, androidx.lifecycle.LiveData
        public void setValue(Slice slice) {
            this.mUpdatePending.set(true);
            super.setValue((SliceLiveDataImpl) slice);
        }
    }
}
