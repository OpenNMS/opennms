/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.springframework.security;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import junit.framework.TestCase;

import org.opennms.netmgt.model.OnmsUser;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class OpenNMSUserDetailsServiceTest extends TestCase {
	
	public void testDaoSetter() {
		SpringSecurityUserDao userDao = createMock(SpringSecurityUserDao.class);
		OpenNMSUserDetailsService detailsService = new OpenNMSUserDetailsService();
		
		detailsService.setUserDao(userDao);
	}
	
	public void testDaoGetter() {
		SpringSecurityUserDao userDao = createMock(SpringSecurityUserDao.class);
		OpenNMSUserDetailsService detailsService = new OpenNMSUserDetailsService();
		detailsService.setUserDao(userDao);
		assertEquals("getUsersDao returned what we passed to setUsersDao", userDao, detailsService.getUserDao());
	}
	
	public void testGetUser() {
		SpringSecurityUserDao userDao = createMock(SpringSecurityUserDao.class);
		OpenNMSUserDetailsService detailsService = new OpenNMSUserDetailsService();
		detailsService.setUserDao(userDao);

		SpringSecurityUser user = new SpringSecurityUser(new OnmsUser());
		expect(userDao.getByUsername("test_user")).andReturn(user);

		replay(userDao);
		
		UserDetails userDetails = detailsService.loadUserByUsername("test_user");
		
		verify(userDao);
		
		assertNotNull("user object from DAO not null", userDetails);
		assertEquals("user objects", user, userDetails);
	}
	
	public void testGetUnknownUser() {
		SpringSecurityUserDao userDao = createMock(SpringSecurityUserDao.class);
		OpenNMSUserDetailsService detailsService = new OpenNMSUserDetailsService();
		detailsService.setUserDao(userDao);
		
		expect(userDao.getByUsername("test_user")).andReturn(null);
		
		replay(userDao);
		
		ThrowableAnticipator ta = new ThrowableAnticipator();
		ta.anticipate(new UsernameNotFoundException("Unable to locate test_user in the userDao"));
		
		try {
			detailsService.loadUserByUsername("test_user");
		} catch (Throwable t) {
			ta.throwableReceived(t);
		}
		verify(userDao);
		ta.verifyAnticipated();
	}
}
