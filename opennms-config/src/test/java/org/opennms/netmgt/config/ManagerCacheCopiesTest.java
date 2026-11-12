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
import static org.junit.Assert.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.model.OnmsGroup;
import org.opennms.netmgt.model.OnmsUser;

/**
 * The OnmsUser and OnmsGroup the managers hand out are copies: a caller that changes one and then does not save it (a
 * refused or failed request) must not leave its change in the cache, where the next save of anything would write it.
 */
public class ManagerCacheCopiesTest {

    private static final String GROUPS_XML =
            "<groupinfo xmlns=\"http://xmlns.opennms.org/xsd/groups\">\n"
            + "  <header><rev>1.0</rev><created>now</created><mstation>test</mstation></header>\n"
            + "  <groups><group><name>ops</name><user>admin</user><user>jroe</user></group></groups>\n"
            + "</groupinfo>";

    private static final String USERS_XML =
            "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\">\n"
            + "  <header><rev>1.0</rev><created>now</created><mstation>test</mstation></header>\n"
            + "  <users>\n"
            + "    <user><user-id>admin</user-id><full-name>Admin</full-name><password salt=\"true\">x</password>"
            + "<duty-schedule>MoTuWeThFr800-1700</duty-schedule><role>ROLE_ADMIN</role></user>\n"
            + "    <user><user-id>legacy</user-id><full-name>Legacy</full-name><password>y</password></user>\n"
            + "  </users>\n"
            + "</userinfo>";

    private static final class TestGroupManager extends GroupManager {
        TestGroupManager() throws IOException {
            parseXml(new ByteArrayInputStream(GROUPS_XML.getBytes(StandardCharsets.UTF_8)));
        }

        @Override
        public void update() {
        }

        @Override
        protected void saveXml(final String data) {
        }
    }

    private static final class TestUserManager extends UserManager {
        TestUserManager() throws IOException {
            super(new TestGroupManager());
            parseXML(new ByteArrayInputStream(USERS_XML.getBytes(StandardCharsets.UTF_8)));
        }

        @Override
        protected void saveXML(final String writerString) {
        }

        @Override
        protected void doUpdate() {
        }

        @Override
        public boolean isUpdateNeeded() {
            return false;
        }

        @Override
        public long getLastModified() {
            return 0;
        }

        @Override
        public long getFileSize() {
            return 0;
        }

        @Override
        public void reload() {
        }
    }

    @After
    public void tearDown() {
        System.clearProperty(UserManager.ALLOW_UNSALTED_PROPERTY);
    }

    @Test
    public void testAnOnmsUserDoesNotShareTheCachedLists() throws Exception {
        final UserManager um = new TestUserManager();
        final OnmsUser admin = um.getOnmsUser("admin");
        admin.getRoles().remove("ROLE_ADMIN");
        admin.getDutySchedule().clear();
        assertEquals(List.of("ROLE_ADMIN"), um.getUser("admin").getRoles());
        assertEquals(List.of("MoTuWeThFr800-1700"), um.getUser("admin").getDutySchedules());

        final OnmsUser listed = um.getOnmsUserList().getUsers().stream().filter(u -> "admin".equals(u.getUsername())).findFirst().orElseThrow();
        listed.getRoles().clear();
        assertEquals(List.of("ROLE_ADMIN"), um.getUser("admin").getRoles());
    }

    @Test
    public void testAnOnmsGroupDoesNotShareTheCachedMembers() throws Exception {
        final GroupManager gm = new TestGroupManager();
        final OnmsGroup group = gm.getOnmsGroup("ops");
        group.removeUser("jroe");
        assertEquals(List.of("admin", "jroe"), gm.getGroup("ops").getUsers());

        final Group cached = gm.getGroup("ops");
        new GroupManager.OnmsGroupMapper().map(cached).getUsers().clear();
        assertEquals(List.of("admin", "jroe"), cached.getUsers());
    }

    /** A save refused for an unsalted password must not leave the refused values in the cache. */
    @Test
    public void testARefusedUnsaltedSaveChangesNothing() throws Exception {
        System.setProperty(UserManager.ALLOW_UNSALTED_PROPERTY, "false");
        final UserManager um = new TestUserManager();
        final OnmsUser legacy = um.getOnmsUser("legacy");
        legacy.setFullName("Changed");
        assertThrows(IllegalStateException.class, () -> um.save(legacy));
        assertEquals("Legacy", um.getUser("legacy").getFullName().orElse(null));
    }
}
