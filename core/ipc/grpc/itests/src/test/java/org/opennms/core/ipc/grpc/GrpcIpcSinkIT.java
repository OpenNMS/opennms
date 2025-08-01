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
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opennms.core.ipc.grpc.GrpcIpcRpcIT.getAvailablePort;

import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.grpc.common.GrpcIpcServer;
import org.opennms.core.grpc.common.GrpcIpcServerBuilder;
import org.opennms.core.ipc.grpc.client.GrpcClientConstants;
import org.opennms.core.ipc.grpc.client.MinionGrpcClient;
import org.opennms.core.ipc.grpc.server.GrpcServerConstants;
import org.opennms.core.ipc.grpc.server.OpennmsGrpcServer;
import org.opennms.distributed.core.api.MinionIdentity;
import org.osgi.service.cm.ConfigurationAdmin;

import com.codahale.metrics.Meter;

/**
 * This test verifies that if there is no gRPC server available, sink message dispatch will block until it succeeds.
 */
public class GrpcIpcSinkIT {

    private static final String REMOTE_LOCATION_NAME = "remote";
    private MinionGrpcClient grpcClient;
    private OpennmsGrpcServer server;
    private HeartbeatModule asyncModule = new HeartbeatModule(true);
    private boolean serverStarted = false;

    @Before
    public void setup() throws Exception {
        Hashtable<String, Object> serverConfig = new Hashtable<>();
        int port = getAvailablePort(new AtomicInteger(GrpcServerConstants.DEFAULT_GRPC_PORT), 9090);
        serverConfig.put(GrpcServerConstants.GRPC_SERVER_PORT, String.valueOf(port));
        serverConfig.put(GrpcServerConstants.TLS_ENABLED, false);
        Hashtable<String, Object> clientConfig = new Hashtable<>();
        clientConfig.put(GrpcClientConstants.GRPC_PORT, String.valueOf(port));

        clientConfig.put(GrpcClientConstants.GRPC_HOST, "localhost");
        clientConfig.put(GrpcClientConstants.TLS_ENABLED, false);
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(GrpcServerConstants.GRPC_SERVER_PID).getProperties()).thenReturn(serverConfig);
        when(configAdmin.getConfiguration(GrpcClientConstants.GRPC_CLIENT_PID).getProperties()).thenReturn(clientConfig);

        MinionIdentity minionIdentity = new MockMinionIdentity(REMOTE_LOCATION_NAME);

        grpcClient = new MinionGrpcClient(minionIdentity, configAdmin);
        GrpcIpcServer grpcIpcServer = new GrpcIpcServerBuilder(configAdmin, port, "PT0S");
        server = new OpennmsGrpcServer(grpcIpcServer);
        grpcClient.start();
    }

    @Test(timeout = 30000)
    public void testSinkMessageGetsDeliveredAsynchronously() throws Exception {
        // Sending message on async module will block until it delivers.
        new Thread(() -> grpcClient.dispatch(asyncModule, null, new Heartbeat())).start();

        Meter meter = new Meter();
        server.registerConsumer(new HeartbeatConsumer(asyncModule, meter));

        Assert.assertEquals(0, meter.getCount());
        // Verify that async message gets delivered after server started.
        await().atMost(20, TimeUnit.SECONDS).pollDelay(3, TimeUnit.SECONDS).pollInterval(3, TimeUnit.SECONDS)
                .until(() -> {
                    startServer();
                    // Sending this message ensures that sink stream is initialized, since this is sync module, it shouldn't block
                    grpcClient.dispatch(new HeartbeatModule(), null, new Heartbeat());
                    return meter.getCount();
                }, Matchers.greaterThan(0L));
    }

    private void startServer() throws IOException {
        if (!serverStarted) {
            server.start();
            serverStarted = true;
        }
    }

    @After
    public void shutdown() throws Exception {
        grpcClient.shutdown();
        server.shutdown();
    }
}
