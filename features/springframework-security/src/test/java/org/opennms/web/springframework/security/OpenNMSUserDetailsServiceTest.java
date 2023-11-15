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
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class OpenNMSUserDetailsServiceTest {
	private SpringSecurityUserDao userDao = mock(SpringSecurityUserDao.class);
	private OpenNMSUserDetailsService detailsService;

	@Before
	public void setUp() throws Exception {
	    userDao = mock(SpringSecurityUserDao.class);
	    detailsService = new OpenNMSUserDetailsService(userDao);
	}

	@After
	public void tearDown() throws Exception {
        verifyNoMoreInteractions(userDao);
	}

	@Test
	public void testDaoGetter() {
		assertEquals("getUsersDao returned what we passed to setUsersDao", userDao, detailsService.getUserDao());

        verifyNoMoreInteractions(userDao);
	}

	@Test
	public void testGetUser() {
		SpringSecurityUser user = new SpringSecurityUser(new OnmsUser());
		when(userDao.getByUsername("test_user")).thenReturn(user);

		UserDetails userDetails = detailsService.loadUserByUsername("test_user");
		
		verify(userDao).getByUsername("test_user");

		assertNotNull("user object from DAO not null", userDetails);
		assertEquals("user objects", user, userDetails);

		verifyNoMoreInteractions(userDao);
	}

	@Test
	public void testGetUnknownUser() {
		when(userDao.getByUsername("test_user")).thenReturn(null);
		
		ThrowableAnticipator ta = new ThrowableAnticipator();
		ta.anticipate(new UsernameNotFoundException("Unable to locate test_user in the userDao"));
		
		try {
			detailsService.loadUserByUsername("test_user");
		} catch (Throwable t) {
			ta.throwableReceived(t);
		}

		verify(userDao).getByUsername("test_user");

		ta.verifyAnticipated();

		verifyNoMoreInteractions(userDao);
	}
}
