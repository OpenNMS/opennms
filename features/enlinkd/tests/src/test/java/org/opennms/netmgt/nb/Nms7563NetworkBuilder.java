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

package org.opennms.netmgt.nb;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsNode;

/**
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */

public class Nms7563NetworkBuilder extends NmsNetworkBuilder {


    static {
    try {
    HOMESERVER_IP_IF_MAP.put(InetAddressUtils.addr("192.168.87.16"), 4);
    HOMESERVER_IP_IF_MAP.put(InetAddressUtils.addr("192.168.88.1"), 2);
    HOMESERVER_IP_IF_MAP.put(InetAddressUtils.addr("2001:4dd0:ff00:074f:0000:0000:0000:0002"), 6);
    
    HOMESERVER_IF_IFNAME_MAP.put(1, "lo");
    HOMESERVER_IF_IFDESCR_MAP.put(1, "lo");

    HOMESERVER_IF_IFNAME_MAP.put(2, "eth0");
    HOMESERVER_IF_IFDESCR_MAP.put(2, "eth0");
    HOMESERVER_IF_NETMASK_MAP.put(2, InetAddressUtils.addr("255.255.255.0"));
    HOMESERVER_IF_MAC_MAP.put(2, "001ff207994f");

    HOMESERVER_IF_IFNAME_MAP.put(3, "eth1");
    HOMESERVER_IF_IFDESCR_MAP.put(3, "eth1");
    HOMESERVER_IF_MAC_MAP.put(3, "001ff2079950");

    HOMESERVER_IF_IFNAME_MAP.put(4, "wlan0");
    HOMESERVER_IF_IFDESCR_MAP.put(4, "wlan0");
    HOMESERVER_IF_NETMASK_MAP.put(4, InetAddressUtils.addr("255.255.255.0"));
    HOMESERVER_IF_MAC_MAP.put(4, "801f02f72c9d");

    HOMESERVER_IF_IFNAME_MAP.put(5, "sit0");
    HOMESERVER_IF_IFDESCR_MAP.put(5, "sit0");
    
    HOMESERVER_IF_IFNAME_MAP.put(6, "sixxs");
    HOMESERVER_IF_IFDESCR_MAP.put(6, "sixxs");
    HOMESERVER_IF_NETMASK_MAP.put(6, InetAddressUtils.addr("ffff:ffff:ffff:ffff:0000:0000:0000:0000"));

    
    CISCO01_IP_IF_MAP.put(InetAddressUtils.addr("192.168.88.240"), 1);
    CISCO01_IF_IFNAME_MAP.put(1, "Vl1");
    CISCO01_IF_IFDESCR_MAP.put(1, "Vlan1");
    CISCO01_IF_NETMASK_MAP.put(1, InetAddressUtils.addr("255.255.255.0"));
    CISCO01_IF_MAC_MAP.put(1, "aca016bf0240");

    CISCO01_IF_IFNAME_MAP.put(10001, "Fa0/1");
    CISCO01_IF_IFDESCR_MAP.put(10001, "FastEthernet0/1");
    CISCO01_IF_MAC_MAP.put(10001, "aca016bf0201");

    CISCO01_IF_IFNAME_MAP.put(10002, "Fa0/2");
    CISCO01_IF_IFDESCR_MAP.put(10002, "FastEthernet0/2");
    CISCO01_IF_MAC_MAP.put(10002, "aca016bf0202");

    CISCO01_IF_IFNAME_MAP.put(10003, "Fa0/3");
    CISCO01_IF_IFDESCR_MAP.put(10003, "FastEthernet0/3");
    CISCO01_IF_MAC_MAP.put(10003, "aca016bf0203");

    CISCO01_IF_IFNAME_MAP.put(10004, "Fa0/4");
    CISCO01_IF_IFDESCR_MAP.put(10004, "FastEthernet0/4");
    CISCO01_IF_MAC_MAP.put(10004, "aca016bf0204");

    CISCO01_IF_IFNAME_MAP.put(10005, "Fa0/5");
    CISCO01_IF_IFDESCR_MAP.put(10005, "FastEthernet0/5");
    CISCO01_IF_MAC_MAP.put(10005, "aca016bf0205");

    CISCO01_IF_IFNAME_MAP.put(10006, "Fa0/6");
    CISCO01_IF_IFDESCR_MAP.put(10006, "FastEthernet0/6");
    CISCO01_IF_MAC_MAP.put(10006, "aca016bf0206");

    CISCO01_IF_IFNAME_MAP.put(10007,           "Fa0/7");
    CISCO01_IF_IFDESCR_MAP.put(10007, "FastEthernet0/7");
    CISCO01_IF_MAC_MAP.put(10007,    "aca016bf0207");

    CISCO01_IF_IFNAME_MAP.put(10008,           "Fa0/8");
    CISCO01_IF_IFDESCR_MAP.put(10008, "FastEthernet0/8");
    CISCO01_IF_MAC_MAP.put(10008,    "aca016bf0208");

    CISCO01_IF_IFNAME_MAP.put(10101,           "Gi0/1 ");
    CISCO01_IF_IFDESCR_MAP.put(10101, "GigabitEthernet0/1");
    CISCO01_IF_MAC_MAP.put(10101,    "aca016bf0209");

    CISCO01_IF_IFNAME_MAP.put(10501, "Nu0");
    CISCO01_IF_IFDESCR_MAP.put(10501, "Null0");

    SWITCH02_IF_IFNAME_MAP.put(1, "1");
    SWITCH02_IF_IFNAME_MAP.put(2, "2");
    SWITCH02_IF_IFNAME_MAP.put(3, "3");
    SWITCH02_IF_IFNAME_MAP.put(4, "4");
    SWITCH02_IF_IFNAME_MAP.put(5, "5");
    SWITCH02_IF_IFNAME_MAP.put(6, "6");
    SWITCH02_IF_IFNAME_MAP.put(7, "7");
    SWITCH02_IF_IFNAME_MAP.put(8, "8");
    SWITCH02_IF_IFNAME_MAP.put(9, "9");
    SWITCH02_IF_IFNAME_MAP.put(10, "10");
    SWITCH02_IF_IFNAME_MAP.put(11, "11");
    SWITCH02_IF_IFNAME_MAP.put(12, "12");
    SWITCH02_IF_IFNAME_MAP.put(13, "13");
    SWITCH02_IF_IFNAME_MAP.put(14, "14");
    SWITCH02_IF_IFNAME_MAP.put(15, "15");
    SWITCH02_IF_IFNAME_MAP.put(16, "16");
    SWITCH02_IF_IFNAME_MAP.put(17, "17");
    SWITCH02_IF_IFNAME_MAP.put(18, "18");
    SWITCH02_IF_IFNAME_MAP.put(19, "19");
    SWITCH02_IF_IFNAME_MAP.put(20, "20");
    SWITCH02_IF_IFNAME_MAP.put(21, "21");
    SWITCH02_IF_IFNAME_MAP.put(22, "22");
    SWITCH02_IF_IFNAME_MAP.put(23, "23");
    SWITCH02_IF_IFNAME_MAP.put(24, "24");
    SWITCH02_IF_IFNAME_MAP.put(25, "25");
    SWITCH02_IF_IFNAME_MAP.put(26, "26");

    SWITCH02_IF_IFDESCR_MAP.put(1, "1");
    SWITCH02_IF_IFDESCR_MAP.put(2, "2");
    SWITCH02_IF_IFDESCR_MAP.put(3, "3");
    SWITCH02_IF_IFDESCR_MAP.put(4, "4");
    SWITCH02_IF_IFDESCR_MAP.put(5, "5");
    SWITCH02_IF_IFDESCR_MAP.put(6, "6");
    SWITCH02_IF_IFDESCR_MAP.put(7, "7");
    SWITCH02_IF_IFDESCR_MAP.put(8, "8");
    SWITCH02_IF_IFDESCR_MAP.put(9, "9");
    SWITCH02_IF_IFDESCR_MAP.put(10, "10");
    SWITCH02_IF_IFDESCR_MAP.put(11, "11");
    SWITCH02_IF_IFDESCR_MAP.put(12, "12");
    SWITCH02_IF_IFDESCR_MAP.put(13, "13");
    SWITCH02_IF_IFDESCR_MAP.put(14, "14");
    SWITCH02_IF_IFDESCR_MAP.put(15, "15");
    SWITCH02_IF_IFDESCR_MAP.put(16, "16");
    SWITCH02_IF_IFDESCR_MAP.put(17, "17");
    SWITCH02_IF_IFDESCR_MAP.put(18, "18");
    SWITCH02_IF_IFDESCR_MAP.put(19, "19");
    SWITCH02_IF_IFDESCR_MAP.put(20, "20");
    SWITCH02_IF_IFDESCR_MAP.put(21, "21");
    SWITCH02_IF_IFDESCR_MAP.put(22, "22");
    SWITCH02_IF_IFDESCR_MAP.put(23, "23");
    SWITCH02_IF_IFDESCR_MAP.put(24, "24");
    SWITCH02_IF_IFDESCR_MAP.put(25, "25");
    SWITCH02_IF_IFDESCR_MAP.put(26, "26");

    SWITCH02_IF_MAC_MAP.put(1, "001db3c5097f");
    SWITCH02_IF_MAC_MAP.put(2, "001db3c5097e");
    SWITCH02_IF_MAC_MAP.put(3, "001db3c5097d");
    SWITCH02_IF_MAC_MAP.put(4, "001db3c5097c");
    SWITCH02_IF_MAC_MAP.put(5, "001db3c5097b");
    SWITCH02_IF_MAC_MAP.put(6, "001db3c5097a");
    SWITCH02_IF_MAC_MAP.put(7, "001db3c50979");
    SWITCH02_IF_MAC_MAP.put(8, "001db3c50978");
    SWITCH02_IF_MAC_MAP.put(9, "001db3c50977");
    SWITCH02_IF_MAC_MAP.put(10, "001db3c50976");
    SWITCH02_IF_MAC_MAP.put(11, "001db3c50975");
    SWITCH02_IF_MAC_MAP.put(12, "001db3c50974");
    SWITCH02_IF_MAC_MAP.put(13, "001db3c50973");
    SWITCH02_IF_MAC_MAP.put(14, "001db3c50972");
    SWITCH02_IF_MAC_MAP.put(15, "001db3c50971");
    SWITCH02_IF_MAC_MAP.put(16, "001db3c50970");
    SWITCH02_IF_MAC_MAP.put(17, "001db3c5096f");
    SWITCH02_IF_MAC_MAP.put(18, "001db3c5096e");
    SWITCH02_IF_MAC_MAP.put(19, "001db3c5096d");
    SWITCH02_IF_MAC_MAP.put(20, "001db3c5096c");
    SWITCH02_IF_MAC_MAP.put(21, "001db3c5096b");
    SWITCH02_IF_MAC_MAP.put(22, "001db3c5096a");
    SWITCH02_IF_MAC_MAP.put(23, "001db3c50969");
    SWITCH02_IF_MAC_MAP.put(24, "001db3c50968");
    SWITCH02_IF_MAC_MAP.put(25, "001db3c50967");
    SWITCH02_IF_MAC_MAP.put(26, "001db3c50966");

    SWITCH02_IP_IF_MAP.put(InetAddressUtils.addr("192.168.88.241"), 57);
    SWITCH02_IF_IFNAME_MAP.put(57, "DEFAULT_VLAN");
    SWITCH02_IF_IFDESCR_MAP.put(57, "DEFAULT_VLAN");
    SWITCH02_IF_NETMASK_MAP.put(57, InetAddressUtils.addr("255.255.255.0"));
    SWITCH02_IF_MAC_MAP.put(57, "001db3c50960");

    SWITCH02_IF_IFNAME_MAP.put(66, "VLAN10");
    SWITCH02_IF_IFDESCR_MAP.put(66, "VLAN10");
    SWITCH02_IF_MAC_MAP.put(66, "001db3c50960");

    SWITCH02_IF_IFNAME_MAP.put(4152, "lo0");
    SWITCH02_IF_IFDESCR_MAP.put(4152, "HP ProCurve Switch software loopback interface");


    } catch (Exception e) {
        
    }
    }
    
    public OnmsNode getHomeServer() {
        return getNode(HOMESERVER_NAME,HOMESERVER_SYSOID,HOMESERVER_IP,HOMESERVER_IP_IF_MAP,HOMESERVER_IF_IFNAME_MAP,HOMESERVER_IF_MAC_MAP,HOMESERVER_IF_IFDESCR_MAP,HOMESERVER_IF_IFALIAS_MAP);
    }    
    
    public OnmsNode getSwitch02() {
        return getNode(SWITCH02_NAME,SWITCH02_SYSOID,SWITCH02_IP,SWITCH02_IP_IF_MAP,SWITCH02_IF_IFNAME_MAP,SWITCH02_IF_MAC_MAP,SWITCH02_IF_IFDESCR_MAP,SWITCH02_IF_IFALIAS_MAP);
    }    

    public OnmsNode getCisco01() {
        return getNode(CISCO01_NAME,CISCO01_SYSOID,CISCO01_IP,CISCO01_IP_IF_MAP,CISCO01_IF_IFNAME_MAP,CISCO01_IF_MAC_MAP,CISCO01_IF_IFDESCR_MAP,CISCO01_IF_IFALIAS_MAP);
    }    

}
