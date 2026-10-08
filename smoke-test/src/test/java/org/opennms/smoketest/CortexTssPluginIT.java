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

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.stacks.TimeSeriesStrategy;
import org.opennms.smoketest.utils.CortexTestUtils;
import org.opennms.smoketest.utils.KarafShell;
import org.opennms.smoketest.utils.KarafShellUtils;
import org.opennms.smoketest.utils.RestClient;
import org.opennms.smoketest.utils.TimeSeriesValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Comprehensive smoke test for the Cortex TSS plugin with a Thanos backend.
 *
 * <p>Validates the full data pipeline: write path (OpenNMS → Thanos), read path
 * (Thanos → OpenNMS measurements API), resource discovery, meta tags, metric
 * sanitization, label ordering, label values discovery, and plugin health.</p>
 *
 * <p>Strategy-agnostic tests (resource tree, measurements API, aggregation) use
 * {@link TimeSeriesValidationUtils} and can be reused with other TSS backends.</p>
 */
@org.junit.experimental.categories.Category(org.opennms.smoketest.junit.FlakyTests.class)
public class CortexTssPluginIT {

    private static final Logger LOG = LoggerFactory.getLogger(CortexTssPluginIT.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern PROMETHEUS_METRIC_NAME = Pattern.compile("^[a-zA-Z_:][a-zA-Z0-9_:]*$");
    private static final Pattern PROMETHEUS_LABEL_NAME = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    @ClassRule
    public static OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withTimeSeriesStrategy(TimeSeriesStrategy.INTEGRATION)
            .withOpenNMS(org.opennms.smoketest.stacks.OpenNMSProfile.newBuilder()
                    .withFile("empty-discovery-configuration.xml", "etc/discovery-configuration.xml")
                    .withInstallFeature("opennms-plugins-cortex-tss", "opennms-cortex-tss-plugin", CortexTestUtils.resolveKarFile())
                    .build())
            .build());

    private static volatile boolean dataReady = false;

    protected KarafShell karafShell = new KarafShell(stack.opennms().getSshAddress());
    protected RestClient restClient;
    protected URL thanosQueryUrl;

    @Before
    public void setUp() throws Exception {
        KarafShellUtils.awaitHealthCheckSucceeded(stack.opennms());
        restClient = stack.opennms().getRestClient();
        thanosQueryUrl = stack.thanosQuery().getExternalQueryUrl();

        if (!dataReady) {
            // Wait for data to appear in Thanos (self-monitoring metrics) — runs once across all tests
            LOG.info("Waiting for data to appear in Thanos...");
            await("data in Thanos")
                    .atMost(5, MINUTES)
                    .pollInterval(15, SECONDS)
                    .until(() -> {
                        try {
                            JsonNode resp = queryThanos("/api/v1/label/resourceId/values");
                            return resp.get("data").size() > 0;
                        } catch (Exception e) {
                            return false;
                        }
                    });
            dataReady = true;
            LOG.info("Data is flowing into Thanos.");
        }
    }

    @Test
    public void testPluginFeatureStarted() throws Exception {
        karafShell.checkFeature("opennms-plugins-cortex-tss", "Started", Duration.ofSeconds(30));
    }

    @Test
    public void testTimeseriesApiFeature() throws Exception {
        karafShell.checkFeature("opennms-timeseries-api", "Started|Uninstalled", Duration.ofSeconds(30));
    }

    @Test
    public void testResourceTreePopulated() {
        TimeSeriesValidationUtils.validateResourceTree(restClient, "selfmonitor:1");
    }

    @Test
    public void testMeasurementsAverage() {
        TimeSeriesValidationUtils.validateMeasurements(restClient,
                "node[selfmonitor:1].interfaceSnmp[opennms-jvm]",
                "HeapUsageUsed", "AVERAGE");
    }

    @Test
    public void testMeasurementsMax() {
        TimeSeriesValidationUtils.validateMeasurements(restClient,
                "node[selfmonitor:1].interfaceSnmp[opennms-jvm]",
                "HeapUsageUsed", "MAX");
    }

    @Test
    public void testMeasurementsMin() {
        TimeSeriesValidationUtils.validateMeasurements(restClient,
                "node[selfmonitor:1].interfaceSnmp[opennms-jvm]",
                "HeapUsageUsed", "MIN");
    }

