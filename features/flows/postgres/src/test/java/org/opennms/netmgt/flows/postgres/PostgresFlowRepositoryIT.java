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
package org.opennms.netmgt.flows.postgres;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.integration.api.v1.flows.Flow;
import org.opennms.netmgt.flows.api.Conversation;
import org.opennms.netmgt.flows.api.Directional;
import org.opennms.netmgt.flows.api.Host;
import org.opennms.netmgt.flows.api.TrafficSummary;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.flows.filter.api.SnmpInterfaceIdFilter;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Table;

/**
 * End-to-end verification of {@link PostgresFlowRepository} and {@link PostgresFlowQueryService}
 * against a real PostgreSQL. By default it starts a {@code postgres:15} Testcontainer; pass
 * {@code -Dit.postgres.jdbcUrl=...} (with optional {@code it.postgres.user}/{@code it.postgres.password})
 * to run against an already-running instance and skip Testcontainers entirely.
 */
public class PostgresFlowRepositoryIT {
    private static final long BASE = 1700000000000L;
    private static final long WINDOW = 1000L;
    private static PostgreSQLContainer<?> POSTGRES;
    private static PGSimpleDataSource DATA_SOURCE;
    private static String JDBC_URL;
    private static String JDBC_USER;
    private static String JDBC_PASSWORD;
    private static PostgresFlowRepository repository;
    private static PostgresFlowQueryService queryService;
    private static JdbcTemplate jdbc;

    @BeforeClass
    public static void startContainer() throws Exception {
        String password;
        String user;
        String url;
        String externalUrl = System.getProperty("it.postgres.jdbcUrl");
        if (externalUrl != null && !externalUrl.isEmpty()) {
            url = externalUrl;
            user = System.getProperty("it.postgres.user", "postgres");
            password = System.getProperty("it.postgres.password", "postgres");
        } else {
            Assume.assumeTrue("No Docker/podman runtime available; skipping live-SQL IT.", DockerClientFactory.instance().isDockerAvailable());
            POSTGRES = new PostgreSQLContainer<>("postgres:15");
            POSTGRES.start();
            url = POSTGRES.getJdbcUrl();
            user = POSTGRES.getUsername();
            password = POSTGRES.getPassword();
        }
        JDBC_URL = url;
        JDBC_USER = user;
        JDBC_PASSWORD = password;
        DATA_SOURCE = new PGSimpleDataSource();
        DATA_SOURCE.setUrl(url);
        DATA_SOURCE.setUser(user);
        DATA_SOURCE.setPassword(password);
        // Exercise the production write path: batched INSERTs are rewritten into a single multi-row
        // statement, which requires the cast-free (plain-placeholder) INSERT + Types.OTHER binding.
        DATA_SOURCE.setReWriteBatchedInserts(true);
        jdbc = new JdbcTemplate(DATA_SOURCE);
        repository = new PostgresFlowRepository(new MetricRegistry());
        repository.setDataSource(DATA_SOURCE);
        repository.setRunSchemaChangelog(true);
        repository.setBatchSize(10);
        repository.setFlushIntervalMs(50L);
        repository.start();
        queryService = new PostgresFlowQueryService();
        queryService.setDataSource(DATA_SOURCE);
        queryService.start();
    }

    @AfterClass
    public static void stopContainer() {
        if (queryService != null) {
            queryService.stop();
        }
        if (repository != null) {
            repository.stop();
        }
        if (POSTGRES != null) {
            POSTGRES.stop();
        }
    }

    @After
    public void truncate() {
        if (jdbc != null) {
            jdbc.execute("TRUNCATE TABLE flow");
        }
    }

    private static List<Filter> window() {
        return Collections.singletonList(new TimeRangeFilter(BASE, BASE + WINDOW));
    }

    private void insert(String application, String direction, long delta, long last, long bytes, Double sampling, String src, String dst, String convoKey) {
        jdbc.update("INSERT INTO flow (flow_ts, delta_switched, last_switched, bytes, sampling_interval, direction, application, src_addr, dst_addr, convo_key, document) VALUES (to_timestamp(?/1000.0), ?, ?, ?, ?, ?, ?, ?::inet, ?::inet, ?, '{}'::jsonb)",
                last, delta, last, bytes, sampling, direction, application, src, dst, convoKey);
    }

    private void insertIf(String application, String direction, long delta, long last, long bytes, Integer inputSnmp, Integer outputSnmp) {
        jdbc.update("INSERT INTO flow (flow_ts, delta_switched, last_switched, bytes, sampling_interval, direction, application, input_snmp, output_snmp, document) VALUES (to_timestamp(?/1000.0), ?, ?, ?, 1.0, ?, ?, ?, ?, '{}'::jsonb)",
                last, delta, last, bytes, direction, application, inputSnmp, outputSnmp);
    }

