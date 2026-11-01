package com.zte.android.productcfg;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/* loaded from: classes.dex */
public interface mcspcfg extends IInterface {
    public static final String DESCRIPTOR = "com.zte.android.productcfg.mcspcfg";

    int ProductBufToCof(String str) throws RemoteException;

    int ProductCfgReset() throws RemoteException;

    String ProductCofToBuf() throws RemoteException;

    int cfgDownloadToFS(String str) throws RemoteException;

    int cfgToFlash() throws RemoteException;

    int cfgUpdateFromFS(String str) throws RemoteException;

    String getConfig(String str, String str2) throws RemoteException;

    String getConfig_distinguishNullCfg(String str, String str2) throws RemoteException;

    String[] getProductCfgList() throws RemoteException;

    boolean isProductCfg(String str, String str2) throws RemoteException;

    int productCfgClearAll() throws RemoteException;

    int productCfgRegisterKey(String str, String str2, String str3) throws RemoteException;

    int productCfgUnRegisterKey(String str, String str2, String str3) throws RemoteException;

    int setConfig(String str, String str2, String str3) throws RemoteException;

    public static class Default implements mcspcfg {
        @Override // com.zte.android.productcfg.mcspcfg
        public String getConfig(String section, String key) throws RemoteException {
            return null;
        }

        @Override // com.zte.android.productcfg.mcspcfg
        public int setConfig(String section, String key, String value) throws RemoteException {
            return 0;
        }

        @Override // com.zte.android.productcfg.mcspcfg
        public boolean isProductCfg(String section, String key) throws RemoteException {
            return false;
        }

        @Override // com.zte.android.productcfg.mcspcfg
        public int productCfgClearAll() throws RemoteException {
            return 0;
        }

        @Override // com.zte.android.productcfg.mcspcfg
        public int cfgToFlash() throws RemoteException {
            return 0;
        }

        @Override // com.zte.android.productcfg.mcspcfg
        public int cfgUpdateFromFS(String Filename) throws RemoteException {
            return 0;
        }

        @Override // com.zte.android.productcfg.mcspcfg
        public int cfgDownloadToFS(String Filename) throws RemoteException {
            return 0;
        }

        @Override // com.zte.android.productcfg.mcspcfg
        public int productCfgRegisterKey(String section, String key, String PackageName) throws RemoteException {
            return 0;
        }

        @Override // com.zte.android.productcfg.mcspcfg
        public int productCfgUnRegisterKey(String section, String key, String PackageName) throws RemoteException {
            return 0;
        }

        @Override // com.zte.android.productcfg.mcspcfg
        public String ProductCofToBuf() throws RemoteException {
            return null;
        }

        @Override // com.zte.android.productcfg.mcspcfg
        public int ProductBufToCof(String Buf) throws RemoteException {
            return 0;
        }

        @Override // com.zte.android.productcfg.mcspcfg
        public String[] getProductCfgList() throws RemoteException {
            return null;
        }

        @Override // com.zte.android.productcfg.mcspcfg
        public int ProductCfgReset() throws RemoteException {
            return 0;
        }

