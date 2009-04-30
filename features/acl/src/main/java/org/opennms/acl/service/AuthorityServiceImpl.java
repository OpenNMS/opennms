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
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 */
@Service("authorityService")
public class AuthorityServiceImpl implements AuthorityService {

    public List<AuthorityDTO> getGroupAuthorities(Integer id) {
        return authorityRepository.getGroupAuthorities(id);
    }

    public List<AuthorityDTO> getFreeAuthoritiesForGroup() {
        return authorityRepository.getFreeAuthoritiesForGroup();
    }

    public List<AuthorityDTO> getAuthorities() {
        return authorityRepository.getAuthorities();
    }

    public boolean insertGroupAuthorities(Integer id, List<Integer> authorities) {
        authorityRepository.removeGroupFromAuthorities(id);
        return authorityRepository.saveAuthorities(id, authorities);
    }

    public List<AuthorityDTO> getFreeAuthorities(String username) {
        return authorityRepository.getFreeAuthorities(username);
    }

    public List<AuthorityDTO> getUserAuthorities(String username) {
        return authorityRepository.getUserAuthorities(username);
    }

    public Integer getTotalItemsNumber() {
        return getAuthoritiesNumber();
    }

    public AuthorityDTO getAuthority(Integer id) {
        return authorityRepository.getAuthority(id);
    }

    public List<AuthorityDTO> getAuthorities(Pager pager) {
        return authorityRepository.getAuthorities(pager);
    }

    public boolean removeAuthority(Integer id) {
        return authorityRepository.removeAuthority(id);
    }

    public boolean save(AuthorityDTO authority) {
        return authorityRepository.save(authority);
    }

    public Integer getAuthoritiesNumber() {
        return authorityRepository.getAuthoritiesNumber();
    }

    public List<Integer> getIdItemsAuthority(Integer id) {
        return authorityRepository.getIdItemsAuthority(id);
    }

    @Autowired
    public void setAuthorityRepository(AuthorityRepository authorityRepository) {
        this.authorityRepository = authorityRepository;
    }

    private AuthorityRepository authorityRepository;
}