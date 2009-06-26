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

import java.util.Collection;
import java.util.ArrayList;


/**
 * @author <a href="mailto:jason.aras@opennms.org">Jason Aras</a>
 */
public class StatusCategory {

	private String m_label;
	private String m_comment;
	private Float m_normal;
	private Integer m_warning;
	private Boolean m_outagestatus;
	private String  m_filter;
	private Collection<StatusNode> m_nodelist;
	
	
	public StatusCategory(){
	
		m_nodelist = new ArrayList<StatusNode>();
	
	}
	
	public String getFilter() {
		
		return m_filter;
		
	}
	
	public void setFilter(String filter) {
		
		m_filter = filter;
				
	}
	
	
	
	
	public Collection<StatusNode> getNodes() {
	
		return m_nodelist;
	
	}
	
	
	public void addNode(StatusNode NewNode){
		
		m_nodelist.add(NewNode);
		
	}
	

	public Boolean getOutageStatus() {
		return m_outagestatus;
	}

	public void setOutageStatus(Boolean OutageStatus) {
		m_outagestatus = OutageStatus;
	}
	
	public String getComment() {
		return m_comment;
	}


	public void setComment(String m_comment) {
		this.m_comment = m_comment;
	}


	public String getLabel() {
		return m_label;
	}


	public void setLabel(String m_label) {
		this.m_label = m_label;
	}


	public Float getNormal() {
		return m_normal;
	}


	public void setNormal(Float m_normal) {
		this.m_normal = m_normal;
	}


	public Integer getWarning() {
		return m_warning;
	}


	public void setWarning(Integer m_warning) {
		this.m_warning = m_warning;
	}
	
}
