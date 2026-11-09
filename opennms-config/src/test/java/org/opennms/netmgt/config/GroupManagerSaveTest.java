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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Groupinfo;
import org.opennms.netmgt.model.OnmsGroup;

/**
 * A save of an OnmsGroup, which carries only the name, the comments and the members, keeps the other fields of the
 * group: those of the cache that the save is checked against, not those of an earlier read.
 */
public class GroupManagerSaveTest {

    private static String groupsXml(final String schedule, final String defaultMap) {
        return "<groupinfo xmlns=\"http://xmlns.opennms.org/xsd/groups\">\n"
                + "  <header><rev>1.0</rev><created>now</created><mstation>test</mstation></header>\n"
                + "  <groups>\n"
                + "    <group><name>ops</name><default-map>" + defaultMap + "</default-map><comments>c</comments>"
                + "<user>jroe</user><duty-schedule>" + schedule + "</duty-schedule></group>\n"
                + "  </groups>\n"
                + "</groupinfo>";
    }

    /** Each update() loads the next state in the queue, as the database manager reloads after another change. */
    private static class ReloadingGroupManager extends GroupManager {
        private final Deque<String> m_reloads = new ArrayDeque<>();
        private String m_lastLoaded;
        private String m_saved;

        ReloadingGroupManager(final String xml) throws IOException {
            loadXml(xml);
        }

        private void loadXml(final String xml) throws IOException {
            m_lastLoaded = xml;
            parseXml(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        }

        @Override
        public synchronized void update() throws IOException {
            final String next = m_reloads.poll();
            if (next != null && !next.isEmpty()) {
                loadXml(next);
            }
        }

        @Override
        protected void saveXml(final String data) throws IOException {
            m_saved = data;
        }

        Group lastLoaded(final String name) {
            return group(m_lastLoaded, name);
        }

        Group saved(final String name) {
            return group(m_saved, name);
        }

        private static Group group(final String xml, final String name) {
            return JaxbUtils.unmarshal(Groupinfo.class, new StringReader(xml)).getGroups().stream()
                    .filter(g -> name.equals(g.getName())).findFirst().orElseThrow();
        }
    }

    @Test
    public void testSaveKeepsTheFieldsOfTheCacheItWrites() throws Exception {
        final ReloadingGroupManager gm = new ReloadingGroupManager(groupsXml("MoTuWeThFr800-1700", "Map1"));
        // another process changes the schedule and the map; the manager sees it at the second update()
        gm.m_reloads.add("");
        gm.m_reloads.add(groupsXml("SaSu800-1700", "Map2"));

        final OnmsGroup group = new OnmsGroup("ops");
        group.setComments("edited");
        group.setUsers(List.of("jroe"));
        gm.save(group);

        final Group saved = gm.saved("ops");
        final Group cached = gm.lastLoaded("ops");
        assertEquals("edited", saved.getComments().orElse(null));
        assertEquals(List.of("jroe"), saved.getUsers());
        // the database manager checks the save against the version of the cache it holds when it writes
        assertEquals(cached.getDutySchedules(), saved.getDutySchedules());
        assertEquals(cached.getDefaultMap(), saved.getDefaultMap());
    }

    @Test
    public void testSaveOfANewGroup() throws Exception {
        final ReloadingGroupManager gm = new ReloadingGroupManager(groupsXml("MoTuWeThFr800-1700", "Map1"));
        final OnmsGroup group = new OnmsGroup("new");
        group.setComments("fresh");
        gm.save(group);

        final Group saved = gm.saved("new");
        assertEquals("fresh", saved.getComments().orElse(null));
        assertEquals(List.of(), saved.getDutySchedules());
        assertEquals(java.util.Optional.empty(), saved.getDefaultMap());
        assertEquals(List.of("MoTuWeThFr800-1700"), gm.saved("ops").getDutySchedules());
    }

    @Test
    public void testSaveDoesNotChangeTheCachedGroupWhenItFails() throws Exception {
        final ReloadingGroupManager gm = new ReloadingGroupManager(groupsXml("MoTuWeThFr800-1700", "Map1")) {
            @Override
            protected void saveXml(final String data) throws IOException {
                throw new IOException("simulated save failure");
            }
        };
        final Group before = gm.getGroup("ops");
        final OnmsGroup group = new OnmsGroup("ops");
        group.setComments("edited");
        try {
            gm.save(group);
        } catch (final IOException expected) {
            // the cache is the database manager's to repair; the group read before must not have been changed
        }
        assertEquals("c", before.getComments().orElse(null));
    }
}
