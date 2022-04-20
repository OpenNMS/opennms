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

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.opennms.smoketest.utils.KarafShellUtils.awaitHealthCheckSucceeded;
import static org.opennms.smoketest.utils.OverlayUtils.writeFeaturesBoot;
import static org.opennms.smoketest.utils.OverlayUtils.writeProps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.awaitility.core.ConditionTimeoutException;
import org.opennms.smoketest.stacks.InternetProtocol;
import org.opennms.smoketest.stacks.IpcStrategy;
import org.opennms.smoketest.stacks.NetworkProtocol;
import org.opennms.smoketest.stacks.OpenNMSProfile;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.stacks.TimeSeriesStrategy;
import org.opennms.smoketest.utils.DevDebugUtils;
import org.opennms.smoketest.utils.OverlayUtils;
import org.opennms.smoketest.utils.RestClient;
import org.opennms.smoketest.utils.SshClient;
import org.opennms.smoketest.utils.TestContainerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.SelinuxContext;
import org.testcontainers.lifecycle.TestDescription;
import org.testcontainers.lifecycle.TestLifecycleAware;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.google.common.collect.ImmutableMap;

/**
 * This class encapsulates all the logic required to start an
 * OpenNMS container and interface with the services
 * it provides.
 *
 * @author jwhite
 */
public class OpenNMSContainer extends GenericContainer implements KarafContainer, TestLifecycleAware {
    public static final String ALIAS = "opennms";
    public static final String DB_ALIAS = "db";
    public static final String KAFKA_ALIAS = "kafka";
    public static final String ELASTIC_ALIAS = "elastic";
    public static final String CASSANDRA_ALIAS = "cassandra";

    public static final String ADMIN_USER = "admin";
    public static final String ADMIN_PASSWORD = "admin";

    private static final Logger LOG = LoggerFactory.getLogger(OpenNMSContainer.class);

    public static final int OPENNMS_WEB_PORT = 8980;
    private static final int OPENNMS_SSH_PORT = 8101;
    private static final int OPENNMS_SYSLOG_PORT = 10514;
    private static final int OPENNMS_SNMP_PORT = 1162;
    private static final int OPENNMS_TELEMETRY_FLOW_PORT = 50000;
    private static final int OPENNMS_TELEMETRY_IPFIX_TCP_PORT = 4730;
    private static final int OPENNMS_TELEMETRY_JTI_PORT = 50001;
    private static final int OPENNMS_TELEMETRY_NXOS_PORT = 50002;
    private static final int OPENNMS_DEBUG_PORT = 8001;
    private static final int OPENNMS_GRPC_PORT = 8990;
    private static final int OPENNMS_BMP_PORT = 11019;
    private static final int OPENNMS_TFTP_PORT = 6969;

    private static final Map<NetworkProtocol, Integer> networkProtocolMap = ImmutableMap.<NetworkProtocol, Integer>builder()
            .put(NetworkProtocol.SSH, OPENNMS_SSH_PORT)
            .put(NetworkProtocol.HTTP, OPENNMS_WEB_PORT)
            .put(NetworkProtocol.JDWP, OPENNMS_DEBUG_PORT)
            .put(NetworkProtocol.SNMP, OPENNMS_SNMP_PORT)
            .put(NetworkProtocol.SYSLOG, OPENNMS_SYSLOG_PORT)
            .put(NetworkProtocol.FLOWS, OPENNMS_TELEMETRY_FLOW_PORT)
            .put(NetworkProtocol.IPFIX_TCP, OPENNMS_TELEMETRY_IPFIX_TCP_PORT)
            .put(NetworkProtocol.JTI, OPENNMS_TELEMETRY_JTI_PORT)
            .put(NetworkProtocol.NXOS, OPENNMS_TELEMETRY_NXOS_PORT)
            .put(NetworkProtocol.GRPC, OPENNMS_GRPC_PORT)
            .put(NetworkProtocol.BMP, OPENNMS_BMP_PORT)
            .put(NetworkProtocol.TFTP, OPENNMS_TFTP_PORT)
            .build();

    private final StackModel model;
    private final OpenNMSProfile profile;
    private final Path overlay;

