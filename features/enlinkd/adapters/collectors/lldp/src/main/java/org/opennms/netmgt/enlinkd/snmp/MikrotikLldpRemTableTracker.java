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
import org.opennms.netmgt.snmp.SnmpRowResult;

public class MikrotikLldpRemTableTracker extends LldpRemTableTracker {

    public MikrotikLldpRemTableTracker() {
        super();
    }

    public static class MikrotikLldpRemRow extends LldpRemRow {

        public MikrotikLldpRemRow(int columnCount, SnmpInstId instance) {
            super(columnCount, instance);
        }

        public Integer getMtxrNeighborIndex() {
            return getInstance().getSubIdAt(2);
        }

        public MikrotikLldpLink getMtxrLldpLink() {
            MikrotikLldpLink mtxrLink = new MikrotikLldpLink();
            mtxrLink.setMtxrNeighborIndex(getMtxrNeighborIndex());
            mtxrLink.setLldpLink(getLldpLink());
            return mtxrLink;
        }


    }

    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new MikrotikLldpRemRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processLldpRemRow((MikrotikLldpRemRow)row);
    }

    /**
     * <p>processLldpRemRow</p>
     *
     * @param row a {@link org.opennms.netmgt.enlinkd.snmp.LldpRemTableTracker.LldpRemRow} object.
     */
    public void processLldpRemRow(final MikrotikLldpRemRow row) {
    }


}
