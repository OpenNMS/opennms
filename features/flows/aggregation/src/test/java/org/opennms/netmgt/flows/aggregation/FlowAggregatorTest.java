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
package org.opennms.netmgt.flows.aggregation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.opennms.integration.api.v1.flows.Flow;

import org.junit.Test;

import com.codahale.metrics.MetricRegistry;

/**
 * Exercises the {@link FlowAggregator} engine in isolation (no database): windowing, proration fan-out
 * across dimensions, watermark-driven close, conservation, direction/ECN handling, and late-flow drop.
 * The background flush thread is intentionally not started; {@link FlowAggregator#flushClosedWindows()}
 * is driven directly so the tests are deterministic.
 */
public class FlowAggregatorTest {

    private static final long SIZE = 1000L;

    private final List<AggregatedFlow> captured = new ArrayList<>();
    private final Consumer<List<AggregatedFlow>> sink = captured::addAll;

    private FlowAggregator aggregator(final long latenessMs) {
        return aggregator(latenessMs, 0); // uncapped
    }

    private FlowAggregator aggregator(final long latenessMs, final int topK) {
        // huge flush interval: we drive flushes manually, the thread is never started
        return new FlowAggregator(SIZE, latenessMs, 3_600_000L, topK, 0L, sink, new MetricRegistry());
    }

    private static FlowInput flow(final long delta, final long last, final long bytes, final Double sampling,
                                  final boolean ingress, final int exporter, final int ifIndex, final String app,
                                  final String convo, final String src, final String dst, final Integer ecn) {
        return flowWithDscp(delta, last, bytes, sampling, ingress, exporter, ifIndex, app, convo, src, dst, ecn, null);
    }

    private static FlowInput flowWithDscp(final long delta, final long last, final long bytes, final Double sampling,
                                          final boolean ingress, final int exporter, final int ifIndex, final String app,
                                          final String convo, final String src, final String dst, final Integer ecn,
                                          final Integer dscp) {
        return new FlowInput(delta, last, bytes, sampling, ingress, exporter, ifIndex, app, convo, src, dst,
                src == null ? null : src + "-host", dst == null ? null : dst + "-host", ecn, dscp);
    }

    /** Push the watermark to {@code ts} with a throwaway flow on an unrelated exporter (its window stays open). */
    private void advanceWatermark(final FlowAggregator agg, final long ts) {
        agg.add(flow(ts, ts, 1L, null, true, 999_999, 1, null, null, null, null, null));
    }

    /** Find a without-TOS (dscp == null) row. */
    private AggregatedFlow row(final long windowStart, final AggregatedFlow.Dimension dim, final String key, final int exporter) {
        return rowTos(windowStart, dim, key, exporter, null);
    }

    /** Find a row for a specific DSCP scope (dscp == null selects the without-TOS rollup). */
    private AggregatedFlow rowTos(final long windowStart, final AggregatedFlow.Dimension dim, final String key,
                                  final int exporter, final Integer dscp) {
        return captured.stream()
                .filter(r -> r.windowStartMs == windowStart && r.dimension == dim
                        && Objects.equals(r.groupedByKey, key) && r.exporterNodeId == exporter
                        && Objects.equals(r.dscp, dscp))
                .findFirst().orElse(null);
    }

    @Test
    public void singleFlowFansOutToInterfaceAndDimensionRows() {
        final FlowAggregator agg = aggregator(0L);
        // flow fully inside [1000, 2000): 1000 ingress bytes, no sampling
        agg.add(flow(1000L, 1999L, 1000L, null, true, 1, 10, "http", "c1", "10.0.0.1", "10.0.0.2", null));
        advanceWatermark(agg, 10_000L); // closes [1000,2000)
        agg.flushClosedWindows();

        final AggregatedFlow itf = row(1000L, AggregatedFlow.Dimension.INTERFACE, null, 1);
        assertNotNull(itf);
        assertEquals(1000L, itf.bytesIn);
        assertEquals(0L, itf.bytesOut);
        assertEquals(2000L, itf.windowEndMs);
        assertEquals(10, itf.ifIndex);
        assertTrue("ecn absent -> non-ECT", itf.nonEcnCapableTransport);
        assertFalse(itf.congestionEncountered);

        assertEquals(1000L, row(1000L, AggregatedFlow.Dimension.APPLICATION, "http", 1).bytesIn);
        assertEquals(1000L, row(1000L, AggregatedFlow.Dimension.CONVERSATION, "c1", 1).bytesIn);
        final AggregatedFlow srcHost = row(1000L, AggregatedFlow.Dimension.HOST, "10.0.0.1", 1);
        assertEquals(1000L, srcHost.bytesIn);
        assertEquals("10.0.0.1-host", srcHost.hostname);
        assertEquals(1000L, row(1000L, AggregatedFlow.Dimension.HOST, "10.0.0.2", 1).bytesIn);
    }

