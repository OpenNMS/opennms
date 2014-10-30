package org.opennms.core.web;

import java.io.Closeable;
import java.io.IOException;
import java.net.ProxySelector;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
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
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class HttpClientWrapper implements Closeable {
    private HttpClientBuilder m_httpClientBuilder;
    private HttpClientContext m_context;

    private RequestConfig.Builder m_requestConfigBuilder;
    private CloseableHttpClient m_httpClient;
    private CloseableHttpResponse m_response;

    private String m_userAgent;
    private String m_virtualHost;
    private HttpVersion m_version;

    protected HttpClientWrapper() {
        m_httpClientBuilder = HttpClientBuilder.create();
        m_context = HttpClientContext.create();
        m_requestConfigBuilder = RequestConfig.custom();
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
        assertNotInitialized();

        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        credentialsProvider.setCredentials(AuthScope.ANY, credentials);
        m_context.setCredentialsProvider(credentialsProvider);

        return this;
    }

    /**
     * Add an {@link HttpRequestInterceptor} for all requests.
     */
    public HttpClientWrapper addRequestInterceptor(final HttpRequestInterceptor interceptor) {
        m_httpClientBuilder.addInterceptorLast(interceptor);
        return this;
    }

    /**
     * Add an {@link HttpResponseInterceptor} for all responses.
     * @return 
     */
    public HttpClientWrapper addResponseInterceptor(HttpResponseInterceptor interceptor) {
        m_httpClientBuilder.addInterceptorLast(interceptor);
        return this;
    }

    /**
     * Configure HttpClient to not reuse connections for multiple requests.
     */
    public HttpClientWrapper dontReuseConnections() {
        m_httpClientBuilder.setConnectionReuseStrategy(new NoConnectionReuseStrategy());
        return this;
    }

    /**
     * Configure HttpClient to honor the system java proxy settings (-Dhttp.proxyHost= -Dhttp.proxyPort=)
     */
    public HttpClientWrapper useSystemProxySettings() {
        m_httpClientBuilder.setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()));
        return this;
    }

    /**
     * Use browser-compatible cookies rather than the default.
     */
    public HttpClientWrapper useBrowserCompatibleCookies() {
        m_requestConfigBuilder.setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY);
        return this;
    }

    /**
     * Use relaxed SSL connection handling (EmptyKeyRelaxedTrustSSLContext.ALGORITHM)
     * @throws NoSuchAlgorithmException 
     */
    public HttpClientWrapper useRelaxedSSL(final String scheme) throws GeneralSecurityException {
        return useSSLContext(SSLContext.getInstance(EmptyKeyRelaxedTrustSSLContext.ALGORITHM), scheme);
    }

    public HttpClientWrapper trustSelfSigned(final String scheme) throws GeneralSecurityException {
        final SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                .useTLS()
                .build();
        return useSSLContext(sslContext, scheme);
    }
    /**
     * Use the specified SSL context when making HTTPS connections.
     * @param sslContext
     */
    public HttpClientWrapper useSSLContext(final SSLContext sslContext, final String scheme) {
        SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register(scheme == null? "https" : scheme, sslConnectionFactory)
                .build();

        final HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
        m_httpClientBuilder.setConnectionManager(ccm);

        return this;
    }

    /**
     * Preemptively pass basic authentication headers, rather than waiting for the server
     * to response asking for it.
     */
    public HttpClientWrapper usePreemptiveAuth() {
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
        m_httpClientBuilder.addInterceptorFirst(preemptiveAuth);
        return this;
    }

    /**
     * Set the socket timeout on connections.
     */
    public HttpClientWrapper setSocketTimeout(final int socketTimeout) {
        m_requestConfigBuilder.setSocketTimeout(socketTimeout);
        return this;
    }

    /**
     * Set the connection timeout on connections.
     */
    public HttpClientWrapper setConnectionTimeout(final int connectionTimeout) {
        m_requestConfigBuilder.setConnectTimeout(connectionTimeout);
        return this;
    }

    /**
     * Set the number of retries when making requests.
     */
    public HttpClientWrapper setRetries(final int retries) {
        m_httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(retries, false));
        return this;
    }

    /**
     * Set the User-Agent header used when making requests.
     */
    public HttpClientWrapper setUserAgent(final String userAgent) {
        m_userAgent = userAgent;
        return this;
    }

    /**
     * Set the Host header used when making requests.
     */
    public HttpClientWrapper setVirtualHost(final String host) {
        m_virtualHost = host;
        return this;
    }

    /**
     * Set the HTTP version used when making requests.
     */
    public HttpClientWrapper setVersion(final HttpVersion httpVersion) {
        m_version = httpVersion;
        return this;
    }

    /**
     * Safely clean up after the last response returned from an execute() call.
     */
    public void closeResponse() {
        if (m_response != null) {
            EntityUtils.consumeQuietly(m_response.getEntity());
            IOUtils.closeQuietly(m_response);
            m_response = null;
        }
    }

    /**
     * Safely clean up the HttpClient.
     */
    @Override
    public void close() throws IOException {
        closeResponse();
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
        if (m_response != null) {
            throw new UnsupportedOperationException("You have an existing response outstanding. Call reset() before executing a new request.");
        }

        // override some headers with our versions
        final HttpRequestWrapper requestWrapper = HttpRequestWrapper.wrap(method);
        if (m_userAgent != null && !m_userAgent.trim().isEmpty()) {
            requestWrapper.setHeader(HTTP.USER_AGENT, m_userAgent);
        }
        if (m_virtualHost != null && !m_virtualHost.trim().isEmpty()) {
            requestWrapper.setHeader(HTTP.TARGET_HOST, m_virtualHost);
        }
        if (m_version != null) {
            requestWrapper.setProtocolVersion(m_version);
        }

        m_response = getClient().execute(requestWrapper, m_context);
        return m_response;
    }

    protected CloseableHttpClient getClient() {
        if (m_httpClient == null) {
            m_httpClientBuilder.setDefaultRequestConfig(m_requestConfigBuilder.build());
            m_httpClient = m_httpClientBuilder.build();

        }
        return m_httpClient;
    }

    protected void assertNotInitialized() {
        if (m_httpClient != null || m_response != null) {
            throw new IllegalStateException("HttpClientWrapper has already created an HttpClient!  You cannot change configuration any more.");
        }
    }

}
