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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.Test;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.api.DatacollectionJsonHelper;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.dao.api.SnmpCollectionMibGroupDao;
import org.opennms.netmgt.dao.api.SnmpCollectionProfileDao;
import org.opennms.netmgt.dao.api.SnmpCollectionResourceTypeDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSourceDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSystemDefDao;
import org.opennms.netmgt.model.SnmpCollectionMibGroup;
import org.opennms.netmgt.model.SnmpCollectionProfile;
import org.opennms.netmgt.model.SnmpCollectionResourceType;
import org.opennms.netmgt.model.SnmpCollectionSource;
import org.opennms.netmgt.model.SnmpCollectionSystemDef;

/**
 * Covers each state that a referenced MIB group or resource type can have
 * when the loader resolves it across sources. The fake DAOs answer from one
 * in-memory model, so the real per-source read runs, with its enabled filters.
 *
 * <p>Rules under test:
 * <ul>
 * <li>An enabled definition in an attached source is used.</li>
 * <li>A definition that an attached source disables is not taken from another source.</li>
 * <li>Otherwise the enabled definition in the enabled source with the lowest id is used.</li>
 * <li>A disabled source counts as not attached.</li>
 * <li>An inline source is used only by the profile that attaches it.</li>
 * <li>Groups are resolved per profile. Resource types are global.</li>
 * </ul>
 */
public class SnmpDataCollectionCrossSourceResolutionTest {

    // --- MIB groups ---------------------------------------------------------

    @Test
    public void groupEnabledInAttachedSourceIsUsed() {
        final World w = new World();
        final SnmpCollectionSource a = w.source(1, "A", true);
        w.systemDef(a, "N");
        w.group(a, "N", true);
        w.group(w.source(2, "X", true), "N", true);
        w.profile("p", "A");

        assertEquals(Map.of("N", "A"), w.groupsOf("p"));
    }

    @Test
    public void groupDisabledInAttachedSourceIsNotTakenFromOtherSource() {
        final World w = new World();
        final SnmpCollectionSource a = w.source(1, "A", true);
        w.systemDef(a, "N");
        w.group(a, "N", false);
        w.group(w.source(2, "X", true), "N", true);
        w.profile("p", "A");

        assertEquals(Map.of(), w.groupsOf("p"));
    }

    @Test
    public void groupEnabledInOneAttachedSourceWinsOverDisabledInAnother() {
        final World w = new World();
        final SnmpCollectionSource a = w.source(1, "A", true);
        w.systemDef(a, "N");
        w.group(a, "N", false);
        w.group(w.source(2, "B", true), "N", true);
        w.group(w.source(3, "X", true), "N", true);
        w.profile("p", "A", "B");

        assertEquals(Map.of("N", "B"), w.groupsOf("p"));
    }

    @Test
    public void groupNotInAttachedSourceIsTakenFromSourceWithLowestId() {
        final World w = new World();
        final SnmpCollectionSource a = w.source(1, "A", true);
        w.systemDef(a, "N");
        w.group(w.source(3, "X", true), "N", true);
        w.group(w.source(2, "Y", true), "N", true);
        w.profile("p", "A");

        assertEquals(Map.of("N", "Y"), w.groupsOf("p"));
    }

    @Test
    public void disabledGroupInUnattachedSourceIsIgnored() {
        final World w = new World();
        final SnmpCollectionSource a = w.source(1, "A", true);
        w.systemDef(a, "N");
        w.group(w.source(2, "X", true), "N", false);
        w.group(w.source(3, "Y", true), "N", true);
        w.profile("p", "A");

        assertEquals(Map.of("N", "Y"), w.groupsOf("p"));
    }

    @Test
    public void groupOnlyInDisabledSourceIsNotCollected() {
        final World w = new World();
        final SnmpCollectionSource a = w.source(1, "A", true);
        w.systemDef(a, "N");
        w.group(w.source(2, "X", false), "N", true);
        w.profile("p", "A");

        assertEquals(Map.of(), w.groupsOf("p"));
    }

    @Test
    public void groupThatNoSourceDefinesIsNotCollected() {
        final World w = new World();
        w.systemDef(w.source(1, "A", true), "N");
        w.profile("p", "A");

        assertEquals(Map.of(), w.groupsOf("p"));
    }

