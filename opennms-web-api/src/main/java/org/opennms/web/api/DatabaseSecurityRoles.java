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
package org.opennms.web.api;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.config.api.UserMgmtStore;
import org.opennms.netmgt.dao.api.SecurityRoleDao;
import org.opennms.netmgt.model.usermgmt.SecurityRole;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/** In the database mode, makes {@link Authentication} take the valid roles from the security_roles table. */
public class DatabaseSecurityRoles implements InitializingBean, DisposableBean {
    private final SecurityRoleDao m_securityRoleDao;

    public DatabaseSecurityRoles(final SecurityRoleDao securityRoleDao) {
        m_securityRoleDao = Objects.requireNonNull(securityRoleDao);
    }

    @Override
    public void afterPropertiesSet() {
        if (UserMgmtStore.isDatabase()) {
            Authentication.setRoleSource(this::roleNames);
        }
    }

    @Override
    public void destroy() {
        Authentication.setRoleSource(null);
    }

    List<String> roleNames() {
        return m_securityRoleDao.findAll().stream().map(SecurityRole::getName).sorted().collect(Collectors.toList());
    }
}
