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
