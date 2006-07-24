package org.opennms.secret.service.impl;

import java.util.HashSet;

import org.opennms.secret.dao.NodeInterfaceDao;
import org.opennms.secret.model.Node;
import org.opennms.secret.model.NodeInterface;
import org.opennms.secret.service.NodeInterfaceService;

public class NodeInterfaceServiceImpl implements NodeInterfaceService {
    private NodeInterfaceDao m_nodeInterfaceDao;
    
    public void setNodeInterfaceDao(NodeInterfaceDao nodeInterfaceDao) {
        m_nodeInterfaceDao = nodeInterfaceDao;
    }
    
    public HashSet getInterfaces(Node node) {
		HashSet interfaces = new HashSet();
		for (int i = 0; i < 5; i++) {
			NodeInterface newInterface = new NodeInterface();
			newInterface.setNodeId(new Long(i));
            newInterface.setIfIndex(new Long(i));
            newInterface.setIpAddr("1.1.1." + (i + 1));
			interfaces.add(newInterface);
		}
		return interfaces;
	}

}
