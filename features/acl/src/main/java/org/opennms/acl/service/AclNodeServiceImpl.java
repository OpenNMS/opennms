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
import java.util.Set;

import org.apache.commons.collections.FastArrayList;
import org.opennms.acl.model.AuthorityDTO;
import org.opennms.acl.model.AuthorityView;
import org.opennms.acl.model.CategoryNodeONMSDTO;
import org.opennms.acl.repository.ItemAclRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Implementation class of {@link AclItemService} to use Node as Items
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
@Service("nodesItemsService")
public class AclNodeServiceImpl implements AclItemService {

	/**
	 * <p>init</p>
	 */
	@SuppressWarnings("unchecked")
	public void init() {
		List<AuthorityDTO> authorities = new FastArrayList();
		for (AuthorityDTO authorityDTO : authorityService.getAuthorities()) {
			authorityDTO.setItems(authorityService
					.getIdItemsAuthority(authorityDTO.getId()));
			if (authorityDTO.hasItems()) {
				authorities.add(authorityDTO);
			}
		}
		if (authorities.size() > 0) {
			authItemsHelper = new AuthoritiesNodeHelper(authorities);
		}
		ready = true;
	}

	/** {@inheritDoc} */
	public Boolean deleteAuthority(String authority) {
		return authItemsHelper.deleteAuthority(authority);
	}

	/**
	 * <p>getItems</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@SuppressWarnings("unchecked")
	public List<CategoryNodeONMSDTO> getItems() {
		return (List<CategoryNodeONMSDTO>) itemAclRepository.getItems();
	}

	/** {@inheritDoc} */
	public Set<Integer> getAclItems(Set<AuthorityView> authorities) {
		if (!ready)
			init();
		return authItemsHelper.getAuthoritiesItems(authorities);
	}

	/** {@inheritDoc} */
	public Boolean deleteItem(Integer id) {
		return authItemsHelper.deleteItem(id);
	}

	/** {@inheritDoc} */
	public List<?> getAuthorityItems(List<Integer> items) {
		return itemAclRepository.getAuthorityItems(items);
	}

	/** {@inheritDoc} */
	public List<?> getFreeItems(List<Integer> items) {
		return itemAclRepository.getFreeItems(items);
	}

	/** {@inheritDoc} */
	public void addAuthority(AuthorityDTO authority) {
		authItemsHelper.addAuthorityWithNodes(authority);
	}

	/**
	 * <p>Setter for the field <code>itemAclRepository</code>.</p>
	 *
	 * @param itemAclRepository a {@link org.opennms.acl.repository.ItemAclRepository} object.
	 */
	@Autowired
	public void setItemAclRepository(
			@Qualifier("categoryNodeRepository") ItemAclRepository itemAclRepository) {
		this.itemAclRepository = itemAclRepository;
	}

	/**
	 * <p>Setter for the field <code>authorityService</code>.</p>
	 *
	 * @param authorityService a {@link org.opennms.acl.service.AuthorityService} object.
	 */
	@Autowired
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	private ItemAclRepository itemAclRepository;
	private AuthorityService authorityService;
	private AuthoritiesNodeHelper authItemsHelper;
	private boolean ready = false;
}
