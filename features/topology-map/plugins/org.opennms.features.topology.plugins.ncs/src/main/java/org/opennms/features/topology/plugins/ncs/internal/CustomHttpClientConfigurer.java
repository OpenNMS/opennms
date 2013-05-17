package org.opennms.features.topology.plugins.ncs.internal;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.camel.component.http4.HttpClientConfigurer;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;

public class CustomHttpClientConfigurer implements HttpClientConfigurer {
    
    
    
    @Override
	public void configureHttpClient(HttpClient client) {
        try {
            SSLContext ctx = SSLContext.getInstance("SSL");
            X509TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] xcs,
                        String string) throws CertificateException {
                }
                public void checkServerTrusted(X509Certificate[] xcs,
                        String string) throws CertificateException {
                }
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            ctx.init(null, new TrustManager[] { tm }, null);
            SSLSocketFactory ssf = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER); 

            client.getConnectionManager().getSchemeRegistry().register(new Scheme("https4", 443, ssf));
            client.getConnectionManager().getSchemeRegistry().register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
            client.getConnectionManager().getSchemeRegistry().register(new Scheme("http4", 80, PlainSocketFactory.getSocketFactory()));
            client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, ssf));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}


}
