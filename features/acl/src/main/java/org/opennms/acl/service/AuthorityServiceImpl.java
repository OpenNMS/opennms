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
import org.opennms.acl.repository.AuthorityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>AuthorityServiceImpl class.</p>
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
@Service("authorityService")
public class AuthorityServiceImpl implements AuthorityService {

    /** {@inheritDoc} */
    public List<AuthorityDTO> getGroupAuthorities(Integer id) {
        return authorityRepository.getGroupAuthorities(id);
    }

    /**
     * <p>getFreeAuthoritiesForGroup</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<AuthorityDTO> getFreeAuthoritiesForGroup() {
        return authorityRepository.getFreeAuthoritiesForGroup();
    }

    /**
     * <p>getAuthorities</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<AuthorityDTO> getAuthorities() {
        return authorityRepository.getAuthorities();
    }

    /** {@inheritDoc} */
    public boolean insertGroupAuthorities(Integer id, List<Integer> authorities) {
        authorityRepository.removeGroupFromAuthorities(id);
        return authorityRepository.saveAuthorities(id, authorities);
    }

    /** {@inheritDoc} */
    public List<AuthorityDTO> getFreeAuthorities(String username) {
        return authorityRepository.getFreeAuthorities(username);
    }

    /** {@inheritDoc} */
    public List<AuthorityDTO> getUserAuthorities(String username) {
        return authorityRepository.getUserAuthorities(username);
    }

    /**
     * <p>getTotalItemsNumber</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getTotalItemsNumber() {
        return getAuthoritiesNumber();
    }

    /** {@inheritDoc} */
    public AuthorityDTO getAuthority(Integer id) {
        return authorityRepository.getAuthority(id);
    }

    /** {@inheritDoc} */
    public List<AuthorityDTO> getAuthorities(Pager pager) {
        return authorityRepository.getAuthorities(pager);
    }

    /** {@inheritDoc} */
    public boolean removeAuthority(Integer id) {
        return authorityRepository.removeAuthority(id);
    }

    /** {@inheritDoc} */
    public boolean save(AuthorityDTO authority) {
        return authorityRepository.save(authority);
    }

    /**
     * <p>getAuthoritiesNumber</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getAuthoritiesNumber() {
        return authorityRepository.getAuthoritiesNumber();
    }

    /** {@inheritDoc} */
    public List<Integer> getIdItemsAuthority(Integer id) {
        return authorityRepository.getIdItemsAuthority(id);
    }

    /**
     * <p>Setter for the field <code>authorityRepository</code>.</p>
     *
     * @param authorityRepository a {@link org.opennms.acl.repository.AuthorityRepository} object.
     */
    @Autowired
    public void setAuthorityRepository(AuthorityRepository authorityRepository) {
        this.authorityRepository = authorityRepository;
    }

    private AuthorityRepository authorityRepository;
}
