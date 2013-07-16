/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.RWSConfig;
import org.opennms.netmgt.config.RancidAdapterConfig;
import org.opennms.netmgt.config.RancidAdapterConfigFactory;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.rancid.ConnectionProperties;
import org.opennms.rancid.RWSClientApi;
import org.opennms.rancid.RancidApiException;
import org.opennms.rancid.RancidNode;
import org.opennms.rancid.RancidNodeAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

/**
 * A Rancid provisioning adapter for integration with OpenNMS Provisioning daemon API.
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */

@EventListener(name="RancidProvisioningAdapter")
public class RancidProvisioningAdapter extends SimpleQueuedProvisioningAdapter implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(RancidProvisioningAdapter.class);
    
    private NodeDao m_nodeDao;
    private volatile EventForwarder m_eventForwarder;

    private RWSConfig m_rwsConfig;
    private RancidAdapterConfig m_rancidAdapterConfig;
    private ConnectionProperties m_cp;
    
    private List<String> m_rancid_categories;
    
    private TransactionTemplate m_template;
    
    /**
     * <p>getTemplate</p>
     *
     * @return a {@link org.springframework.transaction.support.TransactionTemplate} object.
     */
    public TransactionTemplate getTemplate() {
        return m_template;
    }
    /**
     * <p>setTemplate</p>
     *
     * @param template a {@link org.springframework.transaction.support.TransactionTemplate} object.
     */
    public void setTemplate(TransactionTemplate template) {
        m_template = template;
    }

    private static final String MESSAGE_PREFIX = "Rancid provisioning failed: ";
    private static final String ADAPTER_NAME="RancidProvisioningAdapter";
    private static final String RANCID_COMMENT="node provisioned by opennms";

    /** Constant <code>NAME="RancidProvisioningAdapter"</code> */
    public static final String NAME = "RancidProvisioningAdapter";
    private volatile static ConcurrentMap<Integer, RancidNode> m_onmsNodeRancidNodeMap;
    private volatile static ConcurrentMap<Integer, String> m_onmsNodeIpMap;
    

    @Override
    AdapterOperationSchedule createScheduleForNode(final int nodeId, AdapterOperationType adapterOperationType) {
        LOG.debug("Scheduling: {} for nodeid: {}", nodeId, adapterOperationType);
        if (adapterOperationType.equals(AdapterOperationType.CONFIG_CHANGE)) {
            updateRancidNodeState(nodeId, true);

            String ipaddress = m_onmsNodeIpMap.get(nodeId);
            //String ipaddress = (String) m_template.execute(new TransactionCallback() {
            //    public Object doInTransaction(TransactionStatus arg0) {
            //        return getSuitableIpForRancid(nodeId);
            //    }
            //});
            
            LOG.debug("Found Suitable ip address: {}", ipaddress);
            long initialDelay = m_rancidAdapterConfig.getDelay(ipaddress);
            int retries = m_rancidAdapterConfig.getRetries(ipaddress);
            LOG.debug("Setting initialDelay(sec): {}", initialDelay);
            LOG.debug("Setting retries(sec): {}", retries);
            
            return new AdapterOperationSchedule(initialDelay,60, retries, TimeUnit.SECONDS);
        }
        return new AdapterOperationSchedule();
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        RWSClientApi.init();
        Assert.notNull(m_rwsConfig, "Rancid Provisioning Adapter requires RWSConfig property to be set.");
        
        m_cp = getRWSConnection();
        
        Assert.notNull(m_nodeDao, "Rancid Provisioning Adapter requires nodeDao property to be set.");
        
        getRancidCategories();
        
        m_template.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus arg0) {
                buildRancidNodeMap();
            }
        });        
    }

    private void getRancidCategories() {
        
        try {
            m_rancid_categories = RWSClientApi.getRWSResourceDeviceTypesPatternList(m_cp).getResource();
        } catch (RancidApiException e) {
            ConnectionProperties cp = getStandByRWSConnection();
            if (cp != null) {
                try {
                    m_rancid_categories = RWSClientApi.getRWSResourceDeviceTypesPatternList(m_cp).getResource();
                } catch (RancidApiException e1) {
                    LOG.warn("getRancidCategories: not able to retrieve rancid categories from RWS server");
                    m_rancid_categories = new ArrayList<String>();
                    m_rancid_categories.add("cisco");
                    LOG.warn("getRancidCategories: setting categories list to 'cisco'");
                }
            }
        }
    }

    private void buildRancidNodeMap() {
        List<OnmsNode> nodes = m_nodeDao.findAllProvisionedNodes();
        m_onmsNodeRancidNodeMap = new ConcurrentHashMap<Integer, RancidNode>(nodes.size());
        m_onmsNodeIpMap = new ConcurrentHashMap<Integer, String>(nodes.size());

        for (OnmsNode onmsNode : nodes) {
            String ipaddr = getSuitableIpForRancid(onmsNode);
            if (ipaddr != null)
                m_onmsNodeIpMap.putIfAbsent(onmsNode.getId(), ipaddr);
            
            RancidNode rNode = getSuitableRancidNode(onmsNode);
            if (rNode != null) {            
                m_onmsNodeRancidNodeMap.putIfAbsent(onmsNode.getId(), rNode);
            }
        }
    }

    private ConnectionProperties getRWSConnection() {
        return m_rwsConfig.getBase();
    }

    private ConnectionProperties getStandByRWSConnection() {
        return m_rwsConfig.getNextStandBy();
    }

    /**
     * <p>doAdd</p>
     *
     * @param nodeId a int.
     * @param cp a {@link org.opennms.rancid.ConnectionProperties} object.
     * @param retry a boolean.
     * @throws org.opennms.netmgt.provision.ProvisioningAdapterException if any.
     */
    public void doAdd(int nodeId, ConnectionProperties cp, boolean retry) throws ProvisioningAdapterException {
        LOG.debug("doAdd: adding nodeid: {}", nodeId);

        final OnmsNode node = m_nodeDao.get(nodeId);                                                                                                                                                                                            
        Assert.notNull(node, "doAdd: failed to return node for given nodeId:"+nodeId);

        String ipaddress = m_template.execute(new TransactionCallback<String>() {
            @Override
            public String doInTransaction(TransactionStatus arg0) {
                return getSuitableIpForRancid(node);
            }
        });

        
        RancidNode rNode = getSuitableRancidNode(node);
        rNode.setStateUp(true);
        
        try {
            m_rwsConfig.getWriteLock().lock();
            try {
                if (m_onmsNodeRancidNodeMap.containsValue(rNode)) {
                    LOG.error("doAdd: Error Duplicate node: {}", node);
                    ProvisioningAdapterException e = new ProvisioningAdapterException("Duplicate node has been added: "+node); 
                    sendAndThrow(nodeId, e);
                    return;
                }
                LOG.debug("doAdd: adding to router.db node: {}", node.getLabel());
    
                RWSClientApi.createRWSRancidNode(cp, rNode);
                m_onmsNodeIpMap.putIfAbsent(nodeId, ipaddress);
                m_onmsNodeRancidNodeMap.put(Integer.valueOf(nodeId), rNode);
    
                RWSClientApi.createOrUpdateRWSAuthNode(cp, rNode.getAuth());
            } finally {
                m_rwsConfig.getWriteLock().unlock();
            }
        } catch (ProvisioningAdapterException ae) {    
            sendAndThrow(nodeId, ae);
        } catch (Throwable e) {
            cp = getStandByRWSConnection();
            if (retry && cp != null) {
                LOG.info("doAdd: retry Add on standByConn: {}", cp.getUrl());
                doAdd(nodeId, cp, false);
            } else {
                sendAndThrow(nodeId, e);            
            }
        }
    }

    /**
     * <p>doUpdate</p>
     *
     * @param nodeId a int.
     * @param cp a {@link org.opennms.rancid.ConnectionProperties} object.
     * @param retry a boolean.
     * @throws org.opennms.netmgt.provision.ProvisioningAdapterException if any.
     */
    public void doUpdate(int nodeId, ConnectionProperties cp, boolean retry) throws ProvisioningAdapterException {
        LOG.debug("doUpdate: updating nodeid: {}", nodeId);
            
        RancidNode rLocalNode = m_onmsNodeRancidNodeMap.get(Integer.valueOf(nodeId));
        LOG.debug("doUpdate: found local map Node: {}", rLocalNode);
        
        final OnmsNode node = m_nodeDao.get(nodeId);
        Assert.notNull(node, "doUpdate: failed to return node for given nodeId:"+nodeId);
 
        String ipaddress = m_template.execute(new TransactionCallback<String>() {
            @Override
            public String doInTransaction(TransactionStatus arg0) {
                return getSuitableIpForRancid(node);
            }
        });

        m_onmsNodeIpMap.put(nodeId, ipaddress);

        RancidNode rUpdatedNode = getSuitableRancidNode(node);
        LOG.debug("doUpdate: found updated Node : {}", rUpdatedNode);

        if (rLocalNode.getDeviceName().equalsIgnoreCase(rUpdatedNode.getDeviceName())) {            
            try {
                RancidNode rRemoteNode = RWSClientApi.getRWSRancidNodeTLO(cp, rLocalNode.getGroup(), rLocalNode.getDeviceName());
                RancidNodeAuthentication rRemoteNodeAuth = RWSClientApi.getRWSAuthNode(cp, rLocalNode.getDeviceName());
                LOG.debug("doUpdate: found Node in router.db : {}", rRemoteNode);
                if (!rUpdatedNode.getDeviceType().equalsIgnoreCase(rRemoteNode.getDeviceType())) {
                    try {
                        // don't change the status of the node in update operation
                        rUpdatedNode.setStateUp(rRemoteNode.isStateUp());
                        LOG.debug("doUpdate: updating router.db");
                        RWSClientApi.updateRWSRancidNode(cp, rLocalNode);
                    } catch (Throwable e) {
                        LOG.error("doUpdate: failed to update node: {} Exception: {}", e.getMessage(), nodeId);
                    }
                }
                
                if ( updateAuth(rUpdatedNode.getAuth(), rRemoteNodeAuth) ) {
                    LOG.debug("doUpdate: updating authentication data");
                    try {
                        RWSClientApi.updateRWSAuthNode(cp, rUpdatedNode.getAuth());                                                        
                    } catch (Throwable e) {
                        LOG.error("doUpdate: Failed to update node authentication data: {} Exception: {}", e.getMessage(), nodeId);
                    }
                }
                
                rUpdatedNode.setStateUp(rLocalNode.isStateUp());
                m_onmsNodeRancidNodeMap.put(nodeId, rUpdatedNode);
            
            } catch (RancidApiException re) {
                if (re.getRancidCode() ==RancidApiException.RWS_RESOURCE_NOT_FOUND) {
                    LOG.warn("doUpdate: node not found in router.db: {}", rUpdatedNode);
                    try {
                        LOG.debug("doUpdate: adding Node to router.db for nodeid: {}", nodeId);
                        rUpdatedNode.setStateUp(true);
                        RWSClientApi.createRWSRancidNode(cp, rUpdatedNode);
                        RWSClientApi.createOrUpdateRWSAuthNode(cp, rUpdatedNode.getAuth());
                        m_onmsNodeRancidNodeMap.put(nodeId, rUpdatedNode);
                    } catch (RancidApiException e) {
                        LOG.error("doUpdate: Failed to create node: {} Exception: {}", e.getMessage(), nodeId);
                        sendAndThrow(nodeId, e);            
                    }
                } else {
                    cp = getStandByRWSConnection();
                    if (retry && cp != null) {
                        LOG.info("doUpdate: retry Update on standByConn: {}", cp.getUrl());
                        doUpdate(nodeId, cp, false);
                    } else {
                        sendAndThrow(nodeId, re);            
                    }
                } 
            }
        } else {
            LOG.debug("doUpdate: the device name is changed for Nodeid: {}", nodeId);
        
            LOG.debug("doUpdate: calling doDelete for NodeId: {}", nodeId);
            doDelete(nodeId, cp, retry);
            
            try {
                LOG.debug("doUpdate: adding Node to router.db for nodeid: {}", nodeId);
                rUpdatedNode.setStateUp(true);
                RWSClientApi.createRWSRancidNode(cp, rUpdatedNode);
                RWSClientApi.createOrUpdateRWSAuthNode(cp, rUpdatedNode.getAuth());
                m_onmsNodeRancidNodeMap.put(nodeId, rUpdatedNode);
                m_onmsNodeIpMap.put(nodeId, ipaddress);
            } catch (RancidApiException e) {
                LOG.error("doUpdate: Failed to create node: {} Exception: {}", e.getMessage(), nodeId);
                sendAndThrow(nodeId, e);            
            }
        }
    }
    
    /**
     * <p>doDelete</p>
     *
     * @param nodeId a int.
     * @param cp a {@link org.opennms.rancid.ConnectionProperties} object.
     * @param retry a boolean.
     * @throws org.opennms.netmgt.provision.ProvisioningAdapterException if any.
     */
    public void doDelete(int nodeId,ConnectionProperties cp, boolean retry) throws ProvisioningAdapterException {

        LOG.debug("doDelete: deleting nodeid: {}", nodeId);
        
        /*
         * The work to maintain the hashmap boils down to needing to do deletes, so
         * here we go.
         */
        try {
            m_rwsConfig.getWriteLock().lock();
            try {
                if (m_onmsNodeRancidNodeMap.containsKey(Integer.valueOf(nodeId))) {
                    RancidNode rNode = m_onmsNodeRancidNodeMap.get(Integer.valueOf(nodeId));
    
                    RWSClientApi.deleteRWSRancidNode(cp, rNode);
                    RWSClientApi.deleteRWSAuthNode(cp, rNode.getAuth());
    
                    m_onmsNodeRancidNodeMap.remove(Integer.valueOf(nodeId));
                    m_onmsNodeIpMap.remove(Integer.valueOf(nodeId));
                } else {
                    LOG.warn("doDelete: no device found in node Rancid Map for nodeid: {}", nodeId);
                }
            } finally {
                m_rwsConfig.getWriteLock().unlock();
            }
        } catch (Throwable e) {
            cp = getStandByRWSConnection();
            if (retry && cp != null) {
                LOG.info("doDelete: retry Delete on standByConn: {}", cp.getUrl());
                doDelete(nodeId, cp, false);
            } else {
                sendAndThrow(nodeId, e);            
            }
        }
    }

    /**
     * <p>doNodeConfigChanged</p>
     *
     * @param nodeId a int.
     * @param cp a {@link org.opennms.rancid.ConnectionProperties} object.
     * @param retry a boolean.
     * @throws org.opennms.netmgt.provision.ProvisioningAdapterException if any.
     */
    public void doNodeConfigChanged(int nodeId,ConnectionProperties cp, boolean retry) throws ProvisioningAdapterException {
        LOG.debug("doNodeConfigChanged: nodeid: {}", nodeId);
            if (m_onmsNodeRancidNodeMap.containsKey(Integer.valueOf(nodeId))) {
                updateConfiguration(nodeId,m_onmsNodeRancidNodeMap.get(Integer.valueOf(nodeId)),cp, retry);
            } else {
                LOG.warn("doNodeConfigChanged: No node found in nodeRancid Map for nodeid: {}", nodeId);
            }
    }

    private void updateConfiguration(int nodeid, RancidNode rNode,ConnectionProperties cp, boolean retry) throws ProvisioningAdapterException {
        LOG.debug("updateConfiguration: Updating Rancid Router.db configuration for node: {} type: {} group: {}", rNode.getGroup(), rNode.getDeviceName(), rNode.getDeviceType());
        try {
                RWSClientApi.updateRWSRancidNode(cp, rNode);
        } catch (Throwable e) {
            cp = getStandByRWSConnection();
            if (retry && cp != null) {
                LOG.info("updateConfiguration: retry update on standByConn: {}", cp.getUrl());
                updateConfiguration(nodeid, rNode, cp, false);
            } else {
                sendAndThrow(nodeid, e);            
            }
        }
    }

    private void sendAndThrow(int nodeId, Throwable e) {
        LOG.debug("sendAndThrow: error working on nodeid: {}", nodeId);
        LOG.debug("sendAndThrow: Exception: {}", e.getMessage());
        Event event = buildEvent(EventConstants.PROVISIONING_ADAPTER_FAILED, nodeId).addParam("reason", MESSAGE_PREFIX+e.getLocalizedMessage()).getEvent();
        m_eventForwarder.sendNow(event);
        throw new ProvisioningAdapterException(MESSAGE_PREFIX, e);
    }

    private EventBuilder buildEvent(String uei, int nodeId) {
        EventBuilder builder = new EventBuilder(uei, "Provisioner", new Date());
        builder.setNodeid(nodeId);
        return builder;
    }

    /**
     * <p>getNodeDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }
    /**
     * <p>setNodeDao</p>
     *
     * @param dao a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public void setNodeDao(NodeDao dao) {
        m_nodeDao = dao;
    }
    
    /**
     * <p>setEventForwarder</p>
     *
     * @param eventForwarder a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     */
    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    /**
     * <p>getEventForwarder</p>
     *
     * @return a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     */
    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }
    
    /**
     * <p>getRwsConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.RWSConfig} object.
     */
    public RWSConfig getRwsConfig() {
        return m_rwsConfig;
    }

    /**
     * <p>setRwsConfig</p>
     *
     * @param rwsConfig a {@link org.opennms.netmgt.config.RWSConfig} object.
     */
    public void setRwsConfig(RWSConfig rwsConfig) {
        m_rwsConfig = rwsConfig;
    }

    /**
     * <p>getRancidAdapterConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.RancidAdapterConfig} object.
     */
    public RancidAdapterConfig getRancidAdapterConfig() {
        return m_rancidAdapterConfig;
    }

    /**
     * <p>setRancidAdapterConfig</p>
     *
     * @param rancidAdapterConfig a {@link org.opennms.netmgt.config.RancidAdapterConfig} object.
     */
    public void setRancidAdapterConfig(RancidAdapterConfig rancidAdapterConfig) {
        m_rancidAdapterConfig = rancidAdapterConfig;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return ADAPTER_NAME;
    }

    private String getSuitableIpForRancid(OnmsNode node){
        LOG.debug("getSuitableIpForRancid: node: {} Foreign Source: {}", node.getForeignSource(), node.getNodeId());
        OnmsIpInterface primaryInterface = node.getPrimaryInterface();
        String ipaddr = "127.0.0.1";
        if (primaryInterface == null) {
            LOG.debug("getSuitableIpForRancid: found null SNMP Primary Interface, getting interfaces");
            Set<OnmsIpInterface> ipInterfaces = node.getIpInterfaces();
            for (OnmsIpInterface onmsIpInterface : ipInterfaces) {
                LOG.debug("getSuitableIpForRancid: trying Interface with id: {}", onmsIpInterface.getId());
                if (onmsIpInterface.getIpAddress() != null) {
                    ipaddr = str(onmsIpInterface.getIpAddress());
                } else { 
                    LOG.debug("getSuitableIpForRancid: found null ip address on Interface with id: {}", onmsIpInterface.getId());
                }
            }
        } else {        
            LOG.debug("getSuitableIpForRancid: found SNMP Primary Interface");
            if (primaryInterface.getIpAddress() != null ) {
                ipaddr = str(primaryInterface.getIpAddress());
            } else {
                LOG.debug("getSuitableIpForRancid: found null ip address on Primary Interface");
            }
        }
        return ipaddr;
    }
    
    private RancidNode getSuitableRancidNode(OnmsNode node) {
        

        //The group should be the foreign source of the node

        String group = node.getForeignSource();

        if (group == null) return null;
        RancidNode r_node = new RancidNode(group, node.getLabel());

        String ipaddress = m_onmsNodeIpMap.get(node.getId());

        if (m_rancidAdapterConfig.useCategories(ipaddress)) {
            LOG.debug("getSuitableRancidNode: Using Categories to get Rancid devicetype for node: {}", node.getLabel());
            r_node.setDeviceType(getTypeFromCategories(node)); 
        } else {
            LOG.debug("getSuitableRancidNode: Using Sysoid to get Rancid devicetype for node: {}", node.getLabel());
            r_node.setDeviceType(getTypeFromSysObjectId(node.getSysObjectId()));
        }
        r_node.setStateUp(false);
        r_node.setComment(RANCID_COMMENT);
        r_node.setAuth(getSuitableRancidNodeAuthentication(node));
        return r_node;
        

    }
    
    private String getTypeFromSysObjectId(String sysoid) {
        String rancidType = m_rancidAdapterConfig.getType(sysoid);
        LOG.debug("getTypeFromSysObjectId: Rancid devicetype found: {} for sysOid: {}", sysoid, rancidType);
        return rancidType;
    }
    
    private String getTypeFromCategories(OnmsNode node) {
        for (String rancidType: m_rancid_categories) {
            for (OnmsCategory nodecategory: node.getCategories()) {
                if (nodecategory.getName().equalsIgnoreCase(rancidType)) {
                    LOG.debug("getTypeFromCategories: Found Matching Category: Rancid devicetype found: {}", rancidType);
                    return rancidType;
                }
            }
        }
        LOG.warn("getTypeFromCategories: No Matching Category found: trying to get devicetype using config file");
        return getTypeFromCategories(node);
    }

    private RancidNodeAuthentication getSuitableRancidNodeAuthentication(OnmsNode node) {
        // RancidAuthentication
        RancidNodeAuthentication r_auth_node = new RancidNodeAuthentication();
        r_auth_node.setDeviceName(node.getLabel());
        OnmsAssetRecord asset_node = node.getAssetRecord();

        // Seth 2011-09-12: Is this possible? I added this as defensive code against issue NMS-4475
        //
        // http://issues.opennms.org/browse/NMS-4475
        //
        if (asset_node == null) {
            return r_auth_node;
        }

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

    /** {@inheritDoc} */
    @Override
    public boolean isNodeReady(final AdapterOperation op) {
        boolean ready = true;
        if (op.getType() == AdapterOperationType.CONFIG_CHANGE) {
            ready =
            m_rancidAdapterConfig.isCurTimeInSchedule(m_onmsNodeIpMap.get(op.getNodeId()));
        }
        LOG.debug("isNodeReady: {} For Operation {} for node: {}", op.getNodeId(), ready, op.getType());
        return ready;
    }

    /** {@inheritDoc} */
    @Override
    public void processPendingOperationForNode(final AdapterOperation op) throws ProvisioningAdapterException {
        LOG.debug("processPendingOperationForNode: {} for node: {}", op.getNodeId(), op.getType());
        if (op.getType() == AdapterOperationType.ADD) {
            m_template.execute(new TransactionCallbackWithoutResult() {
                @Override
                public void doInTransactionWithoutResult(TransactionStatus arg0) {
                    doAdd(op.getNodeId(),m_cp,true);
                }
            });
        } else if (op.getType() == AdapterOperationType.UPDATE) {
            m_template.execute(new TransactionCallbackWithoutResult() {
                @Override
                public void doInTransactionWithoutResult(TransactionStatus arg0) {
                    doUpdate(op.getNodeId(),m_cp,true);
                }
            });
        } else if (op.getType() == AdapterOperationType.DELETE) {
            m_template.execute(new TransactionCallbackWithoutResult() {
                @Override
                public void doInTransactionWithoutResult(TransactionStatus arg0) {
                    doDelete(op.getNodeId(),m_cp,true);
                }
            });
        } else if (op.getType() == AdapterOperationType.CONFIG_CHANGE) {
            m_template.execute(new TransactionCallbackWithoutResult() {
                @Override
                public void doInTransactionWithoutResult(TransactionStatus arg0) {
                    doNodeConfigChanged(op.getNodeId(),m_cp,true);
                }
            });
        }
    }

    /**
     * <p>handleReloadConfigEvent</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadConfigEvent(Event event) {
        if (isReloadConfigEventTarget(event)) {
            LOG.debug("reloading the rancid adapter configuration");
            try {
                RancidAdapterConfigFactory.init();
                final RancidAdapterConfigFactory factory = RancidAdapterConfigFactory.getInstance();
                factory.getWriteLock().lock();
                try {
                    factory.update();
                    m_template.execute(new TransactionCallbackWithoutResult() {
                        @Override
                        public void doInTransactionWithoutResult(TransactionStatus arg0) {
                            buildRancidNodeMap();
                        }
                    });  
                } finally {
                    factory.getWriteLock().unlock();
                }
            } catch (Throwable e) {
                LOG.info("unable to reload rancid adapter configuration", e);
            }
        }
    }

    private boolean isReloadConfigEventTarget(Event event) {
        boolean isTarget = false;
        
        List<Parm> parmCollection = event.getParmCollection();

        for (Parm parm : parmCollection) {
            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && "Provisiond.RancidProvisioningAdapter".equalsIgnoreCase(parm.getValue().getContent())) {
                isTarget = true;
                break;
            }
        }
        
        LOG.debug("isReloadConfigEventTarget: Provisiond.RancidProvisioningAdapter was target of reload event: {}", isTarget);
        return isTarget;
    }

    /**
     * <p>handleRancidDownLoadFailure</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.RANCID_DOWNLOAD_FAILURE_UEI)
    public void handleRancidDownLoadFailure(Event e) {
        LOG.debug("handleRancidDownLoadFailure: get Event uei/id: {} / {}", e.getDbid(), e.getUei());
        if (e.hasNodeid()) {
            int nodeId = Long.valueOf(e.getNodeid()).intValue();
            if (m_onmsNodeRancidNodeMap.containsKey(Integer.valueOf(nodeId))) {
                updateRancidNodeState(nodeId, false);
            } else {
                LOG.warn("node does not exist with nodeid: {}", e.getNodeid());
            }
        }
    }

    /**
     * <p>handleRancidDownLoadSuccess</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.RANCID_DOWNLOAD_SUCCESS_UEI)
    public void handleRancidDownLoadSuccess(Event e) {
        LOG.debug("handleRancidDownLoadSuccess: get Event uei/id: {} / {}", e.getDbid(), e.getUei());
        if (e.hasNodeid() ) {
            int nodeId = Long.valueOf(e.getNodeid()).intValue();
            if (m_onmsNodeRancidNodeMap.containsKey(Integer.valueOf(nodeId))) {
                updateRancidNodeState(nodeId, false);
            } else {
                LOG.warn("node does not exist with nodeid: {}", e.getNodeid());
            }
        }
    }

    /**
     * <p>handleRancidGroupProcessingCompleted</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.RANCID_GROUP_PROCESSING_COMPLETED_UEI)
    public void handleRancidGroupProcessingCompleted(Event e) {
        LOG.debug("handleRancidGroupProcessingCompleted: get Event uei/id: {} / {}", e.getDbid(), e.getUei());
        for (Parm parm : e.getParmCollection()) {
            LOG.debug("handleRancidGroupProcessingCompleted: parm name: {}", parm.getParmName());
            if (parm.getParmName().equals(".1.3.6.1.4.1.31543.1.1.2.1.1.3")) {
                updateGroupConfiguration(parm.getValue().getContent());
                break;
            }
        }
    }
    
    private void updateGroupConfiguration(String group) {
        Iterator<Integer> ite = m_onmsNodeRancidNodeMap.keySet().iterator();
        while (ite.hasNext()) {
            Integer nodeId = ite.next();
            RancidNode rnode = m_onmsNodeRancidNodeMap.get(nodeId);
            if (group.equals(rnode.getGroup())) {
                boolean stateUp = rnode.isStateUp();
                rnode.setStateUp(false);
                updateConfiguration(nodeId.intValue(), rnode, m_cp, true);
                rnode.setStateUp(stateUp);
            }
        }
    }
    
    private void updateRancidNodeState(int nodeid, boolean up) {
        RancidNode rnode = m_onmsNodeRancidNodeMap.get(Integer.valueOf(nodeid));
        rnode.setStateUp(up);
        m_onmsNodeRancidNodeMap.put(nodeid, rnode);
    }

    private boolean updateAuth(RancidNodeAuthentication localNode, RancidNodeAuthentication remoteNode) {
        if (!localNode.getUser().equals(remoteNode.getUser())) return true;
        if (!localNode.getPassword().equals(remoteNode.getPassword())) return true;
        if (!localNode.getEnablePass().equals(remoteNode.getEnablePass())) return true;
        if (!localNode.getConnectionMethodString().equalsIgnoreCase(remoteNode.getConnectionMethodString())) return true;
        if (localNode.isAutoEnable()) return !remoteNode.isAutoEnable();
        if (!localNode.isAutoEnable()) return remoteNode.isAutoEnable();
        return false;
    }
}
