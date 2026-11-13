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
package org.opennms.smoketest;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.model.QueryResponse;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.model.resource.ResourceDTO;
import org.opennms.smoketest.containers.VictoriaMetricsContainer;
import org.opennms.smoketest.junit.ContainerRule;
import org.opennms.smoketest.stacks.OpenNMSProfile;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.KarafShell;
import org.opennms.smoketest.utils.KarafShellUtils;

/**
 * Verifies that the Prometheus RemoteWrite plugin in the release writes samples
 * to a remote-write compatible store and reads them back.
 */
public class PrometheusRemoteWritePluginIT {
    private static final String FEATURE = "opennms-plugins-prometheus-remotewrite";
    private static final String LOCALHOST_NODE = "selfmonitor:1";
    private static final String COLLECT_AND_PERSIST = "opennms:collect -p -n " + LOCALHOST_NODE
            + " org.opennms.netmgt.collectd.Jsr160Collector 127.0.0.1"
            + " port=18980 collection=jsr160 rrd-base-name=java ds-name=opennms-jvm friendly-name=opennms-jvm";

    public static final VictoriaMetricsContainer victoriaMetrics = new VictoriaMetricsContainer();

    // The default localhost node supplies the samples
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withOpenNMS(OpenNMSProfile.newBuilder()
                    .withInstallFeature(FEATURE)
                    .withFile("prometheus-remotewrite/org.opennms.plugins.tss.prometheus.cfg",
                            "etc/org.opennms.plugins.tss.prometheus.cfg")
                    .withFile("prometheus-remotewrite/timeseries.properties",
                            "etc/opennms.properties.d/timeseries.properties")
                    .build())
            .build());

    @ClassRule
    public static final RuleChain chain = RuleChain.outerRule(ContainerRule.of(victoriaMetrics)).around(stack);

    private final KarafShell karafShell = new KarafShell(stack.opennms().getSshAddress());

    @Before
    public void setUp() throws IOException, InterruptedException {
        KarafShellUtils.awaitHealthCheckSucceeded(stack.opennms());
    }

    @Test
    public void featureStartsWithoutKarFile() throws Exception {
        karafShell.checkFeature(FEATURE, "Started", Duration.ofSeconds(30));
    }

    @Test
    public void writesAndReadsSamples() {
        // An ad hoc collection supplies samples without a wait for the collectd schedule
        await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(5))
                .until(() -> {
                    karafShell.runCommandOnce(COLLECT_AND_PERSIST, output -> output.contains("Persisted collection"), false);
                    return getSeriesInStore();
                }, not(empty()));

        await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(5))
                .ignoreExceptions()
                .until(PrometheusRemoteWritePluginIT::countGraphAttributes, count -> count > 0);

        // More collections make sure that the query window contains samples
        await().atMost(Duration.ofMinutes(3)).pollInterval(Duration.ofSeconds(10))
                .ignoreExceptions()
                .until(() -> {
                    karafShell.runCommandOnce(COLLECT_AND_PERSIST, output -> output.contains("Persisted collection"), false);
                    return hasFiniteSample();
                });
    }

    private static List<Object> getSeriesInStore() {
        return given()
                .queryParam("match[]", "{resourceId!=\"\"}")
                .queryParam("start", "0")
                .get(victoriaMetrics.getBaseUrl() + "/api/v1/series")
                .then()
                .statusCode(200)
                .extract().jsonPath().getList("data");
    }

    private static boolean hasFiniteSample() {
        final ResourceDTO resource = stack.opennms().getRestClient().getResourcesForNode(LOCALHOST_NODE)
                .getChildren().getObjects().stream()
                .filter(child -> !child.getRrdGraphAttributes().isEmpty())
                .findFirst()
                .orElseThrow();

        final long now = System.currentTimeMillis();
        final QueryRequest request = new QueryRequest();
        request.setStart(now - Duration.ofMinutes(30).toMillis());
        request.setEnd(now);
        request.setStep(Duration.ofMinutes(1).toMillis());
        request.setSources(resource.getRrdGraphAttributes().keySet().stream()
                .map(attribute -> new Source(attribute, resource.getId(), attribute, null, false))
                .collect(Collectors.toList()));

        final QueryResponse response = stack.opennms().getRestClient().getMeasurements(request);
        return Arrays.stream(response.getColumns())
                .flatMapToDouble(column -> Arrays.stream(column.getList()))
                .anyMatch(Double::isFinite);
    }

    private static long countGraphAttributes() {
        return stack.opennms().getRestClient().getResourcesForNode(LOCALHOST_NODE)
                .getChildren().getObjects().stream()
                .mapToLong(resource -> resource.getRrdGraphAttributes().size())
                .sum();
    }
}
