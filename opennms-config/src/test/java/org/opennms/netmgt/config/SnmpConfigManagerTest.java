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

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.Range;
import org.opennms.netmgt.config.snmp.SnmpConfig;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public class SnmpConfigManagerTest extends TestCase {
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String BASIC_CONFIG_PATH = "snmp-config-manager-test-basic.json";
    private static final String BASIC_IPV6_CONFIG_PATH = "snmp-config-manager-test-ipv6.json";
    private static final String BASIC_IPV6_LARGE_CONFIG_PATH = "snmp-config-manager-test-ipv6-large.json";

    private SnmpConfig getSnmpConfig(final String path) {
        SnmpConfig config = null;

        try (final InputStream inputStream = this.getClass().getResourceAsStream(path)) {
            assertNotNull("Could not read resource file from path: " + path, inputStream);

            config = mapper.readValue(new ByteArrayInputStream(IOUtils.toByteArray(inputStream)), SnmpConfig.class);
        } catch (Exception e) {
            fail("Could not read resource file from path: " + path);
        }

        return config;
    }

    // IPv4 tests

    public void testReadBasicConfig() {
        SnmpConfig config = getSnmpConfig(BASIC_CONFIG_PATH);
        assertNotNull(config);

        assertEquals("public", config.getReadCommunity());

        List<Definition> definitions = config.getDefinitions();

        assertEquals(1, definitions.size());

        Definition definition = definitions.get(0);
        assertEquals(0, definition.getSpecifics().size());
        assertEquals(0, definition.getIpMatches().size());

        assertEquals("public-1", definition.getReadCommunity());
        assertEquals(1, definition.getRanges().size());
        assertEquals("10.0.0.1", definition.getRanges().get(0).getBegin());
        assertEquals("10.0.0.9", definition.getRanges().get(0).getEnd());
    }

    public void testAddDefinitionSimpleIPv4() {
        SnmpConfig config = getSnmpConfig(BASIC_CONFIG_PATH);

        Definition definition = new Definition();
        definition.setReadCommunity("public-2");
        definition.setRanges(List.of(new Range("11.0.0.1", "11.0.0.9")));

        SnmpConfigManager manager = new SnmpConfigManager(config);
        manager.mergeIntoConfig(definition);

        SnmpConfig updatedConfig = manager.getConfig();
        assertNotNull(updatedConfig);
        assertEquals(2, updatedConfig.getDefinitions().size());

        List<Definition> updatedDefinitions = updatedConfig.getDefinitions();
        Definition definition0 = updatedDefinitions.get(0);
        assertEquals(0, definition0.getSpecifics().size());
        assertEquals(0, definition0.getIpMatches().size());

        assertEquals("public-1", definition0.getReadCommunity());
        assertEquals(1, definition0.getRanges().size());
        assertEquals("10.0.0.1", definition0.getRanges().get(0).getBegin());
        assertEquals("10.0.0.9", definition0.getRanges().get(0).getEnd());

        Definition definition1 = updatedDefinitions.get(1);
        assertEquals(0, definition1.getSpecifics().size());
        assertEquals(0, definition1.getIpMatches().size());

        assertEquals("public-2", definition1.getReadCommunity());
        assertEquals(1, definition1.getRanges().size());
        assertEquals("11.0.0.1", definition1.getRanges().get(0).getBegin());
        assertEquals("11.0.0.9", definition1.getRanges().get(0).getEnd());
    }

    // Merge multiple non-contiguous ranges into a config that has no overlap with them.
    public void testMergeMultipleNonContiguousRangesIPv4() {
        SnmpConfig config = getSnmpConfig(BASIC_CONFIG_PATH);

        Definition definition = new Definition();
        definition.setReadCommunity("public-2");
        definition.setRanges(List.of(new Range("12.0.0.1", "12.0.0.9"), new Range("192.168.0.1", "192.168.10.1")));

        SnmpConfigManager manager = new SnmpConfigManager(config);
        manager.mergeIntoConfig(definition);

        SnmpConfig updatedConfig = manager.getConfig();
        assertNotNull(updatedConfig);
        assertEquals(2, updatedConfig.getDefinitions().size());

        List<Definition> updatedDefinitions = updatedConfig.getDefinitions();

        // Original definition is unchanged
        Definition definition0 = updatedDefinitions.get(0);
        assertEquals(0, definition0.getSpecifics().size());
        assertEquals(0, definition0.getIpMatches().size());
        assertEquals("public-1", definition0.getReadCommunity());
        assertEquals(1, definition0.getRanges().size());
        assertEquals("10.0.0.1", definition0.getRanges().get(0).getBegin());
        assertEquals("10.0.0.9", definition0.getRanges().get(0).getEnd());

        // New definition with both non-contiguous ranges
        Definition definition1 = updatedDefinitions.get(1);
        assertEquals(0, definition1.getSpecifics().size());
        assertEquals(0, definition1.getIpMatches().size());
        assertEquals("public-2", definition1.getReadCommunity());
        assertEquals(2, definition1.getRanges().size());
        assertEquals("12.0.0.1", definition1.getRanges().get(0).getBegin());
        assertEquals("12.0.0.9", definition1.getRanges().get(0).getEnd());
        assertEquals("192.168.0.1", definition1.getRanges().get(1).getBegin());
        assertEquals("192.168.10.1", definition1.getRanges().get(1).getEnd());
    }

    // Merge an overlapping range with the same readCommunity.
    // The overlapping portion is removed from the existing definition, then the new range is merged back in.
    // Adjacent ranges are coalesced, so the result is a single extended range.
    public void testMergeOverlappingRangeSameCommunityIPv4() {
        SnmpConfig config = getSnmpConfig(BASIC_CONFIG_PATH);

        // Overlaps with 10.0.0.1-10.0.0.9 and extends to 10.0.0.12; same community
        Definition definition = new Definition();
        definition.setReadCommunity("public-1");
        definition.setRanges(List.of(new Range("10.0.0.8", "10.0.0.12")));

        SnmpConfigManager manager = new SnmpConfigManager(config);
        manager.mergeIntoConfig(definition);

        SnmpConfig updatedConfig = manager.getConfig();
        assertNotNull(updatedConfig);
        assertEquals(1, updatedConfig.getDefinitions().size());

        // 10.0.0.1-10.0.0.7 (surviving) adjoins 10.0.0.8-10.0.0.12 (new), so they coalesce
        Definition definition0 = updatedConfig.getDefinitions().get(0);
        assertEquals(0, definition0.getSpecifics().size());
        assertEquals(0, definition0.getIpMatches().size());
        assertEquals("public-1", definition0.getReadCommunity());
        assertEquals(1, definition0.getRanges().size());
        assertEquals("10.0.0.1", definition0.getRanges().get(0).getBegin());
        assertEquals("10.0.0.12", definition0.getRanges().get(0).getEnd());
    }

    // Merge an overlapping range with a different readCommunity.
    // The overlap is stripped from the existing definition; a new definition is added.
    public void testMergeOverlappingRangeDifferentCommunityIPv4() {
        SnmpConfig config = getSnmpConfig(BASIC_CONFIG_PATH);

        // Overlaps with 10.0.0.1-10.0.0.9 at 10.0.0.8-10.0.0.9; different community
        Definition definition = new Definition();
        definition.setReadCommunity("public-2");
        definition.setRanges(List.of(new Range("10.0.0.8", "10.0.0.12")));

        SnmpConfigManager manager = new SnmpConfigManager(config);
        manager.mergeIntoConfig(definition);

        SnmpConfig updatedConfig = manager.getConfig();
        assertNotNull(updatedConfig);
        assertEquals(2, updatedConfig.getDefinitions().size());

        List<Definition> updatedDefinitions = updatedConfig.getDefinitions();

        // Existing definition is trimmed to 10.0.0.1-10.0.0.7
        Definition definition0 = updatedDefinitions.get(0);
        assertEquals(0, definition0.getSpecifics().size());
        assertEquals(0, definition0.getIpMatches().size());
        assertEquals("public-1", definition0.getReadCommunity());
        assertEquals(1, definition0.getRanges().size());
        assertEquals("10.0.0.1", definition0.getRanges().get(0).getBegin());
        assertEquals("10.0.0.7", definition0.getRanges().get(0).getEnd());

        // New definition owns the overlapping and extending portion
        Definition definition1 = updatedDefinitions.get(1);
        assertEquals(0, definition1.getSpecifics().size());
        assertEquals(0, definition1.getIpMatches().size());
        assertEquals("public-2", definition1.getReadCommunity());
        assertEquals(1, definition1.getRanges().size());
        assertEquals("10.0.0.8", definition1.getRanges().get(0).getBegin());
        assertEquals("10.0.0.12", definition1.getRanges().get(0).getEnd());
    }

    // Two sequential merges with multiple overlapping ranges.
    // First merge: ranges 12.0.0.1-12.0.0.9 and 12.0.0.3-12.0.0.17 (same community public-1 as existing) coalesce to 12.0.0.1-12.0.0.17.
    // Second merge: range 10.0.0.3-10.0.0.11 with community public-3 strips the overlap from public-1.
    public void testMergeMultipleOverlappingRangesIPv4() {
        SnmpConfig config = getSnmpConfig(BASIC_CONFIG_PATH);
        SnmpConfigManager manager = new SnmpConfigManager(config);

        // First merge: overlapping ranges with the same community as the existing definition;
        // they coalesce to 12.0.0.1-12.0.0.17 and are merged into the public-1 definition
        Definition firstDefinition = new Definition();
        firstDefinition.setReadCommunity("public-1");
        firstDefinition.setRanges(List.of(new Range("12.0.0.1", "12.0.0.9"), new Range("12.0.0.3", "12.0.0.17")));
        manager.mergeIntoConfig(firstDefinition);

        // Second merge: public-3 range overlaps with 10.0.0.3-10.0.0.9 (part of public-1's 10.0.0.1-10.0.0.9)
        Definition secondDefinition = new Definition();
        secondDefinition.setReadCommunity("public-3");
        secondDefinition.setRanges(List.of(new Range("10.0.0.3", "10.0.0.11")));
        manager.mergeIntoConfig(secondDefinition);

        SnmpConfig updatedConfig = manager.getConfig();
        assertNotNull(updatedConfig);
        assertEquals(2, updatedConfig.getDefinitions().size());

        List<Definition> updatedDefinitions = updatedConfig.getDefinitions();

        // public-1 retains 10.0.0.1-10.0.0.2 (trimmed by public-3) and 12.0.0.1-12.0.0.17
        Definition definition0 = updatedDefinitions.get(0);
        assertEquals(0, definition0.getSpecifics().size());
        assertEquals(0, definition0.getIpMatches().size());
        assertEquals("public-1", definition0.getReadCommunity());
        assertEquals(2, definition0.getRanges().size());
        assertEquals("10.0.0.1", definition0.getRanges().get(0).getBegin());
        assertEquals("10.0.0.2", definition0.getRanges().get(0).getEnd());
        assertEquals("12.0.0.1", definition0.getRanges().get(1).getBegin());
        assertEquals("12.0.0.17", definition0.getRanges().get(1).getEnd());

        // public-3 owns 10.0.0.3-10.0.0.11
        Definition definition1 = updatedDefinitions.get(1);
        assertEquals(0, definition1.getSpecifics().size());
        assertEquals(0, definition1.getIpMatches().size());
        assertEquals("public-3", definition1.getReadCommunity());
        assertEquals(1, definition1.getRanges().size());
        assertEquals("10.0.0.3", definition1.getRanges().get(0).getBegin());
        assertEquals("10.0.0.11", definition1.getRanges().get(0).getEnd());
    }

    // Remove the entire definition from the config.
    public void testRemoveEntireDefinitionIPv4() {
        SnmpConfig config = getSnmpConfig(BASIC_CONFIG_PATH);

        Definition definition = new Definition();
        definition.setReadCommunity("public-1");
        definition.setRanges(List.of(new Range("10.0.0.1", "10.0.0.9")));

        SnmpConfigManager manager = new SnmpConfigManager(config);
        boolean removed = manager.removeDefinition(definition);

        assertTrue(removed);
        assertEquals(0, manager.getConfig().getDefinitions().size());
    }

    // Remove part of the definition from the config.
    // Removing 10.0.0.3-10.0.0.6 from 10.0.0.1-10.0.0.9 splits the range into two.
    public void testRemovePartialDefinitionIPv4() {
        SnmpConfig config = getSnmpConfig(BASIC_CONFIG_PATH);

        Definition definition = new Definition();
        definition.setReadCommunity("public-1");
        definition.setRanges(List.of(new Range("10.0.0.3", "10.0.0.6")));

        SnmpConfigManager manager = new SnmpConfigManager(config);
        boolean removed = manager.removeDefinition(definition);

        assertTrue(removed);

        SnmpConfig updatedConfig = manager.getConfig();
        assertEquals(1, updatedConfig.getDefinitions().size());

        Definition definition0 = updatedConfig.getDefinitions().get(0);
        assertEquals(0, definition0.getSpecifics().size());
        assertEquals(0, definition0.getIpMatches().size());
        assertEquals("public-1", definition0.getReadCommunity());
        assertEquals(2, definition0.getRanges().size());
        assertEquals("10.0.0.1", definition0.getRanges().get(0).getBegin());
        assertEquals("10.0.0.2", definition0.getRanges().get(0).getEnd());
        assertEquals("10.0.0.7", definition0.getRanges().get(1).getBegin());
        assertEquals("10.0.0.9", definition0.getRanges().get(1).getEnd());
    }

    // IPv6 tests — mirror all of the above using the IPv6 config (fd00::1-fd00::9 / public-1)

    public void testReadBasicConfigIPv6() {
        SnmpConfig config = getSnmpConfig(BASIC_IPV6_CONFIG_PATH);
        assertNotNull(config);

        assertEquals("public", config.getReadCommunity());

        List<Definition> definitions = config.getDefinitions();
        assertEquals(1, definitions.size());

        Definition definition = definitions.get(0);
        assertEquals(0, definition.getSpecifics().size());
        assertEquals(0, definition.getIpMatches().size());

        assertEquals("public-1", definition.getReadCommunity());
        assertEquals(1, definition.getRanges().size());
        assertEquals("fd00::1", definition.getRanges().get(0).getBegin());
        assertEquals("fd00::9", definition.getRanges().get(0).getEnd());
    }

    public void testAddDefinitionSimpleIPv6() {
        SnmpConfig config = getSnmpConfig(BASIC_IPV6_CONFIG_PATH);

        Definition definition = new Definition();
        definition.setReadCommunity("public-2");
        definition.setRanges(List.of(new Range("fd00:1::1", "fd00:1::9")));

        SnmpConfigManager manager = new SnmpConfigManager(config);
        manager.mergeIntoConfig(definition);

        SnmpConfig updatedConfig = manager.getConfig();
        assertNotNull(updatedConfig);
        assertEquals(2, updatedConfig.getDefinitions().size());

        List<Definition> updatedDefinitions = updatedConfig.getDefinitions();
        Definition definition0 = updatedDefinitions.get(0);
        assertEquals(0, definition0.getSpecifics().size());
        assertEquals(0, definition0.getIpMatches().size());

        assertEquals("public-1", definition0.getReadCommunity());
        assertEquals(1, definition0.getRanges().size());
        assertEquals("fd00::1", definition0.getRanges().get(0).getBegin());
        assertEquals("fd00::9", definition0.getRanges().get(0).getEnd());

        Definition definition1 = updatedDefinitions.get(1);
        assertEquals(0, definition1.getSpecifics().size());
        assertEquals(0, definition1.getIpMatches().size());

        assertEquals("public-2", definition1.getReadCommunity());
        assertEquals(1, definition1.getRanges().size());
        assertEquals("fd00:1::1", definition1.getRanges().get(0).getBegin());
        assertEquals("fd00:1::9", definition1.getRanges().get(0).getEnd());
    }

    public void testMergeMultipleNonContiguousRangesIPv6() {
        SnmpConfig config = getSnmpConfig(BASIC_IPV6_CONFIG_PATH);

        Definition definition = new Definition();
        definition.setReadCommunity("public-2");
        definition.setRanges(List.of(new Range("fd00:2::1", "fd00:2::9"), new Range("fd00:3::1", "fd00:3::ff")));

        SnmpConfigManager manager = new SnmpConfigManager(config);
        manager.mergeIntoConfig(definition);

        SnmpConfig updatedConfig = manager.getConfig();
        assertNotNull(updatedConfig);
        assertEquals(2, updatedConfig.getDefinitions().size());

        List<Definition> updatedDefinitions = updatedConfig.getDefinitions();

        // Original definition is unchanged
        Definition definition0 = updatedDefinitions.get(0);
        assertEquals(0, definition0.getSpecifics().size());
        assertEquals(0, definition0.getIpMatches().size());
        assertEquals("public-1", definition0.getReadCommunity());
        assertEquals(1, definition0.getRanges().size());
        assertEquals("fd00::1", definition0.getRanges().get(0).getBegin());
        assertEquals("fd00::9", definition0.getRanges().get(0).getEnd());

        // New definition with both non-contiguous ranges
        Definition definition1 = updatedDefinitions.get(1);
        assertEquals(0, definition1.getSpecifics().size());
        assertEquals(0, definition1.getIpMatches().size());
        assertEquals("public-2", definition1.getReadCommunity());
        assertEquals(2, definition1.getRanges().size());
        assertEquals("fd00:2::1", definition1.getRanges().get(0).getBegin());
        assertEquals("fd00:2::9", definition1.getRanges().get(0).getEnd());
        assertEquals("fd00:3::1", definition1.getRanges().get(1).getBegin());
        assertEquals("fd00:3::ff", definition1.getRanges().get(1).getEnd());
    }

    // Merge an overlapping range with the same readCommunity.
    // fd00::8-fd00::c overlaps fd00::1-fd00::9; after purge the surviving fd00::1-fd00::7
    // adjoins the incoming fd00::8-fd00::c and they coalesce to fd00::1-fd00::c.
    public void testMergeOverlappingRangeSameCommunityIPv6() {
        SnmpConfig config = getSnmpConfig(BASIC_IPV6_CONFIG_PATH);

        Definition definition = new Definition();
        definition.setReadCommunity("public-1");
        definition.setRanges(List.of(new Range("fd00::8", "fd00::c")));

        SnmpConfigManager manager = new SnmpConfigManager(config);
        manager.mergeIntoConfig(definition);

        SnmpConfig updatedConfig = manager.getConfig();
        assertNotNull(updatedConfig);
        assertEquals(1, updatedConfig.getDefinitions().size());

        Definition definition0 = updatedConfig.getDefinitions().get(0);
        assertEquals(0, definition0.getSpecifics().size());
        assertEquals(0, definition0.getIpMatches().size());
        assertEquals("public-1", definition0.getReadCommunity());
        assertEquals(1, definition0.getRanges().size());
        assertEquals("fd00::1", definition0.getRanges().get(0).getBegin());
        assertEquals("fd00::c", definition0.getRanges().get(0).getEnd());
    }

    // Merge an overlapping range with a different readCommunity.
    // The overlap is stripped from the existing definition; a new definition is added.
    public void testMergeOverlappingRangeDifferentCommunityIPv6() {
        SnmpConfig config = getSnmpConfig(BASIC_IPV6_CONFIG_PATH);

        Definition definition = new Definition();
        definition.setReadCommunity("public-2");
        definition.setRanges(List.of(new Range("fd00::8", "fd00::c")));

        SnmpConfigManager manager = new SnmpConfigManager(config);
        manager.mergeIntoConfig(definition);

        SnmpConfig updatedConfig = manager.getConfig();
        assertNotNull(updatedConfig);
        assertEquals(2, updatedConfig.getDefinitions().size());

        List<Definition> updatedDefinitions = updatedConfig.getDefinitions();

        // Existing definition is trimmed to fd00::1-fd00::7
        Definition definition0 = updatedDefinitions.get(0);
        assertEquals(0, definition0.getSpecifics().size());
        assertEquals(0, definition0.getIpMatches().size());
        assertEquals("public-1", definition0.getReadCommunity());
        assertEquals(1, definition0.getRanges().size());
        assertEquals("fd00::1", definition0.getRanges().get(0).getBegin());
        assertEquals("fd00::7", definition0.getRanges().get(0).getEnd());

        // New definition owns the overlapping and extending portion
        Definition definition1 = updatedDefinitions.get(1);
        assertEquals(0, definition1.getSpecifics().size());
        assertEquals(0, definition1.getIpMatches().size());
        assertEquals("public-2", definition1.getReadCommunity());
        assertEquals(1, definition1.getRanges().size());
        assertEquals("fd00::8", definition1.getRanges().get(0).getBegin());
        assertEquals("fd00::c", definition1.getRanges().get(0).getEnd());
    }

    // Two sequential merges with multiple overlapping ranges.
    // First merge: fd00:2::1-fd00:2::9 and fd00:2::3-fd00:2::11 (public-1) coalesce to fd00:2::1-fd00:2::11
    // and are merged into the existing public-1 definition.
    // Second merge: fd00::3-fd00::b (public-3) strips the overlap from public-1's fd00::1-fd00::9.
    public void testMergeMultipleOverlappingRangesIPv6() {
        SnmpConfig config = getSnmpConfig(BASIC_IPV6_CONFIG_PATH);
        SnmpConfigManager manager = new SnmpConfigManager(config);

        Definition firstDefinition = new Definition();
        firstDefinition.setReadCommunity("public-1");
        firstDefinition.setRanges(List.of(new Range("fd00:2::1", "fd00:2::9"), new Range("fd00:2::3", "fd00:2::11")));
        manager.mergeIntoConfig(firstDefinition);

        Definition secondDefinition = new Definition();
        secondDefinition.setReadCommunity("public-3");
        secondDefinition.setRanges(List.of(new Range("fd00::3", "fd00::b")));
        manager.mergeIntoConfig(secondDefinition);

        SnmpConfig updatedConfig = manager.getConfig();
        assertNotNull(updatedConfig);
        assertEquals(2, updatedConfig.getDefinitions().size());

        List<Definition> updatedDefinitions = updatedConfig.getDefinitions();

        // public-1 retains fd00::1-fd00::2 (trimmed by public-3) and fd00:2::1-fd00:2::11
        Definition definition0 = updatedDefinitions.get(0);
        assertEquals(0, definition0.getSpecifics().size());
        assertEquals(0, definition0.getIpMatches().size());
        assertEquals("public-1", definition0.getReadCommunity());
        assertEquals(2, definition0.getRanges().size());
        assertEquals("fd00::1", definition0.getRanges().get(0).getBegin());
        assertEquals("fd00::2", definition0.getRanges().get(0).getEnd());
        assertEquals("fd00:2::1", definition0.getRanges().get(1).getBegin());
        assertEquals("fd00:2::11", definition0.getRanges().get(1).getEnd());

        // public-3 owns fd00::3-fd00::b
        Definition definition1 = updatedDefinitions.get(1);
        assertEquals(0, definition1.getSpecifics().size());
        assertEquals(0, definition1.getIpMatches().size());
        assertEquals("public-3", definition1.getReadCommunity());
        assertEquals(1, definition1.getRanges().size());
        assertEquals("fd00::3", definition1.getRanges().get(0).getBegin());
        assertEquals("fd00::b", definition1.getRanges().get(0).getEnd());
    }

    public void testRemoveEntireDefinitionIPv6() {
        SnmpConfig config = getSnmpConfig(BASIC_IPV6_CONFIG_PATH);

        Definition definition = new Definition();
        definition.setReadCommunity("public-1");
        definition.setRanges(List.of(new Range("fd00::1", "fd00::9")));

        SnmpConfigManager manager = new SnmpConfigManager(config);
        boolean removed = manager.removeDefinition(definition);

        assertTrue(removed);
        assertEquals(0, manager.getConfig().getDefinitions().size());
    }

    // Removing fd00::3-fd00::6 from fd00::1-fd00::9 splits the range into two.
    public void testRemovePartialDefinitionIPv6() {
        SnmpConfig config = getSnmpConfig(BASIC_IPV6_CONFIG_PATH);

        Definition definition = new Definition();
        definition.setReadCommunity("public-1");
        definition.setRanges(List.of(new Range("fd00::3", "fd00::6")));

        SnmpConfigManager manager = new SnmpConfigManager(config);
        boolean removed = manager.removeDefinition(definition);

        assertTrue(removed);

        SnmpConfig updatedConfig = manager.getConfig();
        assertEquals(1, updatedConfig.getDefinitions().size());

        Definition definition0 = updatedConfig.getDefinitions().get(0);
        assertEquals(0, definition0.getSpecifics().size());
        assertEquals(0, definition0.getIpMatches().size());
        assertEquals("public-1", definition0.getReadCommunity());
        assertEquals(2, definition0.getRanges().size());
        assertEquals("fd00::1", definition0.getRanges().get(0).getBegin());
        assertEquals("fd00::2", definition0.getRanges().get(0).getEnd());
        assertEquals("fd00::7", definition0.getRanges().get(1).getBegin());
        assertEquals("fd00::9", definition0.getRanges().get(1).getEnd());
    }

    // Large 8-group IPv6 tests — all 8 hextets are non-zero so no :: compression occurs.
    // Basic config range: 2001:db8:1:2:3:4:5:1-2001:db8:1:2:3:4:5:9 / public-1

    public void testReadBasicConfigIPv6Large() {
        SnmpConfig config = getSnmpConfig(BASIC_IPV6_LARGE_CONFIG_PATH);
        assertNotNull(config);

        assertEquals("public", config.getReadCommunity());

        List<Definition> definitions = config.getDefinitions();
        assertEquals(1, definitions.size());

        Definition definition = definitions.get(0);
        assertEquals(0, definition.getSpecifics().size());
        assertEquals(0, definition.getIpMatches().size());

        assertEquals("public-1", definition.getReadCommunity());
        assertEquals(1, definition.getRanges().size());
        assertEquals("2001:db8:1:2:3:4:5:1", definition.getRanges().get(0).getBegin());
        assertEquals("2001:db8:1:2:3:4:5:9", definition.getRanges().get(0).getEnd());
    }

    public void testAddDefinitionSimpleIPv6Large() {
        SnmpConfig config = getSnmpConfig(BASIC_IPV6_LARGE_CONFIG_PATH);

        Definition definition = new Definition();
        definition.setReadCommunity("public-2");
        definition.setRanges(List.of(new Range("2001:db8:1:2:3:4:6:1", "2001:db8:1:2:3:4:6:9")));

        SnmpConfigManager manager = new SnmpConfigManager(config);
        manager.mergeIntoConfig(definition);

        SnmpConfig updatedConfig = manager.getConfig();
        assertNotNull(updatedConfig);
        assertEquals(2, updatedConfig.getDefinitions().size());

        List<Definition> updatedDefinitions = updatedConfig.getDefinitions();
        Definition definition0 = updatedDefinitions.get(0);
        assertEquals(0, definition0.getSpecifics().size());
        assertEquals(0, definition0.getIpMatches().size());

        assertEquals("public-1", definition0.getReadCommunity());
        assertEquals(1, definition0.getRanges().size());
        assertEquals("2001:db8:1:2:3:4:5:1", definition0.getRanges().get(0).getBegin());
        assertEquals("2001:db8:1:2:3:4:5:9", definition0.getRanges().get(0).getEnd());

        Definition definition1 = updatedDefinitions.get(1);
        assertEquals(0, definition1.getSpecifics().size());
        assertEquals(0, definition1.getIpMatches().size());

        assertEquals("public-2", definition1.getReadCommunity());
        assertEquals(1, definition1.getRanges().size());
        assertEquals("2001:db8:1:2:3:4:6:1", definition1.getRanges().get(0).getBegin());
        assertEquals("2001:db8:1:2:3:4:6:9", definition1.getRanges().get(0).getEnd());
    }

    public void testMergeMultipleNonContiguousRangesIPv6Large() {
        SnmpConfig config = getSnmpConfig(BASIC_IPV6_LARGE_CONFIG_PATH);

        Definition definition = new Definition();
        definition.setReadCommunity("public-2");
        definition.setRanges(List.of(
                new Range("2001:db8:1:2:3:4:7:1", "2001:db8:1:2:3:4:7:9"),
                new Range("2001:db8:1:2:3:4:8:1", "2001:db8:1:2:3:4:8:ff")));

        SnmpConfigManager manager = new SnmpConfigManager(config);
        manager.mergeIntoConfig(definition);

        SnmpConfig updatedConfig = manager.getConfig();
        assertNotNull(updatedConfig);
        assertEquals(2, updatedConfig.getDefinitions().size());

        List<Definition> updatedDefinitions = updatedConfig.getDefinitions();

        // Original definition is unchanged
        Definition definition0 = updatedDefinitions.get(0);
        assertEquals(0, definition0.getSpecifics().size());
        assertEquals(0, definition0.getIpMatches().size());
        assertEquals("public-1", definition0.getReadCommunity());
        assertEquals(1, definition0.getRanges().size());
        assertEquals("2001:db8:1:2:3:4:5:1", definition0.getRanges().get(0).getBegin());
        assertEquals("2001:db8:1:2:3:4:5:9", definition0.getRanges().get(0).getEnd());

        // New definition with both non-contiguous ranges
        Definition definition1 = updatedDefinitions.get(1);
        assertEquals(0, definition1.getSpecifics().size());
        assertEquals(0, definition1.getIpMatches().size());
        assertEquals("public-2", definition1.getReadCommunity());
        assertEquals(2, definition1.getRanges().size());
        assertEquals("2001:db8:1:2:3:4:7:1", definition1.getRanges().get(0).getBegin());
        assertEquals("2001:db8:1:2:3:4:7:9", definition1.getRanges().get(0).getEnd());
        assertEquals("2001:db8:1:2:3:4:8:1", definition1.getRanges().get(1).getBegin());
        assertEquals("2001:db8:1:2:3:4:8:ff", definition1.getRanges().get(1).getEnd());
    }

    // Merge an overlapping range with the same readCommunity.
    // 2001:db8:1:2:3:4:5:8-2001:db8:1:2:3:4:5:c overlaps 2001:db8:1:2:3:4:5:1-2001:db8:1:2:3:4:5:9;
    // after purge the surviving 2001:db8:1:2:3:4:5:1-2001:db8:1:2:3:4:5:7 adjoins the incoming range
    // and they coalesce to 2001:db8:1:2:3:4:5:1-2001:db8:1:2:3:4:5:c.
    public void testMergeOverlappingRangeSameCommunityIPv6Large() {
        SnmpConfig config = getSnmpConfig(BASIC_IPV6_LARGE_CONFIG_PATH);

        Definition definition = new Definition();
        definition.setReadCommunity("public-1");
        definition.setRanges(List.of(new Range("2001:db8:1:2:3:4:5:8", "2001:db8:1:2:3:4:5:c")));

        SnmpConfigManager manager = new SnmpConfigManager(config);
        manager.mergeIntoConfig(definition);

        SnmpConfig updatedConfig = manager.getConfig();
        assertNotNull(updatedConfig);
        assertEquals(1, updatedConfig.getDefinitions().size());

        Definition definition0 = updatedConfig.getDefinitions().get(0);
        assertEquals(0, definition0.getSpecifics().size());
        assertEquals(0, definition0.getIpMatches().size());
        assertEquals("public-1", definition0.getReadCommunity());
        assertEquals(1, definition0.getRanges().size());
        assertEquals("2001:db8:1:2:3:4:5:1", definition0.getRanges().get(0).getBegin());
        assertEquals("2001:db8:1:2:3:4:5:c", definition0.getRanges().get(0).getEnd());
    }

    // Merge an overlapping range with a different readCommunity.
    // The overlap is stripped from the existing definition; a new definition is added.
    public void testMergeOverlappingRangeDifferentCommunityIPv6Large() {
        SnmpConfig config = getSnmpConfig(BASIC_IPV6_LARGE_CONFIG_PATH);

        Definition definition = new Definition();
        definition.setReadCommunity("public-2");
        definition.setRanges(List.of(new Range("2001:db8:1:2:3:4:5:8", "2001:db8:1:2:3:4:5:c")));

        SnmpConfigManager manager = new SnmpConfigManager(config);
        manager.mergeIntoConfig(definition);

        SnmpConfig updatedConfig = manager.getConfig();
        assertNotNull(updatedConfig);
        assertEquals(2, updatedConfig.getDefinitions().size());

        List<Definition> updatedDefinitions = updatedConfig.getDefinitions();

        // Existing definition is trimmed to 2001:db8:1:2:3:4:5:1-2001:db8:1:2:3:4:5:7
        Definition definition0 = updatedDefinitions.get(0);
        assertEquals(0, definition0.getSpecifics().size());
        assertEquals(0, definition0.getIpMatches().size());
        assertEquals("public-1", definition0.getReadCommunity());
        assertEquals(1, definition0.getRanges().size());
        assertEquals("2001:db8:1:2:3:4:5:1", definition0.getRanges().get(0).getBegin());
        assertEquals("2001:db8:1:2:3:4:5:7", definition0.getRanges().get(0).getEnd());

        // New definition owns the overlapping and extending portion
        Definition definition1 = updatedDefinitions.get(1);
        assertEquals(0, definition1.getSpecifics().size());
        assertEquals(0, definition1.getIpMatches().size());
        assertEquals("public-2", definition1.getReadCommunity());
        assertEquals(1, definition1.getRanges().size());
        assertEquals("2001:db8:1:2:3:4:5:8", definition1.getRanges().get(0).getBegin());
        assertEquals("2001:db8:1:2:3:4:5:c", definition1.getRanges().get(0).getEnd());
    }

    // Two sequential merges with multiple overlapping ranges.
    // First merge: 2001:db8:1:2:3:4:7:1-2001:db8:1:2:3:4:7:9 and 2001:db8:1:2:3:4:7:3-2001:db8:1:2:3:4:7:11
    // (public-1) coalesce to 2001:db8:1:2:3:4:7:1-2001:db8:1:2:3:4:7:11 and merge into existing public-1.
    // Second merge: 2001:db8:1:2:3:4:5:3-2001:db8:1:2:3:4:5:b (public-3) strips the overlap from public-1.
    public void testMergeMultipleOverlappingRangesIPv6Large() {
        SnmpConfig config = getSnmpConfig(BASIC_IPV6_LARGE_CONFIG_PATH);
        SnmpConfigManager manager = new SnmpConfigManager(config);

        Definition firstDefinition = new Definition();
        firstDefinition.setReadCommunity("public-1");
        firstDefinition.setRanges(List.of(
                new Range("2001:db8:1:2:3:4:7:1", "2001:db8:1:2:3:4:7:9"),
                new Range("2001:db8:1:2:3:4:7:3", "2001:db8:1:2:3:4:7:11")));
        manager.mergeIntoConfig(firstDefinition);

        Definition secondDefinition = new Definition();
        secondDefinition.setReadCommunity("public-3");
        secondDefinition.setRanges(List.of(new Range("2001:db8:1:2:3:4:5:3", "2001:db8:1:2:3:4:5:b")));
        manager.mergeIntoConfig(secondDefinition);

        SnmpConfig updatedConfig = manager.getConfig();
        assertNotNull(updatedConfig);
        assertEquals(2, updatedConfig.getDefinitions().size());

        List<Definition> updatedDefinitions = updatedConfig.getDefinitions();

        // public-1 retains 2001:db8:1:2:3:4:5:1-2001:db8:1:2:3:4:5:2 (trimmed by public-3)
        // and 2001:db8:1:2:3:4:7:1-2001:db8:1:2:3:4:7:11
        Definition definition0 = updatedDefinitions.get(0);
        assertEquals(0, definition0.getSpecifics().size());
        assertEquals(0, definition0.getIpMatches().size());
        assertEquals("public-1", definition0.getReadCommunity());
        assertEquals(2, definition0.getRanges().size());
        assertEquals("2001:db8:1:2:3:4:5:1", definition0.getRanges().get(0).getBegin());
        assertEquals("2001:db8:1:2:3:4:5:2", definition0.getRanges().get(0).getEnd());
        assertEquals("2001:db8:1:2:3:4:7:1", definition0.getRanges().get(1).getBegin());
        assertEquals("2001:db8:1:2:3:4:7:11", definition0.getRanges().get(1).getEnd());

        // public-3 owns 2001:db8:1:2:3:4:5:3-2001:db8:1:2:3:4:5:b
        Definition definition1 = updatedDefinitions.get(1);
        assertEquals(0, definition1.getSpecifics().size());
        assertEquals(0, definition1.getIpMatches().size());
        assertEquals("public-3", definition1.getReadCommunity());
        assertEquals(1, definition1.getRanges().size());
        assertEquals("2001:db8:1:2:3:4:5:3", definition1.getRanges().get(0).getBegin());
        assertEquals("2001:db8:1:2:3:4:5:b", definition1.getRanges().get(0).getEnd());
    }

    public void testRemoveEntireDefinitionIPv6Large() {
        SnmpConfig config = getSnmpConfig(BASIC_IPV6_LARGE_CONFIG_PATH);

        Definition definition = new Definition();
        definition.setReadCommunity("public-1");
        definition.setRanges(List.of(new Range("2001:db8:1:2:3:4:5:1", "2001:db8:1:2:3:4:5:9")));

        SnmpConfigManager manager = new SnmpConfigManager(config);
        boolean removed = manager.removeDefinition(definition);

        assertTrue(removed);
        assertEquals(0, manager.getConfig().getDefinitions().size());
    }

    // Removing 2001:db8:1:2:3:4:5:3-2001:db8:1:2:3:4:5:6 from 2001:db8:1:2:3:4:5:1-2001:db8:1:2:3:4:5:9
    // splits the range into two.
    public void testRemovePartialDefinitionIPv6Large() {
        SnmpConfig config = getSnmpConfig(BASIC_IPV6_LARGE_CONFIG_PATH);

        Definition definition = new Definition();
        definition.setReadCommunity("public-1");
        definition.setRanges(List.of(new Range("2001:db8:1:2:3:4:5:3", "2001:db8:1:2:3:4:5:6")));

        SnmpConfigManager manager = new SnmpConfigManager(config);
        boolean removed = manager.removeDefinition(definition);

        assertTrue(removed);

        SnmpConfig updatedConfig = manager.getConfig();
        assertEquals(1, updatedConfig.getDefinitions().size());

        Definition definition0 = updatedConfig.getDefinitions().get(0);
        assertEquals(0, definition0.getSpecifics().size());
        assertEquals(0, definition0.getIpMatches().size());
        assertEquals("public-1", definition0.getReadCommunity());
        assertEquals(2, definition0.getRanges().size());
        assertEquals("2001:db8:1:2:3:4:5:1", definition0.getRanges().get(0).getBegin());
        assertEquals("2001:db8:1:2:3:4:5:2", definition0.getRanges().get(0).getEnd());
        assertEquals("2001:db8:1:2:3:4:5:7", definition0.getRanges().get(1).getBegin());
        assertEquals("2001:db8:1:2:3:4:5:9", definition0.getRanges().get(1).getEnd());
    }
}
