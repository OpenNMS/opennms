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

package org.opennms.netmgt.model;

/**
 * <p>OnmsAgent class.</p>
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
