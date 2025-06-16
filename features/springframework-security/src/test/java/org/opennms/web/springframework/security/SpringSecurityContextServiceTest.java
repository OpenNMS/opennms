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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.web.api.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class SpringSecurityContextServiceTest {

	private SpringSecurityContextService m_securityContextService;
	private final GrantedAuthority ROLE_USER = new SimpleGrantedAuthority(
			Authentication.ROLE_USER);
	private final GrantedAuthority ROLE_ADMIN = new SimpleGrantedAuthority(
			Authentication.ROLE_ADMIN);
	private final GrantedAuthority ROLE_PROVISION = new SimpleGrantedAuthority(
			Authentication.ROLE_PROVISION);
	private final GrantedAuthority ROLE_ANONYMOUS = new SimpleGrantedAuthority(
			"ROLE_ANONYMOUS");
	private final GrantedAuthority ROLE_DASHBOARD = new SimpleGrantedAuthority(
			Authentication.ROLE_DASHBOARD);

	private final String USERNAME = "opennms";

	private final String PASS = "r0c|<Z";

	@Before
	public void setUp() throws Exception {
		SecurityContext context = new SecurityContextImpl();
		User principal = new User(USERNAME, PASS, true, true, true, true,
				Arrays.asList(new GrantedAuthority[] { ROLE_ADMIN, ROLE_PROVISION }));
		org.springframework.security.core.Authentication auth = new PreAuthenticatedAuthenticationToken(
				principal, new Object());
		context.setAuthentication(auth);
		SecurityContextHolder.setContext(context);
		this.m_securityContextService = new SpringSecurityContextService();
	}

	@After
	public void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void testUserCredentials() {
		assertTrue("Check if user name is opennms.",
				"opennms".equals(this.m_securityContextService
						.getUsername()));
		assertFalse("Check if unknown is a not valid user name.",
				"unknown".equals(this.m_securityContextService
						.getUsername()));
		assertTrue("Check if password is correct.",
				PASS.equals(this.m_securityContextService.getPassword()));
		assertFalse("Check if wrong_pass is not correct.",
				"wrong_pass".equals(PASS));
	}

	@Test
	public void testUserRoles() {
		assertTrue("Check if user is in " + ROLE_ADMIN,
				this.m_securityContextService.hasRole(ROLE_ADMIN
						.toString()));
		assertTrue("Check if user is in " + ROLE_PROVISION,
				this.m_securityContextService.hasRole(ROLE_PROVISION
						.toString()));
		assertFalse("Check if user is not in " + ROLE_USER,
				this.m_securityContextService.hasRole(ROLE_USER
						.toString()));
		assertFalse("Check if user is not in " + ROLE_ANONYMOUS,
				this.m_securityContextService.hasRole(ROLE_ANONYMOUS
						.toString()));
		assertFalse("Check if user is not in " + ROLE_DASHBOARD,
				this.m_securityContextService.hasRole(ROLE_DASHBOARD
						.toString()));
	}
}
