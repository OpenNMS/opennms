/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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


import java.util.HashMap;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsNode;

/**
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:antonio@opennme.it">Antonio Russo</a>
 * @author <a href="mailto:alejandro@opennms.org">Alejandro Galue</a>
 */

/*
 * This is a Linkd with capsd test helper class.
 * This will reproduce a specified network
 * for that we have walked the snmp agent
 * 
 */

/*
 * Here are relevant device descriptions for link detection:
 * 
 * CISCO_C870:172.20.1.1:001f6cd034e7:12:Vlan1
 * Run the spanning tree with bridge identifier: 00000c83d9a8
 * Type SRT
 *
 *
 * CISCO_WS_C2948_IP:172.20.1.7:0002baaacffe:3:me1
 * Run the spanning tree protocol
 * with bridge identifier: 0002baaacc00
 * Transparent Bridge
 * 
 * NETGEAR_SW_108:172.20.1.8:00223ff00b7b::
 * Run the spanning tree protocol
 * with bridge identifier: 00223ff00b7b
 * Transparent Bridge
 * 
 * LINUX_UBUNTU:172.20.1.14:406186e28b53:4:br0
 * 
 * DARWIN_10_8:172.20.1.28:0026b0ed8fb8:4:en0
 * 
 * Here are listed the links
 * in the test network laboratory
 * 
 * CISCO_C870:  port3  ------> port 44:CISCO_WS_C2948_IP
 * LINUX_UBUNTU:port4  ------> port 11:CISCO_WS_C2948_IP
 * NETGEAR_SW_108:port8------> port 9 :CISCO_WS_C2948_IP
 * DARWIN_10_8:port4   ------> port 1 :NETGEAR_SW_108
 * 
 * We want to test here:
 * 
 * That collected data is properly saved to tables:
 * vlan, stpnode, stpinterface, atinterface and iprouteinterface.
 * 
 * Linkd in the default configuration discovers all previously
 * defined links and they are properly saved into
 * datalinkinterface table
 * 
 * 
 * 
 */

public class Nms7467NetworkBuilder extends NmsNetworkBuilder {
 
    /* 
     * nodelabel:ip:mac:ifindex:ifdescr
     * CISCO_C870:172.20.1.1:001f6cd034e7:12:Vlan1
     * CISCO_C870:172.20.2.1:001f6cd034e7:13:Vlan2
     * CISCO_C870:10.255.255.2:001f6cd034e7:12:Vlan1
     * CISCO_C870:65.41.39.146:00000c03b09e:14:BVI1
     * 
     * Here are the expected columns:
     * 
     *  snmpifindex |    snmpifname     |    snmpifdescr    | snmpphysaddr 
     *-------------+-------------------+-------------------+--------------
     *         13 | Vl2               | Vlan2             | 001f6cd034e7
     *         12 | Vl1               | Vlan1             | 001f6cd034e7
     *         14 | BV1               | BVI1              | 00000c03b09e
     *         18 | NV0               | NVI0              | 
     *         17 | Tu0               | Tunnel0           | 
     *         11 | AT0-adsl          | ATM0-adsl         | 
     *         10 | ATM0.0-aal5 layer | ATM0.0-aal5 layer | 
     *          9 | ATM0-aal5 layer   | ATM0-aal5 layer   | 
     *          8 | ATM0.0-atm subif  | ATM0.0-atm subif  | 
     *          7 | ATM0-atm layer    | ATM0-atm layer    | 
     *          6 | Nu0               | Null0             | 
     *          5 | AT0               | ATM0              | 
     *          4 | Fa3               | FastEthernet3     | 001f6cd034ea
     *          3 | Fa2               | FastEthernet2     | 001f6cd034e9
     *          2 | Fa1               | FastEthernet1     | 001f6cd034e8
     *          1 | Fa0               | FastEthernet0     | 001f6cd034e7
     * 
     * 
     *  Run the spanning tree with bridge identifier: 00000c83d9a8
     *  Type SRT
     *
     */

