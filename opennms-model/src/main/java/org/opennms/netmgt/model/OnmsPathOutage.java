/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

import java.io.Serializable;
import java.net.InetAddress;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

/**
 * <p>OnmsPathOutage class</p>
 * 
 * @author <a href="ryan@mail1.opennms.com"> Ryan Lambeth </a>
 */
@Entity
@Table(name="pathoutage")
public class OnmsPathOutage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2180867754702562743L;

	private int m_nodeId;
	private InetAddress m_criticalPathIp;
	private String m_criticalPathServiceName;
	private OnmsNode m_node;

	/**
	 * <p>Constructor for OnmsPathOutage</p>
	 * 
	 * @param an int
	 * @param an InetAddress
	 * @param a String
	 */
	public OnmsPathOutage(OnmsNode node, InetAddress criticalPathIp, String criticalPathServiceName) {
		m_nodeId = node.getId();
		m_node = node;
		m_criticalPathIp = criticalPathIp;
		m_criticalPathServiceName = criticalPathServiceName;
	}

	public OnmsPathOutage() {
	}

	/**
	 * The node this asset information belongs to.
	 *
	 * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
	 */
	@OneToOne
	@PrimaryKeyJoinColumn
	public OnmsNode getNode() {
		return m_node;
	}

	public void setNode(OnmsNode node) {
		m_node = node;
	}

	/**
	 * Because the pathOutage table uses the node ID as its ID, this set of 
	 * annotations uses a Hibernate "foreign" strategy ID generator so that 
	 * the foreign key of a related object (the "node") is used as the ID of this object.
	 * This is known as a bidirectional one-to-one primary key relationship.
	 * 
	 * @see http://fruzenshtein.com/bidirectional-one-to-one-primary-key-association/
	 * 
	 * @return
	 */
	@Id
	@Column(name="nodeId")
	@GeneratedValue(generator="nodeGenerator")
	@GenericGenerator(
		name="nodeGenerator", 
		strategy="foreign", 
		parameters=@Parameter(name="property", value="node")
	)
	public int getNodeId() {
		return m_nodeId;
	}

	public void setNodeId(int id) {
		m_nodeId = id;
	}

	/**
	 * <p>Getter for field <code>m_criticalPathIp</code>.</p>
	 * 
	 * @return an InetAddress
	 */
	@Column(name="criticalpathip", nullable = false)
	@Type(type="org.opennms.netmgt.model.InetAddressUserType")
	public InetAddress getCriticalPathIp() {
		return m_criticalPathIp;
	}

	/**
	 * <p>Setter for field <code>m_criticalPathIp</code>.</p>
	 * 
	 * @param an InetAddress
	 */
	public void setCriticalPathIp(InetAddress criticalPathIp) {
		m_criticalPathIp = criticalPathIp;
	}

	/**
	 * <p>Getter for field <code>m_criticalPathServiceName</code>.</p>
	 * 
	 * @return a String
	 */
	@Column(name="criticalpathservicename")
	public String getCriticalPathServiceName() {
		return m_criticalPathServiceName;
	}

	/**
	 * <p>Setter for field <code>m_criticalPathServiceName</code>.</p>
	 * 
	 * @param a String
	 */
	public void setCriticalPathServiceName(String criticalPathServiceName) {
		m_criticalPathServiceName = criticalPathServiceName;
	}
}
