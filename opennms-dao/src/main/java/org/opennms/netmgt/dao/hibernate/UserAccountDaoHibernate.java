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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.model.usermgmt.OnCallRole;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.netmgt.model.usermgmt.UserGroup;
import org.springframework.dao.DataAccessException;

public class UserAccountDaoHibernate extends AbstractDaoHibernate<UserAccount, Integer> implements UserAccountDao {

    public UserAccountDaoHibernate() {
        super(UserAccount.class);
    }

    @Override
    public UserAccount findByUsername(final String username) {
        return findUnique("from UserAccount u where u.username = ?1", username);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<UserAccount> findByUsernames(final Collection<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return Collections.emptyList();
        }
        return getHibernateTemplate().execute(session -> session
                .createQuery("from UserAccount u where u.username in (:usernames) order by u.username")
                .setParameterList("usernames", usernames)
                .list());
    }

    @Override
    public List<UserAccount> findBySecurityRole(final String roleName) {
        return find("select u from UserAccount u join u.securityRoles r where r.name = ?1 order by u.username", roleName);
    }

    @Override
    public List<UserAccount> findAllOrderByUsername() {
        return find("from UserAccount u order by u.username");
    }

    /**
     * Takes the user out of every group and on-call schedule list first. The database would cascade those
     * rows away too, but behind Hibernate's back, leaving gaps in the ordered lists that load as nulls.
     */
    @Override
    public void delete(final UserAccount user) throws DataAccessException {
        final Integer id = user.getId();
        for (final UserGroup group : findObjects(UserGroup.class,
                "select distinct g from UserGroup g join g.members m where m.id = ?1", id)) {
            group.getMembers().removeIf(m -> Objects.equals(m.getId(), id));
        }
        for (final OnCallRole role : findObjects(OnCallRole.class,
                "select distinct r from OnCallRole r join r.schedules s where s.user.id = ?1", id)) {
            role.getSchedules().removeIf(s -> Objects.equals(s.getUser().getId(), id));
        }
        // write those out before the user's delete: orphaned schedules are only deleted at flush time, after
        // the user's delete, which would first null their user_id (not null) and fail
        flush();
        super.delete(user);
    }

    @Override
    public void deleteAll(final Collection<UserAccount> users) throws DataAccessException {
        users.forEach(this::delete);
    }
}
