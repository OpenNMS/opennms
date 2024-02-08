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
package org.opennms.netmgt.collectd.tca;

import java.net.InetAddress;

import org.opennms.netmgt.capsd.snmp.SnmpTable;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

/**
 * The Class TcaData.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public final class TcaData extends SnmpTable<TcaDataEntry> {
	
	/**
	 * Instantiates a new TCA data.
	 *
	 * @param address the address
	 */
	protected TcaData(InetAddress address) {
		super(address, "jnxTcaSlaTable", TcaDataEntry.tca_elemList);
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.capsd.snmp.SnmpTable#createTableEntry(org.opennms.netmgt.snmp.SnmpObjId, org.opennms.netmgt.snmp.SnmpInstId, java.lang.Object)
	 */
	@Override
	protected TcaDataEntry createTableEntry(SnmpObjId base, SnmpInstId inst, Object val) {
		return new TcaDataEntry(inst);
	}

}
