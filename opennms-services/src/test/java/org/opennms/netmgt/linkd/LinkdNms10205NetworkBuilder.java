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

import org.opennms.netmgt.model.OnmsNode;

/**
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:antonio@opennme.it">Antonio Russo</a>
 * @author <a href="mailto:alejandro@opennms.org">Alejandro Galue</a>
 */

public abstract class LinkdNms10205NetworkBuilder extends LinkdNetworkBuilder {

    static final String MUMBAI_IP = "10.205.56.5";
    static final String MUMBAI_NAME = "Mumbai";
    static final String MUMBAI_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.9";
   
    static final Map<InetAddress,Integer> MUMBAI_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> MUMBAI_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> MUMBAI_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> MUMBAI_IF_MAC_MAP = new HashMap<Integer, String>();

    static {
        try {
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("128.0.0.4"), 18);
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("10.0.0.1"), 18);
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("128.0.0.1"), 18);
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("192.168.5.5"), 520);
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("10.0.0.4"), 18);
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("192.168.5.1"), 16);
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("10.205.56.5"), 508);
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("192.168.5.13"), 507);
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("192.168.5.17"), 977);
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("192.168.5.9"), 519);
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("192.168.5.21"), 978);
            MUMBAI_IP_IF_MAP.put(InetAddress.getByName("15.15.0.5"), 518);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        MUMBAI_IF_IFNAME_MAP.put(7, "tap");
        MUMBAI_IF_IFDESCR_MAP.put(7, "tap");
        MUMBAI_IF_IFNAME_MAP.put(507, "ge-0/0/1.0");
        MUMBAI_IF_IFDESCR_MAP.put(507, "ge-0/0/1.0");
        MUMBAI_IF_MAC_MAP.put(507, "0019e2709401");
        MUMBAI_IF_IFNAME_MAP.put(519, "ge-0/1/2.0");
        MUMBAI_IF_IFDESCR_MAP.put(519, "ge-0/1/2.0");
        MUMBAI_IF_MAC_MAP.put(519, "0019e2709481");
        MUMBAI_IF_IFNAME_MAP.put(979, "ge-0/1/3.32767");
        MUMBAI_IF_IFDESCR_MAP.put(979, "ge-0/1/3.32767");
        MUMBAI_IF_MAC_MAP.put(979, "0019e2709482");
        MUMBAI_IF_IFNAME_MAP.put(19, "bcm0");
        MUMBAI_IF_IFDESCR_MAP.put(19, "bcm0");
        MUMBAI_IF_MAC_MAP.put(19, "020000000004");
        MUMBAI_IF_IFNAME_MAP.put(506, "pc-0/0/0");
        MUMBAI_IF_IFDESCR_MAP.put(506, "pc-0/0/0");
        MUMBAI_IF_IFNAME_MAP.put(524, "pfh-1/0/0.16383");
        MUMBAI_IF_IFDESCR_MAP.put(524, "pfh-1/0/0.16383");
        MUMBAI_IF_IFNAME_MAP.put(512, "ge-0/1/2");
        MUMBAI_IF_IFDESCR_MAP.put(512, "ge-0/1/2");
        MUMBAI_IF_MAC_MAP.put(512, "0019e2709481");
        MUMBAI_IF_IFNAME_MAP.put(522, "pfh-0/0/0.16383");
        MUMBAI_IF_IFDESCR_MAP.put(522, "pfh-0/0/0.16383");
        MUMBAI_IF_IFNAME_MAP.put(514, "pc-0/1/0");
        MUMBAI_IF_IFDESCR_MAP.put(514, "pc-0/1/0");
        MUMBAI_IF_IFNAME_MAP.put(516, "ge-1/0/0");
        MUMBAI_IF_IFDESCR_MAP.put(516, "ge-1/0/0");
        MUMBAI_IF_MAC_MAP.put(516, "0019e27095fe");
        MUMBAI_IF_IFNAME_MAP.put(10, "pime");
        MUMBAI_IF_IFDESCR_MAP.put(10, "pime");
        MUMBAI_IF_IFNAME_MAP.put(16, "lo0.0");
        MUMBAI_IF_IFDESCR_MAP.put(16, "lo0.0");
        MUMBAI_IF_IFNAME_MAP.put(4, "lsi");
        MUMBAI_IF_IFDESCR_MAP.put(4, "lsi");
        MUMBAI_IF_IFNAME_MAP.put(12, "mtun");
        MUMBAI_IF_IFDESCR_MAP.put(12, "mtun");
        MUMBAI_IF_IFNAME_MAP.put(11, "pimd");
        MUMBAI_IF_IFDESCR_MAP.put(11, "pimd");
        MUMBAI_IF_IFNAME_MAP.put(977, "ge-0/0/2.0");
        MUMBAI_IF_IFDESCR_MAP.put(977, "ge-0/0/2.0");
        MUMBAI_IF_MAC_MAP.put(977, "0019e2709402");
        MUMBAI_IF_IFNAME_MAP.put(518, "ge-0/1/0.0");
        MUMBAI_IF_IFDESCR_MAP.put(518, "ge-0/1/0.0");
        MUMBAI_IF_MAC_MAP.put(518, "0019e270947f");
        MUMBAI_IF_IFNAME_MAP.put(17, "em0");
        MUMBAI_IF_IFDESCR_MAP.put(17, "em0");
        MUMBAI_IF_MAC_MAP.put(17, "020001000004");
        MUMBAI_IF_IFNAME_MAP.put(513, "ge-0/1/3");
        MUMBAI_IF_IFDESCR_MAP.put(513, "ge-0/1/3");
        MUMBAI_IF_MAC_MAP.put(513, "0019e2709482");
        MUMBAI_IF_IFNAME_MAP.put(515, "pc-0/1/0.16383");
        MUMBAI_IF_IFDESCR_MAP.put(515, "pc-0/1/0.16383");
        MUMBAI_IF_IFNAME_MAP.put(1, "fxp0");
        MUMBAI_IF_IFDESCR_MAP.put(1, "fxp0");
        MUMBAI_IF_MAC_MAP.put(1, "00a0a5623009");
        MUMBAI_IF_IFNAME_MAP.put(521, "pfh-0/0/0");
        MUMBAI_IF_IFDESCR_MAP.put(521, "pfh-0/0/0");
        MUMBAI_IF_IFNAME_MAP.put(523, "pfh-1/0/0");
        MUMBAI_IF_IFDESCR_MAP.put(523, "pfh-1/0/0");
        MUMBAI_IF_IFNAME_MAP.put(511, "ge-0/1/1");
        MUMBAI_IF_IFDESCR_MAP.put(511, "ge-0/1/1");
        MUMBAI_IF_MAC_MAP.put(511, "0019e2709480");
        MUMBAI_IF_IFNAME_MAP.put(520, "ge-0/1/3.0");
        MUMBAI_IF_IFDESCR_MAP.put(520, "ge-0/1/3.0");
        MUMBAI_IF_MAC_MAP.put(520, "0019e2709482");
        MUMBAI_IF_IFNAME_MAP.put(509, "pc-0/0/0.16383");
        MUMBAI_IF_IFDESCR_MAP.put(509, "pc-0/0/0.16383");
        MUMBAI_IF_IFNAME_MAP.put(5, "dsc");
        MUMBAI_IF_IFDESCR_MAP.put(5, "dsc");
        MUMBAI_IF_IFNAME_MAP.put(517, "ge-1/1/0");
        MUMBAI_IF_IFDESCR_MAP.put(517, "ge-1/1/0");
        MUMBAI_IF_MAC_MAP.put(517, "0019e270967d");
        MUMBAI_IF_IFNAME_MAP.put(542, "ge-0/0/0");
        MUMBAI_IF_IFDESCR_MAP.put(542, "ge-0/0/0");
        MUMBAI_IF_MAC_MAP.put(542, "0019e2709400");
        MUMBAI_IF_IFNAME_MAP.put(504, "ge-0/0/2");
        MUMBAI_IF_IFDESCR_MAP.put(504, "ge-0/0/2");
        MUMBAI_IF_MAC_MAP.put(504, "0019e2709402");
        MUMBAI_IF_IFNAME_MAP.put(6, "lo0");
        MUMBAI_IF_IFDESCR_MAP.put(6, "lo0");
        MUMBAI_IF_IFNAME_MAP.put(18, "em0.0");
        MUMBAI_IF_IFDESCR_MAP.put(18, "em0.0");
        MUMBAI_IF_MAC_MAP.put(18, "020001000004");
        MUMBAI_IF_IFNAME_MAP.put(510, "ge-0/1/0");
        MUMBAI_IF_IFDESCR_MAP.put(510, "ge-0/1/0");
        MUMBAI_IF_MAC_MAP.put(510, "0019e270947f");
        MUMBAI_IF_IFNAME_MAP.put(501, "pp0");
        MUMBAI_IF_IFDESCR_MAP.put(501, "pp0");
        MUMBAI_IF_IFNAME_MAP.put(8, "gre");
        MUMBAI_IF_IFDESCR_MAP.put(8, "gre");
        MUMBAI_IF_IFNAME_MAP.put(22, "lo0.16385");
        MUMBAI_IF_IFDESCR_MAP.put(22, "lo0.16385");
        MUMBAI_IF_IFNAME_MAP.put(508, "ge-0/0/3.0");
        MUMBAI_IF_IFDESCR_MAP.put(508, "ge-0/0/3.0");
        MUMBAI_IF_MAC_MAP.put(508, "0019e2709403");
        MUMBAI_IF_IFNAME_MAP.put(9, "ipip");
        MUMBAI_IF_IFDESCR_MAP.put(9, "ipip");
        MUMBAI_IF_IFNAME_MAP.put(505, "ge-0/0/3");
        MUMBAI_IF_IFDESCR_MAP.put(505, "ge-0/0/3");
        MUMBAI_IF_MAC_MAP.put(505, "0019e2709403");
        MUMBAI_IF_IFNAME_MAP.put(503, "ge-0/0/1");
        MUMBAI_IF_IFDESCR_MAP.put(503, "ge-0/0/1");
        MUMBAI_IF_MAC_MAP.put(503, "0019e2709401");
        MUMBAI_IF_IFNAME_MAP.put(20, "bcm0.0");
        MUMBAI_IF_IFDESCR_MAP.put(20, "bcm0.0");
        MUMBAI_IF_MAC_MAP.put(20, "020000000004");
        MUMBAI_IF_IFNAME_MAP.put(978, "ge-0/1/1.0");
        MUMBAI_IF_IFDESCR_MAP.put(978, "ge-0/1/1.0");
        MUMBAI_IF_MAC_MAP.put(978, "0019e2709480");
    }

    static final String CHENNAI_IP = "10.205.56.6";
    static final String CHENNAI_NAME = "Chennai";
    static final String CHENNAI_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.25";
   
    static final Map<InetAddress,Integer> CHENNAI_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> CHENNAI_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> CHENNAI_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> CHENNAI_IF_MAC_MAP = new HashMap<Integer, String>();

    static {
        try {
            CHENNAI_IP_IF_MAP.put(InetAddress.getByName("10.0.0.4"), 18);
            CHENNAI_IP_IF_MAP.put(InetAddress.getByName("192.168.5.6"), 528);
            CHENNAI_IP_IF_MAP.put(InetAddress.getByName("172.16.6.1"), 533);
            CHENNAI_IP_IF_MAP.put(InetAddress.getByName("192.168.1.1"), 532);
            CHENNAI_IP_IF_MAP.put(InetAddress.getByName("192.168.6.1"), 16);
            CHENNAI_IP_IF_MAP.put(InetAddress.getByName("10.205.56.6"), 523);
            CHENNAI_IP_IF_MAP.put(InetAddress.getByName("128.0.0.4"), 18);
            CHENNAI_IP_IF_MAP.put(InetAddress.getByName("192.168.1.18"), 517);
            CHENNAI_IP_IF_MAP.put(InetAddress.getByName("128.0.0.1"), 18);
         } catch (UnknownHostException e) {
            e.printStackTrace();
        }
         CHENNAI_IF_IFNAME_MAP.put(515, "pfe-4/3/0.16383");
         CHENNAI_IF_IFDESCR_MAP.put(515, "pfe-4/3/0.16383");
         CHENNAI_IF_IFNAME_MAP.put(517, "ge-4/0/0.0");
         CHENNAI_IF_IFDESCR_MAP.put(517, "ge-4/0/0.0");
         CHENNAI_IF_MAC_MAP.put(517, "002283d6a528");
         CHENNAI_IF_IFNAME_MAP.put(562, "ge-4/3/3");
         CHENNAI_IF_IFDESCR_MAP.put(562, "ge-4/3/3");
         CHENNAI_IF_MAC_MAP.put(562, "002283d6a621");
         CHENNAI_IF_IFNAME_MAP.put(507, "pfe-4/0/0");
         CHENNAI_IF_IFDESCR_MAP.put(507, "pfe-4/0/0");
         CHENNAI_IF_IFNAME_MAP.put(24, "em1.0");
         CHENNAI_IF_IFDESCR_MAP.put(24, "em1.0");
         CHENNAI_IF_MAC_MAP.put(24, "020001000004");
         CHENNAI_IF_IFNAME_MAP.put(555, "lc-4/2/0");
         CHENNAI_IF_IFDESCR_MAP.put(555, "lc-4/2/0");
         CHENNAI_IF_IFNAME_MAP.put(523, "ge-4/0/1.0");
         CHENNAI_IF_IFDESCR_MAP.put(523, "ge-4/0/1.0");
         CHENNAI_IF_MAC_MAP.put(523, "002283d6a529");
         CHENNAI_IF_IFNAME_MAP.put(556, "lc-4/2/0.32769");
         CHENNAI_IF_IFDESCR_MAP.put(556, "lc-4/2/0.32769");
         CHENNAI_IF_IFNAME_MAP.put(504, "pip0");
         CHENNAI_IF_IFDESCR_MAP.put(504, "pip0");
         CHENNAI_IF_MAC_MAP.put(504, "002283d6a7b0");
         CHENNAI_IF_IFNAME_MAP.put(558, "ge-4/2/9");
         CHENNAI_IF_IFDESCR_MAP.put(558, "ge-4/2/9");
         CHENNAI_IF_MAC_MAP.put(558, "002283d6a5d5");
         CHENNAI_IF_IFNAME_MAP.put(539, "ge-4/1/5");
         CHENNAI_IF_IFDESCR_MAP.put(539, "ge-4/1/5");
         CHENNAI_IF_MAC_MAP.put(539, "002283d6a57f");
         CHENNAI_IF_IFNAME_MAP.put(512, "pfh-4/0/0.16383");
         CHENNAI_IF_IFDESCR_MAP.put(512, "pfh-4/0/0.16383");
         CHENNAI_IF_IFNAME_MAP.put(533, "ge-4/0/4.0");
         CHENNAI_IF_IFDESCR_MAP.put(533, "ge-4/0/4.0");
         CHENNAI_IF_MAC_MAP.put(533, "002283d6a52c");
         CHENNAI_IF_IFNAME_MAP.put(563, "ge-4/3/4");
         CHENNAI_IF_IFDESCR_MAP.put(563, "ge-4/3/4");
         CHENNAI_IF_MAC_MAP.put(563, "002283d6a622");
         CHENNAI_IF_IFNAME_MAP.put(1, "fxp0");
         CHENNAI_IF_IFDESCR_MAP.put(1, "fxp0");
         CHENNAI_IF_MAC_MAP.put(1, "00a0a5628423");
         CHENNAI_IF_IFNAME_MAP.put(536, "ge-4/1/2");
         CHENNAI_IF_IFDESCR_MAP.put(536, "ge-4/1/2");
         CHENNAI_IF_MAC_MAP.put(536, "002283d6a57c");
         CHENNAI_IF_IFNAME_MAP.put(18, "em0.0");
         CHENNAI_IF_IFDESCR_MAP.put(18, "em0.0");
         CHENNAI_IF_MAC_MAP.put(18, "020000000004");
         CHENNAI_IF_IFNAME_MAP.put(531, "ge-4/0/2.32767");
         CHENNAI_IF_IFDESCR_MAP.put(531, "ge-4/0/2.32767");
         CHENNAI_IF_MAC_MAP.put(531, "002283d6a52a");
         CHENNAI_IF_IFNAME_MAP.put(502, "demux0");
         CHENNAI_IF_IFDESCR_MAP.put(502, "demux0");
         CHENNAI_IF_IFNAME_MAP.put(545, "lc-4/1/0");
         CHENNAI_IF_IFDESCR_MAP.put(545, "lc-4/1/0");
         CHENNAI_IF_IFNAME_MAP.put(540, "ge-4/1/6");
         CHENNAI_IF_IFDESCR_MAP.put(540, "ge-4/1/6");
         CHENNAI_IF_MAC_MAP.put(540, "002283d6a580");
         CHENNAI_IF_IFNAME_MAP.put(530, "lc-4/0/0.32769");
         CHENNAI_IF_IFDESCR_MAP.put(530, "lc-4/0/0.32769");
         CHENNAI_IF_IFNAME_MAP.put(508, "pfe-4/1/0");
         CHENNAI_IF_IFDESCR_MAP.put(508, "pfe-4/1/0");
         CHENNAI_IF_IFNAME_MAP.put(534, "ge-4/1/0");
         CHENNAI_IF_IFDESCR_MAP.put(534, "ge-4/1/0");
         CHENNAI_IF_MAC_MAP.put(534, "002283d6a57a");
         CHENNAI_IF_IFNAME_MAP.put(511, "pfe-4/0/0.16383");
         CHENNAI_IF_IFDESCR_MAP.put(511, "pfe-4/0/0.16383");
         CHENNAI_IF_IFNAME_MAP.put(529, "lc-4/0/0");
         CHENNAI_IF_IFDESCR_MAP.put(529, "lc-4/0/0");
         CHENNAI_IF_IFNAME_MAP.put(570, "lc-4/3/0.32769");
         CHENNAI_IF_IFDESCR_MAP.put(570, "lc-4/3/0.32769");
         CHENNAI_IF_IFNAME_MAP.put(560, "ge-4/3/1");
         CHENNAI_IF_IFDESCR_MAP.put(560, "ge-4/3/1");
         CHENNAI_IF_MAC_MAP.put(560, "002283d6a61f");
         CHENNAI_IF_IFNAME_MAP.put(7, "tap");
         CHENNAI_IF_IFDESCR_MAP.put(7, "tap");
         CHENNAI_IF_IFNAME_MAP.put(546, "lc-4/1/0.32769");
         CHENNAI_IF_IFDESCR_MAP.put(546, "lc-4/1/0.32769");
         CHENNAI_IF_IFNAME_MAP.put(501, "cbp0");
         CHENNAI_IF_IFDESCR_MAP.put(501, "cbp0");
         CHENNAI_IF_MAC_MAP.put(501, "002283d6a011");
         CHENNAI_IF_IFNAME_MAP.put(519, "ge-4/0/2");
         CHENNAI_IF_IFDESCR_MAP.put(519, "ge-4/0/2");
         CHENNAI_IF_MAC_MAP.put(519, "002283d6a52a");
         CHENNAI_IF_IFNAME_MAP.put(503, "irb");
         CHENNAI_IF_IFDESCR_MAP.put(503, "irb");
         CHENNAI_IF_MAC_MAP.put(503, "002283d6a7f0");
         CHENNAI_IF_IFNAME_MAP.put(548, "ge-4/2/1");
         CHENNAI_IF_IFDESCR_MAP.put(548, "ge-4/2/1");
         CHENNAI_IF_MAC_MAP.put(548, "002283d6a5cd");
         CHENNAI_IF_IFNAME_MAP.put(535, "ge-4/1/1");
         CHENNAI_IF_IFDESCR_MAP.put(535, "ge-4/1/1");
         CHENNAI_IF_MAC_MAP.put(535, "002283d6a57b");
         CHENNAI_IF_IFNAME_MAP.put(528, "ge-4/0/2.0");
         CHENNAI_IF_IFDESCR_MAP.put(528, "ge-4/0/2.0");
         CHENNAI_IF_MAC_MAP.put(528, "002283d6a52a");
         CHENNAI_IF_IFNAME_MAP.put(518, "ge-4/0/1");
         CHENNAI_IF_IFDESCR_MAP.put(518, "ge-4/0/1");
         CHENNAI_IF_MAC_MAP.put(518, "002283d6a529");
         CHENNAI_IF_IFNAME_MAP.put(23, "em1");
         CHENNAI_IF_IFDESCR_MAP.put(23, "em1");
         CHENNAI_IF_MAC_MAP.put(23, "020001000004");
         CHENNAI_IF_IFNAME_MAP.put(6, "lo0");
         CHENNAI_IF_IFDESCR_MAP.put(6, "lo0");
         CHENNAI_IF_IFNAME_MAP.put(538, "ge-4/1/4");
         CHENNAI_IF_IFDESCR_MAP.put(538, "ge-4/1/4");
         CHENNAI_IF_MAC_MAP.put(538, "002283d6a57e");
         CHENNAI_IF_IFNAME_MAP.put(567, "ge-4/3/8");
         CHENNAI_IF_IFDESCR_MAP.put(567, "ge-4/3/8");
         CHENNAI_IF_MAC_MAP.put(567, "002283d6a626");
         CHENNAI_IF_IFNAME_MAP.put(542, "ge-4/1/6.0");
         CHENNAI_IF_IFDESCR_MAP.put(542, "ge-4/1/6.0");
         CHENNAI_IF_MAC_MAP.put(542, "002283d6a580");
         CHENNAI_IF_IFNAME_MAP.put(557, "ge-4/2/8");
         CHENNAI_IF_IFDESCR_MAP.put(557, "ge-4/2/8");
         CHENNAI_IF_MAC_MAP.put(557, "002283d6a5d4");
         CHENNAI_IF_IFNAME_MAP.put(510, "pfe-4/3/0");
         CHENNAI_IF_IFDESCR_MAP.put(510, "pfe-4/3/0");
         CHENNAI_IF_IFNAME_MAP.put(552, "ge-4/2/5");
         CHENNAI_IF_IFDESCR_MAP.put(552, "ge-4/2/5");
         CHENNAI_IF_MAC_MAP.put(552, "002283d6a5d1");
         CHENNAI_IF_IFNAME_MAP.put(525, "ge-4/0/7");
         CHENNAI_IF_IFDESCR_MAP.put(525, "ge-4/0/7");
         CHENNAI_IF_MAC_MAP.put(525, "002283d6a52f");
         CHENNAI_IF_IFNAME_MAP.put(12, "mtun");
         CHENNAI_IF_IFDESCR_MAP.put(12, "mtun");
         CHENNAI_IF_IFNAME_MAP.put(22, "lo0.16385");
         CHENNAI_IF_IFDESCR_MAP.put(22, "lo0.16385");
         CHENNAI_IF_IFNAME_MAP.put(553, "ge-4/2/6");
         CHENNAI_IF_IFDESCR_MAP.put(553, "ge-4/2/6");
         CHENNAI_IF_MAC_MAP.put(553, "002283d6a5d2");
         CHENNAI_IF_IFNAME_MAP.put(505, "pp0");
         CHENNAI_IF_IFDESCR_MAP.put(505, "pp0");
         CHENNAI_IF_IFNAME_MAP.put(554, "ge-4/2/7");
         CHENNAI_IF_IFDESCR_MAP.put(554, "ge-4/2/7");
         CHENNAI_IF_MAC_MAP.put(554, "002283d6a5d3");
         CHENNAI_IF_IFNAME_MAP.put(10, "pime");
         CHENNAI_IF_IFDESCR_MAP.put(10, "pime");
         CHENNAI_IF_IFNAME_MAP.put(509, "pfe-4/2/0");
         CHENNAI_IF_IFDESCR_MAP.put(509, "pfe-4/2/0");
         CHENNAI_IF_IFNAME_MAP.put(11, "pimd");
         CHENNAI_IF_IFDESCR_MAP.put(11, "pimd");
         CHENNAI_IF_IFNAME_MAP.put(521, "ge-4/0/4");
         CHENNAI_IF_IFDESCR_MAP.put(521, "ge-4/0/4");
         CHENNAI_IF_MAC_MAP.put(521, "002283d6a52c");
         CHENNAI_IF_IFNAME_MAP.put(527, "ge-4/0/9");
         CHENNAI_IF_IFDESCR_MAP.put(527, "ge-4/0/9");
         CHENNAI_IF_MAC_MAP.put(527, "002283d6a531");
         CHENNAI_IF_IFNAME_MAP.put(532, "ge-4/0/3.0");
         CHENNAI_IF_IFDESCR_MAP.put(532, "ge-4/0/3.0");
         CHENNAI_IF_MAC_MAP.put(532, "002283d6a52b");
         CHENNAI_IF_IFNAME_MAP.put(17, "em0");
         CHENNAI_IF_IFDESCR_MAP.put(17, "em0");
         CHENNAI_IF_MAC_MAP.put(17, "020000000004");
         CHENNAI_IF_IFNAME_MAP.put(8, "gre");
         CHENNAI_IF_IFDESCR_MAP.put(8, "gre");
         CHENNAI_IF_IFNAME_MAP.put(543, "ge-4/1/8");
         CHENNAI_IF_IFDESCR_MAP.put(543, "ge-4/1/8");
         CHENNAI_IF_MAC_MAP.put(543, "002283d6a582");
         CHENNAI_IF_IFNAME_MAP.put(547, "ge-4/2/0");
         CHENNAI_IF_IFDESCR_MAP.put(547, "ge-4/2/0");
         CHENNAI_IF_MAC_MAP.put(547, "002283d6a5cc");
         CHENNAI_IF_IFNAME_MAP.put(569, "lc-4/3/0");
         CHENNAI_IF_IFDESCR_MAP.put(569, "lc-4/3/0");
         CHENNAI_IF_IFNAME_MAP.put(565, "ge-4/3/6");
         CHENNAI_IF_IFDESCR_MAP.put(565, "ge-4/3/6");
         CHENNAI_IF_MAC_MAP.put(565, "002283d6a624");
         CHENNAI_IF_IFNAME_MAP.put(568, "ge-4/3/9");
         CHENNAI_IF_IFDESCR_MAP.put(568, "ge-4/3/9");
         CHENNAI_IF_MAC_MAP.put(568, "002283d6a627");
         CHENNAI_IF_IFNAME_MAP.put(524, "ge-4/0/6");
         CHENNAI_IF_IFDESCR_MAP.put(524, "ge-4/0/6");
         CHENNAI_IF_MAC_MAP.put(524, "002283d6a52e");
         CHENNAI_IF_IFNAME_MAP.put(551, "ge-4/2/4");
         CHENNAI_IF_IFDESCR_MAP.put(551, "ge-4/2/4");
         CHENNAI_IF_MAC_MAP.put(551, "002283d6a5d0");
         CHENNAI_IF_IFNAME_MAP.put(9, "ipip");
         CHENNAI_IF_IFDESCR_MAP.put(9, "ipip");
         CHENNAI_IF_IFNAME_MAP.put(537, "ge-4/1/3");
         CHENNAI_IF_IFDESCR_MAP.put(537, "ge-4/1/3");
         CHENNAI_IF_MAC_MAP.put(537, "002283d6a57d");
         CHENNAI_IF_IFNAME_MAP.put(16, "lo0.0");
         CHENNAI_IF_IFDESCR_MAP.put(16, "lo0.0");
         CHENNAI_IF_IFNAME_MAP.put(549, "ge-4/2/2");
         CHENNAI_IF_IFDESCR_MAP.put(549, "ge-4/2/2");
         CHENNAI_IF_MAC_MAP.put(549, "002283d6a5ce");
         CHENNAI_IF_IFNAME_MAP.put(550, "ge-4/2/3");
         CHENNAI_IF_IFDESCR_MAP.put(550, "ge-4/2/3");
         CHENNAI_IF_MAC_MAP.put(550, "002283d6a5cf");
         CHENNAI_IF_IFNAME_MAP.put(516, "ge-4/0/0");
         CHENNAI_IF_IFDESCR_MAP.put(516, "ge-4/0/0");
         CHENNAI_IF_MAC_MAP.put(516, "002283d6a528");
         CHENNAI_IF_IFNAME_MAP.put(561, "ge-4/3/2");
         CHENNAI_IF_IFDESCR_MAP.put(561, "ge-4/3/2");
         CHENNAI_IF_MAC_MAP.put(561, "002283d6a620");
         CHENNAI_IF_IFNAME_MAP.put(506, "pfh-4/0/0");
         CHENNAI_IF_IFDESCR_MAP.put(506, "pfh-4/0/0");
         CHENNAI_IF_IFNAME_MAP.put(4, "lsi");
         CHENNAI_IF_IFDESCR_MAP.put(4, "lsi");
         CHENNAI_IF_IFNAME_MAP.put(513, "pfe-4/1/0.16383");
         CHENNAI_IF_IFDESCR_MAP.put(513, "pfe-4/1/0.16383");
         CHENNAI_IF_IFNAME_MAP.put(526, "ge-4/0/8");
         CHENNAI_IF_IFDESCR_MAP.put(526, "ge-4/0/8");
         CHENNAI_IF_MAC_MAP.put(526, "002283d6a530");
         CHENNAI_IF_IFNAME_MAP.put(514, "pfe-4/2/0.16383");
         CHENNAI_IF_IFDESCR_MAP.put(514, "pfe-4/2/0.16383");
         CHENNAI_IF_IFNAME_MAP.put(566, "ge-4/3/7");
         CHENNAI_IF_IFDESCR_MAP.put(566, "ge-4/3/7");
         CHENNAI_IF_MAC_MAP.put(566, "002283d6a625");
         CHENNAI_IF_IFNAME_MAP.put(522, "ge-4/0/5");
         CHENNAI_IF_IFDESCR_MAP.put(522, "ge-4/0/5");
         CHENNAI_IF_MAC_MAP.put(522, "002283d6a52d");
         CHENNAI_IF_IFNAME_MAP.put(5, "dsc");
         CHENNAI_IF_IFDESCR_MAP.put(5, "dsc");
         CHENNAI_IF_IFNAME_MAP.put(544, "ge-4/1/9");
         CHENNAI_IF_IFDESCR_MAP.put(544, "ge-4/1/9");
         CHENNAI_IF_MAC_MAP.put(544, "002283d6a583");
         CHENNAI_IF_IFNAME_MAP.put(520, "ge-4/0/3");
         CHENNAI_IF_IFDESCR_MAP.put(520, "ge-4/0/3");
         CHENNAI_IF_MAC_MAP.put(520, "002283d6a52b");
         CHENNAI_IF_IFNAME_MAP.put(564, "ge-4/3/5");
         CHENNAI_IF_IFDESCR_MAP.put(564, "ge-4/3/5");
         CHENNAI_IF_MAC_MAP.put(564, "002283d6a623");
         CHENNAI_IF_IFNAME_MAP.put(559, "ge-4/3/0");
         CHENNAI_IF_IFDESCR_MAP.put(559, "ge-4/3/0");
         CHENNAI_IF_MAC_MAP.put(559, "002283d6a61e");
         CHENNAI_IF_IFNAME_MAP.put(541, "ge-4/1/7");
         CHENNAI_IF_IFDESCR_MAP.put(541, "ge-4/1/7");
         CHENNAI_IF_MAC_MAP.put(541, "002283d6a581");
    }

    static final String DELHI_IP = "10.205.56.7";
    static final String DELHI_NAME = "Delhi";
    static final String DELHI_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.29";
   
    static final Map<InetAddress,Integer> DELHI_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> DELHI_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> DELHI_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> DELHI_IF_MAC_MAP = new HashMap<Integer, String>();
    
    static {
        try {
            DELHI_IP_IF_MAP.put(InetAddress.getByName("172.16.7.1"), 17619);
            DELHI_IP_IF_MAP.put(InetAddress.getByName("192.168.1.21"), 29804);
            DELHI_IP_IF_MAP.put(InetAddress.getByName("10.205.56.7"), 13);
            DELHI_IP_IF_MAP.put(InetAddress.getByName("128.0.0.1"), 18);
            DELHI_IP_IF_MAP.put(InetAddress.getByName("10.0.0.4"), 18);
            DELHI_IP_IF_MAP.put(InetAddress.getByName("192.168.1.2"), 28514);
            DELHI_IP_IF_MAP.put(InetAddress.getByName("128.0.0.4"), 18);
            DELHI_IP_IF_MAP.put(InetAddress.getByName("192.168.5.10"), 28503);
            DELHI_IP_IF_MAP.put(InetAddress.getByName("192.168.1.5"), 3674);
            DELHI_IP_IF_MAP.put(InetAddress.getByName("192.168.7.1"), 16);           
        } catch (Exception e) {
            e.printStackTrace();
        }
        DELHI_IF_IFNAME_MAP.put(1303, "pip0");
        DELHI_IF_IFDESCR_MAP.put(1303, "pip0");
        DELHI_IF_MAC_MAP.put(1303, "002283f167b0");
        DELHI_IF_IFNAME_MAP.put(273, "lc-1/2/0.32769");
        DELHI_IF_IFDESCR_MAP.put(273, "lc-1/2/0.32769");
        DELHI_IF_IFNAME_MAP.put(28518, "ge-1/1/4");
        DELHI_IF_IFDESCR_MAP.put(28518, "ge-1/1/4");
        DELHI_IF_MAC_MAP.put(28518, "002283f1633e");
        DELHI_IF_IFNAME_MAP.put(278, "ge-1/3/4");
        DELHI_IF_IFDESCR_MAP.put(278, "ge-1/3/4");
        DELHI_IF_MAC_MAP.put(278, "002283f16488");
        DELHI_IF_IFNAME_MAP.put(277, "ge-1/3/3");
        DELHI_IF_IFDESCR_MAP.put(277, "ge-1/3/3");
        DELHI_IF_MAC_MAP.put(277, "002283f16487");
        DELHI_IF_IFNAME_MAP.put(5, "dsc");
        DELHI_IF_IFDESCR_MAP.put(5, "dsc");
        DELHI_IF_IFNAME_MAP.put(1, "fxp0");
        DELHI_IF_IFDESCR_MAP.put(1, "fxp0");
        DELHI_IF_MAC_MAP.put(1, "00a0a56264ef");
        DELHI_IF_IFNAME_MAP.put(275, "ge-1/3/1");
        DELHI_IF_IFDESCR_MAP.put(275, "ge-1/3/1");
        DELHI_IF_MAC_MAP.put(275, "002283f16485");
        DELHI_IF_IFNAME_MAP.put(12397, "ge-1/2/2");
        DELHI_IF_IFDESCR_MAP.put(12397, "ge-1/2/2");
        DELHI_IF_MAC_MAP.put(12397, "002283f163e1");
        DELHI_IF_IFNAME_MAP.put(3672, "irb");
        DELHI_IF_IFDESCR_MAP.put(3672, "irb");
        DELHI_IF_MAC_MAP.put(3672, "002283f167f0");
        DELHI_IF_IFNAME_MAP.put(10, "pime");
        DELHI_IF_IFDESCR_MAP.put(10, "pime");
        DELHI_IF_IFNAME_MAP.put(284, "lc-1/3/0");
        DELHI_IF_IFDESCR_MAP.put(284, "lc-1/3/0");
        DELHI_IF_IFNAME_MAP.put(28507, "ge-1/0/9");
        DELHI_IF_IFDESCR_MAP.put(28507, "ge-1/0/9");
        DELHI_IF_MAC_MAP.put(28507, "002283f1629e");
        DELHI_IF_IFNAME_MAP.put(28513, "ge-1/1/1");
        DELHI_IF_IFDESCR_MAP.put(28513, "ge-1/1/1");
        DELHI_IF_MAC_MAP.put(28513, "002283f1633b");
        DELHI_IF_IFNAME_MAP.put(28525, "lc-1/1/0");
        DELHI_IF_IFDESCR_MAP.put(28525, "lc-1/1/0");
        DELHI_IF_IFNAME_MAP.put(29800, "ge-1/2/4");
        DELHI_IF_IFDESCR_MAP.put(29800, "ge-1/2/4");
        DELHI_IF_MAC_MAP.put(29800, "002283f163e3");
        DELHI_IF_IFNAME_MAP.put(11, "pimd");
        DELHI_IF_IFDESCR_MAP.put(11, "pimd");
        DELHI_IF_IFNAME_MAP.put(3674, "ge-1/0/1.0");
        DELHI_IF_IFDESCR_MAP.put(3674, "ge-1/0/1.0");
        DELHI_IF_MAC_MAP.put(3674, "002283f16296");
        DELHI_IF_IFNAME_MAP.put(271, "ge-1/2/9");
        DELHI_IF_IFDESCR_MAP.put(271, "ge-1/2/9");
        DELHI_IF_MAC_MAP.put(271, "002283f163e8");
        DELHI_IF_IFNAME_MAP.put(829, "ge-1/2/1");
        DELHI_IF_IFDESCR_MAP.put(829, "ge-1/2/1");
        DELHI_IF_MAC_MAP.put(829, "002283f163e0");
        DELHI_IF_IFNAME_MAP.put(4, "lsi");
        DELHI_IF_IFDESCR_MAP.put(4, "lsi");
        DELHI_IF_IFNAME_MAP.put(28526, "lc-1/1/0.32769");
        DELHI_IF_IFDESCR_MAP.put(28526, "lc-1/1/0.32769");
        DELHI_IF_IFNAME_MAP.put(280, "ge-1/3/5");
        DELHI_IF_IFDESCR_MAP.put(280, "ge-1/3/5");
        DELHI_IF_MAC_MAP.put(280, "002283f16489");
        DELHI_IF_IFNAME_MAP.put(7, "tap");
        DELHI_IF_IFDESCR_MAP.put(7, "tap");
        DELHI_IF_IFNAME_MAP.put(282, "ge-1/3/8");
        DELHI_IF_IFDESCR_MAP.put(282, "ge-1/3/8");
        DELHI_IF_MAC_MAP.put(282, "002283f1648c");
        DELHI_IF_IFNAME_MAP.put(9, "ipip");
        DELHI_IF_IFDESCR_MAP.put(9, "ipip");
        DELHI_IF_IFNAME_MAP.put(267, "ge-1/2/5");
        DELHI_IF_IFDESCR_MAP.put(267, "ge-1/2/5");
        DELHI_IF_MAC_MAP.put(267, "002283f163e4");
        DELHI_IF_IFNAME_MAP.put(8, "gre");
        DELHI_IF_IFDESCR_MAP.put(8, "gre");
        DELHI_IF_IFNAME_MAP.put(24, "em1.0");
        DELHI_IF_IFDESCR_MAP.put(24, "em1.0");
        DELHI_IF_MAC_MAP.put(24, "020001000004");
        DELHI_IF_IFNAME_MAP.put(272, "lc-1/2/0");
        DELHI_IF_IFDESCR_MAP.put(272, "lc-1/2/0");
        DELHI_IF_IFNAME_MAP.put(28503, "ge-1/0/2.0");
        DELHI_IF_IFDESCR_MAP.put(28503, "ge-1/0/2.0");
        DELHI_IF_MAC_MAP.put(28503, "002283f16297");
        DELHI_IF_IFNAME_MAP.put(28505, "ge-1/0/7");
        DELHI_IF_IFDESCR_MAP.put(28505, "ge-1/0/7");
        DELHI_IF_MAC_MAP.put(28505, "002283f1629c");
        DELHI_IF_IFNAME_MAP.put(3671, "demux0");
        DELHI_IF_IFDESCR_MAP.put(3671, "demux0");
        DELHI_IF_IFNAME_MAP.put(23, "em1");
        DELHI_IF_IFDESCR_MAP.put(23, "em1");
        DELHI_IF_MAC_MAP.put(23, "020001000004");
        DELHI_IF_IFNAME_MAP.put(28523, "ge-1/1/9");
        DELHI_IF_IFDESCR_MAP.put(28523, "ge-1/1/9");
        DELHI_IF_MAC_MAP.put(28523, "002283f16343");
        DELHI_IF_IFNAME_MAP.put(28509, "lc-1/0/0");
        DELHI_IF_IFDESCR_MAP.put(28509, "lc-1/0/0");
        DELHI_IF_IFNAME_MAP.put(28517, "ge-1/1/3");
        DELHI_IF_IFDESCR_MAP.put(28517, "ge-1/1/3");
        DELHI_IF_MAC_MAP.put(28517, "002283f1633d");
        DELHI_IF_IFNAME_MAP.put(12, "mtun");
        DELHI_IF_IFDESCR_MAP.put(12, "mtun");
        DELHI_IF_IFNAME_MAP.put(28510, "lc-1/0/0.32769");
        DELHI_IF_IFDESCR_MAP.put(28510, "lc-1/0/0.32769");
        DELHI_IF_IFNAME_MAP.put(28500, "ge-1/0/4");
        DELHI_IF_IFDESCR_MAP.put(28500, "ge-1/0/4");
        DELHI_IF_MAC_MAP.put(28500, "002283f16299");
        DELHI_IF_IFNAME_MAP.put(28506, "ge-1/0/8");
        DELHI_IF_IFDESCR_MAP.put(28506, "ge-1/0/8");
        DELHI_IF_MAC_MAP.put(28506, "002283f1629d");
        DELHI_IF_IFNAME_MAP.put(1302, "cbp0");
        DELHI_IF_IFDESCR_MAP.put(1302, "cbp0");
        DELHI_IF_MAC_MAP.put(1302, "002283f16011");
        DELHI_IF_IFNAME_MAP.put(281, "ge-1/3/7");
        DELHI_IF_IFDESCR_MAP.put(281, "ge-1/3/7");
        DELHI_IF_MAC_MAP.put(281, "002283f1648b");
        DELHI_IF_IFNAME_MAP.put(268, "ge-1/2/6");
        DELHI_IF_IFDESCR_MAP.put(268, "ge-1/2/6");
        DELHI_IF_MAC_MAP.put(268, "002283f163e5");
        DELHI_IF_IFNAME_MAP.put(28521, "ge-1/1/7");
        DELHI_IF_IFDESCR_MAP.put(28521, "ge-1/1/7");
        DELHI_IF_MAC_MAP.put(28521, "002283f16341");
        DELHI_IF_IFNAME_MAP.put(276, "ge-1/3/2");
        DELHI_IF_IFDESCR_MAP.put(276, "ge-1/3/2");
        DELHI_IF_MAC_MAP.put(276, "002283f16486");
        DELHI_IF_IFNAME_MAP.put(28501, "ge-1/0/5");
        DELHI_IF_IFDESCR_MAP.put(28501, "ge-1/0/5");
        DELHI_IF_MAC_MAP.put(28501, "002283f1629a");
        DELHI_IF_IFNAME_MAP.put(28499, "ge-1/0/3");
        DELHI_IF_IFDESCR_MAP.put(28499, "ge-1/0/3");
        DELHI_IF_MAC_MAP.put(28499, "002283f16298");
        DELHI_IF_IFNAME_MAP.put(501, "pp0");
        DELHI_IF_IFDESCR_MAP.put(501, "pp0");
        DELHI_IF_IFNAME_MAP.put(16, "lo0.0");
        DELHI_IF_IFDESCR_MAP.put(16, "lo0.0");
        DELHI_IF_IFNAME_MAP.put(283, "ge-1/3/9");
        DELHI_IF_IFDESCR_MAP.put(283, "ge-1/3/9");
        DELHI_IF_MAC_MAP.put(283, "002283f1648d");
        DELHI_IF_IFNAME_MAP.put(18, "em0.0");
        DELHI_IF_IFDESCR_MAP.put(18, "em0.0");
        DELHI_IF_MAC_MAP.put(18, "020000000004");
        DELHI_IF_IFNAME_MAP.put(29804, "ge-1/1/5.0");
        DELHI_IF_IFDESCR_MAP.put(29804, "ge-1/1/5.0");
        DELHI_IF_MAC_MAP.put(29804, "002283f1633f");
        DELHI_IF_IFNAME_MAP.put(285, "lc-1/3/0.32769");
        DELHI_IF_IFDESCR_MAP.put(285, "lc-1/3/0.32769");
        DELHI_IF_IFNAME_MAP.put(274, "ge-1/3/0");
        DELHI_IF_IFDESCR_MAP.put(274, "ge-1/3/0");
        DELHI_IF_MAC_MAP.put(274, "002283f16484");
        DELHI_IF_IFNAME_MAP.put(28519, "ge-1/1/5");
        DELHI_IF_IFDESCR_MAP.put(28519, "ge-1/1/5");
        DELHI_IF_MAC_MAP.put(28519, "002283f1633f");
        DELHI_IF_IFNAME_MAP.put(17619, "ge-1/1/6.0");
        DELHI_IF_IFDESCR_MAP.put(17619, "ge-1/1/6.0");
        DELHI_IF_MAC_MAP.put(17619, "002283f16340");
        DELHI_IF_IFNAME_MAP.put(502, "ge-1/0/0");
        DELHI_IF_IFDESCR_MAP.put(502, "ge-1/0/0");
        DELHI_IF_MAC_MAP.put(502, "002283f16295");
        DELHI_IF_IFNAME_MAP.put(13, "fxp0.0");
        DELHI_IF_IFDESCR_MAP.put(13, "fxp0.0");
        DELHI_IF_MAC_MAP.put(13, "00a0a56264ef");
        DELHI_IF_IFNAME_MAP.put(28511, "ge-1/1/0");
        DELHI_IF_IFDESCR_MAP.put(28511, "ge-1/1/0");
        DELHI_IF_MAC_MAP.put(28511, "002283f1633a");
        DELHI_IF_IFNAME_MAP.put(28504, "ge-1/0/6");
        DELHI_IF_IFDESCR_MAP.put(28504, "ge-1/0/6");
        DELHI_IF_MAC_MAP.put(28504, "002283f1629b");
        DELHI_IF_IFNAME_MAP.put(28522, "ge-1/1/8");
        DELHI_IF_IFDESCR_MAP.put(28522, "ge-1/1/8");
        DELHI_IF_MAC_MAP.put(28522, "002283f16342");
        DELHI_IF_IFNAME_MAP.put(28515, "ge-1/1/2");
        DELHI_IF_IFDESCR_MAP.put(28515, "ge-1/1/2");
        DELHI_IF_MAC_MAP.put(28515, "002283f1633c");
        DELHI_IF_IFNAME_MAP.put(279, "ge-1/3/6");
        DELHI_IF_IFDESCR_MAP.put(279, "ge-1/3/6");
        DELHI_IF_MAC_MAP.put(279, "002283f1648a");
        DELHI_IF_IFNAME_MAP.put(28497, "ge-1/0/1");
        DELHI_IF_IFDESCR_MAP.put(28497, "ge-1/0/1");
        DELHI_IF_MAC_MAP.put(28497, "002283f16296");
        DELHI_IF_IFNAME_MAP.put(6, "lo0");
        DELHI_IF_IFDESCR_MAP.put(6, "lo0");
        DELHI_IF_IFNAME_MAP.put(28520, "ge-1/1/6");
        DELHI_IF_IFDESCR_MAP.put(28520, "ge-1/1/6");
        DELHI_IF_MAC_MAP.put(28520, "002283f16340");
        DELHI_IF_IFNAME_MAP.put(17, "em0");
        DELHI_IF_IFDESCR_MAP.put(17, "em0");
        DELHI_IF_MAC_MAP.put(17, "020000000004");
        DELHI_IF_IFNAME_MAP.put(269, "ge-1/2/7");
        DELHI_IF_IFDESCR_MAP.put(269, "ge-1/2/7");
        DELHI_IF_MAC_MAP.put(269, "002283f163e6");
        DELHI_IF_IFNAME_MAP.put(29799, "ge-1/2/3");
        DELHI_IF_IFDESCR_MAP.put(29799, "ge-1/2/3");
        DELHI_IF_MAC_MAP.put(29799, "002283f163e2");
        DELHI_IF_IFNAME_MAP.put(28527, "ge-1/2/0");
        DELHI_IF_IFDESCR_MAP.put(28527, "ge-1/2/0");
        DELHI_IF_MAC_MAP.put(28527, "002283f163df");
        DELHI_IF_IFNAME_MAP.put(28498, "ge-1/0/2");
        DELHI_IF_IFDESCR_MAP.put(28498, "ge-1/0/2");
        DELHI_IF_MAC_MAP.put(28498, "002283f16297");
        DELHI_IF_IFNAME_MAP.put(270, "ge-1/2/8");
        DELHI_IF_IFDESCR_MAP.put(270, "ge-1/2/8");
        DELHI_IF_MAC_MAP.put(270, "002283f163e7");
        DELHI_IF_IFNAME_MAP.put(22, "lo0.16385");
        DELHI_IF_IFDESCR_MAP.put(22, "lo0.16385");
        DELHI_IF_IFNAME_MAP.put(28514, "ge-1/1/0.0");
        DELHI_IF_IFDESCR_MAP.put(28514, "ge-1/1/0.0");
        DELHI_IF_MAC_MAP.put(28514, "002283f1633a");
    }
    
    static final String SPACE_EX_SW1_IP = "10.205.56.1";
    static final String SPACE_EX_SW1_NAME = "Space-EX-SW1";
    static final String SPACE_EX_SW1_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.30";
   
    static final Map<InetAddress,Integer> SPACE_EX_SW1_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> SPACE_EX_SW1_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> SPACE_EX_SW1_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> SPACE_EX_SW1_IF_MAC_MAP = new HashMap<Integer, String>();
    
    static {
        try {
            SPACE_EX_SW1_IP_IF_MAP.put(InetAddress.getByName("128.0.0.32"), 38);
            SPACE_EX_SW1_IP_IF_MAP.put(InetAddress.getByName("128.0.0.1"), 38);
            SPACE_EX_SW1_IP_IF_MAP.put(InetAddress.getByName("128.0.0.16"), 38);
            SPACE_EX_SW1_IP_IF_MAP.put(InetAddress.getByName("10.205.56.1"), 524);
            SPACE_EX_SW1_IP_IF_MAP.put(InetAddress.getByName("172.16.7.2"), 528);
            SPACE_EX_SW1_IP_IF_MAP.put(InetAddress.getByName("55.55.55.55"), 506);           
        } catch (Exception e) {
            e.printStackTrace();
        }
        SPACE_EX_SW1_IF_IFNAME_MAP.put(5, "dsc");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(5, "dsc");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(543, "ge-0/0/9");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(543, "ge-0/0/9");
        SPACE_EX_SW1_IF_MAC_MAP.put(543, "00239c023b4c");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(548, "ge-0/0/15.0");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(548, "ge-0/0/15.0");
        SPACE_EX_SW1_IF_MAC_MAP.put(548, "00239c023b52");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(521, "ge-0/0/18");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(521, "ge-0/0/18");
        SPACE_EX_SW1_IF_MAC_MAP.put(521, "00239c023b55");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(509, "ge-0/0/12");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(509, "ge-0/0/12");
        SPACE_EX_SW1_IF_MAC_MAP.put(509, "00239c023b4f");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(507, "ge-0/0/11");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(507, "ge-0/0/11");
        SPACE_EX_SW1_IF_MAC_MAP.put(507, "00239c023b4e");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(505, "ge-0/0/10");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(505, "ge-0/0/10");
        SPACE_EX_SW1_IF_MAC_MAP.put(505, "00239c023b4d");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(538, "ge-0/0/4");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(538, "ge-0/0/4");
        SPACE_EX_SW1_IF_MAC_MAP.put(538, "00239c023b47");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(33, "me0");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(33, "me0");
        SPACE_EX_SW1_IF_MAC_MAP.put(33, "00239c023b7f");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(501, "ge-0/0/0");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(501, "ge-0/0/0");
        SPACE_EX_SW1_IF_MAC_MAP.put(501, "00239c023b43");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(38, "bme0.32768");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(38, "bme0.32768");
        SPACE_EX_SW1_IF_MAC_MAP.put(38, "000bcafe0000");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(524, "ge-0/0/4.0");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(524, "ge-0/0/4.0");
        SPACE_EX_SW1_IF_MAC_MAP.put(524, "00239c023b47");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(4, "lsi");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(4, "lsi");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(506, "vlan.0");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(506, "vlan.0");
        SPACE_EX_SW1_IF_MAC_MAP.put(506, "00239c023b41");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(11, "pimd");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(11, "pimd");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(12, "mtun");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(12, "mtun");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(542, "ge-0/0/8");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(542, "ge-0/0/8");
        SPACE_EX_SW1_IF_MAC_MAP.put(542, "00239c023b4b");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(525, "ge-0/0/2");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(525, "ge-0/0/2");
        SPACE_EX_SW1_IF_MAC_MAP.put(525, "00239c023b45");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(523, "ge-0/0/19");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(523, "ge-0/0/19");
        SPACE_EX_SW1_IF_MAC_MAP.put(523, "00239c023b56");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(528, "ge-0/0/6.0");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(528, "ge-0/0/6.0");
        SPACE_EX_SW1_IF_MAC_MAP.put(528, "00239c023b49");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(534, "ge-0/0/23");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(534, "ge-0/0/23");
        SPACE_EX_SW1_IF_MAC_MAP.put(534, "00239c023b5a");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(9, "ipip");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(9, "ipip");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(6, "lo0");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(6, "lo0");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(539, "ge-0/0/5");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(539, "ge-0/0/5");
        SPACE_EX_SW1_IF_MAC_MAP.put(539, "00239c023b48");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(536, "ge-0/0/3");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(536, "ge-0/0/3");
        SPACE_EX_SW1_IF_MAC_MAP.put(536, "00239c023b46");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(533, "vlan");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(533, "vlan");
        SPACE_EX_SW1_IF_MAC_MAP.put(533, "00239c023b41");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(513, "ge-0/0/14");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(513, "ge-0/0/14");
        SPACE_EX_SW1_IF_MAC_MAP.put(513, "00239c023b51");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(519, "ge-0/0/17");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(519, "ge-0/0/17");
        SPACE_EX_SW1_IF_MAC_MAP.put(519, "00239c023b54");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(8, "gre");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(8, "gre");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(37, "bme0");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(37, "bme0");
        SPACE_EX_SW1_IF_MAC_MAP.put(37, "000bcafe0000");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(7, "tap");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(7, "tap");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(540, "ge-0/0/6");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(540, "ge-0/0/6");
        SPACE_EX_SW1_IF_MAC_MAP.put(540, "00239c023b49");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(529, "ge-0/0/21");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(529, "ge-0/0/21");
        SPACE_EX_SW1_IF_MAC_MAP.put(529, "00239c023b58");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(503, "ge-0/0/1");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(503, "ge-0/0/1");
        SPACE_EX_SW1_IF_MAC_MAP.put(503, "00239c023b44");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(515, "ge-0/0/15");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(515, "ge-0/0/15");
        SPACE_EX_SW1_IF_MAC_MAP.put(515, "00239c023b52");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(531, "ge-0/0/22");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(531, "ge-0/0/22");
        SPACE_EX_SW1_IF_MAC_MAP.put(531, "00239c023b59");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(10, "pime");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(10, "pime");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(511, "ge-0/0/13");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(511, "ge-0/0/13");
        SPACE_EX_SW1_IF_MAC_MAP.put(511, "00239c023b50");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(527, "ge-0/0/20");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(527, "ge-0/0/20");
        SPACE_EX_SW1_IF_MAC_MAP.put(527, "00239c023b57");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(517, "ge-0/0/16");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(517, "ge-0/0/16");
        SPACE_EX_SW1_IF_MAC_MAP.put(517, "00239c023b53");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(541, "ge-0/0/7");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(541, "ge-0/0/7");
        SPACE_EX_SW1_IF_MAC_MAP.put(541, "00239c023b4a");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(1361, "ge-0/0/0.0");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(1361, "ge-0/0/0.0");
        SPACE_EX_SW1_IF_MAC_MAP.put(1361, "00239c023b43");
        SPACE_EX_SW1_IF_IFNAME_MAP.put(549, "ge-0/0/16.0");
        SPACE_EX_SW1_IF_IFDESCR_MAP.put(549, "ge-0/0/16.0");
        SPACE_EX_SW1_IF_MAC_MAP.put(549, "00239c023b53");
    }
    
    static final String BANGALORE_IP = "10.205.56.9";
    static final String BANGALORE_NAME = "Bangalore";
    static final String BANGALORE_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.10";
   
    static final Map<InetAddress,Integer> BANGALORE_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> BANGALORE_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> BANGALORE_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> BANGALORE_IF_MAC_MAP = new HashMap<Integer, String>();

    static {
        try {
            BANGALORE_IP_IF_MAP.put(InetAddress.getByName("128.0.0.4"), 14);
            BANGALORE_IP_IF_MAP.put(InetAddress.getByName("192.168.1.9"), 2396);
            BANGALORE_IP_IF_MAP.put(InetAddress.getByName("192.168.9.1"), 16);
            BANGALORE_IP_IF_MAP.put(InetAddress.getByName("9.1.1.1"), 2407);
            BANGALORE_IP_IF_MAP.put(InetAddress.getByName("192.168.5.14"), 2401);
            BANGALORE_IP_IF_MAP.put(InetAddress.getByName("10.205.56.9"), 2366);
            BANGALORE_IP_IF_MAP.put(InetAddress.getByName("192.168.1.6"), 2397);
            BANGALORE_IP_IF_MAP.put(InetAddress.getByName("9.1.1.2"), 3350);
            BANGALORE_IP_IF_MAP.put(InetAddress.getByName("10.0.0.4"), 14);
            BANGALORE_IP_IF_MAP.put(InetAddress.getByName("128.0.0.1"), 14);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        BANGALORE_IF_IFNAME_MAP.put(3335, "mt-1/2/0");
        BANGALORE_IF_IFDESCR_MAP.put(3335, "mt-1/2/0");
        BANGALORE_IF_IFNAME_MAP.put(3322, "pc-0/0/0");
        BANGALORE_IF_IFDESCR_MAP.put(3322, "pc-0/0/0");
        BANGALORE_IF_IFNAME_MAP.put(3324, "ge-0/1/1");
        BANGALORE_IF_IFDESCR_MAP.put(3324, "ge-0/1/1");
        BANGALORE_IF_MAC_MAP.put(3324, "0022831d7c20");
        BANGALORE_IF_IFNAME_MAP.put(6, "lo0");
        BANGALORE_IF_IFDESCR_MAP.put(6, "lo0");
        BANGALORE_IF_IFNAME_MAP.put(3321, "ge-0/0/1");
        BANGALORE_IF_IFDESCR_MAP.put(3321, "ge-0/0/1");
        BANGALORE_IF_MAC_MAP.put(3321, "0022831d7c01");
        BANGALORE_IF_IFNAME_MAP.put(2401, "ge-0/0/0.0");
        BANGALORE_IF_IFDESCR_MAP.put(2401, "ge-0/0/0.0");
        BANGALORE_IF_MAC_MAP.put(2401, "0022831d7c00");
        BANGALORE_IF_IFNAME_MAP.put(3338, "pc-0/0/0.16383");
        BANGALORE_IF_IFDESCR_MAP.put(3338, "pc-0/0/0.16383");
        BANGALORE_IF_IFNAME_MAP.put(16, "lo0.0");
        BANGALORE_IF_IFDESCR_MAP.put(16, "lo0.0");
        BANGALORE_IF_IFNAME_MAP.put(3339, "pc-0/1/0.16383");
        BANGALORE_IF_IFDESCR_MAP.put(3339, "pc-0/1/0.16383");
        BANGALORE_IF_IFNAME_MAP.put(3334, "vt-1/2/0");
        BANGALORE_IF_IFDESCR_MAP.put(3334, "vt-1/2/0");
        BANGALORE_IF_IFNAME_MAP.put(3350, "ge-0/1/3.1");
        BANGALORE_IF_IFDESCR_MAP.put(3350, "ge-0/1/3.1");
        BANGALORE_IF_MAC_MAP.put(3350, "0022831d7c22");
        BANGALORE_IF_IFNAME_MAP.put(8, "gre");
        BANGALORE_IF_IFDESCR_MAP.put(8, "gre");
        BANGALORE_IF_IFNAME_MAP.put(10, "pime");
        BANGALORE_IF_IFDESCR_MAP.put(10, "pime");
        BANGALORE_IF_IFNAME_MAP.put(3329, "ge-0/0/3");
        BANGALORE_IF_IFDESCR_MAP.put(3329, "ge-0/0/3");
        BANGALORE_IF_MAC_MAP.put(3329, "0022831d7c03");
        BANGALORE_IF_IFNAME_MAP.put(3336, "lt-1/2/0");
        BANGALORE_IF_IFDESCR_MAP.put(3336, "lt-1/2/0");
        BANGALORE_IF_MAC_MAP.put(3336, "0022831d7cbc");
        BANGALORE_IF_IFNAME_MAP.put(2, "fxp1");
        BANGALORE_IF_IFDESCR_MAP.put(2, "fxp1");
        BANGALORE_IF_MAC_MAP.put(2, "020000000004");
        BANGALORE_IF_IFNAME_MAP.put(3337, "ge-1/3/0");
        BANGALORE_IF_IFDESCR_MAP.put(3337, "ge-1/3/0");
        BANGALORE_IF_MAC_MAP.put(3337, "0022831d7cdb");
        BANGALORE_IF_IFNAME_MAP.put(2366, "ge-0/0/2.0");
        BANGALORE_IF_IFDESCR_MAP.put(2366, "ge-0/0/2.0");
        BANGALORE_IF_MAC_MAP.put(2366, "0022831d7c02");
        BANGALORE_IF_IFNAME_MAP.put(1, "fxp0");
        BANGALORE_IF_IFDESCR_MAP.put(1, "fxp0");
        BANGALORE_IF_MAC_MAP.put(1, "00a0a561e749");
        BANGALORE_IF_IFNAME_MAP.put(2397, "ge-0/0/1.0");
        BANGALORE_IF_IFDESCR_MAP.put(2397, "ge-0/0/1.0");
        BANGALORE_IF_MAC_MAP.put(2397, "0022831d7c01");
        BANGALORE_IF_IFNAME_MAP.put(3330, "pd-1/2/0");
        BANGALORE_IF_IFDESCR_MAP.put(3330, "pd-1/2/0");
        BANGALORE_IF_IFNAME_MAP.put(3333, "ip-1/2/0");
        BANGALORE_IF_IFDESCR_MAP.put(3333, "ip-1/2/0");
        BANGALORE_IF_IFNAME_MAP.put(4, "lsi");
        BANGALORE_IF_IFDESCR_MAP.put(4, "lsi");
        BANGALORE_IF_IFNAME_MAP.put(348, "ge-0/0/0");
        BANGALORE_IF_IFDESCR_MAP.put(348, "ge-0/0/0");
        BANGALORE_IF_MAC_MAP.put(348, "0022831d7c00");
        BANGALORE_IF_IFNAME_MAP.put(3325, "ge-0/1/2");
        BANGALORE_IF_IFDESCR_MAP.put(3325, "ge-0/1/2");
        BANGALORE_IF_MAC_MAP.put(3325, "0022831d7c21");
        BANGALORE_IF_IFNAME_MAP.put(12, "mtun");
        BANGALORE_IF_IFDESCR_MAP.put(12, "mtun");
        BANGALORE_IF_IFNAME_MAP.put(3323, "ge-0/1/0");
        BANGALORE_IF_IFDESCR_MAP.put(3323, "ge-0/1/0");
        BANGALORE_IF_MAC_MAP.put(3323, "0022831d7c1f");
        BANGALORE_IF_IFNAME_MAP.put(5, "dsc");
        BANGALORE_IF_IFDESCR_MAP.put(5, "dsc");
        BANGALORE_IF_IFNAME_MAP.put(2396, "ge-0/1/0.0");
        BANGALORE_IF_IFDESCR_MAP.put(2396, "ge-0/1/0.0");
        BANGALORE_IF_MAC_MAP.put(2396, "0022831d7c1f");
        BANGALORE_IF_IFNAME_MAP.put(3331, "pe-1/2/0");
        BANGALORE_IF_IFDESCR_MAP.put(3331, "pe-1/2/0");
        BANGALORE_IF_IFNAME_MAP.put(3332, "gr-1/2/0");
        BANGALORE_IF_IFDESCR_MAP.put(3332, "gr-1/2/0");
        BANGALORE_IF_IFNAME_MAP.put(7, "tap");
        BANGALORE_IF_IFDESCR_MAP.put(7, "tap");
        BANGALORE_IF_IFNAME_MAP.put(9, "ipip");
        BANGALORE_IF_IFDESCR_MAP.put(9, "ipip");
        BANGALORE_IF_IFNAME_MAP.put(2407, "ge-0/1/3.0");
        BANGALORE_IF_IFDESCR_MAP.put(2407, "ge-0/1/3.0");
        BANGALORE_IF_MAC_MAP.put(2407, "0022831d7c22");
        BANGALORE_IF_IFNAME_MAP.put(3327, "ge-0/1/3");
        BANGALORE_IF_IFDESCR_MAP.put(3327, "ge-0/1/3");
        BANGALORE_IF_MAC_MAP.put(3327, "0022831d7c22");
        BANGALORE_IF_IFNAME_MAP.put(22, "lo0.16385");
        BANGALORE_IF_IFDESCR_MAP.put(22, "lo0.16385");
        BANGALORE_IF_IFNAME_MAP.put(3326, "ge-0/0/2");
        BANGALORE_IF_IFDESCR_MAP.put(3326, "ge-0/0/2");
        BANGALORE_IF_MAC_MAP.put(3326, "0022831d7c02");
        BANGALORE_IF_IFNAME_MAP.put(11, "pimd");
        BANGALORE_IF_IFDESCR_MAP.put(11, "pimd");
        BANGALORE_IF_IFNAME_MAP.put(3328, "pc-0/1/0");
        BANGALORE_IF_IFDESCR_MAP.put(3328, "pc-0/1/0");
        BANGALORE_IF_IFNAME_MAP.put(14, "fxp1.0");
        BANGALORE_IF_IFDESCR_MAP.put(14, "fxp1.0");
        BANGALORE_IF_MAC_MAP.put(14, "020000000004");        
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
    
    OnmsNode getDelhi() {
        return getNode(DELHI_NAME,DELHI_SYSOID,DELHI_IP,DELHI_IP_IF_MAP,DELHI_IF_IFNAME_MAP,DELHI_IF_MAC_MAP,DELHI_IF_IFDESCR_MAP);
    }    

    OnmsNode getSpaceExSw1() {
        return getNode(SPACE_EX_SW1_NAME,SPACE_EX_SW1_SYSOID,SPACE_EX_SW1_IP,SPACE_EX_SW1_IP_IF_MAP,SPACE_EX_SW1_IF_IFNAME_MAP,SPACE_EX_SW1_IF_MAC_MAP,SPACE_EX_SW1_IF_IFDESCR_MAP);
    }

    OnmsNode getBangalore() {
        return getNode(BANGALORE_NAME,BANGALORE_SYSOID,BANGALORE_IP,BANGALORE_IP_IF_MAP,BANGALORE_IF_IFNAME_MAP,BANGALORE_IF_MAC_MAP,BANGALORE_IF_IFDESCR_MAP);        
    }
        
}
