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
 * <p>StatusCategory class.</p>
 *
 * @author <a href="mailto:jason.aras@opennms.org">Jason Aras</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class StatusCategory {

	private String m_label;
	private String m_comment;
	private Float m_normal;
	private Integer m_warning;
	private Boolean m_outagestatus;
	private String  m_filter;
	private Collection<StatusNode> m_nodelist;
	
	
	/**
	 * <p>Constructor for StatusCategory.</p>
	 */
	public StatusCategory(){
	
		m_nodelist = new ArrayList<StatusNode>();
	
	}
	
	/**
	 * <p>getFilter</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getFilter() {
		
		return m_filter;
		
	}
	
	/**
	 * <p>setFilter</p>
	 *
	 * @param filter a {@link java.lang.String} object.
	 */
	public void setFilter(String filter) {
		
		m_filter = filter;
				
	}
	
	
	
	
	/**
	 * <p>getNodes</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<StatusNode> getNodes() {
	
		return m_nodelist;
	
	}
	
	
	/**
	 * <p>addNode</p>
	 *
	 * @param NewNode a {@link org.opennms.web.svclayer.catstatus.model.StatusNode} object.
	 */
	public void addNode(StatusNode NewNode){
		
		m_nodelist.add(NewNode);
		
	}
	

	/**
	 * <p>getOutageStatus</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean getOutageStatus() {
		return m_outagestatus;
	}

	/**
	 * <p>setOutageStatus</p>
	 *
	 * @param OutageStatus a {@link java.lang.Boolean} object.
	 */
	public void setOutageStatus(Boolean OutageStatus) {
		m_outagestatus = OutageStatus;
	}
	
	/**
	 * <p>getComment</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getComment() {
		return m_comment;
	}


	/**
	 * <p>setComment</p>
	 *
	 * @param m_comment a {@link java.lang.String} object.
	 */
	public void setComment(String m_comment) {
		this.m_comment = m_comment;
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
	 * <p>getNormal</p>
	 *
	 * @return a {@link java.lang.Float} object.
	 */
	public Float getNormal() {
		return m_normal;
	}


	/**
	 * <p>setNormal</p>
	 *
	 * @param m_normal a {@link java.lang.Float} object.
	 */
	public void setNormal(Float m_normal) {
		this.m_normal = m_normal;
	}


	/**
	 * <p>getWarning</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getWarning() {
		return m_warning;
	}


	/**
	 * <p>setWarning</p>
	 *
	 * @param m_warning a {@link java.lang.Integer} object.
	 */
	public void setWarning(Integer m_warning) {
		this.m_warning = m_warning;
	}
	
}
