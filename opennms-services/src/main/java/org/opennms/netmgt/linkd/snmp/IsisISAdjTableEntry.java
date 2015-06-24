/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.linkd.snmp;

import org.opennms.netmgt.model.IsIsLink.IsisISAdjNeighSysType;
import org.opennms.netmgt.model.IsIsLink.IsisISAdjState;
import org.opennms.netmgt.snmp.NamedSnmpVar;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpStore;

public class IsisISAdjTableEntry extends SnmpStore {

    
    public final static String ISIS_ADJ_STATE_ALIAS    = "isisISAdjState";
    public final static String ISIS_ADJ_STATE_OID       = ".1.3.6.1.2.1.138.1.6.1.1.2";
    public final static String ISIS_ADJ_NEIGH_SNPA_ADDRESS_ALIAS    = "isisISAdjNeighSNPAAddress";
    public final static String ISIS_ADJ_NEIGH_SNPA_ADDRESS_OID       = ".1.3.6.1.2.1.138.1.6.1.1.4";
    public final static String ISIS_ADJ_NEIGH_SYS_TYPE_ALIAS    = "isisISAdjNeighSysType";
    public final static String ISIS_ADJ_NEIGH_SYS_TYPE_OID       = ".1.3.6.1.2.1.138.1.6.1.1.5";
    public final static String ISIS_ADJ_NEIGH_SYS_ID_ALIAS    = "isisISAdjNeighSysID";
    public final static String ISIS_ADJ_NEIGH_SYS_ID_OID       = ".1.3.6.1.2.1.138.1.6.1.1.6";
    public final static String ISIS_ADJ_NBR_EXTENDED_CIRC_ID_ALIAS    = "isisISAdjNbrExtendedCircID";
    public final static String ISIS_ADJ_NBR_EXTENDED_CIRC_ID_OID       = ".1.3.6.1.2.1.138.1.6.1.1.7";

    private boolean hasIndex = false;
    private Integer m_isisCircIndex;
    private Integer m_isisISAdjIndex;

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
        new NamedSnmpVar(NamedSnmpVar.SNMPINT32, ISIS_ADJ_STATE_ALIAS, ISIS_ADJ_STATE_OID, 1),

        /**
         *  isisISAdjNeighSNPAAddress OBJECT-TYPE
         *  SYNTAX IsisOSINSAddress
         *  MAX-ACCESS read-only
         *  STATUS current
         *  DESCRIPTION
         *  "The SNPA address of the neighboring system."
         *  REFERENCE "{ISIS.aoi neighbourSNPAAddress (79)}"
         *  ::= { isisISAdjEntry 4 }
         */
        new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, ISIS_ADJ_NEIGH_SNPA_ADDRESS_ALIAS, ISIS_ADJ_NEIGH_SNPA_ADDRESS_OID, 2),
        
        /**
         *  isisISAdjNeighSysType OBJECT-TYPE
         *          SYNTAX INTEGER
         *          {
         *          l1IntermediateSystem(1),
         *          l2IntermediateSystem(2),
         *          l1L2IntermediateSystem(3),
         *          unknown(4)
         *          }
         *          MAX-ACCESS read-only
         *          STATUS current
         *          DESCRIPTION
         *          "The type of the neighboring system."
         *          REFERENCE "{ISIS.aoi neighbourSystemType (80)}"
         *  ::= { isisISAdjEntry 5 }
         */
        new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, ISIS_ADJ_NEIGH_SYS_TYPE_ALIAS, ISIS_ADJ_NEIGH_SYS_TYPE_OID, 3),
        
        /**
         *     isisISAdjNeighSysID OBJECT-TYPE
         *     SYNTAX IsisSystemID
         *     MAX-ACCESS read-only
         *     STATUS current
         *     DESCRIPTION
         *     "The system ID of the neighboring Intermediate
         *     System."
         *     REFERENCE "{ISIS.aoi neighbourSystemIds (83)}"
         *     ::= { isisISAdjEntry 6 }
         */
        new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, ISIS_ADJ_NEIGH_SYS_ID_ALIAS, ISIS_ADJ_NEIGH_SYS_ID_OID, 4),
        
        /**
         * isisISAdjNbrExtendedCircID OBJECT-TYPE
         * SYNTAX Unsigned32
         * MAX-ACCESS read-only
         * STATUS current
         * DESCRIPTION
         *  "The 4-byte Extended Circuit ID learned from the
         *    Neighbor during 3-way handshake, or 0."
         *    ::= { isisISAdjEntry 7 }
         */
        new NamedSnmpVar(NamedSnmpVar.SNMPGAUGE32, ISIS_ADJ_NBR_EXTENDED_CIRC_ID_ALIAS, ISIS_ADJ_NBR_EXTENDED_CIRC_ID_OID, 5)

    };
    
    public static final String TABLE_OID = ".1.3.6.1.2.1.138.1.6.1"; // start of table (GETNEXT)
    
    public IsisISAdjTableEntry() {
        super(isisIsAdjtable_elemList);
    }

    public IsisISAdjState getIsIsAdjStatus() {
        return IsisISAdjState.get(getInt32(ISIS_ADJ_STATE_ALIAS));
    }

    public String getIsIsAdjNeighSnpaAddress() {
        return getHexString(ISIS_ADJ_NEIGH_SNPA_ADDRESS_ALIAS);
    }
    
    public IsisISAdjNeighSysType getIsisISAdjNeighSysType() {
        return IsisISAdjNeighSysType.get(getInt32(ISIS_ADJ_NEIGH_SYS_TYPE_ALIAS));
    }
    
    public String getIsIsAdjNeighSysId() {
        return getHexString(ISIS_ADJ_NEIGH_SYS_ID_ALIAS);
    }
    
    public Integer getIsisAdjNbrExtendedCircID() {
        return getInt32(ISIS_ADJ_NBR_EXTENDED_CIRC_ID_ALIAS);
    }
    
    public Integer getIsisCircIndex() {
        return m_isisCircIndex;
    }
    
    public Integer getIsisISAdjIndex() {
        return m_isisISAdjIndex;
    }
    
    /** {@inheritDoc} */
    @Override
    public void storeResult(SnmpResult res) {
            if (!hasIndex) {
                    m_isisCircIndex = res.getInstance().getSubIdAt(res.getInstance().length()-2);
                    m_isisISAdjIndex = res.getInstance().getLastSubId();
                    hasIndex = true;
            }
            super.storeResult(res);
    }

}
