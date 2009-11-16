package org.opennms.netmgt.provision.adapters.link;

import static org.opennms.core.utils.LogUtils.debugf;
import static org.opennms.core.utils.LogUtils.infof;

import java.util.Collection;
import java.util.Date;

import org.hibernate.criterion.Restrictions;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.LinkStateDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLinkState;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsLinkState.LinkState;
import org.opennms.netmgt.provision.adapters.link.endpoint.dao.EndPointConfigurationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Transactional
public class DefaultNodeLinkService implements NodeLinkService {
    
    @Autowired
    NodeDao m_nodeDao;
    
    @Autowired
    DataLinkInterfaceDao m_dataLinkDao;
    
    @Autowired
    MonitoredServiceDao m_monitoredServiceDao;
    
    @Autowired
    EndPointConfigurationDao m_endPointConfigDao;
    
    @Autowired
    LinkStateDao m_linkStateDao;
    
    @Transactional
    public void saveLinkState(OnmsLinkState state) {
        debugf(this, "saving LinkState %s", state.getLinkState());
        m_linkStateDao.saveOrUpdate(state);
        m_linkStateDao.flush();
    }
    
    @Transactional
    public void createLink(int nodeParentId, int nodeId) {
        infof(this, "adding link between node: %d and node: %d", nodeParentId, nodeId);
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
            
            OnmsLinkState linkState = new OnmsLinkState();
            linkState.setDataLinkInterface(dataLink);
            
            boolean nodeParentEndPoint = nodeHasEndPointService(nodeParentId);
            boolean nodeEndPoint =  nodeHasEndPointService(nodeId);
            
            if(nodeParentEndPoint && nodeEndPoint) {
                dataLink.setStatus("G");
                linkState.setLinkState(LinkState.LINK_UP);
            }else {
                dataLink.setStatus("U");
                if(nodeEndPoint){
                    linkState.setLinkState(LinkState.LINK_PARENT_NODE_UNMANAGED);
                }else if(nodeParentEndPoint){
                    linkState.setLinkState(LinkState.LINK_NODE_UNMANAGED);
                }else{
                    linkState.setLinkState(LinkState.LINK_BOTH_UNMANAGED);
                }
            }
            dataLink.setLastPollTime(new Date());
            
            m_dataLinkDao.save(dataLink);
            
            m_linkStateDao.save(linkState);
            infof(this, "successfully added link into db for nodes %d and %d", nodeParentId, nodeId);
        } else {
            infof(this, "link between pointOne: %d and pointTwo %d already exists", nodeParentId, nodeId);  
        }
    }
    
    private int getPrimaryIfIndexForNode(OnmsNode node) {
        if(node.getPrimaryInterface() != null && node.getPrimaryInterface().getIfIndex() != null){
            return node.getPrimaryInterface().getIfIndex();
        }else{
            return -1;
        }
    }

    @Transactional(readOnly=true)
    public Integer getNodeId(String endPoint) {
        Collection<OnmsNode> nodes = m_nodeDao.findByLabel(endPoint);
        
        if(nodes.size() > 0){
            return nodes.iterator().next().getId();
        }
        return null;
    }

    @Transactional(readOnly=true)
    public String getNodeLabel(int nodeId) {
        OnmsNode node = m_nodeDao.get(nodeId);
        if(node != null){
            return node.getLabel(); 
        }
        return null;
    }

    @Transactional(readOnly=true)
    public Collection<DataLinkInterface> getLinkContainingNodeId(int nodeId) {
        OnmsCriteria criteria = new OnmsCriteria(DataLinkInterface.class);
        criteria.add(Restrictions.or(
            Restrictions.eq("nodeId", nodeId),
            Restrictions.eq("nodeParentId", nodeId)
        ));
        
        return m_dataLinkDao.findMatching(criteria);
    }

    @Transactional(readOnly=true)
    public OnmsLinkState getLinkStateForInterface(DataLinkInterface dataLinkInterface) {
        return m_linkStateDao.findByDataLinkInterfaceId(dataLinkInterface.getId());
    }
    
    @Transactional
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

    @Transactional(readOnly=true)
    public String getPrimaryAddress(int nodeId) {
        OnmsNode node = m_nodeDao.get(nodeId);
        OnmsIpInterface primaryInterface = node.getPrimaryInterface();
        
        if(node != null && primaryInterface != null) {
            return primaryInterface.getIpAddress();
        }
        
        return null;
    }
    
    @Transactional(readOnly=true)
    public boolean nodeHasEndPointService(int nodeId) {
        
        OnmsMonitoredService endPointService = m_monitoredServiceDao.getPrimaryService(nodeId, m_endPointConfigDao.getValidator().getServiceName());
        
        return endPointService == null ? false : true;
    }
    
}
