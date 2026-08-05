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

package org.opennms.netmgt.flows.victorialogs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.netmgt.flows.api.Directional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import com.google.common.collect.Table;

/**
 * Numerical verification of the LogsQL reproduction of {@code proportional_sum}.
 *
 * <p>This is the test the whole module hangs on. The Drift plugin exists because Elasticsearch
 * cannot spread a flow's bytes across the buckets it overlaps; if VictoriaLogs cannot be made to do
 * the same arithmetic, the flow query API cannot be served from it and the exercise fails. The
 * expected values below are computed by hand from the aggregation's definition, so they are an
 * independent oracle rather than a recording of whatever the implementation happens to produce.
 */
public class ProportionalSumIT {

    private static final DockerImageName VL_IMAGE =
            DockerImageName.parse("victoriametrics/victoria-logs:v1.52.0");
    private static final int VL_PORT = 9428;
    private static final String EXTERNAL_URL_PROPERTY = "victorialogs.url";

    private static final long MINUTE = 60_000L;
    private static final long STEP = 5 * MINUTE;
    /**
     * Must be at least as long as the longest fixture flow. This is not a value the platform
     * enforces -- see {@link ProportionalSumQuery} -- so it is chosen here to cover the fixtures.
     */
    private static final long MAX_FLOW_DURATION = 5 * MINUTE;

    /** 2020-09-13T12:20:00Z — a five-minute bucket boundary, so buckets align at :20, :25, :30. */
    private static final long WINDOW_START = 1_599_999_600_000L;
    private static final long WINDOW_END = WINDOW_START + 30 * MINUTE;

    private static GenericContainer<?> victoriaLogs;
    private static String baseUrl;

    private VictoriaLogsClient client;
    private ProportionalSeriesService series;

    @BeforeClass
    public static void startContainer() {
        final String external = System.getProperty(EXTERNAL_URL_PROPERTY);
        if (external != null && !external.isEmpty()) {
            baseUrl = external;
            return;
        }
        victoriaLogs = new GenericContainer<>(VL_IMAGE)
                .withExposedPorts(VL_PORT)
                .withCommand("-storageDataPath=/victoria-logs-data", "-retentionPeriod=100y")
                .waitingFor(Wait.forHttp("/health").forPort(VL_PORT).forStatusCode(200));
        victoriaLogs.start();
        baseUrl = "http://" + victoriaLogs.getHost() + ":" + victoriaLogs.getMappedPort(VL_PORT);
    }

    @AfterClass
    public static void stopContainer() {
        if (victoriaLogs != null) {
            victoriaLogs.stop();
        }
    }

    @Before
    public void setUp() {
        final VictoriaLogsClientConfig config = new VictoriaLogsClientConfig();
        config.setUrl(baseUrl);
        client = new VictoriaLogsClient(config);
        series = new ProportionalSeriesService(client, MAX_FLOW_DURATION);
    }

    /** Releases the client's selector thread and executor; that is what close() exists for. */
    @After
    public void tearDown() {
        if (client != null) {
            client.close();
        }
    }

    /**
     * A flow crossing a bucket boundary must be split in proportion to the overlap.
     *
     * <p>1000 bytes over 12:24:00–12:29:00 (five minutes). The 12:20 bucket covers one of those
     * minutes and the 12:25 bucket the other four, so the bytes divide 1:4 into 200 and 800.
     */
    @Test
    public void splitsBytesAcrossBucketsInProportionToOverlap() throws Exception {
        final String app = ingest("split", 1000, minutes(4), minutes(9));

        final Table<Directional<String>, Long, Double> result =
                series.applicationSeries(appFilter(app), WINDOW_START, WINDOW_END, STEP);

        final Directional<String> key = new Directional<>(app, true);
        assertEquals(200.0d, result.get(key, WINDOW_START), 0.001d);
        assertEquals(800.0d, result.get(key, WINDOW_START + STEP), 0.001d);
        assertEquals("the flow must not leak into any other bucket", 2, result.row(key).size());
    }

    /** Bytes must be conserved: the split may redistribute them but never create or lose any. */
    @Test
    public void conservesTotalBytes() throws Exception {
        final String app = ingest("conserve", 3333, minutes(4), minutes(9));

        final Table<Directional<String>, Long, Double> result =
                series.applicationSeries(appFilter(app), WINDOW_START, WINDOW_END, STEP);

        final double total = result.values().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(3333.0d, total, 0.001d);
    }

    /** A flow inside one bucket contributes all of its bytes to that bucket and nothing elsewhere. */
    @Test
    public void flowContainedInOneBucketIsNotSplit() throws Exception {
        final String app = ingest("contained", 600, minutes(10), minutes(11));

        final Table<Directional<String>, Long, Double> result =
                series.applicationSeries(appFilter(app), WINDOW_START, WINDOW_END, STEP);

        final Directional<String> key = new Directional<>(app, true);
        assertEquals(1, result.row(key).size());
        assertEquals(600.0d, result.get(key, WINDOW_START + 2 * STEP), 0.001d);
    }

