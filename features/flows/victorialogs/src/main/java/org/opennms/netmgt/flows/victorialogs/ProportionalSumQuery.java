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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds the LogsQL that reproduces the Elasticsearch {@code proportional_sum} aggregation supplied
 * by the OpenNMS Drift plugin.
 *
 * <h2>What the aggregation does</h2>
 *
 * Flow records carry a byte count and the interval over which those bytes were observed,
 * {@code [delta_switched, last_switched]}. A flow rarely lines up with the buckets a chart wants, so
 * a flow overlapping several buckets must contribute to each of them in proportion to the overlap:
 *
 * <pre>{@code share(bucket) = bytes * overlap(bucket, [delta_switched, last_switched]) / duration}</pre>
 *
 * Elasticsearch cannot express that natively, which is why OpenNMS ships a native plugin that must
 * be installed on every Elasticsearch node and rebuilt for every Elasticsearch version. Removing
 * that coupling is the point of this module, so the aggregation has to be reproduced without any
 * server-side extension.
 *
 * <h2>How it is done in LogsQL</h2>
 *
 * A {@code stats by (_time:step)} pipe assigns each record to exactly one bucket, so the record must
 * first be fanned out into one row per bucket it touches. LogsQL has no sequence generator — there
 * is no {@code range()} — but it does have an {@code unroll} pipe that expands a JSON array into
 * rows, and {@code format} can synthesise a literal array. So the fan-out is achieved by unrolling a
 * fixed-length literal {@code [0,1,...,N-1]} and computing each row's bucket from its index.
 *
 * <p>The subtlety that makes this practical is that the index is <em>relative to the flow</em>, not
 * to the query window. Indexing from the window start would fan every flow into one row per bucket
 * in the whole window — 288 rows per flow for a day at five-minute resolution — and then discard
 * almost all of them. Indexing from the flow's own first bucket means {@code N} depends only on how
 * long a flow may last, so the fan-out stays constant no matter how wide the query is.
 *
 * <p>{@code N} therefore follows from {@code maxFlowDurationMs} and nothing else: a flow of that
 * length spans at most {@code ceil(maxFlowDurationMs / step) + 1} buckets. It is deliberately
 * <em>not</em> clamped to the width of the window — see the comment in {@link #build} for why that
 * apparently free optimisation would drop whole flows on a narrow window.
 *
 * <h2>What {@code maxFlowDurationMs} is, and is not</h2>
 *
 * <strong>It is not an enforced cap.</strong> The Elasticsearch repository passes its own
 * {@code maxFlowDurationMs} to {@code IndexSelector}, where it widens the set of indices a query
 * searches so that a flow starting before the window is still found. Nothing rejects or truncates a
 * flow that runs longer, and NetFlow active timeouts routinely exceed two minutes.
 *
 * <p>Here the setting has teeth it does not have there: a flow longer than
 * {@code maxFlowDurationMs} is attributed only to its first {@code N} buckets, and the remainder of
 * its bytes is simply not counted. That is a silent undercount, so the value must be set to at least
 * the longest flow the exporters actually emit — typically the active timeout — rather than left at
 * whatever the Elasticsearch side happens to use. The cost of raising it is linear: each extra
 * bucket is one more row per flow before filtering.
 *
 * <h2>Zero-duration flows</h2>
 *
 * A flow whose start equals its end has no overlap with anything, and dividing by its duration would
 * divide by zero. Elasticsearch attributes such a flow wholly to the bucket containing its start.
 * LogsQL's {@code math} pipe has no conditional, so the same outcome is obtained by flooring the
 * duration at one millisecond: the flow then overlaps exactly one bucket, by its whole length.
 */
public final class ProportionalSumQuery {

    private static final Logger LOG = LoggerFactory.getLogger(ProportionalSumQuery.class);

    /** Guards against a pathological {@code step}/{@code maxFlowDuration} ratio generating a huge array. */
    static final int MAX_BUCKETS_PER_FLOW = 512;

    static final String F_BYTES = "netflow.bytes";
    static final String F_DELTA_SWITCHED = "netflow.delta_switched";
    static final String F_LAST_SWITCHED = "netflow.last_switched";
    static final String F_SAMPLING_INTERVAL = "netflow.sampling_interval";

    private ProportionalSumQuery() {
    }

    /**
     * @param filter    LogsQL filter selecting the flows of interest; {@code *} for all
     * @param groupBy   additional fields to group by, e.g. application and direction
     * @param start     window start, epoch millis, inclusive
     * @param end       window end, epoch millis, exclusive
     * @param step      bucket width in millis
     * @param maxFlowDurationMs longest a flow may span; bounds the fan-out
     * @return LogsQL producing one row per (bucket, group) with a {@code bytes} column
     */
    public static String build(final String filter,
                               final List<String> groupBy,
                               final long start,
                               final long end,
                               final long step,
                               final long maxFlowDurationMs) {
        if (step <= 0) {
            throw new IllegalArgumentException("step must be positive, got " + step);
        }
        if (end <= start) {
            throw new IllegalArgumentException("end must be after start");
        }
        // Bounded by how many buckets a flow can span, and by nothing else. Clamping this to the
        // width of the window looks like a free optimisation and is not: the unroll index counts from
        // the flow's own first bucket, which for a flow that started before the window is negative,
        // so the rows that reach into the window are the later ones. A window one bucket wide would
        // keep only the row before the window and discard the flow entirely.
        final int buckets = bucketsPerFlow(step, maxFlowDurationMs);

        final String indexArray = IntStream.range(0, buckets)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(","));

        final String groupFields = groupBy.stream()
                .map(ProportionalSumQuery::quote)
                .collect(Collectors.joining(", "));
        final String groupClause = groupFields.isEmpty() ? "bstart" : "bstart, " + groupFields;

        final String delta = quote(F_DELTA_SWITCHED);
        final String last = quote(F_LAST_SWITCHED);
        final String bytes = quote(F_BYTES);
        final String sampling = quote(F_SAMPLING_INTERVAL);

        return new StringBuilder()
                .append(filter == null || filter.isBlank() ? "*" : filter)
                // Restrict to flows that can overlap the window at all. Both bounds are inclusive,
                // matching filter_time_range.ftl on the Elasticsearch side. The upper bound is not
                // interchangeable with an exclusive one: when the window is not a whole number of
                // steps wide, a flow starting exactly at `end` still falls in a bucket that begins
                // before it -- start=0, end=25, step=10 puts it in the bucket at 20 -- so excluding
                // it would drop bytes the reference backend counts.
                .append(" | filter ").append(last).append(":>=").append(start)
                .append(" ").append(delta).append(":<=").append(end)
                // Fan out: one row per bucket this flow may touch.
                .append(" | format \"[").append(indexArray).append("]\" as _k")
                .append(" | unroll (_k)")
                // The flow's own first bucket, then the k-th bucket after it.
                .append(" | math floor((").append(delta).append(" - ").append(start)
                .append(") / ").append(step).append(") as _fb")
                .append(" | math (").append(start).append(" + (_fb + _k) * ").append(step).append(") as bstart")
                .append(" | math (bstart + ").append(step).append(") as _bend,")
                .append(" (").append(last).append(" - ").append(delta).append(") as _rawdur")
                // Floor at 1ms so a zero-duration flow lands wholly in its own bucket.
                .append(" | math max(_rawdur, 1) as _dur")
                .append(" | math (min((").append(delta).append(" + _dur), _bend)")
                .append(" - max(").append(delta).append(", bstart)) as _ov,")
                .append(" (").append(last).append(" - bstart) as _tail")
                // Keep every bucket from the one holding delta_switched to the one holding
                // last_switched, and nothing outside the window.
                //
                // The test is on the bucket rather than on the overlap because the last of those
                // buckets can be reached without any of it being occupied: a flow ending exactly on
                // a boundary has zero overlap with the bucket that begins there. Elasticsearch
                // reports that bucket as zero rather than leaving it out, and the difference is
                // visible -- an absent cell becomes NaN once the rows are aligned, so a chart draws
                // a gap where the reference draws a zero.
                .append(" | filter _tail:>=0 bstart:>=").append(start).append(" bstart:<").append(end)
                // Scaled by the sampling interval, which is the fourth field the Drift plugin's
                // proportional_sum reads. An exporter sampling 1-in-N reports the bytes it actually
                // saw, so the stored figure has to be multiplied back up to represent the traffic on
                // the wire -- a 500-byte record at an interval of 10 is 5000 bytes of traffic.
                // Floored at 1 so a record without the field, or with a nonsensical zero, is left
                // alone; the field is absent from unsampled exporters entirely.
                //
                // Only here. top_n_terms.ftl ranks on a plain unscaled sum of netflow.bytes, so the
                // Top-N ranking deliberately does not scale even though the totals it feeds do.
                .append(" | math max(").append(sampling).append(", 1) as _rate")
                .append(" | math (").append(bytes).append(" * _rate * _ov / _dur) as _share")
                .append(" | stats by (").append(groupClause).append(") sum(_share) as bytes")
                .append(" | sort by (bstart)")
                .toString();
    }

    /**
     * How many buckets a single flow can touch.
     *
     * <p>A flow of length {@code d} starting anywhere within a bucket spans at most
     * {@code ceil(d / step) + 1} buckets — the {@code +1} covers the partial bucket at each end.
     */
    static int bucketsPerFlow(final long step, final long maxFlowDurationMs) {
        final long spanned = Math.max(0, maxFlowDurationMs) / step
                + (Math.max(0, maxFlowDurationMs) % step == 0 ? 0 : 1);
        final long buckets = spanned + 1;
        if (buckets > MAX_BUCKETS_PER_FLOW) {
            // Not a partial undercount. The unroll index counts forward from the flow's own first
            // bucket, so a flow starting more than MAX_BUCKETS_PER_FLOW * step before the window has
            // no row that reaches it and contributes nothing whatsoever. Silence here would read as
            // "there was no traffic".
            LOG.warn("A step of {}ms with maxFlowDurationMs={} needs {} buckets per flow, above the "
                    + "limit of {}. Flows starting more than {}ms before the window will be missing "
                    + "from the result entirely. Widen the step or lower maxFlowDurationMs.",
                    step, maxFlowDurationMs, buckets, MAX_BUCKETS_PER_FLOW,
                    MAX_BUCKETS_PER_FLOW * step);
        }
        return (int) Math.min(MAX_BUCKETS_PER_FLOW, Math.max(1, buckets));
    }

    /**
     * Quotes a field name or a value for LogsQL.
     *
     * <p>Flow field names contain dots ({@code netflow.bytes}), which LogsQL would otherwise read as
     * syntax rather than as part of the name. Values need the same treatment for the same reason, so
     * both go through here rather than through two implementations that can drift apart.
     *
     * <p>Control characters are escaped rather than passed through. A quoted LogsQL token does not
     * admit a raw newline or tab, and these strings are not all ours: a conversation key or a
     * hostname arrives from the REST layer or from a PTR record, so one stray control character
     * would otherwise turn into a rejected query rather than a value that simply matches nothing.
     */
    static String quote(final String field) {
        final StringBuilder quoted = new StringBuilder(field.length() + 8).append('"');
        for (int i = 0; i < field.length(); i++) {
            final char c = field.charAt(i);
            switch (c) {
                case '\\': quoted.append("\\\\"); break;
                case '"': quoted.append("\\\""); break;
                case '\n': quoted.append("\\n"); break;
                case '\r': quoted.append("\\r"); break;
                case '\t': quoted.append("\\t"); break;
                default:
                    if (c < 0x20 || c == 0x7f) {
                        quoted.append(String.format("\\u%04x", (int) c));
                    } else {
                        quoted.append(c);
                    }
            }
        }
        return quoted.append('"').toString();
    }
}
