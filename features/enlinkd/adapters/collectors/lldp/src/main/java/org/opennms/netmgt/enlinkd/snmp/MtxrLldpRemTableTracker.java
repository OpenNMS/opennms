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

public class MtxrLldpRemTableTracker extends LldpRemTableTracker {

    public MtxrLldpRemTableTracker() {
        super();
    }

    public static class MtxrLldpRemRow extends LldpRemRow {

        public MtxrLldpRemRow(int columnCount, SnmpInstId instance) {
            super(columnCount, instance);
        }

        public Integer getMtxrNeighborIndex() {
            return super.getLldpRemIndex();
        }

    }

    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new MtxrLldpRemRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processMtxrLldpRemRow((MtxrLldpRemRow)row);
    }

    /**
     * <p>processMtxrLldpRemRow</p>
     *
     * @param row a {@link org.opennms.netmgt.enlinkd.snmp.LldpRemTableTracker.LldpRemRow} object.
     */
    public void processMtxrLldpRemRow(final MtxrLldpRemRow row) {
    }


}
