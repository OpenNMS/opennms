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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.flows.api.Directional;
import org.opennms.netmgt.flows.api.Host;
import org.opennms.netmgt.flows.api.TrafficSummary;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.flows.filter.api.SnmpInterfaceIdFilter;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;

import com.google.common.collect.Table;
import com.google.gson.JsonObject;

/**
 * What the query service does to rows after the server has returned them.
 *
 * <p>Everything here was fixed on this branch and shipped with nothing pinning it. That is not an
 * oversight anyone could have noticed from the suite: {@code ReferenceComparisonIT} reports 77/77
 * and is structurally unable to see any of it. Row order is sorted away before comparison; the
 * corpus yields exactly one result row per group, so per-row truncation matches by accident; no
 * corpus flow has the same host at both ends; and no corpus value is ever literally "Other" or
 * "Unknown". A reviewer worked the arithmetic through every filter set to establish that last point,
 * which is a good deal more effort than a test that simply fails.
 *
 * <p>Driven through a stubbed client rather than a container, because none of this is about what
 * VictoriaLogs computes — it is about what this class does with the answer. Stubbing the rows makes
 * the input exact, including inputs the real corpus cannot produce.
 */
public class QueryPostProcessingTest {

    private VictoriaLogsClient client;
    private VictoriaLogsFlowQueryService service;

    /** The queries the service issued, in order, so the generated LogsQL can be asserted on. */
    private List<String> issued;

    @Before
    public void setUp() {
        client = mock(VictoriaLogsClient.class);
        issued = Collections.synchronizedList(new ArrayList<>());
        service = new VictoriaLogsFlowQueryService(client);
    }

    @org.junit.After
    public void tearDown() {
        service.stop();
    }

    private static List<Filter> filters() {
        return Arrays.asList(new TimeRangeFilter(0, 100), new SnmpInterfaceIdFilter(98));
    }

    // ------------------------------------------------------------------------------------------
    // Stubbing
    // ------------------------------------------------------------------------------------------

    /** Recognises which of the service's queries is being asked, by the aggregate it selects. */
    private enum QueryKind { RANKING, PROPORTIONAL, ECN, HOSTNAMES, OTHER }

    private static QueryKind kindOf(final String logsQl) {
        if (logsQl.contains("sum(_share) as bytes")) {
            return QueryKind.PROPORTIONAL;
        }
        if (logsQl.contains("as congestion")) {
            return QueryKind.ECN;
        }
        if (logsQl.contains("_hostname")) {
            return QueryKind.HOSTNAMES;
        }
        if (logsQl.contains("as total")) {
            return QueryKind.RANKING;
        }
        return QueryKind.OTHER;
    }

