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

public enum AddressFamilyIdentifier {
    RESERVED("Reserved"),
    UNASSIGNED("Unassigned"),
    IPV4(1, "IP (IP version 4)"),
    IPV6(2, "IP6 (IP version 6)"),
    NSAP(3, "NSAP"),
    HDLC(4, "HDLC (8-bit multidrop)"),
    BBN1822(5, "BBN 1822"),
    MEDIA_802(6, "802 (includes all 802 media plus Ethernet \"canonical format\")"),
    E163(7, "E.163"),
    E164(8, "E.164 (SMDS, Frame Relay, ATM)"),
    F69(9, "F.69 (Telex)"),
    X121(10, "X.121 (X.25, Frame Relay)"),
    IPX(11, "IPX"),
    APPLETALK(12, "Appletalk"),
    DECNET_IV(13, "Decnet IV"),
    BANYAN_VINES(14, "Banyan Vines"),
    E164_WITH_NSAP(15, "E.164 with NSAP format subaddress"),
    DNS(16, "DNS (Domain Name System)"),
    DISTINGUISHED_NAME(17, "Distinguished Name"),
    AS(18, "AS Number"),
    XTP_OVER_IPV4(19, "XTP over IP version 4"),
    XTP_OVER_IPV6(20, "XTP over IP version 6"),
    XTP_NATIVE(21, "XTP native mode XTP"),
    FIBRE_CHANNEL_PORT(22, "Fibre Channel World-Wide Port Name"),
    FIBRE_CHANNEL_NODE(23, "Fibre Channel World-Wide Node Name"),
    GWID(24, "GWID"),
    AFI_FOR_L2VPN(25, "AFI for L2VPN information"),
    MPLS_TP_SECTION(26, "MPLS-TP Section Endpoint Identifier"),
    MPLS_TP_LSP(27, "MPLS-TP LSP Endpoint Identifier"),
    MPLS_TP_PSEUDOWIRE(28, "MPLS-TP Pseudowire Endpoint Identifier"),
    MT_IP_IPV4(29, "MT IP: Multi-Topology IP version 4"),
    MT_IP_IPV6(30, "MT IPv6: Multi-Topology IP version 6"),
    EIGRP_COMMON(16384, "EIGRP Common Service Family"),
    EIGRP_IPV4(16385, "EIGRP IPv4 Service Family"),
    EIGRP_IPV6(16386, "EIGRP IPv6 Service Family"),
    LISP(16387, "LISP Canonical Address Format (LCAF)"),
    BGP_LS(16388, "BGP-LS"),
    MAC_48(16389, "48-bit MAC"),
    MAC_64(16390, "64-bit MAC"),
    OUI(16391, "OUI"),
    MAC_24(16392, "MAC/24"),
    MAC_40(16393, "MAC/40"),
    IPV6_64(16394, "IPv6/64"),
    RBRIDGE(16395, "RBridge Port ID"),
    TRILL(16396, "TRILL Nickname"),
    UUID(16397, "Universally Unique Identifier (UUID)");

    private int code;
    private String description;

    AddressFamilyIdentifier(final int code, final String description) {
        this.code = code;
        this.description = description;
    }

    AddressFamilyIdentifier(final String description) {
        this.code = -1;
        this.description = description;
    }

    public static AddressFamilyIdentifier from(final int code) {
        if (code == 0 || code == 65535) {
            return RESERVED;
        }
        if ((code >= 31 && code <= 16383) || (code >= 16398 && code <= 65534)) {
            return UNASSIGNED;
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
