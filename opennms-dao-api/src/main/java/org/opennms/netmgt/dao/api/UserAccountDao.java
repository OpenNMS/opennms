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
package org.opennms.netmgt.dao.api;

import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.model.usermgmt.UserAccount;

/**
 * Users. Delete them through this DAO: {@link #delete(Object)} also takes the user out of the groups and
 * on-call schedules that list them, keeping those lists free of gaps. Deleting a user who supervises an
 * on-call role fails, the role must be changed first.
 */
public interface UserAccountDao extends OnmsDao<UserAccount, Integer> {

    /** @param username matched case-sensitively */
    UserAccount findByUsername(String username);

    /** @return the users among those names, by username; names without a user are ignored */
    List<UserAccount> findByUsernames(Collection<String> usernames);

    /** @param roleName the full authority name, {@code ROLE_ADMIN} */
    List<UserAccount> findBySecurityRole(String roleName);

    List<UserAccount> findAllOrderByUsername();
}
