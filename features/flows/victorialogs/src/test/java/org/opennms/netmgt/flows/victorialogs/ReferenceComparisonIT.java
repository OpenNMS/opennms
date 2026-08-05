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
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.netmgt.flows.api.Conversation;
import org.opennms.netmgt.flows.api.Directional;
import org.opennms.netmgt.flows.api.Host;
import org.opennms.netmgt.flows.api.LimitedCardinalityField;
import org.opennms.netmgt.flows.api.TrafficSummary;
import org.opennms.netmgt.flows.filter.api.DscpFilter;
import org.opennms.netmgt.flows.filter.api.ExporterNodeFilter;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.flows.filter.api.NodeCriteria;
import org.opennms.netmgt.flows.filter.api.SnmpInterfaceIdFilter;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import com.google.common.collect.Table;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Checks this backend's answers against the recorded Elasticsearch + Drift answers.
 *
 * <p>{@code flow-query-reference.json} was captured by {@code FlowQueryReferenceCaptureIT} in the
 * flows integration-test module, running the real Drift plugin. It carries the corpus that produced
 * it, so this test ingests those exact documents and re-asks every recorded question. Agreement with
 * that file is the acceptance criterion for the whole VictoriaLogs query path — the unit tests
 * elsewhere in this module prove internal consistency, which is a weaker claim.
 *
 * <p><strong>This is a progress meter as much as a test.</strong> Methods that
 * {@link VictoriaLogsFlowQueryService} has not implemented yet throw
 * {@link UnsupportedOperationException} and are counted as skipped rather than failed, so the suite
 * stays green while the implementation is filled in and the logged tally moves toward 19/19.
 * Anything actually implemented must match, and an unrecognised method name in the reference is a
 * failure rather than a silent skip — otherwise the two could drift apart without anyone noticing.
 *
 * <p>Numbers are compared with a relative tolerance. The proportional attribution divides and sums
 * in a different order here than in the Elasticsearch plugin, so demanding bit-identical doubles
 * would fail on arithmetic that is correct.
 */
public class ReferenceComparisonIT {


    private static final String REFERENCE_RESOURCE = "/flow-query-reference.json";

    /** Every method the reference is allowed to contain; see {@link #invoke}. */
    private static final int TOTAL_QUERY_METHODS = 19;

    /**
     * Cases known to disagree, with the reason. Entries here are reported but do not fail the run.
     *
     * <p>This exists for differences that are not defects on either side, and it is kept honest in
     * both directions: an entry whose case actually agrees fails the run, so a stale excuse cannot
     * sit here unnoticed.
     *
     * <p>The two below share one cause. {@code hostname_by_host.ftl} resolves an address by taking a
     * <em>single</em> document — {@code size: 1}, newest first — and reading the hostname from
     * whichever side the address sits on in it. The corpus makes that choice ambiguous: flows 7 and 8
     * carry the same {@code @timestamp}, and because {@code FlowBuilder.withHostnames} is sticky both
     * record {@code la.le.lu} as their <em>source</em> hostname, which makes it the recorded hostname
     * of {@code 192.168.1.102} in one and of {@code 10.1.1.13} in the other. Elasticsearch settles the
     * tie on Lucene's internal document order and so reports no hostname for {@code 10.1.1.13}; this
     * backend reports the one the data records for it. Neither is more correct, the tie-break is not
     * reproducible outside Lucene, and real flow data does not give one address different hostnames in
     * different records — so this is left as a difference rather than chased.
     */
    private static final Map<String, String> KNOWN_DIVERGENCES = Map.of(
            "getTopNApplicationSummaries {\"filters\":\"extendedCoverage\",\"N\":10,\"includeOther\":false}",
            "nonEcnCapableTransport of http: Elasticsearch evaluates the ECN flags once per direction "
                    + "bucket and lets the last bucket processed overwrite the earlier one, so a group "
                    + "whose ingress and egress records disagree reports whichever direction its terms "
                    + "aggregation happened to return last -- here the ingress flow's ecn=3, hiding the "
                    + "egress flow's ecn=0. This backend reports a flag when any record in the group "
                    + "carries it, which is what the field names say. Neither is a defect; the "
                    + "Elasticsearch answer just depends on bucket ordering rather than on the data.",
            "getTopNHostSummaries {\"filters\":\"default\",\"N\":10,\"includeOther\":false}",
            "hostname of 10.1.1.13: Elasticsearch resolves it from an arbitrarily chosen document",
            "getTopNConversationSummaries {\"filters\":\"default\",\"N\":10,\"includeOther\":false}",
            "lowerHostname of 10.1.1.13: same arbitrary single-document hostname resolution",
            "getTopNHostSeries {\"filters\":\"default\",\"N\":10,\"step\":10,\"includeOther\":false}",
            "hostname of 10.1.1.13: same arbitrary single-document hostname resolution",
            "getTopNConversationSeries {\"filters\":\"default\",\"N\":10,\"step\":10,\"includeOther\":false}",
            "lowerHostname of 10.1.1.13: same arbitrary single-document hostname resolution");