    static {
        CISCO_C870_IP_IF_MAP.put(InetAddressUtils.addr("172.20.1.1"), 12);
        CISCO_C870_IP_IF_MAP.put(InetAddressUtils.addr("10.255.255.2"), 12);
        CISCO_C870_IP_IF_MAP.put(InetAddressUtils.addr("172.20.2.1"), 13);
        CISCO_C870_IP_IF_MAP.put(InetAddressUtils.addr("65.41.39.146"), 14);            

        CISCO_C870_IF_IFNAME_MAP.put(1, "Fa0");
        CISCO_C870_IF_IFNAME_MAP.put(2, "Fa1");
        CISCO_C870_IF_IFNAME_MAP.put(3, "Fa2");
        CISCO_C870_IF_IFNAME_MAP.put(4, "Fa3");
        CISCO_C870_IF_IFNAME_MAP.put(5, "AT0");
        CISCO_C870_IF_IFNAME_MAP.put(6, "Nu0");
        CISCO_C870_IF_IFNAME_MAP.put(7, "ATM0-atm layer");
        CISCO_C870_IF_IFNAME_MAP.put(8, "ATM0.0-atm subif");
        CISCO_C870_IF_IFNAME_MAP.put(9, "ATM0-aal5 layer");
        CISCO_C870_IF_IFNAME_MAP.put(10, "ATM0.0-aal5 layer");
        CISCO_C870_IF_IFNAME_MAP.put(11, "AT0-adsl");
        CISCO_C870_IF_IFNAME_MAP.put(12, "Vl1");
        CISCO_C870_IF_IFNAME_MAP.put(13, "Vl2");       
        CISCO_C870_IF_IFNAME_MAP.put(14, "BV1");
        CISCO_C870_IF_IFNAME_MAP.put(17, "Tu0");        
        CISCO_C870_IF_IFNAME_MAP.put(18, "NV0");        

        CISCO_C870_IF_IFDESCR_MAP.put(1, "FastEthernet0");
        CISCO_C870_IF_IFDESCR_MAP.put(2, "FastEthernet1");
        CISCO_C870_IF_IFDESCR_MAP.put(3, "FastEthernet2");
        CISCO_C870_IF_IFDESCR_MAP.put(4, "FastEthernet3");
        CISCO_C870_IF_IFDESCR_MAP.put(5, "ATM0");
        CISCO_C870_IF_IFDESCR_MAP.put(6, "Null0");
        CISCO_C870_IF_IFDESCR_MAP.put(7, "ATM0-atm layer");
        CISCO_C870_IF_IFDESCR_MAP.put(8, "ATM0.0-atm subif");
        CISCO_C870_IF_IFDESCR_MAP.put(9, "ATM0-aal5 layer");
        CISCO_C870_IF_IFDESCR_MAP.put(10, "ATM0.0-aal5 layer");
        CISCO_C870_IF_IFDESCR_MAP.put(11, "ATM0-adsl");
        CISCO_C870_IF_IFDESCR_MAP.put(12, "Vlan1");
        CISCO_C870_IF_IFDESCR_MAP.put(13, "Vlan2");       
        CISCO_C870_IF_IFDESCR_MAP.put(14, "BVI1");
        CISCO_C870_IF_IFDESCR_MAP.put(17, "Tunnel0");        
        CISCO_C870_IF_IFDESCR_MAP.put(18, "NVI0");        

        CISCO_C870_IF_MAC_MAP.put(1,  "001f6cd034e7");
        CISCO_C870_IF_MAC_MAP.put(2,  "001f6cd034e8");
        CISCO_C870_IF_MAC_MAP.put(3,  "001f6cd034e9");
        CISCO_C870_IF_MAC_MAP.put(4,  "001f6cd034ea");
        CISCO_C870_IF_MAC_MAP.put(12, "001f6cd034e7");
        CISCO_C870_IF_MAC_MAP.put(13, "001f6cd034e7");
        CISCO_C870_IF_MAC_MAP.put(14, "00000c03b09e");
    }

