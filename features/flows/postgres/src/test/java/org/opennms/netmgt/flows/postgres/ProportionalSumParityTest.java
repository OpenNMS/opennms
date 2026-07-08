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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

/**
 * Verifies that {@link FlowProration} (the pure-Java mirror of the proration SQL rendered by
 * {@link PostgresFlowQueryService}) produces byte-for-byte the same results as a faithful port of
 * the Elasticsearch drift-plugin's {@code ProportionalSumAggregator}. The {@code drift*} helpers
 * below are that port; the fuzz tests exercise hundreds of thousands of random flow/window/step
 * combinations (including non-zero histogram offsets) to lock the two arithmetic paths together.
 */
public class ProportionalSumParityTest {
    private static double tol(double expected) {
        return Math.max(1.0E-6, Math.abs(expected) * 1.0E-9);
    }

    private static long roundDown(long v, long step) {
        return Math.floorDiv(v, step) * step;
    }

    private static long timeInWindow(long ws, long we, long rs, long re) {
        if (rs > we || re < ws) {
            return 0L;
        }
        return Math.min(we, re) - Math.max(ws, rs);
    }

    private static Map<Long, Double> drift(long bytes, Double sampling, long delta, long last, long queryStart, long queryEnd, long step, long offset) {
        double value = bytes;
        if (sampling != null && Double.isFinite(sampling) && sampling != 0.0) {
            value *= sampling.doubleValue();
        }
        long rangeStart = delta;
        long rangeEnd = last;
        long rangeDuration = rangeEnd - rangeStart;
        long startRounded = ProportionalSumParityTest.roundDown(Math.max(rangeStart, queryStart) - offset, step) + offset;
        long lastRounded = ProportionalSumParityTest.roundDown(Math.min(rangeEnd, queryEnd) - offset, step) + offset;
        LinkedHashMap<Long, Double> buckets = new LinkedHashMap<Long, Double>();
        long bucketStart = startRounded;
        while (bucketStart <= lastRounded) {
            long nextBucketStart = bucketStart + step;
            long timeInBucket = ProportionalSumParityTest.timeInWindow(bucketStart, nextBucketStart, rangeStart, rangeEnd);
            double ratio = rangeDuration != 0L ? (double)timeInBucket / (double)rangeDuration : 1.0;
            buckets.merge(bucketStart, value * ratio, Double::sum);
            bucketStart = nextBucketStart;
        }
        return buckets;
    }

    private static double driftSummary(long bytes, Double sampling, long delta, long last, long start, long end) {
        long step = end - start;
        double sum = 0.0;
        for (double v : ProportionalSumParityTest.drift(bytes, sampling, delta, last, start, end - 1L, step, start).values()) {
            sum += v;
        }
        return sum;
    }

    private static Map<Long, Double> driftSeries(long bytes, Double sampling, long delta, long last, long start, long end, long step, long offset) {
        return ProportionalSumParityTest.drift(bytes, sampling, delta, last, start, end, step, offset);
    }

    @Test
    public void summaryFlowFullyInsideWindowContributesWholeValue() {
        ProportionalSumParityTest.assertSummaryParity(1000L, 1.0, 1200L, 1700L, 1000L, 2000L);
        Assert.assertEquals(1000.0, FlowProration.summaryValue(1000L, 1.0, 1200L, 1700L, 1000L, 2000L), ProportionalSumParityTest.tol(1000.0));
    }

    @Test
    public void summaryFlowStraddlingStartContributesInWindowFraction() {
        ProportionalSumParityTest.assertSummaryParity(1000L, 1.0, 800L, 1200L, 1000L, 2000L);
        Assert.assertEquals(500.0, FlowProration.summaryValue(1000L, 1.0, 800L, 1200L, 1000L, 2000L), ProportionalSumParityTest.tol(500.0));
    }

    @Test
    public void summaryFlowStraddlingEndContributesInWindowFraction() {
        ProportionalSumParityTest.assertSummaryParity(1000L, 1.0, 1800L, 2200L, 1000L, 2000L);
        Assert.assertEquals(500.0, FlowProration.summaryValue(1000L, 1.0, 1800L, 2200L, 1000L, 2000L), ProportionalSumParityTest.tol(500.0));
    }

