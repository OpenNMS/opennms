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

import java.io.IOException;

import org.opennms.netmgt.config.api.UserMgmtStore;
import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.dao.api.SecurityRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.dao.api.UserGroupDao;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Creates the user and group managers of the store that org.opennms.usermgmt.store selects: the database ("db",
 * the default) or users.xml and groups.xml ("xml"). UserMgmtStore reads the value, so an invalid one stops the
 * context with a clear message.
 */
public class UserMgmtManagers {
    private final UserManager m_userManager;
    private final GroupManager m_groupManager;
    private final DbUserManager m_dbUserManager;
    private final DbGroupManager m_dbGroupManager;

    public UserMgmtManagers(final UserAccountDao userDao, final UserGroupDao groupDao, final OnCallRoleDao roleDao,
                            final SecurityRoleDao securityRoleDao, final UserMgmtVersionDao versionDao,
                            final PlatformTransactionManager transactionManager) throws IOException {
        if (UserMgmtStore.isDatabase()) {
            final DbGroupManager groupManager = new DbGroupManager(userDao, groupDao, roleDao, versionDao, transactionManager);
            final DbUserManager userManager = new DbUserManager(groupManager, userDao, securityRoleDao, roleDao, versionDao, transactionManager);
            m_groupManager = groupManager;
            m_userManager = userManager;
            m_dbGroupManager = groupManager;
            m_dbUserManager = userManager;
        } else {
            GroupFactory.init();
            UserFactory.init();
            m_groupManager = GroupFactory.getInstance();
            m_userManager = UserFactory.getInstance();
            m_dbGroupManager = null;
            m_dbUserManager = null;
        }
    }

    public UserManager getUserManager() {
        return m_userManager;
    }

    public GroupManager getGroupManager() {
        return m_groupManager;
    }

    /** Loads the users and then the groups from the database now; does nothing in xml mode. */
    public void reloadAll() {
        if (m_dbUserManager != null) {
            m_dbUserManager.reload();
            m_dbGroupManager.reload();
        }
    }
}
