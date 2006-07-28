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
