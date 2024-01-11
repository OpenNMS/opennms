/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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
import static org.hamcrest.CoreMatchers.containsString;
import static org.opennms.smoketest.utils.OverlayUtils.jsonMapper;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileUtils;
import org.opennms.smoketest.stacks.IpcStrategy;
import org.opennms.smoketest.stacks.MinionProfile;
import org.opennms.smoketest.stacks.NetworkProtocol;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.DevDebugUtils;
import org.opennms.smoketest.utils.OverlayUtils;
import org.opennms.smoketest.utils.RestHealthClient;
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

import com.google.common.base.Strings;

public class MinionContainer extends GenericContainer<MinionContainer> implements KarafContainer<MinionContainer>, TestLifecycleAware {
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
    static final String IMAGE = "opennms/minion";

    private final StackModel model;

    private final String id;
    private final String location;
    private final MinionProfile profile;
    private final Path overlay;

    public MinionContainer(final StackModel model, final MinionProfile profile) {
        super(IMAGE);
        this.model = Objects.requireNonNull(model);
        this.profile = Objects.requireNonNull(profile);
        this.id = Objects.requireNonNull(profile.getId());
        this.location = Objects.requireNonNull(profile.getLocation());

        this.overlay = writeOverlay();

        Integer[] tcpPorts = {
                MINION_DEBUG_PORT,
                MINION_SSH_PORT,
                MINION_TELEMETRY_FLOW_PORT,
                MINION_TELEMETRY_IPFIX_TCP_PORT,
                MINION_JETTY_PORT,
        };
        int[] udpPorts = {
                MINION_SYSLOG_PORT,
                MINION_SNMP_TRAP_PORT,
                MINION_TELEMETRY_FLOW_PORT,
                MINION_TELEMETRY_JTI_PORT,
                MINION_TELEMETRY_NXOS_PORT,
        };

        withExposedPorts(tcpPorts)
                .withCreateContainerCmdModifier(createCmd -> {
                    TestContainerUtils.setGlobalMemAndCpuLimits(createCmd);
                    TestContainerUtils.exposePortsAsUdp(createCmd, udpPorts);
                })
                .withEnv("OPENNMS_HTTP_USER", "admin")
                .withEnv("OPENNMS_HTTP_PASS", "admin")
                .withEnv("OPENNMS_BROKER_USER", "admin")
                .withEnv("OPENNMS_BROKER_PASS", "admin")
                .withEnv("JACOCO_AGENT_ENABLED", "1")
                .withEnv("JAVA_OPTS", "-Xms1g -Xmx1g -Djava.security.egd=file:/dev/./urandom")
                .withNetwork(Network.SHARED)
                .withNetworkAliases(ALIAS)
                .withCommand("-c")
                .waitingFor(Objects.requireNonNull(profile.getWaitStrategy()).apply(this))
                .addFileSystemBind(overlay.toString(),
                "/opt/minion-etc-overlay", BindMode.READ_ONLY, SelinuxContext.SINGLE);

        // Help make development/debugging easier
        DevDebugUtils.setupMavenRepoBind(this, "/opt/minion/.m2");

        if (profile.isLegacy()) {
            for (final Map.Entry<String, String> entry : profile.getLegacyConfiguration().entrySet()) {
                addEnv(entry.getKey(), entry.getValue());
            }
        } else {
            addFileSystemBind(writeMinionConfig(profile).toString(),
                    "/opt/minion/minion-config.yaml", BindMode.READ_ONLY, SelinuxContext.SINGLE);
        }

        if (profile.isJvmDebuggingEnabled()) {
            withEnv("KARAF_DEBUG", "true");
            withEnv("JAVA_DEBUG_PORT", "" + MINION_DEBUG_PORT);
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
                "\t\"broker-url\": \"failover:tcp://" + OpenNMSContainer.ALIAS + ":61616\"\n" +
                "}";
        OverlayUtils.writeYaml(minionConfigYaml, jsonMapper.readValue(config, Map.class));

        if (!Strings.isNullOrEmpty(profile.getDominionGrpcScvClientSecret())) {
            final String scvConfig = "{\"scv\": {\"provider\": \"dominion\"}}";
            OverlayUtils.writeYaml(minionConfigYaml, jsonMapper.readValue(scvConfig, Map.class));

            final String gprcConfig = "{\"dominion\": { \"grpc\": { \"client-secret\":\"" + profile.getDominionGrpcScvClientSecret() + "\"}}}";
            OverlayUtils.writeYaml(minionConfigYaml, jsonMapper.readValue(gprcConfig, Map.class));
        }

        if (IpcStrategy.KAFKA.equals(model.getIpcStrategy())) {
            String kafkaIpc = "{\n" +
                    "\t\"ipc\": {\n" +
                    "\t\t\"kafka\": {\n" +
                    "\t\t\t\"bootstrap.servers\": \""+ OpenNMSContainer.KAFKA_ALIAS +":9092\",\n" +
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

        if (model.isJaegerEnabled()) {
            String jaeger = "{\n" +
                    "\t\"system\": {\n" +
                    "\t\t\"properties\": {\n" +
                    "\t\t\t\"JAEGER_ENDPOINT\": \"" + JaegerContainer.getThriftHttpURL() + "\"\n" +
                    "\t\t}\n" +
                    "\t}\n" +
                    "}";
            OverlayUtils.writeYaml(minionConfigYaml, jsonMapper.readValue(jaeger, Map.class));
        }
    }

    private Path writeOverlay() {
        try {
            final Path home = Files.createTempDirectory(ALIAS).toAbsolutePath();
            writeOverlay(home, profile);
            return home;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeOverlay(Path home, MinionProfile profile) throws IOException {
        // Allow other users to read the folder
        OverlayUtils.setOverlayPermissions(home);

        // Copy the files from the profile *first*
        // If this test class writes something, we expect it to be there
        OverlayUtils.copyFiles(profile.getFiles(), home);
    }

    public InetSocketAddress getSyslogAddress() {
        return new InetSocketAddress(getContainerIpAddress(), TestContainerUtils.getMappedUdpPort(this, MINION_SYSLOG_PORT));
    }

    @Override
    public InetSocketAddress getSshAddress() {
        return new InetSocketAddress(getContainerIpAddress(), getMappedPort(MINION_SSH_PORT));
    }

    @Override
    public SshClient ssh() {
        return new SshClient(getSshAddress(), OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD);
    }


    @Override
    public Path getKarafHomeDirectory() {
        return Path.of("/opt/minion");
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

    public static class WaitForMinion extends org.testcontainers.containers.wait.strategy.AbstractWaitStrategy {
        private final MinionContainer container;

        public WaitForMinion(MinionContainer container) {
            this.container = Objects.requireNonNull(container);
        }

        @Override
        protected void waitUntilReady() {
            LOG.info("Waiting for Minion health check...");
            RestHealthClient client = new RestHealthClient(container.getWebUrl(), Optional.of(ALIAS));
            await("waiting for good health check probe")
                    .atMost(5, MINUTES)
                    .pollInterval(10, SECONDS)
                    .failFast("container is no longer running", () -> !container.isRunning())
                    .ignoreExceptionsMatching((e) -> { return e.getCause() != null && e.getCause() instanceof SocketException; })
                    .until(client::getProbeHealthResponse, containsString(client.getProbeSuccessMessage()));
            LOG.info("Health check passed.");

            container.assertNoKarafDestroy(Paths.get("/opt", ALIAS, "data", "log", "karaf.log"));
        }
    }

    @Override
    public void afterTest(TestDescription description, Optional<Throwable> throwable) {
        // not working yet in karaf-started JVMs
        // getCoverage(description.getFilesystemFriendlyName());
        retainLogsfNeeded(description.getFilesystemFriendlyName(), !throwable.isPresent());
    }

    private void retainLogsfNeeded(String prefix, boolean succeeded) {
        Path targetLogFolder = Paths.get("target", "logs", prefix, "minion");
        DevDebugUtils.clearLogs(targetLogFolder);

        AtomicReference<Path> threadDump = new AtomicReference<>();
        await("calling gatherThreadDump")
                .atMost(Duration.ofSeconds(120))
                .untilAsserted(
                        () -> { threadDump.set(DevDebugUtils.gatherThreadDump(this, targetLogFolder, null)); }
                );

        LOG.info("Gathering logs...");
        // List of known log files we expect to find in the container
        final List<String> logFiles = Arrays.asList("karaf.log");
        DevDebugUtils.copyLogs(this,
                // dest
                targetLogFolder,
                // source folder
                Paths.get("/opt", "minion", "data", "log"),
                // log files
                logFiles);

        LOG.info("Log directory: {}", targetLogFolder.toUri());
        LOG.info("Console log: {}", targetLogFolder.resolve(DevDebugUtils.CONTAINER_STDOUT_STDERR).toUri());
        if (threadDump.get() != null) {
            LOG.info("Thread dump: {}", threadDump.get().toUri());
        }
    }

    public String getId() {
        return this.id;
    }
}