    public OpenNMSContainer(StackModel model, OpenNMSProfile profile) {
        super("horizon");
        this.model = Objects.requireNonNull(model);
        this.profile = Objects.requireNonNull(profile);
        this.overlay = writeOverlay();

        String containerCommand = "-s";
        if (TimeSeriesStrategy.NEWTS.equals(model.getTimeSeriesStrategy())) {
            this.withEnv("OPENNMS_TIMESERIES_STRATEGY", model.getTimeSeriesStrategy().name().toLowerCase());
        }

        final Integer[] exposedPorts = new ArrayList<>(networkProtocolMap.values())
                .toArray(new Integer[0]);
        final int[] exposedUdpPorts = networkProtocolMap.entrySet().stream()
                .filter(e -> InternetProtocol.UDP.equals(e.getKey().getIpProtocol()))
                .mapToInt(Map.Entry::getValue)
                .toArray();

        String javaOpts = "-Xms2048m -Xmx2048m -Djava.security.egd=file:/dev/./urandom ";
        if (profile.isJvmDebuggingEnabled()) {
            javaOpts += String.format("-agentlib:jdwp=transport=dt_socket,server=y,address=*:%d,suspend=n", OPENNMS_DEBUG_PORT);
        }

        withExposedPorts(exposedPorts)
                .withCreateContainerCmdModifier(cmd -> {
                    final CreateContainerCmd createCmd = (CreateContainerCmd)cmd;
                    TestContainerUtils.setGlobalMemAndCpuLimits(createCmd);
                    // The framework doesn't support exposing UDP ports directly, so we use this hook to map some of the exposed ports to UDP
                    TestContainerUtils.exposePortsAsUdp(createCmd, exposedUdpPorts);
                })
                .withEnv("POSTGRES_HOST", DB_ALIAS)
                .withEnv("POSTGRES_PORT", Integer.toString(PostgreSQLContainer.POSTGRESQL_PORT))
                // User/pass are hardcoded in PostgreSQLContainer but are not exposed
                .withEnv("POSTGRES_USER", "test")
                .withEnv("POSTGRES_PASSWORD", "test")
                .withEnv("OPENNMS_DBNAME", "opennms")
                .withEnv("OPENNMS_DBUSER", "opennms")
                .withEnv("OPENNMS_DBPASS", "opennms")
                // These are expected to be set when using Newts
                // We also set the corresponding properties explicitly in our overlay
                .withEnv("OPENNMS_CASSANDRA_HOSTNAMES", CASSANDRA_ALIAS)
                .withEnv("OPENNMS_CASSANDRA_KEYSPACE", "newts")
                .withEnv("OPENNMS_CASSANDRA_PORT", Integer.toString(CassandraContainer.CQL_PORT))
                .withEnv("OPENNMS_CASSANDRA_USERNAME", "cassandra")
                .withEnv("OPENNMS_CASSANDRA_USERNAME", "cassandra")
                .withEnv("JAVA_OPTS", javaOpts)
                .withNetwork(Network.SHARED)
                .withNetworkAliases(ALIAS)
                .withCommand(containerCommand)
                .waitingFor(new WaitForOpenNMS(this))
                .addFileSystemBind(overlay.toString(),
                        "/opt/opennms-overlay", BindMode.READ_ONLY, SelinuxContext.SINGLE);

        // Help make development/debugging easier
        DevDebugUtils.setupMavenRepoBind(this, "/root/.m2/repository");
    }

