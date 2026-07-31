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
package org.opennms.smoketest.ha;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.containers.PostgreSQLContainer;
import org.opennms.smoketest.stacks.OpenNMSProfile;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.TestContainerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;

/**
 * End-to-end active/passive HA pair: two OpenNMS containers sharing one
 * PostgreSQL, coordinated through the {@code ha_instance_status} table.
 *
 * <p>Requires an image that ships the HA artifacts (lib jars, feature
 * bundles); the standby activates them via {@code etc/featuresBoot.d}.
 *
 * <p>One ordered flow (containers are expensive):
 * <ol>
 *   <li>secondary starts gated: row STANDBY, services blocked;</li>
 *   <li>REST failover on the primary: STANDBY is published only after its
 *       drain, and the secondary promotes on it via the fast path (well
 *       inside the staleness threshold);</li>
 *   <li>the primary restarts and parks DEGRADED (never a transient ACTIVE);</li>
 *   <li>the active secondary is killed hard: the primary reclaims via
 *       heartbeat staleness, logging the dead-partner warning and never a
 *       split-brain error.</li>
 * </ol>
 */
public class HighAvailabilityIT {

    private static final Logger LOG = LoggerFactory.getLogger(HighAvailabilityIT.class);

    private static final String MANAGER_LOG = "/opt/opennms/logs/manager.log";
    private static final int FAILOVER_THRESHOLD_SECONDS = 60; // keep in sync with ha-configuration-*.xml

    // The primary is gated on restart (DEGRADED), so waiting for the web UI
    // would hang; readiness is asserted against the database instead.
    @ClassRule
    public static final OpenNMSStack STACK = OpenNMSStack.minimal(
            b -> b.withFile("ha/ha-configuration-primary.xml", "etc/ha-configuration.xml"),
            b -> b.withFile("ha/ha.boot", "etc/featuresBoot.d/ha.boot"));

    private static OpenNMSContainer secondary;

    @AfterClass
    public static void tearDownSecondary() {
        if (secondary != null) {
            secondary.stop();
        }
    }

    @Test
    public void activePassivePairFailsOverAndBack() throws Exception {
        // --- 1. Primary is ACTIVE (stack wait already proved the web UI); start the
        // secondary and expect it to park at the gate as STANDBY, services blocked.
        await("primary claims ACTIVE").atMost(5, TimeUnit.MINUTES)
                .until(() -> rowState("opennms-primary"), equalTo("ACTIVE"));

        secondary = new OpenNMSContainer(secondaryModel(), secondaryModel().getOpenNMS());
        secondary.setNetworkAliases(List.of("opennms2"));
        secondary.start();

        await("secondary registers as gated STANDBY").atMost(10, TimeUnit.MINUTES)
                .until(() -> rowState("opennms-secondary"), equalTo("STANDBY"));
        assertEquals("secondary must be gated, not starting services",
                "1", grepCount(secondary, "blocking service startup"));

        // --- 2. Voluntary failover: STANDBY is published only after the primary's
        // drain, and the secondary must promote on it via the fast path.
        final HttpResponse<String> failover = post(STACK.opennms().getWebUrl() + "opennms/rest/ha/failover");
        assertThat("failover accepted", failover.statusCode(), equalTo(202));

        await("primary publishes STANDBY after its drain").atMost(5, TimeUnit.MINUTES)
                .until(() -> rowState("opennms-primary"), equalTo("STANDBY"));
        final long standbyPublishedAt = System.currentTimeMillis();

        await("secondary promotes on the published state").atMost(2, TimeUnit.MINUTES)
                .until(() -> rowState("opennms-secondary"), equalTo("ACTIVE"));
        final long promotionSeconds = (System.currentTimeMillis() - standbyPublishedAt) / 1000;
        LOG.info("secondary promoted {}s after the STANDBY publish", promotionSeconds);
        assertThat("promotion must ride the fast path, not heartbeat staleness",
                promotionSeconds, lessThan((long) FAILOVER_THRESHOLD_SECONDS));

        // --- 3. The primary rejoins and must park DEGRADED — never a transient
        // ACTIVE that could trip the serving secondary's split-brain arbitration.
        TestContainerUtils.restartContainer(STACK.opennms());
        await("restarted primary parks DEGRADED").atMost(10, TimeUnit.MINUTES)
                .until(() -> rowState("opennms-primary"), equalTo("DEGRADED"));
        assertEquals("no split-brain during the rejoin",
                "0", grepCount(secondary, "SPLIT-BRAIN DETECTED"));

        // --- 4. Kill the active secondary: the DEGRADED primary reclaims via
        // heartbeat staleness, logging the dead-partner warning, never split-brain.
        DockerClientFactory.instance().client()
                .killContainerCmd(secondary.getContainerId()).withSignal("SIGKILL").exec();

        await("primary reclaims ACTIVE via staleness").atMost(5, TimeUnit.MINUTES)
                .until(() -> rowState("opennms-primary"), equalTo("ACTIVE"));
        assertEquals("no split-brain against the dead partner",
                "0", grepCount(STACK.opennms(), "SPLIT-BRAIN DETECTED"));
    }

    // -------------------------------------------------------------------------

    private static StackModel secondaryModel() {
        return StackModel.newBuilder()
                .withOpenNMS(OpenNMSProfile.newBuilder()
                        .withFile("empty-discovery-configuration.xml", "etc/discovery-configuration.xml")
                        .withFile("ha/ha-configuration-secondary.xml", "etc/ha-configuration.xml")
                        .withFile("ha/ha.boot", "etc/featuresBoot.d/ha.boot")
                        // A gated standby never opens the web UI; readiness is
                        // asserted against the database from the test instead.
                        .withWaitStrategy(c -> new AbstractWaitStrategy() {
                            @Override
                            protected void waitUntilReady() {
                            }
                        })
                        .build())
                .build();
    }

    private static org.awaitility.core.ConditionFactory await(String alias) {
        return Awaitility.await(alias).pollInterval(5, TimeUnit.SECONDS).ignoreExceptions();
    }

    private static String rowState(String instanceId) throws Exception {
        final String url = String.format("jdbc:postgresql://%s:%d/opennms",
                STACK.postgres().getHost(),
                STACK.postgres().getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT));
        try (Connection conn = DriverManager.getConnection(url, "opennms", "opennms");
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT current_state FROM ha_instance_status WHERE instance_id = ?")) {
            ps.setString(1, instanceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private static String grepCount(OpenNMSContainer container, String pattern) throws Exception {
        final var result = container.execInContainer("sh", "-c",
                "grep -c '" + pattern + "' " + MANAGER_LOG + " || true");
        return result.getStdout().trim();
    }

    private static HttpResponse<String> post(String url) throws Exception {
        final String auth = Base64.getEncoder()
                .encodeToString("admin:admin".getBytes(StandardCharsets.UTF_8));
        return HttpClient.newHttpClient().send(HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", "Basic " + auth)
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .timeout(Duration.ofSeconds(30))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
