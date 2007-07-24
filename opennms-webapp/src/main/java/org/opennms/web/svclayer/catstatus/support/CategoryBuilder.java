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
// 2007 Jul 24: Organize imports, format code, extract common code for
//              node+interface key. - dj@opennms.org
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.web.svclayer.catstatus.support;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.web.svclayer.catstatus.model.StatusInterface;
import org.opennms.web.svclayer.catstatus.model.StatusNode;
import org.opennms.web.svclayer.catstatus.model.StatusService;

public class CategoryBuilder {
	private Map<Integer, StatusNode> m_nodeMap = new HashMap<Integer, StatusNode>();
	private Map<String, StatusInterface> m_nodeAndInterfaceMap = new HashMap<String, StatusInterface>();
		
	public CategoryBuilder addNode(int nodeId, String label) {
		if (!m_nodeMap.containsKey(nodeId)) {
			StatusNode m_statusnode = new StatusNode();
			m_statusnode.setLabel(label);
			m_nodeMap.put(nodeId, m_statusnode);
		}
		
		return this;
	}
	
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
	
	public Collection<StatusNode> getNodes() { 
		return m_nodeMap.values();
	}
}
