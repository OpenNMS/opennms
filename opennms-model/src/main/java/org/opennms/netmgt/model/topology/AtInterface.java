/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.topology;

import java.net.InetAddress;

public class AtInterface {
    Integer m_nodeid;
    Integer m_ifIndex;
    String m_macAddress;
    InetAddress m_ipAddress;

    public AtInterface(final Integer nodeid, final String macAddress, final InetAddress ipAddress) {
        m_nodeid = nodeid;
        m_macAddress = macAddress;
        m_ipAddress = ipAddress;
    }

    public Integer getIfIndex() {
        return m_ifIndex;
    }
    public void setIfIndex(final Integer ifIndex) {
        m_ifIndex = ifIndex;
    }

    public Integer getNodeid() {
        return m_nodeid;
    }
    public void setNodeid(final Integer nodeid) {
        m_nodeid = nodeid;
    }

    public String getMacAddress() {
        return m_macAddress;
    }
    public void setMacAddress(final String macAddress) {
        m_macAddress = macAddress;
    }

    public InetAddress getIpAddress() {
        return m_ipAddress;
    }
    public void setIpAddress(final InetAddress ipAddress) {
        m_ipAddress = ipAddress;
    }

    @Override
    public int hashCode() {
        final int prime = 1117;
        int result = 1;
        result = prime * result + ((m_ifIndex == null) ? 0 : m_ifIndex.hashCode());
        result = prime * result + ((m_ipAddress == null) ? 0 : m_ipAddress.hashCode());
        result = prime * result + ((m_macAddress == null) ? 0 : m_macAddress.hashCode());
        result = prime * result + ((m_nodeid == null) ? 0 : m_nodeid.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof AtInterface)) return false;
        final AtInterface other = (AtInterface) obj;
        if (m_ifIndex == null) {
            if (other.m_ifIndex != null) return false;
        } else if (!m_ifIndex.equals(other.m_ifIndex)) {
            return false;
        }
        if (m_ipAddress == null) {
            if (other.m_ipAddress != null) return false;
        } else if (!m_ipAddress.equals(other.m_ipAddress)) {
            return false;
        }
        if (m_macAddress == null) {
            if (other.m_macAddress != null) return false;
        } else if (!m_macAddress.equals(other.m_macAddress)) {
            return false;
        }
        if (m_nodeid == null) {
            if (other.m_nodeid != null) return false;
        } else if (!m_nodeid.equals(other.m_nodeid)) {
            return false;
        }
        return true;
    }
    
    
    
}
