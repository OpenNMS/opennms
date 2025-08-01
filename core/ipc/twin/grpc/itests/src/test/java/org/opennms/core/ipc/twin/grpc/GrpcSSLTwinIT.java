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
