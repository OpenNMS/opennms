/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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
 *******************************************************************************/

/**
 * 
 */
package org.opennms.provisiond.utils;

import org.apache.commons.lang.StringUtils;

class RequisitionData {
	private String m_nodeLabel = null;
	private String m_primaryIp = null;
	private String m_foreignSource = null;
	private String m_foreignId = null;

	/**
	 * @deprecated
	 * @param fields
	 * @throws Exception
	 */
	public RequisitionData(String[] fields) throws Exception {
		m_nodeLabel = StringUtils.isBlank(fields[0]) ? "noname" : fields[0];
		m_primaryIp = StringUtils.isBlank(fields[1]) ? "169.254.1.1" : fields[1];
		m_foreignSource = StringUtils.isBlank(fields[2]) ? "TS" : StringUtils.deleteWhitespace(fields[2]);
		m_foreignId = StringUtils.isBlank(fields[4]) ? null : fields[4];
		
		if (m_foreignId == null) {
			throw new Exception("ForeignId is blank in fields: \n"+fields+"\n");
		}
		
	}
	
	public RequisitionData(String label, String ipaddr, String fs) {
		m_nodeLabel = StringUtils.isBlank(label) ? "nolabel" : label;
		m_primaryIp = StringUtils.isBlank(ipaddr) ? "169.254.1.1" : ipaddr;
		m_foreignSource = StringUtils.isBlank(fs) ? "default" : StringUtils.deleteWhitespace(fs);
		m_foreignId = StringUtils.isBlank(m_nodeLabel) ? null : m_nodeLabel;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Node Label: ");
		sb.append(m_nodeLabel == null ? "Null" : m_nodeLabel);
		sb.append(", Primary IP: ");
		sb.append(m_primaryIp == null ? "Null" : m_primaryIp);
		sb.append(", Foreign Source: ");
		sb.append(m_foreignSource == null ? "Null" : m_foreignSource);
		sb.append(", Foreign ID: ");
		sb.append(m_foreignId == null ? "Null" : m_foreignId);
		return sb.toString();
	}

	public void setNodeLabel(String nodeLabel) {
		this.m_nodeLabel = nodeLabel;
	}

	public String getNodeLabel() {
		return m_nodeLabel;
	}

	public void setPrimaryIp(String primaryIp) {
		this.m_primaryIp = primaryIp;
	}

	public String getPrimaryIp() {
		return m_primaryIp;
	}

	public void setForeignSource(String foreignSource) {
		m_foreignSource = foreignSource;
	}

	public String getForeignSource() {
		return m_foreignSource;
	}

	public void setForeignId(String foreignId) {
		m_foreignId = foreignId;
	}

	public String getForeignId() {
		return m_foreignId;
	}
	
}