    /*
     * nodelabel:ip:mac:ifindex:ifdescr
     *      
     * CISCO_WS_C2948_IP:172.20.1.7:0002baaacffe:3:me1
     * this device ha 48 Ports
     * the mac address range is: 0002baaacf00 0002baaacfff
     *  snmpifindex | snmpifname |            snmpifdescr            | snmpphysaddr 
     *-------------+------------+-----------------------------------+--------------
     *          1 | sc0        | sc0                               | 0002baaacfff
     *          2 | sl0        | sl0                               | 000000000000
     *          3 | me1        | me1                               | 0002baaacffe
     *          4 | VLAN-1     | VLAN 1                            | 0002baaacc00
     *          5 | VLAN-1002  | VLAN 1002                         | 0:2:ba:aa:cf:e9
     *          6 | VLAN-1004  | VLAN 1004                         | 0:2:ba:aa:cf:eb
     *          7 | VLAN-1005  | VLAN 1005                         | 0:2:ba:aa:cf:ec
     *          8 | VLAN-1003  | VLAN 1003                         | 0:2:ba:aa:cf:ea
     *          9 | 2/1        | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:9e
     *         10 | 2/2        | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:9f
     *         11 | 2/3        | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:a0
     *         12 | 2/4        | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:a1
     *         13 | 2/5        | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:a2
     *         14 | 2/6        | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:a3
     *         15 | 2/7        | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:a4
     *         16 | 2/8        | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:a5
     *         17 | 2/9        | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:a6
     *         18 | 2/10       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:a7
     *         19 | 2/11       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:a8
     *         20 | 2/12       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:a9
     *         21 | 2/13       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:aa
     *         22 | 2/14       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:ab
     *         23 | 2/15       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:ac
     *         24 | 2/16       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:ad
     *         25 | 2/17       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:ae
     *         26 | 2/18       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:af
     *         27 | 2/19       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:b0
     *         28 | 2/20       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:b1
     *         29 | 2/21       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:b2
     *         30 | 2/22       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:b3
     *         31 | 2/23       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:b4
     *         32 | 2/24       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:b5
     *         33 | 2/25       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:b6
     *         34 | 2/26       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:b7
     *         35 | 2/27       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:b8
     *         36 | 2/28       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:b9
     *         37 | 2/29       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:ba
     *         38 | 2/30       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:bb
     *         39 | 2/31       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:bc
     *         40 | 2/32       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:bd
     *         41 | 2/33       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:b3
     *         42 | 2/34       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:bf
     *         43 | 2/35       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:c0
     *         44 | 2/36       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:c1
     *         45 | 2/37       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:c2
     *         46 | 2/38       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:c3
     *         47 | 2/39       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:c4
     *         48 | 2/40       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:c5
     *         49 | 2/41       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:c6
     *         50 | 2/42       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:c7
     *         51 | 2/43       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:c8
     *         52 | 2/44       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:c9
     *         53 | 2/45       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:ca
     *         54 | 2/46       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:cb
     *         55 | 2/47       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:cc
     *         56 | 2/48       | 10/100 utp ethernet (cat 3/5)     | 0:2:ba:aa:cf:cd
     *         57 | 2/49       | short wave fiber gigabit ethernet | 0:2:ba:aa:cf:ce
     *         58 | 2/50       | short wave fiber gigabit ethernet | 0:2:ba:aa:cf:cf
     *
     * Run the spanning tree with bridge identifier: 0002baaacc00
     * Transparent Bridge
     */ 