    private void insertEcn(String application, String direction, long delta, long last, long bytes, Integer ecn) {
        Object document = ecn == null ? "{}" : "{\"netflow.ecn\": " + ecn + "}";
        jdbc.update("INSERT INTO flow (flow_ts, delta_switched, last_switched, bytes, sampling_interval, direction, application, document) VALUES (to_timestamp(?/1000.0), ?, ?, ?, 1.0, ?, ?, ?::jsonb)",
                last, delta, last, bytes, direction, application, document);
    }

    private static Map<String, long[]> byEntity(List<TrafficSummary<String>> summaries) {
        HashMap<String, long[]> m = new HashMap<>();
        for (TrafficSummary<String> s : summaries) {
            m.put(s.getEntity(), new long[]{s.getBytesIn(), s.getBytesOut()});
        }
        return m;
    }

    @Test
    public void applicationSummariesMatchProration() throws Exception {
        this.insert("http", "INGRESS", BASE, BASE + 1000L, 1000L, 1.0, "10.0.0.1", "10.0.0.2", null);
        this.insert("http", "EGRESS", BASE + 250L, BASE + 750L, 400L, 1.0, "10.0.0.1", "10.0.0.2", null);
        this.insert("http", "INGRESS", BASE + 500L, BASE + 500L, 300L, 1.0, "10.0.0.3", "10.0.0.4", null);
        this.insert("http", "INGRESS", BASE + 100L, BASE + 900L, 2L, 4.0, "10.0.0.5", "10.0.0.6", null);
        this.insert("https", "INGRESS", BASE - 1000L, BASE + 1000L, 2000L, 1.0, "10.0.0.7", "10.0.0.8", null);
        Map<String, long[]> got = byEntity(queryService.getTopNApplicationSummaries(10, false, window()).get(10L, TimeUnit.SECONDS));
        Assert.assertEquals(2L, got.size());
        assertArrayEquals2(new long[]{1308L, 400L}, got.get("http"));
        assertArrayEquals2(new long[]{1000L, 0L}, got.get("https"));
    }

    @Test
    public void unknownDirectionExcludedWithoutInterfaceFilter() throws Exception {
        // Exact Elastic parity: with no interface filter the aggregation is constrained to
        // ingress/egress, so UNKNOWN-direction bytes are dropped (not counted as ingress) and an
        // application whose flows are all UNKNOWN does not appear at all.
        this.insert("dns", "UNKNOWN", BASE, BASE + 1000L, 500L, 1.0, "10.0.0.1", "10.0.0.2", null);
        this.insert("dns", "EGRESS", BASE, BASE + 1000L, 300L, 1.0, "10.0.0.1", "10.0.0.2", null);
        this.insert("onlyunknown", "UNKNOWN", BASE, BASE + 1000L, 400L, 1.0, "10.0.0.3", "10.0.0.4", null);
        Map<String, long[]> got = byEntity(queryService.getTopNApplicationSummaries(10, false, window()).get(10L, TimeUnit.SECONDS));
        assertArrayEquals2(new long[]{0L, 300L}, got.get("dns"));
        Assert.assertFalse("an all-UNKNOWN application must be excluded without an interface filter", got.containsKey("onlyunknown"));
    }

    @Test
    public void unknownDirectionReclassifiedUnderInterfaceFilter() throws Exception {
        // Exact Elastic parity: with a SnmpInterfaceIdFilter, an UNKNOWN flow is reclassified to
        // ingress when input_snmp matches the interface, else egress when output_snmp matches.
        this.insertIf("dns", "UNKNOWN", BASE, BASE + 1000L, 500L, 7, 99);  // input_snmp=7  -> INGRESS
        this.insertIf("dns", "UNKNOWN", BASE, BASE + 1000L, 200L, 99, 7);  // output_snmp=7 -> EGRESS
        List<Filter> filters = Arrays.asList(new TimeRangeFilter(BASE, BASE + WINDOW), new SnmpInterfaceIdFilter(7));
        Map<String, long[]> got = byEntity(queryService.getTopNApplicationSummaries(10, false, filters).get(10L, TimeUnit.SECONDS));
        assertArrayEquals2(new long[]{500L, 200L}, got.get("dns"));
    }

