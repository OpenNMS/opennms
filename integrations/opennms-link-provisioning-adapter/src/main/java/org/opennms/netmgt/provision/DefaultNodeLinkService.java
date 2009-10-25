package org.opennms.netmgt.provision;

import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Category;
import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

public class DefaultNodeLinkService implements NodeLinkService {
    
    @Autowired
    NodeDao m_nodeDao;
    
    @Autowired
    DataLinkInterfaceDao m_dataLinkDao;
    
    @Transactional
    public void createLink(int nodeParentId, int nodeId) {
        log().info(String.format("adding link between node: %d and node: %d", nodeParentId, nodeId));
        OnmsNode parentNode = m_nodeDao.get(nodeParentId);
        Assert.notNull(parentNode, "node with id: " + nodeParentId + " does not exist");
        
        OnmsNode node = m_nodeDao.get(nodeId);
        Assert.notNull(node, "node with id: " + nodeId + " does not exist");
        
        OnmsCriteria criteria = new OnmsCriteria(DataLinkInterface.class);
        criteria.add(Restrictions.eq("nodeId", nodeId));
        criteria.add(Restrictions.eq("nodeParentId", nodeParentId));
        
        Collection<DataLinkInterface> dataLinkInterface = m_dataLinkDao.findMatching(criteria);
        
        if(dataLinkInterface.size() <= 0){
            DataLinkInterface dataLink = new DataLinkInterface();
            dataLink.setNodeId(nodeId);
            dataLink.setNodeParentId(nodeParentId);
            dataLink.setIfIndex(getPrimaryIfIndexForNode(node));
            dataLink.setParentIfIndex(getPrimaryIfIndexForNode(parentNode));
            dataLink.setStatus("G");
            dataLink.setLastPollTime(new Date());
            
            m_dataLinkDao.save(dataLink);
            m_dataLinkDao.flush();
            log().info(String.format("successfully added link into db for nodes %d and %d", nodeParentId, nodeId));
        }else {
           log().info(String.format("link between pointOne: %d and pointTwo %d already exists", nodeParentId, nodeId));  
        }
        
    }
    
    private int getPrimaryIfIndexForNode(OnmsNode node) {
        if(node.getPrimaryInterface() != null && node.getPrimaryInterface().getIfIndex() != null){
            return node.getPrimaryInterface().getIfIndex();
        }else{
            return -1;
        }
    }

    public Integer getNodeId(String endPoint) {
        Collection<OnmsNode> nodes = m_nodeDao.findByLabel(endPoint);
        
        if(nodes.size() > 0){
            return nodes.iterator().next().getId();
        }
        return null;
    }

    public String getNodeLabel(int nodeId) {
        OnmsNode node = m_nodeDao.get(nodeId);
        if(node != null){
            return node.getLabel(); 
        }
        return null;
    }

    public void updateLinkStatus(int nodeParentId, int nodeId, String status) {
        OnmsCriteria criteria = new OnmsCriteria(DataLinkInterface.class);
        criteria.add(Restrictions.eq("nodeId", nodeId));
        criteria.add(Restrictions.eq("nodeParentId", nodeParentId));
        
        Collection<DataLinkInterface> dataLinkInterface = m_dataLinkDao.findMatching(criteria);
        
        if(dataLinkInterface.size() > 0){
            DataLinkInterface dataLink = dataLinkInterface.iterator().next();
            dataLink.setStatus(status);
            
            m_dataLinkDao.update(dataLink);
            m_dataLinkDao.flush();
        }
    }
    
    private static Category log() {
        return ThreadCategory.getInstance(LinkProvisioningAdapter.class);
    }

}
