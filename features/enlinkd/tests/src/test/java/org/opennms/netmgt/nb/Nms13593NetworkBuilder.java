/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2021 The OpenNMS Group, Inc.
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

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsNode;

import java.util.Locale;

/**
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */

public class Nms13593NetworkBuilder extends NmsNetworkBuilder {


    static {
    try {
        ZHBGO1Zsr001_IP_IF_MAP.put(InetAddressUtils.addr(ZHBGO1Zsr001_IP), 1);

        ZHBGO1Zsr001_IF_IFDESCR_MAP.put(1, "system, Loopback IP interface");
        ZHBGO1Zsr001_IF_IFNAME_MAP.put(1, "lo0");
        ZHBGO1Zsr001_IF_MAC_MAP.put(1, "24 21 24 EC E2 3F".replaceAll("\\s+","").toLowerCase(Locale.ROOT));
        ZHBGO1Zsr001_IF_IFALIAS_MAP.put(1, "");

        ZHBGO1Zsr001_IF_IFDESCR_MAP.put(104906753, "3/2/c1/1, 100-Gig Ethernet");
        ZHBGO1Zsr001_IF_IFNAME_MAP.put(104906753, "3/2/c1/1");
        ZHBGO1Zsr001_IF_MAC_MAP.put(104906753, "24 21 24 77 97 DF".replaceAll("\\s+","").toLowerCase(Locale.ROOT));
        ZHBGO1Zsr001_IF_IFALIAS_MAP.put(104906753, "");

        ZHBGO1Zsr001_IF_IFDESCR_MAP.put(105037825, "3/2/c5/1, 100-Gig Ethernet");
        ZHBGO1Zsr001_IF_IFNAME_MAP.put(105037825, "3/2/c5/1");
        ZHBGO1Zsr001_IF_MAC_MAP.put(105037825, "24 21 24 77 98 07".replaceAll("\\s+","").toLowerCase(Locale.ROOT));
        ZHBGO1Zsr001_IF_IFALIAS_MAP.put(105037825, "");

        ZHBGO1Zsr001_IF_IFDESCR_MAP.put(105070593, "3/2/c6/1, 100-Gig Ethernet");
        ZHBGO1Zsr001_IF_IFNAME_MAP.put(105070593, "3/2/c6/1");
        ZHBGO1Zsr001_IF_MAC_MAP.put(105070593, "24 21 24 77 98 11".replaceAll("\\s+","").toLowerCase(Locale.ROOT));
        ZHBGO1Zsr001_IF_IFALIAS_MAP.put(105070593, "");

        ZHBGO1Zsr002_IP_IF_MAP.put(InetAddressUtils.addr(ZHBGO1Zsr002_IP), 1);

        ZHBGO1Zsr002_IF_IFDESCR_MAP.put(1, "system, Loopback IP interface");
        ZHBGO1Zsr002_IF_IFNAME_MAP.put(1, "lo0");
        ZHBGO1Zsr002_IF_MAC_MAP.put(1, "24 21 24 DA F6 3F".replaceAll("\\s+","").toLowerCase(Locale.ROOT));
        ZHBGO1Zsr002_IF_IFALIAS_MAP.put(1, "");

        ZHBGO1Zsr002_IF_IFDESCR_MAP.put(104906753, "3/2/c1/1, 100-Gig Ethernet");
        ZHBGO1Zsr002_IF_IFNAME_MAP.put(104906753, "3/2/c1/1");
        ZHBGO1Zsr002_IF_MAC_MAP.put(104906753, "24 21 24 77 4F 37".replaceAll("\\s+","").toLowerCase(Locale.ROOT));
        ZHBGO1Zsr002_IF_IFALIAS_MAP.put(104906753, "");

        ZHBGO1Zsr002_IF_IFDESCR_MAP.put(105037825, "3/2/c5/1, 100-Gig Ethernet");
        ZHBGO1Zsr002_IF_IFNAME_MAP.put(105037825, "3/2/c5/1");
        ZHBGO1Zsr002_IF_MAC_MAP.put(105037825, "24 21 24 77 4F 5F".replaceAll("\\s+","").toLowerCase(Locale.ROOT));
        ZHBGO1Zsr002_IF_IFALIAS_MAP.put(105037825, "");

        ZHBGO1Zsr002_IF_IFDESCR_MAP.put(105070593, "3/2/c6/1, 100-Gig Ethernet");
        ZHBGO1Zsr002_IF_IFNAME_MAP.put(105070593, "3/2/c6/1");
        ZHBGO1Zsr002_IF_MAC_MAP.put(105070593, "24 21 24 77 4F 69".replaceAll("\\s+","").toLowerCase(Locale.ROOT));
        ZHBGO1Zsr002_IF_IFALIAS_MAP.put(105070593, "");

        ZHBGO1Zsr002_IF_IFDESCR_MAP.put(1140918299, "esat-1/1/27, 1-Gig/10-Gig Ethernet, \"to-ZHAUWA1Zbb002 1/1/c36/3 Ref|708432|2393/KTN|-XV003-ODF 48 - 13+14\"");
        ZHBGO1Zsr002_IF_IFNAME_MAP.put(1140918299, "esat-1/1/27");
        ZHBGO1Zsr002_IF_MAC_MAP.put(1140918299, "50 E0 EF 00 50 1C".replaceAll("\\s+","").toLowerCase(Locale.ROOT));
        ZHBGO1Zsr002_IF_IFALIAS_MAP.put(1140918299, "\"to-ZHAUWA1Zbb002 1/1/c36/3 Ref|708432|2393/KTN|-XV003-ODF 48 - 13+14\"");


    } catch (Exception e) {
        
    }
    }
    
    public OnmsNode getZHBGO1Zsr001() {
        return getNode(ZHBGO1Zsr001_NAME,ZHBGO1Zsr001_SYSOID,ZHBGO1Zsr001_IP,ZHBGO1Zsr001_IP_IF_MAP,ZHBGO1Zsr001_IF_IFNAME_MAP,
                ZHBGO1Zsr001_IF_MAC_MAP,ZHBGO1Zsr001_IF_IFDESCR_MAP,ZHBGO1Zsr001_IF_IFALIAS_MAP);
    }

    public OnmsNode getZHBGO1Zsr002() {
        return getNode(ZHBGO1Zsr002_NAME,ZHBGO1Zsr002_SYSOID,ZHBGO1Zsr002_IP,ZHBGO1Zsr002_IP_IF_MAP,ZHBGO1Zsr002_IF_IFNAME_MAP,
                ZHBGO1Zsr002_IF_MAC_MAP,ZHBGO1Zsr002_IF_IFDESCR_MAP,ZHBGO1Zsr002_IF_IFALIAS_MAP);
    }

}
