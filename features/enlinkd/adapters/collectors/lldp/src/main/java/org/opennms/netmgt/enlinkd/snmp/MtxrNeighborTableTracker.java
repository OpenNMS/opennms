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

import org.opennms.netmgt.snmp.TableTracker;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.RowCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MtxrNeighborTableTracker extends TableTracker {

    private final static Logger LOG = LoggerFactory.getLogger(MtxrNeighborTableTracker.class);

    public final static SnmpObjId MTXR_NEIGHBOR_INTERFACE_ID = SnmpObjId.get(".1.3.6.1.4.1.14988.1.1.11.1.1.8");

    public static final SnmpObjId[] s_mtxrneiinterfaceid_elemList = new SnmpObjId[] {
            MTXR_NEIGHBOR_INTERFACE_ID
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
            return getValue(MTXR_NEIGHBOR_INTERFACE_ID).toInt();
        }

    }

    public Map<Integer, MtxrNeighborRow> getMtxrNeighborMap() {
        return mtxrNeighborMap;
    }

    private final Map<Integer, MtxrNeighborRow> mtxrNeighborMap = new HashMap<>();

    public MtxrNeighborTableTracker() {
	    super(s_mtxrneiinterfaceid_elemList);
	}

	public MtxrNeighborTableTracker(final RowCallback rowProcessor) {
        super(rowProcessor, s_mtxrneiinterfaceid_elemList);
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
        mtxrNeighborMap.put(row.getMtxrNeighborIndex(), row);
    }

    public Integer getMtxrinterfaceId(MtxrLldpRemTableTracker.MtxrLldpRemRow mtxrlldprow) {
        Integer mtxrInterfaceId = null;
        if (mtxrNeighborMap.containsKey(mtxrlldprow.getMtxrNeighborIndex())) {
            mtxrInterfaceId = mtxrNeighborMap.get(mtxrlldprow.getMtxrNeighborIndex()).getMtxrNeighborInterfaceId();
        }
       LOG.debug("getLldpLink: neiIndex {} -> interfaceId {}", mtxrlldprow.getMtxrNeighborIndex(), mtxrInterfaceId);
       return mtxrInterfaceId;
    }

}
