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

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.grpc.common.GrpcIpcServer;
import org.opennms.core.grpc.common.GrpcIpcServerBuilder;
import org.opennms.core.grpc.common.GrpcIpcUtils;
import org.opennms.core.ipc.grpc.common.OpenNMSIpcGrpc;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * With tls.enabled=true, the gRPC IPC server must not start when its TLS settings are incomplete
 * or unusable. The RPC/Sink and Twin services both call startServer(), so each case checks that
 * both calls fail. A valid TLS configuration is covered by GrpcTLSMutualAuthIT.
 */
public class GrpcIpcServerTlsConfigIT {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private GrpcIpcServer grpcIpcServer;

    @After
    public void tearDown() {
        if (grpcIpcServer != null) {
            grpcIpcServer.stopServer();
        }
    }

    @Test
    public void doesNotStartWhenPrivateKeyPathIsNotSet() throws Exception {
        final Hashtable<String, Object> config = tlsConfig();
        config.remove(GrpcIpcUtils.PRIVATE_KEY_FILE_PATH);
        assertServerDoesNotStart(config, "must both be set");
    }

    @Test
    public void doesNotStartWhenCertificatePathIsNotSet() throws Exception {
        final Hashtable<String, Object> config = tlsConfig();
        config.remove(GrpcIpcUtils.SERVER_CERTIFICATE_FILE_PATH);
        assertServerDoesNotStart(config, "must both be set");
    }

    @Test
    public void doesNotStartWhenPrivateKeyFileDoesNotExist() throws Exception {
        final Hashtable<String, Object> config = tlsConfig();
        config.put(GrpcIpcUtils.PRIVATE_KEY_FILE_PATH, new File(tmp.getRoot(), "missing.pem").getAbsolutePath());
        assertServerDoesNotStart(config, "cannot set up TLS");
    }

    @Test
    public void doesNotStartWhenPrivateKeyFileIsNotAKey() throws Exception {
        final Hashtable<String, Object> config = tlsConfig();
        config.put(GrpcIpcUtils.PRIVATE_KEY_FILE_PATH, garbageFile("server.pem", "PRIVATE KEY"));
        assertServerDoesNotStart(config, "cannot set up TLS");
    }

    @Test
    public void doesNotStartWhenCertificateFileIsNotACertificate() throws Exception {
        final Hashtable<String, Object> config = tlsConfig();
        config.put(GrpcIpcUtils.SERVER_CERTIFICATE_FILE_PATH, garbageFile("server.crt", "CERTIFICATE"));
        assertServerDoesNotStart(config, "cannot set up TLS");
    }

    private static Hashtable<String, Object> tlsConfig() throws Exception {
        final Hashtable<String, Object> config = new Hashtable<>();
        config.put(GrpcIpcUtils.TLS_ENABLED, "true");
        config.put(GrpcIpcUtils.SERVER_CERTIFICATE_FILE_PATH, resource("server.crt"));
        config.put(GrpcIpcUtils.PRIVATE_KEY_FILE_PATH, resource("server.pem"));
        config.put(GrpcIpcUtils.TRUST_CERTIFICATE_FILE_PATH, resource("ca.crt"));
        return config;
    }

    private void assertServerDoesNotStart(final Hashtable<String, Object> config, final String reason) throws Exception {
        final ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(GrpcIpcUtils.GRPC_SERVER_PID).getProperties()).thenReturn(config);
        grpcIpcServer = new GrpcIpcServerBuilder(configAdmin, freePort(), "PT0S");

        // First the RPC/Sink service, then the Twin service, as in a running Core. The second call
        // must fail too, with the same exception, rather than start the server without TLS.
        final IllegalStateException first = startServerFails(reason);
        final IllegalStateException second = startServerFails(reason);
        assertSame(first, second);
    }

    private IllegalStateException startServerFails(final String reason) throws Exception {
        try {
            grpcIpcServer.startServer(new OpenNMSIpcGrpc.OpenNMSIpcImplBase() { });
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage(), e.getMessage().contains(reason));
            return e;
        }
        throw new AssertionError("startServer must fail when tls.enabled is true and the TLS settings are not usable");
    }

    private String garbageFile(final String name, final String label) throws Exception {
        final File file = tmp.newFile(name);
        Files.write(file.toPath(), ("-----BEGIN " + label + "-----\nbm90IGEgcmVhbCBrZXk=\n-----END " + label + "-----\n")
                .getBytes(StandardCharsets.US_ASCII));
        return file.getAbsolutePath();
    }

    private static int freePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static String resource(final String name) throws Exception {
        return Paths.get(GrpcIpcServerTlsConfigIT.class.getResource("/tls/" + name).toURI()).toString();
    }
}