    @Test
    public void disabledAttachedSourceCountsAsNotAttached() {
        final World w = new World();
        final SnmpCollectionSource a = w.source(1, "A", true);
        w.systemDef(a, "N");
        w.group(w.source(2, "D", false), "N", true);
        w.group(w.source(3, "X", true), "N", true);
        w.profile("p", "A", "D");

        assertEquals(Map.of("N", "X"), w.groupsOf("p"));
    }

    @Test
    public void nestedIncludeFollowsTheSameRules() {
        // A's systemDef uses M from X. M includes N1 and N2.
        // A disables N1. No attached source defines N2, and Y defines it.
        final World w = new World();
        final SnmpCollectionSource a = w.source(1, "A", true);
        w.systemDef(a, "M");
        w.group(a, "N1", false);
        final SnmpCollectionSource x = w.source(2, "X", true);
        w.group(x, "M", true, "N1", "N2");
        w.group(x, "N1", true);
        w.group(w.source(3, "Y", true), "N2", true);
        w.profile("p", "A");

        assertEquals(Map.of("M", "X", "N2", "Y"), w.groupsOf("p"));
    }

    @Test
    public void groupIncludeOfAnAttachedGroupFollowsTheSameRules() {
        // A's own group G includes N, which A disables.
        final World w = new World();
        final SnmpCollectionSource a = w.source(1, "A", true);
        w.systemDef(a, "G");
        w.group(a, "G", true, "N");
        w.group(a, "N", false);
        w.group(w.source(2, "X", true), "N", true);
        w.profile("p", "A");

        assertEquals(Map.of("G", "A"), w.groupsOf("p"));
    }

    @Test
    public void inlineSourceIsNotUsedForOtherProfiles() {
        // "__inline_q" has the lowest id after A, but it holds copies for profile q only.
        final World w = new World();
        final SnmpCollectionSource a = w.source(1, "A", true);
        w.systemDef(a, "N", "M");
        w.group(w.source(2, "__inline_q", true), "N", true);
        w.group(w.source(3, "__inline_q2", true), "M", true);
        w.group(w.source(4, "X", true), "N", true);
        w.profile("p", "A");

        assertEquals(Map.of("N", "X"), w.groupsOf("p"));
    }

    @Test
    public void groupsAreResolvedPerProfile() {
        // Profile p attaches A, which disables N. Profile q attaches C, which does not define N.
        final World w = new World();
        final SnmpCollectionSource a = w.source(1, "A", true);
        w.systemDef(a, "N");
        w.group(a, "N", false);
        w.systemDef(w.source(2, "C", true), "N");
        w.group(w.source(3, "X", true), "N", true);
        w.profile("p", "A");
        w.profile("q", "C");

        assertEquals(Map.of(), w.groupsOf("p"));
        assertEquals(Map.of("N", "X"), w.groupsOf("q"));
    }

    // --- Resource types -----------------------------------------------------

    @Test
    public void resourceTypeDisabledInAttachedSourceIsNotTakenFromOtherSource() {
        final World w = new World();
        final SnmpCollectionSource a = w.attachedGroupUsing("R");
        w.resourceType(a, "R", false);
        w.resourceType(w.source(2, "X", true), "R", true);
        w.profile("p", "A");

        assertEquals(null, w.resourceTypeSource("R"));
    }

    @Test
    public void resourceTypeOfAnotherProfileAttachedSourceIsGlobal() {
        // Profile p attaches A, which disables R. Profile q attaches B, which enables R.
        final World w = new World();
        final SnmpCollectionSource a = w.attachedGroupUsing("R");
        w.resourceType(a, "R", false);
        w.resourceType(w.source(2, "B", true), "R", true);
        w.resourceType(w.source(3, "X", true), "R", true);
        w.profile("p", "A");
        w.profile("q", "B");

        assertEquals("B", w.resourceTypeSource("R"));
    }

