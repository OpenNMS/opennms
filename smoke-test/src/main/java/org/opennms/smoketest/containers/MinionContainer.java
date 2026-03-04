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

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.containsString;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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
                .withEnv("JAVA_OPTS", "-Xms2g -Xmx2g -Djava.security.egd=file:/dev/./urandom")
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
            withEnv("MINION_LOCATION", profile.getLocation())
                    .withEnv("MINION_ID", profile.getId())
                    .withEnv("OPENNMS_BROKER_URL", "failover:tcp://" + OpenNMSContainer.ALIAS + ":61616");

            if (IpcStrategy.KAFKA.equals(model.getIpcStrategy())) {
                withEnv("KAFKA_IPC_BOOTSTRAP_SERVERS", OpenNMSContainer.KAFKA_ALIAS + ":9092")
                        .withEnv("KAFKA_IPC_COMPRESSION_TYPE", model.getKafkaCompressionStrategy().getCodec());
            }
        }

        if (profile.isJvmDebuggingEnabled()) {
            withEnv("KARAF_DEBUG", "true");
            withEnv("JAVA_DEBUG_PORT", "" + MINION_DEBUG_PORT);
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

        Path etc = home.resolve("etc");
        Files.createDirectories(etc);

        writeTrapdConfig(etc);
        writeKarafShellConfig(etc);
        writeTelemetryListenerConfigs(etc);

        if (!profile.isLegacy()) {
            writeMinionControllerConfig(etc, profile);

            if (IpcStrategy.KAFKA.equals(model.getIpcStrategy())) {
                writeKafkaConfigs(etc);
            } else if (IpcStrategy.GRPC.equals(model.getIpcStrategy())) {
                OverlayUtils.writeProps(etc.resolve("org.opennms.core.ipc.grpc.client.cfg"),
                    Map.of(
                        "host", OpenNMSContainer.ALIAS,
                        "port", "8990"
                    ));
            }
        }
    }

    private void writeMinionControllerConfig(Path etc, MinionProfile profile) {
        OverlayUtils.writeProps(etc.resolve("org.opennms.minion.controller.cfg"),
            Map.of(
                "location", profile.getLocation(),
                "id", profile.getId(),
                "broker-url", "failover:tcp://" + OpenNMSContainer.ALIAS + ":61616",
                "http-url", "http://" + OpenNMSContainer.ALIAS + ":8980/opennms"
            ));
    }

    private void writeTrapdConfig(Path etc) {
        OverlayUtils.writeProps(etc.resolve("org.opennms.netmgt.trapd.cfg"),
            Map.of(
                "trapd.listen.interface", "0.0.0.0",
                "trapd.useAddressFromVarbind", "true"
            ));
    }

    private void writeKarafShellConfig(Path etc) {
        OverlayUtils.writeProps(etc.resolve("org.apache.karaf.shell.cfg"),
            Map.of(
                "sshHost", "0.0.0.0"
            ));
    }

    private void writeTelemetryListenerConfigs(Path etc) {
        writeSinglePortFlowsConfig(etc);
        writeFlowListenerConfig(
            etc.resolve("org.opennms.features.telemetry.listeners-JTI-Listener.cfg"),
            "JTI-Listener",
            MINION_TELEMETRY_JTI_PORT,
            "JTI"
        );
        writeFlowListenerConfig(
            etc.resolve("org.opennms.features.telemetry.listeners-NXOS-Listener.cfg"),
            "NXOS-Listener",
            MINION_TELEMETRY_NXOS_PORT,
            "NXOS"
        );
    }

    private void writeSinglePortFlowsConfig(Path etc) {
        Map<String, String> props = new LinkedHashMap<>();
        props.put("name", "Flows");
        props.put("class-name", "org.opennms.netmgt.telemetry.listeners.UdpListener");
        props.put("parameters.port", String.valueOf(MINION_TELEMETRY_FLOW_PORT));
        props.put("parsers.0.name", "Netflow-5");
        props.put("parsers.0.class-name", "org.opennms.netmgt.telemetry.protocols.netflow.parser.Netflow5UdpParser");
        props.put("parsers.1.name", "Netflow-9");
        props.put("parsers.1.class-name", "org.opennms.netmgt.telemetry.protocols.netflow.parser.Netflow9UdpParser");
        props.put("parsers.2.name", "IPFIX");
        props.put("parsers.2.class-name", "org.opennms.netmgt.telemetry.protocols.netflow.parser.IpfixUdpParser");
        props.put("parsers.3.name", "SFlow");
        props.put("parsers.3.class-name", "org.opennms.netmgt.telemetry.protocols.sflow.parser.SFlowUdpParser");
        OverlayUtils.writeProps(etc.resolve("org.opennms.features.telemetry.listeners-udp-single-port-flows.cfg"), props);
    }

    private void writeFlowListenerConfig(Path dest, String name, int port, String parserName) {
        Map<String, String> props = new LinkedHashMap<>();
        props.put("name", name);
        props.put("class-name", "org.opennms.netmgt.telemetry.listeners.UdpListener");
        props.put("parameters.port", String.valueOf(port));
        props.put("parsers.0.name", parserName);
        props.put("parsers.0.class-name", "org.opennms.netmgt.telemetry.protocols.common.parser.ForwardParser");
        OverlayUtils.writeProps(dest, props);
    }

    private void writeKafkaConfigs(Path etc) {
        String bootstrapServers = OpenNMSContainer.KAFKA_ALIAS + ":9092";
        String compressionType = model.getKafkaCompressionStrategy().getCodec();

        Map<String, String> commonProps = new LinkedHashMap<>();
        commonProps.put("bootstrap.servers", bootstrapServers);
        commonProps.put("compression.type", compressionType);
        OverlayUtils.writeProps(etc.resolve("org.opennms.core.ipc.kafka.cfg"), commonProps);

        Map<String, String> rpcProps = new LinkedHashMap<>();
        rpcProps.put("bootstrap.servers", bootstrapServers);
        rpcProps.put("acks", "1");
        rpcProps.put("compression.type", compressionType);
        OverlayUtils.writeProps(etc.resolve("org.opennms.core.ipc.rpc.kafka.cfg"), rpcProps);

        Map<String, String> sinkProps = new LinkedHashMap<>();
        sinkProps.put("bootstrap.servers", bootstrapServers);
        sinkProps.put("acks", "1");
        sinkProps.put("compression.type", compressionType);
        OverlayUtils.writeProps(etc.resolve("org.opennms.core.ipc.sink.kafka.cfg"), sinkProps);
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
