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
package org.opennms.netmgt.flows.elastic;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.opennms.integration.api.v1.flows.Flow.Direction;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.script.ScriptEngineManager;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.core.cache.CacheConfigBuilder;
import org.opennms.features.elastic.client.ElasticRestClient;
import org.opennms.features.elastic.client.ElasticRestClientFactory;
import org.opennms.features.jest.client.index.IndexSelector;
import org.opennms.features.jest.client.index.IndexStrategy;
import org.opennms.features.jest.client.template.IndexSettings;
import org.opennms.netmgt.dao.mock.MockInterfaceToNodeCache;
import org.opennms.netmgt.dao.mock.MockIpInterfaceDao;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.dao.mock.MockSessionUtils;
import org.opennms.netmgt.flows.api.Directional;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.api.TrafficSummary;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.internal.DefaultClassificationEngine;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;
import org.opennms.netmgt.flows.elastic.agg.AggregatedFlowQueryService;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.flows.filter.api.SnmpInterfaceIdFilter;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;
import org.opennms.netmgt.flows.processing.FlowBuilder;
import org.opennms.netmgt.flows.processing.impl.DocumentEnricherImpl;
import org.opennms.netmgt.flows.processing.impl.DocumentMangler;
import org.opennms.netmgt.telemetry.protocols.cache.NodeInfoCache;
import org.opennms.netmgt.telemetry.protocols.cache.NodeInfoCacheImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

/**
 * Runs the two proportional-sum strategies side by side against the same Elasticsearch instance and
 * the same flow data, and asserts they return the same series and totals.
 *
 * <p>Aligned-window assertions run by default (the flows itests pom downloads the plugin and sets
 * the {@code org.opennms.flows.drift.*} properties). Unaligned-window agreement only holds against a
 * drift build that fixes NMS-20001, so it is gated behind {@code driftUnalignedFixed}.</p>
 */
public class ProportionalSumStrategyComparisonIT {

    private static final Logger LOG = LoggerFactory.getLogger(ProportionalSumStrategyComparisonIT.class);

    private static final String ES_VERSION = "8.18.2";
    // Supplied by the pom so it can be bumped to the NMS-20001 fix without editing this test.
    private static final String DRIFT_PLUGIN_VERSION = System.getProperty("org.opennms.flows.drift.version", "2.0.7");

    private static final String ENABLE_PROPERTY = "org.opennms.flows.drift.comparison";
    private static final String UNALIGNED_FIXED_PROPERTY = "org.opennms.flows.drift.unalignedFixed";

    private static final long STEP = 10L;
    private static final long UNALIGNED_START = 5L; // not a multiple of STEP

    // Cells are floating-point; the plugin and the script accumulate in different orders.
    private static final double ERROR = 1e-8;

    private static ElasticTestContainerWithPlugins elasticsearchContainer;

    private SmartQueryService painless;
    private SmartQueryService plugin;

    @BeforeClass
    public static void setUpClass() throws IOException {
        assumeTrue("requires the drift plugin; enabled by the itests pom (-D" + ENABLE_PROPERTY + "=true)",
                Boolean.getBoolean(ENABLE_PROPERTY));
        elasticsearchContainer = new ElasticTestContainerWithPlugins("docker.elastic.co/elasticsearch/elasticsearch:" + ES_VERSION)
                // Only the drift plugin needs installing; Painless is built into the image.
                .withPlugin("org.opennms.elasticsearch", "elasticsearch-drift-plugin-" + ES_VERSION, DRIFT_PLUGIN_VERSION);
        elasticsearchContainer.start();
        if (!elasticsearchContainer.verifyPluginsInstalled()) {
            throw new IllegalStateException("The drift plugin was not installed in the test container; cannot compare strategies.");
        }
        LOG.info("Elasticsearch {} with drift plugin {} started for strategy comparison", ES_VERSION, DRIFT_PLUGIN_VERSION);
    }

    @AfterClass
    public static void tearDownClass() {
        if (elasticsearchContainer != null) {
            elasticsearchContainer.stop();
        }
    }

    @Before
    public void setUp() throws Exception {
        final MetricRegistry metricRegistry = new MetricRegistry();
        final ElasticRestClientFactory factory = new ElasticRestClientFactory(elasticsearchContainer.getHttpHostAddress(), null, null);
        final ElasticRestClient client = factory.createClient();

        final IndexSettings settings = new IndexSettings();
        settings.setIndexPrefix("flows");
        final IndexSelector rawIndexSelector = new IndexSelector(settings, RawFlowQueryService.INDEX_NAME, IndexStrategy.MONTHLY, 120000);

        // Two query services over the same index, differing only in the proportional-sum strategy.
        painless = smartQueryService(metricRegistry, client, rawIndexSelector, ProportionalSumQuery.Strategy.PAINLESS);
        plugin = smartQueryService(metricRegistry, client, rawIndexSelector, ProportionalSumQuery.Strategy.PLUGIN);

        final ElasticFlowRepository flowRepository = new ElasticFlowRepository(metricRegistry, client, IndexStrategy.MONTHLY,
                new MockIdentity(), new MockTracerRegistry(), settings, 0, 0);

        client.deleteIndex("flows*");
        new RawIndexInitializer(client, settings).initialize();

        final List<Flow> flows = getFlows();
        flowRepository.persist(documentEnricher().enrich(flows, new FlowSource("test", "127.0.0.1", null)));
        await().atMost(60, TimeUnit.SECONDS).until(() -> painless.getFlowCount(
                Collections.singletonList(new TimeRangeFilter(0, System.currentTimeMillis()))).get(), equalTo((long) flows.size()));
    }

