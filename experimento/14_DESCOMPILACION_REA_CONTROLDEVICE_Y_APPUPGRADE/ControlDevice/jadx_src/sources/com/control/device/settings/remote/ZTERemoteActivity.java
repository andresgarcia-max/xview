package com.control.device.settings.remote;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.app.LocalePicker;
import com.android.tv.settings.accessories.BluetoothDevicePairer;
import com.android.tv.settings.name.DeviceManager;
import com.control.device.R;
import com.control.device.settings.remote.util.ZTERemoteAnimationControl;
import com.control.device.util.MarketUtil;
import com.control.device.util.ProductUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/* loaded from: classes.dex */
public class ZTERemoteActivity extends Activity implements BluetoothDevicePairer.EventListener {
    private static boolean mPairingSuccess;
    private ImageView bfPair;
    private Button bfPair_button;
    private TextView bfPair_text;
    private String containName;
    private ImageView inPair;
    private int isSetup;
    private TextView mAf_pair_text;
    private BluetoothDevicePairer mBluetoothPairer;
    private int mBootFlag;
    private BluetoothAdapter mBtAdapter;
    private Button mBtnConnect;
    private Button mBtnNext;
    private Button mBtnSkip;
    private Context mContext;
    private LinearLayout mLinearLayout;
    private LinearLayout mLinearlayout_in;
    private LinearLayout mLinearlayout_over;
    private LinearLayout mLinearlayout_start;
    private Handler mMsgHandler;
    private Button mO2SkipBtn;
    private TextView mPairEndTv;
    private TextView mPairEndTvItaly;
    private TextView mPairStartTv;
    private TextView mPairStartTvItaly;
    private TextView mPairStartTvViettel;
    private TextView mPairSuccessNetgem;
    private TextView mPair_text;
    private TextView mPair_text_Th;
    private TextView mTextView_subTip;
    private TextView mTextView_tip;
    private ZTERemoteAnimationControl mZTERemoteAnimationControl;
    private TextView mafPairEn;
    private TextView mafPairMk;
    private TextView mafPairSq;
    TimerTask task;
    Timer timer;
    private static final int MINOR_DEVICE_CLASS_POINTING = Integer.parseInt("0000010000000", 2);
    private static final int MINOR_DEVICE_CLASS_JOYSTICK = Integer.parseInt("0000000000100", 2);
    private static final int MINOR_DEVICE_CLASS_GAMEPAD = Integer.parseInt("0000000001000", 2);
    private static final int MINOR_DEVICE_CLASS_KEYBOARD = Integer.parseInt("0000001000000", 2);
    private static final int MINOR_DEVICE_CLASS_REMOTE = Integer.parseInt("0000000001100", 2);
    private static AnimationDrawable animationDrawable = null;
    private static final String[] Multilaser_LANGUAGE = {"pt-BR", "en-US"};
    private static final String[] Ellies_LANGUAGE = {"en-US", "af-ZA"};
    private static final String[] ASBIS_LANGUAGE = {"ru-RU", "en-US"};
    private static final String[] Hayex_LANGUAGE = {"es-ES", "en-US"};
    private static final String[] Israel_LANGUAGE = {"iw-IL", "en-US"};
    private static final String[] UAE_LANGUAGE = {"ar-AE", "en-US"};
    private static final String[] Indonesia_LANGUAGE = {"id-ID", "en-US"};
    private static final String[] Elisal_LANGUAGE = {"fi-FI", "sv-SE", "en-GB"};
    private static final String[] Bulgaria_A1_LANGUAGE = {"bg-BG", "en-GB"};
    private static final String[] Croatian_LANGUAGE = {"hr-HR", "en-GB", "de-DE"};
    private static final String[] Mk_A1_LANGUAGE = {"mk-MK", "sq-AL", "en-GB"};
    private static final String[] CZECH_LANGUAGE = {"cs-CZ", "en-US"};
    private static final String[] Egyptian_LANGUAGE = {"ar-EG", "en-US"};
    private boolean isFailing = false;
    private final Uri CONTENT_URI = Uri.parse("content://com.google.android.tungsten.setupwraith.locales/localeprefs");
    private String mMarket = SystemProperties.get("config.MarketCode");
    private String mOperator = SystemProperties.get("config.OperatorCode");
    private String MarketCode_OperatorCode = SystemProperties.get("config.MarketCode") + " " + SystemProperties.get("config.OperatorCode");
    final String Russia_MTS = "1101 2";
    final String MARKET_2 = "1101 5";
    final String MARKET_3 = "7001 1";
    final String Indonesia = "3601 1";
    final String Megacable_OTT = "4301 5";
    final String ChileWom_OTT = "5701 2";
    final String BrazilMultilaser_OTT = "7501 2";
    final String ThailandAIS = "4701 2";
    final String Russia_ER = "1101 4";
    final String Mexico_izzi = "4301 4";
    final String MEXICO_AMCO = "4301 6";
    final String Viettel_OTT = "1301 2";
    final String Czech_OTT = "6701 1";
    final String Czech_O2 = "6701 2";
    final String Hungary_OTT = "4501 2";
    final String Elisal_OTT = "8701 1";
    final String Brazil_UQ = "7501 8";
    final String NETFLIXREF_OTT = "9999 10";
    final String GENERAL_INTERNATIONAL = "9999 1";
    final String Nordnet_OTT = "3501 3";
    final String Bulgaria_A1 = "1801 4";
    final String Croatian_A1 = "8901 1";
    final String Mk_A1 = "5101 2";
    private String mOemTag = SystemProperties.get("ro.oem.key1").trim();
    private int mPreviousStatus = 0;
    private BroadcastReceiver mBtReceiver = new BroadcastReceiver() { // from class: com.control.device.settings.remote.ZTERemoteActivity.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("[ControlDevice]", " bluetooth action : " + action);
            BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            Log.d("[ControlDevice]", " bonding device : " + bluetoothDevice.getName() + ", address : " + bluetoothDevice.getAddress());
            if (action.equals("android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED")) {
                int intExtra = intent.getIntExtra("android.bluetooth.profile.extra.STATE", -1);
                Log.d("[ControlDevice]", "Bluetooth connect state :" + intExtra);
                if (intExtra == 0) {
                    ZTERemoteActivity.this.startBluetoothPairer();
                } else {
                    if (intExtra != 2) {
                        return;
                    }
                    ZTERemoteActivity.this.onPairSuccessAndFinish();
                }
            }
        }
    };
    final Handler flashOnTimeHandler = new Handler() { // from class: com.control.device.settings.remote.ZTERemoteActivity.13
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what == 1) {
                Log.d("[ControlDevice]", "[ZTERemoteActivity]<handleMessage> 1");
                Log.d("[ControlDevice]", "[ZTERemoteActivity]<handleMessage> mBluetoothPairer != null = " + (ZTERemoteActivity.this.mBluetoothPairer != null));
                if (ZTERemoteActivity.this.mBluetoothPairer != null && ZTERemoteActivity.this.mBluetoothPairer.getTargetDevice() == null) {
                    Log.d("[ControlDevice]", "[ZTERemoteActivity]<handleMessage> 1, ReBluetoothPairer");
                    ZTERemoteActivity.this.stopBluetoothPairer();
                    ZTERemoteActivity.this.startBluetoothPairer();
                }
            }
            super.handleMessage(message);
        }
    };

    private boolean isMatchingMajorDeviceClass(int i) {
        return i == 1280;
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        Handler handler;
        super.onCreate(bundle);
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<onCreate> ");
        this.mBootFlag = Settings.Global.getInt(getContentResolver(), "device_provisioned", -1);
        Log.d("[ControlDevice]", "mBootFlag = " + this.mBootFlag);
        ZTERemoteAnimationControl zTERemoteAnimationControl = new ZTERemoteAnimationControl();
        this.mZTERemoteAnimationControl = zTERemoteAnimationControl;
        if (!zTERemoteAnimationControl.initWithContext(getApplicationContext())) {
            this.mZTERemoteAnimationControl.init();
        }
        this.containName = this.mZTERemoteAnimationControl.containName;
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<onCreate> containName = " + this.containName);
        this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        handler = new Handler();
        this.mContext = this;
        String str = this.MarketCode_OperatorCode;
        str.hashCode();
        switch (str) {
            case "1801 4":
                insertToLoaclesProvider(Bulgaria_A1_LANGUAGE);
                setContentView(this.mZTERemoteAnimationControl.layout);
                this.bfPair = (ImageView) findViewById(this.mZTERemoteAnimationControl.bfPair);
                this.inPair = (ImageView) findViewById(this.mZTERemoteAnimationControl.inPair);
                this.mafPairMk = (TextView) findViewById(R.id.afPair_tv_mk);
                this.mafPairSq = (TextView) findViewById(R.id.afPair_tv_sq);
                this.mafPairEn = (TextView) findViewById(R.id.afPair_tv_en);
                this.mafPairMk.setVisibility(4);
                this.mafPairEn.setVisibility(4);
                this.mafPairSq.setText(getResources().getString(R.string.bg_bluetooth_tip));
                Log.d("[ControlDevice]", "Bulgaria_A1 customization animation");
                AnimationDrawable animationDrawable2 = (AnimationDrawable) this.bfPair.getBackground();
                animationDrawable = animationDrawable2;
                animationDrawable2.start();
                break;
            case "3501 3":
                Log.d("[ControlDevice]", "Nordnet_OTT Market");
                setContentView(R.layout.Nordnet_OTT_remote);
                this.bfPair = (ImageView) findViewById(this.mZTERemoteAnimationControl.bfPair);
                this.inPair = (ImageView) findViewById(this.mZTERemoteAnimationControl.inPair);
                AnimationDrawable animationDrawable3 = (AnimationDrawable) this.bfPair.getBackground();
                animationDrawable = animationDrawable3;
                animationDrawable3.start();
                this.bfPair_text = (TextView) findViewById(R.id.in_bfPair_text);
                Button button = (Button) findViewById(R.id.in_bfPair_button);
                this.bfPair_button = button;
                button.setOnClickListener(new View.OnClickListener() { // from class: com.control.device.settings.remote.ZTERemoteActivity.7
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        Log.d("[ControlDevice]", "[ZTERemoteActivity]<onClick> SKIP");
                        ZTERemoteActivity.this.stopBluetoothPairer();
                        ZTERemoteActivity.this.flashOnTimeHandler.removeCallbacksAndMessages(null);
                        ZTERemoteActivity.this.mMsgHandler.removeCallbacksAndMessages(null);
                        ZTERemoteActivity.this.FinishActivity();
                    }
                });
                try {
                    this.isSetup = Settings.Secure.getInt(getApplicationContext().getContentResolver(), "user_setup_complete");
                    Log.d("[ControlDevice]", "[ZTERemoteActivity]<onCreate> isSetup = " + this.isSetup);
                } catch (Exception e) {
                    Log.d("[ControlDevice]", "[ZTERemoteActivity]<onCreate> Exception = " + e);
                }
                if (this.isSetup == 1) {
                    Log.d("[ControlDevice]", "[ZTERemoteActivity]<onCreate> Not in SetupWraith, hide Tip!");
                    this.bfPair_button.setVisibility(4);
                    break;
                } else {
                    this.bfPair_text.requestFocus();
                    break;
                }
            case "3601 1":
                insertToLoaclesProvider(Indonesia_LANGUAGE);
                setContentView(this.mZTERemoteAnimationControl.layout);
                this.bfPair = (ImageView) findViewById(this.mZTERemoteAnimationControl.bfPair);
                this.inPair = (ImageView) findViewById(this.mZTERemoteAnimationControl.inPair);
                AnimationDrawable animationDrawable4 = (AnimationDrawable) this.bfPair.getBackground();
                animationDrawable = animationDrawable4;
                animationDrawable4.start();
                this.bfPair_text = (TextView) findViewById(R.id.in_bfPair_text);
                Button button2 = (Button) findViewById(R.id.in_bfPair_button);
                this.bfPair_button = button2;
                button2.setOnClickListener(new View.OnClickListener() { // from class: com.control.device.settings.remote.ZTERemoteActivity.3
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        Log.d("[ControlDevice]", "[ZTERemoteActivity]<onClick> SKIP");
                        ZTERemoteActivity.this.stopBluetoothPairer();
                        ZTERemoteActivity.this.flashOnTimeHandler.removeCallbacksAndMessages(null);
                        ZTERemoteActivity.this.mMsgHandler.removeCallbacksAndMessages(null);
                        ZTERemoteActivity.this.FinishActivity();
                    }
                });
                try {
                    this.isSetup = Settings.Secure.getInt(getApplicationContext().getContentResolver(), "user_setup_complete");
                    Log.d("[ControlDevice]", "[ZTERemoteActivity]<onCreate> isSetup = " + this.isSetup);
                } catch (Exception e2) {
                    Log.d("[ControlDevice]", "[ZTERemoteActivity]<onCreate> Exception = " + e2);
                }
                if (this.isSetup == 1) {
                    Log.d("[ControlDevice]", "[ZTERemoteActivity]<onCreate> Not in SetupWraith, hide Tip!");
                    this.bfPair_text.setVisibility(4);
                    this.bfPair_button.setVisibility(4);
                } else {
                    this.bfPair_text.requestFocus();
                }
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED");
                this.mContext.registerReceiver(this.mBtReceiver, intentFilter);
                break;
            case "4001 1":
                Intent intent = new Intent();
                intent.addFlags(268435456);
                intent.setClassName("com.control.device", "com.control.device.settings.remote.ZTERemoteVideoActivity");
                startActivity(intent);
                finish();
                setContentView(R.layout.italy_tsd_remote);
                this.bfPair = (ImageView) findViewById(R.id.bfPair);
                this.inPair = (ImageView) findViewById(R.id.inPair);
                this.mPairStartTv = (TextView) findViewById(R.id.bfPair_tv);
                this.mPairStartTvItaly = (TextView) findViewById(R.id.bfPair_tv_italy);
                this.mPairEndTv = (TextView) findViewById(R.id.afPair_tv);
                this.mPairEndTvItaly = (TextView) findViewById(R.id.afPair_tv_italy);
                AnimationDrawable animationDrawable5 = (AnimationDrawable) this.bfPair.getBackground();
                animationDrawable = animationDrawable5;
                animationDrawable5.start();
                this.mPairStartTv.setText(R.string.italy_bluetooth_tips1_g20);
                this.mPairStartTvItaly.setText(R.string.italy_bluetooth_tips1_italy_g20);
                break;
            case "4301 4":
                Log.d("[ControlDevice]", "Show certain locales and Hide others");
                ArrayList arrayList = new ArrayList();
                arrayList.add("es-MX");
                arrayList.add("en-US");
                int i = 0;
                while (i < arrayList.size()) {
                    String str2 = (String) arrayList.get(i);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("locale", str2);
                    i++;
                    contentValues.put("rank", Integer.valueOf(i));
                    getContentResolver().insert(this.CONTENT_URI, contentValues);
                }
                ContentValues contentValues2 = new ContentValues();
                contentValues2.put("locale", "*");
                contentValues2.put("rank", (Integer) 0);
                getContentResolver().insert(this.CONTENT_URI, contentValues2);
                Log.d("[ControlDevice]", "Mexico_izzi using mirada application to do bluetooth pair!!");
                setContentView(R.layout.blank);
                handler.postDelayed(new Runnable() { // from class: com.control.device.settings.remote.ZTERemoteActivity.2
                    @Override // java.lang.Runnable
                    public void run() {
                        ZTERemoteActivity.this.FinishActivity();
                    }
                }, 1700L);
                break;
            case "4301 5":
                setContentView(R.layout.bluetooth_pairing_layout);
                Log.d("[ControlDevice]", "Megacable_OTT is not market customized");
                break;
            case "4501 2":
                setContentView(this.mZTERemoteAnimationControl.layout);
                this.bfPair = (ImageView) findViewById(this.mZTERemoteAnimationControl.bfPair);
                this.inPair = (ImageView) findViewById(this.mZTERemoteAnimationControl.inPair);
                Log.d("[ControlDevice]", "Hungary_OTT customization animation");
                AnimationDrawable animationDrawable6 = (AnimationDrawable) this.bfPair.getBackground();
                animationDrawable = animationDrawable6;
                animationDrawable6.start();
                Button button3 = (Button) findViewById(R.id.cetin_skip_bluetooth_pair);
                this.mO2SkipBtn = button3;
                button3.setOnClickListener(new View.OnClickListener() { // from class: com.control.device.settings.remote.ZTERemoteActivity.6
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        ZTERemoteActivity.this.FinishActivity();
                        Settings.Global.putString(ZTERemoteActivity.this.getContentResolver(), "is_need_to_skip", null);
                    }
                });
                String string = Settings.Global.getString(getContentResolver(), "is_need_to_skip");
                if (!TextUtils.isEmpty(string) && "1".equals(string)) {
                    Log.d("[ControlDevice]", "[ZTERemoteActivity]<onCreate> Hungary_OTT Bluetooth pair skip");
                    this.mO2SkipBtn.setVisibility(0);
                    break;
                }
                break;
            case "5101 2":
                insertToLoaclesProvider(Mk_A1_LANGUAGE);
                setContentView(this.mZTERemoteAnimationControl.layout);
                this.bfPair = (ImageView) findViewById(this.mZTERemoteAnimationControl.bfPair);
                this.inPair = (ImageView) findViewById(this.mZTERemoteAnimationControl.inPair);
                this.mafPairMk = (TextView) findViewById(R.id.afPair_tv_mk);
                this.mafPairSq = (TextView) findViewById(R.id.afPair_tv_sq);
                this.mafPairEn = (TextView) findViewById(R.id.afPair_tv_en);
                Log.d("[ControlDevice]", "Mk_A1 customization animation");
                AnimationDrawable animationDrawable7 = (AnimationDrawable) this.bfPair.getBackground();
                animationDrawable = animationDrawable7;
                animationDrawable7.start();
                break;
            case "5801 3":
                setContentView(R.layout.italy_tsd_remote);
                this.bfPair = (ImageView) findViewById(R.id.bfPair);
                this.inPair = (ImageView) findViewById(R.id.inPair);
                this.mPairStartTv = (TextView) findViewById(R.id.bfPair_tv);
                this.mPairStartTvItaly = (TextView) findViewById(R.id.bfPair_tv_italy);
                this.mPairEndTv = (TextView) findViewById(R.id.afPair_tv);
                this.mPairEndTvItaly = (TextView) findViewById(R.id.afPair_tv_italy);
                AnimationDrawable animationDrawable52 = (AnimationDrawable) this.bfPair.getBackground();
                animationDrawable = animationDrawable52;
                animationDrawable52.start();
                this.mPairStartTv.setText(R.string.italy_bluetooth_tips1_g20);
                this.mPairStartTvItaly.setText(R.string.italy_bluetooth_tips1_italy_g20);
                break;
            case "6701 1":
                setContentView(this.mZTERemoteAnimationControl.layout);
                this.bfPair = (ImageView) findViewById(this.mZTERemoteAnimationControl.bfPair);
                this.inPair = (ImageView) findViewById(this.mZTERemoteAnimationControl.inPair);
                Log.d("[ControlDevice]", "Czech_OTT customization animation");
                AnimationDrawable animationDrawable8 = (AnimationDrawable) this.bfPair.getBackground();
                animationDrawable = animationDrawable8;
                animationDrawable8.start();
                Button button4 = (Button) findViewById(R.id.cetin_skip_bluetooth_pair);
                this.mO2SkipBtn = button4;
                button4.setOnClickListener(new View.OnClickListener() { // from class: com.control.device.settings.remote.ZTERemoteActivity.4
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        ZTERemoteActivity.this.FinishActivity();
                        Settings.Global.putString(ZTERemoteActivity.this.getContentResolver(), "is_need_to_skip", null);
                    }
                });
                String string2 = Settings.Global.getString(getContentResolver(), "is_need_to_skip");
                if (!TextUtils.isEmpty(string2) && "1".equals(string2)) {
                    Log.d("[ControlDevice]", "[ZTERemoteActivity]<onCreate> Czech_OTT Bluetooth pair skip");
                    this.mO2SkipBtn.setVisibility(0);
                    break;
                }
                break;
            case "6701 2":
                insertElisalToLoaclesProvider(CZECH_LANGUAGE);
                setContentView(this.mZTERemoteAnimationControl.layout);
                this.bfPair = (ImageView) findViewById(this.mZTERemoteAnimationControl.bfPair);
                this.inPair = (ImageView) findViewById(this.mZTERemoteAnimationControl.inPair);
                Log.d("[ControlDevice]", "Czech_O2 customization animation");
                AnimationDrawable animationDrawable9 = (AnimationDrawable) this.bfPair.getBackground();
                animationDrawable = animationDrawable9;
                animationDrawable9.start();
                Button button5 = (Button) findViewById(R.id.cetin_skip_bluetooth_pair);
                this.mO2SkipBtn = button5;
                button5.setOnClickListener(new View.OnClickListener() { // from class: com.control.device.settings.remote.ZTERemoteActivity.5
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        ZTERemoteActivity.this.FinishActivity();
                        Settings.Global.putString(ZTERemoteActivity.this.getContentResolver(), "is_need_to_skip", null);
                    }
                });
                String string3 = Settings.Global.getString(getContentResolver(), "is_need_to_skip");
                if (!TextUtils.isEmpty(string3) && "1".equals(string3)) {
                    Log.d("[ControlDevice]", "[ZTERemoteActivity]<onCreate> Czech_OTT Bluetooth pair skip");
                    this.mO2SkipBtn.setVisibility(0);
                    break;
                }
                break;
            case "7001 1":
                setContentView(R.layout.italy_tsd_remote);
                this.bfPair = (ImageView) findViewById(R.id.bfPair);
                this.inPair = (ImageView) findViewById(R.id.inPair);
                this.mPairStartTv = (TextView) findViewById(R.id.bfPair_tv);
                this.mPairStartTvItaly = (TextView) findViewById(R.id.bfPair_tv_italy);
                this.mPairEndTv = (TextView) findViewById(R.id.afPair_tv);
                this.mPairEndTvItaly = (TextView) findViewById(R.id.afPair_tv_italy);
                Log.d("[ControlDevice]", "ITALY TSD animation");
                AnimationDrawable animationDrawable10 = (AnimationDrawable) this.bfPair.getBackground();
                animationDrawable = animationDrawable10;
                animationDrawable10.start();
                break;
            case "7501 8":
                setContentView(this.mZTERemoteAnimationControl.layout);
                this.bfPair = (ImageView) findViewById(this.mZTERemoteAnimationControl.bfPair);
                this.inPair = (ImageView) findViewById(this.mZTERemoteAnimationControl.inPair);
                Log.d("[ControlDevice]", "Brazil_UQ customization animation");
                AnimationDrawable animationDrawable11 = (AnimationDrawable) this.bfPair.getBackground();
                animationDrawable = animationDrawable11;
                animationDrawable11.start();
                break;
            case "8701 1":
                setContentView(this.mZTERemoteAnimationControl.layout);
                this.bfPair = (ImageView) findViewById(this.mZTERemoteAnimationControl.bfPair);
                this.inPair = (ImageView) findViewById(this.mZTERemoteAnimationControl.inPair);
                Log.d("[ControlDevice]", "Elisal_OTT customization animation");
                AnimationDrawable animationDrawable12 = (AnimationDrawable) this.bfPair.getBackground();
                animationDrawable = animationDrawable12;
                animationDrawable12.start();
                break;
            case "8901 1":
                insertToLoaclesProvider(Croatian_LANGUAGE);
                setContentView(this.mZTERemoteAnimationControl.layout);
                this.bfPair = (ImageView) findViewById(this.mZTERemoteAnimationControl.bfPair);
                this.inPair = (ImageView) findViewById(this.mZTERemoteAnimationControl.inPair);
                this.mafPairMk = (TextView) findViewById(R.id.afPair_tv_mk);
                this.mafPairSq = (TextView) findViewById(R.id.afPair_tv_sq);
                this.mafPairEn = (TextView) findViewById(R.id.afPair_tv_en);
                this.mafPairMk.setVisibility(4);
                this.mafPairEn.setVisibility(4);
                this.mafPairSq.setText(getResources().getString(R.string.hr_bluetooth_tip));
                Log.d("[ControlDevice]", "Croatian_A1 customization animation");
                AnimationDrawable animationDrawable13 = (AnimationDrawable) this.bfPair.getBackground();
                animationDrawable = animationDrawable13;
                animationDrawable13.start();
                break;
            default:
                setContentView(this.mZTERemoteAnimationControl.layout);
                this.bfPair = (ImageView) findViewById(this.mZTERemoteAnimationControl.bfPair);
                this.inPair = (ImageView) findViewById(this.mZTERemoteAnimationControl.inPair);
                Log.d("[ControlDevice]", "Default animation");
                AnimationDrawable animationDrawable14 = (AnimationDrawable) this.bfPair.getBackground();
                animationDrawable = animationDrawable14;
                animationDrawable14.start();
                Log.d("[ControlDevice]", "[ZTERemoteActivity]<onCreate> Default");
                if ("1301 2".equals(this.MarketCode_OperatorCode)) {
                    this.mPairStartTvViettel = (TextView) findViewById(R.id.bfPair_tv);
                    break;
                } else if (MarketUtil.isFranceNetgem()) {
                    TextView textView = (TextView) findViewById(R.id.fr_pair_success_tip);
                    this.mPairSuccessNetgem = textView;
                    textView.setVisibility(4);
                    break;
                }
                break;
        }
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<onCreate> mPairingSuccess = " + mPairingSuccess);
        if (this.mBootFlag == 0) {
            if ("6701 1".equals(this.MarketCode_OperatorCode)) {
                DeviceManager.setDeviceName(this, "HIPERNET TV BOX");
            }
            if ("4501 2".equals(this.MarketCode_OperatorCode)) {
                DeviceManager.setDeviceName(this, "Yettel");
            }
            if ("8901 1".equals(this.MarketCode_OperatorCode)) {
                Log.d("[ControlDevice]", "[ZTERemoteActivity]<onCreate> Croatian_A1 set default language");
                LocalePicker.updateLocale(new Locale("hr", "HR"));
            }
            if ("5101 2".equals(this.MarketCode_OperatorCode)) {
                Log.d("[ControlDevice]", "[ZTERemoteActivity]<onCreate> Mk_A1 set default language");
                LocalePicker.updateLocale(new Locale("mk", "MK"));
            }
            if ("8701 1".equals(this.MarketCode_OperatorCode)) {
                DeviceManager.setDeviceName(this, "Elisa Viihde");
                insertElisalToLoaclesProvider(Elisal_LANGUAGE);
            }
            if (MarketUtil.isViettel_OTT_12()) {
                DeviceManager.setDeviceName(this, "TV360 4K Box");
            }
            if (MarketUtil.isTurkeyTele()) {
                DeviceManager.setDeviceName(getApplicationContext(), "B836CTSI-A21");
            }
            if (MarketUtil.isITALY_UPT24K()) {
                Log.d("[ControlDevice]", "italy market, init the audio mode and standby time");
                Settings.Global.putInt(getContentResolver(), "encoded_surround_output", 1);
                Settings.Secure.putInt(getContentResolver(), "sleep_timeout", 10800000);
            } else if (!"9999".equals(this.mMarket) || !"1".equals(this.mOperator)) {
                if ("9999 10".equals(this.MarketCode_OperatorCode)) {
                    Log.d("[ControlDevice]", "NETFLIXREF_OTT gtv location default on");
                    writeLocationMode(3);
                } else {
                    Log.d("[ControlDevice]", "not MTS market ,do default");
                }
            }
        } else {
            Log.d("[ControlDevice]", "not first launcher,do nothing");
        }
        this.mMsgHandler = new Handler() { // from class: com.control.device.settings.remote.ZTERemoteActivity.8
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                int i2 = message.what;
                if (i2 == 1) {
                    Log.d("[ControlDevice]", "[ZTERemoteActivity]<handleMessage> MSG_UPDATA_VIEW");
                    ZTERemoteActivity.this.UpdataView();
                } else {
                    if (i2 == 2) {
                        Log.d("[ControlDevice]", "[ZTERemoteActivity]<handleMessage> MSG_FINISH");
                        ZTERemoteActivity.this.FinishActivity();
                        return;
                    }
                    Log.d("[ControlDevice]", "[ZTERemoteActivity]<handleMessage> No handler case available for message:" + message.what);
                    ZTERemoteActivity.this.UpdataView();
                }
            }
        };
        if (!"3501".equals(this.mMarket) || "3".equals(this.mOperator) || MarketUtil.isFranceNetgem()) {
            return;
        }
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<onCreate> 3601 || 3501");
        FinishActivity();
    }

    @Override // android.app.Activity
    public void onStart() {
        super.onStart();
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<onStart> ");
        BluetoothDevicePairer bluetoothDevicePairer = new BluetoothDevicePairer(this, this);
        this.mBluetoothPairer = bluetoothDevicePairer;
        bluetoothDevicePairer.start();
        if ("4301 5".equals(this.MarketCode_OperatorCode)) {
            initView();
        }
        UpdataView();
        this.timer = new Timer();
        this.task = new TimerTask() { // from class: com.control.device.settings.remote.ZTERemoteActivity.9
            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                Log.d("[ControlDevice]", "[ZTERemoteActivity]<run> TimerTask");
                Message message = new Message();
                message.what = 1;
                ZTERemoteActivity.this.flashOnTimeHandler.sendMessage(message);
            }
        };
        if ("4301 6".equals(this.MarketCode_OperatorCode)) {
            this.timer.schedule(this.task, 5000L, 6000L);
        } else {
            this.timer.schedule(this.task, 5000L, 10000L);
        }
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<onBackPressed> ");
        try {
            this.isSetup = Settings.Secure.getInt(getApplicationContext().getContentResolver(), "user_setup_complete");
            Log.d("[ControlDevice]", "[ZTERemoteActivity]<onBackPressed> isSetup = " + this.isSetup);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<onBackPressed> isFailing = " + this.isFailing);
        if ("1101".equals(this.mMarket) && ("2".equals(this.mOperator) || "5".equals(this.mOperator))) {
            if (this.isFailing) {
                Log.d("[ControlDevice]", "[ZTERemoteActivity]<onBackPressed> in Failing, back to try again!");
                this.isFailing = false;
                stopBluetoothPairer();
                this.flashOnTimeHandler.removeCallbacksAndMessages(null);
                this.mMsgHandler.removeCallbacksAndMessages(null);
                recreate();
                return;
            }
            Log.d("[ControlDevice]", "[ZTERemoteActivity]<onBackPressed> exit");
            stopBluetoothPairer();
            this.flashOnTimeHandler.removeCallbacksAndMessages(null);
            this.mMsgHandler.removeCallbacksAndMessages(null);
            RemoveActivity();
            super.onBackPressed();
            return;
        }
        if (this.isSetup == 1 && "3601".equals(this.mMarket) && "1".equals(this.mOperator)) {
            Log.d("[ControlDevice]", "[ZTERemoteActivity]<onBackPressed> SetupWraith had been completed");
            stopBluetoothPairer();
            this.flashOnTimeHandler.removeCallbacksAndMessages(null);
            this.mMsgHandler.removeCallbacksAndMessages(null);
            RemoveActivity();
            super.onBackPressed();
            return;
        }
        if (this.isSetup == 0 && ProductUtils.isB866V5W11()) {
            Log.d("[ControlDevice]", "[ZTERemoteActivity] <onBackPressed> <B866V5W11> SetupWraith had not been completed.");
            return;
        }
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<onBackPressed> exit");
        stopBluetoothPairer();
        this.flashOnTimeHandler.removeCallbacksAndMessages(null);
        this.mMsgHandler.removeCallbacksAndMessages(null);
        RemoveActivity();
        super.onBackPressed();
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<onKeyDown> localKeyCode = " + keyCode);
        if ("3601".equals(this.mMarket) && "1".equals(this.mOperator)) {
            boolean isFocused = this.bfPair_button.isFocused();
            Log.d("[ControlDevice]", "[ZTERemoteActivity]<onKeyDown> buttonIsFocus = " + isFocused);
            if (this.bfPair_button.getVisibility() == 0) {
                if (keyCode == 22) {
                    Log.d("[ControlDevice]", "[ZTERemoteActivity]<onKeyDown> request Focus");
                    this.bfPair_button.requestFocus();
                } else if (keyCode == 23 && isFocused) {
                    Log.d("[ControlDevice]", "[ZTERemoteActivity]<onKeyDown> to SKIP");
                    stopBluetoothPairer();
                    this.flashOnTimeHandler.removeCallbacksAndMessages(null);
                    this.mMsgHandler.removeCallbacksAndMessages(null);
                    FinishActivity();
                } else if (keyCode == 23 && !isFocused) {
                    Log.d("[ControlDevice]", "[ZTERemoteActivity]<onKeyDown> KEYCODE_DPAD_CENTER, loss Focus");
                    this.bfPair_text.requestFocus();
                } else {
                    Log.d("[ControlDevice]", "[ZTERemoteActivity]<onKeyDown> loss Focus");
                    this.bfPair_text.requestFocus();
                }
            }
        }
        return super.onKeyDown(i, keyEvent);
    }

    @Override // android.app.Activity
    public void onStop() {
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<onStop> ");
        super.onStop();
        if (this.timer != null) {
            Log.d("[ControlDevice]", "[ZTERemoteActivity]<onStop> timer.cancel");
            this.timer.cancel();
        }
        stopBluetoothPairer();
        this.flashOnTimeHandler.removeCallbacksAndMessages(null);
        this.mMsgHandler.removeCallbacksAndMessages(null);
    }

    @Override // android.app.Activity
    public void onDestroy() {
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<onDestroy> ");
        super.onDestroy();
        if (this.timer != null) {
            this.timer = null;
        }
        stopBluetoothPairer();
        if (animationDrawable != null) {
            Log.d("[ControlDevice]", "[ZTERemoteActivity]<onDestroy> animationDrawable not null, now stop it");
            animationDrawable.stop();
        }
        this.flashOnTimeHandler.removeCallbacksAndMessages(null);
        this.mMsgHandler.removeCallbacksAndMessages(null);
        if ("3601 1".equals(this.MarketCode_OperatorCode)) {
            this.mContext.unregisterReceiver(this.mBtReceiver);
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // com.android.tv.settings.accessories.BluetoothDevicePairer.EventListener
    public void statusChanged() {
        char c;
        boolean z;
        boolean isConnected;
        Intent intent;
        char c2;
        boolean z2;
        char c3;
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> ");
        BluetoothDevicePairer bluetoothDevicePairer = this.mBluetoothPairer;
        if (bluetoothDevicePairer == null) {
            return;
        }
        int status = bluetoothDevicePairer.getStatus();
        int i = this.mPreviousStatus;
        this.mPreviousStatus = status;
        String name = this.mBluetoothPairer.getTargetDevice() == null ? "NONE" : this.mBluetoothPairer.getTargetDevice().getName();
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> RemoteName = " + name + ", oldStatus=" + i + ", status=" + status);
        if (status == -1) {
            Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_ERROR");
            if ("4301 6".equals(this.MarketCode_OperatorCode)) {
                onPairFailAndRecreate();
                return;
            }
            mPairingSuccess = false;
            this.isFailing = true;
            String str = this.MarketCode_OperatorCode;
            str.hashCode();
            switch (str.hashCode()) {
                case 1510511414:
                    if (str.equals("3501 3")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 1537293525:
                    if (str.equals("4301 5")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 1539140564:
                    if (str.equals("4501 2")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 1570540279:
                    if (str.equals("5801 3")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 1598245907:
                    if (str.equals("6701 1")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 1598245908:
                    if (str.equals("6701 2")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 1620410411:
                    if (str.equals("7001 1")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    animationDrawable.stop();
                    this.inPair.setBackgroundResource(this.mZTERemoteAnimationControl.pair_fail);
                    this.bfPair_button.setVisibility(4);
                    this.bfPair_text.setVisibility(4);
                    break;
                case 1:
                    this.mTextView_tip.setText(R.string.bluetooth_pair_fail);
                    break;
                case 2:
                    Log.d("[ControlDevice]", "<statusChanged> Hungary_OTT Customization STATUS_ERROR");
                    animationDrawable.stop();
                    this.inPair.setBackgroundResource(this.mZTERemoteAnimationControl.pair_fail);
                    Settings.Global.putString(getContentResolver(), "is_need_to_skip", "1");
                    break;
                case 3:
                case 6:
                    animationDrawable.stop();
                    this.inPair.setBackgroundResource(R.drawable.bluetooth_af_pair_failed_italy_0401);
                    this.mPairEndTv.setText("    " + getResources().getString(R.string.italy_bluetooth_tips4));
                    this.mPairEndTv.setTextColor(getResources().getColor(R.color.colorPairFailure));
                    this.mPairEndTvItaly.setText("    " + getResources().getString(R.string.italy_bluetooth_tips4_italy));
                    this.mPairEndTvItaly.setTextColor(getResources().getColor(R.color.colorPairFailure));
                    this.mPairStartTv.setVisibility(4);
                    this.mPairStartTvItaly.setVisibility(4);
                    this.mPairEndTv.setVisibility(0);
                    this.mPairEndTvItaly.setVisibility(0);
                    break;
                case 4:
                    Log.d("[ControlDevice]", "<statusChanged> Czech_OTT Customization STATUS_ERROR");
                    animationDrawable.stop();
                    this.inPair.setBackgroundResource(this.mZTERemoteAnimationControl.pair_fail);
                    Settings.Global.putString(getContentResolver(), "is_need_to_skip", "1");
                    break;
                case 5:
                    Log.d("[ControlDevice]", "<statusChanged> Czech_O2 Customization STATUS_ERROR");
                    animationDrawable.stop();
                    this.inPair.setBackgroundResource(this.mZTERemoteAnimationControl.pair_fail);
                    Settings.Global.putString(getContentResolver(), "is_need_to_skip", "1");
                    break;
                default:
                    Log.d("[ControlDevice]", "<statusChanged> STATUS_ERROR, Default");
                    animationDrawable.stop();
                    this.inPair.setBackgroundResource(this.mZTERemoteAnimationControl.pair_fail);
                    if ("4701 2".equals(this.MarketCode_OperatorCode)) {
                        this.mPair_text.setVisibility(4);
                        this.mPair_text_Th.setVisibility(4);
                        break;
                    } else if ("1101 4".equals(this.MarketCode_OperatorCode)) {
                        this.mPair_text.setVisibility(4);
                        this.mAf_pair_text.setText(R.string.er_pair_failed);
                        this.mAf_pair_text.setVisibility(0);
                        break;
                    } else if ("1301 2".equals(this.MarketCode_OperatorCode)) {
                        this.mPairStartTvViettel.setVisibility(4);
                        break;
                    } else if ("1801 4".equals(this.MarketCode_OperatorCode)) {
                        this.mafPairMk.setVisibility(4);
                        this.mafPairEn.setVisibility(4);
                        this.mafPairSq.setVisibility(4);
                        break;
                    } else if ("8901 1".equals(this.MarketCode_OperatorCode)) {
                        this.mafPairMk.setVisibility(4);
                        this.mafPairEn.setVisibility(4);
                        this.mafPairSq.setVisibility(4);
                        break;
                    } else if ("5101 2".equals(this.MarketCode_OperatorCode)) {
                        this.mafPairMk.setVisibility(4);
                        this.mafPairEn.setVisibility(4);
                        this.mafPairSq.setVisibility(4);
                        break;
                    } else if ("3601 1".equals(this.MarketCode_OperatorCode)) {
                        this.bfPair_text.setVisibility(4);
                        this.bfPair_button.setVisibility(4);
                        break;
                    } else if (MarketUtil.isFranceNetgem()) {
                        this.mPairSuccessNetgem.setVisibility(4);
                        break;
                    }
                    break;
            }
            Log.d("[ControlDevice]", "[ZTERemoteActivity]<run> STATUS_ERROR, postDelayed 5s, recreate Activity");
            new Handler().postDelayed(new Runnable() { // from class: com.control.device.settings.remote.ZTERemoteActivity.12
                @Override // java.lang.Runnable
                public void run() {
                    Log.d("[ControlDevice]", "[ZTERemoteActivity]<run> STATUS_ERROR, recreate Activity now");
                    Log.d("[ControlDevice]", "[ZTERemoteActivity]<run> isFailing = " + ZTERemoteActivity.this.isFailing);
                    if (ZTERemoteActivity.this.isFailing) {
                        ZTERemoteActivity.this.stopBluetoothPairer();
                        ZTERemoteActivity.this.recreate();
                    }
                }
            }, 5000L);
            return;
        }
        if (status == 0) {
            Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_NONE");
            if ("4301 6".equals(this.MarketCode_OperatorCode)) {
                if (i == 2) {
                    Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_NONE, oldStatus=STATUS_WAITING_TO_PAIR");
                    onPairFailAndRecreate();
                    return;
                } else {
                    if (i == 4) {
                        Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_NONE, oldStatus=STATUS_CONNECTING");
                        onPairSuccessAndFinish();
                        return;
                    }
                    return;
                }
            }
            return;
        }
        if (status == 1) {
            Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_SCANNING");
            if ("4301 5".equals(this.MarketCode_OperatorCode)) {
                return;
            }
            mPairingSuccess = false;
            if (this.mBtAdapter != null) {
                Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_SCANNING, mBtAdapter != null");
                if (!ProductUtils.isB866V5W11() || (intent = getIntent()) == null) {
                    z = false;
                } else {
                    z = intent.getBooleanExtra("add_a_new_remote", false);
                    Log.d("[ControlDevice]", "[ZTERemoteActivity] <B866V5W11> is new remote: " + z);
                }
                Set<BluetoothDevice> bondedDevices = this.mBtAdapter.getBondedDevices();
                if (bondedDevices != null) {
                    Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_SCANNING, bondedDevices != null");
                    Iterator<BluetoothDevice> it = bondedDevices.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        BluetoothDevice next = it.next();
                        Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_SCANNING, deviceName=" + next.getName() + ", RemoteName=" + name + ", bfmPairingSuccess=" + mPairingSuccess);
                        if ("4301 6".equals(this.MarketCode_OperatorCode)) {
                            isConnected = next.isConnected();
                        } else {
                            isConnected = (next.getName().contains(this.containName) || next.getName().contains("ZTE")) ? next.isConnected() : false;
                        }
                        if (isConnected && isInputDevice(next.getBluetoothClass())) {
                            Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_SCANNING, device:" + next.getAddress() + " is connected");
                            if (!ProductUtils.isB866V5W11()) {
                                mPairingSuccess = true;
                            } else if (!z) {
                                mPairingSuccess = true;
                            }
                        }
                    }
                }
            }
            if (!mPairingSuccess) {
                Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_SCANNING, Pairing unSuccess or just begin");
                if ("4301 6".equals(this.MarketCode_OperatorCode)) {
                    onPairStart();
                    return;
                }
                return;
            }
            Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_SCANNING, Pairing Success");
            if ("4301 6".equals(this.MarketCode_OperatorCode) || "3601 1".equals(this.MarketCode_OperatorCode)) {
                onPairSuccessAndFinish();
                return;
            }
            this.inPair.setBackgroundResource(this.mZTERemoteAnimationControl.pair_success);
            if (!"1101".equals(this.mMarket) || !"2".equals(this.mOperator)) {
                if (MarketUtil.isITALY_UPT24K() || MarketUtil.isTurkeyTele()) {
                    Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_SCANNING,Pairing Success TSD");
                    this.mPairEndTv.setText(getResources().getString(R.string.italy_bluetooth_tips3));
                    this.mPairEndTv.setTextColor(getResources().getColor(R.color.colorPairSuccessful));
                    this.mPairEndTvItaly.setText(getResources().getString(R.string.italy_bluetooth_tips3_italy));
                    this.mPairEndTvItaly.setTextColor(getResources().getColor(R.color.colorPairSuccessful));
                    this.mPairStartTv.setVisibility(4);
                    this.mPairStartTvItaly.setVisibility(4);
                    this.mPairEndTv.setVisibility(0);
                    this.mPairEndTvItaly.setVisibility(0);
                } else if ("1301 2".equals(this.MarketCode_OperatorCode)) {
                    this.mPairStartTvViettel.setVisibility(4);
                } else if ("1801 4".equals(this.MarketCode_OperatorCode)) {
                    this.mafPairMk.setVisibility(4);
                    this.mafPairEn.setVisibility(4);
                    this.mafPairSq.setVisibility(4);
                } else if ("8901 1".equals(this.MarketCode_OperatorCode)) {
                    this.mafPairMk.setVisibility(4);
                    this.mafPairEn.setVisibility(4);
                    this.mafPairSq.setVisibility(4);
                } else if ("5101 2".equals(this.MarketCode_OperatorCode)) {
                    this.mafPairMk.setVisibility(4);
                    this.mafPairEn.setVisibility(4);
                    this.mafPairSq.setVisibility(4);
                } else if ("3501 3".equals(this.MarketCode_OperatorCode)) {
                    this.bfPair_button.setVisibility(4);
                    this.bfPair_text.setVisibility(4);
                } else if (MarketUtil.isFranceNetgem()) {
                    this.mPairSuccessNetgem.setVisibility(0);
                }
            }
            Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_SCANNING, postDelayed 5s, finish Activity");
            new Handler().postDelayed(new Runnable() { // from class: com.control.device.settings.remote.ZTERemoteActivity.10
                @Override // java.lang.Runnable
                public void run() {
                    Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_SCANNING, finish Activity now");
                    ZTERemoteActivity.this.FinishActivity();
                }
            }, 5000L);
            return;
        }
        if (status == 2) {
            Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_WAITING_TO_PAIR, mPairingSuccess = " + mPairingSuccess);
            if ("4301 5".equals(this.MarketCode_OperatorCode)) {
                this.mTextView_subTip.setText(R.string.bluetooth_pair_start);
                return;
            }
            if ("4301 6".equals(this.MarketCode_OperatorCode)) {
                onPairWaitingToPair();
                return;
            }
            if (mPairingSuccess) {
                Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_WAITING_TO_PAIR, break");
                return;
            }
            if (!name.contains(this.containName) && !name.contains("ZTE")) {
                Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_WAITING_TO_PAIR, not contains " + this.containName + ", now break!");
                BluetoothDevicePairer bluetoothDevicePairer2 = this.mBluetoothPairer;
                if (bluetoothDevicePairer2 != null) {
                    bluetoothDevicePairer2.cancelPairing();
                }
                mPairingSuccess = false;
                return;
            }
            Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_WAITING_TO_PAIR, go on!");
            if ("1101".equals(this.mMarket) && ("2".equals(this.mOperator) || "5".equals(this.mOperator))) {
                Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_WAITING_TO_PAIR, MTS");
                this.inPair.setBackgroundResource(this.mZTERemoteAnimationControl.pairing);
                return;
            }
            if (MarketUtil.isITALY_UPT24K() || MarketUtil.isTurkeyTele()) {
                Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_WAITING_TO_PAIR, TSD");
                animationDrawable.stop();
                this.inPair.setBackgroundResource(this.mZTERemoteAnimationControl.pairing);
                AnimationDrawable animationDrawable2 = (AnimationDrawable) this.inPair.getBackground();
                animationDrawable = animationDrawable2;
                animationDrawable2.start();
                this.mPairStartTv.setText(getResources().getString(R.string.italy_bluetooth_tips2));
                this.mPairStartTvItaly.setText(getResources().getString(R.string.italy_bluetooth_tips2_italy));
                this.mPairEndTv.setVisibility(4);
                this.mPairEndTvItaly.setVisibility(4);
                this.mPairStartTv.setVisibility(0);
                this.mPairStartTvItaly.setVisibility(0);
                return;
            }
            if ("1301 2".equals(this.MarketCode_OperatorCode)) {
                this.mPairStartTvViettel.setVisibility(4);
                return;
            }
            if ("1801 4".equals(this.MarketCode_OperatorCode)) {
                this.mafPairMk.setVisibility(4);
                this.mafPairEn.setVisibility(4);
                this.mafPairSq.setVisibility(4);
                return;
            }
            if ("8901 1".equals(this.MarketCode_OperatorCode)) {
                this.mafPairMk.setVisibility(4);
                this.mafPairEn.setVisibility(4);
                this.mafPairSq.setVisibility(4);
                return;
            }
            if ("5101 2".equals(this.MarketCode_OperatorCode)) {
                this.mafPairMk.setVisibility(4);
                this.mafPairEn.setVisibility(4);
                this.mafPairSq.setVisibility(4);
                return;
            }
            if (MarketUtil.isFranceNetgem()) {
                this.mPairSuccessNetgem.setVisibility(4);
                return;
            }
            Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_WAITING_TO_PAIR, Default");
            animationDrawable.stop();
            if (("3601".equals(this.mMarket) && "1".equals(this.mOperator)) || "3501 3".equals(this.MarketCode_OperatorCode)) {
                Log.d("[ControlDevice]", "[ZTERemoteActivity]<onCreate> Start pair, hide Tip!");
                this.bfPair_text.setVisibility(4);
                this.bfPair_button.setVisibility(4);
            }
            this.inPair.setBackgroundResource(this.mZTERemoteAnimationControl.pairing);
            AnimationDrawable animationDrawable3 = (AnimationDrawable) this.inPair.getBackground();
            animationDrawable = animationDrawable3;
            animationDrawable3.start();
            return;
        }
        if (status == 3) {
            Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_PAIRING");
            if ("4301 6".equals(this.MarketCode_OperatorCode)) {
                onPairPairing();
                return;
            }
            String str2 = this.MarketCode_OperatorCode;
            str2.hashCode();
            switch (str2.hashCode()) {
                case 1510511414:
                    if (str2.equals("3501 3")) {
                        c2 = 0;
                        break;
                    }
                    c2 = 65535;
                    break;
                case 1537293525:
                    if (str2.equals("4301 5")) {
                        c2 = 1;
                        break;
                    }
                    c2 = 65535;
                    break;
                case 1570540279:
                    if (str2.equals("5801 3")) {
                        c2 = 2;
                        break;
                    }
                    c2 = 65535;
                    break;
                case 1620410411:
                    if (str2.equals("7001 1")) {
                        c2 = 3;
                        break;
                    }
                    c2 = 65535;
                    break;
                default:
                    c2 = 65535;
                    break;
            }
            switch (c2) {
                case 0:
                    this.inPair.setBackgroundResource(this.mZTERemoteAnimationControl.pairing);
                    AnimationDrawable animationDrawable4 = (AnimationDrawable) this.inPair.getBackground();
                    animationDrawable = animationDrawable4;
                    animationDrawable4.start();
                    this.bfPair_button.setVisibility(4);
                    this.bfPair_text.setVisibility(4);
                    z2 = true;
                    break;
                case 1:
                    this.mTextView_tip.setText(R.string.bluetooth_pair_in);
                    this.mTextView_subTip.setVisibility(4);
                    Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_PAIRING, Megacable-OTT");
                    z2 = true;
                    mPairingSuccess = true;
                    break;
                case 2:
                case 3:
                    this.inPair.setBackgroundResource(R.drawable.bluetooth_pairing_pairing_italy);
                    AnimationDrawable animationDrawable5 = (AnimationDrawable) this.inPair.getBackground();
                    animationDrawable = animationDrawable5;
                    animationDrawable5.start();
                    this.mPairStartTv.setText(getResources().getString(R.string.italy_bluetooth_tips2));
                    this.mPairStartTvItaly.setText(getResources().getString(R.string.italy_bluetooth_tips2_italy));
                    this.mPairEndTv.setVisibility(4);
                    this.mPairEndTvItaly.setVisibility(4);
                    this.mPairStartTv.setVisibility(0);
                    this.mPairStartTvItaly.setVisibility(0);
                    z2 = true;
                    break;
                default:
                    this.inPair.setBackgroundResource(this.mZTERemoteAnimationControl.pairing);
                    if ("4701 2".equals(this.MarketCode_OperatorCode)) {
                        this.mPair_text.setVisibility(0);
                        this.mPair_text_Th.setVisibility(0);
                    } else if ("1301 2".equals(this.MarketCode_OperatorCode)) {
                        this.mPairStartTvViettel.setVisibility(4);
                    } else if ("1801 4".equals(this.MarketCode_OperatorCode)) {
                        this.mafPairMk.setVisibility(4);
                        this.mafPairEn.setVisibility(4);
                        this.mafPairSq.setVisibility(4);
                    } else if ("8901 1".equals(this.MarketCode_OperatorCode)) {
                        this.mafPairMk.setVisibility(4);
                        this.mafPairEn.setVisibility(4);
                        this.mafPairSq.setVisibility(4);
                    } else if ("5101 2".equals(this.MarketCode_OperatorCode)) {
                        this.mafPairMk.setVisibility(4);
                        this.mafPairEn.setVisibility(4);
                        this.mafPairSq.setVisibility(4);
                    } else if ("3601 1".equals(this.MarketCode_OperatorCode)) {
                        this.bfPair_text.setVisibility(4);
                        this.bfPair_button.setVisibility(4);
                    } else if (MarketUtil.isFranceNetgem()) {
                        this.mPairSuccessNetgem.setVisibility(4);
                    }
                    AnimationDrawable animationDrawable6 = (AnimationDrawable) this.inPair.getBackground();
                    animationDrawable = animationDrawable6;
                    animationDrawable6.start();
                    z2 = true;
                    break;
            }
            mPairingSuccess = z2;
            return;
        }
        if (status != 4) {
            return;
        }
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_CONNECTING");
        if ("4301 6".equals(this.MarketCode_OperatorCode) || "3601 1".equals(this.MarketCode_OperatorCode)) {
            onPairConnecting();
            return;
        }
        mPairingSuccess = true;
        String str3 = this.MarketCode_OperatorCode;
        str3.hashCode();
        switch (str3.hashCode()) {
            case 1510511414:
                if (str3.equals("3501 3")) {
                    c3 = 0;
                    break;
                }
                c3 = 65535;
                break;
            case 1537293525:
                if (str3.equals("4301 5")) {
                    c3 = 1;
                    break;
                }
                c3 = 65535;
                break;
            case 1570540279:
                if (str3.equals("5801 3")) {
                    c3 = 2;
                    break;
                }
                c3 = 65535;
                break;
            case 1620410411:
                if (str3.equals("7001 1")) {
                    c3 = 3;
                    break;
                }
                c3 = 65535;
                break;
            default:
                c3 = 65535;
                break;
        }
        switch (c3) {
            case 0:
                animationDrawable.stop();
                this.inPair.setBackgroundResource(this.mZTERemoteAnimationControl.pair_success);
                this.bfPair_button.setVisibility(4);
                this.bfPair_text.setVisibility(4);
                break;
            case 1:
                this.mTextView_tip.setText(R.string.bluetooth_pair_success);
                Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_CONNECTING, Megacable-OTT");
                break;
            case 2:
            case 3:
                Log.d("[ControlDevice]", "<statusChanged> STATUS_CONNECTING, ITALY");
                animationDrawable.stop();
                this.inPair.setBackgroundResource(R.drawable.bluetooth_af_pair_success_italy_0401);
                this.mPairEndTv.setText(getResources().getString(R.string.italy_bluetooth_tips3));
                this.mPairEndTv.setTextColor(getResources().getColor(R.color.colorPairSuccessful));
                this.mPairEndTvItaly.setText(getResources().getString(R.string.italy_bluetooth_tips3_italy));
                this.mPairEndTvItaly.setTextColor(getResources().getColor(R.color.colorPairSuccessful));
                this.mPairStartTv.setVisibility(4);
                this.mPairStartTvItaly.setVisibility(4);
                this.mPairEndTv.setVisibility(0);
                this.mPairEndTvItaly.setVisibility(0);
                break;
            default:
                Log.d("[ControlDevice]", "<statusChanged> STATUS_CONNECTING, Default");
                animationDrawable.stop();
                this.inPair.setBackgroundResource(this.mZTERemoteAnimationControl.pair_success);
                if ("4701 2".equals(this.MarketCode_OperatorCode)) {
                    this.mPair_text.setVisibility(4);
                    this.mPair_text_Th.setVisibility(4);
                    break;
                } else if ("1101 4".equals(this.MarketCode_OperatorCode)) {
                    this.mPair_text.setVisibility(4);
                    this.mAf_pair_text.setVisibility(0);
                    break;
                } else if ("1301 2".equals(this.MarketCode_OperatorCode)) {
                    this.mPairStartTvViettel.setVisibility(4);
                    break;
                } else if ("1801 4".equals(this.MarketCode_OperatorCode)) {
                    this.mafPairMk.setVisibility(4);
                    this.mafPairEn.setVisibility(4);
                    this.mafPairSq.setVisibility(4);
                    break;
                } else if ("8901 1".equals(this.MarketCode_OperatorCode)) {
                    this.mafPairMk.setVisibility(4);
                    this.mafPairEn.setVisibility(4);
                    this.mafPairSq.setVisibility(4);
                    break;
                } else if ("5101 2".equals(this.MarketCode_OperatorCode)) {
                    this.mafPairMk.setVisibility(4);
                    this.mafPairEn.setVisibility(4);
                    this.mafPairSq.setVisibility(4);
                    break;
                } else if ("3601 1".equals(this.MarketCode_OperatorCode)) {
                    this.bfPair_text.setVisibility(4);
                    this.bfPair_button.setVisibility(4);
                    break;
                } else if (MarketUtil.isFranceNetgem()) {
                    this.mPairSuccessNetgem.setVisibility(0);
                    break;
                }
                break;
        }
        this.mMsgHandler.sendEmptyMessageDelayed(2, 3000L);
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_CONNECTING, postDelayed 5s, finish Activity");
        new Handler().postDelayed(new Runnable() { // from class: com.control.device.settings.remote.ZTERemoteActivity.11
            @Override // java.lang.Runnable
            public void run() {
                Log.d("[ControlDevice]", "[ZTERemoteActivity]<statusChanged> STATUS_CONNECTING, finish Activity now");
                ZTERemoteActivity.this.FinishActivity();
            }
        }, 5000L);
    }

    private void RemoveActivity() {
        setResult(1, null);
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<RemoveActivity> RemoveActivity SUCCESS");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void UpdataView() {
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<UpdataView> ");
        statusChanged();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void FinishActivity() {
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<FinishActivity> ");
        RemoveActivity();
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopBluetoothPairer() {
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<stopBluetoothPairer> ");
        this.mPreviousStatus = 0;
        BluetoothDevicePairer bluetoothDevicePairer = this.mBluetoothPairer;
        if (bluetoothDevicePairer != null) {
            bluetoothDevicePairer.setListener(null);
            this.mBluetoothPairer.dispose();
            this.mBluetoothPairer = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startBluetoothPairer() {
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<startBluetoothPairer> ");
        stopBluetoothPairer();
        BluetoothDevicePairer bluetoothDevicePairer = new BluetoothDevicePairer(this, this);
        this.mBluetoothPairer = bluetoothDevicePairer;
        bluetoothDevicePairer.start();
        statusChanged();
    }

    private void onPairStart() {
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<onPairStart>");
        AnimationDrawable animationDrawable2 = animationDrawable;
        if (animationDrawable2 != null) {
            animationDrawable2.stop();
        }
        ImageView imageView = this.inPair;
        if (imageView != null) {
            imageView.setBackgroundResource(this.mZTERemoteAnimationControl.pairing_start);
            AnimationDrawable animationDrawable3 = (AnimationDrawable) this.inPair.getBackground();
            animationDrawable = animationDrawable3;
            animationDrawable3.start();
        }
    }

    private void onPairWaitingToPair() {
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<onPairing>");
        AnimationDrawable animationDrawable2 = animationDrawable;
        if (animationDrawable2 != null) {
            animationDrawable2.stop();
        }
        ImageView imageView = this.inPair;
        if (imageView != null) {
            imageView.setBackgroundResource(this.mZTERemoteAnimationControl.pairing);
            AnimationDrawable animationDrawable3 = (AnimationDrawable) this.inPair.getBackground();
            animationDrawable = animationDrawable3;
            animationDrawable3.start();
        }
    }

    private void onPairPairing() {
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<onPairPairing>");
        this.mMsgHandler.postDelayed(new Runnable() { // from class: com.control.device.settings.remote.ZTERemoteActivity.14
            @Override // java.lang.Runnable
            public void run() {
                Log.d("[ControlDevice]", "[ZTERemoteActivity]<onPairPairing><run> pairing time out, mPreviousStatus=" + ZTERemoteActivity.this.mPreviousStatus);
                if (ZTERemoteActivity.this.mPreviousStatus != 3 || ZTERemoteActivity.this.mBluetoothPairer == null || ZTERemoteActivity.this.mBluetoothPairer.getTargetDevice() == null) {
                    return;
                }
                BluetoothDevice targetDevice = ZTERemoteActivity.this.mBluetoothPairer.getTargetDevice();
                Log.d("[ControlDevice]", "[ZTERemoteActivity]<onPairPairing><run> unpair the device, name=" + targetDevice.getName() + ", address=" + targetDevice.getAddress());
                ZTERemoteActivity.this.mBluetoothPairer.unpairDevice(targetDevice);
                ZTERemoteActivity.this.onPairFailAndRecreate();
            }
        }, 15000L);
    }

    private void onPairConnecting() {
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<onPairConnecting>");
        this.mMsgHandler.postDelayed(new Runnable() { // from class: com.control.device.settings.remote.ZTERemoteActivity.15
            @Override // java.lang.Runnable
            public void run() {
                Log.d("[ControlDevice]", "[ZTERemoteActivity]<onPairConnecting><run> connecting time out, mPreviousStatus=" + ZTERemoteActivity.this.mPreviousStatus);
                if (ZTERemoteActivity.this.mPreviousStatus != 4 || ZTERemoteActivity.this.mBluetoothPairer == null || ZTERemoteActivity.this.mBluetoothPairer.getTargetDevice() == null) {
                    return;
                }
                BluetoothDevice targetDevice = ZTERemoteActivity.this.mBluetoothPairer.getTargetDevice();
                Log.d("[ControlDevice]", "[ZTERemoteActivity]<onPairConnecting><run> unpair the device, name=" + targetDevice.getName() + ", address=" + targetDevice.getAddress());
                ZTERemoteActivity.this.mBluetoothPairer.unpairDevice(targetDevice);
                ZTERemoteActivity.this.onPairFailAndRecreate();
            }
        }, 15000L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onPairFailAndRecreate() {
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<onPairFailAndRecreate>");
        mPairingSuccess = false;
        stopBluetoothPairer();
        AnimationDrawable animationDrawable2 = animationDrawable;
        if (animationDrawable2 != null) {
            animationDrawable2.stop();
        }
        ImageView imageView = this.inPair;
        if (imageView != null) {
            imageView.setBackgroundResource(this.mZTERemoteAnimationControl.pair_fail);
        }
        new Handler().postDelayed(new Runnable() { // from class: com.control.device.settings.remote.ZTERemoteActivity.16
            @Override // java.lang.Runnable
            public void run() {
                Log.d("[ControlDevice]", "[ZTERemoteActivity]<onPairFailAndRecreate><run> recreate Activity now");
                ZTERemoteActivity.this.recreate();
            }
        }, 3000L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onPairSuccessAndFinish() {
        Log.d("[ControlDevice]", "[ZTERemoteActivity]<onPairSuccessAndFinish>");
        mPairingSuccess = true;
        stopBluetoothPairer();
        AnimationDrawable animationDrawable2 = animationDrawable;
        if (animationDrawable2 != null) {
            animationDrawable2.stop();
        }
        ImageView imageView = this.inPair;
        if (imageView != null) {
            imageView.setBackgroundResource(this.mZTERemoteAnimationControl.pair_success);
        }
        new Handler().postDelayed(new Runnable() { // from class: com.control.device.settings.remote.ZTERemoteActivity.17
            @Override // java.lang.Runnable
            public void run() {
                Log.d("[ControlDevice]", "[ZTERemoteActivity]<onPairSuccessAndFinish><run> finish Activity now");
                ZTERemoteActivity.this.FinishActivity();
            }
        }, 3000L);
    }

    private boolean isMatchingDeviceClass(int i) {
        return (((((MINOR_DEVICE_CLASS_POINTING | MINOR_DEVICE_CLASS_JOYSTICK) | MINOR_DEVICE_CLASS_GAMEPAD) | MINOR_DEVICE_CLASS_KEYBOARD) | MINOR_DEVICE_CLASS_REMOTE) & i) != 0;
    }

    private boolean isInputDevice(BluetoothClass bluetoothClass) {
        Log.d("[ControlDevice]", "[ZTERemoteActivity]MajorDeviceClass=" + bluetoothClass.getMajorDeviceClass() + ", DeviceClass=" + bluetoothClass.getDeviceClass());
        return isMatchingMajorDeviceClass(bluetoothClass.getMajorDeviceClass()) && isMatchingDeviceClass(bluetoothClass.getDeviceClass());
    }

    public void initView() {
        final Set<BluetoothDevice> bondedDevices = this.mBtAdapter.getBondedDevices();
        Log.d("[ControlDevice]", "bondedDevices = " + bondedDevices);
        Log.d("[ControlDevice]", "bondedDevices.size = " + bondedDevices.size());
        this.mLinearLayout = (LinearLayout) findViewById(R.id.ll);
        this.mLinearlayout_start = (LinearLayout) findViewById(R.id.ll_1);
        this.mLinearlayout_in = (LinearLayout) findViewById(R.id.ll_2);
        this.mLinearlayout_over = (LinearLayout) findViewById(R.id.ll_3);
        this.mBtnConnect = (Button) findViewById(R.id.connect);
        this.mBtnSkip = (Button) findViewById(R.id.skip);
        this.mBtnNext = (Button) findViewById(R.id.next);
        this.mTextView_tip = (TextView) findViewById(R.id.tv3);
        this.mTextView_subTip = (TextView) findViewById(R.id.tv4);
        this.mBtnConnect.setOnClickListener(new View.OnClickListener() { // from class: com.control.device.settings.remote.ZTERemoteActivity.18
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                boolean z;
                if (bondedDevices.size() > 0) {
                    Iterator it = bondedDevices.iterator();
                    while (it.hasNext()) {
                        if (((BluetoothDevice) it.next()).getType() == 2) {
                            z = true;
                            break;
                        }
                    }
                }
                z = false;
                if (z) {
                    ZTERemoteActivity.this.mLinearlayout_start.setVisibility(8);
                    ZTERemoteActivity.this.mLinearlayout_in.setVisibility(8);
                } else {
                    ZTERemoteActivity.this.mLinearlayout_start.setVisibility(8);
                }
            }
        });
        this.mBtnSkip.setOnClickListener(new View.OnClickListener() { // from class: com.control.device.settings.remote.ZTERemoteActivity.19
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                ZTERemoteActivity.this.finish();
            }
        });
        this.mBtnNext.setOnClickListener(new View.OnClickListener() { // from class: com.control.device.settings.remote.ZTERemoteActivity.20
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                ZTERemoteActivity.this.finish();
            }
        });
    }

    private void insertToLoaclesProvider(String[] strArr) {
        Log.d("[ControlDevice]", "insertToLoaclesProvider");
        ArrayList arrayList = new ArrayList();
        if (strArr.length > 0) {
            int i = 0;
            for (String str : strArr) {
                arrayList.add(str);
                Log.d("[ControlDevice]", "language = " + str);
            }
            while (i < arrayList.size()) {
                String str2 = (String) arrayList.get(i);
                ContentValues contentValues = new ContentValues();
                contentValues.put("locale", str2);
                i++;
                contentValues.put("rank", Integer.valueOf(i));
                getContentResolver().insert(this.CONTENT_URI, contentValues);
            }
            ContentValues contentValues2 = new ContentValues();
            contentValues2.put("locale", "*");
            contentValues2.put("rank", (Integer) 1);
            getContentResolver().insert(this.CONTENT_URI, contentValues2);
            return;
        }
        Log.d("[ControlDevice]", "empty list ,do nothing");
    }

    private void insertElisalToLoaclesProvider(String[] strArr) {
        Log.d("[ControlDevice]", "insertElisalToLoaclesProvider");
        ArrayList arrayList = new ArrayList();
        if (strArr.length > 0) {
            for (String str : strArr) {
                arrayList.add(str);
                Log.d("[ControlDevice]", "language = " + str);
            }
            int i = 0;
            while (i < arrayList.size()) {
                String str2 = (String) arrayList.get(i);
                ContentValues contentValues = new ContentValues();
                contentValues.put("locale", str2);
                i++;
                contentValues.put("rank", Integer.valueOf(i));
                getContentResolver().insert(this.CONTENT_URI, contentValues);
            }
            ContentValues contentValues2 = new ContentValues();
            contentValues2.put("locale", "*");
            contentValues2.put("rank", (Integer) 0);
            getContentResolver().insert(this.CONTENT_URI, contentValues2);
            return;
        }
        Log.d("[ControlDevice]", "empty list ,do nothing");
    }

    private void writeLocationMode(int i) {
        int i2 = Settings.Secure.getInt(getContentResolver(), "location_mode", 0);
        Intent intent = new Intent("com.android.settings.location.MODE_CHANGING");
        intent.putExtra("CURRENT_MODE", i2);
        intent.putExtra("NEW_MODE", i);
        sendBroadcast(intent, "android.permission.WRITE_SECURE_SETTINGS");
        ((LocationManager) getSystemService(LocationManager.class)).setLocationEnabledForUser(i != 0, Process.myUserHandle());
    }
}
