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
package org.opennms.web.springframework.security;

import java.util.Collection;

import org.opennms.web.api.SecurityContextService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SpringSecurityContextService implements SecurityContextService {

	private SecurityContext m_context;
	
	public SpringSecurityContextService() {
		this.m_context = SecurityContextHolder.getContext();
	}
	
	@Override
	public String getUsername() {
		return getUserDetails().getUsername();
	}
	
	@Override
	public String getPassword() {
		return getUserDetails().getPassword();
	}
	
	private UserDetails getUserDetails() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	    if (principal instanceof UserDetails) {
	      return (UserDetails) principal;
	    } else {
	        throw new IllegalStateException("principal should always be instanceof UserDetails");
	    }
	}
	
	@Override
	public boolean hasRole(String role) {
		boolean hasRole = false;
		UserDetails userDetails = getUserDetails();
		if (userDetails != null) {

			Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
			if (isRolePresent(authorities, role)) {
				hasRole = true;
			}
		}
		return hasRole;
	}

	@Override
	public boolean isAuthenticated() {
		return this.m_context.getAuthentication().isAuthenticated();
	}
	
	/**
	 * Check if the currently logged in user is present in authorities of
	 * current user
	 * 
	 * @param authorities
	 *            - all assigned authorities
	 * @param role
	 *            - required role authority
	 * @return true if role is present, otherwise false
	 */
	private boolean isRolePresent(Collection<? extends GrantedAuthority> authorities, String role) {
		boolean isRolePresent = false;
		for (GrantedAuthority grantedAuthority : authorities) {
			isRolePresent = grantedAuthority.getAuthority().equals(role);
			if (isRolePresent)
				break;
		}
		return isRolePresent;
	}
}
