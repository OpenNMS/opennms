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

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.containsString;
import static org.opennms.smoketest.utils.OverlayUtils.writeFeaturesBoot;
import static org.opennms.smoketest.utils.OverlayUtils.writeProps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileUtils;
import org.opennms.smoketest.stacks.IpcStrategy;
import org.opennms.smoketest.stacks.JsonStoreStrategy;
import org.opennms.smoketest.stacks.SentinelProfile;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.stacks.TimeSeriesStrategy;
import org.opennms.smoketest.utils.DevDebugUtils;
import org.opennms.smoketest.utils.OverlayUtils;
import org.opennms.smoketest.utils.RestHealthClient;
import org.opennms.smoketest.utils.SshClient;
import org.opennms.smoketest.utils.TargetRoot;
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
import org.testcontainers.utility.MountableFile;

import com.google.common.collect.ImmutableMap;

public class SentinelContainer extends GenericContainer<SentinelContainer> implements KarafContainer<SentinelContainer>, TestLifecycleAware {
    private static final Logger LOG = LoggerFactory.getLogger(SentinelContainer.class);
    private static final int SENTINEL_DEBUG_PORT = 5005;
    private static final int SENTINEL_SSH_PORT = 8301;
    private static final int SENTINEL_JETTY_PORT = 8181;
    static final String IMAGE = "opennms/sentinel";
    static final String ALIAS = "sentinel";

    private final StackModel model;
    private final SentinelProfile profile;
    private final Path overlay;

    public SentinelContainer(StackModel model, SentinelProfile profile) {
        super(IMAGE);
        this.model = Objects.requireNonNull(model);
        this.profile = Objects.requireNonNull(profile);
        this.overlay = writeOverlay();
        withExposedPorts(SENTINEL_DEBUG_PORT, SENTINEL_SSH_PORT, SENTINEL_JETTY_PORT)
                .withEnv("SENTINEL_LOCATION", "Sentinel")
                .withEnv("SENTINEL_ID", profile.getId())
                .withEnv("POSTGRES_HOST", OpenNMSContainer.DB_ALIAS)
                .withEnv("POSTGRES_PORT", Integer.toString(PostgreSQLContainer.POSTGRESQL_PORT))
                // User/pass are hardcoded in PostgreSQLContainer but are not exposed
                .withEnv("POSTGRES_USER", "test")
                .withEnv("POSTGRES_PASSWORD", "test")
                .withEnv("OPENNMS_DBNAME", "opennms")
                .withEnv("OPENNMS_DBUSER", "opennms")
                .withEnv("OPENNMS_DBPASS", "opennms")
                .withEnv("OPENNMS_BROKER_URL", "failover:tcp://" + OpenNMSContainer.ALIAS + ":61616")
                .withEnv("OPENNMS_HTTP_USER", "admin")
                .withEnv("OPENNMS_HTTP_PASS", "admin")
                .withEnv("OPENNMS_BROKER_USER", "admin")
                .withEnv("OPENNMS_BROKER_PASS", "admin")
                .withEnv("JACOCO_AGENT_ENABLED", "1")
                .withEnv("JAVA_OPTS", "-Xms1g -Xmx1g -Djava.security.egd=file:/dev/./urandom -Dorg.opennms.rrd.storeByForeignSource=true")
                .withNetwork(Network.SHARED)
                .withNetworkAliases(ALIAS)
                .withCommand("-f")
                .waitingFor(new WaitForSentinel(this))
                .withCreateContainerCmdModifier(TestContainerUtils::setGlobalMemAndCpuLimits)
                .addFileSystemBind(overlay.toString(),
                        "/opt/sentinel-overlay", BindMode.READ_ONLY, SelinuxContext.SINGLE);

        if (profile.isJvmDebuggingEnabled()) {
            withEnv("KARAF_DEBUG", "true");
            withEnv("JAVA_DEBUG_PORT", "" + SENTINEL_DEBUG_PORT);
        }

        // Help make development/debugging easier
        DevDebugUtils.setupMavenRepoBind(this, "/opt/sentinel/.m2");
    }

    @SuppressWarnings("java:S5443")
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

        Path etc = home.resolve("etc");
        Files.createDirectories(etc);

        // Copy configuration from $OPENNMS_HOME/etc
        final Path opennmsSourceEtcDirectory = new TargetRoot(getClass()).getPath("system-test-resources", "etc");
        FileUtils.copyDirectory(opennmsSourceEtcDirectory.resolve("telemetryd-adapters").toFile(), etc.resolve("telemetryd-adapters").toFile());
        FileUtils.copyDirectory(opennmsSourceEtcDirectory.resolve("resource-types.d").toFile(), etc.resolve("resource-types.d").toFile());
        FileUtils.copyDirectory(opennmsSourceEtcDirectory.resolve("datacollection").toFile(), etc.resolve("datacollection").toFile());
        FileUtils.copyFile(opennmsSourceEtcDirectory.resolve("datacollection-config.xml").toFile(), etc.resolve("datacollection-config.xml").toFile());

        // Copy over the fixed configuration from the class-path
        FileUtils.copyDirectory(new File(MountableFile.forClasspathResource("sentinel-overlay").getFilesystemPath()), home.toFile());

        final Properties sysProps = getSystemProperties();
        File propsFile = etc.resolve("custom.system.properties").toFile();
        try (@SuppressWarnings("java:S6300") FileOutputStream fos = new FileOutputStream(propsFile)) {
            sysProps.store(fos, "Generated");
        }

