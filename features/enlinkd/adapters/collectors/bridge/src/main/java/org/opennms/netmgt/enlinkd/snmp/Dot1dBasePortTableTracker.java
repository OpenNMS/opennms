/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.enlinkd.snmp;

import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.TableTracker;

public class Dot1dBasePortTableTracker extends TableTracker {
	public final static SnmpObjId DOT1D_BASE_PORT_OID = SnmpObjId.get(".1.3.6.1.2.1.17.1.4.1.1");
	public final static SnmpObjId DOT1D_BASE_IFINDEX_OID = SnmpObjId.get(".1.3.6.1.2.1.17.1.4.1.2");
	public final static String DOT1D_BASE_IFINDEX = "dot1dBasePortIfIndex";
	public final static String DOT1D_BASE_PORT = "dot1dBasePort";

	public static final SnmpObjId[] bridgePort_elemList = new SnmpObjId[] {
		/*
		 * The port number of the port for which this entry
 		 * contains bridge management information.
		 */
			DOT1D_BASE_PORT_OID,
		
		/*
		 * The value of the instance of the ifIndex object,
		 * defined in MIB-II, for the interface corresponding
 		 * to this port.
		 */
			DOT1D_BASE_IFINDEX_OID
		
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
			return getValue(DOT1D_BASE_PORT_OID).toInt();
		}
	
		/**
		 * <p>getBaseBridgePortIfindex</p>
		 *
		 * @return a Integer.
		 */
		public Integer getBaseBridgePortIfindex() {
			return getValue(DOT1D_BASE_IFINDEX_OID).toInt();
		}
	
	}	

	public Dot1dBasePortTableTracker() {
		super(bridgePort_elemList);
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

    public void processDot1dBasePortRow(final Dot1dBasePortRow row) {
		System.out.printf("\t\t%s (%s)= %s \n", DOT1D_BASE_PORT_OID + "." + row.getInstance().toString(), DOT1D_BASE_PORT, row.getBaseBridgePort() );
		System.out.printf("\t\t%s (%s)= %s \n", DOT1D_BASE_IFINDEX_OID + "." + row.getInstance().toString(), DOT1D_BASE_IFINDEX, row.getBaseBridgePortIfindex() );
	}


}
