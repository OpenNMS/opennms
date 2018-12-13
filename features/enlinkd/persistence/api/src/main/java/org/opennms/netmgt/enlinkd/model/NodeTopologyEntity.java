/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.model;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;
import java.util.Objects;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;

public class NodeTopologyEntity {

    public static NodeTopologyEntity create(OnmsNode node) {
        if (node == null) {
            return null;
        }
        if (node.getPrimaryInterface() != null) {
            return new NodeTopologyEntity(node.getId(), 
                               node.getLabel(),
                               node.getPrimaryInterface().getIpAddress(),
                               node.getSysObjectId(), 
                               node.getSysName(),
                               node.getLocation() == null ? null : node.getLocation().getLocationName(),
                               node.getType(),
                               node.getPrimaryInterface().isManaged());
        }
        OnmsIpInterface elected = null;
        for (OnmsIpInterface ipinterface: node.getIpInterfaces()) {
            if (elected == null) {
                elected = ipinterface;
                continue;
            }
            if (!elected.isManaged() && ipinterface.isManaged()) {
                elected = ipinterface;
                continue;
            }
        }
        if (elected != null) {
            return new NodeTopologyEntity(node.getId(), 
                               node.getLabel(),
                               elected.getIpAddress(),
                               node.getSysObjectId(), node.getSysName(),node.getLocation() == null ? null : node.getLocation().getLocationName(),
                               node.getType(),
                               elected.isManaged());
        }
        return new NodeTopologyEntity(node.getId(), 
                           node.getLabel(),
                           null,
                           node.getSysObjectId(), 
                           node.getSysName(),
                           node.getLocation() == null ? null : node.getLocation().getLocationName(),
                           node.getType(),
                           false);

    }
    private final int m_nodeId;
    private final InetAddress m_primaryAddr;
    private final String m_sysoid;
    private final String m_sysname;
    private final String m_label;
    private final String m_location;
    private final OnmsNode.NodeType m_type;
    private final boolean m_isManaged;

    public String getLocation() {
        return m_location;
    }

    private NodeTopologyEntity(final int nodeId, final String label,
            final InetAddress snmpPrimaryAddr, final String sysoid, final String sysname, final String location, final OnmsNode.NodeType type, final boolean isManaged) {
        m_nodeId = nodeId;
        m_label=label;
        m_primaryAddr = snmpPrimaryAddr;
        m_sysoid = sysoid;
        m_sysname = sysname;
        m_location = location;
        m_type = type;
        m_isManaged = isManaged;
    }

    public String getId() {
        return Integer.toString(m_nodeId);
    }


    public int getNodeId() {
        return m_nodeId;
    }

    public String getAddress() {
        return InetAddressUtils.str(m_primaryAddr);
    }
    public InetAddress getPrimaryIpAddr() {
        return m_primaryAddr;
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
        NodeTopologyEntity node = (NodeTopologyEntity) o;
        return Objects.equals(m_nodeId, node.m_nodeId) &&
                Objects.equals(m_primaryAddr, node.m_primaryAddr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_nodeId, m_primaryAddr);
    }

    public String getLabel() {
        return m_label;
    }

    public OnmsNode.NodeType getType() {
        return m_type;
    }

    public boolean isManaged() {
        return m_isManaged;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("nodeId", m_nodeId)
                .append("label",m_label)
                .append("type",m_type)
                .append("primaryAddr", str(m_primaryAddr))
                .append("sysOid", m_sysoid)
                .append("location", m_location).toString();
    }

}
