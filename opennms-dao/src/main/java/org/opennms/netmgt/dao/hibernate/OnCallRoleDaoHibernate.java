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

import java.util.List;

import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.model.usermgmt.OnCallRole;

public class OnCallRoleDaoHibernate extends AbstractDaoHibernate<OnCallRole, Integer> implements OnCallRoleDao {

    public OnCallRoleDaoHibernate() {
        super(OnCallRole.class);
    }

    @Override
    public OnCallRole findByName(final String name) {
        return findUnique("from OnCallRole r where r.name = ?1", name);
    }

    @Override
    public List<OnCallRole> findByMembershipGroup(final String groupName) {
        return find("from OnCallRole r where r.membershipGroup.name = ?1 order by r.name", groupName);
    }

    @Override
    public List<OnCallRole> findBySupervisor(final String username) {
        return find("from OnCallRole r where r.supervisor.username = ?1 order by r.name", username);
    }

    @Override
    public List<OnCallRole> findByScheduledUser(final String username) {
        return find("select distinct r from OnCallRole r join r.schedules s where s.user.username = ?1 order by r.name", username);
    }
}
