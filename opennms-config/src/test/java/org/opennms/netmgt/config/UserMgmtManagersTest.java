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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;

import org.junit.Test;
import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.dao.api.SecurityRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao;
import org.springframework.transaction.PlatformTransactionManager;

public class UserMgmtManagersTest {

    private static UserMgmtManagers managers() {
        return new UserMgmtManagers(mock(UserAccountDao.class), mock(UserGroupDao.class), mock(OnCallRoleDao.class),
                mock(SecurityRoleDao.class), mock(UserMgmtVersionDao.class), mock(PlatformTransactionManager.class));
    }

    @Test
    public void testBuildsTheDatabaseManagers() {
        final UserMgmtManagers managers = managers();
        assertTrue(managers.getUserManager() instanceof DbUserManager);
        assertTrue(managers.getGroupManager() instanceof DbGroupManager);
    }

    @Test
    public void testExpireAllExpiresBothManagers() throws Exception {
        final UserMgmtManagers managers = managers();
        final Field userCheck = lastCheck(DbUserManager.class);
        final Field groupCheck = lastCheck(DbGroupManager.class);
        userCheck.setLong(managers.getUserManager(), 42L);
        groupCheck.setLong(managers.getGroupManager(), 42L);

        managers.expireAll();
        assertEquals(0L, userCheck.getLong(managers.getUserManager()));
        assertEquals(0L, groupCheck.getLong(managers.getGroupManager()));
    }

    private static Field lastCheck(final Class<?> clazz) throws NoSuchFieldException {
        final Field field = clazz.getDeclaredField("m_lastCheck");
        field.setAccessible(true);
        return field;
    }
}
