/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.nb;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.SnmpInterfaceBuilder;

/**
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * 
 */
public abstract class NmsNetworkBuilder {

    NetworkBuilder m_networkBuilder;

    NetworkBuilder getNetworkBuilder() {
        if ( m_networkBuilder == null )
            m_networkBuilder = new NetworkBuilder();
        return m_networkBuilder;
    }

    OnmsNode getNode(String name, String sysoid, String primaryip,
                     Map<InetAddress, Integer> ipinterfacemap,
                     Map<Integer,String> ifindextoifnamemap,
                     Map<Integer,String> ifindextomacmap,
                     Map<Integer,String> ifindextoifdescrmap,
                     Map<Integer,String> ifindextoifalias,
                     Map<InetAddress,InetAddress>iptonetmaskmap)
    {
        NetworkBuilder nb = getNetworkBuilder();
        nb.addNode(name).setForeignSource("linkd").setForeignId(name).setSysObjectId(sysoid).setSysName(name).setType(NodeType.ACTIVE);
        final Map<Integer, SnmpInterfaceBuilder> ifindexsnmpbuildermap = new HashMap<>();
        for (Integer ifIndex: ifindextoifnamemap.keySet()) {
            ifindexsnmpbuildermap.put(ifIndex,
                    nb.addSnmpInterface(ifIndex).
                        setIfType(6).
                        setIfName(ifindextoifnamemap.get(ifIndex)).
                        setIfAlias(getSuitableString(ifindextoifalias, ifIndex)).
                        setIfSpeed(100000000).
                        setPhysAddr(getSuitableString(ifindextomacmap, ifIndex)).
                        setIfDescr(getSuitableString(ifindextoifdescrmap,ifIndex))
            );
        }

        for (InetAddress ipaddr: ipinterfacemap.keySet()) {
            final String hostAddress = ipaddr.getHostAddress();
            String isSnmpPrimary=(ipaddr.getHostAddress().equals(primaryip) ? "P" :"N");
            Integer ifIndex = ipinterfacemap.get(ipaddr);
            final InetAddress mask = iptonetmaskmap.get(ipaddr);
            SnmpInterfaceBuilder snmpInterfaceBuilder = ifindexsnmpbuildermap.get(ifIndex);
            if (snmpInterfaceBuilder != null) {
                nb.addInterface(hostAddress, ifindexsnmpbuildermap.get(ifIndex).getSnmpInterface())
                        .setNetMask(mask)
                        .setIsSnmpPrimary(isSnmpPrimary)
                        .setIsManaged("M");
            } else {
                nb.addInterface(hostAddress)
                        .setNetMask(mask)
                        .setIsSnmpPrimary(isSnmpPrimary)
                        .setIsManaged("M");
            }
        }

        return nb.getCurrentNode();
    }

    private String getSuitableString(Map<Integer,String> ifindextomacmap, Integer ifIndex) {
        String value = "";
        if (ifindextomacmap.containsKey(ifIndex)) {
            value = ifindextomacmap.get(ifIndex);
        }
        return value;
    }
    
}
