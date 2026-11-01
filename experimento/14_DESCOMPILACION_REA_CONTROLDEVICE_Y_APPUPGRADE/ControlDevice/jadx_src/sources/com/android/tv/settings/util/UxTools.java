package com.android.tv.settings.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import com.control.device.R;
import com.control.device.view.TsdFileManagerNoticeActivity;
import java.lang.reflect.Method;

/* loaded from: classes.dex */
public class UxTools {
    private static String channelId = "zte-ch";
    private static CharSequence channelName = "zte stb ch";
    private static int notificationId = 2777;

    public static boolean isPackageExist(Context context, String str) {
        try {
            context.getPackageManager().getPackageInfo(str, 1);
            return true;
        } catch (Exception unused) {
            return false;
        }
    }

    public static boolean isFirstBoot(Context context) {
        return (Settings.Secure.getInt(context.getContentResolver(), "tv_user_setup_complete", 0) == 1 && Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0) == 1) ? false : true;
    }

    public static void detectOutDateSoftware(Context context) {
        if (getIsTsdOnResultDoNotShowAnyMoreClicked(context) || isFirstBoot(context)) {
            return;
        }
        if (isPackageExist(context, "com.hitv.explore") && !isPackageExist(context, "com.anker.fileexplorer")) {
            notifyOutDateFileManager(context);
        }
        if (!isPackageExist(context, "org.videolan.vlc") || isPackageExist(context, "org.xbmc.kodi")) {
            return;
        }
        notifyOutDateVlc(context);
    }

    public static void setTsdOnResultDoNotShowAnyMore(Context context) {
        Settings.Global.putInt(context.getContentResolver(), "tsd_file_manager_notice", 1);
    }

    public static boolean getIsTsdOnResultDoNotShowAnyMoreClicked(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "tsd_file_manager_notice", 0) == 1;
    }

    private static void notifyOutDateVlc(Context context) {
        postNotification(context, context.getString(R.string.compatibility_notify_title), context.getString(R.string.vlc_notify), TsdFileManagerNoticeActivity.createTsdDialogIntent(context.getString(R.string.compatibility_notify_title), context.getString(R.string.vlc_notify)));
    }

    private static void notifyOutDateFileManager(Context context) {
        postNotification(context, context.getString(R.string.compatibility_notify_title), context.getString(R.string.hitv_notify), TsdFileManagerNoticeActivity.createTsdDialogIntent(context.getString(R.string.compatibility_notify_title), context.getString(R.string.hitv_notify)));
    }

    public static void postNotification(Context context, String str, String str2, Intent intent) {
        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, 4);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        notificationManager.createNotificationChannel(notificationChannel);
        Notification.Builder builder = new Notification.Builder(context);
        Notification.TvExtender tvExtender = new Notification.TvExtender();
        tvExtender.setSuppressShowOverApps(true);
        builder.setContentTitle(str).setSmallIcon(R.drawable.ic_settings_applications).setContentText(str2).setChannelId(channelId).setContentIntent(PendingIntent.getActivity(context, 0, intent, 67108864)).setWhen(System.currentTimeMillis()).setAutoCancel(true).extend(tvExtender).setSortKey("1");
        notificationManager.notify(getNotificationId(), builder.build());
    }

    private static int getNotificationId() {
        int i = notificationId + 1;
        notificationId = i;
        return i;
    }

    public static void makeBluetoothDiscoverableByInvokeWithNoTimeout() {
        Log.d("ControlDevice", "makeBluetoothDiscoverableByInvokeWithNoTimeout: called");
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            Class cls = Integer.TYPE;
            Method method = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", cls);
            method.setAccessible(true);
            method.invoke(defaultAdapter, -1);
            Method method2 = BluetoothAdapter.class.getMethod("setScanMode", cls, Long.TYPE);
            method2.setAccessible(true);
            method2.invoke(defaultAdapter, 23, -1L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
