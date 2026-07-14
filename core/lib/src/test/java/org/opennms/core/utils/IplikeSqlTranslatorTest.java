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
package org.opennms.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class IplikeSqlTranslatorTest {

    /**
     * Every case in the iplike reference corpus (tests.dat from the
     * OpenNMS/iplike repository, also covered DB-side by IPLikeCoverageIT)
     * must either translate with identical match semantics or be declared
     * untranslatable so callers fall back to iplike().
     */
    @Test
    public void testCorpusParity() throws Exception {
        int translated = 0;
        final List<String> untranslatable = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("iplike-tests.dat"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                final String[] cols = line.split("\\s+");
                final String value = cols[0], rule = cols[1];
                final boolean expected = Boolean.parseBoolean(cols[2]);

                final Boolean got = IplikeSqlTranslator.matches(value, rule);
                if (got == null) {
                    assertNull("untranslatable rule must yield a null predicate: " + rule,
                            IplikeSqlTranslator.toSqlPredicate(rule, "ipaddr"));
                    untranslatable.add(rule);
                    continue;
                }
                translated++;
                assertEquals("value=" + value + " rule=" + rule, expected, got);
                assertNotNull("translatable rule must yield a predicate: " + rule,
                        IplikeSqlTranslator.toSqlPredicate(rule, "ipaddr"));
            }
        }
        assertTrue("expected most of the corpus to translate, got " + translated, translated >= 30);
        for (final String rule : untranslatable) {
            // only zone-id rules and wildcard-before-constrained-field IPv6
            // rules (not a bounded union of ranges) may fall back
            assertTrue("unexpected untranslatable rule: " + rule,
                    rule.contains("%") || rule.matches(".*\\*.*:.*[0-9a-f].*"));
        }
    }

    @Test
    public void testMatchAllTranslatesToNotNull() {
        assertEquals("ipaddr IS NOT NULL", IplikeSqlTranslator.toSqlPredicate("*.*.*.*", "ipaddr"));
        assertEquals("{alias}.ipaddr IS NOT NULL",
                IplikeSqlTranslator.toSqlPredicate("*:*:*:*:*:*:*:*", "{alias}.ipaddr"));
    }

    @Test
    public void testSingleRangePredicate() {
        assertEquals("(ipaddr IS NOT NULL AND opennms_safe_inet(ipaddr) IS NOT NULL AND ("
                + "(opennms_safe_inet(ipaddr) >= inet '10.0.1.0'"
                + " AND opennms_safe_inet(ipaddr) <= inet '10.0.3.255')))",
                IplikeSqlTranslator.toSqlPredicate("10.0.1-3.*", "ipaddr"));
    }

    @Test
    public void testExactAddressPredicate() {
        assertEquals("(ipaddr IS NOT NULL AND opennms_safe_inet(ipaddr) IS NOT NULL AND "
                + "(opennms_safe_inet(ipaddr) = inet '192.168.1.1'))",
                IplikeSqlTranslator.toSqlPredicate("192.168.1.1", "ipaddr"));
    }

    @Test
    public void testAdjacentListCoalesces() {
        // 0, 1, 2 in the third octet are adjacent /24s -> one contiguous range
        assertEquals("(ipaddr IS NOT NULL AND opennms_safe_inet(ipaddr) IS NOT NULL AND ("
                + "(opennms_safe_inet(ipaddr) >= inet '192.168.0.0'"
                + " AND opennms_safe_inet(ipaddr) <= inet '192.168.2.255')))",
                IplikeSqlTranslator.toSqlPredicate("192.168.0,1,2.*", "ipaddr"));
    }

    @Test
    public void testCompoundPatternEmitsDisjointRanges() {
        final String sql = IplikeSqlTranslator.toSqlPredicate("192.168.0,1,2-4.0-20", "ipaddr");
        assertNotNull(sql);
        // 5 third-octet values x constrained fourth octet -> 5 disjoint ranges
        assertEquals(5, sql.split(" OR ", -1).length);
    }

    @Test
    public void testIpv6RangePredicate() {
        assertEquals("(ipaddr IS NOT NULL AND opennms_safe_inet(ipaddr) IS NOT NULL AND ("
                + "(opennms_safe_inet(ipaddr) >= inet 'fe80:0:0:0:0:0:0:0'"
                + " AND opennms_safe_inet(ipaddr) <= inet 'fe80:0:0:0:0:0:0:ff')))",
                IplikeSqlTranslator.toSqlPredicate(
                        "fe80:0000:0000:0000:0000:0000:0000:0000-00ff", "ipaddr"));
    }

    @Test
    public void testIpv4MappedIpv6RendersAsFamily6() {
        // InetAddress.getByAddress() would collapse ::ffff:0:0/96 to an
        // Inet4Address; the emitted literal must stay 8-group IPv6 so it
        // compares equal to what opennms_safe_inet() yields for the value
        assertEquals("(ipaddr IS NOT NULL AND opennms_safe_inet(ipaddr) IS NOT NULL AND "
                + "(opennms_safe_inet(ipaddr) = inet '0:0:0:0:0:ffff:102:304'))",
                IplikeSqlTranslator.toSqlPredicate("0:0:0:0:0:ffff:0102:0304", "ipaddr"));
        assertEquals(Boolean.TRUE, IplikeSqlTranslator.matches(
                "0000:0000:0000:0000:0000:ffff:0102:0304", "0:0:0:0:0:ffff:0102:0304"));
    }

    @Test
    public void testZoneIdValueNeverMatchesIpv4Rules() {
        // zone ids are IPv6-only; iplike() returns false for IPv4 values
        // carrying one, and opennms_safe_inet() must not strip it
        assertEquals(Boolean.FALSE, IplikeSqlTranslator.matches("1.2.3.4%eth0", "1.2.3.*"));
    }

    @Test
    public void testExpansionCapFallsBack() {
        // 3 x 254 x 6 x 2 = 9,144 ranges, past MAX_RANGES
        assertNull(IplikeSqlTranslator.toSqlPredicate(
                "10,172,192.1-254.1,3,5,7,9,11.0-100,200-255", "ipaddr"));
    }

    @Test
    public void testZoneIdPatternFallsBack() {
        assertNull(IplikeSqlTranslator.toSqlPredicate("fe80:*:*:*:*:*:*:*%45", "ipaddr"));
    }

    @Test
    public void testMalformedPatternsFallBack() {
        assertNull(IplikeSqlTranslator.toSqlPredicate("1.2.3", "ipaddr"));
        assertNull(IplikeSqlTranslator.toSqlPredicate("1.2.3.4.5", "ipaddr"));
        assertNull(IplikeSqlTranslator.toSqlPredicate("a.b.c.d", "ipaddr"));
        assertNull(IplikeSqlTranslator.toSqlPredicate("", "ipaddr"));
        assertNull(IplikeSqlTranslator.toSqlPredicate(null, "ipaddr"));
    }

    @Test
    public void testZoneIdValueMatchesLikeIplike() {
        // the C implementation ignores the value's zone when the rule has none
        assertEquals(Boolean.TRUE, IplikeSqlTranslator.matches(
                "fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%4", "fe80:*:*:*:*:*:*:*"));
    }

    @Test
    public void testDisabledViaSystemProperty() {
        System.setProperty(IplikeSqlTranslator.ENABLED_PROPERTY, "false");
        try {
            assertNull(IplikeSqlTranslator.toSqlPredicate("192.168.1.1", "ipaddr"));
        } finally {
            System.clearProperty(IplikeSqlTranslator.ENABLED_PROPERTY);
        }
    }
}
