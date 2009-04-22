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
package org.opennms.acl.repository;

import java.util.List;

import org.opennms.acl.model.AuthorityDTO;
import org.opennms.acl.model.Pager;

/**
 * Contract to insert/update/read/delete the authorities
 * 
 * @author Massimiliano Dessi (desmax74@yahoo.it)
 * @since jdk 1.5.0
 */
public interface AuthorityRepository {

    /**
     * Save an authorityDTO
     * 
     * @param authority
     * @return the result of the operation
     */
    public Boolean save(AuthorityDTO authority);

    /**
     * Retrieve an authority by id
     * 
     * @param id
     * @return authority
     */
    public AuthorityDTO getAuthority(Integer id);

    /**
     * Remove an authority by id
     * 
     * @param id
     * @return the result of the operation
     */
    public Boolean removeAuthority(Integer id);

    /**
     * @param pager
     * @return paginated list of authorities
     */
    public List<AuthorityDTO> getAuthorities(Pager pager);

    /**
     * @return list of all authorities
     */
    public List<AuthorityDTO> getAuthorities();

    /**
     * @return numbers of authorities present in the system
     */
    public Integer getAuthoritiesNumber();

    /**
     * @param username
     * @return the list of user's authorities by username
     */
    public List<AuthorityDTO> getUserAuthorities(String username);

    /**
     * @param username
     * @return the list of authorities that user doesn't have
     */
    public List<AuthorityDTO> getFreeAuthorities(String username);

    /**
     * @param username
     * @return the list of authorities that Group doesn't have
     */
    public List<AuthorityDTO> getFreeAuthoritiesForGroup();

    /**
     * Save a list of authorities for a given group
     * 
     * @param username
     * @param authorities
     * @return the result of operation
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
     * @param username
     * @return the result of the operation
     */
    public Boolean deleteUserGroups(String username);

    /**
     * @param id
     * @return
     */
    public List<Integer> getIdItemsAuthority(Integer id);

    public List<AuthorityDTO> getGroupAuthorities(Integer id);

    public Boolean removeGroupFromAuthorities(Integer id);

}
