/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.core.grpc.common;

import com.google.common.base.Strings;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import org.opennms.core.utils.PropertiesUtils;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

public class GrpcIpcUtils {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcClientBuilder.class);
    public static final String GRPC_CLIENT_PID = "org.opennms.core.ipc.grpc.client";
    public static final String GRPC_SERVER_PID = "org.opennms.core.ipc.grpc.server";
    public static final String LOG_PREFIX = "ipc";
    public static final String GRPC_HOST = "host";
    public static final String DEFAULT_GRPC_HOST = "localhost";
    public static final String GRPC_PORT = "port";
    public static final int DEFAULT_TWIN_GRPC_PORT = 8991;
    public static final String TLS_ENABLED = "tls.enabled";
    public static final String GRPC_MAX_INBOUND_SIZE = "max.message.size";
    public static final int DEFAULT_MESSAGE_SIZE = 10485760;

    public static final String CLIENT_CERTIFICATE_FILE_PATH = "client.cert.filepath";
    public static final String CLIENT_PRIVATE_KEY_FILE_PATH = "client.private.key.filepath";
    public static final String TRUST_CERTIFICATE_FILE_PATH = "trust.cert.filepath";

    public static final String SERVER_CERTIFICATE_FILE_PATH = "server.cert.filepath";
    public static final String PRIVATE_KEY_FILE_PATH = "server.private.key.filepath";

    public static SslContextBuilder buildSslContext(Properties properties) throws SSLException {
        SslContextBuilder builder = GrpcSslContexts.forClient();
        String clientCertChainFilePath = properties.getProperty(CLIENT_CERTIFICATE_FILE_PATH);
        String clientPrivateKeyFilePath = properties.getProperty(CLIENT_PRIVATE_KEY_FILE_PATH);
        String trustCertCollectionFilePath = properties.getProperty(TRUST_CERTIFICATE_FILE_PATH);

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

    public static ManagedChannel getChannel(Properties properties, int port) throws IOException {
        String host = PropertiesUtils.getProperty(properties, GRPC_HOST, GrpcIpcUtils.DEFAULT_GRPC_HOST);
        int maxInboundMessageSize = PropertiesUtils.getProperty(properties, GrpcIpcUtils.GRPC_MAX_INBOUND_SIZE, GrpcIpcUtils.DEFAULT_MESSAGE_SIZE);
        NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress(host, port)
                .maxInboundMessageSize(maxInboundMessageSize)
                .keepAliveWithoutCalls(true);
        boolean tlsEnabled = Boolean.parseBoolean(properties.getProperty(TLS_ENABLED));
        if (tlsEnabled) {
            return channelBuilder
                    .negotiationType(NegotiationType.TLS)
                    .sslContext(buildSslContext(properties).build())
                    .build();
        } else {
            return channelBuilder.usePlaintext().build();
        }
    }

    public static SslContextBuilder getSslContextBuilder(Properties properties) {
        String certChainFilePath = properties.getProperty(GrpcIpcUtils.SERVER_CERTIFICATE_FILE_PATH);
        String privateKeyFilePath = properties.getProperty(GrpcIpcUtils.PRIVATE_KEY_FILE_PATH);
        String trustCertCollectionFilePath = properties.getProperty(GrpcIpcUtils.TRUST_CERTIFICATE_FILE_PATH);
        if (Strings.isNullOrEmpty(certChainFilePath) || Strings.isNullOrEmpty(privateKeyFilePath)) {
            return null;
        }
        SslContextBuilder sslClientContextBuilder = SslContextBuilder.forServer(new File(certChainFilePath),
                new File(privateKeyFilePath));
        if (!Strings.isNullOrEmpty(trustCertCollectionFilePath)) {
            sslClientContextBuilder.trustManager(new File(trustCertCollectionFilePath));
            sslClientContextBuilder.clientAuth(ClientAuth.REQUIRE);
        }
        return GrpcSslContexts.configure(sslClientContextBuilder,
                SslProvider.OPENSSL);
    }

    public static Properties getPropertiesFromConfig(ConfigurationAdmin configAdmin, String pid) {
        Properties properties = new Properties();
        try {
            final Dictionary<String, Object> config = configAdmin.getConfiguration(pid).getProperties();
            if (config != null) {
                final Enumeration<String> keys = config.keys();
                while (keys.hasMoreElements()) {
                    final String key = keys.nextElement();
                    properties.put(key, config.get(key));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot load config", e);
        }
        return properties;
    }
}
