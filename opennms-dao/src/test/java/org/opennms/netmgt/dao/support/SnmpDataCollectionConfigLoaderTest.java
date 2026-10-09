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
package org.opennms.netmgt.dao.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.api.DatacollectionJsonHelper;
import org.opennms.netmgt.config.datacollection.Collect;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;
import org.opennms.netmgt.dao.api.SnmpCollectionMibGroupDao;
import org.opennms.netmgt.dao.api.SnmpCollectionProfileDao;
import org.opennms.netmgt.dao.api.SnmpCollectionResourceTypeDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSourceDao;
import org.opennms.netmgt.model.SnmpCollectionMibGroup;
import org.opennms.netmgt.model.SnmpCollectionProfile;
import org.opennms.netmgt.model.SnmpCollectionResourceType;
import org.opennms.netmgt.model.SnmpCollectionSource;

import java.util.List;
import java.util.Map;

/**
 * Tests for how {@link SnmpDataCollectionConfigLoaderImpl} merges sources into
 * a profile.
 *
 * <p>Two sources that contribute the same group or systemDef name to one
 * profile merge into one occurrence. This mirrors the contains-check in
 * {@code DataCollectionConfigParser.addSystemDef}.
 *
 * <p>A group reference that no attached source defines resolves from all
 * enabled sources. This mirrors {@code DataCollectionConfigParser.getMibObjectGroup}.
 */
public class SnmpDataCollectionConfigLoaderTest {

    @Test
    public void reloadDedupsGroupsAndSystemDefsAcrossSourcesInSameProfile() {
        final SnmpCollectionSource srcA = source(1, "A");
        final SnmpCollectionSource srcB = source(2, "B");

        final DatacollectionGroup dcA = new DatacollectionGroup();
        dcA.setName("A");
        dcA.addGroup(group("shared-group"));
        dcA.addSystemDef(systemDef("shared-sd"));

        final DatacollectionGroup dcB = new DatacollectionGroup();
        dcB.setName("B");
        dcB.addGroup(group("shared-group"));   // same name as in A — must dedup
        dcB.addSystemDef(systemDef("shared-sd")); // same name — must dedup
        dcB.addSystemDef(systemDef("unique-sd")); // unique to B — must remain

        final SnmpCollectionProfileDao profileDao = mock(SnmpCollectionProfileDao.class);
        final SnmpCollectionSourceDao sourceDao = mock(SnmpCollectionSourceDao.class);
        final DataCollectionConfigDao configDao = mock(DataCollectionConfigDao.class);

        final SnmpCollectionProfile profile = new SnmpCollectionProfile();
        profile.setName("default");
        profile.setStorageFlag("select");
        profile.setRrdStep(300);
        profile.setSourceNames("[\"A\",\"B\"]");
        profile.setRrdRras("[]");

        when(profileDao.findAllEnabled()).thenReturn(List.of(profile));
        when(sourceDao.findByName("A")).thenReturn(srcA);
        when(sourceDao.findByName("B")).thenReturn(srcB);
        when(configDao.getRrdPath()).thenReturn("/tmp/rrd/");

        // Override buildDataCollectionGroupFromDb so we don't need to mock the
        // three child DAOs (resourceType/mibGroup/systemDef) — the dedup logic
        // we're testing is in the merge step, not the materialization step.
        final SnmpDataCollectionConfigLoaderImpl loader = new SnmpDataCollectionConfigLoaderImpl() {
            @Override
            public DatacollectionGroup buildDataCollectionGroupFromDb(final SnmpCollectionSource s) {
                return s.getId() == 1 ? dcA : dcB;
            }
        };
        loader.setSnmpCollectionProfileDao(profileDao);
        loader.setSnmpCollectionSourceDao(sourceDao);
        loader.setDataCollectionConfigDao(configDao);

        loader.reloadDataCollectionConfigFromDb();

        final ArgumentCaptor<DatacollectionConfig> captor = ArgumentCaptor.forClass(DatacollectionConfig.class);
        verify(configDao).loadFromDatabase(captor.capture(), any(Map.class), any(List.class));

        final SnmpCollection coll = captor.getValue().getSnmpCollection("default");
        assertEquals("shared-group should appear once after dedup",
                1, coll.getGroups().getGroups().size());
        assertEquals("shared-group", coll.getGroups().getGroups().get(0).getName());
        assertEquals("shared-sd should dedup; unique-sd should remain",
                2, coll.getSystems().getSystemDefs().size());
    }