    @Test
    public void flowSpanningTwoWindowsSplitsAndConserves() {
        final FlowAggregator agg = aggregator(0L);
        // 2000 bytes uniformly over [1000, 2999] -> ~1000 per 1000ms window
        agg.add(flow(1000L, 2999L, 2000L, null, true, 1, 10, "http", null, null, null, null));
        advanceWatermark(agg, 10_000L);
        agg.flushClosedWindows();

        final AggregatedFlow w1 = row(1000L, AggregatedFlow.Dimension.INTERFACE, null, 1);
        final AggregatedFlow w2 = row(2000L, AggregatedFlow.Dimension.INTERFACE, null, 1);
        assertNotNull(w1);
        assertNotNull(w2);
        assertEquals(1000L, w1.bytesIn);
        assertEquals(1000L, w2.bytesIn);
        assertEquals("conservation across windows", 2000L, w1.bytesIn + w2.bytesIn);
    }

    @Test
    public void egressAndEcnFlagsAreAggregated() {
        final FlowAggregator agg = aggregator(0L);
        agg.add(flow(1000L, 1999L, 500L, null, false, 7, 3, "https", null, null, null, 3)); // egress, CE
        advanceWatermark(agg, 10_000L);
        agg.flushClosedWindows();

        final AggregatedFlow itf = row(1000L, AggregatedFlow.Dimension.INTERFACE, null, 7);
        assertNotNull(itf);
        assertEquals(0L, itf.bytesIn);
        assertEquals(500L, itf.bytesOut);
        assertTrue("ecn == 3 -> congestion encountered", itf.congestionEncountered);
        assertFalse("ecn == 3 is not non-ECT", itf.nonEcnCapableTransport);
    }

    @Test
    public void windowsDoNotFlushUntilClosed() {
        final FlowAggregator agg = aggregator(0L);
        agg.add(flow(1000L, 1999L, 1000L, null, true, 1, 10, null, null, null, null, null));
        // watermark == 1999 < 2000, so [1000,2000) is still open
        agg.flushClosedWindows();
        assertTrue("nothing should be flushed yet", captured.isEmpty());

        advanceWatermark(agg, 2000L); // watermark 2000 -> threshold 1000 -> [1000,2000) closes
        agg.flushClosedWindows();
        assertNotNull(row(1000L, AggregatedFlow.Dimension.INTERFACE, null, 1));
    }

    @Test
    public void lateFlowForAlreadyClosedWindowIsDropped() {
        final FlowAggregator agg = aggregator(0L);
        advanceWatermark(agg, 10_000L); // watermark high; [1000,2000) is already closed
        final boolean accepted = agg.add(flow(1000L, 1500L, 1000L, null, true, 1, 10, "http", null, null, null, null));
        assertFalse("a flow for a closed window must be rejected", accepted);
        agg.flushClosedWindows();
        // no rows for the closed window / exporter 1
        assertTrue(captured.stream().noneMatch(r -> r.exporterNodeId == 1));
    }

    @Test
    public void allowedLatenessHoldsWindowOpenForLateData() {
        final FlowAggregator agg = aggregator(5000L); // 5s lateness
        agg.add(flow(1000L, 1999L, 1000L, null, true, 1, 10, "http", null, null, null, null));
        advanceWatermark(agg, 2500L); // watermark 2500: without lateness [1000,2000) would close, but 2500-1000-5000<1000
        agg.flushClosedWindows();
        assertTrue("lateness should keep the window open", captured.isEmpty());

        // a late flow for the still-open window is accepted and merged
        assertTrue(agg.add(flow(1500L, 1600L, 500L, null, true, 1, 10, "http", null, null, null, null)));
        advanceWatermark(agg, 6001L); // now 6001-1000-5000 = 1 >= 1000? no... push further
        advanceWatermark(agg, 7000L); // 7000-1000-5000 = 1000 >= 1000 -> closes [1000,2000)
        agg.flushClosedWindows();
        final AggregatedFlow itf = row(1000L, AggregatedFlow.Dimension.INTERFACE, null, 1);
        assertNotNull(itf);
        assertEquals("both the original and the late flow are included", 1500L, itf.bytesIn);
    }

