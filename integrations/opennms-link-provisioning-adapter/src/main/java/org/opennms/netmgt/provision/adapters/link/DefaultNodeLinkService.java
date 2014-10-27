/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.adapters.link;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.AnyRestriction;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.api.LinkStateDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLinkState;
import org.opennms.netmgt.model.OnmsLinkState.LinkState;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.provision.adapters.link.endpoint.dao.EndPointConfigurationDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOG = LoggerFactory.getLogger(DefaultNodeLinkService.class);
    
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
    @Override
    public void saveLinkState(OnmsLinkState state) {
        LOG.debug("saving LinkState {}", state.getLinkState());
        m_linkStateDao.saveOrUpdate(state);
        m_linkStateDao.flush();
    }
    
    /** {@inheritDoc} */
    @Transactional
    @Override
    public void createLink(final int nodeParentId, final int nodeId) {
        LOG.info("adding link between node: {} and node: {}", nodeParentId, nodeId);
        final OnmsNode parentNode = m_nodeDao.get(nodeParentId);
        Assert.notNull(parentNode, "node with id: " + nodeParentId + " does not exist");
        
        final OnmsNode node = m_nodeDao.get(nodeId);
        Assert.notNull(node, "node with id: " + nodeId + " does not exist");
        
        final Criteria criteria = new Criteria(DataLinkInterface.class);
        criteria.setAliases(Arrays.asList(new Alias[] {
            new Alias("node", "node", JoinType.LEFT_JOIN)
        }));
        criteria.addRestriction(new EqRestriction("node.id", nodeId));
        criteria.addRestriction(new EqRestriction("nodeParentId", nodeParentId));
        
        final Collection<DataLinkInterface> dataLinkInterface = m_dataLinkDao.findMatching(criteria);
        DataLinkInterface dli = null;
        
        if (dataLinkInterface.size() > 1) {
            LOG.warn("more than one data link interface exists for nodes {} and {}", nodeParentId, nodeId);
            return;
        } else if (dataLinkInterface.size() > 0) {
            dli = dataLinkInterface.iterator().next();
            LOG.info("link between nodes {} and {} already exists", nodeParentId, nodeId);
        } else {
            dli = new DataLinkInterface();
            dli.setNode(node);
            dli.setNodeParentId(nodeParentId);
            dli.setIfIndex(getPrimaryIfIndexForNode(node));
            dli.setParentIfIndex(getPrimaryIfIndexForNode(parentNode));
            LOG.info("creating new link between nodes {} and {}", nodeParentId, nodeId);
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
    @Override
    public Integer getNodeId(String endPoint) {
        if (endPoint == null){
            return null;
        }
        Collection<OnmsNode> nodes = m_nodeDao.findByLabel(endPoint);
        if(nodes.size() > 0){
            return nodes.iterator().next().getId();
        }
        return null;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly=true)
    @Override
    public String getNodeLabel(int nodeId) {
        OnmsNode node = m_nodeDao.get(nodeId);
        if(node != null){
            return node.getLabel(); 
        }
        return null;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly=true)
    @Override
    public Collection<DataLinkInterface> getLinkContainingNodeId(int nodeId) {
        Criteria criteria = new Criteria(DataLinkInterface.class);
        criteria.setAliases(Arrays.asList(new Alias[] {
            new Alias("node", "node", JoinType.LEFT_JOIN)
        }));
        criteria.addRestriction(new AnyRestriction(
            new EqRestriction("node.id", nodeId),
            new EqRestriction("nodeParentId", nodeId)
        ));
        
        return m_dataLinkDao.findMatching(criteria);
    }

    /** {@inheritDoc} */
    @Transactional(readOnly=true)
    @Override
    public OnmsLinkState getLinkStateForInterface(DataLinkInterface dataLinkInterface) {
        return m_linkStateDao.findByDataLinkInterfaceId(dataLinkInterface.getId());
    }
    
    /** {@inheritDoc} */
    @Transactional
    @Override
    public void updateLinkStatus(int nodeParentId, int nodeId, String status) {
        Criteria criteria = new Criteria(DataLinkInterface.class);
        criteria.setAliases(Arrays.asList(new Alias[] {
            new Alias("node", "node", JoinType.LEFT_JOIN)
        }));
        criteria.addRestriction(new EqRestriction("node.id", nodeId));
        criteria.addRestriction(new EqRestriction("nodeParentId", nodeParentId));
        
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
    @Override
    public String getPrimaryAddress(int nodeId) {
        OnmsNode node = m_nodeDao.get(nodeId);
        if (node != null) {
            OnmsIpInterface primaryInterface = node.getPrimaryInterface();
            if(primaryInterface != null) {
                return InetAddressUtils.str(primaryInterface.getIpAddress());
            }
        }
        
        return null;
    }
    
    /** {@inheritDoc} */
    @Transactional(readOnly=true)
    @Override
    public boolean nodeHasEndPointService(int nodeId) {
        
        OnmsMonitoredService endPointService = m_monitoredServiceDao.getPrimaryService(nodeId, m_endPointConfigDao.getValidator().getServiceName());

        return endPointService == null ? false : true;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly=true)
    @Override
    public Boolean getEndPointStatus(int nodeId) {
        OnmsMonitoredService endPointService = m_monitoredServiceDao.getPrimaryService(nodeId, m_endPointConfigDao.getValidator().getServiceName());
        if (endPointService == null) {
        	return null;
        }

        // want true to be UP, not DOWN
        return !endPointService.isDown();
    }
}
