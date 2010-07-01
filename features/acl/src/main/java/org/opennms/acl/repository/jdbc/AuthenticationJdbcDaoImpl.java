//============================================================================
//
// Copyright (c) 2009+ desmax74
// Copyright (c) 2009+ The OpenNMS Group, Inc.
// All rights reserved everywhere.
//
// This program was developed and is maintained by Rocco RIONERO
// ("the author") and is subject to dual-copyright according to
// the terms set in "The OpenNMS Project Contributor Agreement".
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
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
// USA.
//
// The author can be contacted at the following email address:
//
//       Massimiliano Dess&igrave;
//       desmax74@yahoo.it
//
//
//-----------------------------------------------------------------------------
// OpenNMS Network Management System is Copyright by The OpenNMS Group, Inc.
//============================================================================
package org.opennms.acl.repository.jdbc;

import java.util.HashMap;
import java.util.Map;

import org.opennms.acl.domain.AclUser;
import org.opennms.acl.exception.RepositoryException;
import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.security.userdetails.jdbc.JdbcDaoImpl;

/**
 * Used by Spring security to perform authentication
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
public class AuthenticationJdbcDaoImpl extends JdbcDaoImpl {

    /**
     * {@inheritDoc}
     *
     * Load the user detail in the authentication phase
     */
    public UserDetails loadUserByUsername(String username) {
        try {
            UserDetails user = super.loadUserByUsername(username);
            Map<String, Object> userInfo = new HashMap<String, Object>();
            // put in userInfo your custom objects
            return new AclUser(user.getUsername(), user.getPassword(), user.isEnabled(), user.getAuthorities(), userInfo);
        } catch (UsernameNotFoundException userEx) {
            throw new UsernameNotFoundException("Username not found:" + username);
        } catch (DataAccessException dataEx) {
            throw new RepositoryException(dataEx.getMessage());
        }
    }
}