    @Test
    public void closeFlushesRemainingOpenWindows() {
        final FlowAggregator agg = aggregator(0L);
        agg.add(flow(1000L, 1999L, 1000L, null, true, 1, 10, "http", null, null, null, null));
        // window is still open (never advanced the watermark past it)
        agg.close();
        assertNotNull("close() must flush buffered windows", row(1000L, AggregatedFlow.Dimension.INTERFACE, null, 1));
    }

    @Test
    public void interfaceTotalConservesAcrossManyWindows() {
        final FlowAggregator agg = new FlowAggregator(100L, 0L, 3_600_000L, 0, 0L, sink, new MetricRegistry());
        final long bytes = 123_457L;
        agg.add(flow(100_000L, 137_777L, bytes, null, true, 1, 10, null, null, null, null, null)); // spans many 100ms windows
        agg.close(); // flush everything

        final long sum = captured.stream()
                .filter(r -> r.dimension == AggregatedFlow.Dimension.INTERFACE && r.exporterNodeId == 1)
                .mapToLong(r -> r.bytesIn)
                .sum();
        assertEquals("sum of per-window interface bytes equals the flow's bytes", bytes, sum);
    }

    @Test
    public void topKCapsCappedDimensionsAndRollsRemainderIntoOther() {
        final FlowAggregator agg = aggregator(0L, 2); // keep top 2 conversations per interface
        // five conversations on one exporter/interface, distinct byte counts, all in [1000,2000)
        final long[] bytes = {500, 400, 300, 200, 100};
        for (int i = 0; i < bytes.length; i++) {
            agg.add(flow(1000L, 1999L, bytes[i], null, true, 1, 10, null, "c" + i, null, null, null));
        }
        advanceWatermark(agg, 10_000L);
        agg.flushClosedWindows();

        // top 2 by bytes are c0 (500) and c1 (400), stored individually
        assertEquals(500L, row(1000L, AggregatedFlow.Dimension.CONVERSATION, "c0", 1).bytesIn);
        assertEquals(400L, row(1000L, AggregatedFlow.Dimension.CONVERSATION, "c1", 1).bytesIn);
        // c2..c4 are not stored individually...
        assertNull(row(1000L, AggregatedFlow.Dimension.CONVERSATION, "c2", 1));
        // ...they roll into one null-key "Other" row: 300 + 200 + 100
        assertEquals(600L, row(1000L, AggregatedFlow.Dimension.CONVERSATION, null, 1).bytesIn);
        // exactly three conversation rows: 2 top-K + 1 Other
        assertEquals(3L, captured.stream()
                .filter(r -> r.dimension == AggregatedFlow.Dimension.CONVERSATION && r.exporterNodeId == 1).count());
        // conservation: capped rows + Other still sum to the (uncapped) interface total
        final long convTotal = captured.stream()
                .filter(r -> r.dimension == AggregatedFlow.Dimension.CONVERSATION && r.exporterNodeId == 1)
                .mapToLong(r -> r.bytesIn).sum();
        assertEquals(1500L, convTotal);
        assertEquals(1500L, row(1000L, AggregatedFlow.Dimension.INTERFACE, null, 1).bytesIn);
    }

    @Test
    public void interfaceTotalsAreNeverCappedByTopK() {
        final FlowAggregator agg = aggregator(0L, 1); // aggressive cap
        // three interfaces on one exporter, each with a single conversation
        agg.add(flow(1000L, 1999L, 100L, null, true, 1, 10, null, "a", null, null, null));
        agg.add(flow(1000L, 1999L, 200L, null, true, 1, 11, null, "b", null, null, null));
        agg.add(flow(1000L, 1999L, 300L, null, true, 1, 12, null, "c", null, null, null));
        advanceWatermark(agg, 10_000L);
        agg.flushClosedWindows();

        // all three interface totals are present despite topK=1
        assertEquals(3L, captured.stream()
                .filter(r -> r.dimension == AggregatedFlow.Dimension.INTERFACE && r.exporterNodeId == 1).count());
    }

