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
 * Created: July 26, 2006
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


import java.util.Collection;
import java.util.ArrayList;

/**
 * @author <a href="mailto:jason.aras@opennms.org">Jason Aras</a>
 */
public class StatusNode {
	private String m_label;
	private Boolean m_outagestatus;
	private Collection<StatusInterface> m_ipinterfaces;
	private Integer m_nodeid;
	
	
	public StatusNode(){
		
		m_ipinterfaces = new ArrayList<StatusInterface>();
		
	}
	
	public void addIpInterface(StatusInterface ipInterface){
		
		m_ipinterfaces.add(ipInterface);
		
	}
	
	public Collection<StatusInterface> getIpInterfaces(){
		
		return m_ipinterfaces;
		
	}
	
	
	public String getLabel() {
		return m_label;
	}
	public void setLabel(String m_label) {
		this.m_label = m_label;
	}
	public Boolean getOutagestatus() {
		return m_outagestatus;
	}
	public void setOutagestatus(Boolean m_outagestatus) {
		this.m_outagestatus = m_outagestatus;
	}

	public Integer getNodeid() {
		return m_nodeid;
	}

	public void setNodeid(Integer nodeid) {
		m_nodeid = nodeid;
	}
	

	
}
