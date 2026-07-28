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
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

/**
 * Verifies the write-time aggregation math ported from the OpenNMS Nephron streaming aggregator into
 * {@link FixedWindowAggregation}.
 *
 * <p>The two properties that actually matter are proven directly rather than by re-deriving the
 * ported formula (which would be tautological):
 * <ul>
 *   <li><b>Conservation</b> &mdash; splitting a flow across the windows it spans yields exactly the
 *       same total as not splitting it (the telescoping guarantee), and that total is the floored,
 *       sampling-scaled byte count.</li>
 *   <li><b>Fidelity</b> &mdash; each window's integer share stays within one byte of the ideal
 *       real-valued proration {@code V * overlap / duration} (an independent formula).</li>
 * </ul>
 * The windowing invariants mirror Nephron's {@code UnalignedFixedWindowsTest}, and
 * {@link #perNodeShiftMatchesFastutilGoldenValues()} locks the inlined hash to values computed from
 * fastutil 8.5.2 (the version Nephron pins).
 */
public class FixedWindowAggregationTest {

    // ---- explicit proration cases -------------------------------------------------------------

    @Test
    public void flowFullyInsideOneWindowGetsAllBytes() {
        // window [1000, 2000), flow [1200, 1700], 1000 bytes, no sampling
        assertEquals(1000L, FixedWindowAggregation.bytesInWindow(1200L, 1700L, 1000.0, 1000L, 1999L));
    }

    @Test
    public void flowEvenlySpanningTwoWindowsSplitsInHalf() {
        // 2000 bytes uniformly over [1000, 2999] (2000 ms) across two 1000 ms windows
        final long a = FixedWindowAggregation.bytesInWindow(1000L, 2999L, 2000.0, 1000L, 1999L);
        final long b = FixedWindowAggregation.bytesInWindow(1000L, 2999L, 2000.0, 2000L, 2999L);
        assertEquals(1000L, a);
        assertEquals(1000L, b);
        assertEquals(2000L, a + b);
    }

    @Test
    public void zeroDurationFlowLandsWhollyInItsWindow() {
        // delta == last: whole value goes to the single window containing the instant
        assertEquals(1000L, FixedWindowAggregation.bytesInWindow(1500L, 1500L, 1000.0, 1000L, 1999L));
    }

    @Test
    public void samplingMultiplierScalesBytesAndGuardsDegenerateValues() {
        assertEquals(10.0, FixedWindowAggregation.samplingMultiplier(10.0), 0.0);
        assertEquals(1.0, FixedWindowAggregation.samplingMultiplier(0.0), 0.0);
        assertEquals(1.0, FixedWindowAggregation.samplingMultiplier(null), 0.0);
        assertEquals(1.0, FixedWindowAggregation.samplingMultiplier(Double.NaN), 0.0);
        assertEquals(1.0, FixedWindowAggregation.samplingMultiplier(Double.POSITIVE_INFINITY), 0.0);
        // 1000 bytes at sampling 10 -> 10000 attributed to the containing window
        final double v = 1000L * FixedWindowAggregation.samplingMultiplier(10.0);
        assertEquals(10000L, FixedWindowAggregation.bytesInWindow(1200L, 1700L, v, 1000L, 1999L));
    }

    // ---- conservation (the telescoping guarantee) ---------------------------------------------

    /**
     * Splitting a flow across the windows it spans must total exactly the same as computing it over a
     * single window covering the whole flow. This exercises the window tiling and the telescoping
     * cumulative-difference arithmetic together, and holds exactly regardless of floating-point
     * rounding (adjacent windows evaluate the cumulative function at an identical boundary).
     */
    @Test
    public void splittingAcrossWindowsConservesBytes() {
        final Random rnd = new Random(20240723L);
        for (int i = 0; i < 200000; i++) {
            final long windowSize = 1 + (long) rnd.nextInt(100000);
            final long shift = rnd.nextBoolean() ? 0L : FixedWindowAggregation.perNodeShift(rnd.nextInt(100000), windowSize);
            final long delta = 1_000_000_000L + (long) rnd.nextInt(1_000_000);
            final long last = delta + (long) rnd.nextInt(1_000_000);
            final double v = (1 + rnd.nextInt(1_000_000)) * randomSampling(rnd);

            final long whole = FixedWindowAggregation.bytesInWindow(delta, last, v, delta, last);

            final long firstWindow = FixedWindowAggregation.windowNumber(shift, windowSize, delta);
            final long lastWindow = FixedWindowAggregation.windowNumber(shift, windowSize, last);
            long sum = 0;
            for (long wn = firstWindow; wn <= lastWindow; wn++) {
                final long ws = FixedWindowAggregation.windowStartForWindowNumber(shift, windowSize, wn);
                sum += FixedWindowAggregation.bytesInWindow(delta, last, v, ws, ws + windowSize - 1);
            }
            assertEquals("windowSize=" + windowSize + " shift=" + shift + " delta=" + delta + " last=" + last + " v=" + v,
                    whole, sum);
        }
    }

    /** With exact integer arithmetic (integer multiplier), the whole-flow total is the floored scaled byte count. */
    @Test
    public void wholeFlowTotalEqualsFlooredScaledBytes() {
        final Random rnd = new Random(99L);
        for (int i = 0; i < 100000; i++) {
            final long delta = 1_000_000_000L + (long) rnd.nextInt(1_000_000);
            final long last = delta + (long) rnd.nextInt(100000);
            final long bytes = 1 + rnd.nextInt(1_000_000);
            final long mult = 1 + rnd.nextInt(1000); // integer multiplier -> V is an exact long
            final double v = (double) bytes * mult;
            assertEquals(bytes * mult, FixedWindowAggregation.bytesInWindow(delta, last, v, delta, last));
        }
    }

