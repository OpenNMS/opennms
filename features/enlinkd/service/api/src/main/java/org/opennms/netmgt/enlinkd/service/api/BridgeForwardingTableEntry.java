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
package org.opennms.netmgt.enlinkd.service.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BridgeForwardingTableEntry implements Topology {

    private Integer m_node;
    private Integer m_bridgePort;
    private Integer m_bridgePortIfIndex;
    private String m_macAddress;
    private Integer m_vlan;
    private BridgeDot1qTpFdbStatus m_status;

    /**
     * dot1qTpFdbStatus OBJECT-TYPE SYNTAX INTEGER { other(1), invalid(2),
     * learned(3), self(4), mgmt(5) } MAX-ACCESS read-only STATUS current
     * DESCRIPTION "The status of this entry. The meanings of the values are:
     * other(1) - none of the following. This may include the case where some
     * other MIB object (not the corresponding instance of dot1qTpFdbPort, nor
     * an entry in the dot1qStaticUnicastTable) is being used to determine if
     * and how frames addressed to the value of the corresponding instance of
     * dot1qTpFdbAddress are being forwarded. invalid(2) - this entry is no
     * longer valid (e.g., it was learned but has since aged out), but has not
     * yet been flushed from the table. learned(3) - the value of the
     * corresponding instance of dot1qTpFdbPort was learned and is being used.
     * self(4) - the value of the corresponding instance of dot1qTpFdbAddress
     * represents one of the device's addresses. The corresponding instance of
     * dot1qTpFdbPort indicates which of the device's ports has this address.
     * mgmt(5) - the value of the corresponding instance of dot1qTpFdbAddress
     * is also the value of an existing instance of dot1qStaticAddress."
     */
    public enum BridgeDot1qTpFdbStatus {
        DOT1D_TP_FDB_STATUS_OTHER(1), DOT1D_TP_FDB_STATUS_INVALID(2), DOT1D_TP_FDB_STATUS_LEARNED(
                3), DOT1D_TP_FDB_STATUS_SELF(4), DOT1D_TP_FDB_STATUS_MGMT(5);

        private final int m_type;

        BridgeDot1qTpFdbStatus(int type) {
            m_type = type;
        }

        static final Map<Integer, String> s_typeMap = new HashMap<>();

        static {
            s_typeMap.put(1, "other");
            s_typeMap.put(2, "invalid");
            s_typeMap.put(3, "learned");
            s_typeMap.put(4, "self");
            s_typeMap.put(5, "mgmt");
        }

        public static String getTypeString(Integer code) {
            if (s_typeMap.containsKey(code))
                return s_typeMap.get(code);
            return "other-vendor-specific";
        }

        public Integer getValue() {
            return m_type;
        }

        public static BridgeDot1qTpFdbStatus get(Integer code) {
            if (code == null)
                throw new IllegalArgumentException(
                                                   "Cannot create BridgeDot1qTpFdbStatus from null code");
            if (code <= 0)
                throw new IllegalArgumentException(
                                                   "Cannot create BridgeDot1qTpFdbStatus from"
                                                           + code + " code");
            switch (code) {
            case 1:
                return DOT1D_TP_FDB_STATUS_OTHER;
            case 2:
                return DOT1D_TP_FDB_STATUS_INVALID;
            case 3:
                return DOT1D_TP_FDB_STATUS_LEARNED;
            case 4:
                return DOT1D_TP_FDB_STATUS_SELF;
            case 5:
                return DOT1D_TP_FDB_STATUS_MGMT;
            default:
                throw new IllegalArgumentException(
                                                   "Cannot create BridgeDot1qTpFdbStatus from code "
                                                           + code);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BridgeForwardingTableEntry that = (BridgeForwardingTableEntry) o;
        return Objects.equals(m_node, that.m_node) &&
                Objects.equals(m_bridgePort, that.m_bridgePort) &&
                Objects.equals(m_macAddress, that.m_macAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_node, m_bridgePort, m_macAddress);
    }

    public Integer getNodeId() {
        return m_node;
    }

    public void setNodeId(Integer node) {
        m_node = node;
    }

    public Integer getBridgePort() {
        return m_bridgePort;
    }

    public void setBridgePort(Integer bridgePort) {
        m_bridgePort = bridgePort;
    }

    public Integer getBridgePortIfIndex() {
        return m_bridgePortIfIndex;
    }

    public void setBridgePortIfIndex(Integer bridgePortIfIndex) {
        m_bridgePortIfIndex = bridgePortIfIndex;
    }

    public String getMacAddress() {
        return m_macAddress;
    }

    public void setMacAddress(String macAddress) {
        m_macAddress = macAddress;
    }

    public Integer getVlan() {
        return m_vlan;
    }

    public void setVlan(Integer vlan) {
        m_vlan = vlan;
    }

    public BridgeDot1qTpFdbStatus getBridgeDot1qTpFdbStatus() {
        return m_status;
    }

    public void setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus status) {
        m_status = status;
    }

    public String printTopology() {
        StringBuilder strbfr = new StringBuilder();

        strbfr.append("[");
        strbfr.append(getMacAddress());
        strbfr.append(", bridge:[");
        strbfr.append(getNodeId());
        strbfr.append("], bridgeport:");
        strbfr.append(getBridgePort());
        strbfr.append(", ifindex:");
        strbfr.append(getBridgePortIfIndex());
        strbfr.append(", vlan:");
        strbfr.append(getVlan());
        if (getBridgeDot1qTpFdbStatus() != null) {
            strbfr.append(", status:");
            strbfr.append(BridgeDot1qTpFdbStatus.getTypeString(getBridgeDot1qTpFdbStatus().getValue()));
        }
        strbfr.append("]");
        return strbfr.toString();
    }

}