    /**
     * A zero-duration flow has no interval to spread over. Dividing by its duration would divide by
     * zero; Elasticsearch instead attributes it wholly to the bucket containing its start, and so
     * must this.
     */
    @Test
    public void zeroDurationFlowLandsWhollyInItsOwnBucket() throws Exception {
        final String app = ingest("instant", 50, minutes(16), minutes(16));

        final Table<Directional<String>, Long, Double> result =
                series.applicationSeries(appFilter(app), WINDOW_START, WINDOW_END, STEP);

        final Directional<String> key = new Directional<>(app, true);
        assertEquals(1, result.row(key).size());
        assertEquals(50.0d, result.get(key, WINDOW_START + 3 * STEP), 0.001d);
    }

    /** Ingress and egress are distinct series, not a single combined one. */
    @Test
    public void separatesIngressFromEgress() throws Exception {
        final String app = "directional-" + System.nanoTime();
        send(flow(app, "ingress", 1000, minutes(4), minutes(9)));
        send(flow(app, "egress", 400, minutes(4), minutes(9)));
        settle();

        final Table<Directional<String>, Long, Double> result =
                series.applicationSeries(appFilter(app), WINDOW_START, WINDOW_END, STEP);

        assertEquals(200.0d, result.get(new Directional<>(app, true), WINDOW_START), 0.001d);
        assertEquals(80.0d, result.get(new Directional<>(app, false), WINDOW_START), 0.001d);
        assertEquals(800.0d, result.get(new Directional<>(app, true), WINDOW_START + STEP), 0.001d);
        assertEquals(320.0d, result.get(new Directional<>(app, false), WINDOW_START + STEP), 0.001d);
    }

    /**
     * Widening the query must not widen the per-flow fan-out. The same flow answered over a window
     * 48 times larger must produce the same numbers, which is what makes the approach usable for
     * real time ranges rather than only toy ones.
     */
    @Test
    public void resultsAreIndependentOfWindowWidth() throws Exception {
        final String app = ingest("wide", 1000, minutes(4), minutes(9));

        final Table<Directional<String>, Long, Double> narrow =
                series.applicationSeries(appFilter(app), WINDOW_START, WINDOW_END, STEP);
        final Table<Directional<String>, Long, Double> wide =
                series.applicationSeries(appFilter(app), WINDOW_START, WINDOW_START + 24 * 60 * MINUTE, STEP);

        final Directional<String> key = new Directional<>(app, true);
        // With a delta, like every other numeric assertion here. Double.equals is bit-exact, and the
        // two queries differ in their end bound and so in their summation order -- a legitimate
        // one-ULP difference would fail. Unboxing also makes a both-null result fail loudly rather
        // than comparing equal, which the object overload would not.
        assertEquals(narrow.get(key, WINDOW_START).doubleValue(),
                wide.get(key, WINDOW_START).doubleValue(), 0.001d);
        assertEquals(narrow.get(key, WINDOW_START + STEP).doubleValue(),
                wide.get(key, WINDOW_START + STEP).doubleValue(), 0.001d);
        assertEquals(2, wide.row(key).size());
    }

    /** Finer buckets must still conserve bytes and produce more, smaller slices. */
    @Test
    public void handlesStepsFinerThanTheFlow() throws Exception {
        final String app = ingest("fine", 1000, minutes(4), minutes(9));

        final Table<Directional<String>, Long, Double> result =
                series.applicationSeries(appFilter(app), WINDOW_START, WINDOW_END, MINUTE);

        final Directional<String> key = new Directional<>(app, true);
        // Six buckets, not five. The flow ends exactly on a boundary, so it reaches the bucket
        // beginning at minute 9 without occupying any of it, and that bucket is reported as zero
        // rather than left out -- which is what Elasticsearch does, and the more useful answer: a
        // missing cell becomes NaN once rows are aligned and draws a gap, where zero is what
        // actually happened. Only the trailing bucket is affected; the leading boundary is not,
        // because a flow starting on a boundary does occupy the bucket it starts in.
        final List<Double> values = new ArrayList<>(new TreeMap<>(result.row(key)).values());
        assertEquals("a 5-minute flow at 1-minute resolution covers 5 buckets, plus the boundary "
                + "it ends on", 6, values.size());
        values.subList(0, 5).forEach(v -> assertEquals(200.0d, v, 0.001d));
        assertEquals("the bucket the flow ends on has no overlap with it",
                0.0d, values.get(5), 0.001d);
        assertEquals("and byte conservation is unaffected by the extra bucket",
                1000.0d, values.stream().mapToDouble(Double::doubleValue).sum(), 0.001d);
    }

