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
package org.opennms.netmgt.telemetry.protocols.openconfig.adapter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import groovy.lang.GroovyClassLoader;

public class GnmiLeafMatcherTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private GroovyClassLoader gcl;

    @Before
    public void setUp() throws Exception {
        gcl = GnmiMappingScript.load();
    }

    private Object mapping(String... groupLines) throws Exception {
        String[] lines = new String[groupLines.length + 2];
        lines[0] = "version: 1";
        lines[1] = "groups:";
        System.arraycopy(groupLines, 0, lines, 2, groupLines.length);
        File file = GnmiMappingScript.writeFile(tmp.getRoot(), UUID.randomUUID() + ".yaml", lines);
        Object result = GnmiMappingScript.call(gcl, "MappingLoader", "load", file);
        assertThat((List<?>) GnmiMappingScript.prop(result, "errors"), is(empty()));
        return GnmiMappingScript.prop(result, "mapping");
    }

    private Object leaf(String path, Object value) throws Exception {
        Object elems = GnmiMappingScript.call(gcl, "PathParser", "parse", path);
        return GnmiMappingScript.newInstance(gcl, "Leaf", elems, value);
    }

    private List<String> match(Object mapping, String path, Object value) throws Exception {
        List<?> matches = (List<?>) GnmiMappingScript.call(gcl, "LeafMatcher", "match", leaf(path, value), mapping);
        return matches.stream().map(Object::toString).collect(Collectors.toList());
    }

    private static final String[] COUNTERS = {
            "  - name: g",
            "    match: /interfaces/interface/state/counters",
            "    resource-type: interface",
            "    instance: \"{interface.name}\"",
            "    attributes:",
            "      in-octets: { type: counter }",
            "      in-unicast-pkts: { type: counter, name: inUcastPkts }"};

    @Test
    public void matchesListedAttribute() throws Exception {
        assertThat(match(mapping(COUNTERS), "/interfaces/interface[name=E1]/state/counters/in-octets", 5L),
                contains("g|in-octets|counter|E1|5"));
    }

    @Test
    public void usesRename() throws Exception {
        assertThat(match(mapping(COUNTERS), "/interfaces/interface[name=E1]/state/counters/in-unicast-pkts", 5L),
                contains("g|inUcastPkts|counter|E1|5"));
    }

    @Test
    public void dropsUnlistedLeaf() throws Exception {
        assertThat(match(mapping(COUNTERS), "/interfaces/interface[name=E1]/state/counters/in-errors", 5L), is(empty()));
    }

    @Test
    public void ignoresLeavesOutsideTheMatchPath() throws Exception {
        assertThat(match(mapping(COUNTERS), "/system/state/counters/in-octets", 5L), is(empty()));
    }

    @Test
    public void leafEqualToMatchPathDoesNotMatch() throws Exception {
        assertThat(match(mapping(COUNTERS), "/interfaces/interface[name=E1]/state/counters", 5L), is(empty()));
    }

    @Test
    public void defaultNameJoinsNestedKeysWithDashes() throws Exception {
        Object mapping = mapping(
                "  - name: g",
                "    match: /interfaces/interface/state",
                "    resource-type: interface",
                "    instance: \"{interface.name}\"",
                "    attributes:",
                "      counters/in-octets: { type: counter }");
        assertThat(match(mapping, "/interfaces/interface[name=E1]/state/counters/in-octets", "7"),
                contains("g|counters-in-octets|counter|E1|7"));
    }

    @Test
    public void wildcardMatchesOneElement() throws Exception {
        Object mapping = mapping(
                "  - name: g",
                "    match: /interfaces/*/state",
                "    resource-type: node",
                "    attributes: { mtu: { type: gauge } }");
        assertThat(match(mapping, "/interfaces/interface[name=E1]/state/mtu", 1500L), contains("g|mtu|gauge|null|1500"));
        assertThat(match(mapping, "/interfaces/interface/subinterfaces/state/mtu", 1500L), is(empty()));
    }

    @Test
    public void keyPredicateSupportsGlobs() throws Exception {
        Object mapping = mapping(
                "  - name: cpu",
                "    match: /components/component[name=CPU*]/cpu/utilization/state",
                "    resource-type: ocComponent",
                "    instance: \"{component.name}\"",
                "    attributes: { avg: { type: gauge } }");
        assertThat(match(mapping, "/components/component[name=CPU0:CORE0]/cpu/utilization/state/avg", 15),
                contains("cpu|avg|gauge|CPU0_CORE0|15"));
        assertThat(match(mapping, "/components/component[name=FAN1]/cpu/utilization/state/avg", 15), is(empty()));
        assertThat(match(mapping, "/components/component/cpu/utilization/state/avg", 15), is(empty()));
    }

    @Test
    public void sanitizesInstance() throws Exception {
        assertThat(match(mapping(COUNTERS), "/interfaces/interface[name=TenGigE0/0/0/3]/state/counters/in-octets", 1L),
                contains("g|in-octets|counter|TenGigE0_0_0_3|1"));
    }

    @Test
    public void skipsLeafWithoutInstanceKey() throws Exception {
        assertThat(match(mapping(COUNTERS), "/interfaces/interface[id=7]/state/counters/in-octets", 1L), is(empty()));
    }

    @Test
    public void allTakesNumericLeavesOnly() throws Exception {
        Object mapping = mapping(
                "  - name: all",
                "    match: /interfaces/interface/state/counters",
                "    resource-type: interface",
                "    instance: \"{interface.name}\"",
                "    attributes: all",
                "    default-type: counter");
        assertThat(match(mapping, "/interfaces/interface[name=E1]/state/counters/in-octets", "12"),
                contains("all|in-octets|counter|E1|12"));
        assertThat(match(mapping, "/interfaces/interface[name=E1]/state/counters/last-clear", "2026-10-12T00:00:00Z"),
                is(empty()));
    }

    @Test
    public void everyMatchingGroupApplies() throws Exception {
        Object mapping = mapping(
                "  - name: per-if",
                "    match: /interfaces/interface/state/counters",
                "    resource-type: interface",
                "    instance: \"{interface.name}\"",
                "    attributes: { in-octets: { type: counter } }",
                "  - name: per-node",
                "    match: /interfaces/interface/state/counters",
                "    resource-type: node",
                "    attributes: { in-octets: { type: gauge, name: anyInOctets } }");
        assertThat(match(mapping, "/interfaces/interface[name=E1]/state/counters/in-octets", 9L),
                contains("per-if|in-octets|counter|E1|9", "per-node|anyInOctets|gauge|null|9"));
    }

    @Test
    public void toNumberNormalizesTypes() throws Exception {
        assertEquals(1L, GnmiMappingScript.call(gcl, "Values", "toNumber", true));
        assertEquals(0L, GnmiMappingScript.call(gcl, "Values", "toNumber", false));
        assertEquals(15L, GnmiMappingScript.call(gcl, "Values", "toNumber", 15));
        assertEquals(42L, GnmiMappingScript.call(gcl, "Values", "toNumber", " 42 "));
        assertEquals(new BigInteger("18446744073709551615"), GnmiMappingScript.call(gcl, "Values", "toNumber", "18446744073709551615"));
        assertEquals(12.5d, GnmiMappingScript.call(gcl, "Values", "toNumber", "12.5"));
        assertEquals(12.34d, GnmiMappingScript.call(gcl, "Values", "toNumber", new java.math.BigDecimal("12.34")));
        assertEquals(1.5d, GnmiMappingScript.call(gcl, "Values", "toNumber", 1.5d));
        assertNull(GnmiMappingScript.call(gcl, "Values", "toNumber", "UP"));
        assertNull(GnmiMappingScript.call(gcl, "Values", "toNumber", "NaN"));
    }
}