    @Test
    public void applicationSeriesMatch() throws Exception {
        final Set<String> apps = ImmutableSet.of("http", "https");
        assertSeriesEqual(
                plugin.getApplicationSeries(apps, STEP, true, getFilters()).get(),
                painless.getApplicationSeries(apps, STEP, true, getFilters()).get());
    }

    @Test
    public void topNApplicationSeriesMatch() throws Exception {
        assertSeriesEqual(
                plugin.getTopNApplicationSeries(10, STEP, true, getFilters()).get(),
                painless.getTopNApplicationSeries(10, STEP, true, getFilters()).get());
    }

    @Test
    public void hostSeriesMatch() throws Exception {
        assertSeriesEqual(
                plugin.getTopNHostSeries(10, STEP, true, getFilters()).get(),
                painless.getTopNHostSeries(10, STEP, true, getFilters()).get());
    }

    @Test
    public void topNApplicationSummariesMatch() throws Exception {
        assertSummariesEqual(
                plugin.getTopNApplicationSummaries(10, true, getFilters()).get(),
                painless.getTopNApplicationSummaries(10, true, getFilters()).get());
    }

    @Test
    public void applicationSummariesMatch() throws Exception {
        final Set<String> apps = ImmutableSet.of("http", "https");
        assertSummariesEqual(
                plugin.getApplicationSummaries(apps, true, getFilters()).get(),
                painless.getApplicationSummaries(apps, true, getFilters()).get());
    }

    // Unaligned windows only agree on a drift build that fixes NMS-20001; skipped otherwise.
    @Test
    public void unalignedWindowSeriesMatchOnFixedPlugin() throws Exception {
        assumeTrue("requires a drift build that fixes NMS-20001 (set -DdriftUnalignedFixed=true)",
                Boolean.getBoolean(UNALIGNED_FIXED_PROPERTY));
        final List<Filter> filters = unalignedFilters();
        final Set<String> apps = ImmutableSet.of("http", "https");
        assertSeriesEqual(
                plugin.getApplicationSeries(apps, STEP, true, filters).get(),
                painless.getApplicationSeries(apps, STEP, true, filters).get());
        assertSeriesEqual(
                plugin.getTopNApplicationSeries(10, STEP, true, filters).get(),
                painless.getTopNApplicationSeries(10, STEP, true, filters).get());
        assertSummariesEqual(
                plugin.getTopNApplicationSummaries(10, true, filters).get(),
                painless.getTopNApplicationSummaries(10, true, filters).get());
    }

    private static <R> void assertSeriesEqual(Table<Directional<R>, Long, Double> expected,
                                              Table<Directional<R>, Long, Double> actual) {
        assertThat("row keys", actual.rowKeySet(), containsInAnyOrder(expected.rowKeySet().toArray()));
        assertThat("column (bucket) keys", actual.columnKeySet(), containsInAnyOrder(expected.columnKeySet().toArray()));
        for (Table.Cell<Directional<R>, Long, Double> cell : expected.cellSet()) {
            final Double actualValue = actual.get(cell.getRowKey(), cell.getColumnKey());
            assertThat("cell " + cell.getRowKey() + "@" + cell.getColumnKey(), actualValue, closeTo(cell.getValue(), ERROR));
        }
    }

    private static <L> void assertSummariesEqual(List<TrafficSummary<L>> expected, List<TrafficSummary<L>> actual) {
        assertThat(actual, hasSize(expected.size()));
        for (int i = 0; i < expected.size(); i++) {
            final TrafficSummary<L> e = expected.get(i);
            final TrafficSummary<L> a = actual.get(i);
            assertThat("entity at " + i, a.getEntity(), equalTo(e.getEntity()));
            assertThat("bytesIn for " + e.getEntity(), a.getBytesIn(), equalTo(e.getBytesIn()));
            assertThat("bytesOut for " + e.getEntity(), a.getBytesOut(), equalTo(e.getBytesOut()));
        }
    }

