package com.control.device.settings.MexicAmco.AMXUpdateAtvKey;

import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.text.TextUtils;
import android.util.Log;
import com.android.tv.settings.adv.ISystemControlManager;
import com.android.tv.settings.adv.impl.SystemControlManager;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;

/* loaded from: classes.dex */
public class AttestationKeyUtil {
    private static int KEY_MAX_LENGTH = 1048576;
    private Context mContext;
    private String mKeyCheckSum_http;
    private final int KEY_TYPE = 66;
    private String mKeyDir = "AttestationKey";
    ISystemControlManager mSystenControlManager = SystemControlManager.getInstance();
    private String mSerialNo = Build.getSerial();
    private String mFileName_zip = "attestation_" + this.mSerialNo + ".zip";
    private String mFileName_bin = "attestation_" + this.mSerialNo + "_enc.bin";
    private String mFileName_txt = "attestation_" + this.mSerialNo + "_hash.txt";

    public AttestationKeyUtil(Context context) {
        this.mContext = context;
    }

    public boolean isAttestationKeyValidated() {
        boolean z;
        synchronized (AttestationKeyUtil.class) {
            Log.i("AttestationKeyUtil", "isAttestationValidated");
            z = true;
            try {
                testEcAttestationChain();
                testRsaAttestationChain();
            } catch (Exception e) {
                z = false;
                e.printStackTrace();
            }
        }
        return z;
    }

    public boolean httpGetAttestationKey() {
        boolean z;
        String str;
        String host;
        synchronized (AttestationKeyUtil.class) {
            Log.i("AttestationKeyUtil", "httpGetAttestationKey");
            z = false;
            try {
                String str2 = this.mContext.getCacheDir().getAbsolutePath() + "/" + this.mKeyDir;
                File file = new File(str2);
                if (file.exists()) {
                    Log.i("AttestationKeyUtil", "httpGetAttestationKey, delete " + str2);
                    ComUtil.deleteDirByFile(file);
                }
                if (!file.exists()) {
                    Log.i("AttestationKeyUtil", "httpGetAttestationKey, create dir " + str2);
                    file.mkdirs();
                }
                str = str2 + "/" + this.mFileName_zip;
                host = HttpUtil.getHost();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!TextUtils.isEmpty(host) && host.contains("https")) {
                if (HttpUtil.downloadKey(ComUtil.getUrlBySN(host, "attestation_", this.mSerialNo), str)) {
                    if (new File(str).exists()) {
                        Log.i("AttestationKeyUtil", "httpGetAttestationKey, file=" + str + " exist");
                        z = true;
                    } else {
                        Log.i("AttestationKeyUtil", "httpGetAttestationKey, file=" + str + " does not exist");
                    }
                } else {
                    Log.i("AttestationKeyUtil", "httpGetAttestationKey, downloadKey failed");
                }
            }
            Log.e("AttestationKeyUtil", "httpGetAttestationKey, host error, host=" + host);
        }
        return z;
    }

