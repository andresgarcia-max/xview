package com.control.device.settings.MexicAmco;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.control.device.R;

/* loaded from: classes.dex */
public class MenuKeyRspActivity extends Activity implements View.OnClickListener {
    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.amx_activity_menu_key_rsp);
        ((Button) findViewById(R.id.recargar)).setOnClickListener(this);
        ((Button) findViewById(R.id.ajustes)).setOnClickListener(this);
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ajustes) {
            startActivity("com.android.tv.settings", "com.android.tv.settings.MainSettings");
            finish();
        } else {
            if (id != R.id.recargar) {
                return;
            }
            new Thread() { // from class: com.control.device.settings.MexicAmco.MenuKeyRspActivity.1
                /* JADX WARN: Removed duplicated region for block: B:33:? A[RETURN, SYNTHETIC] */
                /* JADX WARN: Removed duplicated region for block: B:34:0x0123 A[EXC_TOP_SPLITTER, SYNTHETIC] */
                /* JADX WARN: Removed duplicated region for block: B:42:0x011c A[EXC_TOP_SPLITTER, SYNTHETIC] */
                /* JADX WARN: Removed duplicated region for block: B:48:? A[SYNTHETIC] */
                /* JADX WARN: Removed duplicated region for block: B:49:0x0117 A[EXC_TOP_SPLITTER, SYNTHETIC] */
                @Override // java.lang.Thread, java.lang.Runnable
                /*
                    Code decompiled incorrectly, please refer to instructions dump.
                    To view partially-correct add '--show-bad-code' argument
                */
                public void run() {
                    /*
                        Method dump skipped, instructions count: 298
                        To view this dump add '--comments-level debug' option
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.control.device.settings.MexicAmco.MenuKeyRspActivity.AnonymousClass1.run():void");
                }
            }.start();
        }
    }

    public void startActivity(String str, String str2) {
        ComponentName componentName = new ComponentName(str, str2);
        Intent intent = new Intent();
        intent.setComponent(componentName);
        startActivity(new Intent(intent));
    }
}
