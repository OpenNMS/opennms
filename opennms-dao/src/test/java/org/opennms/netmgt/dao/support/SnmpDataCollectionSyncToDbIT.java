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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.api.DatacollectionJsonHelper;
import org.opennms.netmgt.config.datacollection.Collect;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.IpList;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.config.datacollection.SystemDef;
import org.opennms.netmgt.dao.api.SnmpCollectionMibGroupDao;
import org.opennms.netmgt.dao.api.SnmpCollectionProfileDao;
import org.opennms.netmgt.dao.api.SnmpCollectionResourceTypeDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSourceDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSystemDefDao;
import org.opennms.netmgt.model.SnmpCollectionProfile;
import org.opennms.netmgt.model.SnmpCollectionSource;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Round-trip IT for {@link SnmpDataCollectionSyncToDb}: synthetic plugin
 * groups are persisted, re-synced, mutated, and removed; we assert the DB
 * matches expectation at each step and that the admin-set {@code enabled}
 * flag survives across syncs (the design contract for plugin-sourced rows).
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-mockEventForwarder.xml",
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=osgi")
@JUnitTemporaryDatabase
public class SnmpDataCollectionSyncToDbIT {

    @Autowired
    private SnmpDataCollectionSyncToDb syncToDb;

    @Autowired
    private SnmpCollectionSourceDao sourceDao;

    @Autowired
    private SnmpCollectionMibGroupDao mibGroupDao;

    @Autowired
    private SnmpCollectionResourceTypeDao resourceTypeDao;

    @Autowired
    private SnmpCollectionSystemDefDao systemDefDao;

    @Autowired
    private SnmpCollectionProfileDao profileDao;

    private static final String DEFAULT_TARGET = "default";

    @Before
    @Transactional
    public void wipe() {
        sourceDao.deleteAll(sourceDao.findAll());
        sourceDao.flush();
        profileDao.deleteAll(profileDao.findAll());
        profileDao.flush();
    }

    @After
    @Transactional
    public void cleanup() {
        sourceDao.deleteAll(sourceDao.findAll());
        sourceDao.flush();
        profileDao.deleteAll(profileDao.findAll());
        profileDao.flush();
    }

    private static Map<String, List<DatacollectionGroup>> targeted(final DatacollectionGroup... groups) {
        return Map.of(DEFAULT_TARGET, Arrays.asList(groups));
    }

    @Test
    public void initialSync_createsSourceAndChildren() {
        final DatacollectionGroup grp = buildGroup("plugin-foo", 2, 1, 1);

        final boolean changed = syncToDb.syncPluginGroupsToDb(targeted(grp));

        assertTrue("Initial sync of a new group should report changed=true", changed);

        final SnmpCollectionSource saved = sourceDao.findByName("plugin-foo");
        assertNotNull("Plugin source should exist", saved);
        assertEquals(SnmpDataCollectionSyncToDb.PLUGIN_UPLOADED_BY, saved.getUploadedBy());
        assertTrue("New plugin sources default to enabled", saved.getEnabled());
        assertEquals(2, mibGroupDao.findAllBySource(saved.getId()).size());
        assertEquals(1, resourceTypeDao.findAllBySource(saved.getId()).size());
        assertEquals(1, systemDefDao.findAllBySource(saved.getId()).size());
    }

    @Test
    public void resync_withChangedContent_replacesChildren() {
        final DatacollectionGroup v1 = buildGroup("plugin-foo", 2, 1, 1);
        syncToDb.syncPluginGroupsToDb(targeted(v1));

        final DatacollectionGroup v2 = buildGroup("plugin-foo", 4, 2, 3);
        final boolean changed = syncToDb.syncPluginGroupsToDb(targeted(v2));

        assertTrue(changed);
        final SnmpCollectionSource saved = sourceDao.findByName("plugin-foo");
        assertEquals(4, mibGroupDao.findAllBySource(saved.getId()).size());
        assertEquals(2, resourceTypeDao.findAllBySource(saved.getId()).size());
        assertEquals(3, systemDefDao.findAllBySource(saved.getId()).size());
    }