    private static SmartQueryService smartQueryService(MetricRegistry metricRegistry, ElasticRestClient client,
                                                       IndexSelector indexSelector, ProportionalSumQuery.Strategy strategy) {
        final RawFlowQueryService raw = new RawFlowQueryService(client, indexSelector, strategy.name());
        final AggregatedFlowQueryService agg = mock(AggregatedFlowQueryService.class);
        final SmartQueryService smart = new SmartQueryService(metricRegistry, raw, agg);
        smart.setAlwaysUseRawForQueries(true); // Compare the raw-flow query path, which renders proportional sums.
        return smart;
    }

    private DocumentEnricherImpl documentEnricher() throws InterruptedException {
        final var classificationEngine = new DefaultClassificationEngine(() -> Lists.newArrayList(
                new RuleBuilder().withName("http").withDstPort("80").withProtocol("tcp,udp").build(),
                new RuleBuilder().withName("https").withDstPort("443").withProtocol("tcp,udp").build(),
                new RuleBuilder().withName("http").withSrcPort("80").withProtocol("tcp,udp").build(),
                new RuleBuilder().withName("https").withSrcPort("443").withProtocol("tcp,udp").build()),
                FilterService.NOOP);
        final NodeInfoCache nodeInfoCache = new NodeInfoCacheImpl(
                new CacheConfigBuilder()
                        .withName("nodeInfoCache")
                        .withMaximumSize(1000)
                        .withExpireAfterWrite(300)
                        .withExpireAfterRead(300)
                        .build(),
                true,
                new MetricRegistry(),
                new MockNodeDao(),
                new MockIpInterfaceDao(),
                new MockInterfaceToNodeCache(),
                new MockSessionUtils());
        return new DocumentEnricherImpl(new MockSessionUtils(), classificationEngine, 0,
                new DocumentMangler(new ScriptEngineManager()), nodeInfoCache);
    }

    private static List<Filter> getFilters() {
        return Lists.newArrayList(new TimeRangeFilter(0, System.currentTimeMillis()), new SnmpInterfaceIdFilter(98));
    }

    private static List<Filter> unalignedFilters() {
        return Lists.newArrayList(new TimeRangeFilter(UNALIGNED_START, System.currentTimeMillis()), new SnmpInterfaceIdFilter(98));
    }

    // The default flow set from FlowQueryIT: overlapping intervals across several buckets, so the
    // proportional-sum aggregation actually has work to do.
    private static List<Flow> getFlows() {
        return new FlowBuilder()
                .withSnmpInterfaceId(98)
                // 192.168.1.100:43444 <-> 10.1.1.11:80 (110 bytes in [3,15])
                .withDirection(Direction.INGRESS)
                .withTos(4 + 64)
                .withFlow(Instant.ofEpochMilli(3), Instant.ofEpochMilli(15), "192.168.1.100", 43444, "10.1.1.11", 80, 10)
                .withDirection(Direction.EGRESS)
                .withTos(8 + 128)
                .withFlow(Instant.ofEpochMilli(3), Instant.ofEpochMilli(15), "10.1.1.11", 80, "192.168.1.100", 43444, 100)
                // 192.168.1.100:43445 <-> 10.1.1.12:443 (1100 bytes in [13,26])
                .withDirection(Direction.INGRESS)
                .withHostnames(null, "la.le.lu")
                .withTos(16 + 64)
                .withFlow(Instant.ofEpochMilli(13), Instant.ofEpochMilli(26), "192.168.1.100", 43445, "10.1.1.12", 443, 100)
                .withDirection(Direction.EGRESS)
                .withHostnames("la.le.lu", null)
                .withTos(32 + 128)
                .withFlow(Instant.ofEpochMilli(13), Instant.ofEpochMilli(26), "10.1.1.12", 443, "192.168.1.100", 43445, 1000)
                // 192.168.1.101:43442 <-> 10.1.1.12:443 (1210 bytes in [14,45])
                .withDirection(Direction.INGRESS)
                .withHostnames("ingress.only", "la.le.lu")
                .withFlow(Instant.ofEpochMilli(14), Instant.ofEpochMilli(45), "192.168.1.101", 43442, "10.1.1.12", 443, 110)
                .withDirection(Direction.EGRESS)
                .withHostnames("la.le.lu", null)
                .withFlow(Instant.ofEpochMilli(14), Instant.ofEpochMilli(45), "10.1.1.12", 443, "192.168.1.101", 43442, 1100)
                // 192.168.1.102:50000 <-> 10.1.1.13:50001 (300 bytes in [50,52])
                .withDirection(Direction.INGRESS)
                .withFlow(Instant.ofEpochMilli(50), Instant.ofEpochMilli(52), "192.168.1.102", 50000, "10.1.1.13", 50001, 200)
                .withDirection(Direction.EGRESS)
                .withFlow(Instant.ofEpochMilli(50), Instant.ofEpochMilli(52), "10.1.1.13", 50001, "192.168.1.102", 50000, 100)
                .build();
    }
}
