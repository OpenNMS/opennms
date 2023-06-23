/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.snmp;

import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.TableTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MtxrNeighborTableTracker extends TableTracker {

    private final static Logger LOG = LoggerFactory.getLogger(MtxrNeighborTableTracker.class);

    /*
        mtxrNeighborIndex OBJECT-TYPE
            SYNTAX ObjectIndex
            MAX-ACCESS not-accessible
            STATUS current
            DESCRIPTION ""
            ::= { mtxrNeighborTableEntry 1 }

        mtxrNeighborIpAddress OBJECT-TYPE
            SYNTAX IpAddress
            MAX-ACCESS read-only
            STATUS current
            DESCRIPTION ""
            ::= { mtxrNeighborTableEntry 2 }

        mtxrNeighborMacAddress OBJECT-TYPE
            SYNTAX MacAddress
            MAX-ACCESS read-only
            STATUS current
            DESCRIPTION ""
            ::= { mtxrNeighborTableEntry 3 }

        mtxrNeighborVersion OBJECT-TYPE
            SYNTAX DisplayString
            MAX-ACCESS read-only
            STATUS current
            DESCRIPTION ""
            ::= { mtxrNeighborTableEntry 4 }

        mtxrNeighborPlatform OBJECT-TYPE
            SYNTAX DisplayString
            MAX-ACCESS read-only
            STATUS current
            DESCRIPTION ""
            ::= { mtxrNeighborTableEntry 5 }

        mtxrNeighborIdentity OBJECT-TYPE
            SYNTAX DisplayString
            MAX-ACCESS read-only
            STATUS current
            DESCRIPTION ""
            ::= { mtxrNeighborTableEntry 6 }

        mtxrNeighborSoftwareID OBJECT-TYPE
            SYNTAX DisplayString
            MAX-ACCESS read-only
            STATUS current
            DESCRIPTION ""
            ::= { mtxrNeighborTableEntry 7 }

        mtxrNeighborInterfaceID OBJECT-TYPE
            SYNTAX ObjectIndex
            MAX-ACCESS read-only
            STATUS current
            DESCRIPTION ""
            ::= { mtxrNeighborTableEntry 8 }
     */

    public final static SnmpObjId MTXR_NEIGHBOR_INTERFACE_ID_OID = SnmpObjId.get(".1.3.6.1.4.1.14988.1.1.11.1.1.8");
    public final static String MTXR_NEIGHBOR_INTERFACE_ID = "mtxrNeighborInterfaceID";

    public static final SnmpObjId[] s_mtxrneiinterfaceid_elemList = new SnmpObjId[] {
            MTXR_NEIGHBOR_INTERFACE_ID_OID
    };

    public static class MtxrNeighborRow extends SnmpRowResult {
        public MtxrNeighborRow(int columnCount, SnmpInstId instance) {
            super(columnCount, instance);
            LOG.debug( "column count = {}, instance = {}", columnCount, instance);
        }

        public Integer getMtxrNeighborIndex() {
            return getInstance().getSubIdAt(0);
        }

        public Integer getMtxrNeighborInterfaceId() {
            return getValue(MTXR_NEIGHBOR_INTERFACE_ID_OID).toInt();
        }

    }

    public MtxrNeighborTableTracker() {
	    super(s_mtxrneiinterfaceid_elemList);
	}

    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new MtxrNeighborRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processMtxrIndexPortRow((MtxrNeighborRow)row);
    }

    public void processMtxrIndexPortRow(final MtxrNeighborRow row) {
        System.out.printf("\t\t%s (%s)= %s \n", MTXR_NEIGHBOR_INTERFACE_ID_OID + "." + row.getInstance().toString(), MTXR_NEIGHBOR_INTERFACE_ID, row.getMtxrNeighborInterfaceId());
    }

}
