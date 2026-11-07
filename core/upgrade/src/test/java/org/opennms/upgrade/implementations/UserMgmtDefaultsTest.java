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
package org.opennms.upgrade.implementations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Groupinfo;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.config.users.Userinfo;

public class UserMgmtDefaultsTest {

    private static User user(final Userinfo info, final String id) {
        return info.getUsers().stream().filter(u -> id.equals(u.getUserId())).findFirst().orElseThrow(AssertionError::new);
    }

    @Test
    public void testUsers() {
        final Userinfo info = UserMgmtDefaults.users();
        assertEquals(2, info.getUsers().size());

        final User admin = user(info, "admin");
        assertEquals("Administrator", admin.getFullName().get());
        assertEquals("Default administrator, do not delete", admin.getUserComments().get());
        assertEquals("gU2wmSW7k9v1xg4/MrAsaI+VyddBAhJJt4zPX5SGG0BK+qiASGnJsqM8JOug/aEL", admin.getPassword().getEncryptedPassword());
        assertTrue(admin.getPassword().getSalt());
        assertEquals(java.util.Arrays.asList("ROLE_ADMIN"), admin.getRoles());

        final User rtc = user(info, "rtc");
        assertEquals("sHMy+HycWKGJC/uUMF0IGlXUXP1KhcqD0GEchFlvYTw40jT9r+zMxOb3F+phWNzX", rtc.getPassword().getEncryptedPassword());
        assertEquals(java.util.Arrays.asList("ROLE_RTC"), rtc.getRoles());
    }

    @Test
    public void testGroups() {
        final Groupinfo info = UserMgmtDefaults.groups();
        assertEquals(1, info.getGroups().size());
        final Group admin = info.getGroups().get(0);
        assertEquals("Admin", admin.getName());
        assertEquals("The administrators", admin.getComments().get());
        assertEquals(java.util.Arrays.asList("admin"), admin.getUsers());
    }

    @Test
    public void testEveryCallReturnsACopy() {
        final Userinfo first = UserMgmtDefaults.users();
        first.getUsers().clear();
        assertNotSame(first, UserMgmtDefaults.users());
        assertEquals(2, UserMgmtDefaults.users().getUsers().size());

        final Groupinfo groups = UserMgmtDefaults.groups();
        groups.getGroups().clear();
        assertEquals(1, UserMgmtDefaults.groups().getGroups().size());
    }
}
