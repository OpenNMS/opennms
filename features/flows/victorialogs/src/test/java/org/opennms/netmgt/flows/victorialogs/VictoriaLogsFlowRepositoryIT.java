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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import com.codahale.metrics.MetricRegistry;

/**
 * End-to-end ingestion against a real VictoriaLogs instance.
 *
 * <p>Worth contrasting with the Elasticsearch flow ITs, which have to download the OpenNMS Drift
 * plugin, stage it as a zip, and install it into the container through a bespoke container subclass.
 * There is no plugin here and no custom subclass — a stock image is enough, which is a large part of
 * the point of the exercise.
 *
 * <p>The retention period is set absurdly high on purpose. VictoriaLogs keeps seven days by default
 * and silently discards anything with a timestamp outside that window, which would quietly swallow
 * the back-dated fixtures these tests use and present as "queries return nothing".
 */
public class VictoriaLogsFlowRepositoryIT {

    private static final DockerImageName VL_IMAGE =
            DockerImageName.parse("victoriametrics/victoria-logs:v1.52.0");

    private static final int VL_PORT = 9428;

    /**
     * Points the test at an already-running VictoriaLogs instead of starting one. Testcontainers
     * cannot always negotiate with every local Docker flavour, and losing the ability to run these
     * assertions over an environment quirk would be the wrong trade.
     */
    private static final String EXTERNAL_URL_PROPERTY = "victorialogs.url";

    private static GenericContainer<?> victoriaLogs;
    private static String baseUrl;

