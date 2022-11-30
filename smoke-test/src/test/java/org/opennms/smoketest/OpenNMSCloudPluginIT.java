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

import static java.lang.String.format;
import static java.util.Objects.nonNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.KarafShell;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
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

    private KarafShell opennmsShell;
    private KarafShell sentinelShell;

    @ClassRule
    public static Network network = Network.newNetwork();

    @ClassRule
    public static OpenNMSStack stack = OpenNMSStack
            .withModel(StackModel.newBuilder()
                    .withSentinel()
                    .build());

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

    @Before
    public void setUp() {
        this.opennmsShell = new KarafShell(stack.opennms().getSshAddress());
        this.sentinelShell = new KarafShell(stack.sentinel().getSshAddress());
        mockCloudContainer.start();
    }

    @Test
    public void installCloudPlugin() throws Exception {
        install_cloud_plugin_in_opennms_must_be_successful();
        install_cloud_plugin_in_sentinel_must_be_successful();
    }

    public void install_cloud_plugin_in_opennms_must_be_successful() throws Exception {
        // Given
        stack.opennms().copyFileToContainer(MountableFile.forHostPath(PATH_TO_FILE), format("%s%s", CONTAINER_PATH, KAR_FILE_NAME));
        String config = createConfig();

        // When
        opennmsShell.runCommand(String.format("kar:install file:%s%s", CONTAINER_PATH, KAR_FILE_NAME));
        opennmsShell.runCommand("feature:install opennms-plugin-cloud-core");
        opennmsShell.runCommand("feature:list | grep opennms-plugin-cloud-core", output -> output.contains("Started"));
        opennmsShell.runCommand(config);
        opennmsShell.runCommand("opennms-cloud:init key", output -> output.contains("Initialization of cloud plugin in OPENNMS was successful."));
    }


    public void install_cloud_plugin_in_sentinel_must_be_successful() throws Exception {
        // Given
        stack.sentinel().copyFileToContainer(MountableFile.forHostPath(PATH_TO_FILE), format("%s%s", CONTAINER_PATH, KAR_FILE_NAME));
        String config = createConfig();

        // When
        sentinelShell.runCommand(String.format("kar:install file:%s%s", CONTAINER_PATH, KAR_FILE_NAME));
        sentinelShell.runCommand("feature:install opennms-plugin-cloud-sentinel");
        sentinelShell.runCommand("feature:list | grep opennms-plugin-cloud-sentinel", output -> output.contains("Started"));
        sentinelShell.runCommand(config);
        sentinelShell.runCommand("opennms-cloud:init key", output -> output.contains("Initialization of cloud plugin in SENTINEL was successful."));
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

    @AfterClass
    public static void afterAll() {
        if (nonNull(stack)) {
            if (nonNull(stack.opennms())) {
                stack.opennms().stop();
            }
            if (nonNull(stack.sentinel())) {
                stack.sentinel().stop();
            }
            if (nonNull(mockCloudContainer)) {
                mockCloudContainer.stop();
            }
        }
    }

}
