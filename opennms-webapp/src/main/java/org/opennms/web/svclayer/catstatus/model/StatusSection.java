/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.svclayer.catstatus.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>StatusSection class.</p>
 *
 * @author <a href="mailto:jason.aras@opennms.org">Jason Aras</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class StatusSection {

	private String m_name;
	private Collection<StatusCategory> m_categorylist;
	
	/**
	 * <p>Constructor for StatusSection.</p>
	 */
	public StatusSection(){
		
		m_categorylist = new ArrayList<>();
		
	}
	
	/**
	 * <p>setName</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public void setName(String name){
		m_name = name;
	}
	
	/**
	 * <p>getName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return m_name;
	}
	
	/**
	 * <p>getCategories</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<StatusCategory> getCategories() {
		return m_categorylist;
	}
	
	/**
	 * <p>addCategory</p>
	 *
	 * @param newCategory a {@link org.opennms.web.svclayer.catstatus.model.StatusCategory} object.
	 */
	public void addCategory(StatusCategory newCategory) {
		m_categorylist.add(newCategory);
	}
	
}
