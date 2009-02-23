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

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.RWSConfig;
import org.opennms.netmgt.config.RancidAdapterConfig;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
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
public class RancidProvisioningAdapter implements ProvisioningAdapter, InitializingBean {
    
    private NodeDao m_nodeDao;
    private EventForwarder m_eventForwarder;
    private RWSConfig m_rwsConfig;
    private RancidAdapterConfig m_rancidAdapterConfig;
    
    private static final String MESSAGE_PREFIX = "Rancid provisioning failed: ";
    private static final String ADAPTER_NAME="RANCID Provisioning Adapter";
    private static final String RANCID_COMMENT="node provisioned by opennms";

    private volatile static ConcurrentMap<Integer, RancidNodeContainer> m_onmsNodeRancidNodeMap;

    public void afterPropertiesSet() throws Exception {
        RWSClientApi.init();
        
        Assert.notNull(m_nodeDao, "Rancid Provisioning Adapter requires nodeDao property to be set.");
        
        List<OnmsNode> nodes = m_nodeDao.findAllProvisionedNodes();
        m_onmsNodeRancidNodeMap = new ConcurrentHashMap<Integer, RancidNodeContainer>(nodes.size());
        
        for (OnmsNode onmsNode : nodes) {
            RancidNode rNode = getSuitableRancidNode(onmsNode);
            RancidNodeAuthentication rAuth = getSuitableRancidNodeAuthentication(onmsNode);
            
            m_onmsNodeRancidNodeMap.putIfAbsent(onmsNode.getId(), new RancidNodeContainer(rNode, rAuth));
        }
        
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
    public void addNode(int nodeId) throws ProvisioningAdapterException {
        log().debug("RANCID PROVISIONING ADAPTER CALLED addNode");
        try {
            String url = m_rwsConfig.getBaseUrl().getServer_url();
            OnmsNode node = m_nodeDao.get(nodeId);                                                                                                                                                                                            
            Assert.notNull(node, "Rancid Provisioning Adapter addNode method failed to return node for given nodeId:"+nodeId);
            
            RancidNode rNode = getSuitableRancidNode(node);
            RWSClientApi.createRWSRancidNode(url, rNode);

            RancidNodeAuthentication rAuth = getSuitableRancidNodeAuthentication(node);
            RWSClientApi.createOrUpdateRWSAuthNode(url, rAuth);
            
            m_onmsNodeRancidNodeMap.put(Integer.valueOf(nodeId), new RancidNodeContainer(rNode, rAuth));
            
        } catch (Exception e) {
            sendAndThrow(nodeId, e);
        }
    }

    @Transactional
    public void updateNode(int nodeId) throws ProvisioningAdapterException {
        log().debug("RANCID PROVISIONING ADAPTER CALLED updateNode");
        try {
            String url = m_rwsConfig.getBaseUrl().getServer_url();
            OnmsNode node = m_nodeDao.get(nodeId);
            
            //FIXME: Gugliemo, I made this change... we're keeping a reference to the RancidNode object,
            //no need to look it up now via the API unless you think we still need to do this.
            //RancidNode r_node = RWSClientApi.getRWSRancidNode(url, m_rancidAdapterConfig.getGroup(), node.getLabel());
            RancidNode rNode = m_onmsNodeRancidNodeMap.get(Integer.valueOf(nodeId)).getNode();

            //FIXME: something looks weird with this if statement
            if (rNode.getDeviceName() != null ) {
                rNode = getSuitableRancidNode(node);
                RWSClientApi.updateRWSRancidNode(url, rNode);
            } else {
                rNode = getSuitableRancidNode(node);
                RWSClientApi.createRWSRancidNode(url, rNode);                
            }
            
            RancidNodeAuthentication rAuth = getSuitableRancidNodeAuthentication(node);
            RWSClientApi.createOrUpdateRWSAuthNode(url, rAuth);
            
            m_onmsNodeRancidNodeMap.replace(node.getId(), new RancidNodeContainer(rNode, rAuth));
        } catch (Exception e) {
            sendAndThrow(nodeId, e);
        }
    }
    
    @Transactional
    public void deleteNode(int nodeId) throws ProvisioningAdapterException {

        log().debug("RANCID PROVISIONING ADAPTER CALLED deleteNode");
        
        /*
         * The work to maintain the hashmap boils down to needing to do deletes, so
         * here we go.
         */
        try {
            String url = m_rwsConfig.getBaseUrl().getServer_url();

            RancidNode rNode = m_onmsNodeRancidNodeMap.get(Integer.valueOf(nodeId)).getNode();
            RWSClientApi.deleteRWSRancidNode(url, rNode);
            
            RancidNodeAuthentication rAuth = m_onmsNodeRancidNodeMap.get(Integer.valueOf(nodeId)).getAuth();
            RWSClientApi.deleteRWSAuthNode(url, rAuth);
            
            m_onmsNodeRancidNodeMap.remove(Integer.valueOf(nodeId));
        } catch (Exception e) {
            sendAndThrow(nodeId, e);
        }
    }

    public void nodeConfigChanged(int nodeid) throws ProvisioningAdapterException {
        throw new ProvisioningAdapterException("configChanged event not yet implemented.");
    }
    
    private void sendAndThrow(int nodeId, Exception e) {
        log().debug("RANCID PROVISIONING ADAPTER CALLED sendAndThrow");
        m_eventForwarder.sendNow(buildEvent(EventConstants.PROVISIONING_ADAPTER_FAILED, nodeId).addParam("reason", MESSAGE_PREFIX+e.getLocalizedMessage()).getEvent());
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

    private RancidNode getSuitableRancidNode(OnmsNode node) {
        

        //FIXME: Guglielmo, the group should be the foreign source of the node
        String group = node.getForeignSource();
//        RancidNode r_node = new RancidNode(m_rancidAdapterConfig.getGroup(), node.getLabel());
        RancidNode r_node = new RancidNode(group, node.getLabel());

        //FIXME: Guglielmo, the device type is going to have to be mapped by SysObjectId...
        //that should probably be in the RancidNode class
        
        r_node.setDeviceType(RancidNode.DEVICE_TYPE_CISCO_IOS);
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
            r_auth_node.setConnectionMethod(asset_node.getUsername());
        } else {
            r_auth_node.setConnectionMethod(m_rancidAdapterConfig.getDefaultConnectionType());
        }
        
        return r_auth_node;
    }
}