    @Test
    public void tosProducesWithAndWithoutTosAggregationsThatReconcile() {
        final FlowAggregator agg = aggregator(0L, 10);
        // same exporter/interface/app/conversation, two different DSCP values, in [1000,2000)
        agg.add(flowWithDscp(1000L, 1999L, 300L, null, true, 1, 10, "http", "c1", null, null, null, 0));
        agg.add(flowWithDscp(1000L, 1999L, 700L, null, true, 1, 10, "http", "c1", null, null, null, 46));
        advanceWatermark(agg, 10_000L);
        agg.flushClosedWindows();

        // without-TOS rollup (dscp == null): both DSCP merged
        assertEquals(1000L, row(1000L, AggregatedFlow.Dimension.INTERFACE, null, 1).bytesIn);
        assertEquals(1000L, row(1000L, AggregatedFlow.Dimension.APPLICATION, "http", 1).bytesIn);
        assertEquals(1000L, row(1000L, AggregatedFlow.Dimension.CONVERSATION, "c1", 1).bytesIn);

        // with-TOS: separate per-DSCP aggregations
        assertEquals(300L, rowTos(1000L, AggregatedFlow.Dimension.INTERFACE, null, 1, 0).bytesIn);
        assertEquals(700L, rowTos(1000L, AggregatedFlow.Dimension.INTERFACE, null, 1, 46).bytesIn);
        assertEquals(300L, rowTos(1000L, AggregatedFlow.Dimension.APPLICATION, "http", 1, 0).bytesIn);
        assertEquals(700L, rowTos(1000L, AggregatedFlow.Dimension.APPLICATION, "http", 1, 46).bytesIn);

        // conservation: the per-DSCP interface totals sum to the without-TOS total
        final long withTosSum = rowTos(1000L, AggregatedFlow.Dimension.INTERFACE, null, 1, 0).bytesIn
                + rowTos(1000L, AggregatedFlow.Dimension.INTERFACE, null, 1, 46).bytesIn;
        assertEquals(row(1000L, AggregatedFlow.Dimension.INTERFACE, null, 1).bytesIn, withTosSum);
    }

    @Test
    public void topKIsAppliedPerDscpScope() {
        final FlowAggregator agg = aggregator(0L, 1); // keep top 1 per (interface, dscp)
        // dscp 0: c1 (500) beats c2 (100); dscp 46: c3 (300) only
        agg.add(flowWithDscp(1000L, 1999L, 500L, null, true, 1, 10, null, "c1", null, null, null, 0));
        agg.add(flowWithDscp(1000L, 1999L, 100L, null, true, 1, 10, null, "c2", null, null, null, 0));
        agg.add(flowWithDscp(1000L, 1999L, 300L, null, true, 1, 10, null, "c3", null, null, null, 46));
        advanceWatermark(agg, 10_000L);
        agg.flushClosedWindows();

        // dscp 0: c1 is the single top-K, c2 falls into Other (null key)
        assertEquals(500L, rowTos(1000L, AggregatedFlow.Dimension.CONVERSATION, "c1", 1, 0).bytesIn);
        assertNull(rowTos(1000L, AggregatedFlow.Dimension.CONVERSATION, "c2", 1, 0));
        assertEquals(100L, rowTos(1000L, AggregatedFlow.Dimension.CONVERSATION, null, 1, 0).bytesIn);
        // dscp 46: c3 alone, no Other
        assertEquals(300L, rowTos(1000L, AggregatedFlow.Dimension.CONVERSATION, "c3", 1, 46).bytesIn);
        assertNull(rowTos(1000L, AggregatedFlow.Dimension.CONVERSATION, null, 1, 46));
    }

    @Test
    public void idleFlushReleasesWindowsWhenTheWatermarkStalls() {
        final long[] now = {1_000_000L};
        // idleFlushMs = 5000; drive the wall clock manually
        final FlowAggregator agg = new FlowAggregator(SIZE, 0L, 3_600_000L, 0, 5000L, sink, new MetricRegistry());
        agg.setClock(() -> now[0]);

        // one flow in [1000,2000): watermark = 1999, which does NOT close the window
        agg.add(flow(1000L, 1999L, 1000L, null, true, 1, 10, "http", null, null, null, null));
        agg.flushClosedWindows();
        assertTrue("event-time close should not fire with a stalled watermark", captured.isEmpty());

        // not old enough yet (4s < 5s)
        now[0] += 4000L;
        agg.flushIdleWindows();
        assertTrue(captured.isEmpty());

        // past the idle timeout (6s > 5s): the window is released despite the stalled watermark
        now[0] += 2000L;
        agg.flushIdleWindows();
        assertNotNull(row(1000L, AggregatedFlow.Dimension.INTERFACE, null, 1));
        assertEquals(1000L, row(1000L, AggregatedFlow.Dimension.INTERFACE, null, 1).bytesIn);
    }

    @Test
    public void fromFlowSkipsFlowsMissingRequiredFields() {
        // a Flow with no switched timestamps / exporter yields no FlowInput (Mockito returns null/empty)
        assertNull(FlowInput.from(mock(Flow.class)));
    }
}
