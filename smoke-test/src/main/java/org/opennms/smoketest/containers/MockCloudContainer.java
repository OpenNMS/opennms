/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2024 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2024 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.containers;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import com.google.common.base.Joiner;

public class MockCloudContainer extends GenericContainer<MockCloudContainer> {
    public static final String ALIAS = "cloudMock";
    public static final int PORT = 9003;

    private static final Path CLOUD_MOCK_PATH_HOST = Path.of("target/cloud-mock-with-dependencies/org.opennms.plugins.cloud-mock-with-dependencies.jar");
    private static final Path CLOUD_MOCK_PATH_CONTAINER = Path.of("/").resolve(CLOUD_MOCK_PATH_HOST.getFileName());
    private static final String CLOUD_MOCK_MAIN = "org.opennms.plugins.cloud.ittest.MockCloudMain";
    public MockCloudContainer() {
        super(DockerImageName.parse("opennms/deploy-base:ubuntu-3.4.0.b268-jre-11"));
        withCopyFileToContainer(MountableFile.forHostPath(CLOUD_MOCK_PATH_HOST), CLOUD_MOCK_PATH_CONTAINER.toString())
                .withCommand("/usr/bin/java", "-cp", CLOUD_MOCK_PATH_CONTAINER.toString(), CLOUD_MOCK_MAIN)
                .withExposedPorts(PORT)
                .withNetwork(Network.SHARED)
                .withNetworkAliases(ALIAS);
    }

    public static String classpathFileToString(final String fileInClasspath) throws IOException {
        URL url = new URL(format("jar:file:%s!%s", CLOUD_MOCK_PATH_HOST, fileInClasspath));
        try (InputStream inputStream = url.openStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static String createConfig() throws IOException {
        return Joiner.on('\n').join(new String[] {
                "config:edit org.opennms.plugins.cloud",
                String.format("property-set pas.tls.host %s", ALIAS),
                String.format("property-set pas.tls.port %s", PORT),
                "property-set pas.tls.security TLS",
                String.format("property-set pas.mtls.host %s", ALIAS),
                String.format("property-set pas.mtls.port %s", PORT),
                "property-set pas.mtls.security MTLS",
                String.format("property-set grpc.truststore \"%s\"",
                        classpathFileToString("/cert/horizon/servertruststore.pem")),
                "config:update",
        });
    }
}
