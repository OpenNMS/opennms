package org.opennms.netmgt.correlation.drools;

import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.util.Assert;

public class DefaultNodeService implements NodeService {
    
    private NodeDao m_nodeDao;

    public Long getParentNode(Long nodeid) {
        OnmsNode node = m_nodeDao.get(nodeid.intValue());
        Assert.notNull(node, "Unable to find node with id "+nodeid);
        
        OnmsNode parent = node.getParent();
        return (parent == null ? null : new Long(parent.getId().longValue()));
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

}
