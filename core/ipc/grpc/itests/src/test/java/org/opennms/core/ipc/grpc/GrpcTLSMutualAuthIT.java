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
package org.opennms.core.ipc.grpc;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opennms.core.ipc.grpc.GrpcIpcRpcIT.getAvailablePort;

import java.util.Hashtable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.grpc.common.GrpcIpcServer;
import org.opennms.core.grpc.common.GrpcIpcServerBuilder;
import org.opennms.core.ipc.grpc.client.GrpcClientConstants;
import org.opennms.core.ipc.grpc.client.MinionGrpcClient;
import org.opennms.core.ipc.grpc.server.GrpcServerConstants;
import org.opennms.core.ipc.grpc.server.OpennmsGrpcServer;
import org.opennms.core.rpc.echo.EchoRequest;
import org.opennms.core.rpc.echo.EchoResponse;
import org.opennms.core.rpc.echo.EchoRpcModule;
import org.opennms.distributed.core.api.MinionIdentity;
import org.osgi.service.cm.ConfigurationAdmin;

public class GrpcTLSMutualAuthIT {

    private static final String REMOTE_LOCATION_NAME = "remote";
    private static final String MAX_BUFFER_SIZE_COUNTER = "1000000";
    private MockEchoClient echoClient;
    private MinionGrpcClient grpcClient;
    private OpennmsGrpcServer server;
    private EchoRpcModule echoRpcModule = new EchoRpcModule();

    @Before
    public void setup() throws Exception {

        String serverCertFilePath = this.getClass().getResource("/tls/server.crt").getPath();
        String serverKeyFilePath = this.getClass().getResource("/tls/server.pem").getPath();
        String trustCertFilePath = this.getClass().getResource("/tls/ca.crt").getPath();
        String clientCertFilePath = this.getClass().getResource("/tls/client.crt").getPath();
        String clientPrivateKeyFilePath = this.getClass().getResource("/tls/client.pem").getPath();

        Hashtable<String, Object> serverConfig = new Hashtable<>();
        int port = getAvailablePort(new AtomicInteger(GrpcServerConstants.DEFAULT_GRPC_PORT), 9090);
        serverConfig.put(GrpcServerConstants.GRPC_SERVER_PORT, String.valueOf(port));
        serverConfig.put(GrpcServerConstants.TLS_ENABLED, "true");
        serverConfig.put(GrpcServerConstants.SERVER_CERTIFICATE_FILE_PATH, serverCertFilePath);
        serverConfig.put(GrpcServerConstants.PRIVATE_KEY_FILE_PATH, serverKeyFilePath);
        serverConfig.put(GrpcServerConstants.TRUST_CERTIFICATE_FILE_PATH, trustCertFilePath);

        Hashtable<String, Object> clientConfig = new Hashtable<>();
        clientConfig.put(GrpcClientConstants.GRPC_PORT, String.valueOf(port));
        clientConfig.put(GrpcClientConstants.GRPC_HOST, "localhost");
        clientConfig.put(GrpcClientConstants.TLS_ENABLED, "true");
        clientConfig.put(GrpcClientConstants.TRUST_CERTIFICATE_FILE_PATH, trustCertFilePath);
        clientConfig.put(GrpcClientConstants.CLIENT_CERTIFICATE_FILE_PATH, clientCertFilePath);
        clientConfig.put(GrpcClientConstants.CLIENT_PRIVATE_KEY_FILE_PATH, clientPrivateKeyFilePath);

        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(GrpcServerConstants.GRPC_SERVER_PID).getProperties()).thenReturn(serverConfig);
        when(configAdmin.getConfiguration(GrpcClientConstants.GRPC_CLIENT_PID).getProperties()).thenReturn(clientConfig);

        MinionIdentity minionIdentity = new MockMinionIdentity(REMOTE_LOCATION_NAME);

        grpcClient = new MinionGrpcClient(minionIdentity, configAdmin);
        grpcClient.bind(echoRpcModule);
        GrpcIpcServer grpcIpcServer = new GrpcIpcServerBuilder(configAdmin, port, "PT0S");
        server = new OpennmsGrpcServer(grpcIpcServer);
        echoClient = new MockEchoClient(server);
        server.start();
        grpcClient.start();
    }

    @Test(timeout = 30000)
    public void testgRPCWithTLSAtRemoteLocation() {
        await().atMost(15, TimeUnit.SECONDS).pollInterval(2, TimeUnit.SECONDS)
                .until(() -> {
                            grpcClient.dispatch(new HeartbeatModule(), null, new Heartbeat());
                            return server.getRpcHandlerByLocation().size();
                        },
                        not(0));
        EchoRequest request = new EchoRequest("gRPC-RPC-Request");
        request.setLocation(REMOTE_LOCATION_NAME);
        EchoResponse expectedResponse = new EchoResponse("gRPC-RPC-Request");
        try {
            EchoResponse actualResponse = echoClient.execute(request).get();
            assertEquals(expectedResponse, actualResponse);
        } catch (InterruptedException | ExecutionException e) {
            fail();
        }

    }
}
