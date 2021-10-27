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

package org.opennms.netmgt.enlinkd.snmp;

import org.opennms.netmgt.snmp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MikrotikMtxrIndexTableTracker extends TableTracker {

    private final static Logger LOG = LoggerFactory.getLogger(MikrotikMtxrIndexTableTracker.class);

    public final static SnmpObjId MTXR_NEIGHBOR_MAC_ADDRESS =SnmpObjId.get(".1.3.6.1.4.1.14988.1.1.11.1.1.3");
    public final static SnmpObjId MTXR_NEIGHBOR_IDENTITY =SnmpObjId.get(".1.3.6.1.4.1.14988.1.1.11.1.1.6");
    public final static SnmpObjId MTXR_NEIGHBOR_INTERFACE_ID = SnmpObjId.get(".1.3.6.1.4.1.14988.1.1.11.1.1.8");

    public static final SnmpObjId[] s_mtxrneiinterfaceid_elemList = new SnmpObjId[] {
            MTXR_NEIGHBOR_MAC_ADDRESS,
            MTXR_NEIGHBOR_IDENTITY,
            MTXR_NEIGHBOR_INTERFACE_ID
    };

    public static class MtxrIndexPortRow extends SnmpRowResult {
        public MtxrIndexPortRow(int columnCount, SnmpInstId instance) {
            super(columnCount, instance);
            LOG.debug( "column count = {}, instance = {}", columnCount, instance);
        }

        public Integer getMtxrNeighborIndex() {
            return getInstance().getSubIdAt(0);
        }

        public Integer getMtxrNeighborInterfaceId() {
            return getValue(MTXR_NEIGHBOR_INTERFACE_ID).toInt();
        }

        public String getMtxrNeighborIdentity() {
            return getValue(MTXR_NEIGHBOR_IDENTITY).toDisplayString();
        }

        public String getMtxrMacAddress() {
            return LldpLocalGroupTracker.getDisplayable(getValue(MTXR_NEIGHBOR_MAC_ADDRESS));
        }



    }

    private final Map<Integer, MtxrIndexPortRow> mikrotikIndexTable = new HashMap<>();

    public MikrotikMtxrIndexTableTracker() {
	    super(s_mtxrneiinterfaceid_elemList);
	}

	public MikrotikMtxrIndexTableTracker(final RowCallback rowProcessor) {
        super(rowProcessor,s_mtxrneiinterfaceid_elemList);
    }

    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new MtxrIndexPortRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processMtxrIndexPortRow((MtxrIndexPortRow)row);
    }

    public void processMtxrIndexPortRow(final MtxrIndexPortRow row) {
        mikrotikIndexTable.put(row.getMtxrNeighborIndex(),row);
    }

    public MikrotikLldpLink getLldpLink(MikrotikLldpLink mtxrlldplink) {
       if (mikrotikIndexTable.containsKey(mtxrlldplink.getMtxrNeighborIndex())) {
            mtxrlldplink.setMtxrIndex(mikrotikIndexTable.get(mtxrlldplink.getMtxrNeighborIndex()).getMtxrNeighborInterfaceId());
        }
        return mtxrlldplink;
    }

}
