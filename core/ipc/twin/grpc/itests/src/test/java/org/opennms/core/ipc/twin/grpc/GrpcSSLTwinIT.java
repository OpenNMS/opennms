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
import org.opennms.core.grpc.common.GrpcIpcUtils;
import org.opennms.core.ipc.twin.common.LocalTwinSubscriberImpl;
import org.opennms.core.ipc.twin.grpc.publisher.GrpcTwinPublisher;
import org.opennms.core.ipc.twin.grpc.subscriber.GrpcTwinSubscriber;
import org.opennms.distributed.core.api.MinionIdentity;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class GrpcSSLTwinIT extends GrpcTwinIT {

    @Before
    public void setup() throws IOException {
        String serverCertFilePath = this.getClass().getResource("/tls/server.crt").getPath();
        String serverKeyFilePath = this.getClass().getResource("/tls/server.pem").getPath();
        String trustCertFilePath = this.getClass().getResource("/tls/ca.crt").getPath();
        String clientCertFilePath = this.getClass().getResource("/tls/client.crt").getPath();
        String clientPrivateKeyFilePath = this.getClass().getResource("/tls/client.pem").getPath();

        Hashtable<String, Object> serverConfig = new Hashtable<>();
        int port = getAvailablePort(new AtomicInteger(GrpcIpcUtils.DEFAULT_TWIN_GRPC_PORT), 9090);
        serverConfig.put(GrpcIpcUtils.GRPC_PORT, String.valueOf(port));
        serverConfig.put(GrpcIpcUtils.TLS_ENABLED, "true");
        serverConfig.put(GrpcIpcUtils.SERVER_CERTIFICATE_FILE_PATH, serverCertFilePath);
        serverConfig.put(GrpcIpcUtils.PRIVATE_KEY_FILE_PATH, serverKeyFilePath);
        serverConfig.put(GrpcIpcUtils.TRUST_CERTIFICATE_FILE_PATH, trustCertFilePath);

        Hashtable<String, Object> clientConfig = new Hashtable<>();
        clientConfig.put(GrpcIpcUtils.GRPC_PORT, String.valueOf(port));
        clientConfig.put(GrpcIpcUtils.GRPC_HOST, "localhost");
        clientConfig.put(GrpcIpcUtils.TLS_ENABLED, "true");
        clientConfig.put(GrpcIpcUtils.TRUST_CERTIFICATE_FILE_PATH, trustCertFilePath);
        clientConfig.put(GrpcIpcUtils.CLIENT_CERTIFICATE_FILE_PATH, clientCertFilePath);
        clientConfig.put(GrpcIpcUtils.CLIENT_PRIVATE_KEY_FILE_PATH, clientPrivateKeyFilePath);

        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(GrpcIpcUtils.GRPC_SERVER_PID).getProperties()).thenReturn(serverConfig);
        when(configAdmin.getConfiguration(GrpcIpcUtils.GRPC_CLIENT_PID).getProperties()).thenReturn(clientConfig);

        MinionIdentity minionIdentity = new MockMinionIdentity("remote");
        twinSubscriber = new GrpcTwinSubscriber(minionIdentity, configAdmin, port);
        twinSubscriber.start();
        twinPublisher = new GrpcTwinPublisher(new LocalTwinSubscriberImpl(), configAdmin, port);
        twinPublisher.start();
    }

    @After
    public void destroy() {
        super.destroy();
    }
}
