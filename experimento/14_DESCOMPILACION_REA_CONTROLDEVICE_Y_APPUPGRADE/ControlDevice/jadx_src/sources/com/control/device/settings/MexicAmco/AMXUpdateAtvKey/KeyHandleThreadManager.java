package com.control.device.settings.MexicAmco.AMXUpdateAtvKey;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import androidx.constraintlayout.widget.R$styleable;
import com.zte.android.productcfg.ZteMcspCfgManager;

/* loaded from: classes.dex */
public class KeyHandleThreadManager {
    private static ZteMcspCfgManager mZteMcspCfgManager;
    private AttestationKeyUtil mAttestationKeyUtil;
    private Context mContext;
    private HandlerThread mHandlerThread;
    private String mLabel;
    private EventListener mListener;
    private WidevineKeyUtil mWidevineKeyUtil;
    private WorkHandle mWorkHandle;
    private final Object mLock = new Object();
    private int mPreStep = 0;
    private int mCurStep = 0;
    private boolean mWorkingStatus = false;
    private long THREAD_SLEEP_TIME_1S = 1000;
    private long THREAD_SLEEP_TIME_2S = 2000;
    private long THREAD_SLEEP_TIME_3S = 3000;
    private long THREAD_SLEEP_TIME_4S = 4000;
    private long THREAD_SLEEP_TIME_5S = 5000;

    public interface EventListener {
        void onStatusChanged(int i, int i2, boolean z);
    }

    private KeyHandleThreadManager(Context context, EventListener eventListener) {
        Log.i("KeyHandleThreadManager", "new KeyHandleThreadManager");
        this.mContext = context.getApplicationContext();
        this.mListener = eventListener;
        this.mAttestationKeyUtil = new AttestationKeyUtil(this.mContext);
        this.mWidevineKeyUtil = new WidevineKeyUtil(this.mContext);
        mZteMcspCfgManager = ZteMcspCfgManager.getInstance(this.mContext);
        HandlerThread handlerThread = new HandlerThread("key_handle_thread");
        this.mHandlerThread = handlerThread;
        handlerThread.start();
        this.mWorkHandle = new WorkHandle(this.mHandlerThread.getLooper());
        this.mLabel = toString();
    }

    public static KeyHandleThreadManager getInstance(Context context, EventListener eventListener) {
        if (context == null) {
            Log.e("KeyHandleThreadManager", "context is null");
            return null;
        }
        return new KeyHandleThreadManager(context, eventListener);
    }

    public void start() {
        synchronized (this.mLock) {
            this.mWorkingStatus = true;
        }
    }