    static {
        CISCO_WS_C2948_IP_IF_MAP.put(InetAddressUtils.addr(CISCO_WS_C2948_IP), 3);

        CISCO_WS_C2948_IF_IFNAME_MAP.put(1,"sc0");
        CISCO_WS_C2948_IF_IFNAME_MAP.put(2,"sl0");
        CISCO_WS_C2948_IF_IFNAME_MAP.put(3,"me1");
        CISCO_WS_C2948_IF_IFNAME_MAP.put(4,"VLAN-1");
        CISCO_WS_C2948_IF_IFNAME_MAP.put(5,"VLAN-1002");
        CISCO_WS_C2948_IF_IFNAME_MAP.put(6,"VLAN-1003");
        CISCO_WS_C2948_IF_IFNAME_MAP.put(7,"VLAN-1004");
        CISCO_WS_C2948_IF_IFNAME_MAP.put(8,"VLAN-1005");
        for (int ifindex=9;ifindex<59;ifindex++ ) {
            int i = ifindex - 8;
            String ifname = "2/"+ i;
            CISCO_WS_C2948_IF_IFNAME_MAP.put(ifindex,ifname);
        }

        CISCO_WS_C2948_IF_MAC_MAP.put(1,"0002baaacfff");
        CISCO_WS_C2948_IF_MAC_MAP.put(2,"000000000000");
        CISCO_WS_C2948_IF_MAC_MAP.put(3,"0002baaacffe");
        CISCO_WS_C2948_IF_MAC_MAP.put(4,"0002baaacf00");
        CISCO_WS_C2948_IF_MAC_MAP.put(5,"0002baaacfe9");
        CISCO_WS_C2948_IF_MAC_MAP.put(6,"0002baaacfeb");
        CISCO_WS_C2948_IF_MAC_MAP.put(7,"0002baaacfec");
        CISCO_WS_C2948_IF_MAC_MAP.put(8,"0002baaacfea");
        CISCO_WS_C2948_IF_MAC_MAP.put(9,"0002baaacf9e");
        CISCO_WS_C2948_IF_MAC_MAP.put(10,"0002baaacf9f");
        CISCO_WS_C2948_IF_MAC_MAP.put(11,"0002baaacfa0");
        CISCO_WS_C2948_IF_MAC_MAP.put(12,"0002baaacfa1");
        CISCO_WS_C2948_IF_MAC_MAP.put(13,"0002baaacfa2");
        CISCO_WS_C2948_IF_MAC_MAP.put(14,"0002baaacfa3");
        CISCO_WS_C2948_IF_MAC_MAP.put(15,"0002baaacfa4");
        CISCO_WS_C2948_IF_MAC_MAP.put(16,"0002baaacfa5");
        CISCO_WS_C2948_IF_MAC_MAP.put(17,"0002baaacfa6");
        CISCO_WS_C2948_IF_MAC_MAP.put(18,"0002baaacfa7");
        CISCO_WS_C2948_IF_MAC_MAP.put(19,"0002baaacfa8");
        CISCO_WS_C2948_IF_MAC_MAP.put(20,"0002baaacfa9");
        CISCO_WS_C2948_IF_MAC_MAP.put(21,"0002baaacfaa");
        CISCO_WS_C2948_IF_MAC_MAP.put(22,"0002baaacfab");
        CISCO_WS_C2948_IF_MAC_MAP.put(23,"0002baaacfac");
        CISCO_WS_C2948_IF_MAC_MAP.put(24,"0002baaacfad");
        CISCO_WS_C2948_IF_MAC_MAP.put(25,"0002baaacfae");
        CISCO_WS_C2948_IF_MAC_MAP.put(26,"0002baaacfaf");
        CISCO_WS_C2948_IF_MAC_MAP.put(27,"0002baaacfb0");
        CISCO_WS_C2948_IF_MAC_MAP.put(28,"0002baaacfb1");
        CISCO_WS_C2948_IF_MAC_MAP.put(29,"0002baaacfb2");
        CISCO_WS_C2948_IF_MAC_MAP.put(30,"0002baaacfb3");
        CISCO_WS_C2948_IF_MAC_MAP.put(31,"0002baaacfb4");
        CISCO_WS_C2948_IF_MAC_MAP.put(32,"0002baaacfb5");
        CISCO_WS_C2948_IF_MAC_MAP.put(33,"0002baaacfb6");
        CISCO_WS_C2948_IF_MAC_MAP.put(34,"0002baaacfb7");
        CISCO_WS_C2948_IF_MAC_MAP.put(35,"0002baaacfb8");
        CISCO_WS_C2948_IF_MAC_MAP.put(36,"0002baaacfb9");
        CISCO_WS_C2948_IF_MAC_MAP.put(37,"0002baaacfba");
        CISCO_WS_C2948_IF_MAC_MAP.put(38,"0002baaacfbb");
        CISCO_WS_C2948_IF_MAC_MAP.put(39,"0002baaacfbc");
        CISCO_WS_C2948_IF_MAC_MAP.put(40,"0002baaacfbd");
        CISCO_WS_C2948_IF_MAC_MAP.put(41,"0002baaacfbe");
        CISCO_WS_C2948_IF_MAC_MAP.put(42,"0002baaacfbf");
        CISCO_WS_C2948_IF_MAC_MAP.put(43,"0002baaacfc0");
        CISCO_WS_C2948_IF_MAC_MAP.put(44,"0002baaacfc1");
        CISCO_WS_C2948_IF_MAC_MAP.put(45,"0002baaacfc2");
        CISCO_WS_C2948_IF_MAC_MAP.put(46,"0002baaacfc3");
        CISCO_WS_C2948_IF_MAC_MAP.put(47,"0002baaacfc4");
        CISCO_WS_C2948_IF_MAC_MAP.put(48,"0002baaacfc5");
        CISCO_WS_C2948_IF_MAC_MAP.put(49,"0002baaacfc6");
        CISCO_WS_C2948_IF_MAC_MAP.put(50,"0002baaacfc7");
        CISCO_WS_C2948_IF_MAC_MAP.put(51,"0002baaacfc8");
        CISCO_WS_C2948_IF_MAC_MAP.put(52,"0002baaacfc9");
        CISCO_WS_C2948_IF_MAC_MAP.put(53,"0002baaacfca");
        CISCO_WS_C2948_IF_MAC_MAP.put(54,"0002baaacfcb");
        CISCO_WS_C2948_IF_MAC_MAP.put(55,"0002baaacfcc");
        CISCO_WS_C2948_IF_MAC_MAP.put(56,"0002baaacfcd");
        CISCO_WS_C2948_IF_MAC_MAP.put(57,"0002baaacfce");
        CISCO_WS_C2948_IF_MAC_MAP.put(58,"0002baaacfcf");
 
    }

