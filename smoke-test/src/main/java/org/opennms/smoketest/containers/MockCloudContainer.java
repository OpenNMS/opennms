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
        super(DockerImageName.parse("quay.io/bluebird/deploy-base:2.0.1.b20"));
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