    @Test
    public void resync_preservesEnabledFlag_setByAdmin() {
        final DatacollectionGroup grp = buildGroup("plugin-foo", 2, 1, 1);
        syncToDb.syncPluginGroupsToDb(targeted(grp));

        // Admin disables the plugin source via the UI/REST.
        final SnmpCollectionSource saved = sourceDao.findByName("plugin-foo");
        saved.setEnabled(Boolean.FALSE);
        sourceDao.saveOrUpdate(saved);
        sourceDao.flush();

        // Plugin reloads (same content). The disable intent must survive.
        syncToDb.syncPluginGroupsToDb(targeted(grp));

        final SnmpCollectionSource afterResync = sourceDao.findByName("plugin-foo");
        assertNotNull(afterResync);
        assertFalse("Admin-set disable should survive a plugin re-sync", afterResync.getEnabled());
    }

    @Test
    public void resync_withFewerGroups_deletesOrphans() {
        final DatacollectionGroup a = buildGroup("plugin-a", 1, 0, 0);
        final DatacollectionGroup b = buildGroup("plugin-b", 1, 0, 0);
        syncToDb.syncPluginGroupsToDb(targeted(a, b));

        assertNotNull(sourceDao.findByName("plugin-a"));
        assertNotNull(sourceDao.findByName("plugin-b"));

        // Plugin stops contributing 'plugin-b'.
        final boolean changed = syncToDb.syncPluginGroupsToDb(targeted(a));

        assertTrue(changed);
        assertNotNull("Still-contributed group should remain", sourceDao.findByName("plugin-a"));
        assertNull("Dropped group should be deleted", sourceDao.findByName("plugin-b"));
    }

    @Test
    public void sync_doesNotTouch_nonPluginSources() {
        // Pre-populate a user/migration-uploaded source.
        final SnmpCollectionSource user = new SnmpCollectionSource();
        user.setName("user-uploaded");
        user.setUploadedBy("admin");
        user.setEnabled(Boolean.TRUE);
        final Date now = new Date();
        user.setCreatedTime(now);
        user.setLastModified(now);
        sourceDao.save(user);
        sourceDao.flush();

        syncToDb.syncPluginGroupsToDb(targeted(buildGroup("plugin-foo", 1, 0, 0)));

        // Both should coexist.
        assertNotNull(sourceDao.findByName("user-uploaded"));
        assertNotNull(sourceDao.findByName("plugin-foo"));

        // A sync with no incoming groups must NOT delete the user row.
        syncToDb.syncPluginGroupsToDb(Collections.emptyMap());

        assertNotNull("User-uploaded sources are never touched by plugin sync",
                sourceDao.findByName("user-uploaded"));
        assertNull("Plugin source should be removed when no plugin contributes it",
                sourceDao.findByName("plugin-foo"));
    }

    @Test
    public void pluginGroupCollidesWithNonPluginSourceName_takesOver() {
        // Pre-existing non-plugin source occupying the name a plugin will try to use.
        final SnmpCollectionSource shipped = new SnmpCollectionSource();
        shipped.setName("Cisco Nexus");
        shipped.setUploadedBy("system-migration");
        shipped.setEnabled(Boolean.FALSE);  // also verify enabled flag survives takeover
        final Date now = new Date();
        shipped.setCreatedTime(now);
        shipped.setLastModified(now);
        sourceDao.save(shipped);
        sourceDao.flush();
        final Integer originalId = shipped.getId();

        // Plugin contributes a group with the same name; sync should take over
        // the existing row (matches pre-DB-migration override behavior).
        final boolean changed = syncToDb.syncPluginGroupsToDb(
                targeted(buildGroup("Cisco Nexus", 2, 0, 1)));

        assertTrue("Take-over reports changed=true", changed);
        final SnmpCollectionSource taken = sourceDao.findByName("Cisco Nexus");
        assertNotNull(taken);
        assertEquals("Row should keep its id across takeover", originalId, taken.getId());
        assertEquals("Row should now be plugin-managed",
                SnmpDataCollectionSyncToDb.PLUGIN_UPLOADED_BY, taken.getUploadedBy());
        assertEquals("Enabled flag is preserved across takeover", Boolean.FALSE, taken.getEnabled());
        assertEquals("Children should be replaced by plugin content",
                2, mibGroupDao.findAllBySource(taken.getId()).size());
        assertEquals(1, systemDefDao.findAllBySource(taken.getId()).size());
    }

    @Test
    public void emptyInput_isNoOp_whenNoPluginRowsExist() {
        final boolean changed = syncToDb.syncPluginGroupsToDb(Collections.emptyMap());
        assertFalse("Sync of empty input with no existing plugin rows should report no change", changed);
    }

    @Test
    public void nullInput_isTreatedAsEmpty() {
        final boolean changed = syncToDb.syncPluginGroupsToDb(null);
        assertFalse(changed);
    }

