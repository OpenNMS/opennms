/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.linkd.snmp;

import java.util.HashMap;

public class IsisISAdjTableEntry extends SnmpStore {

    public enum IsisISAdjState {
        DOWN(1),
        INITIALING(2),
        UP(3),
        FAILED(4);
        private final Integer m_value;
        private static final HashMap<Integer, IsisISAdjState> m_valueMap = new HashMap<Integer, IsisISAdjState>();
        
        static {
            for (IsisISAdjState state: IsisISAdjState.values()) {
                m_valueMap.put(state.getValue(), state);
            }
        }
        private IsisISAdjState(Integer value) {
            m_value = value;
        }
        
        public Integer getValue() {
            return m_value;
        }
        
        public static IsisISAdjState getByValue(Integer value) {
            return m_valueMap.get(value);
        }
        
    }
    public final static String ISIS_ADJ_STATE_ALIAS    = "isisISAdjState";
    public final static String ISIS_ADJ_STATE_OID       = ".1.3.6.1.2.1.138.1.6.1.1.2";
    public final static String ISIS_ADJ_NEIGH_SNPA_ADDRESS_ALIAS    = "isisISAdjNeighSNPAAddress";
    public final static String ISIS_ADJ_NEIGH_SNPA_ADDRESS_OID       = ".1.3.6.1.2.1.138.1.6.1.1.4";
    public final static String ISIS_ADJ_NEIGH_SYS_ID_ALIAS    = "isisISAdjNeighSysID";
    public final static String ISIS_ADJ_NEIGH_SYS_ID_OID       = ".1.3.6.1.2.1.138.1.6.1.1.6";
    public final static String ISIS_ADJ_NBR_EXTENDED_CIRC_ID_ALIAS    = "isisISAdjNbrExtendedCircID";
    public final static String ISIS_ADJ_NBR_EXTENDED_CIRC_ID_OID       = ".1.3.6.1.2.1.138.1.6.1.1.7";

    public static final NamedSnmpVar[] isisIsAdjtable_elemList = new NamedSnmpVar[] {
        
        /**
         * isisISAdjState OBJECT-TYPE
         * SYNTAX INTEGER
         *  {
         *        down (1),
         *        initializing (2),
         *        up (3),
         *        failed(4)
         *  }
         * MAX-ACCESS read-only
         * STATUS current
         * DESCRIPTION
         *   "The state of the adjacency."
         * REFERENCE "{ISIS.aoi adjacencyState (78)}"
         * ::= { isisISAdjEntry 2 }
         */
        new NamedSnmpVar(NamedSnmpVar.SNMPINT32, ISIS_ADJ_STATE_ALIAS, ISIS_ADJ_STATE_ALIAS, 1),
        
        new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, ISIS_ADJ_NEIGH_SNPA_ADDRESS_ALIAS, ISIS_ADJ_NEIGH_SNPA_ADDRESS_OID, 2),
        
        new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, ISIS_ADJ_NEIGH_SYS_ID_ALIAS, ISIS_ADJ_NEIGH_SYS_ID_OID, 3),
        
        new NamedSnmpVar(NamedSnmpVar.SNMPGAUGE32, ISIS_ADJ_NBR_EXTENDED_CIRC_ID_ALIAS, ISIS_ADJ_NBR_EXTENDED_CIRC_ID_OID, 4)

    };
    
    public static final String TABLE_OID = ".1.3.6.1.2.1.138.1.6.1"; // start of table (GETNEXT)
    
    public IsisISAdjTableEntry() {
        super(isisIsAdjtable_elemList);
    }

    public IsisISAdjState getIsIsAdjStatus() {
        return IsisISAdjState.getByValue(getInt32(ISIS_ADJ_STATE_ALIAS));
    }

    public String getIsIsAdjNeighSnpaAddress() {
        return getHexString(ISIS_ADJ_NEIGH_SNPA_ADDRESS_ALIAS);
    }
    
    public String getIsIsAdjNeighSysId() {
        return getDisplayString(ISIS_ADJ_NEIGH_SYS_ID_ALIAS);
    }
    
    public Integer getIsisAdjNbrExtendedCircID() {
        return getInt32(ISIS_ADJ_NBR_EXTENDED_CIRC_ID_ALIAS);
    }
}
