package com.control.device.settings.MexicAmco;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import com.android.tv.settings.adv.ISystemControlManager;
import com.android.tv.settings.adv.impl.SystemControlManager;
import com.control.device.R;

/* loaded from: classes.dex */
public class FrameratePriorityActivity extends Activity {
    private Button mBtnCancel;
    private Button mBtnOk;
    private CheckBox mCheckBoxPriority;
    private ISystemControlManager mISystemControlManager;

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        Log.i("FrameratePriorityActivity", "onCreate");
        super.onCreate(bundle);
        setContentView(R.layout.activity_framerate_priority);
        this.mISystemControlManager = SystemControlManager.getInstance();
        initView();
    }

    private void initView() {
        this.mCheckBoxPriority = (CheckBox) findViewById(R.id.frameRatePriority_check);
        this.mBtnOk = (Button) findViewById(R.id.frameRatePriority_btn_ok);
        this.mBtnCancel = (Button) findViewById(R.id.frameRatePriority_btn_cancel);
        this.mBtnOk.setOnClickListener(new OnButtonOkClickListener());
        this.mBtnCancel.setOnClickListener(new OnButtonCancelClickListener());
        String property = this.mISystemControlManager.getProperty("persist.vendor.sys.framerate.priority");
        Log.i("FrameratePriorityActivity", "Frame rate priority is " + property);
        if ("true".equals(property)) {
            this.mCheckBoxPriority.setChecked(true);
        } else {
            this.mCheckBoxPriority.setChecked(false);
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        Log.i("FrameratePriorityActivity", "onDestroy");
        super.onDestroy();
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        Log.i("FrameratePriorityActivity", "onBackPressed, nothing to do");
    }

    private class OnButtonOkClickListener implements View.OnClickListener {
        private OnButtonOkClickListener() {
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            Log.i("FrameratePriorityActivity", "onClick ok, CheckBox isChecked=" + FrameratePriorityActivity.this.mCheckBoxPriority.isChecked());
            FrameratePriorityActivity.this.mISystemControlManager.setProperty("persist.vendor.sys.framerate.priority", String.valueOf(FrameratePriorityActivity.this.mCheckBoxPriority.isChecked()));
            FrameratePriorityActivity.this.finish();
        }
    }

    private class OnButtonCancelClickListener implements View.OnClickListener {
        private OnButtonCancelClickListener() {
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            Log.i("FrameratePriorityActivity", "onClick cancel");
            FrameratePriorityActivity.this.finish();
        }
    }
}
