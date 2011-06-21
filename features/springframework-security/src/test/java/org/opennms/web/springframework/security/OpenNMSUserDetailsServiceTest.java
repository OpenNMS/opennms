/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *     along with OpenNMS(R).  If not, see <http://www.gnu.org/licenses/>.
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

import org.opennms.test.ThrowableAnticipator;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UsernameNotFoundException;

public class OpenNMSUserDetailsServiceTest extends TestCase {
	
	public void testDaoSetter() {
		UserDao userDao = createMock(UserDao.class);
		OpenNMSUserDetailsService detailsService = new OpenNMSUserDetailsService();
		
		detailsService.setUserDao(userDao);
	}
	
	public void testDaoGetter() {
		UserDao userDao = createMock(UserDao.class);
		OpenNMSUserDetailsService detailsService = new OpenNMSUserDetailsService();
		detailsService.setUserDao(userDao);
		assertEquals("getUsersDao returned what we passed to setUsersDao", userDao, detailsService.getUserDao());
	}
	
	public void testLoadUserWithoutDao() {
		OpenNMSUserDetailsService detailsService = new OpenNMSUserDetailsService();
		ThrowableAnticipator ta = new ThrowableAnticipator();
		ta.anticipate(new IllegalStateException("usersDao parameter must be set to a UsersDao bean"));
		try {
			detailsService.loadUserByUsername("test_user");
		} catch (Throwable t) {
			ta.throwableReceived(t);
		}
		ta.verifyAnticipated();
	}
	
	public void testGetUser() {
		UserDao userDao = createMock(UserDao.class);
		OpenNMSUserDetailsService detailsService = new OpenNMSUserDetailsService();
		detailsService.setUserDao(userDao);
		
		User user = new User();
		expect(userDao.getByUsername("test_user")).andReturn(user);
		
		replay(userDao);
		
		UserDetails userDetails = detailsService.loadUserByUsername("test_user");
		
		verify(userDao);
		
		assertNotNull("user object from DAO not null", userDetails);
		assertEquals("user objects", user, userDetails);
	}
	
	public void testGetUnknownUser() {
		UserDao userDao = createMock(UserDao.class);
		OpenNMSUserDetailsService detailsService = new OpenNMSUserDetailsService();
		detailsService.setUserDao(userDao);
		
		expect(userDao.getByUsername("test_user")).andReturn(null);
		
		replay(userDao);
		
		ThrowableAnticipator ta = new ThrowableAnticipator();
		ta.anticipate(new UsernameNotFoundException("User test_user is not a valid user"));
		
		try {
			detailsService.loadUserByUsername("test_user");
		} catch (Throwable t) {
			ta.throwableReceived(t);
		}
		verify(userDao);
		ta.verifyAnticipated();
	}
}
