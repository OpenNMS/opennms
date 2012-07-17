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
import java.net.UnknownHostException;
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

public abstract class LinkdNms10205NetworkBuilder {

    NetworkBuilder m_networkBuilder;

    static final String MUMBAI_IP = "10.205.56.5";
    static final String MUMBAI_NAME = "mumbai";
    static final String MUMBAI_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.9";
   
    static final Map<InetAddress,Integer> MUMBAI_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> MUMBAI_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> MUMBAI_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> MUMBAI_IF_MAC_MAP = new HashMap<Integer, String>();

    static {
        try {
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("192.168.5.1"), 16);
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("10.205.56.5"), 508);
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("192.168.5.21"), 978);
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("192.168.5.5"), 520);
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("192.168.5.9"), 519);
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("192.168.5.17"), 977);
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("192.168.5.13"), 507);
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("128.0.0.1"), 18);
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("128.0.0.4"), 18);
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("10.0.0.4"), 18);
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("10.0.0.1"), 18);
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("15.15.0.5"), 518);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        MUMBAI_IF_IFNAME_MAP.put(514, "pc-0/1/0");
        MUMBAI_IF_IFDESCR_MAP.put(514, "pc-0/1/0");
        MUMBAI_IF_IFNAME_MAP.put(510, "ge-0/1/0");
        MUMBAI_IF_IFDESCR_MAP.put(510, "ge-0/1/0");
        MUMBAI_IF_MAC_MAP.put(510, "0019e270947f");
        MUMBAI_IF_IFNAME_MAP.put(19, "bcm0");
        MUMBAI_IF_IFDESCR_MAP.put(19, "bcm0");
        MUMBAI_IF_IFNAME_MAP.put(12, "mtun");
        MUMBAI_IF_IFDESCR_MAP.put(12, "mtun");
        MUMBAI_IF_IFNAME_MAP.put(516, "ge-1/0/0");
        MUMBAI_IF_IFDESCR_MAP.put(516, "ge-1/0/0");
        MUMBAI_IF_MAC_MAP.put(516, "0019e27095fe");
        MUMBAI_IF_IFNAME_MAP.put(6, "lo0");
        MUMBAI_IF_IFDESCR_MAP.put(6, "lo0");
        MUMBAI_IF_IFNAME_MAP.put(507, "ge-0/0/1.0");
        MUMBAI_IF_IFDESCR_MAP.put(507, "ge-0/0/1.0");
        MUMBAI_IF_MAC_MAP.put(507, "0019e2709401");
        MUMBAI_IF_IFNAME_MAP.put(11, "pimd");
        MUMBAI_IF_IFDESCR_MAP.put(11, "pimd");
        MUMBAI_IF_IFNAME_MAP.put(515, "pc-0/1/0.16383");
        MUMBAI_IF_IFDESCR_MAP.put(515, "pc-0/1/0.16383");
        MUMBAI_IF_IFNAME_MAP.put(508, "ge-0/0/3.0");
        MUMBAI_IF_IFDESCR_MAP.put(508, "ge-0/0/3.0");
        MUMBAI_IF_MAC_MAP.put(508, "0019e2709403");
        MUMBAI_IF_IFNAME_MAP.put(977, "ge-0/0/2.0");
        MUMBAI_IF_IFDESCR_MAP.put(977, "ge-0/0/2.0");
        MUMBAI_IF_MAC_MAP.put(977, "0019e2709402");
        MUMBAI_IF_IFNAME_MAP.put(506, "pc-0/0/0");
        MUMBAI_IF_IFDESCR_MAP.put(506, "pc-0/0/0");
        MUMBAI_IF_IFNAME_MAP.put(504, "ge-0/0/2");
        MUMBAI_IF_IFDESCR_MAP.put(504, "ge-0/0/2");
        MUMBAI_IF_MAC_MAP.put(504, "0019e2709402");
        MUMBAI_IF_IFNAME_MAP.put(522, "pfh-0/0/0.16383");
        MUMBAI_IF_IFDESCR_MAP.put(522, "pfh-0/0/0.16383");
        MUMBAI_IF_IFNAME_MAP.put(1, "fxp0");
        MUMBAI_IF_IFDESCR_MAP.put(1, "fxp0");
        MUMBAI_IF_MAC_MAP.put(1, "00a0a5623009");
        MUMBAI_IF_IFNAME_MAP.put(9, "ipip");
        MUMBAI_IF_IFDESCR_MAP.put(9, "ipip");
        MUMBAI_IF_IFNAME_MAP.put(7, "tap");
        MUMBAI_IF_IFDESCR_MAP.put(7, "tap");
        MUMBAI_IF_IFNAME_MAP.put(10, "pime");
        MUMBAI_IF_IFDESCR_MAP.put(10, "pime");
        MUMBAI_IF_IFNAME_MAP.put(517, "ge-1/1/0");
        MUMBAI_IF_IFDESCR_MAP.put(517, "ge-1/1/0");
        MUMBAI_IF_MAC_MAP.put(517, "0019e270967d");
        MUMBAI_IF_IFNAME_MAP.put(501, "pp0");
        MUMBAI_IF_IFDESCR_MAP.put(501, "pp0");
        MUMBAI_IF_IFNAME_MAP.put(978, "ge-0/1/1.0");
        MUMBAI_IF_IFDESCR_MAP.put(978, "ge-0/1/1.0");
        MUMBAI_IF_MAC_MAP.put(978, "0019e2709480");
        MUMBAI_IF_IFNAME_MAP.put(5, "dsc");
        MUMBAI_IF_IFDESCR_MAP.put(5, "dsc");
        MUMBAI_IF_IFNAME_MAP.put(8, "gre");
        MUMBAI_IF_IFDESCR_MAP.put(8, "gre");
        MUMBAI_IF_IFNAME_MAP.put(17, "em0");
        MUMBAI_IF_IFDESCR_MAP.put(17, "em0");
        MUMBAI_IF_MAC_MAP.put(17, "020001000004");
        MUMBAI_IF_IFNAME_MAP.put(512, "ge-0/1/2");
        MUMBAI_IF_IFDESCR_MAP.put(512, "ge-0/1/2");
        MUMBAI_IF_MAC_MAP.put(512, "0019e2709481");
        MUMBAI_IF_IFNAME_MAP.put(519, "ge-0/1/2.0");
        MUMBAI_IF_IFDESCR_MAP.put(519, "ge-0/1/2.0");
        MUMBAI_IF_MAC_MAP.put(519, "0019e2709481");
        MUMBAI_IF_IFNAME_MAP.put(509, "pc-0/0/0.16383");
        MUMBAI_IF_IFDESCR_MAP.put(509, "pc-0/0/0.16383");
        MUMBAI_IF_IFNAME_MAP.put(503, "ge-0/0/1");
        MUMBAI_IF_IFDESCR_MAP.put(503, "ge-0/0/1");
        MUMBAI_IF_MAC_MAP.put(503, "0019e2709401");
        MUMBAI_IF_IFNAME_MAP.put(4, "lsi");
        MUMBAI_IF_IFDESCR_MAP.put(4, "lsi");
        MUMBAI_IF_IFNAME_MAP.put(524, "pfh-1/0/0.16383");
        MUMBAI_IF_IFDESCR_MAP.put(524, "pfh-1/0/0.16383");
        MUMBAI_IF_IFNAME_MAP.put(20, "bcm0.0");
        MUMBAI_IF_IFDESCR_MAP.put(20, "bcm0.0");
        MUMBAI_IF_MAC_MAP.put(20, "020000000004");
        MUMBAI_IF_IFNAME_MAP.put(18, "em0.0");
        MUMBAI_IF_IFDESCR_MAP.put(18, "em0.0");
        MUMBAI_IF_MAC_MAP.put(18, "020001000004");
        MUMBAI_IF_IFNAME_MAP.put(518, "ge-0/1/0.0");
        MUMBAI_IF_IFDESCR_MAP.put(518, "ge-0/1/0.0");
        MUMBAI_IF_MAC_MAP.put(518, "0019e270947f");
        MUMBAI_IF_IFNAME_MAP.put(523, "pfh-1/0/0");
        MUMBAI_IF_IFDESCR_MAP.put(523, "pfh-1/0/0");
        MUMBAI_IF_IFNAME_MAP.put(22, "lo0.16385");
        MUMBAI_IF_IFDESCR_MAP.put(22, "lo0.16385");
        MUMBAI_IF_IFNAME_MAP.put(521, "pfh-0/0/0");
        MUMBAI_IF_IFDESCR_MAP.put(521, "pfh-0/0/0");
        MUMBAI_IF_IFNAME_MAP.put(513, "ge-0/1/3");
        MUMBAI_IF_IFDESCR_MAP.put(513, "ge-0/1/3");
        MUMBAI_IF_MAC_MAP.put(513, "0019e2709482");
        MUMBAI_IF_IFNAME_MAP.put(16, "lo0.0");
        MUMBAI_IF_IFDESCR_MAP.put(16, "lo0.0");
        MUMBAI_IF_IFNAME_MAP.put(511, "ge-0/1/1");
        MUMBAI_IF_IFDESCR_MAP.put(511, "ge-0/1/1");
        MUMBAI_IF_MAC_MAP.put(511, "0019e2709480");
        MUMBAI_IF_IFNAME_MAP.put(520, "ge-0/1/3.0");
        MUMBAI_IF_IFDESCR_MAP.put(520, "ge-0/1/3.0");
        MUMBAI_IF_MAC_MAP.put(520, "0019e2709482");
        MUMBAI_IF_IFNAME_MAP.put(505, "ge-0/0/3");
        MUMBAI_IF_IFDESCR_MAP.put(505, "ge-0/0/3");
        MUMBAI_IF_MAC_MAP.put(505, "0019e2709403");
        MUMBAI_IF_IFNAME_MAP.put(542, "ge-0/0/0");
        MUMBAI_IF_IFDESCR_MAP.put(542, "ge-0/0/0");
        MUMBAI_IF_MAC_MAP.put(542, "0019e2709400");        
    }

    static final String CHENNAI_IP = "10.205.56.6";
    static final String CHENNAI_NAME = "CHENNAI";
    static final String CHENNAI_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.25";
   
    static final Map<InetAddress,Integer> CHENNAI_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> CHENNAI_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> CHENNAI_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> CHENNAI_IF_MAC_MAP = new HashMap<Integer, String>();

    static {
        try {
            CHENNAI_IP_IF_MAP.put(InetAddress.getByName("192.168.5.6"), 530);
            CHENNAI_IP_IF_MAP.put(InetAddress.getByName("128.0.0.4"), 18);
            CHENNAI_IP_IF_MAP.put(InetAddress.getByName("192.168.6.1"), 16);
            CHENNAI_IP_IF_MAP.put(InetAddress.getByName("172.16.6.1"), 533);
            CHENNAI_IP_IF_MAP.put(InetAddress.getByName("192.168.1.1"), 532);
            CHENNAI_IP_IF_MAP.put(InetAddress.getByName("128.0.0.1"), 18);
            CHENNAI_IP_IF_MAP.put(InetAddress.getByName("10.205.56.6"), 529);
            CHENNAI_IP_IF_MAP.put(InetAddress.getByName("10.0.0.4"), 18);
            CHENNAI_IP_IF_MAP.put(InetAddress.getByName("192.168.1.18"), 528);
         } catch (UnknownHostException e) {
            e.printStackTrace();
        }
         CHENNAI_IF_IFNAME_MAP.put(22, "lo0.16385");
         CHENNAI_IF_IFDESCR_MAP.put(22, "lo0.16385");
         CHENNAI_IF_IFNAME_MAP.put(519, "ge-4/0/3");
         CHENNAI_IF_IFDESCR_MAP.put(519, "ge-4/0/3");
         CHENNAI_IF_MAC_MAP.put(519, "002283d6a52b");
         CHENNAI_IF_IFNAME_MAP.put(501, "cbp0");
         CHENNAI_IF_IFDESCR_MAP.put(501, "cbp0");
         CHENNAI_IF_MAC_MAP.put(501, "002283d6a011");
         CHENNAI_IF_IFNAME_MAP.put(567, "ge-4/3/8");
         CHENNAI_IF_IFDESCR_MAP.put(567, "ge-4/3/8");
         CHENNAI_IF_MAC_MAP.put(567, "002283d6a626");
         CHENNAI_IF_IFNAME_MAP.put(526, "lc-4/0/0");
         CHENNAI_IF_IFDESCR_MAP.put(526, "lc-4/0/0");
         CHENNAI_IF_IFNAME_MAP.put(17, "em0");
         CHENNAI_IF_IFDESCR_MAP.put(17, "em0");
         CHENNAI_IF_MAC_MAP.put(17, "020000000004");
         CHENNAI_IF_IFNAME_MAP.put(563, "ge-4/3/4");
         CHENNAI_IF_IFDESCR_MAP.put(563, "ge-4/3/4");
         CHENNAI_IF_MAC_MAP.put(563, "002283d6a622");
         CHENNAI_IF_IFNAME_MAP.put(527, "lc-4/0/0.32769");
         CHENNAI_IF_IFDESCR_MAP.put(527, "lc-4/0/0.32769");
         CHENNAI_IF_IFNAME_MAP.put(6, "lo0");
         CHENNAI_IF_IFDESCR_MAP.put(6, "lo0");
         CHENNAI_IF_IFNAME_MAP.put(520, "ge-4/0/4");
         CHENNAI_IF_IFDESCR_MAP.put(520, "ge-4/0/4");
         CHENNAI_IF_MAC_MAP.put(520, "002283d6a52c");
         CHENNAI_IF_IFNAME_MAP.put(533, "ge-4/0/4.0");
         CHENNAI_IF_IFDESCR_MAP.put(533, "ge-4/0/4.0");
         CHENNAI_IF_MAC_MAP.put(533, "002283d6a52c");
         CHENNAI_IF_IFNAME_MAP.put(570, "lc-4/3/0.32769");
         CHENNAI_IF_IFDESCR_MAP.put(570, "lc-4/3/0.32769");
         CHENNAI_IF_IFNAME_MAP.put(538, "ge-4/1/4");
         CHENNAI_IF_IFDESCR_MAP.put(538, "ge-4/1/4");
         CHENNAI_IF_MAC_MAP.put(538, "002283d6a57e");
         CHENNAI_IF_IFNAME_MAP.put(523, "ge-4/0/7");
         CHENNAI_IF_IFDESCR_MAP.put(523, "ge-4/0/7");
         CHENNAI_IF_MAC_MAP.put(523, "002283d6a52f");
         CHENNAI_IF_IFNAME_MAP.put(565, "ge-4/3/6");
         CHENNAI_IF_IFDESCR_MAP.put(565, "ge-4/3/6");
         CHENNAI_IF_MAC_MAP.put(565, "002283d6a624");
         CHENNAI_IF_IFNAME_MAP.put(518, "ge-4/0/2");
         CHENNAI_IF_IFDESCR_MAP.put(518, "ge-4/0/2");
         CHENNAI_IF_MAC_MAP.put(518, "002283d6a52a");
         CHENNAI_IF_IFNAME_MAP.put(516, "ge-4/0/0");
         CHENNAI_IF_IFDESCR_MAP.put(516, "ge-4/0/0");
         CHENNAI_IF_MAC_MAP.put(516, "002283d6a528");
         CHENNAI_IF_IFNAME_MAP.put(547, "ge-4/2/0");
         CHENNAI_IF_IFDESCR_MAP.put(547, "ge-4/2/0");
         CHENNAI_IF_MAC_MAP.put(547, "002283d6a5cc");
         CHENNAI_IF_IFNAME_MAP.put(506, "pfh-4/0/0");
         CHENNAI_IF_IFDESCR_MAP.put(506, "pfh-4/0/0");
         CHENNAI_IF_IFNAME_MAP.put(512, "pfe-4/0/0.16383");
         CHENNAI_IF_IFDESCR_MAP.put(512, "pfe-4/0/0.16383");
         CHENNAI_IF_IFNAME_MAP.put(558, "lc-4/2/0.32769");
         CHENNAI_IF_IFDESCR_MAP.put(558, "lc-4/2/0.32769");
         CHENNAI_IF_IFNAME_MAP.put(503, "irb");
         CHENNAI_IF_IFDESCR_MAP.put(503, "irb");
         CHENNAI_IF_MAC_MAP.put(503, "002283d6a7f0");
         CHENNAI_IF_IFNAME_MAP.put(5, "dsc");
         CHENNAI_IF_IFDESCR_MAP.put(5, "dsc");
         CHENNAI_IF_IFNAME_MAP.put(548, "ge-4/2/1");
         CHENNAI_IF_IFDESCR_MAP.put(548, "ge-4/2/1");
         CHENNAI_IF_MAC_MAP.put(548, "002283d6a5cd");
         CHENNAI_IF_IFNAME_MAP.put(551, "ge-4/2/4");
         CHENNAI_IF_IFDESCR_MAP.put(551, "ge-4/2/4");
         CHENNAI_IF_MAC_MAP.put(551, "002283d6a5d0");
         CHENNAI_IF_IFNAME_MAP.put(504, "pip0");
         CHENNAI_IF_IFDESCR_MAP.put(504, "pip0");
         CHENNAI_IF_MAC_MAP.put(504, "002283d6a7b0");
         CHENNAI_IF_IFNAME_MAP.put(540, "ge-4/1/6");
         CHENNAI_IF_IFDESCR_MAP.put(540, "ge-4/1/6");
         CHENNAI_IF_MAC_MAP.put(540, "002283d6a580");
         CHENNAI_IF_IFNAME_MAP.put(534, "ge-4/1/0");
         CHENNAI_IF_IFDESCR_MAP.put(534, "ge-4/1/0");
         CHENNAI_IF_MAC_MAP.put(534, "002283d6a57a");
         CHENNAI_IF_IFNAME_MAP.put(543, "ge-4/1/6.0");
         CHENNAI_IF_IFDESCR_MAP.put(543, "ge-4/1/6.0");
         CHENNAI_IF_MAC_MAP.put(543, "002283d6a580");
         CHENNAI_IF_IFNAME_MAP.put(524, "ge-4/0/8");
         CHENNAI_IF_IFDESCR_MAP.put(524, "ge-4/0/8");
         CHENNAI_IF_MAC_MAP.put(524, "002283d6a530");
         CHENNAI_IF_IFNAME_MAP.put(505, "pp0");
         CHENNAI_IF_IFDESCR_MAP.put(505, "pp0");
         CHENNAI_IF_IFNAME_MAP.put(561, "ge-4/3/2");
         CHENNAI_IF_IFDESCR_MAP.put(561, "ge-4/3/2");
         CHENNAI_IF_MAC_MAP.put(561, "002283d6a620");
         CHENNAI_IF_IFNAME_MAP.put(564, "ge-4/3/5");
         CHENNAI_IF_IFDESCR_MAP.put(564, "ge-4/3/5");
         CHENNAI_IF_MAC_MAP.put(564, "002283d6a623");
         CHENNAI_IF_IFNAME_MAP.put(562, "ge-4/3/3");
         CHENNAI_IF_IFDESCR_MAP.put(562, "ge-4/3/3");
         CHENNAI_IF_MAC_MAP.put(562, "002283d6a621");
         CHENNAI_IF_IFNAME_MAP.put(557, "lc-4/2/0");
         CHENNAI_IF_IFDESCR_MAP.put(557, "lc-4/2/0");
         CHENNAI_IF_IFNAME_MAP.put(7, "tap");
         CHENNAI_IF_IFDESCR_MAP.put(7, "tap");
         CHENNAI_IF_IFNAME_MAP.put(23, "em1");
         CHENNAI_IF_IFDESCR_MAP.put(23, "em1");
         CHENNAI_IF_MAC_MAP.put(23, "020001000004");
         CHENNAI_IF_IFNAME_MAP.put(553, "ge-4/2/6");
         CHENNAI_IF_IFDESCR_MAP.put(553, "ge-4/2/6");
         CHENNAI_IF_MAC_MAP.put(553, "002283d6a5d2");
         CHENNAI_IF_IFNAME_MAP.put(502, "demux0");
         CHENNAI_IF_IFDESCR_MAP.put(502, "demux0");
         CHENNAI_IF_IFNAME_MAP.put(9, "ipip");
         CHENNAI_IF_IFDESCR_MAP.put(9, "ipip");
         CHENNAI_IF_IFNAME_MAP.put(515, "pfe-4/3/0.16383");
         CHENNAI_IF_IFDESCR_MAP.put(515, "pfe-4/3/0.16383");
         CHENNAI_IF_IFNAME_MAP.put(10, "pime");
         CHENNAI_IF_IFDESCR_MAP.put(10, "pime");
         CHENNAI_IF_IFNAME_MAP.put(555, "ge-4/2/8");
         CHENNAI_IF_IFDESCR_MAP.put(555, "ge-4/2/8");
         CHENNAI_IF_MAC_MAP.put(555, "002283d6a5d4");
         CHENNAI_IF_IFNAME_MAP.put(556, "ge-4/2/9");
         CHENNAI_IF_IFDESCR_MAP.put(556, "ge-4/2/9");
         CHENNAI_IF_MAC_MAP.put(556, "002283d6a5d5");
         CHENNAI_IF_IFNAME_MAP.put(569, "lc-4/3/0");
         CHENNAI_IF_IFDESCR_MAP.put(569, "lc-4/3/0");
         CHENNAI_IF_IFNAME_MAP.put(545, "lc-4/1/0");
         CHENNAI_IF_IFDESCR_MAP.put(545, "lc-4/1/0");
         CHENNAI_IF_IFNAME_MAP.put(536, "ge-4/1/2");
         CHENNAI_IF_IFDESCR_MAP.put(536, "ge-4/1/2");
         CHENNAI_IF_MAC_MAP.put(536, "002283d6a57c");
         CHENNAI_IF_IFNAME_MAP.put(535, "ge-4/1/1");
         CHENNAI_IF_IFDESCR_MAP.put(535, "ge-4/1/1");
         CHENNAI_IF_MAC_MAP.put(535, "002283d6a57b");
         CHENNAI_IF_IFNAME_MAP.put(532, "ge-4/0/3.0");
         CHENNAI_IF_IFDESCR_MAP.put(532, "ge-4/0/3.0");
         CHENNAI_IF_MAC_MAP.put(532, "002283d6a52b");
         CHENNAI_IF_IFNAME_MAP.put(508, "pfe-4/1/0");
         CHENNAI_IF_IFDESCR_MAP.put(508, "pfe-4/1/0");
         CHENNAI_IF_IFNAME_MAP.put(507, "pfe-4/0/0");
         CHENNAI_IF_IFDESCR_MAP.put(507, "pfe-4/0/0");
         CHENNAI_IF_IFNAME_MAP.put(552, "ge-4/2/5");
         CHENNAI_IF_IFDESCR_MAP.put(552, "ge-4/2/5");
         CHENNAI_IF_MAC_MAP.put(552, "002283d6a5d1");
         CHENNAI_IF_IFNAME_MAP.put(568, "ge-4/3/9");
         CHENNAI_IF_IFDESCR_MAP.put(568, "ge-4/3/9");
         CHENNAI_IF_MAC_MAP.put(568, "002283d6a627");
         CHENNAI_IF_IFNAME_MAP.put(530, "ge-4/0/2.0");
         CHENNAI_IF_IFDESCR_MAP.put(530, "ge-4/0/2.0");
         CHENNAI_IF_MAC_MAP.put(530, "002283d6a52a");
         CHENNAI_IF_IFNAME_MAP.put(528, "ge-4/0/0.0");
         CHENNAI_IF_IFDESCR_MAP.put(528, "ge-4/0/0.0");
         CHENNAI_IF_MAC_MAP.put(528, "002283d6a528");
         CHENNAI_IF_IFNAME_MAP.put(517, "ge-4/0/1");
         CHENNAI_IF_IFDESCR_MAP.put(517, "ge-4/0/1");
         CHENNAI_IF_MAC_MAP.put(517, "002283d6a529");
         CHENNAI_IF_IFNAME_MAP.put(514, "pfe-4/2/0.16383");
         CHENNAI_IF_IFDESCR_MAP.put(514, "pfe-4/2/0.16383");
         CHENNAI_IF_IFNAME_MAP.put(554, "ge-4/2/7");
         CHENNAI_IF_IFDESCR_MAP.put(554, "ge-4/2/7");
         CHENNAI_IF_MAC_MAP.put(554, "002283d6a5d3");
         CHENNAI_IF_IFNAME_MAP.put(539, "ge-4/1/5");
         CHENNAI_IF_IFDESCR_MAP.put(539, "ge-4/1/5");
         CHENNAI_IF_MAC_MAP.put(539, "002283d6a57f");
         CHENNAI_IF_IFNAME_MAP.put(560, "ge-4/3/1");
         CHENNAI_IF_IFDESCR_MAP.put(560, "ge-4/3/1");
         CHENNAI_IF_MAC_MAP.put(560, "002283d6a61f");
         CHENNAI_IF_IFNAME_MAP.put(16, "lo0.0");
         CHENNAI_IF_IFDESCR_MAP.put(16, "lo0.0");
         CHENNAI_IF_IFNAME_MAP.put(531, "ge-4/0/2.32767");
         CHENNAI_IF_IFDESCR_MAP.put(531, "ge-4/0/2.32767");
         CHENNAI_IF_MAC_MAP.put(531, "002283d6a52a");
         CHENNAI_IF_IFNAME_MAP.put(18, "em0.0");
         CHENNAI_IF_IFDESCR_MAP.put(18, "em0.0");
         CHENNAI_IF_MAC_MAP.put(18, "020000000004");
         CHENNAI_IF_IFNAME_MAP.put(8, "gre");
         CHENNAI_IF_IFDESCR_MAP.put(8, "gre");
         CHENNAI_IF_IFNAME_MAP.put(529, "ge-4/0/1.0");
         CHENNAI_IF_IFDESCR_MAP.put(529, "ge-4/0/1.0");
         CHENNAI_IF_MAC_MAP.put(529, "002283d6a529");
         CHENNAI_IF_IFNAME_MAP.put(24, "em1.0");
         CHENNAI_IF_IFDESCR_MAP.put(24, "em1.0");
         CHENNAI_IF_MAC_MAP.put(24, "020001000004");
         CHENNAI_IF_IFNAME_MAP.put(559, "ge-4/3/0");
         CHENNAI_IF_IFDESCR_MAP.put(559, "ge-4/3/0");
         CHENNAI_IF_MAC_MAP.put(559, "002283d6a61e");
         CHENNAI_IF_IFNAME_MAP.put(510, "pfh-4/0/0.16383");
         CHENNAI_IF_IFDESCR_MAP.put(510, "pfh-4/0/0.16383");
         CHENNAI_IF_IFNAME_MAP.put(511, "pfe-4/3/0");
         CHENNAI_IF_IFDESCR_MAP.put(511, "pfe-4/3/0");
         CHENNAI_IF_IFNAME_MAP.put(522, "ge-4/0/6");
         CHENNAI_IF_IFDESCR_MAP.put(522, "ge-4/0/6");
         CHENNAI_IF_MAC_MAP.put(522, "002283d6a52e");
         CHENNAI_IF_IFNAME_MAP.put(11, "pimd");
         CHENNAI_IF_IFDESCR_MAP.put(11, "pimd");
         CHENNAI_IF_IFNAME_MAP.put(12, "mtun");
         CHENNAI_IF_IFDESCR_MAP.put(12, "mtun");
         CHENNAI_IF_IFNAME_MAP.put(546, "lc-4/1/0.32769");
         CHENNAI_IF_IFDESCR_MAP.put(546, "lc-4/1/0.32769");
         CHENNAI_IF_IFNAME_MAP.put(541, "ge-4/1/7");
         CHENNAI_IF_IFDESCR_MAP.put(541, "ge-4/1/7");
         CHENNAI_IF_MAC_MAP.put(541, "002283d6a581");
         CHENNAI_IF_IFNAME_MAP.put(509, "pfe-4/2/0");
         CHENNAI_IF_IFDESCR_MAP.put(509, "pfe-4/2/0");
         CHENNAI_IF_IFNAME_MAP.put(513, "pfe-4/1/0.16383");
         CHENNAI_IF_IFDESCR_MAP.put(513, "pfe-4/1/0.16383");
         CHENNAI_IF_IFNAME_MAP.put(537, "ge-4/1/3");
         CHENNAI_IF_IFDESCR_MAP.put(537, "ge-4/1/3");
         CHENNAI_IF_MAC_MAP.put(537, "002283d6a57d");
         CHENNAI_IF_IFNAME_MAP.put(521, "ge-4/0/5");
         CHENNAI_IF_IFDESCR_MAP.put(521, "ge-4/0/5");
         CHENNAI_IF_MAC_MAP.put(521, "002283d6a52d");
         CHENNAI_IF_IFNAME_MAP.put(544, "ge-4/1/9");
         CHENNAI_IF_IFDESCR_MAP.put(544, "ge-4/1/9");
         CHENNAI_IF_MAC_MAP.put(544, "002283d6a583");
         CHENNAI_IF_IFNAME_MAP.put(566, "ge-4/3/7");
         CHENNAI_IF_IFDESCR_MAP.put(566, "ge-4/3/7");
         CHENNAI_IF_MAC_MAP.put(566, "002283d6a625");
         CHENNAI_IF_IFNAME_MAP.put(525, "ge-4/0/9");
         CHENNAI_IF_IFDESCR_MAP.put(525, "ge-4/0/9");
         CHENNAI_IF_MAC_MAP.put(525, "002283d6a531");
         CHENNAI_IF_IFNAME_MAP.put(1, "fxp0");
         CHENNAI_IF_IFDESCR_MAP.put(1, "fxp0");
         CHENNAI_IF_MAC_MAP.put(1, "00a0a5628423");
         CHENNAI_IF_IFNAME_MAP.put(549, "ge-4/2/2");
         CHENNAI_IF_IFDESCR_MAP.put(549, "ge-4/2/2");
         CHENNAI_IF_MAC_MAP.put(549, "002283d6a5ce");
         CHENNAI_IF_IFNAME_MAP.put(542, "ge-4/1/8");
         CHENNAI_IF_IFDESCR_MAP.put(542, "ge-4/1/8");
         CHENNAI_IF_MAC_MAP.put(542, "002283d6a582");
         CHENNAI_IF_IFNAME_MAP.put(550, "ge-4/2/3");
         CHENNAI_IF_IFDESCR_MAP.put(550, "ge-4/2/3");
         CHENNAI_IF_MAC_MAP.put(550, "002283d6a5cf");
         CHENNAI_IF_IFNAME_MAP.put(4, "lsi");
         CHENNAI_IF_IFDESCR_MAP.put(4, "lsi");
    }


    private NetworkBuilder getNetworkBuilder() {
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
    
    String getMockSnmpFile(String ipaddress) {
        return "classpath:linkd/nms10205/"+ipaddress+"-walk.txt"; 
    }
    
    OnmsNode getMumbai() {
        return getNode(MUMBAI_NAME,MUMBAI_SYSOID,MUMBAI_IP,MUMBAI_IP_IF_MAP,MUMBAI_IF_IFNAME_MAP,MUMBAI_IF_MAC_MAP,MUMBAI_IF_IFDESCR_MAP);
    }    

    OnmsNode getChennai() {
        return getNode(CHENNAI_NAME,CHENNAI_SYSOID,CHENNAI_IP,CHENNAI_IP_IF_MAP,CHENNAI_IF_IFNAME_MAP,CHENNAI_IF_MAC_MAP,CHENNAI_IF_IFDESCR_MAP);
    }    

    OnmsNode getNodeWithoutSnmp(String name, String ipaddr) {
        NetworkBuilder nb = getNetworkBuilder();
        nb.addNode(name).setForeignSource("linkd").setForeignId(name).setType("A");
        nb.addInterface(ipaddr).setIsSnmpPrimary("N").setIsManaged("M");
        return nb.getCurrentNode();
    }
    
}
