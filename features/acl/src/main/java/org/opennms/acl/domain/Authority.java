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
package org.opennms.acl.domain;

import java.io.Serializable;
import java.util.List;

import org.opennms.acl.model.AuthorityDTO;
import org.opennms.acl.model.AuthorityView;
import org.opennms.acl.model.Pager;
import org.opennms.acl.service.AclItemService;
import org.opennms.acl.service.AuthorityService;

/**
 * This entity class represent s an Authority (permission/authority/group/category/other...)
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
public class Authority implements Serializable {

    /**
     * Constructor used by AuthorityFactory
     *
     * @param authority
     * @param authorityService
     * @param authorityService a {@link org.opennms.acl.service.AuthorityService} object.
     * @param aclItemService a {@link org.opennms.acl.service.AclItemService} object.
     */
    public Authority(AuthorityDTO authority, AuthorityService authorityService, AclItemService aclItemService) {
        this.authority = authority;
        this.authorityService = authorityService;
        this.aclItemService = aclItemService;
    }

    /**
     * Return a paginated list of anemic authorities
     *
     * @param pager a {@link org.opennms.acl.model.Pager} object.
     * @return a {@link java.util.List} object.
     */
    public List<AuthorityDTO> getAuthorities(Pager pager) {
        return authorityService.getAuthorities(pager);
    }

    /**
     * Return a read only Autority
     *
     * @return authority
     */
    public AuthorityView getAuthorityView() {
        return authority;
    }

    /**
     * <p>hasItems</p>
     *
     * @return hasItems
     */
    public boolean hasItems() {
        return authority.getItems().size() > 0;
    }

    /**
     * Save the internal state of the Authority
     */
    public void save() {
        authorityService.save(authority);
    }

    /**
     * Overwrite the items assigned to this Autority
     *
     * @param items a {@link java.util.List} object.
     */
    public void setNewItems(List<?> items) {
        authority.setItems(items);
    }

    /**
     * Remove this Autority
     *
     * @return a boolean.
     */
    public boolean remove() {
        return authorityService.removeAuthority(authority.getId());
    }

    /**
     * Return the human readable description of this Authority
     *
     * @return description
     */
    public String getDescription() {
        return authority.getDescription();
    }

    /**
     * Authority unique identifier
     *
     * @return authority's identifier
     */
    public Integer getId() {
        return authority.getId();
    }

    /**
     * Return a list of all items manageable by authorities
     *
     * @return all items
     */
    public List<?> getAllItems() {
        return aclItemService.getItems();
    }

    /**
     * <p>getFreeItems</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<?> getFreeItems() {
        return aclItemService.getFreeItems(authorityService.getIdItemsAuthority(authority.getId()));
    }

    /**
     * Return a list of all items managed by this Authority
     *
     * @return authority items
     */
    public List<?> getItems() {
        authority.setItems(aclItemService.getAuthorityItems(authorityService.getIdItemsAuthority(authority.getId())));
        return authority.getItems();
    }

    /**
     * Return the name of the Authority
     *
     * @return name
     */
    public String getName() {
        return authority.getName();
    }

    private AuthorityDTO authority;
    private AuthorityService authorityService;
    private AclItemService aclItemService;
    private static final long serialVersionUID = 1L;
}
