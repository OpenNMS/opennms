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

import java.util.List;
import java.util.Set;

import org.opennms.acl.model.AuthorityDTO;
import org.opennms.acl.model.AuthorityView;

/**
 * Contract to manage acl items
 * 
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 */
public interface AclItemService {

	/**
	 * @return all items
	 */
	public List<?> getItems();

	/**
	 * delete an item
	 * 
	 * @param id
	 */
	public Boolean deleteItem(Integer id);

	/**
	 * delete an item
	 * 
	 * @param id
	 */
	public Boolean deleteAuthority(String authority);

	/**
	 * the set of items permitted
	 * 
	 * @param role
	 * @return
	 */
	public Set<Integer> getAclItems(Set<AuthorityView> authorities);

	/**
	 * @param authorityItemsID
	 * @return
	 */
	public List<?> getAuthorityItems(List<Integer> authorityItemsId);

	/**
	 * @param authorityItemsID
	 * @return
	 */
	public List<?> getFreeItems(List<Integer> authorityItemsID);

	/**
	 * add an authority with its items
	 * 
	 * @param authority
	 */
	public void addAuthority(AuthorityDTO authority);
}