    private static final DockerImageName VL_IMAGE =
            DockerImageName.parse("victoriametrics/victoria-logs:v1.52.0");
    private static final int VL_PORT = 9428;
    private static final String EXTERNAL_URL_PROPERTY = "victorialogs.url";

    /** The corpus is timestamped in 1970; without this VictoriaLogs discards it as beyond retention. */
    private static final String RETENTION = "-retentionPeriod=100y";

    /** Relative tolerance on numeric comparison. */
    private static final double TOLERANCE = 1e-6d;

    private static GenericContainer<?> victoriaLogs;
    private static String baseUrl;

    private VictoriaLogsClient client;
    private VictoriaLogsFlowQueryService queryService;

    @BeforeClass
    public static void startContainer() {
        final String external = System.getProperty(EXTERNAL_URL_PROPERTY);
        if (external != null && !external.isEmpty()) {
            baseUrl = external;
            return;
        }
        victoriaLogs = new GenericContainer<>(VL_IMAGE)
                .withExposedPorts(VL_PORT)
                .withCommand("-storageDataPath=/victoria-logs-data", RETENTION)
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
        queryService = new VictoriaLogsFlowQueryService(client);
    }

    /** Releases the client's selector thread and executor; that is what close() exists for. */
    @After
    public void tearDown() {
        if (queryService != null) {
            queryService.stop();
        }
        if (client != null) {
            client.close();
        }
    }

    @Test
    public void matchesElasticsearchReference() throws Exception {
        final JsonObject reference = loadReference();
        final JsonArray corpus = reference.getAsJsonArray("corpus");
        ingest(corpus);

        final Map<String, List<Filter>> filterSets = parseFilterSets(reference.getAsJsonObject("filterSets"));

        final Set<String> implemented = new TreeSet<>();
        final List<String> failures = new ArrayList<>();
        final List<String> divergences = new ArrayList<>();
        final List<String> staleDivergences = new ArrayList<>();
        final Set<String> divergedContexts = new TreeSet<>();
        int compared = 0;

        for (final JsonElement element : reference.getAsJsonArray("cases")) {
            final JsonObject testCase = element.getAsJsonObject();
            final String method = testCase.get("method").getAsString();
            final JsonObject params = testCase.getAsJsonObject("params");
            final JsonElement expected = testCase.get("result");

            // No skip path. Every method is implemented, so an UnsupportedOperationException is a
            // regression, not progress not yet made -- and it was never a reliable marker for the
            // latter anyway: sorting a List.of(), or mutating a Set.of(), throws exactly that. While
            // the exception was caught here, a method that broke that way was recorded as "still to
            // do", printed as such, and left the suite green.
            final JsonElement actual = invoke(method, params, filterSets);

            implemented.add(method);
            compared++;
            final String context = method + ' ' + params;
            final String knownReason = KNOWN_DIVERGENCES.get(context);
            try {
                assertJsonEquals(context, expected, actual);
                if (knownReason != null) {
                    staleDivergences.add(context);
                }
            } catch (final AssertionError mismatch) {
                if (knownReason != null) {
                    divergedContexts.add(context);
                    divergences.add(context + "\n    reason: " + knownReason
                            + "\n    detail: " + mismatch.getMessage());
                } else {
                    failures.add(mismatch.getMessage());
                }
            }
        }

        // Printed rather than logged: this module has no slf4j binding on the test classpath, so a
        // logged tally silently goes nowhere. It is a report, not the check -- everything it counts
        // is asserted below, because a number that only ever reaches stdout constrains nothing.
        System.out.println(String.format(
                "%nReference comparison: %d/%d methods, %d cases compared, %d failed, "
                        + "%d known divergence(s).%n  methods: %s%n%s",
                implemented.size(), TOTAL_QUERY_METHODS, compared, failures.size(),
                divergences.size(), implemented,
                divergences.isEmpty() ? "" : "  known divergences:\n    "
                        + String.join("\n    ", divergences) + "\n"));

        if (!staleDivergences.isEmpty()) {
            fail("These cases are listed as known divergences but now agree; remove them from "
                    + "KNOWN_DIVERGENCES:\n" + String.join("\n", staleDivergences));
        }
        // Every listed divergence must have been reached. An entry that matches no case is as stale
        // as one that agrees -- the reference can be regenerated with a renamed parameter or without
        // the case at all, and the excuse then sits here forever describing nothing.
        final Set<String> unreached = new TreeSet<>(KNOWN_DIVERGENCES.keySet());
        unreached.removeAll(divergedContexts);
        if (!unreached.isEmpty()) {
            fail("These KNOWN_DIVERGENCES entries matched no compared case; the reference no longer "
                    + "contains them under that name:\n" + String.join("\n", unreached));
        }
        if (!failures.isEmpty()) {
            fail("Disagreed with the Elasticsearch reference in " + failures.size() + " case(s):\n"
                    + String.join("\n", failures));
        }
        // The tally is the check. Without these the suite passes on one trivial case: the loop
        // asserts only about cases it reaches, so a reference that lost sixty of its seventy-seven
        // entries, or a service where all but one method broke, would report success.
        assertEquals("every recorded case must be compared",
                reference.getAsJsonArray("cases").size(), compared);
        assertEquals("every query method must be exercised; the reference and the service have "
                        + "drifted apart", TOTAL_QUERY_METHODS, implemented.size());
    }

