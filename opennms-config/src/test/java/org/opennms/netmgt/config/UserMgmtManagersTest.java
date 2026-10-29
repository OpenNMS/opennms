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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.netmgt.config.api.UserMgmtStore;
import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.dao.api.SecurityRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao;
import org.springframework.transaction.PlatformTransactionManager;

public class UserMgmtManagersTest {

    @Rule
    public TemporaryFolder m_home = new TemporaryFolder();

    @After
    public void tearDown() {
        System.clearProperty(UserMgmtStore.PROPERTY);
    }

    private static UserMgmtManagers managers() throws Exception {
        return new UserMgmtManagers(mock(UserAccountDao.class), mock(UserGroupDao.class), mock(OnCallRoleDao.class),
                mock(SecurityRoleDao.class), mock(UserMgmtVersionDao.class), mock(PlatformTransactionManager.class));
    }

    @Test
    public void testDatabaseIsTheDefault() throws Exception {
        final UserMgmtManagers managers = managers();
        assertTrue(managers.getUserManager() instanceof DbUserManager);
        assertTrue(managers.getGroupManager() instanceof DbGroupManager);
    }

    @Test
    public void testValueIsTrimmed() throws Exception {
        // java.util.Properties keeps trailing spaces of a value
        System.setProperty(UserMgmtStore.PROPERTY, "db ");
        assertTrue(managers().getUserManager() instanceof DbUserManager);
    }

    @Test
    public void testAnInvalidValueFailsWithAClearMessage() {
        System.setProperty(UserMgmtStore.PROPERTY, "DB");
        final IllegalStateException e = assertThrows(IllegalStateException.class, UserMgmtManagersTest::managers);
        assertEquals("org.opennms.usermgmt.store must be \"db\" or \"xml\", not \"DB\"", e.getMessage());
    }

    @Test
    public void testExpireAllIsNoOpInXmlMode() throws Exception {
        final Path home = m_home.getRoot().toPath();
        final Path etc = Files.createDirectory(home.resolve("etc"));
        Files.write(etc.resolve("users.xml"), ("<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\"><header><rev>1</rev>"
                + "<created>now</created><mstation>m</mstation></header><users><user><user-id>admin</user-id><full-name>A</full-name><password>x</password></user></users></userinfo>").getBytes(StandardCharsets.UTF_8));
        Files.write(etc.resolve("groups.xml"), ("<groupinfo xmlns=\"http://xmlns.opennms.org/xsd/groups\"><header><rev>1</rev>"
                + "<created>now</created><mstation>m</mstation></header><groups><group><name>Admin</name></group></groups></groupinfo>").getBytes(StandardCharsets.UTF_8));
        final String oldHome = System.getProperty("opennms.home");
        System.setProperty("opennms.home", home.toString());
        System.setProperty(UserMgmtStore.PROPERTY, "xml");
        try {
            final UserMgmtManagers managers = managers();
            managers.expireAll();
            assertSame(UserFactory.getInstance(), managers.getUserManager());
            assertSame(GroupFactory.getInstance(), managers.getGroupManager());
            // expireAll() only reaches the database managers: xml mode must not have any
            for (final Field field : UserMgmtManagers.class.getDeclaredFields()) {
                field.setAccessible(true);
                final Object value = field.get(managers);
                assertFalse(field.getName(), value instanceof DbUserManager || value instanceof DbGroupManager);
            }
        } finally {
            if (oldHome == null) {
                System.clearProperty("opennms.home");
            } else {
                System.setProperty("opennms.home", oldHome);
            }
            UserFactory.setInstance(null);
            GroupFactory.setInstance(null);
        }
    }
}
