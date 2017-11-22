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

package org.opennms.netmgt.telemetry.listeners.flow.v9;

import java.util.Optional;

import org.opennms.netmgt.telemetry.listeners.flow.Protocol;
import org.opennms.netmgt.telemetry.listeners.flow.ie.InformationElementDatabase;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.IPv4AddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.IPv6AddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.MacAddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.OctetArrayValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.UnsignedValue;

public class InformationElementProvider implements InformationElementDatabase.Provider {

    @Override
    public void load(InformationElementDatabase.Adder adder) {
        adder.add(Protocol.NETFLOW9, 1, "IN_BYTES", UnsignedValue::parserWith64Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 2, "IN_PKTS", UnsignedValue::parserWith64Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 3, "FLOWS", UnsignedValue::parserWith64Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 4, "PROTOCOL", UnsignedValue::parserWith8Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 5, "TOS", UnsignedValue::parserWith8Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 6, "TCP_FLAGS", UnsignedValue::parserWith8Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 7, "L4_SRC_PORT", UnsignedValue::parserWith16Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 8, "IPV4_SRC_ADDR", IPv4AddressValue::parser, Optional.empty());
        adder.add(Protocol.NETFLOW9, 9, "SRC_MASK", UnsignedValue::parserWith8Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 10, "INPUT_SNMP", UnsignedValue::parserWith64Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 11, "L4_DST_PORT", UnsignedValue::parserWith16Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 12, "IPV4_DST_ADDR", IPv4AddressValue::parser, Optional.empty());
        adder.add(Protocol.NETFLOW9, 13, "DST_MASK", UnsignedValue::parserWith8Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 14, "OUTPUT_SNMP", UnsignedValue::parserWith64Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 15, "IPV4_NEXT_HOP", IPv4AddressValue::parser, Optional.empty());
        adder.add(Protocol.NETFLOW9, 16, "SRC_AS", UnsignedValue::parserWith64Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 17, "DST_AS", UnsignedValue::parserWith64Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 18, "BGP_IPV4_NEXT_HOP", IPv4AddressValue::parser, Optional.empty());
        adder.add(Protocol.NETFLOW9, 19, "MUL_DST_PKTS", UnsignedValue::parserWith64Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 20, "MUL_DST_BYTES", UnsignedValue::parserWith64Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 21, "LAST_SWITCHED", UnsignedValue::parserWith32Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 22, "FIRST_SWITCHED", UnsignedValue::parserWith32Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 23, "OUT_BYTES", UnsignedValue::parserWith64Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 24, "OUT_PKTS", UnsignedValue::parserWith64Bit, Optional.empty());
        // 25 reserved
        // 26 reserved
        adder.add(Protocol.NETFLOW9, 27, "IPV6_SRC_ADDR", IPv6AddressValue::parser, Optional.empty());
        adder.add(Protocol.NETFLOW9, 28, "IPV6_DST_ADDR", IPv6AddressValue::parser, Optional.empty());
        adder.add(Protocol.NETFLOW9, 29, "IPV6_SRC_MASK", UnsignedValue::parserWith8Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 30, "IPV6_DST_MASK", UnsignedValue::parserWith8Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 31, "IPV6_FLOW_LABEL", UnsignedValue::parserWith24Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 32, "ICMP_TYPE", UnsignedValue::parserWith16Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 33, "MUL_IGMP_TYPE", UnsignedValue::parserWith8Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 34, "SAMPLING_INTERVAL", UnsignedValue::parserWith32Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 35, "SAMPLING_ALGORITHM", UnsignedValue::parserWith8Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 36, "FLOW_ACTIVE_TIMEOUT", UnsignedValue::parserWith16Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 37, "FLOW_INACTIVE_TIMEOUT", UnsignedValue::parserWith16Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 38, "ENGINE_TYPE", UnsignedValue::parserWith8Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 39, "ENGINE_ID", UnsignedValue::parserWith8Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 40, "TOTAL_BYTES_EXP", UnsignedValue::parserWith64Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 41, "TOTAL_PKTS_EXP", UnsignedValue::parserWith64Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 42, "TOTAL_FLOWS_EXP", UnsignedValue::parserWith64Bit, Optional.empty());
        // 43 reserved
        // 44 reserved
        // 45 reserved
        adder.add(Protocol.NETFLOW9, 46, "MPLS_TOP_LABEL_TYPE", UnsignedValue::parserWith8Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 47, "MPLS_TOP_LABEL_IP_ADDR", OctetArrayValue.parserWithLimits(4, 4), Optional.empty());
        adder.add(Protocol.NETFLOW9, 48, "FLOW_SAMPLER_ID", UnsignedValue::parserWith8Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 49, "FLOW_SAMPLER_MODE", UnsignedValue::parserWith8Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 50, "FLOW_SAMPLER_RANDOM_INTERVAL", UnsignedValue::parserWith32Bit, Optional.empty());
        // 51 reserved
        // 52 reserved
        // 53 reserved
        // 54 reserved
        adder.add(Protocol.NETFLOW9, 55, "DST_TOS", UnsignedValue::parserWith8Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 56, "SRC_MAC", MacAddressValue::parser, Optional.empty());
        adder.add(Protocol.NETFLOW9, 57, "DST_MAC", MacAddressValue::parser, Optional.empty());
        adder.add(Protocol.NETFLOW9, 58, "SRC_VLAN", UnsignedValue::parserWith16Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 59, "DST_VLAN", UnsignedValue::parserWith16Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 60, "IP_PROTOCOL_VERSION", UnsignedValue::parserWith8Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 61, "DIRECTION", UnsignedValue::parserWith8Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 62, "IPV6_NEXT_HOP", IPv6AddressValue::parser, Optional.empty());
        adder.add(Protocol.NETFLOW9, 63, "BGP_IPV6_NEXT_HOP", IPv6AddressValue::parser, Optional.empty());
        adder.add(Protocol.NETFLOW9, 64, "IPV6_OPTION_HEADERS", UnsignedValue::parserWith32Bit, Optional.empty());
        // 65 reserved
        // 66 reserved
        // 67 reserved
        // 68 reserved
        // 69 reserved
        adder.add(Protocol.NETFLOW9, 70, "MPLS_LABEL_1", UnsignedValue::parserWith24Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 71, "MPLS_LABEL_2", UnsignedValue::parserWith24Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 72, "MPLS_LABEL_3", UnsignedValue::parserWith24Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 73, "MPLS_LABEL_4", UnsignedValue::parserWith24Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 74, "MPLS_LABEL_5", UnsignedValue::parserWith24Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 75, "MPLS_LABEL_6", UnsignedValue::parserWith24Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 76, "MPLS_LABEL_7", UnsignedValue::parserWith24Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 77, "MPLS_LABEL_8", UnsignedValue::parserWith24Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 78, "MPLS_LABEL_9", UnsignedValue::parserWith24Bit, Optional.empty());
        adder.add(Protocol.NETFLOW9, 79, "MPLS_LABEL_10", UnsignedValue::parserWith24Bit, Optional.empty());
    }
}
