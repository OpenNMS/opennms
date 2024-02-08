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
package org.opennms.web.enlinkd;

public class BridgeElementNode {

	private String  m_baseBridgeAddress;
	private Integer m_baseNumPorts;
	private String  m_baseType;
	private String  m_stpProtocolSpecification;
	private Integer m_stpPriority;
	private String  m_stpDesignatedRoot;
	private Integer m_stpRootCost;
	private Integer m_stpRootPort;
	private Integer m_vlan;
	private String  m_vlanname;
        private String  m_bridgeNodeCreateTime;
        private String  m_bridgeNodeLastPollTime;
	
        public String getBaseBridgeAddress() {
		return m_baseBridgeAddress;
	}
	public void setBaseBridgeAddress(String baseBridgeAddress) {
		m_baseBridgeAddress = baseBridgeAddress;
	}
	public Integer getBaseNumPorts() {
		return m_baseNumPorts;
	}
	public void setBaseNumPorts(Integer baseNumPorts) {
		m_baseNumPorts = baseNumPorts;
	}
	public String getBaseType() {
		return m_baseType;
	}
	public void setBaseType(String baseType) {
		m_baseType = baseType;
	}
	public String getStpProtocolSpecification() {
		return m_stpProtocolSpecification;
	}
	public void setStpProtocolSpecification(String stpProtocolSpecification) {
		m_stpProtocolSpecification = stpProtocolSpecification;
	}
	public Integer getStpPriority() {
		return m_stpPriority;
	}
	public void setStpPriority(Integer stpPriority) {
		m_stpPriority = stpPriority;
	}
	public String getStpDesignatedRoot() {
		return m_stpDesignatedRoot;
	}
	public void setStpDesignatedRoot(String stpDesignatedRoot) {
		m_stpDesignatedRoot = stpDesignatedRoot;
	}
	public Integer getStpRootCost() {
		return m_stpRootCost;
	}
	public void setStpRootCost(Integer stpRootCost) {
		m_stpRootCost = stpRootCost;
	}
	public Integer getStpRootPort() {
		return m_stpRootPort;
	}
	public void setStpRootPort(Integer stpRootPort) {
		m_stpRootPort = stpRootPort;
	}
	public Integer getVlan() {
		return m_vlan;
	}
	public void setVlan(Integer vlan) {
		m_vlan = vlan;
	}
	public String getVlanname() {
		return m_vlanname;
	}
	public void setVlanname(String vlanname) {
		m_vlanname = vlanname;
	}
	public String getBridgeNodeCreateTime() {
		return m_bridgeNodeCreateTime;
	}
	public void setBridgeNodeCreateTime(String bridgeNodeCreateTime) {
		m_bridgeNodeCreateTime = bridgeNodeCreateTime;
	}
	public String getBridgeNodeLastPollTime() {
		return m_bridgeNodeLastPollTime;
	}
	public void setBridgeNodeLastPollTime(String bridgeNodeLastPollTime) {
		m_bridgeNodeLastPollTime = bridgeNodeLastPollTime;
	}
    
}
