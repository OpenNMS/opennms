package org.opennms.secret.service.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.opennms.secret.dao.NodeDao;
import org.opennms.secret.model.Node;
import org.opennms.secret.service.NodeService;

public class NodeServiceImpl implements NodeService {

	private NodeDao m_nodeDao;

	public Node getNodeById(Long id) {
		Node node = m_nodeDao.getNode(id);
        m_nodeDao.initialize(node);
		return node;
	}

	public void setNodeDao(NodeDao nodeDao) {
		m_nodeDao = nodeDao;
	}

    public Set findAll() {
        return new HashSet(m_nodeDao.findAll());
    }

    public Set findWithMatchingLabel(String searchKey) {
        Collection nodes = m_nodeDao.findAll();
        Set matching = new HashSet();
        for (Iterator it = nodes.iterator(); it.hasNext();) {
            Node node = (Node) it.next();
            if (node.getNodeLabel().startsWith(searchKey)) {
                matching.add(node);
            }
        }
        return matching;
    }
}
