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

import org.opennms.acl.model.AuthorityDTO;
import org.opennms.acl.model.Pager;

/**
 * Contract to insert/update/read/delete the authorities
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
public interface AuthorityRepository {

    /**
     * Save an authorityDTO
     *
     * @param authority a {@link org.opennms.acl.model.AuthorityDTO} object.
     * @return the result of the operation
     */
    public Boolean save(AuthorityDTO authority);

    /**
     * Retrieve an authority by id
     *
     * @param id a {@link java.lang.Integer} object.
     * @return authority
     */
    public AuthorityDTO getAuthority(Integer id);

    /**
     * Remove an authority by id
     *
     * @param id a {@link java.lang.Integer} object.
     * @return the result of the operation
     */
    public Boolean removeAuthority(Integer id);

    /**
     * <p>getAuthorities</p>
     *
     * @param pager a {@link org.opennms.acl.model.Pager} object.
     * @return paginated list of authorities
     */
    public List<AuthorityDTO> getAuthorities(Pager pager);

    /**
     * <p>getAuthorities</p>
     *
     * @return list of all authorities
     */
    public List<AuthorityDTO> getAuthorities();

    /**
     * <p>getAuthoritiesNumber</p>
     *
     * @return numbers of authorities present in the system
     */
    public Integer getAuthoritiesNumber();

    /**
     * <p>getUserAuthorities</p>
     *
     * @param username a {@link java.lang.String} object.
     * @return the list of user's authorities by username
     */
    public List<AuthorityDTO> getUserAuthorities(String username);

    /**
     * <p>getFreeAuthorities</p>
     *
     * @param username a {@link java.lang.String} object.
     * @return the list of authorities that user doesn't have
     */
    public List<AuthorityDTO> getFreeAuthorities(String username);

    /**
     * <p>getFreeAuthoritiesForGroup</p>
     *
     * @return the list of authorities that Group doesn't have
     */
    public List<AuthorityDTO> getFreeAuthoritiesForGroup();

    /**
     * Save a list of authorities for a given group
     *
     * @param authorities a {@link java.util.List} object.
     * @return the result of operation
     * @param id a {@link java.lang.Integer} object.
     */
    public Boolean saveAuthorities(Integer id, List<Integer> authorities);

    /**
     * Delete all user's authorities
     * 
     * @param username
     * @return the result of the operation
     */
    // public Boolean deleteUserAuthorities(String username);
    /**
     * Delete all user's groups
     *
     * @param username a {@link java.lang.String} object.
     * @return the result of the operation
     */
    public Boolean deleteUserGroups(String username);

    /**
     * <p>getIdItemsAuthority</p>
     *
     * @param id a {@link java.lang.Integer} object.
     * @return a {@link java.util.List} object.
     */
    public List<Integer> getIdItemsAuthority(Integer id);

    /**
     * <p>getGroupAuthorities</p>
     *
     * @param id a {@link java.lang.Integer} object.
     * @return a {@link java.util.List} object.
     */
    public List<AuthorityDTO> getGroupAuthorities(Integer id);

    /**
     * <p>removeGroupFromAuthorities</p>
     *
     * @param id a {@link java.lang.Integer} object.
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean removeGroupFromAuthorities(Integer id);

}
