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

/**
 * The subset of VictoriaLogs' Prometheus counters that says whether ingestion actually worked.
 *
 * <p>Three of these matter and are worth naming explicitly:
 * <ul>
 *   <li>{@code vl_rows_ingested_total} — entries accepted and stored. Compare against the number of
 *       flows sent; a shortfall means loss.</li>
 *   <li>{@code vl_rows_dropped_total} — entries discarded at ingestion, most commonly because their
 *       timestamp falls outside the configured retention window or beyond the future cut-off. This
 *       is not theoretical for flow data: exporters have unreliable clocks, which is why OpenNMS
 *       carries a clock-correction field at all.</li>
 *   <li>{@code vl_streams_created_total} — new stream identities. This should plateau. If it climbs
 *       in step with flow volume then a high-cardinality field has been put into
 *       {@code _stream_fields}, which degrades ingestion and querying alike.</li>
 * </ul>
 */
public class IngestStats {

    private static final String ROWS_INGESTED = "vl_rows_ingested_total";
    private static final String ROWS_DROPPED = "vl_rows_dropped_total";
    private static final String STREAMS_CREATED = "vl_streams_created_total";
    private static final String HTTP_ERRORS = "vl_http_errors_total";

    private final long rowsIngested;
    private final long rowsDropped;
    private final long streamsCreated;
    private final long httpErrors;

    IngestStats(final long rowsIngested, final long rowsDropped,
                final long streamsCreated, final long httpErrors) {
        this.rowsIngested = rowsIngested;
        this.rowsDropped = rowsDropped;
        this.streamsCreated = streamsCreated;
        this.httpErrors = httpErrors;
    }

    /**
     * Sums each counter across all its label combinations.
     *
     * <p>The counters are exported per protocol and, for drops, per reason, so a bare name match is
     * not enough — {@code vl_rows_ingested_total{type="jsonline"}} and any sibling series both count
     * towards the total. Unparseable lines are skipped rather than failing the whole scrape; a
     * malformed metrics response should not mask the ingestion result we are trying to check.
     */
    public static IngestStats parse(final String prometheusText) {
        long ingested = 0;
        long dropped = 0;
        long streams = 0;
        long errors = 0;
        for (final String line : prometheusText.split("\n")) {
            final String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.charAt(0) == '#') {
                continue;
            }
            final int split = trimmed.lastIndexOf(' ');
            if (split < 0) {
                continue;
            }
            final String name = trimmed.substring(0, split);
            final double value;
            try {
                value = Double.parseDouble(trimmed.substring(split + 1));
            } catch (final NumberFormatException e) {
                continue;
            }
            if (matches(name, ROWS_INGESTED)) {
                ingested += (long) value;
            } else if (matches(name, ROWS_DROPPED)) {
                dropped += (long) value;
            } else if (matches(name, STREAMS_CREATED)) {
                streams += (long) value;
            } else if (matches(name, HTTP_ERRORS)) {
                errors += (long) value;
            }
        }
        return new IngestStats(ingested, dropped, streams, errors);
    }

    /**
     * Matches a metric name exactly, or the same name carrying labels.
     *
     * <p>A plain prefix test would also match an unrelated sibling such as
     * {@code vl_rows_ingested_total_bytes}, silently inflating the row count. These counters are the
     * only loss-detection mechanism available, so an inflated {@code rowsIngested} would report
     * success for a batch that was partly discarded.
     */
    private static boolean matches(final String name, final String metric) {
        return name.equals(metric) || name.startsWith(metric + "{");
    }

    /**
     * Rows parsed and handed to the processing pipeline.
     *
     * <p>This is <em>not</em> net of drops. A row with an out-of-range timestamp is counted here and
     * again under {@link #getRowsDropped()}, so the two overlap. Use {@link #getRowsStored()} for the
     * number that actually landed.
     */
    public long getRowsIngested() {
        return rowsIngested;
    }

    /** Rows that survived to storage, i.e. ingested minus dropped. */
    public long getRowsStored() {
        return rowsIngested - rowsDropped;
    }

    public long getRowsDropped() {
        return rowsDropped;
    }

    public long getStreamsCreated() {
        return streamsCreated;
    }

    public long getHttpErrors() {
        return httpErrors;
    }

    @Override
    public String toString() {
        return "IngestStats{ingested=" + rowsIngested
                + ", dropped=" + rowsDropped
                + ", streamsCreated=" + streamsCreated
                + ", httpErrors=" + httpErrors
                + '}';
    }
}
