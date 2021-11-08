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

package org.opennms.core.ipc.twin.grpc;

import org.opennms.core.grpc.common.GrpcIpcUtils;
import java.util.Hashtable;

public class GrpcSSLTwinIT extends GrpcTwinIT {

    private final String SERVER_CERT_FILE_PATH = GrpcSSLTwinIT.class.getResource("/tls/server.crt").getPath();
    private final String SERVER_KEY_FILE_PATH = GrpcSSLTwinIT.class.getResource("/tls/server.pem").getPath();
    private final String TRUST_CERT_FILE_PATH = GrpcSSLTwinIT.class.getResource("/tls/ca.crt").getPath();
    private final String CLIENT_CERT_FILE_PATH = GrpcSSLTwinIT.class.getResource("/tls/client.crt").getPath();
    private final String CLIENT_KEY_FILE_PATH = GrpcSSLTwinIT.class.getResource("/tls/client.pem").getPath();

    protected Hashtable<String, Object> getServerConfig(final int port) {
        final Hashtable<String, Object> serverConfig = new Hashtable<>();
        serverConfig.put(GrpcIpcUtils.GRPC_PORT, String.valueOf(port));
        serverConfig.put(GrpcIpcUtils.TLS_ENABLED, true);
        serverConfig.put(GrpcIpcUtils.SERVER_CERTIFICATE_FILE_PATH, SERVER_CERT_FILE_PATH);
        serverConfig.put(GrpcIpcUtils.PRIVATE_KEY_FILE_PATH, SERVER_KEY_FILE_PATH);
        serverConfig.put(GrpcIpcUtils.TRUST_CERTIFICATE_FILE_PATH, TRUST_CERT_FILE_PATH);

        return serverConfig;
    }

    protected Hashtable<String, Object> getClientConfig(final int port) {
        final Hashtable<String, Object> clientConfig = new Hashtable<>();
        clientConfig.put(GrpcIpcUtils.GRPC_PORT, String.valueOf(port));
        clientConfig.put(GrpcIpcUtils.GRPC_HOST, "localhost");
        clientConfig.put(GrpcIpcUtils.TLS_ENABLED, true);
        clientConfig.put(GrpcIpcUtils.TRUST_CERTIFICATE_FILE_PATH, TRUST_CERT_FILE_PATH);
        clientConfig.put(GrpcIpcUtils.CLIENT_CERTIFICATE_FILE_PATH, CLIENT_CERT_FILE_PATH);
        clientConfig.put(GrpcIpcUtils.CLIENT_PRIVATE_KEY_FILE_PATH, CLIENT_KEY_FILE_PATH);

        return clientConfig;
    }
}
