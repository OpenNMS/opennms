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

import java.util.List;

import org.opennms.netmgt.model.usermgmt.SecurityRole;

/**
 * The security roles. Every delete method refuses a built-in role with an {@link IllegalArgumentException};
 * the database itself does not block it.
 */
public interface SecurityRoleDao extends OnmsDao<SecurityRole, Integer> {

    /** @param name the full authority name, {@code ROLE_ADMIN} */
    SecurityRole findByName(String name);

    /** @return the built-in roles, by name */
    List<SecurityRole> findBuiltin();

    /** @return the custom roles, by name */
    List<SecurityRole> findUserDefined();

    /**
     * Deletes a custom role, revoking it from every user that holds it.
     *
     * @return false if there is no role of that name
     * @throws IllegalArgumentException if the role is built in
     */
    boolean deleteUserDefined(String name);
}
