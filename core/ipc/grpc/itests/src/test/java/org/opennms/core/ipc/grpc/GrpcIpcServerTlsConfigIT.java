/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2026 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2026 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opennms.core.ipc.grpc.GrpcIpcRpcIT.getAvailablePort;

import java.net.ConnectException;
import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Test;
import org.opennms.core.grpc.common.GrpcIpcServer;
import org.opennms.core.grpc.common.GrpcIpcServerBuilder;
import org.opennms.core.ipc.grpc.common.OpenNMSIpcGrpc;
import org.opennms.core.ipc.grpc.server.GrpcServerConstants;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * With tls.enabled=true, the gRPC IPC server must not start at all when its TLS settings are
 * incomplete or unusable. Both the RPC/Sink service and the Twin service call startServer(),
 * so each case also checks that the second call fails and does not start the server either.
 */
public class GrpcIpcServerTlsConfigIT {

    private GrpcIpcServer grpcIpcServer;
    private int port;

    @After
    public void tearDown() {
        if (grpcIpcServer != null) {
            grpcIpcServer.stopServer();
        }
    }

    @Test(timeout = 30000)
    public void doesNotStartWhenPrivateKeyPathIsNotSet() throws Exception {
        final Hashtable<String, Object> config = tlsConfig();
        config.remove(GrpcServerConstants.PRIVATE_KEY_FILE_PATH);
        assertServerDoesNotStart(config, "must both be set");
    }

    @Test(timeout = 30000)
    public void doesNotStartWhenCertificatePathIsNotSet() throws Exception {
        final Hashtable<String, Object> config = tlsConfig();
        config.remove(GrpcServerConstants.SERVER_CERTIFICATE_FILE_PATH);
        assertServerDoesNotStart(config, "must both be set");
    }

    @Test(timeout = 30000)
    public void doesNotStartWhenPrivateKeyFileDoesNotExist() throws Exception {
        final Hashtable<String, Object> config = tlsConfig();
        config.put(GrpcServerConstants.PRIVATE_KEY_FILE_PATH, "/definitely/not/here/server.pem");
        assertServerDoesNotStart(config, "cannot read the certificate or key files");
    }

    private Hashtable<String, Object> tlsConfig() {
        port = getAvailablePort(new AtomicInteger(GrpcServerConstants.DEFAULT_GRPC_PORT + 100), 9290);
        final Hashtable<String, Object> config = new Hashtable<>();
        config.put(GrpcServerConstants.GRPC_SERVER_PORT, String.valueOf(port));
        config.put(GrpcServerConstants.TLS_ENABLED, "true");
        config.put(GrpcServerConstants.SERVER_CERTIFICATE_FILE_PATH, resource("server.crt"));
        config.put(GrpcServerConstants.PRIVATE_KEY_FILE_PATH, resource("server.pem"));
        config.put(GrpcServerConstants.TRUST_CERTIFICATE_FILE_PATH, resource("ca.crt"));
        return config;
    }

    private void assertServerDoesNotStart(final Hashtable<String, Object> config, final String reason) throws Exception {
        final ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(GrpcServerConstants.GRPC_SERVER_PID).getProperties()).thenReturn(config);
        grpcIpcServer = new GrpcIpcServerBuilder(configAdmin, port, "PT0S");

        // First the RPC/Sink service, then the Twin service, as in a running Core.
        assertStartServerFails(reason);
        assertStartServerFails(reason);

        // The server start is scheduled with no delay, so give it time to bind if it was scheduled.
        TimeUnit.SECONDS.sleep(2);
        try (Socket socket = new Socket("localhost", port)) {
            fail("the gRPC server must not listen on port " + port + " when its TLS configuration is broken");
        } catch (ConnectException expected) {
            // Nothing is listening.
        }
    }

    private void assertStartServerFails(final String reason) throws Exception {
        try {
            grpcIpcServer.startServer(new OpenNMSIpcGrpc.OpenNMSIpcImplBase() { });
            fail("startServer must fail when tls.enabled is true and " + reason);
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage(), e.getMessage().contains(reason));
        }
    }

    private static String resource(final String name) {
        return GrpcIpcServerTlsConfigIT.class.getResource("/tls/" + name).getPath();
    }
}
