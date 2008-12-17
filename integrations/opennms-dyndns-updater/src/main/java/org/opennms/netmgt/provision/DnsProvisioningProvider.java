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
import java.util.List;

import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;


/**
 * A Dynamic DNS provider for integration with OpenNMS Provisoning daemon API.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class DnsProvisioningProvider implements ProvisioningProvider {
    
    /*
     * A read-only DAO will be set by the Provisioning Daemon.
     */
    NodeDao m_nodeDao = null;

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningProvider#addNode(org.opennms.netmgt.model.OnmsNode)
     */
    public void addNode(int nodeId) throws ProvisioningProviderException {
        try {
            OnmsNode node = m_nodeDao.get(nodeId);
            DnsRecord record = new DnsRecord(node);
            DynamicDnsProvider.add(record);
        } catch (Exception e) {
            throw new ProvisioningProviderException("Dynamic DNS provisioning failed.", e);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningProvider#updateNode(org.opennms.netmgt.model.OnmsNode)
     */
    public void updateNode(int nodeId) throws ProvisioningProviderException {
        try {
            OnmsNode node = m_nodeDao.get(nodeId);
            DnsRecord record = new DnsRecord(node);
            DynamicDnsProvider.update(record);
        } catch (Exception e) {
            throw new ProvisioningProviderException("Dynamic DNS provisioning failed.", e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningProvider#deleteNode(org.opennms.netmgt.model.OnmsNode)
     */
    public void deleteNode(int nodeId) throws ProvisioningProviderException {
        try {
            OnmsNode node = m_nodeDao.get(nodeId);
            DnsRecord record = new DnsRecord(node);
            DynamicDnsProvider.delete(record);
        } catch (Exception e) {
            throw new ProvisioningProviderException("Dynamic DNS provisioning failed.", e);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningProvider#onEvent()
     */
    public void onEvent(Event e) {
        throw new UnsupportedOperationException("method not yet implemented.");
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningProvider#registeredEventList()
     */
    public List<String> getEventList() {
        throw new UnsupportedOperationException("method not yet implemented.");
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningProvider#registeredEventList()
     */
    public void setReadOnlyNodeDao(NodeDao dao) {
        throw new UnsupportedOperationException("method not yet implemented.");
    }
    
    class DnsRecord {
        private InetAddress m_ip;
        private String m_hostname;
        
        DnsRecord(OnmsNode node) {
            m_ip = node.getCriticalInterface().getInetAddress();
            m_hostname = node.getLabel();
        }

        public InetAddress getIp() {
            return m_ip;
        }

        public String getHostname() {
            return m_hostname;
        }
    }
    
    static class DynamicDnsProvider {
        
        static boolean add(DnsRecord record) {
            throw new UnsupportedOperationException("method not yet implemented.");
        }
        
        static boolean update(DnsRecord record) {
            throw new UnsupportedOperationException("method not yet implemented.");
        }
        
        static boolean delete(DnsRecord record) {
            throw new UnsupportedOperationException("method not yet implemented.");
        }

        static public DnsRecord getRecord(DnsRecord record) {
            throw new UnsupportedOperationException("method not yet implemented.");
        }
    }

}
