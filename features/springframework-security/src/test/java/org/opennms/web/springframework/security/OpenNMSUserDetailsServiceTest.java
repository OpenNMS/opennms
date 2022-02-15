/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class OpenNMSUserDetailsServiceTest {
	
	@Test
	public void testDaoSetter() {
		SpringSecurityUserDao userDao = mock(SpringSecurityUserDao.class);
		OpenNMSUserDetailsService detailsService = new OpenNMSUserDetailsService();
		
		detailsService.setUserDao(userDao);
	}

	@Test
	public void testDaoGetter() {
		SpringSecurityUserDao userDao = mock(SpringSecurityUserDao.class);
		OpenNMSUserDetailsService detailsService = new OpenNMSUserDetailsService();
		detailsService.setUserDao(userDao);
		assertEquals("getUsersDao returned what we passed to setUsersDao", userDao, detailsService.getUserDao());
	}

	@Test
	public void testGetUser() {
		SpringSecurityUserDao userDao = mock(SpringSecurityUserDao.class);
		OpenNMSUserDetailsService detailsService = new OpenNMSUserDetailsService();
		detailsService.setUserDao(userDao);

		SpringSecurityUser user = new SpringSecurityUser(new OnmsUser());
		when(userDao.getByUsername("test_user")).thenReturn(user);

		UserDetails userDetails = detailsService.loadUserByUsername("test_user");
		
		verify(userDao);
		
		assertNotNull("user object from DAO not null", userDetails);
		assertEquals("user objects", user, userDetails);
	}

	@Test
	public void testGetUnknownUser() {
		SpringSecurityUserDao userDao = mock(SpringSecurityUserDao.class);
		OpenNMSUserDetailsService detailsService = new OpenNMSUserDetailsService();
		detailsService.setUserDao(userDao);
		
		when(userDao.getByUsername("test_user")).thenReturn(null);
		
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
