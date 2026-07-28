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

/**
 * Write-time flow aggregation math: assigns a flow to fixed time windows and prorates its
 * (sampling-scaled) bytes across the windows it spans. This is the ingest-time counterpart to the
 * read-time {@link FlowProration}, and is a faithful port of the OpenNMS Nephron streaming
 * aggregator ({@code UnalignedFixedWindows} plus {@code Pipeline.bytesInWindow}/{@code aggregatize}),
 * reproduced here so an in-process aggregator can run inside Horizon core or Sentinel without the
 * Apache Beam / Flink runtime.
 *
 * <h2>Proration model</h2>
 * A flow is assumed to deliver bytes at a uniform rate over its inclusive interval
 * {@code [deltaSwitched, lastSwitched]} (duration {@code last - delta + 1} ms). A window's share is
 * computed as the difference of a single monotone cumulative-bytes function evaluated at consecutive
 * window boundaries:
 *
 * <pre>{@code   C(t) = floor((t - delta + 1) * V / D)      bytesInWindow = C(overlapEnd) - C(overlapStart - 1)}</pre>
 *
 * Because consecutive windows share boundaries and {@code C} is one monotone function, the integer
 * per-window shares of a flow <em>telescope</em> and sum EXACTLY to {@code floor(V)} (the floored,
 * sampling-scaled total) &mdash; no bytes are lost or double counted across windows. That
 * conservation guarantee is the reason for the cumulative-difference formulation rather than an
 * independent per-window {@code V * overlap / D}.
 *
 * <h2>Window alignment</h2>
 * Windows align to {@code shift + k*windowSize}. Horizon/Sentinel aggregation uses a global grid
 * ({@code shift == 0}) so windows are consistent across exporters and cheap to roll up in SQL;
 * {@link #perNodeShift(int, long)} reproduces Nephron's per-exporter jitter for an optional
 * per-node mode. The window arithmetic assumes {@code timestamp >= shift} (Nephron guards flows with
 * {@code deltaSwitched < shift} upstream); with the default {@code shift == 0} this always holds for
 * epoch-millisecond timestamps.
 */
final class FixedWindowAggregation {

    private FixedWindowAggregation() {
    }

    /**
     * Sampling scale-up applied to a flow's byte count. Mirrors {@link FlowProration#effectiveSampling}
     * (finite and positive scales; {@code null}/{@code 0}/non-finite are treated as 1) so the write-time
     * and read-time paths agree. This is slightly stricter than Nephron, which scales by any
     * {@code samplingInterval > 0}.
     */
    static double samplingMultiplier(final Double samplingInterval) {
        return FlowProration.effectiveSampling(samplingInterval);
    }

    /**
     * Bytes of a flow attributable to a single window. Port of Nephron's {@code Pipeline.bytesInWindow}.
     *
     * @param deltaSwitched         flow start (inclusive), ms
     * @param lastSwitchedInclusive flow end (inclusive), ms
     * @param multipliedNumBytes    the flow's bytes already scaled by {@link #samplingMultiplier}
     * @param windowStart           window start (inclusive), ms
     * @param windowEndInclusive    window end (inclusive), ms (i.e. windowStart + windowSize - 1)
     */
    static long bytesInWindow(final long deltaSwitched,
                              final long lastSwitchedInclusive,
                              final double multipliedNumBytes,
                              final long windowStart,
                              final long windowEndInclusive) {
        // The flow duration ranges [delta_switched, last_switched] (both bounds inclusive).
        final long flowDurationMs = lastSwitchedInclusive - deltaSwitched + 1;

        // The portion of the flow that falls inside this window (both bounds inclusive).
        final long overlapStart = Math.max(deltaSwitched, windowStart);
        final long overlapEnd = Math.min(lastSwitchedInclusive, windowEndInclusive);

        // Cumulative bytes delivered through the end of the previous window and through this window's
        // end; their difference telescopes so per-window shares sum exactly to floor(multipliedNumBytes).
        final long previousEnd = overlapStart - 1;
        final long bytesAtPreviousEnd = (long) ((previousEnd - deltaSwitched + 1) * multipliedNumBytes / flowDurationMs);
        final long bytesAtEnd = (long) ((overlapEnd - deltaSwitched + 1) * multipliedNumBytes / flowDurationMs);

        return bytesAtEnd - bytesAtPreviousEnd;
    }

    /** The number of the (shifted) fixed window a timestamp falls into. Port of {@code UnalignedFixedWindows.windowNumber}. */
    static long windowNumber(final long shift, final long windowSize, final long timestamp) {
        return (timestamp - shift) / windowSize;
    }

    /** The start (inclusive, ms) of the (shifted) window containing a timestamp. Port of {@code windowStartForTimestamp}. */
    static long windowStartForTimestamp(final long shift, final long windowSize, final long timestamp) {
        return timestamp - (timestamp - shift) % windowSize;
    }

    /** The start (inclusive, ms) of a window given its number. Port of {@code windowStartForWindowNumber}. */
    static long windowStartForWindowNumber(final long shift, final long windowSize, final long windowNumber) {
        return shift + windowNumber * windowSize;
    }

    /**
     * Per-exporter window shift in {@code [0, windowSize)}, decorrelating window boundaries across
     * exporters. Port of {@code UnalignedFixedWindows.perNodeShift}; the {@link #mix(int)} hash is
     * fastutil's {@code HashCommon.mix(int)} inlined so no runtime dependency is required. Only needed
     * for the optional per-node alignment mode; global-grid aggregation uses {@code shift == 0}.
     */
    static long perNodeShift(final int nodeId, final long windowSize) {
        return Math.abs(mix(nodeId)) % windowSize;
    }

    /** fastutil {@code HashCommon.INT_PHI} (2^32 * (sqrt(5) - 1) / 2), i.e. -1640531527. */
    private static final int INT_PHI = 0x9E3779B9;

    /** fastutil {@code HashCommon.mix(int)}: multiply by INT_PHI, then xor with the high half. */
    private static int mix(final int x) {
        final int h = x * INT_PHI;
        return h ^ (h >>> 16);
    }
}
