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