    /**
     * Documents the one way this can be wrong, so that it is a known limitation rather than a
     * surprise.
     *
     * <p>The per-flow fan-out is sized from {@code maxFlowDurationMs}. A flow longer than that
     * reaches buckets the fan-out does not cover, and the bytes belonging to those buckets are not
     * counted at all — the total comes out low, with no error anywhere. Since nothing in OpenNMS
     * actually caps flow duration, the setting must be at least the exporters' active timeout.
     */
    @Test
    public void flowsLongerThanTheConfiguredMaximumAreUndercounted() throws Exception {
        final String app = ingest("overlong", 1000, minutes(0), minutes(20));

        // Deliberately under-sized: the flow is 20 minutes, this allows for 5.
        final ProportionalSeriesService undersized =
                new ProportionalSeriesService(client, 5 * MINUTE);
        final Table<Directional<String>, Long, Double> result =
                undersized.applicationSeries(appFilter(app), WINDOW_START, WINDOW_END, STEP);

        final Directional<String> key = new Directional<>(app, true);
        final double counted = result.row(key).values().stream()
                .mapToDouble(Double::doubleValue).sum();
        // The exact figure, not merely "less than 1000". A bound that total loss also satisfies
        // cannot tell "undercounts the tail" from "dropped the flow", which are very different
        // failures -- and this test exists to document precisely which one happens.
        // 2 buckets of fan-out at a 5-minute step, each covering 5 of the flow's 20 minutes:
        // 1000 * 5/20 twice.
        assertEquals("an over-long flow is silently undercounted, not rejected",
                500.0d, counted, 0.001d);

        // Sized correctly, the same flow is attributed in full.
        final ProportionalSeriesService sized =
                new ProportionalSeriesService(client, 20 * MINUTE);
        final double full = sized.applicationSeries(appFilter(app), WINDOW_START, WINDOW_END, STEP)
                .row(key).values().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(1000.0d, full, 0.001d);
    }

    /**
     * A flow that began before the window still contributes the share that falls inside it.
     *
     * <p>The unroll index is relative to the flow's own first bucket, which for such a flow lies
     * before the window start. Bounding the fan-out by the width of the window rather than by how
     * many buckets a flow can span therefore drops the flow entirely — and the narrower the window,
     * the more it drops. Here the window is a single bucket wide, which is the worst case.
     *
     * <p>1000 bytes spread over five minutes, of which the last two fall inside the window, so 400
     * bytes are attributable.
     */
    @Test
    public void attributesFlowsThatStartedBeforeTheWindow() throws Exception {
        final String app = ingest("pre-window", 1000, minutes(-3), minutes(2));

        final Table<Directional<String>, Long, Double> result =
                series.applicationSeries(appFilter(app), WINDOW_START, WINDOW_START + STEP, STEP);

        final Directional<String> key = new Directional<>(app, true);
        final double counted = result.row(key).values().stream()
                .mapToDouble(Double::doubleValue).sum();
        assertEquals(400.0d, counted, 0.001d);
    }

    /**
     * Flows whose direction was never determined must not break the series.
     *
     * <p>{@code Directional} is a boolean, so anything that is not ingress shares a row key with
     * egress. Two flows in one bucket then produce two values for the same cell, which an immutable
     * table rejects outright — turning a routine case into a failed query rather than a number.
     *
     * <p>The assertion is on conservation, not on placement: <em>which</em> row an unknown-direction
     * flow ends up in is not settled here. Elasticsearch resolves it from the SNMP interface the
     * query filtered on, which is context this service does not have — see
     * {@link ProportionalSeriesService}.
     */
    @Test
    public void toleratesUnknownDirectionAlongsideEgress() throws Exception {
        final String app = "unknown-dir-" + System.nanoTime();
        send(flow(app, "egress", 100, minutes(4), minutes(9)));
        send(flow(app, "unknown", 200, minutes(4), minutes(9)));
        settle();

        final Table<Directional<String>, Long, Double> result =
                series.applicationSeries(appFilter(app), WINDOW_START, WINDOW_END, STEP);

        final double total = result.values().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals("bytes must be conserved however unknown-direction flows are attributed",
                300.0d, total, 0.001d);
    }

    private static String appFilter(final String app) {
        return "\"netflow.application\":=\"" + app + "\"";
    }

    private static long minutes(final long m) {
        return WINDOW_START + m * MINUTE;
    }

    private String ingest(final String prefix, final long bytes, final long from, final long to)
            throws Exception {
        final String app = prefix + "-" + System.nanoTime();
        send(flow(app, "ingress", bytes, from, to));
        settle();
        return app;
    }

    private static String flow(final String app, final String direction, final long bytes,
                               final long from, final long to) {
        return "{\"_time\":\"" + Instant.ofEpochMilli(from) + "\""
                + ",\"netflow.application\":\"" + app + "\""
                + ",\"netflow.direction\":\"" + direction + "\""
                + ",\"netflow.bytes\":" + bytes
                + ",\"netflow.delta_switched\":" + from
                + ",\"netflow.last_switched\":" + to
                + ",\"location\":\"Default\"}";
    }

    private void send(final String jsonLine) throws Exception {
        client.ingest(jsonLine + "\n");
    }

    /** Ingested rows are not searchable the instant they are accepted. */
    private void settle() throws Exception {
        Thread.sleep(2_000);
    }
}