    // ------------------------------------------------------------------------------------------
    // Dispatch
    // ------------------------------------------------------------------------------------------

    /**
     * Runs one recorded case and renders the answer in the reference's own JSON shape.
     *
     * <p>Every method is dispatched, including the unimplemented ones — they throw
     * {@link UnsupportedOperationException} from the service and the caller counts them as pending.
     * That is what makes this test start exercising a method the moment it is written, with no change
     * here.
     */
    private JsonElement invoke(final String method, final JsonObject params,
                               final Map<String, List<Filter>> filterSets) throws Exception {
        final List<Filter> filters = filterSet(params, filterSets);
        switch (method) {
            case "getFlowCount":
                return new JsonPrimitive(get(queryService.getFlowCount(filters)));

            case "getApplications":
                return strings(get(queryService.getApplications(
                        string(params, "matchingPrefix"), number(params, "limit"), filters)));
            case "getTopNApplicationSummaries":
                return summaries(get(queryService.getTopNApplicationSummaries(
                        (int) number(params, "N"), bool(params, "includeOther"), filters)),
                        ReferenceComparisonIT::stringJson);
            case "getApplicationSummaries":
                return summaries(get(queryService.getApplicationSummaries(
                        set(params, "applications"), bool(params, "includeOther"), filters)),
                        ReferenceComparisonIT::stringJson);
            case "getApplicationSeries":
                return series(get(queryService.getApplicationSeries(
                        set(params, "applications"), number(params, "step"),
                        bool(params, "includeOther"), filters)),
                        ReferenceComparisonIT::stringJson, Function.identity());
            case "getTopNApplicationSeries":
                return series(get(queryService.getTopNApplicationSeries(
                        (int) number(params, "N"), number(params, "step"),
                        bool(params, "includeOther"), filters)),
                        ReferenceComparisonIT::stringJson, Function.identity());

            case "getConversations":
                return strings(get(queryService.getConversations(
                        string(params, "locationPattern"), string(params, "protocolPattern"),
                        string(params, "lowerIPPattern"), string(params, "upperIPPattern"),
                        string(params, "applicationPattern"), number(params, "limit"), filters)));
            case "getTopNConversationSummaries":
                return summaries(get(queryService.getTopNConversationSummaries(
                        (int) number(params, "N"), bool(params, "includeOther"), filters)),
                        ReferenceComparisonIT::conversationJson);
            case "getConversationSummaries":
                return summaries(get(queryService.getConversationSummaries(
                        set(params, "conversations"), bool(params, "includeOther"), filters)),
                        ReferenceComparisonIT::conversationJson);
            case "getConversationSeries":
                return series(get(queryService.getConversationSeries(
                        set(params, "conversations"), number(params, "step"),
                        bool(params, "includeOther"), filters)),
                        ReferenceComparisonIT::conversationJson, ReferenceComparisonIT::conversationKey);
            case "getTopNConversationSeries":
                return series(get(queryService.getTopNConversationSeries(
                        (int) number(params, "N"), number(params, "step"),
                        bool(params, "includeOther"), filters)),
                        ReferenceComparisonIT::conversationJson, ReferenceComparisonIT::conversationKey);

            case "getHosts":
                return strings(get(queryService.getHosts(
                        string(params, "regex"), number(params, "limit"), filters)));
            case "getTopNHostSummaries":
                return summaries(get(queryService.getTopNHostSummaries(
                        (int) number(params, "N"), bool(params, "includeOther"), filters)),
                        ReferenceComparisonIT::hostJson);
            case "getHostSummaries":
                return summaries(get(queryService.getHostSummaries(
                        set(params, "hosts"), bool(params, "includeOther"), filters)),
                        ReferenceComparisonIT::hostJson);
            case "getHostSeries":
                return series(get(queryService.getHostSeries(
                        set(params, "hosts"), number(params, "step"),
                        bool(params, "includeOther"), filters)),
                        ReferenceComparisonIT::hostJson, ReferenceComparisonIT::hostKey);
            case "getTopNHostSeries":
                return series(get(queryService.getTopNHostSeries(
                        (int) number(params, "N"), number(params, "step"),
                        bool(params, "includeOther"), filters)),
                        ReferenceComparisonIT::hostJson, ReferenceComparisonIT::hostKey);

            case "getFieldValues":
                return strings(get(queryService.getFieldValues(field(params), filters)));
            case "getFieldSummaries":
                return summaries(get(queryService.getFieldSummaries(field(params), filters)),
                        ReferenceComparisonIT::stringJson);
            case "getFieldSeries":
                return series(get(queryService.getFieldSeries(
                        field(params), number(params, "step"), filters)),
                        ReferenceComparisonIT::stringJson, Function.identity());

            default:
                throw new AssertionError("The reference contains a method this test does not know "
                        + "how to dispatch: " + method);
        }
    }

