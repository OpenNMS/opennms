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
