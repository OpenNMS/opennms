/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
