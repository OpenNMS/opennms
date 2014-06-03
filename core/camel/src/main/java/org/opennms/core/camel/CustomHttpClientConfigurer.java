package org.opennms.core.camel;

import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.camel.component.http.HttpClientConfigurer;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomHttpClientConfigurer implements HttpClientConfigurer {
    private static final KeyManager[] EMPTY_KEYMANAGER_ARRAY = new KeyManager[0];
    private static final Logger LOG = LoggerFactory.getLogger(CustomHttpClientConfigurer.class);

    private String m_username = "admin";
    private String m_password = "admin";

    @Override
    public void configureHttpClient(final HttpClient client) {
        try {
            final SSLContext ctx = SSLContext.getInstance("SSL");
            ctx.init(EMPTY_KEYMANAGER_ARRAY, new TrustManager[] { new DefaultTrustManager() }, new SecureRandom());
            SSLContext.setDefault(ctx);

            final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(getUsername(), getPassword());
            client.getState().setCredentials(AuthScope.ANY, credentials);
            client.getParams().setAuthenticationPreemptive(true);
            LOG.debug("Configuring HTTP client with modified trust manager, username={}, password=xxxxxxxx", getUsername());
        } catch (final Exception e) {
            throw new CustomConfigurerException(e);
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