    @Test
    public void summaryZeroDurationInsideWindowContributesWholeValue() {
        ProportionalSumParityTest.assertSummaryParity(1000L, 1.0, 1500L, 1500L, 1000L, 2000L);
        Assert.assertEquals(1000.0, FlowProration.summaryValue(1000L, 1.0, 1500L, 1500L, 1000L, 2000L), ProportionalSumParityTest.tol(1000.0));
    }

    @Test
    public void summaryZeroDurationAtExactEndIsExcluded() {
        ProportionalSumParityTest.assertSummaryParity(1000L, 1.0, 2000L, 2000L, 1000L, 2000L);
        Assert.assertEquals(0.0, FlowProration.summaryValue(1000L, 1.0, 2000L, 2000L, 1000L, 2000L), ProportionalSumParityTest.tol(0.0));
    }

    @Test
    public void samplingIntervalScalesValueButZeroAndNonFiniteDoNot() {
        Assert.assertEquals(4000.0, FlowProration.summaryValue(1000L, 4.0, 1200L, 1700L, 1000L, 2000L), ProportionalSumParityTest.tol(4000.0));
        Assert.assertEquals(1000.0, FlowProration.summaryValue(1000L, 0.0, 1200L, 1700L, 1000L, 2000L), ProportionalSumParityTest.tol(1000.0));
        Assert.assertEquals(1000.0, FlowProration.summaryValue(1000L, Double.NaN, 1200L, 1700L, 1000L, 2000L), ProportionalSumParityTest.tol(1000.0));
        Assert.assertEquals(1000.0, FlowProration.summaryValue(1000L, Double.POSITIVE_INFINITY, 1200L, 1700L, 1000L, 2000L), ProportionalSumParityTest.tol(1000.0));
        Assert.assertEquals(1000.0, FlowProration.summaryValue(1000L, null, 1200L, 1700L, 1000L, 2000L), ProportionalSumParityTest.tol(1000.0));
        ProportionalSumParityTest.assertSummaryParity(1000L, 4.0, 1200L, 1700L, 1000L, 2000L);
        ProportionalSumParityTest.assertSummaryParity(1000L, 0.0, 1200L, 1700L, 1000L, 2000L);
    }

    @Test
    public void seriesDistributesAcrossBucketsProportionally() {
        ProportionalSumParityTest.assertSeriesParity(1000L, 1.0, 1000L, 2000L, 1000L, 2000L, 250L, 0L);
        Map<Long, Double> s = FlowProration.series(1000L, 1.0, 1000L, 2000L, 1000L, 2000L, 250L);
        Assert.assertEquals(250.0, s.get(1000L), ProportionalSumParityTest.tol(250.0));
        Assert.assertEquals(250.0, s.get(1250L), ProportionalSumParityTest.tol(250.0));
        Assert.assertEquals(250.0, s.get(1500L), ProportionalSumParityTest.tol(250.0));
        Assert.assertEquals(250.0, s.get(1750L), ProportionalSumParityTest.tol(250.0));
    }

    @Test
    public void seriesZeroDurationLandsWholeValueInOneBucket() {
        ProportionalSumParityTest.assertSeriesParity(1000L, 1.0, 1500L, 1500L, 1000L, 2000L, 250L, 0L);
        Map<Long, Double> s = FlowProration.series(1000L, 1.0, 1500L, 1500L, 1000L, 2000L, 250L);
        Assert.assertEquals(1000.0, s.get(1500L), ProportionalSumParityTest.tol(1000.0));
    }

    @Test
    public void fuzzSummaryParity() {
        Random rnd = new Random(4044185325L);
        for (int i = 0; i < 200000; ++i) {
            long start = 1000000L + (long)rnd.nextInt(1000000);
            long end = start + 1L + (long)rnd.nextInt(2000000);
            long[] range = ProportionalSumParityTest.randomRange(rnd, start, end);
            long bytes = 1 + rnd.nextInt(1000000);
            Double sampling = ProportionalSumParityTest.randomSampling(rnd);
            ProportionalSumParityTest.assertSummaryParity(bytes, sampling, range[0], range[1], start, end);
        }
    }

