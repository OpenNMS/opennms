/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import org.opennms.netmgt.model.OnmsIpInterface
import org.opennms.netmgt.model.OnmsSnmpInterface
import org.opennms.netmgt.model.PrimaryType

import java.util.stream.Collectors

// Example script for modifying the primary interface

List<String> interfaceNames = Arrays.asList("Lo0","lo0","lo0.0","loopback0","Lo222","Lo201"
        ,"mgt0","mgmt0","mgmt","primary","bond0","eth0","Vl103","management");

List<OnmsIpInterface> interfacesWithSNMP = node.getInterfacesWithService("SNMP");

LOG.debug("Interfaces with SNMP service {}", interfacesWithSNMP);

List<OnmsSnmpInterface> snmpInterfaces = interfacesWithSNMP.stream()
        .map({iface -> iface.getSnmpInterface()}).collect(Collectors.toList());

List<String> ifNames = snmpInterfaces.stream()
        .filter({snmpInterface -> snmpInterface.getIfName() != null})
        .map({snmpInterface -> snmpInterface.getIfName()}).collect(Collectors.toList());

String matchedIfName = interfaceNames.stream()
        .filter({ interfaceName -> ifNames.contains(interfaceName) })
        .findFirst()
        .orElse(null);

if(matchedIfName != null) {

    LOG.debug("Found a match for ifName = {}", matchedIfName);

    OnmsSnmpInterface result = snmpInterfaces.stream()
            .filter({snmpInterface -> snmpInterface.getIfName().equals(matchedIfName)})
            .findFirst().orElse(null);

    if(result != null) {
        OnmsIpInterface ipInterface = interfacesWithSNMP.stream()
                .filter({iface -> iface.getSnmpInterface().equals(result)})
                .findFirst()
                .orElse(null);

        if(ipInterface != null) {

            LOG.debug("Setting {} as primary on node {}  " + ipInterface.getIpAddress(), node.getId());

            ipInterface.setIsSnmpPrimary(PrimaryType.PRIMARY);
            node.setLabel(ipInterface.getIpHostName());

            interfacesWithSNMP.stream().filter({iface -> !iface.equals(ipInterface)}).forEach({iface ->
                iface.setIsSnmpPrimary(PrimaryType.SECONDARY);
                LOG.debug("Setting {} as secondary on node {}  ", iface.getIpAddress(), node.getId());
            });
        }

    }
}

return node;