        @Override // com.zte.android.productcfg.mcspcfg
        public String getConfig_distinguishNullCfg(String section, String key) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements mcspcfg {
        static final int TRANSACTION_ProductBufToCof = 11;
        static final int TRANSACTION_ProductCfgReset = 13;
        static final int TRANSACTION_ProductCofToBuf = 10;
        static final int TRANSACTION_cfgDownloadToFS = 7;
        static final int TRANSACTION_cfgToFlash = 5;
        static final int TRANSACTION_cfgUpdateFromFS = 6;
        static final int TRANSACTION_getConfig = 1;
        static final int TRANSACTION_getConfig_distinguishNullCfg = 14;
        static final int TRANSACTION_getProductCfgList = 12;
        static final int TRANSACTION_isProductCfg = 3;
        static final int TRANSACTION_productCfgClearAll = 4;
        static final int TRANSACTION_productCfgRegisterKey = 8;
        static final int TRANSACTION_productCfgUnRegisterKey = 9;
        static final int TRANSACTION_setConfig = 2;

        public Stub() {
            attachInterface(this, mcspcfg.DESCRIPTOR);
        }

        public static mcspcfg asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(mcspcfg.DESCRIPTOR);
            if (iin != null && (iin instanceof mcspcfg)) {
                return (mcspcfg) iin;
            }
            return new Proxy(obj);
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code >= TRANSACTION_getConfig && code <= 16777215) {
                data.enforceInterface(mcspcfg.DESCRIPTOR);
            }
            switch (code) {
                case 1598968902:
                    reply.writeString(mcspcfg.DESCRIPTOR);
                    return true;
                default:
                    switch (code) {
                        case TRANSACTION_getConfig /* 1 */:
                            String _arg0 = data.readString();
                            String _arg1 = data.readString();
                            String _result = getConfig(_arg0, _arg1);
                            reply.writeNoException();
                            reply.writeString(_result);
                            return true;
                        case TRANSACTION_setConfig /* 2 */:
                            String _arg02 = data.readString();
                            String _arg12 = data.readString();
                            String _arg2 = data.readString();
                            int _result2 = setConfig(_arg02, _arg12, _arg2);
                            reply.writeNoException();
                            reply.writeInt(_result2);
                            return true;
                        case TRANSACTION_isProductCfg /* 3 */:
                            String _arg03 = data.readString();
                            String _arg13 = data.readString();
                            boolean _result3 = isProductCfg(_arg03, _arg13);
                            reply.writeNoException();
                            reply.writeInt(_result3 ? TRANSACTION_getConfig : 0);
                            return true;
                        case TRANSACTION_productCfgClearAll /* 4 */:
                            int _result4 = productCfgClearAll();
                            reply.writeNoException();
                            reply.writeInt(_result4);
                            return true;
                        case TRANSACTION_cfgToFlash /* 5 */:
                            int _result5 = cfgToFlash();
                            reply.writeNoException();
                            reply.writeInt(_result5);
                            return true;
                        case TRANSACTION_cfgUpdateFromFS /* 6 */:
                            String _arg04 = data.readString();
                            int _result6 = cfgUpdateFromFS(_arg04);
                            reply.writeNoException();
                            reply.writeInt(_result6);
                            return true;
                        case TRANSACTION_cfgDownloadToFS /* 7 */:
                            String _arg05 = data.readString();
                            int _result7 = cfgDownloadToFS(_arg05);
                            reply.writeNoException();
                            reply.writeInt(_result7);
                            return true;
                        case TRANSACTION_productCfgRegisterKey /* 8 */:
                            String _arg06 = data.readString();
                            String _arg14 = data.readString();
                            String _arg22 = data.readString();
                            int _result8 = productCfgRegisterKey(_arg06, _arg14, _arg22);
                            reply.writeNoException();
                            reply.writeInt(_result8);
                            return true;
                        case TRANSACTION_productCfgUnRegisterKey /* 9 */:
                            String _arg07 = data.readString();
                            String _arg15 = data.readString();
                            String _arg23 = data.readString();
                            int _result9 = productCfgUnRegisterKey(_arg07, _arg15, _arg23);
                            reply.writeNoException();
                            reply.writeInt(_result9);
                            return true;
                        case TRANSACTION_ProductCofToBuf /* 10 */:
                            String _result10 = ProductCofToBuf();
                            reply.writeNoException();
                            reply.writeString(_result10);
                            return true;
                        case TRANSACTION_ProductBufToCof /* 11 */:
                            String _arg08 = data.readString();
                            int _result11 = ProductBufToCof(_arg08);
                            reply.writeNoException();
                            reply.writeInt(_result11);
                            return true;
                        case TRANSACTION_getProductCfgList /* 12 */:
                            String[] _result12 = getProductCfgList();
                            reply.writeNoException();
                            reply.writeStringArray(_result12);
                            return true;
                        case TRANSACTION_ProductCfgReset /* 13 */:
                            int _result13 = ProductCfgReset();
                            reply.writeNoException();
                            reply.writeInt(_result13);
                            return true;
                        case TRANSACTION_getConfig_distinguishNullCfg /* 14 */:
                            String _arg09 = data.readString();
                            String _arg16 = data.readString();
                            String _result14 = getConfig_distinguishNullCfg(_arg09, _arg16);
                            reply.writeNoException();
                            reply.writeString(_result14);
                            return true;
                        default:
                            return super.onTransact(code, data, reply, flags);
                    }
            }
        }

