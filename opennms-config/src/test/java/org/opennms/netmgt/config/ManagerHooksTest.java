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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.junit.Test;
import org.opennms.netmgt.config.api.UserConfig.ContactType;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Groupinfo;
import org.opennms.netmgt.config.users.Userinfo;

/** The load and save hooks that the database implementations use instead of the XML. */
public class ManagerHooksTest {

    private static final String GROUPS = "<groupinfo xmlns=\"http://xmlns.opennms.org/xsd/groups\"><groups>"
            + "<group><name>Admin</name><user>admin</user></group></groups></groupinfo>";

    private static final String USERS = "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\"><users>"
            + "<user><user-id>admin</user-id><password salt=\"true\">x</password></user></users></userinfo>";

    private static class TestGroupManager extends GroupManager {
        final AtomicReference<Groupinfo> m_saved = new AtomicReference<>();

        TestGroupManager() throws IOException {
            parseXml(new ByteArrayInputStream(GROUPS.getBytes(StandardCharsets.UTF_8)));
        }

        @Override
        public void update() {
        }

        @Override
        protected void saveXml(final String data) {
        }

        @Override
        protected void saveGroups(final Groupinfo groupinfo) {
            m_saved.set(groupinfo);
        }
    }

    private static class TestUserManager extends UserManager {
        final AtomicReference<Userinfo> m_saved = new AtomicReference<>();

        TestUserManager(final GroupManager groupManager) {
            super(groupManager);
            parseXML(new ByteArrayInputStream(USERS.getBytes(StandardCharsets.UTF_8)));
        }

        @Override
        protected void saveXML(final String writerString) {
        }

        @Override
        protected void saveUsers(final Userinfo userinfo) {
            m_saved.set(userinfo);
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

    @Test
    public void testGroupSaveHookGetsTheModel() throws Exception {
        final TestGroupManager manager = new TestGroupManager();
        final Group ops = new Group();
        ops.setName("Ops");
        manager.saveGroup("Ops", ops);
        assertEquals(Arrays.asList("Admin", "Ops"),
                manager.m_saved.get().getGroups().stream().map(Group::getName).collect(Collectors.toList()));
    }

    @Test
    public void testUserSaveHookGetsTheModel() throws Exception {
        final TestUserManager manager = new TestUserManager(new TestGroupManager());
        manager.setContactInfo("admin", ContactType.email, "admin@example.org");
        assertEquals("admin@example.org", manager.m_saved.get().getUsers().get(0).getContacts().get(0).getInfo().get());
    }

    @Test
    public void testLoadReplacesTheGroups() throws Exception {
        final TestGroupManager manager = new TestGroupManager();
        final Groupinfo info = new Groupinfo();
        final Group ops = new Group();
        ops.setName("Ops");
        info.setGroups(Collections.singletonList(ops));
        info.setRoles(Collections.emptyList());
        manager.load(info);
        assertEquals(Collections.singletonList("Ops"), manager.getGroupNames());
    }

    @Test
    public void testLoadAcceptsAGroupinfoWithoutRoles() throws Exception {
        final TestGroupManager manager = new TestGroupManager();
        final Groupinfo info = new Groupinfo();
        info.setGroups(Collections.emptyList());
        manager.load(info);
        assertEquals(Collections.emptyList(), manager.getGroupNames());
    }

    @Test
    public void testLoadReplacesTheUsers() throws Exception {
        final TestUserManager manager = new TestUserManager(new TestGroupManager());
        final Userinfo info = new Userinfo();
        final org.opennms.netmgt.config.users.User jane = new org.opennms.netmgt.config.users.User();
        jane.setUserId("jane");
        info.setUsers(Collections.singletonList(jane));
        manager.load(info);
        assertEquals(Collections.singletonList("jane"), manager.getUserNames());
    }
}
