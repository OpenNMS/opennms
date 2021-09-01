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

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.grpc.common.GrpcIpcUtils;
import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.ipc.twin.grpc.publisher.GrpcTwinPublisher;
import org.opennms.core.ipc.twin.grpc.subscriber.GrpcTwinSubscriber;
import org.opennms.distributed.core.api.MinionIdentity;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.jayway.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GrpcTwinIT {

    protected GrpcTwinSubscriber twinSubscriber;
    protected GrpcTwinPublisher twinPublisher;
    protected Set<MinionInfoBean> minionInfoBeanSet = new HashSet<>();


    @Before
    public void setup() throws IOException {
        Hashtable<String, Object> serverConfig = new Hashtable<>();
        int port = getAvailablePort(new AtomicInteger(GrpcIpcUtils.DEFAULT_TWIN_GRPC_PORT), 9090);
        serverConfig.put(GrpcIpcUtils.GRPC_PORT, String.valueOf(port));
        serverConfig.put(GrpcIpcUtils.TLS_ENABLED, false);
        Hashtable<String, Object> clientConfig = new Hashtable<>();
        clientConfig.put(GrpcIpcUtils.GRPC_PORT, String.valueOf(port));

        clientConfig.put(GrpcIpcUtils.GRPC_HOST, "localhost");
        clientConfig.put(GrpcIpcUtils.TLS_ENABLED, false);
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, Mockito.RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(GrpcIpcUtils.GRPC_SERVER_PID).getProperties()).thenReturn(serverConfig);
        when(configAdmin.getConfiguration(GrpcIpcUtils.GRPC_CLIENT_PID).getProperties()).thenReturn(clientConfig);

        MinionIdentity minionIdentity = new MockMinionIdentity("remote");
        twinSubscriber = new GrpcTwinSubscriber(minionIdentity, configAdmin, port);
        twinSubscriber.start();
        twinPublisher = new GrpcTwinPublisher(configAdmin, port);
        twinPublisher.start();
    }

    @Test
    public void testGrpcTwinConsumption() throws IOException {
        String key = "minion-info-bean";
        TwinPublisher.Session<MinionInfoBean> session = twinPublisher.register(key, MinionInfoBean.class);
        session.publish(new MinionInfoBean(5, "Twin-Node"));
        twinSubscriber.subscribe(key, MinionInfoBean.class, new TwinConsumer());
        await().atMost(15, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(() -> minionInfoBeanSet, Matchers.hasSize(1));
        minionInfoBeanSet.clear();
        // Send update to MinionInfoBean
        String updatedLabel = "Twin-Node-Update-Label";
        session.publish(new MinionInfoBean(5, updatedLabel));
        await().atMost(15, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(() -> minionInfoBeanSet, Matchers.hasSize(1));
        MinionInfoBean minionInfoBean = minionInfoBeanSet.iterator().next();
        Assert.assertThat(minionInfoBean.getNodeLabel(), Matchers.equalTo(updatedLabel));
    }

    @After
    public void destroy() {
        twinSubscriber.shutdown();
        twinPublisher.shutdown();
    }

    private class TwinConsumer implements Consumer<MinionInfoBean> {

        @Override
        public void accept(MinionInfoBean minionInfoBean) {
            minionInfoBeanSet.add(minionInfoBean);
        }
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
