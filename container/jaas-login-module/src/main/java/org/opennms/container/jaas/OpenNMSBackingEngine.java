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
package org.opennms.container.jaas;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.karaf.jaas.boot.principal.GroupPrincipal;
import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.apache.karaf.jaas.modules.BackingEngine;

/**
 * <p>This {@link BackingEngine} maps the {@code admin} user to the
 * following roles:</p>
 * <ul>
 * <li>admin</li>
 * <li>manager</li>
 * <li>viewer</li>
 * <li>systembundles</li>
 * </ul>
 */
public class OpenNMSBackingEngine implements BackingEngine {

	@Override
	public void addUser(String username, String password) {
		throw new UnsupportedOperationException("Cannot add users to this JAAS module");
	}

	@Override
	public void deleteUser(String username) {
		throw new UnsupportedOperationException("Cannot delete users from this JAAS module");
	}

	@Override
	public List<UserPrincipal> listUsers() {
		return Collections.singletonList(new UserPrincipal("admin"));
	}

	@Override
	public UserPrincipal lookupUser(String s) {
		if ("admin".equals(s)) {
			return new UserPrincipal("admin");
		} else {
			return null;
		}
	}

	@Override
	public List<GroupPrincipal> listGroups(UserPrincipal user) {
		return Collections.emptyList();
	}

	@Override
	public Map<GroupPrincipal, String> listGroups() {
		return Collections.emptyMap();
	}

	@Override
	public void addGroup(String username, String group) {
		throw new UnsupportedOperationException("Cannot add groups to this JAAS module");
	}

	@Override
	public void createGroup(String group) {
		throw new UnsupportedOperationException("Cannot create groups in this JAAS module");
	}

	@Override
	public void deleteGroup(String username, String group) {
		throw new UnsupportedOperationException("Cannot delete groups from this JAAS module");
	}

	@Override
	public List<RolePrincipal> listRoles(Principal principal) {
		return Arrays.asList(
			new RolePrincipal("admin"),
			new RolePrincipal("manager"),
			new RolePrincipal("viewer"),
			new RolePrincipal("systembundles")
		);
	}

	@Override
	public void addRole(String username, String role) {
		throw new UnsupportedOperationException("Cannot add roles to this JAAS module");
	}

	@Override
	public void deleteRole(String username, String role) {
		throw new UnsupportedOperationException("Cannot delete roles from this JAAS module");
	}

	@Override
	public void addGroupRole(String group, String role) {
		throw new UnsupportedOperationException("Cannot add group roles to this JAAS module");
	}

	@Override
	public void deleteGroupRole(String group, String role) {
		throw new UnsupportedOperationException("Cannot delete group roles from this JAAS module");
	}
}
