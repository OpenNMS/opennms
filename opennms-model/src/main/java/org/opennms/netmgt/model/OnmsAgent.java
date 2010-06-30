//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.model;

/**
 * <p>OnmsAgent class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OnmsAgent extends Object {

	private Integer m_id;
	private OnmsServiceType m_serviceType;
	private String m_ipAddress;
	private OnmsNode m_node;

	/**
	 * <p>Constructor for OnmsAgent.</p>
	 */
	public OnmsAgent() {
		super();
	}
	
	/**
	 * <p>getId</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getId() {
		return m_id;
	}
	
	/**
	 * <p>setId</p>
	 *
	 * @param id a {@link java.lang.Integer} object.
	 */
	public void setId(Integer id) {
		m_id = id;
	}

	/**
	 * <p>getServiceType</p>
	 *
	 * @return a {@link org.opennms.netmgt.model.OnmsServiceType} object.
	 */
	public OnmsServiceType getServiceType() {
		return m_serviceType;
	}

	/**
	 * <p>setServiceType</p>
	 *
	 * @param serviceType a {@link org.opennms.netmgt.model.OnmsServiceType} object.
	 */
	public void setServiceType(OnmsServiceType serviceType) {
		m_serviceType = serviceType;
	}

	/**
	 * <p>getIpAddress</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getIpAddress() {
		return m_ipAddress;
	}

	/**
	 * <p>setIpAddress</p>
	 *
	 * @param ipAddress a {@link java.lang.String} object.
	 */
	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}

	/**
	 * <p>getNode</p>
	 *
	 * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
	 */
	public OnmsNode getNode() {
		return m_node;
	}

	/**
	 * <p>setNode</p>
	 *
	 * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
	 */
	public void setNode(OnmsNode node) {
		m_node = node;
	}

}
