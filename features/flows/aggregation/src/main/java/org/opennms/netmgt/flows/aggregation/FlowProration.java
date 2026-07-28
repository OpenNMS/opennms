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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Canonical, backend-neutral reference implementation of the byte-proration arithmetic. It is the
 * single source of truth that the write-time sampling scale-up shares and that a backend's read path
 * mirrors (for example rendered as SQL), so the proportional-sum behaviour can be unit-tested (against
 * the OpenNMS Elasticsearch drift-plugin's {@code ProportionalSumAggregator}) without a live database.
 * A backend that renders this arithmetic itself must keep its rendering in lockstep with this class.
 *
 * <p>A flow's bytes (scaled by its sampling interval) are distributed across time buckets in
 * proportion to the overlap of {@code [delta_switched, last_switched]} with each bucket, using the
 * <em>full</em> flow duration as the denominator (a flow straddling the window contributes only its
 * in-window fraction), with a zero-duration flow contributing its whole value to the single bucket
 * containing its instant.
 */
final class FlowProration {

    private FlowProration() {
    }

    /** Sampling scale-up: scale only by a finite, non-zero interval; else 1. */
    static double effectiveSampling(final Double samplingInterval) {
        if (samplingInterval == null || !Double.isFinite(samplingInterval) || samplingInterval == 0.0) {
            return 1.0;
        }
        return samplingInterval;
    }

    /**
     * Prorated value for the single summary bucket {@code [s, e)} (half-open at {@code e}).
     * Mirrors {@code proratedForWindow(s, e)}.
     */
    static double summaryValue(final long bytes, final Double samplingInterval,
                               final long delta, final long last, final long s, final long e) {
        final double value = bytes * effectiveSampling(samplingInterval);
        if (last <= delta) {
            return (delta >= s && delta < e) ? value : 0.0;
        }
        final double overlap = Math.max(0, Math.min(last, e) - Math.max(delta, s));
        return value * overlap / (double) (last - delta);
    }

    /** Bucket starts a series row expands to, epoch-aligned (origin 0 — the only alignment mode used). */
    static long[] seriesBucketStarts(final long delta, final long last, final long s, final long e, final long step) {
        return seriesBucketStarts(delta, last, s, e, step, 0L);
    }

    /**
     * Bucket starts a series row expands to, aligned to {@code origin} + k*{@code step}. Flooring
     * toward -inf ({@code Math.floorDiv}) matches the drift plugin's {@code round(v - offset) + offset}
     * bucketing.
     */
    static long[] seriesBucketStarts(final long delta, final long last, final long s, final long e,
                                     final long step, final long origin) {
        final long lower = Math.floorDiv(Math.max(delta, s) - origin, step) * step + origin;
        final long upper = Math.min(last, e);
        if (upper < lower) {
            return new long[0];
        }
        final int n = (int) ((upper - lower) / step) + 1;
        final long[] out = new long[n];
        long b = lower;
        for (int i = 0; i < n; i++) {
            out[i] = b;
            b += step;
        }
        return out;
    }

    /** Contribution of a row to a single series bucket {@code [bucketStart, bucketStart+step)}. Mirrors {@code proratedForBucket(step)}. */
    static double seriesContribution(final long bytes, final Double samplingInterval,
                                     final long delta, final long last, final long bucketStart, final long step) {
        final double value = bytes * effectiveSampling(samplingInterval);
        if (last <= delta) {
            return value;
        }
        final double overlap = Math.max(0, Math.min(last, bucketStart + step) - Math.max(delta, bucketStart));
        return value * overlap / (double) (last - delta);
    }

    /** Full series (bucketStart -&gt; value) a single row contributes over the window {@code [s, e]} at {@code step}, origin 0. */
    static Map<Long, Double> series(final long bytes, final Double samplingInterval,
                                    final long delta, final long last, final long s, final long e, final long step) {
        return series(bytes, samplingInterval, delta, last, s, e, step, 0L);
    }

    /** Full series (bucketStart -&gt; value) a single row contributes over {@code [s, e]} at {@code step}, buckets aligned to {@code origin}. */
    static Map<Long, Double> series(final long bytes, final Double samplingInterval,
                                    final long delta, final long last, final long s, final long e,
                                    final long step, final long origin) {
        final Map<Long, Double> out = new LinkedHashMap<>();
        for (final long bucketStart : seriesBucketStarts(delta, last, s, e, step, origin)) {
            out.put(bucketStart, seriesContribution(bytes, samplingInterval, delta, last, bucketStart, step));
        }
        return out;
    }
}