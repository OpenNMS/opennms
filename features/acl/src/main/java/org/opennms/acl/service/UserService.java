/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
 *
 * From the original copyright headers:
 *
 * Copyright (c) 2009+ desmax74
 * Copyright (c) 2009+ The OpenNMS Group, Inc.
 *
 * This program was developed and is maintained by Rocco RIONERO
 * ("the author") and is subject to dual-copyright according to
 * the terms set in "The OpenNMS Project Contributor Agreement".
 *
 * The author can be contacted at the following email address:
 *
 *     Massimiliano Dess&igrave;
 *     desmax74@yahoo.it
 *******************************************************************************/

package org.opennms.acl.service;

import java.util.List;

import org.opennms.acl.model.Pager;
import org.opennms.acl.model.UserAuthoritiesDTO;
import org.opennms.acl.model.UserDTO;
import org.opennms.acl.model.UserDTOLight;
import org.opennms.acl.model.UserView;

/**
 * <p>UserService interface.</p>
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
public interface UserService extends PagerService {

    /**
     * <p>getEnabledUsers</p>
     *
     * @param pager a {@link org.opennms.acl.model.Pager} object.
     * @return a {@link java.util.List} object.
     */
    public List<UserDTOLight> getEnabledUsers(Pager pager);

    /**
     * Retrieve the disabled users
     *
     * @param pager a {@link org.opennms.acl.model.Pager} object.
     * @return a {@link java.util.List} object.
     */
    public List<UserDTOLight> getDisabledUsers(Pager pager);

    /**
     * Retrieve the id of user by username
     *
     * @param username a {@link java.lang.String} object.
     * @return the unique identifier
     */
    public Object getIdUser(String username);

    /**
     * disable a user
     *
     * @param id a {@link java.lang.String} object.
     * @return the outcome of disable operation
     */
    public Boolean disableUser(String id);

    /**
     * retrieve a user
     *
     * @param id a {@link java.lang.String} object.
     * @return a {@link UserView}
     */
    public UserView getUser(String id);

    /**
     * retrieve a user with credentials
     *
     * @param id a {@link java.lang.String} object.
     * @return a {@link UserView}
     */
    public UserDTO getUserCredentials(String id);

    /**
     * retrieve a user with their authorities
     *
     * @param username a {@link java.lang.String} object.
     * @return {@link UserAuthoritiesDTO} with their roles
     */
    public UserAuthoritiesDTO getUserWithAuthorities(String username);

    /**
     * retrieve a user with their id
     *
     * @return {@link UserAuthoritiesDTO} with their authorities
     * @param sid a {@link java.lang.Integer} object.
     */
    public UserAuthoritiesDTO getUserWithAuthoritiesByID(Integer sid);

    /**
     * <p>getUsersNumber</p>
     *
     * @return user's number
     */
    public Integer getUsersNumber();

    /**
     * Method only for admin, insert a user or change the user password
     *
     * @param user a {@link org.opennms.acl.model.UserDTO} object.
     * @return a boolean.
     */
    public boolean save(UserDTO user);

    /**
     * <p>save</p>
     *
     * @param user a {@link org.opennms.acl.model.UserAuthoritiesDTO} object.
     * @return a boolean.
     */
    public boolean save(UserAuthoritiesDTO user);
}
