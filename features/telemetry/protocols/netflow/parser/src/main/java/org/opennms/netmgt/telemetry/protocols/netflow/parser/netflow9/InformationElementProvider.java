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
        adder.add(Protocol.NETFLOW9, 1, UnsignedValue::parserWith64Bit, "IN_BYTES", Semantics.DELTA_COUNTER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 2, UnsignedValue::parserWith64Bit, "IN_PKTS", Semantics.DELTA_COUNTER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 3, UnsignedValue::parserWith64Bit, "FLOWS", Semantics.DELTA_COUNTER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 4, UnsignedValue::parserWith8Bit, "PROTOCOL", Semantics.IDENTIFIER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 5, UnsignedValue::parserWith8Bit, "TOS", Semantics.IDENTIFIER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 6, UnsignedValue::parserWith8Bit, "TCP_FLAGS", Semantics.FLAGS, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 7, UnsignedValue::parserWith16Bit, "L4_SRC_PORT", Semantics.IDENTIFIER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 8, IPv4AddressValue::parser, "IPV4_SRC_ADDR", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 9, UnsignedValue::parserWith8Bit, "SRC_MASK", Optional.empty(), this.getDatabase());
        adder.add(Protocol.NETFLOW9, 10, UnsignedValue::parserWith64Bit, "INPUT_SNMP", Semantics.IDENTIFIER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 11, UnsignedValue::parserWith16Bit, "L4_DST_PORT", Semantics.IDENTIFIER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 12, IPv4AddressValue::parser, "IPV4_DST_ADDR", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 13, UnsignedValue::parserWith8Bit, "DST_MASK", Optional.empty(), this.getDatabase());
        adder.add(Protocol.NETFLOW9, 14, UnsignedValue::parserWith64Bit, "OUTPUT_SNMP", Semantics.IDENTIFIER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 15, IPv4AddressValue::parser, "IPV4_NEXT_HOP", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 16, UnsignedValue::parserWith32Bit, "SRC_AS", Optional.empty(), this.getDatabase());
        adder.add(Protocol.NETFLOW9, 17, UnsignedValue::parserWith32Bit, "DST_AS", Optional.empty(), this.getDatabase());
        adder.add(Protocol.NETFLOW9, 18, IPv4AddressValue::parser, "BGP_IPV4_NEXT_HOP", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 19, UnsignedValue::parserWith64Bit, "MUL_DST_PKTS", Semantics.DELTA_COUNTER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 20, UnsignedValue::parserWith64Bit, "MUL_DST_BYTES", Semantics.DELTA_COUNTER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 21, UnsignedValue::parserWith32Bit, "LAST_SWITCHED", Optional.empty(), this.getDatabase());
        adder.add(Protocol.NETFLOW9, 22, UnsignedValue::parserWith32Bit, "FIRST_SWITCHED", Optional.empty(), this.getDatabase());
        adder.add(Protocol.NETFLOW9, 23, UnsignedValue::parserWith64Bit, "OUT_BYTES", Semantics.DELTA_COUNTER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 24, UnsignedValue::parserWith64Bit, "OUT_PKTS", Semantics.DELTA_COUNTER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 25, UnsignedValue::parserWith16Bit, "MIN_PKT_LNGTH", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 26, UnsignedValue::parserWith16Bit, "MAX_PKT_LNGTH", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 27, IPv6AddressValue::parser, "IPV6_SRC_ADDR", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 28, IPv6AddressValue::parser, "IPV6_DST_ADDR", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 29, UnsignedValue::parserWith8Bit, "IPV6_SRC_MASK", Optional.empty(), this.getDatabase());
        adder.add(Protocol.NETFLOW9, 30, UnsignedValue::parserWith8Bit, "IPV6_DST_MASK", Optional.empty(), this.getDatabase());
        adder.add(Protocol.NETFLOW9, 31, UnsignedValue::parserWith32Bit, "IPV6_FLOW_LABEL", Semantics.IDENTIFIER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 32, UnsignedValue::parserWith16Bit, "ICMP_TYPE", Semantics.IDENTIFIER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 33, UnsignedValue::parserWith8Bit, "MUL_IGMP_TYPE", Semantics.IDENTIFIER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 34, UnsignedValue::parserWith32Bit, "SAMPLING_INTERVAL", Semantics.QUANTITY, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 35, UnsignedValue::parserWith8Bit, "SAMPLING_ALGORITHM", Semantics.IDENTIFIER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 36, UnsignedValue::parserWith16Bit, "FLOW_ACTIVE_TIMEOUT", Optional.empty(), this.getDatabase());
        adder.add(Protocol.NETFLOW9, 37, UnsignedValue::parserWith16Bit, "FLOW_INACTIVE_TIMEOUT", Optional.empty(), this.getDatabase());
        adder.add(Protocol.NETFLOW9, 38, UnsignedValue::parserWith8Bit, "ENGINE_TYPE", Semantics.IDENTIFIER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 39, UnsignedValue::parserWith8Bit, "ENGINE_ID", Semantics.IDENTIFIER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 40, UnsignedValue::parserWith64Bit, "TOTAL_BYTES_EXP", Semantics.TOTAL_COUNTER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 41, UnsignedValue::parserWith64Bit, "TOTAL_PKTS_EXP", Semantics.TOTAL_COUNTER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 42, UnsignedValue::parserWith64Bit, "TOTAL_FLOWS_EXP", Semantics.TOTAL_COUNTER, this.getDatabase());
        // 43 vendor proprietary
        adder.add(Protocol.NETFLOW9, 44, UnsignedValue::parserWith32Bit, "IPV4_SRC_PREFIX", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 45, UnsignedValue::parserWith32Bit, "IPV4_DST_PREFIX", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 46, UnsignedValue::parserWith8Bit, "MPLS_TOP_LABEL_TYPE", Semantics.IDENTIFIER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 47, OctetArrayValue.parserWithLimits(4, 4), "MPLS_TOP_LABEL_IP_ADDR", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 48, UnsignedValue::parserWith32Bit, "FLOW_SAMPLER_ID", Semantics.IDENTIFIER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 49, UnsignedValue::parserWith8Bit, "FLOW_SAMPLER_MODE", Semantics.IDENTIFIER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 50, UnsignedValue::parserWith32Bit, "FLOW_SAMPLER_RANDOM_INTERVAL", Semantics.QUANTITY, this.getDatabase());
        // 51 vendor proprietary
        adder.add(Protocol.NETFLOW9, 52, UnsignedValue::parserWith8Bit, "MIN_TTL", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 53, UnsignedValue::parserWith8Bit, "MAX_TTL", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 54, UnsignedValue::parserWith32Bit, "IPV4_IDENT", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 55, UnsignedValue::parserWith8Bit, "DST_TOS", Semantics.IDENTIFIER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 56, MacAddressValue::parser, "SRC_MAC", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 57, MacAddressValue::parser, "DST_MAC", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 58, UnsignedValue::parserWith16Bit, "SRC_VLAN", Semantics.IDENTIFIER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 59, UnsignedValue::parserWith16Bit, "DST_VLAN", Semantics.IDENTIFIER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 60, UnsignedValue::parserWith8Bit, "IP_PROTOCOL_VERSION", Semantics.IDENTIFIER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 61, UnsignedValue::parserWith8Bit, "DIRECTION", Semantics.IDENTIFIER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 62, IPv6AddressValue::parser, "IPV6_NEXT_HOP", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 63, IPv6AddressValue::parser, "BGP_IPV6_NEXT_HOP", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 64, UnsignedValue::parserWith32Bit, "IPV6_OPTION_HEADERS", Semantics.FLAGS, this.getDatabase());
        // 65 vendor proprietary
        // 66 vendor proprietary
        // 67 vendor proprietary
        // 68 vendor proprietary
        // 69 vendor proprietary
        adder.add(Protocol.NETFLOW9, 70, UnsignedValue::parserWith24Bit, "MPLS_LABEL_1", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 71, UnsignedValue::parserWith24Bit, "MPLS_LABEL_2", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 72, UnsignedValue::parserWith24Bit, "MPLS_LABEL_3", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 73, UnsignedValue::parserWith24Bit, "MPLS_LABEL_4", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 74, UnsignedValue::parserWith24Bit, "MPLS_LABEL_5", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 75, UnsignedValue::parserWith24Bit, "MPLS_LABEL_6", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 76, UnsignedValue::parserWith24Bit, "MPLS_LABEL_7", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 77, UnsignedValue::parserWith24Bit, "MPLS_LABEL_8", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 78, UnsignedValue::parserWith24Bit, "MPLS_LABEL_9", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 79, UnsignedValue::parserWith24Bit, "MPLS_LABEL_10", Semantics.DEFAULT, this.getDatabase());

        adder.add(Protocol.NETFLOW9, 80, MacAddressValue::parser, "IN_DST_MAC", Semantics.IDENTIFIER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 81, MacAddressValue::parser, "OUT_SRC_MAC", Semantics.IDENTIFIER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 82, StringValue::parser, "IF_NAME", Semantics.IDENTIFIER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 83, StringValue::parser, "IF_DESC", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 84, StringValue::parser, "SAMPLER_NAME", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 85, UnsignedValue::parserWith64Bit, "IN_PERMANENT_BYTES", Semantics.TOTAL_COUNTER, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 86, UnsignedValue::parserWith64Bit, "IN_PERMANENT_PKTS", Semantics.TOTAL_COUNTER, this.getDatabase());
        // 87 vendor proprietary
        adder.add(Protocol.NETFLOW9, 88, UnsignedValue::parserWith16Bit, "FRAGMENT_OFFSET", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 89, UnsignedValue::parserWith8Bit, "FORWARDING STATUS", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 90, OctetArrayValue::parser, "MPLS PAL RD", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 91, UnsignedValue::parserWith8Bit, "MPLS PREFIX LEN", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 92, UnsignedValue::parserWith32Bit, "SRC TRAFFIC INDEX", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 93, UnsignedValue::parserWith32Bit, "DST TRAFFIC INDEX", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 94, StringValue::parser, "APPLICATION DESCRIPTION", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 95, OctetArrayValue::parser, "APPLICATION TAG", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 96, StringValue::parser, "APPLICATION NAME", Semantics.DEFAULT, this.getDatabase());
        // 97 ?
        adder.add(Protocol.NETFLOW9, 98, UnsignedValue::parserWith8Bit, "postipDiffServCodePoint", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 99, UnsignedValue::parserWith32Bit, "replication factor", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 100, OctetArrayValue::parser, "DEPRECATED", Semantics.DEFAULT, this.getDatabase());
        // 101 ?
        adder.add(Protocol.NETFLOW9, 102, UnsignedValue::parserWith32Bit, "layer2packetSectionOffset", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 103, UnsignedValue::parserWith32Bit, "layer2packetSectionSize", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 104, OctetArrayValue::parser, "layer2packetSectionData", Semantics.DEFAULT, this.getDatabase());
        // 105-127 reserved for future use by cisco

        // Cisco also supports absolute timestamps on some platforms, see NMS-13006
        adder.add(Protocol.NETFLOW9, 152, UnsignedValue::parserWith64Bit, "flowStartMilliseconds", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 153, UnsignedValue::parserWith64Bit, "flowEndMilliseconds", Semantics.DEFAULT, this.getDatabase());

        // these IEs also appear in some NF9 implementations, see NMS-14130
        adder.add(Protocol.NETFLOW9, 252, UnsignedValue::parserWith32Bit, "ingressPhysicalInterface", Semantics.DEFAULT, this.getDatabase());
        adder.add(Protocol.NETFLOW9, 253, UnsignedValue::parserWith32Bit, "egressPhysicalInterface", Semantics.DEFAULT, this.getDatabase());
    }

    private InformationElementDatabase database;

    @Override
    public InformationElementDatabase getDatabase() {
        return database;
    }

    @Override
    public void setDatabase(InformationElementDatabase database) {
        this.database = database;
    }
}
