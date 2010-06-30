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
package org.opennms.acl.service;

import java.util.List;

import org.opennms.acl.model.AuthorityDTO;
import org.opennms.acl.model.Pager;

/**
 * <p>AuthorityService interface.</p>
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
public interface AuthorityService extends PagerService {

    /**
     * store an authority
     *
     * @param authority a {@link org.opennms.acl.model.AuthorityDTO} object.
     * @return outcome of the store action
     */
    public boolean save(AuthorityDTO authority);

    /**
     * <p>getAuthority</p>
     *
     * @param id a {@link java.lang.Integer} object.
     * @return authority
     */
    public AuthorityDTO getAuthority(Integer id);

    /**
     * <p>removeAuthority</p>
     *
     * @param id a {@link java.lang.Integer} object.
     * @return the result of removal
     */
    public boolean removeAuthority(Integer id);

    /**
     * <p>getAuthorities</p>
     *
     * @param pager a {@link org.opennms.acl.model.Pager} object.
     * @return paginated list authorities
     */
    public List<AuthorityDTO> getAuthorities(Pager pager);

    /**
     * <p>getAuthorities</p>
     *
     * @return list of all authorities
     */
    public List<AuthorityDTO> getAuthorities();

    /**
     * <p>getUserAuthorities</p>
     *
     * @param username a {@link java.lang.String} object.
     * @return the list of user's authorities
     */
    public List<AuthorityDTO> getUserAuthorities(String username);

    /**
     * <p>getFreeAuthorities</p>
     *
     * @param username a {@link java.lang.String} object.
     * @return unallocated authorities
     */
    public List<AuthorityDTO> getFreeAuthorities(String username);

    /**
     * <p>getFreeAuthoritiesForGroup</p>
     *
     * @return the list of authorities that Group doesn't have
     */
    public List<AuthorityDTO> getFreeAuthoritiesForGroup();

    /**
     * <p>getGroupAuthorities</p>
     *
     * @return the list of authorities that Group have
     * @param id a {@link java.lang.Integer} object.
     */
    public List<AuthorityDTO> getGroupAuthorities(Integer id);

    /**
     * <p>getIdItemsAuthority</p>
     *
     * @param id a {@link java.lang.Integer} object.
     * @return list of items id
     */
    public List<Integer> getIdItemsAuthority(Integer id);

    /**
     * <p>getAuthoritiesNumber</p>
     *
     * @return authorities number
     */
    public Integer getAuthoritiesNumber();

    /**
     * Insert a list of authorities assigned to a group
     *
     * @param authorities a {@link java.util.List} object.
     * @return result of
     * @param id a {@link java.lang.Integer} object.
     */
    public boolean insertGroupAuthorities(Integer id, List<Integer> authorities);
}
