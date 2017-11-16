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

import org.opennms.netmgt.telemetry.listeners.flow.ie.InformationElement;
import org.opennms.netmgt.telemetry.listeners.flow.ie.InformationElementDatabase;
import org.opennms.netmgt.telemetry.listeners.flow.ie.Value;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.IPv4AddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.IPv6AddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.MacAddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.OctetArrayValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.UnsignedValue;

import com.google.common.collect.ImmutableMap;

public class InformationElementProvider implements InformationElementDatabase.Provider {

    @Override
    public void load(ImmutableMap.Builder<InformationElementDatabase.Key, InformationElement> builder) {
        add(builder, 1, "IN_BYTES", UnsignedValue::parserWith64Bit);
        add(builder, 2, "IN_PKTS", UnsignedValue::parserWith64Bit);
        add(builder, 3, "FLOWS", UnsignedValue::parserWith64Bit);
        add(builder, 4, "PROTOCOL", UnsignedValue::parserWith8Bit);
        add(builder, 5, "TOS", UnsignedValue::parserWith8Bit);
        add(builder, 6, "TCP_FLAGS", UnsignedValue::parserWith8Bit);
        add(builder, 7, "L4_SRC_PORT", UnsignedValue::parserWith16Bit);
        add(builder, 8, "IPV4_SRC_ADDR", IPv4AddressValue::parser);
        add(builder, 9, "SRC_MASK", UnsignedValue::parserWith8Bit);
        add(builder, 10, "INPUT_SNMP", UnsignedValue::parserWith64Bit);
        add(builder, 11, "L4_DST_PORT", UnsignedValue::parserWith16Bit);
        add(builder, 12, "IPV4_DST_ADDR", IPv4AddressValue::parser);
        add(builder, 13, "DST_MASK", UnsignedValue::parserWith8Bit);
        add(builder, 14, "OUTPUT_SNMP", UnsignedValue::parserWith64Bit);
        add(builder, 15, "IPV4_NEXT_HOP", IPv4AddressValue::parser);
        add(builder, 16, "SRC_AS", UnsignedValue::parserWith64Bit);
        add(builder, 17, "DST_AS", UnsignedValue::parserWith64Bit);
        add(builder, 18, "BGP_IPV4_NEXT_HOP", IPv4AddressValue::parser);
        add(builder, 19, "MUL_DST_PKTS", UnsignedValue::parserWith64Bit);
        add(builder, 20, "MUL_DST_BYTES", UnsignedValue::parserWith64Bit);
        add(builder, 21, "LAST_SWITCHED", UnsignedValue::parserWith32Bit);
        add(builder, 22, "FIRST_SWITCHED", UnsignedValue::parserWith32Bit);
        add(builder, 23, "OUT_BYTES", UnsignedValue::parserWith64Bit);
        add(builder, 24, "OUT_PKTS", UnsignedValue::parserWith64Bit);
        // 25 reserved
        // 26 reserved
        add(builder, 27, "IPV6_SRC_ADDR", IPv6AddressValue::parser);
        add(builder, 28, "IPV6_DST_ADDR", IPv6AddressValue::parser);
        add(builder, 29, "IPV6_SRC_MASK", UnsignedValue::parserWith8Bit);
        add(builder, 30, "IPV6_DST_MASK", UnsignedValue::parserWith8Bit);
        add(builder, 31, "IPV6_FLOW_LABEL", UnsignedValue::parserWith24Bit);
        add(builder, 32, "ICMP_TYPE", UnsignedValue::parserWith16Bit);
        add(builder, 33, "MUL_IGMP_TYPE", UnsignedValue::parserWith8Bit);
        add(builder, 34, "SAMPLING_INTERVAL", UnsignedValue::parserWith32Bit);
        add(builder, 35, "SAMPLING_ALGORITHM", UnsignedValue::parserWith8Bit);
        add(builder, 36, "FLOW_ACTIVE_TIMEOUT", UnsignedValue::parserWith16Bit);
        add(builder, 37, "FLOW_INACTIVE_TIMEOUT", UnsignedValue::parserWith16Bit);
        add(builder, 38, "ENGINE_TYPE", UnsignedValue::parserWith8Bit);
        add(builder, 39, "ENGINE_ID", UnsignedValue::parserWith8Bit);
        add(builder, 40, "TOTAL_BYTES_EXP", UnsignedValue::parserWith64Bit);
        add(builder, 41, "TOTAL_PKTS_EXP", UnsignedValue::parserWith64Bit);
        add(builder, 42, "TOTAL_FLOWS_EXP", UnsignedValue::parserWith64Bit);
        // 43 reserved
        // 44 reserved
        // 45 reserved
        add(builder, 46, "MPLS_TOP_LABEL_TYPE", UnsignedValue::parserWith8Bit);
        add(builder, 47, "MPLS_TOP_LABEL_IP_ADDR", OctetArrayValue.parserWithLimits(4, 4));
        add(builder, 48, "FLOW_SAMPLER_ID", UnsignedValue::parserWith8Bit);
        add(builder, 49, "FLOW_SAMPLER_MODE", UnsignedValue::parserWith8Bit);
        add(builder, 50, "FLOW_SAMPLER_RANDOM_INTERVAL", UnsignedValue::parserWith32Bit);
        // 51 reserved
        // 52 reserved
        // 53 reserved
        // 54 reserved
        add(builder, 55, "DST_TOS", UnsignedValue::parserWith8Bit);
        add(builder, 56, "SRC_MAC", MacAddressValue::parser);
        add(builder, 57, "DST_MAC", MacAddressValue::parser);
        add(builder, 58, "SRC_VLAN", UnsignedValue::parserWith16Bit);
        add(builder, 59, "DST_VLAN", UnsignedValue::parserWith16Bit);
        add(builder, 60, "IP_PROTOCOL_VERSION", UnsignedValue::parserWith8Bit);
        add(builder, 61, "DIRECTION", UnsignedValue::parserWith8Bit);
        add(builder, 62, "IPV6_NEXT_HOP", IPv6AddressValue::parser);
        add(builder, 63, "BGP_IPV6_NEXT_HOP", IPv6AddressValue::parser);
        add(builder, 64, "IPV6_OPTION_HEADERS", UnsignedValue::parserWith32Bit);
        // 65 reserved
        // 66 reserved
        // 67 reserved
        // 68 reserved
        // 69 reserved
        add(builder, 70, "MPLS_LABEL_1", UnsignedValue::parserWith24Bit);
        add(builder, 71, "MPLS_LABEL_2", UnsignedValue::parserWith24Bit);
        add(builder, 72, "MPLS_LABEL_3", UnsignedValue::parserWith24Bit);
        add(builder, 73, "MPLS_LABEL_4", UnsignedValue::parserWith24Bit);
        add(builder, 74, "MPLS_LABEL_5", UnsignedValue::parserWith24Bit);
        add(builder, 75, "MPLS_LABEL_6", UnsignedValue::parserWith24Bit);
        add(builder, 76, "MPLS_LABEL_7", UnsignedValue::parserWith24Bit);
        add(builder, 77, "MPLS_LABEL_8", UnsignedValue::parserWith24Bit);
        add(builder, 78, "MPLS_LABEL_9", UnsignedValue::parserWith24Bit);
        add(builder, 79, "MPLS_LABEL_10", UnsignedValue::parserWith24Bit);
    }

    private void add(final ImmutableMap.Builder<InformationElementDatabase.Key, InformationElement> builder, final int fieldType, final String name, final InformationElementDatabase.ValueParserFactory valueParserFactory) {
        final Value.Parser type = valueParserFactory.parser(name);

        if (type != null) {
            builder.put(new InformationElementDatabase.Key(Optional.empty(), fieldType), new InformationElement(fieldType, name, type, Optional.empty()));
        }
    }
}
