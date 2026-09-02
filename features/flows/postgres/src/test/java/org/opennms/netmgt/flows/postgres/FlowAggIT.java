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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.sql.Connection;

import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.netmgt.flows.aggregation.FlowAggregator;
import org.opennms.netmgt.flows.aggregation.FlowInput;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import com.codahale.metrics.MetricRegistry;

import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

/**
 * End-to-end verification of the write-time aggregator against a real PostgreSQL: {@link FlowAggregator}
 * feeding {@link FlowAggWriter} into the {@code flow_agg} table, then reading the rows back. Also proves
 * the distribution model &mdash; several writers each emit partial rows per {@code (window, key)} and the
 * reader reconstructs the total by summing them.
 */
public class FlowAggIT {

    private static final String CHANGELOG = "org/opennms/netmgt/flows/postgres/changelog.xml";
    // a window boundary on the global (shift 0) 1000 ms grid
    private static final long BASE = 1700000000000L;
    private static final long WINDOW = 1000L;

    private static PostgreSQLContainer<?> POSTGRES;
    private static PGSimpleDataSource DATA_SOURCE;
    private static JdbcTemplate jdbc;

    @BeforeClass
    public static void startContainer() throws Exception {
        final String externalUrl = System.getProperty("it.postgres.jdbcUrl");
        final String url;
        final String user;
        final String password;
        if (externalUrl != null && !externalUrl.isEmpty()) {
            url = externalUrl;
            user = System.getProperty("it.postgres.user", "postgres");
            password = System.getProperty("it.postgres.password", "postgres");
        } else {
            Assume.assumeTrue("No Docker/podman runtime available; skipping live-SQL IT.",
                    DockerClientFactory.instance().isDockerAvailable());
            POSTGRES = new PostgreSQLContainer<>("postgres:15");
            POSTGRES.start();
            url = POSTGRES.getJdbcUrl();
            user = POSTGRES.getUsername();
            password = POSTGRES.getPassword();
        }
        DATA_SOURCE = new PGSimpleDataSource();
        DATA_SOURCE.setUrl(url);
        DATA_SOURCE.setUser(user);
        DATA_SOURCE.setPassword(password);
        DATA_SOURCE.setReWriteBatchedInserts(true);
        jdbc = new JdbcTemplate(DATA_SOURCE);
        try (Connection conn = DATA_SOURCE.getConnection()) {
            final liquibase.database.Database db = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(conn));
            new Liquibase(CHANGELOG, new ClassLoaderResourceAccessor(FlowAggIT.class.getClassLoader()), db).update("");
        }
    }

    @Before
    public void truncate() {
        jdbc.execute("TRUNCATE flow_agg");
    }

    private static FlowInput flow(final int exporter, final int ifIndex, final long delta, final long last,
                                  final long bytes, final boolean ingress, final String app, final String convo,
                                  final String src, final String dst, final Integer ecn) {
        return new FlowInput(delta, last, bytes, null, ingress, exporter, ifIndex, app, convo, src, dst,
                src == null ? null : src + "-host", dst == null ? null : dst + "-host", ecn, null);
    }

    private static FlowAggregator aggregatorWriting(final String writerId) {
        return new FlowAggregator(WINDOW, 0L, 3_600_000L, 10, 0L,
                new FlowAggWriter(DATA_SOURCE, writerId), new MetricRegistry());
    }

    private long sumTotal(final int exporter, final String dimension, final String key) {
        final Long v;
        if (key == null) {
            v = jdbc.queryForObject("SELECT COALESCE(SUM(bytes_in + bytes_out), 0) FROM flow_agg "
                    + "WHERE exporter_node_id = ? AND dimension = ? AND grouped_by_key IS NULL", Long.class, exporter, dimension);
        } else {
            v = jdbc.queryForObject("SELECT COALESCE(SUM(bytes_in + bytes_out), 0) FROM flow_agg "
                    + "WHERE exporter_node_id = ? AND dimension = ? AND grouped_by_key = ?", Long.class, exporter, dimension, key);
        }
        return v != null ? v : 0L;
    }

    @Test
    public void aggregatesPersistAndReadSumReconstructsTotals() {
        final int exporter = 42;
        // 3000 bytes uniformly over three 1000 ms windows [BASE, BASE+2999]
        try (FlowAggregator agg = aggregatorWriting("core")) {
            agg.add(flow(exporter, 5, BASE, BASE + 2999, 3000L, true, "http", "conv1", "10.0.0.1", "10.0.0.2", 0));
        } // close() flushes all windows synchronously

        // one INTERFACE row per spanned window
        final int interfaceRows = jdbc.queryForObject(
                "SELECT COUNT(*) FROM flow_agg WHERE exporter_node_id = ? AND dimension = 'INTERFACE'", Integer.class, exporter);
        assertEquals(3, interfaceRows);

        // read-time SUM over windows reconstructs the flow's total on every dimension (conservation through the DB)
        assertEquals(3000L, sumTotal(exporter, "INTERFACE", null));
        assertEquals(3000L, sumTotal(exporter, "APPLICATION", "http"));
        assertEquals(3000L, sumTotal(exporter, "CONVERSATION", "conv1"));
        assertEquals(3000L, sumTotal(exporter, "HOST", "10.0.0.1"));
        assertEquals(3000L, sumTotal(exporter, "HOST", "10.0.0.2"));

        // ingress -> bytes_in; ecn 0 -> non-ECT
        assertEquals(Long.valueOf(3000L), jdbc.queryForObject(
                "SELECT SUM(bytes_in) FROM flow_agg WHERE exporter_node_id = ? AND dimension = 'INTERFACE'", Long.class, exporter));
        assertEquals(Integer.valueOf(0), jdbc.queryForObject(
                "SELECT COUNT(*) FROM flow_agg WHERE exporter_node_id = ? AND non_ecn_capable_transport = false", Integer.class, exporter));
    }

    @Test
    public void tosDimensionPersistsWithAndWithoutTosRows() {
        final int exporter = 88;
        try (FlowAggregator agg = aggregatorWriting("core")) {
            // one flow with DSCP 5 -> a without-TOS (dscp NULL) row AND a with-TOS (dscp 5) row, each 1000 bytes
            agg.add(new FlowInput(BASE, BASE + 999, 1000L, null, true, exporter, 1, "http", null, null, null,
                    null, null, null, 5));
        }
        assertEquals("without-TOS rollup", Long.valueOf(1000L), jdbc.queryForObject(
                "SELECT sum(bytes_in+bytes_out) FROM flow_agg WHERE exporter_node_id=? AND dimension='INTERFACE' AND dscp IS NULL",
                Long.class, exporter));
        assertEquals("with-TOS dscp=5", Long.valueOf(1000L), jdbc.queryForObject(
                "SELECT sum(bytes_in+bytes_out) FROM flow_agg WHERE exporter_node_id=? AND dimension='INTERFACE' AND dscp=5",
                Long.class, exporter));
    }

    @Test
    public void aggregatedReadServiceServesTopNApplicationSummaries() throws Exception {
        final int exporter = 55;
        try (FlowAggregator agg = aggregatorWriting("core")) {
            // four applications in one window, distinct byte totals (ingress)
            agg.add(flow(exporter, 1, BASE, BASE + 999, 400L, true, "a1", null, null, null, null));
            agg.add(flow(exporter, 1, BASE, BASE + 999, 300L, true, "a2", null, null, null, null));
            agg.add(flow(exporter, 1, BASE, BASE + 999, 200L, true, "a3", null, null, null, null));
            agg.add(flow(exporter, 1, BASE, BASE + 999, 100L, true, "a4", null, null, null, null));
        }

        final AggregatedFlowQueryService qs =
                new AggregatedFlowQueryService(org.mockito.Mockito.mock(org.opennms.netmgt.flows.api.FlowQueryService.class));
        qs.setDataSource(DATA_SOURCE);
        qs.start();
        try {
            final java.util.List<org.opennms.netmgt.flows.filter.api.Filter> filters =
                    java.util.Collections.singletonList(new org.opennms.netmgt.flows.filter.api.TimeRangeFilter(BASE, BASE + WINDOW));
            // top 2 applications + Other
            final java.util.List<org.opennms.netmgt.flows.api.TrafficSummary<String>> out =
                    qs.getTopNApplicationSummaries(2, true, filters).get(10, java.util.concurrent.TimeUnit.SECONDS);

            assertEquals("top 2 + Other", 3, out.size());
            assertEquals("a1", out.get(0).getEntity());
            assertEquals(400L, out.get(0).getBytesIn());
            assertEquals("a2", out.get(1).getEntity());
            assertEquals(300L, out.get(1).getBytesIn());
            assertEquals("Other", out.get(2).getEntity());
            assertEquals("Other = a3 + a4", 300L, out.get(2).getBytesIn());
            final long total = out.stream().mapToLong(org.opennms.netmgt.flows.api.TrafficSummary::getBytesIn).sum();
            assertEquals("top-N + Other reconstruct the dimension total", 1000L, total);
        } finally {
            qs.stop();
        }
    }

    @Test
    public void aggregatedReadServiceServesTopNApplicationSeries() throws Exception {
        final int exporter = 66;
        try (FlowAggregator agg = aggregatorWriting("core")) {
            // Feed in event-time order (allowedLateness=0): both window-1 flows, then the window-2 flow.
            // a1 spans both windows; a2 is only in window 1.
            agg.add(flow(exporter, 1, BASE, BASE + 999, 300L, true, "a1", null, null, null, null));
            agg.add(flow(exporter, 1, BASE, BASE + 999, 100L, true, "a2", null, null, null, null));
            agg.add(flow(exporter, 1, BASE + 1000, BASE + 1999, 400L, true, "a1", null, null, null, null));
        }
        final AggregatedFlowQueryService qs =
                new AggregatedFlowQueryService(org.mockito.Mockito.mock(org.opennms.netmgt.flows.api.FlowQueryService.class));
        qs.setDataSource(DATA_SOURCE);
        qs.start();
        try {
            final java.util.List<org.opennms.netmgt.flows.filter.api.Filter> filters =
                    java.util.Collections.singletonList(new org.opennms.netmgt.flows.filter.api.TimeRangeFilter(BASE, BASE + 2 * WINDOW));
            // top-1 app (a1) + Other, one bucket per WINDOW-ms step (== aggregation window here)
            final com.google.common.collect.Table<org.opennms.netmgt.flows.api.Directional<String>, Long, Double> t =
                    qs.getTopNApplicationSeries(1, WINDOW, true, filters).get(10, java.util.concurrent.TimeUnit.SECONDS);

            final org.opennms.netmgt.flows.api.Directional<String> a1In = new org.opennms.netmgt.flows.api.Directional<>("a1", true);
            final org.opennms.netmgt.flows.api.Directional<String> otherIn = new org.opennms.netmgt.flows.api.Directional<>("Other", true);
            assertEquals(300.0, t.get(a1In, BASE), 0.0);              // a1, window 1
            assertEquals(400.0, t.get(a1In, BASE + WINDOW), 0.0);     // a1, window 2
            assertEquals("Other = a2 in window 1", 100.0, t.get(otherIn, BASE), 0.0);
            assertNull("no Other in window 2 (only a1 there)", t.get(otherIn, BASE + WINDOW));
            assertNull("a2 folded into Other, not its own row", t.get(new org.opennms.netmgt.flows.api.Directional<>("a2", true), BASE));
        } finally {
            qs.stop();
        }
    }

    @Test
    public void aggregatedReadServiceServesDscpFieldValuesAndSummaries() throws Exception {
        final int exporter = 91;
        try (FlowAggregator agg = aggregatorWriting("core")) {
            // two DSCP classes in one window (with-TOS INTERFACE rows)
            agg.add(new FlowInput(BASE, BASE + 999, 300L, null, true, exporter, 1, "a1", null, null, null, null, null, null, 0));
            agg.add(new FlowInput(BASE, BASE + 999, 700L, null, true, exporter, 1, "a2", null, null, null, null, null, null, 46));
        }
        final AggregatedFlowQueryService qs =
                new AggregatedFlowQueryService(org.mockito.Mockito.mock(org.opennms.netmgt.flows.api.FlowQueryService.class));
        qs.setDataSource(DATA_SOURCE);
        qs.start();
        try {
            final java.util.List<org.opennms.netmgt.flows.filter.api.Filter> filters =
                    java.util.Collections.singletonList(new org.opennms.netmgt.flows.filter.api.TimeRangeFilter(BASE, BASE + WINDOW));

            final java.util.List<String> values =
                    qs.getFieldValues(org.opennms.netmgt.flows.api.LimitedCardinalityField.DSCP, filters).get(10, java.util.concurrent.TimeUnit.SECONDS);
            assertEquals(java.util.Arrays.asList("0", "46"), values);

            final java.util.List<org.opennms.netmgt.flows.api.TrafficSummary<String>> sums =
                    qs.getFieldSummaries(org.opennms.netmgt.flows.api.LimitedCardinalityField.DSCP, filters).get(10, java.util.concurrent.TimeUnit.SECONDS);
            assertEquals(2, sums.size());
            assertEquals("46", sums.get(0).getEntity());   // largest first
            assertEquals(700L, sums.get(0).getBytesIn());
            assertEquals("0", sums.get(1).getEntity());
            assertEquals(300L, sums.get(1).getBytesIn());
        } finally {
            qs.stop();
        }
    }

    @Test
    public void partialRowsFromMultipleWritersSumOnRead() {
        final int exporter = 77;
        // Two independent writers (e.g. two Sentinels) each see the SAME flow and emit their own partials.
        for (final String writerId : new String[] {"sentinel-a", "sentinel-b"}) {
            try (FlowAggregator agg = aggregatorWriting(writerId)) {
                agg.add(flow(exporter, 1, BASE, BASE + 999, 1000L, true, "https", null, null, null, null));
            }
        }
        // two partial rows for the single (window, interface) key...
        assertEquals(Integer.valueOf(2), jdbc.queryForObject(
                "SELECT COUNT(*) FROM flow_agg WHERE exporter_node_id = ? AND dimension = 'INTERFACE'", Integer.class, exporter));
        assertEquals(Integer.valueOf(2), jdbc.queryForObject(
                "SELECT COUNT(DISTINCT writer_id) FROM flow_agg WHERE exporter_node_id = ?", Integer.class, exporter));
        // ...and the reader reconstructs the combined total by summing the partials
        assertEquals(2000L, sumTotal(exporter, "INTERFACE", null));
        assertEquals(2000L, sumTotal(exporter, "APPLICATION", "https"));
    }
}
