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
