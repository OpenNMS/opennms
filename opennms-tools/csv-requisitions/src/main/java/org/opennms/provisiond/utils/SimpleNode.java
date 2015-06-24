/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.provisiond.utils;

public class SimpleNode {

	private Integer m_nodeId;
	private String m_label;
	private String m_foreignSource;
	private String m_foreignId;
	private String m_ipaddr;
	private String m_primaryFlag;

	public SimpleNode(Integer nodeId, String label, String foreignSource, String foreignId, String ipaddr, String primaryFlag) {
		
		m_nodeId = nodeId;
		m_label = label;
		m_foreignSource = foreignSource;
		m_foreignId = foreignId;
		m_ipaddr = ipaddr;
		m_primaryFlag = primaryFlag;
	}

	public Integer getNodeId() {
		return m_nodeId;
	}

	public void setNodeId(Integer m_nodeId) {
		this.m_nodeId = m_nodeId;
	}

	public String getLabel() {
		return m_label;
	}

	public void setLabel(String m_label) {
		this.m_label = m_label;
	}
	
	public String getForeignSource() {
		return m_foreignSource;
	}
	
	public void setForeignSource(String foreignSource) {
		m_foreignSource = foreignSource;
	}

	public String getForeignId() {
		return m_foreignId;
	}
	
	public void setForeignId(String foreignId) {
		m_foreignId = foreignId;
	}

	public String getIpaddr() {
		return m_ipaddr;
	}

	public void setIpaddr(String m_ipaddr) {
		this.m_ipaddr = m_ipaddr;
	}

	public String getPrimaryFlag() {
		return m_primaryFlag;
	}

	public void setPrimaryFlag(String primaryFlag) {
		this.m_primaryFlag = primaryFlag;
	}

}