    @Test
    public void resolvesSystemDefGroupsFromSourcesThatAreNotAttached() {
        // Profile attaches only "Cisco". Its systemDef references a group of
        // "Routers", which in turn includes a group of "MIB2".
        final DatacollectionGroup cisco = new DatacollectionGroup();
        cisco.setName("Cisco");
        cisco.addGroup(group("cisco-router"));
        final SystemDef sd = systemDef("Cisco Routers");
        sd.getCollect().addIncludeGroup("cisco-router");
        sd.getCollect().addIncludeGroup("rfc1315-frame-relay");
        cisco.addSystemDef(sd);

        final SnmpCollectionSource routers = source(2, "Routers");
        final SnmpCollectionSource mib2 = source(3, "MIB2");
        final SnmpCollectionMibGroup frameRelay = mibGroupEntity(routers, "rfc1315-frame-relay",
                "[\"mib2-shared\"]", "frCircuitIfIndex");
        final SnmpCollectionMibGroup shared = mibGroupEntity(mib2, "mib2-shared", null, "ifIndex");

        final Fixture f = new Fixture(cisco);
        when(f.mibGroupDao.findAllEnabledInEnabledSources()).thenReturn(List.of(frameRelay, shared));
        final SnmpCollectionResourceType frType = new SnmpCollectionResourceType();
        frType.setName("frCircuitIfIndex");
        frType.setLabel("Frame-Relay");
        frType.setCollectionSource(routers);
        when(f.resourceTypeDao.findAllEnabledInEnabledSources()).thenReturn(List.of(frType));

        final SnmpDataCollectionConfigLoaderImpl.MaterializedConfig m = f.loader.materializeFromDb();

        final SnmpCollection coll = m.config.getSnmpCollection("default");
        assertEquals(List.of("cisco-router", "rfc1315-frame-relay", "mib2-shared"),
                coll.getGroups().getGroups().stream().map(Group::getName).toList());
        assertTrue("resource type of a group from another source must be available",
                m.allResourceTypes.containsKey("frCircuitIfIndex"));
    }

    @Test
    public void attachedSourcesDoNotQueryOtherSources() {
        final DatacollectionGroup cisco = new DatacollectionGroup();
        cisco.setName("Cisco");
        cisco.addGroup(group("cisco-router"));
        final SystemDef sd = systemDef("Cisco Routers");
        sd.getCollect().addIncludeGroup("cisco-router");
        cisco.addSystemDef(sd);

        final Fixture f = new Fixture(cisco);
        f.loader.materializeFromDb();

        verify(f.mibGroupDao, never()).findAllEnabledInEnabledSources();
        verify(f.resourceTypeDao, never()).findAllEnabledInEnabledSources();
    }

    @Test
    public void unresolvedGroupIsSkipped() {
        final DatacollectionGroup cisco = new DatacollectionGroup();
        cisco.setName("Cisco");
        final SystemDef sd = systemDef("Cisco Routers");
        sd.getCollect().addIncludeGroup("does-not-exist");
        cisco.addSystemDef(sd);

        final Fixture f = new Fixture(cisco);
        when(f.mibGroupDao.findAllEnabledInEnabledSources()).thenReturn(List.of());
        when(f.mibGroupDao.findAllByName("does-not-exist")).thenReturn(List.of());

        final SnmpDataCollectionConfigLoaderImpl.MaterializedConfig m = f.loader.materializeFromDb();

        assertTrue(m.config.getSnmpCollection("default").getGroups().getGroups().isEmpty());
        verify(f.mibGroupDao).findAllByName("does-not-exist");
    }

    @Test
    public void groupDisabledInAttachedSourceIsNotTakenFromOtherSource() {
        // "Cisco" (id 1) is attached and disables "shared". "Other" defines an enabled "shared".
        final DatacollectionGroup cisco = new DatacollectionGroup();
        cisco.setName("Cisco");
        final SystemDef sd = systemDef("Cisco Routers");
        sd.getCollect().addIncludeGroup("shared");
        cisco.addSystemDef(sd);

        final SnmpCollectionMibGroup disabled = mibGroupEntity(source(1, "Cisco"), "shared", null, "ifIndex");
        disabled.setEnabled(false);
        final SnmpCollectionMibGroup other = mibGroupEntity(source(2, "Other"), "shared", null, "ifIndex");

        final Fixture f = new Fixture(cisco);
        when(f.mibGroupDao.findAllByName("shared")).thenReturn(List.of(disabled, other));
        when(f.mibGroupDao.findAllEnabledInEnabledSources()).thenReturn(List.of(other));

        final SnmpDataCollectionConfigLoaderImpl.MaterializedConfig m = f.loader.materializeFromDb();

        assertTrue(m.config.getSnmpCollection("default").getGroups().getGroups().isEmpty());
        verify(f.mibGroupDao, never()).findAllEnabledInEnabledSources();
    }

