package tv.upgrade.service;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

/* loaded from: classes.dex */
public class MyX509TrustManager implements X509TrustManager {
    @Override // javax.net.ssl.X509TrustManager
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    }

    @Override // javax.net.ssl.X509TrustManager
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (chain == null) {
            throw new IllegalArgumentException("checkServerTrusted:x509Certificate array is null");
        }
        if (chain.length <= 0) {
            throw new IllegalArgumentException("checkServerTrusted: X509Certificate is empty");
        }
        if (authType == null) {
            throw new IllegalArgumentException("checkServerTrusted: AuthType is null");
        }
        if (authType.length() <= 0) {
            throw new IllegalArgumentException("checkServerTrusted: AuthType is empty");
        }
        try {
            chain[0].checkValidity();
        } catch (Exception e) {
            throw new CertificateException("Certificate not valid or trusted.");
        }
    }

    @Override // javax.net.ssl.X509TrustManager
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}
