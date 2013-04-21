/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.adapters.link;

import static org.opennms.core.utils.LogUtils.debugf;
import static org.opennms.core.utils.LogUtils.infof;
import static org.opennms.core.utils.LogUtils.warnf;

import java.util.Collection;
import java.util.Date;

import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.LinkStateDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLinkState;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.OnmsLinkState.LinkState;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.provision.adapters.link.endpoint.dao.EndPointConfigurationDao;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Transactional
/**
 * <p>DefaultNodeLinkService class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultNodeLinkService implements NodeLinkService, InitializingBean {
    
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
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
    
    /** {@inheritDoc} */
    @Transactional
    public void saveLinkState(OnmsLinkState state) {
        debugf(this, "saving LinkState %s", state.getLinkState());
        m_linkStateDao.saveOrUpdate(state);
        m_linkStateDao.flush();
    }
    
    /** {@inheritDoc} */
    @Transactional
    public void createLink(final int nodeParentId, final int nodeId) {
        infof(this, "adding link between node: %d and node: %d", nodeParentId, nodeId);
        final OnmsNode parentNode = m_nodeDao.get(nodeParentId);
        Assert.notNull(parentNode, "node with id: " + nodeParentId + " does not exist");
        
        final OnmsNode node = m_nodeDao.get(nodeId);
        Assert.notNull(node, "node with id: " + nodeId + " does not exist");
        
        final OnmsCriteria criteria = new OnmsCriteria(DataLinkInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.eq("node.id", nodeId));
        criteria.add(Restrictions.eq("nodeParentId", nodeParentId));
        
        final Collection<DataLinkInterface> dataLinkInterface = m_dataLinkDao.findMatching(criteria);
        DataLinkInterface dli = null;
        
        if (dataLinkInterface.size() > 1) {
            warnf(this, "more than one data link interface exists for nodes %d and %d", nodeParentId, nodeId);
            return;
        } else if (dataLinkInterface.size() > 0) {
            dli = dataLinkInterface.iterator().next();
            infof(this, "link between nodes %d and %d already exists", nodeParentId, nodeId);  
        } else {
            dli = new DataLinkInterface();
            dli.setNode(node);
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
		dli.setStatus(StatusType.get(state.getDataLinkInterfaceStateType()));
		onmsLinkState.setLinkState(state);
		
        dli.setLastPollTime(new Date());
        dli.setLinkTypeId(777);
        
        m_dataLinkDao.save(dli);
        m_linkStateDao.save(onmsLinkState);
        m_dataLinkDao.flush();
        m_linkStateDao.flush();
    }
    
    private int getPrimaryIfIndexForNode(OnmsNode node) {
        if(node.getPrimaryInterface() != null && node.getPrimaryInterface().getIfIndex() != null){
            return node.getPrimaryInterface().getIfIndex();
        }else{
            return -1;
        }
    }

    /** {@inheritDoc} */
    @Transactional(readOnly=true)
    public Integer getNodeId(String endPoint) {
        Collection<OnmsNode> nodes = m_nodeDao.findByLabel(endPoint);
        
        if(nodes.size() > 0){
            return nodes.iterator().next().getId();
        }
        return null;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly=true)
    public String getNodeLabel(int nodeId) {
        OnmsNode node = m_nodeDao.get(nodeId);
        if(node != null){
            return node.getLabel(); 
        }
        return null;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly=true)
    public Collection<DataLinkInterface> getLinkContainingNodeId(int nodeId) {
        OnmsCriteria criteria = new OnmsCriteria(DataLinkInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.or(
            Restrictions.eq("node.id", nodeId),
            Restrictions.eq("nodeParentId", nodeId)
        ));
        
        return m_dataLinkDao.findMatching(criteria);
    }

    /** {@inheritDoc} */
    @Transactional(readOnly=true)
    public OnmsLinkState getLinkStateForInterface(DataLinkInterface dataLinkInterface) {
        return m_linkStateDao.findByDataLinkInterfaceId(dataLinkInterface.getId());
    }
    
    /** {@inheritDoc} */
    @Transactional
    public void updateLinkStatus(int nodeParentId, int nodeId, String status) {
        OnmsCriteria criteria = new OnmsCriteria(DataLinkInterface.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.add(Restrictions.eq("node.id", nodeId));
        criteria.add(Restrictions.eq("nodeParentId", nodeParentId));
        
        Collection<DataLinkInterface> dataLinkInterface = m_dataLinkDao.findMatching(criteria);
        
        if(dataLinkInterface.size() > 0){
            DataLinkInterface dataLink = dataLinkInterface.iterator().next();
            dataLink.setStatus(StatusType.get(status));
            
            m_dataLinkDao.update(dataLink);
            m_dataLinkDao.flush();
        }
    }

    /** {@inheritDoc} */
    @Transactional(readOnly=true)
    public String getPrimaryAddress(int nodeId) {
        OnmsNode node = m_nodeDao.get(nodeId);
        OnmsIpInterface primaryInterface = node.getPrimaryInterface();
        
        if(node != null && primaryInterface != null) {
            return InetAddressUtils.str(primaryInterface.getIpAddress());
        }
        
        return null;
    }
    
    /** {@inheritDoc} */
    @Transactional(readOnly=true)
    public boolean nodeHasEndPointService(int nodeId) {
        
        OnmsMonitoredService endPointService = m_monitoredServiceDao.getPrimaryService(nodeId, m_endPointConfigDao.getValidator().getServiceName());

        return endPointService == null ? false : true;
    }

    /** {@inheritDoc} */
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
