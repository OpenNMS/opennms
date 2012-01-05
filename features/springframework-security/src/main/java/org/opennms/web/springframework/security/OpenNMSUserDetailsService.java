/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
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

package org.opennms.web.springframework.security;

import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

/**
 * <p>OpenNMSUserDetailsService class.</p>
 */
public class OpenNMSUserDetailsService implements UserDetailsService {
	private SpringSecurityUserDao m_userDao;
	
	/** {@inheritDoc} */
	public UserDetails loadUserByUsername(String username)
		throws UsernameNotFoundException, DataAccessException {
		if (m_userDao == null) {
			// XXX there must be a better way to do this
			throw new IllegalStateException("usersDao parameter must be set to a UsersDao bean");
		}
		
		UserDetails userDetails = m_userDao.getByUsername(username);
		
		if (userDetails == null) {
			throw new UsernameNotFoundException("User test_user is not a valid user");
		}
		
		return userDetails;
	}

	/**
	 * <p>setUserDao</p>
	 *
	 * @param userDao a {@link org.opennms.web.springframework.security.SpringSecurityUserDao} object.
	 */
	public void setUserDao(SpringSecurityUserDao userDao) {
		m_userDao = userDao;
		
	}

	/**
	 * <p>getUserDao</p>
	 *
	 * @return a {@link org.opennms.web.springframework.security.SpringSecurityUserDao} object.
	 */
	public SpringSecurityUserDao getUserDao() {
		return m_userDao;
	}
}
