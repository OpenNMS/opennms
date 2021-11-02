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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.grpc.common.GrpcIpcServer;
import org.opennms.core.grpc.common.GrpcIpcServerBuilder;
import org.opennms.core.grpc.common.GrpcIpcUtils;
import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.core.ipc.twin.common.LocalTwinSubscriberImpl;
import org.opennms.core.ipc.twin.grpc.publisher.GrpcTwinPublisher;
import org.opennms.core.ipc.twin.grpc.subscriber.GrpcTwinSubscriber;
import org.opennms.core.ipc.twin.test.AbstractTwinBrokerIT;
import org.opennms.core.ipc.twin.test.MockMinionIdentity;
import org.opennms.distributed.core.api.MinionIdentity;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GrpcTwinIT extends AbstractTwinBrokerIT {

    protected GrpcTwinSubscriber subscriber;
    protected GrpcTwinPublisher publisher;
    protected ConfigurationAdmin configAdmin;
    protected int port;

    @Override
    protected TwinPublisher createPublisher() throws IOException {
        GrpcIpcServer grpcIpcServer = new GrpcIpcServerBuilder(configAdmin, port, "PT0S");
        this.publisher = new GrpcTwinPublisher(new LocalTwinSubscriberImpl(new MockMinionIdentity("Default")), grpcIpcServer);
        publisher.start();
        return publisher;
    }

    @Override
    protected TwinSubscriber createSubscriber(MinionIdentity identity) throws Exception {
        MinionIdentity minionIdentity = new MockMinionIdentity("remote");
        subscriber = new GrpcTwinSubscriber(minionIdentity, configAdmin, port);
        subscriber.start();
        return subscriber;
    }

    @Before
    public void setup() throws Exception {
        Hashtable<String, Object> serverConfig = new Hashtable<>();
        port = getAvailablePort(new AtomicInteger(GrpcIpcUtils.DEFAULT_TWIN_GRPC_PORT), 9090);
        serverConfig.put(GrpcIpcUtils.GRPC_PORT, String.valueOf(port));
        serverConfig.put(GrpcIpcUtils.TLS_ENABLED, false);
        Hashtable<String, Object> clientConfig = new Hashtable<>();
        clientConfig.put(GrpcIpcUtils.GRPC_PORT, String.valueOf(port));

        clientConfig.put(GrpcIpcUtils.GRPC_HOST, "localhost");
        clientConfig.put(GrpcIpcUtils.TLS_ENABLED, false);
        configAdmin = mock(ConfigurationAdmin.class, Mockito.RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(GrpcIpcUtils.GRPC_SERVER_PID).getProperties()).thenReturn(serverConfig);
        when(configAdmin.getConfiguration(GrpcIpcUtils.GRPC_CLIENT_PID).getProperties()).thenReturn(clientConfig);
        super.setup();
    }

    public void setupAbstract() throws Exception {
        super.setup();
    }

    @Test
    public void testPublisherRestart() throws Exception {
        // It takes a while to initialized RPC/Sink streams. Sleep allows that.
        Thread.sleep(5000);
        super.testPublisherRestart();
    }

    @After
    public void destroy() throws IOException {
        subscriber.close();
        publisher.close();
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
