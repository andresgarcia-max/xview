package com.control.device.settings.tvoption;

import android.content.Context;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import com.android.tv.settings.SettingsConstant;
import com.android.tv.settings.adv.IOutputModeManager;
import com.android.tv.settings.adv.impl.OutputModeManager;
import com.android.tv.settings.adv.impl.SystemControlManager;

/* loaded from: classes.dex */
public class SoundParameterSettingManager {
    private String CpuName = SystemProperties.get("ro.product.cpu.name");
    private AudioManager mAudioManager;
    private Context mContext;
    private IOutputModeManager mIOutputModeManager;
    private Resources mResources;

    public SoundParameterSettingManager(Context context) {
        this.mContext = context;
        this.mResources = context.getResources();
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        this.mIOutputModeManager = new OutputModeManager(context);
    }

    public static boolean CanDebug() {
        return SystemControlManager.getInstance().getPropertyBoolean("vendor.soundparameter.debug", false);
    }

    /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue */
    public void setDigitalAudioFormat(String str) {
        if (CanDebug()) {
            Log.d("SoundParameterSettingManager", "setDigitalAudioFormat = " + str);
        }
        char c = 65535;
        switch (str.hashCode()) {
            case -1081415738:
                if (str.equals("manual")) {
                    c = 2;
                    break;
                }
                break;
            case 110810:
                if (str.equals("pcm")) {
                    c = 0;
                    break;
                }
                break;
            case 3005871:
                if (str.equals("auto")) {
                    c = 3;
                    break;
                }
                break;
            case 109640964:
                if (str.equals("spdif")) {
                    c = 1;
                    break;
                }
                break;
        }
        if (c == 0) {
            this.mIOutputModeManager.setDigitalAudioFormatOut(0);
            return;
        }
        if (c == 1) {
            this.mIOutputModeManager.setDigitalAudioFormatOut(1);
        } else if (c == 2) {
            this.mIOutputModeManager.setDigitalAudioFormatOut(3, getAudioManualFormats());
        } else {
            this.mIOutputModeManager.setDigitalAudioFormatOut(2);
        }
    }

    public String getDigitalAudioFormat() {
        if (this.CpuName.contains("905X4")) {
            int i = Settings.Global.getInt(this.mContext.getContentResolver(), "digital_audio_format", 1);
            Log.d("SoundParameterSettingManager", "getDigitalAudioFormat surround = " + i + ", cpu=" + this.CpuName);
            return i != 0 ? i != 2 ? i != 4 ? "auto" : "spdif" : "manual" : "pcm";
        }
        int i2 = Settings.Global.getInt(this.mContext.getContentResolver(), "digital_audio_format", 2);
        Log.d("SoundParameterSettingManager", "getDigitalAudioFormat value = " + i2 + ", cpu=" + this.CpuName);
        return i2 != 0 ? i2 != 1 ? i2 != 3 ? "auto" : "manual" : "spdif" : "pcm";
    }

    public String getAudioManualFormats() {
        String string = Settings.Global.getString(this.mContext.getContentResolver(), "digital_audio_subformat");
        return string == null ? "" : string;
    }

    public void setARCLatency(int i) {
        Settings.Global.putInt(this.mContext.getContentResolver(), "tv_arc_latency", i);
        this.mIOutputModeManager.setARCLatency(i);
    }

    public int getARCLatency() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "tv_arc_latency", -40);
    }

    public void setDrcModePassthroughSetting(int i) {
        Settings.Global.putInt(this.mContext.getContentResolver(), "drc_mode", i);
    }

    public void initParameterAfterBoot() {
        Log.d("SoundParameterSettingManager", "initParameterAfterBoot");
        setDigitalAudioFormat(getDigitalAudioFormat());
        if (!SettingsConstant.needDroidlogicTvFeature(this.mContext)) {
            setDrcModePassthrough();
        }
        setARCLatency(getARCLatency());
        this.mIOutputModeManager.initSoundParametersAfterBoot();
    }

    public void setDrcModePassthrough() {
        SystemProperties.getBoolean("ro.vendor.platform.has.tuner", false);
        int i = Settings.Global.getInt(this.mContext.getContentResolver(), "drc_mode", 2);
        if (i == 0) {
            this.mIOutputModeManager.enableDobly_DRC(false);
            this.mIOutputModeManager.setDoblyMode("2");
            setDrcModePassthroughSetting(0);
        } else if (i == 1) {
            this.mIOutputModeManager.enableDobly_DRC(true);
            this.mIOutputModeManager.setDoblyMode("2");
            setDrcModePassthroughSetting(1);
        } else {
            if (i != 2) {
                return;
            }
            this.mIOutputModeManager.enableDobly_DRC(false);
            this.mIOutputModeManager.setDoblyMode("3");
            setDrcModePassthroughSetting(2);
        }
    }
}