    @Test
    public void applicationSeriesMatchesProration() throws Exception {
        this.insert("dns", "INGRESS", BASE, BASE + 1000L, 1000L, 1.0, "10.0.0.1", "10.0.0.2", null);
        Table<Directional<String>, Long, Double> series = queryService.getApplicationSeries(Collections.singleton("dns"), 500L, false, window()).get(10L, TimeUnit.SECONDS);
        Directional<String> ingress = new Directional<>("dns", true);
        Assert.assertEquals(500.0, series.get(ingress, BASE), 1.0E-6);
        Assert.assertEquals(500.0, series.get(ingress, BASE + 500L), 1.0E-6);
    }

    @Test
    public void hostSummariesCountBothEndpoints() throws Exception {
        this.insert("x", "INGRESS", BASE, BASE + 1000L, 600L, 1.0, "192.168.1.1", "192.168.1.2", null);
        HashMap<String, long[]> got = new HashMap<>();
        for (TrafficSummary<Host> s : queryService.getTopNHostSummaries(10, false, window()).get(10L, TimeUnit.SECONDS)) {
            got.put(s.getEntity().getIp(), new long[]{s.getBytesIn(), s.getBytesOut()});
        }
        assertArrayEquals2(new long[]{600L, 0L}, got.get("192.168.1.1"));
        assertArrayEquals2(new long[]{600L, 0L}, got.get("192.168.1.2"));
    }

    @Test
    public void conversationSummaryParsesConvoKey() throws Exception {
        String convoKey = "[\"Default\",6,\"10.0.0.1\",\"10.0.0.2\",\"http\"]";
        this.insert("http", "INGRESS", BASE, BASE + 1000L, 800L, 1.0, "10.0.0.1", "10.0.0.2", convoKey);
        List<TrafficSummary<Conversation>> convos = queryService.getConversationSummaries(Collections.singleton(convoKey), false, window()).get(10L, TimeUnit.SECONDS);
        Assert.assertEquals(1L, convos.size());
        Assert.assertEquals(800L, convos.get(0).getBytesIn());
        Assert.assertEquals("http", convos.get(0).getEntity().getApplication());
    }

    @Test
    public void topNApplicationSeriesIncludeOtherAggregatesNonTopN() throws Exception {
        this.insert("top", "INGRESS", BASE, BASE + 1000L, 1000L, 1.0, "10.0.0.1", "10.0.0.2", null);
        this.insert("a", "INGRESS", BASE, BASE + 1000L, 400L, 1.0, "10.0.0.1", "10.0.0.2", null);
        this.insert("b", "INGRESS", BASE, BASE + 1000L, 600L, 1.0, "10.0.0.1", "10.0.0.2", null);
        Table<Directional<String>, Long, Double> series = queryService.getTopNApplicationSeries(1, 500L, true, window()).get(10L, TimeUnit.SECONDS);
        Directional<String> top = new Directional<>("top", true);
        Directional<String> other = new Directional<>("Other", true);
        Assert.assertEquals(500.0, series.get(top, BASE), 1.0E-6);
        Assert.assertEquals(500.0, series.get(top, BASE + 500L), 1.0E-6);
        Assert.assertEquals(500.0, series.get(other, BASE), 1.0E-6);
        Assert.assertEquals(500.0, series.get(other, BASE + 500L), 1.0E-6);
    }

    @Test
    public void applicationSummariesReportEcnFlags() throws Exception {
        this.insertEcn("ce", "INGRESS", BASE, BASE + 1000L, 100L, 3);
        this.insertEcn("noect", "INGRESS", BASE, BASE + 1000L, 100L, 0);
        this.insertEcn("mixed", "INGRESS", BASE, BASE + 1000L, 100L, 3);
        this.insertEcn("mixed", "INGRESS", BASE, BASE + 1000L, 100L, 0);
        this.insertEcn("plain", "INGRESS", BASE, BASE + 1000L, 100L, 1);
        HashMap<String, boolean[]> ecn = new HashMap<>();
        for (TrafficSummary<String> s : queryService.getTopNApplicationSummaries(10, false, window()).get(10L, TimeUnit.SECONDS)) {
            ecn.put(s.getEntity(), new boolean[]{s.isCongestionEncountered(), s.isNonEcnCapableTransport()});
        }
        Assert.assertArrayEquals(new boolean[]{true, false}, ecn.get("ce"));
        Assert.assertArrayEquals(new boolean[]{false, true}, ecn.get("noect"));
        Assert.assertArrayEquals(new boolean[]{true, true}, ecn.get("mixed"));
        Assert.assertArrayEquals(new boolean[]{false, false}, ecn.get("plain"));
    }

