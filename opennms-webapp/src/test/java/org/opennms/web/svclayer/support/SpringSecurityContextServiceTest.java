/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.svclayer.support;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.context.SecurityContextImpl;
import org.springframework.security.providers.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.userdetails.User;

public class SpringSecurityContextServiceTest {

	private SpringSecurityContextService m_securityContextService;
	private final GrantedAuthority ROLE_USER = new GrantedAuthorityImpl(
			"ROLE_USER");
	private final GrantedAuthority ROLE_ADMIN = new GrantedAuthorityImpl(
			"ROLE_ADMIN");
	private final GrantedAuthority ROLE_PROVISION = new GrantedAuthorityImpl(
			"ROLE_PROVISION");
	private final GrantedAuthority ROLE_ANONYMOUS = new GrantedAuthorityImpl(
			"ROLE_ANONYMOUS");
	private final GrantedAuthority ROLE_DASHBOARD = new GrantedAuthorityImpl(
			"ROLE_DASHBOARD");

	private final String USERNAME = "opennms";

	private final String PASS = "r0c|<Z";

	@Before
	public void setUp() throws Exception {
		SecurityContext context = new SecurityContextImpl();
		User principal = new User(USERNAME, PASS, true, true, true, true,
				new GrantedAuthority[] { ROLE_ADMIN, ROLE_PROVISION });
		Authentication auth = new PreAuthenticatedAuthenticationToken(
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
