package org.opennms.dashboard.server;

import java.util.ArrayList;
import java.util.List;

import org.opennms.dashboard.client.NodeService;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class NodeServiceImpl implements NodeService {

    NodeDao m_nodeDao;
    
    
    public String[] getNodeNames() {
        
        List<OnmsNode> nodes = m_nodeDao.findAll();
        
        List<String> labels = new ArrayList<String>(nodes.size());
        for (OnmsNode node : nodes) {
            labels.add(node.getLabel());
        }
        
        return (String[]) labels.toArray(new String[labels.size()]);

    }


    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

}