    @Test
    public void fuzzSeriesParity() {
        Random rnd = new Random(6169061L);
        for (int i = 0; i < 100000; ++i) {
            long start = 1000000L + (long)rnd.nextInt(1000000);
            long span = 1 + rnd.nextInt(2000000);
            long end = start + span;
            long step = 1 + rnd.nextInt((int)Math.min(span, 500000L));
            long[] range = ProportionalSumParityTest.randomRange(rnd, start, end);
            long bytes = 1 + rnd.nextInt(1000000);
            Double sampling = ProportionalSumParityTest.randomSampling(rnd);
            ProportionalSumParityTest.assertSeriesParity(bytes, sampling, range[0], range[1], start, end, step, 0L);
        }
    }

    @Test
    public void fuzzSeriesParityWithNonZeroOffset() {
        Random rnd = new Random(1045991L);
        for (int i = 0; i < 100000; ++i) {
            long start = 1000000L + (long)rnd.nextInt(1000000);
            long span = 1 + rnd.nextInt(2000000);
            long end = start + span;
            long step = 1 + rnd.nextInt((int)Math.min(span, 500000L));
            long origin = rnd.nextInt((int)step);
            long[] range = ProportionalSumParityTest.randomRange(rnd, start, end);
            long bytes = 1 + rnd.nextInt(1000000);
            Double sampling = ProportionalSumParityTest.randomSampling(rnd);
            ProportionalSumParityTest.assertSeriesParity(bytes, sampling, range[0], range[1], start, end, step, origin);
        }
    }

    private static long[] randomRange(Random rnd, long start, long end) {
        long last;
        long lo = start - 500000L;
        long span = end - start + 1000000L;
        long delta = lo + (long)(rnd.nextDouble() * (double)span);
        long l = last = rnd.nextInt(5) == 0 ? delta : lo + (long)(rnd.nextDouble() * (double)span);
        if (last < delta) {
            long t = delta;
            delta = last;
            last = t;
        }
        return new long[]{delta, last};
    }

    private static Double randomSampling(Random rnd) {
        switch (rnd.nextInt(6)) {
            case 0: {
                return null;
            }
            case 1: {
                return 0.0;
            }
            case 2: {
                return Double.NaN;
            }
            case 3: {
                return Double.POSITIVE_INFINITY;
            }
            case 4: {
                return 1.0;
            }
        }
        return 1.0 + (double)rnd.nextInt(1000);
    }

    private static void assertSummaryParity(long bytes, Double sampling, long delta, long last, long start, long end) {
        double expected = ProportionalSumParityTest.driftSummary(bytes, sampling, delta, last, start, end);
        double actual = FlowProration.summaryValue(bytes, sampling, delta, last, start, end);
        Assert.assertEquals("summary bytes=" + bytes + " sampling=" + sampling + " delta=" + delta + " last=" + last + " window=[" + start + "," + end + ")", expected, actual, ProportionalSumParityTest.tol(expected));
    }

    private static void assertSeriesParity(long bytes, Double sampling, long delta, long last, long start, long end, long step, long origin) {
        Map<Long, Double> expected = ProportionalSumParityTest.driftSeries(bytes, sampling, delta, last, start, end, step, origin);
        Map<Long, Double> actual = FlowProration.series(bytes, sampling, delta, last, start, end, step, origin);
        TreeSet<Long> keys = new TreeSet<Long>();
        keys.addAll(expected.keySet());
        keys.addAll(actual.keySet());
        for (Long k : keys) {
            double ev = expected.getOrDefault(k, 0.0);
            double av = actual.getOrDefault(k, 0.0);
            Assert.assertEquals("series bucket=" + k + " bytes=" + bytes + " sampling=" + sampling + " delta=" + delta + " last=" + last + " window=[" + start + "," + end + "] step=" + step, ev, av, ProportionalSumParityTest.tol(ev));
        }
    }
}