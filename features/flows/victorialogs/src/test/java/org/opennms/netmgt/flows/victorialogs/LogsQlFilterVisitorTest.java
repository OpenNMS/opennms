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

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.opennms.netmgt.flows.filter.api.DscpFilter;
import org.opennms.netmgt.flows.filter.api.ExporterNodeFilter;
import org.opennms.netmgt.flows.filter.api.NodeCriteria;
import org.opennms.netmgt.flows.filter.api.SnmpInterfaceIdFilter;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;

/**
 * Pins the LogsQL each filter renders to.
 *
 * <p>The strings are asserted literally rather than checked for substrings: this translation is the
 * one thing every query method depends on, and a silent change to it would show up as a wrong number
 * somewhere far away.
 */
public class LogsQlFilterVisitorTest {

    private static String render(final org.opennms.netmgt.flows.filter.api.Filter filter) {
        return LogsQlFilterVisitor.toQuery(Collections.singletonList(filter));
    }

    /** A flow matches when its extent overlaps the window, not when its timestamp falls inside it. */
    @Test
    public void rendersTimeRangeAgainstTheFlowExtent() {
        assertEquals("(\"netflow.delta_switched\":<=2000 \"netflow.last_switched\":>=1000)",
                render(new TimeRangeFilter(1000, 2000)));
    }

    /** Direction decides whether the interface is the input or the output; unknown matches either. */
    @Test
    public void pairsSnmpInterfaceWithDirection() {
        assertEquals("((\"netflow.input_snmp\":=98 "
                        + "(\"netflow.direction\":=\"ingress\" OR \"netflow.direction\":=\"unknown\")) OR "
                        + "(\"netflow.output_snmp\":=98 "
                        + "(\"netflow.direction\":=\"egress\" OR \"netflow.direction\":=\"unknown\")))",
                render(new SnmpInterfaceIdFilter(98)));
    }

    @Test
    public void rendersDscpValuesAsAlternatives() {
        assertEquals("(\"netflow.dscp\":=17 OR \"netflow.dscp\":=34)",
                render(new DscpFilter(Arrays.asList(17, 34))));
    }

    /**
     * An empty DSCP list matches nothing, reproducing Elasticsearch's empty {@code terms} query
     * rather than the opposite reading of "no constraint".
     */
    @Test
    public void emptyDscpListMatchesNothing() {
        assertEquals("(\"netflow.dscp\":<0)", render(new DscpFilter(Collections.emptyList())));
    }

    @Test
    public void rendersExporterNodeByForeignSourceAndId() {
        assertEquals("(\"node_exporter.foreign_source\":=\"fs\" \"node_exporter.foreign_id\":=\"fid\")",
                render(new ExporterNodeFilter(new NodeCriteria("fs", "fid"))));
    }

    @Test
    public void rendersExporterNodeByNodeId() {
        assertEquals("(\"node_exporter.node_id\":=42)",
                render(new ExporterNodeFilter(new NodeCriteria(42))));
    }

    /** Filters combine with AND, which LogsQL spells as juxtaposition. */
    @Test
    public void joinsFiltersWithConjunction() {
        final String query = LogsQlFilterVisitor.toQuery(Arrays.asList(
                new TimeRangeFilter(0, 100000), new SnmpInterfaceIdFilter(98)));
        assertEquals("(\"netflow.delta_switched\":<=100000 \"netflow.last_switched\":>=0) "
                        + "((\"netflow.input_snmp\":=98 "
                        + "(\"netflow.direction\":=\"ingress\" OR \"netflow.direction\":=\"unknown\")) OR "
                        + "(\"netflow.output_snmp\":=98 "
                        + "(\"netflow.direction\":=\"egress\" OR \"netflow.direction\":=\"unknown\")))",
                query);
    }

    @Test
    public void noFiltersMatchesEverything() {
        assertEquals(LogsQlFilterVisitor.MATCH_ALL, LogsQlFilterVisitor.toQuery(Collections.emptyList()));
        assertEquals(LogsQlFilterVisitor.MATCH_ALL, LogsQlFilterVisitor.toQuery(null));
    }

    /** A value carrying a quote must not be able to close the literal and inject syntax. */
    @Test
    public void escapesQuotesInValues() {
        final String query = render(new ExporterNodeFilter(new NodeCriteria("a\"b", "c\\d")));
        assertTrue(query, query.contains("\"a\\\"b\""));
        assertTrue(query, query.contains("\"c\\\\d\""));
    }

    /** A value carrying a control character must be escaped, not passed through to the server. */
    @Test
    public void literalEscapesControlCharacters() {
        assertEquals("\"a\\nb\"", LogsQlFilterVisitor.literal("a\nb"));
    }
}
