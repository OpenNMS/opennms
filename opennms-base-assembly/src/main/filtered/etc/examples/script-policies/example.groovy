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

import org.opennms.core.utils.ByteArrayComparator
import org.opennms.core.utils.InetAddressUtils
import org.opennms.netmgt.model.OnmsAssetRecord
import org.opennms.netmgt.model.OnmsIpInterface
import org.opennms.netmgt.model.PrimaryType

import java.util.stream.Collectors

// transaction is a boolean to indicate whether the scripts run in transaction or not.
// Not in transaction indicates that the node is in initial stage of scanning phase and  may not be persisted yet.
// Only use this to add assets/metadata or return invalid node (returning null) to abort the scan.

if (!transaction) {
    LOG.debug("customscript: not in transaction");
    OnmsAssetRecord assetRecord = new OnmsAssetRecord();
    assetRecord.setAssetNumber("12345");
    node.setAssetRecord(assetRecord);
    return node;
}

// Modifying primary interface should be done in transaction mode.

// Example script for modifying the primary interface

List<String> interfaceNames = Arrays.asList("Lo0", "lo0", "lo0.0", "loopback0", "Lo222", "Lo201"
        , "mgt0", "mgmt0", "mgmt", "primary", "bond0", "eth0", "Vl103", "management");

List<OnmsIpInterface> interfacesWithSNMP = node.getInterfacesWithService("SNMP");

LOG.debug("customscript : Interfaces with SNMP service {}", interfacesWithSNMP);

List<String> ifNames = interfacesWithSNMP.stream()
        .filter({ ipInterface -> ipInterface.getSnmpInterface() != null })
        .map({ iface -> iface.getSnmpInterface().getIfName() })
        .collect(Collectors.toList());

LOG.debug("customscript : ifNames  {}", ifNames);

String matchedIfName = interfaceNames.stream()
        .filter({ interfaceName -> ifNames.contains(interfaceName) })
        .findFirst()
        .orElse(null);

OnmsIpInterface matchedIpInterface = null;

if (matchedIfName == null) {
    // Find lowest IP Address as there is no match in if names.
    matchedIpInterface = interfacesWithSNMP.stream()
            .min(Comparator.comparing({ onmsIpInterface -> onmsIpInterface.getIpAddress().getAddress() }, new ByteArrayComparator())).get();
    if (matchedIpInterface != null) {
        LOG.debug("customscript : lowest numbered ipAddress = {}", matchedIpInterface.getIpAddress());
    }

} else {
    LOG.debug("customscript : Found a match for ifName = {}", matchedIfName);

    matchedIpInterface = interfacesWithSNMP.stream()
            .filter({ ipInterface ->
                 if(ipInterface.getSnmpInterface() != null) {
                     return ipInterface.getSnmpInterface().getIfName().equals(matchedIfName);
                 }
                 return false;
             })
            .findFirst()
            .orElse(null);

}

if (matchedIpInterface != null) {

    LOG.debug("customscript : Setting {} as primary on node {}", matchedIpInterface.getIpAddress(), node.getId());

    matchedIpInterface.setIsSnmpPrimary(PrimaryType.PRIMARY);

    interfacesWithSNMP.stream().filter({ iface -> !iface.equals(matchedIpInterface) }).forEach({ iface ->
        iface.setIsSnmpPrimary(PrimaryType.SECONDARY);
        LOG.debug("customscript : Setting {} as secondary on node {}", iface.getIpAddress(), node.getId());
    });

    // If IpHostName on the interface is IP Address itself, set sysname as node label.
    if (InetAddressUtils.str(matchedIpInterface.getIpAddress()).equals(matchedIpInterface.getIpHostName()) && node.getSysName() != null) {
        LOG.debug("customscript: Setting sysname {} as node label", node.getSysName());
        node.setLabel(node.getSysName());
    } else {
        LOG.debug("customscript: Setting {} as node label", matchedIpInterface.getIpHostName());
        node.setLabel(matchedIpInterface.getIpHostName());
    }

}
return node;


