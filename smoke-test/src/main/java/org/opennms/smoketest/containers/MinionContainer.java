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
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.opennms.smoketest.utils.OverlayUtils.writeFeaturesBoot;
import static org.opennms.smoketest.utils.OverlayUtils.writeProps;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileUtils;
import org.awaitility.core.ConditionTimeoutException;
import org.opennms.smoketest.stacks.IpcStrategy;
import org.opennms.smoketest.stacks.MinionProfile;
import org.opennms.smoketest.stacks.NetworkProtocol;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.DevDebugUtils;
import org.opennms.smoketest.utils.KarafShellUtils;
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
import com.google.common.collect.ImmutableMap;

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

    static final String ALIAS = "minion";

    private final StackModel model;
    private final MinionProfile profile;
    private final Path overlay;

    public MinionContainer(StackModel model, MinionProfile profile) {
        super("minion");
        this.model = Objects.requireNonNull(model);
        this.profile = Objects.requireNonNull(profile);
        this.overlay = writeOverlay();
        withExposedPorts(MINION_DEBUG_PORT, MINION_SSH_PORT, MINION_SYSLOG_PORT, MINION_SNMP_TRAP_PORT, MINION_TELEMETRY_FLOW_PORT, MINION_TELEMETRY_IPFIX_TCP_PORT, MINION_TELEMETRY_JTI_PORT, MINION_TELEMETRY_NXOS_PORT)
                .withCreateContainerCmdModifier(cmd -> {
                    final CreateContainerCmd createCmd = (CreateContainerCmd)cmd;
                    TestContainerUtils.setGlobalMemAndCpuLimits(createCmd);
                    TestContainerUtils.exposePortsAsUdp(createCmd, MINION_SNMP_TRAP_PORT, MINION_TELEMETRY_FLOW_PORT, MINION_TELEMETRY_JTI_PORT, MINION_TELEMETRY_NXOS_PORT);
                    if (profile.isIcmpSupportEnabled()) {
                        // Run as root when ICMP is enabled
                        createCmd.withUser("root");
                    }
                })
                .withEnv("MINION_LOCATION", profile.getLocation())
                .withEnv("MINION_ID", profile.getId())
                .withEnv("OPENNMS_BROKER_URL", "failover:tcp://" + OpenNMSContainer.ALIAS + ":61616")
                .withEnv("OPENNMS_HTTP_URL", "http://" + OpenNMSContainer.ALIAS + ":8980/opennms")
                .withEnv("OPENNMS_HTTP_USER", "admin")
                .withEnv("OPENNMS_HTTP_PASS", "admin")
                .withEnv("OPENNMS_BROKER_USER", "admin")
                .withEnv("OPENNMS_BROKER_PASS", "admin")
                .withEnv("JAVA_OPTS", "-Xms512m -Xmx512m -Djava.security.egd=file:/dev/./urandom")
                .withNetwork(Network.SHARED)
                .withNetworkAliases(ALIAS)
                .withCommand("-c")
                .waitingFor(new WaitForMinion(this))
                .addFileSystemBind(overlay.toString(),
                        "/opt/minion-etc-overlay", BindMode.READ_ONLY, SelinuxContext.SINGLE);

        if (profile.isJvmDebuggingEnabled()) {
            withEnv("KARAF_DEBUG", "true");
            withEnv("JAVA_DEBUG_PORT", "*:" + MINION_DEBUG_PORT);
        }

        // Help make development/debugging easier
        DevDebugUtils.setupMavenRepoBind(this, "/opt/minion/.m2");
    }

    private Path writeOverlay() {
        try {
            final Path home = createTempDirectory(ALIAS).toAbsolutePath();
            writeOverlay(home);
            return home;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeOverlay(Path etc) throws IOException {
        // Allow other users to read the folder
        OverlayUtils.setOverlayPermissions(etc);

        // Copy over the fixed configuration from the class-path
        FileUtils.copyDirectory(new File(MountableFile.forClasspathResource("minion-overlay").getFilesystemPath()), etc.toFile());

        writeProps(etc.resolve("org.opennms.core.ipc.rpc.kafka.cfg"),
                ImmutableMap.<String,String>builder()
                        .put("bootstrap.servers", OpenNMSContainer.KAFKA_ALIAS + ":9092")
                        .put("acks", "1")
                        .build());

        writeProps(etc.resolve("org.opennms.core.ipc.sink.kafka.cfg"),
                ImmutableMap.<String,String>builder()
                        .put("bootstrap.servers", OpenNMSContainer.KAFKA_ALIAS + ":9092")
                        .put("acks", "1")
                        .build());

        // Features boot
        Path bootD = etc.resolve("featuresBoot.d");
        Files.createDirectories(bootD);
        writeFeaturesBoot(bootD.resolve("stest.boot"), getFeaturesOnBoot());
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

    public List<String> getFeaturesOnBoot() {
        final List<String> featuresOnBoot = new ArrayList<>();
        if (IpcStrategy.KAFKA.equals(model.getIpcStrategy())) {
            // RPC
            featuresOnBoot.addAll(Arrays.asList("!opennms-core-ipc-rpc-jms", "opennms-core-ipc-rpc-kafka"));
            // Sink
            featuresOnBoot.addAll(Arrays.asList("!opennms-core-ipc-sink-camel", "opennms-core-ipc-sink-kafka"));
            // Disable JMS
            featuresOnBoot.add("!minion-jms");
        }
        return featuresOnBoot;
    }

    public String getLocation() {
        return profile.getLocation();
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
            final long timeoutMins = 5;
            final AtomicReference<String> lastOutput = new AtomicReference<>();
            try {
                await().atMost(timeoutMins, MINUTES).pollInterval(5, SECONDS)
                        .until(() -> KarafShellUtils.testHealthCheck(sshAddr, lastOutput));
            } catch(ConditionTimeoutException e) {
                LOG.error("Minion did not finish starting after {} minutes. Last output: {}", lastOutput);
                throw new RuntimeException(e);
            }
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
        return profile.getId();
    }
}
