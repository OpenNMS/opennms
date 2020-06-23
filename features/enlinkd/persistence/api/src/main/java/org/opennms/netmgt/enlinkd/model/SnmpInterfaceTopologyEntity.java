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

import java.util.Optional;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.ReadOnlyEntity;

@ReadOnlyEntity
public class SnmpInterfaceTopologyEntity {

    private final Integer id;
    private final Integer ifIndex;
    private final String ifName;
    private final String ifAlias;
    private final Long ifSpeed;
    private final Integer nodeId;

    public SnmpInterfaceTopologyEntity(
            Integer id,
            Integer ifIndex,
            String ifName,
            String ifAlias,
            Long ifSpeed,
            Integer nodeId){
        this.id=id;
        this.ifIndex=ifIndex;
        this.ifName=ifName;
        this.ifAlias=ifAlias;
        this.ifSpeed = ifSpeed;
        this.nodeId= nodeId;
    }

    public static SnmpInterfaceTopologyEntity create(OnmsSnmpInterface snmpInterface) {
        return new SnmpInterfaceTopologyEntity(
                snmpInterface.getId(),
                snmpInterface.getIfIndex(),
                snmpInterface.getIfName(),
                snmpInterface.getIfAlias(),
                snmpInterface.getIfSpeed(),
                Optional.ofNullable(snmpInterface.getNode()).map(OnmsNode::getId).orElse(null)
        );
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

    public Integer getIfIndex() {
        return ifIndex;
    }

    public String getIfName() {
        return ifName;
    }

    public String getIfAlias() {
        return ifAlias;
    }

    public Long getIfSpeed() {
        return ifSpeed;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    @Override
    public String toString() {
        return "SnmpInterfaceTopologyEntity{" +
                "id=" + id +
                ", ifIndex=" + ifIndex +
                ", ifName='" + ifName + '\'' +
                ", ifSpeed='" + ifSpeed + '\'' +
                ", nodeId=" + nodeId +
                '}';
    }
}
