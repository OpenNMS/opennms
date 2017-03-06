/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class VlanType implements Comparable<VlanType>, Serializable {

    private static final long serialVersionUID = -7012640218990540145L;

    /**
     * <p>String identifiers for the enumeration of values:</p>
     */
    public static final int VLAN_TYPE_UNKNOWN = 0;
    public static final int VLAN_TYPE_VTP_ETHERNET = 1;
    public static final int VLAN_TYPE_VTP_FDDI = 2;
    public static final int VLAN_TYPE_VTP_TOKENRING = 3;
    public static final int VLAN_TYPE_VTP_FDDINET = 4;
    public static final int VLAN_TYPE_VTP_TRNET = 5;
    public static final int VLAN_TYPE_VTP_DEPRECATED = 6;
    public static final int VLAN_TYPE_EXTREME_LAYERTWO = 7;

    public static final int THREECOM_STARTING_INDEX = 7;

    public static final int VLAN_TYPE_THREECOM_vlanLayer2 = 8;
    public static final int VLAN_TYPE_THREECOM_vlanUnspecifiedProtocols = 9;
    public static final int VLAN_TYPE_THREECOM_vlanIPProtocol = 10;
    public static final int VLAN_TYPE_THREECOM_vlanIPXProtocol = 11;
    public static final int VLAN_TYPE_THREECOM_vlanAppleTalkProtocol = 12;
    public static final int VLAN_TYPE_THREECOM_vlanXNSProtocol = 13;
    public static final int VLAN_TYPE_THREECOM_vlanISOProtocol = 14;
    public static final int VLAN_TYPE_THREECOM_vlanDECNetProtocol = 15;
    public static final int VLAN_TYPE_THREECOM_vlanNetBIOSProtocol = 16;
    public static final int VLAN_TYPE_THREECOM_vlanSNAProtocol = 17;
    public static final int VLAN_TYPE_THREECOM_vlanVINESProtocol = 18;
    public static final int VLAN_TYPE_THREECOM_vlanX25Protocol = 19;
    public static final int VLAN_TYPE_THREECOM_vlanIGMPProtocol = 20;
    public static final int VLAN_TYPE_THREECOM_vlanSessionLayer = 21;
    public static final int VLAN_TYPE_THREECOM_vlanNetBeui = 22;
    public static final int VLAN_TYPE_THREECOM_vlanLayeredProtocols = 23;
    public static final int VLAN_TYPE_THREECOM_vlanIPXIIProtocol = 24;
    public static final int VLAN_TYPE_THREECOM_vlanIPX8022Protocol = 25;
    public static final int VLAN_TYPE_THREECOM_vlanIPX8023Protocol = 26;
    public static final int VLAN_TYPE_THREECOM_vlanIPX8022SNAPProtocol = 27;

    /**
     vlanLayer2 	 (1),
     vlanUnspecifiedProtocols 	 (2),
     vlanIPProtocol 	 (3),
     vlanIPXProtocol 	 (4),
     vlanAppleTalkProtocol 	 (5),
     vlanXNSProtocol 	 (6),
     vlanISOProtocol 	 (7),
     vlanDECNetProtocol 	 (8),
     vlanNetBIOSProtocol 	 (9),
     vlanSNAProtocol 	 (10),
     vlanVINESProtocol 	 (11),
     vlanX25Protocol 	 (12),
     vlanIGMPProtocol 	 (13),
     vlanSessionLayer 	 (14),
     vlanNetBeui 	 (15),
     vlanLayeredProtocols 	 (16),
     vlanIPXIIProtocol 	 (17),
     vlanIPX8022Protocol 	 (18),
     vlanIPX8023Protocol 	 (19),
     vlanIPX8022SNAPProtocol 	 (20)
     */

    private static final Integer[] s_order = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27};

    private Integer m_vlanType;

    private static final Map<Integer, String> vlanTypeMap = new HashMap<>();

    static {
        vlanTypeMap.put(0, "Unknown");
        vlanTypeMap.put(1, "Ethernet");
        vlanTypeMap.put(2, "CiscoVtp/FDDI");
        vlanTypeMap.put(3, "CiscoVtp/TokenRing");
        vlanTypeMap.put(4, "CiscoVtp/FDDINet");
        vlanTypeMap.put(5, "CiscoVtp/TRNet");
        vlanTypeMap.put(6, "CiscoVtp/Deprecated");
        vlanTypeMap.put(7, "Extreme/LayerTwo");
        vlanTypeMap.put(8, "3com/vlanLayer2");
        vlanTypeMap.put(9, "3com/vlanUnspecifiedProtocols");
        vlanTypeMap.put(10, "3com/vlanIPProtocol");
        vlanTypeMap.put(11, "3com/vlanIPXProtocol");
        vlanTypeMap.put(12, "3com/vlanAppleTalkProtocol");
        vlanTypeMap.put(13, "3com/vlanXNSProtocol");
        vlanTypeMap.put(14, "3com/vlanISOProtocol");
        vlanTypeMap.put(15, "3com/vlanDECNetProtocol");
        vlanTypeMap.put(16, "3com/vlanNetBIOSProtocol");
        vlanTypeMap.put(17, "3com/vlanSNAProtocol");
        vlanTypeMap.put(18, "3com/vlanVINESProtocol");
        vlanTypeMap.put(19, "3com/vlanX25Protocol");
        vlanTypeMap.put(20, "3com/vlanIGMPProtocol");
        vlanTypeMap.put(21, "3com/vlanSessionLayer");
        vlanTypeMap.put(22, "3com/vlanNetBeui");
        vlanTypeMap.put(23, "3com/vlanLayeredProtocols");
        vlanTypeMap.put(24, "3com/vlanIPXIIProtocol");
        vlanTypeMap.put(25, "3com/vlanIPX8022Protocol");
        vlanTypeMap.put(26, "3com/vlanIPX8023Protocol");
        vlanTypeMap.put(27, "3com/vlanIPX8022SNAPProtocol");
    }

    @SuppressWarnings("unused")
    private VlanType() {
    }

    public VlanType(Integer vlanType) {
        m_vlanType = vlanType;
    }

    public void setIntCode(Integer vlanType) {
        m_vlanType = vlanType;
    }

    @Override
    public int compareTo(VlanType o) {
        return getIndex(m_vlanType) - getIndex(o.m_vlanType);
    }

    private static int getIndex(Integer code) {
        for (int i = 0; i < s_order.length; i++) {
            if (s_order[i] == code) {
                return i;
            }
        }
        throw new IllegalArgumentException("illegal vlanType code '" + code + "'");
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof VlanType) {
            return m_vlanType.intValue() == ((VlanType) o).m_vlanType.intValue();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return String.valueOf(m_vlanType);
    }

    public static final VlanType get(Integer code) {
        if (code == null)
            return VlanType.UNKNOWN;
        switch (code) {
            case VLAN_TYPE_UNKNOWN:
                return UNKNOWN;
            case VLAN_TYPE_VTP_ETHERNET:
                return CISCO_VTP_ETHERNET;
            case VLAN_TYPE_VTP_FDDI:
                return CISCO_VTP_FDDI;
            case VLAN_TYPE_VTP_TOKENRING:
                return CISCO_VTP_TOKENRING;
            case VLAN_TYPE_VTP_FDDINET:
                return CISCO_VTP_FDDINET;
            case VLAN_TYPE_VTP_TRNET:
                return CISCO_VTP_TRNET;
            case VLAN_TYPE_VTP_DEPRECATED:
                return CISCO_VTP_DEPRECATED;
            case VLAN_TYPE_EXTREME_LAYERTWO:
                return EXTREME_LAYER2;
            case VLAN_TYPE_THREECOM_vlanLayer2:
                return THREECOM_vlanLayer2;
            case VLAN_TYPE_THREECOM_vlanUnspecifiedProtocols:
                return THREECOM_vlanUnspecifiedProtocols;
            case VLAN_TYPE_THREECOM_vlanIPProtocol:
                return THREECOM_vlanIPProtocol;
            case VLAN_TYPE_THREECOM_vlanIPXProtocol:
                return THREECOM_vlanIPXProtocol;
            case VLAN_TYPE_THREECOM_vlanAppleTalkProtocol:
                return THREECOM_vlanAppleTalkProtocol;
            case VLAN_TYPE_THREECOM_vlanXNSProtocol:
                return THREECOM_vlanXNSProtocol;
            case VLAN_TYPE_THREECOM_vlanISOProtocol:
                return THREECOM_vlanISOProtocol;
            case VLAN_TYPE_THREECOM_vlanDECNetProtocol:
                return THREECOM_vlanDECNetProtocol;
            case VLAN_TYPE_THREECOM_vlanNetBIOSProtocol:
                return THREECOM_vlanNetBIOSProtocol;
            case VLAN_TYPE_THREECOM_vlanSNAProtocol:
                return THREECOM_vlanSNAProtocol;
            case VLAN_TYPE_THREECOM_vlanVINESProtocol:
                return THREECOM_vlanVINESProtocol;
            case VLAN_TYPE_THREECOM_vlanX25Protocol:
                return THREECOM_vlanX25Protocol;
            case VLAN_TYPE_THREECOM_vlanIGMPProtocol:
                return THREECOM_vlanIGMPProtocol;
            case VLAN_TYPE_THREECOM_vlanSessionLayer:
                return THREECOM_vlanSessionLayer;
            case VLAN_TYPE_THREECOM_vlanNetBeui:
                return THREECOM_vlanNetBeui;
            case VLAN_TYPE_THREECOM_vlanLayeredProtocols:
                return THREECOM_vlanLayeredProtocols;
            case VLAN_TYPE_THREECOM_vlanIPXIIProtocol:
                return THREECOM_vlanIPXIIProtocol;
            case VLAN_TYPE_THREECOM_vlanIPX8022Protocol:
                return THREECOM_vlanIPX8022Protocol;
            case VLAN_TYPE_THREECOM_vlanIPX8023Protocol:
                return THREECOM_vlanIPX8023Protocol;
            case VLAN_TYPE_THREECOM_vlanIPX8022SNAPProtocol:
                return THREECOM_vlanIPX8022SNAPProtocol;
            default:
                throw new IllegalArgumentException("Cannot create vlanType from code " + code);
        }
    }

    /**
     * <p>getVlanTypeString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    /**
     */
    public static String getVlanTypeString(Integer code) {
        if (vlanTypeMap.containsKey(code))
            return vlanTypeMap.get(code);
        return null;
    }

    public static final VlanType UNKNOWN = new VlanType(VLAN_TYPE_UNKNOWN);
    public static final VlanType CISCO_VTP_ETHERNET = new VlanType(VLAN_TYPE_VTP_ETHERNET);
    public static final VlanType CISCO_VTP_FDDI = new VlanType(VLAN_TYPE_VTP_FDDI);
    public static final VlanType CISCO_VTP_TOKENRING = new VlanType(VLAN_TYPE_VTP_TOKENRING);
    public static final VlanType CISCO_VTP_FDDINET = new VlanType(VLAN_TYPE_VTP_FDDINET);
    public static final VlanType CISCO_VTP_TRNET = new VlanType(VLAN_TYPE_VTP_TRNET);
    public static final VlanType CISCO_VTP_DEPRECATED = new VlanType(VLAN_TYPE_VTP_DEPRECATED);
    public static final VlanType EXTREME_LAYER2 = new VlanType(VLAN_TYPE_EXTREME_LAYERTWO);
    public static final VlanType THREECOM_vlanLayer2 = new VlanType(VLAN_TYPE_THREECOM_vlanLayer2);
    public static final VlanType THREECOM_vlanUnspecifiedProtocols = new VlanType(VLAN_TYPE_THREECOM_vlanUnspecifiedProtocols);
    public static final VlanType THREECOM_vlanIPProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanIPProtocol);
    public static final VlanType THREECOM_vlanIPXProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanIPXProtocol);
    public static final VlanType THREECOM_vlanAppleTalkProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanAppleTalkProtocol);
    public static final VlanType THREECOM_vlanXNSProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanXNSProtocol);
    public static final VlanType THREECOM_vlanISOProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanISOProtocol);
    public static final VlanType THREECOM_vlanDECNetProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanDECNetProtocol);
    public static final VlanType THREECOM_vlanNetBIOSProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanNetBIOSProtocol);
    public static final VlanType THREECOM_vlanSNAProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanSNAProtocol);
    public static final VlanType THREECOM_vlanVINESProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanVINESProtocol);
    public static final VlanType THREECOM_vlanX25Protocol = new VlanType(VLAN_TYPE_THREECOM_vlanX25Protocol);
    public static final VlanType THREECOM_vlanIGMPProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanIGMPProtocol);
    public static final VlanType THREECOM_vlanSessionLayer = new VlanType(VLAN_TYPE_THREECOM_vlanSessionLayer);
    public static final VlanType THREECOM_vlanNetBeui = new VlanType(VLAN_TYPE_THREECOM_vlanNetBeui);
    public static final VlanType THREECOM_vlanLayeredProtocols = new VlanType(VLAN_TYPE_THREECOM_vlanLayeredProtocols);
    public static final VlanType THREECOM_vlanIPXIIProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanIPXIIProtocol);
    public static final VlanType THREECOM_vlanIPX8022Protocol = new VlanType(VLAN_TYPE_THREECOM_vlanIPX8022Protocol);
    public static final VlanType THREECOM_vlanIPX8023Protocol = new VlanType(VLAN_TYPE_THREECOM_vlanIPX8023Protocol);
    public static final VlanType THREECOM_vlanIPX8022SNAPProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanIPX8022SNAPProtocol);
}
