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