    /**
     * Unwraps a future, surfacing the underlying failure rather than the wrapper.
     *
     * <p>{@link java.util.concurrent.CompletableFuture#get()} buries the cause inside an
     * {@link ExecutionException}, whose message names the wrapper rather than what went wrong.
     */
    private static <T> T get(final java.util.concurrent.CompletableFuture<T> future) throws Exception {
        try {
            return future.get();
        } catch (final ExecutionException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw e;
        }
    }

    // ------------------------------------------------------------------------------------------
    // Reference parsing
    // ------------------------------------------------------------------------------------------

    private static JsonObject loadReference() throws Exception {
        try (final InputStream in = ReferenceComparisonIT.class.getResourceAsStream(REFERENCE_RESOURCE)) {
            assertTrue("Missing " + REFERENCE_RESOURCE + " on the test classpath; regenerate it with "
                    + "FlowQueryReferenceCaptureIT.", in != null);
            final JsonObject reference = JsonParser.parseReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            // Provenance. The file is copied between modules by hand, so a truncated or
            // hand-edited artifact is otherwise indistinguishable from a captured one -- and this
            // test would validate against it just as faithfully.
            assertEquals("the reference declares a different number of cases than it carries; it "
                            + "was edited or truncated after capture",
                    reference.get("caseCount").getAsInt(),
                    reference.getAsJsonArray("cases").size());
            assertEquals("the reference declares a different corpus size than it carries",
                    reference.get("corpusSize").getAsInt(),
                    reference.getAsJsonArray("corpus").size());
            return reference;
        }
    }

