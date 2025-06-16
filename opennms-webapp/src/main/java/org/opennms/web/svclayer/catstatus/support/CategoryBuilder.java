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
package org.opennms.web.svclayer.catstatus.support;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.web.svclayer.catstatus.model.StatusInterface;
import org.opennms.web.svclayer.catstatus.model.StatusNode;
import org.opennms.web.svclayer.catstatus.model.StatusService;

/**
 * <p>CategoryBuilder class.</p>
 *
 * @author <a href="mailto:jason.aras@opennms.org">Jason Aras</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class CategoryBuilder {
	private Map<Integer, StatusNode> m_nodeMap = new HashMap<Integer, StatusNode>();
	private Map<String, StatusInterface> m_nodeAndInterfaceMap = new HashMap<String, StatusInterface>();
		
	/**
	 * <p>addNode</p>
	 *
	 * @param nodeId a int.
	 * @param label a {@link java.lang.String} object.
	 * @return a {@link org.opennms.web.svclayer.catstatus.support.CategoryBuilder} object.
	 */
	public CategoryBuilder addNode(int nodeId, String label) {
		if (!m_nodeMap.containsKey(nodeId)) {
			StatusNode m_statusnode = new StatusNode();
			m_statusnode.setLabel(label);
			m_nodeMap.put(nodeId, m_statusnode);
		}
		
		return this;
	}
	
	/**
	 * <p>addInterface</p>
	 *
	 * @param nodeId a int.
	 * @param interfaceIp a {@link java.lang.String} object.
	 * @param ipAddress a {@link java.lang.String} object.
	 * @param nodeLabel a {@link java.lang.String} object.
	 * @return a {@link org.opennms.web.svclayer.catstatus.support.CategoryBuilder} object.
	 */
	public CategoryBuilder addInterface(int nodeId, String interfaceIp, String ipAddress, String nodeLabel) {
	    if (!m_nodeAndInterfaceMap.containsKey(getNodeAndInterfaceKey(nodeId, interfaceIp))) {
	        addNode(nodeId, nodeLabel);	
	        StatusNode statusNode = m_nodeMap.get(nodeId);
	        StatusInterface intf = new StatusInterface();
	        intf.setIpAddress(ipAddress);
	        statusNode.addIpInterface(intf);	
	        m_nodeAndInterfaceMap.put(getNodeAndInterfaceKey(nodeId, interfaceIp), intf);
	    }

        return this;
	}
	
	/**
	 * <p>addOutageService</p>
	 *
	 * @param nodeId a int.
	 * @param interfaceIp a {@link java.lang.String} object.
	 * @param ipAddress a {@link java.lang.String} object.
	 * @param nodeLabel a {@link java.lang.String} object.
	 * @param service a {@link java.lang.String} object.
	 * @return a {@link org.opennms.web.svclayer.catstatus.support.CategoryBuilder} object.
	 */
	public CategoryBuilder addOutageService(int nodeId, String interfaceIp, String ipAddress, String nodeLabel, String service) {
		StatusService statusService = new StatusService();
		addInterface(nodeId, interfaceIp, ipAddress, nodeLabel);
        StatusInterface intf = m_nodeAndInterfaceMap.get(getNodeAndInterfaceKey(nodeId, interfaceIp));
		statusService.setName(service);
		intf.addService(statusService);
			
		return this;
	}

    private String getNodeAndInterfaceKey(int nodeId, String interfaceIp) {
        return nodeId + ":" + interfaceIp;
    }
	
	/**
	 * <p>getNodes</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<StatusNode> getNodes() { 
		return m_nodeMap.values();
	}
}
