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

public class Nms8000NetworkBuilder extends NmsNetworkBuilder {


    static {
    try {
        NMMR1_IP_IF_MAP.put(InetAddressUtils.addr("192.168.23.9"), 9);
        NMMR1_IP_IF_MAP.put(InetAddressUtils.addr("192.168.42.8"), 10);
        NMMR1_IP_IF_MAP.put(InetAddressUtils.addr("192.168.3.1"), 8);
        NMMR1_IF_IFNAME_MAP.put(7, "Em0/0");
        NMMR1_IF_IFDESCR_MAP.put(7, "Embedded-Service-Engine0/0");
        NMMR1_IF_MAC_MAP.put(7, "000000000000");
        NMMR1_IF_IFNAME_MAP.put(17, "Nu0");
        NMMR1_IF_IFDESCR_MAP.put(17, "Null0");
        NMMR1_IF_IFNAME_MAP.put(9, "Gi0/1");
        NMMR1_IF_IFDESCR_MAP.put(9, "GigabitEthernet0/1");
        NMMR1_IF_MAC_MAP.put(9, "2c542d337c11");
        NMMR1_IF_IFALIAS_MAP.put(9, "Link-To-SW1");
        NMMR1_IF_NETMASK_MAP.put(9, InetAddressUtils.addr("255.255.255.0"));
        NMMR1_IF_IFNAME_MAP.put(26, "BR0/2/1");
        NMMR1_IF_IFDESCR_MAP.put(26, "BRI0/2/1-Physical");
        NMMR1_IF_IFNAME_MAP.put(15, "Se0/1/1");
        NMMR1_IF_IFDESCR_MAP.put(15, "Serial0/1/1");
        NMMR1_IF_IFNAME_MAP.put(16, "Vo0");
        NMMR1_IF_IFDESCR_MAP.put(16, "VoIP-Null0");
        NMMR1_IF_IFNAME_MAP.put(29, "BR0/2/1:2");
        NMMR1_IF_IFDESCR_MAP.put(29, "BRI0/2/1:2-Bearer Channel");
        NMMR1_IF_IFNAME_MAP.put(27, "BR0/2/1");
        NMMR1_IF_IFDESCR_MAP.put(27, "BRI0/2/1-Signaling");
        NMMR1_IF_IFNAME_MAP.put(13, "Se0/0/1");
        NMMR1_IF_IFDESCR_MAP.put(13, "Serial0/0/1");
        NMMR1_IF_IFNAME_MAP.put(23, "BR0/2/0");
        NMMR1_IF_IFDESCR_MAP.put(23, "BRI0/2/0-Signaling");
        NMMR1_IF_IFNAME_MAP.put(8, "Gi0/0");
        NMMR1_IF_IFDESCR_MAP.put(8, "GigabitEthernet0/0");
        NMMR1_IF_MAC_MAP.put(8, "2c542d337c10");
        NMMR1_IF_IFALIAS_MAP.put(8, "Link-To-R3");
        NMMR1_IF_NETMASK_MAP.put(8, InetAddressUtils.addr("255.255.255.0"));
        NMMR1_IF_IFNAME_MAP.put(10, "Gi0/2");
        NMMR1_IF_IFDESCR_MAP.put(10, "GigabitEthernet0/2");
        NMMR1_IF_MAC_MAP.put(10, "2c542d337c12");
        NMMR1_IF_IFALIAS_MAP.put(10, "Link-To-SW2");
        NMMR1_IF_NETMASK_MAP.put(10, InetAddressUtils.addr("255.255.255.0"));
        NMMR1_IF_IFNAME_MAP.put(22, "BR0/2/0");
        NMMR1_IF_IFDESCR_MAP.put(22, "BRI0/2/0-Physical");
        NMMR1_IF_IFNAME_MAP.put(14, "Se0/1/0");
        NMMR1_IF_IFDESCR_MAP.put(14, "Serial0/1/0");
        NMMR1_IF_IFNAME_MAP.put(11, "Gi0/3");
        NMMR1_IF_IFDESCR_MAP.put(11, "GigabitEthernet0/3");
        NMMR1_IF_MAC_MAP.put(11, "2c542d337c13");
        NMMR1_IF_IFNAME_MAP.put(1, "BR0/2/0");
        NMMR1_IF_IFDESCR_MAP.put(1, "BRI0/2/0");
        NMMR1_IF_IFNAME_MAP.put(4, "BR0/2/1");
        NMMR1_IF_IFDESCR_MAP.put(4, "BRI0/2/1");
        NMMR1_IF_IFNAME_MAP.put(24, "BR0/2/0:1");
        NMMR1_IF_IFDESCR_MAP.put(24, "BRI0/2/0:1-Bearer Channel");
        NMMR1_IF_IFNAME_MAP.put(28, "BR0/2/1:1");
        NMMR1_IF_IFDESCR_MAP.put(28, "BRI0/2/1:1-Bearer Channel");
        NMMR1_IF_IFNAME_MAP.put(25, "BR0/2/0:2");
        NMMR1_IF_IFDESCR_MAP.put(25, "BRI0/2/0:2-Bearer Channel");
        NMMR1_IF_IFNAME_MAP.put(12, "Se0/0/0");
        NMMR1_IF_IFDESCR_MAP.put(12, "Serial0/0/0");
        
        NMMR2_IP_IF_MAP.put(InetAddressUtils.addr("192.168.2.1"), 8);
        NMMR2_IP_IF_MAP.put(InetAddressUtils.addr("192.168.23.8"), 10);
        NMMR2_IP_IF_MAP.put(InetAddressUtils.addr("192.168.42.9"), 9);
        NMMR2_IF_IFNAME_MAP.put(11, "Gi0/3");
        NMMR2_IF_IFDESCR_MAP.put(11, "GigabitEthernet0/3");
        NMMR2_IF_MAC_MAP.put(11, "5057a8f5ba63");
        NMMR2_IF_IFNAME_MAP.put(15, "Se0/1/1");
        NMMR2_IF_IFDESCR_MAP.put(15, "Serial0/1/1");
        NMMR2_IF_IFNAME_MAP.put(1, "BR0/2/0");
        NMMR2_IF_IFDESCR_MAP.put(1, "BRI0/2/0");
        NMMR2_IF_IFNAME_MAP.put(12, "Se0/0/0");
        NMMR2_IF_IFDESCR_MAP.put(12, "Serial0/0/0");
        NMMR2_IF_IFNAME_MAP.put(22, "BR0/2/0");
        NMMR2_IF_IFDESCR_MAP.put(22, "BRI0/2/0-Physical");
        NMMR2_IF_IFNAME_MAP.put(27, "BR0/2/1");
        NMMR2_IF_IFDESCR_MAP.put(27, "BRI0/2/1-Signaling");
        NMMR2_IF_IFNAME_MAP.put(9, "Gi0/1");
        NMMR2_IF_IFDESCR_MAP.put(9, "GigabitEthernet0/1");
        NMMR2_IF_MAC_MAP.put(9, "5057a8f5ba61");
        NMMR2_IF_IFALIAS_MAP.put(9, "Link-To-SW2");
        NMMR2_IF_NETMASK_MAP.put(9, InetAddressUtils.addr("255.255.255.0"));
        NMMR2_IF_IFNAME_MAP.put(8, "Gi0/0");
        NMMR2_IF_IFDESCR_MAP.put(8, "GigabitEthernet0/0");
        NMMR2_IF_MAC_MAP.put(8, "5057a8f5ba60");
        NMMR2_IF_IFALIAS_MAP.put(8, "Link-To-R3");
        NMMR2_IF_NETMASK_MAP.put(8, InetAddressUtils.addr("255.255.255.0"));
        NMMR2_IF_IFNAME_MAP.put(25, "BR0/2/0:2");
        NMMR2_IF_IFDESCR_MAP.put(25, "BRI0/2/0:2-Bearer Channel");
        NMMR2_IF_IFNAME_MAP.put(14, "Se0/1/0");
        NMMR2_IF_IFDESCR_MAP.put(14, "Serial0/1/0");
        NMMR2_IF_IFNAME_MAP.put(4, "BR0/2/1");
        NMMR2_IF_IFDESCR_MAP.put(4, "BRI0/2/1");
        NMMR2_IF_IFNAME_MAP.put(10, "Gi0/2");
        NMMR2_IF_IFDESCR_MAP.put(10, "GigabitEthernet0/2");
        NMMR2_IF_MAC_MAP.put(10, "5057a8f5ba62");
        NMMR2_IF_IFALIAS_MAP.put(10, "Link-To-SW1");
        NMMR2_IF_NETMASK_MAP.put(10, InetAddressUtils.addr("255.255.255.0"));
        NMMR2_IF_IFNAME_MAP.put(28, "BR0/2/1:1");
        NMMR2_IF_IFDESCR_MAP.put(28, "BRI0/2/1:1-Bearer Channel");
        NMMR2_IF_IFNAME_MAP.put(29, "BR0/2/1:2");
        NMMR2_IF_IFDESCR_MAP.put(29, "BRI0/2/1:2-Bearer Channel");
        NMMR2_IF_IFNAME_MAP.put(17, "Nu0");
        NMMR2_IF_IFDESCR_MAP.put(17, "Null0");
        NMMR2_IF_IFNAME_MAP.put(16, "Vo0");
        NMMR2_IF_IFDESCR_MAP.put(16, "VoIP-Null0");
        NMMR2_IF_IFNAME_MAP.put(23, "BR0/2/0");
        NMMR2_IF_IFDESCR_MAP.put(23, "BRI0/2/0-Signaling");
        NMMR2_IF_IFNAME_MAP.put(13, "Se0/0/1");
        NMMR2_IF_IFDESCR_MAP.put(13, "Serial0/0/1");
        NMMR2_IF_IFNAME_MAP.put(26, "BR0/2/1");
        NMMR2_IF_IFDESCR_MAP.put(26, "BRI0/2/1-Physical");
        NMMR2_IF_IFNAME_MAP.put(7, "Em0/0");
        NMMR2_IF_IFDESCR_MAP.put(7, "Embedded-Service-Engine0/0");
        NMMR2_IF_MAC_MAP.put(7, "000000000000");
        NMMR2_IF_IFNAME_MAP.put(24, "BR0/2/0:1");
        NMMR2_IF_IFDESCR_MAP.put(24, "BRI0/2/0:1-Bearer Channel");
        
        NMMR3_IP_IF_MAP.put(InetAddressUtils.addr("192.168.76.101"), 2);
        NMMR3_IP_IF_MAP.put(InetAddressUtils.addr("192.168.255.1"), 16);
        NMMR3_IP_IF_MAP.put(InetAddressUtils.addr("192.168.3.2"), 3);
        NMMR3_IP_IF_MAP.put(InetAddressUtils.addr("192.168.2.2"), 4);
        NMMR3_IF_IFNAME_MAP.put(5, "Gi0/3");
        NMMR3_IF_IFDESCR_MAP.put(5, "GigabitEthernet0/3");
        NMMR3_IF_MAC_MAP.put(5, "2c542d337853");
        NMMR3_IF_IFNAME_MAP.put(17, "NV0");
        NMMR3_IF_IFDESCR_MAP.put(17, "NVI0");
        NMMR3_IF_IFNAME_MAP.put(9, "Se0/1/1");
        NMMR3_IF_IFDESCR_MAP.put(9, "Serial0/1/1");
        NMMR3_IF_IFNAME_MAP.put(3, "Gi0/1");
        NMMR3_IF_IFDESCR_MAP.put(3, "GigabitEthernet0/1");
        NMMR3_IF_MAC_MAP.put(3, "2c542d337851");
        NMMR3_IF_IFALIAS_MAP.put(3, "Link-To-R1");
        NMMR3_IF_NETMASK_MAP.put(3, InetAddressUtils.addr("255.255.255.0"));
        NMMR3_IF_IFNAME_MAP.put(7, "Se0/0/1");
        NMMR3_IF_IFDESCR_MAP.put(7, "Serial0/0/1");
        NMMR3_IF_IFNAME_MAP.put(6, "Se0/0/0");
        NMMR3_IF_IFDESCR_MAP.put(6, "Serial0/0/0");
        NMMR3_IF_IFNAME_MAP.put(2, "Gi0/0");
        NMMR3_IF_IFDESCR_MAP.put(2, "GigabitEthernet0/0");
        NMMR3_IF_MAC_MAP.put(2, "2c542d337850");
        NMMR3_IF_IFALIAS_MAP.put(2, "Link-To-NetLab");
        NMMR3_IF_NETMASK_MAP.put(2, InetAddressUtils.addr("255.255.255.128"));
        NMMR3_IF_IFNAME_MAP.put(10, "Vo0");
        NMMR3_IF_IFDESCR_MAP.put(10, "VoIP-Null0");
        NMMR3_IF_IFNAME_MAP.put(4, "Gi0/2");
        NMMR3_IF_IFDESCR_MAP.put(4, "GigabitEthernet0/2");
        NMMR3_IF_MAC_MAP.put(4, "2c542d337852");
        NMMR3_IF_IFALIAS_MAP.put(4, "Link-To-R2");
        NMMR3_IF_NETMASK_MAP.put(4, InetAddressUtils.addr("255.255.255.0"));
        NMMR3_IF_IFNAME_MAP.put(11, "Nu0");
        NMMR3_IF_IFDESCR_MAP.put(11, "Null0");
        NMMR3_IF_IFNAME_MAP.put(1, "Em0/0");
        NMMR3_IF_IFDESCR_MAP.put(1, "Embedded-Service-Engine0/0");
        NMMR3_IF_MAC_MAP.put(1, "000000000000");
        NMMR3_IF_IFNAME_MAP.put(8, "Se0/1/0");
        NMMR3_IF_IFDESCR_MAP.put(8, "Serial0/1/0");
        NMMR3_IF_IFNAME_MAP.put(16, "Tu1");
        NMMR3_IF_IFDESCR_MAP.put(16, "Tunnel1");
        NMMR3_IF_NETMASK_MAP.put(16, InetAddressUtils.addr("255.255.255.0"));
        
        
        NMMSW1_IP_IF_MAP.put(InetAddressUtils.addr("192.168.23.10"), 1);
        NMMSW1_IF_IFNAME_MAP.put(10018, "Fa0/18");
        NMMSW1_IF_IFDESCR_MAP.put(10018, "FastEthernet0/18");
        NMMSW1_IF_MAC_MAP.put(10018, "a418750ace12");
        NMMSW1_IF_IFNAME_MAP.put(10101, "Gi0/1");
        NMMSW1_IF_IFDESCR_MAP.put(10101, "GigabitEthernet0/1");
        NMMSW1_IF_MAC_MAP.put(10101, "a418750ace19");
        NMMSW1_IF_IFNAME_MAP.put(10024, "Fa0/24");
        NMMSW1_IF_IFDESCR_MAP.put(10024, "FastEthernet0/24");
        NMMSW1_IF_MAC_MAP.put(10024, "a418750ace18");
        NMMSW1_IF_IFNAME_MAP.put(10006, "Fa0/6");
        NMMSW1_IF_IFDESCR_MAP.put(10006, "FastEthernet0/6");
        NMMSW1_IF_MAC_MAP.put(10006, "a418750ace06");
        NMMSW1_IF_IFNAME_MAP.put(1, "Vl1");
        NMMSW1_IF_IFDESCR_MAP.put(1, "Vlan1");
        NMMSW1_IF_MAC_MAP.put(1, "a418750ace40");
        NMMSW1_IF_NETMASK_MAP.put(1, InetAddressUtils.addr("255.255.255.0"));
        NMMSW1_IF_IFNAME_MAP.put(10102, "Gi0/2");
        NMMSW1_IF_IFDESCR_MAP.put(10102, "GigabitEthernet0/2");
        NMMSW1_IF_MAC_MAP.put(10102, "a418750ace1a");
        NMMSW1_IF_IFNAME_MAP.put(10501, "Nu0");
        NMMSW1_IF_IFDESCR_MAP.put(10501, "Null0");
        NMMSW1_IF_IFNAME_MAP.put(10011, "Fa0/11");
        NMMSW1_IF_IFDESCR_MAP.put(10011, "FastEthernet0/11");
        NMMSW1_IF_MAC_MAP.put(10011, "a418750ace0b");
        NMMSW1_IF_IFNAME_MAP.put(10001, "Fa0/1");
        NMMSW1_IF_IFDESCR_MAP.put(10001, "FastEthernet0/1");
        NMMSW1_IF_MAC_MAP.put(10001, "a418750ace01");
        NMMSW1_IF_IFNAME_MAP.put(10003, "Fa0/3");
        NMMSW1_IF_IFDESCR_MAP.put(10003, "FastEthernet0/3");
        NMMSW1_IF_MAC_MAP.put(10003, "a418750ace03");
        NMMSW1_IF_IFNAME_MAP.put(10021, "Fa0/21");
        NMMSW1_IF_IFDESCR_MAP.put(10021, "FastEthernet0/21");
        NMMSW1_IF_MAC_MAP.put(10021, "a418750ace15");
        NMMSW1_IF_IFNAME_MAP.put(10020, "Fa0/20");
        NMMSW1_IF_IFDESCR_MAP.put(10020, "FastEthernet0/20");
        NMMSW1_IF_MAC_MAP.put(10020, "a418750ace14");
        NMMSW1_IF_IFNAME_MAP.put(10005, "Fa0/5");
        NMMSW1_IF_IFDESCR_MAP.put(10005, "FastEthernet0/5");
        NMMSW1_IF_MAC_MAP.put(10005, "a418750ace05");
        NMMSW1_IF_IFNAME_MAP.put(10019, "Fa0/19");
        NMMSW1_IF_IFDESCR_MAP.put(10019, "FastEthernet0/19");
        NMMSW1_IF_MAC_MAP.put(10019, "a418750ace13");
        NMMSW1_IF_IFNAME_MAP.put(10010, "Fa0/10");
        NMMSW1_IF_IFDESCR_MAP.put(10010, "FastEthernet0/10");
        NMMSW1_IF_MAC_MAP.put(10010, "a418750ace0a");
        NMMSW1_IF_IFNAME_MAP.put(10017, "Fa0/17");
        NMMSW1_IF_IFDESCR_MAP.put(10017, "FastEthernet0/17");
        NMMSW1_IF_MAC_MAP.put(10017, "a418750ace11");
        NMMSW1_IF_IFNAME_MAP.put(10023, "Fa0/23");
        NMMSW1_IF_IFDESCR_MAP.put(10023, "FastEthernet0/23");
        NMMSW1_IF_MAC_MAP.put(10023, "a418750ace17");
        NMMSW1_IF_IFNAME_MAP.put(10013, "Fa0/13");
        NMMSW1_IF_IFDESCR_MAP.put(10013, "FastEthernet0/13");
        NMMSW1_IF_MAC_MAP.put(10013, "a418750ace0d");
        NMMSW1_IF_IFNAME_MAP.put(10012, "Fa0/12");
        NMMSW1_IF_IFDESCR_MAP.put(10012, "FastEthernet0/12");
        NMMSW1_IF_MAC_MAP.put(10012, "a418750ace0c");
        NMMSW1_IF_IFNAME_MAP.put(10009, "Fa0/9");
        NMMSW1_IF_IFDESCR_MAP.put(10009, "FastEthernet0/9");
        NMMSW1_IF_MAC_MAP.put(10009, "a418750ace09");
        NMMSW1_IF_IFNAME_MAP.put(10007, "Fa0/7");
        NMMSW1_IF_IFDESCR_MAP.put(10007, "FastEthernet0/7");
        NMMSW1_IF_MAC_MAP.put(10007, "a418750ace07");
        NMMSW1_IF_IFNAME_MAP.put(10014, "Fa0/14");
        NMMSW1_IF_IFDESCR_MAP.put(10014, "FastEthernet0/14");
        NMMSW1_IF_MAC_MAP.put(10014, "a418750ace0e");
        NMMSW1_IF_IFNAME_MAP.put(10002, "Fa0/2");
        NMMSW1_IF_IFDESCR_MAP.put(10002, "FastEthernet0/2");
        NMMSW1_IF_MAC_MAP.put(10002, "a418750ace02");
        NMMSW1_IF_IFNAME_MAP.put(10016, "Fa0/16");
        NMMSW1_IF_IFDESCR_MAP.put(10016, "FastEthernet0/16");
        NMMSW1_IF_MAC_MAP.put(10016, "a418750ace10");
        NMMSW1_IF_IFNAME_MAP.put(10015, "Fa0/15");
        NMMSW1_IF_IFDESCR_MAP.put(10015, "FastEthernet0/15");
        NMMSW1_IF_MAC_MAP.put(10015, "a418750ace0f");
        NMMSW1_IF_IFNAME_MAP.put(10022, "Fa0/22");
        NMMSW1_IF_IFDESCR_MAP.put(10022, "FastEthernet0/22");
        NMMSW1_IF_MAC_MAP.put(10022, "a418750ace16");
        NMMSW1_IF_IFNAME_MAP.put(10004, "Fa0/4");
        NMMSW1_IF_IFDESCR_MAP.put(10004, "FastEthernet0/4");
        NMMSW1_IF_MAC_MAP.put(10004, "a418750ace04");
        NMMSW1_IF_IFNAME_MAP.put(10008, "Fa0/8");
        NMMSW1_IF_IFDESCR_MAP.put(10008, "FastEthernet0/8");
        NMMSW1_IF_MAC_MAP.put(10008, "a418750ace08");
        
        NMMSW2_IP_IF_MAP.put(InetAddressUtils.addr("192.168.42.10"), 1);
        NMMSW2_IF_IFNAME_MAP.put(10022, "Fa0/22");
        NMMSW2_IF_IFDESCR_MAP.put(10022, "FastEthernet0/22");
        NMMSW2_IF_MAC_MAP.put(10022, "f4ea676a1396");
        NMMSW2_IF_IFNAME_MAP.put(10001, "Fa0/1");
        NMMSW2_IF_IFDESCR_MAP.put(10001, "FastEthernet0/1");
        NMMSW2_IF_MAC_MAP.put(10001, "f4ea676a1381");
        NMMSW2_IF_IFNAME_MAP.put(10012, "Fa0/12");
        NMMSW2_IF_IFDESCR_MAP.put(10012, "FastEthernet0/12");
        NMMSW2_IF_MAC_MAP.put(10012, "f4ea676a138c");
        NMMSW2_IF_IFNAME_MAP.put(10008, "Fa0/8");
        NMMSW2_IF_IFDESCR_MAP.put(10008, "FastEthernet0/8");
        NMMSW2_IF_MAC_MAP.put(10008, "f4ea676a1388");
        NMMSW2_IF_IFNAME_MAP.put(10101, "Gi0/1");
        NMMSW2_IF_IFDESCR_MAP.put(10101, "GigabitEthernet0/1");
        NMMSW2_IF_MAC_MAP.put(10101, "f4ea676a1399");
        NMMSW2_IF_IFNAME_MAP.put(10010, "Fa0/10");
        NMMSW2_IF_IFDESCR_MAP.put(10010, "FastEthernet0/10");
        NMMSW2_IF_MAC_MAP.put(10010, "f4ea676a138a");
        NMMSW2_IF_IFNAME_MAP.put(10005, "Fa0/5");
        NMMSW2_IF_IFDESCR_MAP.put(10005, "FastEthernet0/5");
        NMMSW2_IF_MAC_MAP.put(10005, "f4ea676a1385");
        NMMSW2_IF_IFNAME_MAP.put(10018, "Fa0/18");
        NMMSW2_IF_IFDESCR_MAP.put(10018, "FastEthernet0/18");
        NMMSW2_IF_MAC_MAP.put(10018, "f4ea676a1392");
        NMMSW2_IF_IFNAME_MAP.put(10501, "Nu0");
        NMMSW2_IF_IFDESCR_MAP.put(10501, "Null0");
        NMMSW2_IF_IFNAME_MAP.put(10003, "Fa0/3");
        NMMSW2_IF_IFDESCR_MAP.put(10003, "FastEthernet0/3");
        NMMSW2_IF_MAC_MAP.put(10003, "f4ea676a1383");
        NMMSW2_IF_IFNAME_MAP.put(10024, "Fa0/24");
        NMMSW2_IF_IFDESCR_MAP.put(10024, "FastEthernet0/24");
        NMMSW2_IF_MAC_MAP.put(10024, "f4ea676a1398");
        NMMSW2_IF_IFNAME_MAP.put(10002, "Fa0/2");
        NMMSW2_IF_IFDESCR_MAP.put(10002, "FastEthernet0/2");
        NMMSW2_IF_MAC_MAP.put(10002, "f4ea676a1382");
        NMMSW2_IF_IFNAME_MAP.put(10006, "Fa0/6");
        NMMSW2_IF_IFDESCR_MAP.put(10006, "FastEthernet0/6");
        NMMSW2_IF_MAC_MAP.put(10006, "f4ea676a1386");
        NMMSW2_IF_IFNAME_MAP.put(10102, "Gi0/2");
        NMMSW2_IF_IFDESCR_MAP.put(10102, "GigabitEthernet0/2");
        NMMSW2_IF_MAC_MAP.put(10102, "f4ea676a139a");
        NMMSW2_IF_IFNAME_MAP.put(10016, "Fa0/16");
        NMMSW2_IF_IFDESCR_MAP.put(10016, "FastEthernet0/16");
        NMMSW2_IF_MAC_MAP.put(10016, "f4ea676a1390");
        NMMSW2_IF_IFNAME_MAP.put(10007, "Fa0/7");
        NMMSW2_IF_IFDESCR_MAP.put(10007, "FastEthernet0/7");
        NMMSW2_IF_MAC_MAP.put(10007, "f4ea676a1387");
        NMMSW2_IF_IFNAME_MAP.put(10015, "Fa0/15");
        NMMSW2_IF_IFDESCR_MAP.put(10015, "FastEthernet0/15");
        NMMSW2_IF_MAC_MAP.put(10015, "f4ea676a138f");
        NMMSW2_IF_IFNAME_MAP.put(10020, "Fa0/20");
        NMMSW2_IF_IFDESCR_MAP.put(10020, "FastEthernet0/20");
        NMMSW2_IF_MAC_MAP.put(10020, "f4ea676a1394");
        NMMSW2_IF_IFNAME_MAP.put(10013, "Fa0/13");
        NMMSW2_IF_IFDESCR_MAP.put(10013, "FastEthernet0/13");
        NMMSW2_IF_MAC_MAP.put(10013, "f4ea676a138d");
        NMMSW2_IF_IFNAME_MAP.put(10004, "Fa0/4");
        NMMSW2_IF_IFDESCR_MAP.put(10004, "FastEthernet0/4");
        NMMSW2_IF_MAC_MAP.put(10004, "f4ea676a1384");
        NMMSW2_IF_IFNAME_MAP.put(10011, "Fa0/11");
        NMMSW2_IF_IFDESCR_MAP.put(10011, "FastEthernet0/11");
        NMMSW2_IF_MAC_MAP.put(10011, "f4ea676a138b");
        NMMSW2_IF_IFNAME_MAP.put(10023, "Fa0/23");
        NMMSW2_IF_IFDESCR_MAP.put(10023, "FastEthernet0/23");
        NMMSW2_IF_MAC_MAP.put(10023, "f4ea676a1397");
        NMMSW2_IF_IFNAME_MAP.put(10017, "Fa0/17");
        NMMSW2_IF_IFDESCR_MAP.put(10017, "FastEthernet0/17");
        NMMSW2_IF_MAC_MAP.put(10017, "f4ea676a1391");
        NMMSW2_IF_IFNAME_MAP.put(10014, "Fa0/14");
        NMMSW2_IF_IFDESCR_MAP.put(10014, "FastEthernet0/14");
        NMMSW2_IF_MAC_MAP.put(10014, "f4ea676a138e");
        NMMSW2_IF_IFNAME_MAP.put(10009, "Fa0/9");
        NMMSW2_IF_IFDESCR_MAP.put(10009, "FastEthernet0/9");
        NMMSW2_IF_MAC_MAP.put(10009, "f4ea676a1389");
        NMMSW2_IF_IFNAME_MAP.put(10019, "Fa0/19");
        NMMSW2_IF_IFDESCR_MAP.put(10019, "FastEthernet0/19");
        NMMSW2_IF_MAC_MAP.put(10019, "f4ea676a1393");
        NMMSW2_IF_IFNAME_MAP.put(1, "Vl1");
        NMMSW2_IF_IFDESCR_MAP.put(1, "Vlan1");
        NMMSW2_IF_MAC_MAP.put(1, "f4ea676a13c0");
        NMMSW2_IF_NETMASK_MAP.put(1, InetAddressUtils.addr("255.255.255.0"));
        NMMSW2_IF_IFNAME_MAP.put(10021, "Fa0/21");
        NMMSW2_IF_IFDESCR_MAP.put(10021, "FastEthernet0/21");
        NMMSW2_IF_MAC_MAP.put(10021, "f4ea676a1395");
    } catch (Exception e) {
        
    }
    }
    