    /*
     *  nodelabel:ip:mac:ifindex:ifdescr
     *  NETGEAR_SW_108:172.20.1.8:00223ff00b7b::
     * 
     *  snmpifindex | snmpifname |       snmpifdescr       | snmpphysaddr 
     *-------------+------------+-------------------------+--------------
     *          1 |            | Port 1 Gigabit Ethernet | 00223ff00b7c
     *          2 |            | Port 2 Gigabit Ethernet | 00223ff00b7d
     *          3 |            | Port 3 Gigabit Ethernet | 00223ff00b7e
     *          4 |            | Port 4 Gigabit Ethernet | 00223ff00b7f
     *          5 |            | Port 5 Gigabit Ethernet | 00223ff00b80
     *          6 |            | Port 6 Gigabit Ethernet | 00223ff00b81
     *          7 |            | Port 7 Gigabit Ethernet | 00223ff00b82
     *          8 |            | Port 8 Gigabit Ethernet | 00223ff00b83
     *
     * Run the spanning tree with bridge identifier: 00223ff00b7b
     * Transparent Bridge
     * 
     */
    static {
        NETGEAR_SW_108_IP_IF_MAP.put(InetAddressUtils.addr(NETGEAR_SW_108_IP), null);
        NETGEAR_SW_108_IF_IFNAME_MAP.put(1, "");
        NETGEAR_SW_108_IF_IFNAME_MAP.put(2, "");
        NETGEAR_SW_108_IF_IFNAME_MAP.put(3, "");
        NETGEAR_SW_108_IF_IFNAME_MAP.put(4, "");
        NETGEAR_SW_108_IF_IFNAME_MAP.put(5, "");
        NETGEAR_SW_108_IF_IFNAME_MAP.put(6, "");
        NETGEAR_SW_108_IF_IFNAME_MAP.put(7, "");
        NETGEAR_SW_108_IF_IFNAME_MAP.put(8, "");

        NETGEAR_SW_108_IF_MAC_MAP.put(1, "00223ff00b7c");
        NETGEAR_SW_108_IF_MAC_MAP.put(2, "00223ff00b7d");
        NETGEAR_SW_108_IF_MAC_MAP.put(3, "00223ff00b7e");
        NETGEAR_SW_108_IF_MAC_MAP.put(4, "00223ff00b7f");
        NETGEAR_SW_108_IF_MAC_MAP.put(5, "00223ff00b80");
        NETGEAR_SW_108_IF_MAC_MAP.put(6, "00223ff00b81");
        NETGEAR_SW_108_IF_MAC_MAP.put(7, "00223ff00b82");
        NETGEAR_SW_108_IF_MAC_MAP.put(8, "00223ff00b83");
    }
    
