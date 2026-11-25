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
package org.opennms.smoketest.sentinel;

import static org.awaitility.Awaitility.await;
import static org.opennms.smoketest.minion.NxosTelemetryIT.sendNxosTelemetryMessage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.Timeout;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.netmgt.model.resource.ResourceDTO;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.junit.SentinelTests;
import org.opennms.smoketest.stacks.NetworkProtocol;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.KarafShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Container;
import org.testcontainers.utility.MountableFile;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Verifies end-to-end that the resource types stored in the configuration manager of OpenNMS (formerly
 * {@code etc/resource-types.d/}) reach Sentinel through the JSON store, and that types added, updated and deleted
 * on OpenNMS at runtime reach a running Sentinel without a restart.
 *
 * <p>NXOS telemetry is sent to Minion and processed by the NXOS adapter on Sentinel, which persists to Newts. The
 * adapter script creates generic type resources, which Sentinel resolves with its resource type definitions:</p>
 * <ul>
 *     <li>An unknown type without fallback fails the whole collection set, so a persisted resource proves that
 *     Sentinel knew its type.</li>
 *     <li>An unknown type with fallback keeps its name but uses the strategies of the fallback type. The added type
 *     therefore uses a different storage strategy, whose instance name shows which definition Sentinel used.</li>
 * </ul>
 */
@Category(SentinelTests.class)
public class SentinelResourceTypesIT {
    private static final Logger LOG = LoggerFactory.getLogger(SentinelResourceTypesIT.class);

    private static final String NODE_CRITERIA = "telemetry-nxos:nexus9k";
    private static final String NODE_NEWTS_PATH = "snmp:fs:telemetry-nxos:nexus9k:";

    /** Shipped in {@code etc/resource-types.d/nxos-resource.xml}, used by the stock NXOS script. */
    private static final String SHIPPED_TYPE = "nxosCpu";
    /** Managed by this test, used by the test script with {@link #SHIPPED_TYPE} as fallback. */
    private static final String TEST_TYPE = "smokeTestCpu";
    private static final String TEST_CONFIG_PATH = "/resource-types/smoke-test";

    /** Path configured in sentinel-overlay/etc/org.opennms.features.telemetry.adapters-nxos.cfg */
    private static final String SENTINEL_NXOS_SCRIPT = "/opt/sentinel/etc/telemetryd-adapters/cisco-nxos-telemetry-interface.groovy";
    private static final String TEST_SCRIPT_TEMPLATE = "/sentinel-resource-types/cisco-nxos-telemetry-interface.groovy";