    public OnmsNode getNMMR1() {
        return getNode(NMMR1_NAME,NMMR1_SYSOID,NMMR1_IP,NMMR1_IP_IF_MAP,NMMR1_IF_IFNAME_MAP,
                       NMMR1_IF_MAC_MAP,NMMR1_IF_IFDESCR_MAP,NMMR1_IF_IFALIAS_MAP);
    }    
    
    public OnmsNode getNMMR2() {
        return getNode(NMMR2_NAME,NMMR2_SYSOID,NMMR2_IP,NMMR2_IP_IF_MAP,NMMR2_IF_IFNAME_MAP,
                       NMMR2_IF_MAC_MAP,NMMR2_IF_IFDESCR_MAP,NMMR2_IF_IFALIAS_MAP);
    }    

    public OnmsNode getNMMR3() {
        return getNode(NMMR3_NAME,NMMR3_SYSOID,NMMR3_IP,NMMR3_IP_IF_MAP,NMMR3_IF_IFNAME_MAP,
                       NMMR3_IF_MAC_MAP,NMMR3_IF_IFDESCR_MAP,NMMR3_IF_IFALIAS_MAP);
    }    

    public OnmsNode getNMMSW1() {
        return getNode(NMMSW1_NAME,NMMSW1_SYSOID,NMMSW1_IP,NMMSW1_IP_IF_MAP,NMMSW1_IF_IFNAME_MAP,
                       NMMSW1_IF_MAC_MAP,NMMSW1_IF_IFDESCR_MAP,NMMSW1_IF_IFALIAS_MAP);
    }    

    public OnmsNode getNMMSW2() {
        return getNode(NMMSW2_NAME,NMMSW2_SYSOID,NMMSW2_IP,NMMSW2_IP_IF_MAP,NMMSW2_IF_IFNAME_MAP,
                       NMMSW2_IF_MAC_MAP,NMMSW2_IF_IFDESCR_MAP,NMMSW2_IF_IFALIAS_MAP);
    }    

}
