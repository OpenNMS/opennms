/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;

public class GrpcClientBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcClientBuilder.class);
    private static final String CLIENT_CERTIFICATE_FILE_PATH = "client.cert.filepath";
    private static final String CLIENT_PRIVATE_KEY_FILE_PATH = "client.private.key.filepath";
    private static final String TRUST_CERTIFICATE_FILE_PATH = "trust.cert.filepath";


    public static ManagedChannel getChannel(String host, int port, Map<String, String> tlsFilePaths) throws IOException {

        NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress(host, port)
                .keepAliveWithoutCalls(true);
        boolean tlsEnabled = Boolean.getBoolean(tlsFilePaths.get("tls.enabled"));
        if (tlsEnabled && tlsFilePaths != null && !tlsFilePaths.isEmpty()) {
            return channelBuilder
                    .negotiationType(NegotiationType.TLS)
                    .sslContext(buildSslContext(tlsFilePaths).build())
                    .build();
        } else {
            return channelBuilder.usePlaintext().build();
        }
    }

    private static SslContextBuilder buildSslContext(Map<String, String> tlsFilePaths) throws SSLException {
        SslContextBuilder builder = GrpcSslContexts.forClient();
        String clientCertChainFilePath = tlsFilePaths.get(CLIENT_CERTIFICATE_FILE_PATH);
        String clientPrivateKeyFilePath = tlsFilePaths.get(CLIENT_PRIVATE_KEY_FILE_PATH);
        String trustCertCollectionFilePath = tlsFilePaths.get(TRUST_CERTIFICATE_FILE_PATH);

        if (Strings.isNullOrEmpty(trustCertCollectionFilePath)) {
            builder.trustManager(new File(trustCertCollectionFilePath));
        }
        if (!Strings.isNullOrEmpty(clientCertChainFilePath) && !Strings.isNullOrEmpty(clientPrivateKeyFilePath)) {
            builder.keyManager(new File(clientCertChainFilePath), new File(clientPrivateKeyFilePath));
        }
        return builder;
    }
}
