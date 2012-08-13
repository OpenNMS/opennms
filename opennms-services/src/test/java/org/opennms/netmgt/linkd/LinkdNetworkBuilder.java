/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.linkd;


import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.SnmpInterfaceBuilder;

/**
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:antonio@opennme.it">Antonio Russo</a>
 * @author <a href="mailto:alejandro@opennms.org">Alejandro Galue</a>
 */

public abstract class LinkdNetworkBuilder {

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
            Map<Integer,String> ifindextoifdescrmap) {
        NetworkBuilder nb = getNetworkBuilder();
        nb.addNode(name).setForeignSource("linkd").setForeignId(name).setSysObjectId(sysoid).setType("A");
        final Map<Integer, SnmpInterfaceBuilder> ifindexsnmpbuildermap = new HashMap<Integer, SnmpInterfaceBuilder>();
        for (Integer ifIndex: ifindextoifnamemap.keySet()) {
            ifindexsnmpbuildermap.put(ifIndex, nb.addSnmpInterface(ifIndex).
                                      setIfType(6).
                                      setIfName(ifindextoifnamemap.get(ifIndex)).
                                      setIfSpeed(100000000).
                                      setPhysAddr(getSuitableString(ifindextomacmap, ifIndex)).setIfDescr(getSuitableString(ifindextoifdescrmap,ifIndex)));
        }
        
        for (InetAddress ipaddr: ipinterfacemap.keySet()) { 
            String isSnmpPrimary="N";
            Integer ifIndex = ipinterfacemap.get(ipaddr);
            if (ipaddr.getHostAddress().equals(primaryip))
                isSnmpPrimary="P";
            if (ifIndex == null)
                nb.addInterface(ipaddr.getHostAddress()).setIsSnmpPrimary(isSnmpPrimary).setIsManaged("M");
            else {
                nb.addInterface(ipaddr.getHostAddress(), ifindexsnmpbuildermap.get(ifIndex).getSnmpInterface()).
                setIsSnmpPrimary(isSnmpPrimary).setIsManaged("M");            }
        }
            
        return nb.getCurrentNode();
    }
    
    private String getSuitableString(Map<Integer,String> ifindextomacmap, Integer ifIndex) {
        String value = "";
        if (ifindextomacmap.containsKey(ifIndex))
            value = ifindextomacmap.get(ifIndex);
        return value;
    }
    
    
    OnmsNode getNodeWithoutSnmp(String name, String ipaddr) {
        NetworkBuilder nb = getNetworkBuilder();
        nb.addNode(name).setForeignSource("linkd").setForeignId(name).setType("A");
        nb.addInterface(ipaddr).setIsSnmpPrimary("N").setIsManaged("M");
        return nb.getCurrentNode();
    }
    
}