    @Test
    public void groupWithNullOrBlankName_isSkipped() {
        final DatacollectionGroup nameless = new DatacollectionGroup();
        nameless.setGroups(List.of(buildMibGroup("group-1")));

        final DatacollectionGroup valid = buildGroup("plugin-foo", 1, 0, 0);

        syncToDb.syncPluginGroupsToDb(targeted(nameless, valid));

        assertNotNull(sourceDao.findByName("plugin-foo"));
    }

    @Test
    public void autoAttach_addsSourceNameToTargetProfile() {
        final SnmpCollectionProfile profile = newProfile(DEFAULT_TARGET);
        profileDao.saveOrUpdate(profile);
        profileDao.flush();

        syncToDb.syncPluginGroupsToDb(targeted(buildGroup("plugin-foo", 1, 0, 0)));

        final SnmpCollectionProfile after = profileDao.findByName(DEFAULT_TARGET);
        assertNotNull(after);
        assertNotNull("source_names should be populated after auto-attach", after.getSourceNames());
        assertTrue("Profile's source_names should contain the plugin source name",
                after.getSourceNames().contains("plugin-foo"));
    }

    @Test
    public void autoAttach_isIdempotent_acrossResyncs() {
        final SnmpCollectionProfile profile = newProfile(DEFAULT_TARGET);
        profileDao.saveOrUpdate(profile);
        profileDao.flush();

        syncToDb.syncPluginGroupsToDb(targeted(buildGroup("plugin-foo", 1, 0, 0)));
        syncToDb.syncPluginGroupsToDb(targeted(buildGroup("plugin-foo", 2, 0, 0)));
        syncToDb.syncPluginGroupsToDb(targeted(buildGroup("plugin-foo", 1, 0, 0)));

        final SnmpCollectionProfile after = profileDao.findByName(DEFAULT_TARGET);
        final String names = after.getSourceNames();
        final int firstIdx = names.indexOf("plugin-foo");
        assertTrue(firstIdx >= 0);
        assertEquals("plugin-foo should appear exactly once across re-syncs",
                firstIdx, names.lastIndexOf("plugin-foo"));
    }

    @Test
    public void orphanCleanup_detachesSourceFromProfiles() {
        final SnmpCollectionProfile profile = newProfile(DEFAULT_TARGET);
        profileDao.saveOrUpdate(profile);
        profileDao.flush();

        syncToDb.syncPluginGroupsToDb(targeted(buildGroup("plugin-foo", 1, 0, 0)));
        assertTrue(profileDao.findByName(DEFAULT_TARGET).getSourceNames().contains("plugin-foo"));

        syncToDb.syncPluginGroupsToDb(Collections.emptyMap());

        assertNull(sourceDao.findByName("plugin-foo"));
        final SnmpCollectionProfile after = profileDao.findByName(DEFAULT_TARGET);
        if (after.getSourceNames() != null) {
            assertFalse(after.getSourceNames().contains("plugin-foo"));
        }
    }

    @Test
    public void orphanCleanup_leavesOtherProfileEntriesUntouched() {
        final SnmpCollectionProfile profile = newProfile(DEFAULT_TARGET);
        profile.setSourceNames(DatacollectionJsonHelper.toJson(List.of("plugin-foo", "user-source")));
        profileDao.saveOrUpdate(profile);
        profileDao.flush();

        syncToDb.syncPluginGroupsToDb(targeted(buildGroup("plugin-foo", 1, 0, 0)));
        syncToDb.syncPluginGroupsToDb(Collections.emptyMap());

        final SnmpCollectionProfile after = profileDao.findByName(DEFAULT_TARGET);
        assertFalse(after.getSourceNames().contains("plugin-foo"));
        assertTrue(after.getSourceNames().contains("user-source"));
    }

    @Test
    public void missingTargetProfile_doesNotFailSync() {
        final boolean changed = syncToDb.syncPluginGroupsToDb(
                Map.of("no-such-profile", List.of(buildGroup("plugin-foo", 1, 0, 0))));

        assertTrue(changed);
        assertNotNull("Source row is created even when target profile is missing",
                sourceDao.findByName("plugin-foo"));
        assertNull("No profile of that name should magically appear",
                profileDao.findByName("no-such-profile"));
    }

