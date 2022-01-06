/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import static java.nio.file.Files.createTempDirectory;
import static org.opennms.smoketest.utils.KarafShellUtils.awaitHealthCheckSucceeded;
import static org.opennms.smoketest.utils.OverlayUtils.jsonMapper;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.opennms.smoketest.stacks.IpcStrategy;
import org.opennms.smoketest.stacks.MinionProfile;
import org.opennms.smoketest.stacks.NetworkProtocol;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.DevDebugUtils;
import org.opennms.smoketest.utils.OverlayUtils;
import org.opennms.smoketest.utils.SshClient;
import org.opennms.smoketest.utils.TestContainerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.SelinuxContext;
import org.testcontainers.lifecycle.TestDescription;
import org.testcontainers.lifecycle.TestLifecycleAware;
import org.testcontainers.utility.MountableFile;

import com.github.dockerjava.api.command.CreateContainerCmd;

public class MinionContainer extends GenericContainer implements KarafContainer, TestLifecycleAware {
    private static final Logger LOG = LoggerFactory.getLogger(MinionContainer.class);
    private static final int MINION_DEBUG_PORT = 5005;
    private static final int MINION_SYSLOG_PORT = 1514;
    private static final int MINION_SSH_PORT = 8201;
    private static final int MINION_SNMP_TRAP_PORT = 1162;
    private static final int MINION_TELEMETRY_FLOW_PORT = 50000;
    private static final int MINION_TELEMETRY_IPFIX_TCP_PORT = 4730;
    private static final int MINION_TELEMETRY_JTI_PORT = 50001;
    private static final int MINION_TELEMETRY_NXOS_PORT = 50002;
    private static final int MINION_JETTY_PORT = 8181;

    static final String ALIAS = "minion";

    private final StackModel model;

    private final String id;
    private final String location;
    private final GenericContainer container;

    private MinionContainer(final StackModel model, final String id, final String location) {
        super("minion");
        this.model = Objects.requireNonNull(model);
        this.id = Objects.requireNonNull(id);
        this.location = Objects.requireNonNull(location);

        this.container = withExposedPorts(MINION_DEBUG_PORT, MINION_SSH_PORT, MINION_SYSLOG_PORT, MINION_SNMP_TRAP_PORT, MINION_TELEMETRY_FLOW_PORT, MINION_TELEMETRY_IPFIX_TCP_PORT, MINION_TELEMETRY_JTI_PORT, MINION_TELEMETRY_NXOS_PORT, MINION_JETTY_PORT)
                .withCreateContainerCmdModifier(cmd -> {
                    final CreateContainerCmd createCmd = (CreateContainerCmd)cmd;
                    TestContainerUtils.setGlobalMemAndCpuLimits(createCmd);
                    TestContainerUtils.exposePortsAsUdp(createCmd, MINION_SNMP_TRAP_PORT, MINION_TELEMETRY_FLOW_PORT, MINION_TELEMETRY_JTI_PORT, MINION_TELEMETRY_NXOS_PORT);
                })
                .withEnv("OPENNMS_HTTP_USER", "admin")
                .withEnv("OPENNMS_HTTP_PASS", "admin")
                .withEnv("OPENNMS_BROKER_USER", "admin")
                .withEnv("OPENNMS_BROKER_PASS", "admin")
                .withEnv("JAVA_OPTS", "-Xms512m -Xmx512m -Djava.security.egd=file:/dev/./urandom")
                .withNetwork(Network.SHARED)
                .withNetworkAliases(ALIAS)
                .withCommand("-c")
                .waitingFor(new WaitForMinion(this));

        // Help make development/debugging easier
        DevDebugUtils.setupMavenRepoBind(this, "/opt/minion/.m2");
    }

    public MinionContainer(final StackModel model, final MinionProfile profile) {
        this(model, profile.getId(), profile.getLocation());

        container.addFileSystemBind(writeMinionConfig(profile).toString(),
                "/opt/minion/minion-config.yaml", BindMode.READ_ONLY, SelinuxContext.SINGLE);

        if (profile.isJvmDebuggingEnabled()) {
            withEnv("KARAF_DEBUG", "true");
            withEnv("JAVA_DEBUG_PORT", "*:" + MINION_DEBUG_PORT);
        }
    }

    public MinionContainer(final StackModel model, final Map<String, String> configuration) {
        this(model, configuration.get("MINION_ID"), configuration.get("MINION_LOCATION"));

        for(final Map.Entry<String, String> entry : configuration.entrySet()) {
            container.addEnv(entry.getKey(), entry.getValue());
        }
    }

