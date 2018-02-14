/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.web;

import java.io.Closeable;
import java.io.IOException;
import java.net.ProxySelector;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientWrapper implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(HttpClientWrapper.class);

    private CloseableHttpClient m_httpClient;
    private CookieStore m_cookieStore;

    private boolean m_useLaxRedirect = false;
    private boolean m_reuseConnections = true;
    private boolean m_usePreemptiveAuth = false;
    private boolean m_useSystemProxySettings;
    private String m_cookieSpec;
    private String m_username;
    private String m_password;
    private Integer m_socketTimeout;
    private Integer m_connectionTimeout;
    private Integer m_retries;
    private Map<String,SSLContext> m_sslContext = new HashMap<>();

    private Set<HttpRequestInterceptor> m_requestInterceptors = new LinkedHashSet<>();
    private Set<HttpResponseInterceptor> m_responseInterceptors = new LinkedHashSet<>();

    private String m_userAgent;
    private String m_virtualHost;
    private HttpVersion m_version;

    protected HttpClientWrapper() {
        m_cookieStore = new BasicCookieStore();
        // According to the HTTP specification, adding the default ports to the host header is optional.
        // If the default ports are added, several Web Servers like Microsoft IIS 7.5 will complain about it, and could lead to unwanted results.
        addRequestInterceptor(new HttpRequestInterceptor() {
            @Override
            public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
                Header host = request.getFirstHeader(HTTP.TARGET_HOST);
                if (host != null) {
                    if (host.getValue().endsWith(":80") || host.getValue().endsWith(":443")) {
                        request.setHeader(HTTP.TARGET_HOST, host.getValue().replaceFirst(":\\d+", ""));
                        LOG.info("httpRequestInterceptor: removing default port from host header");
                    }
                }
            }
        });
    }

    /**
     * Create a new HTTP client wrapper.
     */
    public static HttpClientWrapper create() {
        return new HttpClientWrapper();
    }

    /**
     * Add basic auth credentials to requests created by the HttpClientWrapper.
     * @param username The username to connect as
     * @param password The password to use
     */
    public HttpClientWrapper addBasicCredentials(final String username, final String password) {
        LOG.debug("addBasicCredentials: username={}", username);
        assertNotInitialized();
        m_username = username;
        m_password = password;
        return this;
    }

    /**
     * Add an {@link HttpRequestInterceptor} for all requests.
     */
    public HttpClientWrapper addRequestInterceptor(final HttpRequestInterceptor interceptor) {
        LOG.debug("addRequestInterceptor: {}", interceptor);
        assertNotInitialized();
        m_requestInterceptors.add(interceptor);
        return this;
    }

    /**
     * Add an {@link HttpResponseInterceptor} for all responses.
     * @return 
     */
    public HttpClientWrapper addResponseInterceptor(HttpResponseInterceptor interceptor) {
        LOG.debug("addResponseInterceptor: {}", interceptor);
        assertNotInitialized();
        m_responseInterceptors.add(interceptor);
        return this;
    }

    /**
     * Configure HttpClient to not reuse connections for multiple requests.
     */
    public HttpClientWrapper dontReuseConnections() {
        LOG.debug("dontReuseConnections()");
        assertNotInitialized();
        m_reuseConnections = false;
        return this;
    }

    /**
     * Configure HttpClient to honor the system java proxy settings (-Dhttp.proxyHost= -Dhttp.proxyPort=)
     */
    public HttpClientWrapper useSystemProxySettings() {
        LOG.debug("useSystemProxySettings()");
        assertNotInitialized();
        m_useSystemProxySettings = true;
        return this;
    }

    /**
     * Use browser-compatible cookies rather than the default.
     */
    public HttpClientWrapper useBrowserCompatibleCookies() {
        LOG.debug("useBrowserCompatibleCookies()");
        assertNotInitialized();
        m_cookieSpec = CookieSpecs.BROWSER_COMPATIBILITY;
        return this;
    }

    /**
     * Use relaxed SSL connection handling (EmptyKeyRelaxedTrustSSLContext.ALGORITHM, allows any certificate)
     * @throws NoSuchAlgorithmException
     */
    public HttpClientWrapper useRelaxedSSL(final String scheme) throws GeneralSecurityException {
        LOG.debug("useRelaxedSSL: scheme={}", scheme);
        assertNotInitialized();
        m_sslContext.put(scheme, SSLContext.getInstance(EmptyKeyRelaxedTrustSSLContext.ALGORITHM));
        return this;
    }

    /**
     * Trust self-signed certificates.
     * @throws GeneralSecurityException
     */
    public HttpClientWrapper trustSelfSigned(final String scheme) throws GeneralSecurityException {
        LOG.debug("trustSelfSigned: scheme={}", scheme);
        assertNotInitialized();
        m_sslContext.put(scheme, SSLContexts.custom()
                         .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                         .useTLS()
                         .build());
        return this;
    }

    /**
     * Preemptively pass basic authentication headers, rather than waiting for the server
     * to response asking for it.
     */
    public HttpClientWrapper usePreemptiveAuth() {
        LOG.debug("usePreemptiveAuth()");
        assertNotInitialized();
        m_usePreemptiveAuth = true;
        return this;
    }

    /**
     * Use LAX redirect strategy.
     * Automatically redirects all HEAD, GET and POST requests.
     * This strategy relaxes restrictions on automatic redirection of POST methods imposed by the HTTP specification.
     */
    public HttpClientWrapper useLaxRedirect() {
        LOG.debug("useLaxRedirect()");
        assertNotInitialized();
        m_useLaxRedirect = true;
        return this;
    }

    /**
     * Set the socket timeout on connections.
     */
    public HttpClientWrapper setSocketTimeout(final Integer socketTimeout) {
        LOG.debug("setSocketTimeout: timeout={}", socketTimeout);
        assertNotInitialized();
        m_socketTimeout = socketTimeout;
        return this;
    }

    /**
     * Set the connection timeout on connections.
     */
    public HttpClientWrapper setConnectionTimeout(final Integer connectionTimeout) {
        LOG.debug("setConnectionTimeout: timeout={}", connectionTimeout);
        assertNotInitialized();
        m_connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * Set the number of retries when making requests.
     */
    public HttpClientWrapper setRetries(final Integer retries) {
        LOG.debug("setRetries: retries={}", retries);
        assertNotInitialized();
        m_retries = retries;
        return this;
    }

    /**
     * Set the User-Agent header used when making requests.
     */
    public HttpClientWrapper setUserAgent(final String userAgent) {
        LOG.debug("setUserAgent: userAgent={}", userAgent);
        assertNotInitialized();
        m_userAgent = userAgent;
        return this;
    }

    /**
     * Set the Host header used when making requests.
     */
    public HttpClientWrapper setVirtualHost(final String host) {
        LOG.debug("setVirtualHost: host={}", host);
        assertNotInitialized();
        m_virtualHost = host;
        return this;
    }

    /**
     * Set the HTTP version used when making requests.
     */
    public HttpClientWrapper setVersion(final HttpVersion httpVersion) {
        LOG.debug("setVersion: version={}", httpVersion);
        assertNotInitialized();
        m_version = httpVersion;
        return this;
    }

    /**
     * Safely clean up after a response.
     */
    public void close(final CloseableHttpResponse response) {
        if (response != null) {
            EntityUtils.consumeQuietly(response.getEntity());
            IOUtils.closeQuietly(response);
        }
    }

    /**
     * Safely clean up the HttpClient.
     */
    @Override
    public void close() throws IOException {
        if (m_httpClient != null) {
            m_httpClient.close();
        }
    }

    /**
     * Execute the given HTTP method, returning an HTTP response.
     * 
     * Note that when you are done with the response, you must call {@link #closeResponse()} so that it gets cleaned up properly.
     */
    public CloseableHttpResponse execute(final HttpUriRequest method) throws ClientProtocolException, IOException {
        LOG.debug("execute: " + this.toString() + "; method: " + method.toString());
        // override some headers with our versions
        final HttpRequestWrapper requestWrapper = HttpRequestWrapper.wrap(method);
        if (!isEmpty(m_userAgent)) {
            requestWrapper.setHeader(HTTP.USER_AGENT, m_userAgent);
        }
        if (!isEmpty(m_virtualHost)) {
            requestWrapper.setHeader(HTTP.TARGET_HOST, m_virtualHost);
        }

        if (m_version != null) {
            if (HttpVersion.HTTP_1_1.equals(m_version) && isEmpty(m_virtualHost)) {
                // NMS-7506, set HTTP version to 1.0 if virtual host is not set (since 1.1 requires a virtual host)
                requestWrapper.setProtocolVersion(HttpVersion.HTTP_1_0);
            } else {
                requestWrapper.setProtocolVersion(m_version);
            }
        }

        return getClient().execute(requestWrapper);
    }

    /**
     * Create a duplicate HttpClientWrapper from this wrapper.
     * All settings are preserved, and the session/cookie store is
     * shared between duplicate wrappers and their parent.
     */
    public HttpClientWrapper duplicate() {
        final HttpClientWrapper ret = HttpClientWrapper.create();
        ret.m_cookieStore = m_cookieStore;
        ret.m_reuseConnections = m_reuseConnections;
        ret.m_usePreemptiveAuth = m_usePreemptiveAuth;
        ret.m_useSystemProxySettings = m_useSystemProxySettings;
        ret.m_cookieSpec = m_cookieSpec;
        ret.m_username = m_username;
        ret.m_password = m_password;
        ret.m_socketTimeout = m_socketTimeout;
        ret.m_connectionTimeout = m_connectionTimeout;
        ret.m_retries = m_retries;
        for (final Map.Entry<String,SSLContext> entry : ret.m_sslContext.entrySet()) {
            ret.m_sslContext.put(entry.getKey(), entry.getValue());
        }
        for (final HttpRequestInterceptor interceptor : m_requestInterceptors) {
            ret.m_requestInterceptors.add(interceptor);
        }
        for (final HttpResponseInterceptor interceptor : m_responseInterceptors) {
            ret.m_responseInterceptors.add(interceptor);
        }
        ret.m_userAgent = m_userAgent;
        ret.m_virtualHost = m_virtualHost;
        ret.m_version = m_version;
        return ret;
    }

    public CloseableHttpClient getClient() {
        if (m_httpClient == null) {
            final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            final RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();

            if (!m_reuseConnections) {
                httpClientBuilder.setConnectionReuseStrategy(new NoConnectionReuseStrategy());
            }
            if (m_usePreemptiveAuth) {
                enablePreemptiveAuth(httpClientBuilder);
            }
            if (m_useSystemProxySettings) {
                httpClientBuilder.setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()));
            }
            if (!isEmpty(m_cookieSpec)) {
                requestConfigBuilder.setCookieSpec(m_cookieSpec);
            }
            if (m_cookieStore != null) {
                httpClientBuilder.setDefaultCookieStore(m_cookieStore);
            }
            if (m_username != null) {
                setCredentials(httpClientBuilder, m_username, m_password);
            }
            if (m_socketTimeout != null) {
                requestConfigBuilder.setSocketTimeout(m_socketTimeout);
            }
            if (m_connectionTimeout != null) {
                requestConfigBuilder.setConnectTimeout(m_connectionTimeout);
            }
            if (m_retries != null) {
                httpClientBuilder.setRetryHandler(new HttpRequestRetryOnExceptionHandler(m_retries, false));
            }
            if (m_sslContext.size() != 0) {
                configureSSLContext(httpClientBuilder);
            }
            for (final HttpRequestInterceptor interceptor : m_requestInterceptors) {
                httpClientBuilder.addInterceptorLast(interceptor);
            }
            for (final HttpResponseInterceptor interceptor : m_responseInterceptors) {
                httpClientBuilder.addInterceptorLast(interceptor);
            }
            if (m_useLaxRedirect) {
                httpClientBuilder.setRedirectStrategy(new LaxRedirectStrategy());
            }
            httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());
            m_httpClient = httpClientBuilder.build();
        }
        return m_httpClient;
    }

    protected void assertNotInitialized() {
        if (m_httpClient != null) {
            throw new IllegalStateException("HttpClientWrapper has already created an HttpClient!  You cannot change configuration any more.");
        }
    }

    protected void setCredentials(final HttpClientBuilder httpClientBuilder, final String username, final String password) {
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        credentialsProvider.setCredentials(AuthScope.ANY, credentials);
        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
    }

    protected void enablePreemptiveAuth(final HttpClientBuilder builder) {
        /**
         * Add an HttpRequestInterceptor that will perform preemptive authentication
         * @see http://hc.apache.org/httpcomponents-client-4.0.1/tutorial/html/authentication.html
         */
        final HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() {
            @Override
            public void process(final HttpRequest request, final HttpContext context) throws IOException {
                if (context instanceof HttpClientContext) {
                    final HttpClientContext clientContext = (HttpClientContext)context;
                    final AuthState authState = clientContext.getTargetAuthState();
                    final CredentialsProvider credsProvider = clientContext.getCredentialsProvider();
                    final HttpHost targetHost = clientContext.getTargetHost();
                    // If not authentication scheme has been initialized yet
                    if (authState.getAuthScheme() == null) {
                        final AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());
                        // Obtain credentials matching the target host
                        final Credentials creds = credsProvider.getCredentials(authScope);
                        // If found, generate BasicScheme preemptively
                        if (creds != null) {
                            authState.update(new BasicScheme(), creds);
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Not sure how to handle a non-HttpClientContext context.");
                }
            }

        };
        builder.addInterceptorFirst(preemptiveAuth);
    }

    protected void configureSSLContext(final HttpClientBuilder builder) {
        final RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create();
        for (final Map.Entry<String,SSLContext> entry : m_sslContext.entrySet()) {
            final SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(entry.getValue(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            registryBuilder.register(entry.getKey(), sslConnectionFactory);
        }
        if (!m_sslContext.containsKey("http")) {
            registryBuilder.register("http", PlainConnectionSocketFactory.INSTANCE);
        }
        if (!m_sslContext.containsKey("https")) {
            registryBuilder.register("https", SSLConnectionSocketFactory.getSystemSocketFactory());
        }

        final HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registryBuilder.build());
        builder.setConnectionManager(ccm);
    }

    @Override
    public String toString() {
        return "HttpClientWrapper ["
                + "reuseConnections=" + m_reuseConnections
                + ", usePreemptiveAuth=" + m_usePreemptiveAuth
                + ", useSystemProxySettings=" + m_useSystemProxySettings
                + ", cookieSpec=" + m_cookieSpec
                + ", username=" + m_username
                + ", password=" + m_password
                + ", socketTimeout=" + m_socketTimeout
                + ", connectionTimeout=" + m_connectionTimeout
                + ", retries=" + m_retries
                + ", sslContext=" + m_sslContext
                + ", requestInterceptors=" + m_requestInterceptors
                + ", responseInterceptors=" + m_responseInterceptors
                + ", userAgent=" + m_userAgent
                + ", virtualHost=" + m_virtualHost
                + ", version=" + m_version
                + "]";
    }

    private static boolean isEmpty(final String value) {
        return (value == null || value.trim().isEmpty());
    }
}
