/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.smoketest;

import static com.jayway.awaitility.Awaitility.await;
import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.stream.Collectors;

import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.KarafShellUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import com.github.dockerjava.api.command.CreateContainerCmd;

public class OpenNMSCloudPluginIT {

    private static final String PATH_TO_FILE = "target/plugin-cloud-assembly/org.opennms.plugins.cloud-assembly.kar";
    private static final String PATH_TO_CLOUD_MOCK = "target/cloud-mock-with-dependencies/org.opennms.plugins.cloud-mock-with-dependencies.jar";
    private static final String CONTAINER_PATH = "/usr/share/opennms/deploy/";
    private static final String CLOUD_MOCK_JAR_NAME = "org.opennms.plugins.cloud-mock-with-dependencies.jar";
    private static final String CLOUD_MOCK_MAIN = "org.opennms.plugins.cloud.ittest.MockCloudMain";
    private static final String KAR_FILE_NAME = "org.opennms.plugins.cloud-assembly.kar";

    @ClassRule
    public static OpenNMSStack stack = OpenNMSStack.SENTINEL;

    @ClassRule
    public static GenericContainer mockCloudContainer = new GenericContainer(DockerImageName.parse("opennms/deploy-base:jre-2.0.6.b165"))
            .withCreateContainerCmdModifier(cmd -> {
                final CreateContainerCmd createCmd = (CreateContainerCmd) cmd;
                createCmd.withEntrypoint("/bin/bash");
                createCmd.withName("cloudMock");
            }).withCopyFileToContainer(MountableFile.forHostPath(PATH_TO_CLOUD_MOCK), format("%s%s", CONTAINER_PATH, CLOUD_MOCK_JAR_NAME))
            .withCommand("-c", format("java -cp %s%s %s", CONTAINER_PATH, CLOUD_MOCK_JAR_NAME, CLOUD_MOCK_MAIN)).withExposedPorts(9003)
            .withNetwork(stack.opennms().getNetwork())
            .withNetworkAliases("cloudMock");

    @Test
    public void installCloudPlugin() throws Exception {
        /* The sentinel plugin requires a successful installation of the core plugin first.
           Reason: The core plugin does the configuration and puts the certificates into the database via KV Store.
           The Sentinel Plugin needs those certificates. The reason it was implemented that way:
           The user needs to configure only one plugin.
        */
        installCloudPluginInOpennmsMustBeSuccessful();
        installCloudPluginInSentinelMustBeSuccessful();
    }

    public void installCloudPluginInOpennmsMustBeSuccessful() throws Exception {
        // Given
        stack.opennms().copyFileToContainer(MountableFile.forHostPath(PATH_TO_FILE), format("%s%s", CONTAINER_PATH, KAR_FILE_NAME));
        String config = createConfig();

        // When
        KarafShellUtils.withKarafShell(stack.opennms().getSshAddress(), Duration.ofMinutes(3), streams -> {
            streams.stdin.println(config);
            streams.stdin.println("feature:install opennms-plugin-cloud-core");
            streams.stdin.println("feature:list | grep opennms-plugin-cloud-core");
            await().atMost(com.jayway.awaitility.Duration.ONE_MINUTE).until(() -> streams.stdout.getLines().stream().anyMatch(line -> line.contains("Started")));
            streams.stdin.println("opennms-cloud:init key");
            await().atMost(com.jayway.awaitility.Duration.ONE_MINUTE).until(() ->
                    String.join("\n", streams.stdout.getLines()).contains("Initialization of cloud plugin in OPENNMS was successful"));
            return true;
        });
    }


    public void installCloudPluginInSentinelMustBeSuccessful() throws Exception {
        // Given
        stack.sentinel().copyFileToContainer(MountableFile.forHostPath(PATH_TO_FILE), format("%s%s", CONTAINER_PATH, KAR_FILE_NAME));
        String config = createConfig();

        // When
        KarafShellUtils.withKarafShell(stack.sentinel().getSshAddress(), Duration.ofMinutes(3), streams -> {
            streams.stdin.println(config);
            streams.stdin.printf("kar:install file:%s%s%n", CONTAINER_PATH, KAR_FILE_NAME);
            streams.stdin.println("feature:install opennms-plugin-cloud-sentinel");
            streams.stdin.println("feature:list | grep opennms-plugin-cloud-sentinel");
            await().atMost(com.jayway.awaitility.Duration.ONE_MINUTE).until(() -> streams.stdout.getLines().stream().anyMatch(line -> line.contains("Started")));
            await().atMost(com.jayway.awaitility.Duration.ONE_MINUTE).until(() ->
            {
                streams.stdin.println("opennms-cloud:init key");
                return streams.stdout.getLines().stream().anyMatch(line -> line.contains("Initialization of cloud plugin in SENTINEL was successful."));
            });
            await().atMost(com.jayway.awaitility.Duration.ONE_MINUTE).until(() ->
                    String.join("\n", streams.stdout.getLines()).contains("Initialization of cloud plugin in SENTINEL was successful."));
            return true;
        });
    }

    private String createConfig() throws IOException {
        return format("config:edit org.opennms.plugins.cloud%n"
                        + "property-set pas.tls.host %s%n"
                        + "property-set pas.tls.port %s%n"
                        + "property-set pas.tls.security TLS%n"
                        + "property-set pas.mtls.host %s%n"
                        + "property-set pas.mtls.port %s%n"
                        + "property-set pas.mtls.security MTLS%n"
                        + "property-set grpc.truststore \"%s\"%n"
                        + "config:update",
                "cloudMock",
                9003,
                "cloudMock",
                9003,
                classpathFileToString("/cert/horizon/servertruststore.pem")
        );
    }

    public static String classpathFileToString(final String fileInClasspath) throws IOException {
        URL url = new URL(format("jar:file:%s!%s", PATH_TO_CLOUD_MOCK, fileInClasspath));
        try (InputStream inputStream = url.openStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

}
