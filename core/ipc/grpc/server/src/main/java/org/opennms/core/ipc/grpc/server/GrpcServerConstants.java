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
package org.opennms.core.ipc.grpc.server;

public interface GrpcServerConstants {

    String GRPC_SERVER_PID = "org.opennms.core.ipc.grpc.server";
    String  GRPC_TTL_PROPERTY = "ttl";
    long DEFAULT_GRPC_TTL = 20000;
    String GRPC_MAX_INBOUND_SIZE = "max.message.size";
    int DEFAULT_MESSAGE_SIZE = 10485760; //10MB
    int DEFAULT_GRPC_PORT = 8990;
    String GRPC_SERVER_PORT = "port";
    String TLS_ENABLED = "tls.enabled";
    String SERVER_CERTIFICATE_FILE_PATH = "server.cert.filepath";
    String PRIVATE_KEY_FILE_PATH = "server.private.key.filepath";
    String TRUST_CERTIFICATE_FILE_PATH = "trust.cert.filepath";
}
