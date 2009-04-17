//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.web.springframework.security;

import org.opennms.test.ThrowableAnticipator;
import org.springframework.security.Authentication;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.providers.dao.DaoAuthenticationProvider;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;


public class AuthenticationIntegrationTest extends AbstractDependencyInjectionSpringContextTests {
	private DaoAuthenticationProvider m_provider; 

	@Override
	protected String[] getConfigLocations() {
		return new String[] {
                "org/opennms/web/springframework.security/applicationContext-authenticationIntegrationTest.xml"
        		};
	}
	
	public void setDaoAuthenticationProvider(DaoAuthenticationProvider provider) {
		m_provider = provider;
	}
	public DaoAuthenticationProvider getDaoAuthenticationProvider() {
		return m_provider;
	}
	
	public void testAuthenticateAdmin() {
	    Authentication authentication = new UsernamePasswordAuthenticationToken("admin", "admin");
		Authentication authenticated = m_provider.authenticate(authentication);
		assertNotNull("authenticated Authentication object not null", authenticated);
		GrantedAuthority[] authorities = authenticated.getAuthorities();
		assertNotNull("GrantedAuthorities should not be null", authorities);
		assertEquals("GrantedAuthorities size", 2, authorities.length);
		assertEquals("GrantedAuthorities zero role", "ROLE_USER", authorities[0].getAuthority());
		assertEquals("GrantedAuthorities two name", "ROLE_ADMIN", authorities[1].getAuthority());
	}
	
	public void testAuthenticateRtc() {
		Authentication authentication = new UsernamePasswordAuthenticationToken("rtc", "rtc");
		Authentication authenticated = m_provider.authenticate(authentication);
		assertNotNull("authenticated Authentication object not null", authenticated);
		GrantedAuthority[] authorities = authenticated.getAuthorities();
		assertNotNull("GrantedAuthorities should not be null", authorities);
		assertEquals("GrantedAuthorities size", 1, authorities.length);
		assertEquals("GrantedAuthorities one name", "ROLE_RTC", authorities[0].getAuthority());
	}
	
	public void testAuthenticateTempUser() {
		Authentication authentication = new UsernamePasswordAuthenticationToken("tempuser", "mike");
		Authentication authenticated = m_provider.authenticate(authentication);
		assertNotNull("authenticated Authentication object not null", authenticated);
		GrantedAuthority[] authorities = authenticated.getAuthorities();
		assertNotNull("GrantedAuthorities should not be null", authorities);
		assertEquals("GrantedAuthorities size", 1, authorities.length);
		assertEquals("GrantedAuthorities zero role", "ROLE_USER", authorities[0].getAuthority());
	}
	
	public void testAuthenticateBadUsername() {
		Authentication authentication = new UsernamePasswordAuthenticationToken("badUsername", "admin");
		
		ThrowableAnticipator ta = new ThrowableAnticipator();
		ta.anticipate(new BadCredentialsException("Bad credentials"));
		try {
			m_provider.authenticate(authentication);
		} catch (Throwable t) {
			ta.throwableReceived(t);
		}
		ta.verifyAnticipated();
	}
	
	public void testAuthenticateBadPassword() {
		Authentication authentication = new UsernamePasswordAuthenticationToken("admin", "badPassword");

		ThrowableAnticipator ta = new ThrowableAnticipator();
		ta.anticipate(new BadCredentialsException("Bad credentials"));
		try {
			m_provider.authenticate(authentication);
		} catch (Throwable t) {
			ta.throwableReceived(t);
		}
		ta.verifyAnticipated();
	}
}