    /* 
     * LINUX_UBUNTU:172.20.1.14:406186e28b53:4:br0
     * 
     *   snmpifindex | snmpifname | snmpifdescr | snmpphysaddr 
     *-------------+------------+-------------+--------------
     *          1 | lo         | lo          | 
     *          2 | eth0       | eth0        | 406186e28b53
     *          3 | wlan0      | wlan0       | 70f1a1085de7
     *          4 | br0        | br0         | 406186e28b53
     *          5 | virbr0     | virbr0      | 56df68c9ab38
     *          9 | vnet0      | vnet0       | fe54000d420a
     *         11 | eth1       | eth1        | 9227e40d2b88
     *  
     *  
     *  ipv4/ipv6 address table:
     * 
     *                  ipaddr                 | ifindex 
     *-----------------------------------------+---------
     * 172.20.1.14                             |       4
     * 192.168.122.1                           |       5
     * 2001:0470:e2f1:0000:4261:86ff:fee2:8b53 |       4
     * 2001:0470:e2f1:0000:695c:e7ef:425e:63b0 |       4
     * 2001:0470:e2f1:cafe:0dc0:e717:08d3:e5d6 |       4
     * 2001:0470:e2f1:cafe:1ce0:7066:22d0:a7d6 |       4
     * 2001:0470:e2f1:cafe:2c16:6246:431b:b906 |       4
     * 2001:0470:e2f1:cafe:31dc:c786:65ac:d3b1 |       4
     * 2001:0470:e2f1:cafe:4261:86ff:fee2:8b53 |       4
     * 2001:0470:e2f1:cafe:4c99:0b4c:ff7b:373b |       4
     * 2001:0470:e2f1:cafe:5025:e9c7:5a63:74b4 |       4
     * 2001:0470:e2f1:cafe:695c:e7ef:425e:63b0 |       4
     * 2001:0470:e2f1:cafe:697f:fe0e:9e47:1db8 |       4
     * 2001:0470:e2f1:cafe:7c4d:9f42:d02a:c8bd |       4
     * 2001:0470:e2f1:cafe:c1d6:02ad:621a:6401 |       4
     * 2001:0470:e2f1:cafe:e17a:e1db:31e1:2a2d |       4
     * 2001:0470:e2f1:cafe:e8f5:957c:ef2a:f427 |       4
     * 
     */ 

