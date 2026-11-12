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

import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.dao.api.SecurityRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao;
import org.springframework.transaction.PlatformTransactionManager;

/** Creates the user and group managers on the user management tables. */
public class UserMgmtManagers {
    private final DbUserManager m_userManager;
    private final DbGroupManager m_groupManager;

    public UserMgmtManagers(final UserAccountDao userDao, final UserGroupDao groupDao, final OnCallRoleDao roleDao,
                            final SecurityRoleDao securityRoleDao, final UserMgmtVersionDao versionDao,
                            final PlatformTransactionManager transactionManager) {
        m_groupManager = new DbGroupManager(userDao, groupDao, roleDao, versionDao, transactionManager);
        m_userManager = new DbUserManager(m_groupManager, userDao, securityRoleDao, roleDao, versionDao, transactionManager);
    }

    public UserManager getUserManager() {
        return m_userManager;
    }

    public GroupManager getGroupManager() {
        return m_groupManager;
    }

    /**
     * Makes the next read of each manager check the database version at once, so a committed write is seen without
     * waiting for the check interval. Reads and reloads nothing now: a burst of writes costs one reload, on the next
     * read.
     */
    public void expireAll() {
        m_userManager.expire();
        m_groupManager.expire();
    }
}
