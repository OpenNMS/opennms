/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 27, 2006
 *
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
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
		
		m_categorylist = new ArrayList<StatusCategory>();
		
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
