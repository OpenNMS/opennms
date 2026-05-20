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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;
import org.opennms.netmgt.dao.api.SnmpCollectionProfileDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSourceDao;
import org.opennms.netmgt.model.SnmpCollectionProfile;
import org.opennms.netmgt.model.SnmpCollectionSource;

import java.util.List;
import java.util.Map;

/**
 * Pinpoint test for the dedup-by-name behavior in
 * {@link SnmpDataCollectionConfigLoader#reloadDataCollectionConfigFromDb()}.
 *
 * Two sources contributing the same group/systemDef name to one profile
 * must merge into a single occurrence each. Mirrors
 * {@code DataCollectionConfigParser.addSystemDef}'s contains-check.
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
        final SnmpDataCollectionConfigLoader loader = new SnmpDataCollectionConfigLoader() {
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
        return sd;
    }
}