    private Path writeOverlay() {
        try {
            final Path home = Files.createTempDirectory(ALIAS).toAbsolutePath();
            writeOverlay(home);
            return home;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeOverlay(Path home) throws IOException {
        // Allow other users to read the folder
        OverlayUtils.setOverlayPermissions(home);

        // Copy the files from the profile *first*
        // If this test class writes something, we expect it to be there
        OverlayUtils.copyFiles(profile.getFiles(), home);

        // Copy over files from the class-path
        // Files ending in .j2 will be templated using Jinja2 with a context that has the model
        OverlayUtils.copyAndTemplate("opennms-overlay", home, model);

        Path etc = home.resolve("etc");
        Path propsD = etc.resolve("opennms.properties.d");
        Files.createDirectories(propsD);

        final Properties sysProps = getSystemProperties();
        File propsFile = propsD.resolve("stest.properties").toFile();
        try (FileOutputStream fos = new FileOutputStream(propsFile)) {
            sysProps.store(fos, "Generated");
        }

        // Karaf feature configuration

        Path bootD = etc.resolve("featuresBoot.d");
        Files.createDirectories(bootD);
        writeFeaturesBoot(bootD.resolve("stest.boot"), getFeaturesOnBoot());

        if (model.isElasticsearchEnabled()) {
            writeProps(etc.resolve("org.opennms.features.flows.persistence.elastic.cfg"),
                    ImmutableMap.<String,String>builder()
                            .put("elasticUrl", "http://" + ELASTIC_ALIAS + ":9200")
                            .build());

            writeProps(etc.resolve("org.opennms.plugin.elasticsearch.rest.forwarder.cfg"),
                    ImmutableMap.<String,String>builder()
                            .put("elasticUrl", "http://" + ELASTIC_ALIAS + ":9200")
                            // Everything
                            .put("logAllEvents", Boolean.TRUE.toString())
                            // Tweak timeouts and batching
                            .put("batchSize", Integer.toString(500))
                            .put("batchInterval", Integer.toString(500))
                            .put("connTimeout", Integer.toString(5000))
                            .put("retries", Integer.toString(10))
                            .build());

            writeProps(etc.resolve("org.opennms.features.alarms.history.elastic.cfg"),
                    ImmutableMap.<String,String>builder()
                            .put("elasticUrl", "http://" + ELASTIC_ALIAS + ":9200")
                            .build());
        }

        if (model.getOpenNMS().isKafkaProducerEnabled()) {
            writeProps(etc.resolve("org.opennms.features.kafka.producer.client.cfg"),
                    ImmutableMap.<String,String>builder()
                            .put("bootstrap.servers", KAFKA_ALIAS + ":9092")
                            .put("compression.type", model.getKafkaCompressionStrategy().getCodec())
                            .build());
            writeProps(etc.resolve("org.opennms.features.kafka.producer.cfg"),
                    ImmutableMap.<String,String>builder()
                            // This is false by default, so we enable it here
                            .put("forward.metrics", Boolean.TRUE.toString())
                            .put("compression.type", model.getKafkaCompressionStrategy().getCodec())
                            .build());
        }
    }

    /**
     * @return the URL in a form consumable by containers networked with this one using the alias and internal port
     */
    public static URL getBaseUrlInternal() {
        try {
            return new URL(String.format("http://%s:%d/", ALIAS, OPENNMS_WEB_PORT));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the URL in a form consumable by the host using the mapped port
     */
    public URL getBaseUrlExternal() {
        try {
            return new URL(String.format("http://%s:%d/", getContainerIpAddress(), getMappedPort(OPENNMS_WEB_PORT)));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public RestClient getRestClient() {
        try {
            return new RestClient(new URL(getBaseUrlExternal() + "opennms"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public InetSocketAddress getSshAddress() {
        return InetSocketAddress.createUnresolved(getContainerIpAddress(), getMappedPort(OPENNMS_SSH_PORT));
    }

    @Override
    public SshClient ssh() {
        return new SshClient(getSshAddress(), OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD);
    }

    public int getWebPort() {
        return getMappedPort(OPENNMS_WEB_PORT);
    }

    public InetSocketAddress getWebAddress() {
        return InetSocketAddress.createUnresolved(getContainerIpAddress(), getMappedPort(OPENNMS_WEB_PORT));
    }

    public Properties getSystemProperties() {
        final Properties props = new Properties();
        if (IpcStrategy.KAFKA.equals(model.getIpcStrategy())) {
            props.put("org.opennms.core.ipc.strategy", "kafka");
            props.put("org.opennms.core.ipc.kafka.bootstrap.servers", KAFKA_ALIAS + ":9092");
            props.put("org.opennms.core.ipc.kafka.compression.type", model.getKafkaCompressionStrategy().getCodec());
        }
        if (IpcStrategy.GRPC.equals(model.getIpcStrategy())) {
            props.put("org.opennms.core.ipc.strategy", "osgi");
        }

        if (TimeSeriesStrategy.RRD.equals(model.getTimeSeriesStrategy())) {
            // Use jrrd2
            props.put("org.opennms.rrd.strategyClass", "org.opennms.netmgt.rrd.rrdtool.MultithreadedJniRrdStrategy");
            props.put("org.opennms.rrd.interfaceJar", "/usr/share/java/jrrd2.jar");
            props.put("opennms.library.jrrd2", "/usr/lib64/libjrrd2.so");
        } else if (TimeSeriesStrategy.NEWTS.equals(model.getTimeSeriesStrategy())) {
            // Use Newts
            props.put("org.opennms.timeseries.strategy", "newts");
            props.put("org.opennms.newts.config.hostname", CASSANDRA_ALIAS);
            props.put("org.opennms.newts.config.port", Integer.toString(CassandraContainer.CQL_PORT));
            props.put("org.opennms.rrd.storeByForeignSource", Boolean.TRUE.toString());
        }

        // output Karaf logs to the console to help in debugging intermittent container startup failures
        props.put("karaf.log.console", "INFO");
        return props;
    }

    public List<String> getFeaturesOnBoot() {
        final List<String> featuresOnBoot = new ArrayList<>();
        if(IpcStrategy.GRPC.equals(model.getIpcStrategy())) {
            featuresOnBoot.add("opennms-core-ipc-grpc-server");
        }
        if (model.isElasticsearchEnabled()) {
            featuresOnBoot.add("opennms-es-rest");
            // Disabled for now as this can cause intermittent health check failures
            // featuresOnBoot.add("opennms-alarm-history-elastic");
        }
        if (profile.isKafkaProducerEnabled()) {
            featuresOnBoot.add("opennms-kafka-producer");
        }
        return featuresOnBoot;
    }

    public InetSocketAddress getNetworkProtocolAddress(NetworkProtocol protocol) {
        final Integer port = networkProtocolMap.get(protocol);
        if (port == null) {
            throw new IllegalArgumentException("No known port mapping for: " + protocol);
        }

        int mappedPort;
        if (InternetProtocol.UDP.equals(protocol.getIpProtocol())) {
            mappedPort = TestContainerUtils.getMappedUdpPort(this, port);
        } else {
            mappedPort = getMappedPort(port);
        }

        return new InetSocketAddress(getContainerIpAddress(), mappedPort);
    }

    public StackModel getModel() {
        return model;
    }

    private static class WaitForOpenNMS extends org.testcontainers.containers.wait.strategy.AbstractWaitStrategy {
        private final OpenNMSContainer container;

        public WaitForOpenNMS(OpenNMSContainer container) {
            this.container = Objects.requireNonNull(container);
        }

        @Override
        protected void waitUntilReady() {
            LOG.info("Waiting for startup to begin.");
            final Path managerLog = Paths.get("/opt", "opennms", "logs", "manager.log");
            await().atMost(3, MINUTES).ignoreExceptions()
                    .until(() -> TestContainerUtils.getFileFromContainerAsString(container, managerLog),
                    containsString("Starter: Beginning startup"));
            LOG.info("OpenNMS has begun starting up.");

            LOG.info("Waiting for OpenNMS REST API...");
            final long timeoutMins = 5;
            final RestClient restClient = container.getRestClient();
            final AtomicReference<String> lastOutput = new AtomicReference<>();
            try {
                await().atMost(timeoutMins, MINUTES)
                        .pollInterval(10, SECONDS)
                        .ignoreExceptions()
                        .until(restClient::getDisplayVersion, notNullValue());
            } catch(ConditionTimeoutException e) {
                LOG.error("OpenNMS did not finish starting after {} minutes. Last output: {}", timeoutMins, lastOutput);
                throw new RuntimeException(e);
            }
            LOG.info("OpenNMS REST API is online.");

            // Wait until all daemons have finished starting up
            // This helps ensure that all of the sockets that should be up and listening i.e. teletrymd flows
            // have been given a chance to bind
            LOG.info("Waiting for startup to complete.");
            await().atMost(5, MINUTES).until(() -> TestContainerUtils.getFileFromContainerAsString(container, managerLog),
                    containsString("Starter: Startup complete"));
            LOG.info("OpenNMS has started.");

            // Defer the health-check (if we do run it) until the system has completely started
            // in order to give all the health checks a chance to load.
            // Only wait for the health-check if Elasticsearch is enabled, since it's
            // currently required to pass.
            if (container.getModel().isElasticsearchEnabled()) {
                LOG.info("Waiting for OpenNMS health check...");
                final InetSocketAddress karafSsh = container.getSshAddress();
                awaitHealthCheckSucceeded(karafSsh, 3, "OpenNMS");
                LOG.info("Health check passed.");
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

    private static void copyLogs(OpenNMSContainer container, String prefix) {
        // List of known log files we expect to find in the container
        final List<String> logFiles = Arrays.asList("alarmd.log",
                "collectd.log",
                "eventd.log",
                "jetty-server.log",
                "karaf.log",
                "manager.log",
                "poller.log",
                "provisiond.log",
                "trapd.log",
                "web.log");
        DevDebugUtils.copyLogs(container,
                // dest
                Paths.get("target", "logs", prefix, "opennms"),
                // source folder
                Paths.get("/opt", "opennms", "logs"),
                // log files
                logFiles);
    }

}
