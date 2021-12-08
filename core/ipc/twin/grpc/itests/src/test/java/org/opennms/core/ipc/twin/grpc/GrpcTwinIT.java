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

import io.opentracing.util.GlobalTracer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.grpc.common.GrpcIpcServerBuilder;
import org.opennms.core.grpc.common.GrpcIpcUtils;
import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.core.ipc.twin.common.LocalTwinSubscriber;
import org.opennms.core.ipc.twin.common.LocalTwinSubscriberImpl;
import org.opennms.core.ipc.twin.grpc.publisher.GrpcTwinPublisher;
import org.opennms.core.ipc.twin.grpc.subscriber.GrpcTwinSubscriber;
import org.opennms.core.ipc.twin.test.AbstractTwinBrokerIT;
import org.opennms.core.ipc.twin.test.MockMinionIdentity;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.distributed.core.api.MinionIdentity;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Hashtable;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GrpcTwinIT extends AbstractTwinBrokerIT {

    protected ConfigurationAdmin configAdmin;

    protected int port;

    @Override
    protected TwinPublisher createPublisher() throws IOException {
        final var grpcIpcServer = new GrpcIpcServerBuilder(this.configAdmin, this.port, "PT0S");

        TracerRegistry tracerRegistry = Mockito.mock(TracerRegistry.class);
        Mockito.when(tracerRegistry.getTracer()).thenReturn(GlobalTracer.get());
        LocalTwinSubscriber localTwinSubscriber = new LocalTwinSubscriberImpl(new MockMinionIdentity("Default"), tracerRegistry);
        final var publisher = new GrpcTwinPublisher(localTwinSubscriber, grpcIpcServer);
        publisher.start();

        return publisher;
    }

    @Override
    protected TwinSubscriber createSubscriber(MinionIdentity identity) throws Exception {
        final var minionIdentity = new MockMinionIdentity("remote");

        TracerRegistry tracerRegistry = Mockito.mock(TracerRegistry.class);
        Mockito.when(tracerRegistry.getTracer()).thenReturn(GlobalTracer.get());
        final var subscriber = new GrpcTwinSubscriber(minionIdentity, this.configAdmin, tracerRegistry, this.port);
        subscriber.start();

        return subscriber;
    }

    protected Hashtable<String, Object> getServerConfig(final int port) {
        final Hashtable<String, Object> serverConfig = new Hashtable<>();
        serverConfig.put(GrpcIpcUtils.GRPC_PORT, String.valueOf(port));
        serverConfig.put(GrpcIpcUtils.TLS_ENABLED, false);

        return serverConfig;
    }

    protected Hashtable<String, Object> getClientConfig(final int port) {
        final Hashtable<String, Object> clientConfig = new Hashtable<>();
        clientConfig.put(GrpcIpcUtils.GRPC_PORT, String.valueOf(port));
        clientConfig.put(GrpcIpcUtils.GRPC_HOST, "localhost");
        clientConfig.put(GrpcIpcUtils.TLS_ENABLED, false);

        return clientConfig;
    }

    @Before
    public void setup() throws Exception {
        this.port = getAvailablePort(GrpcIpcUtils.DEFAULT_TWIN_GRPC_PORT, 9090);

        final Hashtable<String, Object> serverConfig = this.getServerConfig(this.port);
        final Hashtable<String, Object> clientConfig = this.getClientConfig(this.port);

        this.configAdmin = mock(ConfigurationAdmin.class, Mockito.RETURNS_DEEP_STUBS);
        when(this.configAdmin.getConfiguration(GrpcIpcUtils.GRPC_SERVER_PID).getProperties()).thenReturn(serverConfig);
        when(this.configAdmin.getConfiguration(GrpcIpcUtils.GRPC_CLIENT_PID).getProperties()).thenReturn(clientConfig);

        super.setup();
    }

    @Test
    public void testPublisherRestart() throws Exception {
        // It takes a while to initialized RPC/Sink streams. Sleep allows that.
        Thread.sleep(5000);
        super.testPublisherRestart();
    }

    static int getAvailablePort(final int min, final int max) {
        int current = min;
        while (current < max) {
            try (final ServerSocket socket = new ServerSocket(current)) {
                return socket.getLocalPort();
            } catch (final Throwable e) {
                current++;
            }
        }
        throw new IllegalStateException("Can't find an available network port");
    }
}
