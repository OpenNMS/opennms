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
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.rancid.RWSClientApi;
import org.opennms.rancid.RancidNode;
import org.opennms.rancid.RancidNodeAuthentication;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;

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

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#addNode(org.opennms.netmgt.model.OnmsNode)
     */
    @Transactional
    public void addNode(int nodeId) throws ProvisioningAdapterException {
        log().debug("RANCID PROVISIONING ADAPTER CALLED addNode");
        try {
            String url = m_rwsConfig.getBaseUrl().getServer_url();
            OnmsNode node = m_nodeDao.get(nodeId);                                                                                                                                                                                            

            RWSClientApi.createRWSRancidNode(url,getSuitableRancidNode(node));

            RWSClientApi.createOrUpdateRWSAuthNode(url, getSuitableRancidNodeAuthentication(node));
        } catch (Exception e) {
            sendAndThrow(nodeId, e);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#updateNode(org.opennms.netmgt.model.OnmsNode)
     */
    @Transactional
    public void updateNode(int nodeId) throws ProvisioningAdapterException {
        log().debug("RANCID PROVISIONING ADAPTER CALLED updateNode");
        try {
            String url = m_rwsConfig.getBaseUrl().getServer_url();
            OnmsNode node = m_nodeDao.get(nodeId);
            RancidNode r_node = RWSClientApi.getRWSRancidNode(url, m_rancidAdapterConfig.getGroup(), node.getLabel());
            if (r_node.getDeviceName() != null ) {
                RWSClientApi.updateRWSRancidNode(url, getSuitableRancidNode(node));
            } else {
                RWSClientApi.createRWSRancidNode(url,getSuitableRancidNode(node));                
            }
            RWSClientApi.createOrUpdateRWSAuthNode(url, getSuitableRancidNodeAuthentication(node));            
        } catch (Exception e) {
            sendAndThrow(nodeId, e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#deleteNode(org.opennms.netmgt.model.OnmsNode)
     */
    @Transactional
    public void deleteNode(int nodeId) throws ProvisioningAdapterException {
        log().debug("RANCID PROVISIONING ADAPTER CALLED deleteNode");
        try {
            String url = m_rwsConfig.getBaseUrl().getServer_url();
            OnmsNode node = m_nodeDao.get(nodeId);
            
            RWSClientApi.deleteRWSRancidNode(url, getSuitableRancidNode(node));
            RWSClientApi.deleteRWSAuthNode(url, getSuitableRancidNodeAuthentication(node));
        } catch (Exception e) {
            sendAndThrow(nodeId, e);
        }
    }

    @EventHandler(uei=EventConstants.ADD_INTERFACE_EVENT_UEI)
    public void handleInterfaceAddedEvent(Event e) {
        log().debug("RANCID PROVISIONING ADAPTER CALLED handleInterfaceAddedEvent");
        throw new UnsupportedOperationException("method not yet implemented.");
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

    public void afterPropertiesSet() throws Exception {
        // TODO Auto-generated method stub
        // Put here your initialization if needed
        RWSClientApi.init();
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
      RancidNode r_node = new RancidNode(m_rancidAdapterConfig.getGroup(), node.getLabel());
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