    @Test
    public void resourceTypeDisabledInAttachedSourceIsNotTakenFromOtherSource() {
        // "Cisco" (id 1) is attached and disables "ciscoType". "Other" defines an enabled "ciscoType".
        final DatacollectionGroup cisco = new DatacollectionGroup();
        cisco.setName("Cisco");
        final Group g = group("cisco-group");
        final MibObj obj = new MibObj();
        obj.setOid(".1.3.6.1.4.1.9.1");
        obj.setInstance("ciscoType");
        obj.setAlias("ciscoObj");
        obj.setType("gauge");
        g.addMibObj(obj);
        cisco.addGroup(g);

        final SnmpCollectionResourceType disabled = resourceTypeEntity(source(1, "Cisco"), "ciscoType");
        disabled.setEnabled(false);
        final SnmpCollectionResourceType other = resourceTypeEntity(source(2, "Other"), "ciscoType");

        final Fixture f = new Fixture(cisco);
        when(f.resourceTypeDao.findAllByName("ciscoType")).thenReturn(List.of(disabled, other));
        when(f.resourceTypeDao.findAllEnabledInEnabledSources()).thenReturn(List.of(other));

        final SnmpDataCollectionConfigLoaderImpl.MaterializedConfig m = f.loader.materializeFromDb();

        assertFalse(m.allResourceTypes.containsKey("ciscoType"));
        verify(f.resourceTypeDao, never()).findAllEnabledInEnabledSources();
    }

    @Test
    public void resourceTypeDisabledInOtherProfileSourceIsStillTakenFromOtherSource() {
        // Profile "a" attaches "A" (id 1), whose group uses "fooType". No attached source of "a" defines it.
        // Profile "b" attaches "B" (id 2), which disables "fooType". "Other" (id 3) defines an enabled "fooType".
        final DatacollectionGroup dcA = new DatacollectionGroup();
        dcA.setName("A");
        final Group g = group("a-group");
        final MibObj obj = new MibObj();
        obj.setOid(".1.3.6.1.4.1.99.1");
        obj.setInstance("fooType");
        obj.setAlias("fooObj");
        obj.setType("gauge");
        g.addMibObj(obj);
        dcA.addGroup(g);
        final DatacollectionGroup dcB = new DatacollectionGroup();
        dcB.setName("B");

        final SnmpCollectionResourceType disabled = resourceTypeEntity(source(2, "B"), "fooType");
        disabled.setEnabled(false);
        final SnmpCollectionResourceType other = resourceTypeEntity(source(3, "Other"), "fooType");

        final SnmpCollectionProfileDao profileDao = mock(SnmpCollectionProfileDao.class);
        final SnmpCollectionSourceDao sourceDao = mock(SnmpCollectionSourceDao.class);
        final SnmpCollectionMibGroupDao mibGroupDao = mock(SnmpCollectionMibGroupDao.class);
        final SnmpCollectionResourceTypeDao resourceTypeDao = mock(SnmpCollectionResourceTypeDao.class);
        when(profileDao.findAllEnabled()).thenReturn(List.of(profile("a", "A"), profile("b", "B")));
        when(sourceDao.findByName("A")).thenReturn(source(1, "A"));
        when(sourceDao.findByName("B")).thenReturn(source(2, "B"));
        when(resourceTypeDao.findAllByName("fooType")).thenReturn(List.of(disabled, other));
        when(resourceTypeDao.findAllEnabledInEnabledSources()).thenReturn(List.of(other));

        final SnmpDataCollectionConfigLoaderImpl loader = new SnmpDataCollectionConfigLoaderImpl() {
            @Override
            public DatacollectionGroup buildDataCollectionGroupFromDb(final SnmpCollectionSource s) {
                return s.getId() == 1 ? dcA : dcB;
            }
        };
        loader.setSnmpCollectionProfileDao(profileDao);
        loader.setSnmpCollectionSourceDao(sourceDao);
        final DataCollectionConfigDao configDao = mock(DataCollectionConfigDao.class);
        when(configDao.getRrdPath()).thenReturn("/tmp/rrd/");
        loader.setDataCollectionConfigDao(configDao);
        loader.setSnmpCollectionMibGroupDao(mibGroupDao);
        loader.setSnmpCollectionResourceTypeDao(resourceTypeDao);

        final SnmpDataCollectionConfigLoaderImpl.MaterializedConfig m = loader.materializeFromDb();

        assertTrue("profile b must not block the resource type that profile a needs",
                m.allResourceTypes.containsKey("fooType"));
    }

