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

import java.util.Objects;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Hands the database managers to the static UserFactory/GroupFactory, which the servlets, JSPs and other static
 * callers use, and takes them back when the context closes, so a later context in the same JVM does not get managers
 * that are bound to a closed SessionFactory.
 */
public class UserMgmtFactoryRegistration implements InitializingBean, DisposableBean {
    private final UserManager m_userManager;
    private final GroupManager m_groupManager;

    public UserMgmtFactoryRegistration(final UserManager userManager, final GroupManager groupManager) {
        m_userManager = Objects.requireNonNull(userManager);
        m_groupManager = Objects.requireNonNull(groupManager);
    }

    @Override
    public void afterPropertiesSet() {
        GroupFactory.setInstance(m_groupManager);
        UserFactory.setInstance(m_userManager);
    }

    @Override
    public void destroy() {
        synchronized (UserFactory.class) {
            if (UserFactory.getInstance() == m_userManager) {
                UserFactory.setInstance(null);
            }
        }
        synchronized (GroupFactory.class) {
            if (GroupFactory.getInstance() == m_groupManager) {
                GroupFactory.setInstance(null);
            }
        }
    }
}
