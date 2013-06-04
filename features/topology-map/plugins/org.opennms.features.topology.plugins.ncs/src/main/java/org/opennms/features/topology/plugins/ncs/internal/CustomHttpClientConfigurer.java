package org.opennms.features.topology.plugins.ncs.internal;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.camel.component.http.HttpClientConfigurer;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

public class CustomHttpClientConfigurer implements HttpClientConfigurer {
    
    private static class DefaultTrustManager implements X509TrustManager {
        
        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {
                return null;
        }
    }
    
    private String m_username = "admin";
    private String m_password = "admin";
    
    @Override
	public void configureHttpClient(HttpClient client) {
        try {
                      
            SSLContext ctx = SSLContext.getInstance("SSL");
            ctx.init(new KeyManager[0], new TrustManager[] { new DefaultTrustManager() }, new SecureRandom());
            SSLContext.setDefault(ctx);

            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(getUsername(), getPassword());
            
            client.getState().setCredentials(AuthScope.ANY, credentials);
            client.getParams().setAuthenticationPreemptive(true);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}


    public String getUsername() {
        return m_username;
    }

    public void setUsername(String username) {
        m_username = username;
    }

    public String getPassword() {
        return m_password;
    }

    public void setPassword(String password) {
        m_password = password;
    }


}
