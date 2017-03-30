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

package org.opennms.netmgt.provision;

import java.net.InetAddress;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;

class DnsRecord {
    private static final Logger LOG = LoggerFactory.getLogger(DnsRecord.class);
    private InetAddress m_ip;
    private String m_hostname;
    private String m_zone;
    
    DnsRecord(OnmsNode node, int level) {

        LOG.debug("Constructor: set level: {}", level);

        OnmsIpInterface primaryInterface = node.getPrimaryInterface();
        
        
        if (primaryInterface == null) {
            LOG.debug("Constructor: no primary interface found for nodeid: {}", node.getNodeId());
            Set<OnmsIpInterface> ipInterfaces = node.getIpInterfaces();
            for (OnmsIpInterface onmsIpInterface : ipInterfaces) {
                m_ip = onmsIpInterface.getIpAddress();
                break;
            }
        } else {
            LOG.debug("Constructor: primary interface found for nodeid: {}", node.getNodeId());
            m_ip = primaryInterface.getIpAddress();
        }
        LOG.debug("Constructor: set ip address: {}", m_ip);
        m_hostname = node.getLabel() + ".";
        LOG.debug("Constructor: set hostname: {}", m_hostname);

        String[] singlestat = m_hostname.split("\\.");
        if ( level == 0 || level >= singlestat.length){
            m_zone = m_hostname.substring(m_hostname.indexOf('.') + 1);
        } else {
        	String domain="";
        	for (int i=singlestat.length-level;i < singlestat.length;i++ ) {
        		domain+=singlestat[i];
        		domain+=".";
        	}
        	m_zone=domain;
        }
        LOG.debug("Constructor: set zone: {}", m_zone);

    }

    /**
     * <p>getIp</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getIp() {
        return m_ip;
    }

    /**
     * <p>getZone</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getZone() {
        return m_zone;
    }

    /**
     * <p>getHostname</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHostname() {
        return m_hostname;
    }
        
}
