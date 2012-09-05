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

package org.opennms.netmgt.snmp;

import java.net.InetAddress;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.springframework.util.Assert;

public final class SnmpAgentAddress {
    private final InetAddress m_address;
    private final Integer m_port;
    private int m_hashCode = 0;

    public SnmpAgentAddress(final InetAddress agentAddress, final Integer agentPort) {
        Assert.notNull(agentPort);
        m_address = agentAddress;
        m_port = agentPort;
        
        m_hashCode = new HashCodeBuilder(7, 15)
            .append(m_address)
            .append(m_port)
            .toHashCode();
    }

    public InetAddress getAddress() {
        return m_address;
    }
    
    public Integer getPort() {
        return m_port;
    }
    
    public boolean equals(final Object obj) {
        if (!(obj instanceof SnmpAgentAddress)) return false;
        final SnmpAgentAddress that = (SnmpAgentAddress)obj;
        return new EqualsBuilder()
            .append(this.getAddress(), that.getAddress())
            .append(this.getPort(), that.getPort())
            .isEquals();
    }
    
    public int hashCode() {
        return m_hashCode;
    }
    
    public String toString() {
    	return InetAddressUtils.str(m_address) + ":" + m_port;
    }
}