    static {
        LINUX_UBUNTU_IP_IF_MAP.put(InetAddressUtils.addr("172.20.1.14"), 4);
        LINUX_UBUNTU_IP_IF_MAP.put(InetAddressUtils.addr("192.168.122.1"), 5);
        LINUX_UBUNTU_IP_IF_MAP.put(InetAddressUtils.addr("2001:0470:e2f1:0000:4261:86ff:fee2:8b53"), 4);
        LINUX_UBUNTU_IP_IF_MAP.put(InetAddressUtils.addr("2001:0470:e2f1:0000:695c:e7ef:425e:63b0"), 4);
        LINUX_UBUNTU_IP_IF_MAP.put(InetAddressUtils.addr("2001:0470:e2f1:cafe:0dc0:e717:08d3:e5d6"), 4);
        LINUX_UBUNTU_IP_IF_MAP.put(InetAddressUtils.addr("2001:0470:e2f1:cafe:1ce0:7066:22d0:a7d6"), 4);
        LINUX_UBUNTU_IP_IF_MAP.put(InetAddressUtils.addr("2001:0470:e2f1:cafe:2c16:6246:431b:b906"), 4);
        LINUX_UBUNTU_IP_IF_MAP.put(InetAddressUtils.addr("2001:0470:e2f1:cafe:31dc:c786:65ac:d3b1"), 4);
        LINUX_UBUNTU_IP_IF_MAP.put(InetAddressUtils.addr("2001:0470:e2f1:cafe:4261:86ff:fee2:8b53"), 4);
        LINUX_UBUNTU_IP_IF_MAP.put(InetAddressUtils.addr("2001:0470:e2f1:cafe:4c99:0b4c:ff7b:373b"), 4);
        LINUX_UBUNTU_IP_IF_MAP.put(InetAddressUtils.addr("2001:0470:e2f1:cafe:5025:e9c7:5a63:74b4"), 4);
        LINUX_UBUNTU_IP_IF_MAP.put(InetAddressUtils.addr("2001:0470:e2f1:cafe:695c:e7ef:425e:63b0"), 4);
        LINUX_UBUNTU_IP_IF_MAP.put(InetAddressUtils.addr("2001:0470:e2f1:cafe:697f:fe0e:9e47:1db8"), 4);
        LINUX_UBUNTU_IP_IF_MAP.put(InetAddressUtils.addr("2001:0470:e2f1:cafe:7c4d:9f42:d02a:c8bd"), 4);
        LINUX_UBUNTU_IP_IF_MAP.put(InetAddressUtils.addr("2001:0470:e2f1:cafe:c1d6:02ad:621a:6401"), 4);
        LINUX_UBUNTU_IP_IF_MAP.put(InetAddressUtils.addr("2001:0470:e2f1:cafe:e17a:e1db:31e1:2a2d"), 4);
        LINUX_UBUNTU_IP_IF_MAP.put(InetAddressUtils.addr("2001:0470:e2f1:cafe:e8f5:957c:ef2a:f427"), 4);

        LINUX_UBUNTU_IF_IFNAME_MAP.put(1, "lo0");
        LINUX_UBUNTU_IF_IFNAME_MAP.put(2, "eth0");
        LINUX_UBUNTU_IF_IFNAME_MAP.put(3, "vlan0");
        LINUX_UBUNTU_IF_IFNAME_MAP.put(4, "br0");
        LINUX_UBUNTU_IF_IFNAME_MAP.put(5, "virbr0");
        LINUX_UBUNTU_IF_IFNAME_MAP.put(9, "vnet0");
        LINUX_UBUNTU_IF_IFNAME_MAP.put(11, "eth1");

        LINUX_UBUNTU_IF_MAC_MAP.put(2, "406186e28b53");
        LINUX_UBUNTU_IF_MAC_MAP.put(3, "70f1a1085de7");
        LINUX_UBUNTU_IF_MAC_MAP.put(4, "406186e28b53");
        LINUX_UBUNTU_IF_MAC_MAP.put(5, "56df68c9ab38");
        LINUX_UBUNTU_IF_MAC_MAP.put(9, "fe54000d420a");
        LINUX_UBUNTU_IF_MAC_MAP.put(11, "9227e40d2b88");
    }