    @Test
    public void isPluginSourced_helper() {
        final SnmpCollectionSource userOwned = new SnmpCollectionSource();
        userOwned.setUploadedBy("admin");
        assertFalse(SnmpDataCollectionSyncToDb.isPluginSourced(userOwned));

        final SnmpCollectionSource pluginOwned = new SnmpCollectionSource();
        pluginOwned.setUploadedBy(SnmpDataCollectionSyncToDb.PLUGIN_UPLOADED_BY);
        assertTrue(SnmpDataCollectionSyncToDb.isPluginSourced(pluginOwned));

        assertFalse("Null source defaults to non-plugin", SnmpDataCollectionSyncToDb.isPluginSourced(null));
    }

    @Test
    public void requireNotPluginSourced_throwsForPluginRow() {
        final SnmpCollectionSource pluginOwned = new SnmpCollectionSource();
        pluginOwned.setName("plugin-foo");
        pluginOwned.setUploadedBy(SnmpDataCollectionSyncToDb.PLUGIN_UPLOADED_BY);

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> SnmpDataCollectionSyncToDb.requireNotPluginSourced(pluginOwned, "Delete Source"));
        assertTrue("Error message should name the operation", ex.getMessage().contains("Delete Source"));
        assertTrue("Error message should name the source", ex.getMessage().contains("plugin-foo"));
    }

    @Test
    public void requireNotPluginSourced_allowsUserRow() {
        final SnmpCollectionSource userOwned = new SnmpCollectionSource();
        userOwned.setUploadedBy("admin");
        SnmpDataCollectionSyncToDb.requireNotPluginSourced(userOwned, "Delete Source");
        // no throw
    }

    // ─── fixture builders ──────────────────────────────────────────────

    private static SnmpCollectionProfile newProfile(final String name) {
        final SnmpCollectionProfile p = new SnmpCollectionProfile();
        p.setName(name);
        p.setRrdStep(300);
        p.setEnabled(Boolean.TRUE);
        final Date now = new Date();
        p.setCreatedTime(now);
        p.setLastModified(now);
        return p;
    }

    private static DatacollectionGroup buildGroup(final String name,
                                                  final int mibGroupCount,
                                                  final int resourceTypeCount,
                                                  final int systemDefCount) {
        final DatacollectionGroup g = new DatacollectionGroup();
        g.setName(name);

        final List<Group> groups = new ArrayList<>();
        for (int i = 0; i < mibGroupCount; i++) {
            groups.add(buildMibGroup(name + "-group-" + i));
        }
        g.setGroups(groups);

        final List<ResourceType> rts = new ArrayList<>();
        for (int i = 0; i < resourceTypeCount; i++) {
            rts.add(buildResourceType(name + "-rt-" + i));
        }
        g.setResourceTypes(rts);

        final List<SystemDef> sds = new ArrayList<>();
        for (int i = 0; i < systemDefCount; i++) {
            sds.add(buildSystemDef(name + "-sd-" + i));
        }
        g.setSystemDefs(sds);

        return g;
    }

    private static Group buildMibGroup(final String name) {
        final Group group = new Group();
        group.setName(name);
        group.setIfType("ignore");
        final MibObj obj = new MibObj();
        obj.setOid(".1.3.6.1.2.1.1.3");
        obj.setAlias("sysUpTime");
        obj.setType("timeticks");
        obj.setInstance("0");
        group.setMibObjs(List.of(obj));
        return group;
    }

    private static ResourceType buildResourceType(final String name) {
        final ResourceType rt = new ResourceType();
        rt.setName(name);
        rt.setLabel(name + "-label");
        final PersistenceSelectorStrategy ps = new PersistenceSelectorStrategy();
        ps.setClazz("org.opennms.netmgt.collection.support.PersistAllSelectorStrategy");
        rt.setPersistenceSelectorStrategy(ps);
        final StorageStrategy ss = new StorageStrategy();
        ss.setClazz("org.opennms.netmgt.dao.support.IndexStorageStrategy");
        rt.setStorageStrategy(ss);
        return rt;
    }

    private static SystemDef buildSystemDef(final String name) {
        final SystemDef sd = new SystemDef();
        sd.setName(name);
        sd.setSysoid(".1.3.6.1.4.1.42." + name.hashCode());
        final Collect collect = new Collect();
        collect.setIncludeGroups(List.of("dummy-mib-group"));
        sd.setCollect(collect);
        final IpList ipList = new IpList();
        ipList.setIpAddresses(List.of("127.0.0.1"));
        sd.setIpList(ipList);
        return sd;
    }
}