    public boolean hashCheckAttestationKey() {
        boolean z;
        synchronized (AttestationKeyUtil.class) {
            Log.i("AttestationKeyUtil", "hashCheckAttestationKey");
            z = false;
            try {
                String str = this.mContext.getCacheDir().getAbsolutePath() + "/" + this.mKeyDir;
                String str2 = str + "/" + this.mFileName_zip;
                String str3 = str + "/" + this.mFileName_bin;
                String str4 = str + "/" + this.mFileName_txt;
                File file = new File(str3);
                if (file.exists()) {
                    Log.i("AttestationKeyUtil", "hashCheckAttestationKey, delete file=" + str3);
                    file.delete();
                }
                File file2 = new File(str4);
                if (file2.exists()) {
                    Log.i("AttestationKeyUtil", "hashCheckAttestationKey, delete file=" + str4);
                    file2.delete();
                }
                ComUtil.readByZipInputStream(str2, str);
                Thread.sleep(1000L);
                if (file.exists() && file2.exists()) {
                    this.mKeyCheckSum_http = ComUtil.readFileStr(str4);
                    Log.i("AttestationKeyUtil", "hashCheckAttestationKey, sum is " + this.mKeyCheckSum_http + " from server");
                    if (!TextUtils.isEmpty(this.mKeyCheckSum_http)) {
                        z = true;
                    }
                } else {
                    Log.i("AttestationKeyUtil", "hashCheckAttestationKey, some file is lost");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return z;
    }

    public boolean installAttestationKey() {
        synchronized (AttestationKeyUtil.class) {
            Log.i("AttestationKeyUtil", "installAttestationKey");
            FileInputStream fileInputStream = null;
            boolean z = false;
            try {
                try {
                    String str = (this.mContext.getCacheDir().getAbsolutePath() + "/" + this.mKeyDir) + "/" + this.mFileName_bin;
                    File file = new File(str);
                    if (!file.exists() || file.isDirectory()) {
                        Log.i("AttestationKeyUtil", "installAttestationKey, file=" + str + " does not exist");
                    } else {
                        FileInputStream fileInputStream2 = new FileInputStream(file);
                        try {
                            int available = fileInputStream2.available();
                            if (available > KEY_MAX_LENGTH) {
                                Log.i("AttestationKeyUtil", "path=" + file.getAbsolutePath() + ", length=" + available + ", is too large");
                                try {
                                    fileInputStream2.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return false;
                            }
                            byte[] bArr = new byte[available];
                            fileInputStream2.read(bArr);
                            fileInputStream2.close();
                            Log.i("AttestationKeyUtil", "path=" + file.getAbsolutePath() + ", length=" + available + ", length(hex)=" + String.format("%08x", Integer.valueOf(available)));
                            int[] iArr = new int[available];
                            for (int i = 0; i < available; i++) {
                                iArr[i] = bArr[i] & 255;
                            }
                            boolean writeAttestationKey = this.mSystenControlManager.writeAttestationKey(iArr, available);
                            if (writeAttestationKey) {
                                Thread.sleep(1000L);
                                String keyProvisionChecksum = this.mSystenControlManager.getKeyProvisionChecksum(66);
                                if (TextUtils.isEmpty(keyProvisionChecksum) || TextUtils.isEmpty(this.mKeyCheckSum_http) || !this.mKeyCheckSum_http.equalsIgnoreCase(keyProvisionChecksum)) {
                                    Log.i("AttestationKeyUtil", "installAttestationKey, check sum failed, expected:" + this.mKeyCheckSum_http + ", local:" + keyProvisionChecksum);
                                } else {
                                    Log.i("AttestationKeyUtil", "installAttestationKey, check sum success");
                                    Thread.sleep(1000L);
                                    if (isAttestationKeyValidated()) {
                                        Log.i("AttestationKeyUtil", "installAttestationKey, isAttestationKeyValidated true");
                                        z = true;
                                    } else {
                                        Log.i("AttestationKeyUtil", "installAttestationKey, isAttestationKeyValidated false");
                                    }
                                }
                            } else {
                                Log.i("AttestationKeyUtil", "installAttestationKey, bStoreResult=" + writeAttestationKey);
                            }
                            fileInputStream = fileInputStream2;
                        } catch (Exception e2) {
                            e = e2;
                            fileInputStream = fileInputStream2;
                            e.printStackTrace();
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (Exception e3) {
                                    e = e3;
                                    e.printStackTrace();
                                    return z;
                                }
                            }
                            return z;
                        } catch (Throwable th) {
                            th = th;
                            fileInputStream = fileInputStream2;
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (Exception e4) {
                                    e4.printStackTrace();
                                }
                            }
                            throw th;
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Exception e5) {
                            e = e5;
                            e.printStackTrace();
                            return z;
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (Exception e6) {
                e = e6;
            }
            return z;
        }
    }

    private void testEcAttestationChain() throws Exception {
        generateKeyPair("EC", new KeyGenParameterSpec.Builder("test_key", 12).setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1")).setDigests("SHA-256").setAttestationChallenge("challenge".getBytes()).build());
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        try {
            verifyCertificateChain(keyStore.getCertificateChain("test_key"));
        } finally {
            keyStore.deleteEntry("test_key");
        }
    }

    private void testRsaAttestationChain() throws Exception {
        generateKeyPair("RSA", new KeyGenParameterSpec.Builder("test_key", 12).setDigests("SHA-256").setSignaturePaddings("PSS").setAttestationChallenge("challenge".getBytes()).build());
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        try {
            verifyCertificateChain(keyStore.getCertificateChain("test_key"));
        } finally {
            keyStore.deleteEntry("test_key");
        }
    }

    private void generateKeyPair(String str, KeyGenParameterSpec keyGenParameterSpec) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(str, "AndroidKeyStore");
        keyPairGenerator.initialize(keyGenParameterSpec);
        keyPairGenerator.generateKeyPair();
    }

    private static void verifyCertificateChain(Certificate[] certificateArr) throws Exception {
        for (int i = 1; i < certificateArr.length; i++) {
            ((X509Certificate) certificateArr[i]).checkValidity();
            if (i > 0) {
                PublicKey publicKey = certificateArr[i].getPublicKey();
                certificateArr[i - 1].verify(publicKey);
                if (i == certificateArr.length - 1) {
                    certificateArr[i].verify(publicKey);
                    ComUtil.assertTrue("**Certificate chain is valid, but does not have production root**", Arrays.equals(((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream("-----BEGIN CERTIFICATE-----\nMIIFYDCCA0igAwIBAgIJAOj6GWMU0voYMA0GCSqGSIb3DQEBCwUAMBsxGTAXBgNVBAUTEGY5MjAwOWU4NTNiNmIwNDUwHhcNMTYwNTI2MTYyODUyWhcNMjYwNTI0MTYyODUyWjAbMRkwFwYDVQQFExBmOTIwMDllODUzYjZiMDQ1MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAr7bHgiuxpwHsK7Qui8xUFmOr75gvMsd/dTEDDJdSSxtf6An7xyqpRR90PL2abxM1dEqlXnf2tqw1Ne4Xwl5jlRfdnJLmN0pTy/4lj4/7tv0Sk3iiKkypnEUtR6WfMgH0QZfKHM1+di+y9TFRtv6y//0rb+T+W8a9nsNL/ggjnar86461qO0rOs2cXjp3kOG1FEJ5MVmFmBGtnrKpa73XpXyTqRxB/M0n1n/W9nGqC4FSYa04T6N5RIZGBN2z2MT5IKGbFlbC8UrW0DxW7AYImQQcHtGl/m00QLVWutHQoVJYnFPlXTcHYvASLu+RhhsbDmxMgJJ0mcDpvsC4PjvB+TxywElgS70vE0XmLD+OJtvsBslHZvPBKCOdT0MS+tgSOIfga+z1Z1g7+DVagf7quvmag8jfPioyKvxnK/EgsTUVi2ghzq8wm27ud/mIM7AY2qEORR8Go3TVB4HzWQgpZrt3i5MIlCaY504LzSRiigHCzAPlHws+W0rB5N+er5/2pJKnfBSDiCiFAVtCLOZ7gLiMm0jhO2B6tUXHI/+MRPjy02i59lINMRRev56GKtcd9qO/0kUJWdZTdA2XoS82ixPvZtXQpUpuL12ab+9EaDK8Z4RHJYYfCT3Q5vNAXaiWQ+8PTWm2QgBR/bkwSWc+NpUFgNPN9PvQi8WEg5UmAGMCAwEAAaOBpjCBozAdBgNVHQ4EFgQUNmHhAHyIBQlRi0RsR/8aTMnqTxIwHwYDVR0jBBgwFoAUNmHhAHyIBQlRi0RsR/8aTMnqTxIwDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8EBAMCAYYwQAYDVR0fBDkwNzA1oDOgMYYvaHR0cHM6Ly9hbmRyb2lkLmdvb2dsZWFwaXMuY29tL2F0dGVzdGF0aW9uL2NybC8wDQYJKoZIhvcNAQELBQADggIBACDIw41L3KlXG0aMiS//cqrG+EShHUGo8HNsw30W1kJtjn6UBwRM6jnmiwfBPb8VA91chb2vssAtX2zbTvqBJ9+LBPGCdw/E53Rbf86qhxKaiAHOjpvAy5Y3m00mqC0w/Zwvju1twb4vhLaJ5NkUJYsUS7rmJKHHBnETLi8GFqiEsqTWpG/6ibYCv7rYDBJDcR9W62BW9jfIoBQcxUCUJouMPH25lLNcDc1ssqvC2v7iUgI9LeoM1sNovqPmQUiG9rHli1vXxzCyaMTjwftkJLkf6724DFhuKug2jITV0QkXvaJWF4nUaHOTNA4uJU9WDvZLI1j83A+/xnAJUucIv/zGJ1AMH2boHqF8CY16LpsYgBt6tKxxWH00XcyDCdW2KlBCeqbQPcsFmWyWugxdcekhYsAWyoSf818NUsZdBWBaR/OukXrNLfkQ79IyZohZbvabO/X+MVT3rriAoKc8oE2Uws6DF+60PV7/WIPjNvXySdqspImSN78mflxDqwLqRBYkA3I75qppLGG9rp7UCdRjxMl8ZDBld+7yvHVgt1cVzJx9xnyGCC23UaicMDSXYrB4I4WHXPGjxhZuCuPBLTdOLU8YRvMYdEvYebWHMpvwGCF6bAx3JBpIeOQ1wDB5y0USicV3YgYGmi+NZfhA4URSh77Yd6uuJOJENRaNVTzk\n-----END CERTIFICATE-----".getBytes()))).getPublicKey().getEncoded(), publicKey.getEncoded()));
                }
            }
        }
    }
}
