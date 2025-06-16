/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
