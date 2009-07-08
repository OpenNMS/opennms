/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * Copyright (C) 2009 The OpenNMS Group, Inc.
 * All rights reserved.
 *
 * This program was developed and is maintained by Rocco RIONERO
 * ("the author") and is subject to dual-copyright according to
 * the terms set in "The OpenNMS Project Contributor Agreement".
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.acl.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.acl.model.AuthorityDTO;
import org.opennms.acl.model.AuthorityView;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 */
/* This class retrieve the nodes that an authority can view */
class AuthoritiesNodeHelper {

	public AuthoritiesNodeHelper(List<AuthorityDTO> authorities) {

		authItemsMap = new HashMap<String, Set<Integer>>();
		itemsSet = new HashSet<Integer>();
		if (authorities != null && authorities.size() > 0) {
			for (AuthorityDTO authority : authorities) {
				if (authority.hasItems()) {
					addAuthorityWithNodes(authority);
				}
			}
		}
	}

	public Boolean deleteAuthority(String authority) {
		return authItemsMap.remove(authority) != null;
	}

	public synchronized Boolean deleteItem(Integer id) {

		Set<String> keys = authItemsMap.keySet();
		for (String key : keys) {
			authItemsMap.get(key).remove(id);
		}
		return itemsSet.remove(id);
	}

	@SuppressWarnings("unchecked")
	public Set<String> getAuthorities() {
		return Collections.unmodifiableSet(authItemsMap.keySet());
	}

	// TODO put in a cache
	public Set<Integer> getAuthoritiesItems(Set<AuthorityView> authorities) {
		Set<Integer> authItems = new HashSet<Integer>();
		for (AuthorityView auth : authorities) {
			if (authItemsMap.containsKey(auth.getName())) {
				authItems.addAll(new HashSet<Integer>(authItemsMap.get(auth
						.getName())));
			}
		}
		return authItems;
	}

	@SuppressWarnings("unchecked")
	public void addAuthorityWithNodes(AuthorityDTO authority) {

		if (authItemsMap.containsKey(authority.getName())) {

			Set<Integer> oldItems = authItemsMap.get(authority.getName());
			Set<Integer> itemsNew = createFreshAuthorityItems((List<String>) authority
					.getItems());
			itemsSet.addAll(itemsNew);
			oldItems.addAll(itemsNew);
			authItemsMap.put(authority.getName(), oldItems);

		} else {

			Set<Integer> itemsNew = createFreshAuthorityItems((List<String>) authority
					.getItems());
			itemsSet.addAll(itemsNew);
			authItemsMap.put(authority.getName(), itemsNew);
		}
	}

	private Set<Integer> createFreshAuthorityItems(List<String> list) {
		Set<Integer> authItems = new HashSet<Integer>();
		for (String nodeONMSDTO : list) {
			authItems.add(new Integer(nodeONMSDTO));
		}
		return authItems;
	}

	private Map<String, Set<Integer>> authItemsMap;
	private Set<Integer> itemsSet;
}
