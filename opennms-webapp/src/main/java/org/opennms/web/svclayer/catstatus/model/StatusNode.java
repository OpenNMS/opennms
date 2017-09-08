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


import java.util.Collection;
import java.util.ArrayList;

/**
 * <p>StatusNode class.</p>
 *
 * @author <a href="mailto:jason.aras@opennms.org">Jason Aras</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class StatusNode {
	private String m_label;
	private Boolean m_outagestatus;
	private Collection<StatusInterface> m_ipinterfaces;
	private Integer m_nodeid;
	
	
	/**
	 * <p>Constructor for StatusNode.</p>
	 */
	public StatusNode(){
		
		m_ipinterfaces = new ArrayList<>();
		
	}
	
	/**
	 * <p>addIpInterface</p>
	 *
	 * @param ipInterface a {@link org.opennms.web.svclayer.catstatus.model.StatusInterface} object.
	 */
	public void addIpInterface(StatusInterface ipInterface){
		
		m_ipinterfaces.add(ipInterface);
		
	}
	
	/**
	 * <p>getIpInterfaces</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<StatusInterface> getIpInterfaces(){
		
		return m_ipinterfaces;
		
	}
	
	
	/**
	 * <p>getLabel</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getLabel() {
		return m_label;
	}
	/**
	 * <p>setLabel</p>
	 *
	 * @param m_label a {@link java.lang.String} object.
	 */
	public void setLabel(String m_label) {
		this.m_label = m_label;
	}
	/**
	 * <p>getOutagestatus</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean getOutagestatus() {
		return m_outagestatus;
	}
	/**
	 * <p>setOutagestatus</p>
	 *
	 * @param m_outagestatus a {@link java.lang.Boolean} object.
	 */
	public void setOutagestatus(Boolean m_outagestatus) {
		this.m_outagestatus = m_outagestatus;
	}

	/**
	 * <p>getNodeid</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getNodeid() {
		return m_nodeid;
	}

	/**
	 * <p>setNodeid</p>
	 *
	 * @param nodeid a {@link java.lang.Integer} object.
	 */
	public void setNodeid(Integer nodeid) {
		m_nodeid = nodeid;
	}
	

	
}
