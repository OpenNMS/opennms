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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.flows.api.Directional;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.gson.JsonObject;

/**
 * Produces the proportionally-attributed byte series that back the {@code *Series} methods of
 * {@code FlowQueryService}.
 *
 * <p><strong>Test support, not the production path.</strong> The {@code *Series} methods are served
 * by {@link VictoriaLogsFlowQueryService}; this exists only to exercise
 * {@link ProportionalSumQuery}'s arithmetic against a real container in {@code ProportionalSumIT},
 * where a bare table of numbers is easier to assert on than a full query result. It deliberately
 * lives in test scope: as production code it was a second, divergent implementation of the same
 * aggregation that nothing called, and wiring it up would silently have produced different numbers
 * from the code the reference comparison validates.
 *
 * <p>All the arithmetic happens inside VictoriaLogs; see {@link ProportionalSumQuery}. Pulling the
 * matching flows back and attributing them in Java would also work and was the fallback plan, but it
 * would move a full result set over the wire for every chart. Since the aggregation turned out to be
 * expressible in LogsQL, it stays pushed down.
 *
 * <p><strong>Direction handling here is deliberately naive.</strong> {@code netflow.direction} has
 * three values but {@link Directional} has two, and this folds {@code unknown} in with egress. That
 * is wrong for production and right for a test helper: it receives an already-rendered LogsQL string
 * and so has no access to the interface a real resolution needs.
 * {@link VictoriaLogsFlowQueryService#isIngress} does it properly. Keep this class away from the
 * query path.
 */
public class ProportionalSeriesService {

    static final String FIELD_APPLICATION = "netflow.application";
    static final String FIELD_DIRECTION = "netflow.direction";
    static final String INGRESS = "ingress";

    private final VictoriaLogsClient client;
    private final long maxFlowDurationMs;

    public ProportionalSeriesService(final VictoriaLogsClient client, final long maxFlowDurationMs) {
        this.client = Objects.requireNonNull(client);
        this.maxFlowDurationMs = maxFlowDurationMs;
    }

    /**
     * Byte series per application and direction.
     *
     * @param filter LogsQL filter selecting the flows of interest, or {@code *} for all
     */
    public Table<Directional<String>, Long, Double> applicationSeries(final String filter,
                                                                     final long start,
                                                                     final long end,
                                                                     final long step)
            throws VictoriaLogsException {
        return series(filter, FIELD_APPLICATION, start, end, step);
    }

    /** Byte series grouped by an arbitrary entity field, split by direction. */
    public Table<Directional<String>, Long, Double> series(final String filter,
                                                           final String entityField,
                                                           final long start,
                                                           final long end,
                                                           final long step)
            throws VictoriaLogsException {
        final List<String> groupBy = Arrays.asList(entityField, FIELD_DIRECTION);
        final String logsQl =
                ProportionalSumQuery.build(filter, groupBy, start, end, step, maxFlowDurationMs);

        final Table<Directional<String>, Long, Double> table = HashBasedTable.create();
        for (final JsonObject row : client.query(logsQl)) {
            final String entity = asString(row, entityField);
            final String direction = asString(row, FIELD_DIRECTION);
            if (entity == null || direction == null) {
                continue;
            }
            final Directional<String> key = new Directional<>(entity, INGRESS.equals(direction));
            final long bucket = asLong(row, "bstart");
            final Double existing = table.get(key, bucket);
            // Several groups can land in one cell, so values accumulate rather than replace. This is
            // not a theoretical case: the query groups by netflow.direction, which has three values,
            // while Directional has only two -- so a bucket holding both an egress flow and one whose
            // direction was never determined produces two rows for the same cell. Building an
            // immutable table directly would reject the second one and fail the whole query.
            table.put(key, bucket, (existing == null ? 0d : existing) + asDouble(row, "bytes"));
        }
        return ImmutableTable.copyOf(table);
    }

    /**
     * Reads a numeric column.
     *
     * <p>VictoriaLogs returns every column as a JSON string, including the ones {@code math} and
     * {@code stats} computed, so these cannot be read as numbers directly.
     */
    private static double asDouble(final JsonObject row, final String name) {
        final String raw = asString(row, name);
        return raw == null || raw.isEmpty() ? 0d : Double.parseDouble(raw);
    }

    private static long asLong(final JsonObject row, final String name) {
        final String raw = asString(row, name);
        return raw == null || raw.isEmpty() ? 0L : (long) Double.parseDouble(raw);
    }

    private static String asString(final JsonObject row, final String name) {
        return row.has(name) && !row.get(name).isJsonNull() ? row.get(name).getAsString() : null;
    }
}