        Path bootD = etc.resolve("featuresBoot.d");
        Files.createDirectories(bootD);
        writeFeaturesBoot(bootD.resolve("stest.boot"), getFeaturesOnBoot());

        writeProps(etc.resolve("org.opennms.core.ipc.sink.kafka.consumer.cfg"),
                ImmutableMap.<String,String>builder()
                        .put("bootstrap.servers", OpenNMSContainer.KAFKA_ALIAS + ":9092")
                        .put("acks", "1")
                        .put("compression.type", model.getKafkaCompressionStrategy().getCodec())
                        .build());

        writeProps(etc.resolve("org.opennms.core.ipc.sink.kafka.cfg"),
                ImmutableMap.<String,String>builder()
                        .put("bootstrap.servers", OpenNMSContainer.KAFKA_ALIAS + ":9092")
                        .put("acks", "1")
                        .put("compression.type", model.getKafkaCompressionStrategy().getCodec())
                        .build());

        writeProps(etc.resolve("org.opennms.features.flows.persistence.elastic.cfg"),
                ImmutableMap.<String,String>builder()
                        .put("elasticUrl", "http://" + OpenNMSContainer.ELASTIC_ALIAS + ":9200")
                        .build());

        if (TimeSeriesStrategy.NEWTS.equals(model.getTimeSeriesStrategy())) {
            writeProps(etc.resolve("org.opennms.newts.config.cfg"),
                    ImmutableMap.<String,String>builder()
                            .put("hostname", OpenNMSContainer.CASSANDRA_ALIAS)
                            .put("port", Integer.toString(CassandraContainer.CQL_PORT))
                            .build());
        }
    }

    public List<String> getFeaturesOnBoot() {
        final List<String> featuresOnBoot = new ArrayList<>();
        featuresOnBoot.add("sentinel-persistence");
        featuresOnBoot.add("sentinel-core");
        featuresOnBoot.add("opennms-health-rest-service");
        if (IpcStrategy.KAFKA.equals(model.getIpcStrategy())) {
            featuresOnBoot.add("sentinel-kafka");
        } else if (IpcStrategy.JMS.equals(model.getIpcStrategy())) {
            featuresOnBoot.add("sentinel-jms");
        }
        if (TimeSeriesStrategy.NEWTS.equals(model.getTimeSeriesStrategy())) {
            featuresOnBoot.add("sentinel-newts");
        }
        if (model.isTelemetryProcessingEnabled()) {
            featuresOnBoot.add("sentinel-flows");
            featuresOnBoot.add("sentinel-telemetry-bmp");
            featuresOnBoot.add("sentinel-telemetry-graphite");
            featuresOnBoot.add("sentinel-telemetry-jti");
            featuresOnBoot.add("sentinel-telemetry-nxos");
        }
        
        switch (model.getBlobStoreStrategy()) {
            case NOOP:
                featuresOnBoot.add("sentinel-blobstore-noop");
                break;
            case NEWTS_CASSANDRA:
                featuresOnBoot.add("sentinel-blobstore-cassandra");
                break;
        }

        if (model.getJsonStoreStrategy() == null || model.getJsonStoreStrategy() == JsonStoreStrategy.POSTGRES) {
            featuresOnBoot.add("sentinel-jsonstore-postgres");
        }

        if (model.isJaegerEnabled()) {
            featuresOnBoot.add("opennms-core-tracing-jaeger");
        }

        return featuresOnBoot;
    }

    public Properties getSystemProperties() {
        final Properties props = new Properties();

        if (model.isJaegerEnabled()) {
            props.put("JAEGER_ENDPOINT", "http://jaeger:14268/api/traces");
        }

        return props;
    }

    @Override
    public InetSocketAddress getSshAddress() {
        return new InetSocketAddress(getContainerIpAddress(), getMappedPort(SENTINEL_SSH_PORT));
    }

    @Override
    public SshClient ssh() {
        return new SshClient(getSshAddress(), OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD);
    }

    @Override
    public Path getKarafHomeDirectory() {
        return Path.of("/opt/sentinel");
    }

    public URL getWebUrl() {
        try {
            return new URL(String.format("http://%s:%d/", getContainerIpAddress(), getMappedPort(SENTINEL_JETTY_PORT)));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getWebPort() {
        return SENTINEL_JETTY_PORT;
    }

    private static class WaitForSentinel extends org.testcontainers.containers.wait.strategy.AbstractWaitStrategy {
        private final SentinelContainer container;

        public WaitForSentinel(SentinelContainer container) {
            this.container = Objects.requireNonNull(container);
        }

        @Override
        protected void waitUntilReady() {
            LOG.info("Waiting for Sentinel health check...");
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
        // KarafShellUtils.saveCoverage(this, description.getFilesystemFriendlyName(), ALIAS);
        retainLogsfNeeded(description.getFilesystemFriendlyName(), !throwable.isPresent());
    }

    private void retainLogsfNeeded(String prefix, boolean succeeded) {
        Path targetLogFolder = Paths.get("target", "logs", prefix, ALIAS);
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
                Paths.get("/opt", ALIAS, "data", "log"),
                // log files
                logFiles);

        LOG.info("Log directory: {}", targetLogFolder.toUri());
        LOG.info("Console log: {}", targetLogFolder.resolve(DevDebugUtils.CONTAINER_STDOUT_STDERR).toUri());
        if (threadDump.get() != null) {
            LOG.info("Thread dump: {}", threadDump.get().toUri());
        }
    }

}