    @Test
    public void flowCountHonorsTimeWindow() throws Exception {
        this.insert("http", "INGRESS", BASE, BASE + 500L, 100L, 1.0, "10.0.0.1", "10.0.0.2", null);
        this.insert("http", "INGRESS", BASE + 5000L, BASE + 6000L, 100L, 1.0, "10.0.0.1", "10.0.0.2", null);
        Assert.assertEquals(Long.valueOf(1L), queryService.getFlowCount(window()).get(10L, TimeUnit.SECONDS));
    }

    @Test
    public void persistWritesRowsThroughTheBatchingWriter() throws Exception {
        repository.persist(Arrays.asList(
                mockFlow("http", Flow.Direction.INGRESS, BASE + 100L, BASE + 900L, 1234L, "10.1.1.1", "10.1.1.2"),
                mockFlow("http", Flow.Direction.EGRESS, BASE + 100L, BASE + 900L, 5678L, "10.1.1.2", "10.1.1.1")));
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10L);
        Integer count = 0;
        while (System.nanoTime() < deadline && ((count = jdbc.queryForObject("SELECT count(*) FROM flow", Integer.class)) == null || count != 2)) {
            Thread.sleep(50L);
        }
        Assert.assertEquals(Integer.valueOf(2), count);
        String app = jdbc.queryForObject("SELECT document->>'netflow.application' FROM flow WHERE direction = 'INGRESS'", String.class);
        Assert.assertEquals("http", app);
        long total = jdbc.queryForObject("SELECT COALESCE(SUM(bytes),0) FROM flow", Long.class);
        Assert.assertEquals(6912L, total);
    }

    @Test
    public void dedicatedPoolProviderConnectsAndQueries() throws Exception {
        final FlowDataSourceProvider provider = new FlowDataSourceProvider();
        provider.setUrl(JDBC_URL);
        provider.setUsername(JDBC_USER);
        provider.setPassword(JDBC_PASSWORD);
        provider.setMinPool(1);
        provider.setMaxPool(2);
        provider.setMaxSize(2);
        provider.init();
        try {
            final DataSource ds = provider.getDataSource();
            Assert.assertNotNull("dedicated mode must build a pool when a url is set", ds);
            final Integer one = new JdbcTemplate(ds).queryForObject("SELECT 1", Integer.class);
            Assert.assertEquals(Integer.valueOf(1), one);
        } finally {
            provider.close();
        }
    }

    @Test
    public void healthCheckSucceedsAgainstTheDatabase() throws Exception {
        final FlowDataSourceProvider provider = new FlowDataSourceProvider();
        provider.setUrl(JDBC_URL);
        provider.setUsername(JDBC_USER);
        provider.setPassword(JDBC_PASSWORD);
        provider.setMinPool(1);
        provider.setMaxPool(2);
        provider.setMaxSize(2);
        provider.init();
        try {
            final org.opennms.core.health.api.Response response =
                    new PostgresFlowHealthCheck(provider).perform(null);
            Assert.assertEquals(org.opennms.core.health.api.Status.Success, response.getStatus());
        } finally {
            provider.close();
        }
    }

    private static Flow mockFlow(String application, Flow.Direction direction, long delta, long last, long bytes, String src, String dst) {
        Flow flow = Mockito.mock(Flow.class);
        Mockito.lenient().when(flow.getTimestamp()).thenReturn(Instant.ofEpochMilli(last));
        Mockito.lenient().when(flow.getDeltaSwitched()).thenReturn(Instant.ofEpochMilli(delta));
        Mockito.lenient().when(flow.getLastSwitched()).thenReturn(Instant.ofEpochMilli(last));
        Mockito.lenient().when(flow.getFirstSwitched()).thenReturn(Instant.ofEpochMilli(delta));
        Mockito.lenient().when(flow.getBytes()).thenReturn(bytes);
        Mockito.lenient().when(flow.getDirection()).thenReturn(direction);
        Mockito.lenient().when(flow.getApplication()).thenReturn(application);
        Mockito.lenient().when(flow.getSrcAddr()).thenReturn(src);
        Mockito.lenient().when(flow.getDstAddr()).thenReturn(dst);
        Mockito.lenient().when(flow.getSamplingInterval()).thenReturn(1.0);
        return flow;
    }

    private static void assertArrayEquals2(long[] expected, long[] actual) {
        Assert.assertTrue("expected " + Arrays.toString(expected) + " but was " + Arrays.toString(actual),
                actual != null && expected.length == actual.length && expected[0] == actual[0] && expected[1] == actual[1]);
    }
}