    private Path writeMinionConfig(MinionProfile profile) {
        try {
            final Path minionConfig = createTempDirectory(ALIAS).toAbsolutePath().resolve("minion-config.yaml");
            writeMinionConfigYaml(minionConfig, profile);
            return minionConfig;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void writeMinionConfigYaml(Path minionConfigYaml, MinionProfile profile) throws IOException {
        // Copy over the default configuration from the class-path
        FileUtils.copyFile(new File(MountableFile.forClasspathResource("minion-config/minion-config.yaml").getFilesystemPath()), minionConfigYaml.toFile());
        
        // Allow other users to read the file
        OverlayUtils.setOverlayPermissions(minionConfigYaml);

        String config = "{\n" +
                "\t\"location\": \"" + profile.getLocation() + "\",\n" +
                "\t\"id\": \"" + profile.getId() + "\",\n" +
                "\t\"broker-url\": \"failover:tcp://" + OpenNMSContainer.ALIAS + ":61616\",\n" +
                "\t\"http-url\": \"http://" + OpenNMSContainer.ALIAS + ":8980/opennms\"\n" +
                "}";
        OverlayUtils.writeYaml(minionConfigYaml, jsonMapper.readValue(config, Map.class));
        
        if (IpcStrategy.KAFKA.equals(model.getIpcStrategy())) {
            String kafkaIpc = "{\n" +
                    "\t\"ipc\": {\n" +
                    "\t\t\"kafka\": {\n" +
                    "\t\t\t\"bootstrap.servers\": \""+ OpenNMSContainer.KAFKA_ALIAS +":9092\",\n" +
                    "\t\t\t\"acks\": 1,\n" +
                    "\t\t\t\"compression.type\": \""+ model.getKafkaCompressionStrategy().getCodec() +"\"\n" +
                    "\t\t}\n" +
                    "\t}\n" +
                    "}";
            OverlayUtils.writeYaml(minionConfigYaml, jsonMapper.readValue(kafkaIpc, Map.class));
        } else if (IpcStrategy.GRPC.equals(model.getIpcStrategy())) {
            String grpc = "{\n" +
                    "\t\"ipc\": {\n" +
                    "\t\t\"grpc\": {\n" +
                    "\t\t\t\"host\": \"" + OpenNMSContainer.ALIAS + "\",\n" +
                    "\t\t\t\"port\": 8990\n" +
                    "\t\t}\n" +
                    "\t}\n" +
                    "}";
            OverlayUtils.writeYaml(minionConfigYaml, jsonMapper.readValue(grpc, Map.class));
        }
    }

    public InetSocketAddress getSyslogAddress() {
        return new InetSocketAddress(getContainerIpAddress(), TestContainerUtils.getMappedUdpPort(this, MINION_SYSLOG_PORT));
    }

    public InetSocketAddress getSshAddress() {
        return new InetSocketAddress(getContainerIpAddress(), getMappedPort(MINION_SSH_PORT));
    }

    public SshClient ssh() {
        return new SshClient(getSshAddress(), OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD);
    }

    public URL getWebUrl() {
        try {
            return new URL(String.format("http://%s:%d/", getContainerIpAddress(), getMappedPort(MINION_JETTY_PORT)));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getWebPort() {
        return MINION_JETTY_PORT;
    }


    public String getLocation() {
        return this.location;
    }

    public InetSocketAddress getNetworkProtocolAddress(NetworkProtocol protocol) {
        int mappedPort = -1;
        switch (protocol) {
            case SNMP:
                mappedPort = TestContainerUtils.getMappedUdpPort(this, MINION_SNMP_TRAP_PORT);
                break;
            case FLOWS:
                mappedPort = TestContainerUtils.getMappedUdpPort(this, MINION_TELEMETRY_FLOW_PORT);
                break;
            case JTI:
                mappedPort = TestContainerUtils.getMappedUdpPort(this, MINION_TELEMETRY_JTI_PORT);
                break;
            case NXOS:
                mappedPort = TestContainerUtils.getMappedUdpPort(this, MINION_TELEMETRY_NXOS_PORT);
                break;
            case IPFIX_TCP:
                mappedPort = getMappedPort(MINION_TELEMETRY_IPFIX_TCP_PORT);
                break;
        }
        return new InetSocketAddress(getContainerIpAddress(), mappedPort);
    }

    private static class WaitForMinion extends org.testcontainers.containers.wait.strategy.AbstractWaitStrategy {
        private final MinionContainer container;

        public WaitForMinion(MinionContainer container) {
            this.container = Objects.requireNonNull(container);
        }

        @Override
        protected void waitUntilReady() {
            LOG.info("Waiting for Minion health check...");
            final InetSocketAddress sshAddr = container.getSshAddress();
            awaitHealthCheckSucceeded(sshAddr, 5, "Minion");
        }
    }

    @Override
    public void afterTest(TestDescription description, Optional<Throwable> throwable) {
        retainLogsfNeeded(description.getFilesystemFriendlyName(), !throwable.isPresent());
    }

    private void retainLogsfNeeded(String prefix, boolean succeeded) {
        LOG.info("Triggering thread dump...");
        DevDebugUtils.triggerThreadDump(this);
        LOG.info("Gathering logs...");
        copyLogs(this, prefix);
    }

    private static void copyLogs(MinionContainer container, String prefix) {
        // List of known log files we expect to find in the container
        final List<String> logFiles = Arrays.asList("karaf.log");
        DevDebugUtils.copyLogs(container,
                // dest
                Paths.get("target", "logs", prefix, "minion"),
                // source folder
                Paths.get("/opt", "minion", "data", "log"),
                // log files
                logFiles);
    }

    public String getId() {
        return this.id;
    }
}
