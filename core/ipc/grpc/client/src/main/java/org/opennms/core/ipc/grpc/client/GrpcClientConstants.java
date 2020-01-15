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

package org.opennms.core.ipc.grpc.client;

public interface GrpcClientConstants {

    String GRPC_CLIENT_PID = "org.opennms.core.ipc.grpc.client";
    String GRPC_HOST = "host";
    String DEFAULT_GRPC_HOST  = "localhost";
    String GRPC_PORT = "port";
    int DEFAULT_GRPC_PORT = 8990;
    String TLS_ENABLED = "tls.enabled";
    String GRPC_MAX_INBOUND_SIZE = "max.message.size";
    int DEFAULT_MESSAGE_SIZE = 10485760;
    String CLIENT_CERTIFICATE_FILE_PATH = "client.cert.filepath";
    String CLIENT_PRIVATE_KEY_FILE_PATH = "client.private.key.filepath";
    String TRUST_CERTIFICATE_FILE_PATH = "trust.cert.filepath";
}
