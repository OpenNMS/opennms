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
package org.opennms.config.upgrade.datacollection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.SystemDef;

/**
 * Tests for {@link SnmpDataCollectionDefaultsUpdate} that don't need a database.
 * DB behavior (ledger, insert/merge, no-resurrection, archival) is covered by
 * SnmpDataCollectionDefaultsUpdateIT.
 */
public class SnmpDataCollectionDefaultsUpdateTest {

    private static final String NETSNMP_XML =
            "../../../opennms-base-assembly/src/main/filtered/etc/datacollection/netsnmp.xml";

    private static final SnmpDataCollectionDefaultsUpdate.DefaultConfigUpdate UCD_MEMORY_X_UPDATE =
            SnmpDataCollectionDefaultsUpdate.UPDATES.get(0);

    @Test
    public void registryEntriesAreWellFormed() throws SQLException {
        final Set<String> ids = new HashSet<>();
        for (final SnmpDataCollectionDefaultsUpdate.DefaultConfigUpdate update : SnmpDataCollectionDefaultsUpdate.UPDATES) {
            assertNotNull(update.id);
            assertNotNull(update.description);
            assertTrue("duplicate update id: " + update.id, ids.add(update.id));
            // every registered fragment must load and parse
            final DatacollectionGroup fragment = new SnmpDataCollectionDefaultsUpdate().loadFragment(update.resourcePath);
            assertNotNull(fragment.getName());
        }
    }

    @Test
    public void ucdMemoryXFragmentParses() throws SQLException {
        final DatacollectionGroup fragment =
                new SnmpDataCollectionDefaultsUpdate().loadFragment(UCD_MEMORY_X_UPDATE.resourcePath);

        assertEquals("Net-SNMP", fragment.getName());
        assertEquals(1, fragment.getGroups().size());

        final Group group = fragment.getGroups().get(0);
        assertEquals("ucd-memory-X", group.getName());
        assertEquals("ignore", group.getIfType());
        assertEquals(9, group.getMibObjs().size());

        final List<String> aliases = group.getMibObjs().stream()
                .map(MibObj::getAlias)
                .collect(Collectors.toList());
        assertEquals(List.of("memTotalSwapX", "memAvailSwapX", "memTotalRealX", "memAvailRealX",
                "memTotalFreeX", "memSharedX", "memBufferX", "memCachedX", "memSysAvail"), aliases);

        // The systemDef stub: ucd-memory anchors, ucd-memory-X is the reference to add.
        assertEquals(1, fragment.getSystemDefs().size());
        final SystemDef stub = fragment.getSystemDefs().get(0);
        assertEquals("Net-SNMP", stub.getName());
        assertEquals(List.of("ucd-memory", "ucd-memory-X"), stub.getCollect().getIncludeGroups());
    }

    /**
     * The DB delta and the XML migration must produce identical rows: the
     * bundled fragment has to stay in sync with the group shipped in
     * etc/datacollection/netsnmp.xml.
     */
    @Test
    public void bundledGroupMatchesShippedNetsnmpXml() throws SQLException {
        final File netsnmpXml = new File(NETSNMP_XML);
        assertTrue("expected shipped netsnmp.xml at " + netsnmpXml.getAbsolutePath(), netsnmpXml.exists());

        final DatacollectionGroup shippedDcGroup =
                new SnmpDataCollectionMigration().unmarshal(DatacollectionGroup.class, netsnmpXml);
        final Group shipped = shippedDcGroup.getGroups().stream()
                .filter(g -> "ucd-memory-X".equals(g.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("ucd-memory-X group missing from shipped netsnmp.xml"));

        final DatacollectionGroup fragment =
                new SnmpDataCollectionDefaultsUpdate().loadFragment(UCD_MEMORY_X_UPDATE.resourcePath);
        final Group bundled = fragment.getGroups().get(0);

        // Compare via the same mapping the SQL inserts use.
        assertEquals(DataCollectionGroupMapper.mapMibObjects(shipped),
                DataCollectionGroupMapper.mapMibObjects(bundled));
        assertEquals(shipped.getIfType(), bundled.getIfType());
        assertEquals(DataCollectionGroupMapper.mapIncludeGroups(shipped),
                DataCollectionGroupMapper.mapIncludeGroups(bundled));
        assertEquals(DataCollectionGroupMapper.mapMibObjProperties(shipped),
                DataCollectionGroupMapper.mapMibObjProperties(bundled));
    }

    /**
     * The shipped systemDef must reference the new group in the same relative
     * position the fragment stub declares, so the XML-migration path matches
     * what the DB merge produces.
     */
    @Test
    public void shippedSystemDefReferencesNewGroup() throws SQLException {
        final DatacollectionGroup shippedDcGroup =
                new SnmpDataCollectionMigration().unmarshal(DatacollectionGroup.class, new File(NETSNMP_XML));
        final SystemDef systemDef = shippedDcGroup.getSystemDefs().stream()
                .filter(sd -> "Net-SNMP".equals(sd.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Net-SNMP systemDef missing from shipped netsnmp.xml"));

        final List<String> includeGroups = systemDef.getCollect().getIncludeGroups();
        final int memIdx = includeGroups.indexOf("ucd-memory");
        final int memXIdx = includeGroups.indexOf("ucd-memory-X");
        assertTrue("ucd-memory should be referenced", memIdx >= 0);
        assertEquals("ucd-memory-X should directly follow ucd-memory", memIdx + 1, memXIdx);
    }

    @Test
    public void parseStringArrayHandlesEdgeCases() throws SQLException {
        assertTrue(SnmpDataCollectionDefaultsUpdate.parseStringArray(null).isEmpty());
        assertTrue(SnmpDataCollectionDefaultsUpdate.parseStringArray("").isEmpty());
        assertTrue(SnmpDataCollectionDefaultsUpdate.parseStringArray("  ").isEmpty());

        final List<String> parsed = SnmpDataCollectionDefaultsUpdate.parseStringArray("[\"a\",\"b\"]");
        assertNotNull(parsed);
        assertEquals(List.of("a", "b"), parsed);
        // returned list must be mutable — the systemDef merge inserts into it
        parsed.add("c");
        assertEquals(3, parsed.size());
    }

    @Test
    public void archiveIsNoopWhenNothingApplied() throws Exception {
        // Must not create directories or fail when no update was applied.
        final String previousHome = System.getProperty("opennms.home");
        final File tempHome = java.nio.file.Files.createTempDirectory("defaults-update-test").toFile();
        System.setProperty("opennms.home", tempHome.getAbsolutePath());
        try {
            final SnmpDataCollectionDefaultsUpdate update = new SnmpDataCollectionDefaultsUpdate();
            update.archiveAppliedFragments();
            assertFalse(new File(tempHome,
                    "etc_archive/" + SnmpDataCollectionDefaultsUpdate.ARCHIVE_UPDATES_DIR).exists());
        } finally {
            if (previousHome == null) {
                System.clearProperty("opennms.home");
            } else {
                System.setProperty("opennms.home", previousHome);
            }
        }
    }
}
