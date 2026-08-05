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

import org.opennms.netmgt.flows.filter.api.DscpFilter;
import org.opennms.netmgt.flows.filter.api.ExporterNodeFilter;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.flows.filter.api.FilterVisitor;
import org.opennms.netmgt.flows.filter.api.NodeCriteria;
import org.opennms.netmgt.flows.filter.api.SnmpInterfaceIdFilter;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;

/**
 * Translates the flow query {@link Filter}s into LogsQL.
 *
 * <p>Every {@code FlowQueryService} method takes a {@code List<Filter>}, so this is the gate all of
 * them pass through. The semantics are not invented here: each case mirrors the Freemarker template
 * the Elasticsearch repository renders for the same filter, because agreement with that backend is
 * the acceptance criterion. Where a translation looks odd, it is odd in Elasticsearch too, and the
 * comment says so.
 *
 * <p>Filters combine with AND, which in LogsQL is simple juxtaposition. Each fragment is parenthesised
 * so that an OR inside one cannot bind across into its neighbour.
 */
public class LogsQlFilterVisitor implements FilterVisitor<String> {

    static final String F_DELTA_SWITCHED = "netflow.delta_switched";
    static final String F_LAST_SWITCHED = "netflow.last_switched";
    static final String F_INPUT_SNMP = "netflow.input_snmp";
    static final String F_OUTPUT_SNMP = "netflow.output_snmp";
    static final String F_DIRECTION = "netflow.direction";
    static final String F_DSCP = "netflow.dscp";

    static final String F_EXPORTER_FOREIGN_SOURCE = "node_exporter.foreign_source";
    static final String F_EXPORTER_FOREIGN_ID = "node_exporter.foreign_id";
    static final String F_EXPORTER_NODE_ID = "node_exporter.node_id";

    /** Matches every record; LogsQL requires a filter expression, so an empty list needs a value. */
    public static final String MATCH_ALL = "*";

    /**
     * Renders the conjunction of {@code filters}, or {@link #MATCH_ALL} when there are none.
     */
    public static String toQuery(final List<Filter> filters) {
        if (filters == null || filters.isEmpty()) {
            return MATCH_ALL;
        }
        final LogsQlFilterVisitor visitor = new LogsQlFilterVisitor();
        final String query = filters.stream()
                .map(f -> f.visit(visitor))
                .collect(Collectors.joining(" "));
        return query.isEmpty() ? MATCH_ALL : query;
    }

    /**
     * {@inheritDoc}
     *
     * <p>A flow is in range when it overlaps the window at all, so the test is against the flow's
     * extent rather than its timestamp: it must have started at or before the end and finished at or
     * after the start. Both bounds are inclusive, matching {@code filter_time_range.ftl}.
     *
     * <p>Note this deliberately does not filter on {@code _time}. VictoriaLogs would answer such a
     * query faster, but {@code _time} carries the flow's <em>last switched</em> instant alone, which
     * would drop a long flow that began before the window — precisely the records the proportional
     * attribution exists to divide up.
     */
    @Override
    public String visit(final TimeRangeFilter timeRangeFilter) {
        return '(' + range(F_DELTA_SWITCHED, "<=", timeRangeFilter.getEnd())
                + ' ' + range(F_LAST_SWITCHED, ">=", timeRangeFilter.getStart()) + ')';
    }

    /**
     * {@inheritDoc}
     *
     * <p>An interface id alone is ambiguous, because the same physical interface is the input of a
     * flow travelling one way and the output of one travelling the other. So the match is paired with
     * the direction: ingress records match on {@code input_snmp}, egress on {@code output_snmp}, and
     * records whose direction was never determined match on either.
     */
    @Override
    public String visit(final SnmpInterfaceIdFilter snmpInterfaceIdFilter) {
        final long id = snmpInterfaceIdFilter.getSnmpInterfaceId();
        final String ingress = '(' + eq(F_INPUT_SNMP, id) + ' '
                + '(' + eq(F_DIRECTION, "ingress") + " OR " + eq(F_DIRECTION, "unknown") + ')' + ')';
        final String egress = '(' + eq(F_OUTPUT_SNMP, id) + ' '
                + '(' + eq(F_DIRECTION, "egress") + " OR " + eq(F_DIRECTION, "unknown") + ')' + ')';
        return '(' + ingress + " OR " + egress + ')';
    }

    /**
     * {@inheritDoc}
     *
     * <p>An empty value list matches nothing, which is what Elasticsearch's {@code terms} query does
     * with an empty array. It is very likely not what a caller passing an empty list intends — and
     * {@code FlowQueryIT}'s own in-memory predicate reads it the opposite way, as "no constraint" —
     * but this backend is judged against Elasticsearch, so it reproduces Elasticsearch. DSCP is a
     * six-bit field, so a negative value is the honest way to write a contradiction.
     */
    @Override
    public String visit(final DscpFilter dscpFilter) {
        final List<Integer> dscp = dscpFilter.getDscp();
        if (dscp == null || dscp.isEmpty()) {
            return '(' + range(F_DSCP, "<", 0) + ')';
        }
        return '(' + dscp.stream()
                .map(v -> eq(F_DSCP, v))
                .collect(Collectors.joining(" OR ")) + ')';
    }

    /**
     * {@inheritDoc}
     *
     * <p>A node is identified either by its foreign source and id together, or by its database id.
     * {@link NodeCriteria} cannot hold neither, and that is enforced rather than assumed: its
     * {@code (Integer)} and {@code (String, String)} constructors both {@code requireNonNull}, and
     * the {@code (String)} one either parses a node id or splits into a foreign source and id. So
     * the fall-through below cannot render a null, which would otherwise produce
     * {@code "node_exporter.node_id":=null} — a valid term filter matching the literal text
     * {@code null}, quietly emptying a result instead of narrowing it. Verified 2026-07-28; there is
     * deliberately no guard here for a state that cannot be constructed.
     */
    @Override
    public String visit(final ExporterNodeFilter exporterNodeFilter) {
        final NodeCriteria criteria = exporterNodeFilter.getCriteria();
        if (criteria.getForeignSource() != null && criteria.getForeignId() != null) {
            return '(' + eq(F_EXPORTER_FOREIGN_SOURCE, criteria.getForeignSource())
                    + ' ' + eq(F_EXPORTER_FOREIGN_ID, criteria.getForeignId()) + ')';
        }
        return '(' + eq(F_EXPORTER_NODE_ID, criteria.getNodeId()) + ')';
    }

    private static String eq(final String field, final String value) {
        return ProportionalSumQuery.quote(field) + ":=" + literal(value);
    }

    private static String eq(final String field, final Number value) {
        return ProportionalSumQuery.quote(field) + ":=" + value;
    }

    private static String range(final String field, final String operator, final Number bound) {
        return ProportionalSumQuery.quote(field) + ':' + operator + bound;
    }

    /**
     * Quotes a value so that spaces, quotes and LogsQL operators inside it stay data.
     *
     * <p>Delegates rather than repeating the rule: LogsQL quotes a value exactly as it quotes a
     * field name, and two copies of an escaping rule are two places for it to be fixed in only one.
     */
    static String literal(final String value) {
        return ProportionalSumQuery.quote(value);
    }
}