    /* DARWIN_10_8:172.20.1.28:0026b0ed8fb8:4:en0
     * 
     *  snmpifindex | snmpifname | snmpifdescr | snmpphysaddr 
     *-------------+------------+-------------+--------------
     *          1 | lo0        | lo0         | 
     *          2 | gif0       | gif0        | 
     *          3 | stf0       | stf0        | 
     *          4 | en0        | en0         | 0026b0ed8fb8
     *          5 | fw0        | fw0         | 0026b0fffeed8fb8
     *          6 | en1        | en1         | 002608f86155
     * 
     * 
     * 
     */

    static {
        DARWIN_10_8_IP_IF_MAP.put(InetAddressUtils.addr(DARWIN_10_8_IP), 4);

        DARWIN_10_8_IF_IFNAME_MAP.put(1, "lo0");
        DARWIN_10_8_IF_IFNAME_MAP.put(2, "gif0");
        DARWIN_10_8_IF_IFNAME_MAP.put(3, "stf0");
        DARWIN_10_8_IF_IFNAME_MAP.put(4, "en0");
        DARWIN_10_8_IF_IFNAME_MAP.put(5, "fw0");
        DARWIN_10_8_IF_IFNAME_MAP.put(6, "en1");
    
        DARWIN_10_8_IF_MAC_MAP.put(4, "0026b0ed8fb8");
        DARWIN_10_8_IF_MAC_MAP.put(5, "0026b0ed8fb8");
        DARWIN_10_8_IF_MAC_MAP.put(6, "002608f86155");
    }

        
    public OnmsNode getCiscoC870() {
        return getNode(CISCO_C870_NAME,CISCO_C870_SYSOID,CISCO_C870_IP,CISCO_C870_IP_IF_MAP,CISCO_C870_IF_IFNAME_MAP,CISCO_C870_IF_MAC_MAP,CISCO_C870_IF_IFDESCR_MAP,new HashMap<Integer, String>());
    }
    
    public OnmsNode getCiscoWsC2948() {
        return getNode(CISCO_WS_C2948_NAME,CISCO_WS_C2948_SYSOID,CISCO_WS_C2948_IP,CISCO_WS_C2948_IP_IF_MAP,CISCO_WS_C2948_IF_IFNAME_MAP,CISCO_WS_C2948_IF_MAC_MAP, new HashMap<Integer, String>(),new HashMap<Integer, String>());
    }
    
    public OnmsNode getNetGearSw108() {
        return getNode(NETGEAR_SW_108_NAME,NETGEAR_SW_108_SYSOID,NETGEAR_SW_108_IP,NETGEAR_SW_108_IP_IF_MAP,NETGEAR_SW_108_IF_IFNAME_MAP,NETGEAR_SW_108_IF_MAC_MAP,new HashMap<Integer, String>(),new HashMap<Integer, String>());
    }
    
    public OnmsNode getLinuxUbuntu() {
        return getNode(LINUX_UBUNTU_NAME, LINUX_UBUNTU_SYSOID, LINUX_UBUNTU_IP, LINUX_UBUNTU_IP_IF_MAP, LINUX_UBUNTU_IF_IFNAME_MAP, LINUX_UBUNTU_IF_MAC_MAP, new HashMap<Integer, String>(),new HashMap<Integer, String>());
    }
    
    public OnmsNode getDarwin108() {
        return getNode(DARWIN_10_8_NAME,DARWIN_10_8_SYSOID,DARWIN_10_8_IP,DARWIN_10_8_IP_IF_MAP,DARWIN_10_8_IF_IFNAME_MAP,DARWIN_10_8_IF_MAC_MAP, new HashMap<Integer, String>(),new HashMap<Integer, String>());
    }
    
}
