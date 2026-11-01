package com.control.device.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.tv.settings.util.ProductUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.android.tv.settings.util.MarketUtil;
import com.android.tv.settings.util.NetworkInfoHelper;
import com.android.tv.settings.util.bluetooth.BluetoothNameUtils;
import com.control.device.R;

/* loaded from: classes.dex */
public class ConnectionService extends Service {
    private Context mContext;
    private boolean toastFlag = false;
    private boolean bluetoothFlg = false;
    private boolean wifiFlg = false;
    private boolean logFlg = true;
    private BroadcastReceiver connectionReceiver = null;

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        return 1;
    }

    @Override // android.app.Service
    public void onCreate() {
        logi(" <onCreate> ");
        this.mContext = this;
        super.onCreate();
        if (MarketUtil.isNETFLIX_W10() && ProductUtils.isB866V5W11()) {
            this.connectionReceiver = new ConnectionReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.wifi.STATE_CHANGE");
            intentFilter.addAction("android.bluetooth.device.action.ACL_CONNECTED");
            this.mContext.registerReceiver(this.connectionReceiver, intentFilter);
            logi(" <onCreate> Connection receiver is registered.");
            return;
        }
        stopSelf();
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        logi(" <onBind> ");
        return null;
    }

    @Override // android.app.Service
    public void onDestroy() {
        logi(" <onDestroy> ");
        BroadcastReceiver broadcastReceiver = this.connectionReceiver;
        if (broadcastReceiver != null) {
            this.mContext.unregisterReceiver(broadcastReceiver);
            logi(" <onDestroy> connection receiver is unregistered.");
            this.connectionReceiver = null;
        }
        super.onDestroy();
    }

    private class ConnectionReceiver extends BroadcastReceiver {
        private ConnectionReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            ConnectionService.this.logi(" <onReceive>  action received:{" + action + "}");
            if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                ConnectionService.this.updateWifiState();
            } else if ("android.bluetooth.device.action.ACL_CONNECTED".equals(action)) {
                ConnectionService.this.updateBluetoothState();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateWifiState() {
        this.wifiFlg = new NetworkInfoHelper(this.mContext).getWifiFrequencyType() == 0;
        determineShowToast();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateBluetoothState() {
        this.bluetoothFlg = BluetoothNameUtils.isOutputDeviceConnected();
        determineShowToast();
    }

    private void determineShowToast() {
        this.toastFlag = this.wifiFlg && this.bluetoothFlg;
        logi(" <determineShowToast>  show toast ? " + this.toastFlag);
        if (this.toastFlag) {
            showToast();
            this.toastFlag = false;
        }
    }

    private void showToast() {
        View inflate = ((LayoutInflater) getSystemService("layout_inflater")).inflate(R.layout.tips_switch_wifi_band_layout, (ViewGroup) null);
        Toast toast = new Toast(this.mContext);
        toast.setDuration(1);
        toast.setView(inflate);
        toast.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void logi(String str) {
        if (this.logFlg) {
            Log.i("ConnectionService", str);
        }
    }
}
