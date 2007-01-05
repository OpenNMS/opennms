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

import org.opennms.web.svclayer.catstatus.model.StatusCategory;
import org.opennms.web.svclayer.catstatus.model.StatusInterface;
import org.opennms.web.svclayer.catstatus.model.StatusNode;
import org.opennms.web.svclayer.catstatus.model.StatusSection;
import org.opennms.web.svclayer.catstatus.model.StatusService;

public class CategoryBuilder {
	
	private Map <Integer,StatusNode>m_nodemap = new HashMap();
	private Map m_nodeandinterfacemap = new HashMap();
	
	
	
		
	public CategoryBuilder addNode( int nodeId,String label ){
		
		if(!m_nodemap.containsKey(nodeId)){
			StatusNode m_statusnode = new StatusNode();
			m_statusnode.setLabel(label);
			m_nodemap.put(nodeId, m_statusnode);
		}
		
		return this;
	}
	
	public CategoryBuilder addInterface(int nodeId, String interfaceIp, String ipAddress, String nodeLabel){
					
					if(!m_nodeandinterfacemap.containsKey(nodeId + ":" + interfaceIp )){
						addNode(nodeId,nodeLabel);	
						StatusNode statusNode = (StatusNode) m_nodemap.get(nodeId);
						StatusInterface m_interface = new StatusInterface();
						m_interface.setIpAddress(ipAddress);
						statusNode.addIpInterface(m_interface);	
						m_nodeandinterfacemap.put(nodeId + ":" + interfaceIp, m_interface);
					}
		return this;
	}
	
	public CategoryBuilder addOutageService(int nodeId, String interfaceIp, String ipAddress, String nodeLabel, String service){
	
		StatusService m_statusservice = new StatusService();
		StatusInterface m_interface;
		addInterface(nodeId,interfaceIp,ipAddress,nodeLabel);
		m_interface = (StatusInterface) m_nodeandinterfacemap.get(nodeId + ":" + interfaceIp);
		m_statusservice.setName(service);
		m_interface.addService(m_statusservice);
			
		
		return this;
	}
	
	public Collection<StatusNode> getNodes() { 
		Collection <StatusNode> nodes = m_nodemap.values();
		return nodes;
		
	}
	
}
