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

import org.opennms.acl.model.GroupDTO;
import org.opennms.acl.model.Pager;

/**
 * Contract to insert/update/read/delete the groups
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
public interface GroupRepository {

    /**
     * Save a GroupDTO
     *
     * @param group a {@link org.opennms.acl.model.GroupDTO} object.
     * @return the result of the operation
     */
    public Boolean save(GroupDTO group);

    /**
     * Retrieve a group by id
     *
     * @param id a {@link java.lang.Integer} object.
     * @return group
     */
    public GroupDTO getGroup(Integer id);

    /**
     * Remove a group by id
     *
     * @param id a {@link java.lang.Integer} object.
     * @return the result of the operation
     */
    public Boolean removeGroup(Integer id);

    /**
     * <p>getGroups</p>
     *
     * @param pager a {@link org.opennms.acl.model.Pager} object.
     * @return paginated list of groups
     */
    public List<GroupDTO> getGroups(Pager pager);

    /**
     * <p>getGroups</p>
     *
     * @return list of all authorities
     */
    public List<GroupDTO> getGroups();

    /**
     * <p>getGroupsNumber</p>
     *
     * @return numbers of authorities present in the system
     */
    public Integer getGroupsNumber();

    /**
     * <p>getUserGroups</p>
     *
     * @param username a {@link java.lang.String} object.
     * @return the list of user's authorities by username
     */
    public List<GroupDTO> getUserGroups(String username);

    /**
     * <p>getUserGroupsWithAutorities</p>
     *
     * @param username a {@link java.lang.String} object.
     * @return the list of user's authorities by username
     */
    public List<GroupDTO> getUserGroupsWithAutorities(String username);

    /**
     * <p>getFreeGroups</p>
     *
     * @param username a {@link java.lang.String} object.
     * @return the list of authorities that user doesn't have
     */
    public List<GroupDTO> getFreeGroups(String username);

    /**
     * Save a list of authorities for a given user
     *
     * @param username a {@link java.lang.String} object.
     * @param groups a {@link java.util.List} object.
     * @return the result of operation
     */
    public Boolean saveGroups(String username, List<Integer> groups);

    /**
     * Delete all user's authorities
     *
     * @param username a {@link java.lang.String} object.
     * @return the result of the operation
     */
    public Boolean deleteUserGroups(String username);

    /**
     * <p>hasUsers</p>
     *
     * @param id a {@link java.lang.Integer} object.
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean hasUsers(Integer id);

}
