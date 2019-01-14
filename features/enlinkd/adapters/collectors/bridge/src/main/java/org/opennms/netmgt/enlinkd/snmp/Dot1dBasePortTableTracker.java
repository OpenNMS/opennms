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

import org.opennms.netmgt.snmp.RowCallback;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.TableTracker;

public class Dot1dBasePortTableTracker extends TableTracker {
	public final static SnmpObjId DOT1D_BASE_PORT = SnmpObjId.get(".1.3.6.1.2.1.17.1.4.1.1");
	public final static SnmpObjId DOT1D_BASE_IFINDEX = SnmpObjId.get(".1.3.6.1.2.1.17.1.4.1.2");

	public static final SnmpObjId[] bridgePort_elemList = new SnmpObjId[] {
		/**
		 * The port number of the port for which this entry
 		 * contains bridge management information.
		 */
		DOT1D_BASE_PORT,
		
		/**
		 * The value of the instance of the ifIndex object,
		 * defined in MIB-II, for the interface corresponding
 		 * to this port.
		 */
		DOT1D_BASE_IFINDEX
		
	};

	public static class Dot1dBasePortRow extends SnmpRowResult {

		public Dot1dBasePortRow(int columnCount, SnmpInstId instance) {
			super(columnCount, instance);
		}

		/**
		 * <p>getBaseBridgePort</p>
		 *
		 * @return a Integer.
		 */
		public Integer getBaseBridgePort() {
			return getValue(DOT1D_BASE_PORT).toInt();
		}
	
		/**
		 * <p>getBaseBridgePortIfindex</p>
		 *
		 * @return a Integer.
		 */
		public Integer getBaseBridgePortIfindex() {
			return getValue(DOT1D_BASE_IFINDEX).toInt();
		}
	
	}	

	public Dot1dBasePortTableTracker() {
		super(bridgePort_elemList);
	}
	
	public Dot1dBasePortTableTracker(RowCallback rowProcessor) {
		super(rowProcessor, bridgePort_elemList);
	}

    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new Dot1dBasePortRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processDot1dBasePortRow((Dot1dBasePortRow)row);
    }

    /**
     * <p>processDot1DBasePortRow</p>
     *
     * @param row a {@link org.opennms.netmgt.enlinkd.Dot1dBasePortRow.Dot1DBasePortRow} object.
     */
    public void processDot1dBasePortRow(final Dot1dBasePortRow row) {
    }


}