    @Test
    public void testMeasurementsMetadata() {
        TimeSeriesValidationUtils.validateMeasurementsMetadata(restClient,
                "node[selfmonitor:1].nodeSnmp[]", "OnmsEventCount");
    }

    @Test
    public void testMetricsFlowingIntoThanos() throws Exception {
        JsonNode resp = queryThanos("/api/v1/label/__name__/values");
        int metricCount = resp.get("data").size();
        assertThat("Should have metrics in Thanos", metricCount, greaterThan(0));
        LOG.info("Thanos has {} unique metric names", metricCount);
    }

    @Test
    public void testResourceIdsWritten() throws Exception {
        JsonNode resp = queryThanos("/api/v1/label/resourceId/values");
        int count = resp.get("data").size();
        assertThat("Should have resourceIds in Thanos", count, greaterThan(0));
        LOG.info("Thanos has {} unique resourceIds", count);
    }

    @Test
    public void testMultipleResourceTypesPresent() throws Exception {
        JsonNode resp = queryThanos("/api/v1/label/resourceId/values");
        Set<String> prefixes = new HashSet<>();
        for (JsonNode rid : resp.get("data")) {
            String val = rid.asText();
            if (val.contains("/")) {
                prefixes.add(val.substring(0, val.indexOf('/')));
            }
        }
        assertThat("Should have snmp resources", prefixes, hasItem("snmp"));
        assertThat("Should have multiple resource types", prefixes.size(), greaterThan(1));
        LOG.info("Resource types found: {}", prefixes);
    }

    @Test
    public void testMetaTagsWrittenAsLabels() throws Exception {
        JsonNode resp = queryThanos("/api/v1/labels");
        List<String> labels = new ArrayList<>();
        for (JsonNode label : resp.get("data")) {
            labels.add(label.asText());
        }
        assertTrue("Should have 'node' label", labels.contains("node"));
        assertTrue("Should have 'location' label", labels.contains("location"));
        assertTrue("Should have 'mtype' label", labels.contains("mtype"));
    }

    @Test
    public void testMetaTagValuesPopulated() throws Exception {
        JsonNode nodeVals = queryThanos("/api/v1/label/node/values").get("data");
        JsonNode locVals = queryThanos("/api/v1/label/location/values").get("data");
        assertThat("Should have node label values", nodeVals.size(), greaterThan(0));
        assertThat("Should have location label values", locVals.size(), greaterThan(0));
    }

    @Test
    public void testMetaTagsOnSeries() throws Exception {
        JsonNode resp = queryThanos("/api/v1/series?match[]={resourceId=~\"snmp/.*opennms-jvm.*\"}");
        JsonNode series = resp.get("data");
        assertThat("Should have series", series.size(), greaterThan(0));
        for (int i = 0; i < Math.min(10, series.size()); i++) {
            JsonNode s = series.get(i);
            assertTrue("Series should have 'node' tag: " + s, s.has("node"));
            assertTrue("Series should have 'location' tag: " + s, s.has("location"));
            assertTrue("Series should have 'mtype' tag: " + s, s.has("mtype"));
        }
    }

    @Test
    public void testAllMetricNamesPrometheusValid() throws Exception {
        JsonNode resp = queryThanos("/api/v1/label/__name__/values");
        for (JsonNode name : resp.get("data")) {
            String metricName = name.asText();
            assertTrue("Metric name should match Prometheus pattern: " + metricName,
                    PROMETHEUS_METRIC_NAME.matcher(metricName).matches());
        }
    }

    @Test
    public void testAllLabelNamesPrometheusValid() throws Exception {
        JsonNode resp = queryThanos("/api/v1/series?match[]={resourceId=~\"snmp/.*\"}");
        Set<String> allLabels = new HashSet<>();
        JsonNode series = resp.get("data");
        for (int i = 0; i < Math.min(50, series.size()); i++) {
            series.get(i).fieldNames().forEachRemaining(allLabels::add);
        }
        for (String label : allLabels) {
            assertTrue("Label name should match Prometheus pattern: " + label,
                    PROMETHEUS_LABEL_NAME.matcher(label).matches());
        }
    }

