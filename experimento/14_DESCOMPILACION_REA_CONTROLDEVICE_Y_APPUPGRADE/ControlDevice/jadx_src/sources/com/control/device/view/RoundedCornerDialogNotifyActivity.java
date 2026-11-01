package com.control.device.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.control.device.R;

/* loaded from: classes.dex */
public class RoundedCornerDialogNotifyActivity extends Activity {
    private static final String TAG = "RoundedCornerDialogNotifyActivity";

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.confirm_dialog);
        Intent intent = getIntent();
        if (intent != null) {
            String stringExtra = intent.getStringExtra("title");
            String stringExtra2 = intent.getStringExtra("message");
            ((TextView) findViewById(R.id.title)).setText(stringExtra);
            ((TextView) findViewById(R.id.message)).setText(stringExtra2);
        }
        Button button = (Button) findViewById(R.id.confirm_button);
        Button button2 = (Button) findViewById(R.id.cancel_button);
        button.setOnClickListener(new View.OnClickListener() { // from class: com.control.device.view.RoundedCornerDialogNotifyActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                RoundedCornerDialogNotifyActivity.this.onConfirmClicked();
                RoundedCornerDialogNotifyActivity.this.setResultAndFinish(-1, "Confirm clicked");
            }
        });
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.control.device.view.RoundedCornerDialogNotifyActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                RoundedCornerDialogNotifyActivity.this.onCancelClicked();
                RoundedCornerDialogNotifyActivity.this.setResultAndFinish(0, "Cancel clicked");
            }
        });
        button.requestFocusFromTouch();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setResultAndFinish(int i, String str) {
        Intent intent = new Intent();
        intent.putExtra("resultData", str);
        setResult(i, intent);
        finish();
    }

    public void onCancelClicked() {
        Log.d(TAG, "onCancelClicked: ");
    }

    public void onConfirmClicked() {
        Log.d(TAG, "onConfirmClicked: ");
    }
}
