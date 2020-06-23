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

import java.net.InetAddress;
import java.util.Optional;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.ReadOnlyEntity;

@ReadOnlyEntity
public class IpInterfaceTopologyEntity {
    private final Integer id;
    private final InetAddress ipAddress;
    private final String isManaged;
    private final PrimaryType isSnmpPrimary;
    private final Integer nodeId;
    private final Integer snmpInterfaceId;

    public IpInterfaceTopologyEntity(Integer id,
            InetAddress ipAddress, String isManaged, PrimaryType isSnmpPrimary, Integer nodeId,
            Integer snmpInterfaceId){
        this.id = id;
        this.ipAddress = ipAddress;
        this.isManaged = isManaged;
        this.isSnmpPrimary = isSnmpPrimary;
        this.nodeId = nodeId;
        this.snmpInterfaceId = snmpInterfaceId;
    }

    public static IpInterfaceTopologyEntity create(OnmsIpInterface ipInterface) {
        return new IpInterfaceTopologyEntity(
                ipInterface.getId(),
                ipInterface.getIpAddress(),
                ipInterface.getIsManaged(),
                ipInterface.getIsSnmpPrimary(),
                Optional.ofNullable(ipInterface.getNode()).map(OnmsNode::getId).orElse(null),
                Optional.ofNullable(ipInterface.getSnmpInterface()).map(OnmsSnmpInterface::getId).orElse(null));
    }

    public Integer getId() {
        return id;
    }

    public String getNodeIdAsString() {
        if (getNodeId() != null) {
            return getNodeId().toString();
        }
        return null;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public String getIsManaged() {
        return isManaged;
    }

    public boolean isManaged() {
        return "M".equals(getIsManaged());
    }

    public PrimaryType getIsSnmpPrimary() {
        return isSnmpPrimary;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public Integer getSnmpInterfaceId() {
        return snmpInterfaceId;
    }

    @Override
    public String toString() {
        return "IpInterfaceTopologyEntity{" +
                "id=" + id +
                ", ipAddress=" + ipAddress +
                ", isManaged='" + isManaged + '\'' +
                ", isSnmpPrimary=" + isSnmpPrimary +
                ", nodeId=" + nodeId +
                ", snmpInterfaceId=" + snmpInterfaceId +
                '}';
    }
}