    private static JsonObject row(final String... keyValues) {
        final JsonObject row = new JsonObject();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            row.addProperty(keyValues[i], keyValues[i + 1]);
        }
        return row;
    }

    /** Answers every query with {@code rows}, recording the LogsQL that was asked. */
    private void stub(final java.util.function.Function<String, List<JsonObject>> answer)
            throws Exception {
        when(client.query(anyString())).thenAnswer(invocation -> {
            final String logsQl = invocation.getArgument(0);
            issued.add(logsQl);
            final List<JsonObject> rows = answer.apply(logsQl);
            return rows == null ? Collections.emptyList() : rows;
        });
    }

    // ------------------------------------------------------------------------------------------
    // Top-N series row order
    // ------------------------------------------------------------------------------------------

    /**
     * A Top-N series must come back in ranked order, busiest first.
     *
     * <p>{@code FlowRestServiceImpl} builds a chart's columns straight from {@code rowKeySet()}, so
     * iteration order is series order on screen. Returning the underlying {@link
     * com.google.common.collect.HashBasedTable} directly put the entities in hash order, which both
     * loses the ranking and differs between two identical calls. {@code RawFlowQueryService} ends
     * its Top-N series with {@code TableUtils.sortTableByRowKeys} for the same reason.
     */
    @Test
    public void topNSeriesRowsComeBackInRankedOrder() throws Exception {
        stub(logsQl -> {
            switch (kindOf(logsQl)) {
                case RANKING:
                    // https outranks http; the reverse of the alphabetical order, so a table that
                    // lost the ranking and fell back to sorting cannot accidentally agree.
                    return Arrays.asList(
                            row("netflow.application", "https", "total", "2000"),
                            row("netflow.application", "http", "total", "1000"));
                case PROPORTIONAL:
                    return Arrays.asList(
                            row("netflow.application", "http", "netflow.direction", "ingress",
                                    "bstart", "0", "bytes", "1000"),
                            row("netflow.application", "https", "netflow.direction", "ingress",
                                    "bstart", "0", "bytes", "2000"));
                default:
                    return Collections.emptyList();
            }
        });

        final Table<Directional<String>, Long, Double> series =
                service.getTopNApplicationSeries(2, 10, false, filters()).get();

        final List<String> order = series.rowKeySet().stream()
                .map(Directional::getValue)
                .collect(Collectors.toList());
        assertEquals("the busiest entity must come first, not wherever hashing puts it",
                Arrays.asList("https", "http"), order);
    }

    // ------------------------------------------------------------------------------------------
    // Byte accumulation
    // ------------------------------------------------------------------------------------------

    /**
     * Several result rows for one entity and direction must be summed before being rounded.
     *
     * <p>Filtering on an SNMP interface puts {@code input_snmp} and {@code output_snmp} into the
     * grouping, so a single entity routinely comes back as many rows — egress traffic leaving one
     * interface arrives on many. Truncating each row first loses up to a byte per row and makes a
     * summary disagree with the sum of its own series, which keeps the double.
     */
    @Test
    public void fractionalRowsAreSummedBeforeBeingRounded() throws Exception {
        stub(logsQl -> {
            if (kindOf(logsQl) == QueryKind.PROPORTIONAL) {
                // Three rows into one slot; each individually truncates to 100, together to 301.
                return Arrays.asList(
                        row("netflow.application", "https", "netflow.direction", "ingress",
                                "netflow.input_snmp", "98", "bstart", "0", "bytes", "100.6"),
                        row("netflow.application", "https", "netflow.direction", "ingress",
                                "netflow.input_snmp", "98", "bstart", "0", "bytes", "100.6"),
                        row("netflow.application", "https", "netflow.direction", "ingress",
                                "netflow.input_snmp", "98", "bstart", "0", "bytes", "100.6"));
            }
            return Collections.emptyList();
        });

        final List<TrafficSummary<String>> summaries = service
                .getApplicationSummaries(new LinkedHashSet<>(Collections.singletonList("https")),
                        false, filters()).get();

        assertEquals(1, summaries.size());
        assertEquals("301.8 rounds once to 301; truncating each row first would give 300",
                301L, summaries.get(0).getBytesIn());
    }

    // ------------------------------------------------------------------------------------------
    // Self-flows
    // ------------------------------------------------------------------------------------------

    /**
     * A flow whose two endpoints are the same host must be counted once, not twice.
     *
     * <p>Per-host figures come from unioning a source grouping with a destination grouping, which
     * double-counts a flow that is its own peer. Elasticsearch cannot make that mistake because
     * {@code FlowDocument.hosts} is a {@code Set}. LogsQL cannot compare two fields, so the
     * destination pass excludes same-host-both-ends explicitly — and must not, in doing so, exclude
     * a flow merely running between two hosts that were both asked about.
     */
    @Test
    public void theDestinationPassExcludesSelfFlowsButNotFlowsBetweenTwoRequestedHosts()
            throws Exception {
        stub(logsQl -> Collections.emptyList());

        service.getHostSummaries(new LinkedHashSet<>(Arrays.asList("10.0.0.1", "10.0.0.2")),
                false, filters()).get();

        final List<String> destinationPasses = issued.stream()
                .filter(q -> kindOf(q) == QueryKind.PROPORTIONAL)
                .filter(q -> q.contains("stats by (bstart, \"netflow.dst_addr\""))
                .collect(Collectors.toList());
        assertTrue("expected a destination grouping; got " + issued, !destinationPasses.isEmpty());

        final String pass = destinationPasses.get(0);
        for (final String host : Arrays.asList("10.0.0.1", "10.0.0.2")) {
            assertTrue("the destination pass must exclude " + host + " being its own peer: " + pass,
                    pass.contains("(\"netflow.src_addr\":=\"" + host + "\" "
                            + "\"netflow.dst_addr\":=\"" + host + "\")"));
        }
        // The exclusion has to be negated, or the pass returns only self-flows.
        assertTrue("the self-flow clause must be negated: " + pass,
                pass.contains("-((\"netflow.src_addr\""));
    }

    // ------------------------------------------------------------------------------------------
    // Entities whose names collide with the labels
    // ------------------------------------------------------------------------------------------

    /**
     * The missing-value bucket and an entity literally named "Unknown" are one entity, not two.
     *
     * <p>Elasticsearch merges them before ranking, via the {@code missing} parameter on its terms
     * aggregation. Grouping alone ranks them separately, hands the label back twice, consumes two of
     * the N places, and then reports the traffic of only one of them.
     */
    @Test
    public void aLiteralUnknownMergesWithTheMissingValueBucket() throws Exception {
        stub(logsQl -> {
            if (kindOf(logsQl) == QueryKind.RANKING) {
                // Merged, "Unknown" totals 700 and takes second place. Unmerged, its two buckets
                // rank separately at 400 and 300, and "dns" takes second place instead -- which is
                // what makes this corpus discriminating where a simpler one is not.
                return Arrays.asList(
                        row("netflow.application", "https", "total", "5000"),
                        row("netflow.application", "dns", "total", "500"),
                        row("netflow.application", "", "total", "400"),
                        row("netflow.application", "Unknown", "total", "300"));
            }
            return Collections.emptyList();
        });

        service.getTopNApplicationSummaries(2, false, filters()).get();

        // Asserted on the queries rather than the answer: only a ranking that placed "Unknown"
        // second goes on to ask for the records that have no application at all.
        final boolean askedForTheMissingBucket = issued.stream()
                .anyMatch(q -> q.contains("\"netflow.application\":=\"\""));
        assertTrue("Unknown must merge its two buckets and outrank dns; queries were " + issued,
                askedForTheMissingBucket);
    }

    /**
     * An entity whose name is "Other" must not crash the query.
     *
     * <p>It resolves to the same {@link Host} as the "Other" bucket itself, so building the result
     * table directly rejected the second cell and failed the whole call with an
     * {@link IllegalArgumentException} — a broken chart rather than a wrong number.
     */
    @Test
    public void anEntityNamedOtherDoesNotCollideFatallyWithTheOtherBucket() throws Exception {
        stub(logsQl -> {
            if (kindOf(logsQl) == QueryKind.PROPORTIONAL) {
                return Collections.singletonList(
                        row("netflow.src_addr", "Other", "netflow.direction", "ingress",
                                "bstart", "0", "bytes", "500"));
            }
            return Collections.emptyList();
        });

        final Set<String> hosts = new LinkedHashSet<>(Collections.singletonList("Other"));
        final Table<Directional<Host>, Long, Double> series =
                service.getHostSeries(hosts, 10, true, filters()).get();

        // The assertion is that we got here at all; the value is whichever of the two won.
        assertTrue("a host named Other must not fail the query", series.rowKeySet().size() >= 1);
    }
}
