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
package org.opennms.acl.repository;

import java.util.List;

import org.opennms.acl.model.Pager;
import org.opennms.acl.model.UserAuthoritiesDTO;
import org.opennms.acl.model.UserDTO;
import org.opennms.acl.model.UserDTOLight;
import org.opennms.acl.model.UserView;

/**
 * Contract to insert/update/read/delete acl users
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
public interface UserRepository {

    /**
     * <p>getIdUser</p>
     *
     * @param username a {@link java.lang.String} object.
     * @return the unique identifier user.
     */
    public Object getIdUser(String username);

    /**
     * <p>getUserCredentials</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return the user to update its credentials
     */
    public UserDTO getUserCredentials(String id);

    /**
     * disable user by id
     *
     * @param id a {@link java.lang.String} object.
     * @return the resutl of the operation
     */
    public Boolean disableUser(String id);

    /**
     * <p>getUser</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return a read only user
     */
    public UserView getUser(String id);

    /**
     * <p>getUserWithAuthorities</p>
     *
     * @param username a {@link java.lang.String} object.
     * @return user with its authorities
     */
    public UserAuthoritiesDTO getUserWithAuthorities(String username);

    /**
     * <p>getUserWithAuthoritiesByID</p>
     *
     * @return user with its authorities
     * @param sid a {@link java.lang.Integer} object.
     */
    public UserAuthoritiesDTO getUserWithAuthoritiesByID(Integer sid);

    /**
     * <p>getEnabledUsers</p>
     *
     * @param pager a {@link org.opennms.acl.model.Pager} object.
     * @return paginated list of enabled users
     */
    public List<UserDTOLight> getEnabledUsers(Pager pager);

    /**
     * <p>getDisabledUsers</p>
     *
     * @param pager a {@link org.opennms.acl.model.Pager} object.
     * @return paginated list of disabled users
     */
    public List<UserDTOLight> getDisabledUsers(Pager pager);

    /**
     * <p>getUsersNumber</p>
     *
     * @return number of users in the system
     */
    public Integer getUsersNumber();

    /**
     * <p>insertUser</p>
     *
     * @param user a {@link org.opennms.acl.model.UserDTO} object.
     * @return id of the inserted user
     */
    public Long insertUser(UserDTO user);

    /**
     * Update a user
     *
     * @param user a {@link org.opennms.acl.model.UserDTO} object.
     * @return the number of row updated
     */
    public Integer updatePassword(UserDTO user);

    /**
     * Save an UserDTO user
     *
     * @param user a {@link org.opennms.acl.model.UserAuthoritiesDTO} object.
     * @return the result of the operation
     */
    public Boolean save(UserAuthoritiesDTO user);
}
