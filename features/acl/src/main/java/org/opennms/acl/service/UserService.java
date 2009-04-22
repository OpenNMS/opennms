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
//       Massimiliano Dess“
//       desmax74@yahoo.it
//
//
//-----------------------------------------------------------------------------
// OpenNMS Network Management System is Copyright by The OpenNMS Group, Inc.
//============================================================================
package org.opennms.acl.service;

import java.util.List;

import org.opennms.acl.model.Pager;
import org.opennms.acl.model.UserAuthoritiesDTO;
import org.opennms.acl.model.UserDTO;
import org.opennms.acl.model.UserDTOLight;
import org.opennms.acl.model.UserView;

/**
 * @author Massimiliano Dessi (desmax74@yahoo.it)
 * @since jdk 1.5.0
 */
public interface UserService extends PagerService {

    public List<UserDTOLight> getEnabledUsers(Pager pager);

    /**
     * Retrieve the disabled users
     * 
     * @param pager
     * @return
     */
    public List<UserDTOLight> getDisabledUsers(Pager pager);

    /**
     * Retrieve the id of user by username
     * 
     * @param username
     * @return the unique identifier
     */
    public Object getIdUser(String username);

    /**
     * disable a user
     * 
     * @param id
     * @return the outcome of disable operation
     */
    public Boolean disableUser(String id);

    /**
     * retrieve a user
     * 
     * @param id
     * @return a {@link UserView}
     */
    public UserView getUser(String id);

    /**
     * retrieve a user with credentials
     * 
     * @param id
     * @return a {@link UserView}
     */
    public UserDTO getUserCredentials(String id);

    /**
     * retrieve a user with their authorities
     * 
     * @param username
     * @return {@link UserAuthoritiesDTO} with their roles
     */
    public UserAuthoritiesDTO getUserWithAuthorities(String username);

    /**
     * retrieve a user with their id
     * 
     * @param username
     * @return {@link UserAuthoritiesDTO} with their authorities
     */
    public UserAuthoritiesDTO getUserWithAuthoritiesByID(Integer sid);

    /**
     * @return user's number
     */
    public Integer getUsersNumber();

    /**
     * Method only for admin, insert a user or change the user password
     * 
     * @param user
     * @return
     */
    public boolean save(UserDTO user);

    public boolean save(UserAuthoritiesDTO user);
}
