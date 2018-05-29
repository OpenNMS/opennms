/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.topology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.BridgeMacLink.BridgeMacLinkType;

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

        private int m_type;

        BridgeDot1qTpFdbStatus(int type) {
            m_type = type;
        }

        protected static final Map<Integer, String> s_typeMap = new HashMap<Integer, String>();

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
            if (code.intValue() <= 0)
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

    public static String printTopology(Set<BridgeForwardingTableEntry> bft) {
        StringBuffer strbfr = new StringBuffer();
        boolean rn = false;
        for (BridgeForwardingTableEntry bftentry: bft) {
            if (rn) {
                strbfr.append("\n");
            } else {
                rn = true;
            }
            strbfr.append(bftentry.printTopology());
        }
        return strbfr.toString();
    }

    public static List<BridgeMacLink> create(BridgePortWithMacs bft, BridgeMacLinkType type) {
        return create(bft.getPort(), bft.getMacs(), type);
    }
    
    public static List<BridgeMacLink> create(BridgePort bp, Set<String> macs, BridgeMacLinkType type) {
        List<BridgeMacLink> maclinks = new ArrayList<BridgeMacLink>();
        macs.stream().forEach(mac -> maclinks.add(create(bp, mac, type)));
        return maclinks;
    }

    public static BridgeMacLink create(BridgePort bp, String macAddress, BridgeMacLinkType type) {
        BridgeMacLink maclink = new BridgeMacLink();
        OnmsNode node = new OnmsNode();
        node.setId(bp.getNodeId());
        maclink.setNode(node);
        maclink.setBridgePort(bp.getBridgePort());
        maclink.setBridgePortIfIndex(bp.getBridgePortIfIndex());
        maclink.setMacAddress(macAddress);
        maclink.setVlan(bp.getVlan());
        maclink.setLinkType(type);
        return maclink;
    }
    
    public static List<BridgeBridgeLink> create(BridgePort designatedPort, Set<BridgePort> ports) {
        OnmsNode designatedNode = new OnmsNode();
        designatedNode.setId(designatedPort.getNodeId());
        List<BridgeBridgeLink> links = new ArrayList<BridgeBridgeLink>();
        for (BridgePort port:ports) {
            if (port.equals(designatedPort)) {
                continue;
            }
            BridgeBridgeLink link = new BridgeBridgeLink();
            OnmsNode node = new OnmsNode();
            node.setId(port.getNodeId());
            link.setNode(node);
            link.setBridgePort(port.getBridgePort());
            link.setBridgePortIfIndex(port.getBridgePortIfIndex());
            link.setVlan(port.getVlan());
            link.setDesignatedNode(designatedNode);
            link.setDesignatedPort(designatedPort.getBridgePort());
            link.setDesignatedPortIfIndex(designatedPort.getBridgePortIfIndex());
            link.setDesignatedVlan(designatedPort.getVlan());
            links.add(link);
        }
        return links;
    
    }

    public static Set<BridgeForwardingTableEntry> get(BridgePortWithMacs bft) {
        Set<BridgeForwardingTableEntry> bftentries = new HashSet<BridgeForwardingTableEntry>();
        bft.getMacs().stream().forEach(mac -> {
            BridgeForwardingTableEntry bftentry = new BridgeForwardingTableEntry();
            bftentry.setNodeId(bft.getPort().getNodeId());
            bftentry.setBridgePort(bft.getPort().getBridgePort());
            bftentry.setBridgePortIfIndex(bft.getPort().getBridgePortIfIndex());
            bftentry.setVlan(bft.getPort().getVlan());
            bftentry.setMacAddress(mac);
            bftentry.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
            bftentries.add(bftentry);
        });
        return bftentries;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((m_bridgePort == null) ? 0 : m_bridgePort.hashCode());
        result = prime * result
                + ((m_macAddress == null) ? 0 : m_macAddress.hashCode());
        result = prime * result + ((m_node == null) ? 0 : m_node.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BridgeForwardingTableEntry other = (BridgeForwardingTableEntry) obj;
        if (m_bridgePort == null) {
            if (other.m_bridgePort != null)
                return false;
        } else if (!m_bridgePort.equals(other.m_bridgePort))
            return false;
        if (m_macAddress == null) {
            if (other.m_macAddress != null)
                return false;
        } else if (!m_macAddress.equals(other.m_macAddress))
            return false;
        if (m_node == null) {
            if (other.m_node != null)
                return false;
        } else if (!m_node.equals(other.m_node))
            return false;
        return true;
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
        StringBuffer strbfr = new StringBuffer();

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
