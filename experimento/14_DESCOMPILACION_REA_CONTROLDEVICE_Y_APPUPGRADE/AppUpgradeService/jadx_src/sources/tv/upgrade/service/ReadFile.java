package tv.upgrade.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/* loaded from: classes.dex */
public interface ReadFile extends IInterface {
    public static final String DESCRIPTOR = "tv.upgrade.service.ReadFile";

    String getFileName() throws RemoteException;

    int readLogFile(byte[] bArr) throws RemoteException;

    public static class Default implements ReadFile {
        @Override // tv.upgrade.service.ReadFile
        public int readLogFile(byte[] buffer) throws RemoteException {
            return 0;
        }

        @Override // tv.upgrade.service.ReadFile
        public String getFileName() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ReadFile {
        static final int TRANSACTION_getFileName = 2;
        static final int TRANSACTION_readLogFile = 1;

        public Stub() {
            attachInterface(this, ReadFile.DESCRIPTOR);
        }

        public static ReadFile asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(ReadFile.DESCRIPTOR);
            if (iin != null && (iin instanceof ReadFile)) {
                return (ReadFile) iin;
            }
            return new Proxy(obj);
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            byte[] _arg0;
            if (code >= TRANSACTION_readLogFile && code <= 16777215) {
                data.enforceInterface(ReadFile.DESCRIPTOR);
            }
            switch (code) {
                case 1598968902:
                    reply.writeString(ReadFile.DESCRIPTOR);
                    return true;
                default:
                    switch (code) {
                        case TRANSACTION_readLogFile /* 1 */:
                            int _arg0_length = data.readInt();
                            if (_arg0_length < 0) {
                                _arg0 = null;
                            } else {
                                _arg0 = new byte[_arg0_length];
                            }
                            int _result = readLogFile(_arg0);
                            reply.writeNoException();
                            reply.writeInt(_result);
                            reply.writeByteArray(_arg0);
                            return true;
                        case TRANSACTION_getFileName /* 2 */:
                            String _result2 = getFileName();
                            reply.writeNoException();
                            reply.writeString(_result2);
                            return true;
                        default:
                            return super.onTransact(code, data, reply, flags);
                    }
            }
        }

        private static class Proxy implements ReadFile {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return ReadFile.DESCRIPTOR;
            }

            @Override // tv.upgrade.service.ReadFile
            public int readLogFile(byte[] buffer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(ReadFile.DESCRIPTOR);
                    if (buffer == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(buffer.length);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_readLogFile, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(buffer);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // tv.upgrade.service.ReadFile
            public String getFileName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(ReadFile.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getFileName, _data, _reply, 0);
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