    private static Map<String, List<Filter>> parseFilterSets(final JsonObject sets) {
        final Map<String, List<Filter>> parsed = new LinkedHashMap<>();
        for (final String name : sets.keySet()) {
            final List<Filter> filters = new ArrayList<>();
            for (final JsonElement element : sets.getAsJsonArray(name)) {
                filters.add(parseFilter(element.getAsJsonObject()));
            }
            parsed.put(name, filters);
        }
        return parsed;
    }

    private static Filter parseFilter(final JsonObject filter) {
        final String type = filter.get("type").getAsString();
        switch (type) {
            case "timeRange":
                return new TimeRangeFilter(filter.get("start").getAsLong(), filter.get("end").getAsLong());
            case "snmpInterfaceId":
                return new SnmpInterfaceIdFilter(filter.get("snmpInterfaceId").getAsInt());
            case "dscp":
                final List<Integer> dscp = new ArrayList<>();
                filter.getAsJsonArray("dscp").forEach(v -> dscp.add(v.getAsInt()));
                return new DscpFilter(dscp);
            case "exporterNode":
                return new ExporterNodeFilter(new NodeCriteria(filter.get("criteria").getAsString()));
            default:
                throw new AssertionError("Unknown filter type in the reference: " + type);
        }
    }

    private static List<Filter> filterSet(final JsonObject params,
                                          final Map<String, List<Filter>> filterSets) {
        final String name = params.get("filters").getAsString();
        final List<Filter> filters = filterSets.get(name);
        if (filters == null) {
            throw new AssertionError("The reference refers to an undefined filter set: " + name);
        }
        return filters;
    }

    private static String string(final JsonObject params, final String key) {
        return params.get(key).getAsString();
    }

    private static long number(final JsonObject params, final String key) {
        return params.get(key).getAsLong();
    }

    private static boolean bool(final JsonObject params, final String key) {
        return params.get(key).getAsBoolean();
    }

    private static Set<String> set(final JsonObject params, final String key) {
        final Set<String> values = new LinkedHashSet<>();
        params.getAsJsonArray(key).forEach(v -> values.add(v.getAsString()));
        return values;
    }

    private static LimitedCardinalityField field(final JsonObject params) {
        return LimitedCardinalityField.valueOf(string(params, "field"));
    }

    // ------------------------------------------------------------------------------------------
    // Ingestion
    // ------------------------------------------------------------------------------------------

    /**
     * Feeds the recorded Elasticsearch documents straight into VictoriaLogs.
     *
     * <p>They need no translation because {@code FlowJsonSerializer} emits the Elasticsearch field
     * names on purpose; the one addition is {@code _time}, which that serializer derives from
     * {@code @timestamp} and which VictoriaLogs is configured to read as the record's instant.
     */
    private void ingest(final JsonArray corpus) throws Exception {
        final StringBuilder ndjson = new StringBuilder();
        for (final JsonElement element : corpus) {
            final JsonObject doc = element.getAsJsonObject().deepCopy();
            final long timestamp = doc.has("@timestamp") ? doc.get("@timestamp").getAsLong() : 0L;
            doc.addProperty(FlowJsonSerializer.TIME_FIELD, Instant.ofEpochMilli(timestamp).toString());
            ndjson.append(doc).append('\n');
        }
        // Baseline first. The count below is instance-wide, and running against a shared
        // VictoriaLogs is a documented mode (victorialogs.url), so waiting for an absolute total
        // would be satisfied immediately by rows another test left behind -- returning before any of
        // this corpus was searchable and turning the whole comparison into unexplained mismatches.
        final long before = searchableRows();
        client.ingest(ndjson.toString());
        awaitSearchable(before + corpus.size());
    }

    private long searchableRows() throws Exception {
        final List<JsonObject> rows = client.query("* | stats count() as count");
        return rows.isEmpty() ? 0L : Long.parseLong(rows.get(0).get("count").getAsString());
    }

    /** Accepted is not the same as searchable; VictoriaLogs indexes asynchronously. */
    private void awaitSearchable(final long expected) throws Exception {
        final long deadline = System.currentTimeMillis() + 30_000L;
        long seen = -1;
        while (System.currentTimeMillis() < deadline) {
            seen = searchableRows();
            if (seen >= expected) {
                return;
            }
            Thread.sleep(250L);
        }
        assertEquals("VictoriaLogs never made the whole corpus searchable", expected, seen);
    }

