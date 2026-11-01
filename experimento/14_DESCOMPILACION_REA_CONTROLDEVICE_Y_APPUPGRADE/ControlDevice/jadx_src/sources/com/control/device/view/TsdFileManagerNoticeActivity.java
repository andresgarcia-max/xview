package com.control.device.view;

import android.content.Intent;
import android.net.Uri;
import com.android.tv.settings.util.UxTools;

/* loaded from: classes.dex */
public class TsdFileManagerNoticeActivity extends RoundedCornerDialogNotifyActivity {
    @Override // com.control.device.view.RoundedCornerDialogNotifyActivity
    public void onCancelClicked() {
        super.onCancelClicked();
        UxTools.setTsdOnResultDoNotShowAnyMore(getApplicationContext());
    }

    public static Intent createTsdDialogIntent(String str, String str2) {
        Intent intent = new Intent("com.control.device.DIALOG.tsd");
        intent.setData(Uri.parse("custom://data"));
        intent.putExtra("title", str);
        intent.putExtra("message", str2);
        return intent;
    }
}
