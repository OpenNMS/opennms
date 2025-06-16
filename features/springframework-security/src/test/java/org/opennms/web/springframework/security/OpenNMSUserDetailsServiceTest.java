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
