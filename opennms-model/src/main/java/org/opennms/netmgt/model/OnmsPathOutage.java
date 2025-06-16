/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