    @Test
    public void resourceTypeThatOneProfileNeedsIsAddedWhenAnotherProfileDisablesIt() {
        // Profile p attaches A, which uses R and does not define it.
        // Profile q attaches B, which disables R. X enables R.
        final World w = new World();
        w.attachedGroupUsing("R");
        w.resourceType(w.source(2, "B", true), "R", false);
        w.resourceType(w.source(3, "X", true), "R", true);
        w.profile("p", "A");
        w.profile("q", "B");

        assertEquals("X", w.resourceTypeSource("R"));
    }

    // --- Model --------------------------------------------------------------

    /** Sources, definitions and profiles, with DAO mocks that answer from them. */
    private static final class World {
        private final List<SnmpCollectionSource> sources = new ArrayList<>();
        private final List<SnmpCollectionMibGroup> groups = new ArrayList<>();
        private final List<SnmpCollectionResourceType> resourceTypes = new ArrayList<>();
        private final List<SnmpCollectionSystemDef> systemDefs = new ArrayList<>();
        private final List<SnmpCollectionProfile> profiles = new ArrayList<>();
        private SnmpDataCollectionConfigLoaderImpl.MaterializedConfig materialized;

        SnmpCollectionSource source(final int id, final String name, final boolean enabled) {
            final SnmpCollectionSource s = new SnmpCollectionSource();
            s.setId(id);
            s.setName(name);
            s.setEnabled(enabled);
            sources.add(s);
            return s;
        }

        void systemDef(final SnmpCollectionSource source, final String... includeGroups) {
            final SnmpCollectionSystemDef sd = new SnmpCollectionSystemDef();
            sd.setId(systemDefs.size() + 1);
            sd.setName(source.getName() + "-sd");
            sd.setSysoid(".1.3.6.1.4.1.99");
            sd.setEnabled(true);
            sd.setCollectionSource(source);
            sd.setMibGroupNames(DatacollectionJsonHelper.toJson(List.of(includeGroups)));
            systemDefs.add(sd);
        }

        void group(final SnmpCollectionSource source, final String name, final boolean enabled,
                   final String... includeGroups) {
            groupWithInstance(source, name, enabled, "0", includeGroups);
        }

        /** Source "A" (id 1) with a systemDef and an enabled group whose MIB object uses the given instance. */
        SnmpCollectionSource attachedGroupUsing(final String instance) {
            final SnmpCollectionSource a = source(1, "A", true);
            systemDef(a, "G");
            groupWithInstance(a, "G", true, instance);
            return a;
        }

        private void groupWithInstance(final SnmpCollectionSource source, final String name, final boolean enabled,
                                       final String instance, final String... includeGroups) {
            final MibObj obj = new MibObj();
            obj.setOid(".1.3.6.1.4.1.99.1");
            obj.setInstance(instance);
            // The alias records the owning source, so a test can see which copy was used.
            obj.setAlias(source.getName());
            obj.setType("gauge");
            final SnmpCollectionMibGroup g = new SnmpCollectionMibGroup();
            g.setId(groups.size() + 1);
            g.setName(name);
            g.setIfType("all");
            g.setEnabled(enabled);
            g.setCollectionSource(source);
            g.setMibObjects(DatacollectionJsonHelper.toJson(List.of(obj)));
            g.setMibGroupNames(includeGroups.length == 0 ? null : DatacollectionJsonHelper.toJson(List.of(includeGroups)));
            groups.add(g);
        }

        void resourceType(final SnmpCollectionSource source, final String name, final boolean enabled) {
            final SnmpCollectionResourceType rt = new SnmpCollectionResourceType();
            rt.setId(resourceTypes.size() + 1);
            rt.setName(name);
            // The label records the owning source, so a test can see which copy was used.
            rt.setLabel(source.getName());
            rt.setEnabled(enabled);
            rt.setCollectionSource(source);
            resourceTypes.add(rt);
        }

        void profile(final String name, final String... sourceNames) {
            final SnmpCollectionProfile p = new SnmpCollectionProfile();
            p.setName(name);
            p.setStorageFlag("select");
            p.setRrdStep(300);
            p.setRrdRras("[]");
            p.setSourceNames(DatacollectionJsonHelper.toJson(List.of(sourceNames)));
            profiles.add(p);
        }

