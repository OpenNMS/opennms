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
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apache.camel.util.FileUtil;
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.jcraft.jsch.Channel;

public class OpenNMSCloudPluginIT {

    private static final Logger LOG = LoggerFactory.getLogger(OpenNMSCloudPluginIT.class);

    private static final String pathToFile = System.getProperty("user.home") + "/.m2/repository/org/opennms/plugins/cloud/assembly/org.opennms.plugins.cloud.assembly.kar/1.1.0-SNAPSHOT/";
    private static final String pathToCloudMock = System.getProperty("user.home") + "/.m2/repository/org/opennms/plugins/cloud/it-test/1.1.0-SNAPSHOT/it-test-1.1.0-SNAPSHOT-jar-with-dependencies.jar";

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
            }).withCopyFileToContainer(MountableFile.forHostPath(pathToCloudMock), "/usr/share/opennms/mvn/it-test-1.1.0-SNAPSHOT-jar-with-dependencies.jar")
            .withCommand("-c", String.format("java -cp %s %s", "/usr/share/opennms/mvn/it-test-1.1.0-SNAPSHOT-jar-with-dependencies.jar",
                    "org.opennms.plugins.cloud.ittest.MockCloudMain")).withExposedPorts(9003)
            .withNetwork(stack.opennms().getNetwork())
            .withNetworkAliases("cloudMock");

    // ${project.build.directory}/cloud-mock-with-dependencies/org.opennms.plugins.cloud-mock-with-dependencies.jar

    @Test
    public void install_cloud_plugin_in_opennms_must_be_successful() throws Exception {
        // Given
        mockCloudContainer.start();
        stack.opennms().withFileSystemBind(pathToFile, "/usr/share/opennms/mvn", BindMode.READ_ONLY);
        stack.opennms().copyFileToContainer(MountableFile.forHostPath(pathToFile), "/usr/share/opennms/mvn");

        String out;
        InetSocketAddress karafSshAddress = stack.opennms().getSshAddress();
        String config = createConfig();

        // When
        try (final SshClient sshClient = new SshClient(karafSshAddress, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();
            pipe.println("kar:install file:/usr/share/opennms/mvn/org.opennms.plugins.cloud.assembly.kar-1.1.0-SNAPSHOT.kar");
            pipe.println("feature:install opennms-plugin-cloud-core");
            pipe.println("feature:list | grep opennms-plugin-cloud-core");
            pipe.println(config);
            pipe.println("opennms-cloud:init myKey");
            pipe.println("logout");
            out = readLatestPartOfChannelOutput(sshClient.getChannel());
            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());
        }

        // Then
        assertTrue(out.contains("Started"));
        assertTrue(out.contains("Initialization of cloud plugin in OPENNMS was successful."));
    }


    @Test
    public void install_cloud_plugin_in_sentinel_must_be_successful() throws Exception {
        // Given
        stack.sentinel().withFileSystemBind(pathToFile, "/usr/share/opennms/mvn", BindMode.READ_ONLY);
        stack.sentinel().copyFileToContainer(MountableFile.forHostPath(pathToFile), "/usr/share/opennms/mvn");

        String out;
        InetSocketAddress karafSshAddress = stack.sentinel().getSshAddress();
        String config = createConfig();

        // When
        try (final SshClient sshClient = new SshClient(karafSshAddress, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();
            pipe.println("kar:install file:/usr/share/opennms/mvn/1.1.0-SNAPSHOT/org.opennms.plugins.cloud.assembly.kar-1.1.0-SNAPSHOT.kar");
            pipe.println("feature:install opennms-plugin-cloud-sentinel");
            pipe.println("feature:list | grep opennms-plugin-cloud-sentinel");
            pipe.println(config);
            pipe.println("opennms-cloud:init myKey");
            pipe.println("logout");
            out = readLatestPartOfChannelOutput(sshClient.getChannel());
            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());
        }

        // Then
        assertTrue(out.contains("Started"));
        assertTrue(out.contains("Initialization of cloud plugin in SENTINEL was successful."));
    }


    private static String readLatestPartOfChannelOutput(Channel channel) {
        StringBuilder res = new StringBuilder();
        byte[] buffer = new byte[1024];
        try {
            InputStream in = channel.getInputStream();
            String line = EMPTY;
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(buffer, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    line = new String(buffer, 0, i);
                    res.append(line);
                }
                if (line.contains("logout") || channel.isClosed()) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            LOG.error("Error while reading channel output: ", e);
        }
        return res.toString();
    }

    private String createConfig() {
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

    public static String classpathFileToString(final String fileInClasspath) {
        try (InputStream in = FileUtil.class.getResourceAsStream(fileInClasspath)) {
            Objects.requireNonNull(in, format("could not  read %s from classpath", fileInClasspath));
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
