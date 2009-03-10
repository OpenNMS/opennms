/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: December 16, 2008
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.provision;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.RWSConfig;
import org.opennms.netmgt.config.RancidAdapterConfig;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.EventSubscriptionService;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.rancid.ConnectionProperties;
import org.opennms.rancid.RWSClientApi;
import org.opennms.rancid.RancidNode;
import org.opennms.rancid.RancidNodeAuthentication;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * A Rancid provisioning adapter for integration with OpenNMS Provisoning daemon API.
 * 
 * @author <a href="mailto:guglielmoincisa@gmail.com">Guglielmo Incisa</a>
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 *
 */

@EventListener(name="RancidProvisioningAdapter:Listener")
public class RancidProvisioningAdapter extends SimpleQueuedProvisioningAdapter implements InitializingBean, org.opennms.netmgt.model.events.EventListener {
    
    private NodeDao m_nodeDao;
    private volatile EventForwarder m_eventForwarder;
    private volatile EventSubscriptionService m_eventSubscriptionService;

    private RWSConfig m_rwsConfig;
    private RancidAdapterConfig m_rancidAdapterConfig;
    private ConnectionProperties m_cp;
    
    private static final String MESSAGE_PREFIX = "Rancid provisioning failed: ";
    private static final String ADAPTER_NAME="RancidProvisioningAdapter";
    private static final String RANCID_COMMENT="node provisioned by opennms";

    public static final String NAME = "RancidProvisioningAdapter";
    private volatile static ConcurrentMap<Integer, RancidNodeContainer> m_onmsNodeRancidNodeMap;

    @Override
    AdapterOperationSchedule createScheduleForNode(int nodeId, AdapterOperationType type) {
        if (type.equals(AdapterOperationType.CONFIG_CHANGE)) {
            String ipaddress = getSuitableIpForRancid(nodeId);
            return new AdapterOperationSchedule(m_rancidAdapterConfig.getDelay(ipaddress),60000, m_rancidAdapterConfig.getRetries(ipaddress), TimeUnit.MILLISECONDS);
        }
        return new AdapterOperationSchedule();
    }

 
    public void afterPropertiesSet() throws Exception {

        RWSClientApi.init();
        Assert.notNull(m_rwsConfig, "Rancid Provisioning Adapter requires RWSConfig property to be set.");
        
        m_cp = getRWSConnection();
        
        Assert.notNull(m_nodeDao, "Rancid Provisioning Adapter requires nodeDao property to be set.");
        
        createMessageSelectorAndSubscribe();
        buildRancidNodeMap();
    }

    @Transactional
    private void buildRancidNodeMap() {
        List<OnmsNode> nodes = m_nodeDao.findAllProvisionedNodes();
        m_onmsNodeRancidNodeMap = new ConcurrentHashMap<Integer, RancidNodeContainer>(nodes.size());
        

        for (OnmsNode onmsNode : nodes) {
            RancidNode rNode = getSuitableRancidNode(onmsNode);
            if (rNode != null) {
                RancidNodeAuthentication rAuth = getSuitableRancidNodeAuthentication(onmsNode);
            
                m_onmsNodeRancidNodeMap.putIfAbsent(onmsNode.getId(), new RancidNodeContainer(rNode, rAuth));
            }
        }
    }


    private ConnectionProperties getRWSConnection() {
        log().debug("Connections used : " +m_rwsConfig.getBaseUrl().getServer_url()+m_rwsConfig.getBaseUrl().getDirectory());
        log().debug("RWS timeout(sec): "+m_rwsConfig.getBaseUrl().getTimeout());        
        return new ConnectionProperties(m_rwsConfig.getBaseUrl().getServer_url(),m_rwsConfig.getBaseUrl().getDirectory(),m_rwsConfig.getBaseUrl().getTimeout());
    }

    private class RancidNodeContainer {
        private RancidNode m_node;
        private RancidNodeAuthentication m_auth;
        
        public RancidNodeContainer(RancidNode node, RancidNodeAuthentication auth) {
            setNode(node);
            setAuth(auth);
        }

