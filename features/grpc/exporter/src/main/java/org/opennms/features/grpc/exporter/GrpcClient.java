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

package org.opennms.features.grpc.exporter;

import io.grpc.ClientInterceptor;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Writer;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.OutputStreamWriter;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class GrpcClient {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcClient.class);

    private final String host;
    private final String tlsCertPath;
    private final boolean tlsEnabled;
    private ManagedChannel channel;
    private final ClientInterceptor clientInterceptor;
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    public GrpcClient(final String host,
                      final String tlsCertPath,
                      final boolean tlsEnabled,
                      ClientInterceptor clientInterceptor) {
        this.host = Objects.requireNonNull(host);
        this.tlsCertPath = tlsCertPath;
        this.tlsEnabled = tlsEnabled;
        this.clientInterceptor = clientInterceptor;
    }

    public ConnectivityState getChannelState() {
        return this.channel != null ? (this.channel.getState(true)) : ConnectivityState.SHUTDOWN;
    }

    public ManagedChannel getChannel () {
        return this.channel;
    }

    public String getHost () {
        return this.host;
    }

    public boolean getStopped () {
        return this.stopped.get();
    }

    public synchronized void startGrpcConnection() throws SSLException {

        if(getChannelState().equals(ConnectivityState.READY))
        {
            LOG.info("Grpc Channel already connected and in ready state!");
            return;
        }

        final NettyChannelBuilder channelBuilder = NettyChannelBuilder.forTarget(this.host)
                .intercept(clientInterceptor)
                .keepAliveWithoutCalls(true);

        if (tlsEnabled && tlsCertPath != null && !tlsCertPath.isBlank()) {
            this.channel = channelBuilder.useTransportSecurity()
                    .sslContext(GrpcSslContexts.forClient()
                            .trustManager(new File(tlsCertPath)).build())
                    .build();
            LOG.info("TLS enabled with cert at {}", tlsCertPath);
        } else if (tlsEnabled) {
            // Use system store specified in javax.net.ssl.trustStore
            String [] hostAndPort = splitHostAndPort(host);
            try (InputStream certStream = fetchServerCertificateAsPemStream(hostAndPort[0], Integer.parseInt(hostAndPort[1]))) {
                this.channel = channelBuilder.useTransportSecurity()
                        .sslContext(GrpcSslContexts.forClient()
                                .trustManager(certStream)
                                .build())
                        .build();
                LOG.info("TLS enabled using certificate fetched from {} ", host);
            } catch (Exception e) {
                LOG.debug("TLS certificate fetch/setup failed", e);
            }

            LOG.info("TLS enabled with certs from system store");
        } else {
            this.channel = channelBuilder.usePlaintext()
                    .build();
            LOG.info("TLS disabled, using plain text");
        }
        LOG.info("Grpc client started connection to {}", this.host);
    }

    public synchronized void stopGrpcConnection() {
        if (this.channel != null) {
            this.channel.shutdownNow();
        }
        stopped.set(true);
        LOG.info("Grpc client stopped for host {} ", this.host);
    }

    private InputStream fetchServerCertificateAsPemStream(String host, int port) {
        try {
            // Trust all certificates temporarily (for fetching)
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();

            try (SSLSocket socket = (SSLSocket) socketFactory.createSocket(host, port)) {
                socket.startHandshake();

                Certificate[] certs = socket.getSession().getPeerCertificates();
                if (certs.length > 0 && certs[0] instanceof X509Certificate) {
                    X509Certificate cert = (X509Certificate) certs[0];

                    // Encode to PEM format
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try (Writer writer = new OutputStreamWriter(baos)) {
                        writer.write("-----BEGIN CERTIFICATE-----\n");
                        writer.write(Base64.getMimeEncoder(64, new byte[]{'\n'})
                                .encodeToString(cert.getEncoded()));
                        writer.write("\n-----END CERTIFICATE-----\n");
                        writer.flush();
                    }
                    return new ByteArrayInputStream(baos.toByteArray());
                } else {
                    throw new RuntimeException("No valid X.509 certificate found.");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch PEM certificate from " + host + ":" + port, e);
        }
    }

    private static String[] splitHostAndPort(String target) {
        if (target == null || !target.contains(":")) {
            LOG.debug("Expected format 'host:port', but got: " + target);
        }

        String[] parts = target.split(":");
        if (parts.length != 2) {
            LOG.debug("Invalid host:port format: " + target);
        }

        return new String[] { parts[0].trim(), parts[1].trim() };
    }



}