    private VictoriaLogsClient client;
    private MetricRegistry metrics;
    private VictoriaLogsFlowRepository repository;

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
        metrics = new MetricRegistry();
        repository = new VictoriaLogsFlowRepository(metrics, client);
        // persist() only buffers; the flush thread is what sends, so it has to be running for these
        // tests to observe anything reaching VictoriaLogs. This mirrors what blueprint does with
        // init-method="start" once the backend is enabled.
        repository.setDisabled(false);
        repository.setBulkFlushMs(50);
        repository.start();
    }

    @After
    public void tearDown() {
        if (repository != null) {
            repository.stop();
        }
        if (client != null) {
            client.close();
        }
    }

    @Test
    public void containerIsHealthy() {
        assertTrue(client.isHealthy());
    }

    /**
     * The core acceptance criterion. The ingestion protocol reports that a request was accepted, not
     * that every line in it was stored, so the only trustworthy check is to reconcile what we sent
     * against what VictoriaLogs counted.
     */
    @Test
    public void persistedFlowsAreAllAccountedFor() throws Exception {
        final int count = 250;
        final IngestStats before = client.fetchIngestStats();

        repository.setBulkSize(count);
        repository.persist(flows(count));
        repository.stop();

        final IngestStats after = awaitIngestion(before.getRowsIngested() + count);

        assertEquals("every flow sent must be counted as ingested",
                count, after.getRowsIngested() - before.getRowsIngested());
        assertEquals("no flow may be dropped",
                0, after.getRowsDropped() - before.getRowsDropped());
        assertEquals("no line may fail to parse",
                0, after.getHttpErrors() - before.getHttpErrors());
    }

    /**
     * Flows must reach VictoriaLogs when the batch fills, without an explicit flush.
     */
    @Test
    public void bulkSizeTriggersAutomaticFlush() throws Exception {
        final IngestStats before = client.fetchIngestStats();

        repository.setBulkSize(10);
        repository.persist(flows(10));

        final IngestStats after = awaitIngestion(before.getRowsIngested() + 10);
        assertEquals(10, after.getRowsIngested() - before.getRowsIngested());
    }

    /**
     * Stream cardinality must not track flow volume. The fixtures vary source address, port and
     * conversation key — all high-cardinality — while the configured stream fields stay constant, so
     * a large batch should create at most a handful of streams. If this starts failing, a
     * per-flow-varying field has found its way into {@code _stream_fields}.
     */
    @Test
    public void streamCardinalityDoesNotTrackFlowVolume() throws Exception {
        final IngestStats before = client.fetchIngestStats();

        repository.setBulkSize(200);
        repository.persist(flows(200));
        repository.stop();

        final IngestStats after = awaitIngestion(before.getRowsIngested() + 200);
        // awaitIngestion returns rather than failing when its deadline expires, so without this the
        // whole test passes on a run where nothing reached VictoriaLogs: zero flows create zero
        // streams, and zero is comfortably under the bound. Every sibling test asserts an exact
        // delta and would fail loudly; this one was the exception.
        assertEquals("the flows must actually have been ingested for the bound to mean anything",
                200, after.getRowsIngested() - before.getRowsIngested());
        final long created = after.getStreamsCreated() - before.getStreamsCreated();
        assertTrue("200 flows should not create " + created + " streams", created <= 4);
    }

    /** Flows must be searchable by their dotted field names once ingested. */
    @Test
    public void ingestedFlowsAreQueryableByDottedFieldName() throws Exception {
        final String convoKey = "it-queryable-" + System.nanoTime();
        final IngestStats before = client.fetchIngestStats();

        repository.setBulkSize(1);
        repository.persist(Collections.singletonList(TestFlow.full().withConvoKey(convoKey)));

        awaitIngestion(before.getRowsIngested() + 1);

        final String body = query("\"netflow.convo_key\":=\"" + convoKey + "\"");
        assertTrue("expected the flow to be returned, got: " + body, body.contains(convoKey));
        assertTrue("expected dotted field names to survive ingestion, got: " + body,
                body.contains("netflow.bytes"));
    }

    /**
     * Clock-skewed exporters are a fact of life, which is why OpenNMS carries a clock-correction
     * field at all. VictoriaLogs discards timestamps beyond roughly now+2d, and the ingestion
     * response says nothing about it — the request is a plain success.
     *
     * <p>This also pins the counter semantics, which are easy to get wrong and expensive to get
     * wrong quietly: the rejected flow is counted under <em>both</em>
     * {@code vl_rows_ingested_total} and {@code vl_rows_dropped_total{reason="too_big_timestamp"}}.
     * "Ingested" means parsed into the pipeline, not stored. Reconciling a send count against
     * ingested alone would therefore report success for a flow that was thrown away.
     */
    @Test
    public void farFutureTimestampsAreDroppedButStillCountedAsIngested() throws Exception {
        final IngestStats before = client.fetchIngestStats();

        repository.setBulkSize(1);
        repository.persist(Collections.singletonList(
                TestFlow.full().withTimestamp(Instant.now().plus(Duration.ofDays(30)))));
        repository.stop();

        Thread.sleep(2_000);
        final IngestStats after = client.fetchIngestStats();

        assertEquals("the flow is accepted into the pipeline",
                1, after.getRowsIngested() - before.getRowsIngested());
        assertEquals("but discarded for having an out-of-range timestamp",
                1, after.getRowsDropped() - before.getRowsDropped());
        assertEquals("so nothing is actually stored",
                0, after.getRowsStored() - before.getRowsStored());
        assertEquals("and the transport reports no error at all",
                0, after.getHttpErrors() - before.getHttpErrors());

        // The consequence, stated rather than left implied: the repository counted a flow that
        // VictoriaLogs threw away. flowsPersisted means "handed to a request that was accepted", not
        // "stored", and nothing here reconciles the two -- fetchIngestStats() exists for that and has
        // no caller in the write path. Pinned so the day someone wires up reconciliation, this test
        // fails and tells them where to look.
        assertEquals("flowsPersisted counts the flow VictoriaLogs discarded",
                1, metrics.meter("flowsPersisted").getCount());
        assertEquals("and nothing is counted as dropped",
                0, metrics.meter("flowsDropped").getCount());
    }

    /** Polls until the expected ingestion count is reached; entries are not searchable instantly. */
    private IngestStats awaitIngestion(final long expectedRowsIngested) throws Exception {
        IngestStats stats = null;
        for (int i = 0; i < 60; i++) {
            stats = client.fetchIngestStats();
            if (stats.getRowsIngested() >= expectedRowsIngested) {
                // Ingested is not the same as flushed to a searchable part.
                Thread.sleep(1_000);
                return client.fetchIngestStats();
            }
            Thread.sleep(500);
        }
        return stats;
    }

    private String query(final String logsQl) throws Exception {
        final HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/select/logsql/query"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "query=" + java.net.URLEncoder.encode(logsQl, java.nio.charset.StandardCharsets.UTF_8)))
                .build();
        // try-with-resources: Java 21's HttpClient is AutoCloseable and this helper runs per
        // assertion, so leaving each one to the collector leaks a selector thread and an executor
        // per call -- in a class whose teardown javadoc makes a point of releasing exactly those.
        final HttpResponse<String> response;
        try (final HttpClient http = HttpClient.newHttpClient()) {
            response = http.send(request, HttpResponse.BodyHandlers.ofString());
        }
        assertEquals(200, response.statusCode());
        return response.body();
    }

    /**
     * Flows varying in every high-cardinality dimension, constant in the stream dimensions.
     *
     * <p>Addresses and ports vary as well as the conversation key. That was not previously possible
     * — the fixture had no setters for them — so the claim in this javadoc was true of the comment
     * and not of the data, and {@link #streamCardinalityDoesNotTrackFlowVolume} could not have
     * detected {@code netflow.src_addr} or {@code netflow.src_port} finding their way into
     * {@code _stream_fields}, which is the single most likely way to make VictoriaLogs perform
     * badly and exactly what that test exists to catch.
     */
    private static List<TestFlow> flows(final int count) {
        final List<TestFlow> flows = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            flows.add(TestFlow.full()
                    .withConvoKey("convo-" + i)
                    .withAddresses("10.9." + (i / 256) + "." + (i % 256), "10.8.0." + (i % 256))
                    .withPorts(30000 + i, 40000 + i));
        }
        return flows;
    }
}
