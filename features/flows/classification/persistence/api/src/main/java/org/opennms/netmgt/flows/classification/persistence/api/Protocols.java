/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification.persistence.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;

public final class Protocols {

    private static final List<Protocol> protocols = new ArrayList<>();
    private static final Map<Integer, Protocol> decimalProtocolMap = new HashMap<>();
    private static final Map<String, Protocol> keywordProtocolMap = new HashMap<>();

    static {
        protocols.add(new Protocol(  0, "HOPOPT", "IPv6 Hop-by-Hop Option"));
        protocols.add(new Protocol(  1, "ICMP", "Internet Control Message"));
        protocols.add(new Protocol(  2, "IGMP", "Internet Group Management"));
        protocols.add(new Protocol(  3, "GGP", "Gateway-to-Gateway"));
        protocols.add(new Protocol(  4, "IPv4", "IPv4 encapsulation"));
        protocols.add(new Protocol(  5, "ST", "Stream"));
        protocols.add(new Protocol(  6, "TCP", "Transmission Control"));
        protocols.add(new Protocol(  7, "CBT", "CBT"));
        protocols.add(new Protocol(  8, "EGP", "Exterior Gateway Protocol"));
        protocols.add(new Protocol(  9, "IGP", "any private interior gateway"));
        protocols.add(new Protocol( 10, "BBN-RCC-MON", "BBN RCC Monitoring"));
        protocols.add(new Protocol( 11, "NVP-II", "Network Voice Protocol"));
        protocols.add(new Protocol( 12, "PUP", "PUP"));
        protocols.add(new Protocol( 13, "ARGUS", "ARGUS (deprecated)"));
        protocols.add(new Protocol( 14, "EMCON", "EMCON"));
        protocols.add(new Protocol( 15, "XNET", "Cross Net Debugger"));
        protocols.add(new Protocol( 16, "CHAOS", "Chaos"));
        protocols.add(new Protocol( 17, "UDP", "User Datagram"));
        protocols.add(new Protocol( 18, "MUX", "Multiplexing"));
        protocols.add(new Protocol( 19, "DCN-MEAS", "DCN Measurement Subsystems"));
        protocols.add(new Protocol( 20, "HMP", "Host Monitoring"));
        protocols.add(new Protocol( 21, "PRM", "Packet Radio Measurement"));
        protocols.add(new Protocol( 22, "XNS-IDP", "XEROX NS IDP"));
        protocols.add(new Protocol( 23, "TRUNK-1", "Trunk-1"));
        protocols.add(new Protocol( 24, "TRUNK-2", "Trunk-2"));
        protocols.add(new Protocol( 25, "LEAF-1", "Leaf-1"));
        protocols.add(new Protocol( 26, "LEAF-2", "Leaf-2"));
        protocols.add(new Protocol( 27, "RDP", "Reliable Data Protocol"));
        protocols.add(new Protocol( 28, "IRTP", "Internet Reliable Transaction"));
        protocols.add(new Protocol( 29, "ISO-TP4", "ISO Transport Protocol Class 4"));
        protocols.add(new Protocol( 30, "NETBLT", "Bulk Data Transfer Protocol"));
        protocols.add(new Protocol( 31, "MFE-NSP", "MFE Network Services Protocol"));
        protocols.add(new Protocol( 32, "MERIT-INP", "MERIT Internodal Protocol"));
        protocols.add(new Protocol( 33, "DCCP", "Datagram Congestion Control Protocol"));
        protocols.add(new Protocol( 34, "3PC", "Third Party Connect Protocol"));
        protocols.add(new Protocol( 35, "IDPR", "Inter-Domain Policy Routing Protocol"));
        protocols.add(new Protocol( 36, "XTP", "XTP"));
        protocols.add(new Protocol( 37, "DDP", "Datagram Delivery Protocol"));
        protocols.add(new Protocol( 38, "IDPR-CMTP", "IDPR Control Message Transport Proto"));
        protocols.add(new Protocol( 39, "TP++", "TP++ Transport Protocol"));
        protocols.add(new Protocol( 40, "IL", "IL Transport Protocol"));
        protocols.add(new Protocol( 41, "IPv6", "IPv6 encapsulation"));
        protocols.add(new Protocol( 42, "SDRP", "Source Demand Routing Protocol"));
        protocols.add(new Protocol( 43, "IPv6-Route", "Routing Header for IPv6"));
        protocols.add(new Protocol( 44, "IPv6-Frag", "Fragment Header for IPv6"));
        protocols.add(new Protocol( 45, "IDRP", "Inter-Domain Routing Protocol"));
        protocols.add(new Protocol( 46, "RSVP", "Reservation Protocol"));
        protocols.add(new Protocol( 47, "GRE", "Generic Routing Encapsulation"));
        protocols.add(new Protocol( 48, "DSR", "Dynamic Source Routing Protocol"));
        protocols.add(new Protocol( 49, "BNA", "BNA"));
        protocols.add(new Protocol( 50, "ESP", "Encap Security Payload"));
        protocols.add(new Protocol( 51, "AH", "Authentication Header"));
        protocols.add(new Protocol( 52, "I-NLSP", "Integrated Net Layer Security  TUBA"));
        protocols.add(new Protocol( 53, "SWIPE", "IP with Encryption (deprecated)"));
        protocols.add(new Protocol( 54, "NARP", "NBMA Address Resolution Protocol"));
        protocols.add(new Protocol( 55, "MOBILE", "IP Mobility"));
        protocols.add(new Protocol( 56, "TLSP", "Transport Layer Security Protocol"));
        protocols.add(new Protocol( 57, "SKIP", "SKIP"));
        protocols.add(new Protocol( 58, "IPv6-ICMP", "ICMP for IPv6"));
        protocols.add(new Protocol( 59, "IPv6-NoNxt", "No Next Header for IPv6"));
        protocols.add(new Protocol( 60, "IPv6-Opts", "Destination Options for IPv6"));
        protocols.add(new Protocol( 61, "", "any host internal protocol"));
        protocols.add(new Protocol( 62, "CFTP", "CFTP"));
        protocols.add(new Protocol( 63, "", "any local network"));
        protocols.add(new Protocol( 64, "SAT-EXPAK", "SATNET and Backroom EXPAK"));
        protocols.add(new Protocol( 65, "KRYPTOLAN", "Kryptolan"));
        protocols.add(new Protocol( 66, "RVD", "MIT Remote Virtual Disk Protocol"));
        protocols.add(new Protocol( 67, "IPPC", "Internet Pluribus Packet Core"));
        protocols.add(new Protocol( 68, "", "any distributed file system"));
        protocols.add(new Protocol( 69, "SAT-MON", "SATNET Monitoring"));
        protocols.add(new Protocol( 70, "VISA", "VISA Protocol"));
        protocols.add(new Protocol( 71, "IPCV", "Internet Packet Core Utility"));
        protocols.add(new Protocol( 72, "CPNX", "Computer Protocol Network Executive"));
        protocols.add(new Protocol( 73, "CPHB", "Computer Protocol Heart Beat"));
        protocols.add(new Protocol( 74, "WSN", "Wang Span Network"));
        protocols.add(new Protocol( 75, "PVP", "Packet Video Protocol"));
        protocols.add(new Protocol( 76, "BR-SAT-MON", "Backroom SATNET Monitoring"));
        protocols.add(new Protocol( 77, "SUN-ND", "SUN ND PROTOCOL-Temporary"));
        protocols.add(new Protocol( 78, "WB-MON", "WIDEBAND Monitoring"));
        protocols.add(new Protocol( 79, "WB-EXPAK", "WIDEBAND EXPAK"));
        protocols.add(new Protocol( 80, "ISO-IP", "ISO Internet Protocol"));
        protocols.add(new Protocol( 81, "VMTP", "VMTP"));
        protocols.add(new Protocol( 82, "SECURE-VMTP", "SECURE-VMTP"));
        protocols.add(new Protocol( 83, "VINES", "VINES"));
        protocols.add(new Protocol( 84, "TTP", "Transaction Transport Protocol"));
        protocols.add(new Protocol( 84, "IPTM", "Internet Protocol Traffic Manager"));
        protocols.add(new Protocol( 85, "NSFNET-IGP", "NSFNET-IGP"));
        protocols.add(new Protocol( 86, "DGP", "Dissimilar Gateway Protocol"));
        protocols.add(new Protocol( 87, "TCF", "TCF"));
        protocols.add(new Protocol( 88, "EIGRP", "EIGRP"));
        protocols.add(new Protocol( 89, "OSPFIGP", "OSPFIGP"));
        protocols.add(new Protocol( 90, "Sprite-RPC", "Sprite RPC Protocol"));
        protocols.add(new Protocol( 91, "LARP", "Locus Address Resolution Protocol"));
        protocols.add(new Protocol( 92, "MTP", "Multicast Transport Protocol"));
        protocols.add(new Protocol( 93, "AX.25", "AX.25 Frames"));
        protocols.add(new Protocol( 94, "IPIP", "IP-within-IP Encapsulation Protocol"));
        protocols.add(new Protocol( 95, "MICP", "Mobile Internetworking Control Pro. (deprecated)"));
        protocols.add(new Protocol( 96, "SCC-SP", "Semaphore Communications Sec. Pro."));
        protocols.add(new Protocol( 97, "ETHERIP", "Ethernet-within-IP Encapsulation"));
        protocols.add(new Protocol( 98, "ENCAP", "Encapsulation Header"));
        protocols.add(new Protocol( 99, "", "any private encryption scheme"));
        protocols.add(new Protocol(100, "GMTP", "GMTP"));
        protocols.add(new Protocol(101, "IFMP", "Ipsilon Flow Management Protocol"));
        protocols.add(new Protocol(102, "PNNI", "PNNI over IP"));
        protocols.add(new Protocol(103, "PIM", "Protocol Independent Multicast"));
        protocols.add(new Protocol(104, "ARIS", "ARIS"));
        protocols.add(new Protocol(105, "SCPS", "SCPS"));
        protocols.add(new Protocol(106, "QNX", "QNX"));
        protocols.add(new Protocol(107, "A/N", "Active Networks"));
        protocols.add(new Protocol(108, "IPComp", "IP Payload Compression Protocol"));
        protocols.add(new Protocol(109, "SNP", "Sitara Networks Protocol"));
        protocols.add(new Protocol(110, "Compaq-Peer", "Compaq Peer Protocol"));
        protocols.add(new Protocol(111, "IPX-in-IP", "IPX in IP"));
        protocols.add(new Protocol(112, "VRRP", "Virtual Router Redundancy Protocol"));
        protocols.add(new Protocol(113, "PGM", "PGM Reliable Transport Protocol"));
        protocols.add(new Protocol(114, "", "any 0-hop protocol"));
        protocols.add(new Protocol(115, "L2TP", "Layer Two Tunneling Protocol"));
        protocols.add(new Protocol(116, "DDX", "D-II Data Exchange (DDX)"));
        protocols.add(new Protocol(117, "IATP", "Interactive Agent Transfer Protocol"));
        protocols.add(new Protocol(118, "STP", "Schedule Transfer Protocol"));
        protocols.add(new Protocol(119, "SRP", "SpectraLink Radio Protocol"));
        protocols.add(new Protocol(120, "UTI", "UTI"));
        protocols.add(new Protocol(121, "SMP", "Simple Message Protocol"));
        protocols.add(new Protocol(122, "SM", "Simple Multicast Protocol (deprecated)"));
        protocols.add(new Protocol(123, "PTP", "Performance Transparency Protocol"));
        protocols.add(new Protocol(124, "ISIS over IPv4", ""));
        protocols.add(new Protocol(125, "FIRE", ""));
        protocols.add(new Protocol(126, "CRTP", "Combat Radio Transport Protocol"));
        protocols.add(new Protocol(127, "CRUDP", "Combat Radio User Datagram"));
        protocols.add(new Protocol(128, "SSCOPMCE", ""));
        protocols.add(new Protocol(129, "IPLT", ""));
        protocols.add(new Protocol(130, "SPS", "Secure Packet Shield"));
        protocols.add(new Protocol(131, "PIPE", "Private IP Encapsulation within IP"));
        protocols.add(new Protocol(132, "SCTP", "Stream Control Transmission Protocol"));
        protocols.add(new Protocol(133, "FC", "Fibre Channel"));
        protocols.add(new Protocol(134, "RSVP-E2E-IGNORE", ""));
        protocols.add(new Protocol(135, "Mobility Header", ""));
        protocols.add(new Protocol(136, "UDPLite", ""));
        protocols.add(new Protocol(137, "MPLS-in-IP", ""));
        protocols.add(new Protocol(138, "manet", "MANET Protocols"));
        protocols.add(new Protocol(139, "HIP", "Host Identity Protocol"));
        protocols.add(new Protocol(140, "Shim6", "Shim6 Protocol"));
        protocols.add(new Protocol(141, "WESP", "Wrapped Encapsulating Security Payload"));
        protocols.add(new Protocol(142, "ROHC", "Robust Header Compression"));
        protocols.add(new Protocol(253, "", "Use for experimentation and testing"));
        protocols.add(new Protocol(254, "", "Use for experimentation and testing"));
        protocols.add(new Protocol(255, "Reserved", ""));

        for (Protocol eachProtocol : protocols) {
            decimalProtocolMap.put(eachProtocol.getDecimal(), eachProtocol);
            // some protocols have an empty keyword, e.g. "99, ANY PRIVATE ENCRYPTION SCHEME". We skip those
            if (!Strings.isNullOrEmpty(eachProtocol.getKeyword())) {
                keywordProtocolMap.put(eachProtocol.getKeyword().toUpperCase(), eachProtocol);
            }
        }
    }

    public static List<Protocol> getProtocols() {
        return Collections.unmodifiableList(protocols);
    }

    public static Protocol getProtocol(String keyword) {
        if (keyword == null || "".equals(keyword)) {
            throw new IllegalArgumentException("Cannot determine protocol for empty or null keyword");
        }
        return keywordProtocolMap.get(keyword.toUpperCase());
    }

    public static Protocol getProtocol(int decimal) {
        return decimalProtocolMap.get(decimal);
    }

}
