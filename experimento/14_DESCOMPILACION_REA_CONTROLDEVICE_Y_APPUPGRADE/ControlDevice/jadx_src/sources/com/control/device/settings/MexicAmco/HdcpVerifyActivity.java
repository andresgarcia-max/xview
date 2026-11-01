package com.control.device.settings.MexicAmco;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.control.device.R;

/* loaded from: classes.dex */
public class HdcpVerifyActivity extends Activity {
    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_hdcp_verify);
        ((Button) findViewById(R.id.hdcp_verify_accept)).setOnClickListener(new View.OnClickListener() { // from class: com.control.device.settings.MexicAmco.HdcpVerifyActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                HdcpVerifyActivity.this.finish();
            }
        });
        HdcpVerifyService.mVerifyActivity = this;
    }

    @Override // android.app.Activity
    protected void onStop() {
        Log.i("HdcpVerifyActivity", "onStop");
        super.onStop();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        Log.i("HdcpVerifyActivity", "onDestroy");
        HdcpVerifyService.mVerifyActivity = null;
        super.onDestroy();
    }
}