    @Test
    public void testLabelsLexicographicallyOrdered() throws Exception {
        // Prometheus remote write requires labels to be sorted. We verify this by querying
        // stored series and checking that every series' label names are in sorted order.
        // Note: Thanos/Prometheus typically return labels sorted regardless of write order,
        // so this is a sanity check rather than a strict write-path validation.
        JsonNode resp = queryThanos("/api/v1/series?match[]={resourceId=~\"snmp/.*\"}");
        JsonNode series = resp.get("data");
        assertThat("Should have series to check", series.size(), greaterThan(0));
        int checked = 0;
        for (int i = 0; i < Math.min(20, series.size()); i++) {
            List<String> keys = new ArrayList<>();
            series.get(i).fieldNames().forEachRemaining(keys::add);
            List<String> sorted = new ArrayList<>(keys);
            sorted.sort(String::compareTo);
            assertEquals("Labels should be lexicographically ordered for series " + i, sorted, keys);
            checked++;
        }
        LOG.info("Checked label ordering on {} series", checked);
    }

    @Test
    public void testSeriesApiWildcard() throws Exception {
        JsonNode resp = queryThanos("/api/v1/series?match[]={resourceId=~\"^snmp/.*$\"}");
        assertThat("Should return series", resp.get("data").size(), greaterThan(0));
    }

    @Test
    public void testSeriesContainExpectedFields() throws Exception {
        JsonNode resp = queryThanos("/api/v1/series?match[]={resourceId=~\"^snmp/.*$\"}");
        JsonNode first = resp.get("data").get(0);
        assertTrue("Series should have __name__", first.has("__name__"));
        assertTrue("Series should have resourceId", first.has("resourceId"));
        assertTrue("Series should have mtype", first.has("mtype"));
    }

    @Test
    public void testLabelValuesApi() throws Exception {
        JsonNode resp = queryThanos("/api/v1/label/resourceId/values");
        assertEquals("success", resp.get("status").asText());
        assertThat("Should return resourceIds", resp.get("data").size(), greaterThan(0));
    }

    @Test
    public void testLabelValuesFilteredByMatch() throws Exception {
        JsonNode resp = queryThanos("/api/v1/label/resourceId/values?match[]={resourceId=~\"^snmp/.*$\"}");
        JsonNode data = resp.get("data");
        assertThat("Should return filtered resourceIds", data.size(), greaterThan(0));
        for (JsonNode rid : data) {
            assertTrue("Filtered resourceId should start with 'snmp/': " + rid.asText(),
                    rid.asText().startsWith("snmp/"));
        }
    }

    @Test
    public void testTwoPhaseDiscoveryMatchesWildcard() throws Exception {
        // Old approach: wildcard /series
        JsonNode oldResp = queryThanos("/api/v1/series?match[]={resourceId=~\"^snmp/.*$\"}");
        Set<String> oldRids = new HashSet<>();
        for (JsonNode s : oldResp.get("data")) {
            oldRids.add(s.get("resourceId").asText());
        }

        // New approach: label values + batched /series
        JsonNode lvResp = queryThanos("/api/v1/label/resourceId/values?match[]={resourceId=~\"^snmp/.*$\"}");
        Set<String> newRids = new HashSet<>();
        for (JsonNode rid : lvResp.get("data")) {
            newRids.add(rid.asText());
        }

        // Use containsAll in both directions rather than assertEquals to tolerate
        // metrics written between the two sequential queries (eventual consistency)
        assertTrue("Label values should contain all /series resourceIds",
                newRids.containsAll(oldRids));
        assertTrue("/series resourceIds should contain all label values",
                oldRids.containsAll(newRids));
        LOG.info("Two-phase discovery matches: {} resourceIds", oldRids.size());
    }

    @Test
    public void testMtypeValuesValid() throws Exception {
        JsonNode resp = queryThanos("/api/v1/label/mtype/values");
        JsonNode data = resp.get("data");
        assertThat("Should have at least one mtype value", data.size(), greaterThan(0));
        Set<String> knownTypes = Set.of("gauge", "counter", "count");
        Set<String> actualTypes = new HashSet<>();
        for (JsonNode mtype : data) {
            assertTrue("mtype should not be empty", !mtype.asText().isEmpty());
            actualTypes.add(mtype.asText());
        }
        // Verify at least one known type is present (don't fail on new types)
        assertTrue("Should contain at least one known mtype (gauge, counter, count)",
                actualTypes.stream().anyMatch(knownTypes::contains));
    }

