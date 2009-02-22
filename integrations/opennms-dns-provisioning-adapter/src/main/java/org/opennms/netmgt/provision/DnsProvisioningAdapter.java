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

import java.net.InetAddress;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;


/**
 * A Dynamic DNS provisioning adapter for integration with OpenNMS Provisoning daemon API.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class DnsProvisioningAdapter implements ProvisioningAdapter, InitializingBean {
    
    /*
     * A read-only DAO will be set by the Provisioning Daemon.
     */
    private NodeDao m_nodeDao;
    private EventForwarder m_eventForwarder;
    private static final String MESSAGE_PREFIX = "Dynamic DNS provisioning failed: ";
    private static final String ADAPTER_NAME="DNS Provisioning Adapter";

    public void afterPropertiesSet() throws Exception {
        //to initialization here
    }

    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#addNode(org.opennms.netmgt.model.OnmsNode)
     */
    @Transactional
    public void addNode(int nodeId) throws ProvisioningAdapterException {
        OnmsNode node = null;
        try {
            node = m_nodeDao.get(nodeId);
            DnsRecord record = new DnsRecord(node);
            DynamicDnsAdapter.add(record);
        } catch (Exception e) {
            log().error("addNode: Error handling node added event.", e);
            sendAndThrow(nodeId, e);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#updateNode(org.opennms.netmgt.model.OnmsNode)
     */
    @Transactional
    public void updateNode(int nodeId) throws ProvisioningAdapterException {
        try {
            OnmsNode node = m_nodeDao.get(nodeId);
            DnsRecord record = new DnsRecord(node);
            DynamicDnsAdapter.update(record);
        } catch (Exception e) {
            log().error("updateNode: Error handling node added event.", e);
            sendAndThrow(nodeId, e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#deleteNode(org.opennms.netmgt.model.OnmsNode)
     */
    @Transactional
    public void deleteNode(int nodeId) throws ProvisioningAdapterException {
        try {
            OnmsNode node = m_nodeDao.get(nodeId);
            DnsRecord record = new DnsRecord(node);
            DynamicDnsAdapter.delete(record);
        } catch (Exception e) {
            log().error("deleteNode: Error handling node deleted event.", e);
            sendAndThrow(nodeId, e);
        }
    }

    public void nodeConfigChanged(int nodeid) throws ProvisioningAdapterException {
        throw new ProvisioningAdapterException("configChanged event not yet implemented.");
    }

    private void sendAndThrow(int nodeId, Exception e) {
        m_eventForwarder.sendNow(buildEvent(EventConstants.PROVISIONING_ADAPTER_FAILED, nodeId).addParam("reason", MESSAGE_PREFIX+e.getLocalizedMessage()).getEvent());
        throw new ProvisioningAdapterException(MESSAGE_PREFIX, e);
    }

    private EventBuilder buildEvent(String uei, int nodeId) {
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

    class DnsRecord {
        private InetAddress m_ip;
        private String m_hostname;
        
        DnsRecord(OnmsNode node) {
            OnmsIpInterface primaryInterface = node.getPrimaryInterface();
            
            if (primaryInterface == null) {
                Set<OnmsIpInterface> ipInterfaces = node.getIpInterfaces();
                for (OnmsIpInterface onmsIpInterface : ipInterfaces) {
                    m_ip = onmsIpInterface.getInetAddress();
                    break;
                }
            } else {
                m_ip = primaryInterface.getInetAddress();
            }
            m_hostname = node.getLabel();
        }

        public InetAddress getIp() {
            return m_ip;
        }

        public String getHostname() {
            return m_hostname;
        }
    }
    
    static class DynamicDnsAdapter {
        
        static boolean add(DnsRecord record) {
            log().error("DNS Adapter not Implemented.");
            throw new UnsupportedOperationException("method not yet implemented.");
        }
        
        static boolean update(DnsRecord record) {
            log().error("DNS Adapter not Implemented.");
            throw new UnsupportedOperationException("method not yet implemented.");
        }
        
        static boolean delete(DnsRecord record) {
            log().error("DNS Adapter not Implemented.");
            throw new UnsupportedOperationException("method not yet implemented.");
        }

        static public DnsRecord getRecord(DnsRecord record) {
            log().error("DNS Adapter not Implemented.");
            throw new UnsupportedOperationException("method not yet implemented.");
        }
    }
    
    private static Category log() {
        return ThreadCategory.getInstance(DnsProvisioningAdapter.class);
    }


    public String getName() {
        return ADAPTER_NAME;
    }

}
