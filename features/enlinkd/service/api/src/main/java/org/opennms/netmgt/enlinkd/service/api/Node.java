/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.service.api;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;
import java.util.Objects;

import org.apache.commons.lang.builder.ToStringBuilder;

public class Node {

    private final int m_nodeId;

    private final InetAddress m_snmpprimaryaddr;

    private final String m_sysoid;

    private final String m_sysname;

    private final String m_label;

    private final String m_location;

    public String getLocation() {
        return m_location;
    }

    public Node(final int nodeId, final String label,
            final InetAddress snmpPrimaryAddr, final String sysoid, final String sysname, final String location) {
        m_nodeId = nodeId;
        m_label=label;
        m_snmpprimaryaddr = snmpPrimaryAddr;
        m_sysoid = sysoid;
        m_sysname = sysname;
        m_location = location;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("nodeId", m_nodeId)
                .append("snmpPrimaryAddr", str(m_snmpprimaryaddr))
                .append("sysOid", m_sysoid).toString();
    }

    public int getNodeId() {
        return m_nodeId;
    }

    public String getNodeidAsString() {
        return Integer.toString(m_nodeId);
    }
    public InetAddress getSnmpPrimaryIpAddr() {
        return m_snmpprimaryaddr;
    }

    public String getSysoid() {
        return m_sysoid;
    }

    public String getSysname() {
        return m_sysname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(m_nodeId, node.m_nodeId) &&
                Objects.equals(m_snmpprimaryaddr, node.m_snmpprimaryaddr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_nodeId, m_snmpprimaryaddr);
    }

    public String getLabel() {
        return m_label;
    }

}