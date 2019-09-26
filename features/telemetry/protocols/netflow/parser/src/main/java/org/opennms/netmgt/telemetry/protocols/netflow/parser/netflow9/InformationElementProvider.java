/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9;

import java.util.Optional;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.Protocol;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElementDatabase;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Semantics;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.IPv4AddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.IPv6AddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.MacAddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.OctetArrayValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.StringValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UnsignedValue;

public class InformationElementProvider implements InformationElementDatabase.Provider {

    @Override
    public void load(InformationElementDatabase.Adder adder) {
        adder.add(Protocol.NETFLOW9, 1, UnsignedValue::parserWith64Bit, "IN_BYTES", Semantics.DELTA_COUNTER);
        adder.add(Protocol.NETFLOW9, 2, UnsignedValue::parserWith64Bit, "IN_PKTS", Semantics.DELTA_COUNTER);
        adder.add(Protocol.NETFLOW9, 3, UnsignedValue::parserWith64Bit, "FLOWS", Semantics.DELTA_COUNTER);
        adder.add(Protocol.NETFLOW9, 4, UnsignedValue::parserWith8Bit, "PROTOCOL", Semantics.IDENTIFIER);
        adder.add(Protocol.NETFLOW9, 5, UnsignedValue::parserWith8Bit, "TOS", Semantics.IDENTIFIER);
        adder.add(Protocol.NETFLOW9, 6, UnsignedValue::parserWith8Bit, "TCP_FLAGS", Semantics.FLAGS);
        adder.add(Protocol.NETFLOW9, 7, UnsignedValue::parserWith16Bit, "L4_SRC_PORT", Semantics.IDENTIFIER);
        adder.add(Protocol.NETFLOW9, 8, IPv4AddressValue::parser, "IPV4_SRC_ADDR", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 9, UnsignedValue::parserWith8Bit, "SRC_MASK", Optional.empty());
        adder.add(Protocol.NETFLOW9, 10, UnsignedValue::parserWith64Bit, "INPUT_SNMP", Semantics.IDENTIFIER);
        adder.add(Protocol.NETFLOW9, 11, UnsignedValue::parserWith16Bit, "L4_DST_PORT", Semantics.IDENTIFIER);
        adder.add(Protocol.NETFLOW9, 12, IPv4AddressValue::parser, "IPV4_DST_ADDR", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 13, UnsignedValue::parserWith8Bit, "DST_MASK", Optional.empty());
        adder.add(Protocol.NETFLOW9, 14, UnsignedValue::parserWith64Bit, "OUTPUT_SNMP", Semantics.IDENTIFIER);
        adder.add(Protocol.NETFLOW9, 15, IPv4AddressValue::parser, "IPV4_NEXT_HOP", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 16, UnsignedValue::parserWith32Bit, "SRC_AS", Optional.empty());
        adder.add(Protocol.NETFLOW9, 17, UnsignedValue::parserWith32Bit, "DST_AS", Optional.empty());
        adder.add(Protocol.NETFLOW9, 18, IPv4AddressValue::parser, "BGP_IPV4_NEXT_HOP", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 19, UnsignedValue::parserWith64Bit, "MUL_DST_PKTS", Semantics.DELTA_COUNTER);
        adder.add(Protocol.NETFLOW9, 20, UnsignedValue::parserWith64Bit, "MUL_DST_BYTES", Semantics.DELTA_COUNTER);
        adder.add(Protocol.NETFLOW9, 21, UnsignedValue::parserWith32Bit, "LAST_SWITCHED", Optional.empty());
        adder.add(Protocol.NETFLOW9, 22, UnsignedValue::parserWith32Bit, "FIRST_SWITCHED", Optional.empty());
        adder.add(Protocol.NETFLOW9, 23, UnsignedValue::parserWith64Bit, "OUT_BYTES", Semantics.DELTA_COUNTER);
        adder.add(Protocol.NETFLOW9, 24, UnsignedValue::parserWith64Bit, "OUT_PKTS", Semantics.DELTA_COUNTER);
        adder.add(Protocol.NETFLOW9, 25, UnsignedValue::parserWith16Bit, "MIN_PKT_LNGTH", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 26, UnsignedValue::parserWith16Bit, "MAX_PKT_LNGTH", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 27, IPv6AddressValue::parser, "IPV6_SRC_ADDR", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 28, IPv6AddressValue::parser, "IPV6_DST_ADDR", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 29, UnsignedValue::parserWith8Bit, "IPV6_SRC_MASK", Optional.empty());
        adder.add(Protocol.NETFLOW9, 30, UnsignedValue::parserWith8Bit, "IPV6_DST_MASK", Optional.empty());
        adder.add(Protocol.NETFLOW9, 31, UnsignedValue::parserWith32Bit, "IPV6_FLOW_LABEL", Semantics.IDENTIFIER);
        adder.add(Protocol.NETFLOW9, 32, UnsignedValue::parserWith16Bit, "ICMP_TYPE", Semantics.IDENTIFIER);
        adder.add(Protocol.NETFLOW9, 33, UnsignedValue::parserWith8Bit, "MUL_IGMP_TYPE", Semantics.IDENTIFIER);
        adder.add(Protocol.NETFLOW9, 34, UnsignedValue::parserWith32Bit, "SAMPLING_INTERVAL", Semantics.QUANTITY);
        adder.add(Protocol.NETFLOW9, 35, UnsignedValue::parserWith8Bit, "SAMPLING_ALGORITHM", Semantics.IDENTIFIER);
        adder.add(Protocol.NETFLOW9, 36, UnsignedValue::parserWith16Bit, "FLOW_ACTIVE_TIMEOUT", Optional.empty());
        adder.add(Protocol.NETFLOW9, 37, UnsignedValue::parserWith16Bit, "FLOW_INACTIVE_TIMEOUT", Optional.empty());
        adder.add(Protocol.NETFLOW9, 38, UnsignedValue::parserWith8Bit, "ENGINE_TYPE", Semantics.IDENTIFIER);
        adder.add(Protocol.NETFLOW9, 39, UnsignedValue::parserWith8Bit, "ENGINE_ID", Semantics.IDENTIFIER);
        adder.add(Protocol.NETFLOW9, 40, UnsignedValue::parserWith64Bit, "TOTAL_BYTES_EXP", Semantics.TOTAL_COUNTER);
        adder.add(Protocol.NETFLOW9, 41, UnsignedValue::parserWith64Bit, "TOTAL_PKTS_EXP", Semantics.TOTAL_COUNTER);
        adder.add(Protocol.NETFLOW9, 42, UnsignedValue::parserWith64Bit, "TOTAL_FLOWS_EXP", Semantics.TOTAL_COUNTER);
        // 43 vendor proprietary
        adder.add(Protocol.NETFLOW9, 44, UnsignedValue::parserWith32Bit, "IPV4_SRC_PREFIX", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 45, UnsignedValue::parserWith32Bit, "IPV4_DST_PREFIX", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 46, UnsignedValue::parserWith8Bit, "MPLS_TOP_LABEL_TYPE", Semantics.IDENTIFIER);
        adder.add(Protocol.NETFLOW9, 47, OctetArrayValue.parserWithLimits(4, 4), "MPLS_TOP_LABEL_IP_ADDR", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 48, UnsignedValue::parserWith32Bit, "FLOW_SAMPLER_ID", Semantics.IDENTIFIER);
        adder.add(Protocol.NETFLOW9, 49, UnsignedValue::parserWith8Bit, "FLOW_SAMPLER_MODE", Semantics.IDENTIFIER);
        adder.add(Protocol.NETFLOW9, 50, UnsignedValue::parserWith32Bit, "FLOW_SAMPLER_RANDOM_INTERVAL", Semantics.QUANTITY);
        // 51 vendor proprietary
        adder.add(Protocol.NETFLOW9, 52, UnsignedValue::parserWith8Bit, "MIN_TTL", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 53, UnsignedValue::parserWith8Bit, "MAX_TTL", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 54, UnsignedValue::parserWith32Bit, "IPV4_IDENT", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 55, UnsignedValue::parserWith8Bit, "DST_TOS", Semantics.IDENTIFIER);
        adder.add(Protocol.NETFLOW9, 56, MacAddressValue::parser, "SRC_MAC", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 57, MacAddressValue::parser, "DST_MAC", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 58, UnsignedValue::parserWith16Bit, "SRC_VLAN", Semantics.IDENTIFIER);
        adder.add(Protocol.NETFLOW9, 59, UnsignedValue::parserWith16Bit, "DST_VLAN", Semantics.IDENTIFIER);
        adder.add(Protocol.NETFLOW9, 60, UnsignedValue::parserWith8Bit, "IP_PROTOCOL_VERSION", Semantics.IDENTIFIER);
        adder.add(Protocol.NETFLOW9, 61, UnsignedValue::parserWith8Bit, "DIRECTION", Semantics.IDENTIFIER);
        adder.add(Protocol.NETFLOW9, 62, IPv6AddressValue::parser, "IPV6_NEXT_HOP", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 63, IPv6AddressValue::parser, "BGP_IPV6_NEXT_HOP", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 64, UnsignedValue::parserWith32Bit, "IPV6_OPTION_HEADERS", Semantics.FLAGS);
        // 65 vendor proprietary
        // 66 vendor proprietary
        // 67 vendor proprietary
        // 68 vendor proprietary
        // 69 vendor proprietary
        adder.add(Protocol.NETFLOW9, 70, UnsignedValue::parserWith24Bit, "MPLS_LABEL_1", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 71, UnsignedValue::parserWith24Bit, "MPLS_LABEL_2", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 72, UnsignedValue::parserWith24Bit, "MPLS_LABEL_3", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 73, UnsignedValue::parserWith24Bit, "MPLS_LABEL_4", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 74, UnsignedValue::parserWith24Bit, "MPLS_LABEL_5", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 75, UnsignedValue::parserWith24Bit, "MPLS_LABEL_6", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 76, UnsignedValue::parserWith24Bit, "MPLS_LABEL_7", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 77, UnsignedValue::parserWith24Bit, "MPLS_LABEL_8", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 78, UnsignedValue::parserWith24Bit, "MPLS_LABEL_9", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 79, UnsignedValue::parserWith24Bit, "MPLS_LABEL_10", Semantics.DEFAULT);

        adder.add(Protocol.NETFLOW9, 80, MacAddressValue::parser, "IN_DST_MAC", Semantics.IDENTIFIER);
        adder.add(Protocol.NETFLOW9, 81, MacAddressValue::parser, "OUT_SRC_MAC", Semantics.IDENTIFIER);
        adder.add(Protocol.NETFLOW9, 82, StringValue::parser, "IF_NAME", Semantics.IDENTIFIER);
        adder.add(Protocol.NETFLOW9, 83, StringValue::parser, "IF_DESC", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 84, StringValue::parser, "SAMPLER_NAME", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 85, UnsignedValue::parserWith64Bit, "IN_PERMANENT_BYTES", Semantics.TOTAL_COUNTER);
        adder.add(Protocol.NETFLOW9, 86, UnsignedValue::parserWith64Bit, "IN_PERMANENT_PKTS", Semantics.TOTAL_COUNTER);
        // 87 vendor proprietary
        adder.add(Protocol.NETFLOW9, 88, UnsignedValue::parserWith16Bit, "FRAGMENT_OFFSET", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 89, UnsignedValue::parserWith8Bit, "FORWARDING STATUS", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 90, OctetArrayValue::parser, "MPLS PAL RD", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 91, UnsignedValue::parserWith8Bit, "MPLS PREFIX LEN", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 92, UnsignedValue::parserWith32Bit, "SRC TRAFFIC INDEX", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 93, UnsignedValue::parserWith32Bit, "DST TRAFFIC INDEX", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 94, StringValue::parser, "APPLICATION DESCRIPTION", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 95, UnsignedValue::parserWith64Bit, "APPLICATION TAG", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 96, StringValue::parser, "APPLICATION NAME", Semantics.DEFAULT);
        // 97 ?
        adder.add(Protocol.NETFLOW9, 98, UnsignedValue::parserWith8Bit, "postipDiffServCodePoint", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 99, UnsignedValue::parserWith32Bit, "replication factor", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 100, OctetArrayValue::parser, "DEPRECATED", Semantics.DEFAULT);
        // 101 ?
        adder.add(Protocol.NETFLOW9, 102, UnsignedValue::parserWith32Bit, "layer2packetSectionOffset", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 103, UnsignedValue::parserWith32Bit, "layer2packetSectionSize", Semantics.DEFAULT);
        adder.add(Protocol.NETFLOW9, 104, OctetArrayValue::parser, "layer2packetSectionData", Semantics.DEFAULT);
        // 105-127 reserved for future use by cisco
    }
}