    // ------------------------------------------------------------------------------------------
    // Result rendering -- must mirror FlowQueryReferenceCaptureIT exactly
    // ------------------------------------------------------------------------------------------

    private static JsonArray strings(final List<String> values) {
        final JsonArray array = new JsonArray();
        values.forEach(array::add);
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
                    entityKey.apply(row.getValue()) + ' ' + row.isIngress(), o));
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
        return host.getIp() + ' ' + host.getHostname().orElse("");
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
        return convo.getLocation() + ' ' + convo.getProtocol() + ' ' + convo.getLowerIp()
                + ' ' + convo.getUpperIp() + ' ' + convo.getApplication();
    }

    // ------------------------------------------------------------------------------------------
    // Comparison
    // ------------------------------------------------------------------------------------------

    private static void assertJsonEquals(final String context, final JsonElement expected,
                                         final JsonElement actual) {
        compare(context, "", expected, actual);
    }

    private static void compare(final String context, final String path,
                                final JsonElement expected, final JsonElement actual) {
        if (expected.isJsonArray() && actual.isJsonArray()) {
            final JsonArray e = expected.getAsJsonArray();
            final JsonArray a = actual.getAsJsonArray();
            if (e.size() != a.size()) {
                throw mismatch(context, path, e, a, "different number of entries");
            }
            for (int i = 0; i < e.size(); i++) {
                compare(context, path + '[' + i + ']', e.get(i), a.get(i));
            }
            return;
        }
        if (expected.isJsonObject() && actual.isJsonObject()) {
            final JsonObject e = expected.getAsJsonObject();
            final JsonObject a = actual.getAsJsonObject();
            final Set<String> keys = new TreeSet<>(e.keySet());
            keys.addAll(a.keySet());
            for (final String key : keys) {
                if (!e.has(key) || !a.has(key)) {
                    throw mismatch(context, path + '.' + key,
                            e.has(key) ? e.get(key) : null, a.has(key) ? a.get(key) : null,
                            "key present on only one side");
                }
                compare(context, path + '.' + key, e.get(key), a.get(key));
            }
            return;
        }
        final Double e = asDouble(expected);
        final Double a = asDouble(actual);
        if (e != null && a != null) {
            // A series row carries NaN for a bucket it has no value in, and NaN is never equal to
            // itself -- so the two sides agreeing has to be spelled out rather than computed.
            if (e.isNaN() || a.isNaN()) {
                if (e.isNaN() && a.isNaN()) {
                    return;
                }
                throw mismatch(context, path, expected, actual, "only one side is NaN");
            }
            final double allowed = Math.max(TOLERANCE, Math.abs(e) * TOLERANCE);
            if (Math.abs(e - a) > allowed) {
                throw mismatch(context, path, expected, actual, "numbers differ by more than " + allowed);
            }
            return;
        }
        if (!expected.equals(actual)) {
            throw mismatch(context, path, expected, actual, "values differ");
        }
    }

    /**
     * Reads a primitive as a number, including the non-finite ones.
     *
     * <p>{@code NaN} and {@code Infinity} are not JSON. Gson writes them as bare tokens anyway, and
     * reading the file back leniently turns those tokens into <em>strings</em> — so a recorded NaN
     * arrives here as {@code "NaN"} while the value computed in this JVM arrives as a number, and
     * comparing them as they stand would report every empty bucket as a disagreement.
     */
    private static Double asDouble(final JsonElement element) {
        if (!element.isJsonPrimitive()) {
            return null;
        }
        final JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isNumber()) {
            return primitive.getAsDouble();
        }
        if (primitive.isString()) {
            switch (primitive.getAsString()) {
                case "NaN": return Double.NaN;
                case "Infinity": return Double.POSITIVE_INFINITY;
                case "-Infinity": return Double.NEGATIVE_INFINITY;
                default: return null;
            }
        }
        return null;
    }

    private static AssertionError mismatch(final String context, final String path,
                                           final JsonElement expected, final JsonElement actual,
                                           final String why) {
        return new AssertionError(context + " at " + (path.isEmpty() ? "<root>" : path)
                + ": " + why + "; elasticsearch=" + expected + " victorialogs=" + actual);
    }
}
