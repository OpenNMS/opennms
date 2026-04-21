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
package org.opennms.netmgt.config;

import org.junit.Test;
import org.opennms.netmgt.config.SnmpConfigUtils.DefinitionContentsValidationStatus;
import org.opennms.netmgt.config.SnmpConfigUtils.DefinitionStatsValidationStatus;
import org.opennms.netmgt.config.SnmpConfigUtils.ValidatedDefinitionContents;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.Range;

import java.net.InetAddress;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SnmpConfigUtilsTest {

    // -------------------------------------------------------------------------
    // validateDefinitionContents
    // -------------------------------------------------------------------------

    @Test
    public void testValidateDefinitionContentsWithRange() {
        Definition definition = new Definition();
        definition.addRange(new Range("10.0.0.1", "10.0.0.10"));
        assertEquals(DefinitionStatsValidationStatus.VALID, SnmpConfigUtils.validateDefinitionContents(definition));
    }

    @Test
    public void testValidateDefinitionContentsWithSpecific() {
        Definition definition = new Definition();
        definition.addSpecific("10.0.0.1");
        assertEquals(DefinitionStatsValidationStatus.VALID, SnmpConfigUtils.validateDefinitionContents(definition));
    }

    @Test
    public void testValidateDefinitionContentsWithIpMatch() {
        Definition definition = new Definition();
        definition.addIpMatch("10.0.0.*");
        assertEquals(DefinitionStatsValidationStatus.VALID, SnmpConfigUtils.validateDefinitionContents(definition));
    }

    @Test
    public void testValidateDefinitionContentsEmpty() {
        Definition definition = new Definition();
        assertEquals(DefinitionStatsValidationStatus.MISSING_CONTENTS, SnmpConfigUtils.validateDefinitionContents(definition));
    }

    @Test
    public void testValidateDefinitionContentsIpMatchWithRange() {
        Definition definition = new Definition();
        definition.addIpMatch("10.0.0.*");
        definition.addRange(new Range("10.0.0.1", "10.0.0.10"));
        assertEquals(DefinitionStatsValidationStatus.CANNOT_MIX_RANGE_AND_IPMATCH, SnmpConfigUtils.validateDefinitionContents(definition));
    }

    @Test
    public void testValidateDefinitionContentsIpMatchWithSpecific() {
        Definition definition = new Definition();
        definition.addIpMatch("10.0.0.*");
        definition.addSpecific("10.0.0.5");
        assertEquals(DefinitionStatsValidationStatus.CANNOT_MIX_RANGE_AND_IPMATCH, SnmpConfigUtils.validateDefinitionContents(definition));
    }

    // -------------------------------------------------------------------------
    // validateDefinitionIpAddresses
    // -------------------------------------------------------------------------

    @Test
    public void testValidateDefinitionIpAddressesValidIPv4Specific() {
        Definition definition = new Definition();
        definition.addSpecific("192.168.1.1");
        assertNull(SnmpConfigUtils.validateDefinitionIpAddresses(definition));
    }

    @Test
    public void testValidateDefinitionIpAddressesValidIPv6Specific() {
        Definition definition = new Definition();
        definition.addSpecific("2001:db8::1");
        assertNull(SnmpConfigUtils.validateDefinitionIpAddresses(definition));
    }

    @Test
    public void testValidateDefinitionIpAddressesValidIPv6FullSpecific() {
        Definition definition = new Definition();
        definition.addSpecific("2001:db8:1:2:3:4:5:1");
        assertNull(SnmpConfigUtils.validateDefinitionIpAddresses(definition));
    }

    @Test
    public void testValidateDefinitionIpAddressesEmptySpecific() {
        Definition definition = new Definition();
        definition.addSpecific("");
        assertEquals("Invalid specific IP address: empty value.", SnmpConfigUtils.validateDefinitionIpAddresses(definition));
    }

    @Test
    public void testValidateDefinitionIpAddressesInvalidSpecific() {
        Definition definition = new Definition();
        definition.addSpecific("10.");
        assertEquals("Invalid specific IP address: 10.", SnmpConfigUtils.validateDefinitionIpAddresses(definition));
    }

    @Test
    public void testValidateDefinitionIpAddressesValidIPv4Range() {
        Definition definition = new Definition();
        definition.addRange(new Range("10.0.0.1", "10.0.0.100"));
        assertNull(SnmpConfigUtils.validateDefinitionIpAddresses(definition));
    }

    @Test
    public void testValidateDefinitionIpAddressesValidIPv6Range() {
        Definition definition = new Definition();
        definition.addRange(new Range("fd00::1", "fd00::ff"));
        assertNull(SnmpConfigUtils.validateDefinitionIpAddresses(definition));
    }

    @Test
    public void testValidateDefinitionIpAddressesValidIPv6FullRange() {
        Definition definition = new Definition();
        definition.addRange(new Range("fd00:0:0:0:0:0:0:1", "fd00:0:0:0:0:0:0:ff"));
        assertNull(SnmpConfigUtils.validateDefinitionIpAddresses(definition));
    }

    @Test
    public void testValidateDefinitionIpAddressesRangeEmptyBegin() {
        Definition definition = new Definition();
        definition.addRange(new Range("", "10.0.0.100"));
        assertNotNull(SnmpConfigUtils.validateDefinitionIpAddresses(definition));
    }

    @Test
    public void testValidateDefinitionIpAddressesRangeInvalidBegin() {
        Definition definition = new Definition();
        definition.addRange(new Range("10.", "10.0.0.100"));
        assertEquals("Invalid range begin IP address: 10.", SnmpConfigUtils.validateDefinitionIpAddresses(definition));
    }

    @Test
    public void testValidateDefinitionIpAddressesRangeInvalidEnd() {
        Definition definition = new Definition();
        definition.addRange(new Range("10.0.0.1", "10."));
        assertEquals("Invalid range end IP address: 10.", SnmpConfigUtils.validateDefinitionIpAddresses(definition));
    }

    @Test
    public void testValidateDefinitionIpAddressesMixedVersionRange() {
        Definition definition = new Definition();
        definition.addRange(new Range("10.0.0.1", "fd00::ff"));
        assertNotNull(SnmpConfigUtils.validateDefinitionIpAddresses(definition));
    }

    @Test
    public void testValidateDefinitionIpAddressesMixedVersionRangeFullIPv6() {
        Definition definition = new Definition();
        definition.addRange(new Range("10.0.0.1", "fd00:0:0:0:0:0:0:ff"));
        assertNotNull(SnmpConfigUtils.validateDefinitionIpAddresses(definition));
    }

    // -------------------------------------------------------------------------
    // validateDefinitionIpMatches
    // -------------------------------------------------------------------------

    @Test
    public void testValidateDefinitionIpMatchesValidIPv4Wildcard() {
        Definition definition = new Definition();
        definition.addIpMatch("10.0.0.*");
        assertNull(SnmpConfigUtils.validateDefinitionIpMatches(definition));
    }

    @Test
    public void testValidateDefinitionIpMatchesValidIPv4RangeAndComma() {
        Definition definition = new Definition();
        definition.addIpMatch("192.168.1,2.1-10");
        assertNull(SnmpConfigUtils.validateDefinitionIpMatches(definition));
    }

    @Test
    public void testValidateDefinitionIpMatchesValidIPv6FullyExpanded() {
        Definition definition = new Definition();
        definition.addIpMatch("2001:db8:0:0:0:0:0:*");
        assertNull(SnmpConfigUtils.validateDefinitionIpMatches(definition));
    }

    @Test
    public void testValidateDefinitionIpMatchesValidIPv6WithWildcardCompressed() {
        Definition definition = new Definition();
        definition.addIpMatch("2001:db8::*");
        assertNull(SnmpConfigUtils.validateDefinitionIpMatches(definition));
    }

    @Test
    public void testValidateDefinitionIpMatchesValidIPv6Compressed() {
        Definition definition = new Definition();
        definition.addIpMatch("2001:db8::1");
        assertNull(SnmpConfigUtils.validateDefinitionIpMatches(definition));
    }

    @Test
    public void testValidateDefinitionIpMatchesValidIPv6LeadingCompressed() {
        Definition definition = new Definition();
        definition.addIpMatch("::1");
        assertNull(SnmpConfigUtils.validateDefinitionIpMatches(definition));
    }

    @Test
    public void testValidateDefinitionIpMatchesValidIPv6Range() {
        Definition definition = new Definition();
        definition.addIpMatch("2001:db8:0:0:0:0:0:1-ffff");
        assertNull(SnmpConfigUtils.validateDefinitionIpMatches(definition));
    }

    @Test
    public void testValidateDefinitionIpMatchesEmptyExpression() {
        Definition definition = new Definition();
        definition.addIpMatch("");
        assertEquals("Invalid IP match expression: empty value.", SnmpConfigUtils.validateDefinitionIpMatches(definition));
    }

    @Test
    public void testValidateDefinitionIpMatchesInvalidIPv4() {
        Definition definition = new Definition();
        definition.addIpMatch("10.0.0.");
        assertEquals("Invalid IP match expression: '10.0.0.'.", SnmpConfigUtils.validateDefinitionIpMatches(definition));
    }

    @Test
    public void testValidateDefinitionIpMatchesInvalidIPv6Chars() {
        Definition definition = new Definition();
        definition.addIpMatch("2001:zzzz:0:0:0:0:0:1");
        assertEquals("Invalid IP match expression: '2001:zzzz:0:0:0:0:0:1'.", SnmpConfigUtils.validateDefinitionIpMatches(definition));
    }

    @Test
    public void testValidateDefinitionIpMatchesInvalidIPv6CompressedWithBadChars() {
        Definition definition = new Definition();
        definition.addIpMatch("2001:zzzz::1");
        assertEquals("Invalid IP match expression: '2001:zzzz::1'.", SnmpConfigUtils.validateDefinitionIpMatches(definition));
    }

    @Test
    public void testValidateDefinitionIpMatchesInvalidIPv6MultipleDoubleColons() {
        Definition definition = new Definition();
        definition.addIpMatch("2001::db8::1");
        assertEquals("Invalid IP match expression: '2001::db8::1'.", SnmpConfigUtils.validateDefinitionIpMatches(definition));
    }

    // -------------------------------------------------------------------------
    // sanitizeAndValidateDefinitionItems
    // -------------------------------------------------------------------------

    @Test
    public void testSanitizeAndValidateDefinitionItemsValidSpecific() {
        ValidatedDefinitionContents result = SnmpConfigUtils.sanitizeAndValidateDefinitionItems("10.0.0.1", null, null);
        assertEquals(DefinitionContentsValidationStatus.VALID, result.status());
        assertEquals(List.of("10.0.0.1"), result.definitionSpecifics());
        assertEquals(List.of(), result.definitionRanges());
        assertEquals(List.of(), result.definitionIpMatches());
    }

    @Test
    public void testSanitizeAndValidateDefinitionItemsMultipleSpecifics() {
        ValidatedDefinitionContents result = SnmpConfigUtils.sanitizeAndValidateDefinitionItems("10.0.0.1, 10.0.0.2", null, null);
        assertEquals(DefinitionContentsValidationStatus.VALID, result.status());
        assertEquals(List.of("10.0.0.1", "10.0.0.2"), result.definitionSpecifics());
    }

    @Test
    public void testSanitizeAndValidateDefinitionItemsValidRange() {
        ValidatedDefinitionContents result = SnmpConfigUtils.sanitizeAndValidateDefinitionItems(null, "10.0.0.1-10.0.0.10", null);
        assertEquals(DefinitionContentsValidationStatus.VALID, result.status());
        assertEquals(1, result.definitionRanges().size());
        assertEquals("10.0.0.1", result.definitionRanges().get(0).getBegin());
        assertEquals("10.0.0.10", result.definitionRanges().get(0).getEnd());
    }

    @Test
    public void testSanitizeAndValidateDefinitionItemsMultipleRanges() {
        ValidatedDefinitionContents result = SnmpConfigUtils.sanitizeAndValidateDefinitionItems(null, "10.0.0.1-10.0.0.10,10.0.1.1-10.0.1.20", null);
        assertEquals(DefinitionContentsValidationStatus.VALID, result.status());
        assertEquals(2, result.definitionRanges().size());
    }

    @Test
    public void testSanitizeAndValidateDefinitionItemsValidIpMatch() {
        ValidatedDefinitionContents result = SnmpConfigUtils.sanitizeAndValidateDefinitionItems(null, null, "10.0.0.*");
        assertEquals(DefinitionContentsValidationStatus.VALID, result.status());
        assertEquals(List.of("10.0.0.*"), result.definitionIpMatches());
    }

    @Test
    public void testSanitizeAndValidateDefinitionItemsInvalidSpecific() {
        ValidatedDefinitionContents result = SnmpConfigUtils.sanitizeAndValidateDefinitionItems("10.", null, null);
        assertEquals(DefinitionContentsValidationStatus.INVALID_SPECIFIC_ADDRESS, result.status());
        assertEquals("10.", result.invalidItem());
    }

    @Test
    public void testSanitizeAndValidateDefinitionItemsInvalidRangeFormat() {
        ValidatedDefinitionContents result = SnmpConfigUtils.sanitizeAndValidateDefinitionItems(null, "10.0.0.1", null);
        assertEquals(DefinitionContentsValidationStatus.INVALID_RANGE, result.status());
    }

    @Test
    public void testSanitizeAndValidateDefinitionItemsInvalidRangeBegin() {
        ValidatedDefinitionContents result = SnmpConfigUtils.sanitizeAndValidateDefinitionItems(null, "10.-10.0.0.10", null);
        assertEquals(DefinitionContentsValidationStatus.INVALID_RANGE_BEGIN, result.status());
        assertEquals("10.", result.invalidItem());
    }

    @Test
    public void testSanitizeAndValidateDefinitionItemsInvalidRangeEnd() {
        ValidatedDefinitionContents result = SnmpConfigUtils.sanitizeAndValidateDefinitionItems(null, "10.0.0.1-10.", null);
        assertEquals(DefinitionContentsValidationStatus.INVALID_RANGE_END, result.status());
        assertEquals("10.", result.invalidItem());
    }

    @Test
    public void testSanitizeAndValidateDefinitionItemsAllEmpty() {
        ValidatedDefinitionContents result = SnmpConfigUtils.sanitizeAndValidateDefinitionItems(null, null, null);
        assertEquals(DefinitionContentsValidationStatus.INVALID_EMPTY, result.status());
    }

    @Test
    public void testSanitizeAndValidateDefinitionItemsAllBlank() {
        ValidatedDefinitionContents result = SnmpConfigUtils.sanitizeAndValidateDefinitionItems("", " ", "");
        assertEquals(DefinitionContentsValidationStatus.INVALID_EMPTY, result.status());
    }

    // -------------------------------------------------------------------------
    // safeGetInetAddress
    // -------------------------------------------------------------------------

    @Test
    public void testSafeGetInetAddressValidIPv4() {
        InetAddress addr = SnmpConfigUtils.safeGetInetAddress("192.168.1.1");
        assertNotNull(addr);
        assertEquals("192.168.1.1", addr.getHostAddress());
    }

    @Test
    public void testSafeGetInetAddressValidIPv6() {
        InetAddress addr = SnmpConfigUtils.safeGetInetAddress("2001:db8::1");
        assertNotNull(addr);
    }

    @Test
    public void testSafeGetInetAddressInvalid() {
        assertNull(SnmpConfigUtils.safeGetInetAddress("10."));
    }

    @Test
    public void testSafeGetInetAddressNull() {
        assertNull(SnmpConfigUtils.safeGetInetAddress(null));
    }

    @Test
    public void testSafeGetInetAddressEmpty() {
        assertNull(SnmpConfigUtils.safeGetInetAddress(""));
    }
}
