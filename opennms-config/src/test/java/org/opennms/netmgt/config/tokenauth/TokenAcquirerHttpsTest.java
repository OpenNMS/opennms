/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.config.tokenauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

import javax.net.ssl.HostnameVerifier;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

/**
 * Verifies the {@code disable-ssl-verification} branch in
 * {@link TokenAcquirer#configureClient}. With the flag set, the auth
 * call accepts a self-signed cert; without it, the call fails with a
 * TLS handshake exception. The actual TLS behavior comes from
 * {@link org.opennms.core.web.HttpClientWrapper}; this test pins
 * the wiring between the auth-config flag and that wrapper.
 */
public class TokenAcquirerHttpsTest {

    private static final String KEYSTORE = "JUnitHttpServer.keystore";
    private static final char[] STOREPASS = "opennms".toCharArray();

    private HttpsServer server;
    private int port;

    /**
     * Hostname verifier captured from the JVM at the moment this test class
     * starts, so we can restore it after the test class finishes regardless
     * of what other tests in the same surefire fork did to the global
     * default. The class itself installs a known-strict verifier in
     * {@link #installStrictDefaultHostnameVerifier()} so the
     * {@code rejectsSelfSignedCert...} test is order-independent.
     */
    private static HostnameVerifier originalDefaultHostnameVerifier;

    @BeforeClass
    public static void installStrictDefaultHostnameVerifier() {
        originalDefaultHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
        // org.apache.http.conn.ssl.DefaultHostnameVerifier (the strict
        // verifier from Apache HttpClient) rejects mismatched hostnames
        // and self-signed CN=localhost certs that aren't in the
        // truststore -- the behavior the negative test below depends on.
        HttpsURLConnection.setDefaultHostnameVerifier(
                new org.apache.http.conn.ssl.DefaultHostnameVerifier());
    }

    @AfterClass
    public static void restoreDefaultHostnameVerifier() {
        if (originalDefaultHostnameVerifier != null) {
            HttpsURLConnection.setDefaultHostnameVerifier(originalDefaultHostnameVerifier);
        }
    }

    @Before
    public void startTlsServer() throws Exception {
        final KeyStore ks = KeyStore.getInstance("JKS");
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(KEYSTORE)) {
            assertNotNull("keystore resource should exist on the classpath", in);
            ks.load(in, STOREPASS);
        }
        final KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, STOREPASS);

        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);

        server = HttpsServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            @Override
            public void configure(final HttpsParameters params) {
                final SSLContext c = getSSLContext();
                final SSLEngine engine = c.createSSLEngine();
                params.setNeedClientAuth(false);
                params.setCipherSuites(engine.getEnabledCipherSuites());
                params.setProtocols(engine.getEnabledProtocols());
                params.setSSLParameters(new SSLParameters());
            }
        });
        server.createContext("/auth", exchange -> {
            final byte[] body = "{\"Token\":\"https-token-fixture\"}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        port = server.getAddress().getPort();
        server.start();
    }

    @After
    public void stopTlsServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    private TokenAuth httpsAuth() {
        final TokenAuth auth = new TokenAuth();
        auth.setName("https-test");
        auth.setUrl("https://127.0.0.1:" + port + "/auth");
        auth.setMethod("POST");
        final TokenFrom tf = new TokenFrom();
        tf.setJsonpath("Token");
        auth.setTokenFrom(tf);
        return auth;
    }

    @Test
    public void acquiresOverHttpsWhenSslVerificationDisabled() throws IOException {
        final TokenAuth auth = httpsAuth();
        auth.setDisableSslVerification(true);

        final CachedToken token = new TokenAcquirer().acquire(auth);
        assertEquals("https-token-fixture", token.getValue());
    }

    @Test
    public void rejectsSelfSignedCertWhenSslVerificationEnabled() {
        // Don't trust this cert via the JVM truststore for this test;
        // ensure that without disable-ssl-verification the wrapper's
        // default verification rejects the self-signed snakeoil cert.
        // The strict default HostnameVerifier installed in
        // installStrictDefaultHostnameVerifier() ensures this test runs
        // against a known-strict verifier no matter what the JVM
        // default was on entry.
        final TokenAuth auth = httpsAuth();
        auth.setDisableSslVerification(false);

        final IOException ex = assertThrows(IOException.class,
                () -> new TokenAcquirer().acquire(auth));
        // Don't pin the exact message -- different JDK versions phrase
        // SSL handshake errors differently. Confirming an IOException
        // is raised is enough; if verification accepted a
        // CN=localhost self-signed cert with no install in the
        // truststore, the test would have returned a token instead.
        assertNotNull(ex);
    }
}
