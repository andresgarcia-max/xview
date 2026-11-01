package com.control.device.recevier;

import android.R;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import com.android.tv.settings.util.UxTools;

/* loaded from: classes.dex */
public class AppInstallReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.PACKAGE_ADDED".equals(intent.getAction())) {
            String schemeSpecificPart = intent.getData().getSchemeSpecificPart();
            Log.d("AppInstallReceiver", schemeSpecificPart + "done with install");
            if (shouldMakeNotice(context) && "nextapp.fx".equals(schemeSpecificPart)) {
                doAppOutDateNotice(context);
            }
        }
    }

    private boolean shouldMakeNotice(Context context) {
        return UxTools.isPackageExist(context, "com.hitv.explore");
    }

    private static void doAppOutDateNotice(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context.getApplicationContext(), R.style.Theme.DeviceDefault.Dialog.Alert);
        builder.setMessage(com.control.device.R.string.notice_to_remove_hitv_file_manager);
        builder.setPositiveButton(com.control.device.R.string.consumer_information_button_ok, new DialogInterface.OnClickListener() { // from class: com.control.device.recevier.AppInstallReceiver.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.getWindow().setType(2003);
        create.show();
    }
}
