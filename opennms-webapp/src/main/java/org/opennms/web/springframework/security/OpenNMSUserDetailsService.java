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

import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

public class OpenNMSUserDetailsService implements UserDetailsService {
	private UserDao m_userDao;
	
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

	public void setUserDao(UserDao userDao) {
		m_userDao = userDao;
		
	}

	public UserDao getUserDao() {
		return m_userDao;
	}
}
