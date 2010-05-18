/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: February 23, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;

class DnsRecord {
    private InetAddress m_ip;
    private String m_hostname;
    private String m_zone;
    
    DnsRecord(OnmsNode node) {
        
        OnmsIpInterface primaryInterface = node.getPrimaryInterface();
        
        
        if (primaryInterface == null) {
            log().debug("Constructor: no primary interface found for nodeid: " + node.getNodeId());
            Set<OnmsIpInterface> ipInterfaces = node.getIpInterfaces();
            for (OnmsIpInterface onmsIpInterface : ipInterfaces) {
                m_ip = onmsIpInterface.getInetAddress();
                break;
            }
        } else {
            log().debug("Constructor: primary interface found for nodeid: " + node.getNodeId());
            m_ip = primaryInterface.getInetAddress();
        }
        log().debug("Constructor: set ip address: " + m_ip);
        m_hostname = node.getLabel() + ".";
        log().debug("Constructor: set hostname: " + m_hostname);
        m_zone = m_hostname.substring(m_hostname.indexOf('.') + 1);
        log().debug("Constructor: set zone: " + m_zone);

    }

    public InetAddress getIp() {
        return m_ip;
    }

    public String getZone() {
        return m_zone;
    }

    public String getHostname() {
        return m_hostname;
    }
    
    private static ThreadCategory log() {
        return ThreadCategory.getInstance(DnsRecord.class);
    }

}