    /** One profile named "default" that attaches only the given source. */
    private final class Fixture {
        final SnmpCollectionMibGroupDao mibGroupDao = mock(SnmpCollectionMibGroupDao.class);
        final SnmpCollectionResourceTypeDao resourceTypeDao = mock(SnmpCollectionResourceTypeDao.class);
        final SnmpDataCollectionConfigLoaderImpl loader;

        Fixture(final DatacollectionGroup attached) {
            final SnmpCollectionProfileDao profileDao = mock(SnmpCollectionProfileDao.class);
            final SnmpCollectionSourceDao sourceDao = mock(SnmpCollectionSourceDao.class);
            final DataCollectionConfigDao configDao = mock(DataCollectionConfigDao.class);

            final SnmpCollectionProfile profile = new SnmpCollectionProfile();
            profile.setName("default");
            profile.setStorageFlag("select");
            profile.setRrdStep(300);
            profile.setSourceNames("[\"" + attached.getName() + "\"]");
            profile.setRrdRras("[]");
            when(profileDao.findAllEnabled()).thenReturn(List.of(profile));
            when(sourceDao.findByName(attached.getName())).thenReturn(source(1, attached.getName()));
            when(configDao.getRrdPath()).thenReturn("/tmp/rrd/");

            loader = new SnmpDataCollectionConfigLoaderImpl() {
                @Override
                public DatacollectionGroup buildDataCollectionGroupFromDb(final SnmpCollectionSource s) {
                    return attached;
                }
            };
            loader.setSnmpCollectionProfileDao(profileDao);
            loader.setSnmpCollectionSourceDao(sourceDao);
            loader.setDataCollectionConfigDao(configDao);
            loader.setSnmpCollectionMibGroupDao(mibGroupDao);
            loader.setSnmpCollectionResourceTypeDao(resourceTypeDao);
        }
    }

    private SnmpCollectionMibGroup mibGroupEntity(final SnmpCollectionSource source, final String name,
                                                  final String includeGroupsJson, final String instance) {
        final MibObj obj = new MibObj();
        obj.setOid(".1.3.6.1.2.1.10.32.2.1.1");
        obj.setInstance(instance);
        obj.setAlias(name.substring(0, Math.min(name.length(), 10)));
        obj.setType("counter");
        final SnmpCollectionMibGroup e = new SnmpCollectionMibGroup();
        e.setName(name);
        e.setIfType("all");
        e.setEnabled(true);
        e.setCollectionSource(source);
        e.setMibGroupNames(includeGroupsJson);
        e.setMibObjects(DatacollectionJsonHelper.toJson(List.of(obj)));
        return e;
    }

    private SnmpCollectionProfile profile(final String name, final String sourceName) {
        final SnmpCollectionProfile p = new SnmpCollectionProfile();
        p.setName(name);
        p.setStorageFlag("select");
        p.setRrdStep(300);
        p.setSourceNames("[\"" + sourceName + "\"]");
        p.setRrdRras("[]");
        return p;
    }

    private SnmpCollectionResourceType resourceTypeEntity(final SnmpCollectionSource source, final String name) {
        final SnmpCollectionResourceType e = new SnmpCollectionResourceType();
        e.setName(name);
        e.setLabel(name);
        e.setEnabled(true);
        e.setCollectionSource(source);
        return e;
    }

    private SnmpCollectionSource source(final int id, final String name) {
        final SnmpCollectionSource s = new SnmpCollectionSource();
        s.setId(id);
        s.setName(name);
        s.setEnabled(true);
        return s;
    }

    private Group group(final String name) {
        final Group g = new Group();
        g.setName(name);
        g.setIfType("all");
        return g;
    }

    private SystemDef systemDef(final String name) {
        final SystemDef sd = new SystemDef();
        sd.setName(name);
        sd.setSysoid(".1.3.6.1.4.1.99");
        sd.setCollect(new Collect());
        return sd;
    }
}
