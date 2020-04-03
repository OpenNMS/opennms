/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto;

import java.util.Arrays;

public enum SubsequentAddressFamilyIdentifier {
    RESERVED("Reserved"),
    RESERVED_FOR_PRIVATE_USE("Reserved for Private Use"),
    UNASSIGNED("Unassigned"),
    UNICAST_FORWARDING(1, "Network Layer Reachability Information used for unicast forwarding"),
    MULTICAST_FORWARDING(2, "Network Layer Reachability Information used for multicast forwarding"),
    NLRI(4, "Network Layer Reachability Information (NLRI) with MPLS Label"),
    MCAST_VPN(5, "MCAST-VPN"),
    DYNAMIC_PLACEMENT_MULTI_SEGMENT_PSEUDOWIRES(6, "Network Layer Reachability Information used for Dynamic Placement of Multi-Segment Pseudowires"),
    ENCAPSULATION_SAFI(7, "Encapsulation SAFI"),
    MCAST_VPLS(8, "MCAST-VPLS"),
    TUNNEL_SAFI(64, "Tunnel SAFI"),
    VPLS(65, "Virtual Private LAN Service (VPLS)"),
    BGP_MDT_SAFI(66, "BGP MDT SAFI"),
    BGP_4OVER6_SAFI(67, "BGP 4over6 SAFI"),
    BGP_6OVER4_SAFI(68, "BGP 6over4 SAFI"),
    LAYER1_VPN_AUTO_DISCOVERY(69, "Layer-1 VPN auto-discovery information"),
    BGP_EVPNS(70, "BGP EVPNs"),
    BGP_LS(71, "BGP-LS"),
    BGP_LS_VPN(72, "BGP-LS-VPN"),
    SR_TE_POLICY_SAFI(73, "SR TE Policy SAFI"),
    SD_WAN_CAP(74, "SD-WAN Capabilities"),
    MPLS_LABELED_VPN_ADDRESS(128, "MPLS-labeled VPN address"),
    MULTICAST_FOR_BGP_MPLS_VPNS(129, "Multicast for BGP/MPLS IP Virtual Private Networks (VPNs)"),
    ROUTE_TARGET_CONSTRAINS(132, "Route Target constrains"),
    IPV4_FLOW_SPEC(133, "IPv4 dissemination of flow specification rules"),
    VPN4V_FLOW_SPEC(134, "VPNv4 dissemination of flow specification rules"),
    VPN_AUTO_DISCOVERY(140, "VPN auto-discovery");

    private int code;
    private String description;

    SubsequentAddressFamilyIdentifier(final int code, final String description) {
        this.code = code;
        this.description = description;
    }

    SubsequentAddressFamilyIdentifier(final String description) {
        this.code = -1;
        this.description = description;
    }

    public static SubsequentAddressFamilyIdentifier from(final int code) {
        if (code == 0 || code == 3 || code == 255 || (code >= 141 && code <= 240)) {
            return RESERVED;
        }
        if ((code >= 9 && code <= 63) || (code >= 75 && code <= 127)) {
            return UNASSIGNED;
        }
        if (code >= 241 && code <= 254) {
            return RESERVED_FOR_PRIVATE_USE;
        }
        return Arrays.stream(values()).filter(e -> e.code == code).findFirst().get();
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
