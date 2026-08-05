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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.opennms.integration.api.v1.flows.Flow.Direction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.script.ScriptEngineManager;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
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
import org.opennms.netmgt.flows.api.Conversation;
import org.opennms.netmgt.flows.api.Directional;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.api.Host;
import org.opennms.netmgt.flows.api.LimitedCardinalityField;
import org.opennms.netmgt.flows.api.TrafficSummary;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.internal.DefaultClassificationEngine;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;
import org.opennms.netmgt.flows.elastic.agg.AggregatedFlowQueryService;
import org.opennms.netmgt.flows.filter.api.DscpFilter;
import org.opennms.netmgt.flows.filter.api.ExporterNodeFilter;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.flows.filter.api.FilterVisitor;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Captures the Elasticsearch + Drift answers to every {@code FlowQueryService} method as a JSON
 * reference file, so that an alternative backend can be checked against them.
 *
 * <p>This is the oracle for the VictoriaLogs flow backend. Nothing about that backend's query side is
 * verifiable without it: the proportionally-attributed byte series in particular are the output of a
 * closed-source Elasticsearch plugin, and agreement with it is the whole acceptance criterion. Only
 * the reference file is a deliverable; this class exists to regenerate it.
 *
 * <p><strong>The corpus is defined here, not shared with {@link FlowQueryIT}.</strong> It is
 * deliberately a byte-for-byte copy of that test's default flow set, but a copy: an oracle whose
 * inputs can be changed from another file is not an oracle. If the two drift apart, the spot-check
 * assertions at the end of {@link #capturesReferenceOutput()} — which mirror {@code FlowQueryIT}'s
 * own expectations — are what will notice.
 *
 * <p><strong>Determinism.</strong> {@code FlowQueryIT} filters on {@code System.currentTimeMillis()},
 * which is fine for assertions but would make a recorded artifact differ on every run. Every time
 * range here is a fixed constant, table rows and columns are emitted in sorted order, and the DSCP
 * filter values are derived from the corpus rather than hardcoded. List results keep their original
 * order, because for the Top-N methods that order <em>is</em> part of the answer.
 *
 * <p>Run with {@code -Dit.test=FlowQueryReferenceCaptureIT -DfailIfNoTests=false}; requires Docker
 * and the Drift plugin zip that {@code maven-dependency-plugin} places in
 * {@code target/elasticsearch-plugins} during {@code generate-test-resources}. The output path can be
 * overridden with {@code -DflowReference.outputFile=...}.
 */
public class FlowQueryReferenceCaptureIT {

    private static final Logger LOG = LoggerFactory.getLogger(FlowQueryReferenceCaptureIT.class);

    private static final String ES_VERSION = "8.18.2";
    private static final String DRIFT_PLUGIN_VERSION = "2.0.7";

    private static final String DEFAULT_OUTPUT_FILE = "target/flow-query-reference.json";

    /**
     * The window the original corpus occupies; it spans t=[3,80]. The series methods
     * derive their buckets from the matched data rather than from this range, so widening it adds no
     * empty buckets.
     */
    private static final long WINDOW_START = 0L;
    private static final long WINDOW_END = 100_000L;

    /** The corpus is recorded against this interface; see {@link #getCorpus()}. */
    private static final int SNMP_INTERFACE_ID = 98;

    /**
     * A second interface, carrying only the undetermined-direction flows.
     *
     * <p>Keeping them off {@link #SNMP_INTERFACE_ID} is what makes this extension additive: all four
     * of the original filter sets pin that interface, so every case recorded before this existed
     * must come back byte-identical. A moved answer means the regeneration changed something it
     * should not have, and the diff says so immediately.
     */
    private static final int UNKNOWN_SNMP_INTERFACE_ID = 99;

    /** An interface that is never filtered on, so a flow can name one that does not match. */
    private static final int UNRELATED_SNMP_INTERFACE_ID = 5;

    /**
     * One DSCP per undetermined-direction flow, all distinct from the original corpus's four.
     *
     * <p>Distinct so that a DSCP filter can select them individually: that is what lets the recorded
     * answers show <em>which</em> flow a failing query is failing on, instead of leaving it to be
     * inferred from a stack trace.
     */
    private static final int TOS_UNKNOWN_INPUT_ONLY = 44 * 4;
    private static final int TOS_UNKNOWN_OUTPUT_ONLY = 45 * 4;
    private static final int TOS_UNKNOWN_BOTH = 46 * 4;

    // Derived, not restated. Two constants holding 44 and TOS/4 would drift apart the first time
    // someone changed a TOS to avoid a collision, and the filter set built from them has no
    // emptiness guard -- it would silently narrow, or match nothing at all, which the file's own
    // comment on the derived DSCP list calls out as looking like agreement whatever the other
    // backend does.
    /**
     * A third interface and a disjoint window, carrying the flows that close the oracle's blind
     * spots.
     *
     * <p>The window matters as much as the interface. Every pre-existing filter set is bounded by
     * {@link #WINDOW_START}/{@link #WINDOW_END}, including the one with no interface filter at all —
     * so putting these flows beyond that window is what keeps all 72 existing answers byte-identical
     * rather than merely most of them.
     */
    private static final int EXTENDED_SNMP_INTERFACE_ID = 100;
    private static final long EXTENDED_WINDOW_START = 200_000L;
    private static final long EXTENDED_WINDOW_END = 300_000L;

    private static final int TOS_SAMPLED = 50 * 4;
    private static final int TOS_DELTA_AFTER_FIRST = 51 * 4;
    private static final int TOS_DIRECTION_DISAGREES = 52 * 4;
    /** Low two bits are the ECN codepoint; 3 is "congestion encountered". */
    private static final int TOS_CONGESTION = 53 * 4 + 3;
    private static final int TOS_UNKNOWN_BOTH_INTERFACES = 54 * 4;

    private static final int DSCP_UNKNOWN_INPUT_ONLY = TOS_UNKNOWN_INPUT_ONLY / 4;
    private static final int DSCP_UNKNOWN_BOTH = TOS_UNKNOWN_BOTH / 4;

    /** Conversation keys present in the corpus, in the string form the query methods accept. */
    private static final String CONVO_HTTP =
            "[\"test\",6,\"10.1.1.11\",\"192.168.1.100\",\"http\"]";
    private static final String CONVO_HTTPS =
            "[\"test\",6,\"10.1.1.12\",\"192.168.1.100\",\"https\"]";

    private static final String FILTERS_DEFAULT = "default";
    private static final String FILTERS_PARTIAL_WINDOW = "partialWindow";
    private static final String FILTERS_DSCP_ALL = "dscpAll";
    private static final String FILTERS_DSCP_FIRST = "dscpFirst";
    private static final String FILTERS_UNKNOWN_INTERFACE = "unknownInterface";
    private static final String FILTERS_UNKNOWN_INTERFACE_BOTH_SNMP = "unknownInterfaceBothSnmp";
    private static final String FILTERS_NO_INTERFACE = "noInterface";
    private static final String FILTERS_EXTENDED = "extendedCoverage";

    @ClassRule
    public static ElasticTestContainerWithPlugins elasticsearchContainer;

    static {
        try {
            elasticsearchContainer = new ElasticTestContainerWithPlugins(
                    "docker.elastic.co/elasticsearch/elasticsearch:" + ES_VERSION)
                    .withPlugin("org.opennms.elasticsearch",
                            "elasticsearch-drift-plugin-" + ES_VERSION, DRIFT_PLUGIN_VERSION);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to initialize Elasticsearch container", e);
        }
    }

    private ElasticFlowRepository flowRepository;
    private DocumentEnricherImpl documentEnricher;
    private SmartQueryService smartQueryService;

    private final Map<String, List<Filter>> filterSets = new LinkedHashMap<>();
    private final JsonArray cases = new JsonArray();

    @BeforeClass
    public static void setUpClass() {
        if (!elasticsearchContainer.verifyPluginsInstalled()) {
            throw new RuntimeException("Drift plugin is not installed; the reference would be "
                    + "captured from an Elasticsearch that cannot answer the series queries.");
        }
    }

    @Before
    public void setUp() throws IOException, ExecutionException, InterruptedException {
        final MetricRegistry metricRegistry = new MetricRegistry();
        final ElasticRestClientFactory elasticRestClientFactory =
                new ElasticRestClientFactory(elasticsearchContainer.getHttpHostAddress(), null, null);
        final ElasticRestClient elasticRestClient = elasticRestClientFactory.createClient();
        final IndexSettings settings = new IndexSettings();
        settings.setIndexPrefix("flows");
        final IndexSelector rawIndexSelector = new IndexSelector(settings, RawFlowQueryService.INDEX_NAME,
                IndexStrategy.MONTHLY, 120000);
        final RawFlowQueryService rawFlowRepository = new RawFlowQueryService(elasticRestClient, rawIndexSelector);
        final AggregatedFlowQueryService aggFlowRepository = mock(AggregatedFlowQueryService.class);
        smartQueryService = new SmartQueryService(metricRegistry, rawFlowRepository, aggFlowRepository);
        // The aggregated path is off by default in production and is not part of this reference.
        smartQueryService.setAlwaysUseRawForQueries(true);
        flowRepository = new ElasticFlowRepository(metricRegistry, elasticRestClient, IndexStrategy.MONTHLY,
                new MockIdentity(), new MockTracerRegistry(), settings, 0, 0);

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

        documentEnricher = new DocumentEnricherImpl(new MockSessionUtils(),
                classificationEngine,
                0,
                new DocumentMangler(new ScriptEngineManager()),
                nodeInfoCache);

        elasticRestClient.deleteIndex("flows*");
        new RawIndexInitializer(elasticRestClient, settings).initialize();

        // Over the window the corpus actually occupies. A zero-width range returns 0 for an empty
        // index and for a full one alike, so it asserted nothing: a deleteIndex that silently failed
        // would leave stale documents behind and this check would still pass.
        assertThat("the index must be empty before the corpus is written",
                smartQueryService.getFlowCount(
                        Collections.singletonList(new TimeRangeFilter(WINDOW_START, WINDOW_END))).get(),
                equalTo(0L));
    }

    @Test
    public void capturesReferenceOutput() throws Exception {
        final List<Flow> corpus = getCorpus();
        final List<org.opennms.netmgt.flows.processing.enrichment.EnrichedFlow> enriched =
                documentEnricher.enrich(corpus, new FlowSource("test", "127.0.0.1", null));
        flowRepository.persist(enriched);
        // Over a window spanning both corpora; the extended flows sit deliberately outside
        // WINDOW_START..WINDOW_END so they cannot perturb any pre-existing recorded answer.
        await().atMost(60, TimeUnit.SECONDS).until(() -> smartQueryService.getFlowCount(
                        Collections.singletonList(
                                new TimeRangeFilter(WINDOW_START, EXTENDED_WINDOW_END))).get(),
                equalTo((long) corpus.size()));

        buildFilterSets();

        recordFlowCount();
        recordApplications();
        recordConversations();
        recordHosts();
        recordFields();
        recordDirectionCases();
        recordExtendedCoverageCases();

        // Everything below runs BEFORE the file is written. These checks exist to stop a wrong
        // reference reaching disk, and writing first defeated them: a failed assertion still left a
        // complete-looking artifact in target/ for someone to copy out.
        assertThat(corpus, hasSize(16));

        // No case may be a recorded failure. recordSafely() keeps one bad query from destroying the
        // whole capture -- which is what allowed the HTTP 400 in common.ftl to be diagnosed at all --
        // but a recorded error is not an answer, and cases.size() cannot tell the two apart. Without
        // this, a regression in the direction script yields a structurally complete, green,
        // published reference whose direction cases all read "search_phase_execution_exception".
        final List<String> recordedErrors = new ArrayList<>();
        for (final JsonElement element : cases) {
            final JsonObject entry = element.getAsJsonObject();
            final JsonElement result = entry.get("result");
            if (result.isJsonObject() && result.getAsJsonObject().has("error")) {
                recordedErrors.add(entry.get("method").getAsString() + " " + entry.get("params")
                        + " -> " + result.getAsJsonObject().get("error").getAsString());
            }
        }
        if (!recordedErrors.isEmpty()) {
            throw new AssertionError("Elasticsearch could not answer " + recordedErrors.size()
                    + " case(s); the reference would record failures as if they were answers:\n"
                    + String.join("\n", recordedErrors));
        }

        // Spot checks against FlowQueryIT's own expectations. These are not the deliverable; they
        // exist so that a corpus or enrichment change fails loudly here instead of quietly producing
        // a reference file that encodes the wrong answers.
        //
        // Unchanged by the undetermined-direction flows, and required to stay that way: those sit on
        // another interface, which every one of the original filter sets pins away from.
        final List<TrafficSummary<String>> apps =
                smartQueryService.getTopNApplicationSummaries(10, true, filters(FILTERS_DEFAULT)).get();
        assertThat(apps, hasSize(4));
        assertThat(apps.get(0).getEntity(), equalTo("https"));
        assertThat(apps.get(0).getBytesIn(), equalTo(210L));
        assertThat(apps.get(0).getBytesOut(), equalTo(2100L));
        assertThat(cases.size(), equalTo(77));

        // The same answer again, as it was actually rendered. Everything between a correct service
        // result and the file -- summaries(), series(), hostJson() and the rest -- was verified by
        // nothing. Transposing bytesIn and bytesOut in summaries() would corrupt every recorded row
        // while leaving all four assertions above green, and ReferenceComparisonIT carries a
        // textually identical copy of that method, so the mistake would cancel out on both sides and
        // still report full agreement.
        final JsonObject recorded = findCase("getTopNApplicationSummaries",
                params(FILTERS_DEFAULT, "N", 10, "includeOther", true));
        final JsonObject topApp = recorded.getAsJsonArray("result").get(0).getAsJsonObject();
        assertThat(topApp.get("entity").getAsString(), equalTo("https"));
        assertThat(topApp.get("bytesIn").getAsLong(), equalTo(210L));
        assertThat(topApp.get("bytesOut").getAsLong(), equalTo(2100L));

        // And the series renderer, which is the other half of the same hazard. series() is
        // duplicated verbatim in ReferenceComparisonIT, so inverting the ingress flag or dropping
        // the NaN padding would cancel out on both sides and still report full agreement -- taking
        // the elaborate NaN handling in the comparison down with it, unnoticed.
        final JsonObject recordedSeries = findCase("getApplicationSeries",
                params(FILTERS_DEFAULT, "applications", ImmutableSet.of("https"),
                        "step", 10L, "includeOther", false));
        final JsonArray seriesRows = recordedSeries.getAsJsonArray("result");
        assertThat(seriesRows.size(), equalTo(2));
        final JsonObject egress = seriesRows.get(0).getAsJsonObject();
        assertThat("rows are ordered by entity then direction, egress first",
                egress.get("ingress").getAsBoolean(), equalTo(false));
        assertThat(egress.get("entity").getAsString(), equalTo("https"));
        assertThat("the bucket the flow starts in carries its proportional share",
                egress.getAsJsonObject("values").get("10").getAsDouble(), equalTo(751.36476426799));

        final JsonObject root = new JsonObject();
        root.addProperty("description", "Elasticsearch + Drift reference output for every "
                + "FlowQueryService method. Regenerate with FlowQueryReferenceCaptureIT.");
        root.addProperty("elasticsearchVersion", ES_VERSION);
        root.addProperty("driftPluginVersion", DRIFT_PLUGIN_VERSION);
        root.addProperty("alwaysUseRawForQueries", true);
        // Self-declared sizes, so a hand-edited or truncated artifact can be told from a captured
        // one. The file is copied between modules by hand and nothing else ties it to the capture
        // that produced it; ReferenceComparisonIT checks both on load.
        root.addProperty("caseCount", cases.size());
        root.addProperty("corpusSize", enriched.size());
        root.add("filterSets", filterSetsJson());
        root.add("corpus", corpusJson(enriched));
        root.add("cases", cases);

        final Path output = Paths.get(System.getProperty("flowReference.outputFile", DEFAULT_OUTPUT_FILE));
        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }
        final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        Files.write(output, gson.toJson(root).getBytes(StandardCharsets.UTF_8));
        LOG.info("Wrote {} reference cases over {} flows to {}",
                cases.size(), enriched.size(), output.toAbsolutePath());
    }

    // ------------------------------------------------------------------------------------------
    // Filter sets
    // ------------------------------------------------------------------------------------------

    private void buildFilterSets() throws Exception {
        filterSets.put(FILTERS_DEFAULT, Arrays.asList(
                new TimeRangeFilter(WINDOW_START, WINDOW_END),
                new SnmpInterfaceIdFilter(SNMP_INTERFACE_ID)));
        // The sub-range used by FlowQueryIT to exercise partial proportional sums.
        filterSets.put(FILTERS_PARTIAL_WINDOW, Arrays.asList(
                new TimeRangeFilter(10, 20),
                new SnmpInterfaceIdFilter(SNMP_INTERFACE_ID)));

        // Derived rather than hardcoded: a DSCP filter listing values the corpus does not contain
        // would record an empty result, which looks like agreement no matter what the other backend
        // does.
        final List<Integer> dscp = smartQueryService.getFieldValues(
                        LimitedCardinalityField.DSCP, filters(FILTERS_DEFAULT)).get()
                .stream()
                .map(Integer::parseInt)
                .sorted()
                .collect(Collectors.toList());
        if (dscp.isEmpty()) {
            throw new IllegalStateException("Corpus reports no DSCP values; the DSCP cases would be vacuous.");
        }
        filterSets.put(FILTERS_DSCP_ALL, Arrays.asList(
                new TimeRangeFilter(WINDOW_START, WINDOW_END),
                new SnmpInterfaceIdFilter(SNMP_INTERFACE_ID),
                new DscpFilter(dscp)));
        filterSets.put(FILTERS_DSCP_FIRST, Arrays.asList(
                new TimeRangeFilter(WINDOW_START, WINDOW_END),
                new SnmpInterfaceIdFilter(SNMP_INTERFACE_ID),
                new DscpFilter(Collections.singletonList(dscp.get(0)))));

        // Selects only the undetermined-direction flows, and so records how the direction script in
        // common.ftl resolves them against the interface being asked about.
        filterSets.put(FILTERS_UNKNOWN_INTERFACE, Arrays.asList(
                new TimeRangeFilter(WINDOW_START, WINDOW_END),
                new SnmpInterfaceIdFilter(UNKNOWN_SNMP_INTERFACE_ID)));
        // The same interface, minus the one flow that carries no netflow.input_snmp. If the queries
        // that fail under FILTERS_UNKNOWN_INTERFACE succeed here, the missing field is the cause and
        // the recorded answers show what the direction script resolves the rest to -- which is the
        // difference between a measured root cause and one read off a stack trace.
        filterSets.put(FILTERS_UNKNOWN_INTERFACE_BOTH_SNMP, Arrays.asList(
                new TimeRangeFilter(WINDOW_START, WINDOW_END),
                new SnmpInterfaceIdFilter(UNKNOWN_SNMP_INTERFACE_ID),
                new DscpFilter(Arrays.asList(DSCP_UNKNOWN_INPUT_ONLY, DSCP_UNKNOWN_BOTH))));
        // The same guard the derived DSCP list gets. This set exists to isolate the flow that has
        // no input_snmp by excluding it; a set that matched nothing, or that lost one of its two
        // flows, would record an answer that agrees with anything.
        requireFlowCount(FILTERS_UNKNOWN_INTERFACE_BOTH_SNMP, 2);
        requireFlowCount(FILTERS_UNKNOWN_INTERFACE, 3);
        // No interface at all, which is the other branch of every series template: without one they
        // constrain the query to ingress and egress, so an undetermined-direction flow is expected
        // to disappear entirely rather than be resolved.
        filterSets.put(FILTERS_NO_INTERFACE,
                Collections.singletonList(new TimeRangeFilter(WINDOW_START, WINDOW_END)));
        // The blind-spot flows; see getExtendedCoverageCorpus.
        filterSets.put(FILTERS_EXTENDED, Arrays.asList(
                new TimeRangeFilter(EXTENDED_WINDOW_START, EXTENDED_WINDOW_END),
                new SnmpInterfaceIdFilter(EXTENDED_SNMP_INTERFACE_ID)));
    }

    /** Fails the capture if a filter set does not select the flows it was built to select. */
    private void requireFlowCount(final String name, final long expected) throws Exception {
        final long actual = smartQueryService.getFlowCount(filters(name)).get();
        if (actual != expected) {
            throw new IllegalStateException("Filter set '" + name + "' selects " + actual
                    + " flows, expected " + expected + "; the cases recorded under it would not "
                    + "exercise what they claim to.");
        }
    }

    private List<Filter> filters(final String name) {
        final List<Filter> filters = filterSets.get(name);
        if (filters == null) {
            throw new IllegalArgumentException("No such filter set: " + name);
        }
        return filters;
    }

    private JsonObject filterSetsJson() {
        final JsonObject sets = new JsonObject();
        filterSets.forEach((name, filters) -> {
            final JsonArray array = new JsonArray();
            filters.forEach(f -> array.add(filterJson(f)));
            sets.add(name, array);
        });
        return sets;
    }

    private static JsonObject filterJson(final Filter filter) {
        return filter.visit(new FilterVisitor<JsonObject>() {
            @Override
            public JsonObject visit(final ExporterNodeFilter exporterNodeFilter) {
                final JsonObject o = new JsonObject();
                o.addProperty("type", "exporterNode");
                o.addProperty("criteria", String.valueOf(exporterNodeFilter.getCriteria()));
                return o;
            }

            @Override
            public JsonObject visit(final TimeRangeFilter timeRangeFilter) {
                final JsonObject o = new JsonObject();
                o.addProperty("type", "timeRange");
                o.addProperty("start", timeRangeFilter.getStart());
                o.addProperty("end", timeRangeFilter.getEnd());
                return o;
            }

            @Override
            public JsonObject visit(final SnmpInterfaceIdFilter snmpInterfaceIdFilter) {
                final JsonObject o = new JsonObject();
                o.addProperty("type", "snmpInterfaceId");
                o.addProperty("snmpInterfaceId", snmpInterfaceIdFilter.getSnmpInterfaceId());
                return o;
            }

            @Override
            public JsonObject visit(final DscpFilter dscpFilter) {
                final JsonObject o = new JsonObject();
                o.addProperty("type", "dscp");
                final JsonArray values = new JsonArray();
                dscpFilter.getDscp().stream().sorted().forEach(values::add);
                o.add("dscp", values);
                return o;
            }
        });
    }

    // ------------------------------------------------------------------------------------------
    // Cases
    // ------------------------------------------------------------------------------------------

    private void recordFlowCount() throws Exception {
        for (final String set : Arrays.asList(FILTERS_DEFAULT, FILTERS_PARTIAL_WINDOW, FILTERS_DSCP_FIRST)) {
            record("getFlowCount", params(set),
                    new JsonPrimitive(smartQueryService.getFlowCount(filters(set)).get()));
        }
    }

    private void recordApplications() throws Exception {
        for (final Object[] c : new Object[][]{{"", 1L}, {"", 10L}, {"h", 10L}, {"httz", 10L}, {"hyyps", 10L}}) {
            final String prefix = (String) c[0];
            final long limit = (Long) c[1];
            record("getApplications",
                    params(FILTERS_DEFAULT, "matchingPrefix", prefix, "limit", limit),
                    strings(smartQueryService.getApplications(prefix, limit, filters(FILTERS_DEFAULT)).get()));
        }

        for (final Object[] c : new Object[][]{{10, true, FILTERS_DEFAULT}, {10, false, FILTERS_DEFAULT},
                {1, true, FILTERS_DEFAULT}, {0, false, FILTERS_DEFAULT}, {0, true, FILTERS_DEFAULT},
                {1, false, FILTERS_PARTIAL_WINDOW}}) {
            final int n = (Integer) c[0];
            final boolean other = (Boolean) c[1];
            final String set = (String) c[2];
            record("getTopNApplicationSummaries", params(set, "N", n, "includeOther", other),
                    summaries(smartQueryService.getTopNApplicationSummaries(n, other, filters(set)).get(),
                            FlowQueryReferenceCaptureIT::stringJson));
        }

        for (final Object[] c : new Object[][]{{ImmutableSet.of("https"), false},
                {ImmutableSet.of("https", "http"), false}, {ImmutableSet.of("https"), true}}) {
            @SuppressWarnings("unchecked") final Set<String> apps = (Set<String>) c[0];
            final boolean other = (Boolean) c[1];
            record("getApplicationSummaries",
                    params(FILTERS_DEFAULT, "applications", apps, "includeOther", other),
                    summaries(smartQueryService.getApplicationSummaries(apps, other, filters(FILTERS_DEFAULT)).get(),
                            FlowQueryReferenceCaptureIT::stringJson));
        }

        for (final Object[] c : new Object[][]{{ImmutableSet.of("https"), false}, {ImmutableSet.of("https"), true},
                {ImmutableSet.of("http", "https"), false}, {ImmutableSet.of("http", "https"), true}}) {
            @SuppressWarnings("unchecked") final Set<String> apps = (Set<String>) c[0];
            final boolean other = (Boolean) c[1];
            record("getApplicationSeries",
                    params(FILTERS_DEFAULT, "applications", apps, "step", 10L, "includeOther", other),
                    series(smartQueryService.getApplicationSeries(apps, 10, other, filters(FILTERS_DEFAULT)).get(),
                            FlowQueryReferenceCaptureIT::stringJson, Function.identity()));
        }

        for (final Object[] c : new Object[][]{{10, false}, {2, true}, {1, false}}) {
            final int n = (Integer) c[0];
            final boolean other = (Boolean) c[1];
            record("getTopNApplicationSeries",
                    params(FILTERS_DEFAULT, "N", n, "step", 10L, "includeOther", other),
                    series(smartQueryService.getTopNApplicationSeries(n, 10, other, filters(FILTERS_DEFAULT)).get(),
                            FlowQueryReferenceCaptureIT::stringJson, Function.identity()));
        }
    }

    private void recordConversations() throws Exception {
        for (final long limit : new long[]{1L, 10L}) {
            record("getConversations",
                    params(FILTERS_DEFAULT, "locationPattern", ".*", "protocolPattern", ".*",
                            "lowerIPPattern", ".*", "upperIPPattern", ".*", "applicationPattern", ".*",
                            "limit", limit),
                    strings(smartQueryService.getConversations(".*", ".*", ".*", ".*", ".*", limit,
                            filters(FILTERS_DEFAULT)).get()));
        }

        for (final Object[] c : new Object[][]{{2, false}, {1, true}, {10, false}}) {
            final int n = (Integer) c[0];
            final boolean other = (Boolean) c[1];
            record("getTopNConversationSummaries", params(FILTERS_DEFAULT, "N", n, "includeOther", other),
                    summaries(smartQueryService.getTopNConversationSummaries(n, other, filters(FILTERS_DEFAULT)).get(),
                            FlowQueryReferenceCaptureIT::conversationJson));
        }

        for (final Object[] c : new Object[][]{{ImmutableSet.of(CONVO_HTTP), false},
                {ImmutableSet.of(CONVO_HTTPS), true}}) {
            @SuppressWarnings("unchecked") final Set<String> convos = (Set<String>) c[0];
            final boolean other = (Boolean) c[1];
            record("getConversationSummaries",
                    params(FILTERS_DEFAULT, "conversations", convos, "includeOther", other),
                    summaries(smartQueryService.getConversationSummaries(convos, other, filters(FILTERS_DEFAULT)).get(),
                            FlowQueryReferenceCaptureIT::conversationJson));
        }

        for (final boolean other : new boolean[]{false, true}) {
            final Set<String> convos = ImmutableSet.of(CONVO_HTTPS);
            record("getConversationSeries",
                    params(FILTERS_DEFAULT, "conversations", convos, "step", 10L, "includeOther", other),
                    series(smartQueryService.getConversationSeries(convos, 10, other, filters(FILTERS_DEFAULT)).get(),
                            FlowQueryReferenceCaptureIT::conversationJson,
                            FlowQueryReferenceCaptureIT::conversationKey));
        }

        for (final Object[] c : new Object[][]{{10, false}, {2, true}}) {
            final int n = (Integer) c[0];
            final boolean other = (Boolean) c[1];
            record("getTopNConversationSeries",
                    params(FILTERS_DEFAULT, "N", n, "step", 10L, "includeOther", other),
                    series(smartQueryService.getTopNConversationSeries(n, 10, other, filters(FILTERS_DEFAULT)).get(),
                            FlowQueryReferenceCaptureIT::conversationJson,
                            FlowQueryReferenceCaptureIT::conversationKey));
        }
    }

    private void recordHosts() throws Exception {
        for (final Object[] c : new Object[][]{{".*", 1L}, {".*", 10L}, {"10.1.1.*", 10L},
                {"10.1.*|192.168.*", 10L}}) {
            final String regex = (String) c[0];
            final long limit = (Long) c[1];
            record("getHosts", params(FILTERS_DEFAULT, "regex", regex, "limit", limit),
                    strings(smartQueryService.getHosts(regex, limit, filters(FILTERS_DEFAULT)).get()));
        }

        for (final Object[] c : new Object[][]{{10, false}, {1, true}, {0, false}, {0, true}}) {
            final int n = (Integer) c[0];
            final boolean other = (Boolean) c[1];
            record("getTopNHostSummaries", params(FILTERS_DEFAULT, "N", n, "includeOther", other),
                    summaries(smartQueryService.getTopNHostSummaries(n, other, filters(FILTERS_DEFAULT)).get(),
                            FlowQueryReferenceCaptureIT::hostJson));
        }

        for (final Object[] c : new Object[][]{{ImmutableSet.of("10.1.1.12"), false},
                {ImmutableSet.of("10.1.1.11", "10.1.1.12"), false}, {ImmutableSet.of("10.1.1.11"), true}}) {
            @SuppressWarnings("unchecked") final Set<String> hosts = (Set<String>) c[0];
            final boolean other = (Boolean) c[1];
            record("getHostSummaries", params(FILTERS_DEFAULT, "hosts", hosts, "includeOther", other),
                    summaries(smartQueryService.getHostSummaries(hosts, other, filters(FILTERS_DEFAULT)).get(),
                            FlowQueryReferenceCaptureIT::hostJson));
        }

        for (final Object[] c : new Object[][]{{ImmutableSet.of("10.1.1.12"), false},
                {ImmutableSet.of("10.1.1.12"), true},
                {ImmutableSet.of("10.1.1.12", "192.168.1.100"), false},
                {ImmutableSet.of("10.1.1.12", "192.168.1.100"), true}}) {
            @SuppressWarnings("unchecked") final Set<String> hosts = (Set<String>) c[0];
            final boolean other = (Boolean) c[1];
            record("getHostSeries",
                    params(FILTERS_DEFAULT, "hosts", hosts, "step", 10L, "includeOther", other),
                    series(smartQueryService.getHostSeries(hosts, 10, other, filters(FILTERS_DEFAULT)).get(),
                            FlowQueryReferenceCaptureIT::hostJson, FlowQueryReferenceCaptureIT::hostKey));
        }

        for (final Object[] c : new Object[][]{{10, false}, {2, true}, {1, false}}) {
            final int n = (Integer) c[0];
            final boolean other = (Boolean) c[1];
            record("getTopNHostSeries", params(FILTERS_DEFAULT, "N", n, "step", 10L, "includeOther", other),
                    series(smartQueryService.getTopNHostSeries(n, 10, other, filters(FILTERS_DEFAULT)).get(),
                            FlowQueryReferenceCaptureIT::hostJson, FlowQueryReferenceCaptureIT::hostKey));
        }
    }

    private void recordFields() throws Exception {
        for (final String set : Arrays.asList(FILTERS_DEFAULT, FILTERS_DSCP_FIRST)) {
            record("getFieldValues", params(set, "field", "DSCP"),
                    strings(smartQueryService.getFieldValues(LimitedCardinalityField.DSCP, filters(set)).get()));
        }

        for (final String set : Arrays.asList(FILTERS_DEFAULT, FILTERS_DSCP_ALL, FILTERS_DSCP_FIRST)) {
            record("getFieldSummaries", params(set, "field", "DSCP"),
                    summaries(smartQueryService.getFieldSummaries(LimitedCardinalityField.DSCP, filters(set)).get(),
                            FlowQueryReferenceCaptureIT::stringJson));
        }

        // Step 8 is the value FlowQueryIT uses; it does not divide the flow boundaries evenly, which
        // is exactly why it is worth recording alongside the round step of 10.
        for (final Object[] c : new Object[][]{{8L, FILTERS_DEFAULT}, {10L, FILTERS_DEFAULT},
                {8L, FILTERS_DSCP_FIRST}}) {
            final long step = (Long) c[0];
            final String set = (String) c[1];
            record("getFieldSeries", params(set, "field", "DSCP", "step", step),
                    series(smartQueryService.getFieldSeries(LimitedCardinalityField.DSCP, step, filters(set)).get(),
                            FlowQueryReferenceCaptureIT::stringJson, Function.identity()));
        }
    }

    /**
     * The cases that turn on how an undetermined direction is resolved.
     *
     * <p>Only the methods whose answers carry a direction: a summary splits bytes into in and out,
     * and a series keys its rows by {@link Directional}, so both say plainly which way a flow was
     * counted. The listing methods would agree whatever the answer is.
     */
    private void recordDirectionCases() {
        for (final String set : Arrays.asList(FILTERS_UNKNOWN_INTERFACE,
                FILTERS_UNKNOWN_INTERFACE_BOTH_SNMP, FILTERS_NO_INTERFACE)) {
            recordSafely("getFlowCount", params(set),
                    () -> new JsonPrimitive(smartQueryService.getFlowCount(filters(set)).get()));
            recordSafely("getTopNApplicationSummaries", params(set, "N", 10, "includeOther", false),
                    () -> summaries(smartQueryService.getTopNApplicationSummaries(10, false, filters(set)).get(),
                            FlowQueryReferenceCaptureIT::stringJson));
            recordSafely("getTopNApplicationSeries", params(set, "N", 10, "step", 10L, "includeOther", false),
                    () -> series(smartQueryService.getTopNApplicationSeries(10, 10, false, filters(set)).get(),
                            FlowQueryReferenceCaptureIT::stringJson, Function.identity()));
        }
        recordSafely("getTopNHostSummaries",
                params(FILTERS_UNKNOWN_INTERFACE, "N", 10, "includeOther", false),
                () -> summaries(smartQueryService.getTopNHostSummaries(
                        10, false, filters(FILTERS_UNKNOWN_INTERFACE)).get(),
                        FlowQueryReferenceCaptureIT::hostJson));
        recordSafely("getTopNConversationSummaries",
                params(FILTERS_UNKNOWN_INTERFACE, "N", 10, "includeOther", false),
                () -> summaries(smartQueryService.getTopNConversationSummaries(
                        10, false, filters(FILTERS_UNKNOWN_INTERFACE)).get(),
                        FlowQueryReferenceCaptureIT::conversationJson));
    }

    /**
     * The cases over the blind-spot corpus.
     *
     * <p>Direction-bearing methods plus a field summary, which is where the ECN flags surface. What
     * Elasticsearch answers here is not predicted: the point of recording it is that the sampling
     * multiplier, the attribution start and the direction rule become measurable rather than assumed.
     */
    private void recordExtendedCoverageCases() {
        recordSafely("getFlowCount", params(FILTERS_EXTENDED),
                () -> new JsonPrimitive(smartQueryService.getFlowCount(filters(FILTERS_EXTENDED)).get()));
        recordSafely("getTopNApplicationSummaries",
                params(FILTERS_EXTENDED, "N", 10, "includeOther", false),
                () -> summaries(smartQueryService.getTopNApplicationSummaries(
                        10, false, filters(FILTERS_EXTENDED)).get(),
                        FlowQueryReferenceCaptureIT::stringJson));
        recordSafely("getTopNApplicationSeries",
                params(FILTERS_EXTENDED, "N", 10, "step", 10L, "includeOther", false),
                () -> series(smartQueryService.getTopNApplicationSeries(
                        10, 10, false, filters(FILTERS_EXTENDED)).get(),
                        FlowQueryReferenceCaptureIT::stringJson, Function.identity()));
        recordSafely("getTopNHostSummaries",
                params(FILTERS_EXTENDED, "N", 10, "includeOther", false),
                () -> summaries(smartQueryService.getTopNHostSummaries(
                        10, false, filters(FILTERS_EXTENDED)).get(),
                        FlowQueryReferenceCaptureIT::hostJson));
        // Grouped by DSCP, so each blind-spot flow appears as its own row -- and the ECN flags,
        // which were false in every previously recorded row, finally have a true to record.
        recordSafely("getFieldSummaries", params(FILTERS_EXTENDED, "field", "DSCP"),
                () -> summaries(smartQueryService.getFieldSummaries(
                        LimitedCardinalityField.DSCP, filters(FILTERS_EXTENDED)).get(),
                        FlowQueryReferenceCaptureIT::stringJson));
    }

    /**
     * Records one case, recording a refusal as the answer rather than letting it end the run.
     *
     * <p>A query Elasticsearch will not serve is a real property of the reference backend and worth
     * capturing. Letting it propagate would abort the capture and lose every other case with it.
     */
    private void recordSafely(final String method, final JsonObject params,
                              final Callable<JsonElement> answer) {
        JsonElement result;
        try {
            result = answer.call();
        } catch (final Exception e) {
            final Throwable cause = e.getCause() != null ? e.getCause() : e;
            final JsonObject error = new JsonObject();
            error.addProperty("error", cause.getClass().getName() + ": " + cause.getMessage());
            result = error;
            LOG.warn("Recording {} {} as an error", method, params, cause);
        }
        record(method, params, result);
    }

    /** @return the recorded case with these exact method and params; fails if there is not one */
    private JsonObject findCase(final String method, final JsonObject params) {
        for (final JsonElement element : cases) {
            final JsonObject entry = element.getAsJsonObject();
            if (method.equals(entry.get("method").getAsString())
                    && params.equals(entry.getAsJsonObject("params"))) {
                return entry;
            }
        }
        throw new AssertionError("No recorded case for " + method + ' ' + params);
    }

    private void record(final String method, final JsonObject params, final JsonElement result) {
        final JsonObject entry = new JsonObject();
        entry.addProperty("method", method);
        entry.add("params", params);
        entry.add("result", result);
        cases.add(entry);
    }

    /** @param keyValues alternating key/value pairs describing the call, after the filter set name */
    private static JsonObject params(final String filterSet, final Object... keyValues) {
        // params is the only machine-readable description of the call that produced a result, so a
        // silently dropped key would leave a case whose parameters no longer reproduce its answer.
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("params() takes alternating key/value pairs; got an "
                    + "odd number of arguments, so a key would be dropped: "
                    + Arrays.toString(keyValues));
        }
        final JsonObject params = new JsonObject();
        params.addProperty("filters", filterSet);
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            final String key = (String) keyValues[i];
            final Object value = keyValues[i + 1];
            if (value instanceof Number) {
                params.addProperty(key, (Number) value);
            } else if (value instanceof Boolean) {
                params.addProperty(key, (Boolean) value);
            } else if (value instanceof Set) {
                // Iteration order, not sorted order. The summary methods return their results in the
                // order the caller's set yields them (RawFlowQueryService builds the result list by
                // iterating `from`), so sorting here would record an expectation that the recorded
                // parameters can no longer reproduce.
                final JsonArray array = new JsonArray();
                ((Set<?>) value).forEach(v -> array.add(String.valueOf(v)));
                params.add(key, array);
            } else {
                params.addProperty(key, String.valueOf(value));
            }
        }
        return params;
    }

    // ------------------------------------------------------------------------------------------
    // Result serialization
    // ------------------------------------------------------------------------------------------

    private static JsonArray strings(final List<String> values) {
        final JsonArray array = new JsonArray();
        values.forEach(array::add);  // order is the answer for the limit-bearing methods
        return array;
    }

    private static <T> JsonArray summaries(final List<TrafficSummary<T>> summaries,
                                           final Function<T, JsonElement> entityJson) {
        final JsonArray array = new JsonArray();
        for (final TrafficSummary<T> summary : summaries) {
            final JsonObject o = new JsonObject();
            o.add("entity", entityJson.apply(summary.getEntity()));
            o.addProperty("bytesIn", summary.getBytesIn());
            o.addProperty("bytesOut", summary.getBytesOut());
            o.addProperty("congestionEncountered", summary.isCongestionEncountered());
            o.addProperty("nonEcnCapableTransport", summary.isNonEcnCapableTransport());
            array.add(o);
        }
        return array;
    }

    /**
     * Serializes a series table with rows sorted by entity and direction and columns by timestamp.
     *
     * <p>Row order carries no meaning in a {@code Table} but iteration order is not guaranteed
     * stable, so it is imposed here rather than left to chance.
     */
    private static <T> JsonArray series(final Table<Directional<T>, Long, Double> table,
                                        final Function<T, JsonElement> entityJson,
                                        final Function<T, String> entityKey) {
        final List<Map.Entry<String, JsonObject>> rows = new ArrayList<>();
        for (final Directional<T> row : table.rowKeySet()) {
            final JsonObject o = new JsonObject();
            o.add("entity", entityJson.apply(row.getValue()));
            o.addProperty("ingress", row.isIngress());
            final JsonObject values = new JsonObject();
            new TreeMap<>(table.row(row)).forEach((ts, v) -> values.addProperty(Long.toString(ts), v));
            o.add("values", values);
            rows.add(new AbstractMap.SimpleEntry<>(
                    entityKey.apply(row.getValue()) + ' ' + row.isIngress(), o));
        }
        rows.sort(Map.Entry.comparingByKey());
        final JsonArray array = new JsonArray();
        rows.forEach(e -> array.add(e.getValue()));
        return array;
    }

    private static JsonElement stringJson(final String value) {
        return new JsonPrimitive(value);
    }

    private static JsonElement hostJson(final Host host) {
        final JsonObject o = new JsonObject();
        o.addProperty("ip", host.getIp());
        o.addProperty("hostname", host.getHostname().orElse(null));
        return o;
    }

    private static String hostKey(final Host host) {
        return host.getIp() + ' ' + host.getHostname().orElse("");
    }

    private static JsonElement conversationJson(final Conversation convo) {
        final JsonObject o = new JsonObject();
        o.addProperty("location", convo.getLocation());
        o.addProperty("protocol", convo.getProtocol());
        o.addProperty("lowerIp", convo.getLowerIp());
        o.addProperty("lowerHostname", convo.getLowerHostname().orElse(null));
        o.addProperty("upperIp", convo.getUpperIp());
        o.addProperty("upperHostname", convo.getUpperHostname().orElse(null));
        o.addProperty("application", convo.getApplication());
        return o;
    }

    private static String conversationKey(final Conversation convo) {
        return convo.getLocation() + ' ' + convo.getProtocol() + ' ' + convo.getLowerIp()
                + ' ' + convo.getUpperIp() + ' ' + convo.getApplication();
    }

    /**
     * The corpus as the documents Elasticsearch actually indexed.
     *
     * <p>Recorded so the other backend can be fed identical input without depending on OpenNMS
     * enrichment: {@code FlowJsonSerializer} in the VictoriaLogs module emits these same field names
     * deliberately, so these objects are directly ingestible there.
     *
     * <p>Strictly this is the {@link FlowDocument} re-serialized here, not a capture of the bytes
     * {@code ElasticFlowRepository} put on the wire — the same objects, but through a default
     * {@link Gson} rather than the repository's. That is sound only while {@code FlowDocument} needs
     * no custom adapters to round-trip, which is true today: every field is a primitive, a String, a
     * boxed number or a collection of those, and the wire names come from {@code @SerializedName}
     * annotations a default Gson honours. If that ever stops being true, the recorded corpus and the
     * recorded answers would be computed over different documents, and the failure would look like a
     * disagreement in the other backend.
     */
    private static JsonArray corpusJson(
            final List<org.opennms.netmgt.flows.processing.enrichment.EnrichedFlow> flows) {
        final Gson gson = new Gson();
        final JsonArray array = new JsonArray();
        flows.stream()
                .map(FlowDocument::from)
                .map(gson::toJsonTree)
                .forEach(array::add);
        return array;
    }

    // ------------------------------------------------------------------------------------------
    // Corpus
    // ------------------------------------------------------------------------------------------

    /**
     * Everything the reference is computed over: the original eight flows plus the
     * undetermined-direction ones.
     */
    private static List<Flow> getCorpus() {
        final List<Flow> corpus = new ArrayList<>(getDirectedCorpus());
        corpus.addAll(getUnknownDirectionCorpus());
        corpus.addAll(getExtendedCoverageCorpus());
        return corpus;
    }

    /**
     * Flows whose direction is {@code unknown}, all on {@link #UNKNOWN_SNMP_INTERFACE_ID}.
     *
     * <p>Direction is an optional NetFlow v9 / IPFIX field, and {@code Netflow9MessageBuilder}
     * defaults to {@code Direction.UNKNOWN} when an exporter omits it — so for such a device every
     * record looks like these, not a stray few.
     *
     * <p>Each flow isolates one branch of the direction script in {@code common.ftl}, which tests
     * {@code input_snmp} first and {@code output_snmp} second. The interesting one is the second:
     * the flow model writes only the interface the direction implies, so a record that names just
     * its output has <em>no</em> {@code netflow.input_snmp} field at all, and reading {@code .value}
     * off an absent field is an error in Painless rather than a miss. Whether Elasticsearch resolves
     * that flow, drops it, or fails the whole query is precisely what is not knowable from reading
     * the template, and is why these are recorded rather than asserted by hand.
     *
     * <p>Separate builders because the interface ids are sticky: setting an input id would leave it
     * set for the flow that must not have one.
     */
    private static List<Flow> getUnknownDirectionCorpus() {
        final List<Flow> flows = new ArrayList<>();
        // Input interface only, so the script's first test matches. 172.16.0.10:40000 ->
        // 10.2.2.10:80 (400 bytes in [60,70]), http.
        flows.addAll(new FlowBuilder()
                .withDirection(Direction.UNKNOWN)
                .withInputSnmpInterfaceId(UNKNOWN_SNMP_INTERFACE_ID)
                .withTos(TOS_UNKNOWN_INPUT_ONLY)
                .withFlow(Instant.ofEpochMilli(60), Instant.ofEpochMilli(70),
                        "172.16.0.10", 40000, "10.2.2.10", 80, 400)
                .build());
        // Output interface only, so the script must first read an absent input_snmp.
        // 10.2.2.10:80 -> 172.16.0.10:40000 (4000 bytes in [60,70]), http.
        flows.addAll(new FlowBuilder()
                .withDirection(Direction.UNKNOWN)
                .withOutputSnmpInterfaceId(UNKNOWN_SNMP_INTERFACE_ID)
                .withTos(TOS_UNKNOWN_OUTPUT_ONLY)
                .withFlow(Instant.ofEpochMilli(60), Instant.ofEpochMilli(70),
                        "10.2.2.10", 80, "172.16.0.10", 40000, 4000)
                .build());
        // Both interfaces present, the input one not the interface being asked about: the control
        // that separates "the script fell through" from "a field was missing".
        // 10.2.2.11:443 -> 172.16.0.11:40001 (800 bytes in [70,80]), https.
        flows.addAll(new FlowBuilder()
                .withDirection(Direction.UNKNOWN)
                .withInputSnmpInterfaceId(UNRELATED_SNMP_INTERFACE_ID)
                .withOutputSnmpInterfaceId(UNKNOWN_SNMP_INTERFACE_ID)
                .withTos(TOS_UNKNOWN_BOTH)
                .withFlow(Instant.ofEpochMilli(70), Instant.ofEpochMilli(80),
                        "10.2.2.11", 443, "172.16.0.11", 40001, 800)
                .build());
        return flows;
    }

    /**
     * Flows covering what the original corpus could not distinguish.
     *
     * <p>Each closes a blind spot found by reviewing the oracle as a measuring instrument — cases
     * where a wrong implementation reproduced every recorded answer:
     *
     * <ul>
     *   <li><strong>Sampled.</strong> No original flow carried {@code netflow.sampling_interval},
     *       which is the fourth field {@code proportional_sum} reads. A backend ignoring the
     *       sampling multiplier scored full marks and would then under-report every sampled exporter
     *       by the sampling rate — and sampling is the normal case on the hardware this targets.
     *   <li><strong>Delta after first.</strong> Every original flow had
     *       {@code delta_switched == first_switched}, so a backend attributing from
     *       {@code first_switched} was indistinguishable from one attributing from
     *       {@code delta_switched}. Attribution runs from delta, and for a multi-record flow the two
     *       differ; this is the single most load-bearing field in the feature.
     *   <li><strong>Direction disagrees with interface.</strong> Every original flow carried exactly
     *       the interface its direction implied, so a backend that ignored {@code netflow.direction}
     *       and inferred it from an interface match agreed everywhere. This one is egress on an
     *       interface that is both its input and its output, so the two rules give opposite answers.
     *   <li><strong>Congestion.</strong> Every original flow had {@code ecn == 0}, leaving
     *       {@code congestionEncountered} false in all 63 recorded rows and the max-over-bucket
     *       semantics entirely unmeasured.
     *   <li><strong>Unknown with both interfaces equal.</strong> The undetermined-direction trio
     *       covers input-only, output-only and input-elsewhere, but not the case where both name the
     *       queried interface — so which of the script's two tests wins was never pinned down.
     * </ul>
     *
     * <p>Distinct DSCP values again, so a filter can select any one of them and a failure says which.
     */
    private static List<Flow> getExtendedCoverageCorpus() {
        final List<Flow> flows = new ArrayList<>();
        // Sampled 1-in-10. 10.3.3.10:443 -> 172.17.0.10:40100 (500 bytes in [200000,200010]).
        flows.addAll(new FlowBuilder()
                .withSnmpInterfaceId(EXTENDED_SNMP_INTERFACE_ID)
                .withDirection(Direction.INGRESS)
                .withTos(TOS_SAMPLED)
                .withSamplingInterval(10.0d)
                .withFlow(Instant.ofEpochMilli(200_000), Instant.ofEpochMilli(200_010),
                        "10.3.3.10", 443, "172.17.0.10", 40100, 500)
                .build());
        // delta_switched is 20ms after first_switched, so attribution from the wrong one shows.
        flows.addAll(new FlowBuilder()
                .withSnmpInterfaceId(EXTENDED_SNMP_INTERFACE_ID)
                .withDirection(Direction.INGRESS)
                .withTos(TOS_DELTA_AFTER_FIRST)
                .withFlow(Instant.ofEpochMilli(200_000), Instant.ofEpochMilli(200_020),
                        Instant.ofEpochMilli(200_040), "10.3.3.11", 443, "172.17.0.11", 40101, 600)
                .build());
        // Egress, but the queried interface is also its input: the direction field says egress and
        // an interface-first rule says ingress. Admitted by filter_snmp_interface.ftl's second
        // clause (output matches and direction is egress).
        flows.addAll(new FlowBuilder()
                .withDirection(Direction.EGRESS)
                .withSnmpInterfaces(EXTENDED_SNMP_INTERFACE_ID, EXTENDED_SNMP_INTERFACE_ID)
                .withTos(TOS_DIRECTION_DISAGREES)
                .withFlow(Instant.ofEpochMilli(200_000), Instant.ofEpochMilli(200_030),
                        "10.3.3.12", 80, "172.17.0.12", 40102, 700)
                .build());
        // ECN codepoint 3: congestion encountered.
        flows.addAll(new FlowBuilder()
                .withSnmpInterfaceId(EXTENDED_SNMP_INTERFACE_ID)
                .withDirection(Direction.INGRESS)
                .withTos(TOS_CONGESTION)
                .withFlow(Instant.ofEpochMilli(200_000), Instant.ofEpochMilli(200_050),
                        "10.3.3.13", 80, "172.17.0.13", 40103, 800)
                .build());
        // Undetermined direction with the queried interface at both ends.
        flows.addAll(new FlowBuilder()
                .withDirection(Direction.UNKNOWN)
                .withInputSnmpInterfaceId(EXTENDED_SNMP_INTERFACE_ID)
                .withOutputSnmpInterfaceId(EXTENDED_SNMP_INTERFACE_ID)
                .withTos(TOS_UNKNOWN_BOTH_INTERFACES)
                .withFlow(Instant.ofEpochMilli(200_000), Instant.ofEpochMilli(200_060),
                        "10.3.3.14", 443, "172.17.0.14", 40104, 900)
                .build());
        return flows;
    }

    /**
     * The eight flows the reference was originally computed over — a pinned copy of
     * {@code FlowQueryIT}'s default set. Between them they cover both directions, classified and
     * unclassified applications, present and absent hostnames, four distinct TOS values, and four
     * conversations of differing weight.
     */
    private static List<Flow> getDirectedCorpus() {
        return new FlowBuilder()
                .withSnmpInterfaceId(SNMP_INTERFACE_ID)
                // 192.168.1.100:43444 <-> 10.1.1.11:80 (110 bytes in [3,15])
                .withDirection(Direction.INGRESS)
                .withTos(4 + 64)
                .withFlow(Instant.ofEpochMilli(3), Instant.ofEpochMilli(15),
                        "192.168.1.100", 43444, "10.1.1.11", 80, 10)
                .withDirection(Direction.EGRESS)
                .withTos(8 + 128)
                .withFlow(Instant.ofEpochMilli(3), Instant.ofEpochMilli(15),
                        "10.1.1.11", 80, "192.168.1.100", 43444, 100)
                // 192.168.1.100:43445 <-> 10.1.1.12:443 (1100 bytes in [13,26])
                .withDirection(Direction.INGRESS)
                .withHostnames(null, "la.le.lu")
                .withTos(16 + 64)
                .withFlow(Instant.ofEpochMilli(13), Instant.ofEpochMilli(26),
                        "192.168.1.100", 43445, "10.1.1.12", 443, 100)
                .withDirection(Direction.EGRESS)
                .withHostnames("la.le.lu", null)
                .withTos(32 + 128)
                .withFlow(Instant.ofEpochMilli(13), Instant.ofEpochMilli(26),
                        "10.1.1.12", 443, "192.168.1.100", 43445, 1000)
                // 192.168.1.101:43442 <-> 10.1.1.12:443 (1210 bytes in [14,45])
                .withDirection(Direction.INGRESS)
                .withHostnames("ingress.only", "la.le.lu")
                .withFlow(Instant.ofEpochMilli(14), Instant.ofEpochMilli(45),
                        "192.168.1.101", 43442, "10.1.1.12", 443, 110)
                .withDirection(Direction.EGRESS)
                .withHostnames("la.le.lu", null)
                .withFlow(Instant.ofEpochMilli(14), Instant.ofEpochMilli(45),
                        "10.1.1.12", 443, "192.168.1.101", 43442, 1100)
                // 192.168.1.102:50000 <-> 10.1.1.13:50001 (300 bytes in [50,52]), unclassified
                .withDirection(Direction.INGRESS)
                .withFlow(Instant.ofEpochMilli(50), Instant.ofEpochMilli(52),
                        "192.168.1.102", 50000, "10.1.1.13", 50001, 200)
                .withDirection(Direction.EGRESS)
                .withFlow(Instant.ofEpochMilli(50), Instant.ofEpochMilli(52),
                        "10.1.1.13", 50001, "192.168.1.102", 50000, 100)
                .build();
    }
}
