package org.opennms.netmgt.provision.adapters.link;

import static org.opennms.core.utils.LogUtils.debugf;
import static org.opennms.core.utils.LogUtils.infof;
import static org.opennms.core.utils.LogUtils.warnf;

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
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.provision.adapters.link.LinkEventSendingStateTransition;
import org.opennms.netmgt.provision.adapters.link.NodeLinkService;
import org.opennms.netmgt.provision.adapters.link.endpoint.dao.EndPointConfigurationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Transactional
public class DefaultNodeLinkService implements NodeLinkService {
    
    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private DataLinkInterfaceDao m_dataLinkDao;
    
    @Autowired
    private MonitoredServiceDao m_monitoredServiceDao;
    
    @Autowired
    private EndPointConfigurationDao m_endPointConfigDao;
    
    @Autowired
    private LinkStateDao m_linkStateDao;
    
    @Autowired
    @Qualifier("transactionAware")
    private EventForwarder m_eventForwarder;
    
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
        DataLinkInterface dli = null;
        
        if (dataLinkInterface.size() > 1) {
            warnf(this, "more than one data link interface exists for nodes %d and %d", nodeParentId, nodeId);
            return;
        } else if (dataLinkInterface.size() > 0) {
            dli = dataLinkInterface.iterator().next();
            infof(this, "link between nodes %d and %d already exists", nodeParentId, nodeId);  
        } else {
            dli = new DataLinkInterface();
            dli.setNodeId(nodeId);
            dli.setNodeParentId(nodeParentId);
            dli.setIfIndex(getPrimaryIfIndexForNode(node));
            dli.setParentIfIndex(getPrimaryIfIndexForNode(parentNode));
            infof(this, "creating new link between nodes %d and %d", nodeParentId, nodeId);
        }

        OnmsLinkState onmsLinkState = null;
        if (dli.getId() != null) {
            onmsLinkState = m_linkStateDao.findByDataLinkInterfaceId(dli.getId());
        }
        if (onmsLinkState == null) {
            onmsLinkState = new OnmsLinkState();
        }
        onmsLinkState.setDataLinkInterface(dli);
        
        Boolean nodeParentEndPoint = getEndPointStatus(nodeParentId);
        Boolean nodeEndPoint =  getEndPointStatus(nodeId);

        LinkState state = LinkState.LINK_UP;
        LinkEventSendingStateTransition transition = new LinkEventSendingStateTransition(dli, m_eventForwarder, this);

        if (nodeParentEndPoint == null) {
			state = state.parentNodeEndPointDeleted(transition);
		} else if (!nodeParentEndPoint) {
			state = state.parentNodeDown(transition);
		}
		if (nodeEndPoint == null) {
			state = state.nodeEndPointDeleted(transition);
		} else if (!nodeEndPoint) {
			state = state.nodeDown(null);
		}
		dli.setStatus(state.getDataLinkInterfaceStateType());
		onmsLinkState.setLinkState(state);
		
        dli.setLastPollTime(new Date());
        
        m_dataLinkDao.save(dli);
        m_linkStateDao.save(onmsLinkState);
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

    @Transactional(readOnly=true)
    public Boolean getEndPointStatus(int nodeId) {
        OnmsMonitoredService endPointService = m_monitoredServiceDao.getPrimaryService(nodeId, m_endPointConfigDao.getValidator().getServiceName());
        if (endPointService == null) {
        	return null;
        }

        // want true to be UP, not DOWN
        return !endPointService.isDown();
    }
}