        private static class Proxy implements mcspcfg {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return mcspcfg.DESCRIPTOR;
            }

            @Override // com.zte.android.productcfg.mcspcfg
            public String getConfig(String section, String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(mcspcfg.DESCRIPTOR);
                    _data.writeString(section);
                    _data.writeString(key);
                    this.mRemote.transact(Stub.TRANSACTION_getConfig, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.zte.android.productcfg.mcspcfg
            public int setConfig(String section, String key, String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(mcspcfg.DESCRIPTOR);
                    _data.writeString(section);
                    _data.writeString(key);
                    _data.writeString(value);
                    this.mRemote.transact(Stub.TRANSACTION_setConfig, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.zte.android.productcfg.mcspcfg
            public boolean isProductCfg(String section, String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(mcspcfg.DESCRIPTOR);
                    _data.writeString(section);
                    _data.writeString(key);
                    this.mRemote.transact(Stub.TRANSACTION_isProductCfg, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.zte.android.productcfg.mcspcfg
            public int productCfgClearAll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(mcspcfg.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_productCfgClearAll, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.zte.android.productcfg.mcspcfg
            public int cfgToFlash() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(mcspcfg.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_cfgToFlash, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.zte.android.productcfg.mcspcfg
            public int cfgUpdateFromFS(String Filename) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(mcspcfg.DESCRIPTOR);
                    _data.writeString(Filename);
                    this.mRemote.transact(Stub.TRANSACTION_cfgUpdateFromFS, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.zte.android.productcfg.mcspcfg
            public int cfgDownloadToFS(String Filename) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(mcspcfg.DESCRIPTOR);
                    _data.writeString(Filename);
                    this.mRemote.transact(Stub.TRANSACTION_cfgDownloadToFS, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.zte.android.productcfg.mcspcfg
            public int productCfgRegisterKey(String section, String key, String PackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(mcspcfg.DESCRIPTOR);
                    _data.writeString(section);
                    _data.writeString(key);
                    _data.writeString(PackageName);
                    this.mRemote.transact(Stub.TRANSACTION_productCfgRegisterKey, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.zte.android.productcfg.mcspcfg
            public int productCfgUnRegisterKey(String section, String key, String PackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(mcspcfg.DESCRIPTOR);
                    _data.writeString(section);
                    _data.writeString(key);
                    _data.writeString(PackageName);
                    this.mRemote.transact(Stub.TRANSACTION_productCfgUnRegisterKey, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.zte.android.productcfg.mcspcfg
            public String ProductCofToBuf() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(mcspcfg.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_ProductCofToBuf, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.zte.android.productcfg.mcspcfg
            public int ProductBufToCof(String Buf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(mcspcfg.DESCRIPTOR);
                    _data.writeString(Buf);
                    this.mRemote.transact(Stub.TRANSACTION_ProductBufToCof, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.zte.android.productcfg.mcspcfg
            public String[] getProductCfgList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(mcspcfg.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getProductCfgList, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.zte.android.productcfg.mcspcfg
            public int ProductCfgReset() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(mcspcfg.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_ProductCfgReset, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.zte.android.productcfg.mcspcfg
            public String getConfig_distinguishNullCfg(String section, String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(mcspcfg.DESCRIPTOR);
                    _data.writeString(section);
                    _data.writeString(key);
                    this.mRemote.transact(Stub.TRANSACTION_getConfig_distinguishNullCfg, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