        /** Collected group name to the name of the source whose copy is used. */
        Map<String, String> groupsOf(final String profileName) {
            final Map<String, String> result = new LinkedHashMap<>();
            for (final Group g : materialize().config.getSnmpCollection(profileName).getGroups().getGroups()) {
                result.put(g.getName(), g.getMibObjs().get(0).getAlias());
            }
            return result;
        }

        /** Name of the source whose copy of the resource type is used, or null. */
        String resourceTypeSource(final String name) {
            final ResourceType rt = materialize().allResourceTypes.get(name);
            return rt == null ? null : rt.getLabel();
        }

        private SnmpDataCollectionConfigLoaderImpl.MaterializedConfig materialize() {
            if (materialized == null) {
                materialized = loader().materializeFromDb();
            }
            return materialized;
        }

        private SnmpDataCollectionConfigLoaderImpl loader() {
            final SnmpCollectionProfileDao profileDao = mock(SnmpCollectionProfileDao.class);
            when(profileDao.findAllEnabled()).thenReturn(profiles);

            final SnmpCollectionSourceDao sourceDao = mock(SnmpCollectionSourceDao.class);
            when(sourceDao.findByName(anyString())).thenAnswer(i -> sources.stream()
                    .filter(s -> s.getName().equals(i.getArgument(0))).findFirst().orElse(null));

            final SnmpCollectionMibGroupDao mibGroupDao = mock(SnmpCollectionMibGroupDao.class);
            when(mibGroupDao.findAllEnabledBySource(anyInt())).thenAnswer(i -> groups.stream()
                    .filter(g -> g.getEnabled() && g.getCollectionSource().getId().equals(i.getArgument(0))).toList());
            when(mibGroupDao.findAllDisabledWithSource()).thenAnswer(i -> groups.stream()
                    .filter(g -> !g.getEnabled())
                    .sorted(Comparator.comparing((SnmpCollectionMibGroup g) -> g.getCollectionSource().getId()))
                    .toList());
            when(mibGroupDao.findAllWithSource()).thenAnswer(i -> groups.stream()
                    .sorted(Comparator.comparing((SnmpCollectionMibGroup g) -> g.getCollectionSource().getId()))
                    .toList());

            final SnmpCollectionResourceTypeDao resourceTypeDao = mock(SnmpCollectionResourceTypeDao.class);
            when(resourceTypeDao.findAllEnabledBySource(anyInt())).thenAnswer(i -> resourceTypes.stream()
                    .filter(r -> r.getEnabled() && r.getCollectionSource().getId().equals(i.getArgument(0))).toList());
            when(resourceTypeDao.findAllDisabledWithSource()).thenAnswer(i -> resourceTypes.stream()
                    .filter(r -> !r.getEnabled())
                    .sorted(Comparator.comparing((SnmpCollectionResourceType r) -> r.getCollectionSource().getId()))
                    .toList());
            when(resourceTypeDao.findAllWithSource()).thenAnswer(i -> resourceTypes.stream()
                    .sorted(Comparator.comparing((SnmpCollectionResourceType r) -> r.getCollectionSource().getId()))
                    .toList());

            final SnmpCollectionSystemDefDao systemDefDao = mock(SnmpCollectionSystemDefDao.class);
            when(systemDefDao.findAllEnabledBySource(anyInt())).thenAnswer(i -> systemDefs.stream()
                    .filter(sd -> sd.getEnabled() && Objects.equals(sd.getCollectionSource().getId(), i.getArgument(0)))
                    .toList());

            final DataCollectionConfigDao configDao = mock(DataCollectionConfigDao.class);
            when(configDao.getRrdPath()).thenReturn("/tmp/rrd/");

            final SnmpDataCollectionConfigLoaderImpl loader = new SnmpDataCollectionConfigLoaderImpl();
            loader.setSnmpCollectionProfileDao(profileDao);
            loader.setSnmpCollectionSourceDao(sourceDao);
            loader.setSnmpCollectionMibGroupDao(mibGroupDao);
            loader.setSnmpCollectionResourceTypeDao(resourceTypeDao);
            loader.setSnmpCollectionSystemDefDao(systemDefDao);
            loader.setDataCollectionConfigDao(configDao);
            return loader;
        }
    }
}
