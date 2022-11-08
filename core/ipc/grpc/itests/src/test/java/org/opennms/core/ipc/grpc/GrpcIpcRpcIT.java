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

package org.opennms.core.ipc.grpc;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.ServerSocket;
import java.util.Hashtable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.grpc.common.GrpcIpcServer;
import org.opennms.core.grpc.common.GrpcIpcServerBuilder;
import org.opennms.core.ipc.grpc.client.GrpcClientConstants;
import org.opennms.core.ipc.grpc.client.MinionGrpcClient;
import org.opennms.core.ipc.grpc.common.RpcRequestProto;
import org.opennms.core.ipc.grpc.server.GrpcServerConstants;
import org.opennms.core.ipc.grpc.server.OpennmsGrpcServer;
import org.opennms.core.rpc.echo.EchoRequest;
import org.opennms.core.rpc.echo.EchoResponse;
import org.opennms.core.rpc.echo.EchoRpcModule;
import org.opennms.distributed.core.api.MinionIdentity;
import org.osgi.service.cm.ConfigurationAdmin;

import com.google.common.base.Strings;

import io.grpc.stub.StreamObserver;

public class GrpcIpcRpcIT {

    private static final String REMOTE_LOCATION_NAME = "remote";
    private static final String MAX_BUFFER_SIZE_COUNTER = "1000000";
    private MockEchoClient echoClient;
    private MinionGrpcClient grpcClient;
    private OpennmsGrpcServer server;
    private ConfigurationAdmin configAdmin;
    private EchoRpcModule echoRpcModule = new EchoRpcModule();

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
        configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
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
    public void testRpcWithDefaultLocation() {
        await().atMost(10, TimeUnit.SECONDS).pollInterval(2, TimeUnit.SECONDS)
                .until(() -> {
                            grpcClient.dispatch(new HeartbeatModule(), null, new Heartbeat());
                            return server.getRpcHandlerByLocation().size();
                        },
                        not(0));
        EchoRequest request = new EchoRequest("gRPC-RPC-Request");
        EchoResponse expectedResponse = new EchoResponse("gRPC-RPC-Request");
        try {
            EchoResponse actualResponse = echoClient.execute(request).get();
            assertEquals(expectedResponse, actualResponse);
        } catch (InterruptedException | ExecutionException e) {
            fail();
        }

    }

    @Test(timeout = 30000)
    public void testRpcAtRemoteLocation() {
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

    @Test(timeout = 30000)
    public void testLargeMessageWithRpcAtRemoteLocation() {
        await().atMost(15, TimeUnit.SECONDS).pollInterval(2, TimeUnit.SECONDS)
                .until(() -> {
                            grpcClient.dispatch(new HeartbeatModule(), null, new Heartbeat());
                            return server.getRpcHandlerByLocation().size();
                        },
                        not(0));
        EchoRequest request = new EchoRequest();
        // Message size would be > 4MB which is default size for gRPC
        String message = Strings.repeat("OpenNMS", Integer.parseInt(MAX_BUFFER_SIZE_COUNTER));
        request.setBody(message);
        request.setLocation(REMOTE_LOCATION_NAME);
        EchoResponse expectedResponse = new EchoResponse();
        expectedResponse.setBody(message);
        try {
            EchoResponse actualResponse = echoClient.execute(request).get();
            assertEquals(expectedResponse, actualResponse);
        } catch (InterruptedException | ExecutionException e) {
            fail();
        }

    }

    @Test(timeout = 45000)
    public void testMultipleGrpcClientsIteration() throws Exception {
        MinionIdentity minionIdentity = new MockMinionIdentity(REMOTE_LOCATION_NAME, "minion2");
        MinionGrpcClient grpcClient2 = new MinionGrpcClient(minionIdentity, configAdmin);
        grpcClient2.bind(echoRpcModule);
        grpcClient2.start();
        MinionIdentity minionIdentity1 = new MockMinionIdentity(REMOTE_LOCATION_NAME, "minion3");
        MinionGrpcClient grpcClient3 = new MinionGrpcClient(minionIdentity1, configAdmin);
        grpcClient3.bind(echoRpcModule);
        grpcClient3.start();
        await().atMost(10, TimeUnit.SECONDS).pollInterval(2, TimeUnit.SECONDS)
                .until(() -> {
                            grpcClient.dispatch(new HeartbeatModule(), null, new Heartbeat());
                            grpcClient2.dispatch(new HeartbeatModule(), null, new Heartbeat());
                            grpcClient3.dispatch(new HeartbeatModule(), null, new Heartbeat());
                            return server.getRpcHandlerByLocation().size();
                        },
                        is(3));
        // Verify that rpc handler is iterative for 3 minions for a given location
        StreamObserver<RpcRequestProto> observer1 = server.getRpcHandler(REMOTE_LOCATION_NAME, null);
        StreamObserver<RpcRequestProto> observer2 = server.getRpcHandler(REMOTE_LOCATION_NAME, null);
        assertNotEquals(observer1, observer2);
        StreamObserver<RpcRequestProto> observer3 = server.getRpcHandler(REMOTE_LOCATION_NAME, null);
        assertNotEquals(observer2, observer3);
        assertNotEquals(observer1, observer3);


        grpcClient3.shutdown();
        await().atMost(10, TimeUnit.SECONDS).pollInterval(2, TimeUnit.SECONDS)
                .until(() -> server.getRpcHandlerByLocation().size(), is(2));

        // Verify that rpc handler is iterative for 2 minions for a given location
        observer1 = server.getRpcHandler(REMOTE_LOCATION_NAME, null);
        observer2 = server.getRpcHandler(REMOTE_LOCATION_NAME, null);
        assertNotEquals(observer1, observer2);
        observer3 = server.getRpcHandler(REMOTE_LOCATION_NAME, null);
        assertNotEquals(observer2, observer3);
        assertEquals(observer3, observer1);

        // Add one more minion.
        MinionGrpcClient grpcClient4 = new MinionGrpcClient(minionIdentity1, configAdmin);
        grpcClient4.bind(echoRpcModule);
        grpcClient4.start();
        await().atMost(10, TimeUnit.SECONDS).pollInterval(2, TimeUnit.SECONDS)
                .until(() -> {
                    grpcClient4.dispatch(new HeartbeatModule(), null, new Heartbeat());
            return server.getRpcHandlerByLocation().size();
            }, is(3));
        // Verify that rpc handler is iterative for 3 minions for a given location
        observer1 = server.getRpcHandler(REMOTE_LOCATION_NAME, null);
        observer2 = server.getRpcHandler(REMOTE_LOCATION_NAME, null);
        assertNotEquals(observer1, observer2);
        observer3 = server.getRpcHandler(REMOTE_LOCATION_NAME, null);
        assertNotEquals(observer2, observer3);
        assertNotEquals(observer1, observer3);

        grpcClient4.shutdown();
        grpcClient2.shutdown();
        await().atMost(10, TimeUnit.SECONDS).pollInterval(2, TimeUnit.SECONDS)
                .until(() -> server.getRpcHandlerByLocation().size(), is(1));

    }


    @After
    public void shutdown() throws Exception {
        grpcClient.unbind(echoRpcModule);
        grpcClient.shutdown();
        server.shutdown();
    }


    static int getAvailablePort(final AtomicInteger current, final int max) {
        while (current.get() < max) {
            try (final ServerSocket socket = new ServerSocket(current.get())) {
                return socket.getLocalPort();
            } catch (final Throwable e) {
            }
            current.incrementAndGet();
        }
        throw new IllegalStateException("Can't find an available network port");
    }
}