    public boolean isWorking() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mWorkingStatus;
        }
        return z;
    }

    public void quit() {
        synchronized (this.mLock) {
            this.mWorkingStatus = false;
        }
        removeAllMessages();
        this.mHandlerThread.quitSafely();
    }

    public void start_attestationKey_check() {
        Log.i("KeyHandleThreadManager", "start_attestationKey_check");
        if (!isWorking()) {
            Log.i("KeyHandleThreadManager", "TAG=" + this.mLabel + " is stoped");
            return;
        }
        removeAllMessages();
        this.mPreStep = 201;
        this.mWorkHandle.sendEmptyMessage(201);
    }

    public void start_attestationKey_get() {
        Log.i("KeyHandleThreadManager", "start_attestationKey_get");
        if (!isWorking()) {
            Log.i("KeyHandleThreadManager", "TAG=" + this.mLabel + " is stoped");
            return;
        }
        removeAllMessages();
        this.mPreStep = 202;
        this.mWorkHandle.sendEmptyMessage(202);
    }

    public void start_attestationKey_hashCheck() {
        Log.i("KeyHandleThreadManager", "start_attestationKey_hashCheck");
        if (!isWorking()) {
            Log.i("KeyHandleThreadManager", "TAG=" + this.mLabel + " is stoped");
            return;
        }
        removeAllMessages();
        this.mPreStep = 203;
        this.mWorkHandle.sendEmptyMessage(203);
    }

    public void start_attestationKey_install() {
        Log.i("KeyHandleThreadManager", "start_attestationKey_install");
        if (!isWorking()) {
            Log.i("KeyHandleThreadManager", "TAG=" + this.mLabel + " is stoped");
            return;
        }
        removeAllMessages();
        this.mPreStep = 204;
        this.mWorkHandle.sendEmptyMessage(204);
    }

    public void start_widevineKey_check() {
        Log.i("KeyHandleThreadManager", "start_widevineKey_check");
        if (!isWorking()) {
            Log.i("KeyHandleThreadManager", "TAG=" + this.mLabel + " is stoped");
            return;
        }
        removeAllMessages();
        this.mPreStep = 301;
        this.mWorkHandle.sendEmptyMessage(301);
    }

    public void start_widevineKey_get() {
        Log.i("KeyHandleThreadManager", "start_widevineKey_get");
        if (!isWorking()) {
            Log.i("KeyHandleThreadManager", "TAG=" + this.mLabel + " is stoped");
            return;
        }
        removeAllMessages();
        this.mPreStep = 302;
        this.mWorkHandle.sendEmptyMessage(302);
    }

    public void start_widevineKey_hashCheck() {
        Log.i("KeyHandleThreadManager", "start_widevineKey_hashCheck");
        if (!isWorking()) {
            Log.i("KeyHandleThreadManager", "TAG=" + this.mLabel + " is stoped");
            return;
        }
        removeAllMessages();
        this.mPreStep = 303;
        this.mWorkHandle.sendEmptyMessage(303);
    }

    public void start_widevineKey_install() {
        Log.i("KeyHandleThreadManager", "start_widevineKey_install");
        if (!isWorking()) {
            Log.i("KeyHandleThreadManager", "TAG=" + this.mLabel + " is stoped");
            return;
        }
        removeAllMessages();
        this.mPreStep = 304;
        this.mWorkHandle.sendEmptyMessage(304);
    }

    public void start_factory_reset() {
        Log.i("KeyHandleThreadManager", "start_factory_reset");
        if (this.mWorkHandle.hasMessages(400)) {
            this.mWorkHandle.removeMessages(400);
        }
        removeAllMessages();
        this.mPreStep = 400;
        this.mWorkHandle.sendEmptyMessage(400);
    }

    private class WorkHandle extends Handler {
        public WorkHandle(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            Log.i("KeyHandleThreadManager", "WorkHandle: msg.what=" + message.what);
            if (KeyHandleThreadManager.this.mListener == null || KeyHandleThreadManager.this.mAttestationKeyUtil == null || KeyHandleThreadManager.this.mWidevineKeyUtil == null) {
                Log.e("KeyHandleThreadManager", "Error, listener, attestation util and widevine util is not ready");
                return;
            }
            int i = message.what;
            if (i == 101) {
                KeyHandleThreadManager.this.mCurStep = R$styleable.Constraint_layout_goneMarginRight;
                KeyHandleThreadManager keyHandleThreadManager = KeyHandleThreadManager.this;
                if (keyHandleThreadManager.sleep_and_continue(keyHandleThreadManager.THREAD_SLEEP_TIME_2S)) {
                    KeyHandleThreadManager keyHandleThreadManager2 = KeyHandleThreadManager.this;
                    keyHandleThreadManager2.updateEventListener(100, R$styleable.Constraint_layout_goneMarginRight, keyHandleThreadManager2.isInternetValidated());
                    return;
                }
                return;
            }
            if (i == 102) {
                KeyHandleThreadManager.this.mCurStep = R$styleable.Constraint_layout_goneMarginStart;
                KeyHandleThreadManager keyHandleThreadManager3 = KeyHandleThreadManager.this;
                if (keyHandleThreadManager3.sleep_and_continue(keyHandleThreadManager3.THREAD_SLEEP_TIME_2S)) {
                    KeyHandleThreadManager keyHandleThreadManager4 = KeyHandleThreadManager.this;
                    keyHandleThreadManager4.updateEventListener(100, R$styleable.Constraint_layout_goneMarginStart, keyHandleThreadManager4.isNetworkConnected());
                    return;
                }
                return;
            }
            if (i != 400) {
                switch (i) {
                    case 201:
                        KeyHandleThreadManager.this.mCurStep = 201;
                        KeyHandleThreadManager keyHandleThreadManager5 = KeyHandleThreadManager.this;
                        if (keyHandleThreadManager5.sleep_and_continue(keyHandleThreadManager5.THREAD_SLEEP_TIME_2S)) {
                            KeyHandleThreadManager keyHandleThreadManager6 = KeyHandleThreadManager.this;
                            keyHandleThreadManager6.updateEventListener(200, 201, keyHandleThreadManager6.mAttestationKeyUtil.isAttestationKeyValidated());
                            break;
                        }
                        break;
                    case 202:
                        KeyHandleThreadManager.this.mCurStep = 202;
                        KeyHandleThreadManager keyHandleThreadManager7 = KeyHandleThreadManager.this;
                        if (keyHandleThreadManager7.sleep_and_continue(keyHandleThreadManager7.THREAD_SLEEP_TIME_2S)) {
                            KeyHandleThreadManager keyHandleThreadManager8 = KeyHandleThreadManager.this;
                            keyHandleThreadManager8.updateEventListener(200, 202, keyHandleThreadManager8.mAttestationKeyUtil.httpGetAttestationKey());
                            break;
                        }
                        break;
                    case 203:
                        KeyHandleThreadManager.this.mCurStep = 203;
                        KeyHandleThreadManager keyHandleThreadManager9 = KeyHandleThreadManager.this;
                        if (keyHandleThreadManager9.sleep_and_continue(keyHandleThreadManager9.THREAD_SLEEP_TIME_2S)) {
                            KeyHandleThreadManager keyHandleThreadManager10 = KeyHandleThreadManager.this;
                            keyHandleThreadManager10.updateEventListener(200, 203, keyHandleThreadManager10.mAttestationKeyUtil.hashCheckAttestationKey());
                            break;
                        }
                        break;
                    case 204:
                        KeyHandleThreadManager.this.mCurStep = 204;
                        KeyHandleThreadManager keyHandleThreadManager11 = KeyHandleThreadManager.this;
                        if (keyHandleThreadManager11.sleep_and_continue(keyHandleThreadManager11.THREAD_SLEEP_TIME_2S)) {
                            KeyHandleThreadManager keyHandleThreadManager12 = KeyHandleThreadManager.this;
                            keyHandleThreadManager12.updateEventListener(200, 204, keyHandleThreadManager12.mAttestationKeyUtil.installAttestationKey());
                            break;
                        }
                        break;
                    default:
                        switch (i) {
                            case 301:
                                KeyHandleThreadManager.this.mCurStep = 301;
                                KeyHandleThreadManager keyHandleThreadManager13 = KeyHandleThreadManager.this;
                                if (keyHandleThreadManager13.sleep_and_continue(keyHandleThreadManager13.THREAD_SLEEP_TIME_2S)) {
                                    KeyHandleThreadManager keyHandleThreadManager14 = KeyHandleThreadManager.this;
                                    keyHandleThreadManager14.updateEventListener(300, 301, keyHandleThreadManager14.mWidevineKeyUtil.isWidevineKeyValidated());
                                    break;
                                }
                                break;
                            case 302:
                                KeyHandleThreadManager.this.mCurStep = 302;
                                KeyHandleThreadManager keyHandleThreadManager15 = KeyHandleThreadManager.this;
                                if (keyHandleThreadManager15.sleep_and_continue(keyHandleThreadManager15.THREAD_SLEEP_TIME_2S)) {
                                    KeyHandleThreadManager keyHandleThreadManager16 = KeyHandleThreadManager.this;
                                    keyHandleThreadManager16.updateEventListener(300, 302, keyHandleThreadManager16.mWidevineKeyUtil.httpGetWidevineKey());
                                    break;
                                }
                                break;
                            case 303:
                                KeyHandleThreadManager.this.mCurStep = 303;
                                KeyHandleThreadManager keyHandleThreadManager17 = KeyHandleThreadManager.this;
                                if (keyHandleThreadManager17.sleep_and_continue(keyHandleThreadManager17.THREAD_SLEEP_TIME_2S)) {
                                    KeyHandleThreadManager keyHandleThreadManager18 = KeyHandleThreadManager.this;
                                    keyHandleThreadManager18.updateEventListener(300, 303, keyHandleThreadManager18.mWidevineKeyUtil.hashCheckWidevineKey());
                                    break;
                                }
                                break;
                            case 304:
                                KeyHandleThreadManager.this.mCurStep = 304;
                                KeyHandleThreadManager keyHandleThreadManager19 = KeyHandleThreadManager.this;
                                if (keyHandleThreadManager19.sleep_and_continue(keyHandleThreadManager19.THREAD_SLEEP_TIME_2S)) {
                                    KeyHandleThreadManager keyHandleThreadManager20 = KeyHandleThreadManager.this;
                                    keyHandleThreadManager20.updateEventListener(300, 304, keyHandleThreadManager20.mWidevineKeyUtil.installWidevineKey());
                                    break;
                                }
                                break;
                            default:
                                KeyHandleThreadManager.this.mCurStep = 0;
                                Log.i("KeyHandleThreadManager", "WorkHandle, unknown msg");
                                break;
                        }
                }
                return;
            }
            KeyHandleThreadManager.this.mCurStep = 400;
            KeyHandleThreadManager.this.factory_reset();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateEventListener(int i, int i2, boolean z) {
        if (!isWorking()) {
            Log.i("KeyHandleThreadManager", "work thread is stopped, no need to update the listener, step:" + i + "," + i2 + "," + z);
            return;
        }
        EventListener eventListener = this.mListener;
        if (eventListener == null) {
            Log.e("KeyHandleThreadManager", "listener is null");
            return;
        }
        if (this.mCurStep == this.mPreStep) {
            eventListener.onStatusChanged(i, i2, z);
            return;
        }
        Log.i("KeyHandleThreadManager", "there is a new step, no need to update the listener, pre step " + this.mPreStep + ", cur step " + this.mCurStep);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean sleep_and_continue(long j) {
        long j2 = 0;
        while (true) {
            boolean z = true;
            if (j2 >= j) {
                return true;
            }
            try {
                if (!isWorking()) {
                    Log.i("KeyHandleThreadManager", "sleep aborted");
                    z = false;
                    return false;
                }
                Thread.sleep(200L);
                j2 += 200;
            } catch (Exception e) {
                e.printStackTrace();
                return z;
            }
        }
    }

    public boolean isInternetValidated() {
        Log.i("KeyHandleThreadManager", "isInternetValidated");
        boolean z = false;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            Network activeNetwork = connectivityManager.getActiveNetwork();
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            Log.d("KeyHandleThreadManager", "<isInternetValidated> networkCapabilities=" + networkCapabilities);
            if (networkCapabilities != null && networkCapabilities.hasCapability(16)) {
                Log.d("KeyHandleThreadManager", "<isInternetValidated> networkCapabilities has NET_CAPABILITY_VALIDATED");
                z = true;
            } else {
                Log.d("KeyHandleThreadManager", "<isInternetValidated> network is invalidated, network:" + activeNetwork);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return z;
    }

    public boolean isNetworkConnected() {
        Log.i("KeyHandleThreadManager", "isNetworkConnected");
        try {
            NetworkInfo activeNetworkInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
            Log.d("KeyHandleThreadManager", "<isNetworkConnected> networkInfo=" + activeNetworkInfo);
            return activeNetworkInfo.isAvailable();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void factory_reset() {
        if (mZteMcspCfgManager.isProductSeparate()) {
            Log.d("KeyHandleThreadManager", "ProductCfgReset() Ret=" + mZteMcspCfgManager.ProductCfgReset());
            Log.d("KeyHandleThreadManager", "SaveConfigtoFlash() Ret=" + mZteMcspCfgManager.SaveConfigtoFlash());
        }
        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent("android.intent.action.FACTORY_RESET");
        intent.setPackage("android");
        intent.setFlags(268435456);
        intent.putExtra("android.intent.extra.REASON", "ResetButon");
        this.mContext.sendBroadcast(intent);
    }

    private void removeAllMessages() {
        if (this.mWorkHandle.hasMessages(R$styleable.Constraint_layout_goneMarginRight)) {
            this.mWorkHandle.removeMessages(R$styleable.Constraint_layout_goneMarginRight);
        }
        if (this.mWorkHandle.hasMessages(R$styleable.Constraint_layout_goneMarginStart)) {
            this.mWorkHandle.removeMessages(R$styleable.Constraint_layout_goneMarginStart);
        }
        if (this.mWorkHandle.hasMessages(201)) {
            this.mWorkHandle.removeMessages(201);
        }
        if (this.mWorkHandle.hasMessages(202)) {
            this.mWorkHandle.removeMessages(202);
        }
        if (this.mWorkHandle.hasMessages(203)) {
            this.mWorkHandle.removeMessages(203);
        }
        if (this.mWorkHandle.hasMessages(204)) {
            this.mWorkHandle.removeMessages(204);
        }
        if (this.mWorkHandle.hasMessages(301)) {
            this.mWorkHandle.removeMessages(301);
        }
        if (this.mWorkHandle.hasMessages(302)) {
            this.mWorkHandle.removeMessages(302);
        }
        if (this.mWorkHandle.hasMessages(303)) {
            this.mWorkHandle.removeMessages(303);
        }
        if (this.mWorkHandle.hasMessages(304)) {
            this.mWorkHandle.removeMessages(304);
        }
    }
}