    // Sentinel polls the JSON store every 30s, the adapter reloads its script on change, Newts indexes asynchronously
    private static final Duration WAIT = Duration.ofMinutes(5);
    private static final Duration SEND_INTERVAL = Duration.ofSeconds(10);

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withMinion()
            .withSentinel()
            .withTelemetryProcessing()
            .build());

    @Rule
    public Timeout timeout = new Timeout(40, TimeUnit.MINUTES);

    @BeforeClass
    public static void createNodeAndWaitForSentinel() {
        final AbstractAdapterIT.RequisitionCreateInfo requisition = new AbstractAdapterIT.RequisitionCreateInfo();
        requisition.foreignSource = "telemetry-nxos";
        requisition.foreignId = "nexus9k";
        requisition.nodeLabel = "nexus9k";
        requisition.ipAddress = "192.168.0.1";
        AbstractAdapterIT.createRequisition(requisition, stack.opennms().getWebAddress(), stack.postgres().getDaoFactory());

        final KarafShell sentinelShell = new KarafShell(stack.sentinel().getSshAddress());
        sentinelShell.verifyLog(output -> output.contains("Route: Sink.Server.Telemetry-NXOS started and consuming from: queuingservice://"
                + SystemInfoUtils.getInstanceId() + ".Sink.Telemetry-NXOS"));
        // Sentinel may not know the node yet
        sentinelShell.runCommand("opennms:sync-node-cache");
    }

    @Test
    public void resourceTypesStoredOnOpenNMSAreUsedBySentinel() throws Exception {
        // 1. A shipped resource type, imported from etc/resource-types.d/ by the upgrade, is known to Sentinel
        LOG.info("Waiting for {} resources created by the stock NXOS script on Sentinel...", SHIPPED_TYPE);
        awaitWhileSending(() -> hasResource(SHIPPED_TYPE, null, "idle"));

        // 2. A resource type added on OpenNMS reaches the running Sentinel.
        // Until then the test script's resources fall back to the IndexStorageStrategy of nxosCpu: smokeTestCpu/<cpu>.
        // With the added definition they are stored as smokeTestCpu/added.
        installTestScript("idleBefore");
        LOG.info("Adding resource type {} on OpenNMS...", TEST_TYPE);
        cm().contentType(ContentType.JSON).body(testTypeConfig("added"))
                .post(TEST_CONFIG_PATH)
                .then().statusCode(200);
        LOG.info("Waiting for Sentinel to use the added resource type {}...", TEST_TYPE);
        awaitWhileSending(() -> hasResource(TEST_TYPE, "added", "idleBefore"));

        // 3. A resource type updated on OpenNMS is updated on the running Sentinel
        LOG.info("Updating resource type {} on OpenNMS...", TEST_TYPE);
        cm().contentType(ContentType.JSON).body(testTypeConfig("updated"))
                .queryParam("replace", true)
                .put(TEST_CONFIG_PATH)
                .then().statusCode(200);
        LOG.info("Waiting for Sentinel to use the updated resource type {}...", TEST_TYPE);
        awaitWhileSending(() -> hasResource(TEST_TYPE, "updated", "idleBefore"));

        // 4. A resource type deleted on OpenNMS is removed from the running Sentinel: it falls back to nxosCpu again.
        // OpenNMS doesn't list resources of a type it doesn't know any longer, so this is checked in Newts directly.
        // The attribute name changes so the data written before the type was added doesn't count.
        LOG.info("Deleting resource type {} on OpenNMS...", TEST_TYPE);
        cm().delete(TEST_CONFIG_PATH)
                .then().statusCode(200);
        installTestScript("idleAfter");
        LOG.info("Waiting for Sentinel to fall back to {} again...", SHIPPED_TYPE);
        final Pattern fallbackResource = Pattern.compile(Pattern.quote(NODE_NEWTS_PATH + TEST_TYPE + ":")
                + "(?!added:|updated:)[^:]+:smoke-test-cpu");
        awaitWhileSending(() -> newtsHasMetric(fallbackResource, "idleAfter"));
    }

    private static String testTypeConfig(final String instanceName) {
        // JexlIndexStorageStrategy with a constant index-format stores all instances under that name
        return "{\"resourceType\":[{"
                + "\"name\":\"" + TEST_TYPE + "\",\"label\":\"Smoke Test CPU\",\"resourceLabel\":\"CPU ${index}\","
                + "\"persistenceSelectorStrategy\":{\"class\":\"org.opennms.netmgt.collection.support.PersistAllSelectorStrategy\"},"
                + "\"storageStrategy\":{\"class\":\"org.opennms.netmgt.collection.support.JexlIndexStorageStrategy\","
                + "\"parameter\":[{\"key\":\"index-format\",\"value\":\"" + instanceName + "\"}]}"
                + "}]}";
    }

    /**
     * Sends NXOS telemetry to Minion until the condition is met.
     */
    private static void awaitWhileSending(final Callable<Boolean> condition) {
        await().atMost(WAIT).pollDelay(Duration.ZERO).pollInterval(SEND_INTERVAL)
                .ignoreExceptions()
                .until(() -> {
                    sendNxosTelemetryMessage(stack.minion().getNetworkProtocolAddress(NetworkProtocol.NXOS));
                    return condition.call();
                });
    }

    /**
     * @return whether OpenNMS lists a resource of the given type and instance (any if null) with the given attribute
     */
    private static boolean hasResource(final String resourceType, final String instance, final String attribute) {
        final String idPart = "." + resourceType + "[" + (instance != null ? instance + "]" : "");
        final List<ResourceDTO> children = stack.opennms().getRestClient().getResourcesForNode(NODE_CRITERIA)
                .getChildren().getObjects();
        LOG.debug("Resources of node {}: {}", NODE_CRITERIA, children.stream()
                .map(r -> r.getId() + r.getRrdGraphAttributes().keySet())
                .collect(Collectors.joining(", ")));
        return children.stream()
                .filter(r -> r.getId().contains(idPart))
                .anyMatch(r -> r.getRrdGraphAttributes().containsKey(attribute));
    }

    /**
     * @return whether Newts indexed the given metric for a resource matching the given pattern
     */
    private static boolean newtsHasMetric(final Pattern resource, final String metric) throws IOException, InterruptedException {
        final Container.ExecResult result = stack.cassandra().execInContainer("cqlsh", "-e",
                "SELECT resource, metric_name FROM newts.resource_metrics;");
        if (result.getExitCode() != 0) {
            throw new IllegalStateException("cqlsh failed: " + result.getStderr());
        }
        final List<String[]> rows = Arrays.stream(result.getStdout().split("\n"))
                .map(line -> line.split("\\|"))
                .filter(columns -> columns.length == 2)
                .map(columns -> new String[]{columns[0].trim(), columns[1].trim()})
                .filter(columns -> columns[0].startsWith(NODE_NEWTS_PATH + TEST_TYPE + ":"))
                .collect(Collectors.toList());
        LOG.debug("Newts metrics of {}: {}", TEST_TYPE, rows.stream()
                .map(columns -> columns[0] + "/" + columns[1])
                .collect(Collectors.joining(", ")));
        return rows.stream().anyMatch(columns -> resource.matcher(columns[0]).matches() && metric.equals(columns[1]));
    }

    /**
     * Replaces the script of the NXOS adapter on Sentinel by the test script, which stores the idle value as the
     * given attribute. The adapter watches its script and reloads it on change.
     */
    private static void installTestScript(final String attribute) throws IOException {
        final String script;
        try (InputStream in = Objects.requireNonNull(SentinelResourceTypesIT.class.getResourceAsStream(TEST_SCRIPT_TEMPLATE))) {
            script = new String(in.readAllBytes(), StandardCharsets.UTF_8).replace("@METRIC@", attribute);
        }
        final Path file = Files.createTempFile("nxos-", ".groovy");
        try {
            Files.writeString(file, script, StandardCharsets.UTF_8);
            stack.sentinel().copyFileToContainer(MountableFile.forHostPath(file, 0644), SENTINEL_NXOS_SCRIPT);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static RequestSpecification cm() {
        return RestAssured.given()
                .baseUri(stack.opennms().getBaseUrlExternal().toString())
                .port(stack.opennms().getWebPort())
                .basePath("/opennms/rest/cm")
                .auth().preemptive().basic(OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD);
    }
}
