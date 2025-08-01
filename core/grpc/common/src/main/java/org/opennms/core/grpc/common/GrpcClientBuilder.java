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
package org.opennms.core.grpc.common;

import com.google.common.base.Strings;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class GrpcClientBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcClientBuilder.class);
    private static final String CLIENT_CERTIFICATE_FILE_PATH = "tls.client.cert.path";
    private static final String CLIENT_PRIVATE_KEY_FILE_PATH = "tls.client.key.path";
    private static final String TRUST_CERTIFICATE_FILE_PATH = "tls.trust.cert.path";
    private static final String TLS_SKIP_VERIFY = "tls.skip.verify";
    private static final String TLS_ENABLED = "tls.enabled";

    public static ManagedChannel getChannel(String host, int port, Map<String, String> properties) throws IOException {

        NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress(host, port)
                .keepAliveWithoutCalls(true);
        boolean tlsEnabled = Boolean.parseBoolean(properties.get(TLS_ENABLED));
        if (tlsEnabled) {
            return channelBuilder
                    .negotiationType(NegotiationType.TLS)
                    .sslContext(buildSslContext(properties).build())
                    .build();
        } else {
            return channelBuilder.usePlaintext().build();
        }
    }

    public static ManagedChannel getChannelWithInterceptor(String host, int port,
                                                           Map<String, String> properties,
                                                           ClientInterceptor clientInterceptor) throws IOException {

        NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress(host, port)
                .keepAliveWithoutCalls(true);
        boolean tlsEnabled = Boolean.parseBoolean(properties.get(TLS_ENABLED));
        if (tlsEnabled) {
            LOG.info("TLS Enabled for gRPC on {}:{}", host, port);
            return channelBuilder
                    .negotiationType(NegotiationType.TLS)
                    .intercept(clientInterceptor)
                    .sslContext(buildSslContext(properties).build())
                    .build();

        } else {
            return channelBuilder
                    .usePlaintext()
                    .intercept(clientInterceptor)
                    .build();
        }
    }

    private static SslContextBuilder buildSslContext(Map<String, String> properties) throws SSLException {
        SslContextBuilder builder = GrpcSslContexts.forClient();
        String clientCertChainFilePath = properties.get(CLIENT_CERTIFICATE_FILE_PATH);
        String clientPrivateKeyFilePath = properties.get(CLIENT_PRIVATE_KEY_FILE_PATH);
        String trustCertCollectionFilePath = properties.get(TRUST_CERTIFICATE_FILE_PATH);
        boolean tlsSkipVerify = Boolean.parseBoolean(properties.get(TLS_SKIP_VERIFY));

        if (tlsSkipVerify) {
            // Use this only for test purposes
            builder.trustManager(InsecureTrustManagerFactory.INSTANCE);
        }
        if (!Strings.isNullOrEmpty(trustCertCollectionFilePath)) {
            builder.trustManager(new File(trustCertCollectionFilePath));
        }
        if (!Strings.isNullOrEmpty(clientCertChainFilePath) && !Strings.isNullOrEmpty(clientPrivateKeyFilePath)) {
            builder.keyManager(new File(clientCertChainFilePath), new File(clientPrivateKeyFilePath));
        } else if (!Strings.isNullOrEmpty(clientCertChainFilePath) || !Strings.isNullOrEmpty(clientPrivateKeyFilePath)) {
            LOG.error("Only one of the required file paths were provided, need both client cert and client private key");
        }
        return builder;
    }
}
