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
import static org.opennms.smoketest.utils.OverlayUtils.writeFeaturesBoot;
import static org.opennms.smoketest.utils.OverlayUtils.writeProps;

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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.SelinuxContext;
import org.testcontainers.lifecycle.TestDescription;
import org.testcontainers.lifecycle.TestLifecycleAware;

import com.google.common.collect.ImmutableMap;

/**
 * Testcontainers container for a standalone Alarmd instance.
 *
 * <p>This container runs Alarmd as an independent Karaf process, consuming events
 * from Kafka and creating alarms in PostgreSQL. It connects to the same broker
 * and database as the core OpenNMS container, validating that Alarmd can be
 * extracted from the monolith and run independently.</p>
 */
public class AlarmdContainer extends GenericContainer<AlarmdContainer> implements KarafContainer<AlarmdContainer>, TestLifecycleAware {
    private static final Logger LOG = LoggerFactory.getLogger(AlarmdContainer.class);

    private static final int ALARMD_SSH_PORT = 8201;
    private static final int ALARMD_JETTY_PORT = 8181;
    private static final int ALARMD_DEBUG_PORT = 5005;

    static final String IMAGE = "opennms/alarmd";
    static final String ALIAS = "alarmd";

    private final StackModel model;
    private final Path overlay;

    public AlarmdContainer(StackModel model) {
        super(IMAGE);
        this.model = Objects.requireNonNull(model);
        this.overlay = writeOverlay();

        withExposedPorts(ALARMD_SSH_PORT, ALARMD_JETTY_PORT, ALARMD_DEBUG_PORT)
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
                .withEnv("JAVA_OPTS", "-Xms1g -Xmx1g -Djava.security.egd=file:/dev/./urandom")
                .withNetwork(Network.SHARED)
                .withNetworkAliases(ALIAS)
                // The Alarmd Dockerfile has no overlay processing in its entrypoint,
                // so we use a custom command that copies overlay files before starting Karaf.
                .withCommand("/bin/bash", "-c",
                        "cp -a /opt/alarmd-overlay/* /opt/alarmd/ 2>/dev/null; exec /opt/alarmd/bin/karaf server")
                .waitingFor(new WaitForAlarmd(this))
                .withCreateContainerCmdModifier(TestContainerUtils::setGlobalMemAndCpuLimits)
                .addFileSystemBind(overlay.toString(),
                        "/opt/alarmd-overlay", BindMode.READ_ONLY, SelinuxContext.SINGLE);

        // Help make development/debugging easier
        DevDebugUtils.setupMavenRepoBind(this, "/opt/alarmd/.m2");
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

        Path etc = home.resolve("etc");
        Files.createDirectories(etc);

        // Karaf feature boot configuration for the smoke test
        Path bootD = etc.resolve("featuresBoot.d");
        Files.createDirectories(bootD);
        writeFeaturesBoot(bootD.resolve("stest.boot"), getFeaturesOnBoot());

        // Kafka consumer configuration — connects to the shared Kafka container
        writeProps(etc.resolve("org.opennms.features.events.kafka.consumer.cfg"),
                ImmutableMap.<String, String>builder()
                        .put("bootstrap.servers", OpenNMSContainer.KAFKA_ALIAS + ":9092")
                        .build());
    }

    private List<String> getFeaturesOnBoot() {
        return List.of(
                "opennms-health-rest-service"
        );
    }

    @Override
    public InetSocketAddress getSshAddress() {
        return new InetSocketAddress(getContainerIpAddress(), getMappedPort(ALARMD_SSH_PORT));
    }

    @Override
    public SshClient ssh() {
        return new SshClient(getSshAddress(), OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD);
    }

    @Override
    public Path getKarafHomeDirectory() {
        return Path.of("/opt/alarmd");
    }

    public URL getWebUrl() {
        try {
            return new URL(String.format("http://%s:%d/", getContainerIpAddress(), getMappedPort(ALARMD_JETTY_PORT)));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getWebPort() {
        return ALARMD_JETTY_PORT;
    }

    private static class WaitForAlarmd extends org.testcontainers.containers.wait.strategy.AbstractWaitStrategy {
        private final AlarmdContainer container;

        public WaitForAlarmd(AlarmdContainer container) {
            this.container = Objects.requireNonNull(container);
        }

        @Override
        protected void waitUntilReady() {
            LOG.info("Waiting for Alarmd health check...");
            RestHealthClient client = new RestHealthClient(container.getWebUrl(), Optional.of(ALIAS));
            await("waiting for good health check probe")
                    .atMost(5, MINUTES)
                    .pollInterval(10, SECONDS)
                    .failFast("container is no longer running", () -> !container.isRunning())
                    .ignoreExceptionsMatching((e) -> e.getCause() != null && e.getCause() instanceof SocketException)
                    .until(client::getProbeHealthResponse, containsString(client.getProbeSuccessMessage()));
            LOG.info("Health check passed.");

            container.assertNoKarafDestroy(Paths.get("/opt", ALIAS, "data", "log", "karaf.log"));
        }
    }

    @Override
    public void afterTest(TestDescription description, Optional<Throwable> throwable) {
        retainLogsIfNeeded(description.getFilesystemFriendlyName(), !throwable.isPresent());
    }

    private void retainLogsIfNeeded(String prefix, boolean succeeded) {
        Path targetLogFolder = Paths.get("target", "logs", prefix, ALIAS);
        DevDebugUtils.clearLogs(targetLogFolder);

        AtomicReference<Path> threadDump = new AtomicReference<>();
        await("calling gatherThreadDump")
                .atMost(Duration.ofSeconds(120))
                .untilAsserted(
                        () -> threadDump.set(DevDebugUtils.gatherThreadDump(this, targetLogFolder, null))
                );

        LOG.info("Gathering logs...");
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
