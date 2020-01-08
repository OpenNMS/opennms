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

package org.opennms.core.ipc.grpc.server;

public interface GrpcServerConstants {

    String GRPC_SERVER_PID = "org.opennms.core.ipc.grpc.server";
    String  GRPC_TTL_PROPERTY = "ttl";
    long DEFAULT_GRPC_TTL = 20000;
    long DEFAULT_ACK_TIMEOUT = 30000;
    String GRPC_MAX_INBOUND_SIZE = "maxInboundMessageSize";
    int DEFAULT_MESSAGE_SIZE = 10485760; //10MB
    int DEFAULT_GRPC_PORT = 8990;
    String GRPC_SERVER_PORT = "port";
    String GRPC_SERVER_HOST = "host";
    String DEFAULT_HOST_ADDRESS = "localhost";
    String TLS_ENABLED = "tlsEnabled";
    String SERVER_CERTIFICATE_FILE_PATH = "certChainFilePath";
    String CLIENT_CERTIFICATE_FILE_PATH = "clientCertChainFilePath";
    String PRIVATE_KEY_FILE_PATH = "privateKeyFilePath";
}
