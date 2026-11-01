package com.control.device.settings.remote.util;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.VideoView;
import com.control.device.util.DebugUtil;

/* loaded from: classes.dex */
public class VideoUtils {

    interface videoPlayCallback {
        void onCompletion();
    }

    static class Resource {
        int count;
        int resId;

        public Resource(int i, int i2) {
            this.resId = i;
            this.count = i2;
        }
    }

    public static Uri video2Uri(Activity activity, int i) {
        return Uri.parse("android.resource://" + activity.getPackageName() + "/" + i);
    }

    public static void playVideoWithCallback(final VideoView videoView, Uri uri, final int i, final videoPlayCallback videoplaycallback) {
        final int[] iArr = {0};
        videoView.setVideoURI(uri);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() { // from class: com.control.device.settings.remote.util.VideoUtils.1
            @Override // android.media.MediaPlayer.OnPreparedListener
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() { // from class: com.control.device.settings.remote.util.VideoUtils.2
            @Override // android.media.MediaPlayer.OnCompletionListener
            public void onCompletion(MediaPlayer mediaPlayer) {
                int[] iArr2 = iArr;
                iArr2[0] = iArr2[0] + 1;
                if (iArr2[0] == i) {
                    videoView.stopPlayback();
                    iArr[0] = 0;
                    videoplaycallback.onCompletion();
                    return;
                }
                videoView.start();
            }
        });
        DebugUtil.dTAGForClassName("start play on " + videoView);
        videoView.start();
    }
}
