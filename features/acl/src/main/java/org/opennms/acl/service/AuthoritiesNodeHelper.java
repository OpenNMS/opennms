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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.acl.model.AuthorityDTO;
import org.opennms.acl.model.AuthorityView;

/**
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 */
/* This class retrieve the nodes that an authority can view */
/* This class retrieve the nodes that an authority can view */
class AuthoritiesNodeHelper {

	/**
	 * <p>Constructor for AuthoritiesNodeHelper.</p>
	 *
	 * @param authorities a {@link java.util.List} object.
	 */
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

	/**
	 * <p>deleteAuthority</p>
	 *
	 * @param authority a {@link java.lang.String} object.
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean deleteAuthority(String authority) {
		return authItemsMap.remove(authority) != null;
	}

	/**
	 * <p>deleteItem</p>
	 *
	 * @param id a {@link java.lang.Integer} object.
	 * @return a {@link java.lang.Boolean} object.
	 */
	public synchronized Boolean deleteItem(Integer id) {

		Set<String> keys = authItemsMap.keySet();
		for (String key : keys) {
			authItemsMap.get(key).remove(id);
		}
		return itemsSet.remove(id);
	}

	/**
	 * <p>getAuthorities</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<String> getAuthorities() {
		return Collections.unmodifiableSet(authItemsMap.keySet());
	}

	// TODO put in a cache
	/**
	 * <p>getAuthoritiesItems</p>
	 *
	 * @param authorities a {@link java.util.Set} object.
	 * @return a {@link java.util.Set} object.
	 */
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

	/**
	 * <p>addAuthorityWithNodes</p>
	 *
	 * @param authority a {@link org.opennms.acl.model.AuthorityDTO} object.
	 */
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