    @Test
    public void testSeriesIntegrity() throws Exception {
        JsonNode resp = queryThanos("/api/v1/series?match[]={resourceId=~\"snmp/.*\"}");
        JsonNode series = resp.get("data");
        int checked = 0;
        for (int i = 0; i < Math.min(100, series.size()); i++) {
            JsonNode s = series.get(i);
            assertTrue("Series should have __name__", s.has("__name__") && !s.get("__name__").asText().isEmpty());
            assertTrue("Series should have resourceId", s.has("resourceId") && !s.get("resourceId").asText().isEmpty());
            checked++;
        }
        LOG.info("Series integrity check passed for {} series", checked);
    }

    @Test
    public void testResourceIdConsistency() throws Exception {
        // Compare resourceIds from label values API vs /series
        JsonNode lvResp = queryThanos("/api/v1/label/resourceId/values");
        Set<String> lvRids = new HashSet<>();
        for (JsonNode rid : lvResp.get("data")) {
            lvRids.add(rid.asText());
        }

        JsonNode seriesResp = queryThanos("/api/v1/series?match[]={resourceId=~\".+\"}");
        Set<String> seriesRids = new HashSet<>();
        for (JsonNode s : seriesResp.get("data")) {
            seriesRids.add(s.get("resourceId").asText());
        }

        // Use containsAll in both directions rather than assertEquals to tolerate
        // metrics written between the two sequential queries (eventual consistency)
        assertTrue("Label values should contain all /series resourceIds",
                lvRids.containsAll(seriesRids));
        assertTrue("/series resourceIds should contain all label values",
                seriesRids.containsAll(lvRids));
    }

    @Test
    public void testRangeQueryReturnsData() throws Exception {
        long now = System.currentTimeMillis() / 1000;
        long fiveMinAgo = now - 300;
        String url = String.format("/api/v1/query_range?query={__name__=~\".+\",resourceId=~\"snmp/.*opennms-jvm.*\"}&start=%d&end=%d&step=60",
                fiveMinAgo, now);
        JsonNode resp = queryThanos(url);
        JsonNode results = resp.get("data").get("result");
        assertThat("Range query should return series", results.size(), greaterThan(0));
    }

    @Test
    public void testExactResourceIdQuery() throws Exception {
        // Get a specific resourceId
        JsonNode lvResp = queryThanos("/api/v1/label/resourceId/values");
        String targetRid = null;
        for (JsonNode rid : lvResp.get("data")) {
            if (rid.asText().startsWith("snmp/") && rid.asText().contains("opennms-jvm")) {
                targetRid = rid.asText();
                break;
            }
        }
        if (targetRid == null) {
            targetRid = lvResp.get("data").get(0).asText();
        }

        long now = System.currentTimeMillis() / 1000;
        String url = String.format("/api/v1/query_range?query={resourceId=\"%s\"}&start=%d&end=%d&step=60",
                targetRid, now - 300, now);
        JsonNode resp = queryThanos(url);
        JsonNode results = resp.get("data").get("result");
        assertThat("Exact match query should return series", results.size(), greaterThan(0));

        // Verify all returned series have the exact resourceId
        for (JsonNode r : results) {
            assertEquals("All series should match exact resourceId",
                    targetRid, r.get("metric").get("resourceId").asText());
        }
    }

    private JsonNode queryThanos(String path) throws Exception {
        String urlStr = thanosQueryUrl.toString() + path;
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setConnectTimeout(30_000);
        conn.setReadTimeout(30_000);
        try {
            int code = conn.getResponseCode();
            if (code != 200) {
                String error = "";
                try (InputStream es = conn.getErrorStream()) {
                    if (es != null) {
                        error = new String(es.readAllBytes());
                    }
                }
                throw new IOException("Thanos query failed (HTTP " + code + "): " + error);
            }
            try (InputStream is = conn.getInputStream()) {
                return MAPPER.readTree(is);
            }
        } finally {
            conn.disconnect();
        }
    }
}
