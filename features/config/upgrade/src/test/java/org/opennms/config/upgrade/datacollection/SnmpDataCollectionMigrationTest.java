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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.opennms.netmgt.config.datacollection.Collect;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.IncludeCollection;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.Rrd;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.config.datacollection.SystemDef;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for the SNMP data collection migration components:
 * - DataCollectionGroupMapper (JSON serialization)
 * - XML parsing of datacollection-config.xml and datacollection group files
 */
public class SnmpDataCollectionMigrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // --- DataCollectionGroupMapper tests ---

    @Test
    public void testMapRras() {
        final Rrd rrd = new Rrd();
        rrd.setStep(300);
        rrd.addRra("RRA:AVERAGE:0.5:1:2016");
        rrd.addRra("RRA:MAX:0.5:288:366");

        final String json = DataCollectionGroupMapper.mapRras(rrd);
        assertNotNull(json);
        assertTrue(json.contains("RRA:AVERAGE:0.5:1:2016"));
        assertTrue(json.contains("RRA:MAX:0.5:288:366"));
    }

    @Test
    public void testMapRrasNull() {
        assertNull(DataCollectionGroupMapper.mapRras(null));
    }

    @Test
    public void testMapMibObjects() throws Exception {
        final Group group = new Group();
        group.setName("test-group");
        group.setIfType("all");
        group.addMibObj(new MibObj(".1.3.6.1.2.1.1.3", "0", "sysUpTime", "timeticks"));
        group.addMibObj(new MibObj(".1.3.6.1.2.1.2.2.1.10", "ifIndex", "ifInOctets", "counter"));

        final String json = DataCollectionGroupMapper.mapMibObjects(group);
        assertNotNull(json);

        // Verify it's valid JSON and contains expected OIDs
        final List<?> parsed = OBJECT_MAPPER.readValue(json, List.class);
        assertEquals(2, parsed.size());
        assertTrue(json.contains(".1.3.6.1.2.1.1.3"));
        assertTrue(json.contains(".1.3.6.1.2.1.2.2.1.10"));
    }

    @Test
    public void testMapMibObjectsEmpty() {
        final Group group = new Group();
        group.setName("empty");
        group.setIfType("all");

        final String json = DataCollectionGroupMapper.mapMibObjects(group);
        assertEquals("[]", json);
    }

    @Test
    public void testMapIncludeGroups() {
        final Group group = new Group();
        group.setName("test");
        group.setIfType("all");
        group.addIncludeGroup("mib2-interfaces");
        group.addIncludeGroup("mib2-host-resources");

        final String json = DataCollectionGroupMapper.mapIncludeGroups(group);
        assertNotNull(json);
        assertTrue(json.contains("mib2-interfaces"));
        assertTrue(json.contains("mib2-host-resources"));
    }

    @Test
    public void testMapIncludeGroupsEmpty() {
        final Group group = new Group();
        group.setName("test");
        group.setIfType("all");

        assertNull(DataCollectionGroupMapper.mapIncludeGroups(group));
    }

    @Test
    public void testMapSystemDefMibGroupNames() {
        final SystemDef sd = new SystemDef();
        sd.setName("test-systemdef");
        sd.setSysoidMask(".1.3.6.1.4.1.8072.");
        final Collect collect = new Collect();
        collect.addIncludeGroup("mib2-interfaces");
        collect.addIncludeGroup("mib2-host-resources");
        sd.setCollect(collect);

        final String json = DataCollectionGroupMapper.mapSystemDefMibGroupNames(sd);
        assertNotNull(json);
        assertTrue(json.contains("mib2-interfaces"));
        assertTrue(json.contains("mib2-host-resources"));
    }

    @Test
    public void testMapSystemDefNoCollect() {
        final SystemDef sd = new SystemDef();
        sd.setName("test-no-collect");
        sd.setSysoid(".1.3.6.1.4.1.9");

        final String json = DataCollectionGroupMapper.mapSystemDefMibGroupNames(sd);
        assertEquals("[]", json);
    }

    @Test
    public void testMapStorageStrategy() {
        final ResourceType rt = new ResourceType();
        rt.setName("test");
        rt.setLabel("Test");
        final StorageStrategy ss = new StorageStrategy("org.opennms.netmgt.collection.support.IndexStorageStrategy");
        rt.setStorageStrategy(ss);

        assertEquals("org.opennms.netmgt.collection.support.IndexStorageStrategy",
                DataCollectionGroupMapper.mapStorageStrategy(rt));
    }

    @Test
    public void testMapStorageStrategyNull() {
        final ResourceType rt = new ResourceType();
        rt.setName("test");
        rt.setLabel("Test");

        assertNull(DataCollectionGroupMapper.mapStorageStrategy(rt));
    }

    @Test
    public void testMapPersistenceSelectorStrategy() {
        final ResourceType rt = new ResourceType();
        rt.setName("test");
        rt.setLabel("Test");
        final PersistenceSelectorStrategy ps = new PersistenceSelectorStrategy(
                "org.opennms.netmgt.collection.support.PersistAllSelectorStrategy");
        rt.setPersistenceSelectorStrategy(ps);

        assertEquals("org.opennms.netmgt.collection.support.PersistAllSelectorStrategy",
                DataCollectionGroupMapper.mapPersistenceSelectorStrategy(rt));
    }

    @Test
    public void testMapSourceNames() throws Exception {
        final List<String> names = Arrays.asList("MIB2", "Cisco", "Net-SNMP");
        final String json = DataCollectionGroupMapper.mapSourceNames(names);
        assertNotNull(json);

        final List<String> parsed = OBJECT_MAPPER.readValue(json, new TypeReference<List<String>>() {});
        assertEquals(3, parsed.size());
        assertEquals("MIB2", parsed.get(0));
        assertEquals("Cisco", parsed.get(1));
        assertEquals("Net-SNMP", parsed.get(2));
    }

    @Test
    public void testMapSourceNamesNull() {
        assertNull(DataCollectionGroupMapper.mapSourceNames(null));
        assertNull(DataCollectionGroupMapper.mapSourceNames(new ArrayList<>()));
    }

    @Test
    public void testMapRrdStep() {
        final SnmpCollection coll = new SnmpCollection();
        coll.setName("test");
        coll.setSnmpStorageFlag("select");
        final Rrd rrd = new Rrd();
        rrd.setStep(180);
        coll.setRrd(rrd);

        assertEquals(180, DataCollectionGroupMapper.mapRrdStep(coll));
    }

    @Test
    public void testToJsonNull() {
        assertNull(DataCollectionGroupMapper.toJson(null));
    }

    // --- XML Parsing tests ---

    @Test
    public void testParseDatacollectionConfig() throws Exception {
        final File configFile = getTestResource("test-datacollection/datacollection-config.xml");
        final DatacollectionConfig config = unmarshal(DatacollectionConfig.class, configFile);

        assertNotNull(config);
        assertEquals(2, config.getSnmpCollections().size());

        final SnmpCollection defaultColl = config.getSnmpCollection("default");
        assertNotNull(defaultColl);
        assertEquals("select", defaultColl.getSnmpStorageFlag());
        assertEquals(Integer.valueOf(300), defaultColl.getRrd().getStep());
        assertEquals(3, defaultColl.getRrd().getRras().size());
        assertEquals(2, defaultColl.getIncludeCollections().size());

        final SnmpCollection customColl = config.getSnmpCollection("custom");
        assertNotNull(customColl);
        assertEquals("all", customColl.getSnmpStorageFlag());
        assertEquals(Integer.valueOf(20), customColl.getMaxVarsPerPdu());
        assertEquals(Integer.valueOf(180), customColl.getRrd().getStep());
        assertEquals(1, customColl.getIncludeCollections().size());
    }

    @Test
    public void testParseDatacollectionGroup() throws Exception {
        final File groupFile = getTestResource("test-datacollection/datacollection/testmib.xml");
        final DatacollectionGroup group = unmarshal(DatacollectionGroup.class, groupFile);

        assertNotNull(group);
        assertEquals("TestMIB", group.getName());

        // Resource types
        assertEquals(1, group.getResourceTypes().size());
        final ResourceType rt = group.getResourceTypes().get(0);
        assertEquals("hrStorageIndex", rt.getName());
        assertEquals("Storage (MIB-2 Host Resources)", rt.getLabel());
        assertEquals("${hrStorageDescr}", rt.getResourceLabel());
        assertNotNull(rt.getPersistenceSelectorStrategy());
        assertNotNull(rt.getStorageStrategy());

        // Groups
        assertEquals(2, group.getGroups().size());
        final Group ifGroup = group.getGroups().get(0);
        assertEquals("mib2-interfaces", ifGroup.getName());
        assertEquals("all", ifGroup.getIfType());
        assertEquals(2, ifGroup.getMibObjs().size());

        final Group hrGroup = group.getGroups().get(1);
        assertEquals("mib2-host-resources", hrGroup.getName());
        assertEquals(3, hrGroup.getMibObjs().size());

        // System defs
        assertEquals(1, group.getSystemDefs().size());
        final SystemDef sd = group.getSystemDefs().get(0);
        assertEquals("Net-SNMP", sd.getName());
        assertEquals(".1.3.6.1.4.1.8072.", sd.getSysoidMask());
        assertNull(sd.getSysoid());
        assertNotNull(sd.getCollect());
        assertEquals(2, sd.getCollect().getIncludeGroups().size());
    }

    @Test
    public void testParseVendorGroup() throws Exception {
        final File groupFile = getTestResource("test-datacollection/datacollection/testvendor.xml");
        final DatacollectionGroup group = unmarshal(DatacollectionGroup.class, groupFile);

        assertNotNull(group);
        assertEquals("TestVendor", group.getName());

        // Resource types
        assertEquals(1, group.getResourceTypes().size());

        // Groups
        assertEquals(1, group.getGroups().size());
        assertEquals("vendor-memory", group.getGroups().get(0).getName());

        // System defs with sysoid (not sysoidMask)
        assertEquals(1, group.getSystemDefs().size());
        final SystemDef sd = group.getSystemDefs().get(0);
        assertEquals("Cisco Routers", sd.getName());
        assertEquals(".1.3.6.1.4.1.9", sd.getSysoid());
        assertNull(sd.getSysoidMask());
        assertEquals(1, sd.getCollect().getIncludeGroups().size());
    }

    @Test
    public void testJsonRoundTripMibObjects() throws Exception {
        final Group group = new Group();
        group.setName("test");
        group.setIfType("all");
        group.addMibObj(new MibObj(".1.3.6.1.2.1.1.3", "0", "sysUpTime", "timeticks"));

        final String json = DataCollectionGroupMapper.mapMibObjects(group);
        final List<?> parsed = OBJECT_MAPPER.readValue(json, List.class);
        assertEquals(1, parsed.size());
    }

    @Test
    public void testEndToEndMapperWithParsedXml() throws Exception {
        final File groupFile = getTestResource("test-datacollection/datacollection/testmib.xml");
        final DatacollectionGroup group = unmarshal(DatacollectionGroup.class, groupFile);

        // Verify mapper works with real parsed JAXB objects
        for (final Group g : group.getGroups()) {
            final String mibObjJson = DataCollectionGroupMapper.mapMibObjects(g);
            assertNotNull("MIB objects JSON should not be null for group " + g.getName(), mibObjJson);
            assertTrue("MIB objects JSON should not be empty", mibObjJson.length() > 2);
        }

        for (final SystemDef sd : group.getSystemDefs()) {
            final String groupNamesJson = DataCollectionGroupMapper.mapSystemDefMibGroupNames(sd);
            assertNotNull("Group names JSON should not be null for systemDef " + sd.getName(), groupNamesJson);
        }

        for (final ResourceType rt : group.getResourceTypes()) {
            assertNotNull("Storage strategy should be mapped",
                    DataCollectionGroupMapper.mapStorageStrategy(rt));
            assertNotNull("Persistence strategy should be mapped",
                    DataCollectionGroupMapper.mapPersistenceSelectorStrategy(rt));
        }
    }

    @Test
    public void testEndToEndProfileMapping() throws Exception {
        final File configFile = getTestResource("test-datacollection/datacollection-config.xml");
        final DatacollectionConfig config = unmarshal(DatacollectionConfig.class, configFile);

        for (final SnmpCollection coll : config.getSnmpCollections()) {
            final String rrasJson = DataCollectionGroupMapper.mapRras(coll.getRrd());
            assertNotNull(rrasJson);

            final List<String> sourceNames = new ArrayList<>();
            for (var include : coll.getIncludeCollections()) {
                if (include.getDataCollectionGroup() != null) {
                    sourceNames.add(include.getDataCollectionGroup());
                }
            }
            final String sourceNamesJson = DataCollectionGroupMapper.mapSourceNames(sourceNames);
            assertNotNull(sourceNamesJson);
        }
    }

    // --- Include-collection resolution tests ---

    @Test
    public void testResolveIncludesPlainInclude() {
        // <include-collection dataCollectionGroup="MIB2"/> with no excludes
        // stays as a source-name reference; nothing pulled inline.
        final Map<String, DatacollectionGroup> groups = new LinkedHashMap<>();
        groups.put("MIB2", buildGroup("MIB2",
                List.of(systemDef("Net-SNMP", List.of("mib2-interfaces"))),
                List.of(mibGroup("mib2-interfaces")),
                List.of(resourceType("hrStorageIndex"))));

        final SnmpCollection coll = collectionWithInclude("default",
                plainInclude("MIB2"));

        final SnmpDataCollectionMigration.ResolvedIncludes r =
                new SnmpDataCollectionMigration().resolveIncludes(coll, groups);

        assertEquals(List.of("MIB2"), r.sourceNames);
        assertTrue(r.inlineGroups.isEmpty());
        assertTrue(r.inlineSystemDefs.isEmpty());
        assertTrue(r.inlineResourceTypes.isEmpty());
    }

    @Test
    public void testResolveIncludesExcludeFilterPreservesResourceTypes() {
        // <include-collection dataCollectionGroup="MIB2"><exclude-filter>Windows.*</exclude-filter>/> —
        // drops "MIB2" from source_names, inlines surviving systemDefs + their referenced
        // MIB groups, AND inlines ALL resource types from MIB2 (matches legacy
        // DefaultDataCollectionConfigDao behavior).
        final Map<String, DatacollectionGroup> groups = new LinkedHashMap<>();
        groups.put("MIB2", buildGroup("MIB2",
                List.of(
                        systemDef("Net-SNMP", List.of("mib2-interfaces")),
                        systemDef("Windows-Host", List.of("mib2-windows"))
                ),
                List.of(
                        mibGroup("mib2-interfaces"),
                        mibGroup("mib2-windows")
                ),
                List.of(resourceType("hrStorageIndex"))));

        final IncludeCollection inc = plainInclude("MIB2");
        inc.setExcludeFilters(List.of("Windows.*"));

        final SnmpCollection coll = collectionWithInclude("default", inc);

        final SnmpDataCollectionMigration.ResolvedIncludes r =
                new SnmpDataCollectionMigration().resolveIncludes(coll, groups);

        // Source dropped — replaced by inline content
        assertTrue("MIB2 should not appear in source_names when excludes apply",
                r.sourceNames.isEmpty());
        // Only Net-SNMP survived; Windows-Host excluded
        assertEquals(1, r.inlineSystemDefs.size());
        assertEquals("Net-SNMP", r.inlineSystemDefs.get(0).getName());
        // Only the group referenced by the surviving systemDef is pulled in
        assertEquals(1, r.inlineGroups.size());
        assertEquals("mib2-interfaces", r.inlineGroups.get(0).getName());
        // All resource types are preserved regardless of systemDef excludes
        assertEquals(1, r.inlineResourceTypes.size());
        assertEquals("hrStorageIndex", r.inlineResourceTypes.get(0).getName());
    }

    @Test
    public void testResolveIncludesSystemDefRef() {
        // <include-collection systemDef="Net-SNMP"/> — resolves that systemDef plus its
        // referenced MIB groups into inline; no resource types pulled from owning group
        // (matches legacy addSystemDef behavior).
        final Map<String, DatacollectionGroup> groups = new LinkedHashMap<>();
        groups.put("MIB2", buildGroup("MIB2",
                List.of(systemDef("Net-SNMP", List.of("mib2-interfaces"))),
                List.of(mibGroup("mib2-interfaces")),
                List.of(resourceType("hrStorageIndex"))));

        final IncludeCollection inc = new IncludeCollection();
        inc.setSystemDef("Net-SNMP");

        final SnmpCollection coll = collectionWithInclude("default", inc);

        final SnmpDataCollectionMigration.ResolvedIncludes r =
                new SnmpDataCollectionMigration().resolveIncludes(coll, groups);

        assertTrue(r.sourceNames.isEmpty());
        assertEquals(1, r.inlineSystemDefs.size());
        assertEquals("Net-SNMP", r.inlineSystemDefs.get(0).getName());
        assertEquals(1, r.inlineGroups.size());
        assertEquals("mib2-interfaces", r.inlineGroups.get(0).getName());
        // systemDef= include does NOT pull resource types (legacy parser parity)
        assertTrue("systemDef includes should not pull resource types from owning group",
                r.inlineResourceTypes.isEmpty());
    }

    // --- Helper methods ---

    private SnmpCollection collectionWithInclude(final String name, final IncludeCollection include) {
        final SnmpCollection c = new SnmpCollection();
        c.setName(name);
        c.setSnmpStorageFlag("select");
        final Rrd rrd = new Rrd();
        rrd.setStep(300);
        c.setRrd(rrd);
        c.addIncludeCollection(include);
        return c;
    }

    private IncludeCollection plainInclude(final String groupName) {
        final IncludeCollection inc = new IncludeCollection();
        inc.setDataCollectionGroup(groupName);
        return inc;
    }

    private DatacollectionGroup buildGroup(final String name,
                                           final List<SystemDef> sds,
                                           final List<Group> mibs,
                                           final List<ResourceType> rts) {
        final DatacollectionGroup g = new DatacollectionGroup();
        g.setName(name);
        sds.forEach(g::addSystemDef);
        mibs.forEach(g::addGroup);
        rts.forEach(g::addResourceType);
        return g;
    }

    private SystemDef systemDef(final String name, final List<String> includeGroups) {
        final SystemDef sd = new SystemDef();
        sd.setName(name);
        sd.setSysoidMask(".1.3.6.1.4.1.");
        final Collect collect = new Collect();
        includeGroups.forEach(collect::addIncludeGroup);
        sd.setCollect(collect);
        return sd;
    }

    private Group mibGroup(final String name) {
        final Group g = new Group();
        g.setName(name);
        g.setIfType("all");
        return g;
    }

    private ResourceType resourceType(final String name) {
        final ResourceType rt = new ResourceType();
        rt.setName(name);
        rt.setLabel(name);
        return rt;
    }

    // --- Original helper methods ---

    private File getTestResource(final String path) {
        final URL url = getClass().getClassLoader().getResource(path);
        assertNotNull("Test resource not found: " + path, url);
        return new File(url.getFile());
    }

    private <T> T unmarshal(final Class<T> clazz, final File file) throws Exception {
        final JAXBContext ctx = JAXBContext.newInstance(clazz);
        final Unmarshaller unmarshaller = ctx.createUnmarshaller();
        @SuppressWarnings("unchecked")
        final T result = (T) unmarshaller.unmarshal(file);
        return result;
    }
}