        public void setNode(RancidNode node) {
            m_node = node;
        }

        public RancidNode getNode() {
            return m_node;
        }

        public void setAuth(RancidNodeAuthentication auth) {
            m_auth = auth;
        }

        public RancidNodeAuthentication getAuth() {
            return m_auth;
        }
    }

    @Transactional
    public void doAdd(int nodeId) throws ProvisioningAdapterException {
        log().debug("RANCID PROVISIONING ADAPTER CALLED addNode");
        try {
            OnmsNode node = m_nodeDao.get(nodeId);                                                                                                                                                                                            
            Assert.notNull(node, "Rancid Provisioning Adapter addNode method failed to return node for given nodeId:"+nodeId);
            
            RancidNode rNode = getSuitableRancidNode(node);
            rNode.setStateUp(true);
            RWSClientApi.createRWSRancidNode(m_cp, rNode);

            RancidNodeAuthentication rAuth = getSuitableRancidNodeAuthentication(node);
            RWSClientApi.createOrUpdateRWSAuthNode(m_cp, rAuth);
            
            m_onmsNodeRancidNodeMap.put(Integer.valueOf(nodeId), new RancidNodeContainer(rNode, rAuth));
            
        } catch (Exception e) {
            sendAndThrow(nodeId, e);
        }
    }

    @Transactional
    public void doUpdate(int nodeId) throws ProvisioningAdapterException {
        log().debug("RANCID PROVISIONING ADAPTER CALLED updateNode");
        try {
            OnmsNode node = m_nodeDao.get(nodeId);
            Assert.notNull(node, "Rancid Provisioning Adapter update Node method failed to return node for given nodeId:"+nodeId);
            
            // if the node exists and has different label then first delete old data
            if (m_onmsNodeRancidNodeMap.containsKey(Integer.valueOf(nodeId))) {
            RancidNode rNode = m_onmsNodeRancidNodeMap.get(Integer.valueOf(nodeId)).getNode();            
            RancidNodeAuthentication rAuth = m_onmsNodeRancidNodeMap.get(Integer.valueOf(nodeId)).getAuth();
                if (!rNode.getDeviceName().equals(node.getLabel())) {
                    RWSClientApi.deleteRWSRancidNode(m_cp, rNode);
                    RWSClientApi.deleteRWSAuthNode(m_cp, rAuth);
                }
            }
            
            RancidNode rNode = getSuitableRancidNode(node);
            RWSClientApi.createOrUpdateRWSRancidNode(m_cp, rNode);
            
            RancidNodeAuthentication rAuth = getSuitableRancidNodeAuthentication(node);
            RWSClientApi.createOrUpdateRWSAuthNode(m_cp, getSuitableRancidNodeAuthentication(node));
            
            m_onmsNodeRancidNodeMap.replace(node.getId(), new RancidNodeContainer(rNode, rAuth));
        } catch (Exception e) {
            sendAndThrow(nodeId, e);
        }
    }
    
    @Transactional
    public void doDelete(int nodeId) throws ProvisioningAdapterException {

        log().debug("RANCID PROVISIONING ADAPTER CALLED deleteNode");
        
        /*
         * The work to maintain the hashmap boils down to needing to do deletes, so
         * here we go.
         */
        try {

            RancidNode rNode = m_onmsNodeRancidNodeMap.get(Integer.valueOf(nodeId)).getNode();
            RWSClientApi.deleteRWSRancidNode(m_cp, rNode);
            
            RancidNodeAuthentication rAuth = m_onmsNodeRancidNodeMap.get(Integer.valueOf(nodeId)).getAuth();
            RWSClientApi.deleteRWSAuthNode(m_cp, rAuth);
            
            m_onmsNodeRancidNodeMap.remove(Integer.valueOf(nodeId));
        } catch (Exception e) {
            sendAndThrow(nodeId, e);
        }
    }

