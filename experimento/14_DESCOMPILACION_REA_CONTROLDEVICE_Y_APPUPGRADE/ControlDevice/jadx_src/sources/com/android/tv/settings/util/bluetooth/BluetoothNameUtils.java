package com.android.tv.settings.util.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;
import android.util.Log;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* loaded from: classes.dex */
public class BluetoothNameUtils {
    private static final Pattern NAME_PATTERN = Pattern.compile("\"([0-9]{0,3}) ?(.*)\" \\((.*)\\)", 2);
    private static final Pattern COLOR_PATTERN = Pattern.compile("#([0-9a-f]{6})-#([0-9a-f]{6})(p?)(t?)(.*)", 2);

    public static int getSetupType(String str) {
        String group;
        Matcher matcher = NAME_PATTERN.matcher(str);
        if (matcher.matches() && (group = matcher.group(1)) != null) {
            try {
                return Integer.parseInt(group);
            } catch (NumberFormatException unused) {
            }
        }
        return -1;
    }

    public static LedConfiguration getColorConfiguration(String str) {
        Matcher matcher = NAME_PATTERN.matcher(str);
        if (!matcher.matches()) {
            return null;
        }
        String group = matcher.group(3);
        if (TextUtils.isEmpty(group)) {
            return null;
        }
        Matcher matcher2 = COLOR_PATTERN.matcher(group);
        if (!matcher2.matches()) {
            return null;
        }
        LedConfiguration ledConfiguration = new LedConfiguration(Integer.parseInt(matcher2.group(1), 16) | (-16777216), Integer.parseInt(matcher2.group(2), 16) | (-16777216), "p".equals(matcher2.group(3)));
        ledConfiguration.isTransient = "t".equals(matcher2.group(4));
        return ledConfiguration;
    }

    public static boolean isValidName(String str) {
        return NAME_PATTERN.matcher(str).matches();
    }

    public static boolean isOutputDeviceConnected() {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (defaultAdapter == null || !defaultAdapter.isEnabled()) {
            return false;
        }
        boolean z = false;
        for (BluetoothDevice bluetoothDevice : defaultAdapter.getBondedDevices()) {
            int majorDeviceClass = bluetoothDevice.getBluetoothClass().getMajorDeviceClass();
            Log.i("BluetoothNameUtil", " <isOutputDeviceConnected> device : [" + bluetoothDevice.getName() + "], type : [" + majorDeviceClass + "]");
            if (majorDeviceClass == 1024) {
                try {
                    z = ((Boolean) bluetoothDevice.getClass().getMethod("isConnected", new Class[0]).invoke(bluetoothDevice, new Object[0])).booleanValue();
                    if (z) {
                        break;
                    }
                } catch (Exception e) {
                    Log.e("BluetoothNameUtil", "isOutputDeviceConnected: ", e);
                }
            }
        }
        return z;
    }
}
