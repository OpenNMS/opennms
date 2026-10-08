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
package org.opennms.netmgt.dao.hibernate;

import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.dao.api.SecurityRoleDao;
import org.opennms.netmgt.model.usermgmt.SecurityRole;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.springframework.dao.DataAccessException;

public class SecurityRoleDaoHibernate extends AbstractDaoHibernate<SecurityRole, Integer> implements SecurityRoleDao {

    public SecurityRoleDaoHibernate() {
        super(SecurityRole.class);
    }

    @Override
    public SecurityRole findByName(final String name) {
        return findUnique("from SecurityRole r where r.name = ?1", name);
    }

    @Override
    public List<SecurityRole> findBuiltin() {
        return find("from SecurityRole r where r.builtin = true order by r.name");
    }

    @Override
    public List<SecurityRole> findUserDefined() {
        return find("from SecurityRole r where r.builtin = false order by r.name");
    }

    @Override
    public boolean deleteUserDefined(final String name) {
        final SecurityRole role = findByName(name);
        if (role == null) {
            return false;
        }
        checkNotBuiltin(role);
        // revoke it through the users, so users already in the session do not keep a deleted role
        for (final UserAccount user : findObjects(UserAccount.class,
                "select u from UserAccount u join u.securityRoles r where r = ?1", role)) {
            user.getSecurityRoles().remove(role);
        }
        delete(role);
        return true;
    }

    /** Refuses a built-in role, so no delete path can remove one. */
    @Override
    public void delete(final SecurityRole role) throws DataAccessException {
        checkNotBuiltin(role);
        super.delete(role);
    }

    @Override
    public void deleteAll(final Collection<SecurityRole> roles) throws DataAccessException {
        roles.forEach(this::checkNotBuiltin);
        super.deleteAll(roles);
    }

    private void checkNotBuiltin(final SecurityRole role) {
        if (role != null && role.isBuiltin()) {
            throw new IllegalArgumentException("The built-in security role " + role.getName() + " cannot be deleted");
        }
    }
}
