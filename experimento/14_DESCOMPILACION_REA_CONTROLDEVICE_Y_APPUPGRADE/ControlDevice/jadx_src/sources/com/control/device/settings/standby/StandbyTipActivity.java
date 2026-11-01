package com.control.device.settings.standby;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import com.control.device.R;
import java.util.Timer;
import java.util.TimerTask;

/* loaded from: classes.dex */
public class StandbyTipActivity extends Activity {
    private TextView countDownView;
    private TextView lastStandbyTimeView;
    private int countDownTime = 20;
    Timer timer = new Timer();
    TimerTask task = new TimerTask() { // from class: com.control.device.settings.standby.StandbyTipActivity.1
        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            StandbyTipActivity.this.runOnUiThread(new Runnable() { // from class: com.control.device.settings.standby.StandbyTipActivity.1.1
                @Override // java.lang.Runnable
                public void run() {
                    StandbyTipActivity.access$010(StandbyTipActivity.this);
                    StandbyTipActivity.this.countDownView.setText(StandbyTipActivity.this.countDownTime + " " + StandbyTipActivity.this.getResources().getString(R.string.standby_tip_seconds));
                    StringBuilder sb = new StringBuilder();
                    sb.append("[StandbyTipActivity]<TimerTask> countDownTime = ");
                    sb.append(StandbyTipActivity.this.countDownTime);
                    Log.d("[TvSettings]", sb.toString());
                    if (StandbyTipActivity.this.countDownTime <= 0) {
                        StandbyTipActivity.this.timer.cancel();
                        StandbyTipActivity.this.finish();
                        new Thread(new Runnable() { // from class: com.control.device.settings.standby.StandbyTipActivity.1.1.1
                            @Override // java.lang.Runnable
                            public void run() {
                                try {
                                    new Instrumentation().sendKeyDownUpSync(26);
                                    Log.d("[TvSettings]", "[StandbyTipActivity]<TimerTask> Standby!");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
            });
        }
    };

    static /* synthetic */ int access$010(StandbyTipActivity standbyTipActivity) {
        int i = standbyTipActivity.countDownTime;
        standbyTipActivity.countDownTime = i - 1;
        return i;
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Log.d("[TvSettings]", "[StandbyTipActivity]<onCreate> ");
        setContentView(R.layout.standby_tip);
        findViewById(R.id.standby_layout).getBackground().setAlpha(100);
        this.lastStandbyTimeView = (TextView) findViewById(R.id.standby_tip2_b);
        this.countDownView = (TextView) findViewById(R.id.standby_tip3_b);
        this.timer.schedule(this.task, 1000L, 1000L);
        if (getSleepTime() != 0) {
            int sleepTime = ((getSleepTime() / 1000) / 60) - 1;
            Log.d("[TvSettings]", "[StandbyTipActivity]<onCreate> lastStandby = " + sleepTime);
            this.lastStandbyTimeView.setText(sleepTime + " " + getResources().getString(R.string.standby_tip_minute));
        }
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        Log.d("[TvSettings]", "[StandbyTipActivity]<onKeyDown> finish");
        finish();
        Timer timer = this.timer;
        if (timer != null) {
            timer.cancel();
        }
        TimerTask timerTask = this.task;
        if (timerTask != null) {
            timerTask.cancel();
        }
        return super.onKeyDown(i, keyEvent);
    }

    private int getSleepTime() {
        return Settings.Secure.getInt(getContentResolver(), "sleep_timeout", 10800000);
    }
}
