/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
    private final InetAddress netMask;
    private final String isManaged;
    private final PrimaryType isSnmpPrimary;
    private final Integer nodeId;
    private final Integer snmpInterfaceId;

    public IpInterfaceTopologyEntity(Integer id,
            InetAddress ipAddress, InetAddress netMask, String isManaged, PrimaryType isSnmpPrimary, Integer nodeId,
            Integer snmpInterfaceId){
        this.id = id;
        this.ipAddress = ipAddress;
        this.netMask = netMask;
        this.isManaged = isManaged;
        this.isSnmpPrimary = isSnmpPrimary;
        this.nodeId = nodeId;
        this.snmpInterfaceId = snmpInterfaceId;
    }

    public IpInterfaceTopologyEntity(Integer id,
            InetAddress ipAddress, InetAddress netMask, String isManaged, String snmpPrimary, Integer nodeId,
            Integer snmpInterfaceId){
        this(id, ipAddress, netMask, isManaged, PrimaryType.get(snmpPrimary), nodeId, snmpInterfaceId);
    }

    public static IpInterfaceTopologyEntity create(OnmsIpInterface ipInterface) {
        return new IpInterfaceTopologyEntity(
                ipInterface.getId(),
                ipInterface.getIpAddress(),
                ipInterface.getNetMask(),
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

    public InetAddress getNetMask() {
        return netMask;
    }

    public String getIsManaged() {
        return isManaged;
    }

    public boolean isManaged() {
        return "M".equals(getIsManaged());
    }

    public char snmpPrimary() {
        return isSnmpPrimary.getCharCode();
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
                ", netMask=" + netMask +
                ", isManaged='" + isManaged + '\'' +
                ", isSnmpPrimary=" + isSnmpPrimary +
                ", nodeId=" + nodeId +
                ", snmpInterfaceId=" + snmpInterfaceId +
                '}';
    }
}