    // ---- fidelity to the ideal real-valued proration ------------------------------------------

    /** Each window's integer share is within one byte of {@code V * overlapMs / durationMs}. */
    @Test
    public void perWindowShareTracksIdealProration() {
        final Random rnd = new Random(7L);
        for (int i = 0; i < 200000; i++) {
            final long windowSize = 1 + (long) rnd.nextInt(50000);
            final long delta = 1_000_000_000L + (long) rnd.nextInt(1_000_000);
            final long last = delta + (long) rnd.nextInt(500000);
            final double v = (1 + rnd.nextInt(1_000_000)) * randomSampling(rnd);
            final long durationMs = last - delta + 1;

            final long firstWindow = FixedWindowAggregation.windowNumber(0L, windowSize, delta);
            final long lastWindow = FixedWindowAggregation.windowNumber(0L, windowSize, last);
            for (long wn = firstWindow; wn <= lastWindow; wn++) {
                final long ws = FixedWindowAggregation.windowStartForWindowNumber(0L, windowSize, wn);
                final long weInclusive = ws + windowSize - 1;
                final long actual = FixedWindowAggregation.bytesInWindow(delta, last, v, ws, weInclusive);
                final long overlapMs = Math.min(last, weInclusive) - Math.max(delta, ws) + 1;
                final double ideal = v * overlapMs / durationMs;
                assertTrue("actual=" + actual + " ideal=" + ideal + " windowSize=" + windowSize,
                        Math.abs(actual - ideal) < 1.0 + 1.0E-6);
            }
        }
    }

    // ---- windowing invariants (mirror Nephron's UnalignedFixedWindowsTest) ---------------------

    @Test
    public void shiftedWindowContainsTimestampAndRoundTrips() {
        final Random rnd = new Random(1L);
        for (int i = 0; i < 200000; i++) {
            final int nodeId = rnd.nextInt(100000);
            final long windowSize = 1 + (long) rnd.nextInt(100000);
            final long shift = FixedWindowAggregation.perNodeShift(nodeId, windowSize);
            final long timestamp = shift + (long) rnd.nextInt(10_000_000); // precondition: timestamp >= shift

            final long start = FixedWindowAggregation.windowStartForTimestamp(shift, windowSize, timestamp);
            // unshifted start is a multiple of the window size, and the window contains the timestamp
            assertEquals(0L, (start - shift) % windowSize);
            assertTrue(start <= timestamp && start + windowSize > timestamp);

            // windowStartForTimestamp and windowNumber -> windowStartForWindowNumber are consistent
            final long wn = FixedWindowAggregation.windowNumber(shift, windowSize, timestamp);
            assertEquals(start, FixedWindowAggregation.windowStartForWindowNumber(shift, windowSize, wn));
        }
    }

    @Test
    public void globalGridWindowContainsTimestampAndRoundTrips() {
        final Random rnd = new Random(2L);
        for (int i = 0; i < 200000; i++) {
            final long windowSize = 1 + (long) rnd.nextInt(100000);
            final long timestamp = (long) (rnd.nextDouble() * 1_000_000_000_000L);
            final long start = FixedWindowAggregation.windowStartForTimestamp(0L, windowSize, timestamp);
            assertEquals(0L, start % windowSize);
            assertTrue(start <= timestamp && start + windowSize > timestamp);
            final long wn = FixedWindowAggregation.windowNumber(0L, windowSize, timestamp);
            assertEquals(start, FixedWindowAggregation.windowStartForWindowNumber(0L, windowSize, wn));
        }
    }

    // ---- lock the inlined fastutil hash --------------------------------------------------------

    /** Golden values computed from fastutil 8.5.2 HashCommon.mix (the version Nephron pins), windowSize = 60000. */
    @Test
    public void perNodeShiftMatchesFastutilGoldenValues() {
        final long ws = 60000L;
        final long[] expected = {0L, 43410L, 14940L, 28243L, 29881L, 42824L, 2022L, 41611L, 47533L, 48846L, 25648L};
        for (int nodeId = 0; nodeId <= 10; nodeId++) {
            assertEquals("perNodeShift(" + nodeId + ", " + ws + ")", expected[nodeId],
                    FixedWindowAggregation.perNodeShift(nodeId, ws));
        }
    }

    /**
     * An <em>effective</em> multiplier, i.e. what {@link FixedWindowAggregation#samplingMultiplier} would
     * produce for a variety of raw sampling intervals (degenerate values collapse to 1.0). Always finite
     * and positive, so a flow's scaled byte total is well defined.
     */
    private static double randomSampling(final Random rnd) {
        switch (rnd.nextInt(5)) {
            case 0: return FixedWindowAggregation.samplingMultiplier(1.0);
            case 1: return FixedWindowAggregation.samplingMultiplier(0.0);          // -> 1.0
            case 2: return FixedWindowAggregation.samplingMultiplier(Double.NaN);   // -> 1.0
            case 3: return FixedWindowAggregation.samplingMultiplier(1.0 + rnd.nextInt(1000));
            default: return FixedWindowAggregation.samplingMultiplier((double) (1 + rnd.nextInt(1000)));
        }
    }
}