    public void doNodeConfigChanged(int nodeId) throws ProvisioningAdapterException {
        log().debug("RANCID PROVISIONING ADAPTER CALLED updateNode");
        try {
            if (m_onmsNodeRancidNodeMap.containsKey(Integer.valueOf(nodeId))) {
                RancidNode rNode = m_onmsNodeRancidNodeMap.get(Integer.valueOf(nodeId)).getNode();
                RWSClientApi.updateRWSRancidNode(m_cp, rNode);
            } else {
                throw new Exception("No node found for nodeid: " + nodeId);
            }
        } catch (Exception e) {
            sendAndThrow(nodeId, e);
        }
    }
    
    private void sendAndThrow(int nodeId, Exception e) {
        log().debug("RANCID PROVISIONING ADAPTER CALLED sendAndThrow");
        Event event = buildEvent(EventConstants.PROVISIONING_ADAPTER_FAILED, nodeId).addParam("reason", MESSAGE_PREFIX+e.getLocalizedMessage()).getEvent();
        m_eventForwarder.sendNow(event);
        throw new ProvisioningAdapterException(MESSAGE_PREFIX, e);
    }

    private EventBuilder buildEvent(String uei, int nodeId) {
        log().debug("RANCID PROVISIONING ADAPTER CALLED EventBuilder");
        EventBuilder builder = new EventBuilder(uei, "Provisioner", new Date());
        builder.setNodeid(nodeId);
        return builder;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }
    public void setNodeDao(NodeDao dao) {
        m_nodeDao = dao;
    }
    
    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }
    
    private static Category log() {
        return ThreadCategory.getInstance(RancidProvisioningAdapter.class);
    }

    public RWSConfig getRwsConfig() {
        return m_rwsConfig;
    }

    public void setRwsConfig(RWSConfig rwsConfig) {
        m_rwsConfig = rwsConfig;
    }

    public RancidAdapterConfig getRancidAdapterConfig() {
        return m_rancidAdapterConfig;
    }

    public void setRancidAdapterConfig(RancidAdapterConfig rancidAdapterConfig) {
        m_rancidAdapterConfig = rancidAdapterConfig;
    }

    public String getName() {
        return ADAPTER_NAME;
    }

    private String getSuitableIpForRancid(int nodeid){
        OnmsNode node = m_nodeDao.get(nodeid);
        OnmsIpInterface primaryInterface = node.getPrimaryInterface();
        
        if (primaryInterface == null) {
            Set<OnmsIpInterface> ipInterfaces = node.getIpInterfaces();
            for (OnmsIpInterface onmsIpInterface : ipInterfaces) {
                    return onmsIpInterface.getIpAddress();
            }
        }
        return primaryInterface.getIpAddress();
    }
    
    private RancidNode getSuitableRancidNode(OnmsNode node) {
        

        //The group should be the foreign source of the node

        String group = node.getForeignSource();

        if (group == null) return null;
        RancidNode r_node = new RancidNode(group, node.getLabel());

        //FIXME: Check the node categories if useNodecategories is true
        r_node.setDeviceType(m_rancidAdapterConfig.getType(node.getSysObjectId()));
        r_node.setStateUp(false);
        r_node.setComment(RANCID_COMMENT);
        return r_node;

    }
    
    private RancidNodeAuthentication getSuitableRancidNodeAuthentication(OnmsNode node) {
        // RancidAutentication
        RancidNodeAuthentication r_auth_node = new RancidNodeAuthentication();
        r_auth_node.setDeviceName(node.getLabel());
        OnmsAssetRecord asset_node = node.getAssetRecord();

        if (asset_node.getUsername() != null) {
            r_auth_node.setUser(asset_node.getUsername());
        }
        
        if (asset_node.getPassword() != null) {
            r_auth_node.setPassword(asset_node.getPassword());
        }

        if (asset_node.getEnable() != null) {
            r_auth_node.setEnablePass(asset_node.getEnable());
        }
        
        if (asset_node.getAutoenable() != null) {
            r_auth_node.setAutoEnable(asset_node.getAutoenable().equals(OnmsAssetRecord.AUTOENABLED));
        }
        
        if (asset_node.getConnection() != null) {
            r_auth_node.setConnectionMethod(asset_node.getConnection());
        } else {
            r_auth_node.setConnectionMethod("telnet");
        }
        
        return r_auth_node;
    }

    @Override
    public boolean isNodeReady(AdapterOperation op) {
        if (op.getType() == AdapterOperationType.CONFIG_CHANGE) {
            Integer nodeid = op.getNodeId();
            updateRancidNodeState(nodeid, true);
            if ( m_rancidAdapterConfig.isCurTimeInSchedule(getSuitableIpForRancid(nodeid))) {
              return true;
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public void processPendingOperationForNode(AdapterOperation op) throws ProvisioningAdapterException {
        if (op.getType() == AdapterOperationType.ADD) {
            doAdd(op.getNodeId());
        } else if (op.getType() == AdapterOperationType.UPDATE) {
            doUpdate(op.getNodeId());
        } else if (op.getType() == AdapterOperationType.DELETE) {
            doDelete(op.getNodeId());
        } else if (op.getType() == AdapterOperationType.CONFIG_CHANGE) {
            doNodeConfigChanged(op.getNodeId());
        }
    }
    
    @EventHandler(uei = EventConstants.RANCID_DOWNLOAD_FAILURE_UEI)
    public void handleRancidDownLoadFailure(Event e) {
        log().debug("get Event uei/id: " + e.getUei() + "/" + e.getDbid());
        if (e.hasNodeid()) {
            int nodeId = Long.valueOf(e.getNodeid()).intValue();
            if (m_onmsNodeRancidNodeMap.containsKey(Integer.valueOf(nodeId))) {
                updateRancidNodeState(nodeId, true);
                doNodeConfigChanged(nodeId);
            } else {
                log().warn("node does not exist with nodeid: " + e.getNodeid());
            }
        }
    }

    @EventHandler(uei = EventConstants.RANCID_DOWNLOAD_SUCCESS_UEI)
    public void handleRancidDownLoadSuccess(Event e) {
        log().debug("get Event uei/id: " + e.getUei() + "/" + e.getDbid());
        if (e.hasNodeid() ) {
            int nodeId = Long.valueOf(e.getNodeid()).intValue();
            if (m_onmsNodeRancidNodeMap.containsKey(Integer.valueOf(nodeId))) {
                updateRancidNodeState(nodeId, false);
                doNodeConfigChanged(nodeId);
            } else {
                log().warn("node does not exist with nodeid: " + e.getNodeid());
            }
        }
    }

    private void updateRancidNodeState(int nodeid, boolean up) {
        RancidNodeContainer rcont = m_onmsNodeRancidNodeMap.get(Integer.valueOf(nodeid));
        RancidNode rnode = rcont.getNode();
        rnode.setStateUp(up);
        rcont.setNode(rnode);
        m_onmsNodeRancidNodeMap.put(nodeid, rcont);
    }

    public EventSubscriptionService getEventSubscriptionService() {
        return m_eventSubscriptionService;
    }


    public void setEventSubscriptionService(
            EventSubscriptionService eventSubscriptionService) {
        m_eventSubscriptionService = eventSubscriptionService;
    }


    public void onEvent(Event e) {
        if (e == null)
            return;
       
        if (e.getUei().equals(EventConstants.RANCID_DOWNLOAD_FAILURE_UEI))
            handleRancidDownLoadFailure(e);
        else if (e.getUei().equals(EventConstants.RANCID_DOWNLOAD_SUCCESS_UEI))
            handleRancidDownLoadSuccess(e);
    }

    private void createMessageSelectorAndSubscribe() {
        
        List<String> ueiList = new ArrayList<String>();
        ueiList.add(EventConstants.RANCID_DOWNLOAD_FAILURE_UEI);
        ueiList.add(EventConstants.RANCID_DOWNLOAD_SUCCESS_UEI);
        
        getEventSubscriptionService().addEventListener(this, ueiList);
    }

}
