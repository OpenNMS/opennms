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

import java.util.ArrayList;
import java.util.List;

public class BridgeLinkNode {
	
	private String m_bridgeLocalPort;
	private Integer m_bridgeLocalVlan;

	private List<BridgeLinkRemoteNode> m_bridgeLinkRemoteNodes = new ArrayList<BridgeLinkRemoteNode>();

	private String m_bridgeLinkCreateTime;
    private String m_bridgeLinkLastPollTime;
    
    public String getBridgeLinkCreateTime() {
		return m_bridgeLinkCreateTime;
	}
	public void setBridgeLinkCreateTime(String bridgeLinkCreateTime) {
		m_bridgeLinkCreateTime = bridgeLinkCreateTime;
	}
	public String getBridgeLinkLastPollTime() {
		return m_bridgeLinkLastPollTime;
	}
	public void setBridgeLinkLastPollTime(String bridgeLinkLastPollTime) {
		m_bridgeLinkLastPollTime = bridgeLinkLastPollTime;
	}
	public String getBridgeLocalPort() {
		return m_bridgeLocalPort;
	}
	public void setBridgeLocalPort(String bridgeLocalPort) {
		m_bridgeLocalPort = bridgeLocalPort;
	}
	public Integer getBridgeLocalVlan() {
		return m_bridgeLocalVlan;
	}
	public void setBridgeLocalVlan(Integer bridgeLocalVlan) {
		m_bridgeLocalVlan = bridgeLocalVlan;
	}
	public List<BridgeLinkRemoteNode> getBridgeLinkRemoteNodes() {
		return m_bridgeLinkRemoteNodes;
	}
	public void setBridgeLinkRemoteNodes(
			List<BridgeLinkRemoteNode> bridgeLinkRemoteNodes) {
		m_bridgeLinkRemoteNodes = bridgeLinkRemoteNodes;
	}
